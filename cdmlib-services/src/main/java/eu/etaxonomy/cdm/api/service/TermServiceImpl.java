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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.compare.UuidAndTitleCacheComparator;
import eu.etaxonomy.cdm.api.service.UpdateResult.Status;
import eu.etaxonomy.cdm.api.service.config.DeleteConfiguratorBase;
import eu.etaxonomy.cdm.api.service.config.TermDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.exception.DataChangeNoRollbackException;
import eu.etaxonomy.cdm.api.service.exception.ReferencedObjectUndeletableException;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.LanguageStringBase;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.metadata.TermSearchField;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermCollection;
import eu.etaxonomy.cdm.model.term.TermGraphBase;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.persistence.dao.common.ILanguageStringBaseDao;
import eu.etaxonomy.cdm.persistence.dao.common.ILanguageStringDao;
import eu.etaxonomy.cdm.persistence.dao.term.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.term.IRepresentationDao;
import eu.etaxonomy.cdm.persistence.dao.term.ITermCollectionDao;
import eu.etaxonomy.cdm.persistence.dto.TermDto;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

@Service
@Transactional(readOnly = true)
public class TermServiceImpl
            extends IdentifiableServiceBase<DefinedTermBase,IDefinedTermDao>
            implements ITermService{

    @SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	private ILanguageStringDao languageStringDao;

	@Autowired
	private IVocabularyService vocabularyService;

	@Autowired
	private ITermCollectionDao termCollectionDao;

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

	@Override
    @Autowired
	protected void setDao(IDefinedTermDao dao) {
		this.dao = dao;
	}

	@Override
	public <T extends DefinedTermBase> List<T> listByTermType(TermType termType, Integer limit, Integer start,
	        List<OrderHint> orderHints, List<String> propertyPaths) {
	    return dao.listByTermType(termType, limit, start, orderHints, propertyPaths);
	}

	@Override
	public DefinedTermBase getByUri(URI uri) {
		return dao.findByUri(uri);
	}

	@Override
	public Language getLanguageByIso(String iso639) {
	    return dao.getLanguageByIso(iso639);
	}

	@Override
	public Language getLanguageByLabel(String label) {
	    return Language.getLanguageByLabel(label);
	}

	@Override
	public List<Language> getLanguagesByLocale(Enumeration<Locale> locales){
		return dao.getLanguagesByLocale(locales);
	}

	@Override
    public <TERM extends DefinedTermBase> TERM findByIdInVocabulary(String id, UUID vocabularyUuid, Class<TERM> clazz) throws IllegalArgumentException {
        List<TERM> list = dao.getDefinedTermByIdInVocabulary(id, vocabularyUuid, clazz, null, null);
		if (list.isEmpty()){
			return null;
		}else if (list.size() == 1){
			return list.get(0);
		}else{
			String message = "There is more then 1 (%d) term with the same id in vocabulary. This is forbidden. Check the state of your database.";
			throw new IllegalStateException(String.format(message, list.size()));
		}
	}


	@Override
	public NamedArea getAreaByTdwgAbbreviation(String tdwgAbbreviation) {
		if (StringUtils.isBlank(tdwgAbbreviation)){ //TDWG areas should always have a label
			return null;
		}
		List<NamedArea> list = dao.getDefinedTermByIdInVocabulary(tdwgAbbreviation, NamedArea.uuidTdwgAreaVocabulary, NamedArea.class, null, null);
		if (list.isEmpty()){
			return null;
		}else if (list.size() == 1){
			return list.get(0);
		}else{
			String message = "There is more then 1 (%d) TDWG area with the same abbreviated label. This is forbidden. Check the state of your database.";
			throw new IllegalStateException(String.format(message, list.size()));
		}
	}

	@Override
	public <T extends DefinedTermBase> Pager<T> getGeneralizationOf(T definedTerm, Integer pageSize, Integer pageNumber) {
        long numberOfResults = dao.countGeneralizationOf(definedTerm);

		List<T> results = new ArrayList<>();
		if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getGeneralizationOf(definedTerm, pageSize, pageNumber);
		}

		return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
	}

	@Override
	public <T extends DefinedTermBase> Pager<T> getIncludes(Collection<T> definedTerms, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        long numberOfResults = dao.countIncludes(definedTerms);

		List<T> results = new ArrayList<>();
		if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getIncludes(definedTerms, pageSize, pageNumber,propertyPaths);
		}

		return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
	}

	@Override
	public Pager<Media> getMedia(DefinedTermBase definedTerm, Integer pageSize,	Integer pageNumber) {
        long numberOfResults = dao.countMedia(definedTerm);

		List<Media> results = new ArrayList<>();
		if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getMedia(definedTerm, pageSize, pageNumber);
		}

		return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
	}

	@Override
	public <T extends DefinedTermBase> Pager<T> getPartOf(Set<T> definedTerms,Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        long numberOfResults = dao.countPartOf(definedTerms);

		List<T> results = new ArrayList<>();
		if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getPartOf(definedTerms, pageSize, pageNumber, propertyPaths);
		}

		return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
	}

	@Override
	public Pager<NamedArea> list(NamedAreaLevel level, NamedAreaType type, Integer pageSize, Integer pageNumber,
			List<OrderHint> orderHints, List<String> propertyPaths) {
		long numberOfResults = dao.count(level, type);

		List<NamedArea> results = new ArrayList<>();
		if (numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.list(level, type, pageSize, pageNumber, orderHints, propertyPaths);
		}

		return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
	}

	@Override
	public <T extends DefinedTermBase> Pager<T> findByRepresentationText(String label, Class<T> clazz, Integer pageSize, Integer pageNumber) {
        long numberOfResults = dao.countDefinedTermByRepresentationText(label,clazz);

		List<T> results = new ArrayList<>();
		if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getDefinedTermByRepresentationText(label, clazz, pageSize, pageNumber);
		}

		return new DefaultPagerImpl<T>(pageNumber, numberOfResults, pageSize, results);
	}

	@Override
	public <T extends DefinedTermBase> Pager<T> findByRepresentationAbbreviation(String abbrev, Class<T> clazz, Integer pageSize, Integer pageNumber) {
        long numberOfResults = dao.countDefinedTermByRepresentationAbbrev(abbrev,clazz);

		List<T> results = new ArrayList<>();
		if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getDefinedTermByRepresentationAbbrev(abbrev, clazz, pageSize, pageNumber);
		}

		return new DefaultPagerImpl<T>(pageNumber, numberOfResults, pageSize, results);
	}

	@Override
	public List<LanguageString> getAllLanguageStrings(int limit, int start) {
		return languageStringDao.list(limit, start);
	}

	@Override
	public List<Representation> getAllRepresentations(int limit, int start) {
		return representationDao.list(limit,start);
	}

	@Override
	public UUID saveLanguageData(LanguageStringBase languageData) {
		return languageStringBaseDao.save(languageData).getUuid();
	}


	/** @deprecated use {@link #delete(DefinedTermBase, TermDeletionConfigurator)} instead
	 * to allow DeleteResult return type*/
	@Override
	@Deprecated
	public DeleteResult delete(DefinedTermBase term){
		DeleteResult result = new DeleteResult();

		TermDeletionConfigurator defaultConfig = new TermDeletionConfigurator();
		result = delete(term, defaultConfig);
		return result;
	}

	@Override
	@Deprecated
	@Transactional(readOnly = false)
	public DeleteResult delete(UUID termUuid){
	    DeleteResult result = new DeleteResult();

	    TermDeletionConfigurator defaultConfig = new TermDeletionConfigurator();
	    result = delete(dao.load(termUuid), defaultConfig);
	    return result;
	}

	@Override
	public DeleteResult delete(DefinedTermBase term, TermDeletionConfigurator config){
		if (config == null){
			config = new TermDeletionConfigurator();
		}
		Set<DefinedTermBase> termsToSave = new HashSet<>();

		DeleteResult result = isDeletable(term.getUuid(), config);
		if (result.isAbort()) {
            return result;
        }
		//CdmBase.deproxy(dao.merge(term), DefinedTermBase.class);

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
					Exception ex = new DataChangeNoRollbackException(message);
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
					DataChangeNoRollbackException ex = new DataChangeNoRollbackException(message);
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
					DataChangeNoRollbackException ex = new DataChangeNoRollbackException(message);
					result.addException(ex);
				}
			}

			//included in
			Set<DefinedTermBase<?>> includedTerms = term.getIncludes();
			if (includedTerms.size()> 0){
			    if (config.isDeleteIncludedRelations()){
			        DefinedTermBase parent = term.getPartOf();
			        for (DefinedTermBase<?> includedTerm: includedTerms){
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
			        Exception ex = new DataChangeNoRollbackException(message);
			        result.addException(ex);
			    }
			}

			//part of
			if (parentTerm != null){
			    if (config.isDeletePartOfRelations()){
			        parentTerm.removeIncludes(term);
			        termsToSave.add(parentTerm);
			    }else{
			        //handled before "included in"
			    }
			}

			if (result.isOk()){
				TermVocabulary voc = term.getVocabulary();
				if (voc!= null){
					voc.removeTerm(term);
				}
				//TODO save voc
				if (true){
					dao.delete(term);
					result.addDeletedObject(term);
					dao.saveOrUpdateAll(termsToSave);
				}
			}
		} catch (DataChangeNoRollbackException e) {
			result.setStatus(Status.ERROR);
		}
		return result;
	}

	@Override
	@Transactional(readOnly = false)
	public DeleteResult delete(UUID termUuid, TermDeletionConfigurator config){


	    return delete(dao.load(termUuid), config);
	}

	@Override
	@Transactional(readOnly = false)
	public DeleteResult delete(List<UUID> termUuids, TermDeletionConfigurator config){
	    return deleteTerms(load(termUuids, null), config);
	}

	@Override
	@Transactional(readOnly = false)
    public UpdateResult updateCaches(Class<? extends DefinedTermBase> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<DefinedTermBase> cacheStrategy, IProgressMonitor monitor) {
		//TODO shouldn't this be TermBase instead of DefinedTermBase
		if (clazz == null){
			clazz = DefinedTermBase.class;
		}
		return super.updateCachesImpl(clazz, stepSize, cacheStrategy, monitor);
	}

	@Override
    public DeleteResult isDeletable(UUID termUuid, DeleteConfiguratorBase config){
	    TermDeletionConfigurator termConfig = null;
	    if(config instanceof TermDeletionConfigurator){
	        termConfig = (TermDeletionConfigurator) config;
	    }
	    DeleteResult result = new DeleteResult();
	    DefinedTermBase term = load(termUuid);
        Set<CdmBase> references = commonService.getReferencingObjectsForDeletion(term);

	    if(termConfig!=null){
	        //generalization of
	        Set<DefinedTermBase> specificTerms = term.getGeneralizationOf();
	        if (!specificTerms.isEmpty() && termConfig.isDeleteGeneralizationOfRelations()){
	            references.removeAll(specificTerms);
	        }
	        //kind of
	        DefinedTermBase generalTerm = term.getKindOf();
	        if (generalTerm != null && termConfig.isDeleteKindOfRelations()){
	            references.remove(generalTerm);
	        }
	        //part of
	        DefinedTermBase parentTerm = term.getPartOf();
	        if (parentTerm != null && termConfig.isDeletePartOfRelations()){
	            references.remove(parentTerm);
	        }
	        //included in
	        Set<DefinedTermBase> includedTerms = term.getIncludes();
	        if (!includedTerms.isEmpty() && termConfig.isDeleteIncludedRelations()){
	            references.removeAll(includedTerms);
	        }
	    }

	    //gather remaining referenced objects
        for (CdmBase relatedObject : references) {
            if(relatedObject instanceof TermVocabulary){
                continue;
            }
            result.getRelatedObjects().add(relatedObject);
            String message = "An object of " + relatedObject.getClass().getName() + " with ID " + relatedObject.getId() + " is referencing the object" ;
            result.addException(new ReferencedObjectUndeletableException(message));
            result.setAbort();
        }
        return result;
    }

    @Override
    @Transactional(readOnly = false)
    public Map<UUID, Representation> saveOrUpdateRepresentations(Collection<Representation> representations){
        return representationDao.saveOrUpdateAll(representations);
    }

    @Override
    @Transactional(readOnly = true)
    public <S extends DefinedTermBase> List<UuidAndTitleCache<S>> getUuidAndTitleCache(Class<S> clazz,
            List<? extends TermCollection> termCollections,
            Integer limit, String pattern, Language lang, TermSearchField type) {

        List<S> terms = new ArrayList<>();
        type = type == null? TermSearchField.NoAbbrev : type;
        clazz = clazz == null? clazz = (Class)DefinedTermBase.class : clazz;
        @SuppressWarnings("rawtypes")
        List<TermVocabulary> vocs = filterCollectionType(TermVocabulary.class, termCollections);
        if (!vocs.isEmpty() || CdmUtils.isNullSafeEmpty(termCollections)) { //search on all vocabularies if no filter is set
            terms = dao.listByAbbrev(clazz, vocs, limit, pattern, type);  //TODO lang still missing;
        }

        @SuppressWarnings("rawtypes")
        List<TermGraphBase> graphs = filterCollectionType(TermGraphBase.class, termCollections);
        List<S> graphTerms = termCollectionDao.listTerms(clazz, graphs, limit, pattern, type, lang);
        for (S graphArea : graphTerms) {
            if (!terms.contains(graphArea)) {
                terms.add(graphArea);
            }
        }

        List<UuidAndTitleCache<S>> result = new ArrayList<>();
        UuidAndTitleCache<S> uuidAndTitleCache;
        for (S term: terms){
            term = CdmBase.deproxy(term);
            String display = term instanceof NamedArea ?
                    NamedArea.labelWithLevel((NamedArea)term, lang): term.getTitleCache();
            if (!type.equals(TermSearchField.NoAbbrev)){
                if (type.equals(TermSearchField.IDInVocabulary)){
                    display += " - " + term.getIdInVocabulary();
                }else if (type.equals(TermSearchField.Symbol1)){
                    display += " - " + term.getSymbol();
                }else if (type.equals(TermSearchField.Symbol2)){
                    display += " - " + term.getSymbol2();
                }
            }
            uuidAndTitleCache = new UuidAndTitleCache<>(term.getUuid(), term.getId(), display);
            result.add(uuidAndTitleCache);
        }
        Collections.sort(result, new UuidAndTitleCacheComparator<>(true));

        if (limit != null && result.size() > limit) {
            result = result.subList(0, limit);
        }
        return result;
    }

    private <COLL extends TermCollection> List<COLL> filterCollectionType(Class<COLL> clazz,
            List<? extends TermCollection> termCollections) {

        List<COLL> result = new ArrayList<>();
        for (TermCollection<?,?> collection : termCollections) {
            if (collection.isInstanceOf(clazz)) {
                result.add((COLL)collection);
            }
        }
        return result;
    }

    @Override
    public Collection<TermDto> getIncludesAsDto(
            TermDto parentTerm) {
        return dao.getIncludesAsDto(parentTerm);
    }

    @Override
    public Collection<TermDto> getKindOfsAsDto(
            TermDto parentTerm) {
        return dao.getKindOfsAsDto(parentTerm);
    }

    @Transactional(readOnly = false)
    @Override
    public UpdateResult moveTerm(TermDto termDto, UUID parentUUID) {
        return moveTerm(termDto, parentUUID, TermMovePosition.ON);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Transactional(readOnly = false)
    @Override
    public UpdateResult moveTerm(TermDto termDto, UUID parentUuid, TermMovePosition termMovePosition) {
        boolean isKindOf = termDto.getKindOfUuid()!=null && termDto.getKindOfUuid().equals(parentUuid);
        TermVocabulary vocabulary = HibernateProxyHelper.deproxy(vocabularyService.load(termDto.getVocabularyUuid()));
        DefinedTermBase parent = HibernateProxyHelper.deproxy(dao.load(parentUuid));
        UpdateResult result = new UpdateResult();
        if(parent == null){
            //new parent is a vocabulary
            TermVocabulary parentVocabulary = HibernateProxyHelper.deproxy(vocabularyService.load(parentUuid));
            DefinedTermBase term = HibernateProxyHelper.deproxy(dao.load(termDto.getUuid()));
            if(parentVocabulary!=null){
                term.setKindOf(null);
                term.setPartOf(null);

                vocabulary.removeTerm(term);
                parentVocabulary.addTerm(term);
                result.addUpdatedObject(term);
                result.addUpdatedObject(vocabulary);
                result.addUpdatedObject(parentVocabulary);
            }
            vocabularyService.saveOrUpdate(parentVocabulary);
        }
        else {
            DefinedTermBase term = HibernateProxyHelper.deproxy(dao.load(termDto.getUuid()));
            //new parent is a term
            if(parent.isOrderRelevant()
                    && term.isOrderRelevant()
                    && termMovePosition!=null
                    && parent.getVocabulary().isInstanceOf(OrderedTermVocabulary.class)) {
                //new parent is an ordered term
                OrderedTermVocabulary otVoc = HibernateProxyHelper.deproxy(parent.getVocabulary(), OrderedTermVocabulary.class);
                if(termMovePosition.equals(TermMovePosition.BEFORE)) {
                    term.getVocabulary().removeTerm(term);
                    otVoc.addTermAbove(term, parent);
                    if (parent.getPartOf() != null){
                        parent.getPartOf().addIncludes(term);
                    }
                }
                else if(termMovePosition.equals(TermMovePosition.AFTER)) {
                    term.getVocabulary().removeTerm(term);
                    otVoc.addTermBelow(term, parent);
                    if (parent.getPartOf() != null){
                        parent.getPartOf().addIncludes(term);
                    }
                }
                else if(termMovePosition.equals(TermMovePosition.ON)) {
                    term.getVocabulary().removeTerm(term);
                    parent.addIncludes(term);
                    parent.getVocabulary().addTerm(term);
                }
            }
            else{
                vocabulary.removeTerm(term);
                if(isKindOf){
                    parent.addGeneralizationOf(term);
                }
                else{
                    parent.addIncludes(term);
                }
                parent.getVocabulary().addTerm(term);
            }
            result.addUpdatedObject(term);
            result.addUpdatedObject(parent);
            result.addUpdatedObject(vocabulary);
            vocabularyService.saveOrUpdate(parent.getVocabulary());
        }
        return result;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Transactional(readOnly = false)
    @Override
    public TermDto addNewTerm(TermType termType, UUID parentUUID, boolean isKindOf, Language lang) {
        DefinedTermBase term = termType.getEmptyDefinedTermBase(lang);
        dao.save(term);
        DefinedTermBase parent = dao.load(parentUUID);
        if(isKindOf){
            parent.addGeneralizationOf(term);
        }
        else{
            parent.addIncludes(term);
        }
        parent.getVocabulary().addTerm(term);
        dao.saveOrUpdate(parent);
        return TermDto.fromTerm(term, true);
    }

    @Override
    public Collection<TermDto> findByTitleAsDtoWithVocDto(String title, TermType termType){
        return dao.findByTitleAsDtoWithVocDto(title, termType);
    }

    @Override
    public Collection<TermDto> findByUriAsDto(URI uri, String termLabel, TermType termType){
        return dao.findByUriAsDto(uri, termLabel, termType);
    }

    @Override
    public Collection<TermDto> findByUUIDsAsDto(List<UUID> uuidList){
        return dao.findByUUIDsAsDto(uuidList);
    }

    @Override
    public Collection<TermDto> findFeatureByUUIDsAsDto(List<UUID> uuidList){
        return dao.findFeatureByUUIDsAsDto(uuidList);
    }

    @Override
    public Map<UUID, TermDto> findFeatureByUUIDsAsDtos(List<UUID> uuidList){
        if (uuidList == null || uuidList.isEmpty()){
            return null;
        }
        Collection<TermDto> col = dao.findFeatureByUUIDsAsDto(uuidList);
        Map<UUID, TermDto> result = new HashMap<>();
        if (col != null){
            for (TermDto dto: col){
                result.put(dto.getUuid(), dto);
            }
        }
        return result;
    }


    @Override
    public Collection<TermDto> findFeatureByTitleAsDto(String title){
        return dao.findFeatureByTitleAsDto(title);
    }

    @Override
    public Country getCountryByIso(String iso639) {
        return this.dao.getCountryByIso(iso639);
    }

    @Override
    public List<Country> getCountryByName(String name) {
        List<? extends DefinedTermBase> terms = this.dao.findByTitleWithRestrictions(Country.class, name, null, null, null, null, null, null);
        List<Country> countries = new ArrayList<>();
        for (int i = 0; i < terms.size(); i++) {
            countries.add((Country) terms.get(i));
        }
        return countries;
    }


    public enum TermMovePosition{
        BEFORE,
        AFTER,
        ON
    }


	@Override
	public DeleteResult deleteTerms(List<DefinedTermBase> terms, TermDeletionConfigurator config) {
		DeleteResult result = new DeleteResult();
		for (DefinedTermBase term: terms) {
			result.includeResult(delete(term, config));
		}
		return result;
	}

}
