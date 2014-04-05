package org.wikipedia.tvseries.providers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class IMDB {
    private String url = "http://www.imdb.com/title/";
    private String fullCast = "/fullcredits?ref_=tt_cl_sm#cast";
    private String episodeCast = "/?ref_=tt_ep_ep200";
    private String season = "/episodes?season=";
    private String key;
    private LinkedHashMap<String, Info> seriesFullCast;
    private LinkedHashMap<String, String> seasonEpisodes;
    private LinkedHashMap<String, Info> guests;

    public class Info {
	private String role;
	private String year;
	private int nbEpisodes;

	public Info(String role, String year, int nbEpisodes) {
	    this.role = role;
	    this.year = year;
	    this.nbEpisodes = nbEpisodes;
	}

	/**
	 * @return the role
	 */
	public String getRole() {
	    return role;
	}

	/**
	 * @param role
	 *            the role to set
	 */
	public void setRole(String role) {
	    this.role = role;
	}

	/**
	 * @return the year
	 */
	public String getYear() {
	    return year;
	}

	/**
	 * @param year
	 *            the year to set
	 */
	public void setYear(String year) {
	    this.year = year;
	}

	/**
	 * @return the nbEpisodes
	 */
	public int getNbEpisodes() {
	    return nbEpisodes;
	}

	/**
	 * @param nbEpisodes
	 *            the nbEpisodes to set
	 */
	public void setNbEpisodes(int nbEpisodes) {
	    this.nbEpisodes = nbEpisodes;
	}

	public String toString() {
	    return (nbEpisodes != -1 && year != null) ? role + " ("
		    + nbEpisodes + ", " + year + ")" : role;
	}
    }

    public IMDB(String key) {
	this.key = key;
    }

    public HashMap<String, Info> getFullCast() throws IOException {
	if (seriesFullCast != null)
	    return seriesFullCast;
	String url = this.url + key + fullCast;
	Document doc = Jsoup.connect(url).get();
	Element castTable = doc.select("table.cast_list").first();
	seriesFullCast = getFullCast(castTable);
	return seriesFullCast;
    }

    private LinkedHashMap<String, Info> getFullCast(Element castTable) {
	LinkedHashMap<String, Info> cast = new LinkedHashMap<String, IMDB.Info>();
	if(castTable == null)
	    return cast;
	Elements lines = castTable.select("tr.odd, tr.even");
	Iterator<Element> it = lines.iterator();
	for (; it.hasNext();) {
	    Iterator<Element> colsIt = it.next()
		    .select("td.itemprop, td.character").iterator();
	    for (; colsIt.hasNext();) {
		String actor = colsIt.next().text();
		Info info = getInfo(colsIt.next());
		cast.put(actor, info);
	    }

	}
	return cast;
    }

    private Info getInfo(Element element) {
	String text = element.text();
	text = text.replace(" (uncredited)", "");
	int index = text.indexOf("(");
	String role = "";
	String year;
	int nbEpisodes;
	if (index == -1) {
	    role = text;
	    year = null;
	    nbEpisodes = -1;
	} else {
	    role = text.substring(0, index);
	    text = text.replace(role, "");
	    try {
		index = text.indexOf(" ");
		if (index != -1) {
		    nbEpisodes = Integer.valueOf(text.substring(1, index));
		    year = text.substring(text.indexOf(", ") + 2,
			    text.length() - 1);
		} else{
		    nbEpisodes = -1;
		    year = null;
		}
	    } catch (NumberFormatException e) {
		nbEpisodes = -1;
		year = null;
	    }

	}
	return new Info(role, year, nbEpisodes);
    }

    public LinkedHashMap<String, String> getSeasonEpisodes(String season)
	    throws IOException {
	if (seasonEpisodes != null)
	    return seasonEpisodes;
	String url = this.url + key + this.season + season;
	seasonEpisodes = new LinkedHashMap<String, String>();
	Document doc = Jsoup.connect(url).get();
	Elements elements = doc.select("div.list.detail.eplist").first()
		.select("div.list_item.odd, div.list_item.even");
	Iterator<Element> it = elements.iterator();
	for (; it.hasNext();) {
	    Element element = it.next().getElementsByTag("strong").first()
		    .getElementsByTag("a").first();
	    String title = element.text();

	    String id = getId(element.attr("href"));
	    seasonEpisodes.put(title, id);
	}
	return seasonEpisodes;
    }

    private String getId(String link) {
	int index = link.indexOf("tt");
	return link.substring(index, link.indexOf("/", index));
    }

    public LinkedHashMap<String, Info> getEpisodeFullCast(String season, String title)
	    throws IOException {
	HashMap<String, String> episodes = getSeasonEpisodes(season);
	return getFullCastFromLink(url + episodes.get(title));
    }

    private LinkedHashMap<String, Info> getFullCastFromLink(String link)
	    throws IOException {
	link += fullCast;
	Document doc = Jsoup.connect(link).get();
	Element castTable = doc.select("table.cast_list").first();
	return getFullCast(castTable);
    }

    public HashMap<String, Info> getGuestCast() throws IOException {
	if (guests != null)
	    return guests;
	guests = new LinkedHashMap<String, IMDB.Info>();
	if (seriesFullCast == null)
	    getFullCast();
	for (Entry<String, Info> entry : seriesFullCast.entrySet()) {
	    if (entry.getValue().getNbEpisodes() < 5)
		guests.put(entry.getKey(), entry.getValue());
	}
	return guests;
    }

    public LinkedHashMap<String, Info> getGuestForEpisode(String season, String title)
	    throws IOException {
	LinkedHashMap<String, Info> cast = getEpisodeFullCast(season, title);
	getGuestCast();
	Iterator<Map.Entry<String, Info>> iter = cast.entrySet().iterator();
	for (; iter.hasNext();)
	    if (!guests.containsKey(iter.next().getKey()))
		iter.remove();
	return cast;

    }

    public LinkedHashMap<String, Info> getGuestForEpisode(String season, int episode)
	    throws IOException {
	getSeasonEpisodes(season);
	for (Entry<String, String> entry : seasonEpisodes.entrySet()) {
	    episode--;
	    if (episode == 0)
		return getGuestForEpisode(season, entry.getKey());
	}
	return null;
    }
}
