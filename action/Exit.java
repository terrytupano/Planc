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

import core.*;
import core.datasource.*;

/**
 *  ends the apliacations finish all transactions, savin status and ends database connections
 *  
 * @author terry
 *
 */
public class Exit extends TAbstractAction {
	
	public Exit() {
		super(TAbstractAction.NO_SCOPE);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		shutdown();
	}

	/**
	 * ends apliactins
	 */
	public static void shutdown() {
		shutdown1();
		System.exit(0);
	}	

	/**
	 * restart apliacation
	 * 
	 */
	public static void restarApplication() {
		try {
			shutdown1();
			// espera para hasta q finalizen todos los subsys
			Thread.sleep(2000);
			Runtime.getRuntime().exec("cmd /c start /MIN restart.bat");
		} catch (Exception e) {
			SystemLog.logException1(e, false);
		}
	}

	/**
	 * shutdown session and database connections.
	 * 
	 */
	private static void shutdown1() {
		PlanC.saveProperty();
		//170425: save last view in dockingcontainer
		if (Session.inSession()) {
			// can be invoked in signin dialog (user = null)
			DockingContainer dc = DockingContainer.getInstance();
			// my be null if runninmode = console
			if (dc!=null) {
				DockingContainer.saveView(null);
			}
		}
		ConnectionManager.shutdown();
		TPreferences.sendMessage(TPreferences.IS_RUNNING, "false");
	}
}
