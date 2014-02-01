package org.maintenance;

import java.io.IOException;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.FailedLoginException;

import org.wikipedia.Wiki;
import org.wikiutils.ParseUtils;

import Tools.Login;

import com.inet.jortho.Dictionary;
import com.inet.jortho.Suggestion;

/**
 * The Class Date.
 */
public class Date {

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		date();
	}

	/**
	 * Date.
	 */
	public static void date() {
		Wiki wiki = new Wiki("fr.wikipedia.org");
		try {
			Login login = new Login();
			wiki.login(login.getLogin(), login.getPassword());
		} catch (FailedLoginException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			int i = 0;
			String[] titles = wiki
					.getCategoryMembers("Page utilisant le modèle date avec une syntaxe erronée");
			for (i = 0; i < titles.length; i++) {
				try {
					if(titles[i].startsWith("Modèle:"))
						continue;
					String text = wiki.getPageText(titles[i]);
					System.out.println(titles[i]);
					String oldtext = text;
					ArrayList<String> al = ParseUtils
							.getTemplates("Date", text);
					al.addAll(ParseUtils.getTemplates("date de naissance", text));
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
			// corrections);;

		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}

	/**
	 * Correct date.
	 *
	 * @param template the template
	 * @return the string
	 */
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
		if ((d.trim().equals("") && isInteger(m) && (isYear(param)))
				|| isInteger(param) && parseMonth(y) != null) {
			return template;
			/*
			 * day1++; month1++; y = param;
			 * param=ParseUtils.getTemplateParam(template, 5);;
			 */

		}

		String age = ParseUtils.getTemplateParam(template, "age", false);
		if(age == null)
			age = ParseUtils.getTemplateParam(template, "âge", false);

		HashMap<String, String> day = processDay(ParseUtils.getTemplateParam(
				template, day1));
		HashMap<String, String> month = processMonth(ParseUtils
				.getTemplateParam(template, month1));
		HashMap<String, String> year = processYaer(y);
		if (age != null)
			year.put("âge", age);

		return buildTemplate(ParseUtils.getTemplateName(template), day, month,
				year, param);
	}

	/**
	 * Builds the template.
	 *
	 * @param templateName the template name
	 * @param day the day
	 * @param month the month
	 * @param year the year
	 * @param qualificatif the qualificatif
	 * @return the string
	 */
	private static String buildTemplate(String templateName,
			HashMap<String, String> day, HashMap<String, String> month,
			HashMap<String, String> year, String qualificatif) {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		String out = "";
		String day1, month1, year1;
		if(day.get("day") != null && !day.get("day").equals(""))
			day1 = day.get("day");
		else{
			if(day.get("out") != null){
				out = day.get("out") + " ";
				day1 = "";
			}
			else{
				if(month.get("day") != null)
					day1 = month.get("day");
				else{
					if(year.get("day") != null)
						day1 = year.get("day");
					else
						day1 = "";
				}
			}
		}


		if(month.get("month") != null && !month.get("month").equals(""))
			month1 = month.get("month");
		else{
			if(month.get("out") != null && out.equals("")){
				out = month.get("out") + " ";
				month1 = "";
			}
			else{
				if(day.get("month") != null)
					month1 = day.get("month");
				else{
					if(year.get("month") != null)
						month1 = year.get("month");
					else
						month1 = "";
				}
			}
		}


		if(year.get("year") != null && !year.get("year").equals("")){
				year1 = year.get("year");
		}
		else{
			if(day.get("year") != null)
				year1 = day.get("year");
			else{
				if(month.get("year") != null)
					year1 = month.get("year");
				else
					year1 = "";
			}

		}


		if(qualificatif == null){
			if(year.get("qualificatif") != null)
				qualificatif = year.get("qualificatif");
			else
				if(month.get("qualificatif") != null)
					qualificatif = month.get("qualificatif");
		}

		if ((!day1.equals("") && !isInteger(day1))
				|| (!month1.equals("") && parseMonth(month1) == null)
				|| (!year1.equals("") && !isInteger(year1)))
			return null;
		map.put("ParamWithoutName1", day1);
		map.put("ParamWithoutName2", month1);
		map.put("ParamWithoutName3", year1);
		map.put("templateName", templateName);
		if(qualificatif != null)
			map.put("ParamWithoutName4", qualificatif.trim());
		if(year.get("âge") != null)
			map.put("âge", year.get("âge"));

		String template = "";

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

	/**
	 * Process yaer.
	 *
	 * @param year the year
	 * @return the hash map
	 */
	private static HashMap<String, String> processYaer(String year) {
		HashMap<String, String> s = new HashMap<String, String>();
		if (year == null) {
			return s;
		}
		if (year.trim().equals("") || year.contains("I")
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

		String month = parseMonth(yearTrimed);
		if(month != null){
			s.put("month", month);
			yearTrimed = yearTrimed.replace(month, "");
		}
		String yearTest = parseYear(yearTrimed);
		if(yearTest != null){
			s.put("year", yearTest);
			yearTrimed = yearTrimed.replace(yearTest, "");
			if(yearTrimed.contains("ans"))
				s.put("âge", "oui");
			else
				s.put("qualificatif", yearTrimed);
		}
		else{
			if(yearTrimed.contains("ans"))
				s.put("âge", "oui");
			else
				s.put("qualificatif", yearTrimed);
		}

		return s;
	}

	/**
	 * Process month.
	 *
	 * @param month the month
	 * @return the hash map
	 */
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
				|| monthTrimed.toLowerCase().equals("été")
				|| monthTrimed.toLowerCase().equals("printemps")
				|| monthTrimed.toLowerCase().equals("hiver")) {
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
			}
		} catch (Exception e) {
		}

		String monthTest = parseMonth(monthTrimed);
		if(monthTest != null)
			s.put("month", monthTest);
		else
			if(month.startsWith("au") || month.startsWith("en"))
				s.put("qualificatif", month);
		String day = parseDay(monthTrimed);
		if(day != null)
			s.put("day", day);
		String year = parseYear(monthTrimed);
		if(year != null && !year.equals(day))
			s.put("year", year);

		return s;
	}

	/**
	 * Process day.
	 *
	 * @param day the day
	 * @return the hash map
	 */
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
			s.put("out", dayTrimed);
			return s;
		}

		String month = parseMonth(dayTrimed);
		if(month != null)
			s.put("month", month);
		String dayTest = parseDay(dayTrimed);
		if(dayTest != null)
			s.put("day", dayTest);
		String year = parseYear(dayTrimed);
		if(year != null && !year.equals(dayTest))
			s.put("year", year);
		return s;
	}

	/**
	 * Parses the year.
	 *
	 * @param str the str
	 * @return the string
	 */
	private static String parseYear(String str) {
		Pattern p = Pattern.compile("-?\\d\\d?\\d?\\d?");
		Matcher m = p.matcher(str);
		if(m.find())
			return m.group();
		return null;
	}

	/**
	 * Parses the day.
	 *
	 * @param str the str
	 * @return the string
	 */
	private static String parseDay(String str) {
		Pattern p = Pattern.compile("^(\\d\\d?)([^\\d]|$)");
		Matcher m = p.matcher(str);
		if(m.find()){
			int test = Integer.parseInt(m.group(1));
			if(test < 32 && test > 0)
				return m.group();
		}
		return null;
	}

	/**
	 * Parses the month.
	 *
	 * @param str the str
	 * @return the string
	 */
	private static String parseMonth(String str) {
		str = str.trim().toLowerCase();

		Dictionary dic = new Dictionary();
		try {
			dic.load("dic.ortho");
			List<Suggestion> list = dic.searchSuggestions(str);
			if (list.size() == 1)
				str = list.get(0).toString();
		} catch (IOException e) {
		}

		String[] s = new DateFormatSymbols().getMonths();
		for (int i = 0; i < 12; i++) {
			if (str.toLowerCase().contains(s[i]))
				return s[i];
		}
		return null;
	}

	/**
	 * Transform date.
	 *
	 * @param day the day
	 * @return the hash map
	 */
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

	/**
	 * Checks if is integer.
	 *
	 * @param str the str
	 * @return true, if is integer
	 */
	public static boolean isInteger(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException nfe) {
		}
		return false;
	}

	/**
	 * Checks if is year.
	 *
	 * @param day1 the day1
	 * @return true, if is year
	 */
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

	/**
	 * Correct rep date.
	 *
	 * @param template the template
	 * @return the string
	 */
	public static String correctRepDate(String template) {
		String year = ParseUtils.getTemplateParam(template, 3);
		if (year != null && year.contains("an "))

			return template.replace("{{date", "{{Date").replace("{{Date",
					"{{Date républicaine");
		return template;
	}

}
