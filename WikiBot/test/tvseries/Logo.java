package tvseries;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.apache.commons.io.FileUtils;
import org.wikipedia.Wiki;
import org.wikiutils.ParseUtils;

import Tools.Login;

/**
 * The Class Logo.
 */
public class Logo {

	/** The wiki. */
	private static Wiki wiki = new Wiki("fr.wikipedia.org");

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args){
		try {
			Login login = new Login();
			wiki.login(login.getLogin(), login.getPassword());
		} catch (FailedLoginException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String title = "Frères Scott";
		if(title.endsWith(" (série télévisée)"))
			title = title.substring(0,title.length()-18);
		addLogo(title,"One Tree Hill logo.jpg");
	}

	/**
	 * Adds the logo.
	 *
	 * @param title the title
	 * @param imageTitle the image title
	 */
	private static void addLogo(String title, String imageTitle) {
		String[] titles = new String[30];
		for(int i=0;i<30;i++){
			titles[i ]= "Saison " + i +" des "+title;
		}
		try {
			boolean[] tests = wiki.exists(titles);
			for(int i=1;i<30;i++){
				if(tests[i]){
					String article = wiki.getPageText(titles[i]);
					ArrayList<String> al = ParseUtils.getTemplates("Infobox Saison de série télévisée", article);
					if(al.size() != 1)
						System.out.println("Problem : page " + titles[i]+" contains more than one season template");
					else{
						String template = al.get(0);
						String image = ParseUtils.getTemplateParam(template, "image",true);
						if(image != null && !image.equals(""))
							System.out.println("Warning : page "+titles[i]+" contains image");
						else{
							//FileUtils.writeStringToFile(new File(titles[i]+"o"), article);
							template = ParseUtils.setTemplateParam(template, "image", imageTitle+"\n", true);
							template = ParseUtils.setTemplateParam(template, "légende", "''Logo original de la série''\n", true);
							article = article.replace(al.get(0), template);
							FileUtils.writeStringToFile(new File(titles[i]), article);
							wiki.edit(titles[i], article, "bot: ajout logo");
						}
					}
				}
			}
		} catch (IOException | LoginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
