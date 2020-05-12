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
package plugin.planc.accounting;

import gui.docking.*;

import java.awt.event.*;

import net.infonode.docking.*;
import plugin.planc.*;
import action.*;

public class AccountAction extends TAbstractAction {

	public AccountAction() {
		super("module.accounting.contable", "module.accounting.contable", NO_SCOPE, null);
		putValue(SLEPlanC.PLANC_ID, 80050L);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// 180126 (bug1806) : reestructured perspective
		SplitWindow sw1 = new SplitWindow(false, 0.6f,
				DockingContainer.createDynamicView(CostTypesList.class.getName()),
				DockingContainer.createDynamicView(CostCenterList.class.getName()));

		SplitWindow sw3 = new SplitWindow(true, 0.5f, sw1,
				DockingContainer.createDynamicView(AccountingAccountList.class.getName()));

		DockingContainer.setWindow(sw3, getClass().getName());
	}
}
