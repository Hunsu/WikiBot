package org.wikipedia.botrequest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.apache.commons.io.FileUtils;
import org.wikipedia.Wiki;
import org.wikipedia.Wiki.Revision;
import org.wikipedia.login.Login;
import org.wikiutils.ParseUtils;

public class TemplateRename {

    private static Wiki wiki;
    private static ArrayList<String> errors;

    public static void main(String[] args) throws IOException,
	    FailedLoginException {
	wiki = new Wiki("fr.wikipedia.org");
	Login login = new Login();
	wiki.login(login.getBotLogin(), login.getPassword());
	wiki.setMarkBot(true);
	wiki.setThrottle(0);
	wiki.setMarkMinor(true);
	errors = new ArrayList<String>();
	process("Infobox Club de football", "Infobox Club sportif", "params");
	FileUtils.writeStringToFile(new File("errors"), errors.toString()
		.replaceAll(", ", "\n"), false);
    }

    private static void process(String template, String name, String file)
	    throws IOException {
	Map<String, String> params = getParamsToRename(file);
	String[] articles = wiki.whatTranscludesHere("Modèle:" + template, 0);
	Arrays.sort(articles);
	System.out.println(articles[articles.length - 1]);
	for (int i = 768; i < articles.length; i++) {
	    System.out.println("Processing article " + articles[i] + " (" + i
		    + "/" + articles.length + ")");
	    rename(articles[i], template, params, name);
	    i++;
	}
    }

    private static void rename(String article, String templateName,
	    Map<String, String> params, String name) throws IOException {
	String text = wiki.getPageText(article);
	String oldText = text;
	ArrayList<String> templates = ParseUtils.getTemplates(templateName,
		text);
	for (String template : templates) {
	    String oldTemplate = template;
	    if (name != null)
		template = ParseUtils.renameTemplate(template, name);
	    for (String param : params.keySet()) {
		if (params.get(param).equals("sup")) {
		    template = ParseUtils.removeTemplateParam(template, param);
		} else
		    template = ParseUtils.renameTemplateParam(template, param,
			    params.get(param), true);
	    }
	    if (!oldTemplate.equals(template)) {
		template = ParseUtils.formatTemplate(template);
		text = text.replace(oldTemplate, template);
	    }
	}
	if (!oldText.equals(text))
	    saveArticle(article, text, "Standardisation Infobox "
		    + "& remplacement - suppr. des paramètres obsolètes)");

    }

