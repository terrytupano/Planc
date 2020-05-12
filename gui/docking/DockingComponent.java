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

import java.beans.*;

/**
 * Interface que debe ser implementada por todo componente que desee ser elegible para ser o estar contentido dentro de
 * una viasta (ver net.infornode.docking.view).
 * 
 * las propidedades notificadas por<code>propertyChange(PropertyChangeEvent)</code> para las clases interesadas estan
 * contenidas dentro de esta misma clase.
 * 
 * es responsabilidad del creador del interezado en cambio de propiedades que el generador de los cambios implemente la
 * actualizacion de la propiedad
 * 
 * 
 */
public interface DockingComponent extends PropertyChangeListener {

	/**
	 * este metodo es invocado al momento que un componente es adicionado a la perspectiva actual.
	 * 
	 */
	public void init();
}
