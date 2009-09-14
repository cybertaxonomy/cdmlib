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
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
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
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VocabularyEnum;
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
import eu.etaxonomy.cdm.persistence.query.OrderHint;

@Service
@Transactional(readOnly = true)
public class TermServiceImpl extends IdentifiableServiceBase<DefinedTermBase,IDefinedTermDao> implements ITermService{
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
	protected void setDao(IDefinedTermDao dao) {
		this.dao = dao;
	}
	
	@Autowired
	protected void setVocabularyDao(ITermVocabularyDao vocabularyDao) {
		this.vocabularyDao = vocabularyDao;
	}
	
	/**
	 *  (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITermService#getTermByUri(java.lang.String)
	 * FIXME Candidate for harmonization
	 * rename -> getByUri
	 */
	public DefinedTermBase getTermByUri(String uri) {
		//FIXME transformation from URI to UUID
		return dao.findByUri(uri);
	}
	
	/**
	 *  (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITermService#getTermByUuid(java.util.UUID)
	 * FIXME candidate for harmonization
	 */
	public DefinedTermBase getTermByUuid(UUID uuid) {
		return dao.findByUuid(uuid);  
	}
	
	public DefinedTermBase loadTerm(UUID uuid, List<String> propertyPaths){
		return dao.load(uuid, propertyPaths); 
	}
	
	/**
	 * FIXME candidate for harmonization
	 * list
	 */
	public List<DefinedTermBase> getAllDefinedTerms(int limit, int start){
		return dao.list(limit, start);
	}

	/**
	 * FIXME candidate for harmonization
	 * save
	 */
	@Transactional(readOnly = false)
	public UUID saveTerm(DefinedTermBase termBase) {
		return super.saveCdmObject(termBase);
	}
	
	/**
	 * FIXME candidate for harmonization
	 * save(Set<DefinedTermBase> terms)
	 */
	@Transactional(readOnly = false)
	public Map<UUID, DefinedTermBase> saveTermsAll(Collection<? extends DefinedTermBase> termBaseCollection){
		return saveCdmObjectAll(termBaseCollection);
	}

	/**
	 *  (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITermService#getVocabulary(java.util.UUID)
	 * FIXME candidate for harmonization
	 * vocabuaryService.find
	 */
	public TermVocabulary<? extends DefinedTermBase<?>> getVocabulary(UUID vocabularyUuid) {
		TermVocabulary<? extends DefinedTermBase<?>> vocabulary = (TermVocabulary) vocabularyDao.findByUuid(vocabularyUuid);
		return vocabulary;
	}
	
	/**
	 * FIXME candidate for harmonization
	 * vocabularyService.load
	 */
	public TermVocabulary loadVocabulary(UUID vocabularyUuid, List<String> propertyPaths) {
		TermVocabulary<? extends DefinedTermBase<?>> vocabulary = (TermVocabulary) vocabularyDao.load(vocabularyUuid,
			propertyPaths);
		return vocabulary;
	}
	
