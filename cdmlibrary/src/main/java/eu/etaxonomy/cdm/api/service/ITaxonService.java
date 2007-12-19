package eu.etaxonomy.cdm.api.service;

import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;


public interface ITaxonService extends IIdentifiableEntityService<TaxonBase>{
	
	/** */
	public abstract TaxonBase getTaxonByUuid(UUID uuid);

	/** save a taxon and return its UUID**/
	public abstract UUID saveTaxon(TaxonBase taxon);

	/** */
	public abstract List<Taxon> getRootTaxa(ReferenceBase sec);
	
	/** */
	public abstract List<TaxonBase> searchTaxaByName(String name, ReferenceBase sec);
	
}
