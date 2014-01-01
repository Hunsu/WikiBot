package test;

import java.io.IOException;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.wikipedia.Wiki;
import org.wikiutils.ParseUtils;

public class TVSeries {

	private static Wiki wiki = new Wiki();

	public static void main(String[] args) {
		Wiki enWiki = new Wiki("en.wikipedia.org");
		Wiki frWiki = new Wiki("fr.wikipedia.org");

		try {
			//enWiki.login("Hunsu", "MegamiMonster");
			frWiki.login("Hunsu", "MegamiMonster");

			String frWikiTitle = "Saison 7 de Cold Case : Affaires classées";
			String enWikiTitle = frWiki.getArticleInSpecifLang(frWikiTitle,
					"en");

			String enArticle = enWiki.getPageText(enWikiTitle);
			String frArticle = frWiki.getPageText(frWikiTitle);
			String oldArticle = frArticle;

			// frArticle = FileUtils.readFileToString(new
			// File("s3.txt"),"UTF-8");

			/*
			 * frWiki.edit("Utilisateur:Hunsu/Brouillons", frArticle,
			 * "bot : tests");
			 */

			ArrayList<String> enAl = ParseUtils.getTemplates("Episode list",
					enArticle);
			ArrayList<String> frAl = ParseUtils.getTemplates(
					"Saison de série télévisée/Épisode", frArticle);

			// int enSize = enAl.size();
			int frSize = frAl.size();

			/*
			 * if (enSize != frSize) System.exit(0);
			 */

			for (int i = 0; i < frSize; i++) {
				String oldTemplate = frAl.get(i);
				String frEpisodeNumber = ParseUtils.getTemplateParam(
						oldTemplate, "numéro",true);
				String title = ParseUtils.getTemplateParam(oldTemplate,
						"titre original",true);
				LinkedHashMap<String, String> map = ParseUtils
						.getTemplateParametersWithValue(getEnTemplate(enAl,
								frEpisodeNumber, title));
				if (map == null)
					continue;
				String newTemplate = getMessingInfos(oldTemplate, map);
				/*
				 * String numero = String.valueOf(i+1) +" ("+String.valueOf(1)
				 * +"."; if(i<10) numero += "0"; numero +=
				 * String.valueOf(i+1)+")\n"; newTemplate =
				 * ParseUtils.setTemplateParam(newTemplate,"numéro", numero,
				 * true);
				 */

				// newTemplate = conewTemplate);

				frArticle = frArticle.replace(oldTemplate, newTemplate);
			}

			// frArticle =
			// frWiki.getPageText(frWikiTitle);"Utilisateur:Hunsu/Brouillons"

			frArticle = translateDate(frArticle, false);
			// FileUtils.writeStringToFile(new File("text"), frArticle);

			// frWiki.edit("Utilisateur:Hunsu/Brouillons", frArticle,
			// "bot : ajout d'infos depuis WPen");
            if(oldArticle.equalsIgnoreCase(frArticle))
            	return;
			frArticle = frArticle.replace("{{Références}}", "{{Références|colonnes=2}}");
			
			frWiki.edit(frWikiTitle, frArticle,
					"bot : ajout d'infos depuis WPen");

		} catch (FailedLoginException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LoginException e) {
			e.printStackTrace();
		}

	}

