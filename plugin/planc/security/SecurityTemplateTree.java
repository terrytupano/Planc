/**
 * Copyright (c) Terry - All right reserved. PROPRIETARY/CONFIDENTIAL.
 * Use is subject to license terms.
 *
 */

package plugin.planc.security;

import gui.*;
import gui.docking.*;
import gui.tree.*;

import java.beans.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import action.*;
import core.*;
import core.datasource.*;

/**
 * arbol de elementos archivo sle_options
 * 
 */
public class SecurityTemplateTree extends TAbstractTree implements DockingComponent, CellEditorListener {

	private ServiceRequest serviceRequest;

	public SecurityTemplateTree() {
		super(null, "id", "name", "sub_node");
		this.serviceRequest = new ServiceRequest(SecurityTemplateTransaction.class.getName(), null, null);
		putClientProperty(TConstants.TREE_EXPANDED, true);
		putClientProperty(TConstants.TREE_BOOLEAN_FIELD, "autorized");
		setToolBar(true);
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		return null;
	}

	public void init() {
		enableActions(TAbstractAction.TABLE_SCOPE, false);
		setServiceRequest(null);
		setMessage("sle.ui.msg17");

	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object src = evt.getSource();
		Object prp = evt.getPropertyName();
		
		// selected template
		if ((src instanceof SecurityTemplateList) && prp.equals(TConstants.RECORD_SELECTED)) {
			Record rr = (Record) evt.getNewValue();
			setMessage(rr == null ? "sle.ui.msg17" : null);
			if (rr != null) {
				serviceRequest.setData(rr.getFieldValue("id"));
				enableActions(TAbstractAction.TABLE_SCOPE, true);
				setServiceRequest(serviceRequest);
				markLeafNodes();
				getNodeEditor().addCellEditorListener(this);
			}
		}
	}
	@Override
	public void filterTree(String text) {
		super.filterTree(text);
		JTree jt = getJTree();
		for (int i = 0; i < jt.getRowCount(); i++) {
			jt.expandRow(i);
		}
	}

	@Override
	public void editingStopped(ChangeEvent e) {
		AbstractCellEditor edt = (AbstractCellEditor) e.getSource();
		Record r = (Record) ((TEntry) edt.getCellEditorValue()).getKey();
		boolean aval = (Boolean) r.getFieldValue("autorized");
		String tn = "sle_template_roles";

		DBAccess dba = ConnectionManager.getAccessTo(tn);
		Record mod = dba.getModel();
		mod.setFieldValue("template_id", serviceRequest.getData());
		mod.setFieldValue("role_id", r.getFieldValue("id"));

		JTree jt = getJTree();
		TreePath sPath = jt.getSelectionPath();
		sPath = sPath.getParentPath();
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) sPath.getLastPathComponent();
		TDefaultTreeModel tmodel = getTreeModel();

		// if action field is enable_see, add or delete record in file.
		if (r.getFieldValue("field_name").equals("enable_see")) {
			ServiceConnection.sendTransaction(aval ? ServiceRequest.DB_ADD : ServiceRequest.DB_DELETE, tn, mod);

			// if enable_see is false, unckeck the rest of nodes
			if (!aval) {
				// node 0 is enable_see
				for (int j = 1; j < tmodel.getChildCount(parent); j++) {
					DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) tmodel.getChild(parent, j);
					Record r1 = (Record) ((TEntry) dmtn.getUserObject()).getKey();
					r1.setFieldValue("autorized", false);
					tmodel.nodeChanged(dmtn);
				}
				// int[] ix = {1, 2, 3};
				// tmodel.nodesChanged(parent, ix);
			}
		} else {
			Record mod1 = dba.exist(mod);
			// if user check any other that enable_see before enable_see was inserted
			if (mod1 == null) {
				ServiceConnection.sendTransaction(ServiceRequest.DB_ADD, tn, mod);
				DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) tmodel.getChild(parent, 0);
				Record r1 = (Record) ((TEntry) dmtn.getUserObject()).getKey();
				r1.setFieldValue("autorized", true);
				tmodel.nodeChanged(dmtn);
				mod1 = mod;
			}
			// update fields. the field to update in file are store in field_name
			Integer ival = (aval) ? 1 : 0;
			mod1.setFieldValue(r.getFieldValue("field_name").toString(), ival);
			ServiceConnection.sendTransaction(ServiceRequest.DB_UPDATE, tn, mod1);
		}
	}

	@Override
	public void editingCanceled(ChangeEvent e) {

	}
}
