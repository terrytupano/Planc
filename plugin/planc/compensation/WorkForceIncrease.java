package plugin.planc.compensation;

import gui.*;
import gui.docking.*;

import java.beans.*;

import javax.swing.*;

import plugin.planc.*;
import action.*;
import core.*;
import core.datasource.*;

/**
 * planc SleOracle.SLE_PLANC_ACCOUNT_INCREASE
 * 
 * change log:
 * <p>
 * 1811 - Incremento salarial: ahora tiene acciones nueve, editar suprimir para ingreso de datos
 */
public class WorkForceIncrease extends UIListPanel implements DockingComponent {

	private ServiceRequest request;
	private Record workfRcd;

	public WorkForceIncrease() {
		super(null);
		this.request = new ServiceRequest(ServiceRequest.DB_QUERY, "SLE_PLANC_SALARY_INCREASE", null);
		setToolBar(false, new NewRecord2(this), new EditRecord2(this), new DeleteRecord2(this));
		putClientProperty(TConstants.SHOW_COLUMNS, "start_increase;percentage;amount");
		putClientProperty(TConstants.ICON_PARAMETERS, "-1; ");
		putClientProperty(SLEPlanC.PLANC_ID, 70945L);
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		boolean newr = aa instanceof NewRecord2;
		Record rcd = newr ? getRecordModel() : getRecord();
		rcd.setFieldValue("scenario_id", workfRcd.getFieldValue("scenario_id"));
		rcd.setFieldValue("company_id", workfRcd.getFieldValue("company_id"));
		rcd.setFieldValue("workrelation_id", workfRcd.getFieldValue("workrelation_id"));
		return new AccountIncreaseRecord(rcd, newr);
	}

	@Override
	public void init() {
		setMessage("sle.ui.msg13");
		setFormattForColums(0, "MMM-yyy");
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object src = evt.getSource();
		Object prp = evt.getPropertyName();
		Object newval = evt.getNewValue();

		// path selected
		if (prp.equals(TConstants.PATH_SELECTED)) {
			workfRcd = null;
		}

		// workforce selected
		if ((src instanceof WorkforceList)) {
			workfRcd = null;
			if (evt.getNewValue() != null) {
				workfRcd = ((Record) newval);
			}
		}

		// set the request
		if (workfRcd != null) {
			String wc = "scenario_id = " + workfRcd.getFieldValue("scenario_id") + " AND company_id = '"
					+ workfRcd.getFieldValue("company_id") + "' AND workrelation_id = '"
					+ workfRcd.getFieldValue("workrelation_id") + "'";
			request.setData(wc);
			setServiceRequest(request);
		} else {
			setMessage("sle.ui.msg13");
		}
	}
}
