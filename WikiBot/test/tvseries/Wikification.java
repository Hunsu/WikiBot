package tvseries;

import java.io.IOException;
import java.util.ArrayList;

import javax.security.auth.login.LoginException;

import org.wikipedia.Wiki;
import org.wikiutils.ParseUtils;

import test.TVSeries;

public class Wikification {

	public Wikification() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		String title = "Saison 4 des Experts : Manhattan";
		Wiki wiki = new Wiki("fr.wikipedia.org");
		try {
			wiki.login("Hunsu", "MegamiMonster");
			String text = wiki.getPageText(title);
			text = wikifiy(text);
			wiki.edit("Utilisateur:Hunsu/Brouillons", text, "bot: Wikification");
			//wiki.edit(title, text, "bot: Wikification");
			//TVSeries.UpdateFRArticle(title);

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

	public static String RegExReplace(String s, String pattern,
			String replacement) {
		return s.replaceAll(pattern, replacement);
	}

	public static String wikifiy(String s) {
		String titlePattern = "(?i)\\*\\s*'''Titre original\\s*:?\\s*'''\\s*:?\\s*''(.*)''|\\*\\s*Titre original\\s*:\\s*''(.*)''";
		String numbPattern = "(?i)\\*\\s*'''Numéros?\\(?s?\\)?'''\\s*:|\\*\\s*'''Episode N°'''\\s*:";
		String autPattern = "(?i)\\*\\s*'''Autres? titres?( francophone)?'''\\s*:\\s*''(.*)''";
		String scPattern = "(?i)\\*\\s*'''Scénariste\\(?s?\\)?\\s*:?\\s*'''\\s*:?";
		String dirPattern = "(?i)\\*\\s*'''Réalisat(eur|ion)\\(?s?\\)?'''\\s*:";
		String diffPattern = "(?i)\\*\\s*'''Diffusion\\(?s?\\)?'''\\s*:|\\*Diffusion\\s*:|\\*\\s*'''Diffusion\\(s\\)\\s*:";
		String audPattern = "(?i)\\*\\s*'''Audiences?\\(?s?\\)?'''\\s*:|\\*\\s*'''Audience\\(s\\)''' É.-U. :";
		String invPattern = "(?i)\\*\\s*'''(Acteurs secondaires)?(Invité\\(e?s\\))?'''.*:|\\*\\s*'''Invité'''.*:|\\*\\s*'''Invitée'''.*:|\\*\\s*Invité.*:|\\*\\s*'''Invité\\(e\\)s'''.*:|\\*'''Invités'''.*?:";
		String resPattern = "(?i)\\*\\s*'''Résumé'''\\s*:|\\*'''résumé''' :|\\*''' Résumé''' :|\\*'''Synopsis''' :|\\*\\s*Synopsis\\s*:|\\*'''Résumé''' :";
		String refPattern = "(?i)\\*\\s*'''Références'''\\s*:|\\*\\s*'''Clin d’œil'''\\s*:";
		String comPattern = "(?i)\\*\\s*'''(Commentaires?\\(?s?\\)?)?(Remarques?)?'''\\s*:|\\* '''Note''' :|\\*\\s*Note :|\\*\\s*'''Notes'''\\s*:|\\*\\s*'''commentaire'''\\s*:";
		String codeProductionPattern = "(?i)\\*'''Production'''\\s*:";
		String codeProduction = "(?i)/ Prod°(.*)";
		String qctitle = "(?i)\\*\\s*'''Titre québécois'''\\s*:'?'?(.*)'?'?";

		String date = "(?i)\\[?\\[?(\\d\\d?)\\s*(janvier|février|mars|avril|mai|juin|juillet|août|septembre|octobre|novembre|décembre)\\]?\\]?\\s*\\[?\\[?(\\d\\d\\d\\d)\\]?\\]?";

		String date2 = "(?i)\\[?\\[?1er\\s*(janvier|février|mars|avril|mai|juin|juillet|août|septembre|octobre|novembre|décembre)\\]\\]\\s*\\[\\[(\\d\\d\\d\\d)\\]\\]";

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
		s = RegExReplace(s, date, "{{date rapide|$1|$2|$3}}");
		s = RegExReplace(s, date2, "{{date rapide|1|$1|$2}}");
		s = RegExReplace(s, codeProductionPattern, "| code de production =");
		s = s.replace("*'''Résumé''' :", "| résumé   =");
		s = s.replace("* '''Remarque(s)''' :", "| commentaire =");
		s = s.replace("* '''Remarque(s)''':", "| commentaire =")
				.replace("'''Notes''' :", "| commentaire =")
				.replace("{{Date", "{{Date rapide")
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
				.replace("{{date|", "{{date rapide|")
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
		resPattern = "(?s)(?i)\\*\\s*'''Résumé'''.*? : ";
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
	
	public static String formatInvites(String text){
		ArrayList<String> al = ParseUtils.getTemplates("Saison de série télévisée/Épisode", text);
		int size = al.size();
		for(int i=0;i<size;i++){
			String template = al.get(i);
			String guest = ParseUtils.getTemplateParam(template, "invités", true);
			if(guest != null && !guest.equals("")){
				String inv = "\n* " + guest.replace(",", "\n* ").replace(" et ", "\n* ") + "\n";
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
		return text;
	}
	

}
