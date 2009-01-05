/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.Map;

import eu.etaxonomy.cdm.model.reference.ReferenceBase;


public interface IReferenceService extends IIdentifiableEntityService<ReferenceBase> {
	
	/**
	 * FIXME candidate for harmonization?
	 *  
	 * Finds reference by UUID 
	 */
	public abstract ReferenceBase getReferenceByUuid(UUID uuid);

	/**
	 * FIXME candidate for harmonization?
	 * Finds references by title 
	 */
	public List<ReferenceBase> getReferencesByTitle(String title);
		
	/**
	 * FIXME candidate for harmonization?
	 * Finds references of a certain kind by title 
	 */
	public List<ReferenceBase> getReferencesByTitle(String title, Class<ReferenceBase> clazz);
	
	/**
	 * FIXME candidate for harmonization? 
	 * Gets all references 
	 */
	public abstract List<ReferenceBase> getAllReferences(int limit, int start);

//	public abstract UUID saveReference(ReferenceBase reference);

	/** 
	 * FIXME candidate for harmonization?
	 * Saves a reference and return its UUID 
	 */
	public abstract UUID saveReference(ReferenceBase reference);

//	public abstract Map<UUID, ReferenceBase> saveReferenceAll(Collection<ReferenceBase> referenceCollection);
	
	/**
	 * FIXME candidate for harmonization? 
	 * Saves a collection of references
	 */
	public abstract Map<UUID, ReferenceBase> saveReferenceAll(Collection<ReferenceBase> referenceCollection);
	
}
