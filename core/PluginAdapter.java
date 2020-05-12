package core;

import java.io.*;
import java.sql.*;
import java.util.*;

import core.datasource.*;

public abstract class PluginAdapter implements Plugin {

	private Properties myProperties;

	@Override
	public void startPlugin(Properties prps) throws Exception {
		myProperties = prps;

		// perform standar installation & instalation
		// test plugin instalation
		boolean pi = (Boolean) TPreferences.getPreference(TPreferences.PLUGIN_INSTALL_INFO, getClass().getName(), false);
		if (!pi) {
			// execute db script
			Connection conn = ConnectionManager.getDBConnection("");
			ScriptRunner sr = new ScriptRunner(conn, false);
			FileReader fr = new FileReader(new File(myProperties.get("pluginPath") + "script.sql"));
			sr.runScript(fr);

			// set the plugininstalled flag
			TPreferences.setPreference(TPreferences.PLUGIN_INSTALL_INFO, getClass().getName(), true);
		}
	}

	@Override
	public void endPlugin() throws Exception {
		// TODO Auto-generated method stub

	}
}
