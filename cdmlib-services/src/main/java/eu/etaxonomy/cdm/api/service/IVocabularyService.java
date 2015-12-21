/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.List;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
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
	public <T extends DefinedTermBase> List<TermVocabulary<T>> findByTermType(TermType termType);

}
