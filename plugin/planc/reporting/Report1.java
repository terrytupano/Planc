package plugin.planc.reporting;

import gui.*;

import java.util.*;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.*;
import plugin.planc.*;
import core.*;
import core.datasource.*;
import core.reporting.*;

public class Report1 extends SLEReport {

	private Record rcdModel;
	private String companyID, costCenterID;
	private int scenarioID;
	private Hashtable<Date, Integer> date_slot_index;

	public Report1(Hashtable rp) {
		super(rp);
	}
	
	@Override
	public void setReportParameters() {
		super.setReportParameters();
		
		// example: adicional parameters for this report
		jasperParameters.put("report.logo", TResourceUtils.getFile("images/appicon.gif").getAbsolutePath());

	}

	@Override
	protected void fillReport() throws Exception {
		// retrive parameters
		companyID = (String) jasperParameters.get("companyID");
		scenarioID = (Integer) jasperParameters.get("scenarioID");
		costCenterID = (String) jasperParameters.get("costCenterID");

		// retrive the record model form SLE_VIEW_PLANC_ACCOUNTING_DIST. A record model is a database table row with all
		// values empty. in this case, we will use this object to represent a row inside of jasper report. in this way,
		// some fields come form database table an other we need to create it
		rcdModel = ConnectionManager.getAccessTo("SLE_VIEW_PLANC_ACCOUNTING_DIST").getModel();

		// add missing fields. field cames from jasper report design
		rcdModel.addNewField(new Field("account_plans_name", "", 30));
		rcdModel.addNewField(new Field("account_plans_type", "", 30));
		rcdModel.addNewField(new Field("cost_center_name", "", 30));
		rcdModel.addNewField(new Field("amount_total", new Double(0.0), 10));

		// build where clause. Mandatory fields
		String wc = "SCENARIO_ID = " + scenarioID + " AND COMPANY_ID = '" + companyID + "'";

		// alter where clause to retrive only few records (for debug purporse)
		wc += " and bu_id ='NOM891117'";

		// optional fiels (when user select it)
		if (costCenterID != null) {
			wc += " AND COST_CENTER = '" + costCenterID + "'";
		}

		// order to get the records in order
		String ob = "company_id, scenario_id, ledger_account, cost_center, date_slot";

		// retrive data
		Vector<Record> srclist = ConnectionManager.getAccessTo("SLE_VIEW_PLANC_ACCOUNTING_DIST").search(wc, ob);
		Vector<Record> finallist = new Vector<Record>();
		if (srclist.size() > 0) {
			buildSlot(srclist.elementAt(0), rcdModel);
		}

		// account and cost center. used to determinate whether sum amount slot or build a new record
		String key = "";
		Record rcd = null;

		for (Record srcRcd : srclist) {
			String sk = srcRcd.getFieldValue("LEDGER_ACCOUNT") + ";" + srcRcd.getFieldValue("COST_CENTER");
			if (sk.equals(key)) {
				sumSlot(rcd, srcRcd);
			} else {
				key = sk;
				rcd = buildFinalRecord(srcRcd);
				finallist.add(rcd);
				sumSlot(rcd, srcRcd);
			}
		}

		// --------------- finals steps: not much modifications is required here ---------------
		// build a client generated service request that contain the generated data 
		ServiceRequest sr = new ServiceRequest(ServiceRequest.CLIENT_GENERATED_LIST, "", finallist);
		sr.setParameter(ServiceResponse.RECORD_MODEL, rcdModel);

		// build the tablemodel using the framework default implementation
		TAbstractTableModel tm = new TAbstractTableModel(sr);

		// the final result of this methot is fill the report and update the jasperPrint object
		JRTableModelDataSource ds = new JRTableModelDataSource(tm);
		jasperPrint = JasperFillManager.fillReport(report, jasperParameters, ds);
	}

	@Override
	// TODO: temporal while we are in building process. remove after that. this method does'n need override in this case
	public void printReport(JasperPrint prt) throws Exception {
		Report.viewJasperPrint(prt);
	}

