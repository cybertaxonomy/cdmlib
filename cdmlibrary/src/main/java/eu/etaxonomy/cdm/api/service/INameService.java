package eu.etaxonomy.cdm.api.service;

import java.util.List;

import eu.etaxonomy.cdm.model.name.*;

public interface INameService extends IService {

	public abstract TaxonNameBase getTaxonNameById(Integer id);

	public abstract int saveTaxonName(TaxonNameBase taxonName);

	public abstract List<TaxonNameBase> getAllNames();

	public abstract List<TaxonNameBase> getNamesByNameString(String name);

}