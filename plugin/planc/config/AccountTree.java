package plugin.planc.config;

import gui.*;
import gui.docking.*;
import gui.tree.*;

import java.beans.*;

import javax.swing.*;
import javax.swing.tree.*;

import core.*;
import core.datasource.*;
import core.reporting.*;

import plugin.planc.*;

import action.*;

/**
 * tree for SLE_PLANC_ACCOUNT
 * 
 */
public class AccountTree extends TAbstractTree implements DockingComponent {

	private ServiceRequest serviceRequest;

	public AccountTree() {
		super(null, "id", "name", "parent_id");
		this.serviceRequest = new ServiceRequest(ServiceRequest.DB_QUERY, "sle_planc_account", null);
		setToolBar(new NewRecord(this), new EditRecord(this), new DeleteRecord(this), new FixNodeSubnodeRelation(this,
				"id", "name", "parent_id"), new ExportToFileAction(this, ""));
		putClientProperty(TConstants.TREE_EXPANDED, true);
		showSeparator(true);
		putClientProperty(TConstants.TREE_EXPANDED, true);
		putClientProperty(SLEPlanC.PLANC_ID, 70310L);
	}
	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		if (aa instanceof NewRecord) {
			pane = new AccountRecord(getRecordModel(), true);
		}
		if (aa instanceof EditRecord) {
			pane = new AccountRecord(getRecord(), false);
		}
		return pane;
	}

	public void init() {
		setServiceRequest(serviceRequest);
	}

	@Override
	public void setServiceRequest(ServiceRequest sr) {
		super.setServiceRequest(sr);
		JTree jt = getJTree();
		jt.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// Object src = evt.getSource();
		// Object prp = evt.getPropertyName();

	}
}
