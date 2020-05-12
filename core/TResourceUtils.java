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

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;
import javax.swing.*;
import core.datasource.*;

/**
 * Utils for {@link File} {@link Image} and other external resources
 * 
 */
public class TResourceUtils {

	public static String USER_DIR = System.getProperty("user.dir");
	public static String RESOURCE_PATH = USER_DIR + "/resources/";
	private static Vector<File> Files = new Vector();
	private static String iconPath = USER_DIR + "/resources/images/";
	private static String iconPath_impl = USER_DIR + "/resources/images/impl/";

	/**
	 * crea y retorna un archivo .zip con el arrego de archivos pasados como argumento.
	 * 
	 * @param fls - archivo a empaquetar
	 * @return archivo empaquetado
	 */
	public static File createZipFile(File[] fls) {
		File jarf = null;
		try {
			boolean atleastone = false;
			jarf = File.createTempFile("tmp", ".zip");
			FileOutputStream fos = new FileOutputStream(jarf);
			JarOutputStream jos = new JarOutputStream(fos);
			for (File f : fls) {
				if (f.exists() && f.isFile()) {
					BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
					jos.putNextEntry(new ZipEntry(f.getName()));
					byte[] b = new byte[(int) f.length()];
					for (int x = 0; x < b.length; x++) {
						b[x] = (byte) bis.read();
					}
					jos.write(b);
					atleastone = true;
				}
			}
			if (atleastone) {
				jos.close();
			}
		} catch (Exception e) {
			SystemLog.logException(e);
		}
		return jarf;
	}

	/**
	 * TODO: metodo viejo
	 * 
	 * @param addid
	 * @return
	 */
	public static String findAndFormatAddrees(Integer addid) {
		ServiceRequest addr_req = new ServiceRequest(ServiceRequest.DB_EXIST, "Address", null);
		addr_req.setData("address_id = " + addid);
		ServiceResponse resp = ServiceConnection.sendTransaction(addr_req);
		Record radd = (Record) resp.getData();
		String fa = addid.toString();
		if (radd != null) {
			fa = TResourceUtils.formatAddress(radd);
		}
		return fa;

	}

	/**
	 * encuentra todos los archivos que contengan a <code>subst</code> como parte del nombre empezando en el directorio
	 * <code>dir</code> buscando en todos los directorios contenidos dentro de este
	 * 
	 * @param dir - directorio origen
	 * @param ext - substrng a buscar dentro de nombre
	 */
	public static Vector<File> findFiles(File dir, String subst) {
		//long l = System.currentTimeMillis();
		Vector<File> v = new Vector();
		try {
			Files.clear();
			findFiles1(dir, subst);
			v = new Vector(Files);
		} catch (Exception e) {
			// retorna vector vacio
			SystemLog.logException(e);
		}
		return v;
	}

	/**
	 * Retorna formato estandar para las direcciones largas. Si el campo obligatorio <code>urbanitation</code> esta
	 * vacio, retorna ""
	 * 
	 * @param adr - Registro con direccion a dar formato
	 * @return direccion
	 */
	public static String formatAddress(Record adr) {
		String addrpatt = TStringUtils.getBundleString("address_patt");
		for (int i = 0; i < adr.getFieldCount(); i++) {
			String ap = "<" + adr.getFieldName(i) + ">";
			if (addrpatt.contains(ap)) {
				addrpatt = addrpatt.replace(ap, (String) adr.getFieldValue(i));

			}
		}
		// verifica solo uno de los campos oblicatorios.
		if (((String) adr.getFieldValue("city")).equals("")) {
			addrpatt = "";
		}
		return addrpatt;
	}

	/**
	 * crea y retorna un archivo que se encuentre en la carpeta de recursos y recursos/impl
	 * 
	 * @param fn - nombre del archivo (con extencion)
	 * @return archivo
	 */
	public static File getFile(String fn) {
		File f = null;
		String path = fn.startsWith("/") ? USER_DIR : RESOURCE_PATH;
		f = new File(path + fn);
		return f;
	}

	/**
	 * Localiza el archivo de configuracion de la forma y retorna todos los elementos e este dentro de un
	 * <code>Hashtable</code> cuya clave es el nombre del elementos y el valor es el elemento en si. Para las secciones,
	 * la clave es el identificador de seccion. Los mensajes no estan incluidos
	 * 
	 * ----- pendiente depuracion. colocar en una tabla de elementos las formas ya cargadas de manera que no se repita
	 * la compialacion cada ves que se solicite los elementos de una forma ya cargada -------
	 * 
	 * @param fn - id de la forma
	 * @return - lista con todos los elementos public static Hashtable getFormElements(String fn) { Document doc =
	 *         getXMLDocument(fn + ".xml"); Element root = doc.getRootElement(); Hashtable ht = new Hashtable();
	 *         ht.put(root.getName(), root); List l = root.getChildren(); for (int c = 0; c < l.size(); c++) { Element e
	 *         = (Element) l.get(c); if (e.getName().endsWith("section")) { ht.put(e.getAttributeValue("id"), e); } else
	 *         { ht.put(e.getName(), e); } } return ht; }
	 */

