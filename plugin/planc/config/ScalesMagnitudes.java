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
 * planc PLANC_SCALE_MAGNITUDES increase
 * 
 */
public class ScalesMagnitudes extends UIListPanel implements DockingComponent {

	private String scenarioId, scaleId, classif;
	private ServiceRequest request;

	public ScalesMagnitudes() {
		super(null);
		// show columsn on change property
		// putClientProperty(TConstants.ALLOW_INPUT_FROM_CELL, false);
		putClientProperty(TConstants.ICON_PARAMETERS, "-1; ");
		setToolBar(false, new NewRecord(this), new EditRecord(this), new DeleteRecord(this));
	}

	@Override
	public void init() {
		setView(TABLE_VIEW);
		setMessage("sle.ui.msg28");
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		boolean newr = aa instanceof NewRecord;
		Record r = newr ? getRecordModel() : getRecord();
		// panel according to classf field value
		if (classif.equals("period")) {
			r.setFieldValue("SCALE_ID", scaleId);
			r.setFieldValue("SCENARIO_ID", scenarioId);
			pane = new ScalesMagnitudesPeriodRecord(r, newr);
		}
		if (classif.equals("time") || classif.equals("salary")) {
			r.setFieldValue("SCALE_ID", scaleId);
			r.setFieldValue("SCENARIO_ID", scenarioId);
			pane = new ScalesMagnitudesRecord(r, newr);
		}

		if (classif.equals("insurance")) {
			r.setFieldValue("SCALE_ID", scaleId);
			pane = new ScalesInsuranceRecord(r, newr);
		}
		return pane;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object src = evt.getSource();
		Object prp = evt.getPropertyName();
		Object selobj = evt.getNewValue();

		// save the scenario
		if (PlanCSelector.isNodeSelected(PlanCSelector.SCENARIO)) {
			scenarioId = PlanCSelector.getNodeValue(PlanCSelector.SCENARIO);
			// update sr if sceneary change. necesary if was previoly selected a scale and then, select a new scenario
			updateServiceRequest();
		}

		// scale record selected
		if ((src instanceof ScaleList) && prp.equals(TConstants.RECORD_SELECTED)) {
			scaleId = null;
			request = null;
			Record r = (Record) selobj;
			if (r != null) {
				scaleId = r.getFieldValue("id").toString();
				classif = (String) r.getFieldValue("classif");
				updateServiceRequest();
			}
		}

		// set the request
		if ((scenarioId != null) && (scaleId != null) && (classif != null)) {
			setServiceRequest(request);
		} else {
			setMessage("sle.ui.msg28");
		}
	}

	/**
	 * update the {@link ServiceRequest} accoring to selected parameters
	 */
	private void updateServiceRequest() {
		// if any of main field are null, do nothing
		if ((scenarioId == null) || (scaleId == null) || (classif == null)) {
			return;
		}
		clearFormattForColums();
		String wc = "SCALE_ID = " + scaleId + " AND SCENARIO_ID = " + scenarioId;

		if (classif.equals("period")) {
			putClientProperty(TConstants.SHOW_COLUMNS, "l_value;amount");
			Vector<Record> srcdta = ConnectionManager.getAccessTo("planc_scale_magnitudes").search(wc, null);
			Record rcdmod = ConnectionManager.getAccessTo("planc_scale_magnitudes").getModel();
			// translate l_value to date
			rcdmod.setFieldValue("l_value", new Date(TStringUtils.ZERODATE.getTime()));
			for (Record re : srcdta) {
				Double d = ((Double) re.getFieldValue("l_value"));
				String slo = "" + d.intValue();
				re.setFieldValue("l_value", SLEPlanC.getSlotDate(slo));
			}
			request = new ServiceRequest(ServiceRequest.CLIENT_GENERATED_LIST, "planc_scale_magnitudes", srcdta);
			request.setParameter(ServiceResponse.RECORD_MODEL, rcdmod);
			setFormattForColums(0, "MMM-yyy");
		}
		if (classif.equals("time") || classif.equals("salary")) {
			putClientProperty(TConstants.SHOW_COLUMNS, "l_value;h_value;amount");
			request = new ServiceRequest(ServiceRequest.DB_QUERY, "planc_scale_magnitudes", wc);
		}
		if (classif.equals("insurance")) {
			putClientProperty(TConstants.SHOW_COLUMNS, "effective_date;affinity;l_age;h_age;female_amount;male_amount");
			request = new ServiceRequest(ServiceRequest.DB_QUERY, "SLE_SCALE_INSURANCE", "SCALE_ID = " + scaleId);
		}
	}
}
