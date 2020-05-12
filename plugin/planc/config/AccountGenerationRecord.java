package plugin.planc.config;

import gui.*;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import plugin.planc.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;
/**
 * new&edit SLE_PLANC_GEN_ACCOUNT
 * 
 */
public class AccountGenerationRecord extends AbstractRecordDataInput {

	public AccountGenerationRecord(Record rcd, boolean newr) {
		super(null, rcd, newr);

		//		ServiceRequest sr = new ServiceRequest(ServiceRequest.DB_QUERY, "sle_planc_account", null);
		//		RecordSelector cur_rs = new RecordSelector(sr, "id", "name", rcd.getFieldValue("account_id"));
//		addInputComponent("account_id", cur_rs, false, true);

		addInputComponent("qty_driver", TUIUtils.getJFormattedTextField(rcd, "qty_driver"), false, true);
		addInputComponent("value_driver", TUIUtils.getJFormattedTextField(rcd, "value_driver"), false, true);
		addInputComponent("factor_driver", TUIUtils.getJFormattedTextField(rcd, "factor_driver"), false, true);
		JComboBox jcb = SLEPlanC.getJComboBox("anniversaryPlanC", rcd, "in_anniversary");
		addInputComponent("in_anniversary", jcb, false, true);
		addInputComponent("execution_order", TUIUtils.getJFormattedTextField(rcd, "execution_order"), false, true);
		JScrollPane jsp = TUIUtils.getJTextArea(rcd, "formula_expression", 10);
		jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		addInputComponent("formula_expression", jsp, false, true);
		addInputComponent("anniv_months", TUIUtils.getJFormattedTextField(rcd, "anniv_months"), false, true);

		rcd.setFieldValue("with_salary", rcd.getFieldValue("with_salary").equals(Integer.valueOf(1)));
		addInputComponent("with_salary", TUIUtils.getJCheckBox(rcd, "with_salary"), false, true);
		rcd.setFieldValue("with_increase", rcd.getFieldValue("with_increase").equals(Integer.valueOf(1)));
		addInputComponent("with_increase", TUIUtils.getJCheckBox(rcd, "with_increase"), false, true);
		rcd.setFieldValue("general_expense", rcd.getFieldValue("general_expense").equals(Integer.valueOf(1)));
		addInputComponent("general_expense", TUIUtils.getJCheckBox(rcd, "general_expense"), false, true);
		rcd.setFieldValue("lookat_zero", rcd.getFieldValue("lookat_zero").equals(Integer.valueOf(1)));
		addInputComponent("lookat_zero", TUIUtils.getJCheckBox(rcd, "lookat_zero"), false, true);
		addInputComponent("compare_with", TUIUtils.getWebDateField(rcd, "compare_with"), false, true);
		addInputComponent("in_periods", TUIUtils.getJTextField(rcd, "in_periods"), false, true);
		addInputComponent("qty_determine", SLEPlanC.getJComboBox("plancFunc", rcd, "qty_determine"), false, true);
		addInputComponent("val_determine", SLEPlanC.getJComboBox("plancFunc", rcd, "val_determine"), false, true);
		addInputComponent("fac_determine", SLEPlanC.getJComboBox("plancFunc", rcd, "fac_determine"), false, true);

		ServiceRequest		sr = new ServiceRequest(ServiceRequest.DB_QUERY, "sle_category", null);
		RecordSelector rs = new RecordSelector(sr, "id", "name", rcd.getFieldValue("qty_category_id"), false);
		rs.insertItemAt(TStringUtils.getTEntry("tentry.null"), 0);
		addInputComponent("qty_category_id", rs, false, true);

		rs = new RecordSelector(sr, "id", "name", rcd.getFieldValue("qty_category_id"), false);
		rs.insertItemAt(TStringUtils.getTEntry("tentry.null"), 0);
		addInputComponent("val_category_id", rs, false, true);

		rs = new RecordSelector(sr, "id", "name", rcd.getFieldValue("qty_category_id"), false);
		rs.insertItemAt(TStringUtils.getTEntry("tentry.null"), 0);
		addInputComponent("fac_category_id", rs, false, true);

		sr = new ServiceRequest(ServiceRequest.DB_QUERY, "sle_scale", null);
		rs = new RecordSelector(sr, "id", "name", rcd.getFieldValue("qty_scale_id"), false);
		rs.insertItemAt(TStringUtils.getTEntry("tentry.null"), 0);
		addInputComponent("qty_scale_id", rs, false, true);

		rs = new RecordSelector(sr, "id", "name", rcd.getFieldValue("qty_scale_id"), false);
		rs.insertItemAt(TStringUtils.getTEntry("tentry.null"), 0);
		addInputComponent("val_scale_id", rs, false, true);

		rs = new RecordSelector(sr, "id", "name", rcd.getFieldValue("qty_scale_id"), false);
		rs.insertItemAt(TStringUtils.getTEntry("tentry.null"), 0);
		addInputComponent("fac_scale_id", rs, false, true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, 150dlu, 7dlu, left:pref, 50dlu, pref",
				"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu p, 3dlu p, 3dlu p, 3dlu p, p");
		CellConstraints cc = new CellConstraints();
		PanelBuilder mainpb = new PanelBuilder(lay);

		mainpb.add(getLabelFor("execution_order"), cc.xy(5, 1));
		mainpb.add(getInputComponent("execution_order"), cc.xy(7, 1));
		mainpb.add(new JLabel(" "), cc.xy(1, 1));
		mainpb.add(new JLabel(" "), cc.xy(3, 1));

		JPanel jp = new JPanel(new GridLayout(1, 4));
		jp.add(getInputComponent("with_salary"));
		jp.add(getInputComponent("general_expense"));
		jp.add(getInputComponent("with_increase"));
		jp.add(getInputComponent("lookat_zero"));
		mainpb.add(jp, cc.xyw(1, 3, 7));

		// qty panel
		lay = new FormLayout("left:40dlu, 3dlu, 150dlu, 7dlu, left:40dlu, 3dlu, pref", "p, 3dlu, p");
		cc = new CellConstraints();
		PanelBuilder pb = new PanelBuilder(lay);
		pb.add(getLabelFor("qty_driver"), cc.xy(1, 1));
		pb.add(getInputComponent("qty_driver"), cc.xy(3, 1));
		pb.add(getLabelFor("qty_category_id"), cc.xy(5, 1));
		pb.add(getInputComponent("qty_category_id"), cc.xy(7, 1));
		pb.add(getLabelFor("qty_scale_id"), cc.xy(1, 3));
		pb.add(getInputComponent("qty_scale_id"), cc.xy(3, 3));
		pb.add(getLabelFor("qty_determine"), cc.xy(5, 3));
		pb.add(getInputComponent("qty_determine"), cc.xy(7, 3));
		jp = pb.getPanel();
		jp.setBorder(new TitledBorder("Cantidad"));
		mainpb.add(jp, cc.xyw(1, 5, 7));

		// value panel
		lay = new FormLayout("left:40dlu, 3dlu, 150dlu, 7dlu, left:40dlu, 3dlu, pref", "p, 3dlu, p");
		cc = new CellConstraints();
		pb = new PanelBuilder(lay);
		pb.add(getLabelFor("value_driver"), cc.xy(1, 1));
		pb.add(getInputComponent("value_driver"), cc.xy(3, 1));
		pb.add(getLabelFor("val_category_id"), cc.xy(5, 1));
		pb.add(getInputComponent("val_category_id"), cc.xy(7, 1));
		pb.add(getLabelFor("val_scale_id"), cc.xy(1, 3));
		pb.add(getInputComponent("val_scale_id"), cc.xy(3, 3));
		pb.add(getLabelFor("val_determine"), cc.xy(5, 3));
		pb.add(getInputComponent("val_determine"), cc.xy(7, 3));
		jp = pb.getPanel();
		jp.setBorder(new TitledBorder("Valor"));
		mainpb.add(jp, cc.xyw(1, 7, 7));

		// factor panel
		lay = new FormLayout("left:40dlu, 3dlu, 150dlu, 7dlu, left:40dlu, 3dlu, pref", "p, 3dlu, p");
		cc = new CellConstraints();
		pb = new PanelBuilder(lay);
		pb.add(getLabelFor("factor_driver"), cc.xy(1, 1));
		pb.add(getInputComponent("factor_driver"), cc.xy(3, 1));
		pb.add(getLabelFor("fac_category_id"), cc.xy(5, 1));
		pb.add(getInputComponent("fac_category_id"), cc.xy(7, 1));
		pb.add(getLabelFor("fac_scale_id"), cc.xy(1, 3));
		pb.add(getInputComponent("fac_scale_id"), cc.xy(3, 3));
		pb.add(getLabelFor("fac_determine"), cc.xy(5, 3));
		pb.add(getInputComponent("fac_determine"), cc.xy(7, 3));
		jp = pb.getPanel();
		jp.setBorder(new TitledBorder("Factor"));
		mainpb.add(jp, cc.xyw(1, 9, 7));

		// misc component
		lay = new FormLayout("left:40dlu, 3dlu, 150dlu, 7dlu, left:40dlu, 3dlu, 80dlu", "p, 3dlu, p");
		cc = new CellConstraints();
		pb = new PanelBuilder(lay);
		pb.add(getLabelFor("in_anniversary"), cc.xy(1, 1));
		pb.add(getInputComponent("in_anniversary"), cc.xy(3, 1));
		pb.add(getLabelFor("anniv_months"), cc.xy(5, 1));
		pb.add(getInputComponent("anniv_months"), cc.xy(7, 1));
		pb.add(getLabelFor("compare_with"), cc.xy(1, 3));
		pb.add(getInputComponent("compare_with"), cc.xy(3, 3));
		pb.add(getLabelFor("in_periods"), cc.xy(5, 3));
		pb.add(getInputComponent("in_periods"), cc.xy(7, 3));
		jp = pb.getPanel();
		jp.setBorder(new TitledBorder("Aniversario"));
		mainpb.add(jp, cc.xyw(1, 11, 7));

		mainpb.add(getLabelFor("formula_expression"), cc.xy(1, 15));
		mainpb.add(getInputComponent("formula_expression"), cc.xyw(1, 16, 7));

		add(mainpb.getPanel());
		setDefaultActionBar();
		preValidate(null);
	}
	/*
	 * private JPanel getAcordionLayout() { WebAccordion accordion = new WebAccordion(WebAccordionStyle.accordionStyle);
	 * accordion.setMultiplySelectionAllowed(false); accordion.setBorder(null); accordion.setAnimate(false); EmptyBorder
	 * eb = new EmptyBorder(4, 4, 4, 4); JPanel tmp = null;
	 * 
	 * FormLayout lay = new FormLayout("left:pref, 3dlu, pref, 50dlu", "p, 3dlu, p, 3dlu, p"); CellConstraints cc = new
	 * CellConstraints(); PanelBuilder pb = new PanelBuilder(lay); pb.add(getLabelFor("execution_order"), cc.xy(1, 1));
	 * pb.add(getInputComponent("execution_order"), cc.xy(3, 1)); pb.add(getInputComponent("with_salary"), cc.xyw(1, 3,
	 * 4)); JPanel genpan = pb.getPanel(); genpan.setBorder(eb);
	 * 
	 * lay = new FormLayout("left:pref, 3dlu, 200dlu", "p, 3dlu, p, 3dlu, p, 3dlu, p"); cc = new CellConstraints(); pb =
	 * new PanelBuilder(lay); pb.add(getLabelFor("qty_driver"), cc.xy(1, 1)); pb.add(getInputComponent("qty_driver"),
	 * cc.xy(3, 1)); pb.add(getLabelFor("qty_category_id"), cc.xy(1, 3)); pb.add(getInputComponent("qty_category_id"),
	 * cc.xy(3, 3)); pb.add(getLabelFor("qty_scale_id"), cc.xy(1, 5)); pb.add(getInputComponent("qty_scale_id"),
	 * cc.xy(3, 5)); pb.add(getLabelFor("qty_determine"), cc.xy(1, 7)); pb.add(getInputComponent("qty_determine"),
	 * cc.xy(3, 7)); tmp = pb.getPanel(); tmp.setBorder(eb); accordion.addPane("Cantidad", tmp);
	 * 
	 * lay = new FormLayout("left:pref, 3dlu, 200dlu", "p, 3dlu, p, 3dlu, p, 3dlu, p"); cc = new CellConstraints(); pb =
	 * new PanelBuilder(lay); pb.add(getLabelFor("value_driver"), cc.xy(1, 1));
	 * pb.add(getInputComponent("value_driver"), cc.xy(3, 1)); pb.add(getLabelFor("val_category_id"), cc.xy(1, 3));
	 * pb.add(getInputComponent("val_category_id"), cc.xy(3, 3)); pb.add(getLabelFor("val_scale_id"), cc.xy(1, 5));
	 * pb.add(getInputComponent("val_scale_id"), cc.xy(3, 5)); pb.add(getLabelFor("val_determine"), cc.xy(1, 7));
	 * pb.add(getInputComponent("val_determine"), cc.xy(3, 7)); tmp = pb.getPanel(); tmp.setBorder(eb);
	 * accordion.addPane("Valor", tmp);
	 * 
	 * lay = new FormLayout("left:pref, 3dlu, 200dlu", "p, 3dlu, p, 3dlu, p, 3dlu, p"); cc = new CellConstraints(); pb =
	 * new PanelBuilder(lay); pb.add(getLabelFor("factor_driver"), cc.xy(1, 1));
	 * pb.add(getInputComponent("factor_driver"), cc.xy(3, 1)); pb.add(getLabelFor("fac_category_id"), cc.xy(1, 3));
	 * pb.add(getInputComponent("fac_category_id"), cc.xy(3, 3)); pb.add(getLabelFor("fac_scale_id"), cc.xy(1, 5));
	 * pb.add(getInputComponent("fac_scale_id"), cc.xy(3, 5)); pb.add(getLabelFor("fac_determine"), cc.xy(1, 7));
	 * pb.add(getInputComponent("fac_determine"), cc.xy(3, 7)); tmp = pb.getPanel(); tmp.setBorder(eb);
	 * accordion.addPane("Factor", tmp);
	 * 
	 * lay = new FormLayout("left:pref, 3dlu, 250dlu", "p, 3dlu, p, 3dlu, p, 3dlu, p, p"); cc = new CellConstraints();
	 * pb = new PanelBuilder(lay); pb.add(getInputComponent("general_expense"), cc.xyw(1, 1, 3));
	 * pb.add(getInputComponent("with_increase"), cc.xyw(1, 3, 3)); pb.add(getInputComponent("lookat_zero"), cc.xyw(1,
	 * 5, 3)); pb.add(getLabelFor("formula_expression"), cc.xyw(1, 7, 3));
	 * pb.add(getInputComponent("formula_expression"), cc.xyw(1, 8, 3)); JPanel laspanel = pb.getPanel();
	 * laspanel.setBorder(eb);
	 * 
	 * return new GroupPanel(false, genpan, accordion, laspanel); }
	 */

	@Override
	public Record getRecord() {
		Record r = super.getRecord();
		// forall boolean values conver to integer
		for (int c = 0; c < r.getFieldCount(); c++) {
			if (r.getFieldValue(c) instanceof Boolean) {
				r.setFieldValue(c, ((Boolean) r.getFieldValue(c)).equals(Boolean.TRUE) ? 1 : 0);
			}
		}
		return r;
	}
}
