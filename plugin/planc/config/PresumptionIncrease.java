package plugin.planc.config;

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
 * planc SLE_PLANC_PRESUMPTION increase
 * 
 * change log:
 * <p>
 * 1815 - Presunciones ahora tiene acciones nueve, editar suprimir para ingreso de datos
 */
public class PresumptionIncrease extends UIListPanel implements DockingComponent {

	private Long accountId;
	private String scenaryId;
	private ServiceRequest request;

	public PresumptionIncrease() {
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
		putClientProperty(SLEPlanC.PLANC_ID, 70350L);
	}

	@Override
	public void init() {
		setView(TABLE_VIEW);
		setMessage("sle.ui.msg25");
		setFormattForColums(0, "MMM-yyy");
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		boolean newr = aa instanceof NewRecord2;
		Record rcd = newr ? getRecordModel() : getRecord();
		rcd.setFieldValue("scenario_id", scenaryId);
		rcd.setFieldValue("account_id", accountId);
		return new AmountIncreaseRecord(rcd, newr);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object src = evt.getSource();
		Object prp = evt.getPropertyName();
		Object newval = evt.getNewValue();

		// path selected
		if (prp.equals(TConstants.PATH_SELECTED)) {
			scenaryId = null;
			accountId = null;
			if (PlanCSelector.isNodeSelected(PlanCSelector.SCENARIO)) {
				scenaryId = PlanCSelector.getNodeValue(PlanCSelector.SCENARIO);
			}
		}
		// account selected
		// bug180228: only "presunción" account type allow
		if ((src instanceof AccountGeneration)) {
			accountId = null;
			if (newval != null) {
				Record r = ((Record) newval);
				long at = (Long) r.getFieldValue("type_id");
				accountId = at != 16 ? null : (Long) r.getFieldValue("id");
			}
		}

		// set the request
		if (scenaryId != null && accountId != null) {
			reloadSR();
			setServiceRequest(request);
		} else {
			setMessage("sle.ui.msg25");
		}
	}

	@Override
	public void freshen() {
		// reload my data
		reloadSR();
		super.freshen();
	}

	private void reloadSR() {
		String wc = "scenario_id = " + scenaryId + " AND account_id = " + accountId;
		DBAccess dba = ConnectionManager.getAccessTo("sle_planc_presumption");
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