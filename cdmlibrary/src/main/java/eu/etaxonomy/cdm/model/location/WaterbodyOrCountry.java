/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.location;


import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import org.apache.log4j.Logger;

/**
 * +/- current ISO codes. year given with each entry
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:43:54
 */
public class WaterbodyOrCountry extends DefinedTermBase {
	static Logger logger = Logger.getLogger(WaterbodyOrCountry.class);

	@Description("")
	private String iso2code;
	@Description("")
	private TimePeriod validPeriod;
	private ArrayList continents;

	public ArrayList getContinents(){
		return continents;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setContinents(ArrayList newVal){
		continents = newVal;
	}

	public String getIso2code(){
		return iso2code;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setIso2code(String newVal){
		iso2code = newVal;
	}

	public TimePeriod getValidPeriod(){
		return validPeriod;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setValidPeriod(TimePeriod newVal){
		validPeriod = newVal;
	}

	public static final WaterbodyOrCountry ARCTIC_OCEAN(){
		return null;
	}

	public static final WaterbodyOrCountry ATLANTIC_OCEAN(){
		return null;
	}

	public static final WaterbodyOrCountry PACIFIC_OCEAN(){
		return null;
	}

	public static final WaterbodyOrCountry INDIAN_OCEAN(){
		return null;
	}

	public static final WaterbodyOrCountry SOUTHERN_OCEAN(){
		return null;
	}

	public static final WaterbodyOrCountry MEDITERRANEAN_SEA(){
		return null;
	}

	public static final WaterbodyOrCountry BLACK_SEA(){
		return null;
	}

	public static final WaterbodyOrCountry CASPIAN_SEA(){
		return null;
	}

	public static final WaterbodyOrCountry RED_SEA(){
		return null;
	}

	public static final WaterbodyOrCountry PERSIAN_GULF(){
		return null;
	}

}