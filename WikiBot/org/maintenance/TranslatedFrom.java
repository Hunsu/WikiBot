package maintenance;

import java.io.IOException;
import java.util.ArrayList;

import javax.security.auth.login.LoginException;

import org.wikipedia.Wiki;
import org.wikiutils.ParseUtils;

public class TranslatedFrom {

	public TranslatedFrom() {
		// TODO Auto-generated constructor stub
	}
	
	public static void main(String[] args){
		Wiki wiki = new Wiki("fr.wikipedia.org");
		try {
			wiki.login("Hunsu", "MegamiMonster");
			
			String[] titles = wiki.getCategoryMembers("Article utilisant le modèle Traduit de");
			for(int i=0;i<titles.length;i++){
				if(titles[i].toLowerCase().startsWith("catégorie:"))
					continue;
				else{
					String text = wiki.getPageText(titles[i]);
					String oldText = text;
					ArrayList<String> al = ParseUtils.getTemplates("Traduit de", text);
					//al.addAll(ParseUtils.getTemplates("multi bandeau", text));
					if(al.size() != 1)
						continue;
					text = text.replace(al.get(0)+"\n", "");
					text = text.replace(al.get(0), "");
					if(!text.equals(oldText))
						wiki.edit(titles[i], text, "bot: déplacement de modèle Traduit de vers la page de discussion");
					text = al.get(0);
					if(wiki.exists("Discussion:"+titles[i])){
						text += "\n" + wiki.getPageText("Discussion:"+titles[i]);
					}
					wiki.edit("Discussion:"+titles[i], text, "bot: déplacement du modèle Traduit de");
				}
			}
			
		} catch (IOException | LoginException e) {
			e.printStackTrace();
		}
	}
	

}
