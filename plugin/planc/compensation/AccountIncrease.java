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
 * planc SleOracle.SLE_PLANC_ACCOUNT_INCREASE from compopensation module
 * 
 * change log:
 * <p>
 * 1812 - Incremento de cuentas ahora tiene acciones nueve, editar suprimir para ingreso de datos
 * 
 */
public class AccountIncrease extends UIListPanel implements DockingComponent {

	private ServiceRequest request;
	private Long accountId;
	private String workfId;
	private String companyId, scenaryId;

	public AccountIncrease() {
		super(null);
		this.request = new ServiceRequest(ServiceRequest.DB_QUERY, "SLE_PLANC_ACCOUNT_INCREASE", null);
		setToolBar(false, new NewRecord2(this), new EditRecord2(this), new DeleteRecord2(this));
		putClientProperty(TConstants.SHOW_COLUMNS, "start_increase;percentage;amount");
		putClientProperty(TConstants.ICON_PARAMETERS, "-1; ");
		putClientProperty(SLEPlanC.PLANC_ID, 70884L);
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		boolean newr = aa instanceof NewRecord2;
		Record rcd = newr ? getRecordModel() : getRecord();
		rcd.setFieldValue("scenario_id", scenaryId);
		rcd.setFieldValue("company_id", companyId);
		rcd.setFieldValue("workrelation_id", workfId);
		rcd.setFieldValue("account_id", accountId);
		return new AccountIncreaseRecord(rcd, newr);
	}

	@Override
	public void init() {
		setMessage("sle.ui.msg12");
		setFormattForColums(0, "MMM-yyy");
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object src = evt.getSource();
		Object prp = evt.getPropertyName();
		Object newval = evt.getNewValue();

		// path selected
		if (prp.equals(TConstants.PATH_SELECTED)) {
			companyId = null;
			scenaryId = null;
			workfId = null;
			accountId = null;
			if (PlanCSelector.isNodeSelected(PlanCSelector.SCENARIO)) {
				companyId = PlanCSelector.getNodeValue(PlanCSelector.COMPANY);
				scenaryId = PlanCSelector.getNodeValue(PlanCSelector.SCENARIO);
				workfId = "ALL";
			}
		}

		// account selected
		if (src instanceof AccountSelection) {
			Record acr = (Record) newval;
			accountId = (acr == null) ? null : (Long) acr.getFieldValue("id");

			// 1810: AccountIncrease only if with_increase=1
			if (accountId != null && acr.getFieldValue("with_increase").equals(0)) {
				accountId = null;
			}
		}

		// set the request
		if (companyId != null && scenaryId != null && workfId != null && accountId != null) {
			String wc = "scenario_id = " + scenaryId + " AND company_id = '" + companyId + "' AND workrelation_id = '"
					+ workfId + "' AND account_id = " + accountId;
			request.setData(wc);
			setServiceRequest(request);
		} else {
			setMessage("sle.ui.msg12");
		}
	}
}
