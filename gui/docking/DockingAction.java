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
package gui.docking;

import java.awt.event.*;

import action.*;

/**
 * action for append a dynamic view to {@link DockingContainer} simply passing the class (must be instance of
 * {@link DockingComponent}) to instaciate when user select the action
 * 
 * @author terry
 * 
 */
public class DockingAction extends TAbstractAction {

	private String cname;

	public DockingAction(Class cls) {
		super(cls.getSimpleName(), cls.getSimpleName(), NO_SCOPE, "tt" + cls.getSimpleName());
		this.cname = cls.getName();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		DockingContainer.addNewDynamicView(cname);
	}
}
