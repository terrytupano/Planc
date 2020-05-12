package plugin.planc.security;

import gui.*;

import javax.swing.*;

import plugin.planc.*;
import action.*;
import core.*;
import core.datasource.*;

/**
 * sle_user_calc_types
 * 
 */
public class UserAutorizationsCalcTypes extends UIListPanel {

	private Record usrRcd;
	public UserAutorizationsCalcTypes(Record ur) {
		super(null);
		this.usrRcd = ur;
		setToolBar(new NewRecord(this), new EditRecord(this), new DeleteRecord(this));
		putClientProperty(TConstants.SHOW_COLUMNS, "calc_type_id;allow_calc");
		putClientProperty(TConstants.ICON_PARAMETERS, "-1; ");
		// putClientProperty(SLEPlanC.PLANC_ID, 50020L);
	}

	@Override
	public void init() {
		setServiceRequest(new ServiceRequest(ServiceRequest.DB_QUERY, "sle_user_calc_types", "user_id = "
				+ usrRcd.getFieldValue("id")));
		setReferenceColumn("allow_calc", SLEPlanC.getTEntryGroupFromDB("authCalcType"));
	}
	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		if (aa instanceof NewRecord) {
			Record mod = getRecordModel();
			mod.setFieldValue("user_id", usrRcd.getFieldValue("id"));
			pane = new UserAutorizationsCalcTypesRecord(mod, true);
		}
		if (aa instanceof EditRecord) {
			pane = new UserAutorizationsCalcTypesRecord(getRecord(), false);
		}
		return pane;
	}
}
