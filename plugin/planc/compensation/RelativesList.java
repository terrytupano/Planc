package plugin.planc.compensation;

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
 * planc workforce relatives
 * 
 */
public class RelativesList extends UIListPanel implements DockingComponent {

	private ServiceRequest serviceRequest;
	private Record colaboratorRecord;

	public RelativesList() {
		super(null);
		this.serviceRequest = new ServiceRequest(ServiceRequest.DB_QUERY, "sle_planc_relatives", null);
		setToolBar(new NewRecord(this), new EditRecord(this), new DeleteRecord(this), new ExportToFileAction(this, 
				""));
		// new JButton(new ExportToFile("sle_users", "")), new SearchTextField(20, this, false)});
		putClientProperty(TConstants.SHOW_COLUMNS, "document_id;name;affinity");
		// putClientProperty(PropertyNames.SPECIAL_COLUMN, "t_ususer_id");
		putClientProperty(TConstants.ICON_PARAMETERS, "0;relatives_gender_;sex");

		// user id of workforce
		putClientProperty(SLEPlanC.PLANC_ID, 70860L);
	}

	@Override
	public void init() {
		// setServiceRequest(null);
		setView(TABLE_VIEW);
		setVisibleToolBar(false);
		setMessage("sle.ui.msg14");
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		if (aa instanceof NewRecord) {
			Record mod = getRecordModel();
			mod.setFieldValue("scenario_id", colaboratorRecord.getFieldValue("scenario_id"));
			mod.setFieldValue("company_id", colaboratorRecord.getFieldValue("company_id"));
			mod.setFieldValue("workrelation_id", colaboratorRecord.getFieldValue("workrelation_id"));
			pane = new RelativeRecord(mod, true);
		}
		if (aa instanceof EditRecord) {
			pane = new RelativeRecord(getRecord(), false);
		}
		return pane;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object src = evt.getSource();
		Object prp = evt.getPropertyName();
		
		// clear list for path selecction
		if (prp.equals(TConstants.PATH_SELECTED)) {
			setMessage("sle.ui.msg14");
		}
		
		// workforce
		if ((src instanceof WorkforceList) && prp.equals(TConstants.RECORD_SELECTED)) {
			colaboratorRecord = (Record) evt.getNewValue();
		}
		
		if (colaboratorRecord != null) {
			serviceRequest.setData("scenario_id = " + colaboratorRecord.getFieldValue("scenario_id")
					+ " AND company_id = '" + colaboratorRecord.getFieldValue("company_id")
					+ "' AND workrelation_id = '" + colaboratorRecord.getFieldValue("workrelation_id") + "'");
			setServiceRequest(serviceRequest);
		} else {
			setMessage("sle.ui.msg14");
		}
	}
}
