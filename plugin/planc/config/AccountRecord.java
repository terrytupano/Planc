package plugin.planc.config;

import gui.*;

import javax.swing.*;

import plugin.planc.*;


import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * nuevo/editar SLE_PLANC_ACCOUNT
 * 
 */
public class AccountRecord extends AbstractRecordDataInput {

	public AccountRecord(Record rcd, boolean newr) {
		super(null, rcd, newr);

		addInputComponent("id", TUIUtils.getJFormattedTextField(rcd, "id"), true, newr);
		addInputComponent("code", TUIUtils.getJTextField(rcd, "code"), true, true);
		addInputComponent("name", TUIUtils.getJTextField(rcd, "name"), true, true);
		addInputComponent("narrative", TUIUtils.getJTextArea(rcd, "narrative"), false, true);

		TEntry[] telist = SLEPlanC.getTEntryGroupFromDB("accountType");
		for (TEntry te : telist) {
			te.setKey(Long.valueOf((String) te.getKey()));
		}
		JComboBox jcb = TUIUtils.getJComboBox("tttype_id", telist, rcd.getFieldValue("type_id"));
		addInputComponent("type_id", jcb, false, true);

		addInputComponent("ledger_acct_dim1", TUIUtils.getJTextField(rcd, "ledger_acct_dim1"), false, true);

		rcd.setFieldValue("lookat_cost_center", rcd.getFieldValue("lookat_cost_center").equals(Integer.valueOf(1)));
		addInputComponent("lookat_cost_center", TUIUtils.getJCheckBox(rcd, "lookat_cost_center"), false, true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, 100dlu, 7dlu, left:pref, 3dlu, 100dlu", // columns
				"p, 3dlu, p, p, 3dlu, p, p, 3dlu, p,p, 3dlu, p"); // rows
		// lay.setColumnGroups(new int[][] { { 1, 5 }, { 3, 7 } });
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		build.add(getLabelFor("id"), cc.xy(1, 1));
		build.add(getInputComponent("id"), cc.xy(3, 1));
		build.add(getLabelFor("type_id"), cc.xy(5, 1));
		build.add(getInputComponent("type_id"), cc.xy(7, 1));
		build.add(getLabelFor("code"), cc.xy(1, 3));
		build.add(getInputComponent("code"), cc.xyw(1, 4, 7));
		build.add(getLabelFor("name"), cc.xy(1, 6));
		build.add(getInputComponent("name"), cc.xyw(1, 7, 7));
		build.add(getLabelFor("narrative"), cc.xy(1, 9));
		build.add(getInputComponent("narrative"), cc.xyw(1, 10, 7));
		build.add(getLabelFor("ledger_acct_dim1"), cc.xy(1, 12));
		build.add(getInputComponent("ledger_acct_dim1"), cc.xy(3, 12));
		build.add(getInputComponent("lookat_cost_center"), cc.xyw(5, 12, 3));

		setDefaultActionBar();
		add(build.getPanel());
		preValidate(null);
	}
	@Override
	public Record getRecord() {
		Record r = super.getRecord();
		r.setFieldValue("lookat_cost_center", ((Boolean) r.getFieldValue("lookat_cost_center")).equals(Boolean.TRUE)
				? 1
				: 0);
		return r;
	}
}
