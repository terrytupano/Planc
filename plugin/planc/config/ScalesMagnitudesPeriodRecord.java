package plugin.planc.config;

import gui.*;

import java.text.*;
import java.util.*;

import plugin.planc.*;

import com.alee.extended.date.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * new/edit PLANC_SCALE_MAGNITUDES when classif = period
 * 
 */
public class ScalesMagnitudesPeriodRecord extends AbstractRecordDataInput {

	public ScalesMagnitudesPeriodRecord(Record rcd, boolean newr) {
		super(null, rcd, newr);
		WebDateField wdf = TUIUtils.getWebDateField(rcd, "l_value");
		wdf.setDateFormat(new SimpleDateFormat("MMM-yyyy"));
		addInputComponent("l_value", wdf, true, newr);
		addInputComponent("amount", TUIUtils.getJFormattedTextField(rcd, "amount"), true, true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, 100dlu", // columns
				"p, 3dlu, p"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		build.add(getLabelFor("l_value"), cc.xy(1, 1));
		build.add(getInputComponent("l_value"), cc.xy(3, 1));
		build.add(getLabelFor("amount"), cc.xy(1, 3));
		build.add(getInputComponent("amount"), cc.xy(3, 3));

		setDefaultActionBar();
		add(build.getPanel());
		preValidate(null);
	}

	@Override
	public Record getRecord() {
		Record r1 = super.getRecord();
		int ts = SLEPlanC.getSlot((Date) r1.getFieldValue("l_value"));
		r1.setFieldValue("l_value", ts);
		r1.setFieldValue("h_value", ts);
		return r1;
	}
}
