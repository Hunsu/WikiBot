package org.wikipedia.tvseries.providers;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Document.OutputSettings;
import org.wikipedia.tvseries.model.Episode;
import org.wikipedia.tvseries.model.Series;

public class TVRage {
	private String url = "http://www.tvrage.com/";

	public TVRage() {
	}

	public Series getSerie(String name) throws IOException {
		Series serie = new Series(name);
		StringBuilder sb = new StringBuilder(url);
		sb.append(URLEncoder.encode(name.replace(" ", "_"), "UTF-8"));
		Document doc = Jsoup.connect(sb.toString()).get();
		FileUtils.writeStringToFile(new File(name), doc.html());
		String info = getContent(doc);
		if (info == null)
			return null;
		// System.out.println(info);
		serie.setPoster(parsePoster(doc));
		serie.setOverview(parseOverview(doc));
		serie.setGenres(parseGenres(info));
		String[] str = parseNetworkAndCountry(info);
		serie.setNetwork(str[0]);
		serie.setCountry(str[1]);
		serie.setActors(getCast(name));
		serie.setFirstAired(ParseFirstAired(info));
		serie.setRuntime(parseRuntime(info));
		serie.setStatus(parseStatut(info));
		String[] tab = parseAirs(info);
		if (tab != null) {
			serie.setAirsDayOfWeek(tab[0]);
			serie.setAirsTime(tab[1]);
		}
		serie.setReccuringGuest(getReccuringGuest(name));

		return serie;

	}

	public Episode getEoisode(String serieName, String seasonNumber,
			String episodeNumberInSeason) throws IOException {
		Episode episode = new Episode();
		episode.setSeasonNumber(Integer.valueOf(seasonNumber));
		String[] tab = getEpisodeLinkAndAbsoluteNumber(serieName, seasonNumber,
				episodeNumberInSeason);
		episode.setEpisodeNumber(Integer.valueOf(episodeNumberInSeason));
		episode.setLink(tab[0]);
		episode.setAbsoluteNumber(tab[1]);
		episode.setEpisodeName(tab[3]);
		episode.setFirstAired(parseEpisodeAirDate(tab[2]));
		String link = tab[0];
		episode.setId(getId(link));
		Document doc = Jsoup.connect(link).get();
		String info = getInfo(doc);
		// System.out.println(info);
		episode.setDirectors(parseDirectors(info));
		episode.setWriters(parseWriters(info));
		episode.setTeleplayWriters(parseTeleplayWriters(info));
		episode.setStoryWriters(parseStoryWriters(info));
		episode.setGuestStars(getGuestStar(doc));
		episode.setEpImgFlag(parseEpisodeImage(doc));
		episode.setOverview(parseOverview(doc));

		return episode;
	}

	/*
	 * private String parseEpisodeSynopsis(Document doc) { Element element =
	 * doc.select("div.show_synopsis").first(); // get pretty printed html with
	 * preserved br and p tags String prettyPrintedBodyFragment =
	 * Jsoup.clean(element.html(), "", Whitelist.none().addTags("br", "p"), new
	 * OutputSettings().prettyPrint(true)); // get plain text with preserved
	 * line breaks by disabled prettyPrint return
	 * Jsoup.clean(prettyPrintedBodyFragment, "", Whitelist.none(), new
	 * OutputSettings().prettyPrint(false)); }
	 */

	private String parseEpisodeImage(Document doc) {
		Element element = doc.select("span.left.margin_top_bottom").first();
		element = element.getElementsByTag("img").first();
		if (element != null)
			return element.attr("src");
		return null;
	}

	private HashMap<String, String> getGuestStar(Document doc) {
		HashMap<String, String> map = new HashMap<String, String>();
		Elements elements = doc.select("div.grid_7_5.box.margin_top_bottom")
				.not("div.left").first().select("td");
		Iterator<Element> it = elements.iterator();
		for (; it.hasNext();) {
			Element element = it.next();
			String text = element.text();
			if (text.indexOf("Recurring") == -1) {
				String[] tab = text.split(" As ");
				if (tab.length == 2)
					map.put(tab[0].trim(), tab[1].trim());
			}
		}

		return map;
	}

