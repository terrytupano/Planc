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
package core;

import gui.*;

import java.sql.*;
import java.text.*;
import java.util.logging.*;

import core.datasource.*;

/**
 * TODO: end the implementation. all methods must migrato to {@link #log(String, String, String, Object...)} (i think
 * :()
 * 
 * @author terry
 * 
 */
public class SystemLog {

	private static MessageFormat messageFormat = new MessageFormat("");

	/**
	 * log exception on local log file
	 * 
	 * @param e - <code>Exception</code> to log
	 * @param sd - <code>true</code> to display exception dialog
	 */
	public static void logException1(Exception e, boolean sd) {
		PlanC.logger.logp(Level.SEVERE, null, null, e.getMessage(), e);
		if (sd) {
			ExceptionDialog.showDialog(e);
		}
	}

	public static void severe(String msg) {
		PlanC.logger.severe(msg);
	}

	public static void warning(String msg) {
		PlanC.logger.warning(msg);
	}

	public static void info(String msg) {
		PlanC.logger.info(msg);
	}

	public static void logException1(Exception e) {
		logException1(e, false);
	}

	/**
	 * append a new entry on main system log file
	 * 
	 * @param mid - message ID
	 * @param flag - flag
	 * @param det - aditional details
	 * @param dta - values for message sustitution
	 */
	public static void log(String mid, String flag, String det, Object... dta) {
		AplicationException ae = new AplicationException(mid);
		String msg = ae.getMessage();
		if (dta != null) {
			messageFormat.applyPattern(msg);
			msg = messageFormat.format(dta);
		}

		DBAccess dba = ConnectionManager.getAccessTo("t_system_log");
		Record logr = dba.getModel();
		logr.setFieldValue("t_sluserid", Session.getUserName());
		logr.setFieldValue("t_slmillis", System.nanoTime());
		logr.setFieldValue("t_sldatetime", new Timestamp(System.currentTimeMillis()));
		logr.setFieldValue("t_slflag", flag);
		logr.setFieldValue("t_sltype", ae.getExceptionType());
		logr.setFieldValue("t_slmsgid", mid);
		logr.setFieldValue("t_slmessage", msg);
		logr.setFieldValue("t_sldetail", det);
		dba.add(logr);
	}

	public static void logException(Exception ex) {
		logException1(ex);
	}

	/**
	 * clear all row in sistemlog main file for this user and flag passed as argument
	 * 
	 * @param fl - flag
	 */
	public static void clearLogByFlag(String fl) {
		String sql = "DELETE FROM t_system_log WHERE t_sluserid = '" + Session.getUserName() + "' AND t_slflag = '"
				+ fl + "'";
		DataBaseUtilities.executeUpdate(sql, "t_system_log");
	}
}
