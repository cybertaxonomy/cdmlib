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
 * @created 08-Nov-2007 13:06:45
 */
@Entity
public class Proceedings extends PrintedUnitBase implements Cloneable {
	private static final Logger logger = Logger.getLogger(Proceedings.class);
	
	//The conference sponsor
	private String organization;
	
	
	public static Proceedings NewInstance(){
		Proceedings result = new Proceedings();
		return result;
	}
	
	public static Proceedings NewInstance(String organization){
		Proceedings result = NewInstance();
		result.setOrganization(organization);
		return result;
	}
	


	public String getOrganization(){
		return this.organization;
	}
	public void setOrganization(String organization){
		this.organization = organization;
	}

	@Override
	public String generateTitle(){
		logger.warn("generateTitle not yet fully implemented");
		return this.getTitle();
	}
	
	
	//*********** CLONE **********************************/	
			
		public Proceedings clone(){
			Proceedings result = (Proceedings)super.clone();
			//no changes to: organization
			return result;
		}

}