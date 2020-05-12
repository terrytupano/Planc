package plugin.planc;

import gui.*;

import java.util.*;

import javax.swing.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;
import core.reporting.*;

/**
 * Parameter list for planc legacy reports
 * 
 */
public class SLEReportParameters extends AbstractDataInput {

	private Vector<String> jasperParameters;
	private AplicationException badparameters = null;
	private ServiceRequest costCenterSR;
	private RecordSelector companyRS, costCenterRS;

	public SLEReportParameters(ReportParameters drp, Vector<String> jrp) {
		super(null);
		this.jasperParameters = jrp;

		// bussines unit
		ServiceRequest sr = new ServiceRequest(ServiceRequest.DB_QUERY, "SLE_PLANC_BU", null);
		RecordSelector rs = new RecordSelector(sr, "id", "name", drp.getSavedParameter("print.businessID", null));
		rs.insertItemAt(TStringUtils.getTEntry("tentry.all"), 0);
		addInputComponent("print.businessID", rs, false, false);

		// company
		sr = new ServiceRequest(ServiceRequest.DB_QUERY, "sle_company", null);
		companyRS = new RecordSelector(sr, "id", "name", drp.getSavedParameter("print.companyID", null));
		companyRS.addActionListener(this);
		TEntry cs = (TEntry) rs.getSelectedItem();
		addInputComponent("print.companyID", companyRS, false, false);

		// scenarios
		sr = new ServiceRequest(ServiceRequest.DB_QUERY, "SLE_SCENARIO", null);
		rs = new RecordSelector(sr, "id", "name", drp.getSavedParameter("print.scenarioID", null));
		addInputComponent("print.scenarioID", rs, false, false);

		// workforce id
		String wi = (String) drp.getSavedParameter("print.workforceID", "");
		addInputComponent("print.workforceID", TUIUtils.getJTextField("", wi, 10), false, false);

		// currency
		sr = new ServiceRequest(ServiceRequest.DB_QUERY, "SLE_CURRENCY", null);
		rs = new RecordSelector(sr, "id", "name", drp.getSavedParameter("print.monedaID", null));
		addInputComponent("print.monedaID", rs, false, false);

		// account
		sr = new ServiceRequest(ServiceRequest.DB_QUERY, "SLE_PLANC_ACCOUNT", null);
		rs = new RecordSelector(sr, "id", "name", drp.getSavedParameter("print.accountID", null));
		rs.insertItemAt(TStringUtils.getTEntry("tentry.all"), 0);
		addInputComponent("print.accountID", rs, false, false);

		// category value
		sr = new ServiceRequest(ServiceRequest.DB_QUERY, "SLE_CATEGORY_VALUE", null);
		rs = new RecordSelector(sr, "id", "code", drp.getSavedParameter("print.catValueID", null));
		rs.insertItemAt(TStringUtils.getTEntry("tentry.all"), 0);
		addInputComponent("print.catValueID", rs, false, false);

		// category
		sr = new ServiceRequest(ServiceRequest.DB_QUERY, "SLE_CATEGORY", null);
		rs = new RecordSelector(sr, "id", "name", drp.getSavedParameter("print.categoryID", null));
		rs.insertItemAt(TStringUtils.getTEntry("tentry.all"), 0);
		addInputComponent("print.categoryID", rs, false, false);

		// cost center
		String wc = "COMPANY_ID = '" + cs.getKey() + "'";
		this.costCenterSR = new ServiceRequest(ServiceRequest.DB_QUERY, "sle_cost_centers", wc);
		costCenterRS = new RecordSelector(sr, "id", "name", drp.getSavedParameter("print.costCenterID", null));
		costCenterRS.insertItemAt(TStringUtils.getTEntry("tentry.all"), 0);
		addInputComponent("print.costCenterID", costCenterRS, false, false);

		String row = "";
		for (int i = 0; i < jasperParameters.size(); i++) {
			row += "pref, 3dlu, ";
		}
		row = row.substring(0, row.length() - 2);
		FormLayout lay = new FormLayout("left:pref, 3dlu, 200dlu", // columns
				row); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		int rc = 1;
		String dta = "";
		for (String str : jasperParameters) {
			// spetial case for userID. the user id is allways inserted at setReportParameters in SLEReport method
			if (str.equals("userID")) {
				continue;
			}
			String fn = "print." + str;
			JLabel jl = getLabelFor(fn);
			if (jl != null) {
				build.add(getLabelFor(fn), cc.xy(1, rc));
				build.add(getInputComponent(fn), cc.xy(3, rc));
				// enabled the component marking it as available for user and for me, to identify which one are using
				setEnabledInputComponent(fn, true);
				rc += 2;
			} else {
				dta += fn + " ";
				badparameters = new AplicationException("sle.ui.msg24", dta);
			}
		}
		add(build.getPanel());

		// dont prevalidate. invoke validatefields from action
		// preValidate(null);
	}

	@Override
	public void validateFields() {
		// invoked form defaultReportParameters
		// if error in parameters, show critical error and dont allow user to continue
		if (badparameters != null) {
			showAplicationException(badparameters);
			setEnableDefaultButton(false);
			return;
		}
		// cost center (if available)
		if (costCenterRS.isEnabled()) {
			TEntry te = (TEntry) companyRS.getSelectedItem();
			String wc = "COMPANY_ID = '" + te.getKey() + "'";
			costCenterSR.setData(wc);
			costCenterRS.reLoadRecords();
			costCenterRS.insertItemAt(TStringUtils.getTEntry("tentry.all"), 0);
		}
	}
}
