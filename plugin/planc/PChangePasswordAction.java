package plugin.planc;

import java.awt.event.*;
import java.beans.*;

import javax.swing.*;

import action.*;
import core.*;
import core.datasource.*;

public class PChangePasswordAction extends TAbstractAction implements PropertyChangeListener {

	private JDialog dialog;
	private PChangePassword changePass;

	public PChangePasswordAction() {
		super(TAbstractAction.NO_SCOPE);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		changePass = new PChangePassword(Session.getUser(), false);
		changePass.addPropertyChangeListener(TConstants.ACTION_PERFORMED, this);
		dialog = getDialog(changePass, "action.PChangePasswordAction");
		dialog.setVisible(true);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == changePass && evt.getNewValue() instanceof AceptAction) {
			Record r = changePass.getRecord();
			ConnectionManager.getAccessTo("sle_users").write(r);
			// Session.updateUserRecord(r);
		}
		dialog.dispose();
	}
}
