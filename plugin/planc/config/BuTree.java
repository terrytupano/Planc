package plugin.planc.config;

import gui.*;
import gui.docking.*;
import gui.tree.*;

import java.beans.*;

import javax.swing.*;
import javax.swing.tree.*;

import plugin.planc.*;
import action.*;
import core.*;
import core.datasource.*;
import core.reporting.*;

/**
 * tree for SLE_PLANC_BU
 * 
 */
public class BuTree extends TAbstractTree implements DockingComponent {

	private ServiceRequest serviceRequest;

	public BuTree() {
		super(null, "id", "name", "PARENT_ID");
		this.serviceRequest = new ServiceRequest(ServiceRequest.DB_QUERY, "sle_planc_bu", null);
		setToolBar(new NewRecord(this), new EditRecord(this), new DeleteRecord(this), new FixNodeSubnodeRelation(this,
				"id", "name", "PARENT_ID"), new ExportToFileAction(this, "user_audit;date_audit"));

		putClientProperty(TConstants.TREE_EXPANDED, true);
		showSeparator(true);
		setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		putClientProperty(SLEPlanC.PLANC_ID, 70322L);
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		if (aa instanceof NewRecord) {
			pane = new BuRecord(getRecordModel(), true);
		}
		if (aa instanceof EditRecord) {
			pane = new BuRecord(getRecord(), false);
		}
		return pane;
	}

	public void init() {
		setServiceRequest(serviceRequest);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// Object src = evt.getSource();
		// Object prp = evt.getPropertyName();

	}
}
