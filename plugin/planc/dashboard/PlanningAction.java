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
package plugin.planc.dashboard;

import gui.docking.*;

import java.awt.event.*;

import action.*;
import core.*;

public class PlanningAction extends TAbstractAction {

	public PlanningAction() {
		super(NO_SCOPE);
//		putValue(SLEPlanC.PLANC_ID, 70890L);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		PlanC.setEnableJmenubar(true);
		DockingContainer.performTransition(DockingContainer.getRootWindow());
	}
}
