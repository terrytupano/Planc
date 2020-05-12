package plugin.planc.security;

import gui.*;

import javax.swing.*;

import action.*;
import core.*;
import core.datasource.*;

/**
 * sle_payroll_import
 * 
 */
public class UserAutorizationsPayrollImport extends UIListPanel {

	private Record usrRcd;
	public UserAutorizationsPayrollImport(Record ur) {
		super(null);
		this.usrRcd = ur;
		setToolBar(new NewRecord(this), new EditRecord(this), new DeleteRecord(this));
		putClientProperty(TConstants.SHOW_COLUMNS, "co_pay;payroll_id;payroll_name");
		putClientProperty(TConstants.ICON_PARAMETERS, "-1; ");
		// putClientProperty(SLEPlanC.PLANC_ID, 50020L);
	}

	@Override
	public void init() {
		ServiceRequest sr = new ServiceRequest(ServiceRequest.DB_QUERY, "sle_payroll_import", "user_id = "
				+ usrRcd.getFieldValue("id"));
		sr.setParameter(ServiceRequest.IGNORE_SECURITY, true);
		setServiceRequest(sr);
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		if (aa instanceof NewRecord) {
			Record mod = getRecordModel();
			mod.setFieldValue("user_id", usrRcd.getFieldValue("id"));
			pane = new UserAutorizationsPayrollImportRecord(mod, true);
		}
		if (aa instanceof EditRecord) {
			pane = new UserAutorizationsPayrollImportRecord(getRecord(), false);
		}
		return pane;
	}
}
