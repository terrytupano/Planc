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
 * this transaction build data for build a node-subnode tree view acording selected user parameters. the user select the
 * user and the view (database file) and this transaccion build a tree view selectin all elements of associated master
 * file (file whit name an description) and append a boolean field autorizad which is set truo o false if a entry is
 * found y previos selected file (seleccted by user).
 * 
 * @author terry
 * 
 */
public class UserAutorizationsTransaction extends AbstractTransaction {

	private Record rmodel;
	private String tablename;
	private String userId;
	private Vector<Record> rList;

	@Override
	public ServiceResponse commit() {

		// selected parameters form UI
		this.userId = ((Long) request.getData()).toString();
		this.tablename = (String) request.getTableName();

		ServiceResponse resp = new ServiceResponse(null);

		// record model
		Field te[] = new Field[]{new Field("name", "", 30), new Field("node", "", 30), new Field("sub_node", "", 30),
				new Field("autorized", false, 1),new Field("isleaf", false, 1), new Field("src_file", "", 30)};
		this.rmodel = new Record("", te);

		// final data list
		this.rList = new Vector<Record>();

		DBAccess tmpdba = ConnectionManager.getAccessTo(tablename);
		Vector<Record> tmplist;

		// BU
		if (tablename.equals("sle_user_bu")) {
			addGroupNode(userId, TStringUtils.getBundleString(tablename), tablename);
			DBAccess tdba = ConnectionManager.getAccessTo("sle_planc_bu");
			tdba.ignoreSecurity();
			tmplist = tdba.search(null, null);
			for (Record r1 : tmplist) {
				Record nr = new Record(rmodel);
				String bid = r1.getFieldValue("id").toString();
				tmpdba.ignoreSecurity();
				Record tmprcd = tmpdba.exist("user_id = " + userId + " AND bu_id = '" + bid + "'");
				nr.setFieldValue("name", bid + ": " + r1.getFieldValue("name"));
				nr.setFieldValue("node", bid);
				nr.setFieldValue("sub_node", userId);
				nr.setFieldValue("autorized", (tmprcd != null));
				nr.setFieldValue("src_file", tablename);
				rList.add(nr);
			}
		}

		// company
		if (tablename.equals("sle_user_company")) {
			addGroupNode(userId, TStringUtils.getBundleString(tablename), tablename);
			DBAccess tdba = ConnectionManager.getAccessTo("sle_company");
			tdba.ignoreSecurity();
			tmplist = tdba.search(null, null);
			for (Record r1 : tmplist) {
				Record nr = new Record(rmodel);
				String bid = r1.getFieldValue("id").toString();
				tmpdba.ignoreSecurity();
				Record tmprcd = tmpdba.exist("user_id = " + userId + " AND company_id = '" + bid + "'");
				nr.setFieldValue("name", bid + ": " + r1.getFieldValue("name"));
				nr.setFieldValue("node", bid);
				nr.setFieldValue("sub_node", userId);
				nr.setFieldValue("autorized", (tmprcd != null));
				nr.setFieldValue("src_file", tablename);
				rList.add(nr);
			}
		}

		// scenarios
		if (tablename.equals("sle_user_scenarios")) {
			addGroupNode(userId, TStringUtils.getBundleString(tablename), tablename);
			DBAccess tdba = ConnectionManager.getAccessTo("sle_scenario");
			tdba.ignoreSecurity();
			tmplist = tdba.search(null, null);
			for (Record r1 : tmplist) {
				Record nr = new Record(rmodel);
				String bid = r1.getFieldValue("id").toString();
				tmpdba.ignoreSecurity();
				Record tmprcd = tmpdba.exist("user_id = " + userId + " AND scenario_id = '" + bid + "'");
				nr.setFieldValue("name", bid + ": " + r1.getFieldValue("name"));
				nr.setFieldValue("node", bid);
				nr.setFieldValue("sub_node", userId);
				nr.setFieldValue("autorized", (tmprcd != null));
				nr.setFieldValue("src_file", tablename);
				rList.add(nr);
			}
		}

		// payrolls
		if (tablename.equals("sle_user_payrolls")) {
			addGroupNode(userId, TStringUtils.getBundleString(tablename), tablename);
			DBAccess tdba = ConnectionManager.getAccessTo("sle_company");
			tdba.ignoreSecurity();
			Vector<Record> cialist = tdba.search(null, null);
			String wc = "";
			for (Record ciarcd : cialist) {
				String ciaId = ciarcd.getFieldValue("id").toString();
				wc = "co_pay = " + ciaId;
				DBAccess tdba1 = ConnectionManager.getAccessTo("sle_payroll_import");
				tdba1.ignoreSecurity();
				tmplist = tdba1.search(wc, null);
				if (tmplist.size() > 0) {
					Record n = addGroupNode(ciaId, (String) ciarcd.getFieldValue("name"), "sle_company");
					n.setFieldValue("sub_node", userId);
					for (Record r1 : tmplist) {
						Record nr = new Record(rmodel);
						String bid = r1.getFieldValue("payroll_id").toString();
						tmpdba.ignoreSecurity();
						Record tmprcd = tmpdba.exist("user_id = " + userId + " AND company_id = '" + ciaId
								+ "' AND payroll_id = '" + bid + "'");
						nr.setFieldValue("name", bid + ": " + r1.getFieldValue("payroll_name"));
						// node with 2 key fields
						nr.setFieldValue("node", ciaId + "<terry>" + bid);
						nr.setFieldValue("sub_node", ciaId);
						nr.setFieldValue("autorized", (tmprcd != null));
						nr.setFieldValue("src_file", tablename);
						rList.add(nr);
					}
				}
			}
		}

		resp.setParameter(ServiceResponse.RECORD_MODEL, rmodel);
		resp.setData(rList);
		return resp;
	}

	/**
	 * append new group node
	 * 
	 * @param node - value for node field
	 * @param name - value for name field
	 * @param ico - value for name src_field (when is a group node, used for icon file name)
	 * 
	 * @return added node record
	 */
	private Record addGroupNode(String node, String name, String ico) {
		Record r = new Record(rmodel);
		r.setFieldValue("node", node);
		r.setFieldValue("name", name);
		r.setFieldValue("src_file", ico); // used for iconname
		rList.add(r);
		return r;
	}
}
