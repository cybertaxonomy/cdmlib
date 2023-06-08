package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.List;

public class NameServiceImplementBelen {
	private String tempInputName;
	private String tempDatabaseName;
	private String shortenedInputName;
	private String shortenedDatabaseName;

// Phonetic changes performed ONLY on the initial characters of each String

	public String replaceInitialCharacter(String inp) {
	    String input=inp.toLowerCase();
		String output="";
		String[][] phoneticChange = {
				{"ae","e"},{"cn","n"},{"ct","t"},{"cz","c"},
				{"dj","d"},{"ea","e"},{"eu","u"},{"gn","n"},
				{"kn","n"},{"mc","mac"},{"mn","n"},{"oe","e"},
				{"qu","q"},{"ph","f"},{"ps","s"},{"pt","t"},
				{"ts","s"},{"wr","r"},{"x","z"}
				};
		for (int i = 0 ; i< phoneticChange.length; i++) {
			if (input.startsWith(phoneticChange[i][0])){
					output= input.replaceFirst(phoneticChange[i][0], phoneticChange[i][1]);
					break;
			}
		}
		return output;
	}


// trim common characters between query and document

	public List <String> trimCommonChar(String inputName, String databaseName) {

        // trim common leading characters of query and document

        int inputNameLength = inputName.length();
        int databaseNameLength = databaseName.length();
        int largestString = Math.max(inputNameLength, databaseNameLength);
        int i;

        for (i = 0; i < largestString; i++) {
            if (i >= inputNameLength || i >= databaseNameLength || inputName.charAt(i) != databaseName.charAt(i)) {
                // Stop iterating when the characters at the current position are not equal.
                break;
            }
        }

        // Create temp names with common leading characters removed.
        tempInputName = inputName.substring(i);
        tempDatabaseName = databaseName.substring(i);

        List <String> list= new ArrayList<>();

        // trim common tailing characters between query and document

        int restantInputNameLenght = tempInputName.length();
        int restantDatabaseNameLenght = tempDatabaseName.length();
        int shortestString = Math.min(restantInputNameLenght, restantDatabaseNameLenght);

        for (int x = 0; x < shortestString; x++) {
            if (tempInputName.charAt(restantInputNameLenght - x - 1) != tempDatabaseName
                    .charAt(restantDatabaseNameLenght - x - 1)) {
                break;
            }
            shortenedInputName = tempInputName.substring(0, restantInputNameLenght - x - 1);
            shortenedDatabaseName = tempDatabaseName.substring(0, restantDatabaseNameLenght - x - 1);

        }
        list.add(shortenedInputName +" "+ shortenedDatabaseName);
        return list;
    }
}