	private static String getEnTemplate(ArrayList<String> enAl,
			String frEpisodeNumber, String title) {
		if (frEpisodeNumber == null && title == null)
			return null;
		int size = enAl.size();
		for (int i = 0; i < size; i++) {
			String enEpisodeNumber = ParseUtils.getTemplateParam(enAl.get(i),
					"EpisodeNumber",true);
			if (enEpisodeNumber != null)
				enEpisodeNumber = enEpisodeNumber.trim();
			String enTitle = ParseUtils.getTemplateParam(enAl.get(i), "Title",true);
			if(enTitle == null)
				enTitle = ParseUtils.getTemplateParam(enAl.get(i), "RTitle",true);
			if(enTitle != null)
				enTitle = enTitle.toLowerCase();
			if (enTitle != null)
				enTitle = enTitle.trim();
			if ((enEpisodeNumber == null && enTitle == null)
					|| (enTitle == null && frEpisodeNumber == null)
					|| (enEpisodeNumber == null && title == null))
				return null;
			boolean test = enTitle.contains(title.toLowerCase());
			if ((frEpisodeNumber != null && frEpisodeNumber.startsWith(enEpisodeNumber + " ("))
					|| (enTitle != null && enTitle.contains(title.toLowerCase())))
				return enAl.get(i);
		}
		return null;
	}

	private static String getMessingInfos(String template,
			LinkedHashMap<String, String> map) {
		String title = ParseUtils.getTemplateParam(template, "titre original",true);
		String writtenBy = ParseUtils.getTemplateParam(template, "scénariste",true);
		String frEpisodeNumber = ParseUtils.getTemplateParam(template, "numéro",true);
		String enEpisodeNumber = ParseUtils.getTemplateParam(map, "EpisodeNumber",true);
		String enEpisodeNumber2 = ParseUtils.getTemplateParam(map, "EpisodeNumber2",true);
		String directedBy = ParseUtils
				.getTemplateParam(template, "réalisateur",true);
		String viewers = ParseUtils.getTemplateParam(template, "audience",true);
		String prodCode = ParseUtils.getTemplateParam(template,
				"code de production",true);
		String enProdCode = ParseUtils.getTemplateParam(map, "ProdCode",true);
		String originalAirDate = ParseUtils.getTemplateParam(template,
				"première diffusion",true);
		if((frEpisodeNumber == null || frEpisodeNumber.equals(""))){
			if(enEpisodeNumber != null){
				frEpisodeNumber = enEpisodeNumber;
				if(enEpisodeNumber2 != null){
					frEpisodeNumber += " (7-";
					int n = Integer.parseInt(enEpisodeNumber2);
					if(n < 10)
						frEpisodeNumber += "0";
					frEpisodeNumber += enEpisodeNumber2 +")\n";
					template = ParseUtils.setTemplateParam(template, "numéro", frEpisodeNumber, true);
				}
			}
		}
			
		if (title == null || title.trim().equals(""))
			template = ParseUtils.setTemplateParam(template, "titre original",
					ParseUtils.getTemplateParam(map, "Title",true) + "\n",
					true);
		if (writtenBy == null || writtenBy.trim().equals("")) {
			writtenBy = translateInternalLinks(ParseUtils
					.getTemplateParam(map, "WrittenBy",true).replace("&", "et")
					.replace("\"", "").trim());
			template = ParseUtils.setTemplateParam(template, "scénariste",

			writtenBy + "\n", true);
		}
		if (directedBy == null || directedBy.trim().equals("")) {
			directedBy = translateInternalLinks(ParseUtils
					.getTemplateParam(map, "DirectedBy",true).replace("&", "et")
					.replace("\"", "").replace(" and ", " et ").trim());
			template = ParseUtils.setTemplateParam(template, "réalisateur",

			directedBy + "\n", true);
		}
		String frViewers = getViewers(viewers);
		if (!frViewers.toLowerCase().contains("tats-unis")) {
			viewers = ParseUtils
					.removeCommentsAndNoWikiText(formatViewers(ParseUtils
							.getTemplateParam(map, "Viewers",true)));
			if (viewers == null)
				viewers = ParseUtils
						.removeCommentsAndNoWikiText(formatViewers(ParseUtils
								.getTemplateParam(map, "Aux4",true)));
			if (viewers != null)
				template = ParseUtils.setTemplateParam(template, "audience",
						(viewers + frViewers).replace("\n\n", "\n"), true);
		} else
			template = ParseUtils.setTemplateParam(template, "audience",
					frViewers.replace("\n\n", "\n"), true);
		if (enProdCode != null
				&& (prodCode == null || prodCode.trim().equals("")))
			template = ParseUtils.setTemplateParam(template,
					"code de production", enProdCode.trim() + "\n", true);
		if (originalAirDate == null || originalAirDate.trim().equals("")) {
			originalAirDate = "\n*{{États-Unis}} : "
					+ translateDate(
							ParseUtils.getTemplateParam(map, "Aux3",true)
									 + "\n", true);
			template = ParseUtils.setTemplateParam(template,
					"première diffusion", originalAirDate, true);
		}
		int index;
		String date = translateDate(
				ParseUtils.getTemplateParam(map, "OriginalAirDate",true),
				true);
		if ((index = originalAirDate.indexOf("{{États-Unis}} : sur")) != -1) {
			String temp = originalAirDate.substring(0, index + 17);
			temp += date;
			temp += originalAirDate.substring(index + 16);
			template = ParseUtils.setTemplateParam(template,
					"première diffusion", temp, true);
		}

		return template;
	}

