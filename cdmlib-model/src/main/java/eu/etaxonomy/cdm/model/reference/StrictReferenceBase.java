/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.strategy.cache.reference.IReferenceBaseCacheStrategy;
import eu.etaxonomy.cdm.strategy.match.IMatchable;
import eu.etaxonomy.cdm.strategy.match.Match;
import eu.etaxonomy.cdm.strategy.match.MatchMode;
import eu.etaxonomy.cdm.strategy.merge.IMergable;

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
@Indexed(index = "eu.etaxonomy.cdm.model.reference.ReferenceBase")
@Audited
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class StrictReferenceBase<S extends IReferenceBaseCacheStrategy> extends ReferenceBase<S> implements IMergable, IMatchable {
	private static final long serialVersionUID = 1951644614905249231L;
	private static final Logger logger = Logger.getLogger(StrictReferenceBase.class);
	
	//Title of the reference
	@XmlElement(name ="Title" )
	@Column(length=4096, name="title")
	@Lob
	@Field(index=Index.TOKENIZED)
	@Match(MatchMode.EQUAL_REQUIRED)
	private String title;
	
	//The date range assigned to the reference. ISO Date range like. Flexible, year can be left out, etc
	@XmlElement(name ="DatePublished" )
	@Embedded
	@IndexedEmbedded
	private TimePeriod datePublished = TimePeriod.NewInstance();
	
	protected StrictReferenceBase(){
		super();
	}
	
	
	/**
	 * Returns a string representing the title of <i>this</i> reference. If a
	 * reference has different titles (for instance abbreviated and not
	 * abbreviated) then for each title a new instance must be created.
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
	@Override
	@Transient
	// TODO implement 
	public String getCitation(){
		logger.warn("getCitation not yet implemented");
		return "";
	}

	/**
	 * Returns a string representation for the year of publication / creation
	 * of <i>this</i> reference. The string is obtained by transformation of
	 * the {@link #getDatePublished() datePublished} attribute.
	 */
	@Transient
	@Override
	public String getYear(){
		if (this.getDatePublished() != null && this.getDatePublished().getStart() != null){
			return getDatePublished().getYear();
		}else{
			return null;
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
	 * @see ReferenceBase#clone()
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
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