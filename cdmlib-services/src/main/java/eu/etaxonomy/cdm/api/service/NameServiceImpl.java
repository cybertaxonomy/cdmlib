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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.FuzzyLikeThisQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.regex.RegexQuery;
import org.apache.lucene.util.Version;
import org.hibernate.criterion.Criterion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.config.NameDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.exception.ReferencedObjectUndeletableException;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.AbstractPagerImpl;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.api.service.search.DocumentSearchResult;
import eu.etaxonomy.cdm.api.service.search.ISearchResultBuilder;
import eu.etaxonomy.cdm.api.service.search.LuceneSearch;
import eu.etaxonomy.cdm.api.service.search.QueryFactory;
import eu.etaxonomy.cdm.api.service.search.SearchResult;
import eu.etaxonomy.cdm.api.service.search.SearchResultBuilder;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.CdmBaseType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DescriptionElementSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.RelationshipBase.Direction;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;
import eu.etaxonomy.cdm.persistence.dao.common.IOrderedTermVocabularyDao;
import eu.etaxonomy.cdm.persistence.dao.common.IReferencedEntityDao;
import eu.etaxonomy.cdm.persistence.dao.common.ITermVocabularyDao;
import eu.etaxonomy.cdm.persistence.dao.name.IHomotypicalGroupDao;
import eu.etaxonomy.cdm.persistence.dao.name.INomenclaturalStatusDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITypeDesignationDao;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;


@Service
@Transactional(readOnly = true)
public class NameServiceImpl extends IdentifiableServiceBase<TaxonNameBase,ITaxonNameDao> implements INameService {
    static private final Logger logger = Logger.getLogger(NameServiceImpl.class);

    @Autowired
    protected ITermVocabularyDao vocabularyDao;
    @Autowired
    protected IOrderedTermVocabularyDao orderedVocabularyDao;
    @Autowired
    @Qualifier("refEntDao")
    protected IReferencedEntityDao<ReferencedEntityBase> referencedEntityDao;
    @Autowired
    private INomenclaturalStatusDao nomStatusDao;
    @Autowired
    private ITypeDesignationDao typeDesignationDao;
    @Autowired
    private IHomotypicalGroupDao homotypicalGroupDao;
    @Autowired
    private ICdmGenericDao genericDao;

