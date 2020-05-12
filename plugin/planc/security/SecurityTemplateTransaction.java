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

import core.*;
import core.datasource.*;



/**
 * for a record selected on {@link SecurityTemplateList} build autorization treeview searching in
 * <code>sleoracle.sle_roles</code> and append 3 record:
 * <ol>
 * <li>record for see option 
 * <li>
 * record for enable insert
 * <li>
 * record for enable update
 * <li>
 * record for enable delete
 * </ol>
 * 
 * note: method 
 */
public class SecurityTemplateTransaction extends AbstractTransaction {

	private Record rModel;
	private String templateId;
	private Vector<Record> rList;

	@Override
	public ServiceResponse commit() {

		// selected parameters form UI
		this.templateId = ((Long) request.getData()).toString();
		ServiceResponse resp = new ServiceResponse(null);

		DBAccess tmpdba = ConnectionManager.getAccessTo("sle_roles");
		this.rModel = tmpdba.getModel();
		rModel.addNewField(new Field("sub_node", "", 20));
		rModel.addNewField(new Field("field_name", "", 20));
		rModel.addNewField(new Field("autorized", false, 1));
		rModel.addNewField(new Field("isleaf", false, 1));

		tmpdba.ignoreSecurity();
		Vector <Record> tmplist = tmpdba.search(null, null);
		this.rList = new Vector<Record>();
		tmpdba = ConnectionManager.getAccessTo("sle_template_roles");

		for (Record r1 : tmplist) {
			String rid = r1.getFieldValue("id").toString();
			tmpdba.ignoreSecurity();
			Record tmprcd = tmpdba.exist("template_id = " + templateId + " AND role_id = '" + rid + "'");
			r1.addNewField(new Field("sub_node", "", 20));
			r1.addNewField(new Field("field_name", "", 20));
			r1.addNewField(new Field("autorized", false, 1));
			r1.addNewField(new Field("isleaf", false, 1));
			rList.add(r1);
			
			// new subnode for see option
			addNewRecord(r1, "enable_see", (tmprcd != null));
			
			// file true field value: 1 = true
			Integer tint = new Integer(1);
			// new subnode records for insert, uptade and delte fields values
			if (tmprcd != null) {
				addNewRecord(r1, "enable_insert", tmprcd.getFieldValue("enable_insert").equals(tint));
				addNewRecord(r1, "enable_update", tmprcd.getFieldValue("enable_update").equals(tint));
				addNewRecord(r1, "enable_delete", tmprcd.getFieldValue("enable_delete").equals(tint));
			} else {
				addNewRecord(r1, "enable_insert", false);
				addNewRecord(r1, "enable_update", false);
				addNewRecord(r1, "enable_delete", false);
			}
		}

		resp.setParameter(ServiceResponse.RECORD_MODEL, rModel);
		resp.setData(rList);
		return resp;
	}
	
	/** add new subnode record to list based on paraneteres
	 * 
	 * @param srcr - org record for retrive data
	 * @param fn - id of field name on .propertis file
	 * @param aut - boolean value for autorization field
	 */
	private void addNewRecord(Record srcr, String fn, boolean aut) {
		Record nr = new Record(rModel);
		nr.setFieldValue(0, srcr.getFieldValue(0));
		nr.setFieldValue(1, srcr.getFieldValue(1));
		nr.setFieldValue("name", TStringUtils.getBundleString(fn));
		nr.setFieldValue("sub_node", srcr.getFieldValue("id"));
		nr.setFieldValue("field_name", fn);
		nr.setFieldValue("autorized", aut);
		rList.add(nr);
	}
}
