package eu.etaxonomy.cdm.persistence.hibernate.replace;

import java.util.Set;

import eu.etaxonomy.cdm.model.common.CdmBase;

public interface ReferringObjectMetadataFactory {
	
	 Set<ReferringObjectMetadata> get(Class<? extends CdmBase> type);

}
