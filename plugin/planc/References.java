package plugin.planc;

import gui.*;

import javax.swing.*;

import core.*;
import core.datasource.*;

import action.*;

/**
 * mantenimiento sle_references
 * 
 */
public class References extends UIListPanel {

	private String refGroup;

	/**
	 * nueva instanca
	 * 
	 * @param rg - identificador de grupo
	 */
	public References() {
		super("sle.title17");
		// this.refGroup = rg;
		setToolBar(new NewRecord(this), new EditRecord(this), new DeleteRecord(this));
		putClientProperty(TConstants.SHOW_COLUMNS, "domain_name;item_value;meaning");
		// putClientProperty(PropertyNames.ICON_PARAMETERS, "DOMAIN_NAME;ITEM_VALUE;MEANING");
	}

	@Override
	public void init() {
		String wc = (refGroup == null) ? null : "domain_name = '" + refGroup + "'";
		setServiceRequest(new ServiceRequest(ServiceRequest.DB_QUERY, "sle_references", wc));
		setView(TABLE_VIEW);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.lizard.core.AppAbstractTable#getRightPanel(javax.swing.AbstractAction)
	 */
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		if (aa instanceof NewRecord) {
			Record rcd = getRecordModel();
			pane = new ReferencesRecord(rcd, true);
		}
		if (aa instanceof EditRecord) {
			pane = new ReferencesRecord(getRecord(), false);
		}
		return pane;
	}
}