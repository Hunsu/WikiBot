package test;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import maintenance.Date;

import com.inet.jortho.Dictionary;
import com.inet.jortho.FileUserDictionary;
import com.inet.jortho.SpellChecker;

public class Tests {

	public Tests() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws IOException {
		String template = "{{date|23|frimai|an XII}}";
		//ArrayList al = ParseUtils.getTemplates("lien web", template);
		//HashMap<String, String> errors = LoggedInTests.getErrors("erreurs");
		//template = ParseUtils.setTemplateParam(template, "éditeur ", "éditeur",true);
		//template = ParseUtils.removeTemplateParam(template, "url");

		/*LinkedHashMap<String,String> map = ParseUtils.getTemplateParametersWithValue(template);

		Wiki wiki = new Wiki("fr.wikipedia.org");
		ArrayList<String> al = wiki.getPagesInCategory("Saison de série télévisée", 700);
		System.out.println(wiki.getArticleInSpecifLang("Desperate Housewives (season 6)", "fr"));*/
		template = Date.correctRepDate(template);
		//ArrayList<String> al = ParseUtils.getTemplates("Date", template);

/*
		Dictionary dic = new Dictionary();
		File file = new File("dic.txt");

		String lines = FileUtils.readFileToString(file);
		String[] s = lines.split("\n");
		for(int i=0;i<s.length;i++)
			dic.add(s[i]);
		dic.save("dic.ortho");

*/

		System.out.print(template+"\n");

	}

}
