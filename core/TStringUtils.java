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

import java.io.*;
import java.math.*;
import java.security.*;
import java.text.*;
import java.util.*;

import javax.crypto.*;
import javax.crypto.spec.*;

import core.datasource.*;

/**
 * Utils class for String and {@link TEntry} Manipulations
 * 
 */
public class TStringUtils {

	public final static java.sql.Date ZERODATE = java.sql.Date.valueOf("1899-12-31");

	public static TreeMap<String, String> constants;
	private static SimpleDateFormat dateFormat = new SimpleDateFormat();
	private static Random random = new Random();
	private static NumberFormat formatter;

	/**
	 * init enviorement
	 * 
	 * @throws Exception
	 */
	public static void init() throws Exception {
		constants = new TreeMap();
		Vector<File> files = TResourceUtils.findFiles(new File(TResourceUtils.RESOURCE_PATH), ".properties");
		for (File f : files) {
			Properties p = new Properties();
			p.load(new FileInputStream(f));
			Enumeration kls = p.keys();
			while (kls.hasMoreElements()) {
				String k = (String) kls.nextElement();
				constants.put(k, p.get(k).toString());
			}
		}
		formatter = getDecimalFormat();
	}

	/**
	 * remueve de la cadena de caracateres pasada como argumento todas los tag html
	 * 
	 * @param sht - String con tag html
	 * @return String sin tags
	 */
	public static String removeHTMLtags(String sht) {
		sht = sht.replaceAll("</*\\w*>", "");
		sht = sht.replaceAll("&lt", "<");
		sht = sht.replaceAll("&gt", ">");
		return sht;
	}

	/**
	 * inserta el atributo<br>
	 * dentro de una secuencia de caracteres cada vez que se consiga un espacio en blanco despues de una longitud
	 * determinada Ej.: getInsertedBR("Arnaldo Fuentes", 3) retornara "Arnaldo<br>
	 * Fuentes"
	 * 
	 * NOTA: este metodo adiciona <html></html> si estas no se encuentran al inicio/final de la cadena original
	 * 
	 * @param str - secuancia
	 * @param len - longitud
	 * @return secoencia con atributo insertado
	 */
	public static String getInsertedBR(String str, int len) {
		StringBuffer stt1 = new StringBuffer(str);
		int c = 0;
		boolean f = false;
		for (int x = 0; x < stt1.length(); x++) {
			f = (c > len && stt1.charAt(x) == ' ');
			if (f) {
				stt1.insert(x, "<br>");
				c = 0;
			} else {
				c++;
			}
		}
		String bris = stt1.toString();
		bris = bris.startsWith("<html>") ? bris : "<html>" + bris;
		bris = bris.endsWith("</html>") ? bris : bris + "</html>";
		return bris;
	}

	/**
	 * busca y retorna el grupo de constantes que comienzen con el parametro <code>ty</code>. este metodo intenta
	 * primero localizar la lista de constantes en los archivos <code>.properties</code> cargados durante la
	 * inicializacion. si no se encuentran alli, intenta en el archive de referencias en la base de datos principal
	 * 
	 * @param ty - tipo de constantes
	 * @return arreglo de constantes
	 */
	public static TEntry[] getTEntryGroup(String ty) {
		Vector lst = new Vector();
		// boolean find = false;
		// archivos .properties
		Vector kls = new Vector(constants.keySet());
		for (int i = 0; i < kls.size(); i++) {
			String k = (String) kls.elementAt(i);
			if (k.startsWith(ty)) {
				String[] kv = constants.get(k).split(";");
				if (kv.length > 1) {
					// find = true;
					lst.add(new TEntry(kv[0], kv[1]));
				}
			}
		}
		// if (find) {
		TEntry[] lte = (TEntry[]) lst.toArray(new TEntry[lst.size()]);
		return lte;
		// } else {
		// return getTEntryGroupFromDB(ty);
		// }
	}

	public static TEntry getTEntry(String tid) {
		TEntry[] te = getTEntryGroup(tid);
		return (te == null) ? new TEntry(tid, tid) : te[0];
	}

