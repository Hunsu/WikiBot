package maintenance;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.security.auth.login.FailedLoginException;

import org.wikipedia.Wiki;
import org.wikiutils.ParseUtils;

public class Date {

	public static void main(String[] args) {
		date();
	}

	public static void date() {
		Wiki wiki = new Wiki("fr.wikipedia.org");
		try {
			wiki.login("Hunsu", "MegamiMonster");
		} catch (FailedLoginException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			FileInputStream fstream = new FileInputStream("dates");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			Writer out = new PrintWriter(new BufferedWriter(new FileWriter(
					"text.hg", true)));
			String strLine = "";
			int i = 0;
			while ((strLine = br.readLine()) != null) {
				try {
					if (i > 100) {
						break;
					} else {
						if (i < 5) {
							i++;
							continue;
						}
					}
					String text = wiki.getPageText(strLine);
					String oldtext = text;
					// wiki.edit("Utilisateur:Hunsu/Brouillons", text, "");
					i++;
					ArrayList<String> al = ParseUtils
							.getTemplates("Date", text);
					al.addAll(ParseUtils.getTemplates("Date de sport", text));
					int nb = al.size();
					for (int k = 0; k < nb; k++) {
						text = text.replace(al.get(k), correctDate(al.get(k)));

						/*
						 * String lienWeb = citeWeb(map.get(key)); text =
						 * text.replace(map.get(key), lienWeb);
						 */
					}

					// wiki.edit("Utilisateur:Hunsu/Brouillons", text, "");
					if (oldtext.equals(text)) {
						System.out.println(strLine);
						out.write(strLine+"\n");
						continue;
					}

					else {
						text = text.replace("{{pdf}} {{lien web",
								"{{lien web|format=pdf");
						text = text.replace("{{Pdf}} {{lien web",
								"{{lien web|format=pdf");
						text = text.replace("{{Pdf}} {{Lien web",
								"{{Lien web|format=pdf");
						text = text.replace("{{pdf}} {{Lien web",
								"{{Lien web|format=pdf");
						text = text.replace("{{en}} {{lien web",
								"{{lien web|langue=en");
						text = text.replace("{{en}} {{Lien web",
								"{{Lien web|langue=en");
						wiki.edit(strLine, text, "Maintenance modèle Date");
						out.write(strLine + "\n");
					}
					// out.write(text);
					// out.flush();
					// text = text.replaceAll(pattern, "|consulté le=");

					// wiki.edit(strLine, text, "Maintenance lien web");
				} catch (Exception e) {
					System.out.print("Erreur" + e);
					e.printStackTrace();
				}
				// if (wiki.hasNewMessages()) break;

			}
			out.close();
			in.close();

		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}

	public static String correctDate(String template) {
		String day1, month1, year1;
		String day = ParseUtils.getTemplateParam(template, 1);
		String month = ParseUtils.getTemplateParam(template, 2);
		String year = ParseUtils.getTemplateParam(template, 3);
		String param = ParseUtils.getTemplateParam(template, 4);
		if (ParseUtils.getTemplateParam(template, 5) != null)
			return template;
		String templateName = ParseUtils.getTemplateName(template);
		if (day != null)
			day1 = day.trim();
		else
			day1 = "";
		if (month != null)
			month1 = month.trim();
		else
			month1 = "";
		if (year != null)
			year1 = year.trim();
		else
			year1 = "";
		// if(param != null && !param.trim().equals(""))
		
		if(month1.equals("") && param.equals("") && year.equals(""))
			return "";
		if(month1.equals("") && param.equals("") && !year.equals(""))
			return year1;
		

		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put("templateName", templateName);
		try {
			int test = Integer.parseInt(day1);
			if (test > 999) {
				map.put("ParamWithoutName1", "");
				year1 = day1;
				param = month1;
				month1 = "";
			} else{
				if(test == 0)
					map.put("ParamWithoutName1", "");
				else			
					map.put("ParamWithoutName1", day1);
			}
		} catch (Exception e) {
			if (day1.startsWith("..") || day1.startsWith("00")
					|| day1.equals("0") || day1.startsWith("?")
					|| day1.startsWith("-"))
				map.put("ParamWithoutName1", "");
			else{
				if(day != null)
					map.put("ParamWithoutName1", day);
				else
					map.put("ParamWithoutName1", "");
			}
		}
		if (month1.startsWith("..") || month1.startsWith("00")
				|| month1.equals("0") || month1.startsWith("?")
				|| month1.startsWith("-"))
			map.put("ParamWithoutName2", "");
		if (month1.length() < 3) {
			try {
				int test = Integer.parseInt(month1);
				map.put("ParamWithoutName2", new DateFormatSymbols().getMonths()[test - 1]);
			} catch (Exception e) {
				if(month1.equals("") && month != null)
					map.put("ParamWithoutName2", month);
				if(month == null){
					map.put("ParamWithoutName2", "");
				}
			}
		} else {
			if (month1.startsWith("..") || month1.startsWith("00")
					|| month1.equals("0") || month1.startsWith("?")
					|| month1.startsWith("-"))
				map.put("ParamWithoutName2", "");
			else{
				if(month != null)
					map.put("ParamWithoutName2", month);
				else 
					map.put("ParamWithoutName2", "");
			}
			
		}
		String s = "";
		int index = year1.indexOf(" ");
		if (year1.length() != 4 && index != -1)
			s = year1.substring(0, index);
		else
			s = year;
		try {
			if(Integer.parseInt(s) == 0)
				map.put("ParamWithoutName3", "");
			else{
				if (param == null && s != null) {
					map.put("ParamWithoutName3", s);
				if (index != -1 && param == null)
					map.put("ParamWithoutName4", year1.substring(index + 1));
			} else {
				if(year != null)
					map.put("ParamWithoutName3", year);
				else
					map.put("ParamWithoutName3", "");
			}
			}
		} catch (Exception e) {
			if (year1.startsWith("..") || year1.startsWith("0")
					|| year1.equals("0") || year1.startsWith("?")
					|| year1.startsWith("-"))
				map.put("ParamWithoutName3", "");
			else{
				if(year != null)
					map.put("ParamWithoutName3", year);
				else
					map.put("ParamWithoutName3", "");
			}			
		}

		if (param != null)
			map.put("ParamWithoutName4", param);

		return ParseUtils.templateFromMap(map);
	}

}
