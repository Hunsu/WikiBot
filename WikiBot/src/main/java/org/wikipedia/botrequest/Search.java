package org.wikipedia.botrequest;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.io.compress.BZip2Codec;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.mahout.common.iterator.FileLineIterator;
import org.itadaki.bzip2.BZip2OutputStream;

import edu.jhu.nlp.wikipedia.PageCallbackHandler;
import edu.jhu.nlp.wikipedia.WikiPage;
import edu.jhu.nlp.wikipedia.WikiXMLParser;
import edu.jhu.nlp.wikipedia.WikiXMLParserFactory;

public class Search extends Request {

    private static Pattern p;
    private static ArrayList<String> titles;
    private static float i;

    public static void main(String[] args) throws IOException {
	splitXMl("frwiki.xml.bz2", 700);
	WikiXMLParser wxsp = WikiXMLParserFactory
		.getSAXParser("frwiki.xml.bz2");
	i = 1;
	p = getPattern();
	titles = new ArrayList<String>();
	try {

	    wxsp.setPageCallback(new PageCallbackHandler() {

		public void process(WikiPage page) {
		    System.out.println("processing " + page.getTitle().trim()
			    + " " + "(" + (float) (i * 100.0 / 2000000.0)
			    + "%)");
		    i++;
		    String text = page.getWikiText().toLowerCase();
		    Matcher m = p.matcher(text);
		    if (m.find()) {
			System.out.println(m.group());
			titles.add(page.getTitle().trim() + " ---- "
				+ m.group());
		    }
		    if (i % 100 == 0 && titles.size() != 0) {
			try {
			    String lines = getLines(titles);
			    FileUtils.writeStringToFile(new File("listes"),
				    lines, true);
			    titles.clear();
			} catch (IOException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			}
		    }
		}

		private String getLines(ArrayList<String> titles) {
		    String lines = "";
		    for (String title : titles) {
			lines += title + "\n";
		    }
		    return lines;
		}
	    });

	    wxsp.parse();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private static void splitXMl(String filename, int nb) throws IOException {
	int chunkSize = 1024 * 1024 * nb;
	int numChunks = Integer.MAX_VALUE;

	String header = "<mediawiki xmlns=\"http://www.mediawiki.org/xml/export-0.3/\" "
		+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
		+ "xsi:schemaLocation=\"http://www.mediawiki.org/xml/export-0.3/ "
		+ "http://www.mediawiki.org/xml/export-0.3.xsd\" "
		+ "version=\"0.3\" "
		+ "xml:lang=\"en\">\n"
		+ "  <siteinfo>\n"
		+ "<sitename>Wikipedia</sitename>\n"
		+ "    <base>http://en.wikipedia.org/wiki/Main_Page</base>\n"
		+ "    <generator>MediaWiki 1.13alpha</generator>\n"
		+ "    <case>first-letter</case>\n"
		+ "    <namespaces>\n"
		+ "      <namespace key=\"-2\">Media</namespace>\n"
		+ "      <namespace key=\"-1\">Special</namespace>\n"
		+ "      <namespace key=\"0\" />\n"
		+ "      <namespace key=\"1\">Talk</namespace>\n"
		+ "      <namespace key=\"2\">User</namespace>\n"
		+ "      <namespace key=\"3\">User talk</namespace>\n"
		+ "      <namespace key=\"4\">Wikipedia</namespace>\n"
		+ "      <namespace key=\"5\">Wikipedia talk</namespace>\n"
		+ "      <namespace key=\"6\">Image</namespace>\n"
		+ "      <namespace key=\"7\">Image talk</namespace>\n"
		+ "      <namespace key=\"8\">MediaWiki</namespace>\n"
		+ "      <namespace key=\"9\">MediaWiki talk</namespace>\n"
		+ "      <namespace key=\"10\">Template</namespace>\n"
		+ "      <namespace key=\"11\">Template talk</namespace>\n"
		+ "      <namespace key=\"12\">Help</namespace>\n"
		+ "      <namespace key=\"13\">Help talk</namespace>\n"
		+ "      <namespace key=\"14\">Category</namespace>\n"
		+ "      <namespace key=\"15\">Category talk</namespace>\n"
		+ "      <namespace key=\"100\">Portal</namespace>\n"
		+ "      <namespace key=\"101\">Portal talk</namespace>\n"
		+ "    </namespaces>\n" + "  </siteinfo>\n";
	StringBuilder content = new StringBuilder();
	content.append(header);
	NumberFormat decimalFormatter = new DecimalFormat("0000");
	File dumpFile = new File(filename);
	FileLineIterator it;
	if (filename.endsWith(".bz2")) {
	    // default compression format from http://download.wikimedia.org
	    CompressionCodec codec = new BZip2Codec();
	    it = new FileLineIterator(
		    codec.createInputStream(new FileInputStream(dumpFile)));
	} else {
	    // assume the user has previously de-compressed the dump file
	    it = new FileLineIterator(dumpFile);
	}
	int filenumber = 0;
	int data = 0;
	String file = "chunks/chunk-" + decimalFormatter.format(filenumber)
		+ ".xml.bz2";
	OutputStream chunkWriter = new BufferedOutputStream(
		new FileOutputStream(file), 524288);
	BZip2OutputStream outputStream = new BZip2OutputStream(chunkWriter);
	while (it.hasNext()) {
	    String thisLine = it.next();
	    if (thisLine.trim().startsWith("<page>")) {
		boolean end = false;
		while (!thisLine.trim().startsWith("</page>")) {
		    content.append(thisLine).append('\n');
		    if (it.hasNext()) {
			thisLine = it.next();
		    } else {
			end = true;
			break;
		    }
		}
		content.append(thisLine).append('\n');

		if (content.length() > 1000000 || end) {
		    data += content.length();
		    byte[] buffer = content.toString().getBytes();
		    outputStream.write(buffer, 0, buffer.length);
		    outputStream.flush();
		    content = new StringBuilder();
		    if (data > chunkSize || end) {
			data = 0;
			filenumber++;

			buffer = "</mediawiki>".getBytes();
			outputStream.write(buffer, 0, buffer.length);
			outputStream.close();
			chunkWriter.close();
			file = "chunks/chunk-"
				+ decimalFormatter.format(filenumber)
				+ ".xml.bz2";
			chunkWriter = new BufferedOutputStream(
				new FileOutputStream(file), 524288);
			outputStream = new BZip2OutputStream(chunkWriter);
			if (filenumber >= numChunks) {
			    break;
			}
			content.append(header);
		    }
		}
	    }
	}
	outputStream.close();
	it.close();
    }

    private static Pattern getPattern() {
	String regex = "(celle ci|celles ci|celui ci|ceux ci|cessez le feu|ci dessous"
		+ "|ci dessus|quelques uns|quelques unes|rez de chaussée|royaume uni"
		+ "|sacré cœur)";
	return Pattern.compile(regex);
    }

}
