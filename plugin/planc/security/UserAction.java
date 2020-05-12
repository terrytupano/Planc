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
package plugin.planc.security;

import gui.docking.*;

import java.awt.event.*;

import net.infonode.docking.*;
import plugin.planc.*;
import action.*;

public class UserAction extends TAbstractAction {

	public UserAction() {
		super("UserList", "UserList", NO_SCOPE, null);
		putValue(SLEPlanC.PLANC_ID, 50020L);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		SplitWindow sw = new SplitWindow(true, 0.5f, DockingContainer.createDynamicView(UserList.class.getName()),
				DockingContainer.createDynamicView(UserAutorizationsTree.class.getName()));
		DockingContainer.setWindow(sw, getClass().getName());
	}
}
