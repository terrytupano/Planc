package delete;

import gui.*;
import gui.docking.*;

import java.beans.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import plugin.planc.compensation.*;
import plugin.planc.config.*;

import core.*;
import core.datasource.*;


import action.*;

/**
 * planc salary increase
 * 
 */
public class SalaryIncreaseTranspose extends UIListPanel implements DockingComponent, CellEditorListener {

	private ServiceRequest serviceRequest;
	private Record workfRcd, accountRcd;

	public SalaryIncreaseTranspose() {
		super(null);
		/*
		setToolBar(new JComponent[]{new JButton(new NewRecord(this)), new JButton(new EditRecord(this)),
				new JButton(new DeleteRecord(this)),
				new JButton(new ExportToFile("sle_planc_salary_increase", ""))});
*/
		putClientProperty(TConstants.ICON_PARAMETERS, "-1; ");
		putClientProperty(TConstants.ALLOW_INPUT_FROM_CELL, false);
	}

	@Override
	public void init() {
		setView(TABLE_VIEW);
		setMessage("sle.ui.msg13");
		serviceRequest = new ServiceRequest(ServiceRequest.DB_QUERY, "SLE_PLANC_SALARY_INCREASE", null);
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		if (aa instanceof NewRecord) {
			Record mod = getRecordModel();
			mod.setFieldValue("scenario_id", workfRcd.getFieldValue("scenario_id"));
			mod.setFieldValue("company_id", workfRcd.getFieldValue("company_id"));
			mod.setFieldValue("workrelation_id", workfRcd.getFieldValue("workrelation_id"));
			pane = new GeneralSalaryIncreaseRecord(mod, true);
		}
		if (aa instanceof EditRecord) {
			pane = new GeneralSalaryIncreaseRecord(getRecord(), false);
		}
		return pane;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object src = evt.getSource();
		Object prp = evt.getPropertyName();
//		setVisibleToolBar(false);
		setMessage("sle.ui.msg13");

		// company selected
		if (prp.equals(TConstants.PATH_SELECTED)) {
			workfRcd = null;
			accountRcd = null;
		}
		// workforce selected
		if ((src instanceof WorkforceList) && prp.equals(TConstants.RECORD_SELECTED)) {
			workfRcd = (Record) evt.getNewValue();
		}

		// account selected
		/*
		 * if ((src instanceof PAccount) && prp.equals(TConstants.RECORD_SELECTED)) { accountRcd = (Record)
		 * evt.getNewValue(); }
		 */

		// set the request
		// if (workfRcd != null && accountRcd != null) {
		if (workfRcd != null) {
			serviceRequest = createServicerRequest(workfRcd.getFieldValue("company_id"),
					workfRcd.getFieldValue("scenario_id"), (String) workfRcd.getFieldValue("workrelation_id"));

//			getToolBar().setVisible(true);
			setServiceRequest(serviceRequest);

			// overide jtable
			JTable tJTable = getJTable();
			tJTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			tJTable.setRowSelectionAllowed(false);
			tJTable.setColumnSelectionAllowed(true);
			tJTable.getDefaultEditor(Object.class).addCellEditorListener(this);


			// TDefaultTableCellRenderer tdcr = (TDefaultTableCellRenderer) jt.getDefaultRenderer(Date.class);
			// tdcr.setFormat(1, "MM/yyy");
		}
	}

