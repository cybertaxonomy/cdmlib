// $Id$
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

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.LanguageStringBase;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.location.TdwgArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.common.ILanguageStringBaseDao;
import eu.etaxonomy.cdm.persistence.dao.common.ILanguageStringDao;
import eu.etaxonomy.cdm.persistence.dao.common.IRepresentationDao;
import eu.etaxonomy.cdm.persistence.dao.common.ITermVocabularyDao;

@Service
@Transactional(readOnly = true)
public class TermServiceImpl extends ServiceBase<DefinedTermBase,IDefinedTermDao> implements ITermService{
	private static final Logger logger = Logger.getLogger(TermServiceImpl.class);
	
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
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITermService#getTermByUri(java.lang.String)
	 */
	public DefinedTermBase getTermByUri(String uri) {
		//FIXME transformation from URI to UUID
		return dao.findByUri(uri);
	}
	
	/* FIXME candidate for harmonization(non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITermService#getTermByUuid(java.util.UUID)
	 */
	public DefinedTermBase getTermByUuid(UUID uuid) {
		return dao.findByUuid(uuid);  
	}
	
	public List<DefinedTermBase> getAllDefinedTerms(int limit, int start){
		return dao.list(limit, start);
	}

	@Transactional(readOnly = false)
	public UUID saveTerm(DefinedTermBase termBase) {
		return super.saveCdmObject(termBase);
	}
	
	@Transactional(readOnly = false)
	public Map<UUID, DefinedTermBase> saveTermsAll(Collection<? extends DefinedTermBase> termBaseCollection){
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
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITermService#getLanguageVocabulary()
	 */
	public TermVocabulary<Language> getLanguageVocabulary() {
		String uuidString = "45ac7043-7f5e-4f37-92f2-3874aaaef2de";
		UUID uuid = UUID.fromString(uuidString);
		TermVocabulary<Language> languageVocabulary = 
			(TermVocabulary)vocabularyDao.findByUuid(uuid);
		return languageVocabulary;
	}
	
	public Map<UUID, TermVocabulary<DefinedTermBase>> 
    saveTermVocabulariesAll(Collection<TermVocabulary<DefinedTermBase>> termVocabularies) {
		return vocabularyDao.saveAll(termVocabularies);
	}
	
	public UUID saveTermVocabulary(TermVocabulary termVocabulary) {
		return vocabularyDao.save(termVocabulary);
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
	
	public Language getLanguageByIso(String iso639) {
		return dao.getLanguageByIso(iso639);
	}
	
	@Transactional(readOnly = false)
	public UUID saveLanguageData(
			LanguageStringBase languageData) {
			return languageStringBaseDao.save(languageData);
	}

	@Autowired
	protected void setDao(IDefinedTermDao dao) {
		this.dao = dao;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITermService#getAreaByTdwgAbbreviation(java.lang.String)
	 */
	public NamedArea getAreaByTdwgAbbreviation(String tdwgAbbreviation) {
		//FIXME this is just a placeholder until it is decided where to implement this method 
		//(see also FIXMEs in TdwgArea)
		return TdwgArea.getAreaByTdwgAbbreviation(tdwgAbbreviation);
	}

	public <T extends DefinedTermBase> Pager<T> getGeneralizationOf(T definedTerm, Integer pageSize, Integer pageNumber) {
        Integer numberOfResults = dao.countGeneralizationOf(definedTerm);
		
		List<T> results = new ArrayList<T>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getGeneralizationOf(definedTerm, pageSize, pageNumber); 
		}
		
		return new DefaultPagerImpl<T>(pageNumber, numberOfResults, pageSize, results);
	}

	public <T extends DefinedTermBase> Pager<T> getIncludes(Set<T> definedTerms, Integer pageSize, Integer pageNumber) {
        Integer numberOfResults = dao.countIncludes(definedTerms);
		
		List<T> results = new ArrayList<T>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getIncludes(definedTerms, pageSize, pageNumber); 
		}
		
		return new DefaultPagerImpl<T>(pageNumber, numberOfResults, pageSize, results);
	}

	public Pager<Media> getMedia(DefinedTermBase definedTerm, Integer pageSize,	Integer pageNumber) {
        Integer numberOfResults = dao.countMedia(definedTerm);
		
		List<Media> results = new ArrayList<Media>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getMedia(definedTerm, pageSize, pageNumber); 
		}
		
		return new DefaultPagerImpl<Media>(pageNumber, numberOfResults, pageSize, results);
	}

	public <T extends DefinedTermBase> Pager<T> getPartOf(Set<T> definedTerms,Integer pageSize, Integer pageNumber) {
        Integer numberOfResults = dao.countPartOf(definedTerms);
		
		List<T> results = new ArrayList<T>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getPartOf(definedTerms, pageSize, pageNumber); 
		}
		
		return new DefaultPagerImpl<T>(pageNumber, numberOfResults, pageSize, results);
	}

	public Pager<NamedArea> list(NamedAreaLevel level, NamedAreaType type,	Integer pageSize, Integer pageNumber) {
        Integer numberOfResults = dao.count(level, type);
		
		List<NamedArea> results = new ArrayList<NamedArea>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.list(level, type, pageSize, pageNumber); 
		}
		
		return new DefaultPagerImpl<NamedArea>(pageNumber, numberOfResults, pageSize, results);
	}
	
	
	
}
