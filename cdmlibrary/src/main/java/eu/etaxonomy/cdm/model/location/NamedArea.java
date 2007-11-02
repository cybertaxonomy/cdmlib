/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.location;


import eu.etaxonomy.cdm.model.common.EnumeratedTermBase;
import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:18:24
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
	 * @param source
	 */
	public void setSource(NamedAreaInSource source){
		;
	}

	public ArrayList getCountriesOrWaterbodies(){
		return countriesOrWaterbodies;
	}

	/**
	 * 
	 * @param countriesOrWaterbodies
	 */
	public void setCountriesOrWaterbodies(ArrayList countriesOrWaterbodies){
		;
	}

	public NamedAreaType getType(){
		return type;
	}

	/**
	 * 
	 * @param type
	 */
	public void setType(NamedAreaType type){
		;
	}

	public NamedAreaLevel getLevel(){
		return level;
	}

	/**
	 * 
	 * @param level
	 */
	public void setLevel(NamedAreaLevel level){
		;
	}

	public String getDescription(){
		return description;
	}

	/**
	 * 
	 * @param description
	 */
	public void setDescription(String description){
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

	public Binary getShapeFile(){
		return shapeFile;
	}

	/**
	 * 
	 * @param shapeFile
	 */
	public void setShapeFile(Binary shapeFile){
		;
	}

}