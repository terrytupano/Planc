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
package action;
 
import gui.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import com.jgoodies.animation.*;
import com.jgoodies.animation.animations.*;
import com.jgoodies.animation.components.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * presenta la ventana con informacion sobre la aplicacion
 * 
 */
public class About extends TAbstractAction implements PropertyChangeListener {

	private Dimension dimension;
	private BasicTextLabel title, line1, line2;
	private static final int DEFAULT_FRAME_RATE = 30;
	private static Animator animator;
	private JDialog dialog;

	/**
	 * nueva accion
	 * 
	 */
	public About() {
		super("about.title", null, TAbstractAction.NO_SCOPE, null);
		dimension = new Dimension(400, 200);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		aboutPanel ap = new aboutPanel();
		ap.addPropertyChangeListener(TConstants.ACTION_PERFORMED, this);
		dialog = getDialog(ap, "about.title");
		// dialog.setSize(dimension);
		dialog.setVisible(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		dialog.dispose();
	}

	/**
	 * clase que contiene los paneles informativos
	 * 
	 */
	public class aboutPanel extends UIComponentPanel {

		/**
		 * nueva instancia
		 * 
		 */
		public aboutPanel() {
			super(null, false);

			// pesta;as
			JTabbedPane jtp = new JTabbedPane();
			jtp.add(TStringUtils.getBundleString("about.title"), getaboutPanel());
			jtp.add(TStringUtils.getBundleString("about.preference"), getPropertyPanel());

			setActionBar(new AbstractAction[]{new OkAction(this)});

			add(jtp);
		}
	}

	private JPanel getaboutPanel() {
		FormLayout lay = new FormLayout("left:pref, 3dlu, left:pref, max(200dlu;pref)",
				"p, p, 10dlu, p, 3dlu, p, 10dlu, p, 10dlu, p, p");// rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		ImageIcon ii = TResourceUtils.getIcon("appIcon", 80);
		JLabel jl = new JLabel(ii);
		jl.setHorizontalAlignment(JLabel.LEFT);
		build.add(jl, cc.xyw(1, 1, 4));
		build.add(new JLabel(TStringUtils.getBundleString("about.app")), cc.xyw(1, 2, 4));

		build.add(new JLabel(TStringUtils.getBundleString("about.version")), cc.xy(1, 4));
		build.add(new JLabel(SystemVariables.getStringVar("versionID")), cc.xy(3, 4));

		build.add(new JLabel(TStringUtils.getBundleString("about.update")), cc.xy(1, 6));
		build.add(new JLabel(SystemVariables.getStringVar("updateID")), cc.xy(3, 6));

		build.add(new JLabel(TStringUtils.getBundleString("about.msg1")), cc.xyw(1, 8, 4));

		build.add(new JLabel(TStringUtils.getBundleString("about.msg2")), cc.xyw(1, 10, 4));
		build.add(getOpenSourcePanel(), cc.xyw(1, 11, 4));
		// build.add(Box.createVerticalStrut(4), cc.xy(1, 7));
		JPanel jp = build.getPanel();
		jp.setBorder(new EmptyBorder(4, 4, 4, 4));
		return jp;
	}

	private JComponent getOpenSourcePanel() {
		Dimension d = new Dimension(dimension.width, 55);
		// componentes
		Font titf = new Font("Verdana", Font.BOLD, 14);
		Font linf = new Font("Verdana", Font.PLAIN, 11);
		title = new BasicTextLabel(" ");
		title.setFont(titf);
		title.setOpaque(false);
		line1 = new BasicTextLabel(" ");
		line1.setFont(linf);
		line1.setOpaque(false);
		line2 = new BasicTextLabel(" ");
		line2.setFont(linf);
		line2.setOpaque(false);

		JPanel lines = new JPanel(new GridLayout(3, 1));
		lines.setPreferredSize(d);
		lines.setOpaque(true);
		lines.setBackground(Color.LIGHT_GRAY.brighter());
		lines.add(title);
		lines.add(line1);
		lines.add(line2);

		animator = new Animator(createAnimation(), DEFAULT_FRAME_RATE);
		animator.start();
		lines.setBorder(new EtchedBorder());
		return lines;
	}

	/**
	 * crea animacion de lineas
	 * 
	 */
	private Animation createAnimation() {
		String src[] = new String[]{"Forms framework", "Build better screens faster",
				"Copyright (c) 2001-2004 JGoodies Karsten Lentzsch", "iReport 0.5.0", "Desing tool for JasperReport",
				"(c) 2002 Giulio Toffoli ", "JFreeChart ", "A free chart library for the Java(tm) platform",
				"Copyright 2000-2005, by Object Refinery Limited and Contributors.", "JSmooth ",
				"A VM wrapper toolkit for Windows", "Copyright (C) 2003 Rodrigo Reyes", "Looks ",
				"Free high-fidelity Windows and multi-platform appearance",
				"Copyright (c) 2001-2004 JGoodies Karsten Lentzsch ", "Eclipse ", "IDE for software development",
				"(c) Copyright Eclipse contributors and others 2000, 2005.", "HSQL ", "100% Java Database",
				"Copyright © 2001 - 2005 HSQL Development Group.", "JasperReport ", "Open source reporting tool",
				"Copyright (C) 2001-2005 JasperSoft Corporation.", "MySQL Connector/J 3.1.7 ",
				"JDBC level 4 for MySQL DB ", "Copyright (c) 2003 MySQL AB ", "Animation 1.1.3 ",
				"Time-based real-time animations", "Copyright (c) 2001-2004 JGoodies Karsten Lentzsch",
				"SQLylog v4.04 ", "Manager for MySQL DB", "(c) 2002 - 2005 Webyog SoftWork Pvt. Ltd. ", "MySQL ",
				"Database Management System", "Copyright (c) 2003 MySQL AB ", "L2FProd", "Common Components",
				"Copyright 2005 L2FProd.com",

		};

		Vector anim = new Vector();
		for (int i = 0; i < src.length; i = i + 3) {
			Vector v = new Vector(3);
			v.add(BasicTextAnimation.defaultSpace(title, 5500, src[i], Color.GREEN.darker()));
			v.add(BasicTextAnimation.defaultFade(line1, 5000, src[i + 1], Color.GRAY));
			v.add(BasicTextAnimation.defaultFade(line2, 5000, src[i + 2], Color.GRAY));
			v.add(Animations.pause(300));
			anim.add(Animations.parallel(v));
		}
		return Animations.repeat(100, Animations.sequential(anim));
	}

	private JPanel getPropertyPanel() {
		Properties prp = System.getProperties();
		Enumeration e = prp.propertyNames();
		Vector vc = new Vector(2);
		vc.add(TStringUtils.getBundleString("property.name"));
		vc.add(TStringUtils.getBundleString("property.value"));
		Vector vr = new Vector();
		while (e.hasMoreElements()) {
			Vector rd = new Vector(2);
			String k = (String) e.nextElement();
			rd.add(k);
			rd.add(prp.getProperty(k));
			vr.add(rd);
		}
		JTable jt = new JTable(vr, vc);
		TUIUtils.fixTableColumn(jt, new int[]{170, 300});
		jt.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		jt.setEnabled(false);
		JScrollPane jsp = new JScrollPane(jt);
		jsp.setPreferredSize(dimension);
		JPanel pb = new JPanel(new BorderLayout());
		pb.add(jsp, BorderLayout.CENTER);
		return pb;
	}
}