	public String getInfo(String info, String text) {
		int beginIndex = text.indexOf(info);
		if (beginIndex == -1)
			return null;
		beginIndex += info.length() + 1;
		int endIndex = text.indexOf("<br />", beginIndex);
		if (endIndex == -1)
			endIndex = text.length();
		return text.substring(beginIndex, endIndex).replaceAll("<.*?>", "")
				.replaceAll("\\n\\s*", "");
	}

	private List<String> parseWriters(String text) {
		String info = getInfo("<b>Writer: </b>", text);
		List<String> writers = new ArrayList<String>();
		if (info != null)
			writers = getList(info, ",");
		return writers;
	}

	private List<String> parseTeleplayWriters(String text) {
		String info = getInfo("<b>Teleplay: </b>", text);
		List<String> list = new ArrayList<String>();
		if (info != null)
			list = getList(info, ",");
		return list;
	}

	private List<String> parseStoryWriters(String text) {
		String info = getInfo("<b>Story: </b>", text);
		List<String> list = new ArrayList<String>();
		if (info != null)
			list = getList(info, ",");
		return list;
	}

	private List<String> parseDirectors(String info) {
		info = getInfo("<b>Director: </b>", info);
		if (info == null)
			return null;
		return getList(info, ",");
	}

	private List<String> getList(String str, String separator) {
		String[] directors = str.trim().split(
				"\\s*" + Pattern.quote(separator) + "\\s*");

		return Arrays.asList(directors);
	}

