package maintenance;

import java.io.IOException;
import java.util.ArrayList;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.wikipedia.Wiki;
import org.wikiutils.ParseUtils;

public class MultiBandeaux {

	public MultiBandeaux() {
	}
	
	
	public static void main(String[] args){
		Wiki wiki = new Wiki("fr.wikipedia.org");
		try {
			wiki.login("Hunsu", "MegamiMonster");
			
			String[] titles = wiki.getCategoryMembers("Page utilisant un modèle avec une syntaxe erronée");
			for(int i=0;i<titles.length;i++){
				if(titles[i].toLowerCase().startsWith("catégorie:")){
					String text = wiki.getPageText(titles[i]);
					String oldText = text;
					ArrayList<String> al = ParseUtils.getTemplates("Multi bandeau", text);
					//al.addAll(ParseUtils.getTemplates("multi bandeau", text));
					for(int j=0;j<al.size();j++){
						String param = ParseUtils.getTemplateParam(al.get(j), 2);
						if(param == null){
							String template = al.get(j).replace("Multi bandeau|", "").replace("multi bandeau|", "");
							text = text.replace(al.get(j), template);
						}
					}
					if(!text.equals(oldText))
						wiki.edit(titles[i], text, "bot: Le modèle Multi bandeau doit être utilisé avec au moins deux paramètres");
				}
			}
			
		} catch (IOException | LoginException e) {
			e.printStackTrace();
		}
	}

}
