package plugin.planc.config;

import gui.*;
import gui.docking.*;

import java.beans.*;

import javax.swing.*;

import plugin.planc.*;
import action.*;
import core.*;
import core.datasource.*;

/**
 * planc SLE_PLANC_SALARY_INCREASE by workforce = ALL
 * 
 */
public class GeneralSalaryIncrease extends UIListPanel implements DockingComponent {

	private String companyID, scenaryId;

	public GeneralSalaryIncrease() {
		super(null);
		putClientProperty(TConstants.SHOW_COLUMNS, "start_increase;category_id;percentage;amount");
		putClientProperty(TConstants.ICON_PARAMETERS, "-1; ");
		DeleteRecord2 dr = new DeleteRecord2(this);
		setToolBar(false, new NewRecord2(this), new EditRecord2(this), dr);
		putClientProperty(SLEPlanC.PLANC_ID, 70342L);
	}

	@Override
	public void init() {
		setView(TABLE_VIEW);
		setMessage("sle.ui.msg26");
		setFormattForColums(0, "MMM-yyy");
		setReferenceColumn("category_id", SLEPlanC.getTEntryGroupFrom("SLE_CATEGORY", "id", "name", null));
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		boolean newr = aa instanceof NewRecord2;
		Record rcd = newr ? getRecordModel() : getRecord();
		rcd.setFieldValue("scenario_id", scenaryId);
		rcd.setFieldValue("company_id", companyID);
		rcd.setFieldValue("workrelation_id", "ALL");
		return new GeneralSalaryIncreaseRecord(rcd, newr);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// Object src = evt.getSource();
		Object prp = evt.getPropertyName();
		// Object newval = evt.getNewValue();

		// only respond to path selection
		if (prp.equals(TConstants.PATH_SELECTED)) {
			scenaryId = null;
			if (PlanCSelector.isNodeSelected(PlanCSelector.SCENARIO)) {
				companyID = PlanCSelector.getNodeValue(PlanCSelector.COMPANY);
				scenaryId = PlanCSelector.getNodeValue(PlanCSelector.SCENARIO);
			}

			if (scenaryId != null) {
				String wc = "scenario_id = " + scenaryId + " AND company_id = '" + companyID
						+ "' AND workrelation_id = 'ALL'";
				setServiceRequest(new ServiceRequest(ServiceRequest.DB_QUERY, "sle_planc_salary_increase", wc));
			} else {
				setMessage("sle.ui.msg26");
			}
		}
	}
}