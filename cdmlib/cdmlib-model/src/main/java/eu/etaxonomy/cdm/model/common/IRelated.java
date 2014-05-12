/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.model.common;

import java.util.UUID;

/**
 * PLEASE LOOK AT NameRelationship and TaxonRelationship
 * @author m.doering
 *
 * @param <T>
 */
public interface IRelated<T extends RelationshipBase> {
	/**
	 * @param relation
	 */
	public void addRelationship(T relation);
	
	/**
	 * 
	 */
	public UUID getUuid();
	
}
