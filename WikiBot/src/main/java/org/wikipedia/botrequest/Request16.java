package org.wikipedia.botrequest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.security.auth.login.FailedLoginException;

import org.apache.commons.io.FileUtils;

public class Request16 extends Request {

    public static List<String> articles = Collections.synchronizedList(new ArrayList<String>());
    public static Set<String> visitedCateg = new HashSet<String>();
    public static Pattern p = Pattern.compile("\\[\\[catégorie:"
	    + "(naissance en (.*?)?\\d\\d\\d\\d?|"
	    + "date de naissance inconnue).*?\\]\\]");

    public static void main(String[] args) throws FailedLoginException,
	    IOException {
	login();
	process();

    }

    private static void process() {
	List<String> categ = new ArrayList<String>();
	categ.add("Personnalité");
	process(categ);

    }

    private static void process(List<String> categories) {
	int j = 1;
	for (String categ : categories) {
	    printProgress(categ, j, categories.size());
	    j++;
	    if (visitedCateg.contains(categ))
		continue;
	    String[] titles;
	    try {
		titles = wiki.getCategoryMembers(categ, 0);
		visitedCateg.add(categ);
		int i = 1;
		for (final String title : titles) {
		    printProgress(title, i, titles.length);
		    i++;
		    new Thread(new Runnable() {

			@Override
			public void run() {
			    try {
				if (!hasCateg(title)) {
				    System.out.println("Found article: "
					    + title);
				    articles.add(title);
				    if (articles.size() > 20)
					try {
					    write();
					} catch (Exception e) {
					    e.printStackTrace();
					}
				}
			    } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			    }

			}
		    }).start();
		    while (numberOfThreadsRunning() > 20) {
			    try {
				Thread.sleep(1000);
			    } catch (InterruptedException e) {
			    }
			}
		}
		titles = wiki.getCategoryMembers(categ, 14);
		process(Arrays.asList(titles));
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
    }

    private static int numberOfThreadsRunning() {
	int nbRunning = 0;
	for (Thread t : Thread.getAllStackTraces().keySet()) {
	    if (t.getState() == Thread.State.RUNNABLE)
		nbRunning++;
	}
	return nbRunning;
    }

    private static void write() throws IOException {
	String titles = "";
	for (String title : articles)
	    titles += title + "\n";
	FileUtils.writeStringToFile(new File("articlesWithoutCateg"), titles,
		true);
	articles.clear();

    }

    private static boolean hasCateg(String title) throws IOException {
	String text = wiki.getPageText(title);
	if (p.matcher(text.toLowerCase()).find())
	    return true;
	return false;
    }

}
