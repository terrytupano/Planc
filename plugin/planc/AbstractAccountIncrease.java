package plugin.planc;

import gui.*;
import gui.docking.*;

import java.beans.*;
import java.util.*;

import javax.swing.*;

import plugin.planc.config.*;
import core.*;
import core.datasource.*;

/**
 * planc SleOracle.SLE_PLANC_ACCOUNT_INCREASE
 * 
 */
public class AbstractAccountIncrease extends AbstractFileIncreaseSupport implements DockingComponent {

	private Long accountId;
	private String workfId;
	private String companyId, scenaryId;
	protected String messageId;

	public AbstractAccountIncrease() {
		super();
		messageId = "No message id set for this class";
		putClientProperty(TConstants.SHOW_COLUMNS, "start_increase;percentage;amount");
		putClientProperty(TConstants.ICON_PARAMETERS, "-1; ");
		putClientProperty(TConstants.ALLOW_INPUT_FROM_CELL, false);
	}

	@Override
	public void init() {
		setView(TABLE_VIEW);
		setMessage(messageId);
		setFormattForColums(0, "MMM-yyy");
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		return null;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object src = evt.getSource();
		Object prp = evt.getPropertyName();
		Object newval = evt.getNewValue();

		// path selected
		if (prp.equals(TConstants.PATH_SELECTED)) {
			companyId = null;
			scenaryId = null;
			workfId = null;
			accountId = null;
			if (PlanCSelector.isNodeSelected(PlanCSelector.SCENARIO)) {
				companyId = PlanCSelector.getNodeValue(PlanCSelector.COMPANY);
				scenaryId = PlanCSelector.getNodeValue(PlanCSelector.SCENARIO);
				workfId = "ALL";
			}
		}
		// workforce selected ( whe increase is by workforce.), else, workfId = "ALL"
		/*
		 * if (src instanceof WorkforceList && this instanceof AccountIncrease) { workfId = newval == null ? null :
		 * (String) ((Record) newval).getFieldValue("workrelation_id"); }
		 */

		// account selected
		if (src instanceof AccountSelection || src instanceof AccountGeneration) {
			accountId = (newval == null) ? null : (Long) ((Record) newval).getFieldValue("id");
		}

		// bug180228: account selected for general account increase: only if with_salary=1
		// 1810: AccountIncrease only if with_salary=1
		if (src instanceof AccountGeneration) {
			// && (this instanceof GeneralAccountIncrease || this instanceof AccountIncrease)) {
			if (scenaryId != null && accountId != null) {
				String wc = "SCENARIO_ID = " + scenaryId + " AND ACCOUNT_ID = " + accountId;
				Record rcd = ConnectionManager.getAccessTo("sle_planc_gen_account").exist(wc);
				if (rcd == null || rcd.getFieldValue("with_increase").equals(0)) {
					accountId = null;
				}
			}
		}

		// set the request
		if (companyId != null && scenaryId != null && workfId != null && accountId != null) {
			String wc = "scenario_id = " + scenaryId + " AND company_id = '" + companyId + "' AND workrelation_id = '"
					+ workfId + "' AND account_id = " + accountId;
			DBAccess dba = ConnectionManager.getAccessTo("SLE_PLANC_ACCOUNT_INCREASE");
			Vector<Record> tlist = dba.search(wc, null);
			Record mod = dba.getModel();
			mod.setFieldValue("scenario_id", new Long(scenaryId));
			mod.setFieldValue("company_id", companyId);
			mod.setFieldValue("workrelation_id", workfId);
			mod.setFieldValue("ACCOUNT_ID", new Long(accountId));

			ServiceRequest sr = getServicerRequestFromDate(tlist, scenaryId, mod, "start_increase");
			setServiceRequest(sr);
		} else {
			setMessage(messageId);
		}
	}
}
