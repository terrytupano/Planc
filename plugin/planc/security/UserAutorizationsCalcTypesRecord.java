package plugin.planc.security;

import javax.swing.*;

import plugin.planc.*;
import gui.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

public class UserAutorizationsCalcTypesRecord extends AbstractRecordDataInput {

	public UserAutorizationsCalcTypesRecord(Record rcd, boolean newr) {
		super(null, rcd, newr);
		TEntry[] telist = SLEPlanC.getTEntryGroupFromDB("authCalcType");
		JComboBox jcb = TUIUtils.getJComboBox("ttallow_calc", telist, rcd.getFieldValue("allow_calc"));

		addInputComponent("calc_type_id", TUIUtils.getJFormattedTextField(rcd, "calc_type_id"), true, newr);
		addInputComponent("allow_calc", jcb, false, true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, 100dlu", "p, 3dlu, p");
		CellConstraints cc = new CellConstraints();
		PanelBuilder pb = new PanelBuilder(lay);

		pb.add(getLabelFor("calc_type_id"), cc.xy(1, 1));
		pb.add(getInputComponent("calc_type_id"), cc.xy(3, 1));
		pb.add(getLabelFor("allow_calc"), cc.xy(1, 3));
		pb.add(getInputComponent("allow_calc"), cc.xy(3, 3));
		add(pb.getPanel());
		setDefaultActionBar();
		preValidate(null);
	}
}
