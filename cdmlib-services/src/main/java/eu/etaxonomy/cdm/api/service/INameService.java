package eu.etaxonomy.cdm.api.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.name.*;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;


public interface INameService extends IIdentifiableEntityService<TaxonNameBase> {

	public abstract TaxonNameBase getTaxonNameByUuid(UUID uuid);

	public abstract UUID saveTaxonName(TaxonNameBase taxonName);

	/** save a collection of  TaxonNames and return its UUID**/
	public abstract Map<UUID, TaxonNameBase> saveTaxonNameAll(Collection<TaxonNameBase> taxonCollection);

	public abstract List<TaxonNameBase> getAllNames(int limit, int start);

	public abstract List<TaxonNameBase> getNamesByName(String name);

	public abstract TermVocabulary getRankEnumeration();
}