package org.wikipedia.tvseries;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.apache.commons.io.FileUtils;
import org.wikipedia.Wiki;
import org.wikipedia.login.Login;
import org.wikiutils.PDFParser;
import org.wikiutils.ParseUtils;

public class CanadianRatings {
    public static LinkedList<String> ratings;
    public static HashMap<String, String> showsName;
    public static HashMap<String, Integer> currentSeason;

    public static void main(String[] args) throws FailedLoginException,
	    IOException {
	cleanDirectory("shows");
	cleanDirectory("pdfs");
	URL url = contructUrl();
	if (url == null)
	    return;
	downloadRatingsFile(url);
	PDFParser.processFiles(url.toString());
	updateArticles();
    }

    private static void updateArticles() throws FailedLoginException,
	    IOException {
	Wiki wiki = new Wiki("fr.wikipedia.org");
	try {
	    // wiki.login();
	    Login login = new Login();
	    wiki.login(login.getLogin(), login.getPassword());
	} catch (Exception e) {
	    Login login = new Login();
	    wiki.login(login.getLogin(), login.getPassword());
	}
	showsName = loadShowsName();
	currentSeason = getTVSerieCurrentSeason();
	File dir = new File("shows");
	String[] files = dir.list();
	Arrays.sort(files);
	for (String filename : files) {
	    System.out.println(filename);
	    String str = showsName.get(filename);
	    if (str == null) {
		System.out.println("Ignored file : " + filename);
		continue;
	    }
	    ratings = getRatings("shows/" + filename);
	    if (ratings == null)
		continue;
	    Integer i = currentSeason.get(filename);
	    if (i == null)
		i = 0;
	    for (; i < 17; i++) {
		String title = String.format(str, i);
		if (wiki.exists(title)) {
		    String text = wiki.getPageText(title);
		    String newText = addCanadianDate(text);
		    newText = addCanadianRatings(newText);
		    if (!newText.equals(text))
			try {
			    newText = newText.replace("{{SUI}}", "{{Suisse}}")
				    .replace("{{CH}}", "{{Suisse}}")
				    .replace("{{FRA}}", "{{France}}")
				    .replace("{{USA}}", "{{États-Unis}}")
				    .replace("{{BEL}}", "{{Belgique}}")
				    .replace("{{BE}}", "{{Belgique}}")
				    .replace("{{QUE}}", "{{Québec}}")
				    .replace("{{QC}}", "{{Québec}}");
			    // wiki.edit(title, newText,
			    // "bot: Ajout de l'audience au Canada");
			} catch (Exception e) {
			    e.printStackTrace();
			}
		}

	    }

	}
    }

    private static String addCanadianRatings(String text) {
	ArrayList<String> al = ParseUtils.getTemplates(
		"Saison de série télévisée/Épisode", text);
	int size = al.size();
	for (int i = 0; i < size; i++) {
	    String template = addCanadianRatingsToEpisode(al.get(i));
	    text = text.replace(al.get(i), template);

	}

	return text;
    }

    private static String addCanadianRatingsToEpisode(String template) {
	String date = ParseUtils.getTemplateParam(template,
		"première diffusion", false);
	if (date == null) {
	    System.out.println(template);
	    return template;
	}
	if (conainsCanadianRatings(template))
	    return template;
	String[] possibleDates = getCanadianOrAmericanDate(date);
	if (possibleDates == null)
	    return template;
	for (ListIterator<String> it = CanadianRatings.ratings.listIterator(); it
		.hasNext();) {
	    String epDate = it.next();
	    it.next();
	    String rating = it.next();
	    if (epDate.equals(possibleDates[0])
		    || epDate.equals(possibleDates[1])) {
		String oldValue = ParseUtils.getTemplateParam(template,
			"audience", false);
		if (oldValue == null || oldValue.trim().equals(""))
		    oldValue = "\n";
		template = ParseUtils.setTemplateParam(template, "audience",
			formatRatings(oldValue, rating.trim()) + "\n", true);
		return template;
	    }
	}
	return template;
    }

