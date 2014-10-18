package org.wikipedia.botrequest;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.security.auth.login.LoginException;

import org.wikipedia.Wiki;
import org.wikipedia.login.Login;

public class Request9 {

    private static Wiki wiki;

    public static void main(String[] args) throws ClassNotFoundException,
	    FileNotFoundException, IOException, LoginException {
	wiki = new Wiki("fr.wikipedia.org");
	Login login = new Login();
	wiki.login(login.getBotLogin(), login.getPassword());
	wiki.setMarkBot(true);
	wiki.setMarkMinor(true);
	process("celle ci", "celle-ci");
	//process("Une groupe", "Un groupe");
    }

    private static void process(String search, String replace)
	    throws IOException, LoginException {
	String[][] articles = wiki.search("\"" + search + "\"", 0);
	for (int i = 0; i < articles.length; i++) {
	    System.out.println("Processing article " + articles[0][i] + " (" + i + "/"
		    + articles[0].length + ")");
	    String text = wiki.getPageText(articles[0][i]);
	    text = text.replaceAll(search, replace);
	    wiki.edit(articles[0][i], text, search + " -> " + replace );
	}

    }

}
