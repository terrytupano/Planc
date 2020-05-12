package action;

import gui.*;

import java.awt.event.*;
import java.text.*;

import javax.swing.*;

import core.*;
import core.datasource.*;

/**
 * this action reset the user's status seting to online and assign a one time password.
 * 
 * @author terry
 * 
 */
public class ResetPassword extends TAbstractAction {

	private UIListPanel panel;

	public ResetPassword(UIListPanel uilp) {
		super(TAbstractAction.RECORD_SCOPE);
		this.panel = uilp;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Record r = panel.getRecord();
		if (r != null) {
			String msg = MessageFormat.format(TStringUtils.getBundleString("action.reset.ms03"),
					SystemVariables.getintVar("OTPTimeout"));
			Object[] options = {TStringUtils.getBundleString("action.reset.confirm"),
					TStringUtils.getBundleString("action.reset.cancel")};
			int o = JOptionPane.showOptionDialog(PlanC.frame, msg, TStringUtils.getBundleString("action.reset.title"),
					JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);
			if (o == JOptionPane.YES_OPTION) {
				// 1234 << old otp
				r.setFieldValue("password", TStringUtils.getOneTimePassword((String) r.getFieldValue("username")));
				r.setFieldValue("inactive_since", null);
				ConnectionManager.getAccessTo("SLE_USERS").update(r);

				PlanC.showNotification("sle.ui.msg22", r.getFieldValue("username"), r.getFieldValue("password"));
			}
		}
	}
}