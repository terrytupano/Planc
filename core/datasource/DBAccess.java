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
package core.datasource;

import gui.*;

import java.sql.*;
import java.sql.Date;
import java.util.*;

import plugin.planc.*;
import core.*;

/**
 * Esta clase representa una abstraccion de una tabla dentro de una base de datos. Es decir cada instancia de esta clase
 * representa un acceso a una tabla especifica dentro de la base de datos. Dice�ada para adaptarse a las distintas
 * formas de tablas dentro de una base de datos. Aunque esta clase en conjuto con <code>Record</code> pueden cambiar
 * segun la tabla, durante el dice�o de la base de datos, deben tomarse en cuenta:
 * 
 * Campos clave: durante el dice�o, deben colocarse todos los campos claves de una tabla al inicio de la lista.
 * 
 * formato de lista: los metodos que retorna un <code>Vector</code> nunca retornan una lista vacia. El primer elemento
 * es el modelo de registro para el archivo al que esta clase representa.
 * 
 * 
 */
public class DBAccess {

	private String DELETE_STATEMENT;
	private String INSERT_STATEMENT;
	private Record model;
	private String DB_NAME;
	private String LIMIT;
	private ResultSet resultSet;
	private String tableName, externTableName;
	private String UPDATE_STATEMENT;
	private DatabaseMetaData dbmetaData;
	private Properties myProperties;
	private boolean ignoreSecurity = false;

	private static Hashtable<String, String> securityLink = new Hashtable<String, String>();

	static {
		/**
		 * security link internal pattern is table_to_filter(source of data);filter_field_name;filter_table(right
		 * table);filter_field_name;user_id_field;
		 * 
		 * waringn: tablename in uppercase
		 * 
		 */
		securityLink.put("SLE_COMPANY", "sle_company;id;sle_user_company;company_id;user_id");
		securityLink.put("SLE_SCENARIO", "sle_scenario;id;sle_user_scenarios;scenario_id;user_id");
		securityLink.put("SLE_PAYROLL_IMPORT", "sle_payroll_import;user_id;sle_user_payrolls;payroll_id;user_id");
		securityLink.put("SLE_PLANC_BU", "sle_planc_bu;id;sle_user_bu;bu_id;user_id");
	}

	/**
	 * new instance
	 * 
	 * @param tab - full qualify table name
	 * @param dbmd - {@link DatabaseMetaData} for extract adtional info
	 * @param prps - properties from connection
	 */
	protected DBAccess(String tab, DatabaseMetaData dbmd, Properties prps) {
		this.externTableName = tab;
		this.dbmetaData = dbmd;
		this.myProperties = prps;

		// nombre calificado o interno
		String q_t[] = tab.split("[.]");
		this.tableName = (q_t.length > 1) ? q_t[1] : tab;

		ResultSetMetaData meta = null;
		Statement sta = null;
		try {
			sta = ConnectionManager.getConnection(externTableName).createStatement();
			// optimiza en tiempo
			DB_NAME = dbmetaData.getDatabaseProductName();
			LIMIT = "";
			// HSQL Database Engine no soporta limits
			if (!DB_NAME.equals("HSQL Database Engine")) {
				LIMIT = " LIMIT 1";
			}
			// oracle use pseudo column rownumber
			if (DB_NAME.equals("Oracle")) {
				LIMIT = " WHERE ROWNUM = 1";
			}
			resultSet = sta.executeQuery("SELECT * FROM " + tableName + LIMIT);
			resultSet.next();
			this.model = getRecord(resultSet, false);
			meta = resultSet.getMetaData();

			StringBuffer ins = new StringBuffer();
			StringBuffer inf = new StringBuffer();
			StringBuffer ups = new StringBuffer();

			int cols = meta.getColumnCount() + 1;
			for (int l = 1; l < cols; l++) {
				String cn = meta.getColumnName(l);
				// Object o = model.getFieldValue(l - 1);
				// String f_ = (o instanceof Date) ? "'?'" : "?";
				// String f_ = "?";

				ups.append(cn + " = ?, ");
				inf.append((((l + 1) < cols) ? cn + ", " : cn));
				ins.append((((l + 1) < cols) ? " ?, " : " ? "));
			}

			// NOTA: utilizar el ultimo ; para sustituir con la clave generada por getKey(Record)
			INSERT_STATEMENT = "INSERT INTO " + tableName + "(" + inf + ") VALUES (" + ins.toString() + ")";
			UPDATE_STATEMENT = "UPDATE " + tableName + " SET " + ups.substring(0, ups.length() - 2) + " WHERE ;";
			DELETE_STATEMENT = "DELETE FROM " + tableName + " WHERE  ;";

			// 12-03-17: se mnovio al final debido a ORA-01001
			resultSet.close();
			sta.close();

			// alterno para usarlo en oracle
			if (DB_NAME.equals("Oracle")) {
				LIMIT = " AND ROWNUM = 1";
			}

		} catch (Exception e) {
			SystemLog.logException1(e);
		}
	}

