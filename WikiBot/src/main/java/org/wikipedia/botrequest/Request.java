package org.wikipedia.botrequest;

import java.io.IOException;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.wikipedia.Wiki;
import org.wikipedia.login.Login;

public class Request {

    protected static Wiki wiki;

    protected static void login() throws FailedLoginException, IOException{
	wiki = new Wiki("fr.wikipedia.org");
	Login login = new Login();
	wiki.login(login.getBotLogin(), login.getPassword());
	wiki.setMarkBot(true);
	wiki.setMarkMinor(true);
    }

    protected static String beforeSave(String text) {
   	text = text.replace("{{pdf}} {{lien web", "{{lien web|format=pdf");
   	text = text.replace("{{Pdf}} {{lien web", "{{lien web|format=pdf");
   	text = text.replace("{{Pdf}} {{Lien web", "{{Lien web|format=pdf");
   	text = text.replace("{{pdf}} {{Lien web", "{{Lien web|format=pdf");
   	text = text.replace("{{en}} {{lien web", "{{lien web|langue=en");
   	text = text.replace("{{en}} {{Lien web", "{{Lien web|langue=en");
   	return text;
       }

    protected static void printProgress(String title, int i, int max){
	System.out.println("processing " + title + "(" + i + "/"
		    + max + ")");
    }
}
