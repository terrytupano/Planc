package plugin.planc.config;

import gui.*;

import java.text.*;

import com.alee.extended.date.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * new/edit SLE_PLANC_SALARY_INCREASE
 * 
 */
public class GeneralSalaryIncreaseRecord extends AbstractRecordDataInput {

	public GeneralSalaryIncreaseRecord(Record rcd, boolean newr) {
		super(null, rcd, newr);
		WebDateField wdf = TUIUtils.getWebDateField(rcd, "start_increase");
		wdf.setDateFormat(new SimpleDateFormat("MMM-yyyy"));
		addInputComponent("start_increase", wdf, true, newr);
		// NumberFormat nf = DecimalFormat.getInstance();
		// nf.setMinimumFractionDigits(5);
		// JFormattedTextField jftf = TUIUtils.getJFormattedTextField(rcd, "percentage", nf);
		addInputComponent("percentage", TUIUtils.getJFormattedTextField(rcd, "percentage"), false, true);
		addInputComponent("amount", TUIUtils.getJFormattedTextField(rcd, "amount"), false, true);
		RecordSelector rs = new RecordSelector(new ServiceRequest(ServiceRequest.DB_QUERY, "SLE_CATEGORY", null), "id",
				"name", rcd.getFieldValue("category_id"));
		addInputComponent("category_id", rs, false, true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, 150dlu", "p, 3dlu, p, 3dlu, p, 3dlu, p");
		CellConstraints cc = new CellConstraints();
		PanelBuilder pb = new PanelBuilder(lay);

		pb.add(getLabelFor("start_increase"), cc.xy(1, 1));
		pb.add(getInputComponent("start_increase"), cc.xy(3, 1));
		pb.add(getLabelFor("percentage"), cc.xy(1, 3));
		pb.add(getInputComponent("percentage"), cc.xy(3, 3));
		pb.add(getLabelFor("amount"), cc.xy(1, 5));
		pb.add(getInputComponent("amount"), cc.xy(3, 5));
		pb.add(getLabelFor("category_id"), cc.xy(1, 7));
		pb.add(getInputComponent("category_id"), cc.xy(3, 7));
		add(pb.getPanel());

		setDefaultActionBar();
		preValidate(null);
	}
}
