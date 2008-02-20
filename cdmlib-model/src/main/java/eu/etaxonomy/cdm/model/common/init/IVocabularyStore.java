package eu.etaxonomy.cdm.model.common.init;

import java.util.UUID;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

public interface IVocabularyStore {
	
	public void saveOrUpdate(TermVocabulary<DefinedTermBase> vocabulary);
	
	public DefinedTermBase getTermByUuid(UUID uuid);
	
	public TermVocabulary<DefinedTermBase> getVocabularyByUuid(UUID uuid);
	
	//public void createAndSaveDefaultLanguage();

}
