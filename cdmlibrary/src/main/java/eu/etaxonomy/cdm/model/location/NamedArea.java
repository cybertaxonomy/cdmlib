/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.location;


import etaxonomy.cdm.model.common.EnumeratedTermBase;
import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:15:01
 */
public class NamedArea extends EnumeratedTermBase {
	static Logger logger = Logger.getLogger(NamedArea.class);

	//description of this area
	@Description("description of this area")
	private String description;
	//description of time valid context of this area. e.g. year range
	@Description("description of time valid context of this area. e.g. year range")
	private TimePeriod validPeriod;
	//Binary shape definition for user's defined area as polygon
	@Description("Binary shape definition for user's defined area as polygon")
	private Binary shapeFile;
	private NamedAreaInSource source;
	private ArrayList countriesOrWaterbodies;
	private NamedAreaType type;
	private NamedAreaLevel level;

	public NamedAreaInSource getSource(){
		return source;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setSource(NamedAreaInSource newVal){
		source = newVal;
	}

	public ArrayList getCountriesOrWaterbodies(){
		return countriesOrWaterbodies;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setCountriesOrWaterbodies(ArrayList newVal){
		countriesOrWaterbodies = newVal;
	}

	public NamedAreaType getType(){
		return type;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setType(NamedAreaType newVal){
		type = newVal;
	}

	public NamedAreaLevel getLevel(){
		return level;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setLevel(NamedAreaLevel newVal){
		level = newVal;
	}

	public String getDescription(){
		return description;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setDescription(String newVal){
		description = newVal;
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

	public Binary getShapeFile(){
		return shapeFile;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setShapeFile(Binary newVal){
		shapeFile = newVal;
	}

}