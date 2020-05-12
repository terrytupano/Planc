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

import java.awt.event.*;

import javax.swing.*;

import core.*;
import core.datasource.*;

/**
 * test implementation for futher migration of all actions framework
 * 
 */
public class DeleteRecord2 extends TAbstractAction implements NoActionForSpecialRecord {

	public DeleteRecord2(EditableList el) {
		super(TAbstractAction.RECORD_SCOPE);
		editableList = el;
		setIcon("DeleteRecord");
		messagePrefix = "action.delete.";
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object[] options = {TStringUtils.getBundleString(messagePrefix + "confirm"),
				TStringUtils.getBundleString(messagePrefix + "cancel")};
		int o = JOptionPane.showOptionDialog(PlanC.frame, TStringUtils.getBundleString(messagePrefix + "message"),
				TStringUtils.getBundleString(messagePrefix + "title"), JOptionPane.DEFAULT_OPTION,
				JOptionPane.WARNING_MESSAGE, null, options, options[1]);
		if (o == JOptionPane.YES_OPTION) {
			actionPerformed2();
		}
	}

	@Override
	public void actionPerformed2() {
		Record rcd = editableList.getRecord();
		boolean ok = ConnectionManager.getAccessTo(rcd.getTableName()).delete(rcd);
		if (ok) {
			editableList.freshen();
		}
	}
}
