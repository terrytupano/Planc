package plugin.planc.security;

import java.beans.*;

import gui.*;
import gui.docking.*;

import javax.swing.*;

import core.*;
import core.datasource.*;
import core.reporting.*;

import plugin.planc.*;

import action.*;

/**
 * lista de archivo SLE_SECURITY_TEMPLATES
 * 
 */
public class SecurityTemplateList extends UIListPanel implements DockingComponent {

	public SecurityTemplateList() {
		super(null);
		setToolBar(new NewRecord(this), new EditRecord(this), new DeleteRecord(this), new ExportToFileAction(this, 
				""));
		putClientProperty(TConstants.SHOW_COLUMNS, "name");
		putClientProperty(TConstants.ICON_PARAMETERS, "0;SecurityTemplateList");
		putClientProperty(SLEPlanC.PLANC_ID, 50040L);
	}

	@Override
	public void init() {
		setServiceRequest(new ServiceRequest(ServiceRequest.DB_QUERY, "SLE_security_templates", null));
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		if (aa instanceof NewRecord) {
			Record mod = getRecordModel();
			mod.setFieldValue(0, System.currentTimeMillis());
			pane = new SecurityTemplatesRecord(mod, true);
		}
		if (aa instanceof EditRecord) {
			pane = new SecurityTemplatesRecord(getRecord(), false);
		}
		return pane;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

	}
}
