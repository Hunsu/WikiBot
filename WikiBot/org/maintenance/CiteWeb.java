package maintenance;

import java.io.IOException;
import java.util.ArrayList;

import javax.security.auth.login.LoginException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.wikipedia.Wiki;
import org.wikiutils.ParseUtils;

public class CiteWeb {

	public CiteWeb() {
		// TODO Auto-generated constructor stub
	}

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
					// al.addAll(ParseUtils.getTemplates("multi bandeau",
					// text));
					for (int j = 0; j < al.size(); j++) {
						String template = al.get(j);
						String param = ParseUtils.getTemplateParam(template,
								"titre", true);
						if (param == null || param.equals("")) {
							param = ParseUtils.getTemplateParam(template,
									"title", true);
							if (param == null || param.equals("")) {
								String url = ParseUtils.getTemplateParam(
										template, "url", true);
								if (url == null || url.equals(""))
									continue;
								try {
									Document doc = Jsoup.connect(url).get();
									String titre = doc.title();
									template = ParseUtils.removeTemplateParam(
											template, "title");
									template = ParseUtils.setTemplateParam(
											template, "titre", titre, false);
									text = text.replace(al.get(j), template);
								} catch (Exception e) {
									continue;
								}
							}
						}
					}
					if (!text.equals(oldText))
						wiki.edit(titles[i], text,
								"bot: ajout de paramètre titre pour le modèle lien web");
				}
			}

		} catch (IOException | LoginException e) {
			e.printStackTrace();
		}
	}

}
