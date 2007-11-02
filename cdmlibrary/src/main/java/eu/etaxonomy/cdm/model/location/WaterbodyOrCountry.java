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
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * +/- current ISO codes. year given with each entry
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:41
 */
@Entity
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
	 * @param continents
	 */
	public void setContinents(ArrayList continents){
		;
	}

	public String getIso2code(){
		return iso2code;
	}

	/**
	 * 
	 * @param iso2code
	 */
	public void setIso2code(String iso2code){
		;
	}

	public TimePeriod getValidPeriod(){
		return validPeriod;
	}

	/**
	 * 
	 * @param validPeriod
	 */
	public void setValidPeriod(TimePeriod validPeriod){
		;
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