/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.LanguageStringBase;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.common.ILanguageStringBaseDao;
import eu.etaxonomy.cdm.persistence.dao.common.ILanguageStringDao;
import eu.etaxonomy.cdm.persistence.dao.common.IRepresentationDao;
import eu.etaxonomy.cdm.persistence.dao.common.ITermVocabularyDao;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;

@Service
@Transactional(readOnly = true)
public class TermServiceImpl extends ServiceBase<DefinedTermBase> implements ITermService{
	static Logger logger = Logger.getLogger(TermServiceImpl.class);
	
	protected ITermVocabularyDao vocabularyDao;
	@Autowired
	@Qualifier("langStrBaseDao")
	private ILanguageStringBaseDao languageStringBaseDao;
	@Autowired
	private IRepresentationDao representationDao;
	@Autowired
	private ILanguageStringDao languageStringDao;
	
	@Autowired
	protected void setVocabularyDao(ITermVocabularyDao vocabularyDao) {
		this.vocabularyDao = vocabularyDao;
	}
	
	@Autowired
	protected void setDao(IDefinedTermDao dao) {
		this.dao = dao;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITermService#getTermByUri(java.lang.String)
	 */
	public DefinedTermBase getTermByUri(String uri) {
		//FIXME transformation from URI to UUID
		return DefinedTermBase.findByUuid(UUID.fromString(uri));  
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITermService#getTermByUuid(java.util.UUID)
	 */
	public DefinedTermBase getTermByUuid(UUID uuid) {
		return DefinedTermBase.findByUuid(uuid);  
	}
	
	public List<DefinedTermBase> getAllDefinedTerms(int limit, int start){
		return dao.list(limit, start);
	}

	@Transactional(readOnly = false)
	public UUID saveTerm(DefinedTermBase termBase) {
		return super.saveCdmObject(termBase);
	}
	
	@Transactional(readOnly = false)
	public Map<UUID, DefinedTermBase> saveTermsAll(Collection<DefinedTermBase> termBaseCollection){
		return saveCdmObjectAll(termBaseCollection);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITermService#getVocabulary(java.util.UUID)
	 */
	public TermVocabulary getVocabulary(UUID vocabularyUuid) {
		TermVocabulary vocabulary = (OrderedTermVocabulary)vocabularyDao.findByUuid(vocabularyUuid);
		return vocabulary;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITermService#listVocabularies(java.lang.Class)
	 */
	public Set<TermVocabulary> listVocabularies(Class termClass) {
		logger.error("Method not implemented yet");
		return null;
	}

	public List<TermVocabulary<DefinedTermBase>> getAllTermVocabularies(int limit, int start) {
		return vocabularyDao.list(limit, start);
	}
	
	public Map<UUID, TermVocabulary<DefinedTermBase>> 
    saveTermVocabulariesAll(Collection<TermVocabulary<DefinedTermBase>> termVocabularies) {
		return vocabularyDao.saveAll(termVocabularies);
	}

//	@Transactional(readOnly = false)
//	public Map<UUID, Representation> saveRepresentationsAll(Collection<Representation> representations){
//		return representationDao.saveAll(representations);
//	}

	@Transactional(readOnly = false)
	public Map<UUID, LanguageStringBase> saveLanguageDataAll(Collection<LanguageStringBase> languageData) {
		return languageStringBaseDao.saveAll(languageData);
	}
	
	@Transactional(readOnly = false)
	public Map<UUID, Representation> saveRepresentationAll(Collection<Representation> representations) {
		return representationDao.saveAll(representations);
	}
	
	public List<Representation> getAllRepresentations(int limit, int start){
		return representationDao.list(limit, start);
	}
	
	public List<LanguageString> getAllLanguageStrings(int limit, int start) {
		return languageStringDao.list(limit, start);
	}
	
	public Map<UUID, LanguageStringBase> 
	       saveLanguageStringBasesAll(Collection<LanguageStringBase> languageStringBases) {
		return languageStringBaseDao.saveAll(languageStringBases);
	}
	
}
