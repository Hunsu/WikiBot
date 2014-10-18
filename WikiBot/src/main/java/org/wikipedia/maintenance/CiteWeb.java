package org.wikipedia.maintenance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.wikipedia.Wiki;
import org.wikipedia.login.Login;
import org.wikiutils.ParseUtils;

/**
 * The Class CiteWeb.
 */
public class CiteWeb {

    static HashMap<String, String> map = loadsErreurs();
    static List<String> params = Arrays.asList(new String[] { "langue",
	    "auteur1", "prénom1", "nom1", "postnom1", "lien auteur1",
	    "directeur1", "responsabilité1", "auteur", "auteur2", "prénom2",
	    "nom2", "postnom2", "lien auteur2", "directeur2",
	    "responsabilité2", "et al.", "traducteur", "photographe",
	    "champ libre", "titre", "sous-titre", "url", "format", "série",
	    "site", "lieu", "éditeur", "jour", "mois", "année", "date", "isbn",
	    "issn", "oclc", "pmid", "doi", "jstor", "numdam", "bibcode",
	    "math reviews", "zbl", "arxiv", "consulté le", "citation", "page",
	    "id", "libellé", "url", "titre", "consulté le", "site", "éditeur",
	    "auteur", "date", "langue", "année", "série", "en ligne le",
	    "mois", "jour", "id", "page", "lien auteur", "format", "citation",
	    "coauteurs", "prénom", "nom", "isbn", "périodique", "lang", "nom1",
	    "title", "lieu", "prénom1", "publisher", "accessdate", "work",
	    "pages", "Consulté le", "extrait", "website", "consulté", "nom2",
	    "auteur1", "prénom2", "sous-titre", "libellé", "author", "doi",
	    "passage", "last", "first", "auteur2", "editeur", "en ligne",
	    "language", "year", "auteurs", "lire en ligne", "issn", "Auteur",
	    "auteur institutionnel", "coauteur", "location", "quote",
	    "consultée le", "ISBN", "url texte", "author2", "coauthors",
	    "oclc", "month", "pmid", "responsabilité1", "lien auteur2",
	    "traducteur", "arxiv", "jstor", "traduction", "responsabilité2",
	    "bibcode", "directeur2", "et al.", "directeur1", "math reviews",
	    "photographe", "zbl", "last1", "first1", "first2", "last2", "pmc",
	    "PMID", "et alii", "DOI", "day", "lieu édition", "pmcid", "trad",
	    "zbmath" });

    /**
     * Instantiates a new cite web.
     */
    public CiteWeb() {
	// TODO Auto-generated constructor stub
    }

