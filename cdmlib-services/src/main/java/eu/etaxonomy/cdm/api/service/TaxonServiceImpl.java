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
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.sandbox.queries.DuplicateFilter;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.grouping.TopGroups;
import org.apache.lucene.search.join.ScoreMode;
import org.apache.lucene.util.BytesRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.config.DeleteConfiguratorBase;
import eu.etaxonomy.cdm.api.service.config.IFindTaxaAndNamesConfigurator;
import eu.etaxonomy.cdm.api.service.config.IncludedTaxonConfiguration;
import eu.etaxonomy.cdm.api.service.config.MatchingTaxonConfigurator;
import eu.etaxonomy.cdm.api.service.config.NodeDeletionConfigurator.ChildHandling;
import eu.etaxonomy.cdm.api.service.config.SynonymDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.config.TaxonDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.dto.IdentifiedEntityDTO;
import eu.etaxonomy.cdm.api.service.dto.IncludedTaxaDTO;
import eu.etaxonomy.cdm.api.service.dto.MarkedEntityDTO;
import eu.etaxonomy.cdm.api.service.exception.DataChangeNoRollbackException;
import eu.etaxonomy.cdm.api.service.exception.HomotypicalGroupChangeException;
import eu.etaxonomy.cdm.api.service.exception.ReferencedObjectUndeletableException;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.api.service.search.ILuceneIndexToolProvider;
import eu.etaxonomy.cdm.api.service.search.ISearchResultBuilder;
import eu.etaxonomy.cdm.api.service.search.LuceneMultiSearch;
import eu.etaxonomy.cdm.api.service.search.LuceneMultiSearchException;
import eu.etaxonomy.cdm.api.service.search.LuceneParseException;
import eu.etaxonomy.cdm.api.service.search.LuceneSearch;
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
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
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
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.IZoologicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.HomotypicGroupTaxonComparator;
import eu.etaxonomy.cdm.model.taxon.ITaxonTreeNode;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
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
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;
import eu.etaxonomy.cdm.persistence.query.TaxonTitleType;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;


