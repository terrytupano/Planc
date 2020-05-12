package plugin.planc.dashboard;

import java.util.*;
import java.util.concurrent.*;

import javax.swing.*;

import plugin.planc.*;
import core.*;
import core.datasource.*;
import core.tasks.*;

/**
 * NOTES:
 * <p>
 * Mininum work unit workfroce record.
 * <p>
 * this class was designed to futher multiple parallel task where any instance is responsible for 1 view. IE: 1 for
 * workforce view, 1, for bu view etc.
 * 
 * @author terry
 * 
 */
public class AmountViewerTask implements TCallable<Hashtable<String, ServiceRequest>> {

	private Vector<Record> workForce;
	private Hashtable<String, Record> wofoNode;
	private Hashtable<String, Record> accoNode;
	private Hashtable<String, Record> bu__Node;
	private Future future;
	private String companyId, scenarioId;
	private JComponent waitComponent;

	// this elements are create onec and keep update during the live process of the aplication.
	private Record rmodel;
	private Vector<Record> workforceFinalList, buFinalList, accountFinalList;
	private Hashtable<String, ServiceRequest> services;

	public AmountViewerTask(String c, String s, JComponent jc) {
		this.companyId = c;
		this.scenarioId = s;
		this.waitComponent = jc;

		this.workforceFinalList = new Vector<Record>();
		this.accountFinalList = new Vector<Record>();
		this.buFinalList = new Vector<Record>();

		wofoNode = new Hashtable<String, Record>();
		accoNode = new Hashtable<String, Record>();
		bu__Node = new Hashtable<String, Record>();

		String wc = "SCENARIO_ID = " + scenarioId + " AND COMPANY_ID = '" + companyId + "'";
		this.workForce = ConnectionManager.getAccessTo("SLE_PLANC_WORKFORCE").search(wc, null);

		// record model
		Field te[] = new Field[]{new Field("av_name", "", 30)};
		this.rmodel = new Record("", te);

		// slots range from scenario
		Record scer = ConnectionManager.getAccessTo("SLE_SCENARIO").exist("id = " + scenarioId);
		int si = SLEPlanC.getSlot((Date) scer.getFieldValue("START_SLOT"));
		int se = si + (Integer) scer.getFieldValue("periods");

		// insert month slot intro record model.
		// WARNING: mount slot MUST star form column 2. it's required from AmountViewTreeTableModel. future changes in
		// this recordmodel affect AmountViewTreeTableModel
		for (int i = si; i < se; i++) {
			// Record r = wrkList.elementAt(i);
			Field f = new Field("" + i, new Double(0.0), 15);
			rmodel.addNewField(f);
		}
		// add the rest of field
		rmodel.addNewField(new Field("av_total", 0.0, 30));
		rmodel.addNewField(new Field("av_src_file", "", 30));
		rmodel.addNewField(new Field("av_path", "", 100));
		rmodel.addNewField(new Field("av_formula_eval", "", 2048));
		rmodel.addNewField(new Field("av_formula_expr", "", 2048));
		rmodel.addNewField(new Field("av_pattern", "", 60));
		rmodel.addNewField(new Field("av_srcrecord", "", 60));
		rmodel.getField("av_path").iskey = true; // for debug purpose

		// root node (company)
		Record cRcd = ConnectionManager.getAccessTo("SLE_COMPANY").exist("id = '" + companyId + "'");
		Record r = new Record(rmodel);
		r.setFieldValue("av_path", companyId);
		r.setFieldValue("av_pattern", "$id: $name");
		r.setFieldValue("av_name", companyId + ": " + cRcd.getFieldValue("name"));
		r.setFieldValue("av_src_file", "sle_company");
		r.setFieldValue("av_srcrecord", cRcd);
		workforceFinalList.add(r);
		accountFinalList.add(r);
		buFinalList.add(r);

		// scenario node
		r = new Record(rmodel);
		r.setFieldValue("av_path", companyId + "/" + scenarioId);
		r.setFieldValue("av_pattern", "$id: $name");
		r.setFieldValue("av_name", scenarioId + ": " + scer.getFieldValue("name"));
		r.setFieldValue("av_src_file", "sle_scenario");
		r.setFieldValue("av_srcrecord", scer);

		workforceFinalList.add(r);
		accountFinalList.add(r);
		buFinalList.add(r);

		// build fieldsDescriptions hashtable
		Hashtable<String, String> fldsd = new Hashtable<String, String>();
		for (int col = 0; col < rmodel.getFieldCount(); col++) {
			String fn = rmodel.getFieldName(col);
			// if fn is a number, look for slot, else, look for bundled string
			try {
				Integer.parseInt(fn);
				fldsd.put(fn, SLEPlanC.getSlotString(fn));

			} catch (Exception e) {
				fldsd.put(fn, TStringUtils.getBundleString(fn));
			}
		}

		this.services = new Hashtable<String, ServiceRequest>(5);
		ServiceRequest sr = new ServiceRequest(ServiceRequest.CLIENT_GENERATED_LIST, getClass().getName(),
				workforceFinalList);
		sr.setParameter(ServiceResponse.RECORD_MODEL, rmodel);
		sr.setParameter(ServiceResponse.RECORD_FIELDS_DESPRIPTION, fldsd);
		services.put(WorkforceView.class.getSimpleName(), sr);
		sr = new ServiceRequest(ServiceRequest.CLIENT_GENERATED_LIST, getClass().getName(), accountFinalList);
		sr.setParameter(ServiceResponse.RECORD_MODEL, rmodel);
		sr.setParameter(ServiceResponse.RECORD_FIELDS_DESPRIPTION, fldsd);
		services.put(AccountView.class.getSimpleName(), sr);
		sr = new ServiceRequest(ServiceRequest.CLIENT_GENERATED_LIST, getClass().getName(), buFinalList);
		sr.setParameter(ServiceResponse.RECORD_MODEL, rmodel);
		sr.setParameter(ServiceResponse.RECORD_FIELDS_DESPRIPTION, fldsd);
		services.put(BUView.class.getSimpleName(), sr);
	}