    private static String addCanadianDate(String text) {
	ArrayList<String> al = ParseUtils.getTemplates(
		"Saison de série télévisée/Épisode", text);
	for (int i = 0; i < al.size(); i++) {
	    text = text.replace(al.get(i), addCanadianDateToEpisode(al.get(i)));
	}
	return text;
    }

    private static CharSequence addCanadianDateToEpisode(String template) {
	String epDate = ParseUtils.getTemplateParam(template,
		"première diffusion", true);
	if (epDate == null)
	    return template;
	if (epDate.indexOf("Canada") != -1)
	    return template;
	String[] date = getCanadianOrAmericanDate(epDate);
	if (date == null)
	    return template;
	return template.replace(epDate,
		addCanadianDateAndBrodcaster(epDate, date));
    }

    private static CharSequence addCanadianDateAndBrodcaster(String epDate,
	    String[] date) {
	int canDate = 0;
	int i = ratings.indexOf(date[0]);
	if (i == -1) {
	    canDate = 1;
	    i = ratings.indexOf(date[1]);
	}
	if (i == -1)
	    return epDate;
	String brodcaster = ratings.get(i + 1);
	int index = epDate.indexOf("Canada");
	if (index == -1 && canDate == 1) {
	    epDate = "* {{Canada}} : {{date|" + date[0].replace("", "|")
		    + "|à la télévision}} sur [[" + brodcaster + "]]\n"
		    + epDate;
	}
	if (index == -1 && canDate == 0) {
	    epDate = epDate.replaceFirst(":", " / {{Canada}} :");
	    index = epDate.indexOf("\n");
	    String temp = epDate.substring(0, index) + " / [[" + brodcaster
		    + "]]";
	    epDate = temp + epDate.substring(index);
	}

	return epDate;
    }

    private static String formatRatings(String oldValue, String canadianRating) {
	if (oldValue == null || oldValue.trim().equals(""))
	    return canadianRating;
	oldValue = oldValue.trim();
	oldValue = oldValue.replaceAll(
		"<!-- \\* \\{\\{[a|A]udience\\|Canada.*?-->", "");
	int index = oldValue.indexOf("\n");
	if (index == -1)
	    return "\n" + oldValue + "\n" + canadianRating;
	return "\n" + oldValue.substring(0, index) + "\n" + canadianRating
		+ oldValue.substring(index);
    }

    private static boolean conainsCanadianRatings(String template) {
	String rating = ParseUtils.getTemplateParam(template, "audience", true);
	rating = ParseUtils.removeCommentsAndNoWikiText(rating);
	if (rating != null && rating.toLowerCase().indexOf("canada") != -1)
	    return true;
	return false;
    }

    private static String[] getCanadianOrAmericanDate(String dates) {
	if (dates == null)
	    return null;
	String date = dates.trim().toLowerCase();
	boolean canadian = date.contains("canada");
	String[] str = date.split("\\n");
	for (int i = 0; i < str.length; i++) {
	    if (canadian && str[i].indexOf("canada") != -1) {
		date = getDate(str[i]);
		break;
	    } else {
		if (!canadian && !str[i].trim().equals("")) {
		    date = getDate(str[i]);
		    break;
		}
	    }
	}
	if (date == null) {
	    System.out.println(dates);
	    return null;
	}
	int day = getDay(date);
	if (day == 0)
	    return null;
	else {
	    date = date.replace(day + " ", "");
	    String[] possibleDates = new String[2];
	    possibleDates[0] = String.valueOf(day) + " " + date;
	    possibleDates[1] = String.valueOf(day - 1) + " " + date;
	    return possibleDates;
	}
    }

    private static int getDay(String date) {
	String[] str = date.split(" ");
	if (str.length == 3)
	    try {
		return Integer.valueOf(str[0].trim());
	    } catch (Exception e) {
		return 0;
	    }
	else
	    return 0;
    }

