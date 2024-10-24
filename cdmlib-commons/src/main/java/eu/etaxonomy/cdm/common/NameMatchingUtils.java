/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author andreabee90
 * @since 13.08.2023
 */
public class NameMatchingUtils {

//  Delete known text elements such as cf. aff. and subgenera if it is enclosed in brackets

    public static String removeExtraElements(String str) {
    	str = str.toUpperCase();
        String[] extraElements = {" AFF. ", " AFF ", " SUBSP. ",
        		" SUBSP. ", " SP ", " SP. ",
        		" SPP. ", " SPP ", " CF. ",
        		" CF "};

        String [] parsedFullString = str.split(" ");
        if (parsedFullString[1].contains("(")) {
            str = str.replace(parsedFullString[1], "");
        }
        str = deleteEmptySpaces(str);

        for (int j = 0; j < extraElements.length; j++) {
            if (str.contains(extraElements[j])) {
                str = str.replace(extraElements[j], " ");
            }
        }
        return str;
    }

//  Delete HTML ampersand

    public static String removeHTMLAmpersand (String str) {
        if (str == null || str.isEmpty() || str.trim().isEmpty()) {
          return "";
        } else {
          str = str.trim();

          if (str.contains("&amp;")) {
              str = str.replace("&amp;", "&");
          }
          if (str.contains("&AMP;")) {
              str = str.replace("&AMP;", "&");
          }

          if (str.contains("<") && str.contains(">")) {
            String firstStrPart = null;
            String secondStrPart = null;

            while (str.contains("<") && str.contains(">")) {
              firstStrPart = str.substring(0, str.indexOf("<"));
              secondStrPart = str.substring(str.indexOf(">") + 1);
              str= (firstStrPart + " " + secondStrPart).replace("  ", " ");
            }
          }
          return str;
        }
      }
//	Trim white spaces

	public static String deleteEmptySpaces(String inputName) {
		String outputName = inputName.replaceAll("\\s+", " ").trim();
		return outputName;
	}

//	Replace characters with ASCII characters

	public static String replaceSpecialCharacters(String str) {
		String output = str.toUpperCase();
		output = output.replaceAll("[ÁÅÂÃÀÄ]", "A");
		output = output.replaceAll("[ÉÊÈË]", "E");
		output = output.replaceAll("[ÔÕØÒÓÖ]", "O");
		output = output.replaceAll("[ÌÍÎÏ]", "I");
		output = output.replaceAll("[ÜÛÚÙ]", "U");
		output = output.replaceAll("Ñ", "N");
		output = output.replaceAll("Ç", "C");
		return output;
	}

//	Change lists to uppercase

	public static List <String> listToUpperCase(List<String> List) {
		List <String> upperCaseList = new ArrayList<>();
		for (String listElement : List) {
			upperCaseList.add(listElement.toUpperCase());
		}
		return upperCaseList ;
	}

// Phonetic changes performed ONLY on the initial characters of each String

    public static String replaceInitialCharacter(String input) {
        String output = input.toUpperCase();
        String[][] phoneticChange = {
        		{"AE", "E"},
        	    {"CN", "N"},
        	    {"CT", "T"},
        	    {"CZ", "C"},
        	    {"DJ", "D"},
        	    {"EA", "E"},
        	    {"EU", "U"},
        	    {"GN", "N"},
        	    {"KN", "N"},
        	    {"MC", "MAC"},
        	    {"MN", "N"},
        	    {"OE", "E"},
        	    {"QU", "Q"},
        	    {"PH", "F"},
        	    {"PS", "S"},
        	    {"PT", "T"},
        	    {"TS", "S"},
        	    {"WR", "R"},
        	    {"X", "Z"}
                };
        for (int i = 0 ; i < phoneticChange.length; i++) {
            if (output.startsWith(phoneticChange[i][0])){
                    output= output.replaceFirst(phoneticChange[i][0], phoneticChange[i][1]);
                    break;
            }
        }
        return output;
    }

// Replace characters combinations that sound similar

	public static String soundalike(String inputName) {
		inputName = inputName.toUpperCase();
		String[][] soundalike = {
				 {"AE", "E"},
				    {"IA", "A"},
				    {"OE", "I"},
				    {"OI", "A"},
				    {"SC", "S"}
				};
		for (int i = 0; i < soundalike.length; i++) {
			if (inputName.contains(soundalike[i][0])) {
			inputName = inputName.replace(soundalike[i][0],soundalike[i][1]);
			}
		}
		return inputName;
	}

//	Remove duplicated letters

	public static String removeDuplicate(String input) {
		char [] temp = input.toCharArray();
		int lenght = temp.length;

		int index = 0;
		int p;
		for (int i = 0; i < lenght- 1; i++) {
			p = i + 1;
			if (!(temp[i] == temp[p])) {
				temp[index++] = temp[i];
			}
		}
		String output = String.valueOf(Arrays.copyOf(temp, index));
		output = output + temp[lenght - 1];
		return output;
	}

//	normalize ending ignoring gender issues

	public static String replaceGenderEnding(String input) {
		input = input.toUpperCase();
		String output = input;
		if (input.length() >= 2) {
		    String firstPart = input.substring(0, input.length() - 2);
		    String lastTwoChar = input.substring((input.length() - 2), input.length());
		    String[] endingChar = new String[] {"IS", "US", "YS", "ES", "IM", "AS", "UM", "OS"};
		    for (String i : endingChar) {
		        if (lastTwoChar.contains(i)) {
		            lastTwoChar = lastTwoChar.replace(i, "A");
		        }
		    }
		    output = firstPart + lastTwoChar;
		}
		return output;
	}

	public static String normalize(String str) {
	    String result;
	    result = str.toUpperCase();
	    result = NameMatchingUtils.replaceSpecialCharacters(result);
	    return result;
	}

	public static String nearMatch(String str) {
	    String result;
	    result = replaceInitialCharacter(str);
	    result = soundalike(result);
	    result = removeDuplicate(result);
	    result = replaceGenderEnding(result);
        return result;
	}

    public static int modifiedDamerauLevenshteinDistance(String str1, String str2) {
        if (CdmUtils.nullSafeEqual(str1, str2)) {
    		return 0;
    	} else if (str1.isEmpty()) {
    		return str2.length();
    	} else if (str2.isEmpty()) {
    		return str1.length();
    	} else if (str2.length() == 1 && str1.length() == 1 && !str1.equals(str2)) {
    		return 1;
    	} else {

    		int[][] distanceMatrix = new int[str1.length() + 1][str2.length() + 1];

    		for (int i = 0; i <= str1.length(); i++) {
    			distanceMatrix[i][0] = i;
    		}

    		for (int j = 0; j <= str2.length(); j++) {
    			distanceMatrix[0][j] = j;
    		}

    		for (int i = 1; i <= str1.length(); i++) {
    			for (int j = 1; j <= str2.length(); j++) {
    				int cost = (str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : 1;
    				distanceMatrix[i][j] = Math.min(
    						Math.min(distanceMatrix[i - 1][j] + 1, distanceMatrix[i][j - 1] + 1),
    						distanceMatrix[i - 1][j - 1] + cost);

    				if (i > 1 && j > 1 && str1.charAt(i - 1) == str2.charAt(j - 2)
    						&& str1.charAt(i - 2) == str2.charAt(j - 1)) {
    					distanceMatrix[i][j] = Math.min(distanceMatrix[i][j], distanceMatrix[i - 2][j - 2] + cost);
    				}
    			}
    		}
    		return distanceMatrix[str1.length()][str2.length()];
    	}
    }
}
