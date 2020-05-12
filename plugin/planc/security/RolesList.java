package plugin.planc.security;

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
 * lista de archivo sle_roles
 * 
 */
public class RolesList extends UIListPanel implements DockingComponent {

	/**
	 * nueva instancia
	 * 
	 */
	public RolesList() {
		super(null);
		setToolBar(new NewRecord(this), new EditRecord(this), new DeleteRecord(this), new ExportToFileAction(this, ""));
		putClientProperty(TConstants.SHOW_COLUMNS, "id;name");
		putClientProperty(TConstants.ICON_PARAMETERS, "0;RolesList");
		putClientProperty(SLEPlanC.PLANC_ID, 50025L);
	}

	@Override
	public void init() {
		setServiceRequest(new ServiceRequest(ServiceRequest.DB_QUERY, "sle_roles", null));
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		if (aa instanceof NewRecord) {
			pane = new RolesRecord(getRecordModel(), true);
		}
		if (aa instanceof EditRecord) {
			pane = new RolesRecord(getRecord(), false);
		}
		return pane;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

	}
}
