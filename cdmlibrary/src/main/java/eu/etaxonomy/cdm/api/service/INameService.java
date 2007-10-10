package eu.etaxonomy.cdm.api.service;

import java.util.List;

import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;

public interface INameService extends IService {

	/**Returns a new TaxonName object */
	public abstract TaxonName createTaxonName(Rank rank);
	
	public abstract TaxonName getTaxonNameById(Integer id);

	/**
	 * Saves a TaxonName. If the TaxonName is already persisted,
	 * it is updated, otherwise it is saved as a new object.
	 * @param taxonName
	 * @return the TaxonNames Id
	 */
	public abstract int saveTaxonName(TaxonName taxonName);

	public abstract List getAllNames();

	public abstract List getNamesByNameString(String name);

}