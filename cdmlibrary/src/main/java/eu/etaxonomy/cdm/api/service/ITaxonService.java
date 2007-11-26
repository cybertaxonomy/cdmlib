package eu.etaxonomy.cdm.api.service;

import java.util.List;

import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.IDao;

public interface ITaxonService extends IService<TaxonBase>{
	public abstract TaxonBase getTaxonByUuid(String uuid);

	// save a taxon and return its UUID
	public abstract String saveTaxon(TaxonBase taxon);

	public abstract List<Taxon> getRootTaxa(ReferenceBase sec);

	public abstract List<TaxonBase> searchTaxaByName(String name, ReferenceBase sec);
}
