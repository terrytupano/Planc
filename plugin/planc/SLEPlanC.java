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
import gui.docking.*;

import java.io.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import plugin.planc.accounting.*;
import plugin.planc.compensation.*;
import plugin.planc.config.*;
import plugin.planc.security.*;
import action.*;

import com.alee.extended.list.*;
import com.alee.laf.button.*;
import com.alee.managers.popup.*;

import core.*;
import core.datasource.*;

/**
 * plugin entry for PlanC
 * 
 * @author terry
 * 
 */
public class SLEPlanC extends PluginAdapter {

	public static String PLANC_ID = "planc.id";
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("MMMMM-yyy");
	private static GregorianCalendar calendar = new GregorianCalendar();

	@Override
	public Object executePlugin(Object obj) {
		Vector<JComponent> menuVector = new Vector<JComponent>();
		JMenu jm = null;
		// Security
		if (Session.isAutorizedForModule("omit_security")) {
			jm = new JMenu("Seguridad");
			jm.add(new RolesAction());
			jm.add(new SecurityTemplateAction());
			jm.add(new UserAction());
			jm.addSeparator();
			MenuActionFactory maf = new MenuActionFactory(PasswordPolicy.class);
			// has no planc id
//			maf.putValue(SLEPlanC.PLANC_ID, 70330L);
			jm.add(maf);
			menuVector.add(jm);
		}
		// configuration
		if (Session.isAutorizedForModule("omit_config")) {
			jm = new JMenu("Configuracion");
			jm.add(new GeneralAction());
			jm.add(new OrganizationAction());
			MenuActionFactory maf = new MenuActionFactory(ScenariosList.class);
			maf.putValue(SLEPlanC.PLANC_ID, 70320L);
			jm.add(maf);
			jm.add(new ScenarioAction());
			jm.addSeparator();
			maf = new MenuActionFactory(CurrencyExchange.class);
			maf.putValue(SLEPlanC.PLANC_ID, 70330L);
			jm.add(maf);
			maf = new MenuActionFactory(ModeledColumnList.class);
			maf.putValue(SLEPlanC.PLANC_ID, 70362L);
			jm.add(maf);
//			jm.add(getDockingComponentsFrom("config"));
			menuVector.add(jm);
		}

		// Compensation
		if (Session.isAutorizedForModule("omit_compensation")) {
			jm = new JMenu("Compensacion");
			jm.add(new CollaboratorAction());
			jm.addSeparator();
			jm.add(new StoreProcedureAction());
//			jm.add(getDockingComponentsFrom("compensation"));
			menuVector.add(jm);
		}

		// accounting
		if (Session.isAutorizedForModule("OMIT_ACCOUNTING")) {
			jm = new JMenu("Contable");
			jm.add(new AccountAction());
//			jm.add(getDockingComponentsFrom("accounting"));
			menuVector.add(jm);
		}

		// planc legacy reports
		if (Session.isAutorizedForModule("OMIT_REPORT")) {
			JMenu repjm = new JMenu("Listados");
			DBAccess dba = ConnectionManager.getAccessTo("sle_references");
			Vector<Record> rlist = dba.search("domain_name = 'reportAccount'", null);
			// legacy reports: cuentas
			jm = new JMenu("Cuentas");
			for (Record r : rlist) {
				jm.add(new SLEReportAction((String) r.getFieldValue("item_value"), (String) r.getFieldValue("meaning")));
			}
			repjm.add(jm);
			// legacy reports: contable
			rlist = dba.search("domain_name = 'repAccounting'", null);
			jm = new JMenu("Contables");
			for (Record r : rlist) {
				jm.add(new SLEReportAction((String) r.getFieldValue("item_value"), (String) r.getFieldValue("meaning")));
			}
			repjm.add(jm);
			menuVector.add(repjm);
		}

		return menuVector;
	}

	/**
	 * implementation of planc bussines logic according to bug18.3
	 * <ol>
	 * <li>if user is autorized to one or more bussines unit -> show only those BU
	 * <li>if user is not autorized to none of BU -> show all
	 * </ol>
	 * This method look in sle_user_bu file and return a list of autorized bussines unit for actual user. if no record
	 * are found in file, return <code>null</code>
	 * 
	 * @return list of record or <code>null</code>
	 */
	public static Vector<Record> allowAllBU() {
		DBAccess dba = ConnectionManager.getAccessTo("sle_user_bu");
		Vector vb = dba.search("user_id = " + Session.getUserFieldValue("id"), null);
		if (vb.size() == 0) {
			return null;
		}
		return vb;
	}

