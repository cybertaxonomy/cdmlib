/**
* Copyright (C) 2009 EDIT
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
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.filter.VocabularyFilter;
import eu.etaxonomy.cdm.model.common.CdmClass;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.persistence.dao.description.IVocabularyFilterDao;
import eu.etaxonomy.cdm.persistence.dao.term.ITermCollectionDao;
import eu.etaxonomy.cdm.persistence.dao.term.ITermVocabularyDao;
import eu.etaxonomy.cdm.persistence.dto.TermCollectionDto;
import eu.etaxonomy.cdm.persistence.dto.TermDto;
import eu.etaxonomy.cdm.persistence.dto.TermVocabularyDto;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

@Service
@Transactional(readOnly = true)
public class VocabularyServiceImpl
        extends IdentifiableServiceBase<TermVocabulary,ITermVocabularyDao>
        implements IVocabularyService {

    @Autowired
    private ITermService termService;

    @Autowired
    private IVocabularyFilterDao vocabularyFilterDao;

    @Autowired
    private ITermCollectionDao termCollectionDao;


	@Override
    @Autowired
	protected void setDao(ITermVocabularyDao dao) {
		this.dao = dao;
	}

	@Override
	@Transactional(readOnly = false)
    public UpdateResult updateCaches(Class<? extends TermVocabulary> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<TermVocabulary> cacheStrategy, IProgressMonitor monitor) {
		if (clazz == null){
			clazz = TermVocabulary.class;
		}
		return super.updateCachesImpl(clazz, stepSize, cacheStrategy, monitor);
	}

    @Override
    public List<TermVocabulary> listByTermType(TermType termType, boolean includeSubTypes,
            Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        return dao.listByTermType(termType, includeSubTypes, limit, start, orderHints, propertyPaths);
    }

    @Override
    public List<TermVocabulary> listByTermType(Set<TermType> termTypes, boolean includeSubTypes,
            Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {

        List<TermVocabulary> result = new ArrayList<>();
        for (TermType termType : termTypes) {
            result.addAll(listByTermType(termType, includeSubTypes, limit, start, orderHints, propertyPaths));
        }
        return result;
    }

    @Override
	public <T extends DefinedTermBase> List<TermVocabulary<T>> findByTermType(TermType termType, List<String> propertyPaths) {
		return dao.findByTermType(termType, propertyPaths);
	}

    @Override
    public <T extends DefinedTermBase> List<TermVocabulary<T>> findByTermType(Set<TermType> termTypes, List<String> propertyPaths){
        if (termTypes.size() == 1) {
            return findByTermType(termTypes.iterator().next(), propertyPaths);
        }else {
            List<TermVocabulary<T>> result = new ArrayList<>();
            for (TermType termType : termTypes) {
                result.addAll(dao.findByTermType(termType, propertyPaths));
            }
            return result;
        }
    }

	/**
	 * (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITermService#getLanguageVocabulary()
	 * FIXME candidate for harmonization
	 * is this the same as getVocabulary(VocabularyEnum.Language)
	 */
	@Override
	public TermVocabulary<Language> getLanguageVocabulary() {
		String uuidString = "45ac7043-7f5e-4f37-92f2-3874aaaef2de";
		UUID uuid = UUID.fromString(uuidString);
		TermVocabulary<Language> languageVocabulary = dao.findByUuid(uuid);
		return languageVocabulary;
	}

	@Override
	public Pager<DefinedTermBase> getTerms(TermVocabulary vocabulary, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,	List<String> propertyPaths) {
        long numberOfResults = dao.countTerms(vocabulary);

		List<DefinedTermBase> results = new ArrayList<>();
		if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getTerms(vocabulary, pageSize, pageNumber,orderHints,propertyPaths);
		}

		return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
	}


    @Override
    public Collection<TermDto> getTopLevelTerms(UUID vocabularyUuid) {
        return dao.getTopLevelTerms(vocabularyUuid);
    }

    @Override
    public Collection<TermDto> getTerms(UUID vocabularyUuid) {
        return dao.getTerms(vocabularyUuid);
    }

    @Override
    public Collection<TermDto> getTerms(List<UUID> vocabularyUuids) {
        return dao.getTerms(vocabularyUuids);
    }

    @Override
    public Collection<TermDto> getNamedAreaTerms(List<UUID> vocabularyUuids) {
        return dao.getNamedAreaTerms(vocabularyUuids);
    }

    @Override
    public List<TermDto> getCompleteTermHierarchy(TermCollectionDto vocabularyDto) {
        List<TermDto> topLevelTerms = dao.getTopLevelTerms(vocabularyDto.getUuid(), vocabularyDto.getTermType());
        for (TermDto termDto : topLevelTerms) {
            termDto.setVocabularyDto(vocabularyDto);
            initializeIncludes(termDto);
            initializeGeneralizationOf(termDto);
        }

        return topLevelTerms;
    }

    private void initializeGeneralizationOf(TermDto parentTerm){
        Collection<TermDto> generalizationOf = termService.getKindOfsAsDto(parentTerm);
        parentTerm.setGeneralizationOf(generalizationOf);
        generalizationOf.forEach(generalization->{
            generalization.setVocabularyDto(parentTerm.getVocabularyDto());
            generalization.setKindOfDto(parentTerm);
            initializeGeneralizationOf(generalization);
        });
    }

    private void initializeIncludes(TermDto parentTerm){
        Collection<TermDto> includes = termService.getIncludesAsDto(parentTerm);
        parentTerm.setIncludes(includes);
        includes.forEach(include->{
            include.setVocabularyDto(parentTerm.getVocabularyDto());
            initializeIncludes(include);
            include.setPartOfDto(parentTerm);
        });
    }

    @Override
    public List<TermVocabularyDto> findVocabularyDtoByTermType(TermType termType) {
        return findVocabularyDtoByTermTypes(Collections.singleton(termType), true);
    }

    @Override
    public List<TermVocabularyDto> findVocabularyDtoByTermTypeAndPattern(String pattern, TermType termType) {
        return dao.findVocabularyDtoByTermTypes(Collections.singleton(termType), pattern, true);
    }

    @Override
    public List<TermVocabularyDto> findVocabularyDtoByTermTypes(Set<TermType> termTypes) {
        return findVocabularyDtoByTermTypes(termTypes, true);
    }

    @Override
    public List<TermVocabularyDto> findVocabularyDtoByTermType(TermType termType, boolean includeSubtypes) {
        return findVocabularyDtoByTermTypes(Collections.singleton(termType), includeSubtypes);
    }

    @Override
    public List<TermVocabularyDto> findVocabularyDtoByTermTypes(Set<TermType> termTypes, boolean includeSubtypes) {
        return dao.findVocabularyDtoByTermTypes(termTypes, includeSubtypes);
    }

    @Override
    public List<TermVocabularyDto> findFeatureVocabularyDtoByTermTypes(Set<CdmClass> availableFor) {
        return dao.findVocabularyDtoByAvailableFor(availableFor);
    }

    @Override
    public TermCollectionDto findVocabularyDtoByVocabularyUuid(UUID vocUuid) {
        return dao.findVocabularyDtoByUuid(vocUuid);
    }

    @Transactional(readOnly = false)
    @Override
    public TermDto addNewTerm(TermType termType, UUID vocabularyUUID, Language lang) {
        DefinedTermBase<?> term = termType.getEmptyDefinedTermBase(lang);
        termService.save(term);
        TermVocabulary vocabulary = dao.load(vocabularyUUID);
        vocabulary.addTerm(term);
        dao.saveOrUpdate(vocabulary);
        return TermDto.fromTerm(term, true);
    }

    @Override
    public <S extends TermVocabulary> List<UuidAndTitleCache<S>> getUuidAndTitleCache(Class<S> clazz, TermType termType,
            Integer limit, String pattern) {
        return dao.getUuidAndTitleCache(clazz, termType, limit, pattern);
    }

    @Override
    public List<TermVocabularyDto> findVocabularyDtoByVocabularyUuids(List<UUID> vocUuids) {
        return dao.findVocabularyDtoByUuids(vocUuids);
    }

    @Override
    public List<UUID> uuidList(VocabularyFilter filter){
        return vocabularyFilterDao.listUuids(filter);
    }

    @Override
    public List<Integer> idList(VocabularyFilter filter){
        return vocabularyFilterDao.idList(filter);
    }

    @Override
    public long count(VocabularyFilter filter) {
        return vocabularyFilterDao.count(filter);
    }
}