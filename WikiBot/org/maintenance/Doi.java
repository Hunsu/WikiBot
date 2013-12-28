package maintenance;

import java.io.*;
import java.net.URLEncoder;

import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.wikipedia.Wiki;
import org.wikiutils.ParseUtils;

public class Doi {

	private static String url = "http://www.ncbi.nlm.nih.gov/pubmed/";
	private static String arguments = "?report=xml&format=text";
	
	private static Wiki wiki = new Wiki("fr.wikipedia.org");
	private static Wiki enwiki = new Wiki();
	
	
	public static void main(String[] args) throws UnsupportedEncodingException {
		try {
			wiki.login("Hunsu", "MegamiMonster");
			ArrayList<String> pages = wiki.getPagesInCategory("Page à référence DOI incomplète", 500);
			int size = pages.size();
			for(int i=0;i<size;i++){
				String article = wiki.getPageText(pages.get(i),true);
				ArrayList<String> al = ParseUtils.getTemplates("cite doi", article);
				int alSize = al.size();
				for(int j=0;j<alSize;j++){
					try{
						String doi = ParseUtils.getTemplateParam(al.get(j), 1);
						doi = doi.replace("/", ".2F");
						if(wiki.exists("Modèle:Cite doi/"+doi))
							continue;
						//Jsoup.connect("https://toolserver.org/~verisimilus/Bot/DOI_bot/doibot.php?edit=doc&page=Template:Cite_doi/"+doi).get();
						if(enwiki.exists("Template:Cite doi/"+doi)){
							String text = enwiki.getPageText("Template:Cite doi/"+doi,true);
							text = text.replace("{{{cite|cite}}}", "cite");
							ArrayList<String> enal = ParseUtils.getTemplates("cite journal", text);
							if(enal.size() == 1){
								text = enal.get(0) + "<noinclude>\n"
						    			  + "{{Documentation}}\n\n"
						    			  + "[[Catégorie:Modèle de source‎]]\n\n"
						    			  + "[[en:Template:Cite doi/{{subst:#titleparts:{{subst:PAGENAME}}|0|2}}]]\n"
						    			  + "</noinclude>";
								wiki.edit("Modèle:Cite doi/"+doi,text,"bot: création page");
							}
						}
						else{
							/*String text = getArticle(pmid);
							text = parseXML(text);
							if(!wiki.exists("Modèle:Cite pmid/"+pmid))
								wiki.edit("Modèle:Cite pmid/"+pmid,text,"bot: création page");*/
						}
						
								
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
			
			//System.out.print(text);
		} catch (IOException | LoginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//text = URLDecoder.decode(text, "UTF-8");
		

	}

	private static String parseXML(String text) {
		SAXBuilder sxb = new SAXBuilder();
		org.jdom2.Document document;
		Element root;
	      try
	      {
	    	  document = sxb.build(new StringReader(text));

	    	  root = document.getRootElement();
	    	  root = root.getChild("MedlineCitation").getChild("Article");
	    	  String authors = getAuthors(root.getChild("AuthorList"));
	    	  String title   = getTitle(root.getChild("ArticleTitle"));
	    	  String journal = getJournal(root.getChild("Journal"));
	    	  String pages   = getPages(root.getChild("Pagination"));
	    	  String lang    = getLanguage(root.getChild("Language"));
	    	  //String lang    = getLanguages(root.getChild("language"));
	    	  text = "{{Article\n"+lang+authors+title+journal+pages;
	    	  text += "| doi = {{subst:#titleparts:{{subst:PAGENAME}}|0|2}}\n"
	    			  + "| url = \n"
	    			  + "| format = \n"
	    			  + "| date = \n"
	    			  + "}}<noinclude>\n"
	    			  + "{{Documentation}}\n\n"
	    			  + "[[Catégorie:Modèle de source‎]]\n\n"
	    			  + "[[en:Template:Cite pmid/{{subst:#titleparts:{{subst:PAGENAME}}|0|2}}]]\n"
	    			  + "</noinclude>";
	    	  return text;
	      }
	      catch(Exception e){
	    	  return null;
	      }
	}

	

	/*private static String getLanguages(Element element) {
		// TODO Auto-generated method stub
		return null;
	}*/

	private static String getLanguage(Element element) {
		String lang = "| langue = " +correctLang(element.getText())+"\n";
		return lang;
	}

	private static String correctLang(String lang) {
		if(lang.trim().equalsIgnoreCase("eng"))
			return "en";
		else
		return lang;
	}

	private static String getPages(Element element) {
		String pages = "| pages = " + element.getChildText("MedlinePgn")+"\n";
		return pages;
	}

	private static String getJournal(Element element) {
		String journal =  "| périodique = " + element.getChildText("Title")+"\n";
		journal += "| volume =" + element.getChild("JournalIssue").getChildText("Volume")+"\n";
		journal += "| numéro = " + element.getChild("JournalIssue").getChildText("Issue")+"\n";
		journal += "| année = "  + element.getChild("JournalIssue").getChild("PubDate").getChildText("Year")+"\n";
		return journal;
	}

	private static String getTitle(Element element) {
		String title = "| titre = " + element.getText() + "\n";
		return title;
	}


	private static String getAuthors(Element element) {
		List<Element> list = element.getChildren();
		Iterator<Element> it = list.iterator();
		String authors = "";
		int i=1;
		while(it.hasNext()){
		   Element e = it.next();
		   String firstName = e.getChildText("LastName") +" ";
		   String foreName   = e.getChildText("ForeName");
		   if(firstName != null && foreName != null){
			   authors += "| auteur"+i +"="+ firstName + foreName+"\n";
			   i++;
		   }
		}
		//authors = " authors + "\n";
		return authors;
	}

	private static String getArticle(int pmid) {
		try {
			Document doc = Jsoup.connect(url+pmid+arguments).get();
			return Jsoup.parse(doc.html()).text();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
