package org.wikipedia.tvseries;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.wikipedia.Wiki;
import org.wikipedia.tvseries.providers.Allocine;
import org.wikiutils.ParseUtils;

public class SeasonArticleImporter {
    private static Wiki frWiki;

    public static void main(String[] args) throws IOException {
	String frArticle = importEnArticle("List of Charlie's Angels episodes",
		"5", "Drôles de dames 1976");
	FileUtils.writeStringToFile(new File("article"), frArticle);
    }

    private static String[] loadFrTitles(String title, String season, String link)
	    throws IOException {
	try {
	    return FileUtils.readFileToString(new File(title)).split("\\n");
	} catch (Exception e) {
	    ArrayList<String> al = Allocine.getFrenchTitle(title, season, link);
	    String[] dsf = new String[al.size()];
	    return al.toArray(dsf);

	}
    }

    private static String importEnArticle(String enTitle, String season,
	    String tvSeries) throws IOException {
	Wiki wiki = new Wiki();
	frWiki = new Wiki("fr.wikipedia.org");
	String link = "http://www.allocine.fr/series/ficheserie-293/saison-960/";
	String[] frTitles = loadFrTitles(tvSeries, season,link);
	String frArticle = "";
	List<String> al = getTemplates(enTitle, season);

	int size = al.size();
	String frTitle = "";
	for (int i = 0; i < size; i++) {
	    LinkedHashMap<String, String> map = ParseUtils
		    .getTemplateParametersWithValue(al.get(i));
	    if (i < frTitles.length)
		frTitle = frTitles[i];
	    else
		frTitle = null;
	    String frTemplate = translateTemplate(map, frTitle, season, i+1);
	    frArticle += frTemplate + "\n";

	}
	String debutDate = getDate(al.get(0));
	String endDate = getDate(al.get(size - 1));
	String infoBox = createInfobox("Drôles de dames", debutDate, endDate,
		String.valueOf(size), season);
	return infoBox + "\n\n" + frArticle;
    }

    private static List<String> getTemplates(String title, String season)
	    throws IOException {
	Wiki wiki = new Wiki();
	String article = wiki.getPageText(title);
	int s = Integer.parseInt(season);
	if (title.startsWith("List of") && s != 0)
	    article = extractSeasonText(article, season);
	if (article != null) {
	    return ParseUtils.getTemplates("Episode list", article);
	}
	return null;
    }

    private static String extractSeasonText(String article, String season) {
	Pattern p = Pattern.compile("(==*)\\s*Season " + season + ".*?==");
	Matcher matcher = p.matcher(article);
	if (matcher.find()) {
	    String next = matcher.group(1) + " Season";
	    int index = article.indexOf(next, matcher.end());
	    if (index == -1)
		return article.substring(matcher.start());
	    else
		return article.substring(matcher.start(), index);
	}
	return null;
    }

    private static String getDate(String template) {
	String date = ParseUtils.getTemplateParam(template, "OriginalAirDate",
		true);
	if (date == null)
	    return "";

	return date.replaceAll(
		"\\{\\{Start date\\|(.*?)\\|(.*?)\\|(.*?)(\\|.*?)?\\}\\}",
		"{{Date|$3|$2|$1}}");
    }

    private static String translateTemplate(LinkedHashMap<String, String> map,
	    String frTitle, String season, int i) {
	String section = "=== Épisode %s : %s ===\n";

	String template = "{{Saison de série télévisée/Épisode\n}}";

	String epNumber2 = ParseUtils.getTemplateParam(map, "EpisodeNumber2",
		true);
	template = ParseUtils.setTemplateParam(template, "titre original",
		"\n", true);
	template = ParseUtils.setTemplateParam(template, "numéro", "\n", true);
	template = ParseUtils.setTemplateParam(template, "code de production ",
		"\n", true);
	template = ParseUtils.setTemplateParam(template, "réalisateur", "\n",
		true);
	template = ParseUtils.setTemplateParam(template, "scénariste", "\n",
		true);
	template = ParseUtils
		.setTemplateParam(template, "audience", "\n", true);
	template = ParseUtils.setTemplateParam(template, "invités", "\n", true);
	template = ParseUtils.setTemplateParam(template, "résumé", "\n", true);
	template = TVSeries.getMessingInfos(template, map, season);

	if (frTitle == null) {
	    String title = ParseUtils.getTemplateParam(template,
		    "titre original", true);
	    frTitle = "titre français inconnu (''" + title + "'')";
	}
	if (epNumber2 == null)
	    epNumber2 = String.valueOf(i);
	section = String.format(section, epNumber2, frTitle.trim());

	return section + template;
    }

    private static String createInfobox(String title, String debutDate,
	    String endDate, String nbEpisodes, String season)
	    throws IOException {
	String article = frWiki.getPageText(title);
	ArrayList<String> templates = ParseUtils.getTemplates(
		"Infobox Série télévisée", article);
	LinkedHashMap<String, String> templateMap;
	if (templates.size() == 1)
	    templateMap = ParseUtils.getTemplateParametersWithValue(
		    templates.get(0), true);
	else
	    return "";
	LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
	map.put("templateName", "Infobox Saison de série télévisée");
	map.put("nom", "Saison " + season + " de " + title);
	map.put("image", templateMap.get("image"));
	map.put("légende", "Logo de la série");
	map.put("série", "[[" + title + "]]");
	map.put("pays", templateMap.get("pays"));
	String chaine = templateMap.get("chaine");
	if (chaine == null)
	    chaine = templateMap.get("chaîne");
	if (chaine != null)
	    map.put("chaine", chaine);
	map.put("première diffusion", debutDate);
	map.put("dernière diffusion", endDate);
	map.put("nombre épisodes", nbEpisodes);
	return ParseUtils.formatTemplate(ParseUtils.templateFromMap(map));

    }
}
