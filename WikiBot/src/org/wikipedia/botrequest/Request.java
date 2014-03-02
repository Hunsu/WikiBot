package org.wikipedia.botrequest;

import java.io.IOException;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.wikipedia.Wiki;
import org.wikipedia.login.Login;

public class Request {

	public static void main(String[] args) throws FailedLoginException, IOException{
		process();
	}

	private static void process() throws FailedLoginException, IOException{
		Login login = new Login();
		Wiki wiki = new Wiki("fr.wikipedia.org");
		wiki.login(login.getLogin(), login.getPassword());
		String[] articles = wiki.getPageText("Utilisateur:Hunsu/Brouillons").split("\\n");
		for(String title : articles){
			String text = wiki.getPageText(title);
			text = text.replace("http://www.sgg.gov.ma/Bo/", "http://www.sgg.gov.ma/Portals/0/Bo/");
			try {
				wiki.edit(title, text, "[[Wikipédia:Bot/Requêtes/2014/01#Près de 200 liens de références devenus invalides]]");
			} catch (LoginException e) {
				System.out.println("Erreur : " + title);
			}
		}
	}

}
