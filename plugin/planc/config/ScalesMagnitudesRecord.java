package plugin.planc.config;

import gui.*;

import javax.swing.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * new/edit PLANC_SCALE_MAGNITUDES when classif = time || salary
 * 
 */
public class ScalesMagnitudesRecord extends AbstractRecordDataInput {

	public ScalesMagnitudesRecord(Record rcd, boolean newr) {
		super(null, rcd, newr);

		addInputComponent("l_value", TUIUtils.getJFormattedTextField(rcd, "l_value"), true, newr);
		addInputComponent("h_value", TUIUtils.getJFormattedTextField(rcd, "h_value"), true, true);
		addInputComponent("amount", TUIUtils.getJFormattedTextField(rcd, "amount"), true, true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, 100dlu", // columns
				"p, 3dlu, p, 3dlu, p"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		build.add(getLabelFor("l_value"), cc.xy(1, 1));
		build.add(getInputComponent("l_value"), cc.xy(3, 1));
		build.add(getLabelFor("h_value"), cc.xy(1, 3));
		build.add(getInputComponent("h_value"), cc.xy(3, 3));
		build.add(getLabelFor("amount"), cc.xy(1, 5));
		build.add(getInputComponent("amount"), cc.xy(3, 5));

		setDefaultActionBar();
		add(build.getPanel());
		preValidate(null);
	}

	@Override
	public void validateFields() {
		super.validateFields();
		// setEnableDefaultButton(true);
		double lv = (Double) ((JFormattedTextField) getInputComponent("l_value")).getValue();
		double hv = (Double) ((JFormattedTextField) getInputComponent("h_value")).getValue();
		if (lv > hv) {
			showAplicationExceptionMsg("sle.ui.msg100");
			setEnableDefaultButton(false);
		}
	}
}
