package plugin.planc.compensation;

import gui.*;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import plugin.planc.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * new/edit sle_workforce
 * 
 */
public class WorkforceRecord extends AbstractRecordDataInput {

	public WorkforceRecord(Record rcd, boolean newr) {
		super(null, rcd, newr);
		rcd.setFieldValue("isnot_head", 0);

		addInputComponent("workrelation_id", TUIUtils.getJTextField(rcd, "workrelation_id"), true, true);
		addInputComponent("name", TUIUtils.getJTextField(rcd, "name"), true, true);
		addInputComponent("start_date", TUIUtils.getWebDateField(rcd, "start_date"), true, true);
		addInputComponent("end_date", TUIUtils.getWebDateField(rcd, "end_date"), false, true);
		addInputComponent("salary", TUIUtils.getJFormattedTextField(rcd, "salary"), false, true);
		// bu
		ServiceRequest sr = new ServiceRequest(ServiceRequest.DB_QUERY, "sle_planc_bu", null);
		RecordSelector rs = new RecordSelector(sr, "id", "name", rcd.getFieldValue("bu_id"));
		addInputComponent("bu_id", rs, false, true);
		addInputComponent("hire_date_1", TUIUtils.getWebDateField(rcd, "hire_date_1"), false, true);
		addInputComponent("hire_date_2", TUIUtils.getWebDateField(rcd, "hire_date_2"), false, true);
		addInputComponent("hire_date_3", TUIUtils.getWebDateField(rcd, "hire_date_3"), false, true);
		addInputComponent("hire_date_4", TUIUtils.getWebDateField(rcd, "hire_date_4"), false, true);
		// sex YESSS PLEASE JAJAJAJAJAJ !!!
		addInputComponent("sex", TUIUtils.getJComboBox("sle_workforce.gender", rcd, "sex"), false, true);
		addInputComponent("document_id", TUIUtils.getJTextField(rcd, "document_id"), false, true);
		addInputComponent("birthday", TUIUtils.getWebDateField(rcd, "birthday"), false, true);
		// 250118: bug18.3: activos y nuevos puestos para usuarios con filtro x BU
		TEntry[] te = SLEPlanC.getTEntryGroupFromDB("positionType");
		if (SLEPlanC.allowAllBU() != null) {
			te = new TEntry[]{te[0], te[2]};
		}
		JComboBox jcb = TUIUtils.getJComboBox("ttposition_type", te, rcd.getFieldValue("position_type"));
		addInputComponent("position_type", jcb, false, true);
		addInputComponent("step", TUIUtils.getJFormattedTextField(rcd, "step"), false, true);
		// job_id
		sr = new ServiceRequest(ServiceRequest.DB_QUERY, "sle_jobs", null);
		rs = new RecordSelector(sr, "id", "name", rcd.getFieldValue("job_id"));
		rs.insertItemAt(TStringUtils.getTEntry("tentry.null"), 0);
		rs.addActionListener(this);
		addInputComponent("job_id", rs, false, true);
		// tab_id
		sr = new ServiceRequest(ServiceRequest.DB_QUERY, "sle_tabulators", null);
		rs = new RecordSelector(sr, "id", "name", rcd.getFieldValue("tab_id"));
		rs.insertItemAt(TStringUtils.getTEntry("tentry.null"), 0);
		addInputComponent("tab_id", rs, false, true);
		// const types
		sr = new ServiceRequest(ServiceRequest.DB_QUERY, "sle_cost_types", null);
		rs = new RecordSelector(sr, "id", "name", rcd.getFieldValue("cost_type_id"), false);
		rs.insertItemAt(TStringUtils.getTEntry("tentry.null"), 0);
		rs.addActionListener(this);
		addInputComponent("cost_type_id", rs, false, true);
		// const center
		sr = new ServiceRequest(ServiceRequest.DB_QUERY, "sle_cost_centers", null);
		rs = new RecordSelector(sr, "id", "name", rcd.getFieldValue("cost_center_id"));
		rs.insertItemAt(TStringUtils.getTEntry("tentry.null"), 0);
		rs.addActionListener(this);
		addInputComponent("cost_center_id", rs, false, true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, 120dlu, 7dlu, left:pref, 3dlu, 120dlu", // columns
				"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder pb = new PanelBuilder(lay);

		pb.add(getLabelFor("workrelation_id"), cc.xy(1, 1));
		pb.add(getInputComponent("workrelation_id"), cc.xy(3, 1));
		pb.add(getLabelFor("document_id"), cc.xy(5, 1));
		pb.add(getInputComponent("document_id"), cc.xy(7, 1));
		pb.add(getLabelFor("name"), cc.xy(1, 3));
		pb.add(getInputComponent("name"), cc.xyw(3, 3, 5));
		pb.add(getLabelFor("birthday"), cc.xy(1, 5));
		pb.add(getInputComponent("birthday"), cc.xy(3, 5));
		pb.add(getLabelFor("sex"), cc.xy(5, 5));
		pb.add(getInputComponent("sex"), cc.xy(7, 5));
		pb.add(getLabelFor("bu_id"), cc.xy(1, 7));
		pb.add(getInputComponent("bu_id"), cc.xyw(3, 7, 5));
		pb.add(getLabelFor("job_id"), cc.xy(1, 9));
		pb.add(getInputComponent("job_id"), cc.xyw(3, 9, 5));
		// dates
		JPanel jp1 = new JPanel(new GridLayout(2, 6));
		jp1.setBorder(new TitledBorder(TStringUtils.getBundleString("sle_workforce.dates")));
		jp1.add(getLabelFor("start_date"));
		jp1.add(getLabelFor("hire_date_1"));
		jp1.add(getLabelFor("hire_date_2"));
		jp1.add(getLabelFor("hire_date_3"));
		jp1.add(getLabelFor("hire_date_4"));
		jp1.add(getLabelFor("end_date"));
		jp1.add(getInputComponent("start_date"));
		jp1.add(getInputComponent("hire_date_1"));
		jp1.add(getInputComponent("hire_date_2"));
		jp1.add(getInputComponent("hire_date_3"));
		jp1.add(getInputComponent("hire_date_4"));
		jp1.add(getInputComponent("end_date"));
		pb.add(jp1, cc.xyw(1, 11, 7));

		pb.add(getLabelFor("salary"), cc.xy(1, 13));
		pb.add(getInputComponent("salary"), cc.xy(3, 13));
		pb.add(getLabelFor("position_type"), cc.xy(5, 13));
		pb.add(getInputComponent("position_type"), cc.xy(7, 13));
		pb.add(getLabelFor("tab_id"), cc.xy(1, 15));
		pb.add(getInputComponent("tab_id"), cc.xyw(3, 15, 5));
		pb.add(getLabelFor("step"), cc.xy(1, 17));
		pb.add(getInputComponent("step"), cc.xy(3, 17));
		pb.add(getLabelFor("cost_type_id"), cc.xy(1, 19));
		pb.add(getInputComponent("cost_type_id"), cc.xyw(3, 19, 5));
		pb.add(getLabelFor("cost_center_id"), cc.xy(1, 21));
		pb.add(getInputComponent("cost_center_id"), cc.xyw(3, 21, 5));

		JPanel jp = pb.getPanel();
		JPanel jp2 = getAditionalInfoPanel(rcd);
		jp.setBorder(new EmptyBorder(4, 4, 4, 4));
		Dimension jp2d = new Dimension(jp.getPreferredSize().width - 20, jp2.getPreferredSize().height);
		jp2.setPreferredSize(jp2d);

		JScrollPane jsp = new JScrollPane(jp2, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jp2.setBorder(new EmptyBorder(4, 4, 4, 4));
		jsp.setPreferredSize(jp.getPreferredSize());
		jsp.setBorder(null);

		// unit increments of jlabel preferen height
		jsp.getVerticalScrollBar().setUnitIncrement(new JLabel(" ").getPreferredSize().height);

		JTabbedPane jtp = new JTabbedPane();
		jtp.add(TStringUtils.getBundleString("WorkforceRecord.general"), jp);
		jtp.add(TStringUtils.getBundleString("WorkforceRecord.addinfo"), jsp);
		add(jtp);
		setDefaultActionBar();
		preValidate(null);
	}

	private JPanel getAditionalInfoPanel(Record wr) {
		String wpatt = "scenario_id = " + wr.getFieldValue("scenario_id") + " AND company_id = '"
				+ wr.getFieldValue("COMPANY_ID") + "' AND workrelation_id = '" + wr.getFieldValue("workrelation_id")
				+ "' AND column_id = ";

		JPanel jp = new JPanel(new GridLayout(0, 2, 4, 4));
		JLabel label = null;
		JComponent inputjc = null;
		ServiceRequest sreques = null;
		Vector<Record> rlist = ConnectionManager.getAccessTo("sle_workforce_model_column").search(null, "orderval");
		for (Record rmodcol : rlist) {
			label = TUIUtils.getJLabel((String) rmodcol.getFieldValue("title"), false, true);
			String colId = rmodcol.getFieldValue("id").toString();
			String fileName = "";
			String field = "";
			String wc = wpatt + colId;
			// category
			if (rmodcol.getFieldValue("column_type").equals("C")) {
				fileName = "sle_model_cat_driver";
				field = "cat_value_id";
				Record r = ConnectionManager.getAccessTo("sle_model_cat_driver").exist(wc);
				sreques = new ServiceRequest(ServiceRequest.DB_QUERY, "sle_category_value", "category_id = "
						+ rmodcol.getFieldValue("category_id"));
				RecordSelector rs = new RecordSelector(sreques, "id", "code", (r == null)
						? null
						: r.getFieldValue("cat_value_id"), false);
				rs.insertItemAt(TStringUtils.getTEntry("tentry.null"), 0);
				inputjc = rs;
			}
			// String
			if (rmodcol.getFieldValue("column_type").equals("S")) {
				fileName = "sle_model_str_driver";
				field = "string_value";
				Record r = ConnectionManager.getAccessTo("sle_model_str_driver").exist(wc);
				if (r == null) {
					r = ConnectionManager.getAccessTo("sle_model_str_driver").getModel();
				}
				inputjc = TUIUtils.getJTextField(r, "string_value");
			}
			// number
			if (rmodcol.getFieldValue("column_type").equals("N")) {
				fileName = "sle_model_num_driver";
				field = "numeric_value";
				Record r = ConnectionManager.getAccessTo("sle_model_num_driver").exist(wc);
				if (r == null) {
					r = ConnectionManager.getAccessTo("sle_model_num_driver").getModel();
				}
				inputjc = TUIUtils.getJFormattedTextField(r, "numeric_value");
			}
			// date
			if (rmodcol.getFieldValue("column_type").equals("D")) {
				fileName = "sle_model_dat_driver";
				field = "date_value";
				Record r = ConnectionManager.getAccessTo("sle_model_dat_driver").exist(wc);
				if (r == null) {
					r = ConnectionManager.getAccessTo("sle_model_dat_driver").getModel();
				}
				inputjc = TUIUtils.getWebDateField(r, "date_value");
			}
			addInputComponent("colID=" + colId + ";fileName=" + fileName + ";field=" + field, inputjc, false, true);
			jp.add(label);
			jp.add(inputjc);
		}
		return jp;
	}
}
