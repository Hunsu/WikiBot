package maintenance;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.security.auth.login.FailedLoginException;

import org.apache.commons.io.FileUtils;
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
			int i = 0;
			String[] titles = wiki
					.getCategoryMembers("Page utilisant le modèle date avec une syntaxe erronée");
			for (i = 0; i < titles.length; i++) {
				try {

					String text = wiki.getPageText(titles[i]);
					System.out.println(titles[i]);
					String oldtext = text;
					ArrayList<String> al = ParseUtils
							.getTemplates("Date", text);
					int nb = al.size();
					for (int k = 0; k < nb; k++) {
						String c = correctDate(al.get(k));
						if(c == null)
							c = correctRepDate(al.get(k));
						if (c == null) {
							System.out.println(al.get(k) + " : Error!");
							continue;
						}
						if (!c.equals(al.get(k)))
							c = c.replace("|}}", "}}");

						text = text.replace(al.get(k), c);

					}

					if (oldtext.equals(text)) {
						// System.out.println(strLine);
						// out.write(strLine + "\n");
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
						wiki.edit(titles[i], text, "Maintenance modèle Date");
					}

				} catch (Exception e) {
					System.out.print("Erreur" + e);
					e.printStackTrace();
				}

			}
			// FileUtils.writeStringToFile(new File("corrections.txt"),
			// corrections);
			in.close();

		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}

	public static String correctDate(String template) {
		if (template.contains("de décès")) {
			return template.replace("de décès", "")
					.replace("{{date", "{{Date de décès")
					.replace("{{Date", "Date de décès");
		}
		String param = ParseUtils.getTemplateParam(template, 4);
		String y = ParseUtils.getTemplateParam(template, 3);
		String d = ParseUtils.getTemplateParam(template, 1);
		String m = ParseUtils.getTemplateParam(template, 2);
		if (param != null && !param.trim().equals("") && y.trim().equals("")
				&& isYear(param)) {
			y = param;
			param = ParseUtils.getTemplateParam(template, 5);
			;
		}
		int day1 = 1, month1 = 2;
		if ((d.trim().equals("") && isInteger(m) && !isYear(m) && (!isYear(y)))
				|| isInteger(param) && isMonth(y)) {
			return template;
			/*
			 * day1++; month1++; y = param;
			 * param=ParseUtils.getTemplateParam(template, 5);;
			 */

		}

		String age = ParseUtils.getTemplateParam(template, "age", false);
		if (age != null)
			return template.replace("{{date", "{{date de naissance").replace(
					"{{Date", "Date de naissance");

		HashMap<String, String> day = processDay(ParseUtils.getTemplateParam(
				template, day1));
		HashMap<String, String> month = processMonth(ParseUtils
				.getTemplateParam(template, month1));
		HashMap<String, String> year = processYaer(y);

		return buildTemplate(ParseUtils.getTemplateName(template), day, month,
				year, param);
	}

	private static String buildTemplate(String templateName,
			HashMap<String, String> day, HashMap<String, String> month,
			HashMap<String, String> year, String qualificatif) {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		String out = "";

		if (day.get("day") != null && !day.get("day").equals("")) {
			if (day.get("out") == null)
				map.put("ParamWithoutName1", day.get("day"));
			else {
				map.put("ParamWithoutName1", "");
				out = day.get("day").trim() + " ";
			}
		} else {
			if (month.get("day") != null)
				map.put("ParamWithoutName1", month.get("day"));
			else
				map.put("ParamWithoutName1", "");
		}
		if (out.equals("") && month.get("out") != null)
			out = month.get("out") + " ";
		if (month.get("month") == null || month.get("month").trim().equals("")) {
			if (day.get("month") != null)
				map.put("ParamWithoutName2", day.get("month"));
			else {
				if (month.get("month") != null)
					map.put("ParamWithoutName2", month.get("month"));
				else
					map.put("ParamWithoutName2", "");
			}

		} else {
			if (day.get("year") != null && !isMonth(month.get("month"))) {
				map.put("ParamWithoutName2", "");
				qualificatif = month.get("month");
			} else
				map.put("ParamWithoutName2", month.get("month"));
		}

		if (!map.get("ParamWithoutName1").trim().equals("")) {
			map.put("ParamWithoutName2", map.get("ParamWithoutName2")
					.toLowerCase());
		}

		if (year.get("year") == null
				|| year.get("year").trim().equals("")
				|| (year.get("noerror") == null && !isInteger(year.get("year")))) {
			if (!isYear(year.get("year")))
				qualificatif = year.get("year");
			if (day.get("year") != null && month.get("year") == null)
				map.put("ParamWithoutName3", day.get("year"));
			if (day.get("year") == null && month.get("year") != null)
				map.put("ParamWithoutName3", month.get("year"));
			if (day.get("year") == null && month.get("year") == null
					&& year.get("year") == null)
				map.put("ParamWithoutName3", "");
			if (day.get("year") == null && month.get("year") == null
					&& year.get("year") != null)
				map.put("ParamWithoutName3", year.get("year"));
			if ((day.get("year") != null || month.get("year") != null)
					&& year.get("year") != null)
				map.put("ParamWithoutName4", year.get("year"));

		} else
			map.put("ParamWithoutName3", year.get("year"));

		if (qualificatif == null || qualificatif.trim().equals("")) {
			if (year.get("qualificatif") != null)
				map.put("ParamWithoutName4", year.get("qualificatif"));
		} else
			map.put("ParamWithoutName4", qualificatif);
		String template = "";
		if (!map.isEmpty())
			map.put("templateName", templateName);

		if ((map.get("ParamWithoutName1") == null || map
				.get("ParamWithoutName1").trim().equals(""))
				&& (map.get("ParamWithoutName2") == null || map
						.get("ParamWithoutName2").trim().equals(""))) {
			if (!map.get("ParamWithoutName3").trim().equals("")
					&& (map.get("ParamWithoutName4") == null || map
							.get("ParamWithoutName4").trim().equals("")))
				return out + "[[" + map.get("ParamWithoutName3") + "]]";
			if (map.get("ParamWithoutName3").trim().equals("")
					&& out.equals(""))
				return "";
		}

		String day1 = map.get("ParamWithoutName1");
		String month1 = map.get("ParamWithoutName2");
		String year1 = map.get("ParamWithoutName3");

		if ((!day1.equals("") && !isInteger(day1))
				|| (!month1.equals("") && !isMonth(month1))
				|| (!year1.equals("") && !isInteger(year1)))
			return null;

		if (map.containsKey("ParamWithoutName1")
				|| map.containsKey("ParamWithoutName2")
				|| map.containsKey("ParamWithoutName3")
				|| map.containsKey("ParamWithoutName4")) {
			if (!map.containsKey("ParamWithoutName3")
					&& !map.containsKey("ParamWithoutName2"))
				System.out
						.println("Article with day but with no month and year");
			template = ParseUtils.templateFromMap(map);
		}

		return out + template;
	}

	private static HashMap<String, String> processYaer(String year) {
		HashMap<String, String> s = new HashMap<String, String>();
		if (year == null) {
			return s;
		}
		if (year.trim().equals("") || year.toLowerCase().contains("an")
				|| year.toLowerCase().contains("av.")) {
			s.put("noerror", "tre");
			s.put("year", year);
			return s;
		}
		String yearTrimed = year.trim();

		if (yearTrimed.startsWith("..") || yearTrimed.startsWith("00")
				|| yearTrimed.equals("0") || yearTrimed.startsWith("?")
				|| yearTrimed.startsWith("--")
				|| yearTrimed.toLowerCase().startsWith("x"))
			return s;
		int index = yearTrimed.indexOf(" ");
		if (index != -1) {
			if (isInteger(yearTrimed.substring(0, index))) {
				s.put("year", yearTrimed.substring(0, index));
				s.put("qualificatif", yearTrimed.substring(index + 1));
			} else {
				s.put("qualificatif", year);
			}
		} else
			s.put("year", year);
		return s;
	}

	private static HashMap<String, String> processMonth(String month) {
		HashMap<String, String> s = new HashMap<String, String>();
		if (month == null) {
			return s;
		}
		if (month.trim().equals("")) {
			s.put("month", month);
			return s;
		}
		String monthTrimed = month.trim();

		if (monthTrimed.startsWith("..") || monthTrimed.startsWith("00")
				|| monthTrimed.equals("0") || monthTrimed.startsWith("?")
				|| monthTrimed.startsWith("-")
				|| monthTrimed.toLowerCase().startsWith("x"))
			return s;
		if (monthTrimed.equals("en")) {
			s.put("out", monthTrimed);
			return s;
		}
		if (monthTrimed.equals("automne")
				|| monthTrimed.toLowerCase().equals("été")) {
			s.put("out", "[[" + monthTrimed + "]]");
			return s;
		}
		try {
			int test = Integer.parseInt(monthTrimed);
			if (test == 0)
				return s;
			if (test > 0 && test < 13) {
				s.put("month", new DateFormatSymbols().getMonths()[test - 1]);
				return s;
			} else {
				System.out.println("Possible error!");
				s.put("year", monthTrimed);
				return s;
			}
		} catch (Exception e) {
		}

		String[] str = monthTrimed.split(" ");
		if (str.length == 1) {
			if (isMonth(str[0])) {
				s.put("month", month);
				return s;
			} else {
				System.out.println("Possible error");
				s.put("month", month);
				return s;
			}

		}
		if (str.length == 2) {
			if (isInteger(str[1]) && isMonth(str[0])) {
				s.put("month", str[0]);
				s.put("year", str[1]);
				return s;
			}
			if (isInteger(str[0]) && isMonth(str[1])) {
				s.put("month", str[1]);
				s.put("day", str[0]);
				return s;
			}
		}

		s.put("month", month);
		s.put("error", "no");
		return s;
	}

	private static HashMap<String, String> processDay(String day) {
		HashMap<String, String> s = new HashMap<String, String>();
		if (day == null) {
			return s;
		}
		if (day.trim().equals("")) {
			s.put("day", day);
			return s;
		}
		String dayTrimed = day.trim();

		if (dayTrimed.equals("1{{er}}")) {
			s.put("day", "1er");
			return s;
		}

		if (dayTrimed.startsWith("..") || dayTrimed.startsWith("00")
				|| dayTrimed.equals("0") || dayTrimed.startsWith("?")
				|| dayTrimed.startsWith("-")
				|| dayTrimed.toLowerCase().startsWith("x"))
			return s;

		if (dayTrimed.contains("-") || dayTrimed.contains(" au ")
				|| dayTrimed.contains("/")) {
			HashMap<String, String> map = transformDate(dayTrimed);
			if (map != null && !map.isEmpty())
				return s;
			s.put("day", dayTrimed);
			s.put("out", "true");
			return s;
		}
		try {
			int test = Integer.parseInt(dayTrimed);
			if (test == 0)
				return s;
			if (test > 0 && test < 32) {
				s.put("day", String.valueOf(test));
				return s;
			} else {
				s.put("year", dayTrimed);
				return s;
			}
		} catch (Exception e) {
		}

		String[] str = dayTrimed.split(" ");
		if (str.length == 1) {
			if (isMonth(str[0])) {
				String temp = str[0];
				s.put("month", temp);
				return s;
			} else {
				s.put("day", day);
				return s;
			}

		}
		if (str.length == 2) {
			if (isInteger(str[0]) && isMonth(str[1])) {
				if (str[0].startsWith("0"))
					str[0] = str[0].replaceAll("^0", "");
				s.put("day", str[0]);
				s.put("month", str[1]);
				return s;
			}
		}
		if ((str.length == 3 || str.length == 4) && isInteger(str[0])
				&& isMonth(str[1]) && isYear(str[2])) {
			s.put("day", str[0]);
			s.put("month", str[1]);
			s.put("year", str[2]);
			return s;
		}

		s.put("day", day);
		s.put("error", "no");
		return s;
	}

	private static boolean isMonth(String str) {
		str = str.trim().toLowerCase();
		String[] s = new DateFormatSymbols().getMonths();
		for (int i = 0; i < 12; i++) {
			if (s[i].equals(str))
				return true;
		}
		return false;
	}

	private static HashMap<String, String> transformDate(String day) {
		HashMap<String, String> map = new HashMap<String, String>();
		try {
			String[] s = day.split("-");
			if (s.length != 3)
				s = day.split("/");
			if (s.length == 3) {
				s[0] = s[0].trim();
				s[1] = s[1].trim();
				s[2] = s[2].trim();
				if (s[0].equals("0"))
					map.put("day", "");
				map.put("month", new DateFormatSymbols().getMonths()[Integer
						.parseInt(s[1]) - 1]);
				map.put("year", s[2]);
				if (s[2].length() < s[0].length()) {
					String temp = s[0];
					map.put("day", s[2]);
					map.put("year", temp);
				}
				return map;
			}
		} catch (Exception e) {
		}
		return null;
	}

	public static boolean isInteger(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException nfe) {
		}
		return false;
	}

	private static boolean isYear(String day1) {
		if (day1 == null)
			return false;
		else {
			try {
				if (Integer.parseInt(day1) > 999 || Integer.parseInt(day1) < 0)
					return true;
				else
					return false;
			} catch (Exception e) {
				return false;
			}
		}
	}

	public static String correctRepDate(String template) {
		String year = ParseUtils.getTemplateParam(template, 3);
		if (year != null && year.contains("an "))

			return template.replace("{{date", "{{Date").replace("{{Date",
					"{{Date républicaine");
		return template;
	}

}