    private static String processTemplate(String template) {
	Pattern p = Pattern.compile("([^A-Za-z]?à[^A-Za-z])|(</?br\\s*/?>)");
	String param = ParseUtils.getTemplateParam(template, "naissance", true);
	if (param != null && !param.equals("")) {
	    Matcher m = p.matcher(param);
	    if (m.find()) {
		String date = param.substring(0, m.start() + 1);
		if (date.endsWith("<"))
		    date = date.substring(0, date.length() - 1);
		date = date.trim().replaceAll(
			"(^</?br\\s*/?>)|(</?br\\s*/?>$)", "");
		String place = param.substring(m.end());
		place = place.trim().replaceAll(
			"(^((à )|(</?br\\s*/?>)))|(</?br\\s*/?>$)", "");
		if (!date.trim().startsWith("à")) {
		    template = ParseUtils.renameTemplateParam(template,
			    "date naissance", "date de naissance", true);
		    template = ParseUtils.setTemplateParam(template,
			    "date de naissance", date, true);
		}
		template = ParseUtils.renameTemplateParam(template,
			"lieu naissance", "lieu de naissance", true);
		template = ParseUtils.setTemplateParam(template,
			"lieu de naissance", place, true);
		template = ParseUtils
			.removeTemplateParam(template, "naissance");
		template = ParseUtils.removeTemplateParam(template,
			"date naissance");
	    } else {
		param = ParseUtils.getTemplateParam(template,
			"date de naissance", true);
		if (param == null)
		    param = ParseUtils.getTemplateParam(template,
			    "date naissance", true);
		if (param == null || param.equals("")) {
		    param = ParseUtils.getTemplateParam(template, "naissance",
			    true);
		    if (param.startsWith("à") || !param.matches(".*?\\d.*?")) {
			param = ParseUtils.getTemplateParam(template,
				"lieu de naissance", true);
			if (param == null || param.equals("")) {
			    template = ParseUtils.removeTemplateParam(template,
				    "lieu de naissance");
			    template = ParseUtils.renameTemplateParam(template,
				    "naissance", "lieu de naissance", true);
			    template = ParseUtils.removeTemplateParam(template,
				    "lieu naissance");
			}
		    } else {
			param = ParseUtils.getTemplateParam(template,
				"date de naissance", true);
			if (param == null || param.equals("")) {
			    template = ParseUtils.removeTemplateParam(template,
				    "date de naissance");
			    template = ParseUtils.renameTemplateParam(template,
				    "naissance", "date de naissance", true);
			    template = ParseUtils.removeTemplateParam(template,
				    "date naissance");
			}
		    }
		}
	    }
	} else
	    template = ParseUtils.removeTemplateParam(template, "naissance");
	param = ParseUtils.getTemplateParam(template, "décès", true);
	if (param != null && !param.equals("")) {
	    Matcher m = p.matcher(param);
	    if (m.find()) {
		String date = param.substring(0, m.start() + 1);
		if (date.endsWith("<"))
		    date = date.substring(0, date.length() - 1);
		date = date.trim().replaceAll(
			"(^</?br\\s*/?>)|(</?br\\s*/?>$)", "");
		String place = param.substring(m.end());
		place = place.trim().replaceAll(
			"(^((à )|(</?br\\s*/?>)))|(</?br\\s*/?>$)", "");
		if (!date.trim().startsWith("à")) {
		    template = ParseUtils.renameTemplateParam(template,
			    "date décès", "date de décès", true);
		    template = ParseUtils.setTemplateParam(template,
			    "date de décès", date, true);
		}
		template = ParseUtils.renameTemplateParam(template,
			"lieu décès", "lieu de décès", true);
		template = ParseUtils.setTemplateParam(template,
			"lieu de décès", place, true);
		template = ParseUtils.removeTemplateParam(template, "décès");
	    } else {
		param = ParseUtils.getTemplateParam(template, "date de décès",
			true);
		if (param == null)
		    param = ParseUtils.getTemplateParam(template, "date décès",
			    true);
		if (param == null || param.equals("")) {
		    param = ParseUtils
			    .getTemplateParam(template, "décès", true);
		    if (param.startsWith("à") || !param.matches(".*?\\d.*?")) {
			param = ParseUtils.getTemplateParam(template,
				"lieu de décès", true);
			if (param == null || param.equals("")) {
			    template = ParseUtils.removeTemplateParam(template,
				    "lieu de décès");
			    template = ParseUtils.renameTemplateParam(template,
				    "décès", "lieu de décès", true);
			}
		    } else {
			param = ParseUtils.getTemplateParam(template,
				"date de décès", true);
			if (param == null || param.equals("")) {
			    template = ParseUtils.removeTemplateParam(template,
				    "date de décès");
			    template = ParseUtils.renameTemplateParam(template,
				    "décès", "date de décès", true);
			}
		    }
		}
	    }
	} else
	    template = ParseUtils.removeTemplateParam(template, "décès");
	return template;
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

    private static int numberOfThreadsRunning() {
	int nbRunning = 0;
	for (Thread t : Thread.getAllStackTraces().keySet()) {
	    if (t.getState() == Thread.State.RUNNABLE)
		nbRunning++;
	}
	return nbRunning;
    }

    private static void saveArticle(final String article, final String text,
	    final String comment) {
	System.out.println("Saving article " + article);
	final String content = beforeSave(text);
	while (numberOfThreadsRunning() > 10) {
	    try {
		Thread.sleep(5000);
	    } catch (InterruptedException e) {
	    }
	}
	Thread t = new Thread(new Runnable() {

	    @Override
	    public void run() {
		try {
		    wiki.edit(article, content, comment);
		} catch (LoginException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		} catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}

	    }
	});
	t.start();

    }

    private static Map<String, String> getParamsToRename(String file)
	    throws IOException {
	String[] lines = FileUtils.readFileToString(new File(file))
		.split("\\n");
	Map<String, String> map = new HashMap<>();
	for (String line : lines) {
	    String[] params = line.split("=");
	    map.put(params[0].trim(), params[1].trim());
	}
	return map;
    }
}