	/**
	 * retorna instancias para ser valores dentro de los campos dentro de una registro. Este metodo esta sicronizado con
	 * la tabla estandar establecida para concordancia java --- sql. El argumento <code>val</code>, puede ser usado para
	 * establecer el valor inicial del objeto. Si <code>val == null</code> el objeto es creado con valores por omision
	 * (en blanco)
	 * 
	 * @param cn - tipo sql
	 * @param val - representacion del valor o <code>null</code>
	 * @param scal - presicion de la parte decimal (valido solo para campos numericos)
	 * @return objeto
	 */

	public static Object getInstance(int tp, Object val, int len, int scal, int pres) {
		Object obj = null;
		if (tp == Types.VARCHAR || tp == Types.CHAR || tp == Types.LONGVARCHAR || tp == Types.VARCHAR) {
			obj = (val == null) ? "" : val.toString();
		}
		if (tp == Types.BOOLEAN || tp == Types.BIT || tp == Types.TINYINT) {
			obj = (val == null) ? Boolean.valueOf(false) : Boolean.valueOf(val.toString());
		}
		if (tp == Types.DATE) {
			Date da = new Date(TStringUtils.ZERODATE.getTime());
			// 170323: parche para oracle: indica tipo de datos date pero internamente es timestamp
			if (val != null) {
				if (val instanceof Timestamp) {
					da.setTime(((Timestamp) val).getTime());
					val = da;
				}
			}
			obj = (val == null) ? da : Date.valueOf(val.toString());
		}
		if (tp == Types.TIME) {
			java.util.Date da = new java.util.Date(0);
			obj = (val == null) ? new Time(da.getTime()) : Time.valueOf(val.toString());
		}
		if (tp == Types.TIMESTAMP) {
			obj = (val == null) ? new java.sql.Timestamp((new java.util.Date()).getTime()) : java.sql.Timestamp
					.valueOf(val.toString());
		}
		if (tp == Types.NUMERIC) {
			if (scal > 0) {
				obj = new Double((val == null) ? "0" : val.toString());
			} else {
				// 171121: to avoid numeric overflow, check presition
				if (pres > 9) {
					obj = new Long((val == null) ? "0" : val.toString());
				} else {
					obj = new Integer((val == null) ? "0" : val.toString());
				}
			}
		}
		if (tp == Types.REAL) {
			obj = new Double((val == null) ? "0" : val.toString());
		}
		if (tp == Types.BIGINT || tp == Types.DECIMAL) {
			obj = new Long((val == null) ? "0" : val.toString());
		}

		if (tp == Types.INTEGER) {
			obj = new Integer((val == null) ? "0" : val.toString());
		}
		// if (tp == Types.BINARY || tp == Types.BLOB || tp == Types.LONGVARBINARY) {
		if (tp == Types.BINARY || tp == Types.BLOB || tp == Types.CLOB || tp == Types.LONGVARBINARY) {
			obj = (val == null) ? new Byte[0] : (byte[]) val;
		}
		return obj;
	}

