package plugin.planc.config;

import gui.*;


import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

public class TabulatorsIncreaseRecord extends AbstractRecordDataInput {

	public TabulatorsIncreaseRecord(Record rcd, boolean newr) {
		super(null, rcd, newr);

		addInputComponent("step", TUIUtils.getJFormattedTextField(rcd, "step"), true, newr, 0,99);
		addInputComponent("amount", TUIUtils.getJFormattedTextField(rcd, "amount"), false, true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, pref, 50dlu", // columns
				"p, 3dlu, p"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		build.add(getLabelFor("step"), cc.xy(1, 1));
		build.add(getInputComponent("step"), cc.xy(3, 1));
		build.add(getLabelFor("amount"), cc.xy(1, 3));
		build.add(getInputComponent("amount"), cc.xy(3, 3));

		setDefaultActionBar();
		add(build.getPanel());
		preValidate(null);
	}
}