    /**
     * The main method.
     *
     * @param args
     *            the arguments
     */
    public static void main(String[] args) {
	Wiki wiki = new Wiki("fr.wikipedia.org");
	try {
	    Login login = new Login();
	    wiki.login(login.getLogin(), login.getPassword());

	    String[] titles = wiki.getCategoryMembers(
		    "Page du modèle Lien web comportant une erreur", 0);
	    for (int i = 0; i < titles.length; i++) {
		System.out.println("Reading article : " + titles[i]);
		String text = wiki.getPageText(titles[i]);
		String oldText = text;
		ArrayList<String> al = ParseUtils
			.getTemplates("Lien web", text);
		al.addAll(ParseUtils.getTemplates("Lien Web", text));
		for (int j = 0; j < al.size(); j++) {
		    System.out.println("Processing template : " + al.get(j));
		    String template = processCiteWeb(al.get(j));
		    if (template.isEmpty()) {
			text = text.replaceAll(
				"(< *ref.*?>.*)?" + Pattern.quote(al.get(j))
					+ "(.*?< */ref>)?", "");
		    } else {
			template = correctLang(template);
			text = text.replace(al.get(j), template);
		    }
		}

		if (!text.equals(oldText)) {
		    System.out.println("Saving articles : " + titles[i]);
		    wiki.fastEdit(titles[i], text,
			    "bot: maintenance modèle lien web");
		} else {
		    System.out.println("No Errors found !");
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /**
     * Process cite web.
     *
     * @param template
     *            the template
     * @return the string
     * @throws IOException
     */
    public static String processCiteWeb(String template) throws IOException {
	LinkedHashMap<String, String> map = correctParams(template);
	LinkedHashMap<String, String> map2 = new LinkedHashMap<String, String>();
	boolean adjust = false;
	BufferedReader sc = new BufferedReader(new InputStreamReader(System.in));
	for (String key : map.keySet()) {
	    if (map.get(key).endsWith("\n"))
		adjust = true;
	    if (key.startsWith("ParamWithoutName")) {
		System.out.println(key + " " + map.get(key));
		System.out.println("Reading key : ");
		String rd;
		try {
		    rd = sc.readLine();
		    System.out.println("Reading value : ");
		    String val;
		    if (!rd.equals("sup")) {
			val = sc.readLine();
			map2.put(rd, val);
		    }
		} catch (IOException e) {
		    map2.put(key, map.get(key));
		    e.printStackTrace();
		}
	    } else
		map2.put(key, map.get(key));
	}
	String title = ParseUtils.getTemplateParam(map2, "titre", true);
	template = ParseUtils.templateFromMap(map2);
	if (title == null || title.equals("")) {
	    System.out.println("title is null or \"\"" + title);
	    title = ParseUtils.getTemplateParam(map2, "title", true);
	    if (title != null && !title.equals("")) {
		System.out.println("Renaming title to titre");
		template = ParseUtils.renameTemplateParam(template, "title",
			"titre", adjust);
		return template;
	    }
	    String url = ParseUtils.getTemplateParam(map2, "url", true);
	    template = ParseUtils.templateFromMap(map2);
	    if (url == null || url.equals("")) {
		System.out.println("No url in this template!");
		System.out.println(template);
		String rd = sc.readLine();
		if (rd.equals("sup"))
		    return "";
		return template;
	    }
	    try {
		if (!url.trim().startsWith("http"))
		    url = "http://" + url;
		System.out.println("Url : " + url);
		Document doc = Jsoup
			.connect(url.trim())
			.userAgent(
				"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
			.get();
		String titre = doc.title();
		String[] possibleTitles = getPossibleTitles(titre);
		template = ParseUtils.removeTemplateParam(template, "title");
		printPossibleTitles(possibleTitles);
		String rd = sc.readLine();
		int i = -1;
		try {
		    i = Integer.parseInt(rd);
		} catch (Exception e) {
		    i = -1;
		}
		System.out.println("Value entred : " + rd);
		if (rd.equals("sup")) {
		    return "";
		}
		if (!rd.equals("ok"))
		    titre = rd;
		if (i != -1)
		    titre = possibleTitles[i - 1].trim();
		template = ParseUtils.setTemplateParam(template, "titre",
			titre, adjust);
	    } catch (Exception e) {
		String titre = "";
		System.out.println(e);
		String rd = sc.readLine();
		System.out.println("Value entred : " + rd);
		if (rd.equals("sup")) {
		    return template;
		}
		if (!rd.equals("ok"))
		    titre = rd;
		template = ParseUtils.setTemplateParam(template, "titre",
			titre, adjust);
		return template;
	    }
	} else {
	    System.out.println("title " + title);
	}
	return template;
    }

    private static LinkedHashMap<String, String> correctParams(String template) {
	BufferedReader sc = new BufferedReader(new InputStreamReader(System.in));
	LinkedHashMap<String, String> parms = ParseUtils
		.getTemplateParametersWithValue(template);
	LinkedHashMap<String, String> newT = new LinkedHashMap<>();
	for (String key : parms.keySet()) {
	    if (key.equals("templateName") || params.contains(key.trim()))
		newT.put(key, parms.get(key));
	    else {
		System.out.println("Wrong key : " + key + " Value : "
			+ parms.get(key));
		try {
		    String newKey = sc.readLine();
		    if (!newKey.equals("sup")) {
			String newVal = sc.readLine();
			if (newVal.equals("ok"))
			    newVal = parms.get(key);
			newT.put(newKey, newVal);
		    }
		} catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		    newT.put(key, parms.get(key));
		}
	    }

	}
	return newT;
    }

    private static void printPossibleTitles(String[] possibleTitles) {
	int i = 1;
	for (String title : possibleTitles) {
	    System.out.println(i + " : " + title.trim());
	    i++;
	}

    }

    private static String[] getPossibleTitles(String titre) {
	Set<String> list = new HashSet<>();
	list.addAll(Arrays.asList(titre.split("\\|")));
	list.addAll(Arrays.asList(titre.split("-")));
	list.addAll(Arrays.asList(titre.split("/")));
	list.add(titre);
	return list.toArray(new String[list.size()]);
    }

    /**
     * Correct lang.
     *
     * @param template
     *            the template
     * @return the string
     */
    public static String correctLang(String template) {
	String lang = ParseUtils.getTemplateParam(template, "langue", true);
	String param = "langue";
	if (lang == null) {
	    lang = ParseUtils.getTemplateParam(template, "lang", true);
	    param = "lang";
	}
	if (lang == null) {
	    lang = ParseUtils.getTemplateParam(template, "language", true);
	    param = "language";
	}
	if (lang == null) {
	    lang = ParseUtils.getTemplateParam(template, "lien langue", true);
	    param = "lien langue";
	}
	if (lang == null)
	    return template;
	lang = lang.trim().toLowerCase();
	Set<String> langues = new HashSet<>();
	langues.addAll(Arrays.asList(lang.split(",")));
	langues.addAll(Arrays.asList(lang.split("\\+")));
	langues.addAll(Arrays.asList(lang.split("/")));
	String correctLang = "";
	String oldLang = lang;
	for (String code : langues) {
	    correctLang = map.get(code.trim());
	    if (correctLang != null)
		lang = lang.replace(code, correctLang);
	}
	if (!oldLang.equals(lang))
	    template = ParseUtils
		    .setTemplateParam(template, param, lang, false);
	return template;
    }

    /**
     * Loads erreurs.
     *
     * @return the hash map
     */
    private static HashMap<String, String> loadsErreurs() {
	HashMap<String, String> map = new HashMap<String, String>();
	Wiki wiki = new Wiki("fr.wikipedia.org");
	try {
	    String text = wiki.getPageText("Utilisateur:Hunsu/OutilsBot");
	    int index = text.indexOf("== Code langue erronée ==");
	    index = index + 25;
	    int fin = text.indexOf("==", index);
	    if (fin == -1)
		fin = text.length();
	    text = text.substring(index, fin).trim();
	    String[] errors = text.split("\n");
	    for (int i = 0; i < errors.length; i++) {
		index = errors[i].indexOf("=");
		if (index == -1)
		    continue;
		String key = errors[i].substring(0, index).trim();
		String value = errors[i].substring(index + 1).trim();
		map.put(key, value);
	    }
	} catch (Exception e) {
	    System.out.println(e);
	}
	return map;
    }

}