	// update documentation
	public synchronized boolean add(Record rcd) {
		boolean ok = true;
		try {
			PreparedStatement psta = ConnectionManager.getConnection(externTableName)
					.prepareStatement(INSERT_STATEMENT);
			rcd.updateTime();
			boolean at = checkAuditTrack(rcd);
			// using fields form model to ignore tmp fields
			for (int j = 0; j < model.getFieldCount(); j++) {
				String fn = model.getFieldName(j);
				Object o = rcd.getDBFieldValue(fn);
				setObjectInStatement(psta, o, j);
			}
			// System.out.println(psta.toString());
			psta.executeUpdate();
			psta.close();

			// AUDITORIA
			if (at) {
				auditLog("sadd", rcd);
			}
		} catch (Exception e) {
			SystemLog.logException1(e);
			ok = false;
		}
		return ok;
	}

	/**
	 * actualiza registro de auditoria.
	 * 
	 * @param ac - accion debe ser alguna de las siguientes<code>sadd, supd, sdlt</code>
	 * @param rcdr - contiene el registro al cual se le esta efectuando la accion. debera ser <code>null</code>
	 */
	private void auditLog(String ac, Record rcdr) {
		if (!tableName.equals("t_audit_log")) {
			DBAccess dbAT = ConnectionManager.getAccessTo("t_audit_log");
			Record mod = dbAT.getModel();
			mod.setFieldValue("t_altable_name", TStringUtils.getBundleString(tableName));
			mod.setFieldValue("t_alaudit_track", rcdr.getFieldValue("audit_track"));
			mod.setFieldValue("t_alaction", ac);
			mod.updateTime();
			mod.setFieldValue("t_alrecord", DataBaseUtilities.getSerializableRecord(rcdr));
			mod.setFieldValue("t_alkeyfields", rcdr.getKeyFieldValueComaSeparated());
			dbAT.add(mod);
		}
	}

	/**
	 * Delete the record from database's table represented by this instance.
	 * 
	 * @param rcd - record to delete
	 * @return <code>true</code> if delete was suscefully
	 */
	public synchronized boolean delete(Record rcd) {
		try {
			boolean at = checkAuditTrack(rcd);
			String sta = DELETE_STATEMENT.replaceAll(";", getKey(rcd));
			// String tmpsta = DELETE_STATEMENT.replaceAll(";", getKey(rcd));
			PreparedStatement psta = ConnectionManager.getConnection(externTableName).prepareStatement(sta);
			/*
			 * for (int j = 0; j < rcd.getFieldCount(); j++) { if (rcd.isKeyField(j)) { String fn =
			 * model.getFieldName(j); Object o = rcd.getDBFieldValue(fn); tmpsta = tmpsta.replaceFirst("[?]",
			 * o.toString()); setObjectInStatement(psta, o, j); } }
			 */
			psta.executeUpdate();
			psta.close();
			// auditoria
			if (at) {
				auditLog("sdlt", rcd);
			}
			return true;
		} catch (SQLException sqle) {
			if (sqle.getSQLState().equals(String.valueOf(23000))) {
				String fklst = ConnectionManager.getFKListFor(tableName);
				ExceptionDialog.showDialog(ExceptionDialog.WARNING,
						TStringUtils.getBundleString("action.delete.title"),
						TStringUtils.getBundleString("foreingKey.ms01"), fklst);
			} else {
				SystemLog.logException1(sqle);
			}
			return false;
		}
	}

	/**
	 * verifica si en la base de datos existe un registro con los mismos valores de los campos clave del registro pasado
	 * como argumento.
	 * 
	 * @param rcd - Registro cuyos campos claves seran verificados
	 * @return Registro encontrado. o <code>null</code> si no encuentra ninguno. si hay mas de uno, retorna el primero
	 */
	public synchronized Record exist(Record rcd) {
		return exist(getKey(rcd));
	}

