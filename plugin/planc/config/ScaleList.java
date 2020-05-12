package plugin.planc.config;

import gui.*;
import gui.docking.*;

import java.beans.*;

import javax.swing.*;

import core.*;
import core.datasource.*;
import core.reporting.*;

import plugin.planc.*;

import action.*;

/**
 * planc PLANC_SCALE_TYPES
 * 
 */
public class ScaleList extends UIListPanel implements DockingComponent {

	private ServiceRequest request;

	public ScaleList() {
		super(null);
		this.request = new ServiceRequest(ServiceRequest.DB_QUERY, "PLANC_SCALE_TYPES", null);
		setToolBar(new NewRecord(this), new EditRecord(this), new DeleteRecord(this), new ExportToFileAction(this, ""));
		putClientProperty(TConstants.SHOW_COLUMNS, "name;classif");
		putClientProperty(TConstants.ICON_PARAMETERS, "0;ScaleList");
		putClientProperty(SLEPlanC.PLANC_ID, 70340L);
	}

	@Override
	public void init() {
		setReferenceColumn("classif", SLEPlanC.getTEntryGroupFromDB("scaleClassif"));
		setServiceRequest(request);
		setView(TABLE_VIEW);
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		if (aa instanceof NewRecord) {
			pane = new ScaleRecord(getRecordModel(), true);
		}
		if (aa instanceof EditRecord) {
			pane = new ScaleRecord(getRecord(), false);
		}
		return pane;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// Object prp = evt.getPropertyName();
	}
}
