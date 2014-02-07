package org.wikipedia.maintenance;

import java.io.*;

import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import javax.security.auth.login.LoginException;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.wikipedia.Wiki;
import org.wikiutils.ParseUtils;

import Tools.Login;

/**
 * The Class Pmid.
 */
public class Pmid {

	/** The url. */
	private static String url = "http://www.ncbi.nlm.nih.gov/pubmed/";
	
	/** The arguments. */
	private static String arguments = "?report=xml&format=text";
	
	/** The wiki. */
	private static Wiki wiki = new Wiki("fr.wikipedia.org");
	
	/** The enwiki. */
	private static Wiki enwiki = new Wiki();
	
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	public static void main(String[] args) throws UnsupportedEncodingException {
		try {
			Login login = new Login();
			wiki.login(login.getLogin(), login.getPassword());
			ArrayList<String> pages = wiki.getPagesInCategory("Page_avec_une_référence_PMID_incomplète", 25);
			int size = pages.size();
			for(int i=0;i<size;i++){
				String article = wiki.getPageText(pages.get(i),true);
				ArrayList<String> al = ParseUtils.getTemplates("cite pmid", article);
				int alSize = al.size();
				for(int j=0;j<alSize;j++){
					try{
						int pmid = Integer.parseInt(ParseUtils.getTemplateParam(al.get(j), 1).trim());
						if(wiki.exists("Modèle:Cite pmid/"+pmid))
							continue;
						//Jsoup.connect("http://tools.wikimedia.de/~verisimilus/Bot/DOI_bot/doibot.php?edit=doc&page=Template:Cite_pmid/"+pmid).get();
						if(enwiki.exists("Template:Cite pmid/"+pmid)){
							String text = enwiki.getPageText("Template:Cite pmid/"+pmid,true);
							ArrayList<String> enal = ParseUtils.getTemplates("cite journal", text);
							if(enal.size() == 1){
								text = enal.get(0) + "<noinclude>\n"
						    			  + "{{Documentation}}\n\n"
						    			  + "[[Catégorie:Modèle de source‎]]\n\n"
						    			  + "[[en:Template:Cite pmid/{{subst:#titleparts:{{subst:PAGENAME}}|0|2}}]]\n"
						    			  + "</noinclude>";
								wiki.edit("Modèle:Cite pmid/"+pmid,text,"bot: création page");
								wiki.purge(pages.get(i));
							}
						}
						else{
							if(!wiki.exists("Modèle:Cite pmid/"+pmid)){
								String text = getArticle(pmid);
								text = parseXML(text);
								wiki.edit("Modèle:Cite pmid/"+pmid,text,"bot: création page");
							}
							
						}
						
								
					}catch(Exception e){
						e.getCause();
						e.printStackTrace();
					}
				}
				wiki.edit(pages.get(i), article, "");
			}
			
			//System.out.print(text);
		} catch (IOException | LoginException e) {
			e.printStackTrace();
		}
		
		//text = URLDecoder.decode(text, "UTF-8");
		

	}

	/**
	 * Parses the xml.
	 *
	 * @param text the text
	 * @return the string
	 */
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
	    	  text += "| pmid = {{subst:#titleparts:{{subst:PAGENAME}}|0|2}}\n"
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

	/**
	 * Gets the language.
	 *
	 * @param element the element
	 * @return the language
	 */
	private static String getLanguage(Element element) {
		String lang = "| langue = " +correctLang(element.getText())+"\n";
		return lang;
	}

	/**
	 * Correct lang.
	 *
	 * @param lang the lang
	 * @return the string
	 */
	private static String correctLang(String lang) {
		if(lang.trim().equalsIgnoreCase("eng"))
			return "en";
		else
		return lang;
	}

	/**
	 * Gets the pages.
	 *
	 * @param element the element
	 * @return the pages
	 */
	private static String getPages(Element element) {
		String pages = "| pages = " + element.getChildText("MedlinePgn")+"\n";
		return pages;
	}

	/**
	 * Gets the journal.
	 *
	 * @param element the element
	 * @return the journal
	 */
	private static String getJournal(Element element) {
		String journal =  "| périodique = " + element.getChildText("Title")+"\n";
		journal += "| volume =" + element.getChild("JournalIssue").getChildText("Volume")+"\n";
		journal += "| numéro = " + element.getChild("JournalIssue").getChildText("Issue")+"\n";
		journal += "| année = "  + element.getChild("JournalIssue").getChild("PubDate").getChildText("Year")+"\n";
		return journal;
	}

	/**
	 * Gets the title.
	 *
	 * @param element the element
	 * @return the title
	 */
	private static String getTitle(Element element) {
		String title = "| titre = " + element.getText() + "\n";
		return title;
	}


	/**
	 * Gets the authors.
	 *
	 * @param element the element
	 * @return the authors
	 */
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

	/**
	 * Gets the article.
	 *
	 * @param pmid the pmid
	 * @return the article
	 */
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
