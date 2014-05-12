package eu.etaxonomy.cdm.remote.view.oaipmh.dc;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.remote.dto.oaipmh.Metadata;
import eu.etaxonomy.cdm.remote.dto.oaipmh.OaiDc;

public class GetRecordView extends
		eu.etaxonomy.cdm.remote.view.oaipmh.GetRecordView {

	@Override
	public void constructMetadata(Metadata metadata, IdentifiableEntity identifiableEntity) {
		OaiDc dc = (OaiDc)mapper.map(identifiableEntity, OaiDc.class);
		metadata.setAny(dc);
	}

}
