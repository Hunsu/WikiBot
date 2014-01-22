package tvseries;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

import org.wikipedia.Wiki;
import org.wikiutils.ParseUtils;

import Tools.Login;
import test.TVSeries;

/**
 * The Class Wikification.
 */
public class Wikification {

	private static Wiki wiki = new Wiki("fr.wikipedia.org");

	/**
	 * Instantiates a new wikification.
	 */
	public Wikification() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		String category = "Saison de Friends";
		try {
			boolean dosave = true;
			boolean test = false;
			Login login = new Login();
			wiki.login(login.getLogin(), login.getPassword());
			if(dosave){
				saveToPages();
				return;
			}
			if(test){
				test();
				return;
			}

			String text = "";
			String [] titles = wiki.getCategoryMembers(category);
			for(int i=0;i<titles.length;i++){
				String txt = wiki.getPageText(titles[i]);
				if(txt.indexOf("{{Saison de série télévisée/Épisode") != -1)
					continue;
				txt = wikifiy(txt);
				txt = "<title>" + titles[i]+"</title>" + txt + "</endpage>";
				text = text + txt;
			}
			text = process(text);
			wiki.edit("Utilisateur:Hunsu/Brouillons", text, "bot: Wikification");

		} catch (LoginException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * public static String FormatInvites(String s) { String invites; int
	 * startIndex = 0; invites = GetInfo(s, "invités", ref startIndex); while
	 * (invites != null) { invites = invites.Trim(); if (invites.Length > 1) {
	 * /* if(invites.Contains("\n")) { invites = GetInfo(s, "invités", ref
	 * startIndex); continue; } invites = invites.Insert(0, "\n*"); s =
	 * s.Substring(0, startIndex) + s.Substring(startIndex + invites.Length);
	 * invites = RegExReplace(invites, @"\),", ")\n*"); invites =
	 * RegExReplace(invites, @",|\set\s|\s-\s|;", "\n*");
	 * //if(!invites.EndsWith("\n")) invites += "\n"; String[] lines =
	 * invites.Split('\n'); invites = ""; for (int i = 0; i < lines.Length; i++)
	 * { if (lines[i].StartsWith("**")) lines[i] = lines[i].Substring(1); if
	 * (lines[i].Trim().Equals(")")) continue; if (lines[i].EndsWith("."))
	 * lines[i] = lines[i].Substring(0, lines[i].Length - 1); if
	 * (lines[i].Split('(').Length != lines[i].Split(')').Length) { if (i + 1 <
	 * lines.Length) { if (!(lines[i + 1].Contains("("))) { invites += lines[i]
	 * + ", " + lines[i + 1].Substring(1) + "\n"; i++; } else { invites +=
	 * lines[i] + "\n"; Console.WriteLine("problem : " + lines[i]); } } else {
	 * Console.WriteLine("problem : " + lines[i]); invites += lines[i] + "\n"; }
	 * } else { invites += lines[i] + "\n"; } } s = s.Insert(startIndex,
	 * invites); } if (invites.EndsWith("\n\n")) invites = invites.Substring(0,
	 * invites.Length - 2); if (invites.EndsWith("\n")) invites =
	 * invites.Substring(0, invites.Length - 1); invites = GetInfo(s, "invités",
	 * ref startIndex); } return s; }
	 */

	private static void test() throws IOException, LoginException {
		String text = wiki.getPageText("Utilisateur:Hunsu/Brouillons");
		text = formatInvites(text);
		wiki.edit("Utilisateur:Hunsu/Brouillons", text, "");
	}

	private static String process(String text) {
		ArrayList<String> al = ParseUtils.getTemplates("Saison de série télévisée/Épisode", text);
		for(int i=0;i<al.size();i++){
			String resume = ParseUtils.getTemplateParam(al.get(i), "résumé", true);
			if(resume != null){
				if(!resume.startsWith("<ref"))
					continue;
				int end = resume.indexOf("</ref>");
				String ref = null;
				if(end != -1){
					ref = resume.substring(0, end+6);
					resume = resume.replace(ref, "").replaceFirst("^\\s*:\\s*", "");
					String title = ParseUtils.getTemplateParam(al.get(i), "titre original", true);
					String template = ParseUtils.setTemplateParam(al.get(i), "titre original", title+ref+"\n", true);
					template = ParseUtils.setTemplateParam(template, "résumé", resume+"\n", true);
					text = text.replace(al.get(i), template);
				}
			}
		}
		return text;
	}

	/**
	 * Reg ex replace.
	 *
	 * @param s the s
	 * @param pattern the pattern
	 * @param replacement the replacement
	 * @return the string
	 */
	public static String RegExReplace(String s, String pattern,
			String replacement) {
		return s.replaceAll(pattern, replacement);
	}

	/**
	 * Wikifiy.
	 *
	 * @param s the s
	 * @return the string
	 */
	public static String wikifiy(String s) {
		String titlePattern = "(?i)\\*\\s*'''Titre original\\s*:?\\s*'''\\s*:?\\s*''(.*)''|\\*\\s*Titre original\\s*:\\s*''(.*)''";
		String numbPattern = "(?i)\\*\\s*'''Numéros?\\(?s?\\)?'''\\s*:|\\*\\s*'''Episode N°'''\\s*:";
		String autPattern = "(?i)\\*\\s*'''Autres? titres?( francophone)?'''\\s*:\\s*''(.*)''";
		String scPattern = "(?i)\\*\\s*'''Scénariste\\(?s?\\)?\\s*:?\\s*'''\\s*:?";
		String dirPattern = "(?i)\\*\\s*'''Réalisat(eur|ion)\\(?s?\\)?'''\\s*:";
		String diffPattern = "(?i)\\*\\s*'''Diffusion\\(?s?\\)?'''\\s*:|\\*Diffusion\\s*:|\\*\\s*'''Diffusion\\(s\\)\\s*:|\\s*'''Première diffusion'''\\s*:";
		String audPattern = "(?i)\\*\\s*'''Audiences?\\(?s?\\)?'''\\s*:|\\*\\s*'''Audience\\(s\\)''' É.-U. :";
		String invPattern = "(?i)\\*\\s*'''(Acteurs secondaires)?(Invité\\(e?s\\))?'''.*:|\\*\\s*'''Invité'''.*:|\\*\\s*'''Invitée'''.*:|\\*\\s*Invité.*:|\\*\\s*'''Invité\\(e\\)s'''.*:|\\*'''Invités'''.*?:";
		String resPattern = "(?i)\\*\\s*'''Résumé'''\\s*:|\\*'''résumé''' :|\\*''' Résumé''' :|\\*'''Synopsis''' :|\\*\\s*Synopsis\\s*:|\\*'''Résumé''' :";
		String refPattern = "(?i)\\*\\s*'''Références'''\\s*:|\\*\\s*'''Clin d’œil'''\\s*:";
		String comPattern = "(?i)\\*\\s*'''(Commentaires?\\(?s?\\)?)?(Remarques?)?'''\\s*:|\\* '''Note''' :|\\*\\s*Note :|\\*\\s*'''Notes'''\\s*:|\\*\\s*'''commentaire'''\\s*:";
		String codeProductionPattern = "(?i)\\*'''Production'''\\s*:|\\*'''Code de production'''\\s*:";
		String codeProduction = "(?i)/ Prod°(.*)";
		String qctitle = "(?i)\\*\\s*'''Titre québécois'''\\s*:'?'?(.*)'?'?";

		// String spaces = @"\n\s*\n";
		s = RegExReplace(s, titlePattern,
				"{{Saison de série télévisée/Épisode\n| titre original     = $1");
		s = RegExReplace(s, qctitle, "| autre titre     = $1 (Québec)");
		s = RegExReplace(s, numbPattern, "| numéro             =");
		s = RegExReplace(s, autPattern, "| autre titre        = $2");
		s = RegExReplace(s, scPattern, "| scénariste         =");
		s = RegExReplace(s, dirPattern, "| réalisateur        =");
		s = RegExReplace(s, diffPattern, "| première diffusion =");
		s = RegExReplace(s, invPattern, "| invités            =");
		s = RegExReplace(s, audPattern, "| audience           =");
		s = RegExReplace(s, refPattern, "| commentaire        =");
		s = RegExReplace(s, resPattern, "| résumé             =");
		s = RegExReplace(s, comPattern, "| commentaire        =");
		s = RegExReplace(s, codeProduction, "\n| code de production = $1");
		s = RegExReplace(s, codeProductionPattern, "| code de production =");
		s = RegExReplace(s, "\\*'''Autre titre français\\s*:\\s* '?'?(.*?)'?'?","| autre titre     = $1");
		s = s.replace("*'''Résumé''' :", "| résumé   =");
		s = s.replace("* '''Remarque(s)''' :", "| commentaire =");
		s = s.replace("* '''Remarque(s)''':", "| commentaire =")
				.replace("'''Notes''' :", "| commentaire =")
				.replace("* '''Production''' :", "| code de production =")
				.replace("*'''Note''' :", "| commentaire =")
				.replace("*'''Titre original''' :",
						"{{Saison de série télévisée/Épisode\n| titre original     = ")
				.replace("* '''Invités''' :", "| invités            =")
				.replace("*\n", "")
				.replace("{{SUI}}", "{{Suisse}}")
				.replace("{{CH}}", "{{Suisse}}")
				.replace("{{FRA}}", "{{France}}")
				.replace("{{USA}}", "{{États-Unis}}")
				.replace("{{BEL}}", "{{Belgique}}")
				.replace("{{BE}}", "{{Belgique}}")
				.replace("{{QUE}}", "{{Québec}}")
				.replace("{{QC}}", "{{Québec}}")
				.replace("* '''première diffusion''' :",
						"| première diffusion = ")
				.replace("*'''Résumé ''' :", "| résumé             =")
				.replace("*'''Scénario''' :", "| scénariste         =")
				.replace("*'''Acteurs''':", "| invités            =")
				.replace("*'''Invités''' :", "| invités            =")
				.replace("*'''Acteurs''' :", "| invités            =")
				.replace("*'''Musique''':",
						"| nom du gimmick     = Musique\n| gimmick            =")
				.replace("\n*:", "\n*")
				.replace("'''Autre titre français''' :",
						"| autre titre        =")
				.replace("Episode", "Épisode")
				.replace("*'''Référence''' :", "| commentaire        =")
				.replace("* '''Note''':", "| commentaire        =")
				.replace("*'''Traduction du titre''' :",
						"| traduction titre     =")
				.replace("* '''Synopsis''' :", "| résumé             =")
				.replace("*'''Musique''' :",
						"| nom du gimmick     = Musique\n| gimmick            =");

		s = RegExReplace(s, invPattern, "| invités            =");

		invPattern = "(?i)\\*\\s*'''Distribution''' (\\(''Invités''\\))?\\s*:";
		resPattern = "(?s)(?i)\\*\\s*'''Résumé'''\\s*:?";
		s = RegExReplace(s,resPattern,"| résumé             =");
		s = RegExReplace(s, invPattern, "| invités            =");

		int i = 0;

		//StringBuffer sb = new StringBuffer(s);
		while (true) {
			i = s.indexOf("{{Saison de série télévisée/Épisode", i);
			if (i == -1)
				break;

			int j = s.indexOf("==", i);
			if (j == -1)
				j = s.indexOf("{{Palette");
			if (j == -1)
				j = s.indexOf("{{Portail");
			if (j == -1)
				j = s.indexOf("{{Catégorie:");
			if (j == -1)
				break;
			String s1 = s.substring(0,j);
			s1 += "}}\n";
			s1 += s.substring(j);
			s = s1;
			i = j;
		}
		//s = sb.toString();
		 //s = formatResume(s);
		 s = formatInvites(s);
		// s = RegExReplace(s, spaces, "\n");
		s = s.replace("==\n\n{{", "==\n{{");
		s = s.replace("**", "*");
		s = s.replace("\n\n|", "\n|");
		s = s.replace("\n \n|", "\n|");

		// s = RegExReplace(s, "([^=]*)==", "$1\n==");
		s = s.replace("}}==", "}}\n==");
		s = s.replace("\n}}==", "\n}}\n==");
		s = s.replace("\n\n}}", "\n}}");

		// s = FormatRefernces(s);

		return s;
	}

	/**
	 * Format invites.
	 *
	 * @param text the text
	 * @return the string
	 */
	public static String formatInvites(String text){
		ArrayList<String> al = ParseUtils.getTemplates("Saison de série télévisée/Épisode", text);
		int size = al.size();
		for(int i=0;i<size;i++){
			String template = al.get(i);
			String guest = ParseUtils.getTemplateParam(template, "invités", true);
			if(guest != null && !guest.equals("")){
				String inv = "\n* " + guest.replace(",", "\n* ").replace(" et ", "\n* ") + "\n";
				String[] invs = inv.trim().split("\\n");
				String temp = "";
				inv = "";
				for(int j=0;j<invs.length;j++){
					if(temp.equals("") && invs[j].length() > 1){
						temp = invs[j].substring(1);
					}else{
							if(temp.length() > 0 && invs[j].length() > 1)
								temp += "," + invs[j].substring(1);
					}

					if(ParseUtils.countOccurrences(temp, "(") == ParseUtils.countOccurrences(temp, ")" )){
						inv += "\n*" +temp;
						temp = "";
					}

				}
				inv =inv + "\n";
				if(inv.endsWith(".\n"))
					inv = inv.substring(0,inv.length()-2) + "\n";
				template = ParseUtils.setTemplateParam(template, "invités", inv, true);
				text = text.replace(al.get(i), template);
			}
			String resume = ParseUtils.getTemplateParam(template, "résumé",true);
			if(resume != null && !resume.equals("")){
				resume = resume.replaceAll("^:", "");
				template = ParseUtils.setTemplateParam(template, "résumé", resume, true);
				text.replace(al.get(i), template);
			}
		}
		if(text.endsWith("*\n"))
			text = text.substring(0, text.length()-2) + "\n";
		return text;
	}

	private static void saveToPages() throws IOException, LoginException{
		String text = wiki.getPageText("Utilisateur:Hunsu/Brouillons");
		String title = null;
		while((title = getPageTitle(text)) != null){
			int start = text.indexOf("</title>");
			int end = text.indexOf("</endpage>");
			if(start == -1 || end == -1)
				return;
			start += 8;
			String page = text.substring(start, end);
			page = beforeSave(page);
			text = text.substring(end+10);
			wiki.edit(title, page, "bot: wikification");
			TVSeries.UpdateFRArticle(title);
	}
}

	private static String getPageTitle(String text) {
		int start = text.indexOf("<title>");
		int end = text.indexOf("</title>");
		if(start ==-1 || end == -1)
		return null;
		return text.substring(start+7,end);
	}

	private static String beforeSave(String text){
		text = text.replace("{{pdf}} {{lien web", "{{lien web|format=pdf");
		text = text.replace("{{Pdf}} {{lien web", "{{lien web|format=pdf");
		text = text.replace("{{Pdf}} {{Lien web", "{{Lien web|format=pdf");
		text = text.replace("{{pdf}} {{Lien web", "{{Lien web|format=pdf");
		text = text.replace("{{en}} {{lien web", "{{lien web|langue=en");
		text = text.replace("{{en}} {{Lien web", "{{Lien web|langue=en");
		return text;
	}
}


