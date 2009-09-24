package eu.etaxonomy.cdm.api.service;

import java.util.List;
import java.util.Set;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VocabularyEnum;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

public interface IVocabularyService extends IIdentifiableEntityService<TermVocabulary> {
    public TermVocabulary getVocabulary(VocabularyEnum vocabularyType);
	
	public Set<TermVocabulary> listVocabularies(Class termClass);
	
	/**
	 * Returns Language Vocabulary
	 * @return
	 */
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
	public Pager<DefinedTermBase> getTerms(TermVocabulary vocabulary, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);

}
