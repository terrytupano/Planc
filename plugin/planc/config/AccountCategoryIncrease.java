package plugin.planc.config;

import gui.*;
import gui.docking.*;
import java.beans.*;
import javax.swing.*;
import action.*;
import core.*;
import core.datasource.*;

/**
 * planc sle_category_account increase
 * 
 */
public class AccountCategoryIncrease extends UIListPanel implements DockingComponent {

	private Long accountId, catValueId;
	private ServiceRequest request;

	public AccountCategoryIncrease() {
		super(null);
		this.request = new ServiceRequest(ServiceRequest.DB_QUERY, "sle_category_account", null);
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
			r.setFieldValue("cat_value_id", catValueId);
			r.setFieldValue("account_id", accountId);
			pane = new AccountCategoryIncreaseRecord(r, true);
		}
		if (aa instanceof EditRecord) {
			pane = new AccountCategoryIncreaseRecord(getRecord(), false);
		}
		return pane;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object src = evt.getSource();
		Object prp = evt.getPropertyName();
		Object newval = evt.getNewValue();

		// category value selected
		if ((src instanceof CategoryTree) && prp.equals(TConstants.RECORD_SELECTED)) {
			catValueId = null;
			// check if the selected record is a value not a cattegory
			Record r = (Record) newval;
			if (r != null && !r.getFieldValue("subnode").equals((long) 0)) {
				catValueId = (Long) r.getFieldValue("node");
			}
		}
		// account selected
		if ((src instanceof AccountTree) && prp.equals(TConstants.RECORD_SELECTED)) {
			accountId = newval == null ? null : (Long) ((Record) newval).getFieldValue("id");
		}

		// set the request
		if ((accountId != null) && (catValueId != null)) {
			String wc = "CAT_VALUE_ID = " + catValueId + " AND account_id = " + accountId;
			request.setData(wc);
			setServiceRequest(request);
		} else {
			setMessage("sle.ui.msg11");
		}
	}
}
