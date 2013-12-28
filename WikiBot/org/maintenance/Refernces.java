package maintenance;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.wikipedia.Wiki;
import org.wikiutils.ParseUtils;

public class Refernces {
	public static void main(String[] args) {
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
