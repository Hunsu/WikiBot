package test;

import java.io.IOException;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.wikipedia.Wiki;
import org.wikiutils.ParseUtils;

public class Tests {

	public Tests() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws IOException {
		String template = "{{lien web"
				+ "|       <!--------{{</nowiki>titre\n-->"
				+ "|         url\n"
				+ "|1        =site\n"
				+ "}}";
		//ArrayList al = ParseUtils.getTemplates("lien web", template);
		//HashMap<String, String> errors = LoggedInTests.getErrors("erreurs");
		//template = ParseUtils.setTemplateParam(template, "éditeur ", "éditeur",true);
		//template = ParseUtils.removeTemplateParam(template, "url");
		
		/*LinkedHashMap<String,String> map = ParseUtils.getTemplateParametersWithValue(template);
		
		Wiki wiki = new Wiki("fr.wikipedia.org");
		ArrayList<String> al = wiki.getPagesInCategory("Saison de série télévisée", 700);
		System.out.println(wiki.getArticleInSpecifLang("Desperate Housewives (season 6)", "fr"));*/
		
		System.out.print(ParseUtils.getTemplateParam(template, 2));

	}

}
