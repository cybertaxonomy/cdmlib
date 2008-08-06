/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.strategy.cache.reference.PublicationBaseDefaultCacheStrategy;

import javax.persistence.*;

/**
 * This (abstract) class represents all different kind of published {@link StrictReferenceBase references}
 * which constitute a physical or virtual unit. A reference is a published
 * reference if it can be consulted by the general public.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:46
 */
@Entity
public abstract class PublicationBase extends StrictReferenceBase {
	static Logger logger = Logger.getLogger(PublicationBase.class);
	private String publisher;
	private String placePublished;

	public PublicationBase(){
		super();
		this.cacheStrategy = PublicationBaseDefaultCacheStrategy.NewInstance();
	}

	
	/**
	 * Returns the string representing the name of the publisher of <i>this</i>
	 * publication. A publisher is mostly an institution or a private
	 * company which assumed the global responsibility for the publication
	 * process.
	 * 
	 * @return  the string identifying the publisher of <i>this</i>
	 * 			publication
	 */
	public String getPublisher(){
		return this.publisher;
	}
	/**
	 * @see #getPublisher()
	 */
	public void setPublisher(String publisher){
		this.publisher = publisher;
	}


	/**
	 * Returns the string representing the name of the place (mostly the city)
	 * where <i>this</i> publication has been published.
	 * 
	 * @return  the string identifying the publication place of <i>this</i>
	 * 			publication
	 */
	public String getPlacePublished(){
		return this.placePublished;
	}
	/**
	 * @see #getPlacePublished()
	 */
	public void setPlacePublished(String placePublished){
		this.placePublished = placePublished;
	}
	
	/**
	 * Generates, according to the {@link strategy.cache.reference.IReferenceBaseCacheStrategy cache strategy}
	 * assigned to <i>this</i> reference, a string that identifies <i>this</i>
	 * publication base and returns it. This string may be stored in the
	 * inherited {@link common.IdentifiableEntity#getTitleCache() titleCache} attribute.<BR>
	 * This method overrides the generic and inherited
	 * ReferenceBase#generateTitle() method.
	 *
	 * @return  the string identifying <i>this</i> conference proceedings
	 * @see  	ReferenceBase#generateTitle()
	 * @see  	common.IdentifiableEntity#getTitleCache()
	 * @see  	common.IdentifiableEntity#generateTitle()
	 * @see  	strategy.cache.common.IIdentifiableEntityCacheStrategy#getTitleCache()
	 */
	@Override
	public String generateTitle(){
		return this.cacheStrategy.getTitleCache(this);
	}
	
//*********** CLONE **********************************/	


	/** 
	 * Clones <i>this</i> publication. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i>
	 * publication by modifying only some of the attributes.<BR>
	 * This method overrides the {@link StrictReferenceBase#clone() method} from StrictReferenceBase.
	 * 
	 * @see StrictReferenceBase#clone()
	 * @see media.IdentifyableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone(){
		PublicationBase result = (PublicationBase)super.clone();
		//no changes to: placePublished, publisher
		return result;
	}

}