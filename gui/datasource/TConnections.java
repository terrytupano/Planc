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
 * list for record from T_CONNECTIONS file
 * 
 * @author terry
 * 
 */
public class TConnections extends UIListPanel {

	public TConnections() {
		super(null);
		setToolBar(new NewRecord(this), new CloneRecord(this), new DeleteRecord(this), new ExportToFileAction(this, 
				""));
		putClientProperty(TConstants.SHOW_COLUMNS, "T_CNNAME;T_CNDRIVER;T_CNURL");
		putClientProperty(TConstants.ICON_PARAMETERS, "0;drivers_;t_cndrname");
	}

	@Override
	public void init() {
		setServiceRequest(new ServiceRequest(ServiceRequest.DB_QUERY, "t_connections", null));
		setView(LIST_VIEW_VERTICAL);
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;

		return pane;
	}
}
