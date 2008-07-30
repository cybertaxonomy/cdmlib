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

import org.apache.log4j.Logger;

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
@Entity
public class Journal extends PublicationBase implements Cloneable {
	static Logger logger = Logger.getLogger(Journal.class);
	private String issn;

	
	/** 
	 * Class constructor: creates a new empty journal instance
	 * only containing the {@link strategy.cache.reference.JournalDefaultCacheStrategy default cache strategy}.
	 * 
	 * @see strategy.cache.reference.JournalDefaultCacheStrategy
	 */
	protected Journal(){
		super();
		cacheStrategy = JournalDefaultCacheStrategy.NewInstance();
	}
	

	/** 
	 * Creates a new empty journal instance
	 * only containing the {@link strategy.cache.reference.JournalDefaultCacheStrategy default cache strategy}.
	 * 
	 * @see #Journal()
	 * @see strategy.cache.reference.JournalDefaultCacheStrategy
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
	 * @see #getSeries()
	 */
	public void setIssn(String issn){
		this.issn = issn;
	}

	/**
	 * Generates, according to the {@link strategy.cache.reference.JournalDefaultCacheStrategy default cache strategy}
	 * assigned to <i>this</i> journal, a string that identifies <i>this</i>
	 * journal and returns it. This string may be stored in the inherited
	 * {@link common.IdentifiableEntity#getTitleCache() titleCache} attribute.<BR>
	 * This method overrides the generic and inherited
	 * ReferenceBase#generateTitle() method.
	 *
	 * @return  the string identifying <i>this</i> journal
	 * @see  	ReferenceBase#generateTitle()
	 * @see  	NomenclaturalReferenceHelper#generateTitle()
	 * @see  	common.IdentifiableEntity#getTitleCache()
	 * @see  	common.IdentifiableEntity#generateTitle()
	 */
	@Override
	public String generateTitle(){
		//FIXME implement
		//only dummy
		return this.getTitle();
	}
	
	
//*********** CLONE **********************************/	
	
	/** 
	 * Clones <i>this</i> journal instance. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i>
	 * journal instance by modifying only some of the attributes.<BR>
	 * This method overrides the {@link PublicationBase#clone() method} from PublicationBase.
	 * 
	 * @see PublicationBase#clone()
	 * @see media.IdentifyableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	public Journal clone(){
		return (Journal)super.clone();
	}

}