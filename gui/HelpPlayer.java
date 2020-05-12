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
/*			
 * Copyright (c) Lizard technology - All right reserved
 * Lizard technology PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.			
 */
package gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;


import action.*;
import core.*;

/**
 * panel encargado de presentar cintas de videos con tutoriales y ayudas
 * 
 */
public class HelpPlayer extends JLayeredPane implements HideLeft, ListSelectionListener, ActionListener {

	public static String HELP_PATH;

	private static final int AVATAR_EXIT = 1;
	private static final int AVATAR_RUN = 0;
	private static int avatar_status = -1;

	private Component actCmp;
	private ImageIcon[] avatarI;
	private Rectangle bound;
	private JPanel contentP, mediaP;
	
	private JLabel coverJL, videoDesc, avatarJL, nowPlaying;
	private JProgressBar mediaProgres;
	private CardLayout playerLay;
	private JButton playJB, stopJB;
//	private long strDate;
//	private javax.swing.Timer timer;
//	private int totalTime;
	private Hashtable vid_des, vid_tim;
	private JList videoList;

	public HelpPlayer() {
		HELP_PATH = TResourceUtils.RESOURCE_PATH + "help/";
		createMediaPanel();
		createContentPanel();
		createAvatar();
		setMinimumSize(mediaP.getSize());
		setPreferredSize(mediaP.getSize());
		setMaximumSize(mediaP.getSize());

		// playercmp

		add(mediaP, JLayeredPane.DEFAULT_LAYER);
		contentP.setBounds(0, bound.height - contentP.getHeight(), contentP.getWidth(), contentP.getHeight());
		add(contentP, JLayeredPane.MODAL_LAYER);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == playJB) {
			play();
		}
		if (e.getSource() == stopJB) {
			stop();
		}
		/*
		 * calcula el tiempo segun la vitacora de help.properties
		if (e.getSource() == timer) {
			long cd = (new Date()).getTime();

			int i = (int) ((cd - strDate) * 100 / totalTime);
			if (i > 99) {
				stop();
			} else {
				setAvatarPosition();
				mediaProgres.setValue(i);
				if (i > 97) {
					avatarGesture(AVATAR_EXIT);
				}
			}
		}
		*/
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			TEntry lt = (TEntry) videoList.getSelectedValue();
			String txt = "";
			playJB.setEnabled(false);
			if (lt != null) {
				txt = (String) vid_des.get(lt.getKey());
				playJB.setEnabled(true);
			}
			videoDesc.setText(txt);
		}
	}

	/**
	 * gesticula el avatar segun el gesto pasado como argumento <code>AVATAR_EXIT, AVATAR_RUN</code>
	 * 
	 * @param g - gesto
	 */
	private void avatarGesture(int g) {
		if (g == avatar_status) {
			return;
		}
		avatar_status = g;
		if (g == AVATAR_EXIT) {
			avatarJL.setVisible(false);
		} else {
			avatarJL.setIcon(avatarI[g]);
		}
	}

	/**
	 * crea avatar
	 * 
	 * 
	 */
	private void createAvatar() {
		this.avatarI = new ImageIcon[] { new ImageIcon(HELP_PATH + "avatar_run.gif"),
				new ImageIcon(HELP_PATH + "avatar_happy.gif"), new ImageIcon(HELP_PATH + "avatar_sad.gif") };
		this.avatarJL = new JLabel(avatarI[1]);

		// selecciona uno de los 2 gestos finales
		Random d = new Random();
		int a = d.nextBoolean() ? 1 : 2;

	}

	/**
	 * crea panel de control para cintas de ayuda
	 * 
	 * 
	 */
	private void createContentPanel() {
		this.contentP = new JPanel(new BorderLayout(4, 4));
		contentP.setBorder(new CompoundBorder(new LineBorder(Color.GRAY), new EmptyBorder(4, 4, 4, 4)));
		try {
			JEditorPane jep = new JEditorPane(TResourceUtils.getURL("helpPlayer.html"));
			jep.setEditable(false);
			jep.setOpaque(false);

			File f = TResourceUtils.getFile("help/help.properties");
			Properties prp = new Properties();
			try {
				prp.load(new FileInputStream(f));
			} catch (Exception e) {
				e.printStackTrace();
			}
			Enumeration e = prp.keys();
			vid_des = new Hashtable();
			vid_tim = new Hashtable();
			TreeMap v = new TreeMap();
			while (e.hasMoreElements()) {
				String k = (String) e.nextElement();
				String[] n_t_d = prp.getProperty(k).split(";");
				vid_des.put(k, n_t_d[2]);
				v.put(k, new TEntry(k, n_t_d[0]));
				vid_tim.put(k, n_t_d[1]);
			}
			videoList = new JList(new Vector(v.values()));
			videoList.addListSelectionListener(this);
			videoDesc = new JLabel();
			videoDesc.setVerticalAlignment(JLabel.TOP);
			videoDesc.setBorder(new EtchedBorder());

			JPanel jp1 = new JPanel(new GridLayout(1, 2, 4, 0));
			jp1.add(new JScrollPane(videoList));
			jp1.add(videoDesc);

			JButton jb = new JButton(new CloseHelp());

			contentP.add(jep, BorderLayout.NORTH);
			contentP.add(jp1, BorderLayout.CENTER);
			contentP.add(TUIUtils.getInHorizontalBox(new Component[] { jb }, FlowLayout.RIGHT), BorderLayout.SOUTH);

			contentP.setBounds(0, 0, 480, 300);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * crea panel para presentancion y control de cintas de ayuda
	 * 
	 * 
	 */
	private void createMediaPanel() {
		ImageIcon ii = new ImageIcon(HELP_PATH + "cover.gif");
		this.bound = new Rectangle(0, 0, ii.getIconWidth() + 4, ii.getIconHeight() + 4);

		// crea disabledIcon
		// GrayFilter filter = new GrayFilter(true, 80);
		// ImageProducer prod = new FilteredImageSource(ii.getImage().getSource(), filter);
		// Image grayImage = Toolkit.getDefaultToolkit().createImage(prod);

		this.mediaP = new JPanel(new BorderLayout());
		this.nowPlaying = new JLabel("");
		this.coverJL = new JLabel(ii);
		// coverJL.setDisabledIcon(new ImageIcon(grayImage));
		coverJL.setEnabled(false);

		// borde para cuadrar con el explorador
		coverJL.setBorder(new LineBorder(Color.GRAY, 2));
		coverJL.setPreferredSize(bound.getSize());

		this.actCmp = coverJL;

		// panel de controles
		JToolBar tob = TUIUtils.getJToolBar();
		tob.setBorder(new EtchedBorder());

		this.playJB = new JButton(TResourceUtils.getSmallIcon("playMedia"));
		playJB.setEnabled(false);
		playJB.addActionListener(this);
		this.stopJB = new JButton(TResourceUtils.getSmallIcon("stopMedia"));
		stopJB.setEnabled(false);
		stopJB.addActionListener(this);
		this.mediaProgres = new JProgressBar(0, 100);
		mediaProgres.setMinimumSize(new Dimension(300, 10));
		mediaProgres.setPreferredSize(new Dimension(300, 10));
		mediaProgres.setMaximumSize(new Dimension(300, 10));
		nowPlaying.setPreferredSize(new Dimension(300, 10));
		tob.add(Box.createHorizontalStrut(4));
		tob.add(playJB);
		tob.add(stopJB);
		tob.add(mediaProgres);
		tob.add(Box.createHorizontalStrut(4));
		tob.add(nowPlaying);
		mediaP.add(coverJL, BorderLayout.CENTER);
		mediaP.add(tob, BorderLayout.SOUTH);
		Dimension d2 = tob.getPreferredSize();
		Rectangle b = new Rectangle(0, 0, bound.width, bound.height + d2.height);
		mediaP.setBounds(b);
	}

	/**
	 * inicia la reprodiccion del video seleccionado
	 * 
	 * 
	 */
	private void play() {
		setAvatarPosition();
		setVisible(false);
		try {
			TEntry lt = (TEntry) videoList.getSelectedValue();
			playJB.setEnabled(false);
			stopJB.setEnabled(true);
			String p = System.getProperty("java.library.path");
			String fn = (String) lt.getValue();
			nowPlaying.setText(fn);
			mediaP.remove(actCmp);
	//		actCmp = browser;
	//		mediaP.add(browser, BorderLayout.CENTER);
			contentP.setVisible(false);
			avatarGesture(AVATAR_RUN);
			avatarJL.setVisible(true);
//			this.totalTime = Integer.parseInt((String) vid_tim.get(lt.getKey())) * 1000;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		setVisible(true);
	}

	/**
	 * establece la posicion del avatar con respecto a la ventana
	 * 
	 * 
	 */
	private void setAvatarPosition() {
		Point px = new Point(0, 0);
		SwingUtilities.convertPointToScreen(px, this);
		avatarJL.setBounds(bound.width - avatarJL.getWidth() + px.x, bound.height - avatarJL.getHeight() + px.y,
				avatarJL.getWidth(), avatarJL.getHeight());
	}

	/**
	 * detiene la cinta de video y presenta el panel con lista
	 * 
	 */
	private void stop() {
		setVisible(false);
		playJB.setEnabled(true);
		stopJB.setEnabled(false);
		mediaP.remove(actCmp);
		actCmp = coverJL;
		nowPlaying.setText("");
		mediaP.add(coverJL, BorderLayout.CENTER);
//		timer.stop();
		contentP.setVisible(true);
		setVisible(true);
		avatarJL.setVisible(false);
	}

	public class AvatarJPanel extends JPanel {

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
		 */
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
		}
	}

	/**
	 * accion para cerrar entorno de ayuda y volvar al trabajo
	 * 
	 */
	public class CloseHelp extends TAbstractAction {

		public CloseHelp() {
			super("action.close", "Close", TAbstractAction.NO_SCOPE, null);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see client.action.AppAbstractAction#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent arg0) {
			PlanC.setContentPane(PlanC.DOCKING);
		}
	}
}
