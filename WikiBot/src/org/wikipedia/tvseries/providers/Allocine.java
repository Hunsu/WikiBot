package org.wikipedia.tvseries.providers;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.wikipedia.search.GoogleResults;

import com.google.gson.Gson;

public class Allocine {

    public static ArrayList<String> getFrenchTitle(String tvSeries,
	    String season) {
	ArrayList<String> frenchTitle = new ArrayList<String>();
	try {
	    String link = getLink(tvSeries, season);
	    link += "ajax?page=";
	    boolean finished = false;
	    int i = 1;
	    while (!finished) {
		Document doc = Jsoup.connect(link+i).timeout(15000).get();
		Elements lines = doc.select("span.episode-title");
		Iterator<Element> it = lines.iterator();
		if(!it.hasNext())
		    break;
		for (; it.hasNext();) {
		    String title = it.next()
			    .text();
		    if (title == null) {
			finished = true;
			break;
		    }
		    if (!title.trim().equals(""))
			frenchTitle.add(title);
		}
		i++;
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
	Collections.reverse(frenchTitle);
	return frenchTitle;
    }

    private static String getLink(String tvSeries, String season)
	    throws UnsupportedEncodingException, IOException {
	String google = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=";
	String search = "Saison " + season + " de " + tvSeries
		+ " site:allocine.fr";
	String charset = "UTF-8";

	URL url = new URL(google + URLEncoder.encode(search, charset));
	Reader reader = new InputStreamReader(url.openStream(), charset);
	GoogleResults results = new Gson()
		.fromJson(reader, GoogleResults.class);

	return results.getResponseData().getResults().get(0).getUrl();
    }

}
