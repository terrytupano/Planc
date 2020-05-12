package plugin.planc.config;

import gui.*;

import javax.swing.*;

import plugin.planc.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * new/edit SLE_WORKFORCE_MODEL_COLUMN
 * 
 */
public class ModeledColumnRecord extends AbstractRecordDataInput {

	private RecordSelector catCombo;
	private JComboBox coltype;;

	public ModeledColumnRecord(Record rcd, boolean newr) {
		super(null, rcd, newr);

		addInputComponent("title", TUIUtils.getJTextField(rcd, "title"), true, true);
		addInputComponent("code", TUIUtils.getJTextField(rcd, "code"), true, true);

		this.coltype = SLEPlanC.getJComboBox("columnType", rcd, "column_type");
		coltype.addActionListener(this);
		addInputComponent("column_type", coltype, false, true);

		ServiceRequest sr = new ServiceRequest(ServiceRequest.DB_QUERY, "SLE_CATEGORY", null);
		this.catCombo = new RecordSelector(sr, "id", "name", rcd.getFieldValue("category_id"), false);
		addInputComponent("category_id", catCombo, false, true);

		addInputComponent("orderval", TUIUtils.getJFormattedTextField(rcd, "orderval"), false, true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, pref, 7dlu, left:pref, 3dlu, pref", // rows
				"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p");
		CellConstraints cc = new CellConstraints();
		PanelBuilder pb = new PanelBuilder(lay);

		pb.add(getLabelFor("title"), cc.xy(1, 1));
		pb.add(getInputComponent("title"), cc.xyw(3, 1, 5));
		pb.add(getLabelFor("code"), cc.xy(1, 3));
		pb.add(getInputComponent("code"), cc.xyw(3, 3, 5));
		pb.add(getLabelFor("column_type"), cc.xy(1, 7));
		pb.add(getInputComponent("column_type"), cc.xy(3, 7));
		pb.add(getLabelFor("category_id"), cc.xy(5, 7));
		pb.add(getInputComponent("category_id"), cc.xy(7, 7));
		pb.add(getLabelFor("orderval"), cc.xy(1, 9));
		pb.add(getInputComponent("orderval"), cc.xy(3, 9));

		add(pb.getPanel());
		setDefaultActionBar();
		preValidate(null);
	}

	@Override
	public void preValidate(Object src) {
		super.preValidate(src);
		TEntry te = (TEntry) coltype.getSelectedItem();
		setEnabledInputComponent("category_id", te.getKey().equals("C"));
	}

	@Override
	public Record getRecord() {
		Record r = super.getRecord();
		TEntry te = (TEntry) coltype.getSelectedItem();
		if (!te.getKey().equals("C")) {
			r.setFieldValue("category_id", 0);
		}
		return r;
	}
}
