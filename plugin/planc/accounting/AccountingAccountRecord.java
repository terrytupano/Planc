package plugin.planc.accounting;

import gui.*;


import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * new/edit SLE_COST_CENTERS
 * 
 */
public class AccountingAccountRecord extends AbstractRecordDataInput {

	public AccountingAccountRecord(Record rcd, boolean newr) {
		super(null, rcd, newr);
		// cost center
		ServiceRequest sr = new ServiceRequest(ServiceRequest.DB_QUERY, "sle_cost_centers", "company_id='"+rcd.getFieldValue("company_id")+"'");
		RecordSelector rs = new RecordSelector(sr, "id", "name", rcd.getFieldValue("cost_center_id"));
		addInputComponent("cost_center_id", rs, false, newr);

		sr = new ServiceRequest(ServiceRequest.DB_QUERY, "sle_cost_types", "company_id='"+rcd.getFieldValue("company_id")+"'");
		rs = new RecordSelector(sr, "id", "name", rcd.getFieldValue("cost_type_id"));
		addInputComponent("cost_type_id", rs, false, newr);

		sr = new ServiceRequest(ServiceRequest.DB_QUERY, "sle_planc_account", null);
		rs = new RecordSelector(sr, "id", "name", rcd.getFieldValue("account_id"));
		addInputComponent("account_id", rs, false, newr);
		
		addInputComponent("ledger_acct_dim1", TUIUtils.getJTextField(rcd, "ledger_acct_dim1"), true, true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, pref, 50dlu",
				"p, 3dlu, p, 3dlu, p, 3dlu, p");
		CellConstraints cc = new CellConstraints();
		PanelBuilder pb = new PanelBuilder(lay);

		pb.add(getLabelFor("cost_center_id"), cc.xy(1, 1));
		pb.add(getInputComponent("cost_center_id"), cc.xyw(3, 1, 2));
		pb.add(getLabelFor("cost_type_id"), cc.xy(1, 3));
		pb.add(getInputComponent("cost_type_id"), cc.xyw(3, 3, 2));
		pb.add(getLabelFor("account_id"), cc.xy(1, 5));
		pb.add(getInputComponent("account_id"), cc.xyw(3, 5, 2));
		pb.add(getLabelFor("ledger_acct_dim1"), cc.xy(1, 7));
		pb.add(getInputComponent("ledger_acct_dim1"), cc.xy(3, 7));

		add(pb.getPanel());
		setDefaultActionBar();
		preValidate(null);
	}
}
