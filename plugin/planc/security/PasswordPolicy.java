package plugin.planc.security;

import gui.*;

import javax.swing.*;

import action.*;
import core.*;
import core.datasource.*;
import core.reporting.*;

/**
 * SLE_PASSWORD_POLICY list
 * 
 */
public class PasswordPolicy extends UIListPanel {

	private ServiceRequest request;

	public PasswordPolicy() {
		super(null);

		this.request = new ServiceRequest(ServiceRequest.DB_QUERY, "SLE_PASSWORD_POLICY", null);
		setToolBar(new NewRecord(this), new EditRecord(this), new DeleteRecord(this), new ExportToFileAction(this, ""));
		putClientProperty(TConstants.SHOW_COLUMNS, "name;max_attemps;expiry_period");
		putClientProperty(TConstants.ICON_PARAMETERS, "-1; ");
		// putClientProperty(SLEPlanC.PLANC_ID, 70330L);
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
			pane = new PasswordPolicyRecord(getRecordModel(), true);
		}
		if (aa instanceof EditRecord) {
			pane = new PasswordPolicyRecord(getRecord(), false);
		}
		return pane;
	}
}
