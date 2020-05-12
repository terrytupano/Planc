package plugin.planc.config;

import gui.*;

import javax.swing.*;

import plugin.planc.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * nuevo/editar SLE_SCALE_INSURANCE 
 * 
 */
public class ScalesInsuranceRecord extends AbstractRecordDataInput {

	public ScalesInsuranceRecord(Record rcd, boolean newr) {
		super(null, rcd, newr);

		addInputComponent("effective_date", TUIUtils.getWebDateField(rcd, "effective_date"), true, newr);
		addInputComponent("affinity", SLEPlanC.getJComboBox("affinity", rcd, "affinity"), false, newr);
		addInputComponent("l_age", TUIUtils.getJFormattedTextField(rcd, "l_age"), true, newr);
		addInputComponent("h_age", TUIUtils.getJFormattedTextField(rcd, "h_age"), true, newr);
		addInputComponent("female_amount", TUIUtils.getJFormattedTextField(rcd, "female_amount"), true, true);
		addInputComponent("male_amount", TUIUtils.getJFormattedTextField(rcd, "male_amount"), true, true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, left:pref, 7dlu, left:pref, 3dlu, left:pref", // columns
				"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		build.add(getLabelFor("effective_date"), cc.xy(1, 1));
		build.add(getInputComponent("effective_date"), cc.xy(3, 1));
		build.add(getLabelFor("affinity"), cc.xy(5, 1));
		build.add(getInputComponent("affinity"), cc.xy(7, 1));
		
		build.addSeparator(TStringUtils.getBundleString("scale_insurance_age"), cc.xyw(1, 3,7));
		build.add(getLabelFor("l_age"), cc.xy(1, 5));
		build.add(getInputComponent("l_age"), cc.xy(3, 5));
		build.add(getLabelFor("h_age"), cc.xy(5, 5));
		build.add(getInputComponent("h_age"), cc.xy(7, 5));

		build.addSeparator(TStringUtils.getBundleString("scale_insurance_amo"), cc.xyw(1, 7,7));
		build.add(getLabelFor("female_amount"), cc.xy(1, 9));
		build.add(getInputComponent("female_amount"), cc.xy(3, 9));
		build.add(getLabelFor("male_amount"), cc.xy(5, 9));
		build.add(getInputComponent("male_amount"), cc.xy(7, 9));

		setDefaultActionBar();
		add(build.getPanel());
		preValidate(null);
	}

	@Override
	public void validateFields() {
		super.validateFields();
		// setEnableDefaultButton(true);
		double lv = ((Number) ((JFormattedTextField) getInputComponent("l_age")).getValue()).doubleValue();
		double hv = ((Number) ((JFormattedTextField) getInputComponent("h_age")).getValue()).doubleValue();
		if (lv > hv) {
			showAplicationExceptionMsg("sle.ui.msg101");
			setEnableDefaultButton(false);
		}
	}
}
