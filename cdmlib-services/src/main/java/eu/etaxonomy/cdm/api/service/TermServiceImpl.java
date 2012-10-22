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

import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.DeleteResult.DeleteStatus;
import eu.etaxonomy.cdm.api.service.config.TermDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.LanguageStringBase;
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
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class TermServiceImpl extends IdentifiableServiceBase<DefinedTermBase,IDefinedTermDao> implements ITermService{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TermServiceImpl.class);
	private ILanguageStringDao languageStringDao;
	@Autowired
	@Qualifier("langStrBaseDao")
	private ILanguageStringBaseDao languageStringBaseDao;
	private IRepresentationDao representationDao;
	
	@Autowired
	public void setLanguageStringDao(ILanguageStringDao languageStringDao) {
		this.languageStringDao = languageStringDao;
	} 
	
	@Autowired
	public void setRepresentationDao(IRepresentationDao representationDao) {
		this.representationDao = representationDao;
	}
	
	@Autowired
	protected void setDao(IDefinedTermDao dao) {
		this.dao = dao;
	}
		
	public <TERM extends DefinedTermBase> List<TERM> listByTermClass(Class<TERM> clazz, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
		return dao.listByTermClass(clazz, limit, start, orderHints, propertyPaths);
	}	
	
	/**
	 * @see eu.etaxonomy.cdm.api.service.ITermService#getTermByUri(java.net.URI)
	 */
	public DefinedTermBase getByUri(URI uri) {
		return dao.findByUri(uri);
	}
	
	public Language getLanguageByIso(String iso639) {
		return dao.getLanguageByIso(iso639);
	}
	
	public List<Language> getLanguagesByLocale(Enumeration<Locale> locales){
		return dao.getLanguagesByLocale(locales);
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
		if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getGeneralizationOf(definedTerm, pageSize, pageNumber); 
		}
		
		return new DefaultPagerImpl<T>(pageNumber, numberOfResults, pageSize, results);
	}

	public <T extends DefinedTermBase> Pager<T> getIncludes(Set<T> definedTerms, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        Integer numberOfResults = dao.countIncludes(definedTerms);
		
		List<T> results = new ArrayList<T>();
		if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getIncludes(definedTerms, pageSize, pageNumber,propertyPaths); 
		}
		
		return new DefaultPagerImpl<T>(pageNumber, numberOfResults, pageSize, results);
	}

	public Pager<Media> getMedia(DefinedTermBase definedTerm, Integer pageSize,	Integer pageNumber) {
        Integer numberOfResults = dao.countMedia(definedTerm);
		
		List<Media> results = new ArrayList<Media>();
		if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getMedia(definedTerm, pageSize, pageNumber); 
		}
		
		return new DefaultPagerImpl<Media>(pageNumber, numberOfResults, pageSize, results);
	}

	public <T extends DefinedTermBase> Pager<T> getPartOf(Set<T> definedTerms,Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        Integer numberOfResults = dao.countPartOf(definedTerms);
		
		List<T> results = new ArrayList<T>();
		if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getPartOf(definedTerms, pageSize, pageNumber, propertyPaths); 
		}
		
		return new DefaultPagerImpl<T>(pageNumber, numberOfResults, pageSize, results);
	}

	public Pager<NamedArea> list(NamedAreaLevel level, NamedAreaType type, Integer pageSize, Integer pageNumber,
			List<OrderHint> orderHints, List<String> propertyPaths) {
		Integer numberOfResults = dao.count(level, type);

		List<NamedArea> results = new ArrayList<NamedArea>();
		if (numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.list(level, type, pageSize, pageNumber, orderHints, propertyPaths);
		}

		return new DefaultPagerImpl<NamedArea>(pageNumber, numberOfResults, pageSize, results);
	}

	public <T extends DefinedTermBase> Pager<T> findByRepresentationText(String label, Class<T> clazz, Integer pageSize, Integer pageNumber) {
        Integer numberOfResults = dao.countDefinedTermByRepresentationText(label,clazz);
		
		List<T> results = new ArrayList<T>();
		if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getDefinedTermByRepresentationText(label, clazz, pageSize, pageNumber);
		}
		
		return new DefaultPagerImpl<T>(pageNumber, numberOfResults, pageSize, results);
	}
	
	public <T extends DefinedTermBase> Pager<T> findByRepresentationAbbreviation(String abbrev, Class<T> clazz, Integer pageSize, Integer pageNumber) {
        Integer numberOfResults = dao.countDefinedTermByRepresentationAbbrev(abbrev,clazz);
		
		List<T> results = new ArrayList<T>();
		if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getDefinedTermByRepresentationAbbrev(abbrev, clazz, pageSize, pageNumber);
		}
		
		return new DefaultPagerImpl<T>(pageNumber, numberOfResults, pageSize, results);
	}

	public List<LanguageString> getAllLanguageStrings(int limit, int start) {
		return languageStringDao.list(limit, start);
	}

	public List<Representation> getAllRepresentations(int limit, int start) {
		return representationDao.list(limit,start);
	}

	public UUID saveLanguageData(LanguageStringBase languageData) {
		return languageStringBaseDao.save(languageData);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ServiceBase#delete(eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	/** @deprecated use {@link #delete(DefinedTermBase, TermDeletionConfigurator)} instead
	 * to allow DeleteResult return type*/
	@Deprecated
	public UUID delete(DefinedTermBase term){
		UUID result = term.getUuid();
		TermDeletionConfigurator defaultConfig = new TermDeletionConfigurator();
		delete(term, defaultConfig);
		return result;
	}

	@Override
	public DeleteResult delete(DefinedTermBase term, TermDeletionConfigurator config){
		if (config == null){
			config = new TermDeletionConfigurator();
		}
//		boolean isInternal = config.isInternal();
		DeleteResult result = new DeleteResult();
		Set<DefinedTermBase> termsToSave = new HashSet<DefinedTermBase>();
		
		try {
			//generalization of
			Set<DefinedTermBase> specificTerms = term.getGeneralizationOf();
			if (specificTerms.size()>0){
				if (config.isDeleteGeneralizationOfRelations()){
					DefinedTermBase generalTerm = term.getKindOf();
					for (DefinedTermBase specificTerm: specificTerms){
						term.removeGeneralization(specificTerm);
						if (generalTerm != null){
							generalTerm.addGeneralizationOf(specificTerm);
							termsToSave.add(generalTerm);
						}
					}
				}else{
					//TODO Exception type
					String message = "This term has specifing terms. Move or delete specifiing terms prior to delete or change delete configuration.";
					result.addRelatedObjects(specificTerms);
					result.setAbort();
					Exception ex = new Exception(message);
					result.addException(ex);
				}
			}

			//kind of
			DefinedTermBase generalTerm = term.getKindOf();
			if (generalTerm != null){
				if (config.isDeleteKindOfRelations()){
					generalTerm.removeGeneralization(term);
				}else{
					//TODO Exception type
					String message = "This term is kind of another term. Move or delete kind of relationship prior to delete or change delete configuration.";
					result.addRelatedObject(generalTerm);
					result.setAbort();
					Exception ex = new Exception(message);
					result.addException(ex);
					throw ex;
				}
			}

			//part of
			DefinedTermBase parentTerm = term.getPartOf();
			if (parentTerm != null){
				if (! config.isDeletePartOfRelations()){
					//TODO Exception type
					String message = "This term is included in another term. Remove from parent term prior to delete or change delete configuration.";
					result.addRelatedObject(parentTerm);
					result.setAbort();
					Exception ex = new Exception(message);
					result.addException(ex);
				}
			}			

			
			//included in
			Set<DefinedTermBase> includedTerms = term.getIncludes();
			if (includedTerms.size()> 0){
//				if (config.isDeleteIncludedTerms()){
//					for (DefinedTermBase includedTerm: includedTerms){
//						config.setCheck(true);
//						DeleteResult includedResult = this.delete(includedTerm, config);
////						config.setCheck(isCheck);
//						result.includeResult(includedResult);
//					}
//				}else 
					if (config.isDeleteIncludedRelations()){
					DefinedTermBase parent = term.getPartOf();
					for (DefinedTermBase includedTerm: includedTerms){
						term.removeIncludes(includedTerm);
						if (parent != null){
							parent.addIncludes(includedTerm);
							termsToSave.add(parent);
						}
					}
				}else{
					//TODO Exception type
					String message = "This term includes other terms. Move or delete included terms prior to delete or change delete configuration.";
					result.addRelatedObjects(includedTerms);
					result.setAbort();
					Exception ex = new Exception(message);
					result.addException(ex);
				}
			}

			//part of
			if (parentTerm != null){
				if (config.isDeletePartOfRelations()){
					parentTerm.removeIncludes(term);
					termsToSave.add(parentTerm);
				}else{
					//handelede before "included in"
				}
			}
			
//			relatedObjects;
			
			
			if (result.isOk()){
				TermVocabulary voc = term.getVocabulary();
				voc.removeTerm(term);
				//TODO save voc
				if (true /*!config.isInternal()*/){
					dao.delete(term);
					dao.saveOrUpdateAll(termsToSave);
					for (DeleteResult.PersistPair persistPair : result.getObjectsToDelete()){
						persistPair.dao.delete(persistPair.objectToPersist);
					}
					for (DeleteResult.PersistPair persistPair : result.getObjectsToSave()){
						persistPair.dao.saveOrUpdate(persistPair.objectToPersist);
					}
					
				}
			}
		} catch (Exception e) {
			result.setStatus(DeleteStatus.ERROR);
		}
		return result;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IIdentifiableEntityService#updateTitleCache(java.lang.Integer, eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy)
	 */
	@Override
	@Transactional(readOnly = false)
    public void updateTitleCache(Class<? extends DefinedTermBase> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<DefinedTermBase> cacheStrategy, IProgressMonitor monitor) {
		//TODO shouldnt this be TermBase instead of DefinedTermBase
		if (clazz == null){
			clazz = DefinedTermBase.class;
		}
		super.updateTitleCacheImpl(clazz, stepSize, cacheStrategy, monitor);
	}

}
