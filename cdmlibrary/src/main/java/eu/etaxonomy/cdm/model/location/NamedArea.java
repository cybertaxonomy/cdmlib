/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.location;


import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.MediaInstance;
import eu.etaxonomy.cdm.model.common.EnumeratedTermBase;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:36
 */
@Entity
public class NamedArea extends EnumeratedTermBase {
	static Logger logger = Logger.getLogger(NamedArea.class);
	//description of this area
	private String description;
	//description of time valid context of this area. e.g. year range
	private TimePeriod validPeriod;
	//Binary shape definition for user's defined area as polygon
	private MediaInstance shapeFile;
	private NamedAreaInSource source;
	private Set<WaterbodyOrCountry> countriesOrWaterbodies;
	private NamedAreaType type;
	private NamedAreaLevel level;

	public NamedAreaInSource getSource(){
		return this.source;
	}

	/**
	 * 
	 * @param source    source
	 */
	public void setSource(NamedAreaInSource source){
		this.source = source;
	}


	public NamedAreaType getType(){
		return this.type;
	}

	/**
	 * 
	 * @param type    type
	 */
	public void setType(NamedAreaType type){
		this.type = type;
	}

	public NamedAreaLevel getLevel(){
		return this.level;
	}

	/**
	 * 
	 * @param level    level
	 */
	public void setLevel(NamedAreaLevel level){
		this.level = level;
	}

	public String getDescription(){
		return this.description;
	}

	/**
	 * 
	 * @param description    description
	 */
	public void setDescription(String description){
		this.description = description;
	}

	public TimePeriod getValidPeriod(){
		return this.validPeriod;
	}

	/**
	 * 
	 * @param validPeriod    validPeriod
	 */
	public void setValidPeriod(TimePeriod validPeriod){
		this.validPeriod = validPeriod;
	}

	public MediaInstance getShapeFile(){
		return this.shapeFile;
	}

	/**
	 * 
	 * @param shapeFile    shapeFile
	 */
	public void setShapeFile(MediaInstance shapeFile){
		this.shapeFile = shapeFile;
	}

	private void setCountriesOrWaterbodies(
			Set<WaterbodyOrCountry> countriesOrWaterbodies) {
		this.countriesOrWaterbodies = countriesOrWaterbodies;
	}
	public void addCountryOrWaterbody(
			WaterbodyOrCountry countryOrWaterbody) {
		this.countriesOrWaterbodies.add(countryOrWaterbody);
	}
	public void removeCountryOrWaterbody(
			WaterbodyOrCountry countryOrWaterbody) {
		this.countriesOrWaterbodies.remove(countryOrWaterbody);
	}

}