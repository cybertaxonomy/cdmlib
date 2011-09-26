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
import org.hibernate.criterion.Criterion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.AbstractPagerImpl;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.common.IProgressMonitor;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DescriptionElementSource;
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


@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
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
		return delete(name, config);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.INameService#delete(eu.etaxonomy.cdm.model.name.TaxonNameBase, eu.etaxonomy.cdm.api.service.NameDeletionConfigurator)
	 */
	@Override
	public UUID delete(TaxonNameBase name, NameDeletionConfigurator config){
		//check is used
		//relationships
		removeNameRelationshipsByDeleteConfig(name, config);
		
		if (! name.getNameRelations().isEmpty()){
			String message = "Name can't be deleted as it is used in name relationship(s).";
			throw new RuntimeException(message);
		}
		//concepts
		if (! name.getTaxonBases().isEmpty()){
			String message = "Name can't be deleted as it is used in concept(s).";
			throw new RuntimeException(message);
		}
		//hybrid relationships
		if (name.isInstanceOf(NonViralName.class)){
			NonViralName nvn = CdmBase.deproxy(name, NonViralName.class);
			if (! nvn.getHybridChildRelations().isEmpty()){
				String message = "Name can't be deleted as it is a child in (a) hybrid relationship(s).";
				throw new RuntimeException(message);
			}
			if (! nvn.getHybridParentRelations().isEmpty()){
				String message = "Name can't be deleted as it is a parent in (a) hybrid relationship(s).";
				throw new RuntimeException(message);
			}
		}
		
		

		
		Set<CdmBase> referencingObjects = genericDao.getReferencingObjects(name);
		
		for (CdmBase referencingObject : referencingObjects){
			//DerivedUnitBase?.storedUnder
			if (referencingObject.isInstanceOf(DerivedUnitBase.class)){
				String message = "Name can't be deleted as it is used as derivedUnit#storedUnder by %s. Remove 'stored under' prior to deleting this name";
				message = String.format(message, CdmBase.deproxy(referencingObject, DerivedUnitBase.class).getTitleCache());
				throw new RuntimeException(message);
			}
			//DescriptionElementSource#nameUsedInSource
			if (referencingObject.isInstanceOf(DescriptionElementSource.class)){
				String message = "Name can't be deleted as it is used as descriptionElementSource#nameUsedInSource";
//				message = String.format(message, CdmBase.deproxy(referencingObject, DerivedUnitBase.class).getTitleCache());
				throw new RuntimeException(message);
			}
			//TaxonNameDescriptions#taxonName
			//should be deleted via cascade?
//			if (referencingObject.isInstanceOf(TaxonNameDescription.class)){
//				String message = "Name can't be deleted as it is has taxon name description(s)";
//				throw new RuntimeException(message);
//			}
			
			//NomenclaturalStatus
			//should be deleted via cascade?
//		    
				
		}
		
		//check is used
//	    which name relations can be automatically deleted without throwing an exception

		//	    TypeDesignations?
		

//	    inline references
		
		
		dao.delete(name);
		return name.getUuid();
	}

	/**
	 * @param name
	 * @param config
	 */
	private void removeNameRelationshipsByDeleteConfig(TaxonNameBase name,
			NameDeletionConfigurator config) {
		if (config.isRemoveAllNameRelationships()){
			Set<NameRelationship> rels = name.getNameRelations();
			for (NameRelationship rel : rels){
				name.removeNameRelationship(rel);
			}
		}else{
			Set<NameRelationship> rels = name.getRelationsToThisName();
			for (NameRelationship rel : rels){
				if (config.isIgnoreHasBasionym() || config.isIgnoreHasReplacedSynonym()){
					if (NameRelationshipType.BASIONYM().equals(rel.getType())){
						name.removeNameRelationship(rel);
					}
				}
			}
			rels = name.getRelationsFromThisName();
			for (NameRelationship rel : rels){
				if (config.isIgnoreIsBasionymFor() || config.isIgnoreIsReplacedSynonymFor()  ){
					if (NameRelationshipType.BASIONYM().equals(rel.getType())){
						name.removeNameRelationship(rel);
					}
				}
			}
			
		}
	}

