package plugin.planc.config;

import gui.*;
import gui.docking.*;

import java.beans.*;

import javax.swing.*;

import plugin.planc.*;
import action.*;
import core.*;
import core.datasource.*;
import core.reporting.*;

/**
 * planc SLE_CLASSIFICATION_JOBS
 * 
 */
public class ClasificationList extends UIListPanel implements DockingComponent {

	private ServiceRequest request;
	private String companyId;

	public ClasificationList() {
		super(null);
		this.request = new ServiceRequest(ServiceRequest.DB_QUERY, "sle_classification_jobs", null);
		setToolBar(new NewRecord(this), new EditRecord(this), new DeleteRecord(this), new ExportToFileAction(this, 
				""));
		putClientProperty(TConstants.SHOW_COLUMNS, "id;name");
		putClientProperty(TConstants.ICON_PARAMETERS, "0;ClasificationList");
		putClientProperty(SLEPlanC.PLANC_ID, 80030L);
	}
	@Override
	public void init() {
		setView(TABLE_VIEW);
		setVisibleToolBar(false);
		setMessage("sle.ui.msg05");
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		if (aa instanceof NewRecord) {
			Record r = getRecordModel();
			r.setFieldValue("company_id", companyId);
			pane = new ClasificationRecord(r, true);
		}
		if (aa instanceof EditRecord) {
			pane = new ClasificationRecord(getRecord(), false);
		}
		return pane;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object prp = evt.getPropertyName();

		// company selected
		if (prp.equals(TConstants.PATH_SELECTED)) {
			if (PlanCSelector.isNodeSelected(PlanCSelector.COMPANY)) {
				setVisibleToolBar(true);
				companyId = PlanCSelector.getNodeValue(PlanCSelector.COMPANY);
				setServiceRequest(request);
			} else {
				setMessage("sle.ui.msg05");
			}
		}
	}
}
