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

package gui;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import core.*;
import core.datasource.*;

/**
 * Extension de <code>DefaultTableCellRenderer</code> que usa una instancia de <code>ExtendedJLabel</code> para
 * presentar diversos valores. adicionalmente esta clase establece 2 colores por omision para presentar las celdas pares
 * e impares en distintos colores permitiendo mejor visibilidad.
 * 
 * 
 */
public class TDefaultTableCellRenderer extends DefaultTableCellRenderer {

	// private Color odd_color;
	private ExtendedJLabel extendedJLabel;
	private int newRendererColum, iconColum;
	private TableCellRenderer newRenderer;
	private ImageIcon imageIcon;
	private String iconName, valColumn;
	private java.util.Hashtable<Integer, String> formats;
	private Font font = UIManager.getFont("Table.font");
	private Font fontBold = font.deriveFont(Font.BOLD);

	public TDefaultTableCellRenderer() {
		this.formats = new Hashtable<Integer, String>();
		// ExtendedJLabel is used for parsing values
		this.extendedJLabel = new ExtendedJLabel("");
//		this.pair_color = UIManager.getColor("Table.background");
		// this.odd_color = UIManager.getColor("Label.background");
		setIconParameters(-1, null, null);
		setNewCellRenderer(-1, null);
	}

	public void setFormat(Hashtable ffc) {
		formats = ffc;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		TAbstractTableModel stm = (TAbstractTableModel) table.getModel();

		// FIXME: 171211 remove all transpose references
		Record r = stm.isTranspose() ? stm.getRecordAt(column) : stm.getRecordAt(row);

		// 171211: commented in order to enable reference by columns implemented in tablemodel.
		// FIXME: 171211 remove all external references. this class must only decarate the cell with the incoming values
		// String fn = showColumns[stm.isTranspose() ? row : column];
		// Object valuer = r.getExternalFieldValue(fn);

		// lookup for pattenr
		String patt = formats.get(column);
		if (patt != null) {
			extendedJLabel.setFormat(value.getClass(), patt);
		}
		extendedJLabel.setValue(value);
		setText(extendedJLabel.getText());
		setHorizontalAlignment(extendedJLabel.getHorizontalAlignment());
		setOpaque(true);
		// temporal??: if value is double and value < 9.00, show diferenciable font to remark the cell. Some class has
		// too many doubles and is difficult to see the number from #.00 format
		// 180813: REMOVED
//		setFont(font);
//		if (value instanceof Double) {
//			double d = (Double) value;
//			if (d < 9 && d > 0) {
//				setFont(fontBold);
//			}
//		}
		if (!isSelected) {
			// setBackground((row % 2 == 0) ? pair_color : odd_color);
		}
		// redireccion de cellrenderer
		if (column == newRendererColum) {
			JComponent render = (JComponent) newRenderer.getTableCellRendererComponent(table, value, isSelected,
					hasFocus, row, column);
			render.setBackground(getBackground());
			return render;
		}
		// columna para presentar icono
		setIcon(null);
		if (column == iconColum) {
			// icono segun columna valor

			if (valColumn != null) {
				if (iconName.equals("*")) {
					imageIcon = new ImageIcon((byte[]) r.getFieldValue(valColumn));
					Image i = imageIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
					imageIcon = new ImageIcon(i);
				} else {
					String val = stm.getRecordAt(row).getFieldValue(valColumn).toString();
					imageIcon = TResourceUtils.getSmallIcon(iconName + val);
				}
			}
			setIcon(imageIcon);
		}
		return this;
	}

	/**
	 * set the icon parameters for this cell renderer.
	 * 
	 * @param icol - identificador de la columna donde se presentara el icono
	 * @param icon - icon file name, prefix or "*"
	 * @param valCol - field name to complete the icon file name, or contain data necesary to create an imageIcon with
	 *        it
	 * 
	 * @see TConstants#ICON_PARAMETERS
	 * 
	 */
	public void setIconParameters(int icol, String icon, String valCol) {
		this.iconColum = icol;
		this.iconName = icon;
		this.valColumn = valCol;
		if (valCol == null) {
			imageIcon = TResourceUtils.getSmallIcon(iconName);
		}
	}

	/**
	 * establece una instancia diferente de <code>TableCellRenderer</code>. esto permita que una instancia de esta clase
	 * instalada dentro de una tabla, redirija la llamada a otra instancia de TableCellRenderer para entregar un nuevo
	 * componente sin alterar el aspecto general, principalmente los colores de celdas pare e impares y la alineacion
	 * 
	 * @param col - columna
	 * @param nren - instancia de TableCellRenderer
	 */
	public void setNewCellRenderer(int col, TableCellRenderer nren) {
		this.newRenderer = nren;
		this.newRendererColum = col;
	}
}
