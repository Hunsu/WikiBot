package org.wikipedia.botrequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.wikipedia.Wiki;
import org.wikipedia.login.Login;
import org.wikiutils.ParseUtils;

public class Request2 {

	private static String[] oldParameters = { "nom_parc", "ancien_nom",
			"nouveau_nom", "logo_parc", "taille", "taille_photo",
			"Date_ouverture", "Pays", "Propriétaire", "Type parc", "site_web","Domaine", "legende" };
	private static String[] newParameters = { " nom", " ancien nom",
			" nouveau nom", " logo", " taille logo", " taille photo",
			" date ouverture", " pays", " propriétaire", " type", " site web","domaine", " légende" };

	public static void main(String[] args) throws FailedLoginException,
			IOException {
		renameSingleChartParameters("Infobox Parc de loisirs", true);

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

	private static void renameSingleChartParameters(String templateName,
			boolean adjust) throws IOException, FailedLoginException {
		Wiki wiki = new Wiki("fr.wikipedia.org");
		try {
			wiki.login();
		} catch (Exception e) {
			Login login = new Login();
			wiki.login(login.getLogin(), login.getPassword());
		}
		String[] titles = wiki.whatTranscludesHere(templateName);
		for (int i = 0; i < titles.length; i++) {
			String article = wiki.getPageText(titles[i]);
			String newArticle = article;
			ArrayList<String> al = ParseUtils.getTemplates(templateName,
					article);
			for (Iterator<String> iterator = al.iterator(); iterator.hasNext();) {
				String oldTemplate = iterator.next();
				String template = renmaeParamters(oldTemplate, adjust);
				template = orgnize(template);
				newArticle = newArticle.replace(oldTemplate, template);
			}
			if (!article.equals(newArticle)) {
				try {
					newArticle = beforeSave(newArticle);
					wiki.edit(titles[i], newArticle,
							"bot: Mise à jour des paramètres de l'Infobox Parc de loisirs");
				} catch (LoginException e) {
					System.out.println(titles[i]);
					e.printStackTrace();
				}
			}
		}

	}

	private static String orgnize(String template) {
		LinkedHashMap<String,String> map = ParseUtils.getTemplateParametersWithValue(template);
		LinkedHashMap<String,String> newMap = new LinkedHashMap<String,String>();
		for (Entry<String,String> entry : map.entrySet()) {
			String param = entry.getKey();
			if(!param.startsWith("ParamWithoutName") && !param.equals("templateName") && !param.startsWith(" "))
				param = " " + param;
			newMap.put(param, entry.getValue());
		}
		return ParseUtils.templateFromMap(ParseUtils.adjust(newMap));
	}

	private static String renmaeParamters(String template, boolean adjust) {
		for (int i = 0; i < newParameters.length; i++) {
			if (newParameters[i] == null)
				template = ParseUtils.removeTemplateParam(template,
						oldParameters[i]);
			else
				template = ParseUtils.renameTemplateParam(template,
						oldParameters[i], newParameters[i], adjust);
		}
		return template;
	}

}