	private static String getViewers(String viewers) {
		if (viewers == null)
			return "";
		Pattern p = Pattern.compile("[^\\|](\\d\\d?)[\\.,]?(\\d\\d?)?[^\\|]");
		String[] viewer = viewers.trim().split("\n");
		viewers = "";
		for (int i = 0; i < viewer.length; i++) {
			if (viewer[i].trim().equals(""))
				continue;
			if (viewer[i].indexOf("<ref") != -1)
				viewers += "\n" + viewer[i];
			else {
				if (!viewer[i].toLowerCase().contains("usa")
						&& !viewer[i].toLowerCase().contains("tats-unis")
						&& !viewer[i].toUpperCase().contains("É.-U.")
						&& (viewer[i].toLowerCase().contains("france") || viewer[i]
								.toUpperCase().contains("canada"))) {
					Matcher m = p.matcher(viewer[i]);
					if (m.find()) {
						if (viewer[i].toLowerCase().contains("france")
								&& viewer[i].toLowerCase().contains("million")) {
							viewers += "\n* {{Audience|France|" + m.group(1);
							if (m.group(2) != null)
								viewers += "." + m.group(2);
							viewers += "|M}} <small>(première diffusion)</small>\n";
						} else
							viewers += "\n" + viewer[i];
					} else
						viewers += "\n" + viewer[i];
				}
			}
		}
		if (!viewers.endsWith("\n"))
			return viewers + "\n";
		else
			return viewers;
	}

