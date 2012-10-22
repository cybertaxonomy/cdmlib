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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.grouping.TopGroups;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.config.IFindTaxaAndNamesConfigurator;
import eu.etaxonomy.cdm.api.service.config.MatchingTaxonConfigurator;
import eu.etaxonomy.cdm.api.service.config.NameDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.config.TaxonDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.exception.DataChangeNoRollbackException;
import eu.etaxonomy.cdm.api.service.exception.HomotypicalGroupChangeException;
import eu.etaxonomy.cdm.api.service.exception.ReferencedObjectUndeletableException;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.api.service.search.ISearchResultBuilder;
import eu.etaxonomy.cdm.api.service.search.LuceneMultiSearch;
import eu.etaxonomy.cdm.api.service.search.LuceneSearch;
import eu.etaxonomy.cdm.api.service.search.LuceneSearch.TopGroupsWithMaxScore;
import eu.etaxonomy.cdm.api.service.search.QueryFactory;
import eu.etaxonomy.cdm.api.service.search.SearchResult;
import eu.etaxonomy.cdm.api.service.search.SearchResultBuilder;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.hibernate.search.DefinedTermBaseClassBridge;
import eu.etaxonomy.cdm.hibernate.search.MultilanguageTextFieldBridge;
import eu.etaxonomy.cdm.model.CdmBaseType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.RelationshipBase.Direction;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IIdentificationKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonInteraction;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaUtils;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
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
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
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
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class TaxonServiceImpl extends IdentifiableServiceBase<TaxonBase,ITaxonDao> implements ITaxonService{
    private static final Logger logger = Logger.getLogger(TaxonServiceImpl.class);

    public static final String POTENTIAL_COMBINATION_NAMESPACE = "Potential combination";

    public static final String INFERRED_EPITHET_NAMESPACE = "Inferred epithet";

    public static final String INFERRED_GENUS_NAMESPACE = "Inferred genus";


    @Autowired
    private ITaxonNameDao nameDao;

    @Autowired
    private INameService nameService;

    @Autowired
    private ICdmGenericDao genericDao;

    @Autowired
    private IDescriptionService descriptionService;

    @Autowired
    private IOrderedTermVocabularyDao orderedVocabularyDao;

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
    public List<TaxonBase> searchTaxaByName(String name, Reference sec) {
        return dao.getTaxaByName(name, sec);
    }

    /**
     * FIXME Candidate for harmonization
     * list(Synonym.class, ...)
     *  (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#getAllSynonyms(int, int)
     */
    public List<Synonym> getAllSynonyms(int limit, int start) {
        return dao.getAllSynonyms(limit, start);
    }

    /**
     * FIXME Candidate for harmonization
     * list(Taxon.class, ...)
     *  (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#getAllTaxa(int, int)
     */
    public List<Taxon> getAllTaxa(int limit, int start) {
        return dao.getAllTaxa(limit, start);
    }

    /**
     * FIXME Candidate for harmonization
     * merge with getRootTaxa(Reference sec, ..., ...)
     *  (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#getRootTaxa(eu.etaxonomy.cdm.model.reference.Reference, boolean)
     */
    public List<Taxon> getRootTaxa(Reference sec, CdmFetch cdmFetch, boolean onlyWithChildren) {
        if (cdmFetch == null){
            cdmFetch = CdmFetch.NO_FETCH();
        }
        return dao.getRootTaxa(sec, cdmFetch, onlyWithChildren, false);
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#getRootTaxa(eu.etaxonomy.cdm.model.name.Rank, eu.etaxonomy.cdm.model.reference.Reference, boolean, boolean)
     */
    public List<Taxon> getRootTaxa(Rank rank, Reference sec, boolean onlyWithChildren,boolean withMisapplications, List<String> propertyPaths) {
        return dao.getRootTaxa(rank, sec, null, onlyWithChildren, withMisapplications, propertyPaths);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#getAllRelationships(int, int)
     */
    public List<RelationshipBase> getAllRelationships(int limit, int start){
        return dao.getAllRelationships(limit, start);
    }

    /**
     * FIXME Candidate for harmonization
     * is this the same as termService.getVocabulary(VocabularyEnum.TaxonRelationshipType) ?
     */
    @Deprecated
    public OrderedTermVocabulary<TaxonRelationshipType> getTaxonRelationshipTypeVocabulary() {

        String taxonRelTypeVocabularyId = "15db0cf7-7afc-4a86-a7d4-221c73b0c9ac";
        UUID uuid = UUID.fromString(taxonRelTypeVocabularyId);
        OrderedTermVocabulary<TaxonRelationshipType> taxonRelTypeVocabulary =
            (OrderedTermVocabulary)orderedVocabularyDao.findByUuid(uuid);
        return taxonRelTypeVocabulary;
    }



    /*
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#swapSynonymWithAcceptedTaxon(eu.etaxonomy.cdm.model.taxon.Synonym)
     */
    @Transactional(readOnly = false)
    public void swapSynonymAndAcceptedTaxon(Synonym synonym, Taxon acceptedTaxon){

        TaxonNameBase<?,?> synonymName = synonym.getName();
        synonymName.removeTaxonBase(synonym);
        TaxonNameBase<?,?> taxonName = acceptedTaxon.getName();
        taxonName.removeTaxonBase(acceptedTaxon);

        synonym.setName(taxonName);
        acceptedTaxon.setName(synonymName);

        // the accepted taxon needs a new uuid because the concept has changed
        // FIXME this leads to an error "HibernateException: immutable natural identifier of an instance of eu.etaxonomy.cdm.model.taxon.Taxon was altered"
        //acceptedTaxon.setUuid(UUID.randomUUID());
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#changeSynonymToAcceptedTaxon(eu.etaxonomy.cdm.model.taxon.Synonym, eu.etaxonomy.cdm.model.taxon.Taxon)
     */
    //TODO correct delete handling still needs to be implemented / checked
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

        SynonymRelationshipType relTypeForGroup = SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF();
        List<Synonym> heteroSynonyms = acceptedTaxon.getSynonymsInGroup(synonymHomotypicGroup);

        for (Synonym heteroSynonym : heteroSynonyms){
            if (synonym.equals(heteroSynonym)){
                acceptedTaxon.removeSynonym(heteroSynonym, false);
            }else{
                //move synonyms in same homotypic group to new accepted taxon
                heteroSynonym.replaceAcceptedTaxon(newAcceptedTaxon, relTypeForGroup, copyCitationInfo, citation, microCitation);
            }
        }

        //synonym.getName().removeTaxonBase(synonym);
        //TODO correct delete handling still needs to be implemented / checked
        if (deleteSynonym){
//			deleteSynonym(synonym, taxon, false);
            try {
                this.dao.flush();
                this.delete(synonym);

            } catch (Exception e) {
                logger.info("Can't delete old synonym from database");
            }
        }

        return newAcceptedTaxon;
    }


    public Taxon changeSynonymToRelatedTaxon(Synonym synonym, Taxon toTaxon, TaxonRelationshipType taxonRelationshipType, Reference citation, String microcitation){

        // Get name from synonym
        TaxonNameBase<?, ?> synonymName = synonym.getName();

        // remove synonym from taxon
        toTaxon.removeSynonym(synonym);

        // Create a taxon with synonym name
        Taxon fromTaxon = Taxon.NewInstance(synonymName, null);

        // Add taxon relation
        fromTaxon.addTaxonRelation(toTaxon, taxonRelationshipType, citation, microcitation);

        // since we are swapping names, we have to detach the name from the synonym completely.
        // Otherwise the synonym will still be in the list of typified names.
        synonym.getName().removeTaxonBase(synonym);

        return fromTaxon;
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#changeHomotypicalGroupOfSynonym(eu.etaxonomy.cdm.model.taxon.Synonym, eu.etaxonomy.cdm.model.name.HomotypicalGroup, eu.etaxonomy.cdm.model.taxon.Taxon, boolean, boolean)
     */
    @Transactional(readOnly = false)
    @Override
    public void changeHomotypicalGroupOfSynonym(Synonym synonym, HomotypicalGroup newHomotypicalGroup, Taxon targetTaxon,
                        boolean removeFromOtherTaxa, boolean setBasionymRelationIfApplicable){
        // Get synonym name
        TaxonNameBase synonymName = synonym.getName();
        HomotypicalGroup oldHomotypicalGroup = synonymName.getHomotypicalGroup();


        // Switch groups
        oldHomotypicalGroup.removeTypifiedName(synonymName);
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


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IIdentifiableEntityService#updateTitleCache(java.lang.Integer, eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy)
     */
    @Override
    @Transactional(readOnly = false)
    public void updateTitleCache(Class<? extends TaxonBase> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<TaxonBase> cacheStrategy, IProgressMonitor monitor) {
        if (clazz == null){
            clazz = TaxonBase.class;
        }
        super.updateTitleCacheImpl(clazz, stepSize, cacheStrategy, monitor);
    }

    @Autowired
    protected void setDao(ITaxonDao dao) {
        this.dao = dao;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#findTaxaByName(java.lang.Class, java.lang.String, java.lang.String, java.lang.String, java.lang.String, eu.etaxonomy.cdm.model.name.Rank, java.lang.Integer, java.lang.Integer)
     */
    public Pager<TaxonBase> findTaxaByName(Class<? extends TaxonBase> clazz, String uninomial,	String infragenericEpithet, String specificEpithet,	String infraspecificEpithet, Rank rank, Integer pageSize,Integer pageNumber) {
        Integer numberOfResults = dao.countTaxaByName(clazz, uninomial, infragenericEpithet, specificEpithet, infraspecificEpithet, rank);

        List<TaxonBase> results = new ArrayList<TaxonBase>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.findTaxaByName(clazz, uninomial, infragenericEpithet, specificEpithet, infraspecificEpithet, rank, pageSize, pageNumber);
        }

        return new DefaultPagerImpl<TaxonBase>(pageNumber, numberOfResults, pageSize, results);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#listTaxaByName(java.lang.Class, java.lang.String, java.lang.String, java.lang.String, java.lang.String, eu.etaxonomy.cdm.model.name.Rank, java.lang.Integer, java.lang.Integer)
     */
    public List<TaxonBase> listTaxaByName(Class<? extends TaxonBase> clazz, String uninomial,	String infragenericEpithet, String specificEpithet,	String infraspecificEpithet, Rank rank, Integer pageSize,Integer pageNumber) {
        Integer numberOfResults = dao.countTaxaByName(clazz, uninomial, infragenericEpithet, specificEpithet, infraspecificEpithet, rank);

        List<TaxonBase> results = new ArrayList<TaxonBase>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.findTaxaByName(clazz, uninomial, infragenericEpithet, specificEpithet, infraspecificEpithet, rank, pageSize, pageNumber);
        }

        return results;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#listToTaxonRelationships(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    public List<TaxonRelationship> listToTaxonRelationships(Taxon taxon, TaxonRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths){
        Integer numberOfResults = dao.countTaxonRelationships(taxon, type, TaxonRelationship.Direction.relatedTo);

        List<TaxonRelationship> results = new ArrayList<TaxonRelationship>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.getTaxonRelationships(taxon, type, pageSize, pageNumber, orderHints, propertyPaths, TaxonRelationship.Direction.relatedTo);
        }
        return results;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#pageToTaxonRelationships(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    public Pager<TaxonRelationship> pageToTaxonRelationships(Taxon taxon, TaxonRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.countTaxonRelationships(taxon, type, TaxonRelationship.Direction.relatedTo);

        List<TaxonRelationship> results = new ArrayList<TaxonRelationship>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.getTaxonRelationships(taxon, type, pageSize, pageNumber, orderHints, propertyPaths, TaxonRelationship.Direction.relatedTo);
        }
        return new DefaultPagerImpl<TaxonRelationship>(pageNumber, numberOfResults, pageSize, results);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#listFromTaxonRelationships(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    public List<TaxonRelationship> listFromTaxonRelationships(Taxon taxon, TaxonRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths){
        Integer numberOfResults = dao.countTaxonRelationships(taxon, type, TaxonRelationship.Direction.relatedFrom);

        List<TaxonRelationship> results = new ArrayList<TaxonRelationship>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.getTaxonRelationships(taxon, type, pageSize, pageNumber, orderHints, propertyPaths, TaxonRelationship.Direction.relatedFrom);
        }
        return results;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#pageFromTaxonRelationships(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    public Pager<TaxonRelationship> pageFromTaxonRelationships(Taxon taxon, TaxonRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.countTaxonRelationships(taxon, type, TaxonRelationship.Direction.relatedFrom);

        List<TaxonRelationship> results = new ArrayList<TaxonRelationship>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.getTaxonRelationships(taxon, type, pageSize, pageNumber, orderHints, propertyPaths, TaxonRelationship.Direction.relatedFrom);
        }
        return new DefaultPagerImpl<TaxonRelationship>(pageNumber, numberOfResults, pageSize, results);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#getSynonyms(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    public Pager<SynonymRelationship> getSynonyms(Taxon taxon,	SynonymRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.countSynonyms(taxon, type);

        List<SynonymRelationship> results = new ArrayList<SynonymRelationship>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.getSynonyms(taxon, type, pageSize, pageNumber, orderHints, propertyPaths);
        }

        return new DefaultPagerImpl<SynonymRelationship>(pageNumber, numberOfResults, pageSize, results);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#getSynonyms(eu.etaxonomy.cdm.model.taxon.Synonym, eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    public Pager<SynonymRelationship> getSynonyms(Synonym synonym,	SynonymRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.countSynonyms(synonym, type);

        List<SynonymRelationship> results = new ArrayList<SynonymRelationship>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.getSynonyms(synonym, type, pageSize, pageNumber, orderHints, propertyPaths);
        }

        return new DefaultPagerImpl<SynonymRelationship>(pageNumber, numberOfResults, pageSize, results);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#getHomotypicSynonymsByHomotypicGroup(eu.etaxonomy.cdm.model.taxon.Taxon, java.util.List)
     */
    public List<Synonym> getHomotypicSynonymsByHomotypicGroup(Taxon taxon, List<String> propertyPaths){
        Taxon t = (Taxon)dao.load(taxon.getUuid(), propertyPaths);
        return t.getHomotypicSynonymsByHomotypicGroup();
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#getHeterotypicSynonymyGroups(eu.etaxonomy.cdm.model.taxon.Taxon, java.util.List)
     */
    public List<List<Synonym>> getHeterotypicSynonymyGroups(Taxon taxon, List<String> propertyPaths){
        Taxon t = (Taxon)dao.load(taxon.getUuid(), propertyPaths);
        List<HomotypicalGroup> homotypicalGroups = t.getHeterotypicSynonymyGroups();
        List<List<Synonym>> heterotypicSynonymyGroups = new ArrayList<List<Synonym>>(homotypicalGroups.size());
        for(HomotypicalGroup homotypicalGroup : homotypicalGroups){
            heterotypicSynonymyGroups.add(t.getSynonymsInGroup(homotypicalGroup));
        }
        return heterotypicSynonymyGroups;
    }

    public List<UuidAndTitleCache<TaxonBase>> findTaxaAndNamesForEditor(IFindTaxaAndNamesConfigurator configurator){

        List<UuidAndTitleCache<TaxonBase>> result = new ArrayList<UuidAndTitleCache<TaxonBase>>();
//        Class<? extends TaxonBase> clazz = null;
//        if ((configurator.isDoTaxa() && configurator.isDoSynonyms())) {
//            clazz = TaxonBase.class;
//            //propertyPath.addAll(configurator.getTaxonPropertyPath());
//            //propertyPath.addAll(configurator.getSynonymPropertyPath());
//        } else if(configurator.isDoTaxa()) {
//            clazz = Taxon.class;
//            //propertyPath = configurator.getTaxonPropertyPath();
//        } else if (configurator.isDoSynonyms()) {
//            clazz = Synonym.class;
//            //propertyPath = configurator.getSynonymPropertyPath();
//        }


        result = dao.getTaxaByNameForEditor(configurator.isDoTaxa(), configurator.isDoSynonyms(), configurator.getTitleSearchStringSqlized(), configurator.getClassification(), configurator.getMatchMode(), configurator.getNamedAreas());
        return result;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#findTaxaAndNames(eu.etaxonomy.cdm.api.service.config.ITaxonServiceConfigurator)
     */
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
                List<Object[]> commonNameResults = dao.getTaxaByCommonName(configurator.getTitleSearchStringSqlized(), configurator.getClassification(), configurator.getMatchMode(), configurator.getNamedAreas(), configurator.getPageSize(), configurator.getPageNumber(), configurator.getTaxonPropertyPath());
                for( Object[] entry : commonNameResults ) {
                    taxa.add((TaxonBase) entry[0]);
                }
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

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#getAllMedia(eu.etaxonomy.cdm.model.taxon.Taxon, int, int, int, java.lang.String[])
     */
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

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#listTaxonDescriptionMedia(eu.etaxonomy.cdm.model.taxon.Taxon, boolean)
     */
    public List<Media> listTaxonDescriptionMedia(Taxon taxon, boolean limitToGalleries, List<String> propertyPath){

        Pager<TaxonDescription> p =
                    descriptionService.getTaxonDescriptions(taxon, null, null, null, null, propertyPath);

        // pars the media and quality parameters


        // collect all media of the given taxon
        List<Media> taxonMedia = new ArrayList<Media>();
        List<Media> taxonGalleryMedia = new ArrayList<Media>();
        for(TaxonDescription desc : p.getRecords()){

            if(desc.isImageGallery()){
                for(DescriptionElementBase element : desc.getElements()){
                    for(Media media : element.getMedia()){
                        taxonGalleryMedia.add(media);
                    }
                }
            } else if(!limitToGalleries){
                for(DescriptionElementBase element : desc.getElements()){
                    for(Media media : element.getMedia()){
                        taxonMedia.add(media);
                    }
                }
            }

        }

        taxonGalleryMedia.addAll(taxonMedia);
        return taxonGalleryMedia;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#findTaxaByID(java.util.Set)
     */
    public List<TaxonBase> findTaxaByID(Set<Integer> listOfIDs) {
        return this.dao.findById(listOfIDs);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#findTaxonByUuid(UUID uuid, List<String> propertyPaths)
     */
    public TaxonBase findTaxonByUuid(UUID uuid, List<String> propertyPaths){
        return this.dao.findByUuid(uuid, null ,propertyPaths);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#countAllRelationships()
     */
    public int countAllRelationships() {
        return this.dao.countAllRelationships();
    }




    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#findIdenticalTaxonNames(java.util.List)
     */
    public List<TaxonNameBase> findIdenticalTaxonNames(List<String> propertyPath) {
        return this.dao.findIdenticalTaxonNames(propertyPath);
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#deleteTaxon(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.api.service.config.TaxonDeletionConfigurator)
     */
    @Override
    public void deleteTaxon(Taxon taxon, TaxonDeletionConfigurator config) throws ReferencedObjectUndeletableException {
        if (config == null){
            config = new TaxonDeletionConfigurator();
        }

            //    	TaxonNode
            if (! config.isDeleteTaxonNodes()){
                if (taxon.getTaxonNodes().size() > 0){
                    String message = "Taxon can't be deleted as it is used in a classification node. Remove taxon from all classifications prior to deletion.";
                    throw new ReferencedObjectUndeletableException(message);
                }
            }


            //    	SynonymRelationShip
            if (config.isDeleteSynonymRelations()){
                boolean removeSynonymNameFromHomotypicalGroup = false;
                for (SynonymRelationship synRel : taxon.getSynonymRelations()){
                    Synonym synonym = synRel.getSynonym();
                    taxon.removeSynonymRelation(synRel, removeSynonymNameFromHomotypicalGroup);
                    if (config.isDeleteSynonymsIfPossible()){
                        //TODO which value
                        boolean newHomotypicGroupIfNeeded = true;
                        deleteSynonym(synonym, taxon, config.isDeleteNameIfPossible(), newHomotypicGroupIfNeeded);
                    }else{
                        deleteSynonymRelationships(synonym, taxon);
                    }
                }
            }

            //    	TaxonRelationship
            if (! config.isDeleteTaxonRelationships()){
                if (taxon.getTaxonRelations().size() > 0){
                    String message = "Taxon can't be deleted as it is related to another taxon. Remove taxon from all relations to other taxa prior to deletion.";
                    throw new ReferencedObjectUndeletableException(message);
                }
            }


            //    	TaxonDescription
                    Set<TaxonDescription> descriptions = taxon.getDescriptions();

                    for (TaxonDescription desc: descriptions){
                        if (config.isDeleteDescriptions()){
                            //TODO use description delete configurator ?
                            //FIXME check if description is ALWAYS deletable
                            descriptionService.delete(desc);
                        }else{
                            if (desc.getDescribedSpecimenOrObservations().size()>0){
                                String message = "Taxon can't be deleted as it is used in a TaxonDescription" +
                                        " which also describes specimens or abservations";
                                    throw new ReferencedObjectUndeletableException(message);
                                }
                            }
                        }


                //check references with only reverse mapping
            Set<CdmBase> referencingObjects = genericDao.getReferencingObjects(taxon);
            for (CdmBase referencingObject : referencingObjects){
                //IIdentificationKeys (Media, Polytomous, MultiAccess)
                if (HibernateProxyHelper.isInstanceOf(referencingObject, IIdentificationKey.class)){
                    String message = "Taxon can't be deleted as it is used in an identification key. Remove from identification key prior to deleting this name";
                    message = String.format(message, CdmBase.deproxy(referencingObject, DerivedUnitBase.class).getTitleCache());
                    throw new ReferencedObjectUndeletableException(message);
                }


                //PolytomousKeyNode
                if (referencingObject.isInstanceOf(PolytomousKeyNode.class)){
                    String message = "Taxon can't be deleted as it is used in polytomous key node";
                    throw new ReferencedObjectUndeletableException(message);
                }

                //TaxonInteraction
                if (referencingObject.isInstanceOf(TaxonInteraction.class)){
                    String message = "Taxon can't be deleted as it is used in taxonInteraction#taxon2";
                    throw new ReferencedObjectUndeletableException(message);
                }
            }


            //TaxonNameBase
            if (config.isDeleteNameIfPossible()){
                try {
                    nameService.delete(taxon.getName(), config.getNameDeletionConfig());
                } catch (ReferencedObjectUndeletableException e) {
                    //do nothing
                    if (logger.isDebugEnabled()){logger.debug("Name could not be deleted");}
                }
            }

    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#deleteSynonym(eu.etaxonomy.cdm.model.taxon.Synonym, eu.etaxonomy.cdm.model.taxon.Taxon, boolean, boolean)
     */
    @Transactional(readOnly = false)
    @Override
    public void deleteSynonym(Synonym synonym, Taxon taxon, boolean removeNameIfPossible,boolean newHomotypicGroupIfNeeded) {
        if (synonym == null){
            return;
        }
        synonym = CdmBase.deproxy(dao.merge(synonym), Synonym.class);

        //remove synonymRelationship
        Set<Taxon> taxonSet = new HashSet<Taxon>();
        if (taxon != null){
            taxonSet.add(taxon);
        }else{
            taxonSet.addAll(synonym.getAcceptedTaxa());
        }
        for (Taxon relatedTaxon : taxonSet){
//			dao.deleteSynonymRelationships(synonym, relatedTaxon);
            relatedTaxon.removeSynonym(synonym, newHomotypicGroupIfNeeded);
        }
        this.saveOrUpdate(synonym);

        //TODO remove name from homotypical group?

        //remove synonym (if necessary)
        if (synonym.getSynonymRelations().isEmpty()){
            TaxonNameBase<?,?> name = synonym.getName();
            synonym.setName(null);
            dao.delete(synonym);

            //remove name if possible (and required)
            if (name != null && removeNameIfPossible){
                try{
                    nameService.delete(name, new NameDeletionConfigurator());
                }catch (DataChangeNoRollbackException ex){
                    if (logger.isDebugEnabled())logger.debug("Name wasn't deleted as it is referenced");
                }
            }
        }
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#findIdenticalTaxonNameIds(java.util.List)
     */
    public List<TaxonNameBase> findIdenticalTaxonNameIds(List<String> propertyPath) {

        return this.dao.findIdenticalNamesNew(propertyPath);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#getPhylumName(eu.etaxonomy.cdm.model.name.TaxonNameBase)
     */
    public String getPhylumName(TaxonNameBase name){
        return this.dao.getPhylumName(name);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#deleteSynonymRelationships(eu.etaxonomy.cdm.model.taxon.Synonym, eu.etaxonomy.cdm.model.taxon.Taxon)
     */
    public long deleteSynonymRelationships(Synonym syn, Taxon taxon) {
        return dao.deleteSynonymRelationships(syn, taxon);
    }

/* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#deleteSynonymRelationships(eu.etaxonomy.cdm.model.taxon.Synonym)
     */
    public long deleteSynonymRelationships(Synonym syn) {
        return dao.deleteSynonymRelationships(syn, null);
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#listSynonymRelationships(eu.etaxonomy.cdm.model.taxon.TaxonBase, eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List, eu.etaxonomy.cdm.model.common.RelationshipBase.Direction)
     */
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

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#findBestMatchingTaxon(java.lang.String)
     */
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

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#findBestMatchingSynonym(java.lang.String)
     */
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


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#moveSynonymToAnotherTaxon(eu.etaxonomy.cdm.model.taxon.SynonymRelationship, eu.etaxonomy.cdm.model.taxon.Taxon, boolean, eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType, eu.etaxonomy.cdm.model.reference.Reference, java.lang.String, boolean)
     */
    @Override
    public SynonymRelationship moveSynonymToAnotherTaxon(SynonymRelationship oldSynonymRelation, Taxon newTaxon, boolean moveHomotypicGroup,
            SynonymRelationshipType newSynonymRelationshipType, Reference reference, String referenceDetail, boolean keepReference) throws HomotypicalGroupChangeException {

        Synonym synonym = oldSynonymRelation.getSynonym();
        Taxon fromTaxon = oldSynonymRelation.getAcceptedTaxon();
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
        saveOrUpdate(newTaxon);
        //Assert that there is a result
        if (result == null){
            String message = "Old synonym relation could not be transformed into new relation. This should not happen.";
            throw new IllegalStateException(message);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#getUuidAndTitleCacheTaxon()
     */
    @Override
    public List<UuidAndTitleCache<TaxonBase>> getUuidAndTitleCacheTaxon() {
        return dao.getUuidAndTitleCacheTaxon();
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#getUuidAndTitleCacheSynonym()
     */
    @Override
    public List<UuidAndTitleCache<TaxonBase>> getUuidAndTitleCacheSynonym() {
        return dao.getUuidAndTitleCacheSynonym();
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#findByFullText(java.lang.Class, java.lang.String, eu.etaxonomy.cdm.model.taxon.Classification, java.util.List, boolean, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    @Override
    public Pager<SearchResult<TaxonBase>> findByFullText(
            Class<? extends TaxonBase> clazz, String queryString,
            Classification classification, List<Language> languages,
            boolean highlightFragments, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) throws CorruptIndexException, IOException, ParseException {


        LuceneSearch luceneSearch = prepareFindByFullTextSearch(clazz, queryString, classification, languages, highlightFragments);

        // --- execute search
        TopGroupsWithMaxScore topDocsResultSet = luceneSearch.executeSearch(pageSize, pageNumber);

        Map<CdmBaseType, String> idFieldMap = new HashMap<CdmBaseType, String>();
        idFieldMap.put(CdmBaseType.TAXON, "id");

        // ---  initialize taxa, thighlight matches ....
        ISearchResultBuilder searchResultBuilder = new SearchResultBuilder(luceneSearch, luceneSearch.getQuery());
        List<SearchResult<TaxonBase>> searchResults = searchResultBuilder.createResultSet(
                topDocsResultSet, luceneSearch.getHighlightFields(), dao, idFieldMap, propertyPaths);

        int totalHits = topDocsResultSet != null ? topDocsResultSet.topGroups.totalGroupedHitCount : 0;
        return new DefaultPagerImpl<SearchResult<TaxonBase>>(pageNumber, totalHits, pageSize, searchResults);
    }

    /**
     * @param clazz
     * @param queryString
     * @param classification
     * @param languages
     * @param highlightFragments
     * @param directorySelectClass
     * @return
     */
    protected LuceneSearch prepareFindByFullTextSearch(Class<? extends CdmBase> clazz, String queryString, Classification classification, List<Language> languages,
            boolean highlightFragments) {
        BooleanQuery finalQuery = new BooleanQuery();
        BooleanQuery textQuery = new BooleanQuery();

        LuceneSearch luceneSearch = new LuceneSearch(getSession(), TaxonBase.class);
        QueryFactory queryFactory = new QueryFactory(luceneSearch);

        SortField[] sortFields = new  SortField[]{SortField.FIELD_SCORE, new SortField("titleCache__sort", false)};
        luceneSearch.setSortFields(sortFields);

        // ---- search criteria
        luceneSearch.setClazz(clazz);

        textQuery.add(queryFactory.newTermQuery("titleCache", queryString), Occur.SHOULD);
        textQuery.add(queryFactory.newDefinedTermQuery("name.rank", queryString, languages), Occur.SHOULD);

        finalQuery.add(textQuery, Occur.MUST);

        if(classification != null){
            finalQuery.add(queryFactory.newEntityIdQuery("taxonNodes.classification.id", classification), Occur.MUST);
        }
        luceneSearch.setQuery(finalQuery);

        if(highlightFragments){
            luceneSearch.setHighlightFields(queryFactory.getTextFieldNamesAsArray());
        }
        return luceneSearch;
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#findByDescriptionElementFullText(java.lang.Class, java.lang.String, eu.etaxonomy.cdm.model.taxon.Classification, java.util.List, java.util.List, boolean, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
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


    public Pager<SearchResult<TaxonBase>> findByEverythingFullText(String queryString,
            Classification classification, List<Language> languages, boolean highlightFragments,
            Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) throws CorruptIndexException, IOException, ParseException {

        LuceneSearch luceneSearchByDescriptionElement = prepareByDescriptionElementFullTextSearch(null, queryString, classification, null, languages, highlightFragments);
        LuceneSearch luceneSearchByTaxonBase = prepareFindByFullTextSearch(null, queryString, classification, languages, highlightFragments);

        LuceneMultiSearch multiSearch = new LuceneMultiSearch(luceneSearchByDescriptionElement, luceneSearchByTaxonBase);

        // --- execute search
        TopGroupsWithMaxScore topDocsResultSet = multiSearch.executeSearch(pageSize, pageNumber);

        // --- initialize taxa, highlight matches ....
        ISearchResultBuilder searchResultBuilder = new SearchResultBuilder(multiSearch, multiSearch.getQuery());

        Map<CdmBaseType, String> idFieldMap = new HashMap<CdmBaseType, String>();
        idFieldMap.put(CdmBaseType.TAXON, "id");
        idFieldMap.put(CdmBaseType.DESCRIPTION_ELEMENT, "inDescription.taxon.id");

        List<SearchResult<TaxonBase>> searchResults = searchResultBuilder.createResultSet(
                topDocsResultSet, multiSearch.getHighlightFields(), dao, idFieldMap, propertyPaths);

        int totalHits = topDocsResultSet != null ? topDocsResultSet.topGroups.totalGroupedHitCount : 0;
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
    protected LuceneSearch prepareByDescriptionElementFullTextSearch(Class<? extends CdmBase> clazz, String queryString, Classification classification, List<Feature> features,
            List<Language> languages, boolean highlightFragments) {
        BooleanQuery finalQuery = new BooleanQuery();
        BooleanQuery textQuery = new BooleanQuery();

        LuceneSearch luceneSearch = new LuceneSearch(getSession(), DescriptionElementBase.class);
        QueryFactory queryFactory = new QueryFactory(luceneSearch);

        SortField[] sortFields = new  SortField[]{SortField.FIELD_SCORE, new SortField("inDescription.taxon.titleCache__sort", false)};
        luceneSearch.setSortFields(sortFields);

        // ---- search criteria
        luceneSearch.setClazz(clazz);
        textQuery.add(queryFactory.newTermQuery("titleCache", queryString), Occur.SHOULD);

        // common name
        Query nameQuery;
        if(languages == null || languages.size() == 0){
            nameQuery = queryFactory.newTermQuery("name", queryString);
        } else {
            nameQuery = new BooleanQuery();
            BooleanQuery languageSubQuery = new BooleanQuery();
            for(Language lang : languages){
                languageSubQuery.add(queryFactory.newTermQuery("language.uuid",  lang.getUuid().toString()), Occur.SHOULD);
            }
            ((BooleanQuery) nameQuery).add(queryFactory.newTermQuery("name", queryString), Occur.MUST);
            ((BooleanQuery) nameQuery).add(languageSubQuery, Occur.MUST);
        }
        textQuery.add(nameQuery, Occur.SHOULD);


        // text field from TextData
        textQuery.add(queryFactory.newMultilanguageTextQuery("text", queryString, languages), Occur.SHOULD);

        // --- TermBase fields - by representation ----
        // state field from CategoricalData
        textQuery.add(queryFactory.newDefinedTermQuery("states.state", queryString, languages), Occur.SHOULD);

        // state field from CategoricalData
        textQuery.add(queryFactory.newDefinedTermQuery("states.modifyingText", queryString, languages), Occur.SHOULD);

        // area field from Distribution
        textQuery.add(queryFactory.newDefinedTermQuery("area", queryString, languages), Occur.SHOULD);

        // status field from Distribution
        textQuery.add(queryFactory.newDefinedTermQuery("status", queryString, languages), Occur.SHOULD);

        finalQuery.add(textQuery, Occur.MUST);
        // --- classification ----

        if(classification != null){
            finalQuery.add(queryFactory.newEntityIdQuery("inDescription.taxon.taxonNodes.classification.id", classification), Occur.MUST);
        }

        // --- IdentifieableEntity fields - by uuid
        if(features != null && features.size() > 0 ){
            finalQuery.add(queryFactory.newEntityUuidQuery("feature.uuid", features), Occur.MUST);
        }

        // the description must be associated with a taxon
        finalQuery.add(queryFactory.newIsNotNullQuery("inDescription.taxon.id"), Occur.MUST);

        luceneSearch.setQuery(finalQuery);

        if(highlightFragments){
            luceneSearch.setHighlightFields(queryFactory.getTextFieldNamesAsArray());
        }
        return luceneSearch;
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

    public List<Synonym> createInferredSynonyms(Taxon taxon, Classification classification, SynonymRelationshipType type, boolean doWithMisappliedNames){
        List <Synonym> inferredSynonyms = new ArrayList<Synonym>();
        List<Synonym> inferredSynonymsToBeRemoved = new ArrayList<Synonym>();

        HashMap <UUID, ZoologicalName> zooHashMap = new HashMap<UUID, ZoologicalName>();


        UUID uuid= taxon.getName().getUuid();
        ZoologicalName taxonName = getZoologicalName(uuid, zooHashMap);
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
                    TaxonNode parent = (TaxonNode)node.getParent();
                    parent = (TaxonNode)HibernateProxyHelper.deproxy(parent);
                    TaxonNameBase parentName =  parent.getTaxon().getName();
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
                            Set<String> genusNames = new HashSet<String>();

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
            IdentifiableSource originalSource = IdentifiableSource.NewInstance(idInSourceSyn + "; " + idInSourceParent, POTENTIAL_COMBINATION_NAMESPACE, sourceReference, null);
            inferredSynName.addSource(originalSource);
            originalSource = IdentifiableSource.NewInstance(idInSourceSyn + "; " + idInSourceParent, POTENTIAL_COMBINATION_NAMESPACE, sourceReference, null);
            potentialCombination.addSource(originalSource);
        }

        inferredSynName.generateTitle();

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
            IdentifiableSource originalSource = IdentifiableSource.NewInstance(idInSourceSyn + "; " + idInSourceTaxon, INFERRED_GENUS_NAMESPACE, sourceReference, null);
            inferredGenus.addSource(originalSource);

            originalSource = IdentifiableSource.NewInstance(idInSourceSyn + "; " + idInSourceTaxon, INFERRED_GENUS_NAMESPACE, sourceReference, null);
            inferredSynName.addSource(originalSource);
            originalSource = null;

        }else{
            logger.error("There is an idInSource missing: " + idInSourceSyn + " of Synonym or " + idInSourceTaxon + " of Taxon");
            IdentifiableSource originalSource = IdentifiableSource.NewInstance(idInSourceSyn + "; " + idInSourceTaxon, INFERRED_GENUS_NAMESPACE, sourceReference, null);
            inferredGenus.addSource(originalSource);

            originalSource = IdentifiableSource.NewInstance(idInSourceSyn + "; " + idInSourceTaxon, INFERRED_GENUS_NAMESPACE, sourceReference, null);
            inferredSynName.addSource(originalSource);
            originalSource = null;
        }

        taxon.addSynonym(inferredGenus, SynonymRelationshipType.INFERRED_GENUS_OF());

        inferredSynName.generateTitle();


        return inferredGenus;
    }

    private Synonym createInferredEpithets(Taxon taxon,
            HashMap<UUID, ZoologicalName> zooHashMap, ZoologicalName taxonName,
            String epithetOfTaxon, String infragenericEpithetOfTaxon,
            String infraspecificEpithetOfTaxon, List<String> taxonNames,
            TaxonNameBase parentName, TaxonBase syn) {

        Synonym inferredEpithet;
        TaxonNameBase synName;
        ZoologicalName inferredSynName;
        HibernateProxyHelper.deproxy(syn);

        // Determine the idInSource
        String idInSourceSyn = getIdInSource(syn);
        String idInSourceTaxon =  getIdInSource(taxon);
        // Determine the sourceReference
        Reference sourceReference = syn.getSec();

        if (sourceReference == null){
            logger.warn("The synonym has no sec reference because it is a misapplied name! Take the sec reference of taxon");
            //TODO:Remove
            System.out.println("The synonym has no sec reference because it is a misapplied name! Take the sec reference of taxon" + taxon.getSec());
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

        IdentifiableSource originalSource;
        originalSource = IdentifiableSource.NewInstance(taxonId, INFERRED_EPITHET_NAMESPACE, sourceReference, null);

        inferredEpithet.addSource(originalSource);

        originalSource = IdentifiableSource.NewInstance(taxonId, INFERRED_EPITHET_NAMESPACE, sourceReference, null);

        inferredSynName.addSource(originalSource);



        taxon.addSynonym(inferredEpithet, SynonymRelationshipType.INFERRED_EPITHET_OF());

        inferredSynName.generateTitle();
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

    public List<Synonym>  createAllInferredSynonyms(Taxon taxon, Classification tree, boolean doWithMisappliedNames){
        List <Synonym> inferredSynonyms = new ArrayList<Synonym>();

        inferredSynonyms.addAll(createInferredSynonyms(taxon, tree, SynonymRelationshipType.INFERRED_EPITHET_OF(), doWithMisappliedNames));
        inferredSynonyms.addAll(createInferredSynonyms(taxon, tree, SynonymRelationshipType.INFERRED_GENUS_OF(), doWithMisappliedNames));
        inferredSynonyms.addAll(createInferredSynonyms(taxon, tree, SynonymRelationshipType.POTENTIAL_COMBINATION_OF(), doWithMisappliedNames));

        return inferredSynonyms;
    }




}
