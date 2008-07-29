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

/**
 * This class represents electronic publications available on the world wide web.
 * <P>
 * This class corresponds, according to the TDWG ontology, to the publication type
 * term (from PublicationTypeTerm): "WebPage".
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:07:03
 */
@Entity
public class WebPage extends PublicationBase implements Cloneable {
	public static final Logger logger = Logger.getLogger(WebPage.class);

	/** 
	 * Class constructor: creates a new empty web page instance.
	 */
	protected WebPage(){
		super();
	}
	
	/** 
	 * Creates a new empty web page instance.
	 */
	public static WebPage NewInstance(){
		return new WebPage();
	}
	
	/**
	 * Generates, according to the {@link strategy.cache.reference.IReferenceBaseCacheStrategy cache strategy}
	 * assigned to <i>this</i> web page, a string that identifies <i>this</i>
	 * web page and returns it. This string may be stored in the inherited
	 * {@link common.IdentifiableEntity#getTitleCache() titleCache} attribute.<BR>
	 * This method overrides the generic and inherited
	 * ReferenceBase#generateTitle() method.
	 *
	 * @return  the string identifying <i>this</i> web page
	 * @see  	ReferenceBase#generateTitle()
	 * @see  	common.IdentifiableEntity#getTitleCache()
	 * @see  	common.IdentifiableEntity#generateTitle()
	 * @see  	strategy.cache.common.IIdentifiableEntityCacheStrategy#getTitleCache()
	 */
	@Override
	public String generateTitle(){
		//TODO is this method really needed or is ReferenceBase#generateTitle() enough?
		return "";
	}
//*********** CLONE **********************************/	
	
	/** 
	 * Clones <i>this</i> web page instance. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i>
	 * web page instance by modifying only some of the attributes.<BR>
	 * This method overrides the {@link PublicationBase#clone() method} from PublicationBase.
	 * 
	 * @see PublicationBase#clone()
	 * @see media.IdentifyableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	public WebPage clone(){
		WebPage result = (WebPage)super.clone();
		//no changes to: -
		return result;
	}
}