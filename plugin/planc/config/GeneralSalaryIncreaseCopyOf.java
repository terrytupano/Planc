package plugin.planc.config;

import gui.*;
import gui.docking.*;

import java.awt.event.*;
import java.beans.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import plugin.planc.*;

import com.alee.utils.swing.*;

import core.*;
import core.datasource.*;

/**
 * planc SLE_PLANC_SALARY_INCREASE by workforce = ALL
 * 
 */
public class GeneralSalaryIncreaseCopyOf extends AbstractFileIncreaseSupport implements DockingComponent {

	private String companyID, scenaryId;
	private RecordSelector categorySelector;
	private WebDefaultCellEditor cellEditor;

	public GeneralSalaryIncreaseCopyOf() {
		super();
		putClientProperty(TConstants.SHOW_COLUMNS, "start_increase;category_id;percentage;amount");
		putClientProperty(TConstants.ICON_PARAMETERS, "-1; ");
		putClientProperty(TConstants.ALLOW_INPUT_FROM_CELL, false);
		putClientProperty(SLEPlanC.PLANC_ID, 70342L);
	}

	@Override
	public void init() {
		requestFrom = this.getClass().getName();
		setView(TABLE_VIEW);
		setMessage("sle.ui.msg26");
		setFormattForColums(0, "MMM-yyy");
		categorySelector = new RecordSelector(new ServiceRequest(ServiceRequest.DB_QUERY, "sle_category", null), "id",
				"name", null, false);
		categorySelector.insertItemAt(TStringUtils.getTEntry("tentry.int.none"), 0);
		categorySelector.setName("category_id");
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		return null;
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
				DBAccess dba = ConnectionManager.getAccessTo("sle_planc_salary_increase");
				Vector<Record> tlist = dba.search(wc, null);

				Record mod = dba.getModel();
				mod.setFieldValue("scenario_id", scenaryId);
				mod.setFieldValue("company_id", companyID);
				mod.setFieldValue("workrelation_id", "ALL");

				ServiceRequest se = getServicerRequestFromDate(tlist, scenaryId, mod, "start_increase");
				Vector<Record> srcl = (Vector) se.getData();
				dba = ConnectionManager.getAccessTo("sle_category");

				// convert category_id to tentry
				for (int i = 0; i < srcl.size(); i++) {
					Record r = srcl.elementAt(i);
					Record r1 = dba.exist("id = " + r.getFieldValue("category_id"));
					TEntry te = r1 == null ? TStringUtils.getTEntry("tentry.int.none") : new TEntry(
							r1.getFieldValue("id"), r1.getFieldValue("name"));
					r.setFieldValue("category_id", te);
				}
				setServiceRequest(se);

				// editor for category
				JTable jt = getJTable();
				TableColumn tc = jt.getColumnModel().getColumn(1);
				// fire record select on category editor
				categorySelector.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						firePropertyChange(TConstants.RECORD_SELECTED, null, getRecord());
					}
				});
				cellEditor = new WebDefaultCellEditor(categorySelector);
				cellEditor.addCellEditorListener(this);
				tc.setCellEditor(cellEditor);
			} else {
				setMessage("sle.ui.msg26");
			}
		}
	}
}