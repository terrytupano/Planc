package plugin.planc.accounting;

import gui.*;


import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * new/edit SLE_COST_TYPES
 * 
 */
public class CostTypesRecord extends AbstractRecordDataInput {

	public CostTypesRecord(Record rcd, boolean newr) {
		super(null, rcd, newr);
		addInputComponent("id", TUIUtils.getJTextField(rcd, "id"), true, newr);
		addInputComponent("name", TUIUtils.getJTextField(rcd, "name"), true, true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, pref, 100dlu",
				"p, 3dlu, p, p");
		CellConstraints cc = new CellConstraints();
		PanelBuilder pb = new PanelBuilder(lay);

		pb.add(getLabelFor("id"), cc.xy(1, 1));
		pb.add(getInputComponent("id"), cc.xy(3, 1));
		pb.add(getLabelFor("name"), cc.xy(1, 3));
		pb.add(getInputComponent("name"), cc.xyw(1, 4, 4));

		add(pb.getPanel());
		setDefaultActionBar();
		preValidate(null);
	}
}
