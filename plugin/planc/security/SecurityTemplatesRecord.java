/*
 * Created on 22/03/2005
 * (c) QQ  
 */
package plugin.planc.security;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import gui.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * nuevo/editar sle_seurity_templates
 * 
 */
public class SecurityTemplatesRecord extends AbstractRecordDataInput {

	private Record record;
	private Hashtable fields;

	/**
	 * nueva instancia
	 * 
	 * @param rcd - registro
	 * @param newr - true si es para crear un nuevo registro
	 */
	public SecurityTemplatesRecord(Record rcd, boolean newr) {
		super(null, rcd, newr);
		this.record = rcd;
		this.fields = null;

		JTextField jtf = TUIUtils.getJTextField("ttname", rcd.getFieldValue("name").toString(), 60);
		addInputComponent("name", jtf, true, newr);
		addInputComponent("role_opt", getJCheckBox("role_opt"), false, true);
		addInputComponent("check_payloc", getJCheckBox("check_payloc"), false, true);
		addInputComponent("check_payroll", getJCheckBox("check_payroll"), false, true);
		addInputComponent("check_trans", getJCheckBox("check_trans"), false, true);
		addInputComponent("omit_security", getJCheckBox("omit_security"), false, true);
		addInputComponent("omit_config", getJCheckBox("omit_config"), false, true);
		addInputComponent("omit_compensation", getJCheckBox("omit_compensation"), false, true);
		addInputComponent("omit_accounting", getJCheckBox("omit_accounting"), false, true);
		addInputComponent("omit_report", getJCheckBox("omit_report"), false, true);
		addInputComponent("omit_training", getJCheckBox("omit_training"), false, true);
		addInputComponent("omit_custom", getJCheckBox("omit_custom"), false, true);
		RecordSelector rs = new RecordSelector(
				new ServiceRequest(ServiceRequest.DB_QUERY, "SLE_PASSWORD_POLICY", null), "id", "name",
				rcd.getFieldValue("policy_id"), false);
		addInputComponent("policy_id", rs, false, true);

		// FormLayout lay = new FormLayout("left:pref, 3dlu, pref, 7dlu, left:pref, 3dlu, pref", // columns
		FormLayout lay = new FormLayout("left:pref, 3dlu, pref, 200dlu", // columns
				"p, 3dlu, p, 3dlu, p, 3dlu, p"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		build.add(getLabelFor("name"), cc.xy(1, 1));
		build.add(getInputComponent("name"), cc.xyw(3, 1, 2));

		JPanel jp1 = new JPanel(new GridLayout(0, 2));
		jp1.add(getInputComponent("role_opt"));
		jp1.add(getInputComponent("check_payloc"));
		jp1.add(getInputComponent("check_payroll"));
		jp1.add(getInputComponent("check_trans"));
		jp1.setBorder(new TitledBorder(TStringUtils.getBundleString("sle_security_templates.act")));
		build.add(jp1, cc.xyw(1, 3, 4));

		jp1 = new JPanel(new GridLayout(0, 2));
		jp1.add(getInputComponent("omit_security"));
		jp1.add(getInputComponent("omit_config"));
		jp1.add(getInputComponent("omit_compensation"));
		jp1.add(getInputComponent("omit_accounting"));
		jp1.add(getInputComponent("omit_report"));
		jp1.add(getInputComponent("omit_training"));
		jp1.add(getInputComponent("omit_custom"));
		jp1.add(Box.createGlue());
		jp1.setBorder(new TitledBorder(TStringUtils.getBundleString("sle_security_templates.omit")));
		build.add(jp1, cc.xyw(1, 5, 4));
		
		build.add(getLabelFor("policy_id"), cc.xy(1, 7));
		build.add(getInputComponent("policy_id"), cc.xyw(3, 7, 2));

		setDefaultActionBar();
		add(build.getPanel());
		preValidate(null);
	}

	@Override
	public Record getRecord() {
		this.fields = super.getFields();
		record.setFieldValue("name", fields.get("name"));
		record.setFieldValue("policy_id", fields.get("policy_id"));

		// all boolean fields to 0 1 Integer
		for (int i = 0; i < record.getFieldCount(); i++) {
			Object fval = fields.get(record.getFieldName(i));
			if (fval instanceof Boolean) {
				record.setFieldValue(i, ((Boolean) fval).booleanValue() == true ? new Integer(1) : new Integer(0));
			}
		}
		return record;
	}

	/**
	 * utilitario para retornar <code>JCheckBox</code> segun valor numerico 0 o 1
	 * 
	 * @param fn - nombre de campo
	 * @return <code>JCheckBox</code>
	 */
	private JCheckBox getJCheckBox(String fn) {
		int val = (Integer) record.getFieldValue(fn);
		return TUIUtils.getJCheckBox(fn, (val == 1));
	}
}
