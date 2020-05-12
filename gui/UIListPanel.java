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
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import action.*;

import com.alee.extended.date.*;
import com.alee.laf.table.editors.*;
import com.alee.laf.text.*;
import com.alee.managers.notification.*;

import core.*;
import core.datasource.*;
import core.tasks.*;

/**
 * Centraliza el manejo de operaciones comunes a todas las subclases que tienen como funcion presentar una lista (o
 * conjuto) de registros para que el usuario pueda realizar operaciones con ellos.
 * 
 * si el usuario selecciona un elemento dentro de la tabla, se cambia la propiedad
 * putClientProperty(PropertyNames.RECORD_SELECTED, r);
 * 
 * NOTA: subclases DEBEN usar metodo init() para establecer solicitud de servicio dado que las autorizaciones ejecutan
 * cls.newInstance() y esto genera carga inesesaria en la base de datos
 * 
 * 
 */
public abstract class UIListPanel extends UIComponentPanel
		implements
			Exportable,
			EditableList,
			ListSelectionListener,
			ActionPerformer,
			TableModelListener {

	public static int TABLE_VIEW = 0;
	public static int LIST_VIEW_VERTICAL = 1;
	public static int LIST_VIEW_MOSAIC = 2;
	private int view;
	private JScrollPane js_pane;
	private TAbstractTableModel tableModel;
	private JTable tJTable;
	private TAbstractListModel listModel;
	private TJList tJlist;
	private String specialFieldID;
	private Hashtable<String, Hashtable> referenceColumns;
	private Hashtable<Integer, String> formatForColumns;
	private ServiceRequest serviceRequest, filterRequest;

	/**
	 * nueva instancia
	 * 
	 * @param dname - nombre del documento
	 */
	public UIListPanel(String dname) {
		super(dname, false);
		this.js_pane = new JScrollPane();
		referenceColumns = new Hashtable();
		formatForColumns = new Hashtable();
		// better look for weblaf
		js_pane.setBorder(null);
		js_pane.getViewport().setBackground(Color.WHITE);

		// this.view = LIST_VIEW_MOSAIC;
		// this.view = LIST_VIEW_VERTICAL;
		this.view = TABLE_VIEW;
		// para el borde
		Box b1 = Box.createVerticalBox();
		b1.add(js_pane);
		// addWithoutBorder(b1);
		addWithoutBorder(js_pane);
	}

	public void setView(int nv) {
		this.view = nv;
		if (view == TABLE_VIEW) {
			js_pane.setViewportView(tJTable);
		}
		if (view == LIST_VIEW_MOSAIC || view == LIST_VIEW_VERTICAL) {
			if (tJlist != null) {
				tJlist.setLayoutOrientation((view == LIST_VIEW_MOSAIC) ? JList.HORIZONTAL_WRAP : JList.VERTICAL);
				if (view == LIST_VIEW_MOSAIC) {
					// js_pane = new JScrollPane(tJlist, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
					js_pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
					js_pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
					// js_pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
					tJlist.revalidate();
				}
				js_pane.setViewportView(tJlist);
			}
		}
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		// puede venir desde varios listener
		if (e.getSource() == tableModel) {
			tJTable.tableChanged(e);
			// tJlist.ensureIndexIsVisible(sortableModel.getRowCount());
			enableRecordScopeActions(!tJTable.getSelectionModel().isSelectionEmpty());
		}
	}

	/**
	 * Habilita/desabilita todas las acciones de tipo <code>TAbstractAction.RECORD_SCOPE</code> disponibles dentro de la
	 * barra de herramientas y menus emergentes. este metodo adicionalmente: <li>verifica la columna para valores
	 * especiales. si esta fue especificada, y el parametro de entrada es <code>true</code> (habilitar acciones) las
	 * acciones que sean instancias de <code>NoActionForSpecialRecord</code> seran inhabilitadas <li>para vista en
	 * arbol, Si el registro seleccionado es un nodo con hijos, instancias de <code>NoActionForSpecialRecord</code> no
	 * seran habilitadas (Ej: no se puede suprimir nodos con hijos.)
	 * 
	 * 
	 * @param ena - true: habilitar, false: deshabilitar
	 */
	protected void enableRecordScopeActions(boolean ena) {
		TAbstractAction[] btns = getToolBarActions();
		if (btns != null) {
			// si se especifico columna especial y el registro seleccionado actualmente lo contiene
			boolean isSpetialRecord = false;
			if (ena) {
				Record selr = getRecord();
				isSpetialRecord = (specialFieldID != null)
						&& ((String) selr.getFieldValue(specialFieldID)).startsWith("*");
			}

			for (int j = 0; j < btns.length; j++) {
				TAbstractAction ac = (TAbstractAction) btns[j];
				if (ac.getScope() == TAbstractAction.RECORD_SCOPE) {
					// if action must be enabled, check for autorization
					ac.setEnabled(ena ? Session.isAutorizedForAction(ac) : false);
				}
			}
		}
	}

	/**
	 * Asociate a sublist of values for the internal value in this model. when the column value is request by JTable,
	 * the internal value is mapped whit this list to return the meaning of the value instead the value itselft
	 * 
	 * @param col - column
	 * @param telist - list of elements to look for replace text
	 */
	public void setReferenceColumn(String fn, TEntry[] telist) {
		Hashtable ht = new Hashtable();
		for (TEntry te : telist) {
			ht.put(te.getKey(), te.getValue());
		}
		referenceColumns.put(fn, ht);
	}

	/**
	 * Store the format pattern for given column. this format is apply when the table invoke
	 * getTableCellRendererComponent method.
	 * 
	 * @param col - column id where the pattern need to be apply
	 * @param patt - String pattern to apply
	 * 
	 * @see TDefaultTableCellRenderer#getTableCellRendererComponent(JTable, Object, boolean, boolean, int, int)
	 */
	public void setFormattForColums(int col, String fmt) {
		formatForColumns.put(col, fmt);
	}

	public void clearFormattForColums() {
		formatForColumns.clear();
	}

	@Override
	public boolean executeAction(TActionEvent event) {

		boolean ok = true;

		// si la accion de redireccion es alguna instancia de acciones de cancelacion, se
		// retorna sin hacer nada.

		if (event.getRedirectAction() instanceof DefaultCancelAction) {
			return true;
		}

		// estandar edicion
		if (event.getSource() instanceof EditRecord) {
			AbstractRecordDataInput ardi = (AbstractRecordDataInput) event.getData();
			Record r = ardi.getRecord();
			ServiceConnection.sendTransaction(ServiceRequest.DB_UPDATE, r.getTableName(), r);
		}

		// validacion y creacion de nuevo registro
		if (event.getSource() instanceof NewRecord) {
			AbstractRecordDataInput ardi = (AbstractRecordDataInput) event.getData();
			ardi.validateNewRecord();
			ok = !ardi.isShowingError();
			if (!ardi.isShowingError()) {
				Record r = ardi.getRecord();
				ServiceConnection.sendTransaction(ServiceRequest.DB_ADD, r.getTableName(), r);
			}
		}

		// estandar supresion de registro
		if (event.getSource() instanceof DeleteRecord) {
			Object[] options = {TStringUtils.getBundleString("action.delete.confirm"),
					TStringUtils.getBundleString("action.delete.cancel")};
			int o = JOptionPane.showOptionDialog(PlanC.frame, TStringUtils.getBundleString("action.delete.message"),
					TStringUtils.getBundleString("action.delete.title"), JOptionPane.DEFAULT_OPTION,
					JOptionPane.WARNING_MESSAGE, null, options, options[1]);
			if (o == JOptionPane.YES_OPTION) {
				Record rcd = getRecord();
				ok = ConnectionManager.getAccessTo(rcd.getTableName()).delete(rcd);
				/*
				 * Record[] rcds = getRecords(); for (int rc = 0; rc < rcds.length; rc++) { ok =
				 * ConnectionManager.getAccessTo(rcds[rc].getTableName()).delete(rcds[rc]); }
				 */
			} else {
				ok = false;
			}
		}

		// estandar filtro por campos.

		// operacion exitosa? refrescar
		if (ok) {
			freshen();
		}
		return ok;
	}

	/**
	 * default implementation for {@link FilterAction}. this method set the {@link ServiceRequest#FILTER_FIELDS} and
	 * {@link ServiceRequest#FILTER_VALUE} parameters for this instance of {@link ServiceRequest} and send the
	 * transaction to retrive the filter result.
	 * <p>
	 * 180312: the previous implementatin based on ServiceRequest.FILTER_FIELDS and ServiceRequest.FILTER_VALUE are
	 * deprecated: in order to unify the future filter interface like tree does, the fielter action act over the lists
	 * of elements that already present on screen
	 * 
	 * @param txt - text to look for or "" to clear the tilter
	 */
	public void filterList(String txt) {
		String cols = (String) getClientProperty(TConstants.SHOW_COLUMNS);
		if (cols == null) {
			throw new NoSuchElementException("property SHOW_COLUMNS must be present");
		}
		// sortablemodel may be null if list depend on another component selection.
		if (tableModel != null) {
			// perform data filter
			if (!txt.trim().toString().equals("")) {
				Vector<Record> rlst = tableModel.getRecords();
				Record rm = tableModel.getRecordModel();
				TransactionsUtilities.filterList(rlst, cols, txt);
				filterRequest = new ServiceRequest(ServiceRequest.CLIENT_GENERATED_LIST, rm.getTableName(), rlst);
				filterRequest.setParameter(ServiceResponse.RECORD_MODEL, rm);
				tableModel.setServiceRequest(filterRequest);
			} else {
				// reset with the original model
				tableModel.setServiceRequest(serviceRequest);
			}
			/*
			 * tableModel.getServiceRequest().setParameter(ServiceRequest.FILTER_FIELDS, cols);
			 * tableModel.getServiceRequest().setParameter(ServiceRequest.FILTER_VALUE, txt); freshen();
			 */
		}
	}

	public void freshen() {
		tableModel.freshen();
		listModel.freshen();
		listModel = new TAbstractListModel(tableModel);
		tJlist.setModel(listModel);
		// 171231: if no element to display, disable actions
		if (tableModel.getRowCount() == 0) {
			enableRecordScopeActions(false);
		}
	}

	public Record getRecord() {
		Record r = null;
		int sr = -1;
		sr = (view == TABLE_VIEW) ? tJTable.getSelectedRow() : -1;
		sr = (view == LIST_VIEW_MOSAIC || view == LIST_VIEW_VERTICAL) ? tJlist.getSelectedIndex() : sr;
		// verifico sortablemodel.getrowcount() porque si el modelo cambia (evento tablechange)
		// el contenido de la table no ha cambiado y puede generar error
		// ArrayIndexOutOfBoundsException
		if (sr > -1 && sr < tableModel.getRowCount()) {
			r = tableModel.getRecordAt(sr);
		}
		return r;
	}

	/**
	 * localiza el registro pasado como argumento y si este se encuentra dentro de la lista, lo selecciona
	 * 
	 * @param rcd - registro a seleccionar
	 */
	public void selectRecord(Record rcd) {
		int idx = tableModel.indexOf(rcd);
		enableRecordScopeActions(idx > -1);
		if (idx > -1) {
			tJTable.getSelectionModel().setValueIsAdjusting(true);
			tJTable.getSelectionModel().removeIndexInterval(0, tableModel.getRowCount());
			tJTable.getSelectionModel().addSelectionInterval(idx, idx);
			tJTable.getSelectionModel().setValueIsAdjusting(false);
		}
	}

	/**
	 * atajo que retorna el registro modelo usado en <code>SortableTableModel</code> este metodo es igual a
	 * <code>getSortableTable().getSortableTableModel().getModel()</code>
	 * 
	 * @return registro
	 */
	public Record getRecordModel() {
		return tableModel.getRecordModel();
	}

	public TAbstractTableModel getTableModel() {
		return tableModel;
	}

	public JTable getJTable() {
		return tJTable;
	}

	public Record[] getRecords() {
		int[] ridx = tJTable.getSelectedRows();
		if (view == LIST_VIEW_MOSAIC || view == LIST_VIEW_VERTICAL) {
			ridx = tJlist.getSelectedIndices();
		}
		Record[] rcds = new Record[ridx.length];
		for (int rc = 0; rc < ridx.length; rc++) {
			rcds[rc] = tableModel.getRecordAt(ridx[rc]);
		}
		return rcds;
	}

	@Override
	public ServiceRequest getServiceRequest() {
		return serviceRequest;// tableModel.getServiceRequest();
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		// soporte basico para seleccion de elementos dentro de la tabla/lista o arbol.
		if (e.getValueIsAdjusting()) {
			return;
		}
		enableRecordScopeActions(!((ListSelectionModel) e.getSource()).isSelectionEmpty());
		firePropertyChange(TConstants.RECORD_SELECTED, null, getRecord());
	}

	/**
	 * establece una nueva transaccion que se encargara de mantener actualizado el modelo de datos para esta tabla. use
	 * este metodo si desea modificar a voluntad el servicio que se desea presentar por esta tabla. si el valor para el
	 * servicio es null, se coloca un panel en blanco. y se inhabilitan todas las acciones.
	 * 
	 * @param sr - solicitud de servicio.
	 */
	protected void setServiceRequest(ServiceRequest sr) {
		if (sr != null) {
			this.serviceRequest = sr;
			this.specialFieldID = (String) getClientProperty(TConstants.SPECIAL_COLUMN);
			String cols = (String) getClientProperty(TConstants.SHOW_COLUMNS);
			// critical error if no columns
			if (cols == null) {
				throw new NullPointerException("TConstants.SHOW_COLUMNS must be present.");
			}
			// table
			this.tableModel = new TAbstractTableModel(serviceRequest);
			tableModel.setReferenceColumn(referenceColumns);
			this.tJTable = new TJTable(this, tableModel);
			tableModel.addTableModelListener(this);
			tJTable.addMouseListener(new ListMouseProcessor(tJTable));
			ListSelectionModel lsm = tJTable.getSelectionModel();
			lsm.addListSelectionListener(this);
			Integer arm = (Integer) getClientProperty(TConstants.JTABLE_AUTO_RESIZE_MODE);
			tJTable.setAutoResizeMode((arm == null) ? JTable.AUTO_RESIZE_LAST_COLUMN : arm);
			tJTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			tJTable.setShowGrid(false);
			// transpose table
			String cid = (String) getClientProperty(TConstants.TRANSPORSE_COLUMN);
			if (cid != null) {
				tableModel.setTransporseParameters(cid, cols);
				tJTable = new JTable(tableModel);
				tJTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
				tJTable.setGridColor(Color.LIGHT_GRAY);
				tJTable.setRowSelectionAllowed(false);
				tJTable.setColumnSelectionAllowed(true);
				JTable rowTable = new RowHeaderJTable(tJTable, cols);
				js_pane.setRowHeaderView(rowTable);
				js_pane.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowTable.getTableHeader());

			}
			// cell editable
			Boolean cee = (Boolean) getClientProperty(TConstants.ALLOW_INPUT_FROM_CELL);
			if (cee != null) {
				tableModel.setCellEditable(true, cee);
				// cell editor for all my columns
				TTableCellEditor ttce = new TTableCellEditor(cols);
				String[] cls = cols.split(";");
				Record mod = tableModel.getRecordModel();
				for (String fn : cls) {
					tJTable.setDefaultEditor(mod.getFieldValue(fn).getClass(), ttce);
				}
			}

			// list
			this.listModel = new TAbstractListModel(tableModel);
			this.tJlist = new TJList(this, listModel);
			// listModel.addListDataListener(this);
			tJlist.addMouseListener(new ListMouseProcessor(tJlist));
			lsm = tJlist.getSelectionModel();
			lsm.addListSelectionListener(this);

			// cellrenderer
			String ip = (String) getClientProperty(TConstants.ICON_PARAMETERS);
			String[] col_ico_val;
			TDefaultTableCellRenderer tdcr = new TDefaultTableCellRenderer();
			tdcr.setFormat(formatForColumns);
			TDefaultListCellRenderer tdlcr = new TDefaultListCellRenderer(cols);
			tdcr.setIconParameters(0, "document", null);
			tdlcr.setIconParameters("document", null);
			// establece parametros de icono o crea parametros por omision
			if (ip != null) {
				col_ico_val = ip.split(";");
				String vc = (col_ico_val.length > 2) ? col_ico_val[2] : null;
				tdcr.setIconParameters(Integer.parseInt(col_ico_val[0]), col_ico_val[1], vc);
				tdlcr.setIconParameters(col_ico_val[1], vc);
			}
			tJTable.setDefaultRenderer(TEntry.class, tdcr);
			tJTable.setDefaultRenderer(String.class, tdcr);
			tJTable.setDefaultRenderer(Date.class, tdcr);
			tJTable.setDefaultRenderer(Integer.class, tdcr);
			tJTable.setDefaultRenderer(Double.class, tdcr);
			tJTable.setDefaultRenderer(Long.class, tdcr);
			tJlist.setCellRenderer(tdlcr);

			setView(view);
			enableRecordScopeActions(false);
			setMessage(null);
			TTaskManager.getListUpdater().add(this);
		} else {
			TTaskManager.getListUpdater().remove(this);
			setMessage("ui.msg11");
		}
	}

	/**
	 * metodo de inicializacion. subclases deben implementar este metodo para completar la contruccion de la misma. Ej
	 * las clases en el paquete gui.impl deben usar este metodo para establecer la solicitud de servicio.
	 * 
	 */
	abstract public void init();

	public class TTableCellEditor extends WebGenericEditor {
		private String[] showColumns;
		private Component editCmp;

		public TTableCellEditor(String cols) {
			this.showColumns = cols.split(";");
		}

		@Override
		public Object getCellEditorValue() {
			Object val = super.getCellEditorValue();
			// Override to return TEntry instance where key=FieldName, value=Object value
			TEntry te = new TEntry(editCmp.getName(), val);
			return te;
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			this.editCmp = super.getTableCellEditorComponent(table, value, isSelected, row, column);
			// TAbstractTableModel tatm = (TAbstractTableModel) table.getModel();
			String col = showColumns[column];
			// set the component name as a field name with is being editing
			editCmp.setName(col);
			return editCmp;
		}

	}

	public class TTableCellEditorOld extends AbstractCellEditor implements TableCellEditor, ActionListener {

		private String[] showColumns;
		private Record rMod;
		private WebTextField textField;
		private WebFormattedTextField numberField;
		private WebDateField dateField;
		private Component entryCmp;
		// private DefaultCellEditor delegate;
		private boolean allowEditKey;
		private AplicationException aException;

		public TTableCellEditorOld(String cols, Record m, boolean aek) {
			// this.cols = ((String) uilp.getClientProperty(TConstants.SHOW_COLUMNS)).split(";");
			this.showColumns = cols.split(";");
			this.allowEditKey = aek;
			this.rMod = m;
			this.aException = new AplicationException("ui.msg10");

			this.textField = new WebTextField();
			textField.setDrawBorder(false);
			textField.setDrawShade(false);
			textField.addActionListener(this);
			this.dateField = TUIUtils.getWebDateField("", TStringUtils.ZERODATE);
			// this.dateField = new WebDateField();
			dateField.setDrawBorder(false);
			dateField.setDrawShade(false);
			dateField.addActionListener(this);
			this.numberField = new WebFormattedTextField();
			numberField.setDrawBorder(false);
			numberField.setDrawShade(false);
			numberField.setHorizontalAlignment(JTextField.RIGHT);
			numberField.addActionListener(this);
		}

		@Override
		public boolean isCellEditable(EventObject e) {
			if (e instanceof MouseEvent) {
				return ((MouseEvent) e).getClickCount() >= 2;
			}
			return true;
			// return delegate.isCellEditable(e);
		}

		@Override
		public Object getCellEditorValue() {
			// override to return TEntry instance where key=FieldName, value=Object value
			TEntry te = new TEntry(entryCmp.getName(), null);
			if (entryCmp instanceof WebDateField) {
				Date d = ((WebDateField) entryCmp).getDate();
				te.setValue(new java.sql.Date(d.getTime()));
				// avoid aditional validation
				return te;
			}
			if (entryCmp instanceof WebFormattedTextField) {
				Object o = ((WebFormattedTextField) entryCmp).getValue();
				// TODO: Formater
				te.setValue(o);
			}
			if (entryCmp instanceof JTextField) {
				te.setValue(((JTextField) entryCmp).getText());
			}

			return te;
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			entryCmp = null;
			TAbstractTableModel tatm = (TAbstractTableModel) table.getModel();
			String col = showColumns[tatm.isTranspose() ? row : column];

			if (allowEditKey || !(allowEditKey && rMod.isKeyField(col))) {
				if (value instanceof String) {
					textField.setText((String) value);
					textField.setName(col);
					entryCmp = textField;
				}
				if (value instanceof Date) {
					java.sql.Date d = (java.sql.Date) value;
					dateField.setText(d.equals(TStringUtils.ZERODATE) ? "" : d.toString());
					dateField.setName(col);
					entryCmp = dateField;
				}
				if (value instanceof Number) {
					numberField.setValue(value);
					numberField.setName(col);
					entryCmp = numberField;
				}
			} else {
				NotificationManager.showNotification(PlanC.frame, aException.getMessage(),
						aException.getExceptionIcon()).setDisplayTime(AplicationException.SHORT);
			}
			return entryCmp;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			stopCellEditing();
		}
	}
}
