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
/**
 * Copyright (c) Terry - All right reserved. PROPRIETARY/CONFIDENTIAL.
 * Use is subject to license terms.
 *
 * @author Terry
 *
 */

package gui.docking;

import gui.*;

import java.beans.*;

import javax.swing.*;

import core.*;

/**
 * panel de bienvenida TODO: implementar clic
 * 
 * 
 */
public class Wellcome extends UIComponentPanel implements DockingComponent {

	private JEditorPane editorPane;

	/**
	 * nueva instancia
	 * 
	 * 
	 */
	public Wellcome() {
		super(null, false);
		this.editorPane = new JEditorPane();
		try {
			editorPane.setEditable(false);
			editorPane.setPage(TResourceUtils.getURL("wellcome"));
		} catch (Exception e) {

		}
		addWithoutBorder(editorPane);
	}

	@Override
	public void init() {

	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

	}
}
