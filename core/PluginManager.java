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
import java.util.*;



public class PluginManager {

	private static Hashtable<String, Properties> installProperties;
	private static Hashtable<String, Plugin> installPlugin;

	public static void init() {
		try {
			installProperties = new Hashtable<String, Properties>();
			installPlugin = new Hashtable<String, Plugin>();

			FileInputStream fis;
			String pip = TResourceUtils.USER_DIR + "/plugin/";
			// lookup for all plugin properties files
			Vector<File> plugprp = TResourceUtils.findFiles(new File(pip), ".properties");
			for (File file : plugprp) {
				Properties prps = new Properties();
				fis = new FileInputStream(file);
				prps.load(fis);
				// clear property keys and values form white space
				//TODO: create new property object to convert form string to java object
				
//				Properties prps = new Properties()
				
				String pic = prps.getProperty("plugin.class");
				// check if plugin is active by property file
				String pa = prps.getProperty("plugin.active");
				if (pa == null || pa.equals("false")) {
					SystemLog.warning("Plugin " + pic + " not installed because property plugin.active = false");
					continue;
				}
				// try install plugin
				try {
					if (pic != null) {
						String p = file.getPath();
						String p1 = p.substring(pip.length(), p.lastIndexOf("\\"));
						// insert pluginPath property
						prps.put("pluginPath", pip + p1 + "/");
						String cn = "plugin." + p1 + "." + pic;
						Object o = Class.forName(cn).newInstance();
						if (o instanceof Plugin) {
							SystemLog.info("Plugin " + pic + " found");
							Plugin plg = (Plugin) o;
							plg.startPlugin(prps);
							SystemLog.info("Plugin " + pic + ".startPlugin() sucsefully");

							// save plugin instance
							installPlugin.put(pic, plg);
							installProperties.put(pic, prps);

							// append properties to main contant table (no plugin header)
							Vector kls = new Vector(prps.keySet());
							for (int i = 0; i < kls.size(); i++) {
								String pk = (String) kls.elementAt(i);
								String pv = prps.getProperty(pk);
								if (!pv.startsWith("plugin.")) {
									TStringUtils.constants.put(pk, pv);
								}
							}
						}
					}
				} catch (Exception e) {
					SystemLog.logException(e);
					SystemLog.severe("Plugin not instaled");
				}
			}
		} catch (Exception e) {
			SystemLog.logException1(e);
		}
	}

	/**
	 * return the plugin property for the given parameter
	 * 
	 * @param pic - Plugin Class
	 * @param prp - property name
	 * @return
	 */
	public static String getPluginProperty(String pic, String prp) {
		Plugin p = installPlugin.get(pic);
		String rv = null;
		if (p != null) {
			rv = installProperties.get(pic).getProperty(prp);
		}
		return rv;
	}
	
	/**
	 * return the fully qualified file for the given plugin.
	 * 
	 * @param pic - plugin main class name
	 * @param fn - requested file name
	 * 
	 * @return fully qualified file name
	 */
	public static String getMyFile(String pic, String fn) {
		String db = installProperties.get(pic).getProperty("plugin.database");
		return (db.equals("")) ? fn : db + "." + fn;
	}

	/**
	 * return <code>true</code>if the plugin name <code>pn</code> is installed and available
	 * 
	 * @param pn - plugin name
	 * @return <code>true or false</code>
	 */
	public static boolean isPlugInInstalled(String pid) {
		String ap = (String) installProperties.get(pid).get("plugin.active");
		return ap != null && ap.equals("true");
	}

	public static Plugin getPlugin(String pid) {
		return installPlugin.get(pid);
	}

	public static Vector<String> getInstalledPlungin() {
		return new Vector<String>(installPlugin.keySet());
	}
}
