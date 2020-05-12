package core;

import java.util.*;

import javax.swing.*;

import plugin.planc.*;
import action.*;
import core.datasource.*;

/**
 * contiene metodos y datos que permiten agrupar toda la informacion de una sesion de trabajo para inciar, establesca el
 * registro de usuario
 * 
 */
public class Session {

	private static Record user = null;
	public static boolean DBConnected;
	private static String masterUser;
	private static String consoleUser= "Console";

	/**
	 * retorna el registro del usuario que actualmente esta en sesion
	 * 
	 * @return registro de usuario
	 */
	public static Record getUser() {
		return user;
	}
	
	public static void setMasterUser(String mu) {
		masterUser = mu;
	}

	public static Object getUserFieldValue(String fn) {
		return user.getFieldValue(fn);
	}

	public static String getUserName() {
		return (String) user.getFieldValue("username");
	}

	/**
	 * retorna true si existe una sesion activa. para activar o desactivar una session, utilice el metodo setUser(null)
	 * o setUser(Record)
	 * 
	 * @return - true = activo
	 */
	public static boolean inSession() {
		return user != null;
	}

	/**
	 * return <code>true</code> if data autorization are active
	 * 
	 * @return whether data security are active
	 */
	public static boolean isDataSecurityActive() {
		if (preliminaryCheck(false)) {
			// return false to simulate data autorization are off if precheck are true
			return false;
		}
		Record r = ConnectionManager.getAccessTo("sle_security_templates").exist(
				"id = " + user.getFieldValue("template_id"));
		int om = (Integer) r.getFieldValue("check_payroll");
		return om == 0 ? true : false;
	}

	/**
	 * - if action has {@link SLEPlanC#PLANC_ID} is treated as menu action or general action and the autorization are
	 * validated agains sle_role_options. else, action is treated as action inside of a component and validated with
	 * template roles
	 * 
	 * @param act
	 * @return
	 */
	public static boolean isAutorizedForAction(TAbstractAction act) {
		boolean actaut = false;
		if (preliminaryCheck(true)) {
			return true;
		}
		String wc = "template_id = " + user.getFieldValue("template_id");
		Vector<Record> rlist = ConnectionManager.getAccessTo("sle_template_roles").search(wc, null);
		Long pid = (Long) act.getValue(SLEPlanC.PLANC_ID);
		pid = (pid == null) ? -1L : pid;
		for (Record trr : rlist) {
			// instances with SLEPlanC.PLANC_ID
			// if (act instanceof MenuActionFactory || act instanceof DockingAction) {
			if (pid > -1) {
				Record optr = ConnectionManager.getAccessTo("sle_role_options").exist(
						"role_id = '" + trr.getFieldValue("role_id") + "' AND option_id = " + pid);
				actaut = optr != null ? true : actaut;
			} else {
				// actions over data elements
				if (act instanceof NewRecord || act instanceof EditRecord || act instanceof DeleteRecord) {
					if (trr.getFieldValue("enable_insert").equals(1) && act instanceof NewRecord) {
						actaut = true;
					}
					if (trr.getFieldValue("enable_update").equals(1)
							&& (act instanceof EditRecord || act.getScope() == TAbstractAction.RECORD_SCOPE)) {
						actaut = true;
					}
					if (trr.getFieldValue("enable_delete").equals(1) && act instanceof DeleteRecord) {
						actaut = true;
					}
				} else {
					// for others instances, grant acces
					actaut = true;
				}
			}
		}
		return actaut;
	}
	/**
	 * complete docmentation later !! im borring and its 171219-11:36Pm
	 * 
	 * @param cnt - container
	 * @param act - instance of {@link TAbstractAction} inside of container
	 * 
	 * @return true for grant autorization
	 */
	public static boolean isAutorizedForComponent(Object cnt) {
		if (cnt == null) {
			throw new NullPointerException("Container can't be null.");
		}
		if (preliminaryCheck(true)) {
			return true;
		}
		// container autorization must exit in roleoptions
		boolean cntaut = false;
		String wc = "template_id = " + user.getFieldValue("template_id");
		Vector<Record> rlist = ConnectionManager.getAccessTo("sle_template_roles").search(wc, null);
		Long pid = (Long) ((JComponent) cnt).getClientProperty(SLEPlanC.PLANC_ID);
		pid = (pid == null) ? -1L : pid;
		for (Record trr : rlist) {
			Record optr = ConnectionManager.getAccessTo("sle_role_options").exist(
					"role_id = '" + trr.getFieldValue("role_id") + "' AND option_id = " + pid);
			cntaut = optr != null ? true : cntaut;
		}
		return cntaut;
	}

	/**
	 * return whether the user in sesion are autorized for the module according to value stored in corespondient field
	 * <code>fn</code>
	 * 
	 * @param fn - field name of requested autorization (OMIT_REPORT, OMIT_TRAINING, etc)
	 * @return <code>true</code> if user are autorized for module
	 */
	public static boolean isAutorizedForModule(String fn) {
		if (preliminaryCheck(false)) {
			return true;
		}
		Record r = ConnectionManager.getAccessTo("sle_security_templates").exist(
				"id = " + user.getFieldValue("template_id"));
		int om = (Integer) r.getFieldValue(fn);
		// masteruser allow all modules
		// om = isMasterUser() ? 0 : om;
		return om == 0 ? true : false;
	}

	/**
	 * return <code>true</code> if actual user is aplicationns's master user
	 * 
	 * @return <code>true</code>
	 */
	public static boolean isMasterUser() {
		String un = (String) user.getFieldValue("username");
		return un.equalsIgnoreCase(masterUser) || un.equalsIgnoreCase(consoleUser);
	}

	/**
	 * establece el usuario que ha iniciado sesion. si el registro de usuario pasado como argumento es null, se
	 * establece el panel de bienvenida y se asumira que la sesicon ha finalizado.
	 * 
	 * @param user - registro de usuario
	 */
	public static void setUser(Record u) {
		user = u;
		if (PlanC.RUNNING_MODE.equals(PlanC.RM_NORMAL)) {
			int targetpanel = (user == null) ? PlanC.SIGNIN : PlanC.DOCKING;
			// if autentication is autenticationApp check password reset operation
			String autMet = SystemVariables.getStringVar("autenticationMethod");
			if (autMet.equals("autenticationApp") && user.getFieldValue("PASSWORD").toString().length() == 6) {
				targetpanel = PlanC.CHANGE_PASSWORD;
			}
			PlanC.setContentPane(targetpanel);
		}
	}

	/**
	 * udate user record. i.e: changeUserPassword update password field for this user
	 * 
	 * @param u - user record
	 */
	public static void updateUserRecord(Record u) {
		user = u;
	}

	/**
	 * Initial security validation.
	 * 
	 * @param cr <code>true</code> if chech security by roles are active
	 * 
	 * @return <code>true</code> no more validation are required to grant access
	 */
	private static boolean preliminaryCheck(boolean cr) {
		// sigin
		if (user == null) {
			return true;
		}
		// master user
		if (isMasterUser()) {
			//return true;
		}
		if (cr) {
			// check if autorization by roles are off
			Record roler = ConnectionManager.getAccessTo("sle_security_templates").exist(
					"id = " + user.getFieldValue("template_id"));
			// if security by roles inactive, return true
			if ((Integer) roler.getFieldValue("role_opt") == 0) {
				return true;
			}
		}
		return false;
	}
}
