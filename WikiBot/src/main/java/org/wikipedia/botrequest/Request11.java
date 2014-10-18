package org.wikipedia.botrequest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

import org.apache.commons.io.FileUtils;
import org.wikiutils.ParseUtils;

public class Request11 extends Request {

    public static void main(String[] args) throws IOException, LoginException,
	    InterruptedException {
	login();
	process();
    }

    private static void process() throws IOException, LoginException,
	    InterruptedException {
	String[] titles = FileUtils.readFileToString(new File("req11")).split(
		"\\n");
	int i = 1;
	List<String> errors = new ArrayList<String>();
	for (String title : titles) {
	    title = title.split(" - ")[0];
	    System.out.println("processing " + title + "(" + i + "/"
		    + titles.length + ")");
	    ++i;
	    String text = wiki.getPageText(title);
	    ArrayList<String> templates = ParseUtils.getTemplates(
		    "Infobox Langue", text);
	    for (String template : templates) {
		String oldTemplate = template;
		String iso3 = ParseUtils.getTemplateParam(template, "iso3",
			true);
		if (iso3 != null && !iso3.isEmpty() && iso3.length() != 3) {
		    System.out.println(iso3);
		    String old = iso3;
		    String iso3Code = removeRef(iso3);
		    if(iso3Code.equals(old) || iso3Code.trim().length() != 3)
			continue;
		    // errors.add(title + " - " + iso3 + " \n");
			template = ParseUtils.setTemplateParam(template,
				"iso3", iso3Code, false);
			template = ParseUtils.formatTemplate(template);
			text = text.replace(oldTemplate, template);
			text = beforeSave(text);
			wiki.fastEdit(
				title,
				text,
				"[[Wikipédia:Bot/Requêtes/2014/08#Prise"
					+ "_en_charge_du_lien_ISO_639-3_dans_l.27Infobox_Langue"
					+ "|Prise en charge du lien ISO 639-3 dans l'Infobox Langue]]");


		}
	    }
	}
	FileUtils.writeStringToFile(new File("req11"), errors.toString());

    }

    private static String removeRef(String iso3) {
	Pattern p = Pattern.compile("<ref.*?>(.*?</ref>)?");
	Matcher m = p.matcher(iso3);
	if(m.find()){
	    iso3 = iso3.replace(m.group(), "");
	}
	return iso3;
    }

}
