package test;

/**
 *  @(#)LoggedInTests.java
 *  Copyright (C) 2011 - 2013 MER-C
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 3
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.wikipedia.Wiki;
import org.wikiutils.ParseUtils;

import Tools.Login;

/**
 * Tests for Wiki.java which should only be run when logged in.
 *
 * @author MER-C
 */
public class LoggedInTests {

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {

		Wiki wiki = new Wiki("fr.wikipedia.org");
		Login login = new Login();
		wiki.login(login.getLogin(), login.getPassword());
		HashMap<String, String> errors = getErrors("err1");

		try {
			FileInputStream fstream = new FileInputStream("titles.txt");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			//Writer out = new PrintWriter("");
			String strLine = "";
			int i = 0;
			while ((strLine = br.readLine()) != null) {
				try {
					if (i>10000){
						break;
					}
					else{
						if(i<4683){
							i++;
							continue;
						}
					}
					String text = wiki.getPageText(strLine);
					String oldtext = text;
					//wiki.edit("Utilisateur:Hunsu/Brouillons", text, "");
					i++;
					ArrayList<String> al = ParseUtils.getTemplates(
							"lien web", text);
					int nb = al.size();
					for (int k=0;k<nb;k++) {
						text = text.replace(al.get(k),citeWeb(al.get(k),errors));
					}

					//wiki.edit("Utilisateur:Hunsu/Brouillons", text, "");
					if(oldtext.equals(text)){
						System.out.println(strLine);
						continue;
					}

					else{// TODO Auto-generated method stub
						text = text.replace("{{pdf}} {{lien web", "{{lien web|format=pdf");
						text = text.replace("{{Pdf}} {{lien web", "{{lien web|format=pdf");
						text = text.replace("{{Pdf}} {{Lien web", "{{Lien web|format=pdf");
						text = text.replace("{{pdf}} {{Lien web", "{{Lien web|format=pdf");
						text = text.replace("{{en}} {{lien web", "{{lien web|langue=en");
						text = text.replace("{{en}} {{Lien web", "{{Lien web|langue=en");
						wiki.edit(strLine, text, "Maintenance lien web");
						System.out.println(strLine);
					}
				} catch (Exception e) {
					System.out.print("Erreur" + e);
					e.printStackTrace();
				}

			}
			//out.close();
			in.close();

		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}

	}

	/**
	 * Cite web.
	 *
	 * @param template the template
	 * @param map the map
	 * @return the string
	 */
	public static String citeWeb(String template, HashMap<String,String> map) {
		for (String key : map.keySet()) {
			String value = map.get(key);
			if(value.trim().equals("sup"))
				template = ParseUtils.removeTemplateParam(template, key.trim());
			else
				template = ParseUtils.renameTemplateParam(template, key, value,false);
		}
		try{
			template = correctDate(template);
		}
		catch(Exception e){}

		return template;
	}

