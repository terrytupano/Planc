package plugin.planc.config;

import gui.*;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import plugin.planc.*;


import action.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * dialog to clone sle_scenario record & related data
 * 
 */
public class ScenarioCopyDataRecord extends AbstractDataInput {

	public ScenarioCopyDataRecord(Record rcd) {
		super("title.ScenarioCopyDataRecord");
		// all scenary but previous selected
		ServiceRequest sr = new ServiceRequest(ServiceRequest.DB_QUERY, "sle_scenario", "id != " +rcd.getFieldValue("id"));
		
		addInputComponent("copydata.src", TUIUtils.getExtendedJLabel(rcd, "name", false), false, true);
		addInputComponent("copydata.target", new RecordSelector(sr, "id", "name", 0), false, true);
		addInputComponent("copydata.updatetarget", TUIUtils.getJCheckBox("copydata.update", false), false, true);
		addInputComponent("copydata.generate.account", TUIUtils.getJCheckBox("copydata.generate.account", false), false, true);
		addInputComponent("copydata.salaryincrease", TUIUtils.getJCheckBox("copydata.salaryincrease", true), false, true);
		addInputComponent("copydata.presuppose", TUIUtils.getJCheckBox("copydata.presuppose", true), false, true);
		addInputComponent("copydata.scales", TUIUtils.getJCheckBox("copydata.scales", true), false, true);
		TEntry[] val = SLEPlanC.getTEntryGroupFromDB("positionType");
		addInputComponent("copydata.classif", TUIUtils.getJComboBox("ttcopydata.classif.", val, "0"), false, true);
		addInputComponent("copydata.workforce", TUIUtils.getJCheckBox("copydata.workforce", false), false, true);
		addInputComponent("copydata.aditionalinfo", TUIUtils.getJCheckBox("copydata.aditionalinfo", false), false, true);
		addInputComponent("copydata.relatives", TUIUtils.getJCheckBox("copydata.relatives", false), false, true);
		addInputComponent("copydata.workforcefact", TUIUtils.getJCheckBox("copydata.workforcefact", false), false, true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, 100dlu, 7dlu, left:pref, 3dlu, 100dlu", // columns
				"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu p, 3dlu p, 3dlu p"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder pb = new PanelBuilder(lay);

		pb.add(getLabelFor("copydata.src"), cc.xy(1, 1));
		pb.add(getInputComponent("copydata.src"), cc.xyw(3, 1, 5));
		pb.add(getLabelFor("copydata.target"), cc.xy(1, 3));
		pb.add(getInputComponent("copydata.target"), cc.xyw(3, 3, 5));
		
		JPanel jp = new JPanel(new GridLayout(3,2,4,4));
		jp.add(getInputComponent("copydata.updatetarget"));
		jp.add(getInputComponent("copydata.generate.account"));
		jp.add(getInputComponent("copydata.salaryincrease"));
		jp.add(getInputComponent("copydata.presuppose"));
		jp.add(getInputComponent("copydata.scales"));
		jp.setBorder(new TitledBorder("texto"));
		pb.add(jp, cc.xyw(1, 5, 7));

		jp = new JPanel(new GridLayout(3,2,4,4));
		jp.add(getLabelFor("copydata.classif"));
		jp.add(getInputComponent("copydata.classif"));
		jp.add(getInputComponent("copydata.workforce"));
		jp.add(getInputComponent("copydata.aditionalinfo"));
		jp.add(getInputComponent("copydata.relatives"));
		jp.add(getInputComponent("copydata.workforcefact"));
		jp.setBorder(new TitledBorder("otro texto"));
		pb.add(jp, cc.xyw(1, 7, 7));

		add(pb.getPanel());

		AceptAction aa = new AceptAction(this);
		aa.setName("Ejecutar");
		setActionBar(new AbstractAction[]{aa, new CancelAction(this)});
		preValidate(null);
	}

	@Override
	public void validateFields() {
		
	}
}
