package plugin.planc.config;

import gui.*;
import gui.docking.*;

import java.beans.*;
import java.util.*;

import javax.swing.*;

import plugin.planc.*;
import core.*;
import core.datasource.*;

/**
 * planc SLE_PLANC_SALARY_CAT_INCREASE increase
 * 
 */
public class SalaryCategoryIncrease extends AbstractFileIncreaseSupport implements DockingComponent {

	private Long categoryId;
	private String companyId, scenaryId;
	private Date validSince;

	public SalaryCategoryIncrease() {
		super();
		putClientProperty(TConstants.SHOW_COLUMNS, "cat_value_id;percentage;amount");
		putClientProperty(TConstants.ICON_PARAMETERS, "-1; ");
		putClientProperty(TConstants.ALLOW_INPUT_FROM_CELL, false);
		// same as generalsalaryincrease ??
		putClientProperty(SLEPlanC.PLANC_ID, 70342L);
	}

	@Override
	public void init() {
		setView(TABLE_VIEW);
		setMessage("sle.ui.msg27");
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
			if (PlanCSelector.isNodeSelected(PlanCSelector.SCENARIO)) {
				companyId = PlanCSelector.getNodeValue(PlanCSelector.COMPANY);
				scenaryId = PlanCSelector.getNodeValue(PlanCSelector.SCENARIO);
			}
		}
		// catecory form general increase
		if ((src instanceof GeneralSalaryIncrease)) {
			categoryId = null;
			validSince = null;
			Record r = ((Record) newval);
			if (r != null) {
				validSince = (Date) r.getFieldValue("start_increase");
				categoryId = (Long) r.getFieldValue("category_id");
			}
		}

		// set the request
		if (scenaryId != null && categoryId != null) {
			TEntry[] telist = SLEPlanC.getTEntryGroupFrom("SLE_CATEGORY_VALUE", "id", "code", "CATEGORY_ID = " + categoryId);
			setReferenceColumn("cat_value_id", telist);
			
			// build my own servicerequest
			Vector<Record> rlist = new Vector<Record>();
			DBAccess dba = ConnectionManager.getAccessTo("SLE_PLANC_SALARY_CAT_INCREASE");
			Record mod = dba.getModel();
			mod.setFieldValue("scenario_id", scenaryId);
			mod.setFieldValue("company_id", companyId);
			mod.setFieldValue("workrelation_id", "ALL");
			mod.setFieldValue("valid_since", validSince);
			mod.setFieldValue("category_id", categoryId);
			for (int c = 0; c < telist.length; c++) {
				mod.setFieldValue("CAT_VALUE_ID", (Long) telist[c].getKey());
				Record r = dba.exist(mod);
				if ( r == null) {
					r = new Record(mod);
				}
				rlist.add(r);
			}

			ServiceRequest se = new ServiceRequest(ServiceRequest.CLIENT_GENERATED_LIST, "", rlist);
			se.setParameter(ServiceResponse.RECORD_MODEL, mod);
			setServiceRequest(se);
		} else {
			setMessage("sle.ui.msg27");
		}
	}
}