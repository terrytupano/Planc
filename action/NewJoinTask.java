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

import gui.*;

import java.awt.event.*;

import javax.swing.*;

import core.*;
import core.datasource.*;

/**
 * create a new task that is the result of a sequential execution of others task. 
 *  
 * @author terry
 *
 */
public class NewJoinTask extends TAbstractAction {

	private UIListPanel uiListPanel;

	public NewJoinTask(UIListPanel adi) {
		super("action.task.newjoin", "join", TABLE_SCOPE, "ttaction.task.newjoin");
		this.uiListPanel = adi;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object[] options = {TStringUtils.getBundleString("action.task.inexe"),
				TStringUtils.getBundleString("action.task.bgexe"),
				TStringUtils.getBundleString("action.delete.cancel")};
		int opt = JOptionPane.showOptionDialog(null, TStringUtils.getBundleString("action.task.ms03"),
				TStringUtils.getBundleString("action.task.title"), JOptionPane.DEFAULT_OPTION,
				JOptionPane.WARNING_MESSAGE, null, options, options[2]);
		if (opt < 2) {
			Record rtta = uiListPanel.getRecord();
//			TTaskManager.submitTask(rtta, opt == 0);
		}
	}
}
