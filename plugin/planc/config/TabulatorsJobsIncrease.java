package plugin.planc.config;

import gui.*;
import gui.docking.*;

import java.beans.*;

import javax.swing.*;

import plugin.planc.*;
import action.*;
import core.*;
import core.datasource.*;

/**
 * planc SLE_TAB_JOBS increase
 * 
 */
public class TabulatorsJobsIncrease extends UIListPanel implements DockingComponent {

	private Record tabvalRcd, jobRcd;
	private long tavid;
	private String companyId, jobid;

	public TabulatorsJobsIncrease() {
		super(null);
		putClientProperty(TConstants.SHOW_COLUMNS, "step;amount");
		putClientProperty(TConstants.ICON_PARAMETERS, "-1; ");
		setToolBar(new NewRecord(this), new EditRecord(this), new DeleteRecord(this));
	}

	@Override
	public void init() {
		setView(TABLE_VIEW);
		setMessage("sle.ui.msg06");
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		if (aa instanceof NewRecord) {
			Record mod = new Record(getRecordModel());
			mod.setFieldValue("TAB_VALIDITY_ID", tavid);
			mod.setFieldValue("COMPANY_ID", companyId);
			mod.setFieldValue("JOB_ID", jobid);
			pane = new TabulatorsJobsIncreaseRecord(mod, true);
		}
		if (aa instanceof EditRecord) {
			pane = new TabulatorsJobsIncreaseRecord(getRecord(), false);
		}
		return pane;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object src = evt.getSource();
		// Object prp = evt.getPropertyName();
		Object selobj = evt.getNewValue();

		// tabulator vigency selected
		if ((src instanceof TabulatorsTree)) {
			Record r = (Record) selobj;
			tabvalRcd = (r == null) || r.getFieldValue("subnode").equals("") ? null : r;
			// save companyid
			companyId = PlanCSelector.getNodeValue(PlanCSelector.COMPANY);
		}

		// jobs selected
		if ((src instanceof JobsTree)) {
			jobRcd = (Record) selobj;
		}

		// set the request
		if (tabvalRcd != null && jobRcd != null) {
			jobid = jobRcd.getFieldValue("id").toString();
			tavid = Long.valueOf(tabvalRcd.getFieldValue("node").toString());
			String wc = "tab_validity_id = '" + tavid + "' AND company_id = '" + companyId + "' AND job_id = '" + jobid
					+ "'";
			ServiceRequest sr = new ServiceRequest(ServiceRequest.DB_QUERY, "sle_tab_jobs", wc);
			setServiceRequest(sr);
		} else {
			setMessage("sle.ui.msg06");
		}
	}
}
