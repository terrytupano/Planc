package plugin.planc.config;

import gui.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * nuevo/editar SLE_CLASSIFICATION_JOBS
 * 
 */
public class ClasificationRecord extends AbstractRecordDataInput {

	public ClasificationRecord(Record rcd, boolean newr) {
		super(null, rcd, newr);

		addInputComponent("id", TUIUtils.getJTextField(rcd, "id"), true, newr);
		addInputComponent("name", TUIUtils.getJTextArea(rcd, "name"), true, true);

		String wc = "company_id = '" + rcd.getFieldValue("company_id") + "'";
		ServiceRequest sr = new ServiceRequest(ServiceRequest.DB_QUERY, "sle_tabulators", wc);
		RecordSelector rs = new RecordSelector(sr, "id", "name", rcd.getFieldValue("tab_id"), false);
		rs.insertItemAt(TStringUtils.getTEntry("tentry.none"), 0);
		addInputComponent("tab_id", rs, false, true);
		
		FormLayout lay = new FormLayout("left:pref, 3dlu, pref, 100dlu", // columns
				"p, 3dlu, p, p, 3dlu, p, p"); // rows
		// lay.setColumnGroups(new int[][] { { 1, 5 }, { 3, 7 } });
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		build.add(getLabelFor("id"), cc.xy(1, 1));
		build.add(getInputComponent("id"), cc.xy(3, 1));
		build.add(getLabelFor("name"), cc.xy(1, 3));
		build.add(getInputComponent("name"), cc.xyw(1, 4, 4));
		build.add(getLabelFor("tab_id"), cc.xy(1, 6));
		build.add(getInputComponent("tab_id"), cc.xyw(1, 7, 4));

		setDefaultActionBar();
		add(build.getPanel());
		preValidate(null);
	}
}