/**
 * @author a.kohlbecker
 * @since 10.09.2010
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
    private IOccurrenceService occurrenceService;

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

    @Override
    @Transactional(readOnly = false)
    public UpdateResult swapSynonymAndAcceptedTaxon(Synonym synonym, Taxon acceptedTaxon){
    	UpdateResult result = new UpdateResult();
        TaxonName synonymName = synonym.getName();
        synonymName.removeTaxonBase(synonym);
        TaxonName taxonName = acceptedTaxon.getName();
        taxonName.removeTaxonBase(acceptedTaxon);

        synonym.setName(taxonName);
        synonym.setTitleCache(null, false);
        synonym.getTitleCache();
        acceptedTaxon.setName(synonymName);
        acceptedTaxon.setTitleCache(null, false);
        acceptedTaxon.getTitleCache();
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
    public UpdateResult changeSynonymToAcceptedTaxon(Synonym synonym, Taxon acceptedTaxon, boolean deleteSynonym) {
        UpdateResult result = new UpdateResult();
        TaxonName acceptedName = acceptedTaxon.getName();
        TaxonName synonymName = synonym.getName();
        HomotypicalGroup synonymHomotypicGroup = synonymName.getHomotypicalGroup();

        //check synonym is not homotypic
        if (acceptedName.getHomotypicalGroup().equals(synonymHomotypicGroup)){
            String message = "The accepted taxon and the synonym are part of the same homotypical group and therefore can not be both accepted.";
            result.addException(new HomotypicalGroupChangeException(message));
            result.setAbort();
            return result;
        }

        Taxon newAcceptedTaxon = Taxon.NewInstance(synonymName, acceptedTaxon.getSec());
        dao.save(newAcceptedTaxon);
        result.setCdmEntity(newAcceptedTaxon);
        SynonymType relTypeForGroup = SynonymType.HOMOTYPIC_SYNONYM_OF();
        List<Synonym> heteroSynonyms = acceptedTaxon.getSynonymsInGroup(synonymHomotypicGroup);

        for (Synonym heteroSynonym : heteroSynonyms){
            if (synonym.equals(heteroSynonym)){
                acceptedTaxon.removeSynonym(heteroSynonym, false);
            }else{
                //move synonyms in same homotypic group to new accepted taxon
                newAcceptedTaxon.addSynonym(heteroSynonym, relTypeForGroup);
            }
        }
        dao.saveOrUpdate(acceptedTaxon);
        result.addUpdatedObject(acceptedTaxon);
        if (deleteSynonym){

            try {
                this.dao.flush();
                SynonymDeletionConfigurator config = new SynonymDeletionConfigurator();
                config.setDeleteNameIfPossible(false);
                this.deleteSynonym(synonym, config);

            } catch (Exception e) {
                result.addException(e);
            }
        }

        return result;
    }

    @Override
    @Transactional(readOnly = false)
    public UpdateResult changeSynonymToAcceptedTaxon(UUID synonymUuid,
            UUID acceptedTaxonUuid,
            UUID newParentNodeUuid,
            boolean deleteSynonym)  {
        UpdateResult result = new UpdateResult();
        Synonym synonym = CdmBase.deproxy(dao.load(synonymUuid), Synonym.class);
        Taxon acceptedTaxon = CdmBase.deproxy(dao.load(acceptedTaxonUuid), Taxon.class);
        result =  changeSynonymToAcceptedTaxon(synonym, acceptedTaxon, deleteSynonym);
        Taxon newTaxon = (Taxon)result.getCdmEntity();
        TaxonNode newParentNode = taxonNodeDao.load(newParentNodeUuid);
        TaxonNode newNode = newParentNode.addChildTaxon(newTaxon, null, null);
        taxonNodeDao.save(newNode);
        result.addUpdatedObject(newTaxon);
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
        result = changeSynonymToRelatedTaxon(synonym, toTaxon, taxonRelationshipType, citation, microcitation);
        Taxon relatedTaxon = (Taxon)result.getCdmEntity();
//        result.setCdmEntity(relatedTaxon);
        result.addUpdatedObject(relatedTaxon);
        result.addUpdatedObject(toTaxon);
        return result;
    }

    @Override
    @Transactional(readOnly = false)
    public UpdateResult changeSynonymToRelatedTaxon(Synonym synonym, Taxon toTaxon, TaxonRelationshipType taxonRelationshipType, Reference citation, String microcitation){
        // Get name from synonym
        if (synonym == null){
            return null;
        }

        UpdateResult result = new UpdateResult();

        TaxonName synonymName = synonym.getName();

      /*  // remove synonym from taxon
        toTaxon.removeSynonym(synonym);
*/
        // Create a taxon with synonym name
        Taxon fromTaxon = Taxon.NewInstance(synonymName, null);
        fromTaxon.setAppendedPhrase(synonym.getAppendedPhrase());

        // Add taxon relation
        fromTaxon.addTaxonRelation(toTaxon, taxonRelationshipType, citation, microcitation);
        result.setCdmEntity(fromTaxon);
        // since we are swapping names, we have to detach the name from the synonym completely.
        // Otherwise the synonym will still be in the list of typified names.
       // synonym.getName().removeTaxonBase(synonym);
        result.includeResult(this.deleteSynonym(synonym, null));

        return result;
    }

    @Transactional(readOnly = false)
    @Override
    public void changeHomotypicalGroupOfSynonym(Synonym synonym, HomotypicalGroup newHomotypicalGroup,
            Taxon targetTaxon, boolean setBasionymRelationIfApplicable){
        // Get synonym name
        TaxonName synonymName = synonym.getName();
        HomotypicalGroup oldHomotypicalGroup = synonymName.getHomotypicalGroup();

        // Switch groups
        oldHomotypicalGroup.removeTypifiedName(synonymName, false);
        newHomotypicalGroup.addTypifiedName(synonymName);

        //remove existing basionym relationships
        synonymName.removeBasionyms();

        //add basionym relationship
        if (setBasionymRelationIfApplicable){
            Set<TaxonName> basionyms = newHomotypicalGroup.getBasionyms();
            for (TaxonName basionym : basionyms){
                synonymName.addBasionym(basionym);
            }
        }

        //set synonym relationship correctly
        Taxon acceptedTaxon = synonym.getAcceptedTaxon();

        boolean hasNewTargetTaxon = targetTaxon != null && !targetTaxon.equals(acceptedTaxon);
        if (acceptedTaxon != null){

            HomotypicalGroup acceptedGroup = acceptedTaxon.getHomotypicGroup();
            boolean isHomotypicToTaxon = acceptedGroup.equals(newHomotypicalGroup);
            SynonymType newRelationType = isHomotypicToTaxon? SynonymType.HOMOTYPIC_SYNONYM_OF() : SynonymType.HETEROTYPIC_SYNONYM_OF();
            synonym.setType(newRelationType);

            if (hasNewTargetTaxon){
                acceptedTaxon.removeSynonym(synonym, false);
            }
        }
        if (hasNewTargetTaxon ){
            @SuppressWarnings("null")
            HomotypicalGroup acceptedGroup = targetTaxon.getHomotypicGroup();
            boolean isHomotypicToTaxon = acceptedGroup.equals(newHomotypicalGroup);
            SynonymType relType = isHomotypicToTaxon? SynonymType.HOMOTYPIC_SYNONYM_OF() : SynonymType.HETEROTYPIC_SYNONYM_OF();
            targetTaxon.addSynonym(synonym, relType);
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
    public Pager<TaxonBase> findTaxaByName(Class<? extends TaxonBase> clazz, String uninomial,	String infragenericEpithet, String specificEpithet,	String infraspecificEpithet, String authorship, Rank rank, Integer pageSize,Integer pageNumber) {
        if (clazz == null){
            clazz = TaxonBase.class;
        }
        Integer numberOfResults = dao.countTaxaByName(clazz, uninomial, infragenericEpithet, specificEpithet, infraspecificEpithet, rank);

        List<TaxonBase> results = new ArrayList<>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.findTaxaByName(clazz, uninomial, infragenericEpithet, specificEpithet, infraspecificEpithet, authorship, rank, pageSize, pageNumber);
        }

        return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
    }

    @Override
    public List<TaxonBase> listTaxaByName(Class<? extends TaxonBase> clazz, String uninomial, String infragenericEpithet, String specificEpithet,	String infraspecificEpithet, String authorship, Rank rank, Integer pageSize,Integer pageNumber) {
        if (clazz == null){
            clazz = TaxonBase.class;
        }
        Integer numberOfResults = dao.countTaxaByName(clazz, uninomial, infragenericEpithet, specificEpithet, infraspecificEpithet, rank);

        List<TaxonBase> results = new ArrayList<>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.findTaxaByName(clazz, uninomial, infragenericEpithet, specificEpithet, infraspecificEpithet, authorship, rank, pageSize, pageNumber);
        }

        return results;
    }

    @Override
    public List<TaxonRelationship> listToTaxonRelationships(Taxon taxon, TaxonRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths){
        Integer numberOfResults = dao.countTaxonRelationships(taxon, type, TaxonRelationship.Direction.relatedTo);

        List<TaxonRelationship> results = new ArrayList<>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.getTaxonRelationships(taxon, type, pageSize, pageNumber, orderHints, propertyPaths, TaxonRelationship.Direction.relatedTo);
        }
        return results;
    }

    @Override
    public Pager<TaxonRelationship> pageToTaxonRelationships(Taxon taxon, TaxonRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.countTaxonRelationships(taxon, type, TaxonRelationship.Direction.relatedTo);

        List<TaxonRelationship> results = new ArrayList<>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.getTaxonRelationships(taxon, type, pageSize, pageNumber, orderHints, propertyPaths, TaxonRelationship.Direction.relatedTo);
        }
        return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
    }

    @Override
    public List<TaxonRelationship> listFromTaxonRelationships(Taxon taxon, TaxonRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths){
        Integer numberOfResults = dao.countTaxonRelationships(taxon, type, TaxonRelationship.Direction.relatedFrom);

        List<TaxonRelationship> results = new ArrayList<>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.getTaxonRelationships(taxon, type, pageSize, pageNumber, orderHints, propertyPaths, TaxonRelationship.Direction.relatedFrom);
        }
        return results;
    }

    @Override
    public Pager<TaxonRelationship> pageFromTaxonRelationships(Taxon taxon, TaxonRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.countTaxonRelationships(taxon, type, TaxonRelationship.Direction.relatedFrom);

        List<TaxonRelationship> results = new ArrayList<>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.getTaxonRelationships(taxon, type, pageSize, pageNumber, orderHints, propertyPaths, TaxonRelationship.Direction.relatedFrom);
        }
        return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
    }

    @Override
    public List<TaxonRelationship> listTaxonRelationships(Set<TaxonRelationshipType> types,
            Integer pageSize, Integer pageStart, List<OrderHint> orderHints, List<String> propertyPaths) {
        Long numberOfResults = dao.countTaxonRelationships(types);

        List<TaxonRelationship> results = new ArrayList<>();
        if(numberOfResults > 0) {
            results = dao.getTaxonRelationships(types, pageSize, pageStart, orderHints, propertyPaths);
        }
        return results;
    }

    @Override
    public Taxon findAcceptedTaxonFor(UUID synonymUuid, UUID classificationUuid, List<String> propertyPaths){

        Taxon result = null;
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

        count = dao.countAcceptedTaxonFor(synonym, classificationFilter) ;
        if(count > 0){
            result = dao.acceptedTaxonFor(synonym, classificationFilter, propertyPaths);
        }

        return result;
    }


    @Override
    public Set<Taxon> listRelatedTaxa(Taxon taxon, Set<TaxonRelationshipEdge> includeRelationships, Integer maxDepth,
            Integer limit, Integer start, List<String> propertyPaths) {

        Set<Taxon> relatedTaxa = collectRelatedTaxa(taxon, includeRelationships, new HashSet<>(), maxDepth);
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
                if ( relationshipEdgeFilter.getTaxonRelationshipTypes().equals(taxRel.getType()) ) {
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
    public Pager<Synonym> getSynonyms(Taxon taxon,	SynonymType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Long numberOfResults = dao.countSynonyms(taxon, type);

        List<Synonym> results = new ArrayList<>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.getSynonyms(taxon, type, pageSize, pageNumber, orderHints, propertyPaths);
        }

        return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
    }

    @Override
    public List<List<Synonym>> getSynonymsByHomotypicGroup(Taxon taxon, List<String> propertyPaths){
        List<List<Synonym>> result = new ArrayList<>();
        taxon = (Taxon)dao.load(taxon.getUuid(), propertyPaths);
        HomotypicGroupTaxonComparator comparator = new HomotypicGroupTaxonComparator(taxon);


        //homotypic
        result.add(taxon.getHomotypicSynonymsByHomotypicGroup(comparator));

        //heterotypic
        List<HomotypicalGroup> homotypicalGroups = taxon.getHeterotypicSynonymyGroups();  //currently the list is sorted by the Taxon.defaultTaxonComparator
        for(HomotypicalGroup homotypicalGroup : homotypicalGroups){
            result.add(taxon.getSynonymsInGroup(homotypicalGroup, comparator));
        }

        return result;

    }

    @Override
    public List<Synonym> getHomotypicSynonymsByHomotypicGroup(Taxon taxon, List<String> propertyPaths){
        Taxon t = (Taxon)dao.load(taxon.getUuid(), propertyPaths);
        HomotypicGroupTaxonComparator comparator = new HomotypicGroupTaxonComparator(taxon);

        return t.getHomotypicSynonymsByHomotypicGroup(comparator);
    }

    @Override
    public List<List<Synonym>> getHeterotypicSynonymyGroups(Taxon taxon, List<String> propertyPaths){
        Taxon t = (Taxon)dao.load(taxon.getUuid(), propertyPaths);
        List<HomotypicalGroup> homotypicalGroups = t.getHeterotypicSynonymyGroups();
        List<List<Synonym>> heterotypicSynonymyGroups = new ArrayList<>(homotypicalGroups.size());
        for(HomotypicalGroup homotypicalGroup : homotypicalGroups){
            heterotypicSynonymyGroups.add(t.getSynonymsInGroup(homotypicalGroup));
        }
        return heterotypicSynonymyGroups;
    }

    @Override
    public List<UuidAndTitleCache<IdentifiableEntity>> findTaxaAndNamesForEditor(IFindTaxaAndNamesConfigurator configurator){

        List<UuidAndTitleCache<IdentifiableEntity>> results = new ArrayList<>();


        if (configurator.isDoSynonyms() || configurator.isDoTaxa() || configurator.isDoNamesWithoutTaxa() || configurator.isDoTaxaByCommonNames()){
        	results = dao.getTaxaByNameForEditor(configurator.isDoTaxa(), configurator.isDoSynonyms(), configurator.isDoNamesWithoutTaxa(), configurator.isDoMisappliedNames(), configurator.isDoTaxaByCommonNames(), configurator.getTitleSearchStringSqlized(), configurator.getClassification(), configurator.getMatchMode(), configurator.getNamedAreas(), configurator.getOrder());
        }

        return results;
    }

    @Override
    public Pager<IdentifiableEntity> findTaxaAndNames(IFindTaxaAndNamesConfigurator configurator) {

        List<IdentifiableEntity> results = new ArrayList<>();
        long numberOfResults = 0; // overall number of results (as opposed to number of results per page)
        List<TaxonBase> taxa = null;

        // Taxa and synonyms
        long numberTaxaResults = 0L;


        List<String> propertyPath = new ArrayList<>();
        if(configurator.getTaxonPropertyPath() != null){
            propertyPath.addAll(configurator.getTaxonPropertyPath());
        }


       if (configurator.isDoMisappliedNames() || configurator.isDoSynonyms() || configurator.isDoTaxa() || configurator.isDoTaxaByCommonNames()){
            if(configurator.getPageSize() != null){ // no point counting if we need all anyway
                numberTaxaResults =
                    dao.countTaxaByName(configurator.isDoTaxa(),configurator.isDoSynonyms(), configurator.isDoMisappliedNames(),
                        configurator.isDoTaxaByCommonNames(), configurator.isDoIncludeAuthors(), configurator.getTitleSearchStringSqlized(),
                        configurator.getClassification(), configurator.getMatchMode(),
                        configurator.getNamedAreas());
            }

            if(configurator.getPageSize() == null || numberTaxaResults > configurator.getPageSize() * configurator.getPageNumber()){ // no point checking again if less results
                taxa = dao.getTaxaByName(configurator.isDoTaxa(), configurator.isDoSynonyms(),
                    configurator.isDoMisappliedNames(), configurator.isDoTaxaByCommonNames(), configurator.isDoIncludeAuthors(),
                    configurator.getTitleSearchStringSqlized(), configurator.getClassification(),
                    configurator.getMatchMode(), configurator.getNamedAreas(), configurator.getOrder(),
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

            List<? extends TaxonName> names =
                nameDao.findByName(configurator.isDoIncludeAuthors(), configurator.getTitleSearchStringSqlized(), configurator.getMatchMode(),
                        configurator.getPageSize(), configurator.getPageNumber(), null, configurator.getTaxonNamePropertyPath());
            if (logger.isDebugEnabled()) { logger.debug(names.size() + " matching name(s) found"); }
            if (names.size() > 0) {
                for (TaxonName taxonName : names) {
                    if (taxonName.getTaxonBases().size() == 0) {
                        results.add(taxonName);
                        numberNameResults++;
                    }
                }
                if (logger.isDebugEnabled()) { logger.debug(numberNameResults + " matching name(s) without taxa found"); }
                numberOfResults += numberNameResults;
            }
        }



       return new DefaultPagerImpl<>
            (configurator.getPageNumber(), numberOfResults, configurator.getPageSize(), results);
    }

    public List<UuidAndTitleCache<TaxonBase>> getTaxonUuidAndTitleCache(Integer limit, String pattern){
        return dao.getUuidAndTitleCache(limit, pattern);
    }

    @Override
    public List<MediaRepresentation> getAllMedia(Taxon taxon, int size, int height, int widthOrDuration, String[] mimeTypes){
        List<MediaRepresentation> medRep = new ArrayList<>();
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

        Set<Taxon> taxa = new HashSet<>();
        List<Media> taxonMedia = new ArrayList<>();
        List<Media> nonImageGalleryImages = new ArrayList<>();

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
            List<TaxonDescription> taxonDescriptions = new ArrayList<>();
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
            Set<SpecimenOrObservationBase> specimensOrObservations = new HashSet<>();
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

                if (occurrence.isInstanceOf(DerivedUnit.class)) {
                    DerivedUnit derivedUnit = CdmBase.deproxy(occurrence, DerivedUnit.class);
                    // Collection
                    //TODO why may collections have media attached? #
                    if (derivedUnit.getCollection() != null){
                        taxonMedia.addAll(derivedUnit.getCollection().getMedia());
                    }
                }
                //media in hierarchy
                taxonMedia.addAll(occurrenceService.getMediainHierarchy(occurrence, null, null, propertyPath).getRecords());
            }
        }

        if(includeTaxonNameDescriptions != null && includeTaxonNameDescriptions) {
            logger.trace("listMedia() - includeTaxonNameDescriptions");
            // --- TaxonNameDescription
            Set<TaxonNameDescription> nameDescriptions = new HashSet<>();
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
        return this.dao.loadList(listOfIDs, null);
    }

    @Override
    public TaxonBase findTaxonByUuid(UUID uuid, List<String> propertyPaths){
        return this.dao.findByUuid(uuid, null ,propertyPaths);
    }

    @Override
    public int countSynonyms(boolean onlyAttachedToTaxon){
        return this.dao.countSynonyms(onlyAttachedToTaxon);
    }

    @Override
    public List<TaxonName> findIdenticalTaxonNames(List<String> propertyPath) {
        return this.dao.findIdenticalTaxonNames(propertyPath);
    }

    @Override
    @Transactional(readOnly=false)
    public DeleteResult deleteTaxon(UUID taxonUUID, TaxonDeletionConfigurator config, UUID classificationUuid)  {

    	if (config == null){
            config = new TaxonDeletionConfigurator();
        }
    	Taxon taxon = (Taxon)dao.load(taxonUUID);
    	DeleteResult result = new DeleteResult();
    	if (taxon == null){
    	    result.setAbort();
    	    result.addException(new Exception ("The taxon was already deleted."));
    	    return result;
    	}
    	taxon = HibernateProxyHelper.deproxy(taxon);
    	Classification classification = HibernateProxyHelper.deproxy(classificationDao.load(classificationUuid), Classification.class);
        result = isDeletable(taxonUUID, config);

        if (result.isOk()){
            // --- DeleteSynonymRelations
            if (config.isDeleteSynonymRelations()){
                boolean removeSynonymNameFromHomotypicalGroup = false;
                // use tmp Set to avoid concurrent modification
                Set<Synonym> synsToDelete = new HashSet<>();
                synsToDelete.addAll(taxon.getSynonyms());
                for (Synonym synonym : synsToDelete){
                    taxon.removeSynonym(synonym, removeSynonymNameFromHomotypicalGroup);

                    // --- DeleteSynonymsIfPossible
                    if (config.isDeleteSynonymsIfPossible()){
                        //TODO which value
                        boolean newHomotypicGroupIfNeeded = true;
                        SynonymDeletionConfigurator synConfig = new SynonymDeletionConfigurator();
                        result.includeResult(deleteSynonym(synonym, synConfig));
                    }
                }
            }

            // --- DeleteTaxonRelationships
            if (! config.isDeleteTaxonRelationships()){
                if (taxon.getTaxonRelations().size() > 0){
                    result.setAbort();
                    result.addException(new Exception("Taxon can't be deleted as it is related to another taxon. " +
                            "Remove taxon from all relations to other taxa prior to deletion."));

                }
            } else{
                TaxonDeletionConfigurator configRelTaxon = new TaxonDeletionConfigurator();
                configRelTaxon.setDeleteTaxonNodes(false);
                configRelTaxon.setDeleteConceptRelationships(true);

                for (TaxonRelationship taxRel: taxon.getTaxonRelations()){
                    if (config.isDeleteMisappliedNamesAndInvalidDesignations()
                            && taxRel.getType().isMisappliedNameOrInvalidDesignation()
                            && taxon.equals(taxRel.getToTaxon())){
                        this.deleteTaxon(taxRel.getFromTaxon().getUuid(), config, classificationUuid);
                    } else if (config.isDeleteConceptRelationships() && taxRel.getType().isConceptRelationship()){

                        if (taxon.equals(taxRel.getToTaxon()) && isDeletable(taxRel.getFromTaxon().getUuid(), configRelTaxon).isOk()){
                            this.deleteTaxon(taxRel.getFromTaxon().getUuid(), configRelTaxon, classificationUuid);
                        }else if (isDeletable(taxRel.getToTaxon().getUuid(), configRelTaxon).isOk()){
                            this.deleteTaxon(taxRel.getToTaxon().getUuid(), configRelTaxon, classificationUuid);
                        }
                    }
                    taxon.removeTaxonRelation(taxRel);

                }
            }

            //    	TaxonDescription
            if (config.isDeleteDescriptions()){
                Set<TaxonDescription> descriptions = taxon.getDescriptions();
                List<TaxonDescription> removeDescriptions = new ArrayList<>();
                for (TaxonDescription desc: descriptions){
                    //TODO use description delete configurator ?
                    //FIXME check if description is ALWAYS deletable
                    if (desc.getDescribedSpecimenOrObservation() != null){
                        result.setAbort();
                        result.addException(new Exception("Taxon can't be deleted as it is used in a TaxonDescription" +
                                " which also describes specimens or observations"));
                        break;
                    }
                    removeDescriptions.add(desc);


                }
                if (result.isOk()){
                    for (TaxonDescription desc: removeDescriptions){
                        taxon.removeDescription(desc);
                        descriptionService.delete(desc);
                    }
                } else {
                    return result;
                }
            }


         if (! config.isDeleteTaxonNodes() || (!config.isDeleteInAllClassifications() && classification == null && taxon.getTaxonNodes().size() > 1)){
             result.addException(new Exception( "Taxon can't be deleted as it is used in more than one classification."));
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
                        HibernateProxyHelper.deproxy(node, TaxonNode.class);
                        success =taxon.removeTaxonNode(node, deleteChildren);
                        nodeService.delete(node);
                        result.addDeletedObject(node);
                    } else {
                    	result.setError();
                    	result.addException(new Exception("The taxon can not be deleted because it is not used in defined classification."));
                    }
                } else if (config.isDeleteInAllClassifications()){
                    List<TaxonNode> nodesList = new ArrayList<>();
                    nodesList.addAll(taxon.getTaxonNodes());
                    for (ITaxonTreeNode treeNode: nodesList){
                        TaxonNode taxonNode = (TaxonNode) treeNode;
                        if(!deleteChildren){
                            Object[] childNodes = taxonNode.getChildNodes().toArray();
                            for (Object childNode: childNodes){
                                TaxonNode childNodeCast = (TaxonNode) childNode;
                                taxonNode.getParent().addChildNode(childNodeCast, childNodeCast.getReference(), childNodeCast.getMicroReference());
                            }
                        }
                    }
                    config.getTaxonNodeConfig().setDeleteElement(false);
                    DeleteResult resultNodes = nodeService.deleteTaxonNodes(nodesList, config);
                    if (!resultNodes.isOk()){
                    	result.addExceptions(resultNodes.getExceptions());
                    	result.setStatus(resultNodes.getStatus());
                    } else {
                        result.addUpdatedObjects(resultNodes.getUpdatedObjects());
                    }
                }
                if (!success){
                    result.setError();
                    result.addException(new Exception("The taxon can not be deleted because the taxon node can not be removed."));
                }
            }
         }
         TaxonName name = taxon.getName();
         taxon.setName(null);
         this.saveOrUpdate(taxon);

         if ((taxon.getTaxonNodes() == null || taxon.getTaxonNodes().size()== 0)  && result.isOk()){
             try{
                 UUID uuid = dao.delete(taxon);
                 result.addDeletedObject(taxon);
             }catch(Exception e){
                 result.addException(e);
                 result.setError();
             }
         } else {
             result.setError();
             result.addException(new Exception("The Taxon can't be deleted because it is used in a classification."));

         }
            //TaxonName
        if (config.isDeleteNameIfPossible() && result.isOk()){
            DeleteResult nameResult = new DeleteResult();
            //remove name if possible (and required)
            if (name != null ){
                nameResult = nameService.delete(name.getUuid(), config.getNameDeletionConfig());
            }
            if (nameResult.isError() || nameResult.isAbort()){
                result.addRelatedObject(name);
                result.addExceptions(nameResult.getExceptions());
            }else{
                result.includeResult(nameResult);
            }


       }
       }

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
        List<CdmBase> list = genericDao.getCdmBasesByFieldAndClass(PolytomousKeyNode.class, "taxon", taxon, null);
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
    public DeleteResult deleteSynonym(UUID synonymUuid, SynonymDeletionConfigurator config) {
        return deleteSynonym((Synonym)dao.load(synonymUuid), config);

    }


    @Override
    @Transactional(readOnly = false)
    public DeleteResult deleteSynonym(Synonym synonym, SynonymDeletionConfigurator config) {
        DeleteResult result = new DeleteResult();
    	if (synonym == null){
    		result.setAbort();
    		result.addException(new Exception("The synonym was already deleted."));
    		return result;
        }

        if (config == null){
            config = new SynonymDeletionConfigurator();
        }

        result = isDeletable(synonym.getUuid(), config);


        if (result.isOk()){

            synonym = HibernateProxyHelper.deproxy(this.load(synonym.getUuid()), Synonym.class);

            //remove synonym
            Taxon accTaxon = synonym.getAcceptedTaxon();

            if (accTaxon != null){
                accTaxon = HibernateProxyHelper.deproxy(accTaxon, Taxon.class);
                accTaxon.removeSynonym(synonym, false);
                this.saveOrUpdate(accTaxon);
                result.addUpdatedObject(accTaxon);
            }
            this.saveOrUpdate(synonym);
            //#6281
            dao.flush();

            TaxonName name = synonym.getName();
            synonym.setName(null);

            dao.delete(synonym);
            result.addDeletedObject(synonym);

            //remove name if possible (and required)
            if (name != null && config.isDeleteNameIfPossible()){

                    DeleteResult nameDeleteResult = nameService.delete(name, config.getNameDeletionConfig());
                    if (nameDeleteResult.isAbort() || nameDeleteResult.isError()){
                    	result.addExceptions(nameDeleteResult.getExceptions());
                    	result.addRelatedObject(name);
                    }else{
                        result.addDeletedObject(name);
                    }
            }

        }
        return result;
    }

    @Override
    public List<TaxonName> findIdenticalTaxonNameIds(List<String> propertyPath) {

        return this.dao.findIdenticalNamesNew(propertyPath);
    }