    private static String getDate(String date) {
	ArrayList<String> al = ParseUtils.getTemplates("date", date);
	if (al.isEmpty()) {
	    Pattern p = Pattern
		    .compile("(\\d\\d?)[\\|\\s](janvier|février|mars|avril|mai|juin|juillet|août|septembre"
			    + "|octobre|novembre|décembre)[\\|\\s](\\d\\d\\d\\d)");
	    Matcher m = p.matcher(date);
	    if (m.find())
		return m.group(1) + " " + m.group(2) + " " + m.group(3);
	    else
		return null;
	} else {
	    date = al.get(0);
	    try {
		date = ParseUtils.getTemplateParam(date, 1).trim() + " "
			+ ParseUtils.getTemplateParam(date, 2).trim() + " "
			+ ParseUtils.getTemplateParam(date, 3).trim();
		return date;
	    } catch (Exception e) {
		return null;
	    }
	}
    }

    private static LinkedList<String> getRatings(String filename) {
	String str;
	try {
	    str = FileUtils.readFileToString(new File(filename));

	    if (str == null)
		return null;
	    LinkedList<String> list = new LinkedList<String>();
	    ListIterator<String> it = list.listIterator();
	    String[] s = str.split("\\n");
	    for (int i = 0; i < s.length; i = i + 4) {
		it.add(s[i]);
		it.add(s[i + 1]);
		it.add(s[i + 2]);
	    }

	    return list;
	} catch (IOException e) {
	    e.printStackTrace();
	    return null;
	}
    }

    private static void downloadRatingsFile(URL url) throws IOException {
	String filename = url.getFile().substring(
		url.getFile().lastIndexOf("/") + 1);
	FileUtils.copyURLToFile(url, new File("pdfs/" + filename));
    }

    private static URL contructUrl() {
	Calendar cal = Calendar.getInstance();
	cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
	cal.set(Calendar.DATE, cal.get(Calendar.DATE) - 14);
	String s = "http://www.bbm.ca/_documents/top_30_tv_programs_english/2013-14/2013-14_";
	String str = "_TV_ME_NationalTop30.pdf";
	int day = cal.get(Calendar.DATE);
	if (day < 10)
	    str = "_0" + day + str;
	else
	    str = "_" + day + str;
	int month = cal.get(Calendar.MONTH) + 1;
	if (month < 10)
	    str = "0" + month + str;
	else
	    str = month + str;
	s += str;

	try {
	    System.out.println(s);
	    return new URL(s);
	} catch (MalformedURLException e) {
	    e.printStackTrace();
	    return null;
	}
    }

    private static void cleanDirectory(String dirName) {
	File dir = new File(dirName);
	File[] files = dir.listFiles();
	for (File file : files) {
	    FileUtils.deleteQuietly(file);
	}
    }

    private static HashMap<String, String> loadShowsName() throws IOException {
	HashMap<String, String> map = new HashMap<String, String>();
	String s = FileUtils.readFileToString(new File("showsName"));
	String[] str = s.split("\\n");
	for (int i = 0; i < str.length; i++) {
	    int index = str[i].indexOf("=");
	    String key = str[i].substring(0, index).trim();
	    String value = str[i].substring(index + 1).trim();
	    map.put(key, value);
	}
	return map;

    }

    private static HashMap<String, Integer> getTVSerieCurrentSeason()
	    throws IOException {
	HashMap<String, Integer> map = new HashMap<String, Integer>();
	String s = FileUtils.readFileToString(new File("currentSeason"));
	String[] str = s.split("\\n");
	for (int i = 0; i < str.length; i++) {
	    int index = str[i].indexOf("=");
	    String key = str[i].substring(0, index).trim();
	    int value = Integer.valueOf(str[i].substring(index + 1).trim());
	    map.put(key, value);
	}
	return map;
    }

}
