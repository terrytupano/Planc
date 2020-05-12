package plugin.planc;

import gui.*;

import java.beans.*;
import java.io.*;
import java.util.*;

import javax.naming.*;
import javax.naming.ldap.*;
import javax.swing.*;

import action.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * panel de entrada de usuario y contraceña. este panel retorna el registro pasado como argumento del constructor con
 * los campos usuario y contrace;a llenos
 * 
 */
public class PUserLogIn extends AbstractRecordDataInput implements PropertyChangeListener {

	private JCheckBox jcb_rem_usr;
	private JTextField jtf_user_id;
	private Record usrmod, passPolicy;
	private DBAccess dbAccess;
	private int t_usmax_attemps;

	/**
	 * nueva instancia
	 * 
	 * @param usr - registro de usuario
	 * 
	 */
	public PUserLogIn() {
		super("security.title04", null, false);

		// retrive default password policy
		this.passPolicy = ConnectionManager.getAccessTo("sle_password_policy").exist("ID=0");
		this.t_usmax_attemps = -1;
		this.dbAccess = ConnectionManager.getAccessTo("sle_users");
		this.usrmod = dbAccess.getModel();
		setModel(usrmod);
		addPropertyChangeListener(TConstants.ACTION_PERFORMED, this);
		this.jtf_user_id = TUIUtils.getJTextField("ttusername", (String) usrmod.getFieldValue("username"), 20);
		JPasswordField jtfp = TUIUtils.getJPasswordField("ttpassword", (String) usrmod.getFieldValue("password"), 20);
		jtfp.setText("");
		addInputComponent("username", jtf_user_id, true, true);
		addInputComponent("password", jtfp, true, true);

		// recordar usuario
		boolean bol = (Boolean) TPreferences.getPreference(TPreferences.REMIND_USER, "", false);
		String str = (String) TPreferences.getPreference(TPreferences.USER, "", "");
		jtf_user_id.setText(str);
		this.jcb_rem_usr = TUIUtils.getJCheckBox("security.r05", bol);

		FormLayout lay = new FormLayout("left:pref, 3dlu, 80dlu, 7dlu, left:pref, 3dlu, 80dlu",
				"pref, 3dlu, pref, 3dlu, pref, 3dlu, pref");
		PanelBuilder bui = new PanelBuilder(lay);
		CellConstraints cc = new CellConstraints();
		bui.add(getLabelFor("username"), cc.xy(1, 1));
		bui.add(getInputComponent("username"), cc.xy(3, 1));
		bui.add(getLabelFor("password"), cc.xy(5, 1));
		bui.add(getInputComponent("password"), cc.xy(7, 1));
		// bui.add(TUIUtils.getJLabel("i.s04", false, true), cc.xy(1, 3));
		// bui.add(DBselector, cc.xyw(3, 3, 3));
		bui.add(jcb_rem_usr, cc.xyw(1, 5, 3));

		setDefaultActionBar();
		add(bui.getPanel());
		preValidate(null);
	}

