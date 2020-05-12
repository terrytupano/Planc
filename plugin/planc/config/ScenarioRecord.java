package plugin.planc.config;

import java.text.*;

import gui.*;

import com.alee.extended.date.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * new/edit sle_scenario
 * 
 */
public class ScenarioRecord extends AbstractRecordDataInput {

	public ScenarioRecord(Record rcd, boolean newr) {
		super(null, rcd, newr);

		// salary scheme
		ServiceRequest sr = new ServiceRequest(ServiceRequest.DB_QUERY, "sle_references",
				"domain_name = 'salaryScheme'");
		RecordSelector ssh_rs = new RecordSelector(sr, "item_value", "meaning", rcd.getFieldValue("salary_scheme"),
				false);
		// currency
		sr = new ServiceRequest(ServiceRequest.DB_QUERY, "sle_currency", null);
		RecordSelector cur_rs = new RecordSelector(sr, "id", "name", rcd.getFieldValue("currency_id"));
		// company
		sr = new ServiceRequest(ServiceRequest.DB_QUERY, "sle_company", null);
		RecordSelector cny_rs = new RecordSelector(sr, "id", "name", rcd.getFieldValue("company_id"));
		addInputComponent("name", TUIUtils.getJTextField(rcd, "name"), true, true);

		WebDateField wdf = TUIUtils.getWebDateField(rcd, "start_slot");
		wdf.setDateFormat(new SimpleDateFormat("MM/yyy"));
		addInputComponent("start_slot", wdf, true, true);

		addInputComponent("periods", TUIUtils.getJFormattedTextField(rcd, "periods"), true, true);
		addInputComponent("narrative", TUIUtils.getJTextArea(rcd, "narrative"), false, true);
		addInputComponent("status", TUIUtils.getJComboBox("scenarios.sts", rcd, "status"), true, true);
		addInputComponent("process_id", TUIUtils.getJFormattedTextField(rcd, "process_id"), false, true);
		addInputComponent("company_id", cny_rs, false, true);
		addInputComponent("salary_scheme", ssh_rs, false, true);
		addInputComponent("currency_id", cur_rs, false, true);
		// addInputComponent("left_periods", TUIUtils.getJFormattedTextField(rcd, "left_periods"), false, true);

		wdf = TUIUtils.getWebDateField(rcd, "start_forecast");
		wdf.setDateFormat(new SimpleDateFormat("MM/yyy"));
		addInputComponent("start_forecast", wdf, false, true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, 100dlu, 7dlu, left:pref, 3dlu, 100dlu", // columns
				"p, 3dlu, p, p, 3dlu, p, 3dlu, p, 3dlu p, 3dlu p, 3dlu p"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder pb = new PanelBuilder(lay);

		pb.add(getLabelFor("name"), cc.xy(1, 1));
		pb.add(getInputComponent("name"), cc.xyw(3, 1, 5));
		pb.add(getLabelFor("narrative"), cc.xy(1, 3));
		pb.add(getInputComponent("narrative"), cc.xyw(1, 4, 7));
		pb.add(getLabelFor("start_slot"), cc.xy(1, 6));
		pb.add(getInputComponent("start_slot"), cc.xy(3, 6));
		pb.add(getLabelFor("periods"), cc.xy(5, 6));
		pb.add(getInputComponent("periods"), cc.xy(7, 6));
		pb.add(getLabelFor("status"), cc.xy(1, 8));
		pb.add(getInputComponent("status"), cc.xy(3, 8));
		// pb.add(getLabelFor("process_id"), cc.xy(5, 8));
		// pb.add(getInputComponent("process_id"), cc.xy(7, 8));
//		pb.add(getLabelFor("company_id"), cc.xy(1, 10));
//		pb.add(getInputComponent("company_id"), cc.xy(3, 10));
		pb.add(getLabelFor("salary_scheme"), cc.xy(1, 10));
		pb.add(getInputComponent("salary_scheme"), cc.xy(3, 10));
		pb.add(getLabelFor("currency_id"), cc.xy(1, 12));
		pb.add(getInputComponent("currency_id"), cc.xy(3, 12));
		pb.add(getLabelFor("start_forecast"), cc.xy(5, 12));
		pb.add(getInputComponent("start_forecast"), cc.xy(7, 12));

		add(pb.getPanel());
		setDefaultActionBar();
		preValidate(null);
	}
}
