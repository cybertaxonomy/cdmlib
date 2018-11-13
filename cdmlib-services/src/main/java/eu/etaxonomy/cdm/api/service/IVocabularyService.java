/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.persistence.dto.TermDto;
import eu.etaxonomy.cdm.persistence.dto.TermVocabularyDto;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

public interface IVocabularyService extends IIdentifiableEntityService<TermVocabulary> {

    /**
     * Returns term vocabularies that contain terms of a certain {@link TermType} e.g. Feature, Modifier, State.
     *
     * @param <TERMTYPE>
     * @param termType the {@link TermType} of the terms in the vocabulary and of the vocabulary
     * @param includeSubTypes if <code>true</code> all subtypes will be included for computation of the result
     * @param limit The maximum number of vocabularies returned (can be null for all vocabularies)
     * @param start The offset from the start of the result set (0 - based, can be null - equivalent of starting at the beginning of the recordset)
     * @param orderHints
     *            Supports path like <code>orderHints.propertyNames</code> which
     *            include *-to-one properties like createdBy.username or
     *            authorTeam.persistentTitleCache
     * @param propertyPaths properties to be initialized
     * @return a list of term vocabularies
     */
    public List<TermVocabulary> listByTermType(TermType termType, boolean includeSubTypes, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);

	/**
	 * Returns Language Vocabulary
	 * @return
	 */
	//TODO candidate for harmonization: rename to loadLanguageVocabulary(...
	public TermVocabulary<Language> getLanguageVocabulary();

	/**
	 * Returns a list of terms belonging to the vocabulary passed as an argument
	 *
	 * @param vocabulary The vocabulary for which the list of terms is desired
	 * @param limit The maximum number of terms returned (can be null for all terms in the vocabulary)
	 * @param start The offset from the start of the result set (0 - based, can be null - equivalent of starting at the beginning of the recordset)
	 * @param orderHints
	 *            Supports path like <code>orderHints.propertyNames</code> which
	 *            include *-to-one properties like createdBy.username or
	 *            authorTeam.persistentTitleCache
	 * @param propertyPaths properties to be initialized
	 * @return a paged list of terms
	 */
	//TODO candidate for harmonization: rename to getTerms(...
	public Pager<DefinedTermBase> getTerms(TermVocabulary vocabulary, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);

	/**
	 * Returns a list of term vocabularies corresponding to a term type
	 *
	 * @param termType The term type for which the list of vocabularies is desired
	 * @return a list of vocabularies
	 */
	public <T extends DefinedTermBase> List<TermVocabulary<T>> findByTermType(TermType termType, List<String> propertyPaths);

	/**
	 * Loads all top level terms, i.e. terms that have no parent terms, for the given vocabulary
	 * @param vocabularyUuid the uuid of the vocabulary
	 * @return a collection of top level terms
	 */
	public Collection<TermDto> getTopLevelTerms(UUID vocabularyUuid);

	/**
	 * Initializes the complete term hierarchy consisting of {@link TermDto}s
	 * for the given vocabulary
	 * @param vocabularyUuid the UUID of the {@link TermVocabulary}
	 * @return a the top level elements for this vocabulary
	 */

	public Collection<TermDto> getCompleteTermHierarchy(UUID vocabularyUuid);
    /**
     * Returns term vocabularies that contain terms of a certain {@link TermType} e.g. Feature, Modifier, State.
     *
     * @param termType the {@link TermType} of the terms in the vocabulary and of the vocabulary
     * @return a list of term vocabularies
     */
    public List<TermVocabularyDto> findVocabularyDtoByTermType(TermType termType);

    /**
     * Creates a new term as a direct child of the given vocabulary.
     * @param termType the {@link TermType} of the term to create
     * @param vocabularyUUID the {@link UUID} of the vocabulary
     * kindOf relation. Otherwise it will added via a partOf relation
     * @return the new term
     */
    public TermDto addNewTerm(TermType termType, UUID vocabularyUUID);

}
