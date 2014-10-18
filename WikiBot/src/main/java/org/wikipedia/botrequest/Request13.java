package org.wikipedia.botrequest;

import java.beans.Introspector;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.apache.commons.io.FileUtils;
import org.wikiutils.ParseUtils;

public class Request13 extends Request {
    static Set<String> infoBox = new HashSet<>();

    public static void main(String[] args) throws FailedLoginException,
	    IOException {
	login();
	wiki.setThrottle(5);
	processLinks();

    }

    private static void processLinks() throws IOException {
	String[] templates = FileUtils.readFileToString(new File("templates"))
		.split("\\n");
	int j = 0;
	for (String template : templates) {
	    printProgress(template, j++, templates.length);
	    String old = template.split("=")[0].trim();
	    String[] modeles = wiki.whatLinksHere(old, 10);
	    String[] titles = wiki.whatLinksHere(old, 0);
	    List<String> articles = new ArrayList<>();
	    articles.addAll(Arrays.asList(modeles));
	    articles = addTitles(articles, titles);
	    int i = 0;
	    for (String title : articles) {
		//title = "Yamoussoukro";
		printProgress(title, i, titles.length);
		i++;
		if (infoBox.size() % 10 == 0 && infoBox.size() > 0) {
		    FileUtils.writeStringToFile(new File("infBox"),
			    infoBox.toString());
		}
		String text = wiki.getPageText(title);
		String oldText = text;
		text = processText(text, templates);
		if (!oldText.equals(text)) {
		    text = beforeSave(text);
		} else {
		    System.out.println("Error !");
		    try {
			int k = text.toLowerCase().indexOf("{{infobox");
			if (k != -1) {
			    int m = text.indexOf("\n", k);
			    String oldTitle = title;
			    title = "Modèle:" + text.substring(k + 2, m);
			    if (!infoBox.contains(title)) {
				text = processInfobox(title, templates);
				infoBox.add(title);
				System.out.println(title);
			    } else
				title = oldTitle;

			}
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		}
		try {
		    wiki.fastEdit(
			    title,
			    text,
			    "[[Wikip%C3%A9dia:Bot/"
				    + "Requ%C3%AAtes#Rediriger_les_liens_vers_des_articles"
				    + "_et_palettes_langues_par_pays|Requête bot]]");
		} catch (InterruptedException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }
	}

    }

    private static List<String> addTitles(List<String> articles, String[] titles) {
	for (String title : titles)
	    articles.add(title);
	return articles;
    }

    private static String processInfobox(String info, String[] templates)
	    throws IOException {
	String text = wiki.getPageText(info);
	return processText(text, templates);

    }

    private static String processText(String text, String[] templates)
	    throws IOException {
	ArrayList<String> arD = ParseUtils.getTemplates("See also",
		text);
	arD.addAll(ParseUtils.getTemplates("Article connexe", text));
	for (String template : templates) {
	    String old = template.split("=")[0].trim();
	    String newT = template.split("=")[1].trim();
	    for (String ar : arD)
		text = text.replace(ar, ar.replace(old, newT));
	    text = replaceLink(text, old, newT);
	    text = replaceLink(text, Introspector.decapitalize(old),
		    Introspector.decapitalize(newT));
	}
	return text;
    }

    private static String replaceLink(String text, String old, String newT) {
	Pattern p = Pattern.compile("\\[\\[ *(" + Pattern.quote(old)
		+ ") *(#.*?)?(\\|.*?)?\\]\\]");
	Matcher m = p.matcher(text);
	while (m.find()) {
	    String to = m.group();
	    String rep = to.replace(old, newT);
	    System.out.println(to + " --> " + rep);
	    text = text.replace(to, rep);
	}

	return text;
    }

    private static void process() throws IOException {
	String[] templates = FileUtils.readFileToString(new File("templates"))
		.split("\\n");
	int j = 0;
	for (String template : templates) {
	    printProgress(template, j++, templates.length);
	    String old = template.split("=")[0].trim();
	    String newT = template.split("=")[1].trim();
	    String[] titles = wiki.whatTranscludesHere("Modèle:" + old, 0);
	    int i = 0;
	    for (String title : titles) {
		// title = "Langues en Algérie";
		printProgress(title, i, titles.length);
		i++;
		String text = wiki.getPageText(title);
		String oldText = text;
		text = text.replace("{{" + old + "}}", "{{" + newT + "}}");
		ArrayList<String> palettes = ParseUtils.getTemplates("Palette",
			text);
		if (palettes.size() != 0) {
		    String palette = palettes.get(0);
		    old = old.replace("Palette ", "");
		    newT = newT.replace("Palette ", "");
		    text = text.replace(palette, palette.replace(old, newT));
		}
		if (!oldText.equals(text)) {
		    text = beforeSave(text);
		    try {
			wiki.fastEdit(title, text, old + " --> " + newT);
		    } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		} else
		    System.out.println("Error !");
	    }
	}

    }

}