	/**
	 * verifica que existe un registro que conpla con la clausula where pasada como argumento. si existe mas de uno,
	 * retornara el primero
	 * 
	 * @param wc - elementos para la clausula WHERE
	 * @return registro encontrado o null
	 */
	public synchronized Record exist(String wc) {
		// Record r = null;
		Vector v = new Vector(1);
		try {
			Statement sta = ConnectionManager.getConnection(externTableName).createStatement();
			resultSet = sta.executeQuery("SELECT * FROM " + tableName + " WHERE " + wc + LIMIT);
			boolean b = resultSet.next();
			if (b) {
				v.add(getRecord(resultSet, true));
			}
			resultSet.close();
			sta.close();
		} catch (Exception e) {
			SystemLog.logException1(e);
		}
		// apply security filter
		applySecurityFilter(v);
		Record r = v.size() > 0 ? (Record) v.elementAt(0) : null;

		// for debug purpose only
		if (r == null && securityLink.get(tableName) != null) {
			SystemLog.warning("User not autorized to record retrived by WHERE clause " + wc);
		}
		return r;
	}
	/**
	 * construct the WHERE clausule parameter
	 * 
	 * @param r - Record to extract WHERE clausule
	 * 
	 * @return String representation of WHERE
	 */
	public String getKey(Record r) {
		StringBuffer k = new StringBuffer();
		for (int j = 0; j < r.getFieldCount(); j++) {
			if (r.isKeyField(j)) {
				Object val = r.getFieldValue(j);
				String lcom = "";
				String rcom = "";
				if (val instanceof java.util.Date) {
					val = new java.sql.Date(((java.util.Date) val).getTime());
				}
				if (val instanceof String || val instanceof Date || val instanceof Timestamp) {
					lcom = "'";
					rcom = "'";
				}
				if (DB_NAME.equals("Oracle")) {
					if (val instanceof Date || val instanceof Timestamp) {
						lcom = "TO_DATE('";
						rcom = "', 'yyyy-mm-dd')";
					}
				}
				k.append(r.getFieldName(j) + " = " + lcom + val.toString() + rcom + " AND ");
			}
		}
		return k.subSequence(0, k.length() - 5).toString();
	}
	/**
	 * retorna una copia del modelo de registro. el modelo de registro es un registro vacio con la estructura que tiene
	 * la tabla.
	 * 
	 * @return - modelo
	 */
	public Record getModel() {
		return new Record(model);
	}
	/**
	 * Set whether this instance of {@link DBAccess} ignore the data security filter. the {@link #ignoreSecurity} flag
	 * is one-time use. Subsecuents calls to this instance will apply security filter unless this method is invoked
	 * again.
	 * 
	 * @see #applySecurityFilter(Vector)
	 * @param is - <code>true</code> for ignore filter
	 * 
	 */
	public void ignoreSecurity() {
		ignoreSecurity = true;
	};

	/**
	 * Obtiene todos los objetos de la tabla de datos, que cumplen con los requisitos del argumento de entrada. Para
	 * usar la propiedad, hay que escribir los requsitos de seleccion en sintaxis SQL. <code>search(null, 
	 * "NOMBRE = 'juan' AND EDAD = 20");</code>
	 * 
	 * @param wc - clausula WHERE. <code>null</code> para retornar todos
	 * @param ob - columnas para clausula ORDER BY. <code>null</code> para ordenamiento por clave del registro
	 * 
	 * @return Un vector con todos los registros que cumplen la seleccion. cada elemento es una instancia de
	 *         <code>Record</code>
	 * 
	 */
	public synchronized Vector search(String wc, String ob) {
		long t1 = System.currentTimeMillis();
		long t2 = 0;
		int rcds = 0;
		Vector v = new Vector();
		String ssta = "";
		// Hashtable ht = new Hashtable();
		try {
			Statement sta = ConnectionManager.getConnection(externTableName).createStatement();

			// ordenamiento sin comillas
			StringBuffer ord = new StringBuffer(" ORDER BY ");
			if (ob == null) {
				ord.append(model.getOrderBy());
			} else {
				ord.append(ob);
			}
			// clausula where
			ssta = "SELECT * FROM " + tableName + ((wc == null) ? " " : " WHERE " + wc) + ord.toString();

			resultSet = sta.executeQuery(ssta);
			t2 = System.currentTimeMillis();
			while (resultSet.next()) {
				Record r = getRecord(resultSet, true);
				v.addElement(r);
				rcds++;
				if (rcds > 10000) {
					break;
				}
			}
			resultSet.close();
			sta.close();
		} catch (Exception e) {
			SystemLog.logException1(e);
		}
		/*
		 * if (tableName.equals("SLE_PLANC_AMOUNT")) { System.out.println(ssta);
		 * System.out.println("   Total Execution Time: " + (System.currentTimeMillis() - t1));
		 * System.out.println("   Total DB Time: " + (t2 - t1)); System.out.println("   Total Records: " + rcds); }
		 */
		// apply data filter security
		applySecurityFilter(v);
		return v;
	}