	/**
	 * Retorna la imagen solicitada. si no existe el archivo, retorna <code>null</code>
	 * 
	 * @param in - Nombre del la imagen
	 * @return icono
	 */
	public static ImageIcon getIcon(String in) {

		if (in == null) {
			return null;
		}
		String path = in.startsWith("/") ? USER_DIR : iconPath;

		// intenta png
		String fna = path + in + ".png";
		File f = new File(fna);
		if (f.exists()) {
			return new ImageIcon(fna);
		}
		// intenta gif
		fna = path + in + ".gif";
		f = new File(fna);
		if (f.exists()) {
			return new ImageIcon(fna);
		}
		// impl
		// -----------------------
		path = in.startsWith("/") ? USER_DIR + in : iconPath_impl;
		// intenta png
		fna = iconPath_impl + in + ".png";
		f = new File(fna);
		if (f.exists()) {
			return new ImageIcon(fna);
		}
		// intenta gif
		fna = iconPath_impl + in + ".gif";
		f = new File(fna);
		if (f.exists()) {
			return new ImageIcon(fna);
		}
		return null;
	}

	/**
	 * retorna una instancia de <code>ImageIcon</code> con un tamaño de <code>size*size</code>
	 * 
	 * @param in - nombre del archivo de imagen
	 * @param size - tamaño deseado
	 * @return imagen
	 */
	public static ImageIcon getIcon(String in, int size) {
		ImageIcon ii = getIcon(in);
		if (ii == null) {
			return null;
		}
		Image i = ii.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
		return new ImageIcon(i);
	}

	/**
	 * Retorna el icono pequeño (16*16). si no existe, retorna null
	 * 
	 * TODO: este metodo se debe eliminar el nombre+16. ahora todos los iconos deben ser almacenados en formato 32*32.
	 * eliminar usar getIcon(String in, int size)
	 * 
	 * @param in - Nombre del la imagen
	 * @return icono
	 */
	public static ImageIcon getSmallIcon(String in) {
		// intenta solo nombre
		ImageIcon ii = getIcon(in);
		if (ii != null) {
			return getIcon(in, 16);
		} else {
			// try old names
			return getIcon(in + "16");
		}
	}

	/**
	 * retorna <code>URL</code> de un archivo que se encuentre en la carpeta de docuementos. Si la instancia de
	 * <code>new File(qn)</code> no es un archivo, se adiciona la extencion ".html" y se intenta de nuevo
	 * 
	 * @param qn - nombre calificado con extencion o sin ella para qn + ".html"
	 * @return URL
	 */
	public static URL getURL(String qn) {
		URL u = null;
		try {
			File f = getFile(qn);
			if (!f.isFile()) {
				qn = qn + ".html";
				f = getFile(qn);
			}
			u = f.toURI().toURL();
		} catch (Exception e) {

		}
		return u;
	}

	/**
	 * Retorna una instancia de <code>Document</code> construido a partir del archivo xml pasado como argumento. el
	 * archivo se encuentra en la carpeta de recursos
	 * 
	 * @param qf - nombre de archivo (path/nombre.ext)
	 * @return documento public static Document getXMLDocument(String qn) { File f = new File(dir + qn); Document doc =
	 *         null; try { SAXBuilder sb = new SAXBuilder(); doc = sb.build(f); } catch (Exception e) {
	 *         SystemLog.logException(e); } return doc; }
	 */

