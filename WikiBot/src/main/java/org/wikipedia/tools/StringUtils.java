package org.wikipedia.tools;

import net.ricecode.similarity.LevenshteinDistanceStrategy;
import net.ricecode.similarity.StringSimilarityService;
import net.ricecode.similarity.StringSimilarityServiceImpl;

public class StringUtils {

    public static int longestSubstr(String first, String second) {
	if (first == null || second == null || first.length() == 0
		|| second.length() == 0) {
	    return 0;
	}

	int maxLen = 0;
	int fl = first.length();
	int sl = second.length();
	int[][] table = new int[fl + 1][sl + 1];

	for (int s = 0; s <= sl; s++)
	    table[0][s] = 0;
	for (int f = 0; f <= fl; f++)
	    table[f][0] = 0;

	for (int i = 1; i <= fl; i++) {
	    for (int j = 1; j <= sl; j++) {
		if (first.charAt(i - 1) == second.charAt(j - 1)) {
		    if (i == 1 || j == 1) {
			table[i][j] = 1;
		    } else {
			table[i][j] = table[i - 1][j - 1] + 1;
		    }
		    if (table[i][j] > maxLen) {
			maxLen = table[i][j];
		    }
		}
	    }
	}
	return maxLen;
    }

    public static double compare(String s1, String s2){
	    LevenshteinDistanceStrategy strategy = new LevenshteinDistanceStrategy();
	    StringSimilarityService service = new StringSimilarityServiceImpl(strategy);
	    double score = service.score(s1, s2);
	    System.out.println("Comparing " + s1 + " and " + s2 + " give " + score);
	    return score;
	    
	}
}
