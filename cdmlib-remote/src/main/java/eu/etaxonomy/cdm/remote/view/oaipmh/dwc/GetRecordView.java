package eu.etaxonomy.cdm.remote.view.oaipmh.dwc;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.remote.dto.dwc.SimpleDarwinRecord;
import eu.etaxonomy.cdm.remote.dto.oaipmh.Metadata;

public class GetRecordView extends	eu.etaxonomy.cdm.remote.view.oaipmh.ListRecordsView {

	@Override
	public void constructMetadata(Metadata metadata, IdentifiableEntity identifiableEntity) {
		SimpleDarwinRecord dc = (SimpleDarwinRecord)mapper.map(identifiableEntity, SimpleDarwinRecord.class);
		metadata.setAny(dc);
	}

}
