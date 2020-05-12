package plugin.planc;

import gui.*;

import java.text.*;

import com.alee.extended.date.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * new/edit SLE_PLANC_ACCOUNT_INCREASE that can be acces by diferent class
 * 
 */
public class AccountIncreaseRecord extends AbstractRecordDataInput {

	public AccountIncreaseRecord(Record rcd, boolean newr) {
		super(null, rcd, newr);
		WebDateField wdf = TUIUtils.getWebDateField(rcd, "start_increase");
		wdf.setDateFormat(new SimpleDateFormat("MMM-yyyy"));
		addInputComponent("start_increase", wdf, true, newr);
		addInputComponent("percentage", TUIUtils.getJFormattedTextField(rcd, "percentage"), false, true);
		addInputComponent("amount", TUIUtils.getJFormattedTextField(rcd, "amount"), false, true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, 100dlu", // columns
				"p, 3dlu, p, 3dlu, p"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		build.add(getLabelFor("start_increase"), cc.xy(1, 1));
		build.add(getInputComponent("start_increase"), cc.xy(3, 1));
		build.add(getLabelFor("percentage"), cc.xy(1, 3));
		build.add(getInputComponent("percentage"), cc.xy(3, 3));
		build.add(getLabelFor("amount"), cc.xy(1, 5));
		build.add(getInputComponent("amount"), cc.xy(3, 5));

		setDefaultActionBar();
		add(build.getPanel());
		preValidate(null);
	}
}
