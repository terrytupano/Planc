package plugin.planc.config;

import gui.*;


import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * nuevo/editar SLE_CATEGORY_VALUE
 * 
 */
public class CategoryValueRecord extends AbstractRecordDataInput {

	public CategoryValueRecord(Record rcd, boolean newr) {
		super(null, rcd, newr);

		addInputComponent("code", TUIUtils.getJTextField(rcd, "code"), true, true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, 100dlu, 7dlu, left:pref, 3dlu, 100dlu", // columns
				"p,p"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		build.add(getLabelFor("code"), cc.xy(1, 1));
		build.add(getInputComponent("code"), cc.xyw(1, 2, 7));

		setDefaultActionBar();
		add(build.getPanel());
		preValidate(null);
	}
}
