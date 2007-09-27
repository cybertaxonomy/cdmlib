package eu.etaxonomy.cdm.api.service;

import java.util.List;

import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;

public interface INameService extends IService {

	/**Factory method */
	public abstract TaxonName getNewTaxonName(Rank rank);
	
	public abstract TaxonName getTaxonNameById(Integer id);

	public abstract int saveTaxonName(TaxonName tn);

	public abstract List getAllNames();

	public abstract List getNamesByNameString(String name);

}