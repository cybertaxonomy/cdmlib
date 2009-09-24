package eu.etaxonomy.cdm.api.service;

import java.util.Set;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VocabularyEnum;

public interface IVocabularyService extends IIdentifiableEntityService<TermVocabulary> {
    public TermVocabulary getVocabulary(VocabularyEnum vocabularyType);
	
	public Set<TermVocabulary> listVocabularies(Class termClass);
	
	/**
	 * Returns Language Vocabulary
	 * @return
	 */
	public TermVocabulary<Language> getLanguageVocabulary();

}