	/**
	 * Gets the errors.
	 *
	 * @param filename the filename
	 * @return the errors
	 */
	public static HashMap<String, String> getErrors(String filename) {
		try {
			FileInputStream fstream = new FileInputStream(filename);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line = "";
			HashMap<String,String> map = new HashMap<String,String>();
			while((line = br.readLine()) != null){
				int index = line.indexOf("=");
				if(index ==-1)
					continue;
				map.put(line.substring(0,index), line.substring(index+1));
			}
			br.close();
			return map;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Correct date.
	 *
	 * @param template the template
	 * @return the string
	 */
	public static String correctDate(String template){
		template = correctDate(template,"date");
		template = correctDate(template,"consulté le");
		//template = correctDate(template,"en ligne le");
		return template;
	}

	/**
	 * Correct date.
	 *
	 * @param template the template
	 * @param param the param
	 * @return the string
	 */
	public static String correctDate(String template, String param) {
		String date = ParseUtils.getTemplateParam(template, param,false);
		if (date != null) {
			Pattern p = Pattern.compile("(\\d\\d\\d\\d)-(\\d\\d?)-(\\d\\d?)");
			Matcher m = p.matcher(date);
			if (m.find()) {
				if (Integer.parseInt(m.group(2)) < 13) {
					String day = m.group(3);
					if (day.startsWith("0")) day = day.substring(1);
					String newDate = date.replace(m.group(), day
							+ " "
							+ new DateFormatSymbols().getMonths()[Integer
									.parseInt(m.group(2)) - 1] + " "
							+ m.group(1));
					if (newDate.startsWith("[["))
						newDate = newDate.substring(2);
					if(newDate.endsWith("]]"))
						newDate = newDate.substring(0, newDate.length()-2);
					template =ParseUtils.setTemplateParam(template, param, newDate,false);
				}
			}
		}
			if (date != null) {
				Pattern p = Pattern.compile("(\\d\\d?)/(\\d\\d?)/(\\d\\d\\d\\d)");
				Matcher m = p.matcher(date);
				if (m.find()) {
					if (Integer.parseInt(m.group(2)) < 13) {
						String day = m.group(1);
						if (day.startsWith("0")) day = day.substring(1);
						String newDate = date.replace(m.group(), day
								+ " "
								+ new DateFormatSymbols().getMonths()[Integer
										.parseInt(m.group(2)) - 1] + " "
								+ m.group(3));
						if (newDate.startsWith("[["))
							newDate = newDate.substring(2);
						if(newDate.endsWith("]]"))
							newDate = newDate.substring(0, newDate.length()-2);
						template =ParseUtils.setTemplateParam(template, param, newDate,false);
					}
				}
		}

		return template;
	}

	/**
	 * Gets the format.
	 *
	 * @param format the format
	 * @return the format
	 */
	public static String getFormat(String format) {
		format = format.toLowerCase();
		if (format.equalsIgnoreCase("asp") || format.equalsIgnoreCase("asf")
				|| format.equalsIgnoreCase("stm")
				|| format.equalsIgnoreCase("php"))
			return null;
		if (format.contains("web"))
			return null;
		if (format.startsWith("{{") || format.startsWith("[["))
			format = format.substring(2);
		if (format.endsWith("}}") || format.endsWith("]]"))
			format = format.substring(0, format.length() - 2);
		if (format.equals("rar"))
			return "{{RAR}}";
		if (format.toLowerCase().contains("video"))
			return "{{Vidéo}}";
		if (format.contains("text") || format.equalsIgnoreCase("texte"))
			return "{{Txt}}";
		if (format.contains("excel"))
			return "{{Xls}}";
		if (format.length() == 3)
			return "{{" + format + "}}";
		if (format.contains("html"))
			return "{{Html}}";
		if (format.contains("portable document"))
			return "{{Pdf}}";
		if (format.contains("pdf"))
			return "{{Pdf}}";
		if (format.contains("microsoft word") || format.equals("doc")
				|| format.equals(".doc"))
			return "{{Doc}}";

		return null;
	}

	/**
	 * Removes the parameters.
	 *
	 * @param template the template
	 * @return the string
	 */
	public static String removeParameters(String template){
		template = ParseUtils.removeTemplateParam(template, "archiveurl");
		template = ParseUtils.removeTemplateParam(template, "archiveurl");
		template = ParseUtils.removeTemplateParam(template, "archivedate");
		template = ParseUtils.removeTemplateParam(template, "deadurl");
		return template;
	}
	/*
	public static String renameParameters(String template){
		template = ParseUtils.renameTemplateParam(template, "mis en ligne le", "date",false);
		template = ParseUtils.renameTemplateParam(template, "consulte le", "consulté le",false);
		template = ParseUtils.renameTemplateParam(template, "Date", "date",false);
		template = ParseUtils.renameTemplateParam(template, "Auteur", "auteur",false);
		template = ParseUtils.renameTemplateParam(template, "publié le", "date",false);
		template = ParseUtils.renameTemplateParam(template, "accédé le", "consulté le",false);
		template = ParseUtils.renameTemplateParam(template, "date consultation", "consulté le",false);
		template = ParseUtils.renameTemplateParam(template, "éditeurr", "éditeur",false);
		template = ParseUtils.renameTemplateParam(template, "editor", "éditeur",false);
		template = ParseUtils.renameTemplateParam(template, "editor", "éditeur",false);
		template = ParseUtils.renameTemplateParam(template, "autor", "auteur",false);
		template = ParseUtils.renameTemplateParam(template, "consultéle", "consulté le",fal);
		template = ParseUtils.renameTemplateParam(template, "date d'accès", "date");
		template = ParseUtils.renameTemplateParam(template, "mise en ligne le", "date");
		template = ParseUtils.renameTemplateParam(template, "accédé le", "consulté le");
		template = ParseUtils.renameTemplateParam(template, "date consultation", "consulté le");
		template = ParseUtils.renameTemplateParam(template, "Éditeur", "éditeur");
		template = ParseUtils.renameTemplateParam(template, "date de consultation", "consulté le");
		template = ParseUtils.renameTemplateParam(template, "onsulté le", "consulté le");
		template = ParseUtils.renameTemplateParam(template, "visité le", "consulté le");
		template = ParseUtils.renameTemplateParam(template, "Editeur", "éditeur");
		template = ParseUtils.renameTemplateParam(template, "contulté le", "consulté le");
		template = ParseUtils.renameTemplateParam(template, "site web", "site");
		template = ParseUtils.renameTemplateParam(template, "année accès", "année");
		return template;
	}*/
}



// test1
/*
 * String [] errors = { "consultéle",
 * "consutlé le","consulé le","date consultation",
 * "consulté lé","consulté en","cconsulté le","aconsulté ler",
 * "consulte le","consuté le","consult\" le","consulyé le","consultation le",
 * "aconsulté le"
 * ,"date d’accès","Page consultée le","Visitée le","page consultée le",
 * "consulté le le"
 * ,"consumtéle","consult le","consulté me","conulté le","consuluté le",
 * "consutlté le"
 * ,"consulté-le","consulté ke","Consulté le","Consulte le","consnulté le",
 * "conslté le","consultm le"};
 *
 * Wiki wiki = new Wiki("fr.wikipedia.org"); wiki.login("Hunsu",
 * "MegamiMonster");
 *
 * try{ // Open the file that is the first // command line parameter
 * FileInputStream fstream = new FileInputStream("title.txt"); // Get the object
 * of DataInputStream DataInputStream in = new DataInputStream(fstream);
 * BufferedReader br = new BufferedReader(new InputStreamReader(in)); String
 * strLine; //Read File Line By Line while ((strLine = br.readLine()) != null) {
 * try{ String text = wiki.getPageText(strLine); for(int
 * i=0;i<errors.length;i++){ String pattern = "\\|\\s*"+ errors[i] + "\\s*=";
 * text = text.replaceAll(pattern, "|consulté le="); } wiki.edit(strLine, text,
 * "Maintenance lien web"); }catch(Exception e){ System.out.print("Erreur + e");
 * } if (wiki.hasNewMessages()) break;
 *
 * } //Close the input stream in.close(); }catch (Exception e){//Catch exception
 * if any System.err.println("Error: " + e.getMessage()); }
 */
