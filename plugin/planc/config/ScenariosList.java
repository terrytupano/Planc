package plugin.planc.config;

import gui.*;

import javax.swing.*;

import core.*;
import core.datasource.*;
import core.reporting.*;
import plugin.planc.*;
import action.*;

/**
 * SLE_SCENARIO list
 * 
 */
public class ScenariosList extends UIListPanel {

	private ServiceRequest request;
	private EditRecord clone;

	public ScenariosList() {
		super(null);
		this.request = new ServiceRequest(ServiceRequest.DB_QUERY, "sle_scenario", null);
		clone = new EditRecord(this);
		clone.setName("sle.clone.scenario");
		clone.setToolTip("ttsle.clone.scenario");
		clone.setIcon("CloneRecord");
		setToolBar(new NewRecord(this), new EditRecord(this), clone, new DeleteRecord(this), new ExportToFileAction(this, 
				""));
		putClientProperty(TConstants.SHOW_COLUMNS, "name;id");
		putClientProperty(TConstants.ICON_PARAMETERS, "0;ScenariosList");
		putClientProperty(SLEPlanC.PLANC_ID, 70320L);
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
			Record mod = getRecordModel();
			mod.setFieldValue("id", System.currentTimeMillis());
			pane = new ScenarioRecord(mod, true);
		}
		if (aa == clone) {
			pane = new ScenarioCopyDataRecord(getRecord());
			return pane;
		}
		if (aa instanceof EditRecord) {
			pane = new ScenarioRecord(getRecord(), false);
		}
		return pane;
	}
}
