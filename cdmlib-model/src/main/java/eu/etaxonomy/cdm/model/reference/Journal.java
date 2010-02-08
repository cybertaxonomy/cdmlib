/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;


import javax.persistence.Entity;
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
import org.springframework.beans.factory.annotation.Configurable;

import eu.etaxonomy.cdm.strategy.cache.reference.IReferenceBaseCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.JournalDefaultCacheStrategy;

/**
 * This class represents journals. A journal is a periodical {@link PublicationBase publication}
 * containing several {@link Article articles}.
 * <P>
 * This class corresponds, according to the TDWG ontology, to the publication type
 * term (from PublicationTypeTerm): "Journal".
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:31
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Journal", propOrder = {
//    "issn"
})
@XmlRootElement(name = "Journal")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.reference.ReferenceBase")
@Audited
@Configurable
@Deprecated
public class Journal extends PublicationBase<IReferenceBaseCacheStrategy<Journal>> implements Cloneable {
	static Logger logger = Logger.getLogger(Journal.class);
	
//	@XmlElement(name = "ISSN")
//	@Field(index=Index.TOKENIZED)
//	private String issn;

	
	/** 
	 * Class constructor: creates a new empty journal instance
	 * only containing the {@link eu.etaxonomy.cdm.strategy.cache.reference.JournalDefaultCacheStrategy default cache strategy}.
	 * 
	 * @see eu.etaxonomy.cdm.strategy.cache.reference.JournalDefaultCacheStrategy
	 */
	protected Journal(){
		super();
		this.type = ReferenceType.Journal;
		cacheStrategy = JournalDefaultCacheStrategy.NewInstance();
	}
	

	/** 
	 * Creates a new empty journal instance
	 * only containing the {@link eu.etaxonomy.cdm.strategy.cache.reference.JournalDefaultCacheStrategy default cache strategy}.
	 * 
	 * @see #Journal()
	 * @see eu.etaxonomy.cdm.strategy.cache.reference.JournalDefaultCacheStrategy
	 */
	public static Journal NewInstance(){
		Journal result = new Journal();
		return result;
	}
	
	/**
	 * Returns the string representing the ISSN (International Standard Serial
	 * Number, a unique eight-digit number used to identify a periodical
	 * publication) of <i>this</i> journal.
	 * 
	 * @return  the string representing the ISSN
	 */
	public String getIssn(){
		return this.issn;
	}

	/**
	 * @see #getIssn()
	 */
	public void setIssn(String issn){
		this.issn = issn;
	}

	
	
	
	/** 
	 * Clones <i>this</i> journal instance. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i>
	 * journal instance by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link StrictReferenceBase StrictReferenceBase}.
	 * 
	 * @see StrictReferenceBase#clone()
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Journal clone(){
		return (Journal)super.clone();
	}

}