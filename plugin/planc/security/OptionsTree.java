package plugin.planc.security;

import gui.*;
import gui.docking.*;
import gui.tree.*;

import java.beans.*;

import javax.swing.*;
import javax.swing.event.*;

import action.*;
import core.*;
import core.datasource.*;

/**
 * arbol de elementos archivo sle_options
 * 
 */
public class OptionsTree extends TAbstractTree implements CellEditorListener, DockingComponent {

	private ServiceRequest serviceRequest;

	public OptionsTree() {
		super(null, "node", "name", null);
		this.serviceRequest = new ServiceRequest(OptionsTransaction.class.getName(), null, null);
		putClientProperty(TConstants.TREE_EXPANDED, true);
		putClientProperty(TConstants.TREE_ICON_FIELD, "module_id");
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
		setMessage("sle.ui.msg18");
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
	public void propertyChange(PropertyChangeEvent evt) {
		Object src = evt.getSource();
		Object prp = evt.getPropertyName();

		// selected rol
		if ((src instanceof RolesList) && prp.equals(TConstants.RECORD_SELECTED)) {
			Record rr = (Record) evt.getNewValue();
			if (rr != null) {
				serviceRequest.setData(rr.getFieldValue("id"));
				enableActions(TAbstractAction.TABLE_SCOPE, true);
				setServiceRequest(serviceRequest);
				markLeafNodes();
				getNodeEditor().addCellEditorListener(this);
			} else {
				setMessage("sle.ui.msg18");
			}
		}
	}
	

	@Override
	public void editingStopped(ChangeEvent e) {
		AbstractCellEditor edt = (AbstractCellEditor) e.getSource();
		Record r = (Record) ((TEntry) edt.getCellEditorValue()).getKey();
		boolean aval = (Boolean) r.getFieldValue("autorized");
		String tn = "sle_role_options";

		DBAccess dba = ConnectionManager.getAccessTo(tn);
		Record mod = dba.getModel();
		mod.setFieldValue("role_id", serviceRequest.getData());
		mod.setFieldValue("option_id", r.getFieldValue("id"));

		ServiceConnection.sendTransaction(aval ? ServiceRequest.DB_ADD : ServiceRequest.DB_DELETE, tn, mod);
	}

	@Override
	public void editingCanceled(ChangeEvent e) {

	}
}
