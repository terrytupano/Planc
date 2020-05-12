package plugin.planc.config;

import gui.*;

import javax.swing.*;

import plugin.planc.*;
import action.*;
import core.*;
import core.datasource.*;
import core.reporting.*;

/**
 * SLE_WORKFORCE_MODEL_COLUMN list
 * 
 */
public class ModeledColumnList extends UIListPanel {

	private ServiceRequest request;

	public ModeledColumnList() {
		super(null);

		this.request = new ServiceRequest(ServiceRequest.DB_QUERY, "SLE_WORKFORCE_MODEL_COLUMN", null);
		setToolBar(new NewRecord(this), new EditRecord(this), new DeleteRecord(this), new ExportToFileAction(this, 
				""));
		putClientProperty(TConstants.SHOW_COLUMNS, "title;orderval");
		putClientProperty(SLEPlanC.PLANC_ID, 70362L);
		putClientProperty(TConstants.ICON_PARAMETERS, "0;columnType_;column_type");
	}

	@Override
	public void init() {
		setView(TABLE_VIEW);
		setServiceRequest(request);
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		if (aa instanceof NewRecord) {
			Record r = getRecordModel();
			r.setFieldValue("id", System.currentTimeMillis());
			pane = new ModeledColumnRecord(r, true);
		}
		if (aa instanceof EditRecord) {
			pane = new ModeledColumnRecord(getRecord(), false);
		}
		return pane;
	}
}
