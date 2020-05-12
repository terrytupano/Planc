package plugin.planc;

import gui.*;

import java.text.*;
import java.util.*;

import com.alee.extended.date.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * new/edit time_slot, amount record style for increase tables. used by mutiples modules
 * 
 */
public class AmountIncreaseRecord extends AbstractRecordDataInput {

	public AmountIncreaseRecord(Record rcd, boolean newr) {
		super(null, rcd, newr);
		WebDateField wdf = TUIUtils.getWebDateField(rcd, "time_slot");
		wdf.setDateFormat(new SimpleDateFormat("MMM-yyyy"));
		addInputComponent("time_slot", wdf, true, newr);
		addInputComponent("amount", TUIUtils.getJFormattedTextField(rcd, "amount"), false, true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, 100dlu", // columns
				"p, 3dlu, p, 3dlu, p"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		// space
		build.add(getLabelFor("time_slot"), cc.xy(1, 3));
		build.add(getInputComponent("time_slot"), cc.xy(3, 3));
		build.add(getLabelFor("amount"), cc.xy(1, 5));
		build.add(getInputComponent("amount"), cc.xy(3, 5));

		setDefaultActionBar();
		add(build.getPanel());
		preValidate(null);
	}

	@Override
	public Record getRecord() {
		Record r1 = super.getRecord();
		// from date to slot
		int ts = SLEPlanC.getSlot((Date) r1.getFieldValue("time_slot"));
		r1.setFieldValue("time_slot", ts);
		return r1;
	}
}
