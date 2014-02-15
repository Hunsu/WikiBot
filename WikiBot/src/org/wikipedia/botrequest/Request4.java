package org.wikipedia.botrequest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.apache.commons.io.FileUtils;
import org.wikipedia.Wiki;
import org.wikipedia.login.Login;
import org.wikiutils.ParseUtils;

public class Request4 {

    private static String[] toReplace = { "masculin", "mâle", "male",
	    "femelle", "féminin", "homme", "femme" };
    private static String[] replacement = { "[[Homme|Masculin]]",
	    "[[Homme|Masculin]]", "[[Homme|Masculin]]", "[[Femme|Féminin]]",
	    "[[Femme|Féminin]]", "[[Homme|Masculin]]", "[[Femme|Féminin]]" };
    private static String[] notToDelete = { "[[Homme|Masculin]]",
	    "[[Femme|Féminin]]", "androgyne", "asexué", "transgenre",
	    "transsexuel", "hermaphrodite" };

    private static String log = "";
    private static String deleted = "";
    private static String ignored = "";

    static Wiki wiki = null;

    public static void main(String[] args) throws FailedLoginException,
	    IOException {
	process();
	FileUtils.writeStringToFile(new File("log.log"), log);
	FileUtils.writeStringToFile(new File("deleted"), deleted);
	FileUtils.writeStringToFile(new File("ignored"), ignored);

    }

    private static void process() throws FailedLoginException, IOException {
	wiki = new Wiki("fr.wikipedia.org");
	Login login = new Login();
	wiki.login(login.getLogin() + "Bot", login.getPassword());
	wiki.setMarkBot(true);
	String[] titles = wiki
		.whatTranscludesHere("Modèle:Infobox Personnage (fiction)");
	int i = 0;
	for (String title : titles) {
	    processArticle(title);
	    i++;
	}
	log += i;

    }

    private static void processArticle(String title) {
	try {
	    String text = wiki.getPageText(title);
	    String oldText = text;
	    log += title + "\n";
	    text = renmaeInfox(text);
	    if (!text.equals(oldText)) {
		text = beforeSave(text);
		if(wiki.hasNewMessages())
		    return;
		wiki.edit( title, text, "[[Wikipédia:Rbot#Modification_" +
		"du_contenu_du_param.C3.A8tre_genre_" +
		"dans_les_articles_munis_de_" +
		"Mod.C3.A8le:Infobox_Personnage_.28fiction.29|Renommage " +
		"de paramètre genre]]");

	    } else
		ignored += title + "\n";
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    private static String renmaeInfox(String text) {
	ArrayList<String> al = ParseUtils.getTemplates(
		"Infobox Personnage (fiction)", text);
	for (String template : al) {
	    log += "oldTemplate : " + template + "\n";
	    String newTemplate = prcessTemplate(template);
	    log += "newTemplate :" + newTemplate + "\n";
	    text = text.replace(template, newTemplate);
	}
	return text;
    }

    private static String prcessTemplate(String template) {
	if(ParseUtils.getTemplateParam(template, "genre", true) == null){
	    log += "No change made!\n";
	    return template;
	}
	template = ParseUtils.renameTemplateParam(template, "genre", " sexe",
		false);
	String value = ParseUtils.getTemplateParam(template, "sexe", false);
	log += "oldValue : " + value + "\n";
	if (value == null || value.trim().equals(""))
		return adjust(template);

	String newValue = getNewValue(value);
	template = ParseUtils.setTemplateParam(template, "sexe", newValue, true);

	return adjust(template);
    }

    private static String adjust(String template) {
	LinkedHashMap<String, String> map = ParseUtils
		.getTemplateParametersWithValue(template);
	LinkedHashMap<String, String> newMap = new LinkedHashMap<String, String>();
	for (Entry<String, String> entry : map.entrySet()) {
	    String key = entry.getKey();
	    String value = entry.getValue();
	    if (!value.endsWith(" "))
		value += " ";
	    if (!key.equals("templateName") && !key.startsWith(" "))
		key = " " + key;
	    if (!value.startsWith(" ") && !key.equals("templateName") && !key.startsWith("ParamWithoutName"))
		value = " " + value;
	    newMap.put(key, value);
	}
	return ParseUtils.templateFromMap(ParseUtils.adjust(newMap)).replace(
		"\n }}", "\n}}");
    }

    private static String getNewValue(String value) {
	int index = search(value, notToDelete);
	if (index != -1)
	    return value;
	index = search(value, toReplace);
	if (index != -1)
	    return replacement[index] + "\n ";
	deleted += "Value deleted : " + value + "\n";
	return "\n ";
    }

    private static int search(String value, String[] tabToSearch) {
	for (int i = 0; i < tabToSearch.length; i++) {
	    if (value.trim().toLowerCase()
		    .contains(tabToSearch[i].toLowerCase()))
		return i;
	}
	return -1;
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
