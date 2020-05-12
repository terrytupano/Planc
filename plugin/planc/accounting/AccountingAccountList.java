package plugin.planc.accounting;

import gui.*;
import gui.docking.*;

import java.beans.*;

import javax.swing.*;

import core.*;
import core.datasource.*;
import core.reporting.*;

import plugin.planc.*;

import action.*;

/**
 * planc SLE_COST_CENTER_ACCTS list
 * 
 */
public class AccountingAccountList extends UIListPanel implements DockingComponent {

	private ServiceRequest serviceRequest;
	private String companyID;

	public AccountingAccountList() {
		super(null);
		this.serviceRequest = new ServiceRequest(ServiceRequest.DB_QUERY, "sle_cost_center_accts", null);
		setToolBar(new NewRecord(this), new EditRecord(this), new DeleteRecord(this), new ExportToFileAction(this, 
				""));
		putClientProperty(TConstants.SHOW_COLUMNS, "cost_type_id;cost_center_id;account_id;ledger_acct_dim1");
		putClientProperty(TConstants.ICON_PARAMETERS, "0;AccountingAccountList");
		putClientProperty(SLEPlanC.PLANC_ID, 80056L);
	}

	@Override
	public void init() {
		setView(TABLE_VIEW);
		setVisibleToolBar(false);
		setMessage("sle.ui.msg21");
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		if (aa instanceof NewRecord) {
			Record mod = getRecordModel();
			mod.setFieldValue("company_id", companyID);
			pane = new AccountingAccountRecord(mod, true);
		}
		if (aa instanceof EditRecord) {
			pane = new AccountingAccountRecord(getRecord(), false);
		}
		return pane;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// Object src = evt.getSource();
		Object prp = evt.getPropertyName();

		// path selected
		if (prp.equals(TConstants.PATH_SELECTED)) {
			if (PlanCSelector.isNodeSelected(PlanCSelector.COMPANY)) {
				companyID = PlanCSelector.getNodeValue(PlanCSelector.COMPANY);
				serviceRequest.setData("company_id = '" + companyID + "'");
				setVisibleToolBar(true);
				setServiceRequest(serviceRequest);
			} else {
				setVisibleToolBar(false);
				setMessage("sle.ui.msg21");
			}
		}
	}
}
