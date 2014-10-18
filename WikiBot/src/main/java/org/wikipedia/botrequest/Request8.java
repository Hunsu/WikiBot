package org.wikipedia.botrequest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.security.auth.login.LoginException;

import org.apache.commons.io.FileUtils;
import org.wikipedia.Wiki;
import org.wikipedia.login.Login;
import org.wikiutils.ParseUtils;

public class Request8 {
    public static Wiki wiki = new Wiki("fr.wikipedia.org");
    public static String comment = "[[Wikipédia:Rbot#mod.C3.A8le_.7B.7BGares_TER_SNCF"
	    + ".7D.7D|mise à jour du modèle {{Gares TER SNCF}}]]";
    public static HashMap<String, String> uic = new HashMap<>();

    public static void main(String[] args) throws IOException, LoginException {
	Login login = new Login();
	wiki.login(login.getBotLogin(), login.getPassword());
	wiki.setMarkBot(true);
	wiki.setMarkMinor(true);
	loadUic();
	process();
    }

    private static void loadUic() throws IOException {
	String[] lines = FileUtils.readFileToString(new File("/home/meradi/l"))
		.split("\\n");
	for (String line : lines) {
	    String[] t = line.split("=");
	    if (t.length == 2)
		uic.put(t[1].trim().toLowerCase(), t[0].trim());
	    else
		System.out.println(t[0]);
	}
	FileUtils.writeStringToFile(new File("l2"), uic.toString());
    }

    private static void process() throws IOException, LoginException {
	String[] titles = wiki.getCategoryMembers(
		"Article gare TER à mettre à jour", 0);
	System.out.println(uic.get("vauriat"));
	for (String title : titles) {
	    System.out.println(title);
	    String temp = "";
	    String gare = title.replaceAll("Gare des? ", "")
		    .replace("(Nord)", "Nord").replace("Gare d'", "")
		    .replace("Gare du", "").replaceAll("Halte des? ", "")
		    .replace("Halte d'", "").replaceAll(" \\(.*?\\)", "")
		    .replace("Toulouse-", "").trim().toLowerCase();
	    try {
		gare = gare.replace("la ", "").replace("le ", "")
			.replace("les ", "");
		System.out.println("trying : " + gare);
		String uic = Request8.uic.get(gare);
		if (uic == null) {
		    temp = gare.replace("-", " - ").replaceAll("saint -\\s*",
			    "saint-");
		    System.out.println("trying : " + temp);
		    uic = Request8.uic.get(temp);
		}
		if (uic == null) {
		    temp = gare.replaceAll("\\s*-\\s*", " ").replaceAll(
			    "saint ", "saint-");
		    System.out.println("trying : " + temp);
		    uic = Request8.uic.get(temp);
		}
		if (uic == null) {
		    temp = gare.replace("-ville", "");
		    System.out.println("trying : " + temp);
		    uic = Request8.uic.get(temp);
		}
		if (uic == null) {
		    temp = gare.replace("la ", "").replace("le ", "")
			    .replace("les ", "");
		    System.out.println("trying : " + temp);
		    uic = Request8.uic.get(temp);
		}
		if (uic == null) {
		    temp = gare.replaceAll("\\s*-\\s*", " - ").replaceAll(
			    "saint - ", "saint-");
		    String[] t = gare.split(" - ");
		    if (t.length == 2)
			temp = t[1].trim() + " - " + t[0].trim();
		    System.out.println("trying : " + temp);
		    uic = Request8.uic.get(temp);
		}
		if (uic != null) {
		    // System.out.println(uic);
		    String article = wiki.getPageText(title);
		    ArrayList<String> templates = ParseUtils.getTemplates(
			    "Gares TER SNCF", article);
		    for (String template : templates) {
			String newTemplate = ParseUtils.setTemplateParam(
				template, "code_uic", uic, false);
			System.out.println(newTemplate);
			article = article.replace(template, newTemplate);
		    }
		    System.out.println("Saving article!");
		    wiki.edit(title, article, comment);
		} else
		    System.out.println("Error! " + title);
	    } catch (Exception e) {
		System.out.println(e);
		System.out.println("Error! " + title);
	    }
	}

    }
}
