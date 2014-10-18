package org.wikipedia.tvseries;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class FranceAirDates {
    private static String url = "http://tvmag.lefigaro.fr/programme-tv/TNT/guide-tele.html";
    private static Connection c = null;
    private static List<String> series;

    public static void main(String[] args) throws ClassNotFoundException,
	    IOException, SQLException, ParseException {
	c = createDB();
	series = FileUtils.readLines(new File("series"));
	if (c != null) {
	    fetch();
	}
    }

    private static void fetch() throws IOException, SQLException,
	    ParseException {
	Scanner sc = new Scanner(System.in);
	String date = sc.nextLine();
	sc.close();
	for (int i = 18; i < 24; i++) {
	    fetch(date, i);
	}

    }

    private static void fetch(String date, int i) throws IOException,
	    SQLException, ParseException {
	String link = url + "?dateJour=" + date + "&heure=" + i;
	System.out.println("Getting page " + i + "/23");
	Document doc = Jsoup
		.connect(link)
		.timeout(30000)
		.userAgent(
			"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
		.get();
	List<List<Object>> infos = parseContent(doc);
	long start = System.currentTimeMillis();
	writeToDB(infos, date);
	long end = System.currentTimeMillis();
	System.out.println("Time taken : " + (start - end) / 1000);

    }

    private static void writeToDB(List<List<Object>> infos, String date)
	    throws SQLException, ParseException {
	date = formatDate(date);
	Date d = Date.valueOf(date);
	int s = 0;
	for (List<Object> info : infos) {
	    String broadcaster = (String) info.get(info.size() - 1);
	    for (int i = 0; i < info.size() - 1; i++) {
		s++;
		Info inf = (Info) info.get(i);
		String link = inf.getLink();
		String show = inf.getShow();
		System.out.println(show);
		if (!series.contains(show))
		    continue;
		PreparedStatement stmt = c
			.prepareStatement("INSERT OR IGNORE INTO Show (name) values (?)");
		stmt.setString(1, show);
		long start = System.currentTimeMillis();
		stmt.executeUpdate();
		long end = System.currentTimeMillis();
		System.out.println("Time by stmt : " + (end - start) / 1000.0);
		int showID = -1;
		stmt = c.prepareStatement("INSERT OR IGNORE INTO Broadcaster (name) values (?)");
		stmt.setString(1, broadcaster);
		stmt.executeUpdate();
		int broadcasterID = -1;
		stmt = c.prepareStatement("SELECT id FROM Show WHERE name = ?");
		stmt.setString(1, show);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
		    showID = rs.getInt("id");
		}
		stmt = c.prepareStatement("SELECT id FROM Broadcaster WHERE name = ?");
		stmt.setString(1, broadcaster);
		rs = stmt.executeQuery();
		while (rs.next()) {
		    broadcasterID = rs.getInt("id");
		}
		if (showID == -1 || broadcasterID == -1)
		    throw new SQLException(
			    "ShowID or broadcaster is equal to -1");
		stmt = c.prepareStatement("INSERT INTO AirDate (show, broadcaster, airDate, link) values (?, ?, ?, ?)");
		stmt.setInt(1, showID);
		stmt.setInt(2, broadcasterID);
		stmt.setDate(3, d);
		stmt.setString(4, link);
		stmt.executeUpdate();
	    }

	}
	System.out.println(s);
    }

    private static String formatDate(String date) throws ParseException {
	SimpleDateFormat sdf = new SimpleDateFormat("yyyymmdd");
	java.util.Date d = sdf.parse(date);
	Calendar cal = Calendar.getInstance();
	cal.setTime(d);
	int year = cal.get(Calendar.YEAR);
	int month = cal.get(Calendar.MONTH) + 1;
	String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	date = year + "-" + month + "-" + day;
	return date;
    }

    private static List<List<Object>> parseContent(Document doc) {
	Element element = doc.getElementById("table-grille");
	Elements elements = element.getElementsByTag("tr");
	List<List<Object>> infos = new ArrayList<List<Object>>();
	for (Element tr : elements) {
	    if (tr.hasAttr("id"))
		infos.add(parseLine(tr));
	}
	return infos;

    }

    private static List<Object> parseLine(Element tr) {
	Element td = tr.getElementsByTag("td").first();
	String broadcaster = parseBroadcaster(td);
	td = tr.getElementsByTag("td").get(1);
	List<Object> info = new ArrayList<>();
	for (Element el : td.getElementsByTag("td")) {
	    Element a = el.getElementsByTag("a").first();
	    if (a == null)
		continue;
	    Info inf = new Info(a.attr("title").split("-")[0],
		    a.attr("abs:href"));
	    info.add(inf);
	}
	info.add(broadcaster);
	return info;
    }

    private static String parseBroadcaster(Element td) {
	Element img = td.getElementsByTag("img").first();

	return img.attr("alt");
    }

    private static Connection createDB() throws ClassNotFoundException {
	// load the sqlite-JDBC driver using the current class loader
	Class.forName("org.sqlite.JDBC");

	Connection connection = null;
	try {
	    // create a database connection
	    connection = DriverManager
		    .getConnection("jdbc:sqlite:db/sample.db");
	    Statement statement = connection.createStatement();
	    statement.setQueryTimeout(30); // set timeout to 30 sec.
	    statement
		    .executeUpdate("create table if not exists Show (id integer PRIMARY KEY AUTOINCREMENT,"
			    + " name string unique, link string)");
	    statement
		    .executeUpdate("create table if not exists Broadcaster ("
			    + "id integer PRIMARY KEY AUTOINCREMENT, name string unique, link string)");
	    statement.executeUpdate("create table if not exists AirDate ("
		    + "id integer PRIMARY KEY AUTOINCREMENT, "
		    + "show integer, " + "broadcaster integer,"
		    + "airDate DATE," + "" + "link string,"
		    + "FOREIGN KEY(show) REFERENCES Show(id),"
		    + "FOREIGN KEY(broadcaster) REFERENCES Broadcaster(id))");
	    return connection;
	} catch (SQLException e) {
	    // if the error message is "out of memory",
	    // it probably means no database file is found
	    System.err.println(e.getMessage());
	    e.printStackTrace();
	    return null;
	}
    }

    private static class Info {
	private String show = null;
	private String link = null;

	public Info(String show, String link) {
	    this.show = show.trim();
	    this.link = link.trim();
	}

	public String getShow() {
	    return show;
	}

	public String getLink() {
	    return link;
	}
    }

}
