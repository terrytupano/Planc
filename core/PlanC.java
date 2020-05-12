/*******************************************************************************
 * Copyright (C) 2017 terry.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     terry - initial API and implementation
 ******************************************************************************/
package core;

import gui.*;
import gui.docking.*;
import gui.wlaf.*;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;

import net.infonode.docking.*;

import org.apache.commons.logging.impl.*;
import org.jdesktop.core.animation.timing.*;
import org.jdesktop.swing.animation.timing.sources.*;

import plugin.planc.*;
import action.*;

import com.alee.extended.layout.*;
import com.alee.extended.panel.*;
import com.alee.extended.window.*;
import com.alee.laf.*;
import com.alee.laf.button.*;
import com.alee.laf.label.*;
import com.alee.laf.rootpane.*;
import com.alee.managers.notification.*;

import core.datasource.*;
import core.download.*;
import core.tasks.*;

/**
 * Entrada aplicacion
 * 
 * @author Terry
 */
public class PlanC {

	public static final int SIGNIN = 0;
	public static final int DOCKING = 1;
	public static final int HELP = 2;
	public static final int CHANGE_PASSWORD = 3;
	public static final int CONSOLE = 4;

	private static int actualContent;
	public static final String LOG_FILE = "_.log";
	public static Logger logger;

	public static String RUNNING_MODE;
	public static final String RM_NORMAL = "Normal";
	public static final String RM_CONSOLE = "Console";
	public static final String ONE_TASK = "oneTask";

	public static TWebFrame frame;
	private static JComponent actPane;
	private static JMenuBar menuBar;
	private static AudioClip newMsg;
	private static Rectangle oldBound;

