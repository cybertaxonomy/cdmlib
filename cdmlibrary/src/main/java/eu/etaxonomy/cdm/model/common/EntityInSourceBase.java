/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import org.apache.log4j.Logger;

/**
 * Other names/labels/titles (abreviated or not) for the same object (person,
 * reference, source, etc.)
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:18:12
 */
public abstract class EntityInSourceBase extends ReferencedEntityBase {
	static Logger logger = Logger.getLogger(EntityInSourceBase.class);

	//The object's ID in the source, where the alternative string comes from
	@Description("The object's ID in the source, where the alternative string comes from")
	private String idInSource;

	public String getIdInSource(){
		return idInSource;
	}

	/**
	 * 
	 * @param idInSource
	 */
	public void setIdInSource(String idInSource){
		;
	}

}