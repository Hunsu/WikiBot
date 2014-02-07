package org.wikipedia.maintenance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.wikipedia.Wiki;
import org.wikiutils.ParseUtils;

/**
 * The Class CiteWeb.
 */
public class CiteWeb {

	/**
	 * Instantiates a new cite web.
	 */
	public CiteWeb() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		Wiki wiki = new Wiki("fr.wikipedia.org");
		try {
			wiki.login("Hunsu", "MegamiMonster");

			String[] titles = wiki
					.getCategoryMembers("Page du modèle Lien web comportant une erreur");
			for (int i = 0; i < titles.length; i++) {
				if (titles[i].toLowerCase().startsWith("catégorie:"))
					continue;
				else {
					String text = wiki.getPageText(titles[i]);
					String oldText = text;
					ArrayList<String> al = ParseUtils.getTemplates("Lien web",
							text);
					for (int j = 0; j < al.size(); j++) {
						String template = processCiteWeb(al.get(j));
						template = correctLang(template);
						text = text.replace(al.get(j), template);
					}

					if (!text.equals(oldText))
						wiki.edit(titles[i], text,
									"bot: maintenance modèle lien web");
				}
			}
		} catch (Exception e) {
		}
	}

	/**
	 * Process cite web.
	 *
	 * @param template the template
	 * @return the string
	 */
	public static String processCiteWeb(String template) {
		LinkedHashMap<String, String> map = ParseUtils
				.getTemplateParametersWithValue(template);
		LinkedHashMap<String, String> map2 = new LinkedHashMap<String, String>();
		Scanner sc = new Scanner(System.in);
		boolean adjust = false;
		for (String key : map.keySet()) {
			if (map.get(key).endsWith("\n"))
				adjust = true;
			if (key.startsWith("ParamWithoutName")) {
				System.out.println(key + " " + map.get(key));
				String rd = sc.next();
				String val = sc.next();
				if (!rd.equals("sup"))
					map2.put(rd, val);
			} else
				map2.put(key, map.get(key));
		}
		String title = ParseUtils.getTemplateParam(map2, "titre", true);
		if (title == null || title.equals("")) {
			title = ParseUtils.getTemplateParam(map2, "title", true);
			String url = ParseUtils.getTemplateParam(map2, "url", true);
			template = ParseUtils.templateFromMap(map2);
			if (url == null || url.equals("")){
				sc.close();
				return template;
			}
			try {
				Document doc = Jsoup.connect(url).get();
				String titre = doc.title();
				template = ParseUtils.removeTemplateParam(template, "title");
				System.out.println(titre);
				String rd = sc.nextLine();
				if (rd.equals("sup")){
					sc.close();
					return template;
				}
				if (!rd.equals("ok"))
					titre = rd;
				template = ParseUtils.setTemplateParam(template, "titre",
						titre, adjust);
				sc.close();
			} catch (Exception e) {
				return template;
			}
		}
		return template;
	}


	/**
	 * Correct lang.
	 *
	 * @param template the template
	 * @return the string
	 */
	public static String correctLang(String template){
		String lang = ParseUtils.getTemplateParam(template, "langue", true);
		String param = "langue";
		if(lang == null){
			lang = ParseUtils.getTemplateParam(template,"lang",true);
			param = "lang";
		}
		if(lang == null){
			lang = ParseUtils.getTemplateParam(template, "language", true);
			param = "language";
		}
		if(lang == null){
			lang = ParseUtils.getTemplateParam(template, "lien langue", true);
			param = "lien langue";
		}
		if(lang == null)
			return template;

		HashMap<String,String> map =loadsErreurs();
		String correctLang = map.get(lang.toLowerCase());
		if(correctLang != null)
			template = ParseUtils.setTemplateParam(template, param, correctLang, false);
		return template;
	}

	/**
	 * Loads erreurs.
	 *
	 * @return the hash map
	 */
	private static HashMap<String, String> loadsErreurs() {
		HashMap<String,String> map =  new HashMap<String,String>();
		Wiki wiki = new Wiki("fr.wikipedia.org");
		try {
			String text = wiki.getPageText("Utilisateur:Hunsu/OutilsBot");
			int index = text.indexOf("== Code langue erronée ==");
			index = index + 25;
			int fin = text.indexOf("==",index);
			if(fin == -1)
				fin = text.length();
			text = text.substring(index, fin).trim();
			String[] errors = text.split("\n");
			for(int i=0;i<errors.length;i++){
				index = errors[i].indexOf("=");
				if(index == -1)
					continue;
				String key = errors[i].substring(0,index).trim();
				String value = errors[i].substring(index+1).trim();
				map.put(key, value);
			}
		} catch (Exception e) {
		}
		return map;
	}

}
