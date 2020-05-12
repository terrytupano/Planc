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
package action;

import gui.docking.*;

import java.awt.event.*;

import javax.swing.*;

import core.*;

/**
 * Save actual view. this action is for configuration only.
 * 
 * @see gui.docking.DockingContainer#loadView(String)
 */
public class SaveView extends TAbstractAction {

	/**
	 * nueva accion
	 * 
	 */
	public SaveView() {
		super("main.menuitem.saveview", null, TAbstractAction.NO_SCOPE, null);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		String s = (String) JOptionPane.showInputDialog(PlanC.frame, "username or template id for this view:",
				"Save View", JOptionPane.PLAIN_MESSAGE);
		if ((s != null) && (s.length() > 0)) {
			DockingContainer.saveView(s);
		}
	}
}
