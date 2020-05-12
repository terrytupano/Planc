package plugin.planc.security;

import gui.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

public class PasswordPolicyRecord extends AbstractRecordDataInput {

	public PasswordPolicyRecord(Record rcd, boolean newr) {
		super(null, rcd, newr);

//		for new record, generate new record id
		if(newr) {
			rcd.setFieldValue("id", System.currentTimeMillis());
		}
//		addInputComponent("id", TUIUtils.getJFormattedTextField(rcd, "id"), true, newr);
		addInputComponent("name", TUIUtils.getJTextField(rcd, "name"), true, true);
		addInputComponent("max_attemps", TUIUtils.getJFormattedTextField(rcd, "max_attemps"), true, true);
		addInputComponent("expiry_period", TUIUtils.getJFormattedTextField(rcd, "expiry_period"), false, true);
		addInputComponent("pwd_stored", TUIUtils.getJFormattedTextField(rcd, "pwd_stored"), false, true);

		rcd.setFieldValue("SPECIAL_CHAR", ((Integer) rcd.getFieldValue("SPECIAL_CHAR")).equals(1));
		addInputComponent("SPECIAL_CHAR", TUIUtils.getJCheckBox(rcd, "SPECIAL_CHAR"), false, true);

		rcd.setFieldValue("MIN_NUM", ((Integer) rcd.getFieldValue("MIN_NUM")).equals(1));
		addInputComponent("MIN_NUM", TUIUtils.getJCheckBox(rcd, "MIN_NUM"), false, true);

		rcd.setFieldValue("MIN_CHAR", ((Integer) rcd.getFieldValue("MIN_CHAR")).equals(1));
		addInputComponent("MIN_CHAR", TUIUtils.getJCheckBox(rcd, "MIN_CHAR"), false, true);

		addInputComponent("PWD_STORED", TUIUtils.getJFormattedTextField(rcd, "PWD_STORED"), false, true);
		addInputComponent("MIN_LENGTH", TUIUtils.getJFormattedTextField(rcd, "MIN_LENGTH"), false, true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, 100dlu, 7dlu, left:pref, 3dlu, 100dlu",
				"p, 3dlu, p, p, 3dlu, p, 3dlu, p, 3dlu, p, p, p");
		CellConstraints cc = new CellConstraints();
		PanelBuilder pb = new PanelBuilder(lay);

//		pb.add(getLabelFor("id"), cc.xy(1, 1));
//		pb.add(getInputComponent("id"), cc.xy(3, 1));
		
		pb.add(getLabelFor("name"), cc.xy(1, 3));
		pb.add(getInputComponent("name"), cc.xyw(1, 4,7));
		
		pb.add(getLabelFor("max_attemps"), cc.xy(1, 6));
		pb.add(getInputComponent("max_attemps"), cc.xy(3, 6));
		pb.add(getLabelFor("expiry_period"), cc.xy(5, 6));
		pb.add(getInputComponent("expiry_period"), cc.xy(7, 6));
		
		pb.add(getLabelFor("PWD_STORED"), cc.xy(1, 8));
		pb.add(getInputComponent("PWD_STORED"), cc.xy(3, 8));
		pb.add(getLabelFor("MIN_LENGTH"), cc.xy(5, 8));
		pb.add(getInputComponent("MIN_LENGTH"), cc.xy(7, 8));
		
		pb.add(getInputComponent("SPECIAL_CHAR"), cc.xy(1, 10));
		pb.add(getInputComponent("MIN_NUM"), cc.xy(1, 11));
		pb.add(getInputComponent("MIN_CHAR"), cc.xy(1, 12));


		add(pb.getPanel());
		setDefaultActionBar();
		preValidate(null);
	}

	@Override
	public Record getRecord() {
		Record r = super.getRecord();

		// all boolean fields to 0 1 Integer
		for (int i = 0; i < r.getFieldCount(); i++) {
			Object fval = fields.get(r.getFieldName(i));
			if (fval instanceof Boolean) {
				r.setFieldValue(i, ((Boolean) fval).booleanValue() == true ? new Integer(1) : new Integer(0));
			}
		}
		return r;
	}

}
