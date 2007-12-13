package eu.etaxonomy.cdm.api.service;

import java.util.List;

import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;

public interface IReferenceService extends IIdentifiableEntityService<ReferenceBase>{
	
	/** find reference by UUID**/
	public abstract ReferenceBase getReferenceByUuid(String uuid);

	/** save a reference and return its UUID**/
	public abstract String saveReference(ReferenceBase reference);

	
}
