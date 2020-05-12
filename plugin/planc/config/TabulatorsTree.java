package plugin.planc.config;

import gui.*;
import gui.docking.*;
import gui.tree.*;

import java.beans.*;

import javax.swing.*;

import plugin.planc.*;

import action.*;
import core.*;
import core.datasource.*;

/**
 * tree view for join files SLE_TABULATORS and SLE_TAB_VALIDITY
 * 
 */
public class TabulatorsTree extends TAbstractTree implements DockingComponent {

	private ServiceRequest serviceRequest;
	private NewRecord newTab;
	private EditRecord editTab, newVig, editVig;
	private String companyId;

	public TabulatorsTree() {
		super(null, "node", "name", "subnode");
		serviceRequest = new ServiceRequest(TreeViewFromParentChildrenTables.class.getName(), "sle_tabulators", null);
		serviceRequest.setParameter(ServiceRequest.CHILDREN_TABLE_NAME, "sle_tab_validity");
		serviceRequest.setParameter(ServiceRequest.PARENT_STRING, "name");
		serviceRequest.setParameter(ServiceRequest.CHILDREN_STRING, "name");
		serviceRequest.setParameter(ServiceRequest.PARENT_JOIN_BY, "id");
		serviceRequest.setParameter(ServiceRequest.CHILDREN_JOIN_BY, "tab_id");
		serviceRequest.setParameter(ServiceRequest.NODE_FIELD, "id");
		serviceRequest.setParameter(ServiceRequest.SUB_NODE_FIELD, "id");

		// new category
		newTab = new NewRecord(this);
		newTab.setName("tabval.newtab");
		newTab.setToolTip("tttabval.newtab");
		newTab.putValue(Action.SMALL_ICON, TUIUtils.getMergedIcon("sle_tabulators", "NewRecord", 16));

		// edit category
		editTab = new EditRecord(this);
		editTab.setName("tabval.edittab");
		editTab.setToolTip("tttabval.edittab");
		editTab.putValue(Action.SMALL_ICON, TUIUtils.getMergedIcon("sle_tabulators", "EditRecord", 16));

		// new category value
		newVig = new EditRecord(this);
		newVig.setName("tabval.newvig");
		newVig.setToolTip("tttabval.newvig");
		newVig.putValue(Action.SMALL_ICON, TUIUtils.getMergedIcon("sle_tab_validity", "NewRecord", 16));

		// edit category value
		editVig = new EditRecord(this);
		editVig.setName("tabval.editvig");
		editVig.setToolTip("tttabval.editvig");
		editVig.putValue(Action.SMALL_ICON, TUIUtils.getMergedIcon("sle_tab_validity", "EditRecord", 16));

		setToolBar(newTab, editTab, newVig, editVig, new DeleteRecord(this));
		putClientProperty(TConstants.TREE_EXPANDED, true);
		putClientProperty(TConstants.TREE_ICON_FIELD, "icon");
		putClientProperty(SLEPlanC.PLANC_ID, 80020L);
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		Record srcr = getRecord();
		if (aa == newTab) {
			Record r = ConnectionManager.getAccessTo("sle_tabulators").getModel();
			r.setFieldValue("company_id", companyId);
			pane = new TabulatorRecord(r, true);
		}
		if (aa == editTab) {
			Record r = ConnectionManager.getAccessTo("sle_tabulators").exist("id = '" + srcr.getFieldValue("node")+"'");
			pane = new TabulatorRecord(r, false);
		}
		if (aa == newVig) {
			Record r = ConnectionManager.getAccessTo("sle_tab_validity").getModel();
			r.setFieldValue("ID", System.currentTimeMillis());
			r.setFieldValue("company_id", companyId);
			r.setFieldValue("tab_id", srcr.getFieldValue("node"));
			pane = new TabulatorVigencyRecord(r, true);
		}
		if (aa == editVig) {
			Record r = ConnectionManager.getAccessTo("sle_tab_validity").exist("id = " + srcr.getFieldValue("node"));
			pane = new TabulatorVigencyRecord(r, false);
		}
		return pane;
	}

	/**
	 * override to dispach event to correct table
	 */
	@Override
	public boolean executeAction(TActionEvent event) {
		if (event.getRedirectAction() instanceof DefaultCancelAction) {
			return true;
		}

		Object src = event.getSource();
		boolean ok = false;
		AbstractRecordDataInput ardi = (AbstractRecordDataInput) event.getData();
		if (src == newTab) {
			ConnectionManager.getAccessTo("sle_tabulators").add(ardi.getRecord());
			ok = true;
		}
		if (src == editTab) {
			ConnectionManager.getAccessTo("sle_tabulators").update(ardi.getRecord());
			ok = true;
		}
		if (src == newVig) {
			ConnectionManager.getAccessTo("sle_tab_validity").add(ardi.getRecord());
			ok = true;
		}
		if (src == editVig) {
			ConnectionManager.getAccessTo("sle_tab_validity").update(ardi.getRecord());
			ok = true;
		}
		if (ok) {
			freshen();
			return ok;
		}
		return super.executeAction(event);
	}
	/**
	 * override to perform aditional verification: <br>
	 * - if node is a parent node: enable editcategory, newcategoryvalue <br>
	 * - if node is leaf node: enable editcatvalue
	 */
	@Override
	public void enableActions(int sco, boolean ena) {
		super.enableActions(sco, ena);
		Record r = getRecord();
		if (r != null) {
			boolean nosb = r.getFieldValue("subnode").toString().equals("");
			editTab.setEnabled(nosb);
			newVig.setEnabled(nosb);
			editVig.setEnabled(!nosb);
		}
	}

	public void init() {
		setVisibleToolBar(false);
		setMessage("sle.ui.msg08");
		// wait for propertychange to set the service
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// Object src = evt.getSource();
		Object prp = evt.getPropertyName();

		// company selected
		if (prp.equals(TConstants.PATH_SELECTED)) {
			if (PlanCSelector.isNodeSelected(PlanCSelector.COMPANY)) {
				setVisibleToolBar(true);
				companyId = PlanCSelector.getNodeValue(PlanCSelector.COMPANY);
				serviceRequest.setData("company_id = '" + companyId + "'");
				setServiceRequest(serviceRequest);
			} else {
				setMessage("sle.ui.msg08");
			}
		}
	}
}
