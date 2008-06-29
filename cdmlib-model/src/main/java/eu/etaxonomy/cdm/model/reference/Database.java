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
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:19
 */
@Entity
public class Database extends PublicationBase implements Cloneable {
	private static final Logger logger = Logger.getLogger(Database.class);

	public static Database NewInstance(){
		return new Database();
	}
	
	@Override
	public String generateTitle(){
		return "";
	}
	
	
//*********** CLONE **********************************/	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.reference.PublicationBase#clone()
	 */
	public Database clone(){
		Database result = (Database)super.clone();
		//no changes to: -
		return result;
	}

}