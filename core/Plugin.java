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

import java.util.*;

import action.*;

/**
 * interface to incorportate new Alesia plugin.
 * 
 * @author terry
 * 
 */
public interface Plugin {

	/**
	 * UI: this plugins contains user interface. aplication look for this kind of plugins to construct menulist or other
	 * actions related GUI. the execute Methot bust return an instance of Action.
	 */
	public static String TYPE_UI = "UI";
	/**
	 * Plugin for extends or increment internal aplication functions.
	 */
	public static String TYPE_TOOL = "TOOL";
	/**
	 * incorporate new task. execute method must return Runnable object. aplication task manager look for this kind of
	 * plugin for append to task planification, Join task Etc.
	 */
	public static String TYPE_TASK = "TASK";

	/**
	 * Called during plugin initializacion. use this method ot install plugins where database files or to check plugin
	 * integrity by counting class file or other actions. If plugin instalation or verification fail, throw an execption
	 * explain the situation. {@link PluginManager} deactivate the plugin and continue.
	 * 	 
	 * @param prps - Properties for this plugin
	 * 
	 * @throws Exception - throw if somethin wrong
	 */
	public void startPlugin(Properties prps) throws Exception;

	/**
	 * Main entry point for plugin access. Depending of plugin type ({@link Plugin#TYPE_TASK} {@link Plugin#TYPE_TOOL}
	 * or {@link Plugin#TYPE_UI}) this method is called on diferent times of application process. For example. if this
	 * plugin is {@link Plugin#TYPE_UI} Aplication call this method during menu initializacion expecting a
	 * {@link TAbstractAction} instance to put in <code>JMenu</code> bar
	 * 
	 * @param obj - parameters to this plugin
	 * 
	 * @return result acording plugin implementation
	 */
	public Object executePlugin(Object obj);
	
	public void endPlugin() throws Exception;
}