	// update the document
	public synchronized boolean update(Record rcd) {
		boolean ok = true;
		try {
			boolean at = checkAuditTrack(rcd);
			String updsta = UPDATE_STATEMENT.replaceAll(";", getKey(rcd));
			PreparedStatement psta = ConnectionManager.getConnection(externTableName).prepareStatement(updsta);
			int c = 0;
			// user record model to update only fields belong to this table avoiding temporal fields.
			// perform update over all fieldsbecause getKey ensure correct record destination
			for (int j = 0; j < model.getFieldCount(); j++) {
				String fn = model.getFieldName(j);
				Object obj = rcd.getDBFieldValue(fn);
				// utmp = utmp.replaceFirst("[?]", obj.toString());
				// System.out.println(fn + ": " + obj.getClass().getName());
				setObjectInStatement(psta, obj, c);
				c++;
			}
			psta.executeUpdate();
			psta.close();
			// AUDITORIA
			if (at) {
				auditLog("supd", rcd);
			}
		} catch (Exception e) {
			SystemLog.logException1(e);
			ok = false;
		}
		return ok;
	}

	/**
	 * adiciona registro a esta tabla si no existe o actualiza sus campos si ya existe un registro con clave igual al
	 * registro pasado como argumento
	 * 
	 * @param rcd - registro a escribir
	 */
	public synchronized void write(Record rcd) {
		if (exist(rcd) == null) {
			add(rcd);
		} else {
			update(rcd);
		}
	}

	/**
	 * this method filter the the list or record stored in <code>v</code> if data security filter is active or this
	 * instance contain a know link between the acces that represent to another file that store the autorized record for
	 * this access. Any time that this method is invoked, the {@link #ignoreSecurity} field is set to false to avoid
	 * security breach.
	 * 
	 * @see #ignoreSecurity()
	 * @param v - Vector of records to filter
	 */
	private void applySecurityFilter(Vector v) {
		// ignore security or this instance don't have securityl link
		String slnk = securityLink.get(tableName);
		if (ignoreSecurity || (slnk == null)) {
			ignoreSecurity = false;
			return;
		}
		ignoreSecurity = false;
		// is data security active ??
		if (!Session.isDataSecurityActive()) {
			return;
		}
		Long fusid = (Long) Session.getUserFieldValue("id");
		// 180125: bug18.3 planc bussines logic: see method documentation
		if (tableName.equals("SLE_PLANC_BU")) {
			if (SLEPlanC.allowAllBU() == null) {
				return;
			}
		}
		String[] link = slnk.split(";");
		// String lefttn = link[0];
		String leftfn = link[1];
		String righttn = link[2];
		String rightfn = link[3];
		String useridfn = link[4];
		DBAccess ridba = ConnectionManager.getAccessTo(righttn);
		String qurigthfn = ridba.getModel().getFieldValue(rightfn) instanceof Number ? "" : "'";
		for (int i = 0; i < v.size(); i++) {
			Record r = (Record) v.elementAt(i);
			String wc = rightfn + " = " + qurigthfn + r.getFieldValue(leftfn) + qurigthfn + " AND " + useridfn + " = "
					+ fusid;
			if (ridba.exist(wc) == null) {
				v.remove(i);
				i--;
			}
		}
	}

