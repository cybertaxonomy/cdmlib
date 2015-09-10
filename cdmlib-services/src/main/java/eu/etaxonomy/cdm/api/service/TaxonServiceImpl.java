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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanFilter;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.SortField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.config.DeleteConfiguratorBase;
import eu.etaxonomy.cdm.api.service.config.IFindTaxaAndNamesConfigurator;
import eu.etaxonomy.cdm.api.service.config.IncludedTaxonConfiguration;
import eu.etaxonomy.cdm.api.service.config.MatchingTaxonConfigurator;
import eu.etaxonomy.cdm.api.service.config.SynonymDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.config.TaxonDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.config.TaxonNodeDeletionConfigurator.ChildHandling;
import eu.etaxonomy.cdm.api.service.dto.FindByIdentifierDTO;
import eu.etaxonomy.cdm.api.service.dto.IncludedTaxaDTO;
import eu.etaxonomy.cdm.api.service.exception.DataChangeNoRollbackException;
import eu.etaxonomy.cdm.api.service.exception.HomotypicalGroupChangeException;
import eu.etaxonomy.cdm.api.service.exception.ReferencedObjectUndeletableException;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.api.service.search.ILuceneIndexToolProvider;
import eu.etaxonomy.cdm.api.service.search.ISearchResultBuilder;
import eu.etaxonomy.cdm.api.service.search.LuceneMultiSearch;
import eu.etaxonomy.cdm.api.service.search.LuceneMultiSearchException;
import eu.etaxonomy.cdm.api.service.search.LuceneSearch;
import eu.etaxonomy.cdm.api.service.search.LuceneSearch.TopGroupsWithMaxScore;
import eu.etaxonomy.cdm.api.service.search.QueryFactory;
import eu.etaxonomy.cdm.api.service.search.SearchResult;
import eu.etaxonomy.cdm.api.service.search.SearchResultBuilder;
import eu.etaxonomy.cdm.api.service.util.TaxonRelationshipEdge;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.hibernate.search.DefinedTermBaseClassBridge;
import eu.etaxonomy.cdm.hibernate.search.GroupByTaxonClassBridge;
import eu.etaxonomy.cdm.hibernate.search.MultilanguageTextFieldBridge;
import eu.etaxonomy.cdm.model.CdmBaseType;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.RelationshipBase.Direction;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IIdentificationKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonInteraction;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaUtils;
import eu.etaxonomy.cdm.model.molecular.AmplificationResult;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.molecular.SingleRead;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.ITaxonTreeNode;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;
import eu.etaxonomy.cdm.persistence.dao.common.IOrderedTermVocabularyDao;
import eu.etaxonomy.cdm.persistence.dao.initializer.AbstractBeanInitializer;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeDao;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.fetch.CdmFetch;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;


/**
 * @author a.kohlbecker
 * @date 10.09.2010
 *
 */
@Service
@Transactional(readOnly = true)
public class TaxonServiceImpl extends IdentifiableServiceBase<TaxonBase,ITaxonDao> implements ITaxonService{
    private static final Logger logger = Logger.getLogger(TaxonServiceImpl.class);

    public static final String POTENTIAL_COMBINATION_NAMESPACE = "Potential combination";

    public static final String INFERRED_EPITHET_NAMESPACE = "Inferred epithet";

    public static final String INFERRED_GENUS_NAMESPACE = "Inferred genus";

    @Autowired
    private ITaxonNodeDao taxonNodeDao;

    @Autowired
    private ITaxonNameDao nameDao;

    @Autowired
    private INameService nameService;

    @Autowired
    private ITaxonNodeService nodeService;

    @Autowired
    private ICdmGenericDao genericDao;

    @Autowired
    private IDescriptionService descriptionService;

    @Autowired
    private IOrderedTermVocabularyDao orderedVocabularyDao;

    @Autowired
    private IOccurrenceDao occurrenceDao;

    @Autowired
    private IClassificationDao classificationDao;

    @Autowired
    private AbstractBeanInitializer beanInitializer;

    @Autowired
    private ILuceneIndexToolProvider luceneIndexToolProvider;

    /**
     * Constructor
     */
    public TaxonServiceImpl(){
        if (logger.isDebugEnabled()) { logger.debug("Load TaxonService Bean"); }
    }

    /**
     * FIXME Candidate for harmonization
     * rename searchByName ?
     */
    @Override
    public List<TaxonBase> searchTaxaByName(String name, Reference sec) {
        return dao.getTaxaByName(name, sec);
    }

    /**
     * FIXME Candidate for harmonization
     * merge with getRootTaxa(Reference sec, ..., ...)
     *  (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#getRootTaxa(eu.etaxonomy.cdm.model.reference.Reference, boolean)
     */
    @Override
    public List<Taxon> getRootTaxa(Reference sec, CdmFetch cdmFetch, boolean onlyWithChildren) {
        if (cdmFetch == null){
            cdmFetch = CdmFetch.NO_FETCH();
        }
        return dao.getRootTaxa(sec, cdmFetch, onlyWithChildren, false);
    }

    @Override
    public List<Taxon> getRootTaxa(Rank rank, Reference sec, boolean onlyWithChildren,boolean withMisapplications, List<String> propertyPaths) {
        return dao.getRootTaxa(rank, sec, null, onlyWithChildren, withMisapplications, propertyPaths);
    }

    @Override
    public List<RelationshipBase> getAllRelationships(int limit, int start){
        return dao.getAllRelationships(limit, start);
    }

    /**
     * FIXME Candidate for harmonization
     * is this the same as termService.getVocabulary(VocabularyEnum.TaxonRelationshipType) ?
     */
    @Override
    @Deprecated
    public OrderedTermVocabulary<TaxonRelationshipType> getTaxonRelationshipTypeVocabulary() {

        String taxonRelTypeVocabularyId = "15db0cf7-7afc-4a86-a7d4-221c73b0c9ac";
        UUID uuid = UUID.fromString(taxonRelTypeVocabularyId);
        OrderedTermVocabulary<TaxonRelationshipType> taxonRelTypeVocabulary =
            (OrderedTermVocabulary)orderedVocabularyDao.findByUuid(uuid);
        return taxonRelTypeVocabulary;
    }

    @Override
    @Transactional(readOnly = false)
    public UpdateResult swapSynonymAndAcceptedTaxon(Synonym synonym, Taxon acceptedTaxon){
    	UpdateResult result = new UpdateResult();
        TaxonNameBase<?,?> synonymName = synonym.getName();
        synonymName.removeTaxonBase(synonym);
        TaxonNameBase<?,?> taxonName = acceptedTaxon.getName();
        taxonName.removeTaxonBase(acceptedTaxon);

        synonym.setName(taxonName);
        acceptedTaxon.setName(synonymName);
        saveOrUpdate(synonym);
        saveOrUpdate(acceptedTaxon);
        result.addUpdatedObject(acceptedTaxon);
        result.addUpdatedObject(synonym);
		return result;

        // the accepted taxon needs a new uuid because the concept has changed
        // FIXME this leads to an error "HibernateException: immutable natural identifier of an instance of eu.etaxonomy.cdm.model.taxon.Taxon was altered"
        //acceptedTaxon.setUuid(UUID.randomUUID());
    }


    @Override
    @Transactional(readOnly = false)
    public Taxon changeSynonymToAcceptedTaxon(Synonym synonym, Taxon acceptedTaxon, boolean deleteSynonym, boolean copyCitationInfo, Reference citation, String microCitation) throws HomotypicalGroupChangeException{

        TaxonNameBase<?,?> acceptedName = acceptedTaxon.getName();
        TaxonNameBase<?,?> synonymName = synonym.getName();
        HomotypicalGroup synonymHomotypicGroup = synonymName.getHomotypicalGroup();

        //check synonym is not homotypic
        if (acceptedName.getHomotypicalGroup().equals(synonymHomotypicGroup)){
            String message = "The accepted taxon and the synonym are part of the same homotypical group and therefore can not be both accepted.";
            throw new HomotypicalGroupChangeException(message);
        }

        Taxon newAcceptedTaxon = Taxon.NewInstance(synonymName, acceptedTaxon.getSec());
        dao.save(newAcceptedTaxon);
        SynonymRelationshipType relTypeForGroup = SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF();
        List<Synonym> heteroSynonyms = acceptedTaxon.getSynonymsInGroup(synonymHomotypicGroup);
        Set<NameRelationship> basionymsAndReplacedSynonyms = synonymHomotypicGroup.getBasionymAndReplacedSynonymRelations();

        for (Synonym heteroSynonym : heteroSynonyms){
            if (synonym.equals(heteroSynonym)){
                acceptedTaxon.removeSynonym(heteroSynonym, false);

            }else{
                //move synonyms in same homotypic group to new accepted taxon
                heteroSynonym.replaceAcceptedTaxon(newAcceptedTaxon, relTypeForGroup, copyCitationInfo, citation, microCitation);
            }
        }
        dao.saveOrUpdate(acceptedTaxon);
        //synonym.getName().removeTaxonBase(synonym);

        if (deleteSynonym){
//			deleteSynonym(synonym, taxon, false);
            try {
                this.dao.flush();
                SynonymDeletionConfigurator config = new SynonymDeletionConfigurator();
                config.setDeleteNameIfPossible(false);
                this.deleteSynonym(synonym, acceptedTaxon, config);

            } catch (Exception e) {
                logger.info("Can't delete old synonym from database");
            }
        }

        return newAcceptedTaxon;
    }

    @Override
    @Transactional(readOnly = false)
    public UpdateResult changeSynonymToAcceptedTaxon(UUID synonymUuid,
            UUID acceptedTaxonUuid,
            UUID newParentNodeUuid,
            boolean deleteSynonym,
            boolean copyCitationInfo,
            Reference citation,
            String microCitation) throws HomotypicalGroupChangeException {
        UpdateResult result = new UpdateResult();
        Synonym synonym = CdmBase.deproxy(dao.load(synonymUuid), Synonym.class);
        Taxon acceptedTaxon = CdmBase.deproxy(dao.load(acceptedTaxonUuid), Taxon.class);
        Taxon taxon =  changeSynonymToAcceptedTaxon(synonym, acceptedTaxon, deleteSynonym, copyCitationInfo, citation, microCitation);
        TaxonNode newParentNode = taxonNodeDao.load(newParentNodeUuid);
        TaxonNode newNode = newParentNode.addChildTaxon(taxon, null, null);
        taxonNodeDao.save(newNode);
        result.addUpdatedObject(taxon);
        result.addUpdatedObject(acceptedTaxon);
        result.setCdmEntity(newNode);
        return result;
    }

    @Override
    @Transactional(readOnly = false)
    public UpdateResult changeSynonymToRelatedTaxon(UUID synonymUuid,
            UUID toTaxonUuid,
            TaxonRelationshipType taxonRelationshipType,
            Reference citation,
            String microcitation){

        UpdateResult result = new UpdateResult();
        Taxon toTaxon = (Taxon) dao.load(toTaxonUuid);
        Synonym synonym = (Synonym) dao.load(synonymUuid);
        Taxon relatedTaxon = changeSynonymToRelatedTaxon(synonym, toTaxon, taxonRelationshipType, citation, microcitation);
        result.setCdmEntity(relatedTaxon);
        result.addUpdatedObject(relatedTaxon);
        result.addUpdatedObject(toTaxon);
        return result;
    }

    @Override
    @Transactional(readOnly = false)
    public Taxon changeSynonymToRelatedTaxon(Synonym synonym, Taxon toTaxon, TaxonRelationshipType taxonRelationshipType, Reference citation, String microcitation){

        // Get name from synonym
        TaxonNameBase<?, ?> synonymName = synonym.getName();

      /*  // remove synonym from taxon
        toTaxon.removeSynonym(synonym);
*/
        // Create a taxon with synonym name
        Taxon fromTaxon = Taxon.NewInstance(synonymName, null);

        // Add taxon relation
        fromTaxon.addTaxonRelation(toTaxon, taxonRelationshipType, citation, microcitation);

        // since we are swapping names, we have to detach the name from the synonym completely.
        // Otherwise the synonym will still be in the list of typified names.
       // synonym.getName().removeTaxonBase(synonym);
        this.deleteSynonym(synonym, null);

        return fromTaxon;
    }

    @Transactional(readOnly = false)
    @Override
    public void changeHomotypicalGroupOfSynonym(Synonym synonym, HomotypicalGroup newHomotypicalGroup, Taxon targetTaxon,
                        boolean removeFromOtherTaxa, boolean setBasionymRelationIfApplicable){
        // Get synonym name
        TaxonNameBase synonymName = synonym.getName();
        HomotypicalGroup oldHomotypicalGroup = synonymName.getHomotypicalGroup();


        // Switch groups
        oldHomotypicalGroup.removeTypifiedName(synonymName, false);
        newHomotypicalGroup.addTypifiedName(synonymName);

        //remove existing basionym relationships
        synonymName.removeBasionyms();

        //add basionym relationship
        if (setBasionymRelationIfApplicable){
            Set<TaxonNameBase> basionyms = newHomotypicalGroup.getBasionyms();
            for (TaxonNameBase basionym : basionyms){
                synonymName.addBasionym(basionym);
            }
        }

        //set synonym relationship correctly
//			SynonymRelationship relToTaxon = null;
        boolean relToTargetTaxonExists = false;
        Set<SynonymRelationship> existingRelations = synonym.getSynonymRelations();
        for (SynonymRelationship rel : existingRelations){
            Taxon acceptedTaxon = rel.getAcceptedTaxon();
            boolean isTargetTaxon = acceptedTaxon != null && acceptedTaxon.equals(targetTaxon);
            HomotypicalGroup acceptedGroup = acceptedTaxon.getHomotypicGroup();
            boolean isHomotypicToTaxon = acceptedGroup.equals(newHomotypicalGroup);
            SynonymRelationshipType newRelationType = isHomotypicToTaxon? SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF() : SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF();
            rel.setType(newRelationType);
            //TODO handle citation and microCitation

            if (isTargetTaxon){
                relToTargetTaxonExists = true;
            }else{
                if (removeFromOtherTaxa){
                    acceptedTaxon.removeSynonym(synonym, false);
                }else{
                    //do nothing
                }
            }
        }
        if (targetTaxon != null &&  ! relToTargetTaxonExists ){
            Taxon acceptedTaxon = targetTaxon;
            HomotypicalGroup acceptedGroup = acceptedTaxon.getHomotypicGroup();
            boolean isHomotypicToTaxon = acceptedGroup.equals(newHomotypicalGroup);
            SynonymRelationshipType relType = isHomotypicToTaxon? SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF() : SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF();
            //TODO handle citation and microCitation
            Reference citation = null;
            String microCitation = null;
            acceptedTaxon.addSynonym(synonym, relType, citation, microCitation);
        }

    }

    @Override
    @Transactional(readOnly = false)
    public void updateTitleCache(Class<? extends TaxonBase> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<TaxonBase> cacheStrategy, IProgressMonitor monitor) {
        if (clazz == null){
            clazz = TaxonBase.class;
        }
        super.updateTitleCacheImpl(clazz, stepSize, cacheStrategy, monitor);
    }

    @Override
    @Autowired
    protected void setDao(ITaxonDao dao) {
        this.dao = dao;
    }

    @Override
    public Pager<TaxonBase> findTaxaByName(Class<? extends TaxonBase> clazz, String uninomial,	String infragenericEpithet, String specificEpithet,	String infraspecificEpithet, Rank rank, Integer pageSize,Integer pageNumber) {
        Integer numberOfResults = dao.countTaxaByName(clazz, uninomial, infragenericEpithet, specificEpithet, infraspecificEpithet, rank);

        List<TaxonBase> results = new ArrayList<TaxonBase>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.findTaxaByName(clazz, uninomial, infragenericEpithet, specificEpithet, infraspecificEpithet, rank, pageSize, pageNumber);
        }

