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
/* 
 * Copyright (c) 2003 Arnaldo Fuentes. Todos los derechos reservados.
 */

package action;

import java.awt.event.*;

import core.*;


/** Accion destinada a preparar y presentar la ayuda de la aplicacion.
 * 
 */
public class Help extends TAbstractAction {


	/** nueva instancia
	 * 
	 */
	public Help() {
		super("action.help", "help", TAbstractAction.NO_SCOPE, null);

	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		PlanC.setContentPane(PlanC.HELP);
	}
}