	@Override
	public Record getRecord() {
		Record usr = super.getRecord();
		// guarda preferencias
		boolean bol = jcb_rem_usr.isSelected();
		String str = bol ? (String) usr.getFieldValue("username") : "";
		TPreferences.setPreference(TPreferences.REMIND_USER, "", bol);
		TPreferences.setPreference(TPreferences.USER, "", str);
		return usr;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getNewValue() instanceof AceptAction) {
			Record r1 = getRecord();
			String usr = (String) r1.getFieldValue("username");
			String pass = (String) r1.getFieldValue("password");

			String autMet = SystemVariables.getStringVar("autenticationMethod");
			Record r2 = null;
			r2 = autMet.equals("autenticationOracleDB") ? autenticationOracleDB(usr, pass) : null;
			r2 = autMet.equals("autenticationLDAP") ? autenticationLDAP(usr, pass) : r2;
			r2 = autMet.equals("autenticationApp") ? autenticationApp(usr, pass) : r2;
			if (r2 != null) {
				Session.setUser(r2);
			}
		}
		if (evt.getNewValue() instanceof CancelAction) {
			Exit.shutdown();
		}
	}

	/**
	 * this method check user and password against LDAP provider. if database connection ok,
	 * <code>autenticationApp</code> method is called to check all internal aplication conditions.
	 * 
	 * @param usr - user
	 * @param pass - password
	 * 
	 * @return user record or <code>null</code> if any connection error
	 * @see #autenticationApp(String, String)
	 * 
	 */
	private Record autenticationLDAP(String usr, String pass) {
		// LdapContext ctx = null;
		String purl = SystemVariables.getStringVar("ldap_provider_url");

		String secp = SystemVariables.getStringVar("ldap_security_principals");
		secp = secp.replace("<user>", usr);
		// secp = secp.replace("<password>", pass);

		Record r2 = null;
		try {
			Hashtable env = new Hashtable();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			// env.put(Context.SECURITY_PRINCIPAL, "cn=read-only-admin,dc=example,dc=com");
			env.put(Context.SECURITY_PRINCIPAL, secp);
			env.put(Context.SECURITY_CREDENTIALS, pass);
			env.put(Context.PROVIDER_URL, purl);
			// TODO: extract data form context to update app user file
			// InitialLdapContext ctx = new InitialLdapContext(env, null);
			new InitialLdapContext(env, null);

			r2 = dbAccess.exist("username = '" + usr + "'");
			// LDAP pass but user not in internal tables
			if (r2 == null) {
				showAplicationException(new AplicationException("security.msg12"));
				return null;
			}
			r2 = checkUserParameters(r2);
		} catch (Exception ex) {
			// ex.printStackTrace();
			showAplicationException(new AplicationException("security.msg04", ex.getClass().getSimpleName() + ": "
					+ ex.getMessage()));
		}
		return r2;

	}

	/**
	 * this method check user and password against oracle database manager. if database connection ok,
	 * <code>autenticationApp</code> method is called to check all internal aplication conditions.
	 * 
	 * @param usr - user
	 * @param pass - password
	 * 
	 * @return user record or <code>null</code> if any error
	 * @see #autenticationApp(String, String)
	 * 
	 */
	private Record autenticationOracleDB(String usr, String pass) {
		Record r2 = null;
		try {
			Record cf = ConnectionManager.getAccessTo("t_connections").exist("t_cnname = 'SleOracle'");
			// 1823: override fields value from table values to values stored in jdbc.properties file
			String[] rp = PUserLogIn.getJdbcProperties("jdbc.url");
			cf.setFieldValue("t_cnurl", rp[0]);
			Class.forName((String) cf.getFieldValue("t_cndriver")).newInstance();
			java.sql.Connection con = java.sql.DriverManager.getConnection((String) cf.getFieldValue("t_cnurl"), usr,
					pass);
			con.close();

			// 171221: oracle autentication dont need aditional parameters verification
			r2 = dbAccess.exist("username = '" + usr + "'");
			// 180405: if user dont exist show error
			if (r2 == null) {
				showAplicationExceptionMsg("security.msg09");
				return null;
			} else {
				r2 = checkUserParameters(r2);
			}
		} catch (Exception e) {
			showAplicationException(new AplicationException("security.msg04", e.getMessage()));
		}
		return r2;
	}

	/**
	 * Check aditional parameters that must be checked in order to sucsefully login into the system.
	 * 
	 * @param r2 - the user trying login
	 * 
	 * @return <code>Record</code> if user can login or <code>null</code> otherwise.
	 */
	private Record checkUserParameters(Record r2) {

		// verify inactivity field
		long curd = System.currentTimeMillis();
		long usrd = ((Date) r2.getFieldValue("inactive_since")).getTime();
		if (usrd > 0 && (curd > usrd)) {
			showAplicationExceptionMsg("security.msg08");
			return null;
		}
		return r2;
	}

	/**
	 * this method check user and password against internal application file. if all conditions are ok, return the user
	 * record found in the application user file.
	 * 
	 * @param usr - user
	 * @param pass - password
	 * 
	 * @return user record or <code>null</code> if any connection error
	 * 
	 */
	private Record autenticationApp(String usr, String pass) {
		Record r2 = dbAccess.exist("username = '" + usr + "'");
		if (r2 != null) {
			// check num_logins
			if (t_usmax_attemps < 0) {
				t_usmax_attemps = (Integer) passPolicy.getFieldValue("MAX_ATTEMPS");
			}

			// check user inactive date
			if (checkUserParameters(r2) == null) {
				return null;
			}

			String op = (String) r2.getFieldValue("password");
			String np = TStringUtils.getDigestString(pass);

			// if stored pass if 6 char long, is a otp and change msg.
			if (op.length() == 6) {
				if (!(pass.equals(op) && TStringUtils.verifyOneTimePassword(usr, op))) {
					showAplicationExceptionMsg("security.msg03");
				}
			} else {
				if (!op.equals(np)) {
					showAplicationExceptionMsg("security.msg10");
				}
			}

			if (isShowingError()) {
				t_usmax_attemps--;
				// Si se alcanza numero maximo de intentos, se desabilita el usuario
				if (t_usmax_attemps == 0) {
					r2.setFieldValue("INACTIVE_SINCE", new Date());
					dbAccess.update(r2);
					showAplicationExceptionMsg("security.msg11");
				}
				return null;
			}
			// all check
			return r2;
		} else {
			showAplicationExceptionMsg("security.msg09");
			return null;
		}
	}

	/**
	 * Retrive the values stored in <code>jdbc.properties</code> file.
	 * <p>
	 * If any error is found try reaching the file or if any parameter is missing , this method log the exception, Show
	 * the exception message and finish the applicacion. (because its relation with security)
	 * 
	 * @param pn - list of properties
	 * @return array with property value in the same order as requested
	 */
	public static String[] getJdbcProperties(String... pn) {
		String[] rs = null;
		SystemLog.info("Looking for parameters in externa file.");
		try {
			Properties cfgp = new Properties();
			cfgp.load(new FileInputStream(TResourceUtils.getFile("config.properties")));
			String dsf = cfgp.getProperty("datasourceFile");
			Properties dsop = new Properties();
			dsop.load(new FileInputStream(new File(dsf)));
			rs = new String[pn.length];
			for (int i = 0; i < pn.length; i++) {
				String pv = dsop.getProperty(pn[i]);
				if (pv == null) {
					throw new Exception("Parameter " + pn[i] + " not found.");
				}
				rs[i] = pv;
			}
		} catch (Exception e) {
			SystemLog.severe("Error trying to retrive parameters from external property file.");
			SystemLog.logException1(e, true);
			System.exit(-1);
		}
		return rs;
	}
}