	/**
	 * return {@link TEntry} found in .properties files. if not found there, return {@link TEntry#TEntry(ke, ke)}
	 * 
	 * @param ke - key to look for
	 * @return {@link TEntry}
	 */
	public static TEntry getTEntryByKey(String ke) {
		// .properties
		Vector kls = new Vector(constants.keySet());
		TEntry cnt = null;
		for (int i = 0; i < kls.size(); i++) {
			String tx = constants.get(kls.elementAt(i));
			String[] k_v = tx.split(";");
			if (k_v[0].equals(ke) && k_v.length > 1) {
				cnt = new TEntry(k_v[0], k_v[1]);
			}
		}
		return (cnt == null) ? new TEntry(ke, ke) : cnt;
	}

	/**
	 * retorna instancia de <code>DecimalFormat</code> con valores basicos establecidos
	 * 
	 * @return instancia de DecimalFormat
	 */
	public static DecimalFormat getDecimalFormat() {
		DecimalFormat decF = new DecimalFormat("#0.0#;-#0.0#");
		decF.setMinimumFractionDigits(2);
		decF.setMaximumFractionDigits(2);
		return decF;
	}

	/**
	 * return a hex representation of digested string that has been cypher by MD5 algorithm.
	 * 
	 * @param srcs - source string to digest
	 * @return digested string in hex
	 */
	public static String getDigestString(String srcs) {
		String dmsg = "";
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			// terry in numeric keys 53446
			messageDigest.update((srcs + "Terry").getBytes());
			byte[] bytes = messageDigest.digest();
			BigInteger bi = new BigInteger(1, bytes);
			dmsg = String.format("%0" + (bytes.length << 1) + "X", bi);
		} catch (Exception e) {
			SystemLog.logException1(e, true);
		}
		return dmsg;
	}

	/**
	 * crea una instanica de <code>Date</code> que representa el monemto actual y lo retorna como una cadena de
	 * caracteres segun el formato pasado como argumento
	 * 
	 * @param f formato <code>(ver SimpleDateFormat)</code>
	 * @return
	 */
	public static String getStringDate(String f) {
		return getStringDate(new Date(), f);
	}

	/**
	 * return a {@link Vector} of {@link TEntry} converted form string of properies
	 * 
	 * @param prpl - String of properties in standar format: key;value;key;value...
	 * 
	 * @return vector of tentry
	 */
	public static Vector<TEntry> getPropertys(String prpl) {
		Vector<TEntry> prpv = new Vector();
		if (!prpl.equals("")) {
			String[] kv = prpl.split(";");
			for (int i = 0; i < kv.length; i = i + 2) {
				// 171218: try to correct error in incoming string when key;value pair are not even
				String v = (i + 1) > kv.length ? "" : kv[i + 1];
				prpv.add(new TEntry(kv[i], v));
			}
		}
		return prpv;
	}

	/**
	 * retorna la instancia de <code>Date</code> pasada como argumento en una String con el formato
	 * <code>f (ver SimpleDateFormat)</code>
	 * 
	 * @param d - instancia de <code>Date</code>
	 * @param f - formato
	 * @return String
	 */
	public static String getStringDate(Date d, String f) {
		dateFormat.applyPattern(f);
		return dateFormat.format(d);
	}

	/**
	 * retorna <code>String</code> designada dentro de <code>ResourceBundle</code> cliente para el identificador pasado
	 * como argumento. si no se encuetra una cadena de caracteres asociadas al identificador se debuelve el
	 * identificador
	 * 
	 * @param id - id
	 * @return String
	 */
	public static String getBundleString(String id) {
		String el = constants.get(id); // intenta cadena original
		// intenta minusculas (campos de DB)
		el = (el == null) ? el = constants.get(id.toLowerCase()) : el;
		return (el == null) ? id : el;
	}

	/**
	 * compara las 2 instancias de <code>Date</code> pasadas como argumento y retorna un valor < 0 si d1 < d2, 0 si d1 =
	 * d2 o un valor > 0 si d1 > d2. La comparacion es establecida con la presicion del argumento <code>fmt</code>
	 * 
	 * @param d1 - instancia de <code>Date</code> menor
	 * @param d2 - instancia de <code>Date</code> mayor
	 * @param fmt - precision o formato (ver SimpleDateFormat)
	 * @return valor segun comparacion
	 */
	public static int compare(Date d1, Date d2, String fmt) {
		dateFormat.applyPattern(fmt);
		String d_1 = dateFormat.format(d1);
		String d_2 = dateFormat.format(d2);
		return d_1.compareTo(d_2);
	}

	/**
	 * Este metodo retorna <code>true</code> si <code>mid</code> se encuentra entre las dos fechas <code>d1 y d2</code>
	 * es decir <code>d1 > mid < d2</code>
	 * 
	 * @param d1 - fecha menor
	 * @param mid - fecha a evaluar
	 * @param d2 - fecha mayor
	 * @return true is se verifica <code>d1 > mid < d2</code>
	 */
	public static boolean between(Date d1, Date mid, Date d2, String f) {
		int i1 = compare(d1, mid, f);
		int i2 = compare(mid, d2, f);
		return i1 < 0 && i2 < 0;
	}

	/**
	 * retorna un indentificador unico en formato estandar xx-xxx-xxx
	 * 
	 * @return <code>String</code> con identificador unico
	 */
	public static String getUniqueID() {
		String cern = "000";
		String f1 = cern + Integer.toHexString(random.nextInt()).toUpperCase();
		String f2 = cern + Integer.toHexString(random.nextInt()).toUpperCase();
		String f3 = cern + Integer.toHexString(random.nextInt()).toUpperCase();
		String cern1 = f1.substring(f1.length() - 2) + "-" + f2.substring(f2.length() - 3) + "-"
				+ f3.substring(f3.length() - 3);
		// System.out.println(cern1);
		return cern1;
	}

	/**
	 * generate a OTP for the given user. the password lenght MUST BE 6 char long (this is necesary because this system
	 * use the length of store passgord to determine if that field value is a OTP.
	 * 
	 * @see #getPasscodeGenerator(String)
	 * @see #verifyOneTimePassword(String, String)
	 * @param usid - user to generate the otp
	 * 
	 * @return password to store
	 */
	public static String getOneTimePassword(String uid) {
		String nextp = null;
		try {
			nextp = getPasscodeGenerator(uid).generateTimeoutCode();
		} catch (Exception e) {

		}
		return nextp;
	}
	/**
	 * create a standar {@link PasscodeGenerator}
	 * 
	 * @param uid - uid to generate/verfy OTP
	 * 
	 * @return PasscodeGenerator
	 */
	private static PasscodeGenerator getPasscodeGenerator(String uid) {
		try {
			int to = SystemVariables.getintVar("OTPTimeout");
			Mac mac = Mac.getInstance("HMACSHA1");
			mac.init(new SecretKeySpec(uid.toLowerCase().getBytes(), ""));
			PasscodeGenerator pcg = new PasscodeGenerator(mac, 6, to * 60);
			return pcg;
		} catch (Exception e) {

		}
		return null;
	}

	/**
	 * verify if the given OTP is still valid for the user pass as argument.
	 * 
	 * @see #getPasscodeGenerator(String)
	 * @see #getOneTimePassword(String)
	 * 
	 * @param uid - user to verify
	 * @param p - OTP to check
	 * 
	 * @return <code>true</code> if otp still valid
	 */
	public static boolean verifyOneTimePassword(String uid, String p) {
		boolean ok = false;
		try {
			ok = getPasscodeGenerator(uid).verifyTimeoutCode(p, 0, 0);
		} catch (Exception e) {

		}
		return ok;
	}

	/**
	 * Parse the properties stored in String <code>prplis</code> and append in <code>prps</code>
	 * 
	 * @param pl - String of properties stores in standar format (key;value;key;value...)
	 * @param prps - {@code Properties} to append the parsed string
	 */
	public static void parseProperties(String pl, Properties prps) {
		if (!pl.equals("")) {
			String[] kv = pl.split(";");
			for (int j = 0; j < kv.length; j = j + 2) {
				prps.put(kv[j], kv[j + 1]);
			}
		}
	}

	/**
	 * retorna texto identificador de aplicacion y version. generalmente usado par grabar en archivos externos
	 * 
	 * @return String en formato <b>Clio Version: 1.36 Update: 0</b> o similar
	 */
	public static String getAboutAppShort() {
		return TStringUtils.getBundleString("about.app.id") + " " + TStringUtils.getBundleString("about.version") + " "
				+ SystemVariables.getStringVar("versionID") + " " + TStringUtils.getBundleString("about.update") + " "
				+ SystemVariables.getStringVar("updateID");
	}

	/**
	 * retorna una cadna de caracteres que convierte el tama;o en bytes pasado como argumento a Kb o Mb
	 * 
	 * @param s - tama;o en bytes
	 * @return tama;o en Kb o Mb
	 */
	public static String formatSize(double s) {
		double siz = 0;
		String suf = " Kb";
		if (s < 1024000) {
			siz = s / 1024;
			suf = " Kb";
		} else {
			siz = s / 1024000;
			suf = " Mb";
		}
		return formatter.format(siz) + suf;
	}

	/**
	 * este metodo verifica que el texto txt este escrito segun el patron pasado como argumento
	 * 
	 * @param patt - plantilla que indentifica el formato en que el texto debe estar descrito
	 * @param txt - valor a compara contra la plantiall
	 * 
	 * @return id de mensaje si el valor txt no esta escrito segun el patron
	 */
	public static AplicationException validatePatt(String patt, String txt) {
		if (txt.length() > 0) {
			AplicationException ape = new AplicationException("ui.msg21");
			ape.setMessage(MessageFormat.format(ape.getMessage(), patt));

			// datos > que patron
			if (txt.length() > patt.length()) {
				return ape;
			}
			// verifica patron: mientras el texto escrito este correcto, no muestra mensaje
			for (int i = 0; i < txt.length(); i++) {
				if (patt.charAt(i) == '.' && !(txt.charAt(i) == '.')) {
					return ape;
				}
				if (patt.charAt(i) == '#' && !(Character.isDigit(txt.charAt(i)))) {
					return ape;
				}
			}
			// verifica longitud solo cuando el patron esta correcto
			if (txt.length() == patt.length()) {
				// showMessage("ui.msg21");
				// return;
			}
		}
		return null;
	}

	private static String UNIDADES[][] = new String[5][];
	static {

		UNIDADES[0] = new String[]{"cero ", "un ", "dos ", "tres ", "cuatro ", "cinco ", "seis ", "siete ", "ocho ",
				"nueve ", "diez ", "once ", "doce ", "trece ", "catorce ", "quince ", "diecis�is ", "Diecisiete ",
				"Dieciocho ", "Diecinueve "};
		UNIDADES[1] = new String[]{"veinte ", "treinta ", "cuarenta ", "cincuenta ", "sesenta ", "setenta ",
				"ochenta ", "noventa "};
		UNIDADES[2] = new String[]{"cien ", "ciento ", "doscientos ", "trescientos ", "cuatrocientos ", "quinientos ",
				"seiscientos ", "setecientos ", "ochocientos ", "novecientos "};
		UNIDADES[3] = new String[]{"mil ", "millon ", "billon "};
	}

	/**
	 * convierte el numero pasado como argumento a texto. Ej: 1234.23 retorna mil docientos treinta y 4 con 23
	 * 
	 * @param d - Numero a convertir
	 * @return texto
	 */
	public static String converToText(Double dnum) {
		String strVal = String.valueOf(dnum.intValue());
		int intVal = Integer.valueOf(strVal);
		String rtnStr = "";

		// unidades
		if (intVal < 19) {
			rtnStr = UNIDADES[0][intVal];
		}
		// decenas
		if (intVal >= 20 && intVal < 99) {
			int d = intVal / 10;
			int u = intVal % 10;
			rtnStr = UNIDADES[1][d] + ((u == 0) ? "" : UNIDADES[0][u]);
		}
		// centenas
		if (intVal >= 100 && intVal < 999) {
			int c = (int) (intVal % 100);
			rtnStr = UNIDADES[2][c] + (c == 0 ? "" : converToText((double) intVal));

		}
		// miles
		return rtnStr;
	}
	/**
	 * Monto escrito
	 * 
	 * @param amount - valor o monto
	 * @return secuencia de caracteres del valor o monto pasado como argumento
	 */
	public static String amountWritten(double amount) {
		HashMap millions = new HashMap();

		millions.put(Integer.valueOf(1), "");
		millions.put(Integer.valueOf(2), "MILLONES");
		millions.put(Integer.valueOf(3), "BILLONES");
		millions.put(Integer.valueOf(4), "TRILLONES");
		millions.put(Integer.valueOf(5), "CUATRILLONES");
		millions.put(Integer.valueOf(6), "QUINTILLONES");
		millions.put(Integer.valueOf(7), "SEXTILLONES");
		millions.put(Integer.valueOf(8), "SEPTILLONES");
		millions.put(Integer.valueOf(9), "OCTILLONES");
		millions.put(Integer.valueOf(10), "NONILLONES");
		millions.put(Integer.valueOf(11), "DECILLONES");
		millions.put(Integer.valueOf(12), "UNDECILLONES");
		millions.put(Integer.valueOf(13), "DUODECILONES");
		millions.put(Integer.valueOf(14), "TRIDECILLONES");
		millions.put(Integer.valueOf(15), "CUATURODECILLONES");
		millions.put(Integer.valueOf(16), "QUINDECILLONES");
		millions.put(Integer.valueOf(17), "SEXDECILLONES");
		millions.put(Integer.valueOf(18), "SEPTENDECILLONES");
		millions.put(Integer.valueOf(19), "OCTODECILLONES");
		millions.put(Integer.valueOf(20), "NOVENDECILLONES");
		millions.put(Integer.valueOf(21), "VIGENTILLONES");
		String millionWord = "";

		BigDecimal bdMount = new BigDecimal(String.valueOf(amount));
		bdMount.setScale(2, 2);
		BigInteger iPart = bdMount.toBigInteger();
		BigInteger iDecPart = bdMount.multiply(new BigDecimal(100.0D)).toBigInteger();
		long intPart = iPart.longValue();
		long decPart = iDecPart.longValue() - iPart.longValue() * 100L;
		String written = "";
		long quotient = intPart / 1000000L;
		long remainder = intPart % 1000000L;
		int groupQ = 1;
		// int groupR = 0;
		// long remQuo = intPart;
		while (quotient > 0L) {
			groupQ++;
			if (remainder == 0L) {
				// groupR++;
				// remQuo = quotient;
			}

			quotient /= 1000000L;
			remainder = quotient % 1000000L;
		}

		if (amount == 0.0D) {
			written = "CERO";
		} else {
			quotient = intPart;
			for (int i = 1; i <= groupQ; i++) {
				remainder = quotient % 1000000L;
				quotient /= 1000000L;
				millionWord = (String) millions.get(Integer.valueOf(i));
				if (remainder == 1L) {
					millionWord = millionWord.substring(0, millionWord.indexOf("ES"));
				}
				if ((remainder == 0L) && (quotient > 0L))
					millionWord = "";
				written = amountWritten(remainder, 0) + millionWord + " " + written;
			}
		}
		// return written.trim() + " con " + lpad(String.valueOf(decPart), '0', 2) + "/100";
		String lp = "0" + decPart;
		int l = lp.length();
		return written.trim() + " con " + lp.substring(l - 2, l) + "/100";
		// return written.trim() + " con " + lpad(String.valueOf(decPart), '0', 2) + "/100";
	}

	/**
	 * utilitario para <code>amountWritten(double)</code>
	 * 
	 * @param amount
	 * @param unit
	 * @return
	 */
	private static String ordinalToWord(long amount, int unit) {
		HashMap hundreds = new HashMap();
		hundreds.put(Integer.valueOf(1), "UN");
		hundreds.put(Integer.valueOf(2), "DOS");
		hundreds.put(Integer.valueOf(3), "TRES");
		hundreds.put(Integer.valueOf(4), "CUATRO");
		hundreds.put(Integer.valueOf(5), "CINCO");
		hundreds.put(Integer.valueOf(6), "SEIS");
		hundreds.put(Integer.valueOf(7), "SIETE");
		hundreds.put(Integer.valueOf(8), "OCHO");
		hundreds.put(Integer.valueOf(9), "NUEVE");
		hundreds.put(Integer.valueOf(10), "DIEZ");
		hundreds.put(Integer.valueOf(11), "ONCE");
		hundreds.put(Integer.valueOf(12), "DOCE");
		hundreds.put(Integer.valueOf(13), "TRECE");
		hundreds.put(Integer.valueOf(14), "CATORCE");
		hundreds.put(Integer.valueOf(15), "QUINCE");
		hundreds.put(Integer.valueOf(16), "DIECISEIS");
		hundreds.put(Integer.valueOf(17), "DIECISIETE");
		hundreds.put(Integer.valueOf(18), "DIECIOCHO");
		hundreds.put(Integer.valueOf(19), "DIECINUEVE");
		hundreds.put(Integer.valueOf(20), "VEINTE");
		hundreds.put(Integer.valueOf(21), "VEINTIUN");
		hundreds.put(Integer.valueOf(22), "VEINTIDOS");
		hundreds.put(Integer.valueOf(23), "VEINTITRES");
		hundreds.put(Integer.valueOf(24), "VEINTICUATRO");
		hundreds.put(Integer.valueOf(25), "VEINTICINCO");
		hundreds.put(Integer.valueOf(26), "VEINTISEIS");
		hundreds.put(Integer.valueOf(27), "VEINTISIETE");
		hundreds.put(Integer.valueOf(28), "VEINTIOCHO");
		hundreds.put(Integer.valueOf(29), "VEINTINUEVE");
		hundreds.put(Integer.valueOf(30), "TREINTA");
		hundreds.put(Integer.valueOf(40), "CUARENTA");
		hundreds.put(Integer.valueOf(50), "CINCUENTA");
		hundreds.put(Integer.valueOf(60), "SESENTA");
		hundreds.put(Integer.valueOf(70), "SETENTA");
		hundreds.put(Integer.valueOf(80), "OCHENTA");
		hundreds.put(Integer.valueOf(90), "NOVENTA");
		hundreds.put(Integer.valueOf(100), "CIENTO");
		hundreds.put(Integer.valueOf(200), "DOSCIENTOS");
		hundreds.put(Integer.valueOf(300), "TRESCIENTOS");
		hundreds.put(Integer.valueOf(400), "CUATROCIENTOS");
		hundreds.put(Integer.valueOf(500), "QUINIENTOS");
		hundreds.put(Integer.valueOf(600), "SEISCIENTOS");
		hundreds.put(Integer.valueOf(700), "SETECIENTOS");
		hundreds.put(Integer.valueOf(800), "OCHOCIENTOS");
		hundreds.put(Integer.valueOf(900), "NOVECIENTOS");
		String nWord = "";
		String millionWord = "";

		if (amount != 0L) {
			int hundred = (int) amount / 100;
			int tens = (int) amount % 100;
			if (hundred != 0) {
				nWord = (String) hundreds.get(Integer.valueOf(hundred * 100));
				if ((hundred == 1) && (tens == 0)) {
					nWord = "CIEN";
				}
			}
			if (tens != 0) {
				if (tens <= 29) {
					nWord = nWord + " " + (String) hundreds.get(Integer.valueOf(tens));
				} else {
					int ones = tens % 10;
					tens /= 10;
					nWord = nWord + " " + (String) hundreds.get(Integer.valueOf(tens * 10));
					if (ones != 0) {
						nWord = nWord + " Y " + (String) hundreds.get(Integer.valueOf(ones));
					}
				}
			}
		}
		if ((unit != 0) && (unit % 2 != 0) && (amount != 0L)) {
			millionWord = "MIL ";
		}

		return nWord + " " + millionWord;
	}

	/**
	 * utilitario para <code>amountWritten(double)</code>
	 * 
	 * @param amount
	 * @param unit
	 * @return
	 */
	private static String amountWritten(long amount, int unit) {
		String written = "";
		long intPart = amount;
		if (intPart != 0L) {
			long quotient = intPart / 1000L;
			long remainder = intPart % 1000L;
			if (quotient != 0L)
				written = ordinalToWord(quotient, 1);
			written = written + ordinalToWord(remainder, 0);
		}
		return written + " ";
	}

	/**
	 * replace the given variables in patt with the corresponding field value content in <code>rcd</code>.
	 * <p>
	 * If after the replace, remains variables names, those are marked as <code>_var_name</code>
	 * 
	 * @param patt - name_pattern. variables are all fieldname who star with $ sign
	 * @param rcd - values to replace
	 * 
	 * @return Formatted string
	 */
	public static String format(String patt, Record rcd) {
		String res = patt;
		for (int c = 0; c < rcd.getFieldCount(); c++) {
			res = res.replace("$" + rcd.getFieldName(c), rcd.getFieldValue(c).toString());
		}
		return res.replaceAll("[$]", "_");
	}

	/**
	 * Utility method to return all fields id, name pairs in a Hashtable acording to the service request. Sometimes is
	 * hard to deside whether to look for field despription. this method build the list acording to the servicerequest
	 * type.
	 * 
	 * @param sr - servicereques
	 * 
	 * @return Hashtable with the id, description pair
	 */
	public static Hashtable<String, String> getFieldsDescriptions(ServiceRequest sr) {
		Hashtable<String, String> fldstxt = new Hashtable<String, String>();
		if (sr.getName().equals(ServiceRequest.CLIENT_GENERATED_LIST)) {
			fldstxt = (Hashtable<String, String>) sr.getParameter(ServiceResponse.RECORD_FIELDS_DESPRIPTION);
		} else {
			Record m = ConnectionManager.getAccessTo(sr.getTableName()).getModel();
			for (int c = 0; c < m.getFieldCount(); c++) {
				fldstxt.put(m.getFieldName(c), (String) getBundleString(m.getFieldName(c)));
			}
		}
		return fldstxt;
	}
}
