package plugin.planc.config;

import gui.*;
import plugin.planc.*;

import com.alee.laf.separator.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * new/edit SLE_SCALE
 * 
 */
public class ScaleRecord extends AbstractRecordDataInput {

	public ScaleRecord(Record rcd, boolean newr) {
		super(null, rcd, newr);

		addInputComponent("name", TUIUtils.getJTextField(rcd, "name"), true, true);
		// not editable 
		addInputComponent("classif", SLEPlanC.getJComboBox("scaleClassif", rcd, "classif"), false, newr);

		rcd.setFieldValue("time_unit", rcd.getFieldValue("time_unit").toString());
		addInputComponent("time_unit", SLEPlanC.getJComboBox("timeUnit", rcd, "time_unit"), false, true);

		addInputComponent("start_dtype", SLEPlanC.getJComboBox("dateType", rcd, "start_dtype"), false, true);
		addInputComponent("end_dtype", SLEPlanC.getJComboBox("dateType", rcd, "end_dtype"), false, true);
		addInputComponent("compare_dtype", SLEPlanC.getJComboBox("dateType", rcd, "compare_dtype"), false, true);
		addInputComponent("affinities", TUIUtils.getJTextField(rcd, "affinities"), false, true);

		rcd.setFieldValue("omit_titular", rcd.getFieldValue("omit_titular").equals(1));
		addInputComponent("omit_titular", TUIUtils.getJCheckBox(rcd, "omit_titular"), false, true);

		addInputComponent("compare_dte", TUIUtils.getWebDateField(rcd, "compare_dte"), false, true);
		addInputComponent("ratio_type", TUIUtils.getJFormattedTextField(rcd, "ratio_type"), false, true);
		addInputComponent("adjust_val", TUIUtils.getJFormattedTextField(rcd, "adjust_val"), false, true);
		addInputComponent("adjust_type", TUIUtils.getJFormattedTextField(rcd, "adjust_type"), false, true);
		addInputComponent("fraction_val", TUIUtils.getJFormattedTextField(rcd, "fraction_val"), false, true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, 100dlu, 7dlu, left:pref, 3dlu, 100dlu", // columns
				"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu p, 3dlu p, 3dlu p, 3dlu p, 3dlu p"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder pb = new PanelBuilder(lay);

		pb.add(getLabelFor("name"), cc.xy(1, 1));
		pb.add(getInputComponent("name"), cc.xyw(3, 1, 5));
		pb.add(getLabelFor("classif"), cc.xy(1, 3));
		pb.add(getInputComponent("classif"), cc.xy(3, 3));
		pb.add(getLabelFor("time_unit"), cc.xy(5, 3));
		pb.add(getInputComponent("time_unit"), cc.xy(7, 3));
		pb.add(getLabelFor("start_dtype"), cc.xy(1, 5));
		pb.add(getInputComponent("start_dtype"), cc.xy(3, 5));
		pb.add(getLabelFor("end_dtype"), cc.xy(5, 5));
		pb.add(getInputComponent("end_dtype"), cc.xy(7, 5));
		pb.add(getLabelFor("compare_dtype"), cc.xy(1, 7));
		pb.add(getInputComponent("compare_dtype"), cc.xy(3, 7));
		pb.add(getLabelFor("compare_dte"), cc.xy(5, 7));
		pb.add(getInputComponent("compare_dte"), cc.xy(7, 7));
		pb.add(getLabelFor("ratio_type"), cc.xy(1, 9));
		pb.add(getInputComponent("ratio_type"), cc.xy(3, 9));
		pb.add(getLabelFor("adjust_val"), cc.xy(5, 9));
		pb.add(getInputComponent("adjust_val"), cc.xy(7, 9));
		pb.add(getLabelFor("adjust_type"), cc.xy(1, 11));
		pb.add(getInputComponent("adjust_type"), cc.xy(3, 11));
		pb.add(getLabelFor("fraction_val"), cc.xy(5, 11));
		pb.add(getInputComponent("fraction_val"), cc.xy(7, 11));

//		pb.addSeparator("", cc.xyw(1, 13, 7));
		pb.add(new WebSeparator(true, true), cc.xyw(1, 13, 7));

		pb.add(getLabelFor("affinities"), cc.xy(1, 15));
		pb.add(getInputComponent("affinities"), cc.xyw(3, 15,5));
		pb.add(getInputComponent("omit_titular"), cc.xy(1, 17));

		add(pb.getPanel());
		setDefaultActionBar();
		preValidate(null);
	}
	
	@Override
	public Record getRecord() {
		Record r = super.getRecord();
		r.setFieldValue("omit_titular", r.getFieldValue("omit_titular").equals(Boolean.TRUE) ? 1 : 0);
		r.setFieldValue("time_unit", Integer.valueOf(r.getFieldValue("time_unit").toString()));
		return r;
	}
}
