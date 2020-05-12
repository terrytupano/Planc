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
package gui.docking;

import gui.*;

import java.awt.*;
import java.beans.*;
import java.io.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import net.infonode.docking.*;
import net.infonode.docking.properties.*;
import net.infonode.docking.theme.*;
import net.infonode.docking.util.*;
import net.infonode.util.*;
import net.sf.jasperreports.engine.*;
import plugin.planc.*;
import plugin.planc.dashboard.*;

import com.alee.extended.panel.*;
import com.alee.extended.statusbar.*;
import com.alee.extended.transition.*;
import com.alee.extended.transition.effects.curtain.*;
import com.alee.laf.button.*;
import com.alee.laf.label.*;

import core.*;
import core.tasks.*;

public class DockingContainer extends JPanel {

	private static DockingContainer dockingContainer;
	// to install componentmoveadapter class in main frame (see PlanC main class)
	public static JComponent titleBar;
	public static JasperPrint jasperPrint = null;
	private static HashMap<String, View> dynamicViews;
	private static Vector<String> storedView;
	private static RootWindowProperties properties;
	private static RootWindow rootWindow;
	private static boolean addListern;
	private static ComponentTransition transitionPanel;
	private static JComponent backgroundPanel;
	private static AmountViewer amountViewer;
	private static String actualViewName;

	public DockingContainer() {
		super(new BorderLayout());
		properties = new RootWindowProperties();
		dynamicViews = new HashMap<String, View>();
		amountViewer = new AmountViewer();
		amountViewer.init();

		configureRootWindow();
//		loadView(null);

		backgroundPanel = TUIUtils.getBackgroundPanel();
		transitionPanel = new ComponentTransition();
		transitionPanel.setContent(amountViewer);
		transitionPanel.setContent(rootWindow);
		transitionPanel.setContent(backgroundPanel);

		add(transitionPanel, BorderLayout.CENTER);
		add(statusBar(), BorderLayout.SOUTH);

		// Transition effect
		final CurtainTransitionEffect effect = new CurtainTransitionEffect();
		effect.setDirection(com.alee.extended.transition.effects.Direction.down);
		effect.setType(CurtainType.fade);
		effect.setSpeed(9);
		transitionPanel.setTransitionEffect(effect);

		dockingContainer = this;
	}

	public static AmountViewer getAmountViewer() {
		return amountViewer;
	}

	public static JComponent getBackgroundPanel() {
		return backgroundPanel;
	}

	/**
	 * return the active instance for this class
	 * 
	 * @return active instace of {@link DockingContainer}
	 */
	public static DockingContainer getInstance() {
		return dockingContainer;
	}

	public static void performTransition(JComponent tt) {
		transitionPanel.performTransition(tt);
	}

	/**
	 * Return a {@link JLabel} formatted to present warning message about problems with autorization for user
	 * 
	 * @param txt - text to format message
	 * 
	 * @return {@link JLabel}
	 */
	private static JLabel getLockPanel(String txt) {
		// JPanel jp = new JPanel(new BorderLayout());
		String msg = MessageFormat.format(TStringUtils.getBundleString("ui.msg09"), txt);
		JLabel jl = new JLabel(msg, TResourceUtils.getIcon("lock_gray", 48), JLabel.CENTER);
		jl.setVerticalTextPosition(JLabel.BOTTOM);
		jl.setHorizontalTextPosition(JLabel.CENTER);
		jl.setBorder(new EmptyBorder(4, 4, 4, 4));
		return jl;
	}

	/**
	 * apend new dynamic view to the actual docking window
	 * 
	 * @param cn - classname for internal view component
	 */
	public static void addNewDynamicView(String cn) {
		View v = dynamicViews.get(cn);
		if (v == null) {
			v = createDynamicView(cn);
			rootWindow.setVisible(false);
			DockingUtil.addWindow(v, rootWindow);
			fireProperty("", TConstants.PATH_SELECTED, PlanCSelector.getActualPath());
			rootWindow.setVisible(true);
		} else {
			v.restoreFocus();
		}
	}

