package plugin.planc.compensation;

import plugin.planc.*;
import gui.*;


import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * new/edit SLE_PLANC_RELATIVES
 * 
 */
public class RelativeRecord extends AbstractRecordDataInput {

	/**
	 * new instance
	 * 
	 * @param rcd - workforce record
	 * @param newr - new or edit
	 */
	public RelativeRecord(Record rcd, boolean newr) {
		super(null, rcd, newr);

		// addInputComponent("relative_id", TUIUtils.getJTextField(rcd, "relative_id"), true, true);
		addInputComponent("relative_id",
				TUIUtils.getJTextField("ttrelative_id", (String) rcd.getFieldValue("relative_id"), 10), true, true);
		addInputComponent("name", TUIUtils.getJTextField(rcd, "name"), true, true);
		addInputComponent(
				"affinity",
				TUIUtils.getJComboBox("ttaffinity", SLEPlanC.getTEntryGroupFromDB("affinity"),
						rcd.getFieldValue("affinity")), false, true);
		addInputComponent("sex", TUIUtils.getJComboBox("sle_workforce.gender", rcd, "sex"), false, true);
		addInputComponent("birthday", TUIUtils.getWebDateField(rcd, "birthday"), false, true);
		addInputComponent("document_id", TUIUtils.getJTextField(rcd, "document_id"), true, true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, pref, 7dlu, left:pref, 3dlu, pref", // columns
				"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder pb = new PanelBuilder(lay);

		pb.add(getLabelFor("relative_id"), cc.xy(1, 1));
		pb.add(getInputComponent("relative_id"), cc.xy(3, 1));
		pb.add(getLabelFor("document_id"), cc.xy(5, 1));
		pb.add(getInputComponent("document_id"), cc.xy(7, 1));
		pb.add(getLabelFor("name"), cc.xy(1, 3));
		pb.add(getInputComponent("name"), cc.xyw(3, 3, 5));
		pb.add(getLabelFor("birthday"), cc.xy(1, 5));
		pb.add(getInputComponent("birthday"), cc.xy(3, 5));
		pb.add(getLabelFor("sex"), cc.xy(5, 5));
		pb.add(getInputComponent("sex"), cc.xy(7, 5));
		pb.add(getLabelFor("affinity"), cc.xy(1, 7));
		pb.add(getInputComponent("affinity"), cc.xy(3, 7));

		add(pb.getPanel());
		setDefaultActionBar();
		preValidate(null);
	}
}
