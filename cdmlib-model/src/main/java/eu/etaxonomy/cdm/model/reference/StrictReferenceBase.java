/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;

import eu.etaxonomy.cdm.model.common.TimePeriod;
import org.apache.log4j.Logger;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * A year() method is required to get the year of publication out of the
 * datePublished field
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:54
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StrictReferenceBase", propOrder = {
	"title",
    "datePublished"
})
@XmlRootElement(name = "RelationshipBase")
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class StrictReferenceBase extends ReferenceBase{
	
	static Logger logger = Logger.getLogger(StrictReferenceBase.class);
	
	//Title of the reference
	@XmlElement(name ="Title" )
	private String title;
	
	//The date range assigned to the reference. ISO Date range like. Flexible, year can be left out, etc
	@XmlElement(name ="DatePublished" )
	private TimePeriod datePublished;
	

	public String getTitle(){
		return this.title;
	}
	public void setTitle(String title){
		this.title = title;
	}

	public TimePeriod getDatePublished(){
		return this.datePublished;
	}
	public void setDatePublished(TimePeriod datePublished){
		this.datePublished = datePublished;
	}

	/**
	 * returns a formatted string containing the entire reference citation including
	 * authors
	 */
	@Transient
	public String getCitation(){
		return "";
	}

	/**
	 * transform the datePublished into a string representation for a year
	 */
	@Transient
	public String getYear(){
		if (this.getDatePublished() == null){
			return null;
		}else{
			return getDatePublished().getYear();
		}
	}
	
//******************** CLONE *****************************************/
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.reference.ReferenceBase#clone()
	 */
	public Object clone() {
		try {
			StrictReferenceBase result = (StrictReferenceBase)super.clone();
			result.setDatePublished(datePublished != null? (TimePeriod)datePublished.clone(): null);
			//no change to: title
			return result;
		} catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}


}