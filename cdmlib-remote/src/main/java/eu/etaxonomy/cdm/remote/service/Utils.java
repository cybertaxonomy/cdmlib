package eu.etaxonomy.cdm.remote.service;

import org.springframework.stereotype.Component;


public class Utils {
	
	/**
	 * Interprets a string as boolean value with true/TRUE/True or any integer greater 0 being true
	 * @param boolString
	 * @return
	 */
	public static boolean isTrue(String boolString){
		boolean result = Boolean.parseBoolean(boolString);
		if (!result && Integer.valueOf(boolString)>0){
			result = true;
		}
		return result;
	}
}