    /**
     * Constructor
     */
    public NameServiceImpl(){
        if (logger.isDebugEnabled()) { logger.debug("Load NameService Bean"); }
    }

//********************* METHODS ****************************************************************//

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ServiceBase#delete(eu.etaxonomy.cdm.model.common.CdmBase)
     */
    @Override
    public UUID delete(TaxonNameBase name){
        NameDeletionConfigurator config = new NameDeletionConfigurator();
        try {
            return delete(name, config);
        } catch (ReferencedObjectUndeletableException e) {
            //TODO throw DeleteException - current implementation is preliminary for testing
            throw new RuntimeException(e);
        }
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.INameService#delete(eu.etaxonomy.cdm.model.name.TaxonNameBase, eu.etaxonomy.cdm.api.service.NameDeletionConfigurator)
     */
    @Override
    public UUID delete(TaxonNameBase name, NameDeletionConfigurator config) throws ReferencedObjectUndeletableException{
        if (name == null){
            return null;
        }

        //remove references to this name
        removeNameRelationshipsByDeleteConfig(name, config);

        //check if this name is still used somewhere

        //name relationships
        if (! name.getNameRelations().isEmpty()){
            String message = "Name can't be deleted as it is used in name relationship(s). Remove name relationships prior to deletion.";
            throw new ReferencedObjectUndeletableException(message);
//			return null;
        }

        //concepts
        if (! name.getTaxonBases().isEmpty()){
            String message = "Name can't be deleted as it is used in concept(s). Remove or change concept prior to deletion.";
            throw new ReferencedObjectUndeletableException(message);
        }

        //hybrid relationships
        if (name.isInstanceOf(NonViralName.class)){
            NonViralName nvn = CdmBase.deproxy(name, NonViralName.class);
//			if (! nvn.getHybridChildRelations().isEmpty()){
//				String message = "Name can't be deleted as it is a child in (a) hybrid relationship(s). Remove hybrid relationships prior to deletion.";
//				throw new RuntimeException(message);
//			}
            if (! nvn.getHybridParentRelations().isEmpty()){
                String message = "Name can't be deleted as it is a parent in (a) hybrid relationship(s). Remove hybrid relationships prior to deletion.";
                throw new ReferencedObjectUndeletableException(message);
            }
        }

        //all type designation relationships are removed as they belong to the name
        deleteTypeDesignation(name, null);
//		//type designations
//		if (! name.getTypeDesignations().isEmpty()){
//			String message = "Name can't be deleted as it has types. Remove types prior to deletion.";
//			throw new ReferrencedObjectUndeletableException(message);
//		}

        //check references with only reverse mapping
        Set<CdmBase> referencingObjects = genericDao.getReferencingObjects(name);
        for (CdmBase referencingObject : referencingObjects){
            //DerivedUnitBase?.storedUnder
            if (referencingObject.isInstanceOf(DerivedUnitBase.class)){
                String message = "Name can't be deleted as it is used as derivedUnit#storedUnder by %s. Remove 'stored under' prior to deleting this name";
                message = String.format(message, CdmBase.deproxy(referencingObject, DerivedUnitBase.class).getTitleCache());
                throw new ReferencedObjectUndeletableException(message);
            }
            //DescriptionElementSource#nameUsedInSource
            if (referencingObject.isInstanceOf(DescriptionElementSource.class)){
                String message = "Name can't be deleted as it is used as descriptionElementSource#nameUsedInSource";
                throw new ReferencedObjectUndeletableException(message);
            }
            //NameTypeDesignation#typeName
            if (referencingObject.isInstanceOf(NameTypeDesignation.class)){
                String message = "Name can't be deleted as it is used as a name type in a NameTypeDesignation";
                throw new ReferencedObjectUndeletableException(message);
            }

            //TaxonNameDescriptions#taxonName
            //deleted via cascade?

            //NomenclaturalStatus
            //deleted via cascade?

        }

        //TODO inline references

        dao.delete(name);
        return name.getUuid();
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.INameService#deleteTypeDesignation(eu.etaxonomy.cdm.model.name.TaxonNameBase, eu.etaxonomy.cdm.model.name.TypeDesignationBase)
     */
    @Override
    public void deleteTypeDesignation(TaxonNameBase name, TypeDesignationBase typeDesignation){
        if (name == null && typeDesignation == null){
            return;
        }else if (name != null && typeDesignation != null){
            removeSingleDesignation(name, typeDesignation);
        }else if (name != null){
            Set<TypeDesignationBase> designationSet = new HashSet<TypeDesignationBase>(name.getTypeDesignations());
            for (Object o : designationSet){
                TypeDesignationBase desig = CdmBase.deproxy(o, TypeDesignationBase.class);
                removeSingleDesignation(name, desig);
            }
        }else if (typeDesignation != null){
            Set<TaxonNameBase> nameSet = new HashSet<TaxonNameBase>(typeDesignation.getTypifiedNames());
            for (Object o : nameSet){
                TaxonNameBase singleName = CdmBase.deproxy(o, TaxonNameBase.class);
                removeSingleDesignation(singleName, typeDesignation);
            }
        }
    }

    /**
     * @param name
     * @param typeDesignation
     */
    private void removeSingleDesignation(TaxonNameBase name, TypeDesignationBase typeDesignation) {
        name.removeTypeDesignation(typeDesignation);
        if (typeDesignation.getTypifiedNames().isEmpty()){
            typeDesignation.removeType();
            typeDesignationDao.delete(typeDesignation);
        }
    }



    /**
     * @param name
     * @param config
     */
    private void removeNameRelationshipsByDeleteConfig(TaxonNameBase name, NameDeletionConfigurator config) {
        if (config.isRemoveAllNameRelationships()){
            Set<NameRelationship> rels = name.getNameRelations();
            for (NameRelationship rel : rels){
                name.removeNameRelationship(rel);
            }
        }else{
            //relations to this name
            Set<NameRelationship> rels = name.getRelationsToThisName();
            for (NameRelationship rel : rels){
                if (config.isIgnoreHasBasionym() && NameRelationshipType.BASIONYM().equals(rel.getType() )){
                        name.removeNameRelationship(rel);
                }else if (config.isIgnoreHasReplacedSynonym() && NameRelationshipType.REPLACED_SYNONYM().equals(rel.getType())){
                    name.removeNameRelationship(rel);
                }
            }
            //relations from this name
            rels = name.getRelationsFromThisName();
            for (NameRelationship rel : rels){
                if (config.isIgnoreIsBasionymFor() && NameRelationshipType.BASIONYM().equals(rel.getType())  ){
                    name.removeNameRelationship(rel);
                }else if (config.isIgnoreIsReplacedSynonymFor() && NameRelationshipType.REPLACED_SYNONYM().equals(rel.getType())){
                    name.removeNameRelationship(rel);
                }
            }

        }
    }

//********************* METHODS ****************************************************************//

    /**
     * @deprecated To be removed for harmonization see http://dev.e-taxonomy.eu/trac/wiki/CdmLibraryConventions
     * duplicate of findByName
     */
    @Override
    @Deprecated
    public List getNamesByName(String name){
        return super.findCdmObjectsByTitle(name);
    }

    /**
     * TODO candidate for harmonization
     * new name findByName
     */
    @Override
    public List<NonViralName> getNamesByNameCache(String nameCache){
        List result = dao.findByName(nameCache, MatchMode.EXACT, null, null, null, null);
        return result;
    }


    /**
     * TODO candidate for harmonization
     * new name saveHomotypicalGroups
     *
     * findByTitle
     */
    @Override
    public List<NonViralName> findNamesByTitleCache(String titleCache, MatchMode matchMode, List<String> propertyPaths){
        List result = dao.findByTitle(titleCache, matchMode, null, null, null ,propertyPaths);
        return result;
    }

    /**
     * TODO candidate for harmonization
     * new name saveHomotypicalGroups
     *
     * findByTitle
     */
    @Override
    public List<NonViralName> findNamesByNameCache(String nameCache, MatchMode matchMode, List<String> propertyPaths){
        List result = dao.findByName(nameCache, matchMode, null, null, null ,propertyPaths);
        return result;
    }

    /**
     * @deprecated To be removed for harmonization see http://dev.e-taxonomy.eu/trac/wiki/CdmLibraryConventions
     * Replace by load(UUID, propertyPaths)
     */
    @Override
    @Deprecated
    public NonViralName findNameByUuid(UUID uuid, List<String> propertyPaths){
        return (NonViralName)dao.findByUuid(uuid, null ,propertyPaths);
    }

    /**
     * TODO candidate for harmonization
     */
    @Override
    public List getNamesByName(String name, CdmBase sessionObject){
        return super.findCdmObjectsByTitle(name, sessionObject);
    }

    /**
     * @deprecated To be removed for harmonization see http://dev.e-taxonomy.eu/trac/wiki/CdmLibraryConventions
     * duplicate of findByTitle(clazz, queryString, matchmode, criteria, pageSize, pageNumber, orderHints, propertyPaths)
     */
    @Override
    @Deprecated
    public List findNamesByTitle(String title){
        return super.findCdmObjectsByTitle(title);
    }

    /**
     * @deprecated To be removed for harmonization see http://dev.e-taxonomy.eu/trac/wiki/CdmLibraryConventions
     * duplicate of findByTitle()
     */
    @Override
    @Deprecated
    public List findNamesByTitle(String title, CdmBase sessionObject){
        return super.findCdmObjectsByTitle(title, sessionObject);
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
    public Map<UUID, TypeDesignationBase> saveTypeDesignationAll(Collection<TypeDesignationBase> typeDesignationCollection){
        return typeDesignationDao.saveAll(typeDesignationCollection);
    }

    /**
     * TODO candidate for harmonization
     * new name saveReferencedEntities
     */
    @Override
    @Transactional(readOnly = false)
    public Map<UUID, ReferencedEntityBase> saveReferencedEntitiesAll(Collection<ReferencedEntityBase> referencedEntityCollection){
        return referencedEntityDao.saveAll(referencedEntityCollection);
    }

    /**
     * TODO candidate for harmonization
     * new name getNames
     */
    public List<TaxonNameBase> getAllNames(int limit, int start){
        return dao.list(limit, start);
    }

    /**
     * TODO candidate for harmonization
     * new name getNomenclaturalStatus
     */
    @Override
    public List<NomenclaturalStatus> getAllNomenclaturalStatus(int limit, int start){
        return nomStatusDao.list(limit, start);
    }

    /**
     * TODO candidate for harmonization
     * new name getTypeDesignations
     */
    @Override
    public List<TypeDesignationBase> getAllTypeDesignations(int limit, int start){
        return typeDesignationDao.getAllTypeDesignations(limit, start);
    }
      /**
     * FIXME Candidate for harmonization
     * homotypicalGroupService.list
     */
    @Override
    public List<HomotypicalGroup> getAllHomotypicalGroups(int limit, int start){
        return homotypicalGroupDao.list(limit, start);
    }

    /**
     * FIXME Candidate for harmonization
     * remove
     */
    @Override
    @Deprecated
    public List<RelationshipBase> getAllRelationships(int limit, int start){
        return dao.getAllRelationships(limit, start);
    }

    /**
     * FIXME Candidate for harmonization
     * is this not the same as termService.getVocabulary(VocabularyEnum.Rank)
     * since this returns OrderedTermVocabulary
     *
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.INameService#getRankVocabulary()
     */
    @Override
    public OrderedTermVocabulary<Rank> getRankVocabulary() {
        String uuidString = "ef0d1ce1-26e3-4e83-b47b-ca74eed40b1b";
        UUID uuid = UUID.fromString(uuidString);
        OrderedTermVocabulary<Rank> rankVocabulary =
            (OrderedTermVocabulary)orderedVocabularyDao.findByUuid(uuid);
        return rankVocabulary;
    }

    /**
      * FIXME Candidate for harmonization
     * is this the same as termService.getVocabulary(VocabularyEnum.NameRelationshipType)
     *  (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.INameService#getNameRelationshipTypeVocabulary()
     */
    @Override
    public TermVocabulary<NameRelationshipType> getNameRelationshipTypeVocabulary() {
        String uuidString = "6878cb82-c1a4-4613-b012-7e73b413c8cd";
        UUID uuid = UUID.fromString(uuidString);
        TermVocabulary<NameRelationshipType> nameRelTypeVocabulary =
            vocabularyDao.findByUuid(uuid);
        return nameRelTypeVocabulary;
    }

    /**
      * FIXME Candidate for harmonization
     * is this the same as termService.getVocabulary(VocabularyEnum.StatusType)
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.INameService#getStatusTypeVocabulary()
     */
    @Override
    public TermVocabulary<NomenclaturalStatusType> getStatusTypeVocabulary() {
        String uuidString = "bb28cdca-2f8a-4f11-9c21-517e9ae87f1f";
        UUID uuid = UUID.fromString(uuidString);
        TermVocabulary<NomenclaturalStatusType> nomStatusTypeVocabulary =
            vocabularyDao.findByUuid(uuid);
        return nomStatusTypeVocabulary;
    }

    /**
      * FIXME Candidate for harmonization
     * is this the same as termService.getVocabulary(VocabularyEnum.SpecimenTypeDesignationStatus)
     *  (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.INameService#getTypeDesignationStatusVocabulary()
     */
    @Override
    public TermVocabulary<SpecimenTypeDesignationStatus> getSpecimenTypeDesignationStatusVocabulary() {
        String uuidString = "ab177bd7-d3c8-4e58-a388-226fff6ba3c2";
        UUID uuid = UUID.fromString(uuidString);
        TermVocabulary<SpecimenTypeDesignationStatus> typeDesigStatusVocabulary =
            vocabularyDao.findByUuid(uuid);
        return typeDesigStatusVocabulary;
    }

    /**
       * FIXME Candidate for harmonization
     * is this the same as termService.getVocabulary(VocabularyEnum.SpecimenTypeDesignationStatus)
     * and also seems to duplicate the above method, differing only in the DAO used and the return type
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.INameService#getTypeDesignationStatusVocabulary()
     */
    @Override
    public OrderedTermVocabulary<SpecimenTypeDesignationStatus> getSpecimenTypeDesignationVocabulary() {
        String uuidString = "ab177bd7-d3c8-4e58-a388-226fff6ba3c2";
        UUID uuid = UUID.fromString(uuidString);
        OrderedTermVocabulary<SpecimenTypeDesignationStatus> typeDesignationVocabulary =
            (OrderedTermVocabulary)orderedVocabularyDao.findByUuid(uuid);
        return typeDesignationVocabulary;
    }


    @Override
    @Autowired
    protected void setDao(ITaxonNameDao dao) {
        this.dao = dao;
    }

    @Override
    public Pager<HybridRelationship> getHybridNames(NonViralName name,	HybridRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.countHybridNames(name, type);

        List<HybridRelationship> results = new ArrayList<HybridRelationship>();
        if(AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)) { // no point checking again
            results = dao.getHybridNames(name, type, pageSize, pageNumber,orderHints,propertyPaths);
        }

        return new DefaultPagerImpl<HybridRelationship>(pageNumber, numberOfResults, pageSize, results);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.INameService#listNameRelationships(eu.etaxonomy.cdm.model.name.TaxonNameBase, eu.etaxonomy.cdm.model.common.RelationshipBase.Direction, eu.etaxonomy.cdm.model.name.NameRelationshipType, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    @Override
    public List<NameRelationship> listNameRelationships(TaxonNameBase name,	Direction direction, NameRelationshipType type, Integer pageSize,
            Integer pageNumber, List<OrderHint> orderHints,	List<String> propertyPaths) {

        Integer numberOfResults = dao.countNameRelationships(name, NameRelationship.Direction.relatedFrom, type);

        List<NameRelationship> results = new ArrayList<NameRelationship>();
        if (AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)) { // no point checking again
            results = dao.getNameRelationships(name, direction, type, pageSize,	pageNumber, orderHints, propertyPaths);
        }
        return results;
    }


    protected LuceneSearch prepareFindByFuzzyNameSearch(Class<? extends CdmBase> clazz, 
    		NonViralName nvn,
    		float accuracy,
    		int maxNoOfResults,
    		List<Language> languages,
    		boolean highlightFragments) {
    	String similarity = Float.toString(accuracy);    	
    	String searchSuffix = "~" + similarity;
    	

    	BooleanQuery finalQuery = new BooleanQuery(false);
    	BooleanQuery textQuery = new BooleanQuery(false);

    	LuceneSearch luceneSearch = new LuceneSearch(getSession(), TaxonNameBase.class);    	   
    	QueryFactory queryFactory = new QueryFactory(luceneSearch);

    	SortField[] sortFields = new  SortField[]{SortField.FIELD_SCORE, new SortField("titleCache__sort", SortField.STRING,  false)};
    	luceneSearch.setSortFields(sortFields);

    	// ---- search criteria
    	luceneSearch.setClazz(clazz);
    	
    	FuzzyLikeThisQuery fltq = new FuzzyLikeThisQuery(maxNoOfResults, luceneSearch.getAnalyzer());
    	if(nvn.getGenusOrUninomial() != null && !nvn.getGenusOrUninomial().equals("")) {        	
    		fltq.addTerms(nvn.getGenusOrUninomial().toLowerCase(), "genusOrUninomial", accuracy, 3);    		
    	} else {
    		textQuery.add(new RegexQuery (new Term ("genusOrUninomial", "^[a-zA-Z]*")), Occur.MUST_NOT);
    	}

    	if(nvn.getInfraGenericEpithet() != null && !nvn.getInfraGenericEpithet().equals("")){
    		fltq.addTerms(nvn.getInfraGenericEpithet().toLowerCase(), "infraGenericEpithet", accuracy, 3); 
    	} else {
    		textQuery.add(new RegexQuery (new Term ("infraGenericEpithet", "^[a-zA-Z]*")), Occur.MUST_NOT);
    	}

    	if(nvn.getSpecificEpithet() != null && !nvn.getSpecificEpithet().equals("")){
    		fltq.addTerms(nvn.getSpecificEpithet().toLowerCase(), "specificEpithet", accuracy, 3); 
    	} else {
    		textQuery.add(new RegexQuery (new Term ("specificEpithet", "^[a-zA-Z]*")), Occur.MUST_NOT);    		
    	}

    	if(nvn.getInfraSpecificEpithet() != null && !nvn.getInfraSpecificEpithet().equals("")){
    		fltq.addTerms(nvn.getInfraSpecificEpithet().toLowerCase(), "infraSpecificEpithet", accuracy, 3); 
    	} else {
    		textQuery.add(new RegexQuery (new Term ("infraSpecificEpithet", "^[a-zA-Z]*")), Occur.MUST_NOT);
    	}

    	if(nvn.getAuthorshipCache() != null && !nvn.getAuthorshipCache().equals("")){
    		fltq.addTerms(nvn.getAuthorshipCache().toLowerCase(), "authorshipCache", accuracy, 3);
    	} else {
    		//textQuery.add(new RegexQuery (new Term ("authorshipCache", "^[a-zA-Z]*")), Occur.MUST_NOT);
    	}

    	textQuery.add(fltq, Occur.MUST);    	    

    	finalQuery.add(textQuery, Occur.MUST); 
    	
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
        BooleanQuery finalQuery = new BooleanQuery();
        BooleanQuery textQuery = new BooleanQuery();

        LuceneSearch luceneSearch = new LuceneSearch(getSession(), TaxonNameBase.class);
        QueryFactory queryFactory = new QueryFactory(luceneSearch);

        SortField[] sortFields = new  SortField[]{SortField.FIELD_SCORE, new SortField("titleCache__sort", SortField.STRING,  false)};
        luceneSearch.setSortFields(sortFields);

        // ---- search criteria
        luceneSearch.setClazz(clazz);
        
        
        
        if(name != null && !name.equals("")) {        	        	
        	if(wildcard) {
        		textQuery.add(new WildcardQuery(new Term("nameCache", name + "*")), Occur.MUST);
        	} else {
        		textQuery.add(queryFactory.newTermQuery("nameCache", name, false), Occur.MUST);
        	}
        } 
        
        finalQuery.add(textQuery, Occur.MUST);

        luceneSearch.setQuery(textQuery);

        if(highlightFragments){
            luceneSearch.setHighlightFields(queryFactory.getTextFieldNamesAsArray());
        }
        return luceneSearch;
    }
    
    public List<SearchResult<TaxonNameBase>> findByNameFuzzySearch(
            String name,
            float accuracy,
            List<Language> languages,
            boolean highlightFragments, 
            List<String> propertyPaths,
            int maxNoOfResults) throws CorruptIndexException, IOException, ParseException {

    	logger.info("Name to fuzzy search for : " + name);
    	// parse the input name
    	NonViralNameParserImpl parser = new NonViralNameParserImpl();
    	NonViralName nvn = parser.parseFullName(name);
    	if(name != null && !name.equals("") && nvn == null) {
    		throw new ParseException("Could not parse name " + name);
    	}
        LuceneSearch luceneSearch = prepareFindByFuzzyNameSearch(null, nvn, accuracy, maxNoOfResults, languages, highlightFragments);

        // --- execute search        
        TopDocs topDocs = luceneSearch.executeSearch(maxNoOfResults);

        
        Map<CdmBaseType, String> idFieldMap = new HashMap<CdmBaseType, String>();
        idFieldMap.put(CdmBaseType.NONVIRALNAME, "id");

        // --- initialize taxa, highlight matches ....
        ISearchResultBuilder searchResultBuilder = new SearchResultBuilder(luceneSearch, luceneSearch.getQuery());
        
        @SuppressWarnings("rawtypes")
        List<SearchResult<TaxonNameBase>> searchResults = searchResultBuilder.createResultSet(
                topDocs, luceneSearch.getHighlightFields(), dao, idFieldMap, propertyPaths);

        return searchResults;

    }
    
    public List<DocumentSearchResult> findByNameFuzzySearch(
            String name,
            float accuracy,
            List<Language> languages,
            boolean highlightFragments, 
            int maxNoOfResults) throws CorruptIndexException, IOException, ParseException {

    	logger.info("Name to fuzzy search for : " + name);
    	// parse the input name
    	NonViralNameParserImpl parser = new NonViralNameParserImpl();
    	NonViralName nvn = parser.parseFullName(name);
    	if(name != null && !name.equals("") && nvn == null) {
    		throw new ParseException("Could not parse name " + name);
    	}
        LuceneSearch luceneSearch = prepareFindByFuzzyNameSearch(null, nvn, accuracy, maxNoOfResults, languages, highlightFragments);

        // --- execute search        
        TopDocs topDocs = luceneSearch.executeSearch(maxNoOfResults);
        
        Map<CdmBaseType, String> idFieldMap = new HashMap<CdmBaseType, String>();

        // --- initialize taxa, highlight matches ....
        ISearchResultBuilder searchResultBuilder = new SearchResultBuilder(luceneSearch, luceneSearch.getQuery());
        
        @SuppressWarnings("rawtypes")
        List<DocumentSearchResult> searchResults = searchResultBuilder.createResultSet(topDocs, luceneSearch.getHighlightFields());

        return searchResults;
    }
    
    public List<DocumentSearchResult> findByNameExactSearch(
            String name,
            boolean wildcard,
            List<Language> languages,
            boolean highlightFragments, 
            int maxNoOfResults) throws CorruptIndexException, IOException, ParseException {

    	logger.info("Name to fuzzy search for : " + name);
    	
        LuceneSearch luceneSearch = prepareFindByExactNameSearch(null, name, wildcard, languages, highlightFragments);

        // --- execute search        
        TopDocs topDocs = luceneSearch.executeSearch(maxNoOfResults);
        Map<CdmBaseType, String> idFieldMap = new HashMap<CdmBaseType, String>();        

        // --- initialize taxa, highlight matches ....
        ISearchResultBuilder searchResultBuilder = new SearchResultBuilder(luceneSearch, luceneSearch.getQuery());
        
        @SuppressWarnings("rawtypes")
        List<DocumentSearchResult> searchResults = searchResultBuilder.createResultSet(topDocs, luceneSearch.getHighlightFields());

        return searchResults;
    }
    
    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.INameService#pageNameRelationships(eu.etaxonomy.cdm.model.name.TaxonNameBase, eu.etaxonomy.cdm.model.common.RelationshipBase.Direction, eu.etaxonomy.cdm.model.name.NameRelationshipType, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    @Override
    public Pager<NameRelationship> pageNameRelationships(TaxonNameBase name, Direction direction, NameRelationshipType type, Integer pageSize,
            Integer pageNumber, List<OrderHint> orderHints,	List<String> propertyPaths) {
        List<NameRelationship> results = listNameRelationships(name, direction, type, pageSize, pageNumber, orderHints, propertyPaths);
        return new DefaultPagerImpl<NameRelationship>(pageNumber, results.size(), pageSize, results);
    }

    @Override
    public List<NameRelationship> listFromNameRelationships(TaxonNameBase name, NameRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        return listNameRelationships(name, Direction.relatedFrom, type, pageSize, pageNumber, orderHints, propertyPaths);
    }

    @Override
    public Pager<NameRelationship> pageFromNameRelationships(TaxonNameBase name, NameRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        List<NameRelationship> results = listNameRelationships(name, Direction.relatedFrom, type, pageSize, pageNumber, orderHints, propertyPaths);
        return new DefaultPagerImpl<NameRelationship>(pageNumber, results.size(), pageSize, results);
    }

    @Override
    public List<NameRelationship> listToNameRelationships(TaxonNameBase name, NameRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        return listNameRelationships(name, Direction.relatedTo, type, pageSize, pageNumber, orderHints, propertyPaths);
    }

    @Override
    public Pager<NameRelationship> pageToNameRelationships(TaxonNameBase name, NameRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        List<NameRelationship> results = listNameRelationships(name, Direction.relatedTo, type, pageSize, pageNumber, orderHints, propertyPaths);
        return new DefaultPagerImpl<NameRelationship>(pageNumber, results.size(), pageSize, results);
    }

    @Override
    public Pager<TypeDesignationBase> getTypeDesignations(TaxonNameBase name, SpecimenTypeDesignationStatus status,
            Integer pageSize, Integer pageNumber) {
        return getTypeDesignations(name, status, pageSize, pageNumber, null);
    }

    @Override
    public Pager<TypeDesignationBase> getTypeDesignations(TaxonNameBase name, SpecimenTypeDesignationStatus status,
                Integer pageSize, Integer pageNumber, List<String> propertyPaths){
        Integer numberOfResults = dao.countTypeDesignations(name, status);

        List<TypeDesignationBase> results = new ArrayList<TypeDesignationBase>();
        if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
            results = dao.getTypeDesignations(name, status, pageSize, pageNumber, propertyPaths);
        }

        return new DefaultPagerImpl<TypeDesignationBase>(pageNumber, numberOfResults, pageSize, results);
    }

    /**
     * FIXME Candidate for harmonization
     * rename search
     */
    @Override
    public Pager<TaxonNameBase> searchNames(String uninomial,String infraGenericEpithet, String specificEpithet, String infraspecificEpithet, Rank rank, Integer pageSize,	Integer pageNumber, List<OrderHint> orderHints,
            List<String> propertyPaths) {
        Integer numberOfResults = dao.countNames(uninomial, infraGenericEpithet, specificEpithet, infraspecificEpithet, rank);

        List<TaxonNameBase> results = new ArrayList<TaxonNameBase>();
        if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
            results = dao.searchNames(uninomial, infraGenericEpithet, specificEpithet, infraspecificEpithet, rank, pageSize, pageNumber, orderHints, propertyPaths);
        }

        return new DefaultPagerImpl<TaxonNameBase>(pageNumber, numberOfResults, pageSize, results);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.INameService#getUuidAndTitleCacheOfNames()
     */
    @Override
    public List<UuidAndTitleCache> getUuidAndTitleCacheOfNames() {
        return dao.getUuidAndTitleCacheOfNames();
    }

    @Override
    public Pager<TaxonNameBase> findByName(Class<? extends TaxonNameBase> clazz, String queryString, MatchMode matchmode, List<Criterion> criteria, Integer pageSize,Integer pageNumber, List<OrderHint> orderHints,List<String> propertyPaths) {
        Integer numberOfResults = dao.countByName(clazz, queryString, matchmode, criteria);

         List<TaxonNameBase> results = new ArrayList<TaxonNameBase>();
         if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
                results = dao.findByName(clazz, queryString, matchmode, criteria, pageSize, pageNumber, orderHints, propertyPaths);
         }

          return new DefaultPagerImpl<TaxonNameBase>(pageNumber, numberOfResults, pageSize, results);
    }