	/**
	 * Adds a PropertyChangeListener to the listener list of target component identified by <code>className</code>.
	 * 
	 * @param className - class name of internal component of View which property are interested in
	 * @param propertyName - property name
	 * @param listener - the property listener to add
	 */
	public static void addPropertyChangeListener(String className, String propertyName, PropertyChangeListener listener) {
		View dv = dynamicViews.get(className);
		// if user is not autorized, component is JComponent
		if (dv != null && (dv.getComponent() instanceof DockingComponent)) {
			JComponent changer = (JComponent) dv.getComponent();
			// avoid mutiple propertyChange invocation on listener
			changer.removePropertyChangeListener(propertyName, listener);
			changer.addPropertyChangeListener(propertyName, listener);
		}
	}

	/**
	 * create and return a {@link View} with a {@link DockingComponent} as internal component with all initial
	 * conditions setted. if autorization fail for actual user the internal compoent is a {@link JComponent}
	 * 
	 * @param cn - class name for internal component
	 * 
	 * @return View
	 */
	public static View createDynamicView(String cn) {
		return createDynamicView(cn, true);
	}

	/**
	 * create and return a {@link View} with a {@link DockingComponent} as internal component with all initial
	 * conditions setted. if autorization fail for actual user the internal compoent is a {@link JComponent}
	 * 
	 * @param cn - class name for internal component
	 * @param init - wether perform {@link DockingComponent#init()} or not
	 * 
	 * @return View
	 */
	private static View createDynamicView(String cn, boolean init) {
		View dv = null;
		try {
			DockingComponent doc = (DockingComponent) Class.forName(cn).newInstance();
			// check for autorizations. if docking component exist in save view and unautorizaed later, docing component
			// is changed for no autorized panel
			JComponent jcmp = (JComponent) doc;
			// 171204: now dockingcomponent text and icon are the class name
			String scn = doc.getClass().getSimpleName();
			String txt = TStringUtils.getBundleString(scn);
			if (!Session.isAutorizedForComponent(doc)) {
				jcmp = getLockPanel(txt);
				// don't init
				init = false;
			}
			dv = new View(txt, TResourceUtils.getSmallIcon(scn), jcmp);
			dv.setName(cn);

			// move toolbar to custom componet in tabpanel
			/*
			 * if (doc instanceof UIListPanel) { UIListPanel uilp = (UIListPanel) doc; JToolBar jtb = uilp.getToolBar();
			 * if (jtb != null) { Component cmps[] = jtb.getComponents();
			 * dv.getCustomTabComponents().add(Box.createHorizontalStrut(4)); for (Component c : cmps) {
			 * dv.getCustomTabComponents().add(c); dv.getCustomTabComponents().add(Box.createHorizontalStrut(4)); if (c
			 * instanceof JButton) { jtb.remove(c); JButton jc = (JButton) c; jc.setText(null); jc.setOpaque(false); //
			 * jc.setBorder(null); jc.setFocusable(false); } } jtb.setVisible(false); jtb = null; } }
			 */

			// perform init if is needed and the component pass user autorization
			if (init) {
				doc.init();
			}
		} catch (Exception ex) {
			SystemLog.logException(ex);
		}
		return dv;
	}

	/**
	 * fire a external property to all internal component
	 * 
	 * @param src - source of property
	 * @param pn - property name
	 * @param nval - new value for property event
	 */
	public static void fireProperty(Object src, String pn, Object nval) {
		Collection<View> vc = dynamicViews.values();
		for (View cn : vc) {
			// if user is not autorized, component is JComponent
			if (cn != null && (cn.getComponent() instanceof DockingComponent)) {
				DockingComponent dc = (DockingComponent) cn.getComponent();
				dc.propertyChange(new PropertyChangeEvent(src, pn, null, nval));
			}
		}
		// TODO: Temporal: dispach property to amountviewer if property is path selected
		if (pn.equals(TConstants.PATH_SELECTED)) {
			amountViewer.propertyChange(new PropertyChangeEvent(src, pn, null, nval));
		}
	}

