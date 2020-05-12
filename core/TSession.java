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

import core.datasource.*;


/**
 * contiene metodos y datos que permiten agrupar toda la informacion de una sesion de trabajo para inciar, establesca el
 * registro de usuario
 * 
 */
public class TSession {

	private static Record user, userRol;
	public static boolean DBConnected;
	private static String masterUser = "*master";

	public static String getUserName() {
		return (String) user.getFieldValue("T_USNAME");
	}

	/**
	 * retorna el registro del usuario que actualmente esta en sesion
	 * 
	 * @return registro de usuario
	 */
	public static Record getUser() {
		return user;
	}
	
	public static Object getUserFieldValue(String fn) {
		return user.getFieldValue(fn);
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
	 * establece el usuario que ha iniciado sesion. si el registro de usuario pasado como argumento es null, se
	 * establece el panel de bienbenida y se asumira que la sesicon ha finalizado.
	 * 
	 * @param user - registro de usuario
	 */
	public static void setUser(Record u) {
		user = u;
		// localiza el roll para tenerlo en memoria. null para master
		userRol = (user == null) ? null : ConnectionManager.getAccessTo("t_users").exist(
				"t_ususer_id = '" + user.getFieldValue("t_usroll") + "'");
		PlanC.setContentPane((user == null) ? PlanC.SIGNIN : PlanC.DOCKING);
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
	 * verifica si el objeto <code>obj</code>contenido en el componente <code>cnt</code> esta autorizado para el usuario
	 * q esta en sesion actualmente. este metodo
	 * <ol>
	 * <li>verifica la autorizacion para el roll de usuario al que este usuario esta asignado. si no esta asociado a
	 * ningun roll o en la autorizacion descrita dentro del roll no le permite el acceso:
	 * <li>se verifica la autorizacion asociada al usuario. se verifica la
	 * </ol>
	 * 
	 * @param cnt - contenedor
	 * @param obj - objeto puede ser <code>null</code>si solo se desea verficar autorizacion para un contenedor
	 * @return true si se tiene autorizacion a ambos elementos o al <code>cnt</code>cuando obj = null
	 */
	public static boolean checkAutoritation(Object cnt, Object obj) {
		if (cnt == null) {
			throw new NullPointerException("Container can't be null");
		}
		// master user
		if (isMasterUser()) {
			return true;
		}
		// component in default docking package are available for all (only container)
		if (obj == null) {
			String s1 = cnt.getClass().getName();
			String s = s1.substring(0, s1.lastIndexOf(".") + 1);
			if (s.equals("gui.docking")) {
				return true;
			}
		}

		boolean aut = false;
		// se verifica autorizacion para el rol de usuario
		if (userRol != null) {
			aut = checkAutoritation1((String) userRol.getFieldValue("t_uspermission"), cnt, obj);
		}
		// no esta autorizado o no posee rol. se verifica usuario
		if (userRol == null || aut == false) {
			aut = checkAutoritation1((String) user.getFieldValue("t_uspermission"), cnt, obj);
		}
		return aut;
	}

	/**
	 * return <code>true</code> if actual user is aplicationns's master user
	 * 
	 * @return <code>true</code>
	 */
	public static boolean isMasterUser() {
		return user.getFieldValue("T_USuser_id").toString().equalsIgnoreCase(masterUser);
	}

	/**
	 * verifica autorizaciones. usando es string que describe la autorizacion se procede asi:
	 * <ol>
	 * <li>se busca del nombre del contenedor <code>cnt</code> si no se encuentra, se retorna <code>false</code>
	 * <li>si se encuentra el contenedor, se verifican la lista nombre de objetos. si no se encuentran, se retorna
	 * <code>false</code> <br>
	 * </ol>
	 * 
	 * @param ur - string en formato: ContainerName,ObjetName,ObjetName,...;Container,*all
	 * @param cnt - Objeto contenedor
	 * @param obj - Objeto
	 * @return <code>true</code> si el usuario esta autorizado a ambos: objeto y contenedor
	 */
	private static boolean checkAutoritation1(String ua, Object cnt, Object obj) {
		boolean cnta = false;
		boolean obja = (obj == null);
		String cntn = TResourceUtils.getClassName(cnt);
		String objn = (obj == null) ? "" : TResourceUtils.getClassName(obj);
		// contenedor
		String[] c_o = ua.split(";");
		for (int i = 0; i < c_o.length && !cnta; i++) {
			String[] cls = c_o[i].split(",");
			cnta = cls[0].equals(cntn);
			if (cnta) {
				for (int j = 1; j < cls.length && !obja; j++) {
					obja = cls[j].equals(objn);
				}
			}
		}
		return cnta && obja;
	}
}
