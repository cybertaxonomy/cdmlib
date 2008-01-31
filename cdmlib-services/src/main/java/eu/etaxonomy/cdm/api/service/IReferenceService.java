package eu.etaxonomy.cdm.api.service;

import java.util.UUID;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;


public interface IReferenceService extends IIdentifiableEntityService<ReferenceBase>{
	
	/** find reference by UUID**/
	public abstract ReferenceBase getReferenceByUuid(UUID uuid);

	/** save a reference and return its UUID**/
	public abstract UUID saveReference(ReferenceBase reference);

	
}