	private ServiceRequest createServicerRequest(Object ciaid, Object sceid, String wrkid) {
		String wrkid2 = (wrkid == null) ? "ALL" : wrkid;
		String wc = "scenario_id = " + workfRcd.getFieldValue("scenario_id") + " AND company_id = '"
				+ workfRcd.getFieldValue("company_id") + "' AND workrelation_id = '" + wrkid2 + "'";

		// list form db
		DBAccess dba = ConnectionManager.getAccessTo("sle_planc_salary_increase");
		Vector<Record> tlist = dba.search(wc, null);

		Record mod = dba.getModel();
		mod.setFieldValue("scenario_id", sceid);
		mod.setFieldValue("company_id", ciaid);
		mod.setFieldValue("workrelation_id", wrkid2);

		SimpleDateFormat sdf = new SimpleDateFormat("MMMMM-yyy");
		GregorianCalendar cal = new GregorianCalendar();

		ArrayList<TEntry> rcdlist = new ArrayList();
		for (Record tr : tlist) {
			rcdlist.add(new TEntry(tr, ((Date) tr.getFieldValue("start_increase")).getTime()));
		}

		// append blanck period
		// scenario data
		Record scercd = ConnectionManager.getAccessTo("SLE_SCENARIO").exist("id = " + sceid);

		// append black slot in returned list if slot is not present
		cal.setTime((Date) scercd.getFieldValue("start_slot"));
		for (int k = 0; k < (Integer) scercd.getFieldValue("periods"); k++) {
			java.sql.Date scedat = new java.sql.Date(cal.getTime().getTime());
			String sces = sdf.format(scedat);
			boolean f = false;
			for (TEntry te : rcdlist) {
				String rds = sdf.format(new Date((Long) te.getValue()));
				f = rds.equals(sces) ? true : f;
			}
			// if not found, append blanck slot
			if (!f) {
				Record r = new Record(mod);
				r.setFieldValue("start_increase", scedat);
				rcdlist.add(new TEntry(r, scedat.getTime()));
			}
			cal.add(GregorianCalendar.MONTH, 1);
		}

		// sort element & transpose record fields
		Collections.sort(rcdlist);
		Vector<Record> rlist = new Vector();
		for (TEntry te : rcdlist) {
			rlist.add((Record) te.getKey());
		}

		Hashtable<String, Object> ht = transposeList(rlist, "START_INCREASE", sdf);
		putClientProperty(TConstants.SHOW_COLUMNS, ht.get("newColumns"));
		
		// remove the former key fields and last field
		Vector transl = (Vector) ht.get("transposeList");
		transl.remove(0);
		transl.remove(0);
		transl.remove(0);
		transl.remove(0);
		transl.remove(transl.size()-1);

		ServiceRequest sr = new ServiceRequest(ServiceRequest.CLIENT_GENERATED_LIST,
				"sle_planc_salary_increase", transl);
		sr.setParameter(ServiceResponse.RECORD_MODEL, ht.get("newRecodModel"));
		return sr;
	}

	/**
	 * this method recive a list of records and build an transpose version of that list. the transpose vertion have the following characteristic:
	 * <ul>
	 * <li> the columns name are the value found in the field named by <code>cfn</code> and formated using <code>fmt</code>
	 *  <li> 
	 * </ul> 
	 * TODO: complete documentation !!! son las 1:00 am y tengo flojera de pensar
	 * TODO: return the former columns definitions to avoid problems ????? O.o
	 * 
	 * @param rcdlist - list with data to transpose
	 * @param cfn - fieldname of original list who values will be the the new columns.
	 * @param fmt - a instance of {@link Format} to apply to every value of <code>cfn</code> to build the columns name
	 * 
	 * @return Hashtable
	 */
	private Hashtable<String, Object> transposeList(Vector<Record> rcdlist, String cfn, Format fmt) {
		Vector<Record> newlist = new Vector();
		String ncol = "";
		Record nmodel = new Record("", new Field[0]);

		// buidl fields
		for (int row = 0; row < rcdlist.size(); row++) {
			Record rowr = rcdlist.elementAt(row);
			Object o = rowr.getFieldValue(cfn);
			String nfn = fmt.format(o);
			nmodel.addNewField(new Field(nfn, rowr.getFieldValue(cfn), 10));
			ncol += nfn + ";";
		}

		// for every column in old list, add a new record in newone
		Record orgr = rcdlist.elementAt(0);
		for (int col = 0; col < orgr.getFieldCount(); col++) {
			Record newr = new Record(nmodel);
			for (int row = 0; row < rcdlist.size(); row++) {
				Record r = rcdlist.elementAt(row);
				newr.setFieldValue(row, r.getFieldValue(col));
			}
			newlist.add(newr);
		}

		// store the required info
		Hashtable<String, Object> ht = new Hashtable<String, Object>();
		ht.put("newColumns", ncol.substring(0, ncol.length() - 1));
		ht.put("newRecodModel", nmodel);
		ht.put("transposeList", newlist);
		return ht;
	}

	@Override
	public void editingStopped(ChangeEvent e) {
		System.out.println("SalaryIncreaseStd.editingStopped()");
	}

	@Override
	public void editingCanceled(ChangeEvent e) {
		System.out.println("SalaryIncreaseStd.editingCanceled()");
		
	}
}
