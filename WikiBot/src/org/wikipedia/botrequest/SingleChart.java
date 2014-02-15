package org.wikipedia.botrequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.wikipedia.Wiki;
import org.wikipedia.login.Login;
import org.wikiutils.ParseUtils;

public class SingleChart {

	public static void main(String[] args) throws FailedLoginException,
			IOException {
		renameSingleChartParameters();

	}

	private static void renameSingleChartParameters() throws IOException,
			FailedLoginException {
		Wiki wiki = new Wiki("fr.wikipedia.org");
		try {
			wiki.login();
		} catch (Exception e) {
			Login login = new Login();
			wiki.login(login.getLogin(), login.getPassword());
		}
		String[] titles = wiki.whatTranscludesHere("Singlechart");
		for (int i = 0; i < titles.length; i++) {
			String article = wiki.getPageText(titles[i]);
			String newArticle = article;
			ArrayList<String> al = ParseUtils.getTemplates("Singlechart",
					article);
			for (Iterator<String> iterator = al.iterator(); iterator.hasNext();) {
				String oldTemplate = iterator.next();
				String template = renmaeParamters(oldTemplate);
				newArticle = newArticle.replace(oldTemplate, template);
			}
			if (!article.equals(newArticle)) {
				try {
					wiki.edit(titles[i], newArticle,
							"bot: renommage des paramètres de modèle singlechart");
				} catch (LoginException e) {
					System.out.println(titles[i]);
					e.printStackTrace();
				}
			}
		}

	}

	private static String renmaeParamters(String template) {
		String[] enParameters = { "song", "songid", "artist", "artistid",
				"year", "week", "publishdate", "accessdate" };
		String[] frParameters = { "chanson", "chansonid", "artiste",
				"artisteid", "année", "semaine", "en ligne le", "consulté le" };
		for (int i = 0; i < frParameters.length; i++) {
			template = ParseUtils.renameTemplateParam(template,
					enParameters[i], frParameters[i], false);
		}
		String date = ParseUtils.getTemplateParam(template, "date", false);
		if (date != null) {
			date = translateDate(date);
			template = ParseUtils.setTemplateParam(template, "date", date, false);
		}

		date = ParseUtils.getTemplateParam(template, "consulté le", false);
		if (date != null) {
			date = translateDate(date);
			template = ParseUtils.setTemplateParam(template, "consulté le", date, false);
		}
		return template;
	}

	private static String translateDate(String date) {
		String[] frMonth = { "janvier", "février", "mars", "avril", "mai",
				"juin", "juillet", "août", "septembre", "octobre", "novembre",
				"décembre" };
		String[] enMonth = { "January", "February", "March", "April", "May",
				"June", "July", "August", "September", "October", "November",
				"December" };
		for (int i = 0; i < 12; i++) {
			date = date.replace(enMonth[i], frMonth[i]);
		}
		date = date
				.replaceAll(
						"(janvier|février|mars|avril|mai|juin|juillet|août|septembre|octobre|novembre|décembre) (\\d\\d?),",
						"$2 $1");
		return date;
	}
}