	/**
	 * metodo encargado de verificar e instalar posibles ptf. toda entrada exepto "producer" sera considerada como
	 * archivo de ptf
	 * 
	 * Si este metodo detecta ptf disponibles: - se localiza el archivo manifest.txt y se presenta su contenido - se
	 * instalan los ptf durante la instalacion - se finaliza la aplicacion.
	 * 
	 * @param qjn - nombre calificado del archivo .jar.
	 * @throws IOException
	 */
	public static void installPTF(ResourceBundle man) {
		try {
			// direccion del archivo
			String jf = TStringUtils.getBundleString("ptf_dir") + man.getString("ptf_jar");

			// descargar en temporal
			// TODO: barra de progreso
			URL c_ptf = new URL(jf);
			BufferedInputStream bis = new BufferedInputStream(c_ptf.openStream());
			byte[] data = new byte[bis.available()];
			bis.read(data);
			File f = new File(System.getProperty("java.io.tmpdir") + "ptf.jar");
			f.createNewFile();
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(data);
			fos.close();

			JarFile jarf = new JarFile(f);
			Enumeration e = jarf.entries();
			boolean fl = false;
			while (e.hasMoreElements()) {
				ZipEntry ze = (ZipEntry) e.nextElement();
				String zefn = ze.getName();
				// instalacion
				InputStream is = jarf.getInputStream(ze);
				byte[] b = new byte[(int) ze.getSize()];
				for (int x = 0; x < b.length; x++) {
					b[x] = (byte) is.read();
				}
				String sfn = System.getProperty("user.dir") + "/" + zefn;
				File fn = new File(sfn);
				if (fn.exists()) {
					fn.delete();
				}
				// crea nuevo por error cuando existe y se borra
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(sfn)));
				bos.write(b);
				bos.close();
				is.close();
			}
			if (fl) {
				// TODO: restart
			}
		} catch (Exception e) {
			SystemLog.logException(e);
		}
	}

	/**
	 * retorna el nombre de la clase del objeto pasado como argumento
	 * 
	 * @param obj - objeto
	 * @return nombre de clase
	 */
	public static String getClassName(Object obj) {
		String cn1[];
		if (obj instanceof Class) {
			cn1 = ((Class) obj).getName().split("[.]");
		} else {
			cn1 = obj.getClass().getName().split("[.]");
		}
		return cn1[cn1.length - 1];
	}

	/**
	 * retorna arreglo de los nombres de las clases que se encuentran en el paquete pasado como argumento
	 * 
	 * @param pn - nombre de paquete. Ej: gui.impl
	 * @return arreglo con los nombres de las clases dentro del paquete
	 */
	public static String[] getClassFrom(String pn) {
		File dir = new File(System.getProperty("user.dir") + "/" + pn.replace('.', '/'));
		File[] clsf = dir.listFiles();
		// dir whit 0 files
		if (clsf == null) {
			return new String[0];
		}
		Vector<String> v = new Vector<String>();
		for (int k = 0; k < clsf.length; k++) {
			String cn = clsf[k].getName();
			if (cn.endsWith(".class")) {
				v.add(pn + cn.replace(".class", ""));
			}
		}
		return v.toArray(new String[v.size()]);
	}

	/**
	 * este metodo localiza contruye y retorna el archivo pasado como argumento el cual debe contener el nombre del
	 * manifiesto para un ptf
	 * 
	 * @param man - id de resource bundle para nombre
	 * @return manifiesto o null si no existe ptf disponible
	 */
	public static ResourceBundle lookUpPTFManifes(String mn) {
		ResourceBundle ptfbundle = null;
		try {
			URL c_ptf = new URL(TStringUtils.getBundleString("ptf_dir") + TStringUtils.getBundleString(mn));
			BufferedInputStream bis = new BufferedInputStream(c_ptf.openStream());
			ptfbundle = new PropertyResourceBundle(bis);

			// TODO: verificar si ptf debe ser cargado

		} catch (Exception e) {
			SystemLog.logException(e);
		}
		return ptfbundle;
	}

	/**
	 * envia el mensaje <code>txt</code>de correo electronico con el archivo adjunto pasado como argumento a la
	 * direccion de correo electronico por defecto
	 * 
	 * @param txt - id de texto de mensaje
	 * @param attach - archivo adjunto public static void sendEmail(String txtid, File attach) { try { Properties props
	 *        = System.getProperties(); props.put("mail.transport.protocol", "smtp"); props.put("mail.smtp.host",
	 *        "mail.cantv.net"); javax.mail.Session session = javax.mail.Session.getDefaultInstance(props, null); //
	 *        session.setDebug(true); MimeMessage message = new MimeMessage(session); message.setFrom(new
	 *        InternetAddress(ConstantUtilities.getBundleString("emailfrom"), ConstantUtilities
	 *        .getBundleString("fromname") + " - El_DE")); message.setRecipient( Message.RecipientType.TO, new
	 *        InternetAddress(ConstantUtilities.getBundleString("emailto"), ConstantUtilities
	 *        .getBundleString("toname"))); message.setSubject(ConstantUtilities.getBundleString(txtid));
	 *        message.setSentDate(new Date());
	 * 
	 *        MimeMultipart mmp = new MimeMultipart(); // mensaje MimeBodyPart mbp = new MimeBodyPart();
	 *        mbp.setText(ConstantUtilities.getBundleString(txtid)); mmp.addBodyPart(mbp);
	 * 
	 *        // archivo log mbp = new MimeBodyPart(); mbp.attachFile(attach); mmp.addBodyPart(mbp);
	 * 
	 *        message.setContent(mmp); message.saveChanges();
	 * 
	 *        Transport.send(message); Thread.sleep(10000); } catch (Exception e) { SystemLog.logException(e); } }
	 */

	/**
	 * localiza todos los archivos que contengan la secuencia de caracteres <code>subst</code> empezando en el
	 * directorio <code>dir</code> llenando el vector <code>Files</code>
	 * 
	 * @param dir - directorio
	 * @param ext - substring a buscar dentro del nombre de archivo
	 */
	private static void findFiles1(File dir, String subst) throws Exception {
		File[] fl = dir.listFiles();
		for (File f : fl) {
			if (f.isDirectory()) {
				findFiles1(f, subst);
			} else {
				// if (f.getName().endsWith(ext)) {
				if (f.getName().contains(subst)) {
					Files.add(f);
				}
			}
		}
	}
}
