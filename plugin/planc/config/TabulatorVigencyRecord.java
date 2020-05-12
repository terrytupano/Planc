package plugin.planc.config;

import gui.*;


import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * nuevo/editar SLE_TAB_VALIDITY
 * 
 */
public class TabulatorVigencyRecord extends AbstractRecordDataInput {

	public TabulatorVigencyRecord(Record rcd, boolean newr) {
		super(null, rcd, newr);

		addInputComponent("validity", TUIUtils.getWebDateField(rcd, "validity"), true, newr);
		addInputComponent("name", TUIUtils.getJTextField(rcd, "name"), true, true);
		addInputComponent("narrative", TUIUtils.getJTextArea(rcd, "narrative"), false, true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, pref, 7dlu, left:pref, 3dlu, 50dlu", // columns
				"p, 3dlu, p, p, 3dlu, p, p"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		build.add(getLabelFor("validity"), cc.xy(1, 1));
		build.add(getInputComponent("validity"), cc.xy(3, 1));
		build.add(getLabelFor("name"), cc.xy(1, 3));
		build.add(getInputComponent("name"), cc.xyw(1, 4, 7));
		build.add(getLabelFor("narrative"), cc.xy(1, 6));
		build.add(getInputComponent("narrative"), cc.xyw(1, 7, 7));

		setDefaultActionBar();
		add(build.getPanel());
		preValidate(null);
	}
}
