package eu.etaxonomy.cdm.remote.view.oaipmh.dwc;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.remote.dto.dwc.SimpleDarwinRecord;
import eu.etaxonomy.cdm.remote.dto.oaipmh.Metadata;

public class GetRecordView extends	eu.etaxonomy.cdm.remote.view.oaipmh.ListRecordsView {
	

	@Override
	public void constructMetadata(Metadata metadata, IdentifiableEntity identifiableEntity) {
		SimpleDarwinRecord dc = (SimpleDarwinRecord)mapper.map(identifiableEntity, SimpleDarwinRecord.class);
		if(identifiableEntity instanceof Taxon){
			NonViralName<?> name = HibernateProxyHelper.deproxy(((Taxon)identifiableEntity).getName(), NonViralName.class);
			mapper.map(name, dc);
		}
		dc.setBasisOfRecord("Taxon");
		metadata.setAny(dc);
	}

}
