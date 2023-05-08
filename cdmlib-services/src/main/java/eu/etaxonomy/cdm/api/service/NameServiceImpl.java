/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.sandbox.queries.FuzzyLikeThisQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.hibernate.criterion.Criterion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.config.DeleteConfiguratorBase;
import eu.etaxonomy.cdm.api.service.config.NameDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.dto.TypeDesignationStatusFilter;
import eu.etaxonomy.cdm.api.service.exception.ReferencedObjectUndeletableException;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.AbstractPagerImpl;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.api.service.search.DocumentSearchResult;
import eu.etaxonomy.cdm.api.service.search.ILuceneIndexToolProvider;
import eu.etaxonomy.cdm.api.service.search.ISearchResultBuilder;
import eu.etaxonomy.cdm.api.service.search.LuceneParseException;
import eu.etaxonomy.cdm.api.service.search.LuceneSearch;
import eu.etaxonomy.cdm.api.service.search.QueryFactory;
import eu.etaxonomy.cdm.api.service.search.SearchResult;
import eu.etaxonomy.cdm.api.service.search.SearchResultBuilder;
import eu.etaxonomy.cdm.api.util.TaxonNamePartsFilter;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.CdmBaseType;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.RelationshipBase.Direction;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalSource;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;
import eu.etaxonomy.cdm.persistence.dao.common.Restriction;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.dao.name.IHomotypicalGroupDao;
import eu.etaxonomy.cdm.persistence.dao.name.INomenclaturalStatusDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITypeDesignationDao;
import eu.etaxonomy.cdm.persistence.dao.reference.IOriginalSourceDao;
import eu.etaxonomy.cdm.persistence.dto.TaxonNameParts;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.strategy.match.IMatchStrategy;
import eu.etaxonomy.cdm.strategy.match.IMatchable;
import eu.etaxonomy.cdm.strategy.match.IParsedMatchStrategy;
import eu.etaxonomy.cdm.strategy.match.MatchException;
import eu.etaxonomy.cdm.strategy.match.MatchStrategyFactory;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

