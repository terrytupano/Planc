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

public class ScenarioAction extends TAbstractAction {

	public ScenarioAction() {
		super("module.config.scenarios", "module.config.escenario", NO_SCOPE, null);
		putValue(SLEPlanC.PLANC_ID, 70110L);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		View agt = DockingContainer.createDynamicView(AccountGeneration.class.getName());
		View gsi = DockingContainer.createDynamicView(GeneralSalaryIncrease.class.getName());
		SplitWindow sw1 = new SplitWindow(false, 0.6f, agt, gsi);

		// tabs for increases
		View[] incs = new View[]{DockingContainer.createDynamicView(GeneralAccountIncrease.class.getName()),
				DockingContainer.createDynamicView(PresumptionIncrease.class.getName()),
				DockingContainer.createDynamicView(SalaryCategoryIncrease.class.getName()),
				DockingContainer.createDynamicView(ScalesMagnitudes.class.getName())
		};
		TabWindow inc = new TabWindow(incs);

		// escalas, incrementos
		SplitWindow sw2 = new SplitWindow(false, 0.6f, DockingContainer.createDynamicView(ScaleList.class.getName()),
				inc);

		SplitWindow sw3 = new SplitWindow(true, 0.4f, sw1, sw2);

		DockingContainer.setWindow(sw3, getClass().getName());
	}
}
