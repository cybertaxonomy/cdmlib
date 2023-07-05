/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.common.DoubleResult;
import eu.etaxonomy.cdm.persistence.dto.TaxonNameParts;

@Service
@Transactional(readOnly = true)
public class NameServiceImplementBelen {


// Phonetic changes performed ONLY on the initial characters of each String

	public static String replaceInitialCharacter(String inp) {
	    String input=inp.toLowerCase();
		String output=input;
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

	/**
	 * Deletes common characters at the beginning and end of both parameters.
	 * Returns the space separated concatenation of the remaining strings.
	 *<BR>
	 * Returns empty string if input strings are equal.
	 */
	public static String trimCommonChar(String inputName, String databaseName) {

	    String shortenedInputName="";
	    String shortenedDatabaseName="";
	    String tempInputName;
	    String tempDatabaseName;
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

        // trim common tailing characters between query and document

        int restantInputNameLenght = tempInputName.length();
        int restantDatabaseNameLenght = tempDatabaseName.length();
        int shortestString = Math.min(restantInputNameLenght, restantDatabaseNameLenght);
        int x;
        for (x = 0; x < shortestString; x++) {
            if (tempInputName.charAt(restantInputNameLenght - x - 1) != tempDatabaseName
                    .charAt(restantDatabaseNameLenght - x - 1)) {
                break;
            }

        }
        shortenedInputName = tempInputName.substring(0, restantInputNameLenght - x);
        shortenedDatabaseName = tempDatabaseName.substring(0, restantDatabaseNameLenght - x);

        if (shortenedInputName.equals(shortenedDatabaseName)) {
            return "";
        }else {
            return shortenedInputName +" "+ shortenedDatabaseName;
        }
    }

	public static List <DoubleResult<TaxonNameParts, Integer>> exactResults (List <DoubleResult<TaxonNameParts, Integer>> list){
	    List <DoubleResult<TaxonNameParts, Integer>> exactResults = new ArrayList<>();
	    for (DoubleResult<TaxonNameParts, Integer> best:list) {
            if (best.getSecondResult()==0){
                exactResults.add(best);
            }
        }
	    return exactResults;
	}

	public static List <DoubleResult<TaxonNameParts, Integer>> bestResults (List <DoubleResult<TaxonNameParts, Integer>> list){
	    List <DoubleResult<TaxonNameParts, Integer>> bestResults = new ArrayList<>();
	    for (DoubleResult<TaxonNameParts, Integer> best:list) {
	        if (best.getSecondResult()==1||best.getSecondResult()==2||best.getSecondResult()==3||best.getSecondResult()==4){
	            bestResults.add(best);
	        }
	    }
	    return bestResults;
	}
}