	/**
	 * return the {@link RootWindow} for this active instance
	 * 
	 * @return {@link RootWindow}
	 */
	public static RootWindow getRootWindow() {
		return rootWindow;
	}

	/**
	 * look for saved view whit viewid. <code>vnam</code> id can be:
	 * <ul>
	 * <li>a username
	 * <li>A template name.
	 * <li>A <code>null</code> value
	 * </ul>
	 * if view is not null, this method load the especific view name. <br>
	 * On the other hand, if null values is specified, the system firt look at username saved view, if that view not
	 * found, wellcomre docking page is display.
	 * <p>
	 * if a view is sucsefully load, this method configure all listener for any intereted view.
	 */
	public static void loadView(String vnam) {
		String obj = (vnam == null) ? Session.getUserName() : vnam;
		Object oval = TPreferences.getPreference(TPreferences.DOCKING_STATE, obj, null);
		// add
		try {
			// not null, saved view
			if (oval != null) {
				addListern = false;
				// 180224: storedView keep the saved instances under control in read/write view
				storedView = (Vector<String>) TPreferences.getPreference("StoredView", obj, new Vector());
				byte[] saved = (byte[]) oval;
				ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(saved));
				rootWindow.read(in, false);
				// System.out.println("Loaded: \n" + DeveloperUtil.getWindowLayoutAsString(rootWindow));
				in.close();
				// build listner list
				Collection<View> vc = dynamicViews.values();
				for (View cn : vc) {
					Component cmp = cn.getComponent();
					if (cmp instanceof DockingComponent) {
						((DockingComponent) cmp).init();
					}
					addPropertyChangeListener(cn);
				}
				addListern = true;
			} else {
				addListern = true;
				addNewDynamicView(Wellcome.class.getName());
			}
		} catch (Exception e1) {
			SystemLog.logException(e1);
		}
	}

	/**
	 * Save actual view in screen.
	 * 
	 * @param vnam - View name. if <code>null</code> then save this view for username in sesion. else, save vnam as
	 *        template for future load
	 */
	public static void saveView(String vnam) {
		try {
			storedView = new Vector<String>(dynamicViews.keySet());
			String obj = (vnam == null) ? Session.getUserName() : vnam;
			// System.out.println("Saved: \n" + DeveloperUtil.getWindowLayoutAsString(rootWindow));
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);
			rootWindow.write(out, false);
			out.close();
			TPreferences.setPreference("StoredView", obj, storedView);
			TPreferences.setPreference(TPreferences.DOCKING_STATE, obj, bos.toByteArray());
		} catch (Exception ex) {
			SystemLog.logException(ex);
		}
	}

	/**
	 * Sets the top level docking window inside this root window. This methdo clear all component inside the root window
	 * and set a new window or a seved one according to follow:
	 * <ol>
	 * <li>if another window was loaded using this method, the actual state is saved (using the former <code>nam</code>
	 * parameter as view name)
	 * <li>check for saved instances of the actual viewname <code>nam</code>. if exist, then load that view, else set
	 * the window as build in <code>nw</code> parameter
	 * </ol>
	 * This method is intended for actions that build the view hardcoded at first instance but need, in future call,
	 * restore the previous state modfiy by user
	 * 
	 * @param nw - Window to set if not previos saved instance exist
	 * @param nam - name of window for save/load
	 */
	public static void setWindow(DockingWindow nw, String nam) {
		if (actualViewName != null) {
			// 180227: actualview save implementation incomplete !!!
			// 180311: due constant intromision from user. saveview can be guarantee.
//			saveView(actualViewName);
		}
		actualViewName = nam;
		Object oval = TPreferences.getPreference(TPreferences.DOCKING_STATE, actualViewName, null);
		rootWindow.setVisible(false);
		if (oval == null) {
			Collection<View> col = dynamicViews.values();
			for (View v : col) {
				// System.out.println("setWindow: " + v.getName());
				rootWindow.removeView(v);
				v = null; // <<<<<<<<<< important to complete removeView !! DONT DELETE
			}
			dynamicViews.clear();
			rootWindow.setWindow(nw);
		} else {
			loadView(actualViewName);
		}
		fireProperty("", TConstants.PATH_SELECTED, PlanCSelector.getActualPath());
		rootWindow.setVisible(true);
	}

	/**
	 * for all active internal components in windows, add a {@link PropertyChangeListener} where listener is the
	 * internal component in view <code>dview</code>. The propertyName parameter are:
	 * <ul>
	 * <li>TConstants.RECORD_SELECTED</li>
	 * </ul>
	 * 
	 * @param dview - view with instance of {@link DockingComponent} inside as component
	 * 
	 * @see #addPropertyChangeListener(String, String, PropertyChangeListener)
	 */
	private static void addPropertyChangeListener(View dview) {
		// if user is not autorized, internal component is JComponent
		if (dview.getComponent() instanceof DockingComponent) {
			DockingComponent lst = (DockingComponent) dview.getComponent();
			Set<String> vs = dynamicViews.keySet();
			for (String cn : vs) {
				// check not addPropertyChangeListener to myself
				if (!dview.getName().equals(cn)) {
					addPropertyChangeListener(cn, TConstants.RECORD_SELECTED, lst);
					addPropertyChangeListener(cn, TConstants.FIND_TEXT, lst);
					addPropertyChangeListener(cn, TConstants.LOG_MESSAGE, lst);
				}
			}
		}
	}

	/**
	 * crea y configura los componentes infonode.
	 * 
	 * se usa el nombre del usuario concatenado con el nombre de la clase para identificar a la vista
	 * 
	 */
	private static void configureRootWindow() {
		ViewMap vm = new ViewMap();

		MixedViewHandler handler = new MixedViewHandler(vm, new ViewSerializer() {

			@Override
			public View readView(ObjectInputStream in) throws IOException {
				int l = in.readInt();
				// possible int id from view not saved
				if (l < 1) {
					return null;
				}
				byte[] b = new byte[l];
				in.read(b);
				String dv = new String(b);
				// only create dinamic view from the stored views in vector
				if (!storedView.contains(dv)) {
					return null;
				}
				// System.out.println("readView: " + dv);
				return createDynamicView(dv, false);
			}

			@Override
			public void writeView(View view, ObjectOutputStream out) throws IOException {
				String cn = view.getName();
				// only store dinamic view that are in rootwindow
				if (storedView.contains(cn)) {
					// System.out.println("writeView:" + cn);
					out.writeInt(cn.length());
					out.write(cn.getBytes());
				}
			}
		});

		rootWindow = DockingUtil.createRootWindow(vm, handler, false);

		// DockingWindowsTheme ct = new DefaultDockingTheme();

		DockingWindowsTheme[] themes = {new DefaultDockingTheme(), new LookAndFeelDockingTheme(),
				new BlueHighlightDockingTheme(), new SlimFlatDockingTheme(), new TGradientDockingTheme(),
				new ShapedGradientDockingTheme(), new SoftBlueIceDockingTheme(), new ClassicDockingTheme()};

		// UIManager.put("Desktop.background", Color.WHITE);
		// DockingWindowsTheme ctheme = themes[4];
		DockingWindowsTheme ctheme = themes[1];

		// rootWindow.getRootWindowProperties().addSuperObject(properties);

		// TGradientDockingTheme gdt = new TGradientDockingTheme(true, true, false, false, null, Color.WHITE);
		// properties.addSuperObject(gdt.getRootWindowProperties());

		// 180224: this properties only work on LookAndFeelDockingTheme
		 RootWindowProperties tp = ctheme.getRootWindowProperties();
		// tp.getWindowAreaProperties().setBackgroundColor(Color.WHITE);
		// tp.getTabWindowProperties().getTabbedPanelProperties().setShadowSize(20);
		 properties.addSuperObject(tp);

		// RootWindowProperties titleBarStyleProperties = PropertiesUtil.createTitleBarStyleRootWindowProperties();
		// properties.addSuperObject(titleBarStyleProperties);

		rootWindow.getRootWindowProperties().addSuperObject(properties);
		rootWindow.addListener(new TDockingWindowAdapter());

		rootWindow.getWindowBar(Direction.DOWN).setVisible(false);
		rootWindow.getWindowBar(Direction.RIGHT).setVisible(false);
		rootWindow.getWindowBar(Direction.LEFT).setVisible(false);
		rootWindow.getWindowBar(Direction.UP).setVisible(false);

		rootWindow.getWindowBar(Direction.DOWN).setEnabled(false);
		rootWindow.getWindowBar(Direction.RIGHT).setEnabled(false);
		rootWindow.getWindowBar(Direction.LEFT).setEnabled(false);
		rootWindow.getWindowBar(Direction.UP).setEnabled(false);
		
		rootWindow.setBorder(null);

		properties.getDockingWindowProperties().setUndockEnabled(false);
		properties.getDockingWindowProperties().setMaximizeEnabled(false);
		properties.getDockingWindowProperties().setMinimizeEnabled(false);
		properties.getDockingWindowProperties().setCloseEnabled(false);

		properties.getTabWindowProperties().getTabbedPanelProperties().getTabAreaProperties().getComponentProperties()
				.setBackgroundColor(Color.RED);

		TabWindowProperties tgp = properties.getTabWindowProperties();
		tgp.getMinimizeButtonProperties().setVisible(false);
		tgp.getRestoreButtonProperties().setVisible(false);
		tgp.getCloseButtonProperties().setVisible(false);
		tgp.getMaximizeButtonProperties().setVisible(false);
		tgp.getUndockButtonProperties().setVisible(false);
	}

	/**
	 * crate and return status bar
	 * 
	 * @return - webstatusbar
	 */
	private static WebStatusBar statusBar() {
		WebStatusBar bar = new WebStatusBar();
		Insets i = bar.getMargin();
		i.right = 16;
		bar.setMargin(i);

		WebLabel pd = new WebLabel(TStringUtils.getAboutAppShort(), TResourceUtils.getSmallIcon("beta"));
		bar.add(pd);
		bar.addSpacing();

		WebToggleButton left = new WebToggleButton(new PlanningAction());
		left.setSelected(true);
		WebToggleButton right = new WebToggleButton(new DashBoardAction());
		WebButtonGroup textGroup = new WebButtonGroup(true, left, right);
		textGroup.setButtonsDrawFocus(false);

		bar.addToEnd(textGroup);
		bar.addToEnd(TTaskManager.getProgressBar());
		return bar;
	}
	/**
	 * actualiza lista de componentes que actualmente estan visibles dentro de esta perspectiva
	 * 
	 * @param window - ventana que genero el evento
	 * @param add - indica si la ventana fue adicionada o removida
	 */
	static void updateViews(DockingWindow window, boolean add) {
		if (window instanceof View) {
			View v = (View) window;
			String cn = v.getName();
			if (!add) {
				dynamicViews.remove(cn);
				// System.out.println("updateViews remove:  " + cn);
			} else {
				dynamicViews.put(cn, v);
				// System.out.println("updateViews added:  " + cn);
				// addPropertyChangeListener
				if (addListern) {
					addPropertyChangeListener(v);
				}
			}
		} else {
			for (int i = 0; i < window.getChildWindowCount(); i++) {
				updateViews(window.getChildWindow(i), add);
			}
		}
	}
}