        return new DefaultPagerImpl<TaxonBase>(pageNumber, numberOfResults, pageSize, results);
    }

    @Override
    public List<TaxonBase> listTaxaByName(Class<? extends TaxonBase> clazz, String uninomial,	String infragenericEpithet, String specificEpithet,	String infraspecificEpithet, Rank rank, Integer pageSize,Integer pageNumber) {
        Integer numberOfResults = dao.countTaxaByName(clazz, uninomial, infragenericEpithet, specificEpithet, infraspecificEpithet, rank);

        List<TaxonBase> results = new ArrayList<TaxonBase>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.findTaxaByName(clazz, uninomial, infragenericEpithet, specificEpithet, infraspecificEpithet, rank, pageSize, pageNumber);
        }

        return results;
    }

    @Override
    public List<TaxonRelationship> listToTaxonRelationships(Taxon taxon, TaxonRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths){
        Integer numberOfResults = dao.countTaxonRelationships(taxon, type, TaxonRelationship.Direction.relatedTo);

        List<TaxonRelationship> results = new ArrayList<TaxonRelationship>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.getTaxonRelationships(taxon, type, pageSize, pageNumber, orderHints, propertyPaths, TaxonRelationship.Direction.relatedTo);
        }
        return results;
    }

    @Override
    public Pager<TaxonRelationship> pageToTaxonRelationships(Taxon taxon, TaxonRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.countTaxonRelationships(taxon, type, TaxonRelationship.Direction.relatedTo);

        List<TaxonRelationship> results = new ArrayList<TaxonRelationship>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.getTaxonRelationships(taxon, type, pageSize, pageNumber, orderHints, propertyPaths, TaxonRelationship.Direction.relatedTo);
        }
        return new DefaultPagerImpl<TaxonRelationship>(pageNumber, numberOfResults, pageSize, results);
    }

    @Override
    public List<TaxonRelationship> listFromTaxonRelationships(Taxon taxon, TaxonRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths){
        Integer numberOfResults = dao.countTaxonRelationships(taxon, type, TaxonRelationship.Direction.relatedFrom);

        List<TaxonRelationship> results = new ArrayList<TaxonRelationship>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.getTaxonRelationships(taxon, type, pageSize, pageNumber, orderHints, propertyPaths, TaxonRelationship.Direction.relatedFrom);
        }
        return results;
    }

    @Override
    public Pager<TaxonRelationship> pageFromTaxonRelationships(Taxon taxon, TaxonRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.countTaxonRelationships(taxon, type, TaxonRelationship.Direction.relatedFrom);

        List<TaxonRelationship> results = new ArrayList<TaxonRelationship>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.getTaxonRelationships(taxon, type, pageSize, pageNumber, orderHints, propertyPaths, TaxonRelationship.Direction.relatedFrom);
        }
        return new DefaultPagerImpl<TaxonRelationship>(pageNumber, numberOfResults, pageSize, results);
    }

    @Override
    public List<Taxon> listAcceptedTaxaFor(UUID synonymUuid, UUID classificationUuid, Integer pageSize, Integer pageNumber,
            List<OrderHint> orderHints, List<String> propertyPaths){
        return pageAcceptedTaxaFor(synonymUuid, classificationUuid, pageSize, pageNumber, orderHints, propertyPaths).getRecords();
    }

    @Override
    public Pager<Taxon> pageAcceptedTaxaFor(UUID synonymUuid, UUID classificationUuid, Integer pageSize, Integer pageNumber,
            List<OrderHint> orderHints, List<String> propertyPaths){

        List<Taxon> list = new ArrayList<Taxon>();
        Long count = 0l;

        Synonym synonym = null;

        try {
            synonym = (Synonym) dao.load(synonymUuid);
        } catch (ClassCastException e){
            throw new EntityNotFoundException("The TaxonBase entity referenced by " + synonymUuid + " is not a Synonmy");
        } catch (NullPointerException e){
            throw new EntityNotFoundException("No TaxonBase entity found for " + synonymUuid);
        }

        Classification classificationFilter = null;
        if(classificationUuid != null){
            try {
            classificationFilter = classificationDao.load(classificationUuid);
            } catch (NullPointerException e){
                throw new EntityNotFoundException("No Classification entity found for " + classificationUuid);
            }
            if(classificationFilter == null){

            }
        }

        count = dao.countAcceptedTaxaFor(synonym, classificationFilter) ;
        if(count > (pageSize * pageNumber)){
            list = dao.listAcceptedTaxaFor(synonym, classificationFilter, pageSize, pageNumber, orderHints, propertyPaths);
        }

        return new DefaultPagerImpl<Taxon>(pageNumber, count.intValue(), pageSize, list);
    }


    @Override
    public Set<Taxon> listRelatedTaxa(Taxon taxon, Set<TaxonRelationshipEdge> includeRelationships, Integer maxDepth,
            Integer limit, Integer start, List<String> propertyPaths) {

        Set<Taxon> relatedTaxa = collectRelatedTaxa(taxon, includeRelationships, new HashSet<Taxon>(), maxDepth);
        relatedTaxa.remove(taxon);
        beanInitializer.initializeAll(relatedTaxa, propertyPaths);
        return relatedTaxa;
    }


    /**
     * recursively collect related taxa for the given <code>taxon</code> . The returned list will also include the
     *  <code>taxon</code> supplied as parameter.
     *
     * @param taxon
     * @param includeRelationships
     * @param taxa
     * @param maxDepth can be <code>null</code> for infinite depth
     * @return
     */
    private Set<Taxon> collectRelatedTaxa(Taxon taxon, Set<TaxonRelationshipEdge> includeRelationships, Set<Taxon> taxa, Integer maxDepth) {

        if(taxa.isEmpty()) {
            taxa.add(taxon);
        }

        if(includeRelationships.isEmpty()){
            return taxa;
        }

        if(maxDepth != null) {
            maxDepth--;
        }
        if(logger.isDebugEnabled()){
            logger.debug("collecting related taxa for " + taxon + " with maxDepth=" + maxDepth);
        }
        List<TaxonRelationship> taxonRelationships = dao.getTaxonRelationships(taxon, null, null, null, null, null, null);
        for (TaxonRelationship taxRel : taxonRelationships) {

            // skip invalid data
            if (taxRel.getToTaxon() == null || taxRel.getFromTaxon() == null || taxRel.getType() == null) {
                continue;
            }
            // filter by includeRelationships
            for (TaxonRelationshipEdge relationshipEdgeFilter : includeRelationships) {
                if ( relationshipEdgeFilter.getTaxonRelationshipType().equals(taxRel.getType()) ) {
                    if (relationshipEdgeFilter.getDirections().contains(Direction.relatedTo) && !taxa.contains(taxRel.getToTaxon())) {
                        if(logger.isDebugEnabled()){
                            logger.debug(maxDepth + ": " + taxon.getTitleCache() + " --[" + taxRel.getType().getLabel() + "]--> " + taxRel.getToTaxon().getTitleCache());
                        }
                        taxa.add(taxRel.getToTaxon());
                        if(maxDepth == null || maxDepth > 0) {
                            taxa.addAll(collectRelatedTaxa(taxRel.getToTaxon(), includeRelationships, taxa, maxDepth));
                        }
                    }
                    if(relationshipEdgeFilter.getDirections().contains(Direction.relatedFrom) && !taxa.contains(taxRel.getFromTaxon())) {
                        taxa.add(taxRel.getFromTaxon());
                        if(logger.isDebugEnabled()){
                            logger.debug(maxDepth + ": " +taxRel.getFromTaxon().getTitleCache() + " --[" + taxRel.getType().getLabel() + "]--> " + taxon.getTitleCache() );
                        }
                        if(maxDepth == null || maxDepth > 0) {
                            taxa.addAll(collectRelatedTaxa(taxRel.getFromTaxon(), includeRelationships, taxa, maxDepth));
                        }
                    }
                }
            }
        }
        return taxa;
    }

    @Override
    public Pager<SynonymRelationship> getSynonyms(Taxon taxon,	SynonymRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.countSynonyms(taxon, type);

        List<SynonymRelationship> results = new ArrayList<SynonymRelationship>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.getSynonyms(taxon, type, pageSize, pageNumber, orderHints, propertyPaths);
        }

        return new DefaultPagerImpl<SynonymRelationship>(pageNumber, numberOfResults, pageSize, results);
    }

    @Override
    public Pager<SynonymRelationship> getSynonyms(Synonym synonym,	SynonymRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.countSynonyms(synonym, type);

        List<SynonymRelationship> results = new ArrayList<SynonymRelationship>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.getSynonyms(synonym, type, pageSize, pageNumber, orderHints, propertyPaths);
        }

        return new DefaultPagerImpl<SynonymRelationship>(pageNumber, numberOfResults, pageSize, results);
    }

    @Override
    public List<List<Synonym>> getSynonymsByHomotypicGroup(Taxon taxon, List<String> propertyPaths){
         List<List<Synonym>> result = new ArrayList<List<Synonym>>();
        Taxon t = (Taxon)dao.load(taxon.getUuid(), propertyPaths);

        //homotypic
        result.add(t.getHomotypicSynonymsByHomotypicGroup());

        //heterotypic
        List<HomotypicalGroup> homotypicalGroups = t.getHeterotypicSynonymyGroups();
        for(HomotypicalGroup homotypicalGroup : homotypicalGroups){
            result.add(t.getSynonymsInGroup(homotypicalGroup));
        }

        return result;

    }

    @Override
    public List<Synonym> getHomotypicSynonymsByHomotypicGroup(Taxon taxon, List<String> propertyPaths){
        Taxon t = (Taxon)dao.load(taxon.getUuid(), propertyPaths);
        return t.getHomotypicSynonymsByHomotypicGroup();
    }

    @Override
    public List<List<Synonym>> getHeterotypicSynonymyGroups(Taxon taxon, List<String> propertyPaths){
        Taxon t = (Taxon)dao.load(taxon.getUuid(), propertyPaths);
        List<HomotypicalGroup> homotypicalGroups = t.getHeterotypicSynonymyGroups();
        List<List<Synonym>> heterotypicSynonymyGroups = new ArrayList<List<Synonym>>(homotypicalGroups.size());
        for(HomotypicalGroup homotypicalGroup : homotypicalGroups){
            heterotypicSynonymyGroups.add(t.getSynonymsInGroup(homotypicalGroup));
        }
        return heterotypicSynonymyGroups;
    }

    @Override
    public List<UuidAndTitleCache<IdentifiableEntity>> findTaxaAndNamesForEditor(IFindTaxaAndNamesConfigurator configurator){

        List<UuidAndTitleCache<IdentifiableEntity>> results = new ArrayList<UuidAndTitleCache<IdentifiableEntity>>();


        if (configurator.isDoSynonyms() || configurator.isDoTaxa() || configurator.isDoNamesWithoutTaxa()){
        	results = dao.getTaxaByNameForEditor(configurator.isDoTaxa(), configurator.isDoSynonyms(), configurator.isDoNamesWithoutTaxa(), configurator.isDoMisappliedNames(),configurator.getTitleSearchStringSqlized(), configurator.getClassification(), configurator.getMatchMode(), configurator.getNamedAreas());
        }
        if (configurator.isDoTaxaByCommonNames()) {
            //if(configurator.getPageSize() == null ){
                List<UuidAndTitleCache<IdentifiableEntity>> commonNameResults = dao.getTaxaByCommonNameForEditor(configurator.getTitleSearchStringSqlized(), configurator.getClassification(), configurator.getMatchMode(), configurator.getNamedAreas());
                if(commonNameResults != null){
                    results.addAll(commonNameResults);
                }
           // }
        }
        return results;
    }

    @Override
    public Pager<IdentifiableEntity> findTaxaAndNames(IFindTaxaAndNamesConfigurator configurator) {

        List<IdentifiableEntity> results = new ArrayList<IdentifiableEntity>();
        int numberOfResults = 0; // overall number of results (as opposed to number of results per page)
        List<TaxonBase> taxa = null;

        // Taxa and synonyms
        long numberTaxaResults = 0L;


        List<String> propertyPath = new ArrayList<String>();
        if(configurator.getTaxonPropertyPath() != null){
            propertyPath.addAll(configurator.getTaxonPropertyPath());
        }


       if (configurator.isDoMisappliedNames() || configurator.isDoSynonyms() || configurator.isDoTaxa()){
            if(configurator.getPageSize() != null){ // no point counting if we need all anyway
                numberTaxaResults =
                    dao.countTaxaByName(configurator.isDoTaxa(),configurator.isDoSynonyms(), configurator.isDoMisappliedNames(),
                        configurator.getTitleSearchStringSqlized(), configurator.getClassification(), configurator.getMatchMode(),
                        configurator.getNamedAreas());
            }

            if(configurator.getPageSize() == null || numberTaxaResults > configurator.getPageSize() * configurator.getPageNumber()){ // no point checking again if less results
                taxa = dao.getTaxaByName(configurator.isDoTaxa(), configurator.isDoSynonyms(),
                    configurator.isDoMisappliedNames(), configurator.getTitleSearchStringSqlized(), configurator.getClassification(),
                    configurator.getMatchMode(), configurator.getNamedAreas(),
                    configurator.getPageSize(), configurator.getPageNumber(), propertyPath);
            }
       }

        if (logger.isDebugEnabled()) { logger.debug(numberTaxaResults + " matching taxa counted"); }

        if(taxa != null){
            results.addAll(taxa);
        }

        numberOfResults += numberTaxaResults;

        // Names without taxa
        if (configurator.isDoNamesWithoutTaxa()) {
            int numberNameResults = 0;

            List<? extends TaxonNameBase<?,?>> names =
                nameDao.findByName(configurator.getTitleSearchStringSqlized(), configurator.getMatchMode(),
                        configurator.getPageSize(), configurator.getPageNumber(), null, configurator.getTaxonNamePropertyPath());
            if (logger.isDebugEnabled()) { logger.debug(names.size() + " matching name(s) found"); }
            if (names.size() > 0) {
                for (TaxonNameBase<?,?> taxonName : names) {
                    if (taxonName.getTaxonBases().size() == 0) {
                        results.add(taxonName);
                        numberNameResults++;
                    }
                }
                if (logger.isDebugEnabled()) { logger.debug(numberNameResults + " matching name(s) without taxa found"); }
                numberOfResults += numberNameResults;
            }
        }

        // Taxa from common names

        if (configurator.isDoTaxaByCommonNames()) {
            taxa = new ArrayList<TaxonBase>();
            numberTaxaResults = 0;
            if(configurator.getPageSize() != null){// no point counting if we need all anyway
                numberTaxaResults = dao.countTaxaByCommonName(configurator.getTitleSearchStringSqlized(), configurator.getClassification(), configurator.getMatchMode(), configurator.getNamedAreas());
            }
            if(configurator.getPageSize() == null || numberTaxaResults > configurator.getPageSize() * configurator.getPageNumber()){
                List<Taxon> commonNameResults = dao.getTaxaByCommonName(configurator.getTitleSearchStringSqlized(), configurator.getClassification(), configurator.getMatchMode(), configurator.getNamedAreas(), configurator.getPageSize(), configurator.getPageNumber(), configurator.getTaxonPropertyPath());
                taxa.addAll(commonNameResults);
            }
            if(taxa != null){
                results.addAll(taxa);
            }
            numberOfResults += numberTaxaResults;

        }

       return new DefaultPagerImpl<IdentifiableEntity>
            (configurator.getPageNumber(), numberOfResults, configurator.getPageSize(), results);
    }

    public List<UuidAndTitleCache<TaxonBase>> getTaxonUuidAndTitleCache(){
        return dao.getUuidAndTitleCache();
    }

    @Override
    public List<MediaRepresentation> getAllMedia(Taxon taxon, int size, int height, int widthOrDuration, String[] mimeTypes){
        List<MediaRepresentation> medRep = new ArrayList<MediaRepresentation>();
        taxon = (Taxon)dao.load(taxon.getUuid());
        Set<TaxonDescription> descriptions = taxon.getDescriptions();
        for (TaxonDescription taxDesc: descriptions){
            Set<DescriptionElementBase> elements = taxDesc.getElements();
            for (DescriptionElementBase descElem: elements){
                for(Media media : descElem.getMedia()){

                    //find the best matching representation
                    medRep.add(MediaUtils.findBestMatchingRepresentation(media, null, size, height, widthOrDuration, mimeTypes));

                }
            }
        }
        return medRep;
    }

    @Override
    public List<Media> listTaxonDescriptionMedia(Taxon taxon, Set<TaxonRelationshipEdge> includeRelationships, boolean limitToGalleries, List<String> propertyPath){
        return listMedia(taxon, includeRelationships, limitToGalleries, true, false, false, propertyPath);
    }

    @Override
    public List<Media> listMedia(Taxon taxon, Set<TaxonRelationshipEdge> includeRelationships,
            Boolean limitToGalleries, Boolean includeTaxonDescriptions, Boolean includeOccurrences,
            Boolean includeTaxonNameDescriptions, List<String> propertyPath) {

    //    logger.setLevel(Level.TRACE);
//        Logger.getLogger("org.hibernate.SQL").setLevel(Level.TRACE);

        logger.trace("listMedia() - START");

        Set<Taxon> taxa = new HashSet<Taxon>();
        List<Media> taxonMedia = new ArrayList<Media>();
        List<Media> nonImageGalleryImages = new ArrayList<Media>();

        if (limitToGalleries == null) {
            limitToGalleries = false;
        }

        // --- resolve related taxa
        if (includeRelationships != null && ! includeRelationships.isEmpty()) {
            logger.trace("listMedia() - resolve related taxa");
            taxa = listRelatedTaxa(taxon, includeRelationships, null, null, null, null);
        }

        taxa.add((Taxon) dao.load(taxon.getUuid()));

        if(includeTaxonDescriptions != null && includeTaxonDescriptions){
            logger.trace("listMedia() - includeTaxonDescriptions");
            List<TaxonDescription> taxonDescriptions = new ArrayList<TaxonDescription>();
            // --- TaxonDescriptions
            for (Taxon t : taxa) {
                taxonDescriptions.addAll(descriptionService.listTaxonDescriptions(t, null, null, null, null, propertyPath));
            }
            for (TaxonDescription taxonDescription : taxonDescriptions) {
                if (!limitToGalleries || taxonDescription.isImageGallery()) {
                    for (DescriptionElementBase element : taxonDescription.getElements()) {
                        for (Media media : element.getMedia()) {
                            if(taxonDescription.isImageGallery()){
                                taxonMedia.add(media);
                            }
                            else{
                                nonImageGalleryImages.add(media);
                            }
                        }
                    }
                }
            }
            //put images from image gallery first (#3242)
            taxonMedia.addAll(nonImageGalleryImages);
        }


        if(includeOccurrences != null && includeOccurrences) {
            logger.trace("listMedia() - includeOccurrences");
            Set<SpecimenOrObservationBase> specimensOrObservations = new HashSet<SpecimenOrObservationBase>();
            // --- Specimens
            for (Taxon t : taxa) {
                specimensOrObservations.addAll(occurrenceDao.listByAssociatedTaxon(null, t, null, null, null, null));
            }
            for (SpecimenOrObservationBase occurrence : specimensOrObservations) {

//            	direct media removed from specimen #3597
//              taxonMedia.addAll(occurrence.getMedia());

                // SpecimenDescriptions
                Set<SpecimenDescription> specimenDescriptions = occurrence.getSpecimenDescriptions();
                for (DescriptionBase specimenDescription : specimenDescriptions) {
                    if (!limitToGalleries || specimenDescription.isImageGallery()) {
                        Set<DescriptionElementBase> elements = specimenDescription.getElements();
                        for (DescriptionElementBase element : elements) {
                            for (Media media : element.getMedia()) {
                                taxonMedia.add(media);
                            }
                        }
                    }
                }

                // Collection
                //TODO why may collections have media attached? #
                if (occurrence.isInstanceOf(DerivedUnit.class)) {
                    DerivedUnit derivedUnit = CdmBase.deproxy(occurrence, DerivedUnit.class);
                    if (derivedUnit.getCollection() != null){
                        taxonMedia.addAll(derivedUnit.getCollection().getMedia());
                    }
                }

                // pherograms & gelPhotos
                if (occurrence.isInstanceOf(DnaSample.class)) {
                    DnaSample dnaSample = CdmBase.deproxy(occurrence, DnaSample.class);
                    Set<Sequence> sequences = dnaSample.getSequences();
                    //we do show only those gelPhotos which lead to a consensus sequence
                    for (Sequence sequence : sequences) {
                        Set<Media> dnaRelatedMedia = new HashSet<Media>();
                        for (SingleRead singleRead : sequence.getSingleReads()){
                            AmplificationResult amplification = singleRead.getAmplificationResult();
                            dnaRelatedMedia.add(amplification.getGelPhoto());
                            dnaRelatedMedia.add(singleRead.getPherogram());
                            dnaRelatedMedia.remove(null);
                        }
                        taxonMedia.addAll(dnaRelatedMedia);
                    }
                }

            }
        }

        if(includeTaxonNameDescriptions != null && includeTaxonNameDescriptions) {
            logger.trace("listMedia() - includeTaxonNameDescriptions");
            // --- TaxonNameDescription
            Set<TaxonNameDescription> nameDescriptions = new HashSet<TaxonNameDescription>();
            for (Taxon t : taxa) {
                nameDescriptions .addAll(t.getName().getDescriptions());
            }
            for(TaxonNameDescription nameDescription: nameDescriptions){
                if (!limitToGalleries || nameDescription.isImageGallery()) {
                    Set<DescriptionElementBase> elements = nameDescription.getElements();
                    for (DescriptionElementBase element : elements) {
                        for (Media media : element.getMedia()) {
                            taxonMedia.add(media);
                        }
                    }
                }
            }
        }


        logger.trace("listMedia() - initialize");
        beanInitializer.initializeAll(taxonMedia, propertyPath);

        logger.trace("listMedia() - END");

        return taxonMedia;
    }

    @Override
    public List<TaxonBase> findTaxaByID(Set<Integer> listOfIDs) {
        return this.dao.listByIds(listOfIDs, null, null, null, null);
    }

    @Override
    public TaxonBase findTaxonByUuid(UUID uuid, List<String> propertyPaths){
        return this.dao.findByUuid(uuid, null ,propertyPaths);
    }

    @Override
    public int countAllRelationships() {
        return this.dao.countAllRelationships();
    }

    @Override
    public List<TaxonNameBase> findIdenticalTaxonNames(List<String> propertyPath) {
        return this.dao.findIdenticalTaxonNames(propertyPath);
    }

    @Override
    @Transactional(readOnly = false)
    public DeleteResult deleteTaxon(UUID taxonUUID, TaxonDeletionConfigurator config, UUID classificationUuid)  {

    	if (config == null){
            config = new TaxonDeletionConfigurator();
        }
    	Taxon taxon = (Taxon)dao.load(taxonUUID);
    	Classification classification = HibernateProxyHelper.deproxy(classificationDao.load(classificationUuid), Classification.class);
        DeleteResult result = isDeletable(taxon, config);

        if (result.isOk()){
            // --- DeleteSynonymRelations
            if (config.isDeleteSynonymRelations()){
                boolean removeSynonymNameFromHomotypicalGroup = false;
                // use tmp Set to avoid concurrent modification
                Set<SynonymRelationship> synRelsToDelete = new HashSet<SynonymRelationship>();
                synRelsToDelete.addAll(taxon.getSynonymRelations());
                for (SynonymRelationship synRel : synRelsToDelete){
                    Synonym synonym = synRel.getSynonym();
                    // taxon.removeSynonymRelation will set the accepted taxon and the synonym to NULL
                    // this will cause hibernate to delete the relationship since
                    // the SynonymRelationship field on both is annotated with removeOrphan
                    // so no further explicit deleting of the relationship should be done here
                    taxon.removeSynonymRelation(synRel, removeSynonymNameFromHomotypicalGroup);

                    // --- DeleteSynonymsIfPossible
                    if (config.isDeleteSynonymsIfPossible()){
                        //TODO which value
                        boolean newHomotypicGroupIfNeeded = true;
                        SynonymDeletionConfigurator synConfig = new SynonymDeletionConfigurator();
                        deleteSynonym(synonym, taxon, synConfig);
                    }
                    // relationship will be deleted by hibernate automatically,
                    // see comment above and http://dev.e-taxonomy.eu/trac/ticket/3797
                    // else{
                    //     deleteSynonymRelationships(synonym, taxon);
                    // }
                }
            }

            // --- DeleteTaxonRelationships
            if (! config.isDeleteTaxonRelationships()){
                if (taxon.getTaxonRelations().size() > 0){
                    String message = "Taxon can't be deleted as it is related to another taxon. " +
                            "Remove taxon from all relations to other taxa prior to deletion.";
                   // throw new ReferencedObjectUndeletableException(message);
                }
            } else{
                for (TaxonRelationship taxRel: taxon.getTaxonRelations()){
                    if (config.isDeleteMisappliedNamesAndInvalidDesignations()){
                        if (taxRel.getType().equals(TaxonRelationshipType.MISAPPLIED_NAME_FOR()) || taxRel.getType().equals(TaxonRelationshipType.INVALID_DESIGNATION_FOR())){
                            if (taxon.equals(taxRel.getToTaxon())){

                                this.deleteTaxon(taxRel.getFromTaxon().getUuid(), config, classificationUuid);
                            }
                        }
                    }
                    taxon.removeTaxonRelation(taxRel);
                    /*if (taxFrom.equals(taxon)){
                        try{
                            this.deleteTaxon(taxTo, taxConf, classification);
                        } catch(DataChangeNoRollbackException e){
                            logger.debug("A related taxon will not be deleted." + e.getMessage());
                        }
                    } else {
                        try{
                            this.deleteTaxon(taxFrom, taxConf, classification);
                        } catch(DataChangeNoRollbackException e){
                            logger.debug("A related taxon will not be deleted." + e.getMessage());
                        }

                    }*/
                }
            }

            //    	TaxonDescription
            if (config.isDeleteDescriptions()){
                Set<TaxonDescription> descriptions = taxon.getDescriptions();
                List<TaxonDescription> removeDescriptions = new ArrayList<TaxonDescription>();
                for (TaxonDescription desc: descriptions){
                    //TODO use description delete configurator ?
                    //FIXME check if description is ALWAYS deletable
                    if (desc.getDescribedSpecimenOrObservation() != null){
                        String message = "Taxon can't be deleted as it is used in a TaxonDescription" +
                                " which also describes specimens or abservations";
                        //throw new ReferencedObjectUndeletableException(message);
                    }
                    removeDescriptions.add(desc);


                }
                for (TaxonDescription desc: removeDescriptions){
                    taxon.removeDescription(desc);
                    descriptionService.delete(desc);
                }
            }


         /*   //check references with only reverse mapping
        String message = checkForReferences(taxon);
        if (message != null){
            //throw new ReferencedObjectUndeletableException(message.toString());
        }*/

         if (! config.isDeleteTaxonNodes() || (!config.isDeleteInAllClassifications() && classification == null )){
                //if (taxon.getTaxonNodes().size() > 0){
                   // message = "Taxon can't be deleted as it is used in a classification node. Remove taxon from all classifications prior to deletion or define a classification where it should be deleted or adapt the taxon deletion configurator.";
                   // throw new ReferencedObjectUndeletableException(message);
                //}
            }else{
                if (taxon.getTaxonNodes().size() != 0){
                    Set<TaxonNode> nodes = taxon.getTaxonNodes();
                    Iterator<TaxonNode> iterator = nodes.iterator();
                    TaxonNode node = null;
                    boolean deleteChildren;
                    if (config.getTaxonNodeConfig().getChildHandling().equals(ChildHandling.DELETE)){
                        deleteChildren = true;
                    }else {
                        deleteChildren = false;
                    }
                    boolean success = true;
                    if (!config.isDeleteInAllClassifications() && !(classification == null)){
                        while (iterator.hasNext()){
                            node = iterator.next();
                            if (node.getClassification().equals(classification)){
                                break;
                            }
                            node = null;
                        }
                        if (node != null){
                            success =taxon.removeTaxonNode(node, deleteChildren);
                            nodeService.delete(node);
                        } else {
                        	result.setError();
                        	result.addException(new Exception("The taxon can not be deleted because it is not used in defined classification."));
                        }
                    } else if (config.isDeleteInAllClassifications()){
                        Set<ITaxonTreeNode> nodesList = new HashSet<ITaxonTreeNode>();
                        nodesList.addAll(taxon.getTaxonNodes());

                            for (ITaxonTreeNode treeNode: nodesList){
                                TaxonNode taxonNode = (TaxonNode) treeNode;
                                if(!deleteChildren){
                                   /* Object[] childNodes = taxonNode.getChildNodes().toArray();
                                    //nodesList.addAll(taxonNode.getChildNodes());
                                    for (Object childNode: childNodes){
                                        TaxonNode childNodeCast = (TaxonNode) childNode;
                                        deleteTaxon(childNodeCast.getTaxon(), config, classification);

                                    }

                                    /*for (TaxonNode childNode: taxonNode.getChildNodes()){
                                        deleteTaxon(childNode.getTaxon(), config, classification);

                                    }
                                   // taxon.removeTaxonNode(taxonNode);
                                    //nodeService.delete(taxonNode);
                                } else{
                                    */
                                    Object[] childNodes = taxonNode.getChildNodes().toArray();
                                    for (Object childNode: childNodes){
                                        TaxonNode childNodeCast = (TaxonNode) childNode;
                                        taxonNode.getParent().addChildNode(childNodeCast, childNodeCast.getReference(), childNodeCast.getMicroReference());
                                    }

                                    //taxon.removeTaxonNode(taxonNode);
                                }
                            }
                        config.getTaxonNodeConfig().setDeleteTaxon(false);
                        DeleteResult resultNodes = nodeService.deleteTaxonNodes(nodesList, config);
                        if (!resultNodes.isOk()){
                        	result.addExceptions(resultNodes.getExceptions());
                        	result.setStatus(resultNodes.getStatus());
                        }
                    }
                    if (!success){
                        result.setError();
                        result.addException(new Exception("The taxon can not be deleted because the taxon node can not be removed."));
                    }
                }
            }


             //PolytomousKey TODO


            //TaxonNameBase
            if (config.isDeleteNameIfPossible()){


                    //TaxonNameBase name = nameService.find(taxon.getName().getUuid());
                    TaxonNameBase name = (TaxonNameBase)HibernateProxyHelper.deproxy(taxon.getName());
                    //check whether taxon will be deleted or not
                    if ((taxon.getTaxonNodes() == null || taxon.getTaxonNodes().size()== 0) && name != null ){
                        taxon = (Taxon) HibernateProxyHelper.deproxy(taxon);
                        //name.removeTaxonBase(taxon);
                        //nameService.saveOrUpdate(name);
                        taxon.setName(null);
                        //dao.delete(taxon);
                        DeleteResult nameResult = new DeleteResult();

                        //remove name if possible (and required)
                        if (name != null && config.isDeleteNameIfPossible()){
                        	nameResult = nameService.delete(name.getUuid(), config.getNameDeletionConfig());
                        }

                        if (nameResult.isError() || nameResult.isAbort()){
                        	//result.setError();
                        	result.addRelatedObject(name);
                        	result.addExceptions(nameResult.getExceptions());
                        }

                    }

            }else {
                taxon.setName(null);
            }


//        	TaxonDescription
           /* Set<TaxonDescription> descriptions = taxon.getDescriptions();

            for (TaxonDescription desc: descriptions){
                if (config.isDeleteDescriptions()){
                    //TODO use description delete configurator ?
                    //FIXME check if description is ALWAYS deletable
                    taxon.removeDescription(desc);
                    descriptionService.delete(desc);
                }else{
                    if (desc.getDescribedSpecimenOrObservations().size()>0){
                        String message = "Taxon can't be deleted as it is used in a TaxonDescription" +
                                " which also describes specimens or observations";
                            throw new ReferencedObjectUndeletableException(message);
    }
                    }
                }*/

            if ((taxon.getTaxonNodes() == null || taxon.getTaxonNodes().size()== 0)  ){
            	try{
            		UUID uuid = dao.delete(taxon);

            	}catch(Exception e){
            		result.addException(e);
            		result.setError();

            	}
            } else {
            	result.setError();
            	result.addException(new Exception("The Taxon can't be deleted."));

            }
        }
//        }else {
//        	List<Exception> exceptions = new ArrayList<Exception>();
//        	for (String message: referencedObjects){
//        		ReferencedObjectUndeletableException exception = new ReferencedObjectUndeletableException(message);
//        		exceptions.add(exception);
//        	}
//        	result.addExceptions(exceptions);
//        	result.setError();
//
//        }
        return result;

    }

    private String checkForReferences(Taxon taxon){
        Set<CdmBase> referencingObjects = genericDao.getReferencingObjects(taxon);
        for (CdmBase referencingObject : referencingObjects){
            //IIdentificationKeys (Media, Polytomous, MultiAccess)
            if (HibernateProxyHelper.isInstanceOf(referencingObject, IIdentificationKey.class)){
                String message = "Taxon" + taxon.getTitleCache() + "can't be deleted as it is used in an identification key. Remove from identification key prior to deleting this name";

                return message;
            }


           /* //PolytomousKeyNode
            if (referencingObject.isInstanceOf(PolytomousKeyNode.class)){
                String message = "Taxon" + taxon.getTitleCache() + " can't be deleted as it is used in polytomous key node";
                return message;
            }*/

            //TaxonInteraction
            if (referencingObject.isInstanceOf(TaxonInteraction.class)){
                String message = "Taxon can't be deleted as it is used in taxonInteraction#taxon2";
                return message;
            }

          //TaxonInteraction
            if (referencingObject.isInstanceOf(DeterminationEvent.class)){
                String message = "Taxon can't be deleted as it is used in a determination event";
                return message;
            }

        }

        referencingObjects = null;
        return null;
    }

    private boolean checkForPolytomousKeys(Taxon taxon){
        boolean result = false;
        List<CdmBase> list = genericDao.getCdmBasesByFieldAndClass(PolytomousKeyNode.class, "taxon", taxon);
        if (!list.isEmpty()) {
            result = true;
        }
        return result;
    }

    @Override
    @Transactional(readOnly = false)
    public DeleteResult delete(UUID synUUID){
    	DeleteResult result = new DeleteResult();
    	Synonym syn = (Synonym)dao.load(synUUID);

        return this.deleteSynonym(syn, null);
    }


    @Override
    @Transactional(readOnly = false)
    public DeleteResult deleteSynonym(Synonym synonym, SynonymDeletionConfigurator config) {
        return deleteSynonym(synonym, null, config);

    }

    @Override
    @Transactional(readOnly = false)
    public DeleteResult deleteSynonym(UUID synonymUuid, SynonymDeletionConfigurator config) {
        return deleteSynonym((Synonym)dao.load(synonymUuid), config);

    }

    @Transactional(readOnly = false)
    @Override
    public DeleteResult deleteSynonym(Synonym synonym, Taxon taxon, SynonymDeletionConfigurator config) {
        DeleteResult result = new DeleteResult();
    	if (synonym == null){
    		result.setAbort();
    		return result;
        }

        if (config == null){
            config = new SynonymDeletionConfigurator();
        }
        result = isDeletable(synonym, config);


        if (result.isOk()){

            synonym = CdmBase.deproxy(dao.merge(synonym), Synonym.class);

            //remove synonymRelationship
            Set<Taxon> taxonSet = new HashSet<Taxon>();
            if (taxon != null){
                taxonSet.add(taxon);
            }else{
                taxonSet.addAll(synonym.getAcceptedTaxa());
            }
            for (Taxon relatedTaxon : taxonSet){
            	relatedTaxon = HibernateProxyHelper.deproxy(relatedTaxon, Taxon.class);
                relatedTaxon.removeSynonym(synonym, false);
                this.saveOrUpdate(relatedTaxon);
            }
            this.saveOrUpdate(synonym);

            //TODO remove name from homotypical group?

            //remove synonym (if necessary)

            result.addUpdatedObject(taxon);
            if (synonym.getSynonymRelations().isEmpty()){
                TaxonNameBase<?,?> name = synonym.getName();
                synonym.setName(null);
                dao.delete(synonym);

                //remove name if possible (and required)
                if (name != null && config.isDeleteNameIfPossible()){

                        DeleteResult nameDeleteresult = nameService.delete(name.getUuid(), config.getNameDeletionConfig());
                        if (nameDeleteresult.isAbort()){
                        	result.addExceptions(nameDeleteresult.getExceptions());
                        	result.addUpdatedObject(name);
                        }

                }

            }else {
            	result.setError();
            	result.addException(new ReferencedObjectUndeletableException("Synonym can not be deleted it is used in a synonymRelationship."));
                return result;
            }


        }
        return result;
//        else{
//        	List<Exception> exceptions = new ArrayList<Exception>();
//        	for (String message :messages){
//        		exceptions.add(new ReferencedObjectUndeletableException(message));
//        	}
//        	result.setError();
//        	result.addExceptions(exceptions);
//            return result;
//        }


    }

    @Override
    public List<TaxonNameBase> findIdenticalTaxonNameIds(List<String> propertyPath) {

        return this.dao.findIdenticalNamesNew(propertyPath);
    }

    @Override
    public String getPhylumName(TaxonNameBase name){
        return this.dao.getPhylumName(name);
    }

    @Override
    public long deleteSynonymRelationships(Synonym syn, Taxon taxon) {
        return dao.deleteSynonymRelationships(syn, taxon);
    }

    @Override
    public long deleteSynonymRelationships(Synonym syn) {
        return dao.deleteSynonymRelationships(syn, null);
    }

    @Override
    public List<SynonymRelationship> listSynonymRelationships(
            TaxonBase taxonBase, SynonymRelationshipType type, Integer pageSize, Integer pageNumber,
            List<OrderHint> orderHints, List<String> propertyPaths, Direction direction) {
        Integer numberOfResults = dao.countSynonymRelationships(taxonBase, type, direction);

        List<SynonymRelationship> results = new ArrayList<SynonymRelationship>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.getSynonymRelationships(taxonBase, type, pageSize, pageNumber, orderHints, propertyPaths, direction);
        }
        return results;
    }

    @Override
    public Taxon findBestMatchingTaxon(String taxonName) {
        MatchingTaxonConfigurator config = MatchingTaxonConfigurator.NewInstance();
        config.setTaxonNameTitle(taxonName);
        return findBestMatchingTaxon(config);
    }

    @Override
    public Taxon findBestMatchingTaxon(MatchingTaxonConfigurator config) {

        Taxon bestCandidate = null;
        try{
            // 1. search for acceptet taxa
            List<TaxonBase> taxonList = dao.findByNameTitleCache(true, false, config.getTaxonNameTitle(), null, MatchMode.EXACT, null, 0, null, null);
            boolean bestCandidateMatchesSecUuid = false;
            boolean bestCandidateIsInClassification = false;
            int countEqualCandidates = 0;
            for(TaxonBase taxonBaseCandidate : taxonList){
                if(taxonBaseCandidate instanceof Taxon){
                    Taxon newCanditate = CdmBase.deproxy(taxonBaseCandidate, Taxon.class);
                    boolean newCandidateMatchesSecUuid = isMatchesSecUuid(newCanditate, config);
                    if (! newCandidateMatchesSecUuid && config.isOnlyMatchingSecUuid() ){
                        continue;
                    }else if(newCandidateMatchesSecUuid && ! bestCandidateMatchesSecUuid){
                        bestCandidate = newCanditate;
                        countEqualCandidates = 1;
                        bestCandidateMatchesSecUuid = true;
                        continue;
                    }

                    boolean newCandidateInClassification = isInClassification(newCanditate, config);
                    if (! newCandidateInClassification && config.isOnlyMatchingClassificationUuid()){
                        continue;
                    }else if (newCandidateInClassification && ! bestCandidateIsInClassification){
                        bestCandidate = newCanditate;
                        countEqualCandidates = 1;
                        bestCandidateIsInClassification = true;
                        continue;
                    }
                    if (bestCandidate == null){
                        bestCandidate = newCanditate;
                        countEqualCandidates = 1;
                        continue;
                    }

                }else{  //not Taxon.class
                    continue;
                }
                countEqualCandidates++;

            }
            if (bestCandidate != null){
                if(countEqualCandidates > 1){
                    logger.info(countEqualCandidates + " equally matching TaxonBases found, using first accepted Taxon: " + bestCandidate.getTitleCache());
                    return bestCandidate;
                } else {
                    logger.info("using accepted Taxon: " + bestCandidate.getTitleCache());
                    return bestCandidate;
                }
            }


            // 2. search for synonyms
            if (config.isIncludeSynonyms()){
                List<TaxonBase> synonymList = dao.findByNameTitleCache(false, true, config.getTaxonNameTitle(), null, MatchMode.EXACT, null, 0, null, null);
                for(TaxonBase taxonBase : synonymList){
                    if(taxonBase instanceof Synonym){
                        Synonym synonym = CdmBase.deproxy(taxonBase, Synonym.class);
                        Set<Taxon> acceptetdCandidates = synonym.getAcceptedTaxa();
                        if(!acceptetdCandidates.isEmpty()){
                            bestCandidate = acceptetdCandidates.iterator().next();
                            if(acceptetdCandidates.size() == 1){
                                logger.info(acceptetdCandidates.size() + " Accepted taxa found for synonym " + taxonBase.getTitleCache() + ", using first one: " + bestCandidate.getTitleCache());
                                return bestCandidate;
                            } else {
                                logger.info("using accepted Taxon " +  bestCandidate.getTitleCache() + "for synonym " + taxonBase.getTitleCache());
                                return bestCandidate;
                            }
                            //TODO extend method: search using treeUUID, using SecUUID, first find accepted then include synonyms until a matching taxon is found
                        }
                    }
                }
            }

        } catch (Exception e){
            logger.error(e);
            e.printStackTrace();
        }

        return bestCandidate;
    }

    private boolean isInClassification(Taxon taxon, MatchingTaxonConfigurator config) {
        UUID configClassificationUuid = config.getClassificationUuid();
        if (configClassificationUuid == null){
            return false;
        }
        for (TaxonNode node : taxon.getTaxonNodes()){
            UUID classUuid = node.getClassification().getUuid();
            if (configClassificationUuid.equals(classUuid)){
                return true;
            }
        }
        return false;
    }

    private boolean isMatchesSecUuid(Taxon taxon, MatchingTaxonConfigurator config) {
        UUID configSecUuid = config.getSecUuid();
        if (configSecUuid == null){
            return false;
        }
        UUID taxonSecUuid = (taxon.getSec() == null)? null : taxon.getSec().getUuid();
        return configSecUuid.equals(taxonSecUuid);
    }

    @Override
    public Synonym findBestMatchingSynonym(String taxonName) {
        List<TaxonBase> synonymList = dao.findByNameTitleCache(false, true, taxonName, null, MatchMode.EXACT, null, 0, null, null);
        if(! synonymList.isEmpty()){
            Synonym result = CdmBase.deproxy(synonymList.iterator().next(), Synonym.class);
            if(synonymList.size() == 1){
                logger.info(synonymList.size() + " Synonym found " + result.getTitleCache() );
                return result;
            } else {
                logger.info("Several matching synonyms found. Using first: " +  result.getTitleCache());
                return result;
            }
        }
        return null;
    }

    @Override
    @Transactional(readOnly = false)
    public SynonymRelationship moveSynonymToAnotherTaxon(SynonymRelationship oldSynonymRelation,
            Taxon newTaxon,
            boolean moveHomotypicGroup,
            SynonymRelationshipType newSynonymRelationshipType,
            Reference reference,
            String referenceDetail,
            boolean keepReference) throws HomotypicalGroupChangeException {

        Synonym synonym = (Synonym) dao.load(oldSynonymRelation.getSynonym().getUuid());
        Taxon fromTaxon = (Taxon) dao.load(oldSynonymRelation.getAcceptedTaxon().getUuid());
        //TODO what if there is no name ?? Concepts may be cached (e.g. via TCS import)
        TaxonNameBase<?,?> synonymName = synonym.getName();
        TaxonNameBase<?,?> fromTaxonName = fromTaxon.getName();
        //set default relationship type
        if (newSynonymRelationshipType == null){
            newSynonymRelationshipType = SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF();
        }
        boolean newRelTypeIsHomotypic = newSynonymRelationshipType.equals(SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF());

        HomotypicalGroup homotypicGroup = synonymName.getHomotypicalGroup();
        int hgSize = homotypicGroup.getTypifiedNames().size();
        boolean isSingleInGroup = !(hgSize > 1);

        if (! isSingleInGroup){
            boolean isHomotypicToAccepted = synonymName.isHomotypic(fromTaxonName);
            boolean hasHomotypicSynonymRelatives = isHomotypicToAccepted ? hgSize > 2 : hgSize > 1;
            if (isHomotypicToAccepted){
                String message = "Synonym is in homotypic group with accepted taxon%s. First remove synonym from homotypic group of accepted taxon before moving to other taxon.";
                String homotypicRelatives = hasHomotypicSynonymRelatives ? " and other synonym(s)":"";
                message = String.format(message, homotypicRelatives);
                throw new HomotypicalGroupChangeException(message);
            }
            if (! moveHomotypicGroup){
                String message = "Synonym is in homotypic group with other synonym(s). Either move complete homotypic group or remove synonym from homotypic group prior to moving to other taxon.";
                throw new HomotypicalGroupChangeException(message);
            }
        }else{
            moveHomotypicGroup = true;  //single synonym always allows to moveCompleteGroup
        }
//        Assert.assertTrue("Synonym can only be moved with complete homotypic group", moveHomotypicGroup);

        SynonymRelationship result = null;
        //move all synonyms to new taxon
        List<Synonym> homotypicSynonyms = fromTaxon.getSynonymsInGroup(homotypicGroup);
        for (Synonym syn: homotypicSynonyms){
            Set<SynonymRelationship> synRelations = syn.getSynonymRelations();
            for (SynonymRelationship synRelation : synRelations){
                if (fromTaxon.equals(synRelation.getAcceptedTaxon())){
                    Reference<?> newReference = reference;
                    if (newReference == null && keepReference){
                        newReference = synRelation.getCitation();
                    }
                    String newRefDetail = referenceDetail;
                    if (newRefDetail == null && keepReference){
                        newRefDetail = synRelation.getCitationMicroReference();
                    }
                    newTaxon = HibernateProxyHelper.deproxy(newTaxon, Taxon.class);
                    fromTaxon = HibernateProxyHelper.deproxy(fromTaxon, Taxon.class);
                    SynonymRelationship newSynRelation = newTaxon.addSynonym(syn, newSynonymRelationshipType, newReference, newRefDetail);
                    fromTaxon.removeSynonymRelation(synRelation, false);
//
                    //change homotypic group of synonym if relType is 'homotypic'
//                	if (newRelTypeIsHomotypic){
//                		newTaxon.getName().getHomotypicalGroup().addTypifiedName(syn.getName());
//                	}
                    //set result
                    if (synRelation.equals(oldSynonymRelation)){
                        result = newSynRelation;
                    }
                }
            }

        }
        saveOrUpdate(fromTaxon);
        saveOrUpdate(newTaxon);
        //Assert that there is a result
        if (result == null){
            String message = "Old synonym relation could not be transformed into new relation. This should not happen.";
            throw new IllegalStateException(message);
        }
        return result;
    }

    @Override
    public List<UuidAndTitleCache<TaxonBase>> getUuidAndTitleCacheTaxon() {
        return dao.getUuidAndTitleCacheTaxon();
    }

    @Override
    public List<UuidAndTitleCache<TaxonBase>> getUuidAndTitleCacheSynonym() {
        return dao.getUuidAndTitleCacheSynonym();
    }

    @Override
    public Pager<SearchResult<TaxonBase>> findByFullText(
            Class<? extends TaxonBase> clazz, String queryString,
            Classification classification, List<Language> languages,
            boolean highlightFragments, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) throws CorruptIndexException, IOException, ParseException {


        LuceneSearch luceneSearch = prepareFindByFullTextSearch(clazz, queryString, classification, languages, highlightFragments, null);

        // --- execute search
        TopGroupsWithMaxScore topDocsResultSet = luceneSearch.executeSearch(pageSize, pageNumber);

        Map<CdmBaseType, String> idFieldMap = new HashMap<CdmBaseType, String>();
        idFieldMap.put(CdmBaseType.TAXON, "id");

        // ---  initialize taxa, thighlight matches ....
        ISearchResultBuilder searchResultBuilder = new SearchResultBuilder(luceneSearch, luceneSearch.getQuery());
        List<SearchResult<TaxonBase>> searchResults = searchResultBuilder.createResultSet(
                topDocsResultSet, luceneSearch.getHighlightFields(), dao, idFieldMap, propertyPaths);

        int totalHits = topDocsResultSet != null ? topDocsResultSet.topGroups.totalGroupCount : 0;
        return new DefaultPagerImpl<SearchResult<TaxonBase>>(pageNumber, totalHits, pageSize, searchResults);
    }

    @Override
    public Pager<SearchResult<TaxonBase>> findByDistribution(List<NamedArea> areaFilter, List<PresenceAbsenceTerm> statusFilter,
            Classification classification,
            Integer pageSize, Integer pageNumber,
            List<OrderHint> orderHints, List<String> propertyPaths) throws IOException, ParseException {

        LuceneSearch luceneSearch = prepareByDistributionSearch(areaFilter, statusFilter, classification);

        // --- execute search
        TopGroupsWithMaxScore topDocsResultSet = luceneSearch.executeSearch(pageSize, pageNumber);

        Map<CdmBaseType, String> idFieldMap = new HashMap<CdmBaseType, String>();
        idFieldMap.put(CdmBaseType.TAXON, "id");

        // ---  initialize taxa, thighlight matches ....
        ISearchResultBuilder searchResultBuilder = new SearchResultBuilder(luceneSearch, luceneSearch.getQuery());
        List<SearchResult<TaxonBase>> searchResults = searchResultBuilder.createResultSet(
                topDocsResultSet, luceneSearch.getHighlightFields(), dao, idFieldMap, propertyPaths);

        int totalHits = topDocsResultSet != null ? topDocsResultSet.topGroups.totalGroupCount : 0;
        return new DefaultPagerImpl<SearchResult<TaxonBase>>(pageNumber, totalHits, pageSize, searchResults);
    }

    /**
     * @param clazz
     * @param queryString
     * @param classification
     * @param languages
     * @param highlightFragments
     * @param sortFields TODO
     * @param directorySelectClass
     * @return
     */
    protected LuceneSearch prepareFindByFullTextSearch(Class<? extends CdmBase> clazz, String queryString, Classification classification, List<Language> languages,
            boolean highlightFragments, SortField[] sortFields) {
        BooleanQuery finalQuery = new BooleanQuery();
        BooleanQuery textQuery = new BooleanQuery();

        LuceneSearch luceneSearch = new LuceneSearch(luceneIndexToolProvider, GroupByTaxonClassBridge.GROUPBY_TAXON_FIELD, TaxonBase.class);
        QueryFactory taxonBaseQueryFactory = luceneIndexToolProvider.newQueryFactoryFor(TaxonBase.class);

        if(sortFields == null){
            sortFields = new  SortField[]{SortField.FIELD_SCORE, new SortField("titleCache__sort", SortField.STRING,  false)};
        }
        luceneSearch.setSortFields(sortFields);

        // ---- search criteria
        luceneSearch.setCdmTypRestriction(clazz);

        if(!queryString.isEmpty() && !queryString.equals("*") && !queryString.equals("?") ) {
            textQuery.add(taxonBaseQueryFactory.newTermQuery("titleCache", queryString), Occur.SHOULD);
            textQuery.add(taxonBaseQueryFactory.newDefinedTermQuery("name.rank", queryString, languages), Occur.SHOULD);
        }

        if(textQuery.getClauses().length > 0) {
            finalQuery.add(textQuery, Occur.MUST);
        }


        if(classification != null){
            finalQuery.add(taxonBaseQueryFactory.newEntityIdQuery("taxonNodes.classification.id", classification), Occur.MUST);
        }
        luceneSearch.setQuery(finalQuery);

        if(highlightFragments){
            luceneSearch.setHighlightFields(taxonBaseQueryFactory.getTextFieldNamesAsArray());
        }
        return luceneSearch;
    }

    /**
     * Uses org.apache.lucene.search.join.JoinUtil for query time joining, alternatively
     * the BlockJoinQuery could be used. The latter might be more memory save but has the
     * drawback of requiring to do the join an indexing time.
     * see  http://dev.e-taxonomy.eu/trac/wiki/LuceneNotes#JoinsinLucene for more information on this.
     *
     * Joins TaxonRelationShip with Taxon depending on the direction of the given edge:
     * <ul>
     * <li>direct, everted: {@link Direction.relatedTo}: TaxonRelationShip.relatedTo.id --&gt; Taxon.id </li>
     * <li>inverse: {@link Direction.relatedFrom}:  TaxonRelationShip.relatedFrom.id --&gt; Taxon.id </li>
     * <ul>
     * @param queryString
     * @param classification
     * @param languages
     * @param highlightFragments
     * @param sortFields TODO
     *
     * @return
     * @throws IOException
     */
    protected LuceneSearch prepareFindByTaxonRelationFullTextSearch(TaxonRelationshipEdge edge, String queryString, Classification classification, List<Language> languages,
            boolean highlightFragments, SortField[] sortFields) throws IOException {

        String fromField;
        String queryTermField;
        String toField = "id"; // TaxonBase.uuid

        if(edge.isBidirectional()){
            throw new RuntimeException("Bidirectional joining not supported!");
        }
        if(edge.isEvers()){
            fromField = "relatedFrom.id";
            queryTermField = "relatedFrom.titleCache";
        } else if(edge.isInvers()) {
            fromField = "relatedTo.id";
            queryTermField = "relatedTo.titleCache";
        } else {
            throw new RuntimeException("Invalid direction: " + edge.getDirections());
        }

        BooleanQuery finalQuery = new BooleanQuery();

        LuceneSearch luceneSearch = new LuceneSearch(luceneIndexToolProvider, GroupByTaxonClassBridge.GROUPBY_TAXON_FIELD, TaxonBase.class);
        QueryFactory taxonBaseQueryFactory = luceneIndexToolProvider.newQueryFactoryFor(TaxonBase.class);

        BooleanQuery joinFromQuery = new BooleanQuery();
        joinFromQuery.add(taxonBaseQueryFactory.newTermQuery(queryTermField, queryString), Occur.MUST);
        joinFromQuery.add(taxonBaseQueryFactory.newEntityIdQuery("type.id", edge.getTaxonRelationshipType()), Occur.MUST);
        Query joinQuery = taxonBaseQueryFactory.newJoinQuery(fromField, toField, joinFromQuery, TaxonRelationship.class);

        if(sortFields == null){
            sortFields = new  SortField[]{SortField.FIELD_SCORE, new SortField("titleCache__sort", SortField.STRING,  false)};
        }
        luceneSearch.setSortFields(sortFields);

        finalQuery.add(joinQuery, Occur.MUST);

        if(classification != null){
            finalQuery.add(taxonBaseQueryFactory.newEntityIdQuery("taxonNodes.classification.id", classification), Occur.MUST);
        }
        luceneSearch.setQuery(finalQuery);

        if(highlightFragments){
            luceneSearch.setHighlightFields(taxonBaseQueryFactory.getTextFieldNamesAsArray());
        }
        return luceneSearch;
    }

    @Override
    public Pager<SearchResult<TaxonBase>> findTaxaAndNamesByFullText(
            EnumSet<TaxaAndNamesSearchMode> searchModes, String queryString, Classification classification,
            Set<NamedArea> namedAreas, Set<PresenceAbsenceTerm> distributionStatus, List<Language> languages,
            boolean highlightFragments, Integer pageSize,
            Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths)
            throws CorruptIndexException, IOException, ParseException, LuceneMultiSearchException {

        // FIXME: allow taxonomic ordering
        //  hql equivalent:  order by t.name.genusOrUninomial, case when t.name.specificEpithet like '\"%\"' then 1 else 0 end, t.name.specificEpithet, t.name.rank desc, t.name.nameCache";
        // this require building a special sort column by a special classBridge
        if(highlightFragments){
            logger.warn("findTaxaAndNamesByFullText() : fragment highlighting is " +
                    "currently not fully supported by this method and thus " +
                    "may not work with common names and misapplied names.");
        }

        // convert sets to lists
        List<NamedArea> namedAreaList = null;
        List<PresenceAbsenceTerm>distributionStatusList = null;
        if(namedAreas != null){
            namedAreaList = new ArrayList<NamedArea>(namedAreas.size());
            namedAreaList.addAll(namedAreas);
        }
        if(distributionStatus != null){
            distributionStatusList = new ArrayList<PresenceAbsenceTerm>(distributionStatus.size());
            distributionStatusList.addAll(distributionStatus);
        }

        // set default if parameter is null
        if(searchModes == null){
            searchModes = EnumSet.of(TaxaAndNamesSearchMode.doTaxa);
        }

        // set sort order and thus override any sort orders which may have been
        // defindes by prepare*Search methods
        if(orderHints == null){
            orderHints = OrderHint.NOMENCLATURAL_SORT_ORDER;
        }
        SortField[] sortFields = new SortField[orderHints.size()];
        int i = 0;
        for(OrderHint oh : orderHints){
            sortFields[i++] = oh.toSortField();
        }
//        SortField[] sortFields = new SortField[]{SortField.FIELD_SCORE, new SortField("id", SortField.STRING, false)};
//        SortField[] sortFields = new SortField[]{new SortField(NomenclaturalSortOrderBrigde.NAME_SORT_FIELD_NAME, SortField.STRING, false)};


        boolean addDistributionFilter = namedAreas != null && namedAreas.size() > 0;

        List<LuceneSearch> luceneSearches = new ArrayList<LuceneSearch>();
        Map<CdmBaseType, String> idFieldMap = new HashMap<CdmBaseType, String>();

        /*
          ======== filtering by distribution , HOWTO ========

           - http://www.javaranch.com/journal/2009/02/filtering-a-lucene-search.html
           - http://stackoverflow.com/questions/17709256/lucene-solr-using-complex-filters -> QueryWrapperFilter
          add Filter to search as http://lucene.apache.org/core/3_6_0/api/all/org/apache/lucene/search/Filter.html
          which will be put into a FilteredQuersy  in the end ?


          3. how does it work in spatial?
          see
           - http://www.nsshutdown.com/projects/lucene/whitepaper/locallucene_v2.html
           - http://www.infoq.com/articles/LuceneSpatialSupport
           - http://www.mhaller.de/archives/156-Spatial-search-with-Lucene.html
          ------------------------------------------------------------------------

          filter strategies:
          A) use a separate distribution filter per index sub-query/search:
           - byTaxonSyonym (query TaxaonBase):
               use a join area filter (Distribution -> TaxonBase)
           - byCommonName (query DescriptionElementBase): use an area filter on
               DescriptionElementBase !!! PROBLEM !!!
               This cannot work since the distributions are different entities than the
               common names and thus these are different lucene documents.
           - byMisaplliedNames (join query TaxonRelationship -> TaxaonBase):
               use a join area filter (Distribution -> TaxonBase)

          B) use a common distribution filter for all index sub-query/searches:
           - use a common join area filter (Distribution -> TaxonBase)
           - also implement the byCommonName as join query (CommonName -> TaxonBase)
           PROBLEM in this case: we are losing the fragment highlighting for the
           common names, since the returned documents are always TaxonBases
        */

        /* The QueryFactory for creating filter queries on Distributions should
         * The query factory used for the common names query cannot be reused
         * for this case, since we want to only record the text fields which are
         * actually used in the primary query
         */
        QueryFactory distributionFilterQueryFactory = luceneIndexToolProvider.newQueryFactoryFor(Distribution.class);

        BooleanFilter multiIndexByAreaFilter = new BooleanFilter();


        // search for taxa or synonyms
        if(searchModes.contains(TaxaAndNamesSearchMode.doTaxa) || searchModes.contains(TaxaAndNamesSearchMode.doSynonyms)) {
            Class taxonBaseSubclass = TaxonBase.class;
            if(searchModes.contains(TaxaAndNamesSearchMode.doTaxa) && !searchModes.contains(TaxaAndNamesSearchMode.doSynonyms)){
                taxonBaseSubclass = Taxon.class;
            } else if (!searchModes.contains(TaxaAndNamesSearchMode.doTaxa) && searchModes.contains(TaxaAndNamesSearchMode.doSynonyms)) {
                taxonBaseSubclass = Synonym.class;
            }
            luceneSearches.add(prepareFindByFullTextSearch(taxonBaseSubclass, queryString, classification, languages, highlightFragments, sortFields));
            idFieldMap.put(CdmBaseType.TAXON, "id");
            /* A) does not work!!!!
            if(addDistributionFilter){
                // in this case we need a filter which uses a join query
                // to get the TaxonBase documents for the DescriptionElementBase documents
                // which are matching the areas in question
                Query taxonAreaJoinQuery = createByDistributionJoinQuery(
                        namedAreaList,
                        distributionStatusList,
                        distributionFilterQueryFactory
                        );
                multiIndexByAreaFilter.add(new QueryWrapperFilter(taxonAreaJoinQuery), Occur.SHOULD);
            }
            */
            if(addDistributionFilter && searchModes.contains(TaxaAndNamesSearchMode.doSynonyms)){
                // add additional area filter for synonyms
                String fromField = "inDescription.taxon.id"; // in DescriptionElementBase index
                String toField = "accTaxon.id"; // id in TaxonBase index

                BooleanQuery byDistributionQuery = createByDistributionQuery(namedAreaList, distributionStatusList, distributionFilterQueryFactory);

                Query taxonAreaJoinQuery = distributionFilterQueryFactory.newJoinQuery(fromField, toField, byDistributionQuery, Distribution.class);
                multiIndexByAreaFilter.add(new QueryWrapperFilter(taxonAreaJoinQuery), Occur.SHOULD);

            }
        }

        // search by CommonTaxonName
        if(searchModes.contains(TaxaAndNamesSearchMode.doTaxaByCommonNames)) {
            // B)
            QueryFactory descriptionElementQueryFactory = luceneIndexToolProvider.newQueryFactoryFor(DescriptionElementBase.class);
            Query byCommonNameJoinQuery = descriptionElementQueryFactory.newJoinQuery(
                    "inDescription.taxon.id",
                    "id",
                    QueryFactory.addTypeRestriction(
                                createByDescriptionElementFullTextQuery(queryString, classification, null, languages, descriptionElementQueryFactory)
                                , CommonTaxonName.class
                                ),
                    CommonTaxonName.class);
            logger.debug("byCommonNameJoinQuery: " + byCommonNameJoinQuery.toString());
            LuceneSearch byCommonNameSearch = new LuceneSearch(luceneIndexToolProvider, GroupByTaxonClassBridge.GROUPBY_TAXON_FIELD, Taxon.class);
            byCommonNameSearch.setCdmTypRestriction(Taxon.class);
            byCommonNameSearch.setQuery(byCommonNameJoinQuery);
            byCommonNameSearch.setSortFields(sortFields);
            idFieldMap.put(CdmBaseType.TAXON, "id");

            luceneSearches.add(byCommonNameSearch);

            /* A) does not work!!!!
            luceneSearches.add(
                    prepareByDescriptionElementFullTextSearch(CommonTaxonName.class,
                            queryString, classification, null, languages, highlightFragments)
                        );
            idFieldMap.put(CdmBaseType.DESCRIPTION_ELEMENT, "inDescription.taxon.id");
            if(addDistributionFilter){
                // in this case we are able to use DescriptionElementBase documents
                // which are matching the areas in question directly
                BooleanQuery byDistributionQuery = createByDistributionQuery(
                        namedAreaList,
                        distributionStatusList,
                        distributionFilterQueryFactory
                        );
                multiIndexByAreaFilter.add(new QueryWrapperFilter(byDistributionQuery), Occur.SHOULD);
            } */
        }

        // search by misapplied names
        if(searchModes.contains(TaxaAndNamesSearchMode.doMisappliedNames)) {
            // NOTE:
            // prepareFindByTaxonRelationFullTextSearch() is making use of JoinUtil.createJoinQuery()
            // which allows doing query time joins
            // finds the misapplied name (Taxon B) which is an misapplication for
            // a related Taxon A.
            //
            luceneSearches.add(prepareFindByTaxonRelationFullTextSearch(
                    new TaxonRelationshipEdge(TaxonRelationshipType.MISAPPLIED_NAME_FOR(), Direction.relatedTo),
                    queryString, classification, languages, highlightFragments, sortFields));
            idFieldMap.put(CdmBaseType.TAXON, "id");

            if(addDistributionFilter){
                String fromField = "inDescription.taxon.id"; // in DescriptionElementBase index

                /*
                 * Here i was facing wired and nasty bug which took me bugging be really for hours until I found this solution.
                 * Maybe this is a but in java itself java.
                 *
                 * When the string toField is constructed by using the expression TaxonRelationshipType.MISAPPLIED_NAME_FOR().getUuid().toString()
                 * directly:
                 *
                 *    String toField = "relation." + TaxonRelationshipType.MISAPPLIED_NAME_FOR().getUuid().toString() +".to.id";
                 *
                 * The byDistributionQuery fails, however when the uuid is first stored in another string variable the query
                 * will execute as expected:
                 *
                 *    String misappliedNameForUuid = TaxonRelationshipType.MISAPPLIED_NAME_FOR().getUuid().toString();
                 *    String toField = "relation." + misappliedNameForUuid +".to.id";
                 *
                 * Comparing both strings by the String.equals method returns true, so both String are identical.
                 *
                 * The bug occurs when running eu.etaxonomy.cdm.api.service.TaxonServiceSearchTest in eclipse and in maven and seems to to be
                 * dependent from a specific jvm (openjdk6  6b27-1.12.6-1ubuntu0.13.04.2, openjdk7 7u25-2.3.10-1ubuntu0.13.04.2,  oracle jdk1.7.0_25 tested)
                 * The bug is persistent after a reboot of the development computer.
                 */
//                String misappliedNameForUuid = TaxonRelationshipType.MISAPPLIED_NAME_FOR().getUuid().toString();
//                String toField = "relation." + misappliedNameForUuid +".to.id";
                String toField = "relation.1ed87175-59dd-437e-959e-0d71583d8417.to.id";
//                System.out.println("relation.1ed87175-59dd-437e-959e-0d71583d8417.to.id".equals("relation." + misappliedNameForUuid +".to.id") ? " > identical" : " > different");
//                System.out.println("relation.1ed87175-59dd-437e-959e-0d71583d8417.to.id".equals("relation." + TaxonRelationshipType.MISAPPLIED_NAME_FOR().getUuid().toString() +".to.id") ? " > identical" : " > different");

                BooleanQuery byDistributionQuery = createByDistributionQuery(namedAreaList, distributionStatusList, distributionFilterQueryFactory);
                Query taxonAreaJoinQuery = distributionFilterQueryFactory.newJoinQuery(fromField, toField, byDistributionQuery, Distribution.class);
                QueryWrapperFilter filter = new QueryWrapperFilter(taxonAreaJoinQuery);

//                debug code for bug described above
                DocIdSet filterMatchSet = filter.getDocIdSet(luceneIndexToolProvider.getIndexReaderFor(Taxon.class));
//                System.err.println(DocIdBitSetPrinter.docsAsString(filterMatchSet, 100));

                multiIndexByAreaFilter.add(filter, Occur.SHOULD);
            }
        }

        LuceneMultiSearch multiSearch = new LuceneMultiSearch(luceneIndexToolProvider,
                luceneSearches.toArray(new LuceneSearch[luceneSearches.size()]));


        if(addDistributionFilter){

            // B)
            // in this case we need a filter which uses a join query
            // to get the TaxonBase documents for the DescriptionElementBase documents
            // which are matching the areas in question
            //
            // for toTaxa, doByCommonName
            Query taxonAreaJoinQuery = createByDistributionJoinQuery(
                    namedAreaList,
                    distributionStatusList,
                    distributionFilterQueryFactory
                    );
            multiIndexByAreaFilter.add(new QueryWrapperFilter(taxonAreaJoinQuery), Occur.SHOULD);
        }

        if (addDistributionFilter){
            multiSearch.setFilter(multiIndexByAreaFilter);
        }


        // --- execute search
        TopGroupsWithMaxScore topDocsResultSet = multiSearch.executeSearch(pageSize, pageNumber);

        // --- initialize taxa, highlight matches ....
        ISearchResultBuilder searchResultBuilder = new SearchResultBuilder(multiSearch, multiSearch.getQuery());


        List<SearchResult<TaxonBase>> searchResults = searchResultBuilder.createResultSet(
                topDocsResultSet, multiSearch.getHighlightFields(), dao, idFieldMap, propertyPaths);

        int totalHits = topDocsResultSet != null ? topDocsResultSet.topGroups.totalGroupCount : 0;
        return new DefaultPagerImpl<SearchResult<TaxonBase>>(pageNumber, totalHits, pageSize, searchResults);
    }

    /**
     * @param namedAreaList at least one area must be in the list
     * @param distributionStatusList optional
     * @return
     * @throws IOException
     */
    protected Query createByDistributionJoinQuery(
            List<NamedArea> namedAreaList,
            List<PresenceAbsenceTerm> distributionStatusList,
            QueryFactory queryFactory
            ) throws IOException {

        String fromField = "inDescription.taxon.id"; // in DescriptionElementBase index
        String toField = "id"; // id in TaxonBase index

        BooleanQuery byDistributionQuery = createByDistributionQuery(namedAreaList, distributionStatusList, queryFactory);

        Query taxonAreaJoinQuery = queryFactory.newJoinQuery(fromField, toField, byDistributionQuery, Distribution.class);

        return taxonAreaJoinQuery;
    }

    /**
     * @param namedAreaList
     * @param distributionStatusList
     * @param queryFactory
     * @return
     */
    private BooleanQuery createByDistributionQuery(List<NamedArea> namedAreaList,
            List<PresenceAbsenceTerm> distributionStatusList, QueryFactory queryFactory) {
        BooleanQuery areaQuery = new BooleanQuery();
        // area field from Distribution
        areaQuery.add(queryFactory.newEntityIdsQuery("area.id", namedAreaList), Occur.MUST);

        // status field from Distribution
        if(distributionStatusList != null && distributionStatusList.size() > 0){
            areaQuery.add(queryFactory.newEntityIdsQuery("status.id", distributionStatusList), Occur.MUST);
        }

        logger.debug("createByDistributionQuery() query: " + areaQuery.toString());
        return areaQuery;
    }

    /**
     * This method has been primarily created for testing the area join query but might
     * also be useful in other situations
     *
     * @param namedAreaList
     * @param distributionStatusList
     * @param classification
     * @param highlightFragments
     * @return
     * @throws IOException
     */
    protected LuceneSearch prepareByDistributionSearch(
            List<NamedArea> namedAreaList, List<PresenceAbsenceTerm> distributionStatusList,
            Classification classification) throws IOException {

        BooleanQuery finalQuery = new BooleanQuery();

        LuceneSearch luceneSearch = new LuceneSearch(luceneIndexToolProvider, GroupByTaxonClassBridge.GROUPBY_TAXON_FIELD, Taxon.class);

        // FIXME is this query factory using the wrong type?
        QueryFactory taxonQueryFactory = luceneIndexToolProvider.newQueryFactoryFor(Taxon.class);

        SortField[] sortFields = new  SortField[]{SortField.FIELD_SCORE, new SortField("titleCache__sort", SortField.STRING, false)};
        luceneSearch.setSortFields(sortFields);


        Query byAreaQuery = createByDistributionJoinQuery(namedAreaList, distributionStatusList, taxonQueryFactory);

        finalQuery.add(byAreaQuery, Occur.MUST);

        if(classification != null){
            finalQuery.add(taxonQueryFactory.newEntityIdQuery("taxonNodes.classification.id", classification), Occur.MUST);
        }

        logger.info("prepareByAreaSearch() query: " + finalQuery.toString());
        luceneSearch.setQuery(finalQuery);

        return luceneSearch;
    }

    @Override
    public Pager<SearchResult<TaxonBase>> findByDescriptionElementFullText(
            Class<? extends DescriptionElementBase> clazz, String queryString,
            Classification classification, List<Feature> features, List<Language> languages,
            boolean highlightFragments, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) throws CorruptIndexException, IOException, ParseException {


        LuceneSearch luceneSearch = prepareByDescriptionElementFullTextSearch(clazz, queryString, classification, features, languages, highlightFragments);

        // --- execute search
        TopGroupsWithMaxScore topDocsResultSet = luceneSearch.executeSearch(pageSize, pageNumber);

        Map<CdmBaseType, String> idFieldMap = new HashMap<CdmBaseType, String>();
        idFieldMap.put(CdmBaseType.DESCRIPTION_ELEMENT, "inDescription.taxon.id");

        // --- initialize taxa, highlight matches ....
        ISearchResultBuilder searchResultBuilder = new SearchResultBuilder(luceneSearch, luceneSearch.getQuery());
        @SuppressWarnings("rawtypes")
        List<SearchResult<TaxonBase>> searchResults = searchResultBuilder.createResultSet(
                topDocsResultSet, luceneSearch.getHighlightFields(), dao, idFieldMap, propertyPaths);

        int totalHits = topDocsResultSet != null ? topDocsResultSet.topGroups.totalGroupCount : 0;
        return new DefaultPagerImpl<SearchResult<TaxonBase>>(pageNumber, totalHits, pageSize, searchResults);

    }


    @Override
    public Pager<SearchResult<TaxonBase>> findByEverythingFullText(String queryString,
            Classification classification, List<Language> languages, boolean highlightFragments,
            Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) throws CorruptIndexException, IOException, ParseException, LuceneMultiSearchException {

        LuceneSearch luceneSearchByDescriptionElement = prepareByDescriptionElementFullTextSearch(null, queryString, classification, null, languages, highlightFragments);
        LuceneSearch luceneSearchByTaxonBase = prepareFindByFullTextSearch(null, queryString, classification, languages, highlightFragments, null);

        LuceneMultiSearch multiSearch = new LuceneMultiSearch(luceneIndexToolProvider, luceneSearchByDescriptionElement, luceneSearchByTaxonBase);

        // --- execute search
        TopGroupsWithMaxScore topDocsResultSet = multiSearch.executeSearch(pageSize, pageNumber);

        // --- initialize taxa, highlight matches ....
        ISearchResultBuilder searchResultBuilder = new SearchResultBuilder(multiSearch, multiSearch.getQuery());

        Map<CdmBaseType, String> idFieldMap = new HashMap<CdmBaseType, String>();
        idFieldMap.put(CdmBaseType.TAXON, "id");
        idFieldMap.put(CdmBaseType.DESCRIPTION_ELEMENT, "inDescription.taxon.id");

        List<SearchResult<TaxonBase>> searchResults = searchResultBuilder.createResultSet(
                topDocsResultSet, multiSearch.getHighlightFields(), dao, idFieldMap, propertyPaths);

        int totalHits = topDocsResultSet != null ? topDocsResultSet.topGroups.totalGroupCount : 0;
        return new DefaultPagerImpl<SearchResult<TaxonBase>>(pageNumber, totalHits, pageSize, searchResults);

    }


    /**
     * @param clazz
     * @param queryString
     * @param classification
     * @param features
     * @param languages
     * @param highlightFragments
     * @param directorySelectClass
     * @return
     */
    protected LuceneSearch prepareByDescriptionElementFullTextSearch(Class<? extends CdmBase> clazz,
            String queryString, Classification classification, List<Feature> features,
            List<Language> languages, boolean highlightFragments) {

        LuceneSearch luceneSearch = new LuceneSearch(luceneIndexToolProvider, GroupByTaxonClassBridge.GROUPBY_TAXON_FIELD, DescriptionElementBase.class);
        QueryFactory descriptionElementQueryFactory = luceneIndexToolProvider.newQueryFactoryFor(DescriptionElementBase.class);

        SortField[] sortFields = new  SortField[]{SortField.FIELD_SCORE, new SortField("inDescription.taxon.titleCache__sort", SortField.STRING, false)};

        BooleanQuery finalQuery = createByDescriptionElementFullTextQuery(queryString, classification, features,
                languages, descriptionElementQueryFactory);

        luceneSearch.setSortFields(sortFields);
        luceneSearch.setCdmTypRestriction(clazz);
        luceneSearch.setQuery(finalQuery);
        if(highlightFragments){
            luceneSearch.setHighlightFields(descriptionElementQueryFactory.getTextFieldNamesAsArray());
        }

        return luceneSearch;
    }

    /**
     * @param queryString
     * @param classification
     * @param features
     * @param languages
     * @param descriptionElementQueryFactory
     * @return
     */
    private BooleanQuery createByDescriptionElementFullTextQuery(String queryString, Classification classification,
            List<Feature> features, List<Language> languages, QueryFactory descriptionElementQueryFactory) {
        BooleanQuery finalQuery = new BooleanQuery();
        BooleanQuery textQuery = new BooleanQuery();
        textQuery.add(descriptionElementQueryFactory.newTermQuery("titleCache", queryString), Occur.SHOULD);

        // common name
        Query nameQuery;
        if(languages == null || languages.size() == 0){
            nameQuery = descriptionElementQueryFactory.newTermQuery("name", queryString);
        } else {
            nameQuery = new BooleanQuery();
            BooleanQuery languageSubQuery = new BooleanQuery();
            for(Language lang : languages){
                languageSubQuery.add(descriptionElementQueryFactory.newTermQuery("language.uuid",  lang.getUuid().toString(), false), Occur.SHOULD);
            }
            ((BooleanQuery) nameQuery).add(descriptionElementQueryFactory.newTermQuery("name", queryString), Occur.MUST);
            ((BooleanQuery) nameQuery).add(languageSubQuery, Occur.MUST);
        }
        textQuery.add(nameQuery, Occur.SHOULD);


        // text field from TextData
        textQuery.add(descriptionElementQueryFactory.newMultilanguageTextQuery("text", queryString, languages), Occur.SHOULD);

        // --- TermBase fields - by representation ----
        // state field from CategoricalData
        textQuery.add(descriptionElementQueryFactory.newDefinedTermQuery("stateData.state", queryString, languages), Occur.SHOULD);

        // state field from CategoricalData
        textQuery.add(descriptionElementQueryFactory.newDefinedTermQuery("stateData.modifyingText", queryString, languages), Occur.SHOULD);

        // area field from Distribution
        textQuery.add(descriptionElementQueryFactory.newDefinedTermQuery("area", queryString, languages), Occur.SHOULD);

        // status field from Distribution
        textQuery.add(descriptionElementQueryFactory.newDefinedTermQuery("status", queryString, languages), Occur.SHOULD);

        finalQuery.add(textQuery, Occur.MUST);
        // --- classification ----

        if(classification != null){
            finalQuery.add(descriptionElementQueryFactory.newEntityIdQuery("inDescription.taxon.taxonNodes.classification.id", classification), Occur.MUST);
        }

        // --- IdentifieableEntity fields - by uuid
        if(features != null && features.size() > 0 ){
            finalQuery.add(descriptionElementQueryFactory.newEntityUuidsQuery("feature.uuid", features), Occur.MUST);
        }

        // the description must be associated with a taxon
        finalQuery.add(descriptionElementQueryFactory.newIsNotNullQuery("inDescription.taxon.id"), Occur.MUST);

        logger.info("prepareByDescriptionElementFullTextSearch() query: " + finalQuery.toString());
        return finalQuery;
    }

    /**
     * DefinedTerm representations and MultilanguageString maps are stored in the Lucene index by the {@link DefinedTermBaseClassBridge}
     * and {@link MultilanguageTextFieldBridge } in a consistent way. One field per language and also in one additional field for all languages.
     * This method is a convenient means to retrieve a Lucene query string for such the fields.
     *
     * @param name name of the term field as in the Lucene index. Must be field created by {@link DefinedTermBaseClassBridge}
     * or {@link MultilanguageTextFieldBridge }
     * @param languages the languages to search for exclusively. Can be <code>null</code> to search in all languages
     * @param stringBuilder a StringBuilder to be reused, if <code>null</code> a new StringBuilder will be instantiated and is returned
     * @return the StringBuilder given a parameter or a new one if the stringBuilder parameter was null.
     *
     * TODO move to utiliy class !!!!!!!!
     */
    private StringBuilder appendLocalizedFieldQuery(String name, List<Language> languages, StringBuilder stringBuilder) {

        if(stringBuilder == null){
            stringBuilder = new StringBuilder();
        }
        if(languages == null || languages.size() == 0){
            stringBuilder.append(name + ".ALL:(%1$s) ");
        } else {
            for(Language lang : languages){
                stringBuilder.append(name + "." + lang.getUuid().toString() + ":(%1$s) ");
            }
        }
        return stringBuilder;
    }

    @Override
    public List<Synonym> createInferredSynonyms(Taxon taxon, Classification classification, SynonymRelationshipType type, boolean doWithMisappliedNames){
        List <Synonym> inferredSynonyms = new ArrayList<Synonym>();
        List<Synonym> inferredSynonymsToBeRemoved = new ArrayList<Synonym>();

        HashMap <UUID, ZoologicalName> zooHashMap = new HashMap<UUID, ZoologicalName>();


        UUID nameUuid= taxon.getName().getUuid();
        ZoologicalName taxonName = getZoologicalName(nameUuid, zooHashMap);
        String epithetOfTaxon = null;
        String infragenericEpithetOfTaxon = null;
        String infraspecificEpithetOfTaxon = null;
        if (taxonName.isSpecies()){
             epithetOfTaxon= taxonName.getSpecificEpithet();
        } else if (taxonName.isInfraGeneric()){
            infragenericEpithetOfTaxon = taxonName.getInfraGenericEpithet();
        } else if (taxonName.isInfraSpecific()){
            infraspecificEpithetOfTaxon = taxonName.getInfraSpecificEpithet();
        }
        String genusOfTaxon = taxonName.getGenusOrUninomial();
        Set<TaxonNode> nodes = taxon.getTaxonNodes();
        List<String> taxonNames = new ArrayList<String>();

        for (TaxonNode node: nodes){
           // HashMap<String, String> synonymsGenus = new HashMap<String, String>(); // Changed this to be able to store the idInSource to a genusName
           // List<String> synonymsEpithet = new ArrayList<String>();

            if (node.getClassification().equals(classification)){
                if (!node.isTopmostNode()){
                    TaxonNode parent = node.getParent();
                    parent = (TaxonNode)HibernateProxyHelper.deproxy(parent);
                    TaxonNameBase<?,?> parentName =  parent.getTaxon().getName();
                    ZoologicalName zooParentName = HibernateProxyHelper.deproxy(parentName, ZoologicalName.class);
                    Taxon parentTaxon = (Taxon)HibernateProxyHelper.deproxy(parent.getTaxon());
                    Rank rankOfTaxon = taxonName.getRank();


                    //create inferred synonyms for species, subspecies
                    if ((parentName.isGenus() || parentName.isSpecies() || parentName.getRank().equals(Rank.SUBGENUS())) ){

                        Synonym inferredEpithet = null;
                        Synonym inferredGenus = null;
                        Synonym potentialCombination = null;

                        List<String> propertyPaths = new ArrayList<String>();
                        propertyPaths.add("synonym");
                        propertyPaths.add("synonym.name");
                        List<OrderHint> orderHints = new ArrayList<OrderHint>();
                        orderHints.add(new OrderHint("relatedFrom.titleCache", SortOrder.ASCENDING));

                        List<SynonymRelationship> synonymRelationshipsOfParent = dao.getSynonyms(parentTaxon, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF(), null, null,orderHints,propertyPaths);
                        List<SynonymRelationship> synonymRelationshipsOfTaxon= dao.getSynonyms(taxon, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF(), null, null,orderHints,propertyPaths);

                        List<TaxonRelationship> taxonRelListParent = null;
                        List<TaxonRelationship> taxonRelListTaxon = null;
                        if (doWithMisappliedNames){
                            taxonRelListParent = dao.getTaxonRelationships(parentTaxon, TaxonRelationshipType.MISAPPLIED_NAME_FOR(), null, null, orderHints, propertyPaths, Direction.relatedTo);
                            taxonRelListTaxon = dao.getTaxonRelationships(taxon, TaxonRelationshipType.MISAPPLIED_NAME_FOR(), null, null, orderHints, propertyPaths, Direction.relatedTo);
                        }


                        if (type.equals(SynonymRelationshipType.INFERRED_EPITHET_OF())){


                            for (SynonymRelationship synonymRelationOfParent:synonymRelationshipsOfParent){
                                Synonym syn = synonymRelationOfParent.getSynonym();

                                inferredEpithet = createInferredEpithets(taxon,
                                        zooHashMap, taxonName, epithetOfTaxon,
                                        infragenericEpithetOfTaxon,
                                        infraspecificEpithetOfTaxon,
                                        taxonNames, parentName,
                                        syn);


                                inferredSynonyms.add(inferredEpithet);
                                zooHashMap.put(inferredEpithet.getName().getUuid(), (ZoologicalName)inferredEpithet.getName());
                                taxonNames.add(((ZoologicalName)inferredEpithet.getName()).getNameCache());
                            }

                            if (doWithMisappliedNames){

                                for (TaxonRelationship taxonRelationship: taxonRelListParent){
                                     Taxon misappliedName = taxonRelationship.getFromTaxon();

                                     inferredEpithet = createInferredEpithets(taxon,
                                             zooHashMap, taxonName, epithetOfTaxon,
                                             infragenericEpithetOfTaxon,
                                             infraspecificEpithetOfTaxon,
                                             taxonNames, parentName,
                                             misappliedName);

                                    inferredSynonyms.add(inferredEpithet);
                                    zooHashMap.put(inferredEpithet.getName().getUuid(), (ZoologicalName)inferredEpithet.getName());
                                     taxonNames.add(((ZoologicalName)inferredEpithet.getName()).getNameCache());
                                }
                            }

                            if (!taxonNames.isEmpty()){
                            List<String> synNotInCDM = dao.taxaByNameNotInDB(taxonNames);
                            ZoologicalName name;
                            if (!synNotInCDM.isEmpty()){
                                inferredSynonymsToBeRemoved.clear();

                                for (Synonym syn :inferredSynonyms){
                                    name = getZoologicalName(syn.getName().getUuid(), zooHashMap);
                                    if (!synNotInCDM.contains(name.getNameCache())){
                                        inferredSynonymsToBeRemoved.add(syn);
                                    }
                                }

                                // Remove identified Synonyms from inferredSynonyms
                                for (Synonym synonym : inferredSynonymsToBeRemoved) {
                                    inferredSynonyms.remove(synonym);
                                }
                            }
                        }

                    }else if (type.equals(SynonymRelationshipType.INFERRED_GENUS_OF())){


                        for (SynonymRelationship synonymRelationOfTaxon:synonymRelationshipsOfTaxon){
                            TaxonNameBase synName;
                            ZoologicalName inferredSynName;

                            Synonym syn = synonymRelationOfTaxon.getSynonym();
                            inferredGenus = createInferredGenus(taxon,
                                    zooHashMap, taxonName, epithetOfTaxon,
                                    genusOfTaxon, taxonNames, zooParentName, syn);

                            inferredSynonyms.add(inferredGenus);
                            zooHashMap.put(inferredGenus.getName().getUuid(), (ZoologicalName)inferredGenus.getName());
                            taxonNames.add(( (ZoologicalName)inferredGenus.getName()).getNameCache());


                        }

                        if (doWithMisappliedNames){

                            for (TaxonRelationship taxonRelationship: taxonRelListTaxon){
                                Taxon misappliedName = taxonRelationship.getFromTaxon();
                                inferredGenus = createInferredGenus(taxon, zooHashMap, taxonName, infraspecificEpithetOfTaxon, genusOfTaxon, taxonNames, zooParentName,  misappliedName);

                                inferredSynonyms.add(inferredGenus);
                                zooHashMap.put(inferredGenus.getName().getUuid(), (ZoologicalName)inferredGenus.getName());
                                 taxonNames.add(( (ZoologicalName)inferredGenus.getName()).getNameCache());
                            }
                        }


                        if (!taxonNames.isEmpty()){
                            List<String> synNotInCDM = dao.taxaByNameNotInDB(taxonNames);
                            ZoologicalName name;
                            if (!synNotInCDM.isEmpty()){
                                inferredSynonymsToBeRemoved.clear();

                                for (Synonym syn :inferredSynonyms){
                                    name = getZoologicalName(syn.getName().getUuid(), zooHashMap);
                                    if (!synNotInCDM.contains(name.getNameCache())){
                                        inferredSynonymsToBeRemoved.add(syn);
                                    }
                                }

                                // Remove identified Synonyms from inferredSynonyms
                                for (Synonym synonym : inferredSynonymsToBeRemoved) {
                                    inferredSynonyms.remove(synonym);
                                }
                            }
                        }

                    }else if (type.equals(SynonymRelationshipType.POTENTIAL_COMBINATION_OF())){

                        Reference sourceReference = null; // TODO: Determination of sourceReference is redundant
                        ZoologicalName inferredSynName;
                        //for all synonyms of the parent...
                        for (SynonymRelationship synonymRelationOfParent:synonymRelationshipsOfParent){
                            TaxonNameBase synName;
                            Synonym synParent = synonymRelationOfParent.getSynonym();
                            synName = synParent.getName();

                            HibernateProxyHelper.deproxy(synParent);

                            // Set the sourceReference
                            sourceReference = synParent.getSec();

                            // Determine the idInSource
                            String idInSourceParent = getIdInSource(synParent);

                            ZoologicalName parentSynZooName = getZoologicalName(synName.getUuid(), zooHashMap);
                            String synParentGenus = parentSynZooName.getGenusOrUninomial();
                            String synParentInfragenericName = null;
                            String synParentSpecificEpithet = null;

                            if (parentSynZooName.isInfraGeneric()){
                                synParentInfragenericName = parentSynZooName.getInfraGenericEpithet();
                            }
                            if (parentSynZooName.isSpecies()){
                                synParentSpecificEpithet = parentSynZooName.getSpecificEpithet();
                            }

                           /* if (synGenusName != null && !synonymsGenus.containsKey(synGenusName)){
                                synonymsGenus.put(synGenusName, idInSource);
                            }*/

                            //for all synonyms of the taxon

                            for (SynonymRelationship synonymRelationOfTaxon:synonymRelationshipsOfTaxon){

                                Synonym syn = synonymRelationOfTaxon.getSynonym();
                                ZoologicalName zooSynName = getZoologicalName(syn.getName().getUuid(), zooHashMap);
                                potentialCombination = createPotentialCombination(idInSourceParent, parentSynZooName, zooSynName,
                                        synParentGenus,
                                        synParentInfragenericName,
                                        synParentSpecificEpithet, syn, zooHashMap);

                                taxon.addSynonym(potentialCombination, SynonymRelationshipType.POTENTIAL_COMBINATION_OF());
                                inferredSynonyms.add(potentialCombination);
                                zooHashMap.put(potentialCombination.getName().getUuid(), (ZoologicalName)potentialCombination.getName());
                                 taxonNames.add(( (ZoologicalName)potentialCombination.getName()).getNameCache());

                            }


                        }

                        if (doWithMisappliedNames){

                            for (TaxonRelationship parentRelationship: taxonRelListParent){

                                TaxonNameBase misappliedParentName;

                                Taxon misappliedParent = parentRelationship.getFromTaxon();
                                misappliedParentName = misappliedParent.getName();

                                HibernateProxyHelper.deproxy(misappliedParent);

                                // Set the sourceReference
                                sourceReference = misappliedParent.getSec();

                                // Determine the idInSource
                                String idInSourceParent = getIdInSource(misappliedParent);

                                ZoologicalName parentSynZooName = getZoologicalName(misappliedParentName.getUuid(), zooHashMap);
                                String synParentGenus = parentSynZooName.getGenusOrUninomial();
                                String synParentInfragenericName = null;
                                String synParentSpecificEpithet = null;

                                if (parentSynZooName.isInfraGeneric()){
                                    synParentInfragenericName = parentSynZooName.getInfraGenericEpithet();
                                }
                                if (parentSynZooName.isSpecies()){
                                    synParentSpecificEpithet = parentSynZooName.getSpecificEpithet();
                                }


                                for (TaxonRelationship taxonRelationship: taxonRelListTaxon){
                                    Taxon misappliedName = taxonRelationship.getFromTaxon();
                                    ZoologicalName zooMisappliedName = getZoologicalName(misappliedName.getName().getUuid(), zooHashMap);
                                    potentialCombination = createPotentialCombination(
                                            idInSourceParent, parentSynZooName, zooMisappliedName,
                                            synParentGenus,
                                            synParentInfragenericName,
                                            synParentSpecificEpithet, misappliedName, zooHashMap);


                                    taxon.addSynonym(potentialCombination, SynonymRelationshipType.POTENTIAL_COMBINATION_OF());
                                    inferredSynonyms.add(potentialCombination);
                                    zooHashMap.put(potentialCombination.getName().getUuid(), (ZoologicalName)potentialCombination.getName());
                                     taxonNames.add(( (ZoologicalName)potentialCombination.getName()).getNameCache());
                                }
                            }
                        }

                        if (!taxonNames.isEmpty()){
                            List<String> synNotInCDM = dao.taxaByNameNotInDB(taxonNames);
                            ZoologicalName name;
                            if (!synNotInCDM.isEmpty()){
                                inferredSynonymsToBeRemoved.clear();
                                for (Synonym syn :inferredSynonyms){
                                    try{
                                        name = (ZoologicalName) syn.getName();
                                    }catch (ClassCastException e){
                                        name = getZoologicalName(syn.getName().getUuid(), zooHashMap);
                                    }
                                    if (!synNotInCDM.contains(name.getNameCache())){
                                        inferredSynonymsToBeRemoved.add(syn);
                                    }
                                 }
                                // Remove identified Synonyms from inferredSynonyms
                                for (Synonym synonym : inferredSynonymsToBeRemoved) {
                                    inferredSynonyms.remove(synonym);
                                }
                            }
                         }
                        }
                    }else {
                        logger.info("The synonymrelationship type is not defined.");
                        return inferredSynonyms;
                    }
                }
            }

        }

        return inferredSynonyms;
    }

    private Synonym createPotentialCombination(String idInSourceParent,
            ZoologicalName parentSynZooName, 	ZoologicalName zooSynName, String synParentGenus,
            String synParentInfragenericName, String synParentSpecificEpithet,
            TaxonBase syn, HashMap<UUID, ZoologicalName> zooHashMap) {
        Synonym potentialCombination;
        Reference sourceReference;
        ZoologicalName inferredSynName;
        HibernateProxyHelper.deproxy(syn);

        // Set sourceReference
        sourceReference = syn.getSec();
        if (sourceReference == null){
            logger.warn("The synonym has no sec reference because it is a misapplied name! Take the sec reference of taxon");
            //TODO:Remove
            if (!parentSynZooName.getTaxa().isEmpty()){
                TaxonBase taxon = parentSynZooName.getTaxa().iterator().next();

                sourceReference = taxon.getSec();
            }
        }
        String synTaxonSpecificEpithet = zooSynName.getSpecificEpithet();

        String synTaxonInfraSpecificName= null;

        if (parentSynZooName.isSpecies()){
            synTaxonInfraSpecificName = zooSynName.getInfraSpecificEpithet();
        }

        /*if (epithetName != null && !synonymsEpithet.contains(epithetName)){
            synonymsEpithet.add(epithetName);
        }*/

        //create potential combinations...
        inferredSynName = ZoologicalName.NewInstance(syn.getName().getRank());

        inferredSynName.setGenusOrUninomial(synParentGenus);
        if (zooSynName.isSpecies()){
              inferredSynName.setSpecificEpithet(synTaxonSpecificEpithet);
              if (parentSynZooName.isInfraGeneric()){
                  inferredSynName.setInfraGenericEpithet(synParentInfragenericName);
              }
        }
        if (zooSynName.isInfraSpecific()){
            inferredSynName.setSpecificEpithet(synParentSpecificEpithet);
            inferredSynName.setInfraSpecificEpithet(synTaxonInfraSpecificName);
        }
        if (parentSynZooName.isInfraGeneric()){
            inferredSynName.setInfraGenericEpithet(synParentInfragenericName);
        }


        potentialCombination = Synonym.NewInstance(inferredSynName, null);

        // Set the sourceReference
        potentialCombination.setSec(sourceReference);


        // Determine the idInSource
        String idInSourceSyn= getIdInSource(syn);

        if (idInSourceParent != null && idInSourceSyn != null) {
            IdentifiableSource originalSource = IdentifiableSource.NewInstance(OriginalSourceType.Transformation, idInSourceSyn + "; " + idInSourceParent, POTENTIAL_COMBINATION_NAMESPACE, sourceReference, null);
            inferredSynName.addSource(originalSource);
            originalSource = IdentifiableSource.NewInstance(OriginalSourceType.Transformation, idInSourceSyn + "; " + idInSourceParent, POTENTIAL_COMBINATION_NAMESPACE, sourceReference, null);
            potentialCombination.addSource(originalSource);
        }

        return potentialCombination;
    }

    private Synonym createInferredGenus(Taxon taxon,
            HashMap<UUID, ZoologicalName> zooHashMap, ZoologicalName taxonName,
            String epithetOfTaxon, String genusOfTaxon,
            List<String> taxonNames, ZoologicalName zooParentName,
            TaxonBase syn) {

        Synonym inferredGenus;
        TaxonNameBase synName;
        ZoologicalName inferredSynName;
        synName =syn.getName();
        HibernateProxyHelper.deproxy(syn);

        // Determine the idInSource
        String idInSourceSyn = getIdInSource(syn);
        String idInSourceTaxon = getIdInSource(taxon);
        // Determine the sourceReference
        Reference sourceReference = syn.getSec();

        //logger.warn(sourceReference.getTitleCache());

        synName = syn.getName();
        ZoologicalName synZooName = getZoologicalName(synName.getUuid(), zooHashMap);
        String synSpeciesEpithetName = synZooName.getSpecificEpithet();
                     /* if (synonymsEpithet != null && !synonymsEpithet.contains(synSpeciesEpithetName)){
            synonymsEpithet.add(synSpeciesEpithetName);
        }*/

        inferredSynName = ZoologicalName.NewInstance(taxon.getName().getRank());
        //TODO:differ between parent is genus and taxon is species, parent is subgenus and taxon is species, parent is species and taxon is subspecies and parent is genus and taxon is subgenus...


        inferredSynName.setGenusOrUninomial(genusOfTaxon);
        if (zooParentName.isInfraGeneric()){
            inferredSynName.setInfraGenericEpithet(zooParentName.getInfraGenericEpithet());
        }

        if (taxonName.isSpecies()){
            inferredSynName.setSpecificEpithet(synSpeciesEpithetName);
        }
        if (taxonName.isInfraSpecific()){
            inferredSynName.setSpecificEpithet(epithetOfTaxon);
            inferredSynName.setInfraSpecificEpithet(synZooName.getInfraGenericEpithet());
        }


        inferredGenus = Synonym.NewInstance(inferredSynName, null);

        // Set the sourceReference
        inferredGenus.setSec(sourceReference);

        // Add the original source
        if (idInSourceSyn != null && idInSourceTaxon != null) {
            IdentifiableSource originalSource = IdentifiableSource.NewInstance(OriginalSourceType.Transformation,
                    idInSourceSyn + "; " + idInSourceTaxon, INFERRED_GENUS_NAMESPACE, sourceReference, null);
            inferredGenus.addSource(originalSource);

            originalSource = IdentifiableSource.NewInstance(OriginalSourceType.Transformation,
                    idInSourceSyn + "; " + idInSourceTaxon, INFERRED_GENUS_NAMESPACE, sourceReference, null);
            inferredSynName.addSource(originalSource);
            originalSource = null;

        }else{
            logger.error("There is an idInSource missing: " + idInSourceSyn + " of Synonym or " + idInSourceTaxon + " of Taxon");
            IdentifiableSource originalSource = IdentifiableSource.NewInstance(OriginalSourceType.Transformation,
                    idInSourceSyn + "; " + idInSourceTaxon, INFERRED_GENUS_NAMESPACE, sourceReference, null);
            inferredGenus.addSource(originalSource);

            originalSource = IdentifiableSource.NewInstance(OriginalSourceType.Transformation,
                    idInSourceSyn + "; " + idInSourceTaxon, INFERRED_GENUS_NAMESPACE, sourceReference, null);
            inferredSynName.addSource(originalSource);
            originalSource = null;
        }

        taxon.addSynonym(inferredGenus, SynonymRelationshipType.INFERRED_GENUS_OF());

        return inferredGenus;
    }

    private Synonym createInferredEpithets(Taxon taxon,
            HashMap<UUID, ZoologicalName> zooHashMap, ZoologicalName taxonName,
            String epithetOfTaxon, String infragenericEpithetOfTaxon,
            String infraspecificEpithetOfTaxon, List<String> taxonNames,
            TaxonNameBase parentName, TaxonBase syn) {

        Synonym inferredEpithet;
        TaxonNameBase<?,?> synName;
        ZoologicalName inferredSynName;
        HibernateProxyHelper.deproxy(syn);

        // Determine the idInSource
        String idInSourceSyn = getIdInSource(syn);
        String idInSourceTaxon =  getIdInSource(taxon);
        // Determine the sourceReference
        Reference<?> sourceReference = syn.getSec();

        if (sourceReference == null){
             logger.warn("The synonym has no sec reference because it is a misapplied name! Take the sec reference of taxon" + taxon.getSec());
             sourceReference = taxon.getSec();
        }

        synName = syn.getName();
        ZoologicalName zooSynName = getZoologicalName(synName.getUuid(), zooHashMap);
        String synGenusName = zooSynName.getGenusOrUninomial();
        String synInfraGenericEpithet = null;
        String synSpecificEpithet = null;

        if (zooSynName.getInfraGenericEpithet() != null){
            synInfraGenericEpithet = zooSynName.getInfraGenericEpithet();
        }

        if (zooSynName.isInfraSpecific()){
            synSpecificEpithet = zooSynName.getSpecificEpithet();
        }

                     /* if (synGenusName != null && !synonymsGenus.containsKey(synGenusName)){
            synonymsGenus.put(synGenusName, idInSource);
        }*/

        inferredSynName = ZoologicalName.NewInstance(taxon.getName().getRank());

        // DEBUG TODO: for subgenus or subspecies the infrageneric or infraspecific epithet should be used!!!
        if (epithetOfTaxon == null && infragenericEpithetOfTaxon == null && infraspecificEpithetOfTaxon == null) {
            logger.error("This specificEpithet is NULL" + taxon.getTitleCache());
        }
        inferredSynName.setGenusOrUninomial(synGenusName);

        if (parentName.isInfraGeneric()){
            inferredSynName.setInfraGenericEpithet(synInfraGenericEpithet);
        }
        if (taxonName.isSpecies()){
            inferredSynName.setSpecificEpithet(epithetOfTaxon);
        }else if (taxonName.isInfraSpecific()){
            inferredSynName.setSpecificEpithet(synSpecificEpithet);
            inferredSynName.setInfraSpecificEpithet(infraspecificEpithetOfTaxon);
        }

        inferredEpithet = Synonym.NewInstance(inferredSynName, null);

        // Set the sourceReference
        inferredEpithet.setSec(sourceReference);

        /* Add the original source
        if (idInSource != null) {
            IdentifiableSource originalSource = IdentifiableSource.NewInstance(idInSource, "InferredEpithetOf", syn.getSec(), null);

            // Add the citation
            Reference citation = getCitation(syn);
            if (citation != null) {
                originalSource.setCitation(citation);
                inferredEpithet.addSource(originalSource);
            }
        }*/
        String taxonId = idInSourceTaxon+ "; " + idInSourceSyn;


        IdentifiableSource originalSource = IdentifiableSource.NewInstance(OriginalSourceType.Transformation,
                taxonId, INFERRED_EPITHET_NAMESPACE, sourceReference, null);

        inferredEpithet.addSource(originalSource);

        originalSource = IdentifiableSource.NewInstance(OriginalSourceType.Transformation,
                taxonId, INFERRED_EPITHET_NAMESPACE, sourceReference, null);

        inferredSynName.addSource(originalSource);



        taxon.addSynonym(inferredEpithet, SynonymRelationshipType.INFERRED_EPITHET_OF());

        return inferredEpithet;
    }

    /**
     * Returns an existing ZoologicalName or extends an internal hashmap if it does not exist.
     * Very likely only useful for createInferredSynonyms().
     * @param uuid
     * @param zooHashMap
     * @return
     */
    private ZoologicalName getZoologicalName(UUID uuid, HashMap <UUID, ZoologicalName> zooHashMap) {
        ZoologicalName taxonName =nameDao.findZoologicalNameByUUID(uuid);
        if (taxonName == null) {
            taxonName = zooHashMap.get(uuid);
        }
        return taxonName;
    }

    /**
     * Returns the idInSource for a given Synonym.
     * @param syn
     */
    private String getIdInSource(TaxonBase taxonBase) {
        String idInSource = null;
        Set<IdentifiableSource> sources = taxonBase.getSources();
        if (sources.size() == 1) {
            IdentifiableSource source = sources.iterator().next();
            if (source != null) {
                idInSource  = source.getIdInSource();
            }
        } else if (sources.size() > 1) {
            int count = 1;
            idInSource = "";
            for (IdentifiableSource source : sources) {
                idInSource += source.getIdInSource();
                if (count < sources.size()) {
                    idInSource += "; ";
                }
                count++;
            }
        } else if (sources.size() == 0){
            logger.warn("No idInSource for TaxonBase " + taxonBase.getUuid() + " - " + taxonBase.getTitleCache());
        }


        return idInSource;
    }


    /**
     * Returns the citation for a given Synonym.
     * @param syn
     */
    private Reference getCitation(Synonym syn) {
        Reference citation = null;
        Set<IdentifiableSource> sources = syn.getSources();
        if (sources.size() == 1) {
            IdentifiableSource source = sources.iterator().next();
            if (source != null) {
                citation = source.getCitation();
            }
        } else if (sources.size() > 1) {
            logger.warn("This Synonym has more than one source: " + syn.getUuid() + " (" + syn.getTitleCache() +")");
        }

        return citation;
    }

    @Override
    public List<Synonym>  createAllInferredSynonyms(Taxon taxon, Classification tree, boolean doWithMisappliedNames){
        List <Synonym> inferredSynonyms = new ArrayList<Synonym>();

        inferredSynonyms.addAll(createInferredSynonyms(taxon, tree, SynonymRelationshipType.INFERRED_EPITHET_OF(), doWithMisappliedNames));
        inferredSynonyms.addAll(createInferredSynonyms(taxon, tree, SynonymRelationshipType.INFERRED_GENUS_OF(), doWithMisappliedNames));
        inferredSynonyms.addAll(createInferredSynonyms(taxon, tree, SynonymRelationshipType.POTENTIAL_COMBINATION_OF(), doWithMisappliedNames));

        return inferredSynonyms;
    }

    @Override
    public List<Classification> listClassifications(TaxonBase taxonBase, Integer limit, Integer start, List<String> propertyPaths) {

        // TODO quickly implemented, create according dao !!!!
        Set<TaxonNode> nodes = new HashSet<TaxonNode>();
        Set<Classification> classifications = new HashSet<Classification>();
        List<Classification> list = new ArrayList<Classification>();

        if (taxonBase == null) {
            return list;
        }

        taxonBase = load(taxonBase.getUuid());

        if (taxonBase instanceof Taxon) {
            nodes.addAll(((Taxon)taxonBase).getTaxonNodes());
        } else {
            for (Taxon taxon : ((Synonym)taxonBase).getAcceptedTaxa() ) {
                nodes.addAll(taxon.getTaxonNodes());
            }
        }
        for (TaxonNode node : nodes) {
            classifications.add(node.getClassification());
        }
        list.addAll(classifications);
        return list;
    }

    @Override
    @Transactional(readOnly = false)
    public UpdateResult changeRelatedTaxonToSynonym(UUID fromTaxonUuid,
            UUID toTaxonUuid,
            TaxonRelationshipType oldRelationshipType,
            SynonymRelationshipType synonymRelationshipType) throws DataChangeNoRollbackException {
        UpdateResult result = new UpdateResult();
        Taxon fromTaxon = (Taxon) dao.load(fromTaxonUuid);
        Taxon toTaxon = (Taxon) dao.load(toTaxonUuid);
        Synonym synonym = changeRelatedTaxonToSynonym(fromTaxon, toTaxon, oldRelationshipType, synonymRelationshipType);
        result.setCdmEntity(synonym);
        result.addUpdatedObject(fromTaxon);
        result.addUpdatedObject(toTaxon);
        result.addUpdatedObject(synonym);

        return result;
    }

    @Override
    @Transactional(readOnly = false)
    public Synonym changeRelatedTaxonToSynonym(Taxon fromTaxon, Taxon toTaxon, TaxonRelationshipType oldRelationshipType,
            SynonymRelationshipType synonymRelationshipType) throws DataChangeNoRollbackException {
        // Create new synonym using concept name
                TaxonNameBase<?, ?> synonymName = fromTaxon.getName();
                Synonym synonym = Synonym.NewInstance(synonymName, fromTaxon.getSec());

                // Remove concept relation from taxon
                toTaxon.removeTaxon(fromTaxon, oldRelationshipType);




                // Create a new synonym for the taxon
                SynonymRelationship synonymRelationship;
                if (synonymRelationshipType != null
                        && synonymRelationshipType.equals(SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF())){
                    synonymRelationship = toTaxon.addHomotypicSynonym(synonym, null, null);
                } else{
                    synonymRelationship = toTaxon.addHeterotypicSynonymName(synonymName);
                }

                this.saveOrUpdate(toTaxon);
                //TODO: configurator and classification
                TaxonDeletionConfigurator config = new TaxonDeletionConfigurator();
                config.setDeleteNameIfPossible(false);
                this.deleteTaxon(fromTaxon.getUuid(), config, null);
                return synonymRelationship.getSynonym();

    }

    @Override
    public DeleteResult isDeletable(TaxonBase taxonBase, DeleteConfiguratorBase config){
        DeleteResult result = new DeleteResult();
        Set<CdmBase> references = commonService.getReferencingObjectsForDeletion(taxonBase);
        if (taxonBase instanceof Taxon){
            TaxonDeletionConfigurator taxonConfig = (TaxonDeletionConfigurator) config;
            result = isDeletableForTaxon(references, taxonConfig);
        }else{
            SynonymDeletionConfigurator synonymConfig = (SynonymDeletionConfigurator) config;
            result = isDeletableForSynonym(references, synonymConfig);
        }
        return result;
    }

    private DeleteResult isDeletableForSynonym(Set<CdmBase> references, SynonymDeletionConfigurator config){
        String message;
        DeleteResult result = new DeleteResult();
        for (CdmBase ref: references){
            if (!(ref instanceof SynonymRelationship || ref instanceof Taxon || ref instanceof TaxonNameBase )){
                message = "The Synonym can't be deleted as long as it is referenced by " + ref.getClass().getSimpleName() + " with id "+ ref.getId();
                result.addException(new ReferencedObjectUndeletableException(message));
                result.addRelatedObject(ref);
                result.setAbort();
            }
        }

        return result;
    }

    private DeleteResult isDeletableForTaxon(Set<CdmBase> references, TaxonDeletionConfigurator config){
        String message = null;
        DeleteResult result = new DeleteResult();
        for (CdmBase ref: references){
            if (!(ref instanceof TaxonNameBase)){
            	message = null;
                if (!config.isDeleteSynonymRelations() && (ref instanceof SynonymRelationship)){
                    message = "The Taxon can't be deleted as long as it has synonyms.";

                }
                if (!config.isDeleteDescriptions() && (ref instanceof DescriptionBase)){
                    message = "The Taxon can't be deleted as long as it has factual data.";

                }

                if (!config.isDeleteTaxonNodes() && (ref instanceof TaxonNode)){
                    message = "The Taxon can't be deleted as long as it belongs to a taxon node.";

                }
                if (!config.isDeleteTaxonRelationships() && (ref instanceof TaxonNode)){
                    if (!config.isDeleteMisappliedNamesAndInvalidDesignations() && (((TaxonRelationship)ref).getType().equals(TaxonRelationshipType.MISAPPLIED_NAME_FOR())|| ((TaxonRelationship)ref).getType().equals(TaxonRelationshipType.INVALID_DESIGNATION_FOR()))){
                        message = "The Taxon can't be deleted as long as it has misapplied names or invalid designations.";

                    } else{
                        message = "The Taxon can't be deleted as long as it belongs to a taxon node.";

                    }
                }
                if (ref instanceof PolytomousKeyNode){
                    message = "The Taxon can't be deleted as long as it is referenced by a polytomous key node.";

                }

                if (HibernateProxyHelper.isInstanceOf(ref, IIdentificationKey.class)){
                   message = "Taxon can't be deleted as it is used in an identification key. Remove from identification key prior to deleting this name";


                }


               /* //PolytomousKeyNode
                if (referencingObject.isInstanceOf(PolytomousKeyNode.class)){
                    String message = "Taxon" + taxon.getTitleCache() + " can't be deleted as it is used in polytomous key node";
                    return message;
                }*/

                //TaxonInteraction
                if (ref.isInstanceOf(TaxonInteraction.class)){
                    message = "Taxon can't be deleted as it is used in taxonInteraction#taxon2";

                }

              //TaxonInteraction
                if (ref.isInstanceOf(DeterminationEvent.class)){
                    message = "Taxon can't be deleted as it is used in a determination event";

                }

            }
            if (message != null){
	            result.addException(new ReferencedObjectUndeletableException(message));
	            result.addRelatedObject(ref);
	            result.setAbort();
            }
        }

        return result;
    }

    @Override
    public IncludedTaxaDTO listIncludedTaxa(UUID taxonUuid, IncludedTaxonConfiguration config) {
        IncludedTaxaDTO result = new IncludedTaxaDTO(taxonUuid);

        //preliminary implementation

        Set<Taxon> taxa = new HashSet<Taxon>();
        TaxonBase taxonBase = find(taxonUuid);
        if (taxonBase == null){
            return new IncludedTaxaDTO();
        }else if (taxonBase.isInstanceOf(Taxon.class)){
            Taxon taxon = CdmBase.deproxy(taxonBase, Taxon.class);
            taxa.add(taxon);
        }else if (taxonBase.isInstanceOf(Synonym.class)){
            //TODO partial synonyms ??
            //TODO synonyms in general
            Synonym syn = CdmBase.deproxy(taxonBase, Synonym.class);
            taxa.addAll(syn.getAcceptedTaxa());
        }else{
            throw new IllegalArgumentException("Unhandled class " + taxonBase.getClass().getSimpleName());
        }

        Set<Taxon> related = makeRelatedIncluded(taxa, result, config);
        int i = 0;
        while((! related.isEmpty()) && i++ < 100){  //to avoid
             related = makeRelatedIncluded(related, result, config);
        }

        return result;
    }

    /**
     * Computes all children and conceptually congruent and included taxa and adds them to the existingTaxa
     * data structure.
     * @return the set of conceptually related taxa for further use
     */
    /**
     * @param uncheckedTaxa
     * @param existingTaxa
     * @param config
     * @return
     */
    private Set<Taxon> makeRelatedIncluded(Set<Taxon> uncheckedTaxa, IncludedTaxaDTO existingTaxa, IncludedTaxonConfiguration config) {

        //children
        Set<TaxonNode> taxonNodes = new HashSet<TaxonNode>();
        for (Taxon taxon: uncheckedTaxa){
            taxonNodes.addAll(taxon.getTaxonNodes());
        }

        Set<Taxon> children = new HashSet<Taxon>();
        if (! config.onlyCongruent){
            for (TaxonNode node: taxonNodes){
                List<TaxonNode> childNodes = nodeService.loadChildNodesOfTaxonNode(node, null, true, null);
                for (TaxonNode child : childNodes){
                    children.add(child.getTaxon());
                }
            }
            children.remove(null);  // just to be on the save side
        }

        Iterator<Taxon> it = children.iterator();
        while(it.hasNext()){
            UUID uuid = it.next().getUuid();
            if (existingTaxa.contains(uuid)){
                it.remove();
            }else{
                existingTaxa.addIncludedTaxon(uuid, new ArrayList<UUID>(), false);
            }
        }

        //concept relations
        Set<Taxon> uncheckedAndChildren = new HashSet<Taxon>(uncheckedTaxa);
        uncheckedAndChildren.addAll(children);

        Set<Taxon> relatedTaxa = makeConceptIncludedTaxa(uncheckedAndChildren, existingTaxa, config);


        Set<Taxon> result = new HashSet<Taxon>(relatedTaxa);
        return result;
    }

    /**
     * Computes all conceptually congruent or included taxa and adds them to the existingTaxa data structure.
     * @return the set of these computed taxa
     */
    private Set<Taxon> makeConceptIncludedTaxa(Set<Taxon> unchecked, IncludedTaxaDTO existingTaxa, IncludedTaxonConfiguration config) {
        Set<Taxon> result = new HashSet<Taxon>();

        for (Taxon taxon : unchecked){
            Set<TaxonRelationship> fromRelations = taxon.getRelationsFromThisTaxon();
            Set<TaxonRelationship> toRelations = taxon.getRelationsToThisTaxon();

            for (TaxonRelationship fromRel : fromRelations){
                if (config.includeDoubtful == false && fromRel.isDoubtful()){
                    continue;
                }
                if (fromRel.getType().equals(TaxonRelationshipType.CONGRUENT_TO()) ||
                        !config.onlyCongruent && fromRel.getType().equals(TaxonRelationshipType.INCLUDES()) ||
                        !config.onlyCongruent && fromRel.getType().equals(TaxonRelationshipType.CONGRUENT_OR_INCLUDES())
                        ){
                    result.add(fromRel.getToTaxon());
                }
            }

            for (TaxonRelationship toRel : toRelations){
                if (config.includeDoubtful == false && toRel.isDoubtful()){
                    continue;
                }
                if (toRel.getType().equals(TaxonRelationshipType.CONGRUENT_TO())){
                    result.add(toRel.getFromTaxon());
                }
            }
        }

        Iterator<Taxon> it = result.iterator();
        while(it.hasNext()){
            UUID uuid = it.next().getUuid();
            if (existingTaxa.contains(uuid)){
                it.remove();
            }else{
                existingTaxa.addIncludedTaxon(uuid, new ArrayList<UUID>(), false);
            }
        }
        return result;
    }

    @Override
    public List<TaxonBase> findTaxaByName(MatchingTaxonConfigurator config){
        List<TaxonBase> taxonList = dao.getTaxaByName(true, false, false, config.getTaxonNameTitle(), null, MatchMode.EXACT, null, 0, 0, config.getPropertyPath());
        return taxonList;
    }

	@Override
	@Transactional(readOnly = true)
	public <S extends TaxonBase> Pager<FindByIdentifierDTO<S>> findByIdentifier(
			Class<S> clazz, String identifier, DefinedTerm identifierType, TaxonNode subtreeFilter,
			MatchMode matchmode, boolean includeEntity, Integer pageSize,
			Integer pageNumber,	List<String> propertyPaths) {
		if (subtreeFilter == null){
			return findByIdentifier(clazz, identifier, identifierType, matchmode, includeEntity, pageSize, pageNumber, propertyPaths);
		}

		Integer numberOfResults = dao.countByIdentifier(clazz, identifier, identifierType, subtreeFilter, matchmode);
        List<Object[]> daoResults = new ArrayList<Object[]>();
        if(numberOfResults > 0) { // no point checking again
        	daoResults = dao.findByIdentifier(clazz, identifier, identifierType, subtreeFilter,
    				matchmode, includeEntity, pageSize, pageNumber, propertyPaths);
        }

        List<FindByIdentifierDTO<S>> result = new ArrayList<FindByIdentifierDTO<S>>();
        for (Object[] daoObj : daoResults){
        	if (includeEntity){
        		result.add(new FindByIdentifierDTO<S>((DefinedTerm)daoObj[0], (String)daoObj[1], (S)daoObj[2]));
        	}else{
        		result.add(new FindByIdentifierDTO<S>((DefinedTerm)daoObj[0], (String)daoObj[1], (UUID)daoObj[2], (String)daoObj[3]));
        	}
        }
		return new DefaultPagerImpl<FindByIdentifierDTO<S>>(pageNumber, numberOfResults, pageSize, result);
	}

	@Override
	@Transactional(readOnly = false)
	public UpdateResult moveSynonymToAnotherTaxon(SynonymRelationship oldSynonymRelation, UUID newTaxonUUID, boolean moveHomotypicGroup,
            SynonymRelationshipType newSynonymRelationshipType, Reference reference, String referenceDetail, boolean keepReference) throws HomotypicalGroupChangeException {

	    UpdateResult result = new UpdateResult();
		Taxon newTaxon = (Taxon) dao.load(newTaxonUUID);
		SynonymRelationship sr = moveSynonymToAnotherTaxon(oldSynonymRelation, newTaxon, moveHomotypicGroup, newSynonymRelationshipType, reference, referenceDetail, keepReference);
		result.setCdmEntity(sr);
		result.addUpdatedObject(sr);
		result.addUpdatedObject(newTaxon);
		return result;
	}

	@Override
	public UpdateResult moveFactualDateToAnotherTaxon(UUID fromTaxonUuid, UUID toTaxonUuid){
		UpdateResult result = new UpdateResult();

		Taxon fromTaxon = (Taxon)dao.load(fromTaxonUuid);
		Taxon toTaxon = (Taxon) dao.load(toTaxonUuid);
		  for(TaxonDescription description : fromTaxon.getDescriptions()){
              //reload to avoid session conflicts
              description = HibernateProxyHelper.deproxy(description, TaxonDescription.class);

              String moveMessage = String.format("Description moved from %s", fromTaxon);
              if(description.isProtectedTitleCache()){
                  String separator = "";
                  if(!StringUtils.isBlank(description.getTitleCache())){
                      separator = " - ";
                  }
                  description.setTitleCache(description.getTitleCache() + separator + moveMessage, true);
              }
              Annotation annotation = Annotation.NewInstance(moveMessage, Language.getDefaultLanguage());
              annotation.setAnnotationType(AnnotationType.TECHNICAL());
              description.addAnnotation(annotation);
              toTaxon.addDescription(description);
              dao.saveOrUpdate(toTaxon);
              dao.saveOrUpdate(fromTaxon);
              result.addUpdatedObject(toTaxon);
              result.addUpdatedObject(fromTaxon);

          }


		return result;
	}

	@Override
	public DeleteResult deleteSynonym(UUID synonymUuid, UUID taxonUuid,
			SynonymDeletionConfigurator config) {
		TaxonBase base = this.load(synonymUuid);
		Synonym syn = HibernateProxyHelper.deproxy(base, Synonym.class);
		base = this.load(taxonUuid);
		Taxon taxon = HibernateProxyHelper.deproxy(base, Taxon.class);

		return this.deleteSynonym(syn, taxon, config);
	}

	@Override
	@Transactional(readOnly = false)
	public UpdateResult swapSynonymAndAcceptedTaxon(UUID synonymUUid,
			UUID acceptedTaxonUuid) {
		TaxonBase base = this.load(synonymUUid);
		Synonym syn = HibernateProxyHelper.deproxy(base, Synonym.class);
		base = this.load(acceptedTaxonUuid);
		Taxon taxon = HibernateProxyHelper.deproxy(base, Taxon.class);

		return this.swapSynonymAndAcceptedTaxon(syn, taxon);
	}



}
