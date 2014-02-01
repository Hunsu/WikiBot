package org.maintenance;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.security.auth.login.LoginException;

import org.wikipedia.Wiki;
import org.wikiutils.ParseUtils;

import Tools.Login;

/**
 * The Class Refernces.
 */
public class Refernces {

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		correctRefslist();
	}

	/**
	 * Correct refslist.
	 */
	public static void correctRefslist() {
		Wiki wiki = new Wiki("fr.wikipedia.org");
		try {
			Login login = new Login();
			wiki.login(login.getLogin(), login.getPassword());

			String[] titles = wiki
					.getCategoryMembers("Page utilisant un modèle avec un paramètre obsolète");
			for (int i = 0; i < titles.length; i++) {
				if (titles[i].toLowerCase().startsWith("catégorie:"))
					continue;
				else {
					String text = wiki.getPageText(titles[i]);
					String oldText = text;
					ArrayList<String> al = ParseUtils.getTemplates("reflist",
							text);
					al.addAll(ParseUtils.getTemplates("Références", text));
					al.addAll(ParseUtils.getTemplates("references", text));
					// al.addAll(ParseUtils.getTemplates("multi bandeau",
					// text));
					if(al.size() > 1)
						System.out.println("Problem : "+titles[i]);
					for (int j = 0; j < al.size(); j++) {
						String template = al.get(j);
						template = ParseUtils.setTemplateParam(template, "templateName", "Références", false);
						template = ParseUtils.renameTemplateParam(template, "group", "groupe", false);
						template = ParseUtils.removeTemplateParam(template,
								"colwidth");
						for (int k = 1; k < 3; k++) {
							String param = ParseUtils.getTemplateParam(
									template, k);
							if (param != null) {
								if (param.trim().length() > 2 || param.trim().equals("1"))
									template = ParseUtils.removeTemplateParam(
											template, 1);
								else {
									try {
										int cols = Integer.parseInt(param
												.trim());
										template = ParseUtils
												.removeTemplateParam(template,
														1);
										template = ParseUtils.setTemplateParam(
												template, "colonnes",
												String.valueOf(cols), false);
									} catch (Exception e) {
										template = ParseUtils
												.removeTemplateParam(template,
														1);
									}
								}
							}
						}
						text = text.replace(al.get(j), template);
					}
					if (!text.equals(oldText))
						wiki.edit(titles[i], text, "bot: Maintenance");
				}
			}

		} catch (IOException | LoginException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Coorect refs.
	 */
	public static void coorectRefs() {
		try {
			FileInputStream fstream = new FileInputStream("titles.txt");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			// Writer out = new PrintWriter("");
			String strLine = "";
			int i = 0;
			while ((strLine = br.readLine()) != null) {
				try {
					Wiki wiki = new Wiki("fr.wikipedia.org");
					wiki.login("Hunsu", "MegamiMonster");
					String text = wiki.getPageText(strLine);
					String oldtext = text;
					// wiki.edit("Utilisateur:Hunsu/Brouillons", text, "");
					i++;
					ArrayList<String> al = ParseUtils.getTemplates(
							"Références", text);
					int nb = al.size();
					for (int k = 0; k < nb; k++) {
						String template = al.get(k);
						template = ParseUtils.renameTemplateParam(template,
								"Colonnes", "colonnes", false);
						template = ParseUtils.renameTemplateParam(template,
								"colonne", "colonnes", false);
						template = ParseUtils.renameTemplateParam(template,
								"col", "colonnes", false);
						template = ParseUtils.renameTemplateParam(template,
								"colones", "colonnes", false);
						template = ParseUtils.renameTemplateParam(template,
								"cols", "colonnes", false);
						text = text.replace(al.get(k), template);
					}

					// wiki.edit("Utilisateur:Hunsu/Brouillons", text, "");
					if (oldtext.equals(text)) {
						System.out.println(strLine);
						continue;
					}

					else {
						text = text.replace("{{pdf}} {{lien web",
								"{{lien web|format=pdf");
						text = text.replace("{{Pdf}} {{lien web",
								"{{lien web|format=pdf");
						text = text.replace("{{Pdf}} {{Lien web",
								"{{Lien web|format=pdf");
						text = text.replace("{{pdf}} {{Lien web",
								"{{Lien web|format=pdf");
						text = text.replace("{{en}} {{lien web",
								"{{lien web|langue=en");
						text = text.replace("{{en}} {{Lien web",
								"{{Lien web|langue=en");
						wiki.edit(strLine, text, "Maintenance");
						System.out.println(strLine);
					}
					if (i % 5 == 0) {
						if (wiki.hasNewMessages()) {
							System.out.print("true\n");
							System.exit(-1);
						}
					}
					// out.write(text);
					// out.flush();
					// text = text.replaceAll(pattern, "|consulté le=");

					// wiki.edit(strLine, text, "Maintenance lien web");
				} catch (Exception e) {
					System.out.print("Erreur" + e);
					e.printStackTrace();
				}
				// if (wiki.hasNewMessages()) break;

			}
			// out.close();
			in.close();

		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}

	}
}
