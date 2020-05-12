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
 * tree for sle_jobs
 * 
 */
public class JobsTree extends TAbstractTree implements DockingComponent {

	private ServiceRequest serviceRequest;
	private String companyId;

	public JobsTree() {
		super(null, "id", "name", "parent_id");
		this.serviceRequest = new ServiceRequest(ServiceRequest.DB_QUERY, "sle_jobs", null);
		setToolBar(new NewRecord(this), new EditRecord(this), new DeleteRecord(this), new FixNodeSubnodeRelation(this,
				"id", "name", "parent_id"), new ExportToFileAction(this, ""));

		putClientProperty(TConstants.TREE_EXPANDED, true);
		showSeparator(true);
		setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		putClientProperty(SLEPlanC.PLANC_ID, 80040L);
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		if (aa instanceof NewRecord) {
			Record r = getRecordModel();
			r.setFieldValue("company_id", companyId);
			pane = new JobsRecord(r, true);
		}
		if (aa instanceof EditRecord) {
			pane = new JobsRecord(getRecord(), false);
		}
		return pane;
	}

	@Override
	public void init() {
		setVisibleToolBar(false);
		setMessage("sle.ui.msg09");
		// wait for propertychange to set the service
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// Object src = evt.getSource();
		Object prp = evt.getPropertyName();

		// only respond to path selection
		if (prp.equals(TConstants.PATH_SELECTED)) {
			if (PlanCSelector.isNodeSelected(PlanCSelector.COMPANY)) {
				setVisibleToolBar(true);
				companyId = PlanCSelector.getNodeValue(PlanCSelector.COMPANY);
				serviceRequest.setData("COMPANY_ID = '" + companyId + "'");
				setServiceRequest(serviceRequest);
			} else {
				setVisibleToolBar(false);
				setMessage("sle.ui.msg09");
			}
		}
	}
}