	/**
	 * FIXME candidate for harmonization
	 * move to vocabularyService
	 */
	public TermVocabulary<? extends DefinedTermBase<?>> getVocabulary(VocabularyEnum vocabularyType){
		TermVocabulary<? extends DefinedTermBase<?>> vocabulary = getVocabulary(vocabularyType.getUuid());
		return vocabulary;
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
	 * FIXME candidate for harmonization
	 * vocabularyService.list
	 */
	public List<TermVocabulary<DefinedTermBase>> getAllTermVocabularies(int limit, int start) {
		return vocabularyDao.list(limit, start);
	}
	
	public List<TermVocabulary<DefinedTermBase>> listTermVocabularies(Integer limit, Integer start, List<OrderHint> orderHints,
		List<String> propertyPaths){
		return vocabularyDao.list(limit, start, orderHints, propertyPaths);
	}

	/**
	 * FIXME candidate for harmonization
	 * vocabularyService.page
	 */
	public Pager<TermVocabulary<DefinedTermBase>> pageTermVocabularies(Integer pageSize, Integer pageNumber,
			List<OrderHint> orderHints, List<String> propertyPaths) {

		List<TermVocabulary<DefinedTermBase>> vocabs = vocabularyDao.list(pageSize, pageNumber * pageSize, orderHints,
			propertyPaths);
		Pager<TermVocabulary<DefinedTermBase>> pager = new DefaultPagerImpl<TermVocabulary<DefinedTermBase>>(
			pageNumber, vocabs.size(), pageSize, vocabs);
		return pager;
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
		TermVocabulary<Language> languageVocabulary = 
			(TermVocabulary)vocabularyDao.findByUuid(uuid);
		return languageVocabulary;
	}
	
	/**
	 * FIXME candidate for harmonization
	 * vocabularyService.save(Set<TermVocabulary> vocabularies)
	 */
	public Map<UUID, TermVocabulary<DefinedTermBase>> 
    saveTermVocabulariesAll(Collection<TermVocabulary<DefinedTermBase>> termVocabularies) {
		return vocabularyDao.saveAll(termVocabularies);
	}
	
	public UUID saveTermVocabulary(TermVocabulary termVocabulary) {
		return vocabularyDao.save(termVocabulary);
	}

	/**
	 * FIXME candidate for harmonization
	 * remove redundant code
	 */
//	@Transactional(readOnly = false)
//	public Map<UUID, Representation> saveRepresentationsAll(Collection<Representation> representations){
//		return representationDao.saveAll(representations);
//	}

	/**
	 * FIXME candidate for harmonization
	 * this code is not used, remove it
	 */
	@Transactional(readOnly = false)
	public Map<UUID, LanguageStringBase> saveLanguageDataAll(Collection<LanguageStringBase> languageData) {
		return languageStringBaseDao.saveAll(languageData);
	}
	
	/**
	 * FIXME candidate for harmonization
	 * Given that representations are owned by TermBase, this method is redundant
	 * @param representations
	 * @return
	 */
	@Transactional(readOnly = false)
	public Map<UUID, Representation> saveRepresentationAll(Collection<Representation> representations) {
		return representationDao.saveAll(representations);
	}
	
	/**
 	 * FIXME candidate for harmonization
	 * Given that representations are owned by TermBase, this method is redundant
	 */
	public List<Representation> getAllRepresentations(int limit, int start){
		return representationDao.list(limit, start);
	}

	/**
 	 * FIXME candidate for harmonization
	 * Given that languageStrings are owned by other objects, this method is redundant
	 */
	public List<LanguageString> getAllLanguageStrings(int limit, int start) {
		return languageStringDao.list(limit, start);
	}
	
	/**
 	 * FIXME candidate for harmonization
	 * Given that languageStrings are owned by other objects, this method is redundant
	 */
	public Map<UUID, LanguageStringBase> 
	       saveLanguageStringBasesAll(Collection<LanguageStringBase> languageStringBases) {
		return languageStringBaseDao.saveAll(languageStringBases);
	}
	
	public Language getLanguageByIso(String iso639) {
		return dao.getLanguageByIso(iso639);
	}
	
	public List<Language> getLanguagesByLocale(Enumeration<Locale> locales){
		return dao.getLanguagesByLocale(locales);
	}
	
	/**
 	 * FIXME candidate for harmonization
	 * Given that languageStrings are owned by other objects, this method is redundant
	 */
	@Transactional(readOnly = false)
	public UUID saveLanguageData(
			LanguageStringBase languageData) {
			return languageStringBaseDao.save(languageData);
	}

	/**
	 *  (non-Javadoc)
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

	public <T extends DefinedTermBase> Pager<T> getIncludes(Set<T> definedTerms, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        Integer numberOfResults = dao.countIncludes(definedTerms);
		
		List<T> results = new ArrayList<T>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getIncludes(definedTerms, pageSize, pageNumber,propertyPaths); 
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

	public <T extends DefinedTermBase> Pager<T> getPartOf(Set<T> definedTerms,Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        Integer numberOfResults = dao.countPartOf(definedTerms);
		
		List<T> results = new ArrayList<T>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getPartOf(definedTerms, pageSize, pageNumber, propertyPaths); 
		}
		
		return new DefaultPagerImpl<T>(pageNumber, numberOfResults, pageSize, results);
	}

	public Pager<NamedArea> list(NamedAreaLevel level, NamedAreaType type, Integer pageSize, Integer pageNumber,
			List<OrderHint> orderHints, List<String> propertyPaths) {
		Integer numberOfResults = dao.count(level, type);

		List<NamedArea> results = new ArrayList<NamedArea>();
		if (numberOfResults > 0) { // no point checking again
			results = dao.list(level, type, pageSize, pageNumber, orderHints, propertyPaths);
		}

		return new DefaultPagerImpl<NamedArea>(pageNumber, numberOfResults, pageSize, results);
	}

	public <T extends DefinedTermBase> Pager<T> findByRepresentationText(String label, Class<T> clazz, Integer pageSize, Integer pageNumber) {
        Integer numberOfResults = dao.countDefinedTermByRepresentationText(label,clazz);
		
		List<T> results = new ArrayList<T>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getDefinedTermByRepresentationText(label, clazz, pageSize, pageNumber);
		}
		
		return new DefaultPagerImpl<T>(pageNumber, numberOfResults, pageSize, results);
	}

	public void generateTitleCache() {
		// TODO Auto-generated method stub
		
	}	
}