	private static String translateDate(String text, boolean useDateTemplate) {
		if (text == null)
			return "";
		// text = date.replace("{{Start date", "").replace("}}", "");
		Pattern p = Pattern
				.compile(
						"\\{\\{Start date\\|(\\d\\d\\d\\d)\\|(\\d\\d?)\\|(\\d\\d?)\\}\\}",
						Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(text);
		if (m.find()) {
			if (useDateTemplate)
				return m.replaceAll("{{date rapide|"
						+ m.group(3)
						+ "|"
						+ new DateFormatSymbols().getMonths()[Integer
								.parseInt(m.group(2)) - 1] + "|" + m.group(1)
						+ "|à la télévision}}");
			else
				return m.replaceAll(m.group(3)
						+ " "
						+ new DateFormatSymbols().getMonths()[Integer
								.parseInt(m.group(2)) - 1] + " " + m.group(1));
		}
		return text;
	}

	private static String formatViewers(String viewers) {
		if (viewers == null)
			return null;
		int index = viewers.indexOf("<ref");
		if (index == -1)
			return null;
		return citeweb("\n* {{Audience|États-Unis|"
				+ viewers.substring(0, index).trim() + "|M}}"
				+ viewers.substring(index).trim()
				+ " <small>(première diffusion)</small>\n");
	}

	private static String citeweb(String text) {
		ArrayList<String> al = ParseUtils.getTemplates("cite web", text);
		int size = al.size();
		for (int i = 0; i < size; i++) {
			String oldTemplate = al.get(i);
			String newTemplate = ParseUtils.removeTemplateParam(oldTemplate,
					"deadurl");
			newTemplate = ParseUtils.removeTemplateParam(newTemplate,
					"archiveurl");
			newTemplate = ParseUtils.removeTemplateParam(newTemplate,
					"archivedate");
			String title = ParseUtils.getTemplateParam(newTemplate, "title",false);
			Pattern p = Pattern.compile("([:\\s])'([^'])");
			Matcher m = p.matcher(title);
			StringBuffer buf = new StringBuffer();
			while (m.find())
				m.appendReplacement(buf, Matcher.quoteReplacement((m.group(1) + "''" + m.group(2))));
			m.appendTail(buf);
			title = buf.toString();
			p = Pattern.compile("([^'])'([\\s,])");
			m = p.matcher(title);
			buf = new StringBuffer();
			while (m.find())
				m.appendReplacement(buf, Matcher.quoteReplacement(m.group(1) + "''" + m.group(2)));
			m.appendTail(buf);
			title = buf.toString().replace("&", "and");
			newTemplate = ParseUtils.setTemplateParam(newTemplate, "title",
					title, false);
			newTemplate = LoggedInTests.correctDate(newTemplate);
			text = text.replace(oldTemplate, newTemplate);
		}
		return text;
	}

	private static String correctInternalLink(String internalLink) {
		if (internalLink.indexOf("|") == -1)
			return internalLink;
		internalLink = internalLink.substring(2, internalLink.length() - 2);
		String[] s = internalLink.split("\\|");
		if (s[0].equals(s[1]))
			return "[[" + s[0] + "]]";
		else
			return "[[" + s[0] + "|" + s[1] + "]]";
	}

	private static String translateInternalLinks(String text) {
		ArrayList<String> al = ParseUtils.getInternalLinks(text);
		int size = al.size();
		for (int i = 0; i < size; i++) {
			String enTitle = "";
			String title;
			String internalLink = al.get(i);
			int index;
			if ((index = internalLink.indexOf("|")) != -1) {
				enTitle = internalLink.substring(0, index);
				title = internalLink
						.substring(index + 1, internalLink.length());
			} else {
				enTitle = internalLink;
				title = enTitle;
			}

			String frTitle = wiki.getArticleInSpecifLang(enTitle, "fr");
			if (frTitle == null)
				frTitle = title;
			else
				frTitle = correctInternalLink("[["
						+ internalLink.replace(enTitle, frTitle) + "]]");

			text = text.replaceFirst(Pattern.quote("[[" + internalLink + "]]"),
					frTitle);
		}
		return text;
	}

	/*
	 * private static String correctDate(String template){ template =
	 * correctDate(template,"date","(\d\d\d\d)-(\d\d)") return template;
	 * 
	 * }
	 * 
	 * private static String correctDate(String Template,String param, String
	 * regex,String replacement) { String date =
	 * ParseUtils.getTemplateParam(Template, param); date =
	 * date.replaceAll(regex, replacement); Template =
	 * ParseUtils.setTemplateParam(Template, param, date); return Template; }
	 * 
	 * private static String translateCiteWeb(String citeWebTemplate) { String[]
	 * citeWebParameters = { "title","author", "last", "first", "last1",
	 * "first1", "last2", "first2", "website", "publisher", "accessdate" };
	 * String[] lienWebParameters = { "titre","auteur", "nom", "prénom", "nom1",
	 * "prénom1", "nom2", "prénom2", "site", "éditeur", "consulté le" }; for(int
	 * i=0;i<citeWebParameters.length;i++){ citeWebTemplate =
	 * ParseUtils.renameTemplateParam(citeWebTemplate, citeWebParameters[i],
	 * lienWebParameters[i]); } return citeWebTemplate; }
	 */

}
