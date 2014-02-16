package org.wikiutils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.wikipedia.Wiki;

/**
 * Useful parsing methods for MediaWiki syntax.
 *
 * @author Fastily
 *
 * @see org.wikiutils.CollectionUtils
 * @see org.wikiutils.DateUtils
 * @see org.wikiutils.GUIUtils
 * @see org.wikiutils.IOUtils
 * @see org.wikiutils.LoginUtils
 * @see org.wikiutils.StringUtils
 * @see org.wikiutils.WikiUtils
 */
public class ParseUtils {

	/**
	 * Hiding constructor from JavaDoc.
	 */
	private ParseUtils() {
	}

	/**
	 * Gets the target of the redirect page. </br><b>PRECONDITION</b>:
	 * <tt>redirect</tt> must be a Redirect.
	 *
	 * @param redirect
	 *            The title of the redirect to get the target for.
	 * @param wiki
	 *            The wiki object to use.
	 * @return String The title of the redirect's target.
	 * @throws IOException
	 *             If network error
	 */

	public static String getRedirectTarget(String redirect, Wiki wiki)
			throws IOException {
		String text = wiki.getPageText(redirect).trim();

		if (text.matches("(?si)^#(redirect)\\s*?\\[\\[.+?\\]\\].*?"))
			return text.substring(text.indexOf("[[") + 2, text.indexOf("]]"));

		throw new UnsupportedOperationException(
				"Parameter passed in is not a redirect page!");
	}

	/**
	 * Gets redirects of a template, returns then as a String regex. Ready for
	 * replacing instances of templates. Pass in template with "Template:"
	 * prefix.
	 *
	 * @param template
	 *            The title of the main Template (CANNOT BE REDIRECT), including
	 *            the "Template:" prefix.
	 * @param wiki
	 *            The wiki object to use.
	 *
	 * @return The regex, in the form
	 *         (?si)\{\{(Template:)??)(XXXXX|XXXX|XXXX...).*?\}\}, where XXXX is
	 *         the template and its redirects.
	 *
	 * @throws IOException
	 *             If network error.
	 */

	public static String getRedirectsAsRegex(String template, Wiki wiki)
			throws IOException {
		String r = "(?si)\\{{2}?\\s*?(Template:)??\\s*?("
				+ namespaceStrip(template, wiki);
		for (String str : wiki.whatLinksHere(template, true,
				Wiki.TEMPLATE_NAMESPACE))
			r += "|" + namespaceStrip(str, wiki);
		r += ").*?\\}{2}?";

		return r;
	}

	/**
	 * Used to check if <tt>{{bots}}</tt> or <tt>{{robots}}</tt>,
	 * case-insensitive, is present in a String.
	 *
	 * @param text
	 *            The String to check for <tt>{{bots}}</tt> or
	 *            <tt>{{nobots}}</tt>
	 * @param user
	 *            The account to check for, without the "User:" prefix.
	 *
	 * @return boolean True if this particular bot should be allowed to edit
	 *         this page.
	 *
	 */

	public static boolean allowBots(String text, String user) {
		return !text
				.matches("(?i).*?\\{\\{(nobots|bots\\|(allow=none|deny=(.*?"
						+ user + ".*?|all)|optout=all))\\}\\}.*?");
	}

	/**
	 * Replaces all transclusions of a template/page with specified text.
	 *
	 * @param template
	 *            The template (including namespace prefix) to be replaced
	 * @param replacementText
	 *            The text to replace the template with (can include subst:XXXX)
	 * @param reason
	 *            Edit summary to use
	 * @param wiki
	 *            The wiki object to use.
	 * @throws IOException
	 *             If network error.
	 */

