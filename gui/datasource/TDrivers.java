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

import javax.swing.*;

import core.*;
import core.datasource.*;
import core.reporting.*;

import action.*;

/**
 * drivers list form t_drivers
 * 
 * @author terry
 * 
 */
public class TDrivers extends UIListPanel {

	public TDrivers() {
		super(null);
		setToolBar(new CloneRecord(this), new ExportToFileAction(this, ""));
		putClientProperty(TConstants.SHOW_COLUMNS, "t_drname;T_DRCORP");
		putClientProperty(TConstants.ICON_PARAMETERS, "0;drivers_;t_drname");
	}

	@Override
	public void init() {
		setServiceRequest(new ServiceRequest(ServiceRequest.DB_QUERY, "t_drivers", null));
		setView(LIST_VIEW_VERTICAL);
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		/*
		 * if (aa instanceof NewRecord) { pane = new TDriverManagerRecord(getRecordModel(), true); } if (aa instanceof
		 * EditRecord) { pane = new TDriverManagerRecord(getRecord(), false); }
		 */
		return pane;
	}
}
