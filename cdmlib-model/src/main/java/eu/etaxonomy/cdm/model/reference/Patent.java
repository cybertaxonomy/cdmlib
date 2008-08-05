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
 * This class represents patents. A patent is a document containing the set of
 * exclusive rights granted by a state to an inventor or his assignee for a
 * fixed period of time in exchange for a disclosure of an invention.
 * <P>
 * This class corresponds, according to the TDWG ontology, to the publication type
 * term (from PublicationTypeTerm): "Patent".
 *  
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:42
 */
@Entity
public class Patent extends StrictReferenceBase implements Cloneable {
	private static final Logger logger = Logger.getLogger(Patent.class);
	
	/** 
	 * Creates a new empty patent instance
	 */
	public static Patent NewInstance(){
		Patent result = new Patent();
		return result;
	}
	
	/**
	 * Generates, according to the {@link strategy.cache.reference.IReferenceBaseCacheStrategy cache strategy}
	 * assigned to <i>this</i> patent, a string that identifies <i>this</i>
	 * patent and returns it. This string may be stored in the inherited
	 * {@link common.IdentifiableEntity#getTitleCache() titleCache} attribute.<BR>
	 * This method overrides the generic and inherited
	 * ReferenceBase#generateTitle() method.
	 *
	 * @return  the string identifying <i>this</i> patent
	 * @see  	ReferenceBase#generateTitle()
	 * @see  	common.IdentifiableEntity#getTitleCache()
	 * @see  	common.IdentifiableEntity#generateTitle()
	 * @see  	strategy.cache.common.IIdentifiableEntityCacheStrategy#getTitleCache()
	 */
	@Override
	public String generateTitle(){
		//TODO is this method really needed or is ReferenceBase#generateTitle() enough?
		logger.warn("generate Title not yet implemented");
		return "";
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.reference.PublicationBase#clone()
	 */
	public Patent clone(){
		Patent result = (Patent)super.clone();
		//no changes to: -
		return result;
	}

}