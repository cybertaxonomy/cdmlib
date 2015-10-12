/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.common;


import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
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
	public int countTerms(TermVocabulary termVocabulary);

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
     * Returns term vocabularies that contain terms of a certain class e.g. Feature, Modifier, State.
     *
     * @param <TERMCLASS>
     * @param clazz the term class of the terms in the vocabulary
     * @param includeSubclasses if <code>true</code> all subclasses of clazz will be included for computation of the result
     * @param includeEmptyVocs if <code>true</code> all vocabularies that do not contain any term will be included in the result
     * @param limit The maximum number of vocabularies returned (can be null for all vocabularies)
     * @param start The offset from the start of the result set (0 - based, can be null - equivalent of starting at the beginning of the recordset)
     * @param orderHints
     *            Supports path like <code>orderHints.propertyNames</code> which
	 *            include *-to-one properties like createdBy.username or
	 *            authorTeam.persistentTitleCache
     * @param propertyPaths properties to be initialized
     * @return a list of term vocabularies
     */
	/**
     * @deprecated This method is deprecated as we are using {@link TermType} now.
     * It may be removed in a future version.
     */
	@Deprecated
	public <TERMCLASS extends DefinedTermBase> List<TermVocabulary<? extends TERMCLASS>> listByTermClass(Class<TERMCLASS> clazz, boolean includeSubclasses, boolean includeEmptyVocs, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);


    /**
     * Return a List of vocabularies that belong to the term type supplied
     *
     * @param termType The term type corresponding to the vocabularies of interest
     * @return a List of vocabularies
     */
    public <T extends DefinedTermBase> List<TermVocabulary<T>> findByTermType(TermType termType);


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
//
//	/**
//	 * Returns all empty vocabularies.
//	 * @param limit
//	 * @param start
//	 * @param orderHints
//	 * @param propertyPaths
//	 * @return
//	 */
//	public List<TermVocabulary> listEmpty(Integer limit, Integer start,List<OrderHint> orderHints, List<String> propertyPaths);

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

}
