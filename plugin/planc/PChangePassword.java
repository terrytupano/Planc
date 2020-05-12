/*******************************************************************************
 * Copyright (C) 2017 terry.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     terry - initial API and implementation
 ******************************************************************************/
package plugin.planc;

import gui.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.alee.laf.progressbar.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * Cambiar contraseña de aplicacion
 * 
 * 
 */
public class PChangePassword extends AbstractRecordDataInput implements ActionListener {

	private JPasswordField oldPassJP, newPassJP, confPassJP;
	private WebProgressBar progressBar;
	private String oldPass, newPass;
	private Record passPolicy;
	private int passlen;
	private Double BE;
	private Record record;
	// 171224: AT LASTTT !!!!! finished this fu**cking component!!! at last i made it beautyfull!!!

	/**
	 * new instance
	 * 
	 * @param rcd - SLE_USERS record
	 * @param cp - <code>true</code> if change password for a previouly password reset record
	 */
	public PChangePassword(Record rcd, boolean cp) {
		super(cp ? "security.title09" : "security.title08", rcd, false);
		this.record = rcd;
		this.passPolicy = ConnectionManager.getAccessTo("sle_password_policy").exist("ID=0");
		this.passlen = (Integer) passPolicy.getFieldValue("min_length");
		this.oldPass = (String) rcd.getFieldValue("password");
		rcd.setFieldValue("password", "");
		this.oldPassJP = TUIUtils.getJPasswordField("ttpassword", (String) rcd.getFieldValue("password"), 20);
		addInputComponent("password", oldPassJP, true, !cp);
		this.newPassJP = TUIUtils.getJPasswordField("ttchgpass.new", "", 20);
		addInputComponent("chgpass.new", newPassJP, true, true);
		this.confPassJP = TUIUtils.getJPasswordField("ttchgpass.verify", "", 20);
		confPassJP.getDocument().addDocumentListener(this);

		// entrpy H = L*Log2 N
		// base password strength n=26,
		BE = 10 * Math.log(26) / Math.log(2) * 1000;
		this.progressBar = new WebProgressBar(0, BE.intValue());
		progressBar.setStringPainted(true);
		progressBar.setPreferredSize(confPassJP.getPreferredSize());
		TUIUtils.setToolTip("ttchgpass.strong", progressBar);

		FormLayout lay = new FormLayout("left:pref, 40dlu, left:pref", "p, 3dlu, p, 3dlu, p, 15dlu, p, p, ");// rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		build.add(getLabelFor("password"), cc.xy(1, 1));
		build.add(getInputComponent("password"), cc.xy(3, 1));
		build.add(getLabelFor("chgpass.new"), cc.xy(1, 3));
		build.add(newPassJP, cc.xy(3, 3));
		build.add(TUIUtils.getJLabel("chgpass.verify", true, true), cc.xy(1, 5));
		build.add(confPassJP, cc.xy(3, 5));
		build.add(TUIUtils.getJLabel("chgpass.strong", false, true), cc.xy(1, 7));
		build.add(progressBar, cc.xyw(1, 8, 3));

		setDefaultActionBar();
		add(build.getPanel());

		preValidate(null);
	}

	@Override
	public void preValidate(Object src) {
		super.preValidate(src);
		progressBar.setString("Entropía base: " + BE.intValue());
		progressBar.setValue(0);
		if (!isShowingError()) {
			String op = new String(oldPassJP.getPassword());
			newPass = new String(newPassJP.getPassword());
			String cp = new String(confPassJP.getPassword());
			setEnableDefaultButton(false);

			// base lowercase character space
			int N = 26;

			// campo contrace;a no contiene la actual (solo si esta habilitado)
			if (src == oldPassJP && !oldPass.equals(op)) {
				showAplicationExceptionMsg("security.msg06");
				return;
			}

			// patron de contraceña
			if (!newPass.equals("")) {
				String err = "";

				// debe contener mayuscula
				boolean cs = newPass.matches("(?=.*[A-Z]).{1,}");
				if (cs) {
					N += 26;
				}
				if (passPolicy.getFieldValue("min_num").equals(1) && !cs) {
					err = TStringUtils.getBundleString("min_num") + ", ";
				}

				// debe contener digitos numericos
				cs = newPass.matches("(?=.*\\d).{1,}");
				if (cs) {
					N += 10;
				}
				if (passPolicy.getFieldValue("min_char").equals(1) && !cs) {
					err = err + TStringUtils.getBundleString("min_char") + ", ";
				}
				// for spetial char are all but leter or number
				cs = false;
				for (int i = 0; i < newPass.length(); i++) {
					cs = !(Character.isLetter(newPass.charAt(i)) || Character.isDigit(newPass.charAt(i))) ? true : cs;
				}
				if (cs) {
					N += 27; // all execpt digits and leters = 95 - 52-16
				}
				if (passPolicy.getFieldValue("special_char").equals(1) && !cs) {
					err = err + TStringUtils.getBundleString("special_char") + ", ";
					// err = newPass.matches("(?=.*[@#$%^&+=!¡]).{4,}") ? err : err + TStringUtils
					// .getBundleString("special_char") + ", ";
				}

				// Longitud
				if (newPass.length() < passlen) {
					err += TStringUtils.getBundleString("min_length") + " " + passlen + ", ";
				}

				// password strength
				// formula: L=H/Log2 N
				// base (a-z): n=26
				// numbers: n=10
				// mixed: n=52
				// all ascii: n=26 ???
				int L = newPass.length();
				Double H = L * Math.log(N) / Math.log(2) * 1000;
				progressBar.setValue(H.intValue());

				// Double d = H * 100 / progressBar.getMaximum() / 25;
				float f = (float) (H * .1 / progressBar.getMaximum()) / 3; // from red to green
				Color c = new Color(Color.HSBtoRGB(f, .8f, .6f));
				progressBar.setProgressTopColor(c);
				// TUIUtils.setProgressColor(progressBar, d);
				progressBar.setString("Entropía: " + H.intValue() + " Ataque x fuerza bruta: " + getAtackString(N, L));

				if (!err.equals("")) {
					err = err.substring(0, err.length() - 2);
					showAplicationException(new AplicationException("security.msg05", err));
					return;
				}
			}

			if (newPass.equals("") || cp.equals("")) {
				return;
			}
			// nueve y verificaicon no coinciden
			if (!newPass.equals(cp)) {
				showAplicationExceptionMsg("security.msg07");
				return;
			}
			setEnableDefaultButton(true);
		}
	}

	/**
	 * calculate average cost of brute force attack at 1000 try per secons
	 * 
	 * @param n - symbols space
	 * @param l - password leng
	 * 
	 * @return Average time to breack the password
	 */
	private static String getAtackString(int n, int l) {
		String rs = "";
		long tps = (new Double(Math.pow(n, l) / 2000)).longValue();
		rs = tps + " Segundos";
		tps = tps / 60;
		rs = tps > 1 ? tps + " Minutos" : rs;
		tps = tps / 60;
		rs = tps > 1 ? tps + " Horas" : rs;
		tps = tps / 24;
		rs = tps > 1 ? tps + " Dias" : rs;
		tps = tps / 365;
		rs = tps > 1 ? tps + " Años" : rs;

		// return rs + " (" + Math.pow(n, l) + ")";
		return rs;
	}

	@Override
	public Record getRecord() {
		record.setFieldValue("password", TStringUtils.getDigestString(newPass));
		return record;
	}
}
