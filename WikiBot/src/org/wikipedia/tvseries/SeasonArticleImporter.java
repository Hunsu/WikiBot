package org.wikipedia.tvseries;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.wikipedia.Wiki;
import org.wikiutils.ParseUtils;

import com.omertron.thetvdbapi.TheTVDBApi;
import com.omertron.thetvdbapi.model.Actor;
import com.omertron.thetvdbapi.model.Episode;
import com.omertron.thetvdbapi.model.Series;

public class SeasonArticleImporter {

	public static void main(String[] args) throws IOException{
		String frArticle = importEnArticle("Law & Order: Special Victims Unit (season 1)","1");
		FileUtils.writeStringToFile(new File("article"), frArticle);
	}



	private static String[] loadFrTitles(String filename) throws IOException{
		return FileUtils.readFileToString(new File(filename)).split("\\n");
	}


	private static String importEnArticle(String title, String season) throws IOException{
		Wiki wiki = new Wiki();
		String enArticle = wiki.getPageText(title);
		String[] frTitles = loadFrTitles(title);
		String frArticle = "";
		List<String> al = ParseUtils.getTemplates("Episode list/sublist", enArticle);

		int size = al.size() < frTitles.length ? al.size() : frTitles.length;

		for (int i = 0; i < size; i++) {
			LinkedHashMap<String,String> map = ParseUtils.getTemplateParametersWithValue(al.get(i));
			String frTemplate = translateTemplate(map,frTitles[i],season);
			frArticle += frTemplate + "\n";

		}



		return frArticle;
	}


	private static String translateTemplate(LinkedHashMap<String, String> map, String frTitle,String season) {
		String section = "=== Épisode %s : ''%s'' ===\n";

		String template = "{{Saison de série télévisée/Épisode\n}}";
		template = TVSeries.getMessingInfos(template, map, season);

		/*
		String title = ParseUtils.getTemplateParam(map, "Title", true);
		String epNumber = ParseUtils.getTemplateParam(map, "EpisodeNumber", true);*/
		String epNumber2 = ParseUtils.getTemplateParam(map, "EpisodeNumber2", true);
		/*String number = getNumber(epNumber,epNumber2,season);
		String writer = ParseUtils.getTemplateParam(map, "WrittenBy", true);
		String director = ParseUtils.getTemplateParam(map, "DirectedBy", true);
		String airDate = ParseUtils.getTemplateParam(map, "OriginalAirDate", true);
		String prodCode = ParseUtils.getTemplateParam(map, "ProdCode", true);
		section = String.format(section, epNumber2.trim(),frTitle);
		if(title != null)
			template = ParseUtils.setTemplateParam(template, "titre original", title +"\n", true);
		if(number != null)
			template = ParseUtils.setTemplateParam(template, "numéro", number+"\n", true);
		if(prodCode != null)
			template = ParseUtils.setTemplateParam(template, "code de production ", prodCode+"\n", true);
		if(writer != null)
			template = ParseUtils.setTemplateParam(template, "scénariste", writer+"\n", true);
		if(director != null)
			template = ParseUtils.setTemplateParam(template, "réalisateur", director + "\n", true);
		if(airDate != null)
			template = ParseUtils.setTemplateParam(template, "première diffusion ", airDate + "\n", true);
*/
		template = ParseUtils.setTemplateParam(template, "audience", "\n", true);
		template = ParseUtils.setTemplateParam(template, "invités", "\n", true);
		template = ParseUtils.setTemplateParam(template, "résumé", "\n", true);

		section = String.format(section,epNumber2, frTitle.trim());

		return section + template;
	}

	


	/*private static String getNumber(String epNumber, String epNumber2, String season) {
		if(epNumber == null || epNumber2 == null)
			return null;
		if(epNumber2.trim().length() == 1)
			epNumber2 = "0" + epNumber2.trim();
		return epNumber + " (" + season + "-" + epNumber2 + ")";
	}*/

}
