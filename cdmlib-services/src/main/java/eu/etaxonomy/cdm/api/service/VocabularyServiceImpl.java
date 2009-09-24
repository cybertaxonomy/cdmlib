package eu.etaxonomy.cdm.api.service;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VocabularyEnum;
import eu.etaxonomy.cdm.persistence.dao.common.ITermVocabularyDao;

@Service
@Transactional(readOnly = true)
public class VocabularyServiceImpl extends IdentifiableServiceBase<TermVocabulary,ITermVocabularyDao>  implements IVocabularyService {

	@Autowired
	protected void setDao(ITermVocabularyDao dao) {
		this.dao = dao;
	}

	public void generateTitleCache() {
		// TODO Auto-generated method stub
	}
	
	public TermVocabulary<DefinedTermBase> getVocabulary(VocabularyEnum vocabularyType){
		return dao.findByUuid(vocabularyType.getUuid());
	}
	
	/**
	 *  (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITermService#listVocabularies(java.lang.Class)
	 * FIXME candidate for harmonization
	 * vocabularyService.list
	 */
	public Set<TermVocabulary> listVocabularies(Class termClass) {
		logger.error("Method not implemented yet");
		return null;
	}	
	
	/** 
	 * (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITermService#getLanguageVocabulary()
	 * FIXME candidate for harmonization
	 * is this the same as getVocabulary(VocabularyEnum.Language)
	 */
	public TermVocabulary<Language> getLanguageVocabulary() {
		String uuidString = "45ac7043-7f5e-4f37-92f2-3874aaaef2de";
		UUID uuid = UUID.fromString(uuidString);
		TermVocabulary<Language> languageVocabulary = (TermVocabulary)dao.findByUuid(uuid);
		return languageVocabulary;
	}

}
