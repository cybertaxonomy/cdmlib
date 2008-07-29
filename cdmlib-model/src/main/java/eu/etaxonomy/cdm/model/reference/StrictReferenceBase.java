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
 * This (abstract) class represents all different kind of references regardless
 * of their peculiarities. In order to take in account their peculiar
 * relationships and their use for different purposes each kind of reference is
 * represented by a subclass and not by an attribute (the values of which would
 * have been defined terms of a particular vocabulary).
 * <P>
 * This class corresponds to: <ul>
 * <li> PublicationCitation according to the TDWG ontology
 * <li> Publication according to the TCS
 * <li> Reference according to the ABCD schema
 * </ul>
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:54
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StrictReferenceBase", propOrder = {
	"title",
    "datePublished"
})
@XmlRootElement(name = "StrictReferenceBase")
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
	

	/**
	 * Returns a string representing the title of <i>this</i> reference.
	 * 
	 * @return  the title string of <i>this</i> reference
	 * @see 	#getCitation()
	 */
	public String getTitle(){
		return this.title;
	}
	/**
	 * @see 	#getTitle()
	 */
	public void setTitle(String title){
		this.title = title;
	}

	/**
	 * Returns the date (mostly only the year) of publication / creation of
	 * <i>this</i> reference.
	 */
	public TimePeriod getDatePublished(){
		return this.datePublished;
	}
	/**
	 * @see 	#getDatePublished()
	 */
	public void setDatePublished(TimePeriod datePublished){
		this.datePublished = datePublished;
	}

	/**
	 * Returns a formatted string containing the entire reference citation,
	 * including authors, corresponding to <i>this</i> reference.
	 * 
	 * @see  #getTitle()
	 */
	@Transient
	// TODO implement 
	public String getCitation(){
		return "";
	}

	/**
	 * Returns a string representation for the year of publication / creation
	 * of <i>this</i> reference. The string is obtained by transformation of
	 * the {@link #getDatePublished() datePublished} attribute.
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
	/** 
	 * Clones <i>this</i> reference. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> reference by
	 * modifying only some of the attributes.
	 * 
	 * @see media.IdentifyableMediaEntity#clone()
	 * @see java.lang.Object#clone()
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