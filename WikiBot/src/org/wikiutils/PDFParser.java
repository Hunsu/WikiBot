package org.wikiutils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.TaggedPdfReaderTool;

public class PDFParser {

	private static String[] frMonth = { "janvier", "février", "mars", "avril",
			"mai", "juin", "juillet", "août", "septembre", "octobre",
			"novembre", "décembre" };
	private static String[] enMonth = { "January", "February", "March",
			"April", "May", "June", "July", "August", "September", "October",
			"November", "December" };


	public static void processFiles(String urls) {
		File dir = new File("pdfs");
		String[] files = dir.list();
		files = removesXMLfiles(files);
		int processed = 0;
		int i = 0;
		for (i = 0; i < files.length; i++) {
			String filename = files[i];
			if (!filename.toLowerCase().endsWith(".pdf"))
				continue;
			try {
				System.out.println("processing file " + filename);
				if (createXMLFromPdf(dir.getAbsolutePath() + File.separator
						+ filename,urls)) {
					parseXML(dir.getAbsolutePath() + File.separator
							+ filename.replace(".pdf", ".xml"),urls);
					processed++;
				} else
					FileUtils.writeStringToFile(new File("unprocessed.txt"),
							filename + "\n", true);
				i++;
			} catch (Exception e) {
				// System.out.println(e);
				e.printStackTrace();
				// System.exit(-1);
			}
		}
		System.out.println(processed + "/" + i);

	}

	private static String[] removesXMLfiles(String[] files) {
		ArrayList<String> al = new ArrayList<String>(files.length);
		for(String file : files){
			if(!file.endsWith(".xml"))
				al.add(file);
		}
		return al.toArray(new String[al.size()]);
	}

	private static boolean createXMLFromPdf(String filename,String urls) throws IOException {
		try {
			PdfReader reader = new PdfReader(filename);
			//File file = new File(filename.replace(".pdf", ".xml"));
			//if (file.exists())
				//file = null;// return false;
			TaggedPdfReaderTool readertool = new TaggedPdfReaderTool();
			readertool.convertToXml(reader,
					new FileOutputStream(filename.replace(".pdf", ".xml")));
			reader.close();
			return true;
		} catch (Exception e) {
			PdfReader reader = new PdfReader(filename);
			String str = PdfTextExtractor.getTextFromPage(reader, 1);
			process(str,filename,urls);
			System.out.println(filename.substring(filename
					.lastIndexOf(File.separator) + 1));
			new File(filename.replace(".pdf", ".xml")).delete();
			// e.printStackTrace();
			return false;
			//
		}

	}

	private static void process(String text,String filename,String urls) throws IOException {
		String[] lines = text.split("\\n");
		String url = "";
		Calendar date = getDate(lines);
		if (date == null) {
			System.out.println("Problem with the file : no date found "
					+ lines[1]);
			return;
		}
		//url += getDateForURL(date) + ".pdf";
		url = getUrl(filename,urls);
		String ref = getRef(url);
		int j = 6;
		for (int i = 0; i < 30; i++) {
			try {
				String brodcaster = getBrodcaster(lines[j]).replace("Total", "");
				lines[j] = lines[j].replace(" " + brodcaster, "");
				String[] cells = lines[j].split(" ");
				String showName = "";
				String day = cells[cells.length - 4];
				String str = getDate(date, day);
				String viewers = cells[cells.length - 1];
				int k = 1;
				while (k < cells.length - 4) {
					showName += " " + cells[k];
					k++;
				}
				j++;
				showName = formatShowName(showName.trim());
				Pattern p = Pattern.compile("\\d\\d:\\d\\d");
				if (p.matcher(showName).find() || showName.length() > 30) {
					System.out.println("Problem : " + showName + "\nlength : "
							+ showName.length());
					continue;
				}
				FileUtils.writeStringToFile(
						new File("shows/"
								+ showName.replace("/", "_backslash_").trim()), str
								+ "\n" + brodcaster + "\n" + formatViewers(viewers, ref) + "\n",
						true);
			} catch (Exception e) {
				System.out.println(e);
			}
		}

	}


	private static String getBrodcaster(String text) {
		String[] brodcasters = {"CTV Total", "TSN+", "CBC Total", "Global Canada Com", "Global Canada",
					"City TV Total", "'A' Total", "Discovery SD+",
					"Global Com", "CTV Com", "Global Total"};
		for(String brodcaster : brodcasters)
			if(text.indexOf(brodcaster) != -1)
				return brodcaster;
		return null;
	}