	/**
	 * sum the amount field from the original record (source record) to the final record (the record that will be
	 * appended to the final list that feed the report
	 * 
	 * @param finalRcd
	 * @param srcRcd
	 */
	private void sumSlot(Record finalRcd, Record srcRcd) {
		Date d = (Date) srcRcd.getFieldValue("DATE_SLOT");
		int idx = date_slot_index.get(d);

		// TODO: table field has no presicion. Java cast it as integer.
		Integer sd = (Integer) srcRcd.getFieldValue("AMOUNT");

		Double as = (Double) finalRcd.getFieldValue("amount_slot" + idx);
		Double tot = (Double) finalRcd.getFieldValue("amount_total");
		finalRcd.setFieldValue("amount_slot" + idx, sd + as);
		finalRcd.setFieldValue("amount_total", tot + sd);
	}

	/**
	 * Final record is the recort that will be added to the final vector which is the source of data for the report.
	 * this object contains all fields declared on jasper report. in this case due to report nature, this method only
	 * fill some fields. the rest of field are filled in {@link #sumSlot(Record, Record)} method
	 * 
	 * @param srcRcd - source record (record from
	 * @return
	 */
	private Record buildFinalRecord(Record srcRcd) {
		Record rcd = new Record(rcdModel);
		Record.copyFields(srcRcd, rcd);

		// account name & type
		Record trcd = ConnectionManager.getAccessTo("SLE_ACCOUNTING_PLANS").exist(
				"COMPANY_ID = '" + companyID + "' AND ID = '" + srcRcd.getFieldValue("ledger_account") + "'");
		String accnam = "** Account not Found";
		String acctip = "** Account not Found";
		if (trcd != null) {
			accnam = (String) trcd.getFieldValue("NAME");
			acctip = (String) trcd.getFieldValue("ACCOUNT_TYPE");
		}
		rcd.setFieldValue("account_plans_name", accnam);
		rcd.setFieldValue("account_plans_type", acctip);

		// cost center
		trcd = ConnectionManager.getAccessTo("SLE_COST_CENTERS").exist(
				"COMPANY_ID = '" + companyID + "' AND ID = '" + srcRcd.getFieldValue("cost_center") + "'");
		String ccenam = "** CoCe not Found";
		if (trcd != null) {
			ccenam = (String) trcd.getFieldValue("NAME");
		}
		rcd.setFieldValue("cost_center_name", ccenam);

		return rcd;
	}

	/**
	 * called at firt time to build the slot_date# parameters and ammount_slot# variables inside of record object.
	 * <p>
	 * this method take a sample from the original parameters to detect the slot range
	 * 
	 * @param samp
	 * @param rcdModel
	 */
	private void buildSlot(Record samp, Record rcdModel) {

		// a map form data value to slot_date index inside the record object
		date_slot_index = new Hashtable<Date, Integer>();

		// take sample
		String wc = "SCENARIO_ID = '" + samp.getFieldValue("SCENARIO_ID") + "' AND COMPANY_ID = '"
				+ samp.getFieldValue("COMPANY_ID") + "' AND WORKRELATION_ID = '"
				+ samp.getFieldValue("WORKRELATION_ID") + "' AND ACCOUNT_ID = " + samp.getFieldValue("ACCOUNT_ID");
		String ob = "company_id, scenario_id, workrelation_id, account_id, date_slot";
		Vector<Record> list = ConnectionManager.getAccessTo("SLE_VIEW_PLANC_ACCOUNTING_DIST").search(wc, ob);

		// complete report parameter, add field and build index map
		for (int i = 0; i < list.size(); i++) {
			Date d = (Date) list.elementAt(i).getFieldValue("DATE_SLOT");
			rcdModel.addNewField(new Field("amount_slot" + i, new Double(0.0), 10));
			jasperParameters.put("date_slot" + i, d);
			date_slot_index.put(d, i);
		}
	}
}
