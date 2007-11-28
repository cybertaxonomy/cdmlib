/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.location;


import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.MediaInstance;
import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.*;

import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:36
 */
@Entity
public class NamedArea extends OrderedTermBase {
	static Logger logger = Logger.getLogger(NamedArea.class);
	//description of time valid context of this area. e.g. year range
	private TimePeriod validPeriod;
	//Binary shape definition for user's defined area as polygon
	private MediaInstance shapeFile;
	private Set<WaterbodyOrCountry> waterbodiesOrCountries = new HashSet();
	private NamedAreaType type;
	private NamedAreaLevel level;

	public NamedArea() {
		super();
		// TODO Auto-generated constructor stub
	}
	public NamedArea(String term, String label) {
		super(term, label);
		// TODO Auto-generated constructor stub
	}
	
	
	@ManyToOne
	public NamedAreaType getType(){
		return this.type;
	}
	public void setType(NamedAreaType type){
		this.type = type;
	}

	@ManyToOne
	public NamedAreaLevel getLevel(){
		return this.level;
	}
	public void setLevel(NamedAreaLevel level){
		this.level = level;
	}

	public TimePeriod getValidPeriod(){
		return this.validPeriod;
	}
	public void setValidPeriod(TimePeriod validPeriod){
		this.validPeriod = validPeriod;
	}

	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public MediaInstance getShapeFile(){
		return this.shapeFile;
	}
	public void setShapeFile(MediaInstance shapeFile){
		this.shapeFile = shapeFile;
	}


	@ManyToMany
	public Set<WaterbodyOrCountry> getWaterbodiesOrCountries() {
		return waterbodiesOrCountries;
	}
	protected void setWaterbodiesOrCountries(
			Set<WaterbodyOrCountry> waterbodiesOrCountries) {
		this.waterbodiesOrCountries = waterbodiesOrCountries;
	}
	public void addWaterbodyOrCountry(
			WaterbodyOrCountry waterbodyOrCountry) {
		this.waterbodiesOrCountries.add(waterbodyOrCountry);
	}
	public void removeWaterbodyOrCountry(
			WaterbodyOrCountry waterbodyOrCountry) {
		this.waterbodiesOrCountries.remove(waterbodyOrCountry);
	}

}