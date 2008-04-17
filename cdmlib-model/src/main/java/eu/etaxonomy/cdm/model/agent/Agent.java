/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.agent;



import javax.persistence.Entity;
import eu.etaxonomy.cdm.model.common.IdentifyableMediaEntity;

/**
 * The upmost (abstract) class for agents such as persons, teams 
 * or institutions used in nomenclatural or bibliographical references
 * for taxa and for information associated to them.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:57
 */
@Entity
public abstract class Agent extends IdentifyableMediaEntity{
	

	
	
}
