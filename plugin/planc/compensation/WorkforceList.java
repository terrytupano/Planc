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
import core.reporting.*;

/**
 * planc workforce
 * 
 */
public class WorkforceList extends UIListPanel implements DockingComponent {

	private ServiceRequest serviceRequest;
	private String scenarioID, companyID;

	public WorkforceList() {
		super(null);
		this.serviceRequest = new ServiceRequest(ServiceRequest.DB_QUERY, "sle_planc_workforce", null);
		setToolBar(new NewRecord(this), new EditRecord(this), new DeleteRecord(this), new ExportToFileAction(this, ""),
				new ImportFromFileAction(this));
		putClientProperty(TConstants.SHOW_COLUMNS, "workrelation_id;document_id;name");
		putClientProperty(TConstants.ICON_PARAMETERS, "0;gender_;sex");
		putClientProperty(SLEPlanC.PLANC_ID, 70860L);
	}

	@Override
	public void init() {
		setView(TABLE_VIEW);
		setMessage("sle.ui.msg15");
		// 180125: bug18.3 construct where clause according to BU autorization
		Vector<Record> vb = SLEPlanC.allowAllBU();
		if (vb != null) {
			String wc = "";
			for (Record r : vb) {
				wc += "bu_id = '" + r.getFieldValue("bu_id") + "' OR ";
			}
			serviceRequest.setData(wc.substring(0, wc.length() - 4));
		}
	}

	@Override
	public boolean executeAction(TActionEvent event) {
		if (event.getRedirectAction() instanceof DefaultCancelAction) {
			return true;
		}

		boolean ok = super.executeAction(event);
		if (ok && (event.getSource() instanceof EditRecord || event.getSource() instanceof NewRecord)) {
			AbstractRecordDataInput ardi = (AbstractRecordDataInput) event.getData();
			Record wr = ardi.getRecord();
			Hashtable<String, Object> ht = ardi.getFields();
			Vector<String> v = new Vector<String>(ht.keySet());
			for (String str : v) {
				if (str.startsWith("colID")) {
					Object val = ht.get(str);
					if (!val.toString().startsWith("*")) {
						String[] cofi = str.split(";");
						String colid = cofi[0].split("[=]")[1];
						String filename = cofi[1].split("[=]")[1];
						String field = cofi[2].split("[=]")[1];
						Record mod = ConnectionManager.getAccessTo(filename).getModel();
						mod.setFieldValue("SCENARIO_ID", wr.getFieldValue("scenario_id"));
						mod.setFieldValue("COMPANY_ID", wr.getFieldValue("COMPANY_ID"));
						mod.setFieldValue("WORKRELATION_ID", wr.getFieldValue("WORKRELATION_ID"));
						mod.setFieldValue("COLUMN_ID", Long.valueOf(colid));
						mod.setFieldValue(field, val);
						ConnectionManager.getAccessTo(filename).write(mod);
					}
				}
			}
		}
		return ok;
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		if (aa instanceof NewRecord) {
			Record mod = getRecordModel();
			mod.setFieldValue("scenario_id", scenarioID);
			mod.setFieldValue("company_id", companyID);
			pane = new WorkforceRecord(mod, true);
		}
		if (aa instanceof EditRecord) {
			pane = new WorkforceRecord(getRecord(), false);
		}
		if (aa instanceof ImportFromFileAction) {
			Record mod = getRecordModel();
			mod.setFieldValue("scenario_id", scenarioID);
			mod.setFieldValue("company_id", companyID);
			pane = new ImportFromFile(mod, "CDworkforce");
		}

		return pane;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// Object src = evt.getSource();
		Object prp = evt.getPropertyName();

		// path selected
		if (prp.equals(TConstants.PATH_SELECTED)) {
			// if path is complete show list
			if (PlanCSelector.isNodeSelected(PlanCSelector.COMPANY, PlanCSelector.SCENARIO)) {
				companyID = PlanCSelector.getNodeValue(PlanCSelector.COMPANY);
				scenarioID = PlanCSelector.getNodeValue(PlanCSelector.SCENARIO);
				serviceRequest.setData("scenario_id = " + scenarioID + " AND company_id = '" + companyID + "'");
				setServiceRequest(serviceRequest);
			} else {
				setMessage("sle.ui.msg15");
			}
		}
	}
}
