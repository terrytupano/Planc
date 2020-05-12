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
public class CategoryRecord extends AbstractRecordDataInput {

	public CategoryRecord(Record rcd, boolean newr) {
		super(null, rcd, newr);

		addInputComponent("name", TUIUtils.getJTextField(rcd, "name"), true, true);
		addInputComponent("narrative", TUIUtils.getJTextArea(rcd, "narrative"), false, true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, 100dlu, 7dlu, left:pref, 3dlu, 100dlu", // columns
				"p,p, 3dlu, p, p"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		build.add(getLabelFor("name"), cc.xy(1, 1));
		build.add(getInputComponent("name"), cc.xyw(1, 2, 7));
		build.add(getLabelFor("narrative"), cc.xy(1, 4));
		build.add(getInputComponent("narrative"), cc.xyw(1, 5, 7));

		setDefaultActionBar();
		add(build.getPanel());
		preValidate(null);
	}
}
