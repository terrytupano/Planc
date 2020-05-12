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
package plugin.planc.config;

import java.util.*;

import core.datasource.*;

public class AccountGenerationTransaction extends AbstractTransaction {

	private Record rModel;
	private String scenaryId;
	private Vector<Record> rList;

	@Override
	public ServiceResponse commit() {

		// selected parameters form UI
		this.scenaryId = ((String) request.getData()).toString();
		ServiceResponse resp = new ServiceResponse(null);

		DBAccess tmpdba = ConnectionManager.getAccessTo("sle_planc_account");
		this.rModel = tmpdba.getModel();
		Field hasGenAcc = new Field("hasGenAcc", false, 1);
		Field withinc = new Field("with_increase", 0, 1);
		rModel.addNewField(hasGenAcc);
		rModel.addNewField(withinc);

		tmpdba.ignoreSecurity();
		this.rList = tmpdba.search(null, null);
		tmpdba = ConnectionManager.getAccessTo("sle_planc_gen_account");

		// set the icon field
		for (Record r1 : rList) {
			r1.addNewField(hasGenAcc);
			r1.addNewField(withinc);
			String id = r1.getFieldValue("id").toString();
			// account has asociated generation parameters
			tmpdba.ignoreSecurity();
			Record tmprcd = tmpdba.exist("SCENARIO_ID = '" + scenaryId + "' AND ACCOUNT_ID = " + id);
			if (tmprcd != null) {
				r1.setFieldValue("hasGenAcc", true);
				r1.setFieldValue("with_increase", tmprcd.getFieldValue("with_increase"));
			}
		}

		resp.setParameter(ServiceResponse.RECORD_MODEL, rModel);
		resp.setData(rList);
		return resp;
	}
}
