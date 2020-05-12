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
import java.io.*;
import java.sql.*;
import java.util.Date;

import core.datasource.*;
import core.tasks.*;


public class ExportCSV1 extends AbstractExternalTask {

	@Override
	public void executeExternalTask() throws Exception {
		Statement stm = ConnectionManager.getDBConnection("sleoracle").createStatement();

		String query = "select lpad(decode(empl.nactra,'Venezolana','V','E')||empl.cedula,12,'0')"
				+ " ||','||rpad(to_char(to_number(empl.ficha)),12,' ') "
				+ " ||','||substr(rpad(empl.nombre1||' '||empl.nombre2,35,' '),1,35)"
				+ " ||','||substr(rpad(empl.apellido1||' '||empl.apellido2,60,' '),1,60)"
				+ " ||','||case when empl.fecret is null then '1' else '2' end "
				+ " ||','||to_char(empl.fecing,'dd/mm/yyyy')"
				+ " ||','||'1'"
				+ " ||','||lpad(trim(empl.cia_codcia),3,'0')"
				+ " ||','||case when empl.fecret is null then '1' else '0' end"
				+ " ||','||lpad(decode(empl.nactra,'Venezolana','V','E')||empl.cedula,12,'0')"
				+ " relation "
				+ " ,substr(rpad(nvl(trim(pers.direccion),'xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx'),30,' '),1,30) adresslinea"
				+ " from nm_empleado empl" + " inner join eo_persona pers on empl.id_persona =pers.id"
				+ " where (fecret is null or fecret >= trunc(sysdate)-90)"
				+ " and empl.cia_codcia in ('01', '02', '03', '04', '05', '08', '09')"
				+ " and empl.tnom_tipnom not in ('06','07')";

		// String query1 = getProperty("query");
		stm.execute(query);
		ResultSet result = stm.getResultSet();

		File tempfile = File.createTempFile("AlesiaTemporalFile", ".tmp");
		int rcdcnt = 0;
		FileOutputStream fos = new FileOutputStream(tempfile);

		while (result.next()) {
			String relation = result.getString(1);
			String address = result.getString(2);
			String line = relation + ",\"" + address + "\"" + "\n";
			fos.write(line.getBytes());
			rcdcnt++;
		}
		fos.close();

		// archivo definitivo
		Date now = new Date();
		String fname = "A9999CLI_" + rcdcnt + "_" + String.format("%1$ty%1$tm%1$td%1$tI%1$tM%1$tS", now) + "_"
				+ String.format("%1$tY%1$tm%1$td", now) + ".csv";
		String dir = getProperty("outFilePath");
		tempfile.renameTo(new File(dir + fname));
	}
}