@Service
@Transactional(readOnly = true)
public class NameServiceImpl
          extends IdentifiableServiceBase<TaxonName,ITaxonNameDao>
          implements INameService {

    static private final Logger logger = LogManager.getLogger();

    @Autowired
    private IOccurrenceService occurrenceService;
    @Autowired
    private ICollectionService collectionService;
    @Autowired
    private ITaxonService taxonService;
    @Autowired
    private ICommonService commonService;
    @Autowired
    private INomenclaturalStatusDao nomStatusDao;
    @Autowired
    private ITypeDesignationDao typeDesignationDao;
    @Autowired
    private IHomotypicalGroupDao homotypicalGroupDao;
    @Autowired
    private ICdmGenericDao genericDao;
    @Autowired
    private ILuceneIndexToolProvider luceneIndexToolProvider;
    @Autowired
    private IOriginalSourceDao sourcedDao;

    @Autowired
    // @Qualifier("defaultBeanInitializer")
    protected IBeanInitializer defaultBeanInitializer;

//***************************** CONSTRUCTOR **********************************/

    /**
     * Constructor
     */
    public NameServiceImpl(){}

//********************* METHODS ***********************************************//

    @Override
    @Transactional(readOnly = false)
    public DeleteResult delete(UUID nameUUID){
        NameDeletionConfigurator config = new NameDeletionConfigurator();
        DeleteResult result = delete(nameUUID, config);
        return result;
    }

    @Override
    public DeleteResult delete(TaxonName name){
        return delete(name.getUuid());
    }

    @Override
    @Transactional(readOnly = false)
    public DeleteResult delete(TaxonName name, NameDeletionConfigurator config) {
        DeleteResult result = new DeleteResult();

        if (name == null){
            result.setAbort();
            return result;
        }

        try{
            result = this.isDeletable(name, config, null);
        }catch(Exception e){
            result.addException(e);
            result.setError();
            return result;
        }
        if (result.isOk()){
        //remove references to this name
            removeNameRelationshipsByDeleteConfig(name, config);

           //remove name from homotypical group
            HomotypicalGroup homotypicalGroup = name.getHomotypicalGroup();
            if (homotypicalGroup != null){
                homotypicalGroup.removeTypifiedName(name, false);
            }
            //all type designation relationships are removed as they belong to the name
            deleteTypeDesignation(name, null);
            //if original spellings should be deleted, remove it from the nomenclatural source
            Set<TaxonName> namesToUpdate = new HashSet<>();
            for (Object o: result.getRelatedObjects()){
                if (o instanceof NomenclaturalSource && ((NomenclaturalSource)o).getNameUsedInSource() != null && ((NomenclaturalSource)o).getNameUsedInSource().equals(name)){
                    NomenclaturalSource nomSource = (NomenclaturalSource)o;
                    nomSource.setNameUsedInSource(null);
                    namesToUpdate.add(nomSource.getSourcedName());
                }
            }

            try{
                if (!namesToUpdate.isEmpty()){
                    Map<UUID, TaxonName> updatedNames = dao.saveOrUpdateAll(namesToUpdate);
                    Set<TaxonName> names = new HashSet<>(updatedNames.values());
                    result.addUpdatedObjects(names);
                }
                dao.delete(name);
                result.addDeletedObject(name);

            }catch(Exception e){
                result.addException(e);
                result.setError();
            }
            return result;
        }

        return result;
    }

    @Override
    @Transactional(readOnly = false)
    public DeleteResult delete(UUID nameUUID, NameDeletionConfigurator config) {

        TaxonName name = dao.load(nameUUID);
        return delete(name, config);
    }

    @Override
    @Transactional(readOnly = false)
    public UpdateResult cloneTypeDesignation(UUID nameUuid, SpecimenTypeDesignation baseDesignation,
            String accessionNumber, String barcode, String catalogNumber,
            UUID collectionUuid, SpecimenTypeDesignationStatus typeStatus, URI preferredStableUri){
        UpdateResult result = new UpdateResult();

        DerivedUnit baseSpecimen = HibernateProxyHelper.deproxy(occurrenceService.load(baseDesignation.getTypeSpecimen().getUuid(), Arrays.asList("collection")), DerivedUnit.class);
        DerivedUnit duplicate = DerivedUnit.NewInstance(baseSpecimen.getRecordBasis());
        DerivationEvent derivedFrom = baseSpecimen.getDerivedFrom();
        Collection<FieldUnit> fieldUnits = occurrenceService.findFieldUnits(baseSpecimen.getUuid(), null);
        if(fieldUnits.size()!=1){
            result.addException(new Exception("More than one or no field unit found for specimen"));
            result.setError();
            return result;
        }
        for (SpecimenOrObservationBase<?> original : derivedFrom.getOriginals()) {
            DerivationEvent.NewSimpleInstance(original, duplicate, derivedFrom.getType());
        }
        duplicate.setAccessionNumber(accessionNumber);
        duplicate.setBarcode(barcode);
        duplicate.setCatalogNumber(catalogNumber);
        duplicate.setCollection(collectionService.load(collectionUuid));
        SpecimenTypeDesignation typeDesignation = SpecimenTypeDesignation.NewInstance();
        typeDesignation.setTypeSpecimen(duplicate);
        typeDesignation.setTypeStatus(typeStatus);
        typeDesignation.getTypeSpecimen().setPreferredStableUri(preferredStableUri);

        TaxonName name = load(nameUuid);
        name.getTypeDesignations().add(typeDesignation);

        result.setCdmEntity(typeDesignation);
        result.addUpdatedObject(name);
        return result;
    }

    @Override
    @Transactional
    public DeleteResult deleteTypeDesignation(TaxonName name, TypeDesignationBase<?> typeDesignation){
    	if(typeDesignation != null && typeDesignation .isPersited()){
    		typeDesignation = HibernateProxyHelper.deproxy(typeDesignationDao.load(typeDesignation.getUuid()));
    	}

        DeleteResult result = new DeleteResult();
        if (name == null && typeDesignation == null){
            result.setError();
            return result;
        }else if (name != null && typeDesignation != null){
            removeSingleDesignation(name, typeDesignation);
        }else if (name != null){
            @SuppressWarnings("rawtypes")
            Set<TypeDesignationBase> designationSet = new HashSet<>(name.getTypeDesignations());
            for (TypeDesignationBase<?> desig : designationSet){
                desig = CdmBase.deproxy(desig);
                removeSingleDesignation(name, desig);
            }
        }else if (typeDesignation != null){
            Set<TaxonName> nameSet = new HashSet<>(typeDesignation.getTypifiedNames());
            for (TaxonName singleName : nameSet){
                singleName = CdmBase.deproxy(singleName);
                removeSingleDesignation(singleName, typeDesignation);
            }
        }
        result.addDeletedObject(typeDesignation);
        result.addUpdatedObject(name);
        return result;
    }


    @Override
    @Transactional(readOnly = false)
    public DeleteResult deleteTypeDesignation(UUID nameUuid, UUID typeDesignationUuid){
        TaxonName nameBase = load(nameUuid);
        TypeDesignationBase<?> typeDesignation = HibernateProxyHelper.deproxy(typeDesignationDao.load(typeDesignationUuid));
        return deleteTypeDesignation(nameBase, typeDesignation);
    }

    @Transactional
    private void removeSingleDesignation(TaxonName name, TypeDesignationBase<?> typeDesignation) {

        name.removeTypeDesignation(typeDesignation);
        if (typeDesignation.getTypifiedNames().isEmpty()){
            typeDesignation.removeType();
            if (!typeDesignation.getRegistrations().isEmpty()){
                for(Object reg: typeDesignation.getRegistrations()){
                    if (reg instanceof Registration){
                        ((Registration)reg).removeTypeDesignation(typeDesignation);
                    }
                }
            }

            typeDesignationDao.delete(typeDesignation);

        }
    }



    /**
     * @param name
     * @param config
     */
    private void removeNameRelationshipsByDeleteConfig(TaxonName name, NameDeletionConfigurator config) {
        try {
            if (config.isRemoveAllNameRelationships()){
                Set<NameRelationship> rels = getModifiableSet(name.getNameRelations());
                for (NameRelationship rel : rels){
                    name.removeNameRelationship(rel);
                }
            }else{
                //relations to this name
                Set<NameRelationship> rels = getModifiableSet(name.getRelationsToThisName());
                for (NameRelationship rel : rels){
                    if (config.isIgnoreHasBasionym() && NameRelationshipType.BASIONYM().equals(rel.getType() )){
                            name.removeNameRelationship(rel);
                    }else if (config.isIgnoreHasReplacedSynonym() && NameRelationshipType.REPLACED_SYNONYM().equals(rel.getType())){
                        name.removeNameRelationship(rel);
                    }
                }
                //relations from this name
                rels = getModifiableSet(name.getRelationsFromThisName());
                for (NameRelationship rel : rels){
                    if (config.isIgnoreIsBasionymFor() && NameRelationshipType.BASIONYM().equals(rel.getType())  ){
                        name.removeNameRelationship(rel);
                    }else if (config.isIgnoreIsReplacedSynonymFor() && NameRelationshipType.REPLACED_SYNONYM().equals(rel.getType())){
                        name.removeNameRelationship(rel);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Set<NameRelationship> getModifiableSet(Set<NameRelationship> relations) {
        Set<NameRelationship> rels = new HashSet<NameRelationship>();
        for (NameRelationship rel : relations){
            rels.add(rel);
        }
        return rels;
    }

//********************* METHODS ****************************************************************//

    /**
     * TODO candidate for harmonization
     * new name findByName
     */
    @Override
    @Deprecated
    public List<TaxonName> getNamesByNameCache(String nameCache){
        boolean includeAuthors = false;
        List<TaxonName> result = dao.findByName(includeAuthors, nameCache, MatchMode.EXACT, null, null, null, null);
        return result;
    }

    /**
     * TODO candidate for harmonization
     * new name saveHomotypicalGroups
     *
     * findByTitle
     */
    @Override
    @Deprecated
    public List<TaxonName> findNamesByTitleCache(String titleCache, MatchMode matchMode, List<String> propertyPaths){
        List<TaxonName> result = dao.findByTitle(titleCache, matchMode, null, null, null, propertyPaths);
        return result;
    }

    /**
     * TODO candidate for harmonization
     * new name saveHomotypicalGroups
     *
     * findByTitle
     */
    @Override
    @Deprecated
    public List<TaxonName> findNamesByNameCache(String nameCache, MatchMode matchMode, List<String> propertyPaths){
        List<TaxonName> result = dao.findByName(false, nameCache, matchMode, null, null, null , propertyPaths);
        return result;
    }

    @Override
    public Pager<TaxonNameParts> findTaxonNameParts(Optional<String> genusOrUninomial,
            Optional<String> infraGenericEpithet, Optional<String> specificEpithet,
            Optional<String> infraSpecificEpithet, Rank rank, Set<UUID> excludedNamesUuids,
            Integer pageSize, Integer pageIndex, List<OrderHint> orderHints) {


        long count = dao.countTaxonNameParts(genusOrUninomial, infraGenericEpithet, specificEpithet, infraGenericEpithet, rank, excludedNamesUuids);

        List<TaxonNameParts> results;
        if(AbstractPagerImpl.hasResultsInRange(count, pageIndex, pageSize)){
            results = dao.findTaxonNameParts(genusOrUninomial, infraGenericEpithet, specificEpithet, infraSpecificEpithet,
                    rank, excludedNamesUuids,
                    pageSize, pageIndex, orderHints);
        } else {
            results = new ArrayList<>();
        }

        return new DefaultPagerImpl<TaxonNameParts>(pageIndex, count, pageSize, results);
    }

    @Override
    public Pager<TaxonNameParts> findTaxonNameParts(TaxonNamePartsFilter filter, String namePartQueryString,
            Integer pageSize, Integer pageIndex, List<OrderHint> orderHints) {

        return findTaxonNameParts(
                filter.uninomialQueryString(namePartQueryString),
                filter.infraGenericEpithet(namePartQueryString),
                filter.specificEpithet(namePartQueryString),
                filter.infraspecificEpithet(namePartQueryString),
                filter.getRank(),
                filter.getExludedNamesUuids(),
                pageSize, pageIndex, orderHints);
    }

    /**
     * TODO candidate for harmonization
     * new name saveHomotypicalGroups
     */
    @Override
    @Transactional(readOnly = false)
    public Map<UUID, HomotypicalGroup> saveAllHomotypicalGroups(Collection<HomotypicalGroup> homotypicalGroups){
        return homotypicalGroupDao.saveAll(homotypicalGroups);
    }

    /**
     * TODO candidate for harmonization
     * new name saveTypeDesignations
     */
    @Override
    @Transactional(readOnly = false)
    public Map<UUID, TypeDesignationBase<?>> saveTypeDesignationAll(Collection<TypeDesignationBase<?>> typeDesignationCollection){
        return typeDesignationDao.saveAll(typeDesignationCollection);
    }

    /**
     * TODO candidate for harmonization
     * new name getNomenclaturalStatus
     */
    @Override
    public List<NomenclaturalStatus> getAllNomenclaturalStatus(int limit, int start){
        return nomStatusDao.list(limit, start);
    }

    @Override
    public NomenclaturalStatus loadNomenclaturalStatus(UUID uuid,  List<String> propertyPaths){
        return nomStatusDao.load(uuid, propertyPaths);
    }

    /**
     * TODO candidate for harmonization
     * new name getTypeDesignations
     */
    @Override
    public List<TypeDesignationBase<?>> getAllTypeDesignations(int limit, int start){
        return typeDesignationDao.getAllTypeDesignations(limit, start);
    }

    @Override
    public TypeDesignationBase<?> loadTypeDesignation(int id, List<String> propertyPaths){
        return typeDesignationDao.load(id, propertyPaths);
    }

    @Override
    public TypeDesignationBase<?> loadTypeDesignation(UUID uuid, List<String> propertyPaths){
        return typeDesignationDao.load(uuid, propertyPaths);
    }

    @Override
    public List<TypeDesignationBase<?>> loadTypeDesignations(List<UUID> uuids, List<String> propertyPaths){
    	if(uuids == null) {
            return null;
        }

        List<TypeDesignationBase<?>> entities = new ArrayList<>();
        for(UUID uuid : uuids) {
            entities.add(uuid == null ? null : typeDesignationDao.load(uuid, propertyPaths));
        }
        return entities;
    }

    /**
     * FIXME Candidate for harmonization
     * homotypicalGroupService.list
     */
    @Override
    public List<HomotypicalGroup> getAllHomotypicalGroups(int limit, int start){
        return homotypicalGroupDao.list(limit, start);
    }


    @Override
    public List<NomenclaturalSource> listOriginalSpellings(Integer pageSize, Integer pageNumber,
            List<OrderHint> orderHints, List<String> propertyPaths) {

        Long numberOfResults = sourcedDao.countWithNameUsedInSource(NomenclaturalSource.class);
        List<NomenclaturalSource> results = new ArrayList<>();
        if(numberOfResults > 0) {
            results = sourcedDao.listWithNameUsedInSource(NomenclaturalSource.class, pageSize, pageNumber, orderHints, propertyPaths);
        }
        return results;
    }

    @Override
    public List<NameRelationship> listNameRelationships(Set<NameRelationshipType> types,
            Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {

        Long numberOfResults = dao.countNameRelationships(types);
        List<NameRelationship> results = new ArrayList<>();
        if(numberOfResults > 0) {
            results = dao.getNameRelationships(types, pageSize, pageNumber, orderHints, propertyPaths);
        }
        return results;
    }

    @Override
    public List<HybridRelationship> listHybridRelationships(Set<HybridRelationshipType> types,
            Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {

        Long numberOfResults = dao.countHybridRelationships(types);
        List<HybridRelationship> results = new ArrayList<>();
        if(numberOfResults > 0) {
            results = dao.getHybridRelationships(types, pageSize, pageNumber, orderHints, propertyPaths);
        }
        return results;
    }


    @Override
    @Autowired
    protected void setDao(ITaxonNameDao dao) {
        this.dao = dao;
    }

    @Override
    public Pager<HybridRelationship> getHybridNames(INonViralName name,	HybridRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.countHybridNames(name, type);

        List<HybridRelationship> results = new ArrayList<HybridRelationship>();
        if(AbstractPagerImpl.hasResultsInRange(numberOfResults.longValue(), pageNumber, pageSize)) { // no point checking again
            results = dao.getHybridNames(name, type, pageSize, pageNumber,orderHints,propertyPaths);
        }

        return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
    }

    @Override
    public List<NameRelationship> listNameRelationships(TaxonName name,	Direction direction, NameRelationshipType type, Integer pageSize,
            Integer pageNumber, List<OrderHint> orderHints,	List<String> propertyPaths) {

        Integer numberOfResults = dao.countNameRelationships(name, direction, type);

        List<NameRelationship> results = new ArrayList<NameRelationship>();
        if (AbstractPagerImpl.hasResultsInRange(numberOfResults.longValue(), pageNumber, pageSize)) { // no point checking again
            results = dao.getNameRelationships(name, direction, type, pageSize,	pageNumber, orderHints, propertyPaths);
        }
        return results;
    }


    protected LuceneSearch prepareFindByFuzzyNameSearch(Class<? extends CdmBase> clazz,
            INonViralName nvn,
            float accuracy,
            int maxNoOfResults,
            List<Language> languages,
            boolean highlightFragments) {

        String similarity = Float.toString(accuracy);
        String searchSuffix = "~" + similarity;

        Builder finalQueryBuilder = new Builder();
        finalQueryBuilder.setDisableCoord(false);
        Builder textQueryBuilder = new Builder();
        textQueryBuilder.setDisableCoord(false);

        LuceneSearch luceneSearch = new LuceneSearch(luceneIndexToolProvider, TaxonName.class);
        QueryFactory queryFactory = luceneIndexToolProvider.newQueryFactoryFor(TaxonName.class);

//    	SortField[] sortFields = new  SortField[]{SortField.FIELD_SCORE, new SortField("titleCache__sort", SortField.STRING,  false)};
//    	luceneSearch.setSortFields(sortFields);

        // ---- search criteria
        luceneSearch.setCdmTypRestriction(clazz);

        FuzzyLikeThisQuery fltq = new FuzzyLikeThisQuery(maxNoOfResults, luceneSearch.getAnalyzer());
        if(nvn.getGenusOrUninomial() != null && !nvn.getGenusOrUninomial().equals("")) {
            fltq.addTerms(nvn.getGenusOrUninomial().toLowerCase(), "genusOrUninomial", accuracy, 3);
        } else {
            //textQuery.add(new RegexQuery (new Term ("genusOrUninomial", "^[a-zA-Z]*")), Occur.MUST_NOT);
            textQueryBuilder.add(queryFactory.newTermQuery("genusOrUninomial", "_null_", false), Occur.MUST);
        }

        if(nvn.getInfraGenericEpithet() != null && !nvn.getInfraGenericEpithet().equals("")){
            fltq.addTerms(nvn.getInfraGenericEpithet().toLowerCase(), "infraGenericEpithet", accuracy, 3);
        } else {
            //textQuery.add(new RegexQuery (new Term ("infraGenericEpithet", "^[a-zA-Z]*")), Occur.MUST_NOT);
            textQueryBuilder.add(queryFactory.newTermQuery("infraGenericEpithet", "_null_", false), Occur.MUST);
        }

        if(nvn.getSpecificEpithet() != null && !nvn.getSpecificEpithet().equals("")){
            fltq.addTerms(nvn.getSpecificEpithet().toLowerCase(), "specificEpithet", accuracy, 3);
        } else {
            //textQuery.add(new RegexQuery (new Term ("specificEpithet", "^[a-zA-Z]*")), Occur.MUST_NOT);
            textQueryBuilder.add(queryFactory.newTermQuery("specificEpithet", "_null_", false), Occur.MUST);
        }

        if(nvn.getInfraSpecificEpithet() != null && !nvn.getInfraSpecificEpithet().equals("")){
            fltq.addTerms(nvn.getInfraSpecificEpithet().toLowerCase(), "infraSpecificEpithet", accuracy, 3);
        } else {
            //textQuery.add(new RegexQuery (new Term ("infraSpecificEpithet", "^[a-zA-Z]*")), Occur.MUST_NOT);
            textQueryBuilder.add(queryFactory.newTermQuery("infraSpecificEpithet", "_null_", false), Occur.MUST);
        }

        if(nvn.getAuthorshipCache() != null && !nvn.getAuthorshipCache().equals("")){
            fltq.addTerms(nvn.getAuthorshipCache().toLowerCase(), "authorshipCache", accuracy, 3);
        } else {
            //textQuery.add(new RegexQuery (new Term ("authorshipCache", "^[a-zA-Z]*")), Occur.MUST_NOT);
        }

        textQueryBuilder.add(fltq, Occur.MUST);

        BooleanQuery textQuery = textQueryBuilder.build();
        finalQueryBuilder.add(textQuery, Occur.MUST);

        luceneSearch.setQuery(finalQueryBuilder.build());

        if(highlightFragments){
            luceneSearch.setHighlightFields(queryFactory.getTextFieldNamesAsArray());
        }
        return luceneSearch;
    }

    protected LuceneSearch prepareFindByFuzzyNameCacheSearch(Class<? extends CdmBase> clazz,
            String name,
            float accuracy,
            int maxNoOfResults,
            List<Language> languages,
            boolean highlightFragments) {

        LuceneSearch luceneSearch = new LuceneSearch(luceneIndexToolProvider, TaxonName.class);
        QueryFactory queryFactory = luceneIndexToolProvider.newQueryFactoryFor(TaxonName.class);

//    	SortField[] sortFields = new  SortField[]{SortField.FIELD_SCORE, new SortField("titleCache__sort", SortField.STRING,  false)};
//    	luceneSearch.setSortFields(sortFields);

        // ---- search criteria
        luceneSearch.setCdmTypRestriction(clazz);
        FuzzyLikeThisQuery fltq = new FuzzyLikeThisQuery(maxNoOfResults, luceneSearch.getAnalyzer());

        fltq.addTerms(name, "nameCache", accuracy, 3);

         BooleanQuery finalQuery = new BooleanQuery(false);

         finalQuery.add(fltq, Occur.MUST);

        luceneSearch.setQuery(finalQuery);

        if(highlightFragments){
            luceneSearch.setHighlightFields(queryFactory.getTextFieldNamesAsArray());
        }
        return luceneSearch;
    }

    protected LuceneSearch prepareFindByExactNameSearch(Class<? extends CdmBase> clazz,
            String name,
            boolean wildcard,
            List<Language> languages,
            boolean highlightFragments) {
        Builder textQueryBuilder = new Builder();

        LuceneSearch luceneSearch = new LuceneSearch(luceneIndexToolProvider, TaxonName.class);
        QueryFactory queryFactory = luceneIndexToolProvider.newQueryFactoryFor(TaxonName.class);

//    	SortField[] sortFields = new  SortField[]{SortField.FIELD_SCORE, new SortField("titleCache__sort", SortField.STRING,  false)};
//    	luceneSearch.setSortFields(sortFields);

        // ---- search criteria
        luceneSearch.setCdmTypRestriction(clazz);

        if(name != null && !name.equals("")) {
            if(wildcard) {
                textQueryBuilder.add(new WildcardQuery(new Term("nameCache", name + "*")), Occur.MUST);
            } else {
                textQueryBuilder.add(queryFactory.newTermQuery("nameCache", name, false), Occur.MUST);
            }
        }

        luceneSearch.setQuery(textQueryBuilder.build());

        if(highlightFragments){
            luceneSearch.setHighlightFields(queryFactory.getTextFieldNamesAsArray());
        }
        return luceneSearch;
    }

    @Override
    public List<SearchResult<TaxonName>> findByNameFuzzySearch(
            String name,
            float accuracy,
            List<Language> languages,
            boolean highlightFragments,
            List<String> propertyPaths,
            int maxNoOfResults) throws IOException, LuceneParseException {

        logger.info("Name to fuzzy search for : " + name);
        // parse the input name
        NonViralNameParserImpl parser = new NonViralNameParserImpl();
        INonViralName nvn = parser.parseFullName(name);
        if(name != null && !name.equals("") && nvn == null) {
            throw new LuceneParseException("Could not parse name " + name);
        }
        LuceneSearch luceneSearch = prepareFindByFuzzyNameSearch(null, nvn, accuracy, maxNoOfResults, languages, highlightFragments);

        // --- execute search
        TopDocs topDocs = luceneSearch.executeSearch(maxNoOfResults);


        Map<CdmBaseType, String> idFieldMap = new HashMap<CdmBaseType, String>();
        idFieldMap.put(CdmBaseType.TAXON_NAME, "id");

        // --- initialize taxa, highlight matches ....
        ISearchResultBuilder searchResultBuilder = new SearchResultBuilder(luceneSearch, luceneSearch.getQuery());

        List<SearchResult<TaxonName>> searchResults = searchResultBuilder.createResultSet(
                topDocs, luceneSearch.getHighlightFields(), dao, idFieldMap, propertyPaths);

        return searchResults;

    }

    @Override
    public List<DocumentSearchResult> findByNameFuzzySearch(
            String name,
            float accuracy,
            List<Language> languages,
            boolean highlightFragments,
            int maxNoOfResults) throws IOException, LuceneParseException {

        logger.info("Name to fuzzy search for : " + name);
        // parse the input name
        NonViralNameParserImpl parser = new NonViralNameParserImpl();
        INonViralName nvn = parser.parseFullName(name);
        if(name != null && !name.equals("") && nvn == null) {
            throw new LuceneParseException("Could not parse name " + name);
        }
        LuceneSearch luceneSearch = prepareFindByFuzzyNameSearch(null, nvn, accuracy, maxNoOfResults, languages, highlightFragments);

        // --- execute search
        TopDocs topDocs = luceneSearch.executeSearch(maxNoOfResults);

        // --- initialize taxa, highlight matches ....
        ISearchResultBuilder searchResultBuilder = new SearchResultBuilder(luceneSearch, luceneSearch.getQuery());

        List<DocumentSearchResult> searchResults = searchResultBuilder.createResultSet(topDocs, luceneSearch.getHighlightFields());

        return searchResults;
    }

    @Override
    public List<DocumentSearchResult> findByFuzzyNameCacheSearch(
            String name,
            float accuracy,
            List<Language> languages,
            boolean highlightFragments,
            int maxNoOfResults) throws IOException, LuceneParseException {

        logger.info("Name to fuzzy search for : " + name);

        LuceneSearch luceneSearch = prepareFindByFuzzyNameCacheSearch(null, name, accuracy, maxNoOfResults, languages, highlightFragments);

        // --- execute search
        TopDocs topDocs = luceneSearch.executeSearch(maxNoOfResults);

        // --- initialize taxa, highlight matches ....
        ISearchResultBuilder searchResultBuilder = new SearchResultBuilder(luceneSearch, luceneSearch.getQuery());

        List<DocumentSearchResult> searchResults = searchResultBuilder.createResultSet(topDocs, luceneSearch.getHighlightFields());

        return searchResults;
    }

    @Override
    public List<DocumentSearchResult> findByNameExactSearch(
            String name,
            boolean wildcard,
            List<Language> languages,
            boolean highlightFragments,
            int maxNoOfResults) throws IOException, LuceneParseException {

        logger.info("Name to exact search for : " + name);

        LuceneSearch luceneSearch = prepareFindByExactNameSearch(null, name, wildcard, languages, highlightFragments);

        // --- execute search

        TopDocs topDocs = luceneSearch.executeSearch(maxNoOfResults);

        // --- initialize taxa, highlight matches ....
        ISearchResultBuilder searchResultBuilder = new SearchResultBuilder(luceneSearch, luceneSearch.getQuery());

        List<DocumentSearchResult> searchResults = searchResultBuilder.createResultSet(topDocs, luceneSearch.getHighlightFields());

        return searchResults;
    }

    @Override
    public Pager<NameRelationship> pageNameRelationships(TaxonName name, Direction direction, NameRelationshipType type, Integer pageSize,
            Integer pageNumber, List<OrderHint> orderHints,	List<String> propertyPaths) {
        List<NameRelationship> results = listNameRelationships(name, direction, type, pageSize, pageNumber, orderHints, propertyPaths);
        return new DefaultPagerImpl<>(pageNumber, results.size(), pageSize, results);
    }

    @Override
    public List<NameRelationship> listFromNameRelationships(TaxonName name, NameRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        return listNameRelationships(name, Direction.relatedFrom, type, pageSize, pageNumber, orderHints, propertyPaths);
    }

    @Override
    public Pager<NameRelationship> pageFromNameRelationships(TaxonName name, NameRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        List<NameRelationship> results = listNameRelationships(name, Direction.relatedFrom, type, pageSize, pageNumber, orderHints, propertyPaths);
        return new DefaultPagerImpl<>(pageNumber, results.size(), pageSize, results);
    }

    @Override
    public List<NameRelationship> listToNameRelationships(TaxonName name, NameRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        return listNameRelationships(name, Direction.relatedTo, type, pageSize, pageNumber, orderHints, propertyPaths);
    }

    @Override
    public Pager<NameRelationship> pageToNameRelationships(TaxonName name, NameRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        List<NameRelationship> results = listNameRelationships(name, Direction.relatedTo, type, pageSize, pageNumber, orderHints, propertyPaths);
        return new DefaultPagerImpl<>(pageNumber, results.size(), pageSize, results);
    }

    @Override
    public Pager<TypeDesignationBase> getTypeDesignations(TaxonName name, SpecimenTypeDesignationStatus status,
            Integer pageSize, Integer pageNumber) {
        return getTypeDesignations(name, status, pageSize, pageNumber, null);
    }

    @Override
    public Pager<TypeDesignationBase> getTypeDesignations(TaxonName name, SpecimenTypeDesignationStatus status,
                Integer pageSize, Integer pageNumber, List<String> propertyPaths){
        long numberOfResults = dao.countTypeDesignations(name, status);

        List<TypeDesignationBase> results = new ArrayList<>();
        if(AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)) {
            results = dao.getTypeDesignations(name, null, status, pageSize, pageNumber, propertyPaths);
        }
        return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
    }

    @Override
    public List<TypeDesignationBase> getTypeDesignationsInHomotypicalGroup(UUID nameUuid, Integer pageSize,
            Integer pageNumber, List<String> propertyPaths){
        TaxonName name = load(nameUuid, Arrays.asList("nomenclaturalSource.citation.authorship"));
        Set<TypeDesignationBase<?>> typeDesignations = name.getHomotypicalGroup().getTypeDesignations();
        List<TypeDesignationBase> result = defaultBeanInitializer.initializeAll(new ArrayList(typeDesignations), propertyPaths);
        return result;
    }

    /**
     * FIXME Candidate for harmonization
     * rename search
     */
    @Override
    public Pager<TaxonName> searchNames(String uninomial,String infraGenericEpithet, String specificEpithet, String infraspecificEpithet, Rank rank, Integer pageSize,	Integer pageNumber, List<OrderHint> orderHints,
            List<String> propertyPaths) {
        long numberOfResults = dao.countNames(uninomial, infraGenericEpithet, specificEpithet, infraspecificEpithet, rank);

        List<TaxonName> results = new ArrayList<>();
        if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
            results = dao.searchNames(uninomial, infraGenericEpithet, specificEpithet, infraspecificEpithet, rank, pageSize, pageNumber, orderHints, propertyPaths);
        }

        return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
    }

    @Override
    public List<UuidAndTitleCache> getUuidAndTitleCacheOfNames(Integer limit, String pattern) {
        return dao.getUuidAndTitleCacheOfNames(limit, pattern);
    }

    @Override
    public Pager<TaxonName> findByName(Class<TaxonName> clazz, String queryString, MatchMode matchmode, List<Criterion> criteria,
            Integer pageSize,Integer pageNumber, List<OrderHint> orderHints,List<String> propertyPaths) {
         Long numberOfResults = dao.countByName(clazz, queryString, matchmode, criteria);

         List<TaxonName> results = new ArrayList<>();
         if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
                results = dao.findByName(clazz, queryString, matchmode, criteria, pageSize, pageNumber, orderHints, propertyPaths);
         }

         return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
    }

    @Override
    public List<TaxonName> findByFullTitle(Class<TaxonName> clazz, String queryString, MatchMode matchmode, List<Criterion> criteria,
            Integer pageSize,Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {

         Long numberOfResults = dao.countByFullTitle(clazz, queryString, matchmode, criteria);

         List<TaxonName> results = new ArrayList<>();
         if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
                results = dao.findByFullTitle(queryString, matchmode, pageSize, pageNumber, criteria, propertyPaths);
         }

         return results;
    }

    @Override
    public HomotypicalGroup findHomotypicalGroup(UUID uuid) {
        return homotypicalGroupDao.findByUuid(uuid);
    }

    @Override
    @Transactional(readOnly = false)
    public UpdateResult updateCaches(Class<? extends TaxonName> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<TaxonName> cacheStrategy, IProgressMonitor monitor) {
        if (clazz == null){
            clazz = TaxonName.class;
        }
        return super.updateCachesImpl(clazz, stepSize, cacheStrategy, monitor);
    }


    @Override
    public List<TaggedText> getTaggedName(UUID uuid) {
        TaxonName taxonName = dao.load(uuid);
        List<TaggedText> taggedName = taxonName.getTaggedName();
        return taggedName;
    }


    public DeleteResult isDeletable(TaxonName name, DeleteConfiguratorBase config, UUID taxonUuid){
        DeleteResult result = new DeleteResult();

         NameDeletionConfigurator nameConfig = null;
        if (config instanceof NameDeletionConfigurator){
            nameConfig = (NameDeletionConfigurator) config;
        }else{
             result.addException(new Exception("The delete configurator should be of the type NameDeletionConfigurator."));
             result.setError();
             return result;
        }

        if (!name.getNameRelations().isEmpty() && !nameConfig.isRemoveAllNameRelationships()){
            HomotypicalGroup homotypicalGroup = HibernateProxyHelper.deproxy(name.getHomotypicalGroup(), HomotypicalGroup.class);

            if (!nameConfig.isIgnoreIsBasionymFor() && homotypicalGroup.getBasionyms().contains(name)){
                result.addException(new Exception( "Name can't be deleted as it is a basionym."));
                result.setAbort();
            }
            if (!nameConfig.isIgnoreHasBasionym() && (name.getBasionyms().size()>0)){
                result.addException(new Exception( "Name can't be deleted as it has a basionym."));
                result.setAbort();
            }
            Set<NameRelationship> relationships = name.getNameRelations();
            for (NameRelationship rel: relationships){
                if (!rel.getType().equals(NameRelationshipType.BASIONYM())){
                    result.addException(new Exception("Name can't be deleted as it is used in name relationship(s). Remove name relationships prior to deletion."));
                    result.setAbort();
                    break;
                }
            }
        }
        //concepts
        if (!name.getTaxonBases().isEmpty()){
            boolean isDeletableTaxon = true;
            List <TaxonBase> notDeletedTaxonBases = name.getTaxonBases().stream()
                    .filter((taxonBase) -> !taxonBase.getUuid().equals(taxonUuid))
                    .collect(Collectors.toList());
            if (!notDeletedTaxonBases.isEmpty()){
                result.addException(new Exception("Name can't be deleted as it is used in concept(s). Remove or change concept prior to deletion."));
                result.setAbort();
            }
        }

        //hybrid relationships
        if (name.isNonViral()){
            INonViralName nvn = name;
            Set<HybridRelationship> parentHybridRelations = nvn.getHybridParentRelations();
            //Hibernate.initialize(parentHybridRelations);
            if (! parentHybridRelations.isEmpty()){
                result.addException(new Exception("Name can't be deleted as it is a parent in (a) hybrid relationship(s). Remove hybrid relationships prior to deletion."));
                result.setAbort();
            }
        }
        Set<CdmBase> referencingObjects = genericDao.getReferencingObjectsForDeletion(name);
        for (CdmBase referencingObject : referencingObjects){
            //DerivedUnit?.storedUnder
            if (referencingObject.isInstanceOf(DerivedUnit.class)){
                String message = "Name can't be deleted as it is used as derivedUnit#storedUnder by %s. Remove 'stored under' prior to deleting this name";
                message = String.format(message, CdmBase.deproxy(referencingObject, DerivedUnit.class).getTitleCache());
                result.addException(new ReferencedObjectUndeletableException(message));
                result.addRelatedObject(referencingObject);
                result.setAbort();
            }

            //DescriptionElementSource#nameUsedInSource
            else if (referencingObject.isInstanceOf(DescriptionElementSource.class) && !referencingObject.isInstanceOf(NomenclaturalSource.class) ){
                String message = "Name can't be deleted as it is used as descriptionElementSource#nameUsedInSource";
                result.addException(new ReferencedObjectUndeletableException(message));
                result.addRelatedObject(referencingObject);
                result.setAbort();
            }
            //NameTypeDesignation#typeName
            else if (referencingObject.isInstanceOf(NameTypeDesignation.class)){
                NameTypeDesignation typeDesignation = HibernateProxyHelper.deproxy(referencingObject, NameTypeDesignation.class);

                if (typeDesignation.getTypeName().equals(name) && !typeDesignation.getTypifiedNames().isEmpty()){
                    String message = "Name can't be deleted as it is used as a name type in a NameTypeDesignation";
                    result.addException(new ReferencedObjectUndeletableException(message));
                    result.addRelatedObject(referencingObject);
                    result.setAbort();
                }
            }
            //DeterminationEvent#taxonName
            else if (referencingObject.isInstanceOf(DeterminationEvent.class)){
                String message = "Name can't be deleted as it is used as a determination event";
                result.addException(new ReferencedObjectUndeletableException(message));
                result.addRelatedObject(referencingObject);
                result.setAbort();
            }
            //original spelling
            else if (referencingObject.isInstanceOf(NomenclaturalSource.class) && !((NameDeletionConfigurator)config).isIgnoreIsOriginalSpellingFor()){
                if (((NomenclaturalSource)referencingObject).getNameUsedInSource() != null && ((NomenclaturalSource)referencingObject).getNameUsedInSource().equals(name)){
                    String message = "Name can't be deleted as it is used as original spelling";
                    result.addException(new ReferencedObjectUndeletableException(message));
                    result.addRelatedObject(referencingObject);
                    result.setAbort();
                }
            }
            if (referencingObject.isInstanceOf(NomenclaturalSource.class)){
                if (((NomenclaturalSource)referencingObject).getNameUsedInSource() != null && ((NomenclaturalSource)referencingObject).getNameUsedInSource().equals(name)){
                    result.addRelatedObject(referencingObject);
                }

            }
        }

        //TODO inline references


        if (!nameConfig.isIgnoreIsReplacedSynonymFor() && name.isReplacedSynonym()){
            String message = "Name can't be deleted as it is a replaced synonym.";
            result.addException(new Exception(message));
            result.setAbort();
        }
        if (!nameConfig.isIgnoreHasReplacedSynonym() && (name.getReplacedSynonyms().size()>0)){
            String message = "Name can't be deleted as it has a replaced synonym.";
            result.addException(new Exception(message));
            result.setAbort();
        }
        return result;

    }


    @Override
    public DeleteResult isDeletable(UUID nameUUID, DeleteConfiguratorBase config){
        TaxonName name = this.load(nameUUID);
        return isDeletable(name, config, null);
    }

    @Override
    public DeleteResult isDeletable(UUID nameUUID, DeleteConfiguratorBase config, UUID taxonUuid){
        TaxonName name = this.load(nameUUID);
        return isDeletable(name, config, taxonUuid);
    }

    @Override
    @Transactional(readOnly = true)
    public UpdateResult setAsGroupsBasionym(UUID nameUuid) {
        TaxonName name = dao.load(nameUuid);
        UpdateResult result = new UpdateResult();
        name.setAsGroupsBasionym();
        result.addUpdatedObject(name);
        return result;

    }

    @Override
    public List<HashMap<String,String>> getNameRecords(){
		return dao.getNameRecords();

    }

    @Override
    public List<TypeDesignationStatusBase> getTypeDesignationStatusInUse(){
        return typeDesignationDao.getTypeDesignationStatusInUse();
    }

    @Override
    public Collection<TypeDesignationStatusFilter> getTypeDesignationStatusFilterTerms(List<Language> preferredLanguages){
        List<TypeDesignationStatusBase> termList = typeDesignationDao.getTypeDesignationStatusInUse();
        Map<String, TypeDesignationStatusFilter>  filterMap = new HashMap<>();
        for(TypeDesignationStatusBase term : termList){
            TypeDesignationStatusFilter filter = new TypeDesignationStatusFilter(term, preferredLanguages, true);
            String key = filter.getKey();
            if(filterMap.containsKey(key)){
                filterMap.get(key).addStatus(term);
            } else {
                filterMap.put(key, filter);
            }
        }
        return filterMap.values();
    }

    @Override
    public <S extends TaxonName> Pager<S> page(Class<S> clazz, List<Restriction<?>> restrictions, Integer pageSize,
            Integer pageIndex, List<OrderHint> orderHints, List<String> propertyPaths) {
        return page(clazz, restrictions, pageSize, pageIndex, orderHints, propertyPaths, INCLUDE_UNPUBLISHED);
    }

    @Override
    public <S extends TaxonName> Pager<S> page(Class<S> clazz, List<Restriction<?>> restrictions, Integer pageSize,
            Integer pageIndex, List<OrderHint> orderHints, List<String> propertyPaths, boolean includeUnpublished) {

        List<S> records;
        long resultSize = dao.count(clazz, restrictions);
        if(AbstractPagerImpl.hasResultsInRange(resultSize, pageIndex, pageSize)){
            records = dao.list(clazz, restrictions, pageSize, pageIndex, orderHints, propertyPaths, includeUnpublished);
        } else {
            records = new ArrayList<>();
        }
        Pager<S> pager = new DefaultPagerImpl<>(pageIndex, resultSize, pageSize, records);
        return pager;
    }

    @Override
    public List<UuidAndTitleCache> getUuidAndTitleCacheOfSynonymy(Integer limit, UUID taxonUuid) {
        List<String> propertyPaths = new ArrayList<>();
        propertyPaths.add("synonyms.name.*");
        TaxonBase<?> taxonBase = taxonService.load(taxonUuid, propertyPaths);
        if (taxonBase instanceof Taxon){
            Taxon taxon = (Taxon)taxonBase;
            Set<TaxonName> names = taxon.getSynonymNames();
            List<UuidAndTitleCache> uuidAndTitleCacheList = new ArrayList<>();
            UuidAndTitleCache<TaxonName> uuidAndTitleCache;
            for (TaxonName name: names){
                uuidAndTitleCache = new UuidAndTitleCache<TaxonName>(TaxonName.class, name.getUuid(), name.getId(), name.getTitleCache());
                uuidAndTitleCacheList.add(uuidAndTitleCache);
            }
        }
        return null;
    }

    @Override
    @Transactional(readOnly = false) //as long as the deduplication may lead to a flush which may cause a titleCache update, this happens in  CdmGenericDaoImpl.findMatching()
    public UpdateResult parseName(String stringToBeParsed, NomenclaturalCode code, Rank preferredRank, boolean doDeduplicate) {
        TaxonName name = TaxonNameFactory.NewNameInstance(code, preferredRank);
        return parseName(name, stringToBeParsed, preferredRank, true, doDeduplicate);
    }

    @Override
    @Transactional(readOnly = false) //as long as the deduplication may lead to a flush which may cause a titleCache update, this happens in  CdmGenericDaoImpl.findMatching()
    public UpdateResult parseName(TaxonName nameToBeFilled, String stringToBeParsed, Rank preferredRank,
            boolean doEmpty, boolean doDeduplicate){

        UpdateResult result = new UpdateResult();
        NonViralNameParserImpl nonViralNameParser = NonViralNameParserImpl.NewInstance();
        nonViralNameParser.parseReferencedName(nameToBeFilled, stringToBeParsed, preferredRank, doEmpty);
        TaxonName name = nameToBeFilled;
        if(doDeduplicate) {
            try {
//              Level sqlLogLevel = LogManager.getLogger("org.hibernate.SQL").getLevel();
//              LogUtils.setLevel("org.hibernate.SQL", Level.TRACE);

                //references
                if (name.getNomenclaturalReference()!= null && !name.getNomenclaturalReference().isPersited()){
                    Reference nomRef = name.getNomenclaturalReference();
                    IMatchStrategy referenceMatcher = MatchStrategyFactory.NewParsedReferenceInstance(nomRef);
                    List<Reference> matchingReferences = commonService.findMatching(nomRef, referenceMatcher);
                    if(matchingReferences.size() >= 1){
                        Reference duplicate = findBestMatching(nomRef, matchingReferences, referenceMatcher);
                        name.setNomenclaturalReference(duplicate);
                    }else{
                        if (nomRef.getInReference() != null){
                            List<Reference> matchingInReferences = commonService.findMatching(nomRef.getInReference(), MatchStrategyFactory.NewParsedReferenceInstance(nomRef.getInReference()));
                            if(matchingInReferences.size() >= 1){
                                Reference duplicate = findBestMatching(nomRef, matchingInReferences, referenceMatcher);
                                nomRef.setInReference(duplicate);
                            }
                        }
                        TeamOrPersonBase<?> author = deduplicateAuthor(nomRef.getAuthorship());
                        nomRef.setAuthorship(author);
                    }
                }
                Reference nomRef = name.getNomenclaturalReference();

                //authors
                IParsedMatchStrategy authorMatcher = MatchStrategyFactory.NewParsedTeamOrPersonInstance();
                if (name.getCombinationAuthorship()!= null && !name.getCombinationAuthorship().isPersited()){
                    //use same nom.ref. author if possible (should always be possible if nom.ref. exists)
                    if (nomRef != null && nomRef.getAuthorship() != null){
                        if(authorMatcher.invoke(name.getCombinationAuthorship(), nomRef.getAuthorship()).isSuccessful()){
                            name.setCombinationAuthorship(nomRef.getAuthorship());
                        }
                    }
                    name.setCombinationAuthorship(deduplicateAuthor(name.getCombinationAuthorship()));
                }
                if (name.getExCombinationAuthorship()!= null && !name.getExCombinationAuthorship().isPersited()){
                    name.setExCombinationAuthorship(deduplicateAuthor(name.getExCombinationAuthorship()));
                }
                if (name.getBasionymAuthorship()!= null && !name.getBasionymAuthorship().isPersited()){
                    name.setBasionymAuthorship(deduplicateAuthor(name.getBasionymAuthorship()));
                }
                if (name.getExBasionymAuthorship()!= null && !name.getExBasionymAuthorship().isPersited()){
                    name.setExBasionymAuthorship(deduplicateAuthor(name.getExBasionymAuthorship()));
                }

                //originalSpelling
                if (name.getOriginalSpelling()!= null && !name.getOriginalSpelling().isPersited()){
                    TaxonName origName = name.getOriginalSpelling();
                    IMatchStrategy nameMatcher = MatchStrategyFactory.NewParsedOriginalSpellingInstance();
                    List<TaxonName> matchingNames = commonService.findMatching(origName, nameMatcher);
                    if(matchingNames.size() >= 1){
                        TaxonName duplicate = findBestMatching(origName, matchingNames, nameMatcher);
                        name.setOriginalSpelling(duplicate);
                    }
                }
//              LogUtils.setLevel("org.hibernate.SQL", sqlLogLevel);
            } catch (MatchException e) {
                throw new RuntimeException(e);
            }
        }
        result.setCdmEntity(name);
        return result;
    }

    private TeamOrPersonBase<?> deduplicateAuthor(TeamOrPersonBase<?> authorship) throws MatchException {
        if (authorship == null){
            return null;
        }
        IParsedMatchStrategy authorMatcher = MatchStrategyFactory.NewParsedTeamOrPersonInstance();
        List<TeamOrPersonBase<?>> matchingAuthors = commonService.findMatching(authorship, authorMatcher);
        if(matchingAuthors.size() >= 1){
            TeamOrPersonBase<?> duplicate = findBestMatching(authorship, matchingAuthors, authorMatcher);
            return duplicate;
        }else{
            if (authorship instanceof Team){
                deduplicateTeam((Team)authorship);
            }
            return authorship;
        }
    }

    private void deduplicateTeam(Team team) throws MatchException {
        List<Person> members = team.getTeamMembers();
        IParsedMatchStrategy personMatcher = MatchStrategyFactory.NewParsedPersonInstance();
        for (int i =0; i< members.size(); i++){
            Person person = CdmBase.deproxy(members.get(i));
            List<Person> matchingPersons = commonService.findMatching(person, personMatcher);
            if (matchingPersons.size() > 0){
                person = findBestMatching(person, matchingPersons, personMatcher);
                members.set(i, person);
            }
        }
    }

    private <M extends IMatchable> M findBestMatching(M matchable, List<M> matchingList,
            IMatchStrategy matcher) {
        // FIXME TODO resolve multiple duplications. Use first match for a start
        if(matchingList.isEmpty()){
            return null;
        }
        M bestMatching = matchingList.iterator().next();
        return bestMatching;
    }

    public int modifiedDamerauLevenshteinDistance(String str1, String str2) {

    	//str1 is the query
    	//str2 is the document

		if (str1 == str2) {
			return 0;
		} else if (str1.isEmpty()) {
			return str2.length();
		} else if (str2.isEmpty()) {
			return str1.length();
		} else if (str2.length() == 1 && str1.length() == 1 && str1 != str2) {
			return 1;
		} else {

			int[][] distanceMatrix = new int[str1.length() + 1][str2.length() + 1];

			for (int i = 0; i <= str1.length(); i++) {
				distanceMatrix[i][0] = i;
			}

			for (int j = 0; j <= str2.length(); j++) {
				distanceMatrix[0][j] = j;
			}

			for (int i = 1; i <= str1.length(); i++) {
				for (int j = 1; j <= str2.length(); j++) {
					int cost = (str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : 1;
					distanceMatrix[i][j] = Math.min(
							Math.min(distanceMatrix[i - 1][j] + 1, distanceMatrix[i][j - 1] + 1),
							distanceMatrix[i - 1][j - 1] + cost);

					if (i > 1 && j > 1 && str1.charAt(i - 1) == str2.charAt(j - 2)
							&& str1.charAt(i - 2) == str2.charAt(j - 1)) {
						distanceMatrix[i][j] = Math.min(distanceMatrix[i][j], distanceMatrix[i - 2][j - 2] + cost);
					}
				}
			}
			return distanceMatrix[str1.length()][str2.length()];
		}
	}

    public List<UuidAndTitleCache<TaxonName>> setDatabaseList (){
    	List<UuidAndTitleCache<TaxonName>> dataBaseList= getUuidAndTitleCache(0, "*");
    	return dataBaseList;
    }
    
	public Map<UuidAndTitleCache<TaxonName>,Integer> findMatchingNames(String query, int limit, List<UuidAndTitleCache<TaxonName>> dataBaseList ) {
		
		Map<UuidAndTitleCache<TaxonName>,Integer> result = new HashMap<>();
			
		result.putAll(calculateDistance(query, dataBaseList));
		
		Map<UuidAndTitleCache<TaxonName>,Integer> 
		sorted = result.entrySet().stream().sorted(Map.Entry.comparingByValue()).limit(limit)
			       .collect(Collectors.toMap(
			    	          Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		return sorted;
		
	}

	private Map<UuidAndTitleCache<TaxonName>, Integer> calculateDistance(String query, List<UuidAndTitleCache<TaxonName>> dataBaseList) {
		
		Map<UuidAndTitleCache<TaxonName>, Integer> distanceMap = new HashMap<>();

		for (UuidAndTitleCache<TaxonName> document : dataBaseList) {

			int distance = modifiedDamerauLevenshteinDistance(query, document.getTitleCache());
			distanceMap.put(document, distance);
		}
		return distanceMap;
	}
}