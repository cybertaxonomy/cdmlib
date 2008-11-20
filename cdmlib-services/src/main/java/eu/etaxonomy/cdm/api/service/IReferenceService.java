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


public interface IReferenceService<T extends ReferenceBase> extends IIdentifiableEntityService<T> {
//public interface IReferenceService extends IIdentifiableEntityService<ReferenceBase> {
	
	/** Finds reference by UUID **/
	public abstract ReferenceBase getReferenceByUuid(UUID uuid);

	/** Finds references by title **/
	public List<T> getReferencesByTitle(String title);
		
	/** Finds references of a certain kind by title **/
	public List<T> getReferencesByTitle(String title, Class<T> clazz);
	
	/** Gets all references **/
	public abstract List<ReferenceBase> getAllReferences(int limit, int start);

//	public abstract UUID saveReference(ReferenceBase reference);

	/** Saves a reference and return its UUID **/
	public abstract UUID saveReference(T reference);

//	public abstract Map<UUID, ReferenceBase> saveReferenceAll(Collection<ReferenceBase> referenceCollection);
	
	/** Saves a collection of references **/
	public abstract Map<UUID, T> saveReferenceAll(Collection<T> referenceCollection);
	
}
