package org.wikipedia.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.security.auth.login.FailedLoginException;

import org.jsoup.Jsoup;
import org.wikipedia.tvseries.providers.Allocine;
import org.wikipedia.tvseries.providers.IMDB;
import org.wikipedia.tvseries.providers.IMDB.Info;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;

/**
 * The Class Tests.
 */
public class Tests {

    /**
     * Instantiates a new tests.
     */
    public Tests() {
    }

    /**
     * The main method.
     * 
     * @param args
     *            the arguments
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws FailedLoginException
     */
    public static void main(String[] args) throws IOException,
	    FailedLoginException {

	/*
	 * System.out .println(Jsoup .connect(
	 * "http://www.allocine.fr/series/ficheserie-10328/saison-21673/ajax?page=2"
	 * ) .get().html());
	 */
	for (String title : Allocine.getFrenchTitle("Anger Management", "1"))
	    System.out.println(title);

	// IMDB imdb = new IMDB("tt2171665");
	// System.out.println(imdb.getFullCast());
	// System.out.println(imdb.getSeasonEpisodes("1"));
	// System.out.println(imdb.getEpisodeFullCast("1",
	// "Wrath of Northmen"));
	// System.out.println(imdb.getGuestCast());
	// System.out.println(imdb.getGuestForEpisode("1",
	// "Wrath of Northmen"));
	/*
	 * HashMap<String, Info> guests = imdb.getGuestForEpisode("1", 54); for
	 * (Entry<String, Info> entry : guests.entrySet()) {
	 * System.out.println("* [[" + entry.getKey() + "]] (" +
	 * entry.getValue().getRole() + ")"); }
	 */
	/*
	 * TVRage tvRage = new TVRage(); Season season =
	 * tvRage.getSeason("Law_And_Order_SVU", "1");
	 * season.getEpisodesInfos(); System.out.println(season); String refs =
	 * ""; Wiki wiki = new Wiki("fr.wikipedia.org"); refs +=
	 * ParseUtils.getRefs(wiki.getPageText("Ursula Moore")).toString(); for
	 * (int i = 0; i < 10; i++) { String title = wiki.random();
	 * System.out.println(title); String text = wiki.getPageText(title);
	 * refs += "\n\n" + ParseUtils.getRefs(text).toString(); }
	 * 
	 * FileUtils.write(new File("refs"),refs);
	 * 
	 * //String text = wiki.getPageText("Saison 1 de K 2000"); //String
	 * template =
	 * ParseUtils.getTemplates("Saison de série télévisée/Épisode",
	 * text).get(0);
	 * 
	 * 
	 * //System.out.println(ParseUtils.getTemplateParam(template,
	 * "DirectedBy", true));
	 * 
	 * //LinkedHashMap<String, String> map =
	 * ParseUtils.getTemplateParametersWithValue(template);
	 * 
	 * //System.out.println(map);
	 * 
	 * //TVRage tvRage = new TVRage();
	 * 
	 * //Episode episode = tvRage.getEoisode("Law_And_Order_SVU", "15",
	 * "14"); // Series serie = tvRage.getSerie("Law_And_Order_SVU");
	 * //System.out.println(episode); // System.out.println(serie);
	 * 
	 * /* Wiki wiki = new Wiki("fr.wikipedia.org"); try{ wiki.readObject(new
	 * ObjectInputStream(new FileInputStream("wiki"))); }catch(IOException |
	 * ClassNotFoundException e){ Login login = new Login();
	 * wiki.login(login.getLogin(), login.getPassword());
	 * wiki.writeObject(new ObjectOutputStream(new
	 * FileOutputStream("wiki"))); }
	 * 
	 * String[] pages = wiki.whatTranscludesHere("Palette Akon");
	 * System.out.println(pages);
	 * 
	 * /*String text = wiki.getPageText("Saison 3 des Frères Scott"); String
	 * template =
	 * ParseUtils.getTemplates("Infobox Saison de série télévisée",
	 * text).get(0); template = ParseUtils.setTemplateParam(template, "nom",
	 * "value\n", true); System.out.println(template);
	 */

	// String template = "{{Date de naissance|25|octobre|1983 (29 ans)}}";
	// ArrayList al = ParseUtils.getTemplates("lien web", template);
	// HashMap<String, String> errors = LoggedInTests.getErrors("erreurs");
	// template = ParseUtils.setTemplateParam(template, "éditeur ",
	// "éditeur",true);
	// template = ParseUtils.removeTemplateParam(template, "url");

	/*
	 * LinkedHashMap<String,String> map =
	 * ParseUtils.getTemplateParametersWithValue(template);
	 * 
	 * Wiki wiki = new Wiki("fr.wikipedia.org"); ArrayList<String> al =
	 * wiki.getPagesInCategory("Saison de série télévisée", 700);
	 * System.out.
	 * println(wiki.getArticleInSpecifLang("Desperate Housewives (season 6)"
	 * , "fr"));
	 */
	// template = Date.correctDate(template);
	// ArrayList<String> al = ParseUtils.getTemplates("Date", template);

	/*
	 * Dictionary dic = new Dictionary(); File file = new File("dic.txt");
	 * 
	 * String lines = FileUtils.readFileToString(file); String[] s =
	 * lines.split("\n"); for(int i=0;i<s.length;i++) dic.add(s[i]);
	 * dic.save("dic.ortho"); dic.load("dic.ortho");
	 * 
	 * List<Suggestion> list = dic.searchSuggestions("mar"); for(int
	 * i=0;i<list.size();i++) System.out.println(list.get(i));
	 */

	// System.out.println(template);

    }
}
