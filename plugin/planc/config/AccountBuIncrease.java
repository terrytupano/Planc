package plugin.planc.config;

import gui.*;
import gui.docking.*;

import java.beans.*;

import javax.swing.*;

import action.*;
import core.*;
import core.datasource.*;

/**
 * planc SLE_BU_ACCOUNT increase
 * 
 */
public class AccountBuIncrease extends UIListPanel implements DockingComponent {

	private Long accountId;
	private ServiceRequest request;
	private String buId;

	public AccountBuIncrease() {
		super(null);
		this.request = new ServiceRequest(ServiceRequest.DB_QUERY, "sle_bu_account", null);
		setToolBar(false, new NewRecord(this), new EditRecord(this), new DeleteRecord(this));
		putClientProperty(TConstants.SHOW_COLUMNS, "since;amount");
		putClientProperty(TConstants.ICON_PARAMETERS, "-1; ");
		// putClientProperty(SLEPlanC.PLANC_ID, 80030L);
	}

	@Override
	public void init() {
		setView(TABLE_VIEW);
		setMessage("sle.ui.msg11");
		setFormattForColums(0, "MMM-yyy");
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		if (aa instanceof NewRecord) {
			Record r = getRecordModel();
			r.setFieldValue("bu_id", buId);
			r.setFieldValue("account_id", accountId);
			pane = new AccountBuIncreaseRecord(r, true);
		}
		if (aa instanceof EditRecord) {
			pane = new AccountBuIncreaseRecord(getRecord(), false);
		}
		return pane;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object src = evt.getSource();
		Object prp = evt.getPropertyName();
		Object newval = evt.getNewValue();

		// bud selected
		if ((src instanceof BuTree) && prp.equals(TConstants.RECORD_SELECTED)) {
			buId = newval == null ? null : (String) ((Record) newval).getFieldValue("id");
		}
		// account selected
		if ((src instanceof AccountTree) && prp.equals(TConstants.RECORD_SELECTED)) {
			accountId = newval == null ? null : (Long) ((Record) newval).getFieldValue("id");
		}

		// set the request
		if ((accountId != null) && (buId != null)) {
			String wc = "bu_id = '" + buId + "' AND account_id = " + accountId;
			request.setData(wc);
			setServiceRequest(request);
		} else {
			setMessage("sle.ui.msg11");
		}
	}
}