	public static void templateReplace(String template, String replacementText,
			String reason, Wiki wiki) throws IOException {
		String[] list = wiki.whatTranscludesHere(template);
		if (template.startsWith("Template:"))
			template = namespaceStrip(template, wiki);

		for (String page : list) {
			try {
				wiki.edit(
						page,
						wiki.getPageText(page).replaceAll(
								"(?i)(" + template + ")", replacementText),
						reason);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Strips the namespace prefix of a page, if applicable. If there is no
	 * namespace attached to the passed in string, then the original string is
	 * returned.
	 *
	 * @param title
	 *            The String to remove a namespace identifier from.
	 * @param wiki
	 *            the home wiki
	 * @return The String without a namespace identifier.
	 * @throws IOException
	 *             if a network error occurs (rare)
	 *
	 */
	public static String namespaceStrip(String title, Wiki wiki)
			throws IOException {
		String ns = wiki.namespaceIdentifier(wiki.namespace(title));
		return ns.isEmpty() ? title : title.substring(ns.length() + 1);
	}

	/**
	 * Attempts to parse out a template parameter.
	 *
	 * @param template
	 *            The template to work on. Must be entered in format
	 *            {{NAME|PARM1|PARAM2|...}}
	 * @param number
	 *            The parameter to retrieve: {{NAME|1|2|3|4...}}
	 *
	 * @return The param we parsed out or null if we didn't find a param
	 *         matching the specified criteria
	 *
	 */
	public static String getTemplateParam(String template, int number) {
		String param = String.valueOf(number);
		int i = number;
		LinkedHashMap<String, String> map = getTemplateParametersWithValue(template);
		for (String key : map.keySet()) {
			if (param.equals(key.trim()))
				return map.get(key);
			else {
				try {
					if (Integer.parseInt(key.trim()) < number)
						i--;
				} catch (NumberFormatException e) {
				}
			}

		}
		return map.get("ParamWithoutName" + i);
	}

	/**
	 * Attempts to parse out a template parameter based on specification.
	 *
	 * @param template
	 *            The template to work on. Must be entered in format
	 *            {{NAME|PARM1|PARAM2|...}}
	 * @param param
	 *            The parameter to retrieve, without "=".
	 * @param trim
	 *            if we should trim the parameter value
	 * @return The param we parsed out or null if we didn't find a param
	 *         matching the specified criteria
	 */

	public static String getTemplateParam(String template, String param,
			boolean trim) {
		LinkedHashMap<String, String> map = getTemplateParametersWithValue(template);
		if (map == null)
			return null;
		for (String key : map.keySet())
			if (key.trim().equals(param.trim()))
				if (trim)
					return map.get(key).trim();
				else
					return map.get(key);

		return null; // if nothing matched
	}

	/**
	 * Count occurrences of substring.
	 *
	 * @param text
	 *            the string
	 * @param substring
	 *            the substring
	 * @return the number of occurences
	 */
	public static int countOccurrences(String text, String substring) {
		Pattern p = Pattern.compile(Pattern.quote(substring));
		Matcher m = p.matcher(text);
		int count = 0;
		while (m.find()) {
			count += 1;
		}
		return count;
	}

	/**
	 * Removes a parameter and its value from the given template.
	 *
	 * @param template
	 *            the template
	 * @param param
	 *            the parameter to remove
	 * @return the new template
	 */
	public static String removeTemplateParam(String template, String param) {
		LinkedHashMap<String, String> map = getTemplateParametersWithValue(template);
		if (map == null)
			return template;
		for (String key : map.keySet()) {
			if (key.trim().equals(param.trim())) {
				map.remove(key);
				return templateFromMap(map);
			}
		}
		return template;
	}

	/**
	 * Removes a parameter from the given template.
	 *
	 * @param template
	 *            the template
	 * @param position
	 *            of the parameter to remove
	 */
	public static String removeTemplateParam(String template, int number) {
		String param = String.valueOf(number);
		int i = number;
		LinkedHashMap<String, String> map = getTemplateParametersWithValue(template);
		for (String key : map.keySet()) {
			// we check the case like : {{name|1|2|3=}}
			if (param.equals(key.trim())) {
				map.remove(key);
				return templateFromMap(map);
			}
		}
		map.remove("ParamWithoutName" + i);
		return templateFromMap(map);
	}

	/**
	 * Gets the template paramerters.
	 *
	 * @param template
	 *            the template
	 * @return the template paramerters
	 */
	public static ArrayList<String> getTemplateParamerters(String template) {
		ArrayList<String> f = new ArrayList<String>();
		int i = template.indexOf('|');
		if (i == -1)
			return null; // the template doesn't have parameters;
		template = template.substring(i + 1, template.length() - 2);
		for (String s : template.split("\\|"))
			f.add(s);
		if (template.endsWith("|"))
			f.add("");

		for (i = 0; i < f.size(); i++) {
			String s = f.get(i);
			String temp = removeCommentsAndNoWikiText(s);
			if ((countOccurrences(temp, "{{") != countOccurrences(temp, "}}") || countOccurrences(
					temp, "[[") != countOccurrences(temp, "]]"))
					&& i != f.size() - 1) {
				s += "|" + f.get(i + 1);
				f.remove(i);
				f.remove(i);
				f.add(i, s);
				i--;
			}
		}
		return f;
	}

	/**
	 * Returns the param of a template. e.g. If we get "|foo = baz", we return
	 * baz.
	 *
	 * @param p
	 *            Must be a param in the form "|foo = baz" or "foo = baz"
	 *
	 * @return The param we parsed out
	 *
	 */

	public static String templateParamStrip(String p) {
		int i = p.indexOf("=");
		if (i == -1)
			return p.replace("}}", "").trim();
		else
			return p.substring(i + 1).replace("}}", "").trim();
	}

	/**
	 * Parses out the first instance of a template from a body of text, based on
	 * specified template.
	 *
	 * @param text
	 *            Text to search
	 * @param template
	 *            The Template (in template namespace) to look for. DO NOT add
	 *            namespace prefix.
	 * @param redirects
	 *            Specify <tt>true</tt> to incorporate redirects in this parse
	 *            job for this template.
	 * @param wiki
	 *            The wiki object to use
	 *
	 * @return The template we parsed out, in the form
	 *         {{TEMPLATE|ARG1|ARG2|...}} or NULL, if we didn't find the
	 *         specified template.
	 * @throws IOException
	 *             If network error
	 */

	public static String parseTemplateFromPage(String text, String template,
			boolean redirects, Wiki wiki) throws IOException {
		return redirects ? parseFromPageRegex(text,
				getRedirectsAsRegex("Template:" + template, wiki))
				: parseFromPageRegex(text,
						"(?si)\\{\\{\\s*?(Template:)??\\s*?(" + template
								+ ").*?\\}\\}");
	}

	/**
	 * Parses out the first group of matching text, based on specified regex.
	 * Useful for parsing out templates.
	 *
	 * @param text
	 *            Text to search
	 * @param regex
	 *            The regex to use
	 *
	 * @return The text we parsed out, or null if we didn't find anything.
	 *
	 */

	public static String parseFromPageRegex(String text, String regex) {
		Matcher m = Pattern.compile(regex).matcher(text);
		if (m.find())
			return text.substring(m.start(), m.end());
		else
			return null;
	}

	/**
	 * Add the character c at the end of the string until its length will be
	 * len.
	 *
	 * @param c
	 *            the character to add
	 * @param len
	 *            the final length
	 * @return the string
	 */
	public static String getString(char c, int len) {
		if (len == 0)
			return "";
		String str = "";
		for (int i = 0; i < len; i++)
			str += String.valueOf(c);
		return str;

	}

	/**
	 * Insert a substring into the given string.
	 *
	 * @param s1
	 *            the String
	 * @param s2
	 *            the Substring
	 * @param index
	 *            the position we to insert the substring
	 * @return the result string
	 */
	public static String insert(String s1, String s2, int index) {
		String bagBegin = s1.substring(0, index);
		String bagEnd = s1.substring(index);
		return bagBegin + s2 + bagEnd;
	}

	/**
	 * Gets all the templates <i>template</i> contained in text.
	 *
	 * @param template
	 *            the template name
	 * @param text
	 *            the text
	 * @return the templates
	 */
	public static ArrayList<String> getTemplates(String template, String text) {
		HashMap<Integer, Integer> noWiki = getIgnorePositions(text, "<nowiki>",
				"</nowiki>");
		HashMap<Integer, Integer> comment = getIgnorePositions(text, "<!-",
				"->");
		ArrayList<String> al = new ArrayList<String>();
		char firstChar = template.charAt(0);
		template = template.substring(1);
		Pattern p = Pattern.compile("\\{\\{\\s*("
				+ Character.toLowerCase(firstChar) + "|"
				+ Character.toUpperCase(firstChar) + ")"
				+ Pattern.quote(template) + "\\s*[\\|\\}]");
		Matcher m = p.matcher(text);
		while (m.find()) {
			int startPos = m.start();
			if (isIgnorePosition(noWiki, startPos)
					|| isIgnorePosition(comment, startPos))
				continue;
			int i = startPos + 2;
			int nb = 1;
			int len = text.length();
			while (nb != 0 && i < len - 1) {
				if (text.charAt(i) == '{' && text.charAt(i + 1) == '{') {
					nb++;
					i++;
				} else {
					if (text.charAt(i) == '}' && text.charAt(i + 1) == '}') {
						nb--;
						i++;
					}
				}
				i++;
			}
			if (i > len)
				continue;
			i = (i + 1 > len) ? len : i + 1;
			String temp = text.substring(startPos, i);
			if (!temp.endsWith("}}"))
				temp = temp.substring(0, temp.length() - 1);
			al.add(temp);
		}

		return al;
	}

	/**
	 * Removes the comments and no wiki text from text.
	 *
	 * @param text
	 *            the text
	 * @return the new text
	 */
	public static String removeCommentsAndNoWikiText(String text) {
		if (text == null)
			return null;
		text = text.replaceAll("(?s)<\\s*nowiki\\s*>.*?<\\s*/nowiki\\s*>", "");
		return text.replaceAll("(?s)<!--.*?-->", "");
	}

	/**
	 * Checks if the given position must be ignored. Useful to not change text
	 * between noWiki tags or comments text
	 *
	 * @param map
	 *            the map that contains position to ignore
	 * @param position
	 *            the position
	 * @return true, if the position will be ignored
	 */
	private static boolean isIgnorePosition(HashMap<Integer, Integer> map,
			int position) {
		if (map == null)
			return false;
		for (Integer pos : map.keySet()) {
			if (position > pos && position < map.get(pos))
				return true;
		}
		return false;
	}

	/**
	 * Gets the position of the text that start with a specified String and ends
	 * with a specfied string. Used to get the start and end position of noWiki
	 * text and comments
	 *
	 * @param text
	 *            the text
	 * @param start
	 *            the starting string
	 * @param end
	 *            the ending string
	 * @return the start and end of text contanied between no
	 */
	private static HashMap<Integer, Integer> getIgnorePositions(String text,
			String start, String end) {
		int startPos = text.indexOf(start);
		if (startPos == -1)
			return null;
		HashMap<Integer, Integer> noWikiPos = new HashMap<Integer, Integer>();
		while (startPos != -1) {
			int endPos = text.indexOf(end, startPos);
			if (endPos != -1)
				noWikiPos.put(startPos, endPos);
			else
				return noWikiPos; // article with error
			startPos = text.indexOf(start, endPos);

		}
		return noWikiPos;
	}

	/**
	 * Gets the internal links contained in a given text.
	 *
	 * @param text
	 *            the text
	 * @return the internal links
	 */
	public static ArrayList<String> getInternalLinks(String text) {
		ArrayList<String> al = new ArrayList<String>();
		text = removeCommentsAndNoWikiText(text);
		Pattern p = Pattern.compile("\\[\\[.*?\\]\\]");
		Matcher m = p.matcher(text);
		while (m.find()) {
			al.add(m.group().substring(2, m.group().length() - 2));
		}
		return al;
	}

	/**
	 * Add a parameter to a given template. If the parameter already exist we
	 * replace its value by the new value.
	 *
	 * @param template
	 *            the template
	 * @param param
	 *            the parameter name
	 * @param value
	 *            the parameter value
	 * @param adjust
	 *            if we should adjust the parameter length with spaces
	 * @return the new template
	 */
	public static String setTemplateParam(String template, String param,
			String value, boolean adjust) {

		LinkedHashMap<String, String> map = getTemplateParametersWithValue(template);
		if (map == null)
			return null;
		boolean added = false;
		for (String key : map.keySet()) {
			if (!key.trim().equals(param.trim())) {
				continue;
			} else {
				map.put(key, value);
				added = true;
			}
		}
		if (!added)
			map.put(param, value);
		if (adjust)
			return templateFromMap(adjust(map));
		else
			return templateFromMap(map);
	}

	/**
	 * Construct a template from map that contains its parameters and their
	 * values.
	 *
	 * @param map
	 *            the map
	 * @return the template
	 */
	public static String templateFromMap(HashMap<String, String> map) {
		String templateName = map.get("templateName");
		if(templateName == null)
			return null;
		String template = "{{" + map.get("templateName");
		for (String key : map.keySet()) {
			if (key.equals("templateName"))
				continue;
			if (key.startsWith("ParamWithoutName"))
				template += "|" + map.get(key);
			else
				template += "|" + key + "=" + map.get(key);
		}
		template += "}}";
		return template;
	}

	/**
	 * Rename a template parameter from the given template.
	 *
	 * @param template
	 *            the template
	 * @param param
	 *            the parameter to rename
	 * @param name
	 *            the new name
	 * @param adjust
	 *            if we should adjust the parameters length
	 * @return the new template
	 */
	public static String renameTemplateParam(String template, String param,
			String name, boolean adjust) {
		LinkedHashMap<String, String> map = getTemplateParametersWithValue(template);
		if (map == null)
			return null;
		LinkedHashMap<String, String> newMap = new LinkedHashMap<String, String>();
		for (String key : map.keySet()) {
			if (key.trim().equals(param.trim())) {
				newMap.put(name, map.get(key));
			} else
				newMap.put(key, map.get(key));
		}
		if (adjust)
			return templateFromMap(adjust(newMap));
		else
			return templateFromMap(newMap);
	}

	/**
	 * Make that all the parameters have the same length. Useful with Infobox
	 * template
	 *
	 * @param map
	 *            the map that contains the parameters
	 * @return the new map
	 */
	public static LinkedHashMap<String, String> adjust(
			LinkedHashMap<String, String> map) {
		LinkedHashMap<String, String> newMap = new LinkedHashMap<String, String>();
		int length = 0;
		for (String key : map.keySet()) {
			key = key.replace("\t", "    "); // replace tabs whith four spaces
			if (key.trim().equals("templateName"))
				continue;
			if (key.length() > length)
				length = key.length();
		}
		for (String key : map.keySet()) {
			if (key.trim().equals("templateName"))
				newMap.put(key, map.get(key));
			else
				newMap.put(adjust(key, length), map.get(key));
		}
		return newMap;
	}

	/**
	 * Add spaces at the end of the string until its length will be length.
	 *
	 * @param key
	 *            the string
	 * @param length
	 *            the length
	 * @return the new key
	 */
	private static String adjust(String key, int length) {
		key = key.replace("\t", "    ");
		int len = key.length();
		for (int i = 0; i < length - len; i++) {
			key += " ";
		}
		return key;
	}

	/**
	 * Gets the template parameters with their value. If a parameter appears
	 * many times in the template just its last value will be taken.
	 *
	 * @param template
	 *            the template
	 * @return the template parameters with value
	 */
	public static LinkedHashMap<String, String> getTemplateParametersWithValue(
			String template) {
		if (template == null)
			return null;
		int index = template.indexOf("|");
		String templateName;
		if (index == -1)
			templateName = template.substring(2, template.length() - 2);
		else
			templateName = template.substring(2, index);
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put("templateName", templateName);
		ArrayList<String> al = getTemplateParamerters(template);
		if (al == null)
			return map;
		int j = 1;
		int size = al.size();
		for (int i = 0; i < size; i++) {
			index = al.get(i).indexOf("=");
			if (index == -1) {
				map.put("ParamWithoutName" + (j), al.get(i));
				j++;
			} else {
				String param = al.get(i).substring(0, index); // don't trim
				String value = al.get(i).substring(index + 1);
				map.put(param, value);
			}
		}
		return map;
	}

	/**
	 * Gets the template parameter.
	 *
	 * @param map
	 *            the map
	 * @param param
	 *            the param
	 * @param trim
	 *            if we should trim the result
	 * @return the template param
	 */
	public static String getTemplateParam(LinkedHashMap<String, String> map,
			String param, boolean trim) {
		for (String key : map.keySet()) {
			if (key.trim().equals(param.trim()))
				if (trim)
					return map.get(key).trim();
				else
					return map.get(key);
		}
		return null;
	}

	/**
	 * Gets the template name.
	 *
	 * @param template
	 *            the template
	 * @return the template name
	 */
	public static String getTemplateName(String template) {
		if (template == null)
			return null;
		int index = template.indexOf("|");
		String templateName;
		if (index == -1)
			templateName = template.substring(2, template.length() - 2);
		else
			templateName = template.substring(2, index);
		return templateName;
	}

	public static String getInternalLinkTitle(String internalLink){
		if(internalLink == null)
			return null;
		Pattern p = Pattern.compile("\\[\\[.*?\\|(.*)\\]\\]");
		Matcher m = p.matcher(internalLink);
		if(m.find())
			return m.group(1);
		else
			return internalLink;
	}

	public static String formatTemplate(String template) {
		LinkedHashMap<String, String> map = ParseUtils
			.getTemplateParametersWithValue(template);
		LinkedHashMap<String, String> newMap = new LinkedHashMap<String, String>();
		for (Entry<String, String> entry : map.entrySet()) {
		    String key = entry.getKey();
		    String value = entry.getValue();
		    if (!value.endsWith(" "))
			value += " ";
		    if (!key.equals("templateName") && !key.startsWith(" "))
			key = " " + key;
		    if (!value.startsWith(" ") && !key.equals("templateName") && !key.startsWith("ParamWithoutName"))
			value = " " + value;
		    newMap.put(key, value);
		}
		return ParseUtils.templateFromMap(ParseUtils.adjust(newMap)).replace(
			"\n }}", "\n}}");
	    }
}