package org.wikipedia.botrequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.FailedLoginException;

public class Request15 extends Request {

    public static void main(String[] args) throws FailedLoginException,
	    IOException {
	login();

	process();

    }

    private static void process() throws IOException {
	String[] titles = wiki.whatLinksHere("voxographie", 0);
	int i = 1;
	for (String title : titles) {
	    printProgress(title, i, titles.length);
	    i++;
	    String text = wiki.getPageText(title);
	    List<String> links = getLinks(text);
	    System.out.println(links);
	    for (String link : links) {
		String rep = getRep(link);
		text = text.replace(link, rep);
	    }
	    text = beforeSave(text);
	    try {
		wiki.fastEdit(
		    title,
		    text,
		    "[[Wikipédia:Bot/Requêtes/2014/10#Liens_vers_Voxographie|"
		    + "Liens vers Voxographie]]");
	    } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}

    }

    private static String getRep(String link) {
	String rep;
	if (link.contains("voix françaises"))
	    rep = "voix françaises";
	else {
	    if (link.contains("voix française"))
		rep = "voix française";
	    else
		rep = "doublage";
	}
	link = link.replace("voxographie", rep).replace("Voxographie",
		capitalize(rep));
	if(link.contains("Voix française|voix française"))
	    link = "[[voix française]]";
	if(link.contains("voix française|voix française"))
	    link = "[[voix française]]";
	if(link.contains("Voix françaises|voix françaises"))
	    link = "[[voix françaises]]";
	if(link.contains("voix françaises|voix françaises"))
	    link = "[[voix françaises]]";
	return link;
    }

    public static String capitalize(String s) {
	return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static List<String> getLinks(String text) {
	List<String> list = new ArrayList<String>();
	Pattern p = Pattern.compile("\\[\\[[Vv]oxographie.*?\\]\\]");
	Matcher m = p.matcher(text);
	while (m.find())
	    list.add(m.group());
	return list;
    }

}