	private Date parseEpisodeAirDate(String date) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy",
				Locale.ENGLISH);
		try {
			return formatter.parse(date);
		} catch (ParseException e) {
			return null;
		}
	}

	private String getInfo(Document doc) {
		Elements elements = doc.select("div.left.padding_left_right");
		if (elements != null && elements.size() > 0)
			return elements.first().html();
		return null;
	}

	private String getId(String link) {
		int index = link.lastIndexOf("/");
		return link.substring(index + 1);
	}

	private String[] getEpisodeLinkAndAbsoluteNumber(String serieName,
			String seasonNumber, String episodeNumberInSeason)
			throws IOException {
		StringBuilder sb = new StringBuilder(url);
		sb.append(URLEncoder.encode(serieName.replace(" ", "_"), "UTF-8"));
		sb.append("/episode_list/").append(seasonNumber);
		Document doc = Jsoup
				.connect(sb.toString())
				.userAgent(
						"Mozilla/5.0 (Ubuntu 13.10) Gecko/20100101 Firefox/27.0")
				.get();
		return getEpisodeLink(episodeNumberInSeason, doc);
	}

	private String[] getEpisodeLink(String episodeNumberInSeason, Document doc) {
		if (episodeNumberInSeason.length() == 1)
			episodeNumberInSeason = "0" + episodeNumberInSeason;
		Elements elements = doc.select("table.b");
		Element element = elements.first();
		Iterator<Element> iterator = element.select("td").iterator();
		iterator.next();
		iterator.next();
		iterator.next();
		iterator.next();
		Element e;
		for (; iterator.hasNext();) {
			e = iterator.next();
			element = iterator.next();
			if (element.text().contains("x" + episodeNumberInSeason)) {
				element = element.select("a").first();
				String[] tab = new String[4];
				tab[0] = url + element.attr("href").substring(1);
				tab[1] = e.text();
				tab[2] = iterator.next().text();
				tab[3] = iterator.next().text();
				return tab;
			}
			iterator.next();
			iterator.next();
			iterator.next();

		}

		return null;
	}

	private String parseOverview(Document doc) {
		Elements elements = doc.select("div.show_synopsis");
		Element elem = elements.get(0);
		elem.select("div").remove();
		elem.select("strong").remove();
		// get pretty printed html with preserved br and p tags
		String prettyPrintedBodyFragment = Jsoup.clean(elem.html(), "",
				Whitelist.none().addTags("br", "p"),
				new OutputSettings().prettyPrint(true));
		// get plain text with preserved line breaks by disabled prettyPrint
		return Jsoup
				.clean(prettyPrintedBodyFragment, "", Whitelist.none(),
						new OutputSettings().prettyPrint(false))
				.replaceAll(",\\n", ",").replaceAll(" +", " ")
				.replace("  *\\n", "\n");
	}

	private String parsePoster(Document doc) {
		Elements elements = doc.select("div.padding_bottom_10");
		Element elem = elements.get(0);
		String poster = elem.getElementsByTag("img").attr("src");
		return poster;
	}

	private String[] parseAirs(String text) {
		String info = getInfo("<b>Airs</b>:", text);
		if (info == null)
			return null;
		String[] airs = info.split(" at ");
		if (airs.length != 2)
			return null;
		return airs;
	}

	private Date ParseFirstAired(String text) {
		String date = getInfo("<b>Premiere</b>:", text);
		if (date == null)
			return null;
		return parseDate(date);
	}

	private Date parseDate(String date) {

		try {
			return new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH)
					.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	private HashMap<String, String> getCast(String name) throws IOException {
		StringBuilder sb = new StringBuilder(url);
		sb.append(name.replace(" ", "_")).append("/").append("cast");
		Document doc = Jsoup.connect(sb.toString()).get();
		Element elem = doc.select("table.b").first();
		Iterator<Element> ite = elem.select("td").not("td.b1").not("td.b2")
				.iterator();
		HashMap<String, String> actors = new HashMap<String, String>();
		for (; ite.hasNext();) {
			Element actor = ite.next();
			ite.next();
			Element role = ite.next();
			actors.put(actor.text().substring(1).trim(), role.text()
					.replaceAll("\\(.*?\\)", "").trim());

		}
		return actors;
	}

	private List<String> parseGenres(String text) {
		String info = getInfo("<b>Genre</b>:", text);
		List<String> list = new ArrayList<String>();
		if (info != null)
			list = getList(info, "|");
		return list;
	}

	private HashMap<String, String> getReccuringGuest(String name)
			throws IOException {
		StringBuilder sb = new StringBuilder(url);
		sb.append(name.replace(" ", "_")).append("/other/recurring");
		Document doc = Jsoup.connect(sb.toString()).get();
		Element elem = doc.select("table.b").first();
		Iterator<Element> ite = elem.select("td").iterator();
		HashMap<String, String> actors = new HashMap<String, String>();
		ite.next();
		ite.next();
		ite.next();
		ite.next();
		for (; ite.hasNext();) {
			Element actor = ite.next();
			ite.next();
			Element role = ite.next();
			ite.next();
			actors.put(actor.text().substring(1).trim(), role.text()
					.replaceAll("\\(.*?\\)", "").trim());

		}
		return actors;
	}

	private String[] parseNetworkAndCountry(String info) {
		int beginIndex = info.indexOf("<b>Network</b>:") + 15;
		int endIndex = info.indexOf("<br />", beginIndex);
		String s = info.substring(beginIndex, endIndex).replaceAll("<.*?>", "");
		beginIndex = s.indexOf("(");
		endIndex = s.indexOf(")", beginIndex);
		String[] str = new String[2];
		str[0] = s.substring(0, beginIndex).trim();
		str[1] = s.substring(beginIndex + 1, endIndex).trim();
		return str;
	}

	private String getContent(Document doc) {
		Elements elements = doc.select("div.grid_4.box.margin_top_bottom");
		if (elements != null && elements.size() != 0)
			return elements.get(0).html();
		return null;
	}

	private String parseRuntime(String text) {
		return getInfo("<b>Runtime</b>:", text);
	}

	private String parseStatut(String text) {
		return getInfo("<b>Status</b>:", text);
	}
}
