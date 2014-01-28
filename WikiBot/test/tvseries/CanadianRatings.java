package tvseries;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.apache.commons.io.FileUtils;
import org.wikipedia.Wiki;
import org.wikiutils.ParseUtils;

import Tools.Login;

public class CanadianRatings {
	
	public static void main(String[] args) throws FailedLoginException, IOException{
		updateArticles();
	}
	
	private static void updateArticles() throws FailedLoginException, IOException{
		Login login = new Login();
		Wiki wiki = new Wiki("fr.wikipedia.org");
		wiki.login(login.getLogin(), login.getPassword());
		File dir = new File("shows");
		String[] files = dir.list();
		Arrays.sort(files);
		for(String filename : files){
			if(!filename.trim().toLowerCase().equals("castle"))
				continue;
			String str = "Saison %s de " + filename.trim();
			LinkedHashMap<String,String> ratings = getRatings("shows/"+filename);
			if(ratings == null)
				continue;
			for(int i=1;i<17;i++){
				String title = String.format(str, i);
				if(wiki.exists(title)){
					String text = wiki.getPageText(title);
					String newText = addCanadianRatings(text,ratings);
					if(!newText.equals(text))
						try {
							wiki.edit(title, newText, "Ajout de l'audience au Canada");
						} catch (LoginException e) {
							e.printStackTrace();
						}
				}
					
				}
				
			}
		}

private static String addCanadianRatings(String text,
			LinkedHashMap<String, String> ratings) {
		ArrayList<String> al = ParseUtils.getTemplates("Saison de série télévisée/Épisode", text) ;
		int size = al.size();
		for(int i=0;i<size;i++){
			String template = addCanadianRatingsToEpisode(al.get(i),ratings);
			text = text.replace(al.get(i), template);
			
		}
		
		
		return text;
	}

	private static String addCanadianRatingsToEpisode(String template,
		LinkedHashMap<String, String> ratings) {
	String date = ParseUtils.getTemplateParam(template, "première diffusion", false);
	if(conainsCanadianRatings(template))
		return template;
	String[] possibleDates = getCanadianOrAmericanDate(date);
	if(possibleDates == null)
		return template;
	for(Entry<String, String> entry : ratings.entrySet()){
		String key = entry.getKey();
		String value = entry.getValue().trim();
		if(key.equals(possibleDates[0]) || key.equals(possibleDates[0])){
			String oldValue = ParseUtils.getTemplateParam(template, "audience", false);
			if(oldValue == null || oldValue.trim().equals(""))
				oldValue = "\n";
			template = ParseUtils.setTemplateParam(template, "audience", oldValue + "\n" + value.trim() + "\n", true);
			return template;
		}
	}
	return template;
}

	private static boolean conainsCanadianRatings(String template) {
		ArrayList<String> al = ParseUtils.getTemplateParamerters(template);
		if(al.isEmpty())
			return true;
		else{
			String rating = ParseUtils.getTemplateParam(template, "audience", true);
			rating = ParseUtils.removeCommentsAndNoWikiText(template);
			if(rating.toLowerCase().indexOf("canada") != -1)
				return true;
		}
		return false;
	}

	private static String[] getCanadianOrAmericanDate(String date) {
		date = date.toLowerCase();
		boolean canadian = date.contains("canada");
		String[] str = date.split("\\n");
		for(int i=0;i<str.length;i++){
			if(canadian && str[i].indexOf("canada") != -1){
				date = getDate(str[i]);
				break;
			}else{
				if(!canadian && !str[i].trim().equals("")){
					date = getDate(str[i]);
					break;
				}
			}
		}
		int day = getDay(date);
		if(day == 0)
			return null;
		else{
			date = date.replace(day + " ", "");
			String[] possibleDates = new String[2];
			possibleDates[0] = String.valueOf(day) + " " +date;
			possibleDates[1] = String.valueOf(day -1) + " " +date;
			return possibleDates;
		}
	}

	private static int getDay(String date) {
		String[] str = date.split(" ");
		if(str.length == 3)
			try{
				return Integer.valueOf(str[0].trim());
			}catch(Exception e){
				return 0;
			}
		else
			return 0;
	}

	private static String getDate(String date) {
		ArrayList<String> al = ParseUtils.getTemplates("date", date);
		if(al.isEmpty()){
			Pattern p = Pattern.compile("\\d\\d? (janvier|février|mars|avril|mai|juin|juillet|août|septembre"
					+ "|octobre|novembre|décembre) \\d\\d\\d\\d");
			Matcher m = p.matcher(date);
			if(m.find())
				return m.group();
			else
				return null;
		}
		else{
			date = al.get(0);
			try{
			date = ParseUtils.getTemplateParam(date, 1).trim() + " " + ParseUtils.getTemplateParam(date, 2).trim() + " " +
					ParseUtils.getTemplateParam(date, 3).trim();
			return date;
			}catch(Exception e){
				return null;
			}
		}
	}

	private static LinkedHashMap<String,String> getRatings(String filename) {
		String str;
		try {
			str = FileUtils.readFileToString(new File(filename));
		
		if(str == null)
			return null;
		LinkedHashMap<String,String> map = new LinkedHashMap<String,String>();
		String[] s = str.split("\\n");
		for(int i=0;i<s.length;i=i+3){
			map.put(s[i], s[i+1]);
		}
		
		return map;
	} catch (IOException e) {
		e.printStackTrace();
		return null;
	}
	}
		
	
}