	@Override
	public void setFuture(Future<Hashtable<String, ServiceRequest>> f, boolean ab) {
		this.future = f;
	}

	@Override
	public Hashtable<String, ServiceRequest> call() throws Exception {
		try {
			// process 1 worker at time
			for (int cnt = 0; cnt < workForce.size(); cnt++) {
				if (future.isCancelled()) {
					break;
				}
				Record workRecord = workForce.elementAt(cnt);
				processRecord(workRecord);
				int per = cnt * 100 / workForce.size();
				waitComponent.setName("" + per);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return services;
	}

	/**
	 * add 1 workforce individual to the final list.
	 * 
	 * @param workRecord
	 */
	private void processRecord(Record workRecord) {
		// working list
		String wc = "SCENARIO_ID = " + scenarioId + " AND COMPANY_ID = '" + companyId + "' AND WORKRELATION_ID = '"
				+ workRecord.getFieldValue("workrelation_id") + "'";
		String ob = "company_id, scenario_id, bu_id, account_id, workrelation_id";
		Vector<Record> amountlist = ConnectionManager.getAccessTo("sle_planc_amount").search(wc, ob);
		DBAccess budba = ConnectionManager.getAccessTo("sle_planc_bu");
		DBAccess accdba = ConnectionManager.getAccessTo("sle_planc_account");

		Record wofrcd = null;
		for (Record amountrcd : amountlist) {
			// 1821: process the ammount records if account type_id = 0
			Record air = accdba.exist("ID = " + amountrcd.getFieldValue("account_id"));
			// System.out.println(air.getFieldValue("type_id"));
			if (((Long) air.getFieldValue("type_id")) != 0L) {
				continue;
			}
			// index for internal references
			String bu__n = companyId + "/" + scenarioId + "/" + amountrcd.getFieldValue("bu_id");
			String bu__nwf = companyId + "/" + scenarioId + "/" + amountrcd.getFieldValue("bu_id") + "/"
					+ amountrcd.getFieldValue("workrelation_id");
			String accon = companyId + "/" + scenarioId + "/" + amountrcd.getFieldValue("account_id");
			String acconwf = companyId + "/" + scenarioId + "/" + amountrcd.getFieldValue("account_id") + "/"
					+ amountrcd.getFieldValue("workrelation_id");
			String wofon = companyId + "/" + scenarioId + "/" + amountrcd.getFieldValue("workrelation_id");
			String monthslot = amountrcd.getFieldValue("month_slot").toString();

			// workforce view
			wofrcd = wofoNode.get(wofon);
			if (wofrcd == null) {
				wofrcd = getNewWorkForceRecord(workRecord, wofon, amountrcd);
				wofoNode.put(wofon, wofrcd);
				workforceFinalList.add(wofrcd);
			}
			sumMonthSlot(monthslot, amountrcd, wofrcd);
			sumMonthSlot(monthslot, amountrcd, workforceFinalList.elementAt(0));
			sumMonthSlot(monthslot, amountrcd, workforceFinalList.elementAt(1));

			// account view
			Record accrcd = accoNode.get(accon);
			if (accrcd == null) {
				Record r = accdba.exist("id = " + amountrcd.getFieldValue("account_id"));
				String patt = "$id: $name";
				accrcd = new Record(rmodel);
				accrcd.setFieldValue("av_pattern", patt);
				accrcd.setFieldValue("av_path", accon);
				accrcd.setFieldValue("av_src_file", "sle_planc_account");
				accrcd.setFieldValue("av_name", TStringUtils.format(patt, r));
				accrcd.setFieldValue("av_srcrecord", r);
				accoNode.put(accon, accrcd);
				accountFinalList.add(accrcd);
			}
			sumMonthSlot(monthslot, amountrcd, accrcd);

			// asociated wf record for account view
			Record accrcdwf = accoNode.get(acconwf);
			if (accrcdwf == null) {
				accrcdwf = getNewWorkForceRecord(workRecord, acconwf, amountrcd);
				accoNode.put(acconwf, accrcdwf);
				accountFinalList.add(accrcdwf);
			}
			sumMonthSlot(monthslot, amountrcd, accrcdwf);

			// bussines unit view
			Record burcd = bu__Node.get(bu__n);
			if (burcd == null) {
				budba.ignoreSecurity();
				Record r = budba.exist("id = '" + amountrcd.getFieldValue("bu_id") + "'");
				String patt = "$id: $name";
				burcd = new Record(rmodel);
				burcd.setFieldValue("av_pattern", patt);
				burcd.setFieldValue("av_path", bu__n);
				burcd.setFieldValue("av_src_file", "sle_planc_bu");
				burcd.setFieldValue("av_name", TStringUtils.format(patt, r));
				burcd.setFieldValue("av_srcrecord", r);
				bu__Node.put(bu__n, burcd);
				buFinalList.add(burcd);
			}
			sumMonthSlot(monthslot, amountrcd, burcd);

			// asociated wf record for bussines unit view
			Record burcdwf = bu__Node.get(bu__nwf);
			if (burcdwf == null) {
				burcdwf = getNewWorkForceRecord(workRecord, bu__nwf, amountrcd);
				bu__Node.put(bu__nwf, burcdwf);
				buFinalList.add(burcdwf);
			}
			sumMonthSlot(monthslot, amountrcd, burcdwf);
		}
	}

	/**
	 * Add the amount field value found in <code>amr</code> to the selected mounth slot <code>ms</code> in target
	 * record. moreover increment the total field value to math the entire row.
	 * 
	 * @param ms - mounth slot to add
	 * @param amr - amount record from retrive amount value
	 * @param tar - target record to update
	 */
	private void sumMonthSlot(String ms, Record amr, Record tar) {
		double nd = (Double) (Double) amr.getFieldValue("amount");
		double td = (Double) tar.getFieldValue(ms);
		double tot = (Double) tar.getFieldValue("av_total");
		tar.setFieldValue(ms, new Double(td + nd));
		tar.setFieldValue("av_total", new Double(tot + nd));
	}

	/**
	 * create new record based on {@link #rmodel} setted as workforce record
	 * 
	 * @param src - source record to build the name field using the format method
	 * @param path - value for path field
	 * @param amounr - amount record for other parameters
	 * 
	 * @return record
	 */
	private Record getNewWorkForceRecord(Record src, String path, Record amounr) {
		String patt = "$workrelation_id: $name";
		Record wofrcd = new Record(rmodel);
		wofrcd.setFieldValue("av_pattern", patt);
		wofrcd.setFieldValue("av_path", path);
		wofrcd.setFieldValue("av_src_file", "sle_planc_workforce");
		wofrcd.setFieldValue("av_name", TStringUtils.format(patt, src));
		wofrcd.setFieldValue("av_formula_eval", amounr.getFieldValue("formula_eval"));
		wofrcd.setFieldValue("av_formula_expr", amounr.getFieldValue("formula_expr"));
		wofrcd.setFieldValue("av_srcrecord", src);
		return wofrcd;
	}
}
