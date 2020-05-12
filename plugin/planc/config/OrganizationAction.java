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

public class OrganizationAction extends TAbstractAction {

	public OrganizationAction() {
		super("module.config.ORGANIZACIONAL", "module.config.ORGANIZACIONAL", NO_SCOPE, null);
		putValue(SLEPlanC.PLANC_ID, 80010L);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {

		// incrementos x cargo e incrementos x tabulador
		SplitWindow sw1 = new SplitWindow(false, 0.5f, DockingContainer.createDynamicView(TabulatorsJobsIncrease.class
				.getName()), DockingContainer.createDynamicView(TabulatorsIncrease.class.getName()));

		// tabuladores y clasificacion de cargos
		SplitWindow sw2 = new SplitWindow(false, 0.5f, DockingContainer.createDynamicView(TabulatorsTree.class
				.getName()), DockingContainer.createDynamicView(ClasificationList.class.getName()));

		// jobs
		View accv = DockingContainer.createDynamicView(JobsTree.class.getName());

		SplitWindow sw3 = new SplitWindow(true, 0.5f, sw2, sw1);
		SplitWindow sw4 = new SplitWindow(true, 0.3f, accv, sw3);

		DockingContainer.setWindow(sw4, getClass().getName());
	}
}