	/**
	 * inicio de aplicacion
	 * 
	 * @param arg - argumentos de entrada
	 */
	public static void main(String[] args) {

		// user.name

		/*
		 * Properties prp = System.getProperties(); System.out.println(getWmicValue("bios", "SerialNumber"));
		 * System.out.println(getWmicValue("cpu", "SystemName"));
		 */

		try {
			// log
			// -Djava.util.logging.SimpleFormatter.format='%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s %2$s %5$s%6$s%n'
			// System.setProperty("java.util.logging.SimpleFormatter.format",
			// "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s %2$s %5$s%6$s%n");
			System.setProperty("java.util.logging.SimpleFormatter.format",
					"%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %5$s%6$s%n");

			FileHandler fh = new FileHandler(LOG_FILE);
			fh.setFormatter(new SimpleFormatter());
			fh.setLevel(Level.INFO);
			ConsoleHandler ch = new ConsoleHandler();
			ch.setFormatter(new SimpleFormatter());
			ch.setLevel(Level.INFO);
			logger = Logger.getLogger("");
			Handler[] hs = logger.getHandlers();
			for (int x = 0; x < hs.length; x++) {
				logger.removeHandler(hs[x]);
			}
			logger.addHandler(fh);
			logger.addHandler(ch);
			// point apache log to this log
			System.setProperty("org.apache.commons.logging.Log", Jdk14Logger.class.getName());

			TPreferences.init();
			TStringUtils.init();

			Font fo = Font.createFont(Font.TRUETYPE_FONT, TResourceUtils.getFile("Dosis-Light.ttf"));
			GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(fo);
			fo = Font.createFont(Font.TRUETYPE_FONT, TResourceUtils.getFile("Dosis-Medium.ttf"));
			GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(fo);
			fo = Font.createFont(Font.TRUETYPE_FONT, TResourceUtils.getFile("AERO_ITALIC.ttf"));
			GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(fo);

			SwingTimerTimingSource ts = new SwingTimerTimingSource();
			AnimatorBuilder.setDefaultTimingSource(ts);
			ts.init();

			// parse app argument parameters and append to tpreferences to futher uses
			for (String arg : args) {
				String[] kv = arg.split("=");
				TPreferences.setProperty(kv[0], kv[1]);
			}
			RUNNING_MODE = TPreferences.getProperty("runningMode", RM_NORMAL);

			newMsg = Applet.newAudioClip(TResourceUtils.getURL("newMsg.wav"));

		} catch (Exception e) {
			SystemLog.logException1(e, true);
		}

		// pass icon from metal to web look and feel
		Icon i1 = UIManager.getIcon("OptionPane.errorIcon");
		Icon i2 = UIManager.getIcon("OptionPane.informationIcon");
		Icon i3 = UIManager.getIcon("OptionPane.questionIcon");
		Icon i4 = UIManager.getIcon("OptionPane.warningIcon");
		// Object fcui = UIManager.get("FileChooserUI");
		// JFileChooser fc = new JFileChooser();

		WebLookAndFeel.install();
		// WebLookAndFeel.setDecorateFrames(true);
		// WebLookAndFeel.setDecorateDialogs(true);

		UIManager.put("OptionPane.errorIcon", i1);
		UIManager.put("OptionPane.informationIcon", i2);
		UIManager.put("OptionPane.questionIcon", i3);
		UIManager.put("OptionPane.warningIcon", i4);
		// UIManager.put("TFileChooserUI", fcui);

		// warm up the IDW.
		// in my computer, some weird error ocurr if i don't execute this preload.
		new RootWindow(null);

		frame = new TWebFrame();
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				Exit.shutdown();
			}
		});

		if (RUNNING_MODE.equals(RM_NORMAL)) {
			initEnviorement();
		}
		if (RUNNING_MODE.equals(RM_CONSOLE)) {
			initConsoleEnviorement();
		}

		if (RUNNING_MODE.equals(ONE_TASK)) {
			String cln = TPreferences.getProperty("taskName", "*TaskNotFound");
			PlanC.logger.log(Level.INFO, "OneTask parameter found in .properties. file Task name = " + cln);
			try {
				Class cls = Class.forName(cln);
				Object dobj = cls.newInstance();
				// new class must be extends form AbstractExternalTask
				TTaskManager.executeTask((Runnable) dobj);
				return;
			} catch (Exception e) {
				PlanC.logger.log(Level.SEVERE, e.getMessage(), e);
				Exit.shutdown();
			}
		}
	}

	/**
	 * save properties of actual session
	 */
	public static void saveProperty() {
		// usuario puede ser null si la seccion esta cerrada o cuando se esta cambiando contraceña
		if (Session.getUser() != null && actualContent == DOCKING) {
			int s = frame.getState();
			TPreferences.setPreference(TPreferences.WINDOW_STATE, PlanC.class.getName(), s);
			if (s != WebFrame.MAXIMIZED_BOTH) {
				Rectangle bo = frame.getBounds();
				TPreferences.setPreference(TPreferences.WINDOW_BOUND, PlanC.class.getName(), bo);
			}
		}
	}

	public static void setContentPane(int p) {
		actualContent = p;
		if (p == SIGNIN) {
			frame.setVisible(false);
			frame.setJMenuBar(null);
			frame.setResizable(false);
			actPane = new PUserLogIn();
			JButton jb = (JButton) ((PUserLogIn) actPane).getClientProperty(TConstants.DEFAULT_BUTTON);
			frame.getRootPane().setDefaultButton(jb);

			// TODO: bug when user or sistem sign out the app, the dockein pane still inside the frame and cause a blink
			// error.temporaly use setcontentpane method
			// frame.setContent(actPane);
			frame.setContentPane(actPane);

			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);

			// backdoor for developers
			if (false) {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						String us = TPreferences.getProperty(TPreferences.USER, "SLEPLANC");
						Record r = (Record) ConnectionManager.getAccessTo("sle_users").exist("username ='" + us + "'");
						Session.setUser(r);
					}
				});
				return;
			}
		}
		if (p == CONSOLE) {
			frame.setJMenuBar(null);
			frame.setResizable(false);
			actPane = new TTilePanel();
			frame.setContentPane(actPane);
			frame.setBounds(frame.getSizeBy(.5));
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		}

		if (p == CHANGE_PASSWORD) {
			oldBound = frame.getBounds();
			JButton jb = null;
			actPane = new PChangePassword(Session.getUser(), true);
			jb = (JButton) ((PChangePassword) actPane).getClientProperty(TConstants.DEFAULT_BUTTON);
			jb.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					Record r = ((PChangePassword) actPane).getRecord();
					ConnectionManager.getAccessTo("sle_users").write(r);
					Session.setUser(r);
				}
			});
			JButton ca = (JButton) ((PChangePassword) actPane).getClientProperty(TConstants.DEFAULT_CANCEL_BUTTON);
			ca.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					Exit.shutdown();
				}
			});
			frame.getRootPane().setDefaultButton(jb);
			// frame.setContentPane(actPane);
			frame.setContent(actPane);
			frame.performTransition(oldBound);
		}

		if (p == DOCKING) {
			oldBound = frame.getBounds();

			// TODO: temporal. bug when sign out action is performed
			frame.setContentPane(new JPanel());

			// frame bound
			Rectangle sbound = (Rectangle) TPreferences.getPreference(TPreferences.WINDOW_BOUND, PlanC.class.getName(),
					frame.getSizeBy(0.7));

			// frame state
			boolean ani = true;
			int sts = (Integer) TPreferences.getPreference(TPreferences.WINDOW_STATE, PlanC.class.getName(),
					JFrame.NORMAL);
			if ((sts & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
				ani = false;
				frame.setBounds(sbound);
				frame.setExtendedState(sts);
			}
			if (sts == JFrame.ICONIFIED) {
				frame.setBounds(sbound);
				frame.setState(JFrame.NORMAL);
			}

			frame.setResizable(true);
			frame.setContent(TUIUtils.getBackgroundPanel());

			// perform animation if frame state allow it
			if (ani) {
				frame.performTransition1(oldBound, sbound);
			}
		}

		if (p == HELP) {

			frame.setJMenuBar(null);
			actPane = new HelpPlayer();
			frame.setContentPane(actPane);
			frame.setResizable(false);
			frame.pack();
		}

	}

	public static void executeFinal() {
		/*
		 * try { Thread.sleep(250); } catch (InterruptedException e) { }
		 */
		actPane = new DockingContainer();
		frame.setContentPane(actPane);

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				PlanC.configureWorkMenuBar();
				DockingContainer.loadView(null);
				DockingContainer.fireProperty("", TConstants.PATH_SELECTED, PlanCSelector.getActualPath());
				DockingContainer.performTransition(DockingContainer.getRootWindow());
			}
		});
	}

	/**
	 * set the enable/disable status for all JMenuItem inside of MenuBar
	 * 
	 * @param e - enable status
	 */
	public static void setEnableJmenubar(boolean e) {
		Component[] cmps = menuBar.getComponents();
		for (Component cmp : cmps) {
			if (cmp instanceof JMenuItem) {
				cmp.setEnabled(e);
			}
		}
	}

	public static void showConfirmDialog(String msg) {
		final WebPopOver popOver = new WebPopOver(frame);
		popOver.setModal(true);
		popOver.setMargin(10);
		popOver.setMovable(false);
		popOver.setLayout(new VerticalFlowLayout());
		popOver.add(new WebLabel("1. This pop-over is modal and blocks parent window"));
		popOver.add(new WebLabel("2. This pop-over will not close on focus loss"));
		popOver.add(new WebLabel("3. This pop-over is made non-movable"));
		popOver.add(new SingleAlignPanel(new WebButton("Close pop-over", new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				popOver.dispose();
			}
		}), SingleAlignPanel.RIGHT).setMargin(10, 0, 0, 0));
		// popOver.show((WebButton) e.getSource());
		popOver.show(frame);
	}
	/**
	 * show notification that disapear according to {@link AplicationException} limited time
	 * 
	 * @param mid
	 * @param dta
	 */
	public static void showNotificationLT(String mid, Object... dta) {
		showNotification(mid, new AplicationException(mid).getMiliSeconds(), dta);
	}

	public static void showNotification(String mid, int lt, Object... dta) {
		AplicationException ae = new AplicationException(mid, dta);
		WebNotificationPopup npop = NotificationManager.showNotification(PlanC.frame, ae.getMessage(),
				ae.getExceptionIcon());
		// ae.getExceptionIcon(), NotificationOption.accept);
		npop.setDisplayTime(lt);
		if (mid.equals("notification.msg00")) {
			UIManager.getLookAndFeel().provideErrorFeedback(null);
		} else {
			newMsg.play();
		}
	}

	/**
	 * show notification that does't disapear ultil user click on it
	 * 
	 * @param mid - messate id for {@link AplicationException}
	 * @param dta - sustitution data for AplicationException
	 */
	public static void showNotification(String mid, Object... dta) {
		showNotification(mid, 0, dta);
	}

	/**
	 * retrive global identificator from <code>wmic</code>
	 * 
	 * @param gid - gobal id
	 * @param vn - variable name
	 * 
	 * @return variable value
	 */
	private static String getWmicValue(String gid, String vn) {
		String rval = null;
		try {
			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(new String[]{"wmic", gid, "get", vn});
			InputStream is = process.getInputStream();
			Scanner sc = new Scanner(is);
			while (sc.hasNext()) {
				String next = sc.next();
				if (vn.equals(next)) {
					rval = sc.next().trim();
					break;
				}
			}
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rval;
	}

	private static void configureWorkMenuBar() {
		menuBar = new JMenuBar();

		// ---------------------------------------------------------------
		// app menu - Only master user
		// ---------------------------------------------------------------
		JMenu menu = new JMenu(TStringUtils.getBundleString("about.app.id"));

		// MenuActionFactory maf = null;
		// maf = new MenuActionFactory(SystemVars.class);
		// menu.add(maf);
		// maf = new MenuActionFactory(TConnectionDialog.class);
		// maf.setDimension(MenuActionFactory.PACK_DIMMENTION);
		// menu.add(maf);
		// maf = new MenuActionFactory(TDriverDialog.class);
		// maf.setDimension(MenuActionFactory.PACK_DIMMENTION);
		// menu.add(maf);
		// menu.add(new LoadView());
		// menu.add(new SaveView());
		// menu.add(new UpdateManifestView());
		// menuBar.add(menu);

		// apend plugin actions.
		menu = new JMenu("Extensions");
		Vector<String> ip = PluginManager.getInstalledPlungin();
		for (String pic : ip) {
			String act = PluginManager.getPluginProperty(pic, "plugin.type");
			if (act.equals(Plugin.TYPE_UI)) {
				Object obj = PluginManager.getPlugin(pic).executePlugin(null);
				if (obj instanceof TAbstractAction) {
					menu.add((TAbstractAction) obj);
				}
				// TODO: 180116: is necesary grant plugins more control over jmenubar. !!!!
				if (obj instanceof Vector) {
					Vector v = (Vector) obj;
					// for plancplugin, every action in vector is a main menu
					if (pic.equals("SLEPlanC")) {
						for (Object jc : v) {
							menuBar.add((JComponent) jc);
						}
					} else {
						JMenu jm = new JMenu(PluginManager.getPluginProperty(pic, "plugin.caption"));
						for (Object aa : v) {
							jm.add((AbstractAction) aa);
						}
						menu.add(jm);
					}
				}
			}
		}
		if (menu.getMenuComponentCount() > 0) {
			menuBar.add(menu);
		}

		// ---------------------------------------------------------------
		// Help
		// ---------------------------------------------------------------
		menu = new JMenu(TStringUtils.getBundleString("main.menuitem.help"));
		// g7.add(new Help());
		// menu.add(new UpdateManifestView());
		// menu.add(new JSeparator(JSeparator.HORIZONTAL));
		// menu.add(new DockingAction(Wellcome.class));
		// menu.add(new DockingAction(HelpBrowser.class));
		// HelpRecorderAction act = new HelpRecorderAction();
		// act.setEnabled(SLESession.getUserFieldValue("t_ususer_id").equals("*master"));
		// g7.add(act);
		// maf = new MenuActionFactory(AuditLog.class);
		// maf.setDimension(MenuActionFactory.LARGE_DIMMENTION);
		// menu.add(maf);
		// maf = new MenuActionFactory(References.class);
		// maf.setDimension(MenuActionFactory.LARGE_DIMMENTION);
		// menu.add(maf);

		menu.add(new About());
		menuBar.add(menu);

		// ---------------------------------------------------------------
		// user
		// ---------------------------------------------------------------

		// 171201 1.24: Mierdaaa ya es diciembre y yo pelandooooooooo otro añooo !?!?! user options moved to
		// plancselector

		// menu = new JMenu((String) Session.getUserName());
		// menu.setIcon(TResourceUtils.getSmallIcon("user_user"));
		// menu.add(new PChangePasswordAction());
		// menu.add(new SignOut());
		// menu.add(new JSeparator(JSeparator.HORIZONTAL));
		// menu.add(new Exit());
		// menuBar.add(menu);

		// ---------------------------------------------------------------
		// selector
		// ---------------------------------------------------------------
		final PlanCSelector pcs = new PlanCSelector();
		menuBar.add(pcs.getBreadcrumb());

		frame.setJMenuBar(menuBar);
	}

	/**
	 * try to connect to local database. this method determine if an instance of this app is already running. in this
	 * case, send {@link TPreferences#REQUEST_MAXIMIZE} message throwout internal comunication file (_.properties file)
	 * to signal active instance to display main frame and this execution ends
	 * 
	 */
	private static void connectToLocalDB() {
		// System.getProperties().put("connectTimeout", 10 * 1000);
		// System.getProperties().put("socketTimeout", 10 * 1000);
		try {
			ConnectionManager.connect();
		} catch (Exception e) {
			// if local db is lock, app is already running
			if (e instanceof SQLException) {
				SQLException se = (SQLException) e;
				if (se.getSQLState().equals("08001")) {
					TPreferences.sendMessage(TPreferences.REQUEST_MAXIMIZE, "true");
					System.exit(0);
				}
			}
			SystemLog.logException1(e, true);
			System.exit(-1);
		}
	}

	private static void initConsoleEnviorement() {
		connectToLocalDB();
		Field[] f = new Field[]{new Field("id", new Long(1), 20), new Field("username", "Console", 20)};
		Record ur = new Record("", f);
		Session.setUser(ur);
		DownloadManager.init();
		// UpdateManager.checkForPTF();
		TTaskManager.init();
		setContentPane(CONSOLE);
	}

	private static void initEnviorement() {
		try {
			connectToLocalDB();
			TSplash splash = new TSplash();
			// connection a perfiles de datos
			DBAccess dba = ConnectionManager.getAccessTo("t_connections");
			Vector v = dba.search(null, null);
			Properties prps = new Properties();
			for (int i = 0; i < v.size(); i++) {
				Record r = (Record) v.elementAt(i);
				// for planc: save the conection user as master user
				if (r.getFieldValue("t_cnname").equals("SleOracle")) {
					Session.setMasterUser((String) r.getFieldValue("t_cnuser"));
					// 1823: override fields value from table values to values stored in jdbc.properties file
					String[] rp = PUserLogIn.getJdbcProperties("jdbc.username", "jdbc.password", "jdbc.url");
					r.setFieldValue("T_CNUSER", rp[0]);
					r.setFieldValue("T_CNPASSWORD", rp[1]);
					r.setFieldValue("T_CNURL", rp[2]);
				}
				String pl = (String) r.getFieldValue("T_CNEXTENDED_PRP");
				prps.clear();
				TStringUtils.parseProperties(pl, prps);
				String sc = " (" + prps.getProperty("*schema", "") + ")";
				String msg = "Connecting to database " + r.getFieldValue("t_cnname") + sc;
				String ac = prps.getProperty("*autoconnection", "");
				SystemLog.info(msg + " with *autoconnection=" + ac);
				if (!ac.equals("false")) {
					splash.increment(msg);
					ConnectionManager.connect(r);
				}
			}
			// update running hardware info
			splash.increment("Updating instalation info");
			// checkInstalation();

			// EN ESTE ORDEN
			splash.increment("Starting download subsystem");
			DownloadManager.init();
			// patch update need download manager
			splash.increment("Checking for updates");
			// UpdateManager.checkForPTF();
			splash.increment("Starting task manager");
			TTaskManager.init();
			// load plugins MUST BE last
			splash.increment("Loading plugins");
			PluginManager.init();
			splash.increment("Configuring general aspect and L&F...");
			setContentPane(SIGNIN);
			splash.dispose();
		} catch (Exception e) {
			SystemLog.logException1(e, true);
			System.exit(-1);
		}
	}
}
