package org.wikipedia.botrequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.wikipedia.Wiki;
import org.wikipedia.login.Login;
import org.wikiutils.ParseUtils;

public class Request3 {

	public static void main(String[] args) throws FailedLoginException,
			IOException {
		process();

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

	private static void process() throws IOException, FailedLoginException {
		Wiki wiki = new Wiki("fr.wikipedia.org");
		try {
			wiki.login();
		} catch (Exception e) {
			Login login = new Login();
			wiki.login(login.getLogin(), login.getPassword());
		}
		String[] stubCatArticles = wiki.getCategoryMembers("Wikipédia:ébauche Suisse",true);
		String[] catArticle = wiki.getCategoryMembers("Canton du Valais",true);
		List<String> aList =  new LinkedList<String>(Arrays.asList(stubCatArticles));
		List<String> bList =  new LinkedList<String>(Arrays.asList(catArticle));
		if(true)
			return;
		aList.retainAll(bList);
		String[] titles = aList.toArray(new String[0]);
		for (int i = 0; i < titles.length; i++) {
			String article = wiki.getPageText(titles[i]);
			String newArticle = article;
			ArrayList<String> al = ParseUtils.getTemplates("ébauche",
					article);
			for (Iterator<String> iterator = al.iterator(); iterator.hasNext();) {
				String oldTemplate = iterator.next();
				String template = renmaeParamters(oldTemplate, false);
				newArticle = newArticle.replace(oldTemplate, template);
			}
			if (!article.equals(newArticle)) {
				try {
					newArticle = beforeSave(newArticle);
					wiki.edit(titles[i], newArticle,
							"bot: [[Wikipédia:Bot/Requêtes/2014/01#.C3.89bauche_Valais]]");
				} catch (LoginException e) {
					System.out.println(titles[i]);
					e.printStackTrace();
				}
			}
		}

	}

	private static String renmaeParamters(String template, boolean adjust) {
		LinkedHashMap<String,String> map = ParseUtils.getTemplateParametersWithValue(template);
		for(Entry<String,String> entry : map.entrySet()){
			String value = entry.getValue();
			if(value.toLowerCase().equals("suisse")){
				return ParseUtils.setTemplateParam(template, entry.getKey(), "Valais", false);

			}
		}
		return template;
	}

}
