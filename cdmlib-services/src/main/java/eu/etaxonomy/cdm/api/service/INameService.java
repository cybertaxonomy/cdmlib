package eu.etaxonomy.cdm.api.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.name.*;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;


public interface INameService extends IIdentifiableEntityService<TaxonNameBase> {

	/**
	 * @param uuid
	 * @return
	 */
	public abstract TaxonNameBase getTaxonNameByUuid(UUID uuid);

	/**
	 * @param taxonName
	 * @return
	 */
	public abstract UUID saveTaxonName(TaxonNameBase taxonName);

	/**
	 * Saves a collection of  TaxonNames and return its UUID@param taxonCollection
	 * @return
	 */
	public abstract Map<UUID, TaxonNameBase> saveTaxonNameAll(Collection<TaxonNameBase> taxonCollection);

	/**
	 * @param limit
	 * @param start
	 * @return
	 */
	public abstract List<TaxonNameBase> getAllNames(int limit, int start);

	/**
	 * @param name
	 * @return
	 */
	public abstract List<TaxonNameBase> getNamesByName(String name);

	/**
	 * Returns all Ranks.
	 * @return
	 */
	public abstract OrderedTermVocabulary<Rank> getRankVocabulary();
}