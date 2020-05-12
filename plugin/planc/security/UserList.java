package plugin.planc.security;

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
 * SLE_USERS
 * 
 */
public class UserList extends UIListPanel implements DockingComponent {

	public UserList() {
		super(null);
		setToolBar(new NewRecord(this), new EditRecord(this), new ResetPassword(this), new DeleteRecord(this),
				new ExportToFileAction(this, ""));
		putClientProperty(TConstants.SHOW_COLUMNS, "username;fullname;position");
		putClientProperty(TConstants.ICON_PARAMETERS, "0;UserList");
		putClientProperty(SLEPlanC.PLANC_ID, 50020L);
	}

	@Override
	public void init() {
		setServiceRequest(new ServiceRequest(ServiceRequest.DB_QUERY, "sle_users", null));
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		if (aa instanceof NewRecord) {
			Record mod = getRecordModel();
			mod.setFieldValue(0, System.currentTimeMillis());
			pane = new UserRecord(mod, true);
		}
		if (aa instanceof EditRecord) {
			pane = new UserRecord(getRecord(), false);
		}
		return pane;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

	}
}
