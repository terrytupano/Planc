package dev.utils;

import java.io.*;
import java.util.*;

public class Flicka {

	public static Vector<String> table = new Vector<String>();
	public static Vector<String> record = new Vector<String>();

	public static void main(String args[]) {
		parseRightData();
	}

	private static void parseRightData() {
		try {
			File fn = new File("body.txt");
			FileReader fr = new FileReader(fn);
			BufferedReader br = new BufferedReader(fr);
			String line = "";
			Vector<String> lines = new Vector();

			// buffer lines
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}

			// scan lines
			int lineM = 1;
			boolean twolst = false;
			for (int i = 0; i < lines.size(); i++) {
				line = lines.elementAt(i).trim();

				// marck of group ends
				if (line.startsWith("De  ")) {
					for (int j = i; j > (i - 4); j--) {
						line = lines.elementAt(i).trim();
						if (line.equals("D E B U T A N T E")) {
							System.out.println(line);
							break;
						}
						String[] fields = line.split(" ");
						// race

					}

					lineM++;
					// bread
					System.out.print(lines.elementAt(i - 1) + "\t");
					// birthd
					System.out.print(lines.elementAt(i + 1) + "\t");
					// horsen
					System.out.print(lines.elementAt(i + 2) + "\t");
					// stud
					line = lines.elementAt(i + 3).substring(6);
					String st = lines.elementAt(i + 4).trim();
					if (!line.startsWith("[A-Z]./h")) {
						line += " " + st;
						twolst = true;
					}
					System.out.print(line + "\t");

					int nc = twolst ? i + 5 : i + 4;
					// coacch
					System.out.print(lines.elementAt(nc) + "\t");
					// ??
					System.out.print(lines.elementAt(++nc) + "\t");
					// jockeyW
					System.out.print(lines.elementAt(++nc) + "\t");
					// jockeyN
					System.out.println(lines.elementAt(++nc));
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private static void parseLeftTable() {
		try {
			File fn = new File("head.txt");
			FileReader fr = new FileReader(fn);
			BufferedReader br = new BufferedReader(fr);
			String line = "";
			Vector<String> lines = new Vector();

			// buffer lines
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}

			// scan lines
			int lineM = 1;
			boolean twolst = false;
			for (int i = 0; i < lines.size(); i++) {
				line = lines.elementAt(i).trim();
				if (line.equals(String.valueOf(lineM))) {
					lineM++;
					// bread
					System.out.print(lines.elementAt(i - 1) + "\t");
					// birthd
					System.out.print(lines.elementAt(i + 1) + "\t");
					// horsen
					System.out.print(lines.elementAt(i + 2) + "\t");
					// stud
					line = lines.elementAt(i + 3).substring(6);
					String st = lines.elementAt(i + 4).trim();
					if (!line.startsWith("[A-Z]./h")) {
						line += " " + st;
						twolst = true;
					}
					System.out.print(line + "\t");

					int nc = twolst ? i + 5 : i + 4;
					// coacch
					System.out.print(lines.elementAt(nc) + "\t");
					// ??
					System.out.print(lines.elementAt(++nc) + "\t");
					// jockeyW
					System.out.print(lines.elementAt(++nc) + "\t");
					// jockeyN
					System.out.println(lines.elementAt(++nc));
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
