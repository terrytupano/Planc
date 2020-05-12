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
package plugin.planc.compensation;

import gui.docking.*;

import java.awt.event.*;

import net.infonode.docking.*;
import plugin.planc.*;
import action.*;

public class CollaboratorAction extends TAbstractAction {

	public CollaboratorAction() {
		super("WorkforceList", "WorkforceList", NO_SCOPE, null);
		putValue(SLEPlanC.PLANC_ID, 70050L);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		SplitWindow sw1 = new SplitWindow(true, 0.5f,
				DockingContainer.createDynamicView(WorkforceList.class.getName()),
				DockingContainer.createDynamicView(RelativesList.class.getName()));

		// increases
		View[] views = new View[]{DockingContainer.createDynamicView(WorkForceIncrease.class.getName()),
				DockingContainer.createDynamicView(AccountIncrease.class.getName()),
				DockingContainer.createDynamicView(FactsIncrease.class.getName())};
		SplitWindow sw2 = new SplitWindow(true, 0.3f, DockingContainer.createDynamicView(AccountSelection.class
				.getName()), new TabWindow(views));

		SplitWindow sw3 = new SplitWindow(false, 0.5f, sw1, sw2);

		DockingContainer.setWindow(sw3, getClass().getName());
	}
}
