package org.wikipedia.botrequest;

import java.io.File;
import java.io.IOException;

import javax.security.auth.login.LoginException;

import org.apache.commons.io.FileUtils;

public class Request12 extends Request {

    public static void main(String[] args) throws IOException, LoginException {
	login();
	wiki.setThrottle(0);
	process();
    }

    private static void process() throws IOException, LoginException {
	String[] titles = FileUtils.readFileToString(new File("listes")).split(
		"\\n");
	int i = 1;
	int max = 132;
	for (String title : titles) {
	    title = title.split("----")[0].trim();
	    printProgress(title, i, titles.length);
	    i++;
	    if(i < max)
		continue;
	    String text = wiki.getPageText(title);
	    String oldText = text;
	    text = text.replace("celle ci ", "celle-ci ");
	    text = text.replace("Celle ci ", "Celle-ci ");
	    text = text.replace("celles ci ", "celles-ci ");
	    text = text.replace("Celles ci ", "Celles-ci ");
	    text = text.replace("celui ci ", "celui-ci ");
	    text = text.replace("Celui ci ", "Celui-ci ");
	    text = text.replace("ceux ci ", "ceux-ci ");
	    text = text.replace("Ceux ci ", "Ceux-ci ");
	    text = text.replace("cessez le feu", "cessez-le-feu");
	    text = text.replace("Cessez le feu", "Cessez le feu");
	    text = text.replace(" ci dessus ", " ci-dessus ");
	    text = text.replace(" Ci dessus ", " Ci-dessus ");
	    text = text.replace("quelques uns ", "quelques-uns ");
	    text = text.replace("Quelques uns ", "Quelques-uns ");
	    text = text.replace("rez de chaussée", "rez-de-chaussée");
	    text = text.replace("Rez de chaussée", "Rez-de-chaussée");
	    text = text.replace("sacré cœur", "sacré-cœur");
	    text = text.replace("Sacré cœur", "Sacré-cœur");
	    text = text.replace("Royaume Uni", "Royaume-Uni");
	    if (!oldText.equals(text)) {
		text = beforeSave(text);
		try{
		wiki.fastEdit(title, text, "[[Wikipédia:Bot/Requêtes/2014/08#"
			+ "correction_typo|correction_typo]]");
		}catch(Exception e){
		    System.out.println(e);
		}
	    }
	}

    }

}
