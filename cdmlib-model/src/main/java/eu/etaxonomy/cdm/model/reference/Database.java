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

import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;

/**
 * This class represents a database used as an information source. A database is
 * a structured collection of records or data.
 * <P>
 * This class corresponds, according to the TDWG ontology, partially to the
 * publication type term (from PublicationTypeTerm): "ComputerProgram".
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:19
 */
@Entity
public class Database extends PublicationBase implements Cloneable {
	private static final Logger logger = Logger.getLogger(Database.class);

	/** 
	 * Creates a new empty database instance.
	 */
	public static Database NewInstance(){
		return new Database();
	}
	
	/**
	 * Generates, according to the {@link strategy.cache.reference.IReferenceBaseCacheStrategy cache strategy}
	 * assigned to <i>this</i> database, a string that identifies <i>this</i>
	 * database and returns it. This string may be stored in the inherited
	 * {@link common.IdentifiableEntity#getTitleCache() titleCache} attribute.<BR>
	 * This method overrides the generic and inherited
	 * ReferenceBase#generateTitle() method.
	 *
	 * @return  the string identifying <i>this</i> database
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
	 * Clones <i>this</i> database instance. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i>
	 * database instance by modifying only some of the attributes.<BR>
	 * This method overrides the {@link PublicationBase#clone() method} from PublicationBase.
	 * 
	 * @see PublicationBase#clone()
	 * @see media.IdentifyableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	public Database clone(){
		Database result = (Database)super.clone();
		//no changes to: -
		return result;
	}

}