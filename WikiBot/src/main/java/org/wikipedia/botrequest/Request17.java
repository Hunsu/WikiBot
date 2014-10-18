package org.wikipedia.botrequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.security.auth.login.FailedLoginException;

public class Request17 extends Request {

    public static void main(String[] args) throws FailedLoginException,
	    IOException {
	login();
	proces();
    }

    private static void proces() throws IOException {
	List<String> titles = new ArrayList<String>(Arrays.asList(wiki
		.whatLinksHere("Dispositifs tactiques en football", 0)));
	titles.addAll(Arrays.asList(wiki.whatLinksHere("Attaquant (football)",
		0)));
	int i = 1;
	for (String title : titles) {
	    printProgress(title, i, titles.size());
	    i++;
	    String text = wiki.getPageText(title);
	    String oldText = text;
	    text = process(text);
	    if (!oldText.equals(text)) {
		text = beforeSave(text);
		try {
		    wiki.fastEdit(title, text, "[[Wikipédia:Bot/Requêtes/2014/09#lien_"
		    	+ "direct_Dispositifs_tactiques_en_football.23Attaquants_.E2."
		    	+ "86.92_Attaquant_.28football.29|lien direct Dispositifs "
		    	+ "tactiques en football#Attaquants → Attaquant (football)]]");
		} catch (InterruptedException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    } else {
		System.out.println("Error !");
	    }
	}

    }

    private static String process(String text) {
	text = text.replace("[[Dispositifs tactiques en football#Attaquants",
		"[[Attaquant (football)");
	text = text.replace(
		"[[Dispositifs tactiques en football#Attaquants|Buteur]]",
		"[[Attaquant (football)#Avant-centre|Avant-centre]]");
	text = text.replace("[[Attaquant (football)|Buteur]]",
		"[[Attaquant (football)#Avant-centre|Avant-centre]]");
	text = text.replace("[[Dispositifs tactiques en football|Attaquant]]",
		"[[Attaquant (football)|Attaquant]]");
	text = text.replace(
		"[[Dispositifs tactiques en football|Avant-centre]]",
		"[[Attaquant (football)#Avant-centre|Avant-centre]]");
	text = text.replace("[[Dispositifs tactiques en football|Buteur]]",
		"[[Attaquant (football)#Avant-centre|Avant-centre]]");
	text = text.replace("[[Dispositifs tactiques en football|Ailier",
		"[[Attaquant (football)#Ailier|Ailier");
	return text;
    }

}
