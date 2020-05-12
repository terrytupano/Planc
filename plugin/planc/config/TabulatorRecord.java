package plugin.planc.config;

import gui.*;


import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * nuevo/editar SLE_CATEGORY
 * 
 */
public class TabulatorRecord extends AbstractRecordDataInput {

	public TabulatorRecord(Record rcd, boolean newr) {
		super(null, rcd, newr);

		addInputComponent("id", TUIUtils.getJTextField(rcd, "id"), true, newr);
		addInputComponent("name", TUIUtils.getJTextArea(rcd, "name"), false, true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, pref, 7dlu, left:pref, 3dlu, 50dlu", // columns
				"p, 3dlu, p, p"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		build.add(getLabelFor("id"), cc.xy(1, 1));
		build.add(getInputComponent("id"), cc.xy(3, 1));
		build.add(getLabelFor("name"), cc.xy(1, 3));
		build.add(getInputComponent("name"), cc.xyw(1, 4, 7));

		setDefaultActionBar();
		add(build.getPanel());
		preValidate(null);
	}
}
