package eu.etaxonomy.cdm.remote.view.oaipmh.dwc;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.remote.dto.oaipmh.DarwinCoreSimple;
import eu.etaxonomy.cdm.remote.dto.oaipmh.Metadata;

public class GetRecordView extends	eu.etaxonomy.cdm.remote.view.oaipmh.GetRecordView {

	@Override
	public void constructMetadata(Metadata metadata, IdentifiableEntity identifiableEntity) {
		DarwinCoreSimple dc = (DarwinCoreSimple)mapper.map(identifiableEntity, DarwinCoreSimple.class);
		metadata.setAny(dc);
	}

}
