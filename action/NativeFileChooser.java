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

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;

import core.*;

public class NativeFileChooser extends TAbstractAction {

	public static int OPEN_STYLE = 0;
	public static int SAVE_STYLE = 1;
	private int style;
	private FileDialog fileDialog;
	private PropertyChangeListener listener;

	public NativeFileChooser(int stl, PropertyChangeListener l) {
		super(stl == OPEN_STYLE ? "filechooser.open" : "filechooser.save", stl == OPEN_STYLE ? "folder" : "disk_blue",
				TAbstractAction.NO_SCOPE, stl == OPEN_STYLE ? "ttfilechooser.open" : "ttfilechooser.save");
		this.style = stl;
		this.listener = l;
		this.fileDialog = new FileDialog(PlanC.frame, null, style);
		fileDialog.setMultipleMode(false);
		addPropertyChangeListener(listener);

	}

	public void addChoosableFileFilter(String nam, String... ext) {
		TFileNameFileter fnf = new TFileNameFileter(ext);
		fileDialog.setFilenameFilter(fnf);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		fileDialog.setVisible(true);
		String sf = fileDialog.getFile();
		String sd = fileDialog.getDirectory();
		if (sf != null || sf != null) {
			File nf = new File(sd + sf);
			firePropertyChange(TConstants.FILE_SELECTED, null, nf);
			// fileDialog.setDirectory(sd);
		}
	}
	class TFileNameFileter implements FilenameFilter {

		String[] extens;

		TFileNameFileter(String... ext) {
			this.extens = ext;
		}
		@Override
		public boolean accept(File dir, String name) {
			boolean a = false;
			for (String ext : extens) {
				a = (name.endsWith(ext)) ? true : a;
			}
			return a;
		}

	}
}
