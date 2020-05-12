/*******************************************************************************
 * Copyright (C) 2017 terry.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     terry - initial API and implementation
 ******************************************************************************/
package plugin.planc;

import gui.*;

import java.awt.event.*;
import java.beans.*;
import java.util.*;

import javax.swing.*;

import action.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;
import core.tasks.*;

public class StoreProcedureAction extends TAbstractAction implements PropertyChangeListener {

	private StoreProcedureParameters rdInput;
	private JDialog dialog;

	public StoreProcedureAction() {
		super(NO_SCOPE);
		putValue(SLEPlanC.PLANC_ID, 70880L);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		rdInput = new StoreProcedureParameters();
		rdInput.addPropertyChangeListener(TConstants.ACTION_PERFORMED, this);
		dialog = getDialog(rdInput, "action.StoreProcedureAction");
		dialog.setVisible(true);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		dialog.dispose();
		// cancel
		if (evt.getNewValue() instanceof DefaultCancelAction) {
			return;
		}
		// submith the task
		Hashtable<String, Object> fields = rdInput.getFields();
		StoreProcedureCallTask spct = new StoreProcedureCallTask(fields);
		TTaskManager.submitRunnable(spct, null);
	}

	class StoreProcedureParameters extends AbstractDataInput {

		private RecordSelector categorySelector, catValueSelector;

		StoreProcedureParameters() {
			super("StoreProcedureAction.title");

			String cmp = PlanCSelector.getNodeValue(PlanCSelector.COMPANY);
			String sce = PlanCSelector.getNodeValue(PlanCSelector.SCENARIO);

			// bussines unit
			ServiceRequest sr = new ServiceRequest(ServiceRequest.DB_QUERY, "SLE_PLANC_BU", null);
			RecordSelector rs = new RecordSelector(sr, "id", "name", null);
			rs.insertItemAt(TStringUtils.getTEntry("tentry.all"), 0);
			addInputComponent("storeprocedure.ibu", rs, false, true);

			// workforce
			sr = new ServiceRequest(ServiceRequest.DB_QUERY, "SLE_PLANC_WORKFORCE", "SCENARIO_ID = " + sce
					+ " AND COMPANY_ID = '" + cmp + "'");
			rs = new RecordSelector(sr, "WORKRELATION_ID", "name", null);
			rs.insertItemAt(TStringUtils.getTEntry("tentry.all"), 0);
			addInputComponent("storeprocedure.irelation", rs, false, true);

			// category
			sr = new ServiceRequest(ServiceRequest.DB_QUERY, "SLE_CATEGORY", null);
			categorySelector = new RecordSelector(sr, "id", "name", null, false);
			categorySelector.insertItemAt(TStringUtils.getTEntry("tentry.all"), 0);
			categorySelector.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					TEntry te = (TEntry) categorySelector.getSelectedItem();
					boolean ena = !te.getKey().equals("*all");
					if (ena) {
						String wc = "CATEGORY_ID = " + te.getKey();
						catValueSelector.getServiceRequest().setData(wc);
						catValueSelector.reLoadRecords();
					}
					setEnabledInputComponent("storeprocedure.ivaluecat", ena);
					preValidate(null);
				}
			});
			addInputComponent("storeprocedure.icolumnid", categorySelector, false, true);

			// category value
			sr = new ServiceRequest(ServiceRequest.DB_QUERY, "SLE_CATEGORY_VALUE", null);
			catValueSelector = new RecordSelector(sr, "id", "code", null, false);
			addInputComponent("storeprocedure.ivaluecat", catValueSelector, false, false);

			// account
			sr = new ServiceRequest(ServiceRequest.DB_QUERY, "SLE_PLANC_ACCOUNT", null);
			rs = new RecordSelector(sr, "id", "name", null);
			rs.insertItemAt(TStringUtils.getTEntry("tentry.all"), 0);
			addInputComponent("storeprocedure.iaccountid", rs, false, true);

			FormLayout lay = new FormLayout("left:pref, 3dlu, 200dlu", // columns
					"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p"); // rows
			CellConstraints cc = new CellConstraints();
			PanelBuilder build = new PanelBuilder(lay);

			build.add(getLabelFor("storeprocedure.ibu"), cc.xy(1, 1));
			build.add(getInputComponent("storeprocedure.ibu"), cc.xy(3, 1));
			build.add(getLabelFor("storeprocedure.irelation"), cc.xy(1, 3));
			build.add(getInputComponent("storeprocedure.irelation"), cc.xy(3, 3));
			build.add(getLabelFor("storeprocedure.icolumnid"), cc.xy(1, 5));
			build.add(getInputComponent("storeprocedure.icolumnid"), cc.xy(3, 5));
			build.add(getLabelFor("storeprocedure.ivaluecat"), cc.xy(1, 7));
			build.add(getInputComponent("storeprocedure.ivaluecat"), cc.xy(3, 7));
			build.add(getLabelFor("storeprocedure.iaccountid"), cc.xy(1, 9));
			build.add(getInputComponent("storeprocedure.iaccountid"), cc.xy(3, 9));

			setDefaultActionBar();
			add(build.getPanel());
			preValidate(null);
		}
		@Override
		public void validateFields() {

		}
	}
}