	/**
	 * translate from planc time_slot/month_slot to string
	 * 
	 * @param slo - planc time_slot/month_slot
	 * @return String in calendar form (MMMMM-yyy)
	 */
	public static String getSlotString(String slo) {
		return dateFormat.format(getSlotDate(slo));
	}

	/**
	 * translate from planc time_slot/month_slot to Date
	 * 
	 * @param slo - planc time_slot/month_slot
	 * @return Date from slot
	 */
	public static Date getSlotDate(String slo) {
		calendar.set(GregorianCalendar.YEAR, 2000);
		calendar.set(GregorianCalendar.MONTH, Integer.parseInt(slo));
		return calendar.getTime();
	}

	public static int getSlot(Date d) {
		calendar.setTime(d);
		int y = calendar.get(GregorianCalendar.YEAR);
		int m = calendar.get(GregorianCalendar.MONTH);
		return (((y - 2000) * 12) + m);
	}

	/**
	 * similar to {@link TUIUtils#getJComboBox(String, TEntry[], Object)} but the list el element comming form planc
	 * reference file
	 * 
	 * @param dom - domain or grup of elements
	 * @param rcd - record to obtain data
	 * @param fld - field name
	 * 
	 * @return JComboBox
	 */
	public static JComboBox getJComboBox(String dom, Record rcd, String fld) {
		TEntry[] val = getTEntryGroupFromDB(dom);
		return TUIUtils.getJComboBox("tt" + fld, val, rcd.getFieldValue(fld));

	}
	/**
	 * similar to {@link TUIUtils#getCheckComboBox(String, Record, String)} but the list el element comming form planc
	 * reference file
	 * 
	 * @param dom - domain or grup of elements
	 * @param rcd - record to obtain data
	 * @param fld - field name
	 * 
	 * @return JComboBox
	 */
	public static CheckComboBox getCheckComboBox(String dom, Record rcd, String fld) {
		TEntry[] val = getTEntryGroupFromDB(dom);
		return TUIUtils.getCheckComboBox("tt" + fld, val, (String) rcd.getFieldValue(fld));
	}

	/**
	 * return a {@link WebButton} with an asociated list of check element. this component allow multiples element
	 * seleccion
	 * 
	 * @param dom - group id to obtain the list of elements
	 * @param rcd - Record
	 * @param fn - field name
	 * 
	 * @return a {@link WebButton} where the text is readeable string for internal selected values stored in
	 *         {@link #getName()}
	 */
	public static WebButton getMultiSelectionWebButton(String dom, Record rcd, String fn) {
		TEntry[] tearr = SLEPlanC.getTEntryGroupFromDB(dom);
		return getMultiSelectionWebButton("tt" + fn, tearr, rcd.getFieldValue(fn).toString());
	}

