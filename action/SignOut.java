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

import core.*;

/**
 * ends actual session.
 * 
 * @author terry
 *
 */
public class SignOut extends TAbstractAction {
	
	public SignOut() {
		super("main.menuitem.logout", null, TAbstractAction.NO_SCOPE, null);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		signOut();
	}
	
	/**
	 * Static method that perform the sign out of the application.
	 * 
	 */
	public static void signOut(){
		PlanC.saveProperty();
		TAbstractAction.shutdown();
		Session.setUser(null);
	}
}
