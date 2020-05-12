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
package plugin.planc.config;

import gui.docking.*;

import java.awt.event.*;

import net.infonode.docking.*;
import plugin.planc.*;
import action.*;

public class GeneralAction extends TAbstractAction {

	public GeneralAction() {
		super("module.config.general", "module.config.general", NO_SCOPE, null);
		putValue(SLEPlanC.PLANC_ID, 80004L);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// increas by bu and by category value
		SplitWindow sw1 = new SplitWindow(false, 0.5f, DockingContainer.createDynamicView(AccountCategoryIncrease.class
				.getName()), DockingContainer.createDynamicView(AccountBuIncrease.class.getName()));

		// categorytree and business tree
		SplitWindow sw2 = new SplitWindow(false, 0.5f,
				DockingContainer.createDynamicView(CategoryTree.class.getName()),
				DockingContainer.createDynamicView(BuTree.class.getName()));

		// account view
		View accv = DockingContainer.createDynamicView(AccountTree.class.getName());

		SplitWindow sw3 = new SplitWindow(true, 0.5f, sw2, sw1);
		SplitWindow sw4 = new SplitWindow(true, 0.3f, accv, sw3);

		DockingContainer.setWindow(sw4, getClass().getName());
	}
}
