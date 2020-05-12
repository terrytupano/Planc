package plugin.planc.config;

import gui.*;

import java.text.*;

import com.alee.extended.date.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * nuevo/editar sle_bu_account
 * 
 */
public class AccountCategoryIncreaseRecord extends AbstractRecordDataInput {

	public AccountCategoryIncreaseRecord(Record rcd, boolean newr) {
		super(null, rcd, newr);

		WebDateField wdf = TUIUtils.getWebDateField(rcd, "since");
		wdf.setDateFormat(new SimpleDateFormat("MMM-yyy"));

		addInputComponent("since", wdf, true, newr);
		addInputComponent("amount", TUIUtils.getJFormattedTextField(rcd, "amount"), true, true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, 100dlu", // columns
				"p, 3dlu, p"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		build.add(getLabelFor("since"), cc.xy(1, 1));
		build.add(getInputComponent("since"), cc.xy(3, 1));
		build.add(getLabelFor("amount"), cc.xy(1, 3));
		build.add(getInputComponent("amount"), cc.xy(3, 3));

		setDefaultActionBar();
		add(build.getPanel());
		preValidate(null);
	}
}
