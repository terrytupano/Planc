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
package plugin.planc.security;

import java.util.*;

import core.datasource.*;

/**
 * this class search al data found in <code>sle_options</code>, append the autorized field and set true or false if a
 * record is found in <code>sle_role_options</code>
 * 
 */
public class OptionsTransaction extends AbstractTransaction {

	private Record rModel;
	private String roleId;
	private Vector<Record> rList;

	@Override
	public ServiceResponse commit() {

		// selected parameters form UI
		this.roleId = ((String) request.getData()).toString();
		ServiceResponse resp = new ServiceResponse(null);

		DBAccess tmpdba = ConnectionManager.getAccessTo("sle_options");
		this.rModel = tmpdba.getModel();
		rModel.addNewField(new Field("autorized", false, 1));
		rModel.addNewField(new Field("isLeaf", false, 1));

		tmpdba.ignoreSecurity();
		this.rList = tmpdba.search("SYSTEM_ID = 'SLEPLANC'", null);
		tmpdba = ConnectionManager.getAccessTo("sle_role_options");

		// append autorized field for tree view
		for (Record r1 : rList) {
			r1.addNewField(new Field("autorized", false, 1));
			r1.addNewField(new Field("isLeaf", false, 1));
			String oid = r1.getFieldValue("id").toString();
			tmpdba.ignoreSecurity();
			Record tmprcd = tmpdba.exist("role_id = '" + roleId + "' AND option_id = " + oid);
			r1.setFieldValue("autorized", (tmprcd != null));

			// overide module_id value to correct map module <-> iconname in treeview
			r1.setFieldValue("module_id", "module." + r1.getFieldValue("module_id") + "." + r1.getFieldValue("name"));
		}

		resp.setParameter(ServiceResponse.RECORD_MODEL, rModel);
		resp.setData(rList);
		return resp;
	}
}
