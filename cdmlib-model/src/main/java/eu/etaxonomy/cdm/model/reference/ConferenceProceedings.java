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
 * This class represents conference proceedings. A conference proceedings is a
 * document containing contributions made during a conference which takes place
 * only once.
 * <P>
 * This class corresponds, according to the TDWG ontology, to the publication type
 * term (from PublicationTypeTerm): "ConferenceProceedings".
 *   
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:17
 */
@Entity
public class ConferenceProceedings extends StrictReferenceBase {
	private static final Logger logger = Logger.getLogger(ConferenceProceedings.class);



	/** 
	 * Creates a new empty conference proceedings instance.
	 */
	public static ConferenceProceedings NewInstance(){
		ConferenceProceedings result = new ConferenceProceedings();
		return result;
	}
	
	/**
	 * Generates, according to the {@link strategy.cache.reference.IReferenceBaseCacheStrategy cache strategy}
	 * assigned to <i>this</i> reference, a string that identifies <i>this</i>
	 * conference proceedings and returns it. This string may be stored in the
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
		//TODO is this method really needed or is ReferenceBase#generateTitle() enough?
		return "";
	}

}