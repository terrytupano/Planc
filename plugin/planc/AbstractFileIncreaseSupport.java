package plugin.planc;

import gui.*;
import gui.docking.*;

import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import plugin.planc.config.*;

import com.alee.utils.swing.*;

import core.*;
import core.datasource.*;

/**
 * this class implement base direct edition and a convenient method for append blanck slot loaded form scenarios file
 * 
 */
public abstract class AbstractFileIncreaseSupport extends UIListPanel implements DockingComponent, CellEditorListener {

	protected String requestFrom = "";
	public AbstractFileIncreaseSupport() {
		super(null);
	}

	@Override
	public void editingCanceled(ChangeEvent e) {

	}

	@Override
	public void editingStopped(ChangeEvent e) {
		Object src = e.getSource();
		TEntry te = null;
		if (src instanceof TTableCellEditor) {
			TTableCellEditor tce = (TTableCellEditor) src;
			te = (TEntry) tce.getCellEditorValue();
		} else if (src instanceof WebDefaultCellEditor) {
			WebDefaultCellEditor wde = (WebDefaultCellEditor) src;
			RecordSelector rs = (RecordSelector) wde.getComponent();
			TEntry tes = (TEntry) rs.getSelectedItem();
			te = new TEntry(rs.getName(), tes);
		}
		Record sr = getRecord();
		//int ro = getJTable().getSelectedRow();
		//Record sr = ((TAbstractTableModel) getJTable().getModel()).getRecordAt(ro);
		// update values for the display list: filedname comes from component name
		sr.setFieldValue((String) te.getKey(), te.getValue());
		// i want update a copy of the selected record
		Record r1 = new Record(sr);

		// post process acording to servicerequest type
		// from date to slot
		if (requestFrom.equals("Times")) {
			int ts = SLEPlanC.getSlot((Date) r1.getFieldValue("time_slot"));
			r1.setFieldValue("time_slot", ts);
		}

		// l_value, h_value to slot 
		if (requestFrom.equals("Periods")) {
			int ts = SLEPlanC.getSlot((Date) r1.getFieldValue("l_value"));
			r1.setFieldValue("l_value", ts);
			r1.setFieldValue("h_value", ts);
		}

		// change category id from tentry to te.getkey
		if (requestFrom.equals(GeneralSalaryIncrease.class.getName())) {
			Object v = r1.getFieldValue("category_id");
			v = (v instanceof TEntry) ? ((TEntry) v).getKey() : v;
			r1.setFieldValue("category_id", v);
		}
		ConnectionManager.getAccessTo(r1.getTableName()).write(r1);
		freshen();
	}

	/**
	 * build an {@link ServiceRequest} procesing the initial list {@code srcdta} and append the empty slot to complete
	 * all slot configured in SLE_SCENARIO table.
	 * 
	 * @param srcdta - initial list for asociated file
	 * @param scenarioId - scenario id
	 * @param rcdmod - record model for table
	 * @param slotfn - slot field name form srcdata
	 * 
	 * @return {@link ServiceRequest#CLIENT_GENERATED_LIST} with new data
	 */
	public ServiceRequest getServicerRequestFromDate(Vector<Record> srcdta, String scenarioId, Record rcdmod,
			String slotfn) {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyMM");
		GregorianCalendar cal = new GregorianCalendar();

		ArrayList<TEntry> rcdlist = new ArrayList();
		for (Record tr : srcdta) {
			rcdlist.add(new TEntry(tr, ((Date) tr.getFieldValue(slotfn)).getTime()));
		}

		Record scercd = ConnectionManager.getAccessTo("SLE_SCENARIO").exist("id = " + scenarioId);

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
				Record r = new Record(rcdmod);
				r.setFieldValue(slotfn, scedat);
				rcdlist.add(new TEntry(r, scedat.getTime()));
			}
			cal.add(GregorianCalendar.MONTH, 1);
		}

		// sort elements
		Collections.sort(rcdlist);
		Vector<Record> rlist = new Vector();
		for (TEntry te : rcdlist) {
			rlist.add((Record) te.getKey());
		}

		ServiceRequest sr = new ServiceRequest(ServiceRequest.CLIENT_GENERATED_LIST, "", rlist);
		sr.setParameter(ServiceResponse.RECORD_MODEL, rcdmod);
		return sr;
	}

	/**
	 * create a {@link ServiceRequest} that complete all steps in increase by steps tables. the resulting list are the
	 * initial records found in src file plus the empty steps not present in src file plus and aditional record at the
	 * end of list
	 * 
	 * @param srclist - source list to complete
	 * @param rmod - record model to obtain data & keyfield
	 * 
	 * @return procesed list
	 */
	public ServiceRequest getServicerRequestFromStep(Vector<Record> srclist, Record rmod) {
		ArrayList<TEntry> rcdlist = new ArrayList();

		// calculate the last step in srclist
		int lasstep = 10;
		for (Record tr : srclist) {
			int st = (Integer) tr.getFieldValue("step");
			lasstep = (st > lasstep) ? st : lasstep;
			rcdlist.add(new TEntry(tr, st));
		}
		// steps allway star in 1
		for (int k = 1; k < lasstep + 2; k++) {
			boolean fs = false;
			for (TEntry te : rcdlist) {
				Integer rds = (Integer) te.getValue();
				fs = rds.equals(k) ? true : fs;
			}
			// if not found, append blanck slot
			if (!fs) {
				Record r = new Record(rmod);
				r.setFieldValue("step", k);
				rcdlist.add(new TEntry(r, k));
			}
		}

		// sort elements
		Collections.sort(rcdlist);
		Vector<Record> rlist = new Vector();
		for (TEntry te : rcdlist) {
			rlist.add((Record) te.getKey());
		}

		ServiceRequest sr = new ServiceRequest(ServiceRequest.CLIENT_GENERATED_LIST, "", rlist);
		sr.setParameter(ServiceResponse.RECORD_MODEL, rmod);
		return sr;
	}

	public ServiceRequest getServicerRequestFromPeriods(Vector<Record> srcdta, String scenarioId, Record rcdmod) {
		requestFrom = "Periods";
		// translate l_value to date
		rcdmod.setFieldValue("l_value", new Date(TStringUtils.ZERODATE.getTime()));
		for (Record r : srcdta) {
			Double d = ((Double) r.getFieldValue("l_value"));
			String slo = "" + d.intValue();
			r.setFieldValue("l_value", SLEPlanC.getSlotDate(slo));
		}
		return getServicerRequestFromDate(srcdta, scenarioId, rcdmod, "l_value");

	}

	public ServiceRequest getServicerRequestFromTime(Vector<Record> srcdta, String scenarioId, Record rcdmod) {
		requestFrom = "Times";
		// translate form timeslot format to date
		rcdmod.setFieldValue("time_slot", new Date(TStringUtils.ZERODATE.getTime()));
		for (Record r : srcdta) {
			String slo = r.getFieldValue("TIME_SLOT").toString();
			r.setFieldValue("TIME_SLOT", SLEPlanC.getSlotDate(slo));
		}
		return getServicerRequestFromDate(srcdta, scenarioId, rcdmod, "TIME_SLOT");

	}

	@Override
	protected void setServiceRequest(ServiceRequest sr) {
		super.setServiceRequest(sr);

		// override table elements and editor listeners
		JTable jt = getJTable();
		jt.getDefaultEditor(Double.class).addCellEditorListener(this);
	}
}
