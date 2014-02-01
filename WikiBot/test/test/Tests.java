package test;

import java.io.IOException;
import org.wikipedia.Wiki;
import org.wikiutils.ParseUtils;

/**
 * The Class Tests.
 */
public class Tests {

	/**
	 * Instantiates a new tests.
	 */
	public Tests() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws IOException {
		Wiki wiki = new Wiki("fr.wikipedia.org");

		String text = wiki.getPageText("Saison 3 des Frères Scott");
		String template = ParseUtils.getTemplates("Infobox Saison de série télévisée", text).get(0);
		template = ParseUtils.setTemplateParam(template, "nom", "value\n", true);
		System.out.println(template);




		//String template = "{{Date de naissance|25|octobre|1983 (29 ans)}}";
		//ArrayList al = ParseUtils.getTemplates("lien web", template);
		//HashMap<String, String> errors = LoggedInTests.getErrors("erreurs");
		//template = ParseUtils.setTemplateParam(template, "éditeur ", "éditeur",true);
		//template = ParseUtils.removeTemplateParam(template, "url");

		/*LinkedHashMap<String,String> map = ParseUtils.getTemplateParametersWithValue(template);

		Wiki wiki = new Wiki("fr.wikipedia.org");
		ArrayList<String> al = wiki.getPagesInCategory("Saison de série télévisée", 700);
		System.out.println(wiki.getArticleInSpecifLang("Desperate Housewives (season 6)", "fr"));*/
		//template = Date.correctDate(template);
		//ArrayList<String> al = ParseUtils.getTemplates("Date", template);


		/*Dictionary dic = new Dictionary();
		File file = new File("dic.txt");

		String lines = FileUtils.readFileToString(file);
		String[] s = lines.split("\n");
		for(int i=0;i<s.length;i++)
			dic.add(s[i]);
		dic.save("dic.ortho");
		dic.load("dic.ortho");

		List<Suggestion> list = dic.searchSuggestions("mar");
		for(int i=0;i<list.size();i++)
			System.out.println(list.get(i));*/

		//System.out.println(template);

	}

}
