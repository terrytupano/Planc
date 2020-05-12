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
package gui.datasource;

import gui.*;

import java.awt.*;

import javax.swing.*;

import action.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * record edition compoenet for T_CONNECTIONS file
 * 
 * @author terry
 *
 */
public class TConnectionsRecord extends AbstractRecordDataInput {

	private TPropertyJTable propertyJTable;

	public TConnectionsRecord(Record rcd, boolean newr) {
		super(null, rcd, newr);
		setVisibleMessagePanel(false);

		// drivers
		ServiceRequest sr = new ServiceRequest(ServiceRequest.DB_QUERY, "t_drivers", null);
		RecordSelector rs = new RecordSelector(sr, "t_drclass", "t_drname", rcd.getFieldValue("t_cndriver"));

		this.propertyJTable = new TPropertyJTable((String) rcd.getFieldValue("t_cnextended_prp"));

		addInputComponent("t_cnname", TUIUtils.getJTextField(rcd, "t_cnname"), true, true);
		addInputComponent("t_cndriver", rs, false, true);
		addInputComponent("t_cnurl", TUIUtils.getJTextField(rcd, "t_cnurl"), true, true);
		addInputComponent("t_cnuser", TUIUtils.getJTextField(rcd, "t_cnuser"), false, true);
		addInputComponent("t_cnpassword", TUIUtils.getJTextField(rcd, "t_cnpassword"), false, true);

		FormLayout lay = new FormLayout("left:pref, 200dlu", // columns
				"p, p, 3dlu, p, p, 3dlu, p, p, 3dlu, p, p, 3dlu, p, p, 3dlu, p, 100dlu"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		build.add(getLabelFor("t_cnname"), cc.xy(1, 1));
		build.add(getInputComponent("t_cnname"), cc.xyw(1, 2, 2));
		build.add(getLabelFor("t_cndriver"), cc.xy(1, 4));
		build.add(getInputComponent("t_cndriver"), cc.xyw(1, 5, 2));
		build.add(getLabelFor("t_cnurl"), cc.xy(1, 7));
		build.add(getInputComponent("t_cnurl"), cc.xyw(1, 8, 2));
		build.add(getLabelFor("t_cnuser"), cc.xy(1, 10));
		build.add(getInputComponent("t_cnuser"), cc.xyw(1, 11, 2));
		build.add(getLabelFor("t_cnpassword"), cc.xy(1, 13));
		build.add(getInputComponent("t_cnpassword"), cc.xyw(1, 14, 2));
		build.add(TUIUtils.getJLabel("t_cnextended_prp", false, true), cc.xy(1, 16));
		JScrollPane js = new JScrollPane(propertyJTable);
		js.getViewport().setBackground(Color.WHITE);
		build.add(js, cc.xyw(1, 17, 2));

		setActionBar(new ApplyAction(this));
		addWithoutBorder(build.getPanel());
		preValidate(null);
	}
	
	@Override
	public void setModel(Record mod) {
		super.setModel(mod);
		// tabla de propiedades 
		propertyJTable.setPropertys((String) mod.getFieldValue("t_cnextended_prp"));
	}

	@Override
	public Record getRecord() {
		Record rcd = super.getRecord();
		// aditional properties
		rcd.setFieldValue("t_cnextended_prp", propertyJTable.getProperties());
		return rcd;
	}
}
