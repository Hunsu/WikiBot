package org.wikipedia.botrequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.wikipedia.Wiki;
import org.wikipedia.login.Login;
import org.wikiutils.ParseUtils;

public class Request5 {
    public static void main(String[] args) throws FailedLoginException,
	    IOException {
	process();
    }

    private static void process() throws FailedLoginException, IOException {
	Wiki wiki = new Wiki("fr.wikipedia.org");
	Login login = new Login();
	wiki.login(login.getLogin() + "Bot", login.getPassword());
	wiki.setMarkBot(true);

	Set<String> titles = new HashSet<String>();

	String[] temp = wiki.getCategoryMembers("Catégorie:Portail:Sous-marins/Articles_liés");

	titles.addAll(Arrays.asList(temp));
	System.out.println(titles.size());

	for (String title : titles) {
	    String talkPage = "Discussion:" + title;
	    try {
		String evaluation = "?";
		String text = wiki.getPageText(title);
		if(text.indexOf("{{Ébauche") != -1 || text.indexOf("{{ébauche") != -1)
		    evaluation = "ébauche";
		if (!wiki.exists(talkPage)) {
		    wiki.edit(talkPage,
			    "{{Wikiprojet|Sous-marins|?|avancement=" + evaluation + "}}",
			    "Ajout évaluation");
		    continue;
		}
		text = wiki.getPageText(talkPage);
		ArrayList<String> al = ParseUtils.getTemplates("Wikiprojet", text);
		if(al.isEmpty()){
		    wiki.edit(talkPage,
			    "{{Wikiprojet|Sous-marins|?|avancement=" + evaluation + "}}\n" + text,
			    "Ajout évaluation");
		    continue;
		}
		String template = al.get(0);
		String oldTemplate = template;
		if(!template.contains("Sous-marins") && !template.contains("sous-marins")){
		    String oldText = text;
		    template = template.replace("{{Wikiprojet|", "{{Wikiprojet|Sous-marins|?|");
		    template = template.replace("{{Wikiprojet\n", "{{Wikiprojet\n|Sous-marins|?\n");
		    template = template.replace("{{wikiprojet|", "{{wikiprojet|Sous-marins|?|");
		    template = template.replace("{{wikiprojet\n", "{{wikiprojet\n|Sous-marins|?\n");
		    String avancement = ParseUtils.getTemplateParam(template, "avancement", true);
		    if(!evaluation.equals("?") && (avancement == null || avancement.equals("?")))
			template = ParseUtils.setTemplateParam(template, "avancement", evaluation, false);
		    text = text.replace(oldTemplate, template);
		    if(text.length() == oldText.length()){
			System.out.println(talkPage);
			continue;
		    }
		    wiki.edit(talkPage, text, "Ajout évaluation");
		} else {
		    template = template.replace("Sous-marins|ébauche", "Sous-marins|?");
		    String avancement = ParseUtils.getTemplateParam(template, "avancement", true);
		    if(!evaluation.equals("?") && (avancement == null || avancement.equals("?")))
			template = ParseUtils.setTemplateParam(template, "avancement", evaluation, false);
		    text = text.replace(oldTemplate, template);
		    if(!oldTemplate.equals(template))
			 wiki.edit(talkPage, text, "Ajout évaluation");
		}

	    } catch (LoginException e) {
		e.printStackTrace();
		continue;
	    }

	}

    }
}
