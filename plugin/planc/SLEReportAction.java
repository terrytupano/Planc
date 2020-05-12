package plugin.planc;

import java.awt.event.*;
import java.util.*;

import core.reporting.*;

/**
 * this action represent a planc report.
 * 
 */
public class SLEReportAction extends TAbstractPrintAction {

	public SLEReportAction(String rn, String tex) {
		super(rn, tex);
		setIcon("printer2");
		reportInstance = SLEReport.class;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// check if there are a report class in plugin.reporting
		try {
			reportInstance = Class.forName("plugin.planc.reporting."+reportName);
		} catch (ClassNotFoundException e) {
			// no exist?? do nothing
		}
		
		// rebuild the parameters ui to update for posible data changes.
		Vector<String> jrp = SLEReport.getJasperParameters(reportName);
		defaultDataInput = new ReportParameters(reportName);
		SLEReportParameters srp = new SLEReportParameters(defaultDataInput, jrp);
		defaultDataInput.insert(srp, "Parametros");
		// invoque validate to check report parameter consistency
		srp.validateFields();
		displayPrintDialog();
	}
}