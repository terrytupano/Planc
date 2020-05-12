package plugin.planc;

import java.io.*;
import java.util.*;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.*;
import net.sf.jasperreports.engine.export.ooxml.*;
import net.sf.jasperreports.engine.util.*;

import org.apache.commons.csv.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import core.*;
import core.datasource.*;
import core.reporting.*;

public class SLEReport extends Report {

	public SLEReport(Hashtable rp) {
		super(rp);
	}

	@Override
	public void setReportParameters() {
		super.setReportParameters();
		Object o = reportParameters.get("print.businessID");
		o = o.equals("*all") ? null : o;
		jasperParameters.put("businessID", o);

		o = reportParameters.get("print.companyID");
		o = o.equals("*all") ? null : o;
		jasperParameters.put("companyID", o);

		o = reportParameters.get("print.scenarioID");
		o = o.equals("*all") ? null : ((Long) o).intValue();
		jasperParameters.put("scenarioID", o);

		o = reportParameters.get("print.workforceID");
		o = o.equals("") ? null : o;
		jasperParameters.put("workforceID", o);

		o = reportParameters.get("print.monedaID");
		jasperParameters.put("monedaID", o);

		o = reportParameters.get("print.accountID");
		o = o.equals("*all") ? null : ((Long) o).intValue();
		jasperParameters.put("accountID", o);

		jasperParameters.put("userID", Session.getUserName());

		o = reportParameters.get("print.catValueID");
		o = o.equals("*all") ? null : ((Long) o).intValue();
		jasperParameters.put("catValueID", o);

		o = reportParameters.get("print.categoryID");
		o = o.equals("*all") ? null : ((Long) o).intValue();
		jasperParameters.put("categoryID", o);

		o = reportParameters.get("print.costCenterID");
		o = o.equals("*all") ? null : o;
		jasperParameters.put("costCenterID", o);
	}

	@Override
	protected void fillReport() throws Exception {
		jasperPrint = JasperFillManager.fillReport(report, jasperParameters,
				ConnectionManager.getConnection("sle_planc_amount"));
	}

	@SuppressWarnings("deprecation")
	@Override
	public void printReport(JasperPrint prt) throws Exception {
		String rn = (String) reportParameters.get(ReportParameters.REPORT_NAME);
		String ff = (String) reportParameters.get(ReportParameters.FILE_FORMAT);

		if (reportParameters.get(ReportParameters.OUT_TYPE).equals(ReportParameters.EXPORT)
				&& (ff.equals(".xls") || ff.equals(".xlso"))) {
			// specia treatment for:
			if (rn.equals("AccountingInterfaceJDE")) {
				String ef = (String) reportParameters.get(ReportParameters.FILE_NAME);
				File tf = Report.temporalExport(prt, ef);
				interfaceJDE(tf, ef);
				return;
			}

			// original code
			// JRXlsxExporter exporter = new JRXlsxExporter();
			// exporter.setParameter(JRExporterParameter.JASPER_PRINT, prt);
			// exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, exportFileName);
			// exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
			// if (ff.equals(".xlso")) {
			// exporter.setParameter(JRXlsAbstractExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.TRUE);
			// }
			// exporter.exportReport();

			// 1822: copy code from carlos mejias:
			// --------------
			JRXlsxExporter exporter = new JRXlsxExporter();
			exporter.setParameter(JRExporterParameter.JASPER_PRINT, prt);
			exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, exportFileName);
			exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
			exporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
			exporter.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, Boolean.TRUE);
			if (ff.equals(".xlso")) {
				exporter.setParameter(JRXlsAbstractExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.TRUE);
			}
			exporter.exportReport();
			// --------------
		} else {
			super.printReport(prt);
		}
	}

	/**
	 * special ttreatment for InterfaceJDE report. read a CSV file and create the excel vertion of it.
	 * 
	 * @param tf - temporal file in csv format
	 * @param ef - destination excel file
	 */
	private static void interfaceJDE(File tf, String ef) {
		try {
			Reader in = new FileReader(tf); // Reader
			CSVParser par = new CSVParser(in, CSVFormat.EXCEL);
			Iterable<CSVRecord> inrlst = par.getRecords();

			// create excel
			Workbook wb = ef.endsWith(".xls") ? new HSSFWorkbook() : new XSSFWorkbook();
			Sheet sheet = wb.createSheet("InterfaceJDE");
			int rowcnt = 0, colcnt = 1;
			String acol = null, bcol = null;
			for (CSVRecord csvr : inrlst) {
				Row row = sheet.createRow(rowcnt++);
				for (int c = 0; c < csvr.size(); c++) {
					String sval = csvr.get(c);
					colcnt = colcnt < c ? c : colcnt;

					// values for a and b cols are last value diferent of ""
					acol = (c == 0 && !sval.equals("")) ? sval : acol;
					bcol = (c == 1 && !sval.equals("")) ? sval : bcol;
					if (c == 0)
						sval = new String(acol);
					if (c == 1)
						sval = new String(bcol);

					Cell cell = row.createCell(c);
					cell.setCellValue(sval);
					if (c > 3) {
						sval = sval.replaceAll("[.]", "");
						sval = sval.replaceAll("[,]", ".");
						try {
							double d = Double.parseDouble(sval);
							CellStyle style = wb.createCellStyle();
							style.setAlignment(HorizontalAlignment.RIGHT);
							style.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));
							cell.setCellStyle(style);
							cell.setCellValue(d);
						} catch (Exception e) {

						}
					}
				}
			}

			// column size
			for (int co = 0; co < colcnt + 1; co++) {
				sheet.autoSizeColumn(co);
			}

			// Write the output to a file
			FileOutputStream out = new FileOutputStream(ef);
			wb.write(out);
			out.close();
			par.close();
			wb.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * extrat the parameters from .jasper file. the parameters returned are all requiered for report generation and are
	 * needed by user. this parameters are mark with "ID" at the end of parameter name
	 * 
	 * @param fn - file name of .jasper file
	 * @return parameters required by user
	 */
	public static Vector<String> getJasperParameters(String fn) {
		try {
			Vector<String> v = new Vector<String>();
			JasperReport jr = (JasperReport) JRLoader.loadObject(new FileInputStream(TResourceUtils.getFile(fn
					+ ".jasper")));
			JRParameter[] jrps = jr.getParameters();
			for (JRParameter jrp : jrps) {
				String pn = jrp.getName();
				if (pn.endsWith("ID")) {
					v.add(pn);
				}
			}
			return v;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
