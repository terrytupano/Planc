/*
 * Created on 22/03/2005
 * (c) QQ  
 */
package plugin.planc.security;

import gui.*;

import javax.swing.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * nuevo/editar sle_users
 * 
 */
public class UserRecord extends AbstractRecordDataInput {

	private boolean newr;

	/**
	 * nueva instancia
	 * 
	 * @param rcd - registro
	 * @param newr - true si es para crear un nuevo registro
	 * @param uid - id de tipo de usuario a crear (constant.prperties)
	 */
	public UserRecord(Record rcd, boolean newr) {
		super(null, rcd, newr);
		this.newr = newr;
		// start_page
		ServiceRequest sr = new ServiceRequest(ServiceRequest.DB_QUERY, "sle_references",
				"domain_name = 'sleplancModule'");
		RecordSelector stp_rs = new RecordSelector(sr, "item_value", "meaning", rcd.getFieldValue("start_page"), false);
		// country
		sr = new ServiceRequest(ServiceRequest.DB_QUERY, "sle_country", null);
		RecordSelector cou_rs = new RecordSelector(sr, "id", "name", rcd.getFieldValue("country_id"));
		// templates
		sr = new ServiceRequest(ServiceRequest.DB_QUERY, "sle_security_templates", null);
		RecordSelector temp_rs = new RecordSelector(sr, "id", "name", rcd.getFieldValue("template_id"), false);

		JTextField jtf = TUIUtils.getJTextField("ttusername", rcd.getFieldValue("username").toString(), 10);
		addInputComponent("username", jtf, true, newr);
		addInputComponent("fullname", TUIUtils.getJTextField(rcd, "fullname"), true, true);
		addInputComponent("template_id", temp_rs, false, true);
		addInputComponent("email", TUIUtils.getJTextField(rcd, "email"), false, true);
		addInputComponent("position", TUIUtils.getJTextArea(rcd, "position"), false, true);
		addInputComponent("start_page", stp_rs, false, true);
		addInputComponent("country_id", cou_rs, false, true);
		addInputComponent("inactive_since", TUIUtils.getWebDateField(rcd, "inactive_since"), false, true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, pref, 7dlu, left:pref, 3dlu, pref", // columns
				"p, 3dlu, p, p, 3dlu, p, p, 3dlu, p, p, 3dlu p, p, 3dlu p, 3dlu p"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder pb = new PanelBuilder(lay);

		pb.add(getLabelFor("username"), cc.xy(1, 1));
		pb.add(getInputComponent("username"), cc.xy(3, 1));
		pb.add(getLabelFor("fullname"), cc.xy(1, 3));
		pb.add(getInputComponent("fullname"), cc.xyw(1, 4, 7));
		pb.add(getLabelFor("template_id"), cc.xy(1, 6));
		pb.add(getInputComponent("template_id"), cc.xyw(1, 7, 7));
		pb.add(getLabelFor("email"), cc.xy(1, 9));
		pb.add(getInputComponent("email"), cc.xyw(1, 10, 7));
		pb.add(getLabelFor("position"), cc.xy(1, 12));
		pb.add(getInputComponent("position"), cc.xyw(1, 13, 7));
		pb.add(getLabelFor("country_id"), cc.xy(1, 15));
		pb.add(getInputComponent("country_id"), cc.xy(3, 15));
		pb.add(getLabelFor("start_page"), cc.xy(5, 15));
		pb.add(getInputComponent("start_page"), cc.xy(7, 15));
		pb.add(getLabelFor("inactive_since"), cc.xy(1, 17));
		pb.add(getInputComponent("inactive_since"), cc.xy(3, 17));

		add(pb.getPanel());
		setDefaultActionBar();
		preValidate(null);
	}

	@Override
	public Record getRecord() {
		Record r = super.getRecord();
		// if new, init password = OTP
		if (newr) {
			// 1234 << old otp
			String uid = (String) r.getFieldValue("username");
			r.setFieldValue("password", TStringUtils.getOneTimePassword(uid));
		}
		return r;
	}
}