//********************* METHODS ****************************************************************//


    public List getNamesByName(String name){
        return super.findCdmObjectsByTitle(name);
    }

    public List<NonViralName> getNamesByNameCache(String nameCache){
        List result = dao.findByName(nameCache, MatchMode.EXACT, null, null, null, null);
        return result;
    }

    public List getNamesByName(String name, CdmBase sessionObject){
        return super.findCdmObjectsByTitle(name, sessionObject);
    }

    public List findNamesByTitle(String title){
        return super.findCdmObjectsByTitle(title);
    }

    public List findNamesByTitle(String title, CdmBase sessionObject){
        return super.findCdmObjectsByTitle(title, sessionObject);
    }

    @Transactional(readOnly = false)
    public Map<UUID, HomotypicalGroup> saveAllHomotypicalGroups(Collection<HomotypicalGroup> homotypicalGroups){
        return homotypicalGroupDao.saveAll(homotypicalGroups);
    }

    @Transactional(readOnly = false)
    public Map<UUID, TypeDesignationBase> saveTypeDesignationAll(Collection<TypeDesignationBase> typeDesignationCollection){
        return typeDesignationDao.saveAll(typeDesignationCollection);
    }

    @Transactional(readOnly = false)
    public Map<UUID, ReferencedEntityBase> saveReferencedEntitiesAll(Collection<ReferencedEntityBase> referencedEntityCollection){
        return referencedEntityDao.saveAll(referencedEntityCollection);
    }

    public List<TaxonNameBase> getAllNames(int limit, int start){
        return dao.list(limit, start);
    }

    public List<NomenclaturalStatus> getAllNomenclaturalStatus(int limit, int start){
        return nomStatusDao.list(limit, start);
    }

    public List<TypeDesignationBase> getAllTypeDesignations(int limit, int start){
        return typeDesignationDao.getAllTypeDesignations(limit, start);
    }
      /**
     * FIXME Candidate for harmonization
     * homotypicalGroupService.list
     */
    public List<HomotypicalGroup> getAllHomotypicalGroups(int limit, int start){
        return homotypicalGroupDao.list(limit, start);
    }

    /**
     * FIXME Candidate for harmonization
     * remove
     */
    @Deprecated
    public List<RelationshipBase> getAllRelationships(int limit, int start){
        return dao.getRelationships(limit, start);
    }

    /**
     * FIXME Candidate for harmonization
     * is this the same as termService.getVocabulary(VocabularyEnum.Rank)
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.INameService#getRankVocabulary()
     */
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
    public TermVocabulary<NameRelationshipType> getNameRelationshipTypeVocabulary() {
        String uuidString = "6878cb82-c1a4-4613-b012-7e73b413c8cd";
        UUID uuid = UUID.fromString(uuidString);
        TermVocabulary<NameRelationshipType> nameRelTypeVocabulary =
            (TermVocabulary)vocabularyDao.findByUuid(uuid);
        return nameRelTypeVocabulary;
    }

    /**
      * FIXME Candidate for harmonization
     * is this the same as termService.getVocabulary(VocabularyEnum.StatusType)
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.INameService#getStatusTypeVocabulary()
     */
    public TermVocabulary<NomenclaturalStatusType> getStatusTypeVocabulary() {
        String uuidString = "bb28cdca-2f8a-4f11-9c21-517e9ae87f1f";
        UUID uuid = UUID.fromString(uuidString);
        TermVocabulary<NomenclaturalStatusType> nomStatusTypeVocabulary =
            (TermVocabulary)vocabularyDao.findByUuid(uuid);
        return nomStatusTypeVocabulary;
    }

    /**
      * FIXME Candidate for harmonization
     * is this the same as termService.getVocabulary(VocabularyEnum.SpecimenTypeDesignationStatus)
     *  (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.INameService#getTypeDesignationStatusVocabulary()
     */
    public TermVocabulary<SpecimenTypeDesignationStatus> getSpecimenTypeDesignationStatusVocabulary() {
        String uuidString = "ab177bd7-d3c8-4e58-a388-226fff6ba3c2";
        UUID uuid = UUID.fromString(uuidString);
        TermVocabulary<SpecimenTypeDesignationStatus> typeDesigStatusVocabulary =
            (TermVocabulary)vocabularyDao.findByUuid(uuid);
        return typeDesigStatusVocabulary;
    }

    /**
       * FIXME Candidate for harmonization
     * is this the same as termService.getVocabulary(VocabularyEnum.SpecimenTypeDesignationStatus)
     * and also seems to duplicate the above method, differing only in the DAO used and the return type
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.INameService#getTypeDesignationStatusVocabulary()
     */
    public OrderedTermVocabulary<SpecimenTypeDesignationStatus> getSpecimenTypeDesignationVocabulary() {
        String uuidString = "ab177bd7-d3c8-4e58-a388-226fff6ba3c2";
        UUID uuid = UUID.fromString(uuidString);
        OrderedTermVocabulary<SpecimenTypeDesignationStatus> typeDesignationVocabulary =
            (OrderedTermVocabulary)orderedVocabularyDao.findByUuid(uuid);
        return typeDesignationVocabulary;
    }


    @Autowired
    protected void setDao(ITaxonNameDao dao) {
        this.dao = dao;
    }

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

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.INameService#pageNameRelationships(eu.etaxonomy.cdm.model.name.TaxonNameBase, eu.etaxonomy.cdm.model.common.RelationshipBase.Direction, eu.etaxonomy.cdm.model.name.NameRelationshipType, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    @Override
    public Pager<NameRelationship> pageNameRelationships(TaxonNameBase name, Direction direction, NameRelationshipType type, Integer pageSize,
            Integer pageNumber, List<OrderHint> orderHints,	List<String> propertyPaths) {
        List<NameRelationship> results = listNameRelationships(name, direction, type, pageSize, pageNumber, orderHints, propertyPaths);
        return new DefaultPagerImpl<NameRelationship>(pageNumber, results.size(), pageSize, results);
    }

    public List<NameRelationship> listFromNameRelationships(TaxonNameBase name, NameRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        return listNameRelationships(name, Direction.relatedFrom, type, pageSize, pageNumber, orderHints, propertyPaths);
    }

    public Pager<NameRelationship> pageFromNameRelationships(TaxonNameBase name, NameRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        List<NameRelationship> results = listNameRelationships(name, Direction.relatedFrom, type, pageSize, pageNumber, orderHints, propertyPaths);
        return new DefaultPagerImpl<NameRelationship>(pageNumber, results.size(), pageSize, results);
    }

    public List<NameRelationship> listToNameRelationships(TaxonNameBase name, NameRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        return listNameRelationships(name, Direction.relatedTo, type, pageSize, pageNumber, orderHints, propertyPaths);
    }

    public Pager<NameRelationship> pageToNameRelationships(TaxonNameBase name, NameRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        List<NameRelationship> results = listNameRelationships(name, Direction.relatedTo, type, pageSize, pageNumber, orderHints, propertyPaths);
        return new DefaultPagerImpl<NameRelationship>(pageNumber, results.size(), pageSize, results);
    }

    public Pager<TypeDesignationBase> getTypeDesignations(TaxonNameBase name, SpecimenTypeDesignationStatus status,
            Integer pageSize, Integer pageNumber) {
        return getTypeDesignations(name, status, pageSize, pageNumber, null);
    }

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
    public List<UuidAndTitleCache> getUuidAndTitleCacheOfNames() {
        return dao.getUuidAndTitleCacheOfNames();
    }

    public Pager<TaxonNameBase> findByName(Class<? extends TaxonNameBase> clazz, String queryString, MatchMode matchmode, List<Criterion> criteria, Integer pageSize,Integer pageNumber, List<OrderHint> orderHints,List<String> propertyPaths) {
        Integer numberOfResults = dao.countByName(clazz, queryString, matchmode, criteria);

         List<TaxonNameBase> results = new ArrayList<TaxonNameBase>();
         if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
                results = dao.findByName(clazz, queryString, matchmode, criteria, pageSize, pageNumber, orderHints, propertyPaths);
         }

          return new DefaultPagerImpl<TaxonNameBase>(pageNumber, numberOfResults, pageSize, results);
    }

    public HomotypicalGroup findHomotypicalGroup(UUID uuid) {
        return homotypicalGroupDao.findByUuid(uuid);
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IIdentifiableEntityService#updateTitleCache(java.lang.Integer, eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy)
     */
    @Override
    public void updateTitleCache(Class<? extends TaxonNameBase> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<TaxonNameBase> cacheStrategy, IProgressMonitor monitor) {
        if (clazz == null){
            clazz = TaxonNameBase.class;
        }
        super.updateTitleCacheImpl(clazz, stepSize, cacheStrategy, monitor);
    }


    @Override
    protected void setOtherCachesNull(TaxonNameBase name) {
        if (name.isInstanceOf(NonViralName.class)){
            NonViralName nvn = CdmBase.deproxy(name, NonViralName.class);
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
