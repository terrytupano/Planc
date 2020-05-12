package plugin.planc.config;

import gui.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * new/edit sle_scenario
 * 
 */
public class CurrencyExchangeRecord extends AbstractRecordDataInput {

	public CurrencyExchangeRecord(Record rcd, boolean newr) {
//		super(null, rcd, newr);
		super(null, rcd, newr);

		// Record cr = ConnectionManager.getAccessTo("SLE_CURRENCY").exist("id = 937");
		ExtendedJLabel ejl = new ExtendedJLabel(new TEntry(937, "BOLIVAR"));
		addInputComponent("source_id", ejl, false, true);

		ServiceRequest sr = new ServiceRequest(ServiceRequest.DB_QUERY, "sle_currency", "id != 937");
		RecordSelector cur_rs = new RecordSelector(sr, "id", "name", rcd.getFieldValue("target_id"));
		addInputComponent("target_id", cur_rs, false, true);

		addInputComponent("since", TUIUtils.getWebDateField(rcd, "since"), true, true);
		addInputComponent("rate", TUIUtils.getJFormattedTextField(rcd, "rate"), true, true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, 100dlu", "p, 3dlu, p, 3dlu, p, 3dlu, p");
		CellConstraints cc = new CellConstraints();
		PanelBuilder pb = new PanelBuilder(lay);

		pb.add(getLabelFor("source_id"), cc.xy(1, 1));
		pb.add(getInputComponent("source_id"), cc.xy(3, 1));
		pb.add(getLabelFor("target_id"), cc.xy(1, 3));
		pb.add(getInputComponent("target_id"), cc.xy(3, 3));
		pb.add(getLabelFor("rate"), cc.xy(1, 5));
		pb.add(getInputComponent("rate"), cc.xy(3, 5));
		pb.add(getLabelFor("since"), cc.xy(1, 7));
		pb.add(getInputComponent("since"), cc.xy(3, 7));

		add(pb.getPanel());
		setDefaultActionBar();
		preValidate(null);
	}
}
