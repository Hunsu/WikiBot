package org.wikipedia.botrequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.security.auth.login.FailedLoginException;
import org.wikiutils.ParseUtils;

public class Request14 extends Request {

    private static List<String> params = Arrays.asList(new String[] { "nom",
	    "image", "taille image", "légende", "pays", "nom de division",
	    "division", "nom de division2", "division2", "nom de division3",
	    "division3", "nom de division4", "division4", "nom de division5",
	    "division5", "nom de division6", "division6", "nom de division7",
	    "division7", "nom de division8", "division8", "nom de division9",
	    "division9", "nom de division10", "division10", "titre autorité",
	    "autorité", "titre autorité2", "autorité2", "titre autorité3",
	    "autorité3", "titre autorité4", "autorité4", "url", "époque",
	    "sites touristiques", "population", "année_pop", "revenu",
	    "fonction", "gare", "métro", "tram", "bus", "auto", "vélo", "cp",
	    "latitude", "longitude", "altitude", "superficie", "eau",
	    "imageloc", "taille imageloc", "légende imageloc",
	    "géolocalisation", "type coord" });

    public static void main(String[] args) throws FailedLoginException,
	    IOException {
	login();
	wiki.setThrottle(10000);
	process();

    }

    private static void process() throws IOException {
	String[] titles = wiki
		.whatTranscludesHere("Modèle:Infobox Quartier", 0);
	int i = 1;
	for (String title : titles) {
	    printProgress(title, i, titles.length);
	    i++;
	    String text = wiki.getPageText(title);
	    String oldText = text;
	    ArrayList<String> templates = ParseUtils.getTemplates(
		    "Infobox Quartier", text);
	    for (String template : templates) {
		String newTemplate = process(template);
		text = text.replace(template, newTemplate);
	    }
	    if (!text.equals(oldText)) {
		text = beforeSave(text);
	    } else
		continue;
	    try {
		wiki.fastEdit(title, text,
			"[[Wikipédia:Bot/Requêtes/2014/10#Mod.C3.A8le:"
				+ "Infobox_Quartier|Harmonisation Infobox]]");
	    } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
    }

    private static String process(String template) {
	List<String> devision = Arrays.asList(new String[] { "état",
		"collectivité", "région", "département", "province", "ville",
		"arrondissement", "secteur", "canton", "district", "comitat",
		"comté", "paroisse", "arrondissement municipal", "freguesia" });
	List<String> lienDevision = Arrays.asList(new String[] { "lien état",
		"lien collectivité", "lien région", "lien département",
		"lien province", "lien ville", "lien arrondissement",
		"lien secteur", "lien canton", "lien district", "lien comitat",
		"lien comté", "lien paroisse", "lien arrondissement municipal",
		"Freguesia" });
	List<String> autorite = Arrays.asList(new String[] { "maire délégué",
		"conseil", "stadsdeelwethouder", "stadsdeelleider" });
	List<String> titreAuto = Arrays
		.asList(new String[] {
			"[[Commune associée|Maire délégué]]",
			"[[Conseil de quartier]]",
			"Conseil d'arrondissement <small>(''Stadsdeelwethouder'')</small>",
			"Chef d'arrondissement <small>(''Stadsdeelleider'')</small>" });

	/*
	 * template = "{{Infobox Quartier" + "  | état               = " +
	 * "  | collectivité       = " +
	 * "  | région             = [[Voïvodie de Mazovie|Mazovie]]" +
	 * "  | lien région        = Voïvodie" + "  | arrondissement     = " +
	 * "  | comté              = " + "  | paroisse           = " +
	 * "  | district           = " + "  | canton             = " +
	 * "  | lien canton        = " + "  | ville              = [[Varsovie]]"
	 * + "  | arrondissement municipal = " + "  | freguesia          = " +
	 * "  | maire délégué      = Rafał Miastowski" +
	 * "  | conseil            = " + " }}";
	 */
	int n = 1;
	String dev = "division";
	String dev1;
	String devName = "nom de division";
	String devName1;

	String auto = "autorité";
	String auto1;
	String autoName = "titre autorité";
	String autoName1;
	for (int i = 0; i < devision.size(); i++) {
	    String param = ParseUtils.getTemplateParam(template,
		    devision.get(i), false);
	    String t = ParseUtils.removeCommentsAndNoWikiText(param);
	    if (t != null && !t.trim().isEmpty()) {
		if (n > 1) {
		    dev1 = dev + n;
		    devName1 = devName + n;
		} else {
		    dev1 = dev;
		    devName1 = devName;
		}
		n++;
		if (n == 11) {
		    System.out.println("Error !");
		}
		String lien = ParseUtils.getTemplateParam(template,
			lienDevision.get(i), false);
		lien = ParseUtils.removeCommentsAndNoWikiText(lien);
		String value;
		if (lien != null && !lien.trim().isEmpty()) {
		    if (lien.toLowerCase()
			    .equals(devision.get(i).toLowerCase()))
			value = "[[" + capitalize(lien.trim()) + "]]";
		    else
			value = "[[" + lien.trim() + "|"
				+ capitalize(devision.get(i)) + "]]";
		} else {
		    value = "[[" + capitalize(devision.get(i)) + "]]";
		}
		template = ParseUtils.setTemplateParam(template, devName1,
			value, false);
		template = ParseUtils.setTemplateParam(template, dev1, param,
			false);
	    }
	    template = ParseUtils
		    .removeTemplateParam(template, devision.get(i));
	    template = ParseUtils.removeTemplateParam(template,
		    lienDevision.get(i));

	}
	n = 1;
	for (int i = 0; i < autorite.size(); i++) {
	    String param = ParseUtils.getTemplateParam(template,
		    autorite.get(i), false);
	    String t = ParseUtils.removeCommentsAndNoWikiText(param);
	    if (t != null && !t.trim().isEmpty()) {
		if (n > 1) {
		    auto1 = auto + n;
		    autoName1 = autoName + n;
		} else {
		    auto1 = auto;
		    autoName1 = autoName;
		}
		n++;
		template = ParseUtils.setTemplateParam(template, autoName1,
			titreAuto.get(i), false);
		template = ParseUtils.setTemplateParam(template, auto1, param,
			false);
	    }
	    template = ParseUtils
		    .removeTemplateParam(template, autorite.get(i));

	}
	return ParseUtils.formatTemplate(template, params,
		" key                 = value\n");
    }

    public static String capitalize(String s) {
	return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
