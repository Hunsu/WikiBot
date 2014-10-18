package org.wikipedia.botrequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

import org.apache.commons.io.FileUtils;
import org.wikipedia.Wiki;
import org.wikipedia.login.Login;
import org.wikiutils.ParseUtils;

public class Request10 {

    private static Wiki wiki;
    private static List<String> pays;

    public static void main(String[] args) throws IOException, LoginException,
	    ClassNotFoundException, InterruptedException {
	wiki = new Wiki("fr.wikipedia.org");
	Login login = new Login();
	wiki.login(login.getBotLogin(), login.getPassword());
	wiki.setMarkBot(true);
	wiki.setMarkMinor(true);
	pays = FileUtils.readLines(new File("pays"));
	process();
    }

    private static void process() throws LoginException,
	    ClassNotFoundException, InterruptedException,
	    FileNotFoundException, IOException {
	System.out.println(System.currentTimeMillis());
	String[] titles = getTitles();
	System.out.println(System.currentTimeMillis());
	ObjectInputStream in = new ObjectInputStream(new FileInputStream(
		"ignore.obj"));
	@SuppressWarnings("unchecked")
	List<String> ignore = (List<String>) in.readObject();
	ignore.addAll(FileUtils.readLines(new File("ignore")));
	List<String> t = new ArrayList<String>(Arrays.asList(titles));
	t.removeAll(ignore);
	in.close();
	int i = 1;
	System.out.println(titles.length);
	System.out.println(ignore.size());
	System.out.println(t.size());
	int max = 962;
	/*for (String title : t) {
	    i++;
	    if(i <max)
		continue;
	    System.out.println("processing " + title + "(" + i + "/" + t.size()
		    + ")");
	    String text;
	    try {
		text = wiki.getPageText(title);
	    } catch (IOException e) {
		continue;
	    }
	    editPage(title, text);
	}
	i = 1;*/
	Pattern p = getPattern();
	Collections.sort(ignore);
	for (String title : ignore) {
	    System.out.println("processing " + title + "(" + i + "/"
		    + ignore.size() + ")");
	    i++;
	    if(i <max)
		continue;
	    try {
		String text = wiki.getPageText(title);
		Matcher matcher = p.matcher(text.toLowerCase());
		if (matcher.find()) {
		    System.out.println(title);
		    editPage(title, text);
		}
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}

	/*
	 * i = 0; String text = ""; int j=0; Random r = new Random(); for (i =
	 * 0; i < 1500; i++) { String title = t.get(r.nextInt(t.size())); i++;
	 * if (text.indexOf(title) == -1){ j++; text += "* [[" + title + "]]\n";
	 * } if (j == 500) break; }
	 *
	 * String text = ""; i = 0; p =
	 * Pattern.compile("\\[\\[catégorie:(.*?français.*?)\\]\\]");
	 * Set<String> categs = new HashSet<String>(); for (String title : t) {
	 * i++; System.out.println("processing " + title + "(" + i + "/" +
	 * t.size() + ")"); try { String article =
	 * wiki.getPageText(title).toLowerCase(); Matcher m =
	 * p.matcher(article); if (!m.find()) { System.out.println("Error!");
	 * text += "* [[" + title + "]]\n"; } } catch (Exception e) { continue;
	 * } } try { wiki.edit("Utilisateur:Hunsu/Brouillons", text, ""); }
	 * catch (IOException e1) { // TODO Auto-generated catch block
	 * e1.printStackTrace(); } text = ""; for (String title : ignore) { i++;
	 * System.out.println("processing " + title + "(" + i + "/" +
	 * ignore.size() + ")"); try { String article =
	 * wiki.getPageText(title).toLowerCase(); Matcher m =
	 * p.matcher(article); if (m.find()) { categs.add(m.group(1));
	 * System.out.println(m.group(1)); text += "* [[" + title + "]] (" +
	 * m.group(1) + ")\n"; } } catch (Exception e) { continue; } } text =
	 * "\n==Catégories ==" + categs.toString();
	 * wiki.edit("Utilisateur:Hunsu/Brouillons", text, "");
	 */
	ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
		"ignore.obj"));
	out.writeObject(ignore);
	out.close();

    }

    private static boolean editPage(String title, String text)
	    throws InterruptedException, LoginException, IOException {
	String oldText = text;
	List<String> templates = ParseUtils.getTemplates("Portail", text);
	if (templates.size() == 0) {
	    System.out.println("No portail found");
	    int i = text.toLowerCase().indexOf("{{portail ");
	    if (i != -1) {
		text = text.substring(0, i + "{{Portail".length()) + "|"
			+ text.substring(i + "{{Portail ".length());
		templates = ParseUtils.getTemplates("Portail", text);
	    } else
		return false;
	}
	if (templates.size() != 0) {
	    String template = templates.get(0);
	    if (template.toLowerCase().indexOf("france") == -1) {
		int year = getYear(text);
		if (year > 986 && year < 1791)
		    template = template.replace("}}", "|Royaume de France}}");
		else
		    template = template.replace("}}", "|France}}");
		text = text.replace(templates.get(0), template);
		text = beforeSave(text);
		wiki.edit(
			title,
			text,
			"[[Wikipédia:Bot/Requêtes/2014/08#D.C3.A9ploiement_de_.7B."
				+ "7BPortail_France.7D.7D_sur_les_biographies_de_Fran.C3.A7ais|Déploiement de "
				+ "{{Portail France}} sur les biographies de Français]]");
		return true;
	    } else if (!oldText.equals(text)) {
		wiki.edit(
			title,
			text,
			"[[Wikipédia:Bot/Requêtes/2014/08#D.C3.A9ploiement_de_.7B."
				+ "7BPortail_France.7D.7D_sur_les_biographies_de_Fran.C3.A7ais|Déploiement de "
				+ "{{Portail France}} sur les biographies de Français]]");
		return true;
	    }
	    System.out.println("Portail contains France");
	    return false;

	}
	return false;

    }

    private static int getYear(String text) {
	Pattern p = Pattern
		.compile("\\[\\[catégorie:\\s*naissance en.*?(\\d\\d\\d\\d?).*?\\]\\]");
	Matcher m = p.matcher(text.toLowerCase());
	if (m.find())
	    return Integer.valueOf(m.group(1));
	System.out.println("Error!");
	return -1;
    }

    private static Pattern getPattern() {
	try {
	    String[] categs = wiki.getPageText("Utilisateur:Hunsu/Brouillons")
		    .split("\\n");
	    String regex = "\\[\\[catégorie:\\s*(";
	    for (String categ : categs) {
		regex += categ + "|";
	    }
	    regex = regex.substring(0, regex.length() - 1);
	    regex += ").*?\\]\\]";
	    //System.out.println(regex);
	    return Pattern.compile(regex);
	} catch (IOException e) {
	}

	return null;
    }

    private static boolean isGood(String title) {
	String text;
	String intro;
	try {
	    text = wiki.getPageText(title);
	} catch (IOException e) {
	    return false;
	}
	intro = getIntroductionText(text).toLowerCase();
	Pattern p = Pattern
		.compile("((est|était|fut) un.*?français)|"
			+ "((nationalité|pays)\\s*=.*?(française|france|\\{\\{fra\\}\\}))");
	Matcher m = p.matcher(intro);
	if (m.find()) {
	    System.out.println(m.group());
	    if (m.group().indexOf("d'origine") != -1)
		return false;
	} else
	    return false;
	List<String> templates = ParseUtils.getTemplates("Portail", text);
	if (templates.size() == 0)
	    System.out.println("No portail found");
	else {
	    String template = templates.get(0);
	    LinkedHashMap<String, String> map = ParseUtils
		    .getTemplateParametersWithValue(template);
	    for (String set : map.keySet()) {
		String value = map.get(set);
		if (pays.contains(value)) {
		    System.out.println(value);
		    return false;
		}
	    }
	}
	return true;
    }

    private static String getIntroductionText(String text) {
	Pattern p = Pattern.compile("(?m)^==");
	Matcher m = p.matcher(text);
	if (m.find()) {
	    text = text.substring(0, m.start());
	}
	return text;
    }

    private static String[] getTitles() throws IOException,
	    ClassNotFoundException {
	// String[] ab = wiki.getCategoryMembers(
	// "Catégorie:Wikipédia:Article biographique", 0);
	// ObjectInputStream in = new ObjectInputStream(new FileInputStream(
	// "ab.obj"));
	// String[] pf = wiki.getCategoryMembers(
	// "Catégorie:Personnalité française", true, 0);
	/*
	 * @SuppressWarnings("unchecked") List<String> list1 = (List<String>)
	 * in.readObject(); in.close(); in = new ObjectInputStream(new
	 * FileInputStream("pf.obj"));
	 *
	 * @SuppressWarnings("unchecked") List<String> list2 = (List<String>)
	 * in.readObject(); in.close(); HashSet<String> set1 = new
	 * HashSet<String>(list1); HashSet<String> set2 = new
	 * HashSet<String>(list2); set1.retainAll(set2); String[] pal =
	 * wiki.getCategoryMembers( "Catégorie:Portail:France/Articles liés",
	 * 0); list2.clear(); list2.addAll(Arrays.asList(pal)); set2 = new
	 * HashSet<String>(list2); set1.removeAll(set2);
	 * System.out.println(list1.size()); System.out.println(set1.size());
	 */
	ObjectInputStream in = new ObjectInputStream(new FileInputStream(
		"set1.obj"));
	@SuppressWarnings("unchecked")
	HashSet<String> set1 = (HashSet<String>) in.readObject();
	in.close();
	return set1.toArray(new String[set1.size()]);
    }

    private static String beforeSave(String text) {
	text = text.replace("{{pdf}} {{lien web", "{{lien web|format=pdf");
	text = text.replace("{{Pdf}} {{lien web", "{{lien web|format=pdf");
	text = text.replace("{{Pdf}} {{Lien web", "{{Lien web|format=pdf");
	text = text.replace("{{pdf}} {{Lien web", "{{Lien web|format=pdf");
	text = text.replace("{{en}} {{lien web", "{{lien web|langue=en");
	text = text.replace("{{en}} {{Lien web", "{{Lien web|langue=en");
	return text;
    }
}