	private static void parseXML(String filename,String urls) throws IOException {
		File file = new File(filename);
		String url = "http://www.bbm.ca/_documents/top_30_tv_programs_english/";
		String text = FileUtils.readFileToString(file);
		text = text.replaceAll("\\n\\n*", "\n")
				.replace("<TR>\n<TD>", "<TR><TD>").replace("\n</TD>", "</TD>")
				.replace("\n<TR></TR>", "");
		String[] lines = text.split("\\n");
		Calendar date = getDate(lines);
		if (date == null) {
			System.out.println("Problem with the file : no date found "
					+ removeTags(lines[3]));
			return;
		}
		//url += getDateForURL(date) + ".pdf";
		url = getUrl(filename.replace(".xml", ".pdf"),urls);
		String ref = getRef(url);

		int j = 4;
		for (int i = 0; i < 30; i++) {
			while ((Integer(removeTags(lines[j]))) == null
					|| Integer(removeTags(lines[j])) > 30)
				j++;
			j++;
			String showName = formatShowName(removeTags(lines[j]));
			showName = showName.replace("/", "_backslash_").trim().replace("&apos;", "'").replace("&amp;", "&");
			String brodcaster = removeTags(lines[j+1]);
			String day = removeTags(lines[j + 2]);
			String str = getDate(date, day);
			String viewers = removeTags(lines[j + 5]);
			j += 5;
			FileUtils.writeStringToFile(new File("shows/" + showName), str
					+ "\n" + brodcaster + "\n"+ formatViewers(viewers, ref) + "\n", true);
		}
		// file.delete();
	}

	private static String getUrl(String filename,String urls) {
		filename = filename.substring(filename.lastIndexOf("/")+1);
		int index = urls.indexOf(filename);
		if(index == -1){
			System.out.println("Problem : " + filename);
			return null;
		}
		int i = urls.lastIndexOf("http", index);
		return urls.substring(i,index+filename.length());
	}

	private static Integer Integer(String str) {
		try {
			return Integer.parseInt(str);
		} catch (Exception e) {
			return null;
		}
	}

	private static String formatShowName(String showName) {
		String[] str = showName.split(" ");
		showName = "";
		for (int i = 0; i < str.length; i++) {
			String temp = str[i];
			if (temp.indexOf(".") == -1 && temp.length() > 1) {
				temp = temp.toLowerCase();
				temp = temp.substring(0, 1).toUpperCase() + temp.substring(1);
			}
			showName += " " + temp;

		}
		return showName;
	}

	private static String getRef(String url) {
		Calendar cal = Calendar.getInstance();
		String date = cal.get(Calendar.DATE) + " "
				+ getMonthName(cal.get(Calendar.MONTH)) + " "
				+ cal.get(Calendar.YEAR);
		return "<ref>{{lien web|format=pdf|url="
				+ url
				+ "|titre=Audience de l'épisode au Canada|éditeur=BBM|consulté le="
				+ date + "}}</ref>";
	}

	/*private static String getDateForURL(Calendar date) {
		String str = date.get(Calendar.YEAR) + "/nat";
		int month = date.get(Calendar.MONTH) + 1;
		int day = date.get(Calendar.DATE);
		if (month < 10)
			str += "0" + String.valueOf(month);
		else
			str += month;
		if (day < 10)
			str += "0" + day;
		else
			str += day;
		str += date.get(Calendar.YEAR);
		return str;
	}*/

	private static String formatViewers(String viewers, String ref) {
		float v = Float.parseFloat(viewers);
		v = (float) v / 1000;
		viewers = String.valueOf(v);
		int index = viewers.indexOf(".");
		if (index > 0) {
			if (viewers.length() > index + 3) {
				viewers = viewers.substring(0, index + 3);
			}
		}
		return "* {{Audience|Canada|" + viewers + "|M}}" + ref
				+ "<small>(première diffusion)</small>\n";
	}

	private static String getDate(Calendar date, String day) {
		int i = getday(day);
		Calendar cal = (Calendar) date.clone();
		cal.add(Calendar.DAY_OF_MONTH, i);
		return cal.get(Calendar.DATE) + " "
				+ getMonthName(cal.get(Calendar.MONTH)) + " "
				+ cal.get(Calendar.YEAR);
	}

	private static String getMonthName(int month) {
		return new DateFormatSymbols().getMonths()[month];
	}

	private static int getday(String day) {
		int i = 0;
		while (day.charAt(i) == '.')
			i++;
		return i;
	}

	private static String removeTags(String str) {
		return str.replaceAll("<.*?/?>", "");
	}

	private static Calendar getDate(String[] lines) {
		for (int i = 0; i < lines.length; i++) {
			String line = translateDate(removeTags(lines[i]));
			Pattern p = Pattern
					.compile("(.*?) (\\d\\d?).*?(\\d\\d\\d\\d)?.*?(\\d\\d\\d\\d)");
			Matcher m = p.matcher(line);
			if (m.find()) {
				DateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
				Date d;
				try {
					String year = "";
					if (m.group(3) != null)
						year = m.group(3);
					else
						year = m.group(4);
					d = (Date) formatter.parse(m.group(2) + " " + m.group(1)
							+ " " + year);
				} catch (ParseException e) {
					continue;
				}
				Calendar cal = Calendar.getInstance();
				cal.setTime(d);
				return cal;
			}
		}
		return null;
	}

	private static String translateDate(String date) {
		for (int i = 0; i < 12; i++) {
			date = date.replace(enMonth[i], frMonth[i]);
		}
		return date;
	}

}
