package plugin.planc.security;

import gui.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

public class UserAutorizationsPayrollImportRecord extends AbstractRecordDataInput {

	public UserAutorizationsPayrollImportRecord(Record rcd, boolean newr) {
		super(null, rcd, newr);
		addInputComponent("co_pay", TUIUtils.getJTextField(rcd, "co_pay"), true, newr);
		addInputComponent("payroll_id", TUIUtils.getJTextField(rcd, "payroll_id"), true, newr);
		addInputComponent("payroll_name", TUIUtils.getJTextField(rcd, "payroll_name"), true, true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, left:pref, 7dlu, left:pref, 3dlu, left:pref", "p, 3dlu, p, p");
		CellConstraints cc = new CellConstraints();
		PanelBuilder pb = new PanelBuilder(lay);

		pb.add(getLabelFor("co_pay"), cc.xy(1, 1));
		pb.add(getInputComponent("co_pay"), cc.xy(3, 1));
		pb.add(getLabelFor("payroll_id"), cc.xy(5, 1));
		pb.add(getInputComponent("payroll_id"), cc.xy(7, 1));
		pb.add(getLabelFor("payroll_name"), cc.xy(1, 3));
		pb.add(getInputComponent("payroll_name"), cc.xyw(1, 4, 7));
		add(pb.getPanel());
		setDefaultActionBar();
		preValidate(null);
	}
}
