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
 * This class represents personal communications. A personal communication is a
 * non published document originally written for information exchange between
 * private persons. 
 * <P>
 * This class corresponds to: <ul>
 * <li> the term "Communication" from PublicationTypeTerm according to the TDWG ontology
 * </ul>
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:43
 */
@Entity
public class PersonalCommunication extends StrictReferenceBase implements Cloneable {
	private static final Logger logger = Logger.getLogger(PersonalCommunication.class);


	/**
	 * Creates a new empty personal communication instance.
	 */
	public static PersonalCommunication NewInstance(){
		PersonalCommunication result = new PersonalCommunication();
		return result;
	}
	

	/**
	 * Generates, according to the {@link strategy.cache.reference.IReferenceBaseCacheStrategy cache strategy}
	 * assigned to <i>this</i> reference, a string that identifies <i>this</i>
	 * personal communication and returns it. This string may be stored in the
	 * inherited {@link common.IdentifiableEntity#getTitleCache() titleCache} attribute.<BR>
	 * This method overrides the generic and inherited
	 * ReferenceBase#generateTitle() method.
	 *
	 * @return  the string identifying <i>this</i> personal communication
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

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.reference.PublicationBase#clone()
	 */
	public PersonalCommunication clone(){
		PersonalCommunication result = (PersonalCommunication)super.clone();
		//no changes to: -
		return result;
	}
}