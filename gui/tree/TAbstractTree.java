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

package gui.tree;

import gui.*;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import action.*;
import core.*;
import core.datasource.*;

/**
 * Centraliza el manejo de operaciones comunes a todas las subclases que tienen como funcion presentar los datos
 * contenidos dentro de un archivo en forma de arbol. el usuario podra realizar operaciones con ellos. Los datos
 * presentados por esta clase esta determinado por el resultado retornado por el servidor al momento de enviar la
 * solicitud de servicio.
 * 
 * adicionalmente al servicion, es obligatorio establecer 3 elementos para el correcto funcionamiento de este
 * componente:
 * 
 * Nodo: Nombre del campo dentro del registro que identifica en forma exclusiva un nodo en particular. <br>
 * Name: Nombre del campo que describe al nodo. <br>
 * sub nodo de: nombre del campo que indica que el registro es un subnodo de otro
 * 
 * 
 * si el usuario selecciona un elemento dentro de la tabla, se cambia la propiedad
 * putClientProperty(PropertyNames.RECORD_SELECTED, r);
 * 
 * 
 */
public abstract class TAbstractTree extends UIComponentPanel
		implements
			ActionPerformer,
			EditableList,
			TreeSelectionListener,
			Exportable {

	private JScrollPane js_pane;
	private String nodeIdField, nodeNameField, subNodeField;
	private JTree tree;
	private boolean isLeaf, separator;
	private TDefaultTreeCellRenderer treeCellRenderer;
	private TCheckBoxNodeEditor nodeEditor;
	private Rectangle lastVis;
	private int selectionMode;
	private DefaultMutableTreeNode originalRoot;
	private TDefaultTreeModel treeModel;

	/**
	 * nueva instancia. ver documentacion de <code>TDefaultTreeModel</code> para utilizacion del parametro
	 * <code>sn</code>
	 * 
	 * 
	 * @param mi - id para area informatica
	 * @param no - campo que identifica el Nodo
	 * @param na - campo que identifica el nombre o texto del nodo
	 * @param sn - campo que identifica el subnodo o <code>null</code>
	 */
	public TAbstractTree(String mi, String no, String na, String sn) {
		this(mi);
		this.nodeIdField = no;
		this.nodeNameField = na;
		this.subNodeField = sn;
		putClientProperty(TConstants.TREE_EXPANDED, false);
	}

	/**
	 * TEMPOARAL METHOD: this mark the isLeaf field on the record inside of {@link DefaultMutableTreeNode}. this is to
	 * solve some sincronization issues between {@link TreeCellRenderer} and {@link TreeCellEditor} when a tree is
	 * editable and support search.
	 * <p>
	 * this fix is for trees that have search suport and are editable. in some searchs, an element that is not dessire
	 * to be editet is remain as leaf in a search result. subclass of tabstracttree must determine which node are
	 * editable or not.
	 * <p>
	 * interested class in this marck suport, must call this method inmediately after
	 * {@link #setServiceRequest(ServiceRequest)}
	 */
	public void markLeafNodes() {
		JTree jt = getJTree();
		for (int i = 0; i < jt.getRowCount(); i++) {
			TreePath tp = jt.getPathForRow(i);
			DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) tp.getLastPathComponent();
			TEntry te = (TEntry) dmtn.getUserObject();
			Record r = (Record) te.getKey();
			r.setFieldValue("isLeaf", dmtn.isLeaf());
		}
	}

	/**
	 * nueva instancia
	 * 
	 * @param mi - Identificador para titulo
	 */
	private TAbstractTree(String mi) {
		super(mi, false);
		this.nodeIdField = null;
		this.nodeNameField = null;
		this.js_pane = new JScrollPane();
		this.separator = false;
		this.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION;
		// better look for weblaf
		js_pane.setBorder(null);

		// para el borde
		Box b1 = Box.createVerticalBox();
		b1.add(js_pane);
		addWithoutBorder(b1);
	}

	public void showSeparator(boolean ss) {
		this.separator = ss;
	}

	/**
	 * Habilita/desabilita todas las acciones disponibles dentro de la barra de herramientas segun el tipo de accion.
	 * NOTA: Si el registro seleccionado es un nodo sin hijos, se habilita la opcion de suprimir. de lo contrario, se
	 * desabilita
	 * 
	 * @param sco - tipo de accion que se desea inhabilitar. este puede ser alguno de los tipos desctito en
	 *        <code>AppAbstractAction</code>
	 * @param ena - =true, habilitar, = false, deshabilitar
	 */
	public void enableActions(int sco, boolean ena) {
		TAbstractAction[] btns = getToolBarActions();
		if (btns != null) {
			for (int j = 0; j < btns.length; j++) {
				TAbstractAction ac = (TAbstractAction) btns[j];
				if (ac.getScope() == sco) {
					// if action must be enabled, check for autorization
					ac.setEnabled(ena ? Session.isAutorizedForAction(ac) : false);
					if (ac instanceof DeleteRecord) {
						ac.setEnabled(isLeaf ? ena : false);
					}
				}
			}
		}
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

		// operacion exitosa? refrescar
		if (ok) {
			freshen();
		}
		return ok;
	}

	/**
	 * detault implementation for filter brach.
	 * 
	 * @param text - filter txt
	 */
	public void filterTree(String text) {
		// treemodel may be null if depend of another component selection
		if (treeModel != null) {
			// reset with the original root
			if (text.trim().toString().equals("")) {
				treeModel.setRoot(originalRoot);
				tree.setModel(treeModel);
				tree.updateUI();
			} else {
				DefaultMutableTreeNode filteredRoot = copyNode(originalRoot);
				TreeNodeBuilder b = new TreeNodeBuilder(text);
				filteredRoot = b.prune((DefaultMutableTreeNode) filteredRoot.getRoot());
				treeModel.setRoot(filteredRoot);
				tree.setModel(treeModel);
				tree.updateUI();
				for (int i = 0; i < tree.getRowCount(); i++) {
					tree.expandRow(i);
				}
			}
		}
	}

	/**
	 * Clone/Copy a tree node. TreeNodes in Swing don't support deep cloning.
	 * 
	 * @param orig to be cloned
	 * @return cloned copy
	 */
	private DefaultMutableTreeNode copyNode(DefaultMutableTreeNode orig) {
		DefaultMutableTreeNode newOne = new DefaultMutableTreeNode();
		newOne.setUserObject(orig.getUserObject());
		Enumeration enm = orig.children();
		while (enm.hasMoreElements()) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) enm.nextElement();
			newOne.add(copyNode(child));
		}
		return newOne;
	}

	public void freshen() {
		lastVis = tree.getVisibleRect();
		setServiceRequest(getServiceRequest());
		js_pane.getViewport().scrollRectToVisible(lastVis);
		// TODO: apply filter if any
	}

	/**
	 * return the underling {@link JTree} inside this component.
	 * 
	 * @return {@link JTree}
	 */
	public JTree getJTree() {
		return tree;
	}

	/**
	 * return instance of <code>TCheckBoxNodeEditor</code> used to edit tree cells.
	 * 
	 * NOTA: active only when tree view is for autorizations (Property <code>PropertyNames.TREE_BOOLEAN_FIELD</code>
	 * setted
	 * 
	 * @return Node editor
	 */
	public TCheckBoxNodeEditor getNodeEditor() {
		return nodeEditor;
	}

	public Record getRecord() {
		Record[] r = getRecords();
		return r == null ? null : r[0];
	}

	public Record[] getRecords() {
		TreePath[] tpsel = tree.getSelectionPaths();
		if (tpsel != null) {
			Record[] selrcds = new Record[tpsel.length];
			int i = 0;
			for (TreePath tp : tpsel) {
				DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) tp.getLastPathComponent();
				TEntry te = (TEntry) dmtn.getUserObject();
				selrcds[i++] = ((Record) te.getKey());
			}
			return selrcds;
		}
		return null;
	}

	@Override
	public ServiceRequest getServiceRequest() {
		return treeModel.getServiceRequest();
	}

	/**
	 * return whether the current selected node is a leaf node. valid only for
	 * {@link TreeSelectionModel#SINGLE_TREE_SELECTION}
	 * 
	 * @return {@code true} if selected node is leaf. <code>false</code>otherwise
	 */
	public boolean isLastSelectionLeaf() {
		return isLeaf;
	}

	/**
	 * return the {@link TDefaultTreeModel} instance used to build the tree for this component
	 * 
	 * @return {@link TDefaultTreeModel}
	 */
	public TDefaultTreeModel getTreeModel() {
		return treeModel;
	}

	/**
	 * establece una nueva transaccion que se encargara de mantener actualizado el modelo de datos para esta tabla. use
	 * este metodo si desea modificar a voluntad el servicio que se desea presentar por este arbol. si el valor para el
	 * servicio es null, se coloca un panel en blanco. y se inhabilitan todas las acciones.
	 * 
	 * @param sr - solicitud de servicio.
	 */
	public void setServiceRequest(ServiceRequest sr) {
		if (sr != null) {
			this.tree = new JTree();
			tree.setRootVisible(false);
			this.treeModel = new TDefaultTreeModel(nodeIdField, nodeNameField, subNodeField);
			treeModel.setServiceRequest(sr);
			tree.setModel(treeModel);
			this.originalRoot = (DefaultMutableTreeNode) treeModel.getRoot();

			String icon = (String) getClientProperty(TConstants.TREE_ICON_FIELD);
			treeCellRenderer = new TDefaultTreeCellRenderer(icon, nodeIdField, nodeNameField);
			treeCellRenderer.showSeparator(separator);
			// cambio segun propiedad
			String fn = (String) getClientProperty(TConstants.TREE_BOOLEAN_FIELD);
			if (fn != null) {
				treeCellRenderer = new TJCheckBoxTreeCellRenderer(icon, nodeIdField, nodeNameField, fn);
				this.nodeEditor = new TCheckBoxNodeEditor(tree, icon, nodeIdField, nodeNameField, fn);
				tree.setCellEditor(nodeEditor);
				tree.setEditable(true);
			}
			tree.setCellRenderer(treeCellRenderer);
			tree.addTreeSelectionListener(this);
			tree.getSelectionModel().setSelectionMode(selectionMode);
			tree.addMouseListener(new ListMouseProcessor(tree));
			boolean et = (Boolean) getClientProperty(TConstants.TREE_EXPANDED);
			if (et) {
				for (int i = 0; i < tree.getRowCount(); i++) {
					tree.expandRow(i);
				}
			}
			enableActions(TAbstractAction.RECORD_SCOPE, false);

			js_pane.setViewportView(tree);
			setMessage(null);
		} else {
			setMessage("ui.msg11");
		}
	}

	/**
	 * set the selection model for the internal {@link JTree}
	 * 
	 * @param sm some of the {@link TreeSelectionModel} values
	 */
	public void setSelectionMode(int sm) {
		selectionMode = sm;
	}

	@Override
	public void valueChanged(TreeSelectionEvent tse) {
		TreePath tp = tse.getNewLeadSelectionPath();
		if (tp != null) {
			DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) tp.getLastPathComponent();
			this.isLeaf = dmtn.isLeaf();
		}
		enableActions(TAbstractAction.RECORD_SCOPE, (tp != null));
		firePropertyChange(TConstants.RECORD_SELECTED, null, getRecord());
	}

	/**
	 * retorna una copia del modelo de registro para de la tabla que se esta presentado como arbol
	 * 
	 * @return modelo
	 */
	protected Record getRecordModel() {
		return treeModel.getModel();
	}

	/**
	 * Class that prunes off all leaves which do not match the search string.
	 * 
	 * @author Oliver.Watkins
	 */

	public class TreeNodeBuilder {

		private String textToMatch;

		public TreeNodeBuilder(String textToMatch) {
			this.textToMatch = textToMatch.toLowerCase();
		}

		public DefaultMutableTreeNode prune(DefaultMutableTreeNode root) {

			boolean badLeaves = true;

			// keep looping through until tree contains only leaves that match
			while (badLeaves) {
				badLeaves = removeBadLeaves(root);
			}
			return root;
		}

		/**
		 * 
		 * @param root
		 * @return boolean bad leaves were returned
		 */
		private boolean removeBadLeaves(DefaultMutableTreeNode root) {

			// no bad leaves yet
			boolean badLeaves = false;

			// reference first leaf
			DefaultMutableTreeNode leaf = root.getFirstLeaf();

			// if leaf is root then its the only node
			if (leaf.isRoot())
				return false;

			// this get method changes if in for loop so have to define outside of it
			int leafCount = root.getLeafCount();
			for (int i = 0; i < leafCount; i++) {

				DefaultMutableTreeNode nextLeaf = leaf.getNextLeaf();

				// if it does not start with the text then snip it off its parent
				if (!leaf.getUserObject().toString().toLowerCase().contains(textToMatch)) {
					DefaultMutableTreeNode parent = (DefaultMutableTreeNode) leaf.getParent();

					if (parent != null)
						parent.remove(leaf);

					badLeaves = true;
				}
				leaf = nextLeaf;
			}
			return badLeaves;
		}
	}
}