	/**
	 * return a {@link WebButton} with an asociated list of check element. this component allow multiples element
	 * seleccion
	 * 
	 * @param ttid - id for tooltip
	 * @param val - group of elements to build the list
	 * @param sell - internal list of values already selected
	 * 
	 * @return a {@link WebButton} where the text is readeable string for internal selected values stored in
	 *         {@link #getName()}
	 */
	public static WebButton getMultiSelectionWebButton(String ttid, TEntry[] val, String sell) {
		final WebButton towb = new WebButton();
		/*
		 * String txt = " "; String name = ""; CheckBoxListModel cblmodel = new CheckBoxListModel(); for (TEntry te :
		 * val) { boolean sel = sell.contains(te.getKey().toString()); cblmodel.addCheckBoxElement(te, sel); if (sel) {
		 * txt += te.getValue() + ", "; name += te.getKey() + ";"; } } txt = txt.equals(" ") ? txt : txt.substring(0,
		 * txt.length() - 2); name = name.equals("") ? name : name.substring(0, name.length() - 1); towb.setName(name);
		 * towb.setText(txt);
		 */

		CheckBoxListModel cblmodel = new CheckBoxListModel();
		for (TEntry te : val) {
			boolean sel = sell.contains(te.getKey().toString());
			cblmodel.addCheckBoxElement(te, sel);
		}

		TUIUtils.setToolTip(ttid, towb);

		final WebCheckBoxList checkBoxList = new WebCheckBoxList(cblmodel);
		checkBoxList.setVisibleRowCount(10);
		ListSelectionListener listen = new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				ListModel model = checkBoxList.getModel();
				int siz = model.getSize();
				String txt = " ";
				String name = "";
				for (int i = 0; i < siz; i++) {
					CheckBoxCellData cd = (CheckBoxCellData) model.getElementAt(i);
					TEntry te = (TEntry) cd.getUserObject();
					if (cd.isSelected()) {
						txt += te.getValue() + ", ";
						name += te.getKey() + ";";
					}
				}
				txt = txt.equals(" ") ? txt : txt.substring(0, txt.length() - 2);
				name = name.equals("") ? name : name.substring(0, name.length() - 1);
				towb.setName(name);
				towb.setText(txt);
			}
		};
		checkBoxList.addListSelectionListener(listen);
		checkBoxList.setEditable(true);
		WebButtonPopup popup = new WebButtonPopup(towb, PopupWay.rightDown);
		popup.setContent(checkBoxList);
		// fire event to update button text & internal components
		listen.valueChanged(null);
		return towb;
	}
	/**
	 * return an array of {@link TEntry} from reference file SLE_REFERENCES
	 * 
	 * @param dom - domain name to list retrival (group of elements)
	 * 
	 * @return array of {@link TEntry}
	 */
	public static TEntry[] getTEntryGroupFromDB(String dom) {
		Vector lst = new Vector();
		Vector kls = ConnectionManager.getAccessTo("sle_references").search("domain_name = '" + dom + "'", "order_val");
		for (int i = 0; i < kls.size(); i++) {
			Record rcd = (Record) kls.elementAt(i);
			String k = rcd.getFieldValue("item_value").toString();
			String v = rcd.getFieldValue("meaning").toString();
			lst.add(new TEntry(k, v));
		}
		TEntry[] lte = (TEntry[]) lst.toArray(new TEntry[lst.size()]);
		return lte;
	}

	public static TEntry[] getTEntryGroupFrom(String tn, String kf, String vf, String wc) {
		Vector lst = new Vector();
		Vector kls = ConnectionManager.getAccessTo(tn).search(wc, null);
		for (int i = 0; i < kls.size(); i++) {
			Record rcd = (Record) kls.elementAt(i);
			lst.add(new TEntry(rcd.getFieldValue(kf), rcd.getFieldValue(vf)));
		}
		TEntry[] lte = (TEntry[]) lst.toArray(new TEntry[lst.size()]);
		return lte;
	}

	/**
	 * look up inside of package name <code>pnam</code> for all instances of {@link DockingComponent} to build a JMenu
	 * with all view founde inside
	 * 
	 * @param pnam - package name to look for
	 * 
	 * @return JMenu build with all view or <code>null</code> if no DockingComponent was found
	 */
	public static JMenu getDockingComponentsFrom(String pnam) {
		JMenu jm = null;
		boolean ejm = false;
		try {
			String pip = TResourceUtils.USER_DIR + "/plugin/planc/" + pnam;
			// lookup for all plugin properties files
			Vector<File> clslist = TResourceUtils.findFiles(new File(pip), ".class");
			for (File file : clslist) {
				String c = "plugin.planc." + pnam + "." + file.getName().substring(0, file.getName().lastIndexOf("."));
				Class cls = Class.forName(c);
				Class interarr[] = cls.getInterfaces();
				for (Class inter : interarr) {
					if (inter.equals(DockingComponent.class)) {
						jm = jm == null ? new JMenu("Vistas") : jm;
						DockingComponent dc = (DockingComponent) cls.newInstance();
						DockingAction da = new DockingAction(cls);
						boolean tb = Session.isAutorizedForComponent(dc);
						da.setEnabled(tb);
						ejm = tb ? true : ejm;
						jm.add(da);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// disable jmenu if all items are disabled
		jm.setEnabled(ejm);
		return jm;
	}
}