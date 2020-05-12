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
 */

package gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import core.*;
import core.datasource.*;

/**
 * {@link JTable} with support for save column width
 * 
 */
public class TJTable extends JTable {

	private TAbstractTableModel sortableModel;
	private TableColumnModel columnModel;
	protected String tosavewidth;
	private boolean init;
	private String classname;
	private UIListPanel uiListPanel;
	private boolean isTranspose;

	/**
	 * nuena instancia
	 * 
	 * @param src - instancia de <code>UIListPanel</code> que contiene a esta clase
	 * @param tm - modelo de datos
	 */
	public TJTable(UIListPanel src, TAbstractTableModel tm) {
		super(tm);
		this.init = true;
		this.sortableModel = tm;
		this.uiListPanel = src;
		this.classname = src.getClass().getName();

		// Column model. for transpose table, is ignored
		isTranspose = uiListPanel.getClientProperty(TConstants.TRANSPORSE_COLUMN) != null;
		if (!isTranspose) {
			String[] cols = ((String) uiListPanel.getClientProperty(TConstants.SHOW_COLUMNS)).split(";");
			DefaultTableColumnModel dtcm = new DefaultTableColumnModel();
			Record mod = sortableModel.getRecordModel();
			for (String col : cols) {
				dtcm.addColumn(new TableColumn(mod.getIndexOf(col)));
			}
			setColumnModel(dtcm);

			TableRowSorter sorter = new TableRowSorter(sortableModel);
			setRowSorter(sorter);
			sortableModel.setTableRowSorter(sorter);

			fixTableColumn();
			this.columnModel = getColumnModel();
			this.init = false;
		}
	}

	@Override
	public TableModel getModel() {
		return sortableModel;
	}

	@Override
	public void columnMarginChanged(ChangeEvent e) {
		super.columnMarginChanged(e);

		// actualiza valores a salvar (no en inicializacion)
		if (init || isTranspose) {
			return;
		}
		StringBuffer val = new StringBuffer();
		for (int k = 0; k < columnModel.getColumnCount(); k++) {
			TableColumn tc = columnModel.getColumn(k);
			val.append(tc.getWidth());
			val.append(k == columnModel.getColumnCount() - 1 ? "" : ";");
		}
		this.tosavewidth = val.toString();
		TPreferences.setPreference(TPreferences.TABLE_COLUMN_WIDTH, classname, tosavewidth);
	}

	/**
	 * este metodo altera la instancia de <code>TableColumnsModel</code> para ajustar el ancho de la columna. el acncho
	 * es determinado seleccionando el mas largo entre el titulo de la columna o el maximo ancho de su contenido
	 * 
	 */
	private void fixTableColumn() {
		TableColumnModel cm = getColumnModel();
		Record mod = sortableModel.getRecordModel();

		// verifica si exite una preferencia de ancho de columna salvada para esta clase
		String vals[] = null;
		String v = (String) TPreferences.getPreference(TPreferences.TABLE_COLUMN_WIDTH, classname, null);
		if (v != null) {
			this.tosavewidth = v;
			vals = tosavewidth.split(";");
		}

		// ancho de las columnas. se toma el mayor entre el encabezado y la longitud del campo o
		// el valor salvado y el minimo entre este y 100
		for (int c = 0; c < cm.getColumnCount(); c++) {
			TableColumn tc = cm.getColumn(c);
			String en = TStringUtils.getBundleString(mod.getFieldName(tc.getModelIndex()));
			tc.setHeaderValue(en);
			if (vals == null || c > vals.length) {
				int siz = en.length() > mod.getFieldSize(c) ? en.length() : mod.getFieldSize(c);
				tc.setPreferredWidth(Math.min(siz * 9, 100 * 9));
			} else {
				if (c < vals.length) {
					tc.setPreferredWidth(Integer.parseInt(vals[c]));
				}
			}
		}
	}
}
