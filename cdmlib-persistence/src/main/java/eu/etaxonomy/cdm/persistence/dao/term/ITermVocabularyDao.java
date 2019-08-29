/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.term;


import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import eu.etaxonomy.cdm.persistence.dto.TermDto;
import eu.etaxonomy.cdm.persistence.dto.TermVocabularyDto;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.OrderHint;


/**
 * @author a.mueller
 *
 */
public interface ITermVocabularyDao extends IIdentifiableDao<TermVocabulary> {

	/**
	 * Return a count of terms that belong to the termVocabulary supplied
	 *
	 * @param termVocabulary The term vocabulary which 'owns' the terms of interest
	 * @return a count of terms
	 */
	public long countTerms(TermVocabulary termVocabulary);

	/**
	 * Return a List of terms that belong to the termVocabulary supplied
	 *
	 * @param termVocabulary The term vocabulary which 'owns' the terms of interest
	 * @param pageSize The maximum number of terms returned (can be null for all terms)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a List of terms
	 */
	public <T extends DefinedTermBase> List<T> getTerms(TermVocabulary<T> termVocabulary, Integer pageSize, Integer pageNumber);

	public <T extends DefinedTermBase> TermVocabulary<T> findByUri(String termSourceUri, Class<T> clazz);

	/**
	 * Return a List of terms that belong to the termVocabulary supplied
	 *
	 * @param termVocabulary The term vocabulary which 'owns' the terms of interest
	 * @param pageSize The maximum number of terms returned (can be null for all terms)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints
	 *            Supports path like <code>orderHints.propertyNames</code> which
	 *            include *-to-one properties like createdBy.username or
	 *            authorTeam.persistentTitleCache
	 * @param propertyPaths properties to be initialized
	 * @return a List of terms
	 */
	public <T extends DefinedTermBase> List<T> getTerms(TermVocabulary<T> vocabulary,Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Return a List of vocabularies that belong to the term type supplied
     *
     * @param termType The term type corresponding to the vocabularies of interest
     * @param propertyPaths
     * @return a List of vocabularies
     */
    public <T extends DefinedTermBase> List<TermVocabulary<T>> findByTermType(TermType termType, List<String> propertyPaths);


	/**
     * Returns term vocabularies that contain terms of a certain {@link TermType} e.g. Feature, Modifier, State.
     *
     * @param <TERMTYPE>
     * @param termType the {@link TermType} of the terms in the vocabulary and of the vocabulary
     * @param includeSubtypes if <code>true</code> all subtypes will be included for computation of the result
     * @param limit The maximum number of vocabularies returned (can be null for all vocabularies)
     * @param start The offset from the start of the result set (0 - based, can be null - equivalent of starting at the beginning of the recordset)
     * @param orderHints
     *            Supports path like <code>orderHints.propertyNames</code> which
     *            include *-to-one properties like createdBy.username or
     *            authorTeam.persistentTitleCache
     * @param propertyPaths properties to be initialized
     * @return a list of term vocabularies
     */
	public List<TermVocabulary> listByTermType(TermType termType, boolean includeSubtypes, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);

	/**
	 * Fills the response map with those term uuids which do exist in the requested map
	 * but not in the repository (missing terms). The map key is the vocabulary uuid in both cases.
	 * If parameter vocabularyRepsonse is not <code>null</code> the vocabularies will be fully loaded
	 * and returned within the map. The later is for using this method together with fast termloading.
	 * @param uuidsRequested
	 * @param uuidsRepsonse
	 * @param vocabularyResponse
	 */
	public void missingTermUuids(Map<UUID, Set<UUID>> uuidsRequested,
			Map<UUID, Set<UUID>> uuidsRepsonse,
			Map<UUID, TermVocabulary<?>> vocabularyResponse);

    /**
     * Loads all top level terms, i.e. terms that have no parent terms, for the given vocabulary
     * @param vocabularyUuid the id of the vocabulary
     * @return a collection of top level terms
     */
    public Collection<TermDto> getTopLevelTerms(UUID vocabularyUuid);

    /**
     * Loads all terms for the given vocabulary
     * @param vocabularyUuid the id of the vocabulary
     * @return a collection of terms
     */
    public Collection<TermDto> getTerms(UUID vocabularyUuid);

    /**
     * Loads all terms for the given vocabularies
     * @param vocabularyUuids the ids of the vocabularies
     * @return a collection of terms
     */
    public Collection<TermDto> getTerms(List<UUID> vocabularyUuids);

    /**
     * Returns term vocabularies that contain terms of a certain {@link TermType} e.g. Feature, Modifier, State.
     *
     * @param termType the {@link TermType} of the terms in the vocabulary and of the vocabulary
     * @return a list of term vocabularies
     */
    public List<TermVocabularyDto> findVocabularyDtoByTermType(TermType termType);

    /**
     * Returns term vocabularies that contain terms of a certain {@link TermType} e.g. Feature, Modifier, State.
     *
     * @param termType the {@link TermType} of the terms in the vocabulary and of the vocabulary
     * @return a list of term vocabularies
     */
    public List<TermVocabularyDto> findVocabularyDtoByTermTypes(Set<TermType> termType);

    /**
     * Returns term vocabulary for UUID
     *
     * @param UUID the {@link UUID} of the vocabulary
     * @return term vocabularies
     */
    public TermVocabularyDto findVocabularyDtoByUuid(UUID vocUuid);

    /**
     *
     * Like {@link #getUuidAndTitleCache(Class, Integer, String)} but filtering
     * the results by {@link TermType} of the vocabularies.
     *
     *
     * @param clazz
     *            the (sub)class
     * @param termType
     *            the {@link TermType} of the vocabularies to be retrieved
     * @param limit
     *            max number of results
     * @param pattern
     *            search pattern
     * @return a list of {@link UuidAndTitleCache}
     *
     * @see #getUuidAndTitleCache(Class, Integer, String))
     */
    public <S extends TermVocabulary> List<UuidAndTitleCache<S>> getUuidAndTitleCache(Class<S> clazz, TermType termType,
            Integer limit, String pattern);
}
