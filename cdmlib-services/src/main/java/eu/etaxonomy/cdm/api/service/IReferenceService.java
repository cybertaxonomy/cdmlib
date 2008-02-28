package eu.etaxonomy.cdm.api.service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.Map;

import eu.etaxonomy.cdm.model.reference.ReferenceBase;


public interface IReferenceService extends IIdentifiableEntityService<ReferenceBase>{
	
	/** find reference by UUID**/
	public abstract ReferenceBase getReferenceByUuid(UUID uuid);

	/** save a reference and return its UUID**/
	public abstract UUID saveReference(ReferenceBase reference);

	/** save a collection of  reference and return its UUID**/
	public abstract Map<UUID, ReferenceBase> saveReferenceAll(Collection<ReferenceBase> referenceCollection);
	
	public abstract List<ReferenceBase> getAllReferences(int limit, int start);

	
}
