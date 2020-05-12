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
import java.beans.*;

import javax.swing.*;
import javax.swing.border.*;


import action.*;
import core.*;
import core.datasource.*;

/**
 * present a list of valid data sources ({@link TConnections}) and record edition component ({@link TConnectionsRecord})
 *   
 * @author terry
 *
 */
public class TConnectionDialog extends UIComponentPanel implements PropertyChangeListener {

	private TConnections connections;
	private TConnectionsRecord connectionsRecord;
	private Record recordModel;
	private Record lastselectRecord;

	public TConnectionDialog() {
		super("connection.profiles.title", true);
		this.connections = new TConnections();
		connections.init();
		connections.setBorder(new TitledBorder(TStringUtils.getBundleString("connection.border.left")));
		connections.setPreferredSize(new Dimension(250, 500));
		connections.setParent(this);

		this.recordModel = connections.getRecordModel();
		this.connectionsRecord = new TConnectionsRecord(recordModel, false);
		connectionsRecord.setBorder(new TitledBorder(TStringUtils.getBundleString("connection.border.right")));
		TUIUtils.setEnabled(connectionsRecord, false);

		setActionBar(new DriverManagerAction(), new CloseAction(this));

		JPanel jp = new JPanel(new BorderLayout(4, 4));
		jp.add(connections, BorderLayout.WEST);
		jp.add(connectionsRecord, BorderLayout.CENTER);
		add(jp);

		connections.addPropertyChangeListener(TConstants.RECORD_SELECTED, this);
		connectionsRecord.addPropertyChangeListener(TConstants.ACTION_PERFORMED, this);
		addPropertyChangeListener(TConstants.ACTION_PERFORMED, this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(TConstants.RECORD_SELECTED)) {
			Record rcd = connections.getRecord();
			TUIUtils.setEnabled(connectionsRecord, rcd != null);
			if (rcd == null) {
				connectionsRecord.showAplicationExceptionMsg(null); // borra mensajes anteriores
				connectionsRecord.setModel(recordModel);
			} else {
				connectionsRecord.setModel(rcd);
				connectionsRecord.preValidate(null);
				// connectionsRecord.setEnabledInputComponent("t_cnname", false);
				lastselectRecord = rcd;
			}
		}
		// nuevo
		if (evt.getNewValue() instanceof NewRecord) {
			connectionsRecord.showAplicationExceptionMsg(null); // borra mensajes anteriores
			connectionsRecord.setModel(recordModel);
			TUIUtils.setEnabled(connectionsRecord, true);
		}

		// aplicar cambios a registro
		if (evt.getNewValue() instanceof ApplyAction) {
			Record newrcd = connectionsRecord.getRecord();
			DBAccess dba = ConnectionManager.getAccessTo(newrcd.getTableName());
			/*
			 * dba.write(newrcd); connections.freshen(); connections.selectRecord(newrcd);
			 */
			Record r = dba.exist(newrcd);
			if (r != null) {
				dba.delete(lastselectRecord);
				dba.add(newrcd);
				lastselectRecord = newrcd;
				connections.freshen();
				connections.selectRecord(newrcd);
			} else {
				showAplicationExceptionMsg("msg03");
			}
		}

		// cerrar ventana
		if (evt.getNewValue() instanceof CloseAction) {
			JDialog jdl = (JDialog) getRootPane().getParent();
			jdl.dispose();
		}
	}
}
