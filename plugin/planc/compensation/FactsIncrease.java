package plugin.planc.compensation;

import gui.*;
import gui.docking.*;

import java.beans.*;
import java.util.*;

import javax.swing.*;

import plugin.planc.*;
import action.*;
import core.*;
import core.datasource.*;

/**
 * planc SleOracle.SLE_PLANC_ACCOUNT_INCREASE
 * 
 * change log:
 * <p>
 * 1813 - Hechos o movimientos ahora tiene acciones nueve, editar suprimir para ingreso de datos
 */
public class FactsIncrease extends UIListPanel implements DockingComponent {

	private Long accountId;
	private String workfId;
	private String companyId, scenaryId;
	private ServiceRequest request;

	public FactsIncrease() {
		super(null);
		DeleteRecord2 dr = new DeleteRecord2(this) {
			@Override
			public void actionPerformed2() {
				// from date to slot
				Record r1 = getRecord();
				int ts = SLEPlanC.getSlot((Date) r1.getFieldValue("time_slot"));
				r1.setFieldValue("time_slot", ts);
				boolean ok = ConnectionManager.getAccessTo(r1.getTableName()).delete(r1);
				if (ok) {
					freshen();
				}
			}
		};
		setToolBar(false, new NewRecord2(this), new EditRecord2(this), dr);
		request = new ServiceRequest(ServiceRequest.CLIENT_GENERATED_LIST, "", null);
		putClientProperty(TConstants.SHOW_COLUMNS, "time_slot;amount");
		putClientProperty(TConstants.ICON_PARAMETERS, "-1; ");
		putClientProperty(SLEPlanC.PLANC_ID, 70870L);
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		boolean newr = aa instanceof NewRecord2;
		Record rcd = newr ? getRecordModel() : getRecord();
		rcd.setFieldValue("scenario_id", scenaryId);
		rcd.setFieldValue("company_id", companyId);
		rcd.setFieldValue("workrelation_id", workfId);
		rcd.setFieldValue("account_id", accountId);
		return new AmountIncreaseRecord(rcd, newr);
	}

	@Override
	public void init() {
		setMessage("sle.ui.msg01");
		setFormattForColums(0, "MMM-yyy");
	}

	@Override
	public void freshen() {
		// reload my data
		reloadSR();
		super.freshen();
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

		// workforce selected
		if ((src instanceof WorkforceList)) {
			workfId = null;
			if (evt.getNewValue() != null) {
				workfId = (String) ((Record) newval).getFieldValue("workrelation_id");
			}
		}

		// account selected
		if (src instanceof AccountSelection) {
			Record acr = (Record) newval;
			accountId = (acr == null) ? null : (Long) acr.getFieldValue("id");
		}

		// set the request
		if (companyId != null && scenaryId != null && workfId != null && accountId != null) {
			reloadSR();
			setServiceRequest(request);
		} else {
			setMessage("sle.ui.msg01");
		}
	}

	private void reloadSR() {
		String wc = "SCENARIO_ID = " + scenaryId + " AND COMPANY_ID = '" + companyId + "' AND WORKRELATION_ID = '"
				+ workfId + "' AND ACCOUNT_ID = " + accountId;
		DBAccess dba = ConnectionManager.getAccessTo("sle_workrelation_fact");
		Vector<Record> tlist = dba.search(wc, null);
		Record mod = dba.getModel();

		// translate form timeslot format to date
		mod.setFieldValue("time_slot", new Date(TStringUtils.ZERODATE.getTime()));
		for (Record r : tlist) {
			String slo = r.getFieldValue("time_slot").toString();
			r.setFieldValue("time_slot", SLEPlanC.getSlotDate(slo));
		}
		request.setParameter(ServiceResponse.RECORD_MODEL, mod);
		request.setData(tlist);
	}
}
