package plugin.planc.config;

import gui.*;
import gui.docking.*;

import java.beans.*;

import javax.swing.*;

import action.*;
import core.*;
import core.datasource.*;

/**
 * planc SLE_TAB_AMOUNT increase
 * 
 */
public class TabulatorsIncrease extends UIListPanel implements DockingComponent {

	private Record tabvalRcd;
	private long tabv;

	public TabulatorsIncrease() {
		super(null);
		putClientProperty(TConstants.SHOW_COLUMNS, "step;amount");
		putClientProperty(TConstants.ICON_PARAMETERS, "-1; ");
		setToolBar(new NewRecord(this), new EditRecord(this), new DeleteRecord(this));
	}

	@Override
	public void init() {
		setView(TABLE_VIEW);
		setMessage("sle.ui.msg07");
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		if (aa instanceof NewRecord) {
			Record mod = new Record(getRecordModel());
			mod.setFieldValue("TAB_VALIDITY_ID", tabv);
			pane = new TabulatorsIncreaseRecord(mod, true);
		}
		if (aa instanceof EditRecord) {
			pane = new TabulatorsIncreaseRecord(getRecord(), false);
		}
		return pane;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object src = evt.getSource();
		Object prp = evt.getPropertyName();
		Object selobj = evt.getNewValue();

		// tabulator vigency selected
		if ((src instanceof TabulatorsTree) && prp.equals(TConstants.RECORD_SELECTED)) {
			Record r = (Record) selobj;
			tabvalRcd = (r == null) || r.getFieldValue("subnode").equals("") ? null : r;
		}

		// set the request
		if (tabvalRcd != null) {
			tabv = new Long((String) tabvalRcd.getFieldValue("node"));
			ServiceRequest sr = new ServiceRequest(ServiceRequest.DB_QUERY, "SLE_TAB_AMOUNT", "tab_validity_id = "
					+ tabv);
			setServiceRequest(sr);
		} else {
			setMessage("sle.ui.msg07");
		}
	}
}