	/*
	 * 
	 * private Record getRecord(ResultSet rset) throws SQLException { Record r = new Record(model); for (int i = 1; i <
	 * r.getFieldCount() + 1; i++) { Object val = rset.getObject(i);
	 * 
	 * r.setFieldValue(i-1, val); } return r; }
	 */

	/**
	 * verifica la existencia del campo de rastro de auditoria (no todas las tablas lo contienen) y si este no esta
	 * establecido (debido , posiblemente a importacion de datos masivamente) lo establece
	 * 
	 * @param rcd - registro a donde buscar y establecer campo de auditoria
	 * 
	 * @return true si si el campo de auditoria existe dentro del registro
	 */
	private boolean checkAuditTrack(Record rcd) {
		boolean atb = false;
		for (int i = 0; i < rcd.getFieldCount(); i++) {
			if (rcd.getFieldName(i).equals("audit_track")) {
				atb = true;
				if (rcd.getFieldValue(i).toString().equals("")) {
					rcd.setFieldValue(i, TStringUtils.getUniqueID());
				}
			}
		}
		return atb;
	}

	/**
	 * metodo que crea y retorna un registro que representa una ilera de datos dentro de un archivo de base de datos.
	 * Segun el valor del parametro <code>dta</code> este metodo retorna un registro de datos o un modelo.
	 * 
	 * @param rset - objeto a estudiar para modelo o para datos
	 * @param dta - <code>true</code> datos
	 * 
	 * @return regostro
	 * 
	 * @throws SQLException
	 */
	private Record getRecord(ResultSet rset, boolean dta) throws SQLException {
		Record drcd = null;
		if (!dta) {
			ResultSetMetaData meta = rset.getMetaData();
			Field[] flds = new Field[meta.getColumnCount()];
			for (int i = 1; i < meta.getColumnCount() + 1; i++) {
				String cn = meta.getColumnName(i).toLowerCase();
				int scal = meta.getScale(i);
				int colt = meta.getColumnType(i);
				int pres = meta.getPrecision(i);
				int leng = meta.getColumnDisplaySize(i);
				boolean inu = meta.isNullable(i) == ResultSetMetaData.columnNullable;
				String clsn = meta.getColumnClassName(i);

				/*
				 * if (tableName.equals("SLE_SECURITY_TEMPLATES")) { Object obj = rset.getObject(i); Class cls = obj ==
				 * null ? null : obj.getClass(); System.out.println(cn + " " + meta.getColumnTypeName(i) + "(" + colt +
				 * ") " + "presition " + pres + " Scale " + scal + " " + "length " + meta.getColumnDisplaySize(i) +
				 * " DB object class map " + cls); }
				 */

				Object val1 = getInstance(colt, null, leng, scal, pres);

				// Object val = rset.getObject(i);
				// System.out.println(tableName + " " + cn + " " + val1.getClass().getName());

				// System.out.println(tableName + " " + cn + " " + val1.getClass().getName());
				// flds[i - 1] = new Field(cn, val1, leng, pres, false, inu);
				flds[i - 1] = new Field(cn, val1, leng, pres, false, inu, clsn);
			}
			// set the primary keys for this record
			ResultSet rs = dbmetaData.getPrimaryKeys(null, myProperties.getProperty("*schema", null), tableName);
			int kfcnt = 0;
			while (rs.next()) {
				String fn = rs.getString("COLUMN_NAME").toLowerCase();
				// System.out.println(tableName + ": " + fn + " >>" + myProperties.getProperty("*schema", null));
				for (Field fl : flds) {
					if (fl.name.equals(fn)) {
						fl.iskey = true;
						kfcnt++;
					}
				}
			}
			rs.close();

			// know file without primary keys
			String pkls[] = null;
			if (kfcnt == 0) {
				SystemLog.warning("No key field found for database table " + tableName);
				if (tableName.equals("SLE_PLANC_ACCOUNT")) {
					pkls = new String[]{"id"};
				}
				if (tableName.equals("SLE_PLANC_PRESUMPTION")) {
					pkls = new String[]{"scenario_id", "account_id", "time_slot"};
				}
				if (tableName.equals("SLE_PLANC_AMOUNT")) {
					pkls = new String[]{"scenario_id", "company_id", "bu_id", "account_id", "workrelation_id"};
				}
				if (tableName.equals("SLE_SCALE")) {
					pkls = new String[]{"id"};
				}
				if (tableName.equals("SLE_PLANC_GEN_ACCOUNT")) {
					pkls = new String[]{"scenario_id", "account_id"};
				}
				if (tableName.equals("SLE_USER_CALC_TYPES")) {
					pkls = new String[]{"user_id", "calc_type_id"};
				}
				if (tableName.equals("SLE_VIEW_PLANC_ACCOUNTING_DIST")) {
					pkls = new String[]{"scenario_id", "company_id", "workrelation_id"};
				}
				
				
				if (pkls.length > 0) {
					SystemLog.warning("Database table " + tableName + " keys fields alter by DBAccess.");
					for (Field fl : flds) {
						for (String pk : pkls) {
							// ignorecase for Oracle db because for view, field name can be in lowercase !!!
							if (fl.name.equalsIgnoreCase(pk)) {
								fl.iskey = true;
								kfcnt++;
							}
						}
					}
				}
			}
			if (kfcnt == 0) {
				throw new SQLException("No primary key found in Database table " + tableName);
			}
			drcd = new Record(externTableName, flds);
		} else {
			drcd = new Record(model);
			for (int i = 1; i < drcd.getFieldCount() + 1; i++) {
				Object oval = drcd.getFieldValue(i - 1);
				Object rval = rset.getObject(i);
				if (oval instanceof String) {
					rval = rval == null ? "" : rset.getString(i);
				}
				if (oval instanceof Boolean) {
					rval = rval == null ? Boolean.FALSE : rset.getBoolean(i);
				}
				if (oval instanceof Double) {
					rval = rval == null ? 0.0 : rset.getDouble(i);
				}
				if (oval instanceof Long) {
					rval = rval == null ? 0 : rset.getLong(i);
				}
				if (oval instanceof Float) {
					rval = rval == null ? 0.0 : rset.getFloat(i);
				}
				if (oval instanceof Integer) {
					rval = rval == null ? 0 : rset.getInt(i);
				}
				if (oval instanceof Date) {
					rval = rval == null ? new java.sql.Date(TStringUtils.ZERODATE.getTime()) : rset.getDate(i);
				}
				if (oval instanceof Time) {
					rval = rval == null ? new java.sql.Date(TStringUtils.ZERODATE.getTime()) : rset.getTime(i);
				}
				if (oval instanceof Timestamp) {
					rval = rval == null ? new java.sql.Date(TStringUtils.ZERODATE.getTime()) : rset.getTimestamp(i);
				}
				if (oval instanceof Byte[]) {
					rval = rval == null ? new Byte[0] : rset.getBytes(i);
				}
				drcd.setFieldValue(i - 1, rval);
			}
		}
		return drcd;
	}
	private void setObjectInStatement(PreparedStatement psta, Object val, int col) throws SQLException {
		if (DB_NAME.equals("Oracle")) {
			boolean set = false;
			if (val instanceof java.util.Date || val instanceof java.sql.Date) {
				psta.setTimestamp(col + 1, new Timestamp(((java.util.Date) val).getTime()));
				set = true;
			}
			if (val instanceof Integer) {
				psta.setObject(col + 1, val, Types.NUMERIC);
				set = true;
			}
			if (val instanceof byte[]) {
				/*
				 * byte[] val1 = (byte[]) val; try { ByteArrayInputStream ois = new ByteArrayInputStream(val1);
				 * psta.setBlob(col + 1, ois, val1.length); // psta.setBinaryStream(col + 1, ois); } catch (Exception e)
				 * { e.printStackTrace(); } set = true;
				 */
			}

			if (!set) {
				psta.setObject(col + 1, val);
			}
		} else {
			// siempre ha funcionado para las demas base de datos
			psta.setObject(col + 1, val);
		}
	}

}
