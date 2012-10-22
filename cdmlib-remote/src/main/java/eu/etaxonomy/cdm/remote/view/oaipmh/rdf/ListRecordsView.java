package eu.etaxonomy.cdm.remote.view.oaipmh.rdf;

import java.util.HashMap;
import java.util.Map;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.remote.dto.oaipmh.Metadata;
import eu.etaxonomy.cdm.remote.dto.tdwg.BaseThing;
import eu.etaxonomy.cdm.remote.dto.tdwg.voc.SpeciesProfileModel;
import eu.etaxonomy.cdm.remote.dto.tdwg.voc.TaxonConcept;

public class ListRecordsView extends
		eu.etaxonomy.cdm.remote.view.oaipmh.ListRecordsView {
	
	private Map<Class<? extends CdmBase>,Class<? extends BaseThing>> classMap = new HashMap<Class<? extends CdmBase>,Class<? extends BaseThing>>();

	public ListRecordsView() {
		classMap.put(Taxon.class, TaxonConcept.class);
		classMap.put(Synonym.class, TaxonConcept.class);
		classMap.put(TaxonDescription.class, SpeciesProfileModel.class);
	}
	
	@Override
	public void constructMetadata(Metadata metadata, IdentifiableEntity identifiableEntity) {
		Class clazz = classMap.get(identifiableEntity.getClass());
        if(clazz != null) {
          BaseThing baseThing = (BaseThing)mapper.map(identifiableEntity, clazz);
          metadata.setAny(baseThing);
        }
	}

}
