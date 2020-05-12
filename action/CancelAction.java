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

/** Accion estandar para botone <code>Cancelar</code>. 
 * 
 */
public class CancelAction extends RedirectAction implements DefaultCancelAction{
	
	/** nueva accion
	 * 
	 * @param ca - accion de para confirmacion
	 */
	public CancelAction(UIComponentPanel uip) {
		super(uip);
	}	
}

