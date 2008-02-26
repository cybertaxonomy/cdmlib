package eu.etaxonomy.cdm.remote.service;

import org.springframework.stereotype.Component;


public class Utils {
	
	/**
	 * Interprets a string as boolean value with true/TRUE/True or any integer greater 0 being true
	 * @param boolString
	 * @return
	 */
	public static Boolean isTrue(String boolString){
		// empty string returns null
		if (boolString==null || boolString.trim().length()==0){
			return null;
		}
		boolean result = Boolean.parseBoolean(boolString);
		try{
			if (!result && Integer.valueOf(boolString)>0){
				result = true;
			}
		}catch(Exception e){
			result = false;
		}
		return result;
	}
}
