package org.wikipedia.maintenance;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

import org.apache.commons.io.FileUtils;
import org.wikipedia.Wiki;
import org.wikipedia.Wiki.Revision;
import org.wikipedia.login.Login;
import org.wikipedia.tools.StringUtils;
import org.wikiutils.ParseUtils;

public class Oldid {

    private static Wiki wiki;
    private static Map<String, List<String>> equalsTitle = new HashMap<String, List<String>>();

    public static void main(String[] args) throws IOException, LoginException {
	wiki = new Wiki("fr.wikipedia.org");
	Login login = new Login();
	wiki.login(login.getBotLogin(), login.getPassword());
	wiki.setMarkBot(true);
	wiki.setMarkMinor(true);
	process();
    }

    public static <T> T[] concat(T[] first, T[] second) {
	T[] result = Arrays.copyOf(first, first.length + second.length);
	System.arraycopy(second, 0, result, first.length, second.length);
	return result;
    }

    private static void process() throws IOException, LoginException {
	String[] articles = wiki.getCategoryMembers(
		"Page avec un oldid invalide", 0);
	articles = concat(articles,
		FileUtils.readFileToString(new File("liste")).split("\\n"));
	//Arrays.sort(articles);
	for (String article : articles) {
	    try {
		article = "La Madone des Harpies";
		article = article.trim();
		System.out.println("Processing article : " + article);
		Revision rv = wiki.getTopRevision(article);
		String text = rv.getText();
		String oldText = text;
		List<String> templates = ParseUtils.getTemplates(
			"Traduction/Référence", text);
		templates.addAll(ParseUtils.getTemplates(
			"Traduction/référence", text));
		for (String template : templates) {
		    String oldTemplate = template;
		    String lang = ParseUtils.getTemplateParam(template, 1);
		    String title = ParseUtils.getTemplateParam(template, 2);
		    if (lang != null) {
			Wiki wiki2 = new Wiki(lang + ".wikipedia.org");
			String oldTitle = title;
			try {
			    title = getTitle(title, wiki2);
			} catch (Exception e) {
			    title = wiki.getArticleInSpecifLang(article, lang);
			    if (title == null) {
				System.out.println(article + " : Error! " + e);
				continue;
			    } else {
				title = getTitle(title, wiki2);
				if (equalsTitle.get(title) == null) {
				    List<String> l = new ArrayList<>();
				    l.add(oldTitle);
				    equalsTitle.put(title, l);
				} else
				    equalsTitle.get(title).add(oldTitle);

				System.out.println("Found new Title! " + title);
			    }
			}
			template = template.replace(oldTitle, title);
			rv = getRevision(article, oldTemplate);
			Calendar date = rv.getTimestamp();
			Revision rv2 = getRevision(title, date, wiki2);
			System.out.println(rv2.getRevid());
			String oldid = String.valueOf(rv2.getRevid());
			template = ParseUtils.setTemplateParam(template, "3",
				oldid, false);
			text = text.replace(oldTemplate, template);
		    }
		    }
		if (!text.equals(oldText)) {
			text = beforeSave(text);
			wiki.fastEdit(article, text, "Ajout oldid");
			equalsTitle.clear();
		}
	    } catch (IOException e) {
		e.printStackTrace();
		System.out.println(article + " : Error! " + e);
	    } catch (Exception e) {
		System.out.println(article + " : Error! " + e);
		e.printStackTrace();
	    }
	}

    }

    private static Revision getRevision(String title, Calendar date, Wiki wiki)
	    throws IOException {
	System.out.println(date.getTime());
	Revision[] rvs = wiki.getRevisions(title, date, 100);
	for (Revision rv : rvs) {
	    Calendar d = rv.getTimestamp();
	    if (d.compareTo(date) <= 0)
		return rv;
	}
	System.out.println("Error 10 is not enough" + rvs[0] + " "
		+ rvs[rvs.length - 1]);
	return null;
    }