    @Override
    public HomotypicalGroup findHomotypicalGroup(UUID uuid) {
        return homotypicalGroupDao.findByUuid(uuid);
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IIdentifiableEntityService#updateTitleCache(java.lang.Integer, eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy)
     */
    @Override
    @Transactional(readOnly = false)
    public void updateTitleCache(Class<? extends TaxonNameBase> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<TaxonNameBase> cacheStrategy, IProgressMonitor monitor) {
        if (clazz == null){
            clazz = TaxonNameBase.class;
        }
        super.updateTitleCacheImpl(clazz, stepSize, cacheStrategy, monitor);
    }


    @Override
    protected void setOtherCachesNull(TaxonNameBase name) {
        if (name.isInstanceOf(NonViralName.class)){
            NonViralName<?> nvn = CdmBase.deproxy(name, NonViralName.class);
            if (! nvn.isProtectedNameCache()){
                nvn.setNameCache(null, false);
            }
            if (! nvn.isProtectedAuthorshipCache()){
                nvn.setAuthorshipCache(null, false);
            }
            if (! nvn.isProtectedFullTitleCache()){
                nvn.setFullTitleCache(null, false);
            }
        }
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.INameService#getTaggedName(eu.etaxonomy.cdm.model.name.TaxonNameBase)
     */
    @Override
    public List<TaggedText> getTaggedName(UUID uuid) {
        TaxonNameBase taxonNameBase = dao.load(uuid);
        List taggedName = taxonNameBase.getTaggedName();
        return taggedName;
    }


}