//
//    @Override
//    public String getPhylumName(TaxonName name){
//        return this.dao.getPhylumName(name);
//    }

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
            // 1. search for accepted taxa
            List<TaxonBase> taxonList = dao.findByNameTitleCache(true, false, config.getTaxonNameTitle(), null, MatchMode.EXACT, null, null, 0, null, null);
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
                List<TaxonBase> synonymList = dao.findByNameTitleCache(false, true, config.getTaxonNameTitle(), null, MatchMode.EXACT, null, null, 0, null, null);
                for(TaxonBase taxonBase : synonymList){
                    if(taxonBase instanceof Synonym){
                        Synonym synonym = CdmBase.deproxy(taxonBase, Synonym.class);
                        bestCandidate = synonym.getAcceptedTaxon();
                        if(bestCandidate != null){
                            logger.info("using accepted Taxon " +  bestCandidate.getTitleCache() + " for synonym " + taxonBase.getTitleCache());
                            return bestCandidate;
                        }
                        //TODO extend method: search using treeUUID, using SecUUID, first find accepted then include synonyms until a matching taxon is found
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
        List<TaxonBase> synonymList = dao.findByNameTitleCache(false, true, taxonName, null, MatchMode.EXACT, null, null, 0, null, null);
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
    public UpdateResult moveSynonymToAnotherTaxon(Synonym oldSynonym,
            Taxon newTaxon,
            boolean moveHomotypicGroup,
            SynonymType newSynonymType) throws HomotypicalGroupChangeException {
        return moveSynonymToAnotherTaxon(oldSynonym, newTaxon, moveHomotypicGroup,
                newSynonymType,
                oldSynonym.getSec(),
                oldSynonym.getSecMicroReference(),
                true);
    }

    @Override
    @Transactional(readOnly = false)
    public UpdateResult moveSynonymToAnotherTaxon(Synonym oldSynonym,
            Taxon newTaxon,
            boolean moveHomotypicGroup,
            SynonymType newSynonymType,
            Reference newSecundum,
            String newSecundumDetail,
            boolean keepSecundumIfUndefined) throws HomotypicalGroupChangeException {

        Synonym synonym = CdmBase.deproxy(dao.load(oldSynonym.getUuid()), Synonym.class);
        Taxon oldTaxon = CdmBase.deproxy(dao.load(synonym.getAcceptedTaxon().getUuid()), Taxon.class);
        //TODO what if there is no name ?? Concepts may be cached (e.g. via TCS import)
        TaxonName synonymName = synonym.getName();
        TaxonName fromTaxonName = oldTaxon.getName();
        //set default relationship type
        if (newSynonymType == null){
            newSynonymType = SynonymType.HETEROTYPIC_SYNONYM_OF();
        }
        boolean newRelTypeIsHomotypic = newSynonymType.equals(SynonymType.HOMOTYPIC_SYNONYM_OF());

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

        UpdateResult result = new UpdateResult();
        //move all synonyms to new taxon
        List<Synonym> homotypicSynonyms = oldTaxon.getSynonymsInGroup(homotypicGroup);
        for (Synonym synRelation: homotypicSynonyms){

            newTaxon = HibernateProxyHelper.deproxy(newTaxon, Taxon.class);
            oldTaxon = HibernateProxyHelper.deproxy(oldTaxon, Taxon.class);
            newTaxon.addSynonym(synRelation, newSynonymType);
            oldTaxon.removeSynonym(synRelation, false);
            if (newSecundum != null || !keepSecundumIfUndefined){
                synRelation.setSec(newSecundum);
            }
            if (newSecundumDetail != null || !keepSecundumIfUndefined){
                synRelation.setSecMicroReference(newSecundumDetail);
            }

            //set result  //why is this needed? Seems wrong to me (AM 10.10.2016)
            if (!synRelation.equals(oldSynonym)){
                result.setError();
            }
        }

        result.addUpdatedObject(oldTaxon);
        result.addUpdatedObject(newTaxon);
        saveOrUpdate(oldTaxon);
        saveOrUpdate(newTaxon);

        return result;
    }

    @Override
    public <T extends TaxonBase> List<UuidAndTitleCache<T>> getUuidAndTitleCache(Class<T> clazz, Integer limit, String pattern) {
        return dao.getUuidAndTitleCache(clazz, limit, pattern);
    }

    @Override
    public Pager<SearchResult<TaxonBase>> findByFullText(
            Class<? extends TaxonBase> clazz, String queryString,
            Classification classification, List<Language> languages,
            boolean highlightFragments, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) throws IOException, LuceneParseException {


        LuceneSearch luceneSearch = prepareFindByFullTextSearch(clazz, queryString, classification, languages, highlightFragments, null);

        // --- execute search
        TopGroups<BytesRef> topDocsResultSet;
        try {
            topDocsResultSet = luceneSearch.executeSearch(pageSize, pageNumber);
        } catch (ParseException e) {
            LuceneParseException luceneParseException = new LuceneParseException(e.getMessage());
            luceneParseException.setStackTrace(e.getStackTrace());
            throw luceneParseException;
        }

        Map<CdmBaseType, String> idFieldMap = new HashMap<>();
        idFieldMap.put(CdmBaseType.TAXON, "id");

        // ---  initialize taxa, thighlight matches ....
        ISearchResultBuilder searchResultBuilder = new SearchResultBuilder(luceneSearch, luceneSearch.getQuery());
        List<SearchResult<TaxonBase>> searchResults = searchResultBuilder.createResultSet(
                topDocsResultSet, luceneSearch.getHighlightFields(), dao, idFieldMap, propertyPaths);

        int totalHits = topDocsResultSet != null ? topDocsResultSet.totalGroupCount : 0;
        return new DefaultPagerImpl<>(pageNumber, totalHits, pageSize, searchResults);
    }

    @Override
    public Pager<SearchResult<TaxonBase>> findByDistribution(List<NamedArea> areaFilter, List<PresenceAbsenceTerm> statusFilter,
            Classification classification,
            Integer pageSize, Integer pageNumber,
            List<OrderHint> orderHints, List<String> propertyPaths) throws IOException, LuceneParseException {

        LuceneSearch luceneSearch = prepareByDistributionSearch(areaFilter, statusFilter, classification);

        // --- execute search
        TopGroups<BytesRef> topDocsResultSet;
        try {
            topDocsResultSet = luceneSearch.executeSearch(pageSize, pageNumber);
        } catch (ParseException e) {
            LuceneParseException luceneParseException = new LuceneParseException(e.getMessage());
            luceneParseException.setStackTrace(e.getStackTrace());
            throw luceneParseException;
        }

        Map<CdmBaseType, String> idFieldMap = new HashMap<>();
        idFieldMap.put(CdmBaseType.TAXON, "id");

        // ---  initialize taxa, thighlight matches ....
        ISearchResultBuilder searchResultBuilder = new SearchResultBuilder(luceneSearch, luceneSearch.getQuery());
        List<SearchResult<TaxonBase>> searchResults = searchResultBuilder.createResultSet(
                topDocsResultSet, luceneSearch.getHighlightFields(), dao, idFieldMap, propertyPaths);

        int totalHits = topDocsResultSet != null ? topDocsResultSet.totalGroupCount : 0;
        return new DefaultPagerImpl<>(pageNumber, totalHits, pageSize, searchResults);
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
        Builder finalQueryBuilder = new Builder();
        Builder textQueryBuilder = new Builder();

        LuceneSearch luceneSearch = new LuceneSearch(luceneIndexToolProvider, GroupByTaxonClassBridge.GROUPBY_TAXON_FIELD, TaxonBase.class);
        QueryFactory taxonBaseQueryFactory = luceneIndexToolProvider.newQueryFactoryFor(TaxonBase.class);

        if(sortFields == null){
            sortFields = new  SortField[]{SortField.FIELD_SCORE, new SortField("titleCache__sort", SortField.Type.STRING,  false)};
        }
        luceneSearch.setSortFields(sortFields);

        // ---- search criteria
        luceneSearch.setCdmTypRestriction(clazz);

        if(!queryString.isEmpty() && !queryString.equals("*") && !queryString.equals("?") ) {
            textQueryBuilder.add(taxonBaseQueryFactory.newTermQuery("titleCache", queryString), Occur.SHOULD);
            textQueryBuilder.add(taxonBaseQueryFactory.newDefinedTermQuery("name.rank", queryString, languages), Occur.SHOULD);
        }

        BooleanQuery textQuery = textQueryBuilder.build();
        if(textQuery.clauses().size() > 0) {
            finalQueryBuilder.add(textQuery, Occur.MUST);
        }


        if(classification != null){
            finalQueryBuilder.add(taxonBaseQueryFactory.newEntityIdQuery("taxonNodes.classification.id", classification), Occur.MUST);
        }
        luceneSearch.setQuery(finalQueryBuilder.build());

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

        Builder finalQueryBuilder = new Builder();

        LuceneSearch luceneSearch = new LuceneSearch(luceneIndexToolProvider, GroupByTaxonClassBridge.GROUPBY_TAXON_FIELD, TaxonBase.class);
        QueryFactory taxonBaseQueryFactory = luceneIndexToolProvider.newQueryFactoryFor(TaxonBase.class);

        Builder joinFromQueryBuilder = new Builder();
        joinFromQueryBuilder.add(taxonBaseQueryFactory.newTermQuery(queryTermField, queryString), Occur.MUST);
        joinFromQueryBuilder.add(taxonBaseQueryFactory.newEntityIdsQuery("type.id", edge.getTaxonRelationshipTypes()), Occur.MUST);

        Query joinQuery = taxonBaseQueryFactory.newJoinQuery(TaxonRelationship.class, fromField, false, joinFromQueryBuilder.build(), toField, null, ScoreMode.Max);

        if(sortFields == null){
            sortFields = new  SortField[]{SortField.FIELD_SCORE, new SortField("titleCache__sort", SortField.Type.STRING,  false)};
        }
        luceneSearch.setSortFields(sortFields);

        finalQueryBuilder.add(joinQuery, Occur.MUST);

        if(classification != null){
            finalQueryBuilder.add(taxonBaseQueryFactory.newEntityIdQuery("taxonNodes.classification.id", classification), Occur.MUST);
        }
        luceneSearch.setQuery(finalQueryBuilder.build());

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
            throws IOException, LuceneParseException, LuceneMultiSearchException {

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
            namedAreaList = new ArrayList<>(namedAreas.size());
            namedAreaList.addAll(namedAreas);
        }
        if(distributionStatus != null){
            distributionStatusList = new ArrayList<>(distributionStatus.size());
            distributionStatusList.addAll(distributionStatus);
        }

        // set default if parameter is null
        if(searchModes == null){
            searchModes = EnumSet.of(TaxaAndNamesSearchMode.doTaxa);
        }

        // set sort order and thus override any sort orders which may have been
        // defined by prepare*Search methods
        if(orderHints == null){
            orderHints = OrderHint.NOMENCLATURAL_SORT_ORDER.asList();
        }
        SortField[] sortFields = new SortField[orderHints.size()];
        int i = 0;
        for(OrderHint oh : orderHints){
            sortFields[i++] = oh.toSortField();
        }
//        SortField[] sortFields = new SortField[]{SortField.FIELD_SCORE, new SortField("id", SortField.STRING, false)};
//        SortField[] sortFields = new SortField[]{new SortField(NomenclaturalSortOrderBrigde.NAME_SORT_FIELD_NAME, SortField.STRING, false)};


        boolean addDistributionFilter = namedAreas != null && namedAreas.size() > 0;

        List<LuceneSearch> luceneSearches = new ArrayList<>();
        Map<CdmBaseType, String> idFieldMap = new HashMap<>();

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

        Builder multiIndexByAreaFilterBuilder = new Builder();

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
                String toField = "accTaxon.id"; // id in TaxonBase index (is multivalued)

                //TODO replace by createByDistributionJoinQuery
                BooleanQuery byDistributionQuery = createByDistributionQuery(namedAreaList, distributionStatusList, distributionFilterQueryFactory);
                Query taxonAreaJoinQuery = distributionFilterQueryFactory.newJoinQuery(Distribution.class, fromField, true, byDistributionQuery, toField, Taxon.class, ScoreMode.None);
                multiIndexByAreaFilterBuilder.add(taxonAreaJoinQuery, Occur.SHOULD);

            }
        }

        // search by CommonTaxonName
        if(searchModes.contains(TaxaAndNamesSearchMode.doTaxaByCommonNames)) {
            // B)
            QueryFactory descriptionElementQueryFactory = luceneIndexToolProvider.newQueryFactoryFor(DescriptionElementBase.class);
            Query byCommonNameJoinQuery = descriptionElementQueryFactory.newJoinQuery(
                    CommonTaxonName.class,
                    "inDescription.taxon.id",
                    true,
                    QueryFactory.addTypeRestriction(
                                createByDescriptionElementFullTextQuery(queryString, classification, null, languages, descriptionElementQueryFactory)
                                , CommonTaxonName.class
                                ).build(), "id", null, ScoreMode.Max);
            logger.debug("byCommonNameJoinQuery: " + byCommonNameJoinQuery.toString());
            LuceneSearch byCommonNameSearch = new LuceneSearch(luceneIndexToolProvider, GroupByTaxonClassBridge.GROUPBY_TAXON_FIELD, Taxon.class);
            byCommonNameSearch.setCdmTypRestriction(Taxon.class);
            byCommonNameSearch.setQuery(byCommonNameJoinQuery);
            byCommonNameSearch.setSortFields(sortFields);

            DuplicateFilter df = new DuplicateFilter("inDescription.taxon.id");
            Set<String> results=new HashSet<>();
//            ScoreDoc[] hits = searcher.search(tq,df, 1000).scoreDocs;
//
//            byCommonNameSearch.setFilter(df);
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
                    new TaxonRelationshipEdge(TaxonRelationshipType.allMisappliedNameTypes(), Direction.relatedTo),
                    queryString, classification, languages, highlightFragments, sortFields));
            idFieldMap.put(CdmBaseType.TAXON, "id");

            if(addDistributionFilter){
                String fromField = "inDescription.taxon.id"; // in DescriptionElementBase index

                /*
                 * Here i was facing wired and nasty bug which took me bugging be really for hours until I found this solution.
                 * Maybe this is a bug in java itself java.
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

                //TODO replace by createByDistributionJoinQuery
                BooleanQuery byDistributionQuery = createByDistributionQuery(namedAreaList, distributionStatusList, distributionFilterQueryFactory);
                Query taxonAreaJoinQuery = distributionFilterQueryFactory.newJoinQuery(Distribution.class, fromField, true, byDistributionQuery, toField, null, ScoreMode.None);

//                debug code for bug described above
                //does not compile anymore since changing from lucene 3.6.2 to lucene 4.10+
//                DocIdSet filterMatchSet = filter.getDocIdSet(luceneIndexToolProvider.getIndexReaderFor(Taxon.class));
//                System.err.println(DocIdBitSetPrinter.docsAsString(filterMatchSet, 100));

                multiIndexByAreaFilterBuilder.add(taxonAreaJoinQuery, Occur.SHOULD);
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
                    distributionFilterQueryFactory,
                    Taxon.class, true
                    );
            multiIndexByAreaFilterBuilder.add(taxonAreaJoinQuery, Occur.SHOULD);
        }

        if (addDistributionFilter){
            multiSearch.setFilter(multiIndexByAreaFilterBuilder.build());
        }


        // --- execute search
        TopGroups<BytesRef> topDocsResultSet;
        try {
            topDocsResultSet = multiSearch.executeSearch(pageSize, pageNumber);
        } catch (ParseException e) {
            LuceneParseException luceneParseException = new LuceneParseException(e.getMessage());
            luceneParseException.setStackTrace(e.getStackTrace());
            throw luceneParseException;
        }

        // --- initialize taxa, highlight matches ....
        ISearchResultBuilder searchResultBuilder = new SearchResultBuilder(multiSearch, multiSearch.getQuery());


        List<SearchResult<TaxonBase>> searchResults = searchResultBuilder.createResultSet(
                topDocsResultSet, multiSearch.getHighlightFields(), dao, idFieldMap, propertyPaths);

        int totalHits = topDocsResultSet != null ? topDocsResultSet.totalGroupCount : 0;
        return new DefaultPagerImpl<>(pageNumber, totalHits, pageSize, searchResults);
    }

    /**
     * @param namedAreaList at least one area must be in the list
     * @param distributionStatusList optional
     * @param toType toType
     *      Optional parameter. Only used for debugging to print the toType documents
     * @param asFilter TODO
     * @return
     * @throws IOException
     */
    protected Query createByDistributionJoinQuery(
            List<NamedArea> namedAreaList,
            List<PresenceAbsenceTerm> distributionStatusList,
            QueryFactory queryFactory, Class<? extends CdmBase> toType, boolean asFilter
            ) throws IOException {

        String fromField = "inDescription.taxon.id"; // in DescriptionElementBase index
        String toField = "id"; // id in toType usually this is the TaxonBase index

        BooleanQuery byDistributionQuery = createByDistributionQuery(namedAreaList, distributionStatusList, queryFactory);

        ScoreMode scoreMode = asFilter ?  ScoreMode.None : ScoreMode.Max;

        Query taxonAreaJoinQuery = queryFactory.newJoinQuery(Distribution.class, fromField, false, byDistributionQuery, toField, toType, scoreMode);

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
        Builder areaQueryBuilder = new Builder();
        // area field from Distribution
        areaQueryBuilder.add(queryFactory.newEntityIdsQuery("area.id", namedAreaList), Occur.MUST);

        // status field from Distribution
        if(distributionStatusList != null && distributionStatusList.size() > 0){
            areaQueryBuilder.add(queryFactory.newEntityIdsQuery("status.id", distributionStatusList), Occur.MUST);
        }

        BooleanQuery areaQuery = areaQueryBuilder.build();
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

        Builder finalQueryBuilder = new Builder();

        LuceneSearch luceneSearch = new LuceneSearch(luceneIndexToolProvider, GroupByTaxonClassBridge.GROUPBY_TAXON_FIELD, Taxon.class);

        // FIXME is this query factory using the wrong type?
        QueryFactory taxonQueryFactory = luceneIndexToolProvider.newQueryFactoryFor(Taxon.class);

        SortField[] sortFields = new  SortField[]{SortField.FIELD_SCORE, new SortField("titleCache__sort", SortField.Type.STRING, false)};
        luceneSearch.setSortFields(sortFields);


        Query byAreaQuery = createByDistributionJoinQuery(namedAreaList, distributionStatusList, taxonQueryFactory, null, false);

        finalQueryBuilder.add(byAreaQuery, Occur.MUST);

        if(classification != null){
            finalQueryBuilder.add(taxonQueryFactory.newEntityIdQuery("taxonNodes.classification.id", classification), Occur.MUST);
        }
        BooleanQuery finalQuery = finalQueryBuilder.build();
        logger.info("prepareByAreaSearch() query: " + finalQuery.toString());
        luceneSearch.setQuery(finalQuery);

        return luceneSearch;
    }

    @Override
    public Pager<SearchResult<TaxonBase>> findByDescriptionElementFullText(
            Class<? extends DescriptionElementBase> clazz, String queryString,
            Classification classification, List<Feature> features, List<Language> languages,
            boolean highlightFragments, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) throws IOException, LuceneParseException {


        LuceneSearch luceneSearch = prepareByDescriptionElementFullTextSearch(clazz, queryString, classification, features, languages, highlightFragments);

        // --- execute search
        TopGroups<BytesRef> topDocsResultSet;
        try {
            topDocsResultSet = luceneSearch.executeSearch(pageSize, pageNumber);
        } catch (ParseException e) {
            LuceneParseException luceneParseException = new LuceneParseException(e.getMessage());
            luceneParseException.setStackTrace(e.getStackTrace());
            throw luceneParseException;
        }

        Map<CdmBaseType, String> idFieldMap = new HashMap<>();
        idFieldMap.put(CdmBaseType.DESCRIPTION_ELEMENT, "inDescription.taxon.id");

        // --- initialize taxa, highlight matches ....
        ISearchResultBuilder searchResultBuilder = new SearchResultBuilder(luceneSearch, luceneSearch.getQuery());
        @SuppressWarnings("rawtypes")
        List<SearchResult<TaxonBase>> searchResults = searchResultBuilder.createResultSet(
                topDocsResultSet, luceneSearch.getHighlightFields(), dao, idFieldMap, propertyPaths);

        int totalHits = topDocsResultSet != null ? topDocsResultSet.totalGroupCount : 0;
        return new DefaultPagerImpl<>(pageNumber, totalHits, pageSize, searchResults);

    }


    @Override
    public Pager<SearchResult<TaxonBase>> findByEverythingFullText(String queryString,
            Classification classification, List<Language> languages, boolean highlightFragments,
            Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) throws IOException, LuceneParseException, LuceneMultiSearchException {

        LuceneSearch luceneSearchByDescriptionElement = prepareByDescriptionElementFullTextSearch(null, queryString, classification, null, languages, highlightFragments);
        LuceneSearch luceneSearchByTaxonBase = prepareFindByFullTextSearch(null, queryString, classification, languages, highlightFragments, null);

        LuceneMultiSearch multiSearch = new LuceneMultiSearch(luceneIndexToolProvider, luceneSearchByDescriptionElement, luceneSearchByTaxonBase);

        // --- execute search
        TopGroups<BytesRef> topDocsResultSet;
        try {
            topDocsResultSet = multiSearch.executeSearch(pageSize, pageNumber);
        } catch (ParseException e) {
            LuceneParseException luceneParseException = new LuceneParseException(e.getMessage());
            luceneParseException.setStackTrace(e.getStackTrace());
            throw luceneParseException;
        }

        // --- initialize taxa, highlight matches ....
        ISearchResultBuilder searchResultBuilder = new SearchResultBuilder(multiSearch, multiSearch.getQuery());

        Map<CdmBaseType, String> idFieldMap = new HashMap<>();
        idFieldMap.put(CdmBaseType.TAXON, "id");
        idFieldMap.put(CdmBaseType.DESCRIPTION_ELEMENT, "inDescription.taxon.id");

        List<SearchResult<TaxonBase>> searchResults = searchResultBuilder.createResultSet(
                topDocsResultSet, multiSearch.getHighlightFields(), dao, idFieldMap, propertyPaths);

        int totalHits = topDocsResultSet != null ? topDocsResultSet.totalGroupCount : 0;
        return new DefaultPagerImpl<>(pageNumber, totalHits, pageSize, searchResults);

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

        SortField[] sortFields = new  SortField[]{SortField.FIELD_SCORE, new SortField("inDescription.taxon.titleCache__sort", SortField.Type.STRING, false)};

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
        Builder finalQueryBuilder = new Builder();
        Builder textQueryBuilder = new Builder();
        textQueryBuilder.add(descriptionElementQueryFactory.newTermQuery("titleCache", queryString), Occur.SHOULD);

        // common name
        Builder nameQueryBuilder = new Builder();
        if(languages == null || languages.size() == 0){
            nameQueryBuilder.add(descriptionElementQueryFactory.newTermQuery("name", queryString), Occur.MUST);
        } else {
            Builder languageSubQueryBuilder = new Builder();
            for(Language lang : languages){
                languageSubQueryBuilder.add(descriptionElementQueryFactory.newTermQuery("language.uuid",  lang.getUuid().toString(), false), Occur.SHOULD);
            }
            nameQueryBuilder.add(descriptionElementQueryFactory.newTermQuery("name", queryString), Occur.MUST);
            nameQueryBuilder.add(languageSubQueryBuilder.build(), Occur.MUST);
        }
        textQueryBuilder.add(nameQueryBuilder.build(), Occur.SHOULD);


        // text field from TextData
        textQueryBuilder.add(descriptionElementQueryFactory.newMultilanguageTextQuery("text", queryString, languages), Occur.SHOULD);

        // --- TermBase fields - by representation ----
        // state field from CategoricalData
        textQueryBuilder.add(descriptionElementQueryFactory.newDefinedTermQuery("stateData.state", queryString, languages), Occur.SHOULD);

        // state field from CategoricalData
        textQueryBuilder.add(descriptionElementQueryFactory.newDefinedTermQuery("stateData.modifyingText", queryString, languages), Occur.SHOULD);

        // area field from Distribution
        textQueryBuilder.add(descriptionElementQueryFactory.newDefinedTermQuery("area", queryString, languages), Occur.SHOULD);

        // status field from Distribution
        textQueryBuilder.add(descriptionElementQueryFactory.newDefinedTermQuery("status", queryString, languages), Occur.SHOULD);

        finalQueryBuilder.add(textQueryBuilder.build(), Occur.MUST);
        // --- classification ----

        if(classification != null){
            finalQueryBuilder.add(descriptionElementQueryFactory.newEntityIdQuery("inDescription.taxon.taxonNodes.classification.id", classification), Occur.MUST);
        }

        // --- IdentifieableEntity fields - by uuid
        if(features != null && features.size() > 0 ){
            finalQueryBuilder.add(descriptionElementQueryFactory.newEntityUuidsQuery("feature.uuid", features), Occur.MUST);
        }

        // the description must be associated with a taxon
        finalQueryBuilder.add(descriptionElementQueryFactory.newIsNotNullQuery("inDescription.taxon.id"), Occur.MUST);

        BooleanQuery finalQuery = finalQueryBuilder.build();
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
    public List<Synonym> createInferredSynonyms(Taxon taxon, Classification classification, SynonymType type, boolean doWithMisappliedNames){
        List <Synonym> inferredSynonyms = new ArrayList<>();
        List<Synonym> inferredSynonymsToBeRemoved = new ArrayList<>();

        HashMap <UUID, IZoologicalName> zooHashMap = new HashMap<>();


        UUID nameUuid= taxon.getName().getUuid();
        IZoologicalName taxonName = getZoologicalName(nameUuid, zooHashMap);
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
        List<String> taxonNames = new ArrayList<>();

        for (TaxonNode node: nodes){
           // Map<String, String> synonymsGenus = new HashMap<>(); // Changed this to be able to store the idInSource to a genusName
           // List<String> synonymsEpithet = new ArrayList<>();

            if (node.getClassification().equals(classification)){
                if (!node.isTopmostNode()){
                    TaxonNode parent = node.getParent();
                    parent = CdmBase.deproxy(parent);
                    TaxonName parentName =  parent.getTaxon().getName();
                    IZoologicalName zooParentName = CdmBase.deproxy(parentName);
                    Taxon parentTaxon = CdmBase.deproxy(parent.getTaxon());
                    Rank rankOfTaxon = taxonName.getRank();


                    //create inferred synonyms for species, subspecies
                    if ((parentName.isGenus() || parentName.isSpecies() || parentName.getRank().equals(Rank.SUBGENUS())) ){

                        Synonym inferredEpithet = null;
                        Synonym inferredGenus = null;
                        Synonym potentialCombination = null;

                        List<String> propertyPaths = new ArrayList<>();
                        propertyPaths.add("synonym");
                        propertyPaths.add("synonym.name");
                        List<OrderHint> orderHintsSynonyms = new ArrayList<>();
                        orderHintsSynonyms.add(new OrderHint("titleCache", SortOrder.ASCENDING));

                        List<Synonym> synonyMsOfParent = dao.getSynonyms(parentTaxon, SynonymType.HETEROTYPIC_SYNONYM_OF(), null, null,orderHintsSynonyms,propertyPaths);
                        List<Synonym> synonymsOfTaxon= dao.getSynonyms(taxon, SynonymType.HETEROTYPIC_SYNONYM_OF(),
                                null, null,orderHintsSynonyms,propertyPaths);

                        List<TaxonRelationship> taxonRelListParent = new ArrayList<>();
                        List<TaxonRelationship> taxonRelListTaxon = new ArrayList<>();
                        if (doWithMisappliedNames){
                            List<OrderHint> orderHintsMisapplied = new ArrayList<>();
                            orderHintsMisapplied.add(new OrderHint("relatedFrom.titleCache", SortOrder.ASCENDING));
                            taxonRelListParent = dao.getTaxonRelationships(parentTaxon, TaxonRelationshipType.MISAPPLIED_NAME_FOR(),
                                    null, null, orderHintsMisapplied, propertyPaths, Direction.relatedTo);
                            taxonRelListTaxon = dao.getTaxonRelationships(taxon, TaxonRelationshipType.MISAPPLIED_NAME_FOR(),
                                    null, null, orderHintsMisapplied, propertyPaths, Direction.relatedTo);
                        }

                        if (type.equals(SynonymType.INFERRED_EPITHET_OF())){
                            for (Synonym synonymRelationOfParent:synonyMsOfParent){

                                inferredEpithet = createInferredEpithets(taxon,
                                        zooHashMap, taxonName, epithetOfTaxon,
                                        infragenericEpithetOfTaxon,
                                        infraspecificEpithetOfTaxon,
                                        taxonNames, parentName,
                                        synonymRelationOfParent);

                                inferredSynonyms.add(inferredEpithet);
                                zooHashMap.put(inferredEpithet.getName().getUuid(), inferredEpithet.getName());
                                taxonNames.add(inferredEpithet.getName().getNameCache());
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
                                    zooHashMap.put(inferredEpithet.getName().getUuid(), inferredEpithet.getName());
                                    taxonNames.add(inferredEpithet.getName().getNameCache());
                                }
                            }

                            if (!taxonNames.isEmpty()){
                            List<String> synNotInCDM = dao.taxaByNameNotInDB(taxonNames);
                            IZoologicalName name;
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

                    }else if (type.equals(SynonymType.INFERRED_GENUS_OF())){

                        for (Synonym synonymRelationOfTaxon:synonymsOfTaxon){

                            inferredGenus = createInferredGenus(taxon,
                                    zooHashMap, taxonName, epithetOfTaxon,
                                    genusOfTaxon, taxonNames, zooParentName, synonymRelationOfTaxon);

                            inferredSynonyms.add(inferredGenus);
                            zooHashMap.put(inferredGenus.getName().getUuid(), inferredGenus.getName());
                            taxonNames.add(inferredGenus.getName().getNameCache());
                        }

                        if (doWithMisappliedNames){

                            for (TaxonRelationship taxonRelationship: taxonRelListTaxon){
                                Taxon misappliedName = taxonRelationship.getFromTaxon();
                                inferredGenus = createInferredGenus(taxon, zooHashMap, taxonName, infraspecificEpithetOfTaxon, genusOfTaxon, taxonNames, zooParentName,  misappliedName);

                                inferredSynonyms.add(inferredGenus);
                                zooHashMap.put(inferredGenus.getName().getUuid(), inferredGenus.getName());
                                 taxonNames.add(inferredGenus.getName().getNameCache());
                            }
                        }


                        if (!taxonNames.isEmpty()){
                            List<String> synNotInCDM = dao.taxaByNameNotInDB(taxonNames);
                            IZoologicalName name;
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

                    }else if (type.equals(SynonymType.POTENTIAL_COMBINATION_OF())){

                        Reference sourceReference = null; // TODO: Determination of sourceReference is redundant
                        IZoologicalName inferredSynName;
                        //for all synonyms of the parent...
                        for (Synonym synonymRelationOfParent:synonyMsOfParent){
                            TaxonName synName;
                            HibernateProxyHelper.deproxy(synonymRelationOfParent);

                            synName = synonymRelationOfParent.getName();

                            // Set the sourceReference
                            sourceReference = synonymRelationOfParent.getSec();

                            // Determine the idInSource
                            String idInSourceParent = getIdInSource(synonymRelationOfParent);

                            IZoologicalName parentSynZooName = getZoologicalName(synName.getUuid(), zooHashMap);
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

                            for (Synonym synonymRelationOfTaxon:synonymsOfTaxon){

                                IZoologicalName zooSynName = getZoologicalName(synonymRelationOfTaxon.getName().getUuid(), zooHashMap);
                                potentialCombination = createPotentialCombination(idInSourceParent, parentSynZooName, zooSynName,
                                        synParentGenus,
                                        synParentInfragenericName,
                                        synParentSpecificEpithet, synonymRelationOfTaxon, zooHashMap);

                                taxon.addSynonym(potentialCombination, SynonymType.POTENTIAL_COMBINATION_OF());
                                inferredSynonyms.add(potentialCombination);
                                zooHashMap.put(potentialCombination.getName().getUuid(), potentialCombination.getName());
                                 taxonNames.add(potentialCombination.getName().getNameCache());

                            }

                        }

                        if (doWithMisappliedNames){

                            for (TaxonRelationship parentRelationship: taxonRelListParent){

                                TaxonName misappliedParentName;

                                Taxon misappliedParent = parentRelationship.getFromTaxon();
                                misappliedParentName = misappliedParent.getName();

                                HibernateProxyHelper.deproxy(misappliedParent);

                                // Set the sourceReference
                                sourceReference = misappliedParent.getSec();

                                // Determine the idInSource
                                String idInSourceParent = getIdInSource(misappliedParent);

                                IZoologicalName parentSynZooName = getZoologicalName(misappliedParentName.getUuid(), zooHashMap);
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
                                    IZoologicalName zooMisappliedName = getZoologicalName(misappliedName.getName().getUuid(), zooHashMap);
                                    potentialCombination = createPotentialCombination(
                                            idInSourceParent, parentSynZooName, zooMisappliedName,
                                            synParentGenus,
                                            synParentInfragenericName,
                                            synParentSpecificEpithet, misappliedName, zooHashMap);


                                    taxon.addSynonym(potentialCombination, SynonymType.POTENTIAL_COMBINATION_OF());
                                    inferredSynonyms.add(potentialCombination);
                                    zooHashMap.put(potentialCombination.getName().getUuid(), potentialCombination.getName());
                                     taxonNames.add(potentialCombination.getName().getNameCache());
                                }
                            }
                        }

                        if (!taxonNames.isEmpty()){
                            List<String> synNotInCDM = dao.taxaByNameNotInDB(taxonNames);
                            IZoologicalName name;
                            if (!synNotInCDM.isEmpty()){
                                inferredSynonymsToBeRemoved.clear();
                                for (Synonym syn :inferredSynonyms){
                                    try{
                                        name = syn.getName();
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
                        logger.info("The synonym type is not defined.");
                        return inferredSynonyms;
                    }
                }
            }

        }

        return inferredSynonyms;
    }

    private Synonym createPotentialCombination(String idInSourceParent,
            IZoologicalName parentSynZooName, 	IZoologicalName zooSynName, String synParentGenus,
            String synParentInfragenericName, String synParentSpecificEpithet,
            TaxonBase<?> syn, Map<UUID, IZoologicalName> zooHashMap) {
        Synonym potentialCombination;
        Reference sourceReference;
        IZoologicalName inferredSynName;
        HibernateProxyHelper.deproxy(syn);

        // Set sourceReference
        sourceReference = syn.getSec();
        if (sourceReference == null){
            logger.warn("The synonym has no sec reference because it is a misapplied name! Take the sec reference of taxon");
            //TODO:Remove
            if (!parentSynZooName.getTaxa().isEmpty()){
                TaxonBase<?> taxon = parentSynZooName.getTaxa().iterator().next();

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
        inferredSynName = TaxonNameFactory.NewZoologicalInstance(syn.getName().getRank());

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
            Map<UUID, IZoologicalName> zooHashMap, IZoologicalName taxonName,
            String epithetOfTaxon, String genusOfTaxon,
            List<String> taxonNames, IZoologicalName zooParentName,
            TaxonBase syn) {

        Synonym inferredGenus;
        TaxonName synName;
        IZoologicalName inferredSynName;
        synName =syn.getName();
        HibernateProxyHelper.deproxy(syn);

        // Determine the idInSource
        String idInSourceSyn = getIdInSource(syn);
        String idInSourceTaxon = getIdInSource(taxon);
        // Determine the sourceReference
        Reference sourceReference = syn.getSec();

        //logger.warn(sourceReference.getTitleCache());

        synName = syn.getName();
        IZoologicalName synZooName = getZoologicalName(synName.getUuid(), zooHashMap);
        String synSpeciesEpithetName = synZooName.getSpecificEpithet();
                     /* if (synonymsEpithet != null && !synonymsEpithet.contains(synSpeciesEpithetName)){
            synonymsEpithet.add(synSpeciesEpithetName);
        }*/

        inferredSynName = TaxonNameFactory.NewZoologicalInstance(taxon.getName().getRank());
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

        taxon.addSynonym(inferredGenus, SynonymType.INFERRED_GENUS_OF());

        return inferredGenus;
    }

    private Synonym createInferredEpithets(Taxon taxon,
            Map<UUID, IZoologicalName> zooHashMap, IZoologicalName taxonName,
            String epithetOfTaxon, String infragenericEpithetOfTaxon,
            String infraspecificEpithetOfTaxon, List<String> taxonNames,
            TaxonName parentName, TaxonBase<?> syn) {

        Synonym inferredEpithet;
        TaxonName synName;
        IZoologicalName inferredSynName;
        HibernateProxyHelper.deproxy(syn);

        // Determine the idInSource
        String idInSourceSyn = getIdInSource(syn);
        String idInSourceTaxon =  getIdInSource(taxon);
        // Determine the sourceReference
        Reference sourceReference = syn.getSec();

        if (sourceReference == null){
             logger.warn("The synonym has no sec reference because it is a misapplied name! Take the sec reference of taxon" + taxon.getSec());
             sourceReference = taxon.getSec();
        }

        synName = syn.getName();
        IZoologicalName zooSynName = getZoologicalName(synName.getUuid(), zooHashMap);
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

        inferredSynName = TaxonNameFactory.NewZoologicalInstance(taxon.getName().getRank());

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



        taxon.addSynonym(inferredEpithet, SynonymType.INFERRED_EPITHET_OF());

        return inferredEpithet;
    }

    /**
     * Returns an existing IZoologicalName or extends an internal hashmap if it does not exist.
     * Very likely only useful for createInferredSynonyms().
     * @param uuid
     * @param zooHashMap
     * @return
     */
    private IZoologicalName getZoologicalName(UUID uuid, Map <UUID, IZoologicalName> zooHashMap) {
        IZoologicalName taxonName =nameDao.findZoologicalNameByUUID(uuid);
        if (taxonName == null) {
            taxonName = zooHashMap.get(uuid);
        }
        return taxonName;
    }

    /**
     * Returns the idInSource for a given Synonym.
     * @param syn
     */
    private String getIdInSource(TaxonBase<?> taxonBase) {
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
        List <Synonym> inferredSynonyms = new ArrayList<>();

        inferredSynonyms.addAll(createInferredSynonyms(taxon, tree, SynonymType.INFERRED_EPITHET_OF(), doWithMisappliedNames));
        inferredSynonyms.addAll(createInferredSynonyms(taxon, tree, SynonymType.INFERRED_GENUS_OF(), doWithMisappliedNames));
        inferredSynonyms.addAll(createInferredSynonyms(taxon, tree, SynonymType.POTENTIAL_COMBINATION_OF(), doWithMisappliedNames));

        return inferredSynonyms;
    }

    @Override
    public List<Classification> listClassifications(TaxonBase taxonBase, Integer limit, Integer start, List<String> propertyPaths) {

        // TODO quickly implemented, create according dao !!!!
        Set<TaxonNode> nodes = new HashSet<>();
        Set<Classification> classifications = new HashSet<>();
        List<Classification> list = new ArrayList<>();

        if (taxonBase == null) {
            return list;
        }

        taxonBase = load(taxonBase.getUuid());

        if (taxonBase instanceof Taxon) {
            nodes.addAll(((Taxon)taxonBase).getTaxonNodes());
        } else {
            Taxon taxon = ((Synonym)taxonBase).getAcceptedTaxon();
            if (taxon != null){
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
            SynonymType synonymType) throws DataChangeNoRollbackException {
        UpdateResult result = new UpdateResult();
        Taxon fromTaxon = (Taxon) dao.load(fromTaxonUuid);
        Taxon toTaxon = (Taxon) dao.load(toTaxonUuid);
        result = changeRelatedTaxonToSynonym(fromTaxon, toTaxon, oldRelationshipType, synonymType);

        result.addUpdatedObject(fromTaxon);
        result.addUpdatedObject(toTaxon);
        result.addUpdatedObject(result.getCdmEntity());

        return result;
    }

    @Override
    @Transactional(readOnly = false)
    public UpdateResult changeRelatedTaxonToSynonym(Taxon fromTaxon, Taxon toTaxon, TaxonRelationshipType oldRelationshipType,
            SynonymType synonymType) throws DataChangeNoRollbackException {

        UpdateResult result = new UpdateResult();
        // Create new synonym using concept name
        TaxonName synonymName = fromTaxon.getName();

        // Remove concept relation from taxon
        toTaxon.removeTaxon(fromTaxon, oldRelationshipType);

        // Create a new synonym for the taxon
        Synonym synonym;
        if (synonymType != null
                && synonymType.equals(SynonymType.HOMOTYPIC_SYNONYM_OF())){
            synonym = Synonym.NewInstance(synonymName, fromTaxon.getSec());
            toTaxon.addHomotypicSynonym(synonym);
        } else{
            synonym = toTaxon.addHeterotypicSynonymName(synonymName);
        }

        this.saveOrUpdate(toTaxon);
        //TODO: configurator and classification
        TaxonDeletionConfigurator config = new TaxonDeletionConfigurator();
        config.setDeleteNameIfPossible(false);
        result.includeResult(this.deleteTaxon(fromTaxon.getUuid(), config, null));
        result.setCdmEntity(synonym);
        result.addUpdatedObject(toTaxon);
        result.addUpdatedObject(synonym);
        return result;
    }

    @Override
    public DeleteResult isDeletable(UUID taxonBaseUuid, DeleteConfiguratorBase config){
        DeleteResult result = new DeleteResult();
        TaxonBase<?> taxonBase = load(taxonBaseUuid);
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
            if (!(ref instanceof Taxon || ref instanceof TaxonName )){
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
            if (!(ref instanceof TaxonName)){
            	message = null;
                if (!config.isDeleteSynonymRelations() && (ref instanceof Synonym)){
                    message = "The taxon can't be deleted as long as it has synonyms.";
                }
                if (!config.isDeleteDescriptions() && (ref instanceof DescriptionBase)){
                    message = "The taxon can't be deleted as long as it has factual data.";
                }

                if (!config.isDeleteTaxonNodes() && (ref instanceof TaxonNode)){
                    message = "The taxon can't be deleted as long as it belongs to a taxon node.";
                }
                if (!config.isDeleteTaxonRelationships() && (ref instanceof TaxonRelationship)){
                    if (!config.isDeleteMisappliedNamesAndInvalidDesignations() &&
                            (((TaxonRelationship)ref).getType().isMisappliedNameOrInvalidDesignation())){
                        message = "The taxon can't be deleted as long as it has misapplied names or invalid designations.";
                    } else{
                        message = "The taxon can't be deleted as long as it belongs to taxon relationship.";
                    }
                }
                if (ref instanceof PolytomousKeyNode){
                    message = "The taxon can't be deleted as long as it is referenced by a polytomous key node.";
                }

                if (HibernateProxyHelper.isInstanceOf(ref, IIdentificationKey.class)){
                   message = "Taxon can't be deleted as it is used in an identification key. Remove from identification key prior to deleting this taxon";
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

        Set<Taxon> taxa = new HashSet<>();
        TaxonBase<?> taxonBase = find(taxonUuid);
        if (taxonBase == null){
            return new IncludedTaxaDTO();
        }else if (taxonBase.isInstanceOf(Taxon.class)){
            Taxon taxon = CdmBase.deproxy(taxonBase, Taxon.class);
            taxa.add(taxon);
        }else if (taxonBase.isInstanceOf(Synonym.class)){
            //TODO partial synonyms ??
            //TODO synonyms in general
            Synonym syn = CdmBase.deproxy(taxonBase, Synonym.class);
            taxa.add(syn.getAcceptedTaxon());
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
        Set<TaxonNode> taxonNodes = new HashSet<>();
        for (Taxon taxon: uncheckedTaxa){
            taxonNodes.addAll(taxon.getTaxonNodes());
        }

        Set<Taxon> children = new HashSet<>();
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
                existingTaxa.addIncludedTaxon(uuid, new ArrayList<>(), false);
            }
        }

        //concept relations
        Set<Taxon> uncheckedAndChildren = new HashSet<>(uncheckedTaxa);
        uncheckedAndChildren.addAll(children);

        Set<Taxon> relatedTaxa = makeConceptIncludedTaxa(uncheckedAndChildren, existingTaxa, config);


        Set<Taxon> result = new HashSet<>(relatedTaxa);
        return result;
    }

    /**
     * Computes all conceptually congruent or included taxa and adds them to the existingTaxa data structure.
     * @return the set of these computed taxa
     */
    private Set<Taxon> makeConceptIncludedTaxa(Set<Taxon> unchecked, IncludedTaxaDTO existingTaxa, IncludedTaxonConfiguration config) {
        Set<Taxon> result = new HashSet<>();

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
                existingTaxa.addIncludedTaxon(uuid, new ArrayList<>(), false);
            }
        }
        return result;
    }

    @Override
    public List<TaxonBase> findTaxaByName(MatchingTaxonConfigurator config){
        List<TaxonBase> taxonList = dao.getTaxaByName(true, config.isIncludeSynonyms(), false, false, false,
                config.getTaxonNameTitle(), null, MatchMode.EXACT, null, null, 0, 0, config.getPropertyPath());
        return taxonList;
    }

	@Override
	@Transactional(readOnly = true)
	public <S extends TaxonBase> Pager<IdentifiedEntityDTO<S>> findByIdentifier(
			Class<S> clazz, String identifier, DefinedTerm identifierType, TaxonNode subtreeFilter,
			MatchMode matchmode, boolean includeEntity, Integer pageSize,
			Integer pageNumber,	List<String> propertyPaths) {
		if (subtreeFilter == null){
			return findByIdentifier(clazz, identifier, identifierType, matchmode, includeEntity, pageSize, pageNumber, propertyPaths);
		}

		Integer numberOfResults = dao.countByIdentifier(clazz, identifier, identifierType, subtreeFilter, matchmode);
        List<Object[]> daoResults = new ArrayList<>();
        if(numberOfResults > 0) { // no point checking again
        	daoResults = dao.findByIdentifier(clazz, identifier, identifierType, subtreeFilter,
    				matchmode, includeEntity, pageSize, pageNumber, propertyPaths);
        }

        List<IdentifiedEntityDTO<S>> result = new ArrayList<>();
        for (Object[] daoObj : daoResults){
        	if (includeEntity){
        		result.add(new IdentifiedEntityDTO<S>((DefinedTerm)daoObj[0], (String)daoObj[1], (S)daoObj[2]));
        	}else{
        		result.add(new IdentifiedEntityDTO<S>((DefinedTerm)daoObj[0], (String)daoObj[1], (UUID)daoObj[2], (String)daoObj[3], null));
        	}
        }
		return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, result);
	}

	@Override
    @Transactional(readOnly = true)
    public <S extends TaxonBase> Pager<MarkedEntityDTO<S>> findByMarker(
            Class<S> clazz, MarkerType markerType, Boolean markerValue,
            TaxonNode subtreeFilter, boolean includeEntity, TaxonTitleType titleType,
            Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        if (subtreeFilter == null){
            return super.findByMarker (clazz, markerType, markerValue, includeEntity, pageSize, pageNumber, propertyPaths);
        }

        Long numberOfResults = dao.countByMarker(clazz, markerType, markerValue, subtreeFilter);
        List<Object[]> daoResults = new ArrayList<>();
        if(numberOfResults > 0) { // no point checking again
            daoResults = dao.findByMarker(clazz, markerType, markerValue, subtreeFilter,
                    includeEntity, titleType, pageSize, pageNumber, propertyPaths);
        }

        List<MarkedEntityDTO<S>> result = new ArrayList<>();
        for (Object[] daoObj : daoResults){
            if (includeEntity){
                result.add(new MarkedEntityDTO<S>((MarkerType)daoObj[0], (Boolean)daoObj[1], (S)daoObj[2]));
            }else{
                result.add(new MarkedEntityDTO<S>((MarkerType)daoObj[0], (Boolean)daoObj[1], (UUID)daoObj[2], (String)daoObj[3]));
            }
        }
        return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, result);
    }

    @Override
	@Transactional(readOnly = false)
	public UpdateResult moveSynonymToAnotherTaxon(Synonym oldSynonym, UUID newTaxonUUID, boolean moveHomotypicGroup,
            SynonymType newSynonymType, Reference newSecundum, String newSecundumDetail,
            boolean keepSecundumIfUndefined) throws HomotypicalGroupChangeException {

	    UpdateResult result = new UpdateResult();
		Taxon newTaxon = CdmBase.deproxy(dao.load(newTaxonUUID),Taxon.class);
		result = moveSynonymToAnotherTaxon(oldSynonym, newTaxon, moveHomotypicGroup, newSynonymType,
		        newSecundum, newSecundumDetail, keepSecundumIfUndefined);

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
	@Transactional(readOnly = false)
	public UpdateResult swapSynonymAndAcceptedTaxon(UUID synonymUUid,
			UUID acceptedTaxonUuid) {
		TaxonBase<?> base = this.load(synonymUUid);
		Synonym syn = HibernateProxyHelper.deproxy(base, Synonym.class);
		base = this.load(acceptedTaxonUuid);
		Taxon taxon = HibernateProxyHelper.deproxy(base, Taxon.class);

		return this.swapSynonymAndAcceptedTaxon(syn, taxon);
	}


}