    private static Revision getRevision(String article, String template)
	    throws IOException {
	Revision[] rvs = wiki.getRevisions(article, null, -1);
	return binarySearch(rvs, 0, rvs.length, template);
    }

    private static Revision binarySearch(Revision[] array, int left, int right,
	    String template) throws IOException {

	if (right - left < 10) {

	    boolean done = false;
	    Revision temp = array[left];
	    Revision rv = temp;
	    while (temp != null && !done) {
		String t = temp.getText();
		if (!containsTemplate(t, template))
		    done = true;
		else {
		    rv = temp;
		    temp = rv.getPrevious();
		}
	    }
	    System.out.println("Oldid : " + rv.getRevid());
	    return rv;
	}

	int middle = (left + right) / 2;
	Revision rv = array[middle];
	String text = rv.getText();
	if (!containsTemplate(text, template))
	    return binarySearch(array, left, middle - 1, template);
	else
	    return binarySearch(array, middle + 1, right, template);

    }

    private static String getTitle(String title, Wiki wiki) throws IOException {
	String oldTitle = title;
	Pattern p = Pattern.compile("#REDIRECT\\s*\\[\\[(.*?)\\]\\]");
	boolean done = false;
	while (!done) {
	    String text = wiki.getPageText(title);
	    Matcher m = p.matcher(text);
	    if (m.find()) {
		title = m.group(1);
	    } else
		return title;
	}
	return oldTitle;
    }

    private static boolean containsTemplate(String text, String template)
	    throws IOException {
	if (text.indexOf(template) != -1)
	    return true;
	List<String> templates = ParseUtils.getTemplates(
		"Traduction/Référence", text);
	templates.addAll(ParseUtils.getTemplates("Traduction/référence", text));
	String lang = ParseUtils.getTemplateParam(template, 1);
	if (templates.size() == 0)
	    return false;
	boolean test = false;
	for (String t : templates) {
	    if (test)
		return true;
	    LinkedHashMap<String, String> p1 = ParseUtils
		    .getTemplateParametersWithValue(t);
	    LinkedHashMap<String, String> p2 = ParseUtils
		    .getTemplateParametersWithValue(template);
	    for (String key : p1.keySet()) {
		if (key.equals("templateName"))
		    continue;
		String v1 = p1.get(key);
		String v2 = p2.get(key);
		if (v1 != null && v2 != null
			&& StringUtils.compare(v1.trim(), (v2.trim())) < .80) {

		    List<String> l1 = equalsTitle.get(v1);
		    if (l1 == null || !l1.contains(v2)) {
			l1 = equalsTitle.get(v2);
			if (l1 == null || !l1.contains(v2)) {
			    Wiki wiki = new Wiki(lang + ".wikipedia.org");
			    String t1 = getTitle(v1, wiki);
			    String t2 = getTitle(v2, wiki);
			    if (!t1.equals(t2)) {
				test = false;
				break;
			    } else if (equalsTitle.get(v1) == null) {
				List<String> l = new ArrayList<>();
				l.add(v2);
				equalsTitle.put(v1, l);
			    } else
				equalsTitle.get(v1).add(v2);
			}
		    }
		}
		if (v1 != null && v2 != null && v1.trim().equals(v2.trim()))
		    test = true;

	    }
	}
	return test;
    }

    private static String beforeSave(String text) {
	text = text.replace("{{pdf}} {{lien web", "{{lien web|format=pdf");
	text = text.replace("{{Pdf}} {{lien web", "{{lien web|format=pdf");
	text = text.replace("{{Pdf}} {{Lien web", "{{Lien web|format=pdf");
	text = text.replace("{{pdf}} {{Lien web", "{{Lien web|format=pdf");
	text = text.replace("{{en}} {{lien web", "{{lien web|langue=en");
	text = text.replace("{{en}} {{Lien web", "{{Lien web|langue=en");
	return text;
    }

}
