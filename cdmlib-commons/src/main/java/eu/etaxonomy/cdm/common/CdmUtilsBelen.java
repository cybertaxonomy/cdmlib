package eu.etaxonomy.cdm.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CdmUtilsBelen {

//	Trim white spaces

	public static String deleteEmptySpaces(String inputName) {
		String outputName = inputName.replaceAll("\\s+", " ").trim();
		return outputName;
	}

//	Replace characters with ASCII characters

	public static String replaceSpecialCharacters(String str) {
		String output;
		output = str.replaceAll("[áåâãàä]", "a");
		output = output.replaceAll("[éêèë]", "e");
		output = output.replaceAll("[ôõøòóö]", "o");
		output = output.replaceAll("[ìíîï]", "i");
		output = output.replaceAll("[üûúù]", "u");
		output = output.replaceAll("ñ", "n");
		output = output.replaceAll("ç", "c");
		return output;
	}

//	Change lists to lowercase

	public static List <String> listToLowerCase(List<String> List) {
		List <String> lowerCaseList = new ArrayList<>();
		for (String x : List) {
			lowerCaseList.add(x.toLowerCase());
		}
		return lowerCaseList ;
	}

// Replace characters combinations that sound similar

	public static String soundalike(String inputName) {
		String[][] soundalike = {
				{"ae","e"},
				{"ia","a"},
				{"oe", "i"},
				{"oi", "a"},
				{"sc", "s"}
				};
		for (int i = 0 ; i<soundalike.length;i++) {
			if (inputName.contains(soundalike[i][0])) {
			inputName = inputName.replace(soundalike[i][0],soundalike[i][1]);
			}
		}
		return inputName;
	}

//	Remove duplicated letters

	public static String removeDuplicate(String input) {
		char [] temp= input.toCharArray();
		int lenght=temp.length;

		int index = 0;
		int p;
		for (int i = 0; i < lenght- 1; i++) {
			p = i + 1;
			if (!(temp[i] == temp[p])) {
				temp[index++] = temp[i];
			}
		}
		String output = String.valueOf(Arrays.copyOf(temp, index));
		output= output+ temp[lenght- 1];
		return output;
	}

//	normalize ending ignoring gender issues

	public static String replacerGenderEnding(String input) {

		String firstPart= input.substring(0, input.length() - 2);
		String lastTwoChar = input.substring((input.length() - 2), input.length());
		String[] endingChar = new String[] { "is", "us", "ys", "es", "im", "as", "um", "os" };
		for (String i : endingChar) {
			if (lastTwoChar.contains(i)) {
				lastTwoChar = lastTwoChar.replace(i, "a");
			}
		}
		String output = firstPart + lastTwoChar;
		return output;
	}
}
