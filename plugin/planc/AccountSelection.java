package plugin.planc;

import gui.*;
import gui.docking.*;

import java.beans.*;
import java.util.*;

import javax.swing.*;

import action.*;
import core.*;
import core.datasource.*;

/**
 * SLE_PLANC_ACCOUNT only for selection
 * 
 * 1809: debido a esta modificacion, esta clase ya no puede ser arbol
 */
public class AccountSelection extends UIListPanel implements DockingComponent {

	private String scenaryId;

	public AccountSelection() {
		super(null);
		putClientProperty(TConstants.SHOW_COLUMNS, "name;id");
		putClientProperty(TConstants.ICON_PARAMETERS, "0;AccountSelection_;with_increase");
		setToolBar(false, new FilterAction(this));
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {

		return null;
	}

	@Override
	public void init() {
		setMessage("sle.ui.msg29");
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// Object src = evt.getSource();
		Object prp = evt.getPropertyName();

		// path selected
		if (prp.equals(TConstants.PATH_SELECTED)) {
			scenaryId = null;
			if (PlanCSelector.isNodeSelected(PlanCSelector.SCENARIO)) {
				scenaryId = PlanCSelector.getNodeValue(PlanCSelector.SCENARIO);
			}

			// set the request
			if (scenaryId != null) {
				DBAccess dba = ConnectionManager.getAccessTo("sle_planc_account");
				// 1809: todas exepto cuentas de presuncion
				Vector<Record> rlist = dba.search("TYPE_ID !=  16", null);
				Record mod = dba.getModel();
				// 1809: append new field to mark account by with_increase field (icon name)
				mod.addNewField(new Field("with_increase", 0, 1));
				dba = ConnectionManager.getAccessTo("sle_planc_gen_account");
				for (Record r : rlist) {
					String wc = "SCENARIO_ID = " + scenaryId + " AND ACCOUNT_ID = " + r.getFieldValue("id");
					Record rcd = dba.exist(wc);
					Field wi = new Field("with_increase", rcd == null || rcd.getFieldValue("with_increase").equals(0)
							? 0
							: 1, 1);
					r.addNewField(wi);
				}
				ServiceRequest sr = new ServiceRequest(ServiceRequest.CLIENT_GENERATED_LIST, "", rlist);
				sr.setParameter(ServiceResponse.RECORD_MODEL, mod);
				setServiceRequest(sr);
			} else {
				setMessage("sle.ui.msg29");
			}
		}
	}
}
