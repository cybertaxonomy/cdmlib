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
import java.util.Arrays;
import java.util.Collection;
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
import org.apache.lucene.search.SortField;
import org.hibernate.TransientObjectException;
import org.hibernate.search.spatial.impl.Rectangle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeConfigurator;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeNotSupportedException;
import eu.etaxonomy.cdm.api.service.molecular.ISequenceService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.api.service.search.ILuceneIndexToolProvider;
import eu.etaxonomy.cdm.api.service.search.ISearchResultBuilder;
import eu.etaxonomy.cdm.api.service.search.LuceneSearch;
import eu.etaxonomy.cdm.api.service.search.LuceneSearch.TopGroupsWithMaxScore;
import eu.etaxonomy.cdm.api.service.search.QueryFactory;
import eu.etaxonomy.cdm.api.service.search.SearchResult;
import eu.etaxonomy.cdm.api.service.search.SearchResultBuilder;
import eu.etaxonomy.cdm.api.service.util.TaxonRelationshipEdge;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.CdmBaseType;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.initializer.AbstractBeanInitializer;
import eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

/**
 * @author a.babadshanjan
 * @created 01.09.2008
 */
@Service
@Transactional(readOnly = true)
public class OccurrenceServiceImpl extends IdentifiableServiceBase<SpecimenOrObservationBase,IOccurrenceDao> implements IOccurrenceService {

    static private final Logger logger = Logger.getLogger(OccurrenceServiceImpl.class);

    @Autowired
    private IDefinedTermDao definedTermDao;

    @Autowired
    private IDescriptionService descriptionService;

    @Autowired
    private ITaxonService taxonService;

    @Autowired
    private ISequenceService sequenceService;

    @Autowired
    private AbstractBeanInitializer beanInitializer;

    @Autowired
    private ITaxonDao taxonDao;

    @Autowired
    private ILuceneIndexToolProvider luceneIndexToolProvider;


    public OccurrenceServiceImpl() {
        logger.debug("Load OccurrenceService Bean");
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IIdentifiableEntityService#updateTitleCache(java.lang.Integer, eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy)
     */
    @Override
    @Transactional(readOnly = false)
    public void updateTitleCache(Class<? extends SpecimenOrObservationBase> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<SpecimenOrObservationBase> cacheStrategy, IProgressMonitor monitor) {
        if (clazz == null){
            clazz = SpecimenOrObservationBase.class;
        }
        super.updateTitleCacheImpl(clazz, stepSize, cacheStrategy, monitor);
    }


    /**
     * FIXME Candidate for harmonization
     * move to termService
     */
    @Override
    public Country getCountryByIso(String iso639) {
        return this.definedTermDao.getCountryByIso(iso639);

    }

    /**
     * FIXME Candidate for harmonization
     * move to termService
     */
    @Override
    public List<Country> getCountryByName(String name) {
        List<? extends DefinedTermBase> terms = this.definedTermDao.findByTitle(Country.class, name, null, null, null, null, null, null) ;
        List<Country> countries = new ArrayList<Country>();
        for (int i=0;i<terms.size();i++){
            countries.add((Country)terms.get(i));
        }
        return countries;
    }

    @Override
    @Autowired
    protected void setDao(IOccurrenceDao dao) {
        this.dao = dao;
    }

    @Override
    public Pager<DerivationEvent> getDerivationEvents(SpecimenOrObservationBase occurence, Integer pageSize,Integer pageNumber, List<String> propertyPaths) {
        Integer numberOfResults = dao.countDerivationEvents(occurence);

        List<DerivationEvent> results = new ArrayList<DerivationEvent>();
        if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
            results = dao.getDerivationEvents(occurence, pageSize, pageNumber,propertyPaths);
        }

        return new DefaultPagerImpl<DerivationEvent>(pageNumber, numberOfResults, pageSize, results);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IOccurrenceService#countDeterminations(eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase, eu.etaxonomy.cdm.model.taxon.TaxonBase)
     */
    @Override
    public int countDeterminations(SpecimenOrObservationBase occurence, TaxonBase taxonbase) {
        return dao.countDeterminations(occurence, taxonbase);
    }

    @Override
    public Pager<DeterminationEvent> getDeterminations(SpecimenOrObservationBase occurrence, TaxonBase taxonBase, Integer pageSize,Integer pageNumber, List<String> propertyPaths) {
        Integer numberOfResults = dao.countDeterminations(occurrence, taxonBase);

        List<DeterminationEvent> results = new ArrayList<DeterminationEvent>();
        if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
            results = dao.getDeterminations(occurrence,taxonBase, pageSize, pageNumber, propertyPaths);
        }

        return new DefaultPagerImpl<DeterminationEvent>(pageNumber, numberOfResults, pageSize, results);
    }

    @Override
    public Pager<Media> getMedia(SpecimenOrObservationBase occurence,Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        Integer numberOfResults = dao.countMedia(occurence);

        List<Media> results = new ArrayList<Media>();
        if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
            results = dao.getMedia(occurence, pageSize, pageNumber, propertyPaths);
        }

        return new DefaultPagerImpl<Media>(pageNumber, numberOfResults, pageSize, results);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IOccurrenceService#list(java.lang.Class, eu.etaxonomy.cdm.model.taxon.TaxonBase, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    @Override
    public Pager<SpecimenOrObservationBase> list(Class<? extends SpecimenOrObservationBase> type, TaxonBase determinedAs, Integer pageSize, Integer pageNumber,	List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.count(type,determinedAs);
        List<SpecimenOrObservationBase> results = new ArrayList<SpecimenOrObservationBase>();
        pageNumber = pageNumber == null ? 0 : pageNumber;
        if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
            Integer start = pageSize == null ? 0 : pageSize * pageNumber;
            results = dao.list(type,determinedAs, pageSize, start, orderHints,propertyPaths);
        }
        return new DefaultPagerImpl<SpecimenOrObservationBase>(pageNumber, numberOfResults, pageSize, results);
    }

    @Override
    public List<UuidAndTitleCache<DerivedUnit>> getDerivedUnitUuidAndTitleCache() {
        return dao.getDerivedUnitUuidAndTitleCache();
    }

    @Override
    public List<UuidAndTitleCache<FieldUnit>> getFieldUnitUuidAndTitleCache() {
        return dao.getFieldUnitUuidAndTitleCache();
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IOccurrenceService#getDerivedUnitFacade(eu.etaxonomy.cdm.model.occurrence.DerivedUnit)
     */
    @Override
    public DerivedUnitFacade getDerivedUnitFacade(DerivedUnit derivedUnit, List<String> propertyPaths) throws DerivedUnitFacadeNotSupportedException {
        derivedUnit = (DerivedUnit)dao.load(derivedUnit.getUuid(), null);
        DerivedUnitFacadeConfigurator config = DerivedUnitFacadeConfigurator.NewInstance();
        config.setThrowExceptionForNonSpecimenPreservationMethodRequest(false);
        DerivedUnitFacade derivedUnitFacade = DerivedUnitFacade.NewInstance(derivedUnit, config);
        beanInitializer.initialize(derivedUnitFacade, propertyPaths);
        return derivedUnitFacade;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IOccurrenceService#listDerivedUnitFacades(eu.etaxonomy.cdm.model.description.DescriptionBase, java.util.List)
     */
    @Override
    public List<DerivedUnitFacade> listDerivedUnitFacades(
            DescriptionBase description, List<String> propertyPaths) {

        List<DerivedUnitFacade> derivedUnitFacadeList = new ArrayList<DerivedUnitFacade>();
        IndividualsAssociation tempIndividualsAssociation;
        SpecimenOrObservationBase tempSpecimenOrObservationBase;
        List<DescriptionElementBase> elements = descriptionService.listDescriptionElements(description, null, IndividualsAssociation.class, null, 0, Arrays.asList(new String []{"associatedSpecimenOrObservation"}));
        for(DescriptionElementBase element : elements){
            if(element instanceof IndividualsAssociation){
                tempIndividualsAssociation = (IndividualsAssociation)element;
                if(tempIndividualsAssociation.getAssociatedSpecimenOrObservation() != null){
                    tempSpecimenOrObservationBase = HibernateProxyHelper.deproxy(tempIndividualsAssociation.getAssociatedSpecimenOrObservation(), SpecimenOrObservationBase.class);
                    if(tempSpecimenOrObservationBase instanceof DerivedUnit){
                        try {
                            derivedUnitFacadeList.add(DerivedUnitFacade.NewInstance((DerivedUnit)tempSpecimenOrObservationBase));
                        } catch (DerivedUnitFacadeNotSupportedException e) {
                            logger.warn(tempIndividualsAssociation.getAssociatedSpecimenOrObservation().getTitleCache() + " : " +e.getMessage());
                        }
                    }
                }

            }
        }

        beanInitializer.initializeAll(derivedUnitFacadeList, propertyPaths);

        return derivedUnitFacadeList;
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IOccurrenceService#listByAnyAssociation(java.lang.Class, java.util.Set, eu.etaxonomy.cdm.model.taxon.Taxon, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    @Override
    public <T extends SpecimenOrObservationBase> List<T> listByAssociatedTaxon(Class<T> type, Set<TaxonRelationshipEdge> includeRelationships,
            Taxon associatedTaxon, Integer maxDepth, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {

        return pageByAssociatedTaxon(type, includeRelationships, associatedTaxon, maxDepth, pageSize, pageNumber, orderHints, propertyPaths).getRecords();
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IOccurrenceService#pageByAssociatedTaxon(java.lang.Class, java.util.Set, eu.etaxonomy.cdm.model.taxon.Taxon, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends SpecimenOrObservationBase> Pager<T> pageByAssociatedTaxon(Class<T> type, Set<TaxonRelationshipEdge> includeRelationships,
            Taxon associatedTaxon, Integer maxDepth, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {

        Set<Taxon> taxa = new HashSet<Taxon>();
        Set<Integer> occurrenceIds = new HashSet<Integer>();
        List<T> occurrences = new ArrayList<T>();

//        Integer limit = PagerUtils.limitFor(pageSize);
//        Integer start = PagerUtils.startFor(pageSize, pageNumber);

        associatedTaxon = (Taxon) taxonDao.load(associatedTaxon.getUuid());

        if(includeRelationships != null) {
            taxa = taxonService.listRelatedTaxa(associatedTaxon, includeRelationships, maxDepth, null, null, propertyPaths);
        }

        taxa.add(associatedTaxon);

        for (Taxon taxon : taxa) {
            List<T> perTaxonOccurrences = dao.listByAssociatedTaxon(type, taxon, null, null, orderHints, propertyPaths);
            for (SpecimenOrObservationBase o : perTaxonOccurrences) {
                occurrenceIds.add(o.getId());
            }
        }
        occurrences = (List<T>) dao.listByIds(occurrenceIds, pageSize, pageNumber, orderHints, propertyPaths);

        return new DefaultPagerImpl<T>(pageNumber, occurrenceIds.size(), pageSize, occurrences);

    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IOccurrenceService#pageByAssociatedTaxon(java.lang.Class, java.util.Set, eu.etaxonomy.cdm.model.taxon.Taxon, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends SpecimenOrObservationBase> Pager<T> pageByAssociatedTaxon(Class<T> type, Set<TaxonRelationshipEdge> includeRelationships,
            String taxonUUID, Integer maxDepth, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {

        UUID uuid = UUID.fromString(taxonUUID);
        Taxon tax = (Taxon) taxonDao.load(uuid);
       //TODO REMOVE NULL STATEMENT
type=null;
        return pageByAssociatedTaxon( type,includeRelationships,tax, maxDepth, pageSize, pageNumber, orderHints, propertyPaths );

    }


    @Override
    public Pager<SearchResult<SpecimenOrObservationBase>> findByFullText(
            Class<? extends SpecimenOrObservationBase> clazz, String queryString, Rectangle boundingBox, List<Language> languages,
            boolean highlightFragments, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,
            List<String> propertyPaths) throws CorruptIndexException, IOException, ParseException {

        LuceneSearch luceneSearch = prepareByFullTextSearch(clazz, queryString, boundingBox, languages, highlightFragments);

        // --- execute search
        TopGroupsWithMaxScore topDocsResultSet = luceneSearch.executeSearch(pageSize, pageNumber);

        Map<CdmBaseType, String> idFieldMap = new HashMap<CdmBaseType, String>();
        idFieldMap.put(CdmBaseType.SPECIMEN_OR_OBSERVATIONBASE, "id");

        // --- initialize taxa, highlight matches ....
        ISearchResultBuilder searchResultBuilder = new SearchResultBuilder(luceneSearch, luceneSearch.getQuery());
        @SuppressWarnings("rawtypes")
        List<SearchResult<SpecimenOrObservationBase>> searchResults = searchResultBuilder.createResultSet(
                topDocsResultSet, luceneSearch.getHighlightFields(), dao, idFieldMap, propertyPaths);

        int totalHits = topDocsResultSet != null ? topDocsResultSet.topGroups.totalGroupCount : 0;

        return new DefaultPagerImpl<SearchResult<SpecimenOrObservationBase>>(pageNumber, totalHits, pageSize,
                searchResults);

    }


    /**
     * @param clazz
     * @param queryString
     * @param languages
     * @param highlightFragments
     * @return
     */
    private LuceneSearch prepareByFullTextSearch(Class<? extends SpecimenOrObservationBase> clazz, String queryString, Rectangle bbox,
            List<Language> languages, boolean highlightFragments) {

        BooleanQuery finalQuery = new BooleanQuery();
        BooleanQuery textQuery = new BooleanQuery();

        LuceneSearch luceneSearch = new LuceneSearch(luceneIndexToolProvider, FieldUnit.class);
        QueryFactory queryFactory = luceneIndexToolProvider.newQueryFactoryFor(FieldUnit.class);

        // --- criteria
        luceneSearch.setCdmTypRestriction(clazz);
        if(queryString != null){
            textQuery.add(queryFactory.newTermQuery("titleCache", queryString), Occur.SHOULD);
            finalQuery.add(textQuery, Occur.MUST);
        }

        // --- spacial query
        if(bbox != null){
            finalQuery.add(QueryFactory.buildSpatialQueryByRange(bbox, "gatheringEvent.exactLocation.point"), Occur.MUST);
        }

        luceneSearch.setQuery(finalQuery);

        // --- sorting
        SortField[] sortFields = new  SortField[]{SortField.FIELD_SCORE, new SortField("titleCache__sort", SortField.STRING, false)};
        luceneSearch.setSortFields(sortFields);

        if(highlightFragments){
            luceneSearch.setHighlightFields(queryFactory.getTextFieldNamesAsArray());
        }
        return luceneSearch;
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IOccurrenceService#getFieldUnits(eu.etaxonomy.cdm.model.occurrence.DerivedUnit)
     */
    @Override
    public Collection<FieldUnit> getFieldUnits(UUID derivedUnitUuid) {
        //It will search recursively over all {@link DerivationEvent}s and get the "originals" ({@link SpecimenOrObservationBase})
        //from which this DerivedUnit was derived until all FieldUnits are found.

        //FIXME: use HQL queries to increase performance
        Collection<FieldUnit> fieldUnits = new ArrayList<FieldUnit>();
        SpecimenOrObservationBase derivedUnit = load(derivedUnitUuid);
        if(derivedUnit instanceof DerivedUnit){
            getFieldUnits((DerivedUnit) derivedUnit, fieldUnits);
        }
        return fieldUnits;
    }


    /**
     * @param original
     * @param fieldUnits
     */
    private void getFieldUnits(DerivedUnit derivedUnit, Collection<FieldUnit> fieldUnits) {
        Set<SpecimenOrObservationBase> originals = derivedUnit.getOriginals();
        if(originals!=null && !originals.isEmpty()){
            for(SpecimenOrObservationBase<?> original:originals){
                if(original instanceof FieldUnit){
                    fieldUnits.add((FieldUnit) original);
                }
                else if(original instanceof DerivedUnit){
                    getFieldUnits((DerivedUnit) original, fieldUnits);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IOccurrenceService#moveSequence(eu.etaxonomy.cdm.model.molecular.DnaSample, eu.etaxonomy.cdm.model.molecular.DnaSample, eu.etaxonomy.cdm.model.molecular.Sequence)
     */
    @Override
    public boolean moveSequence(DnaSample from, DnaSample to, Sequence sequence) {
        //reload specimens to avoid session conflicts
        from = (DnaSample) load(from.getUuid());
        to = (DnaSample) load(to.getUuid());
        sequence = sequenceService.load(sequence.getUuid());

        if(from==null || to==null || sequence==null){
            throw new TransientObjectException("One of the CDM entities has not been saved to the data base yet. Moving only works for persisted/saved CDM entities.\n" +
                    "Operation was move "+sequence+ " from "+from+" to "+to);
        }
        from.removeSequence(sequence);
        saveOrUpdate(from);
        to.addSequence(sequence);
        saveOrUpdate(to);
        return true;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IOccurrenceService#moveDerivate(eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase, eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase, eu.etaxonomy.cdm.model.occurrence.DerivedUnit)
     */
    @Override
    public boolean moveDerivate(SpecimenOrObservationBase<?> from, SpecimenOrObservationBase<?> to, DerivedUnit derivate) {
        //reload specimens to avoid session conflicts
        from = load(from.getUuid());
        to = load(to.getUuid());
        derivate = (DerivedUnit) load(derivate.getUuid());

        if(from==null || to==null || derivate==null){
            throw new TransientObjectException("One of the CDM entities has not been saved to the data base yet. Moving only works for persisted/saved CDM entities.\n" +
            		"Operation was move "+derivate+ " from "+from+" to "+to);
        }

        SpecimenOrObservationType derivateType = derivate.getRecordBasis();
        SpecimenOrObservationType toType = to.getRecordBasis();
        //check if type is a sub derivate type
        if(toType==SpecimenOrObservationType.FieldUnit //moving to FieldUnit always works
                || (derivateType.isKindOf(toType) && toType!=derivateType)){ //moving only to parent derivate type
            //remove derivation event from parent specimen of dragged object
            DerivationEvent eventToRemove = null;
            for(DerivationEvent event:from.getDerivationEvents()){
                if(event.getDerivatives().contains(derivate)){
                    eventToRemove = event;
                    break;
                }
            }
            from.removeDerivationEvent(eventToRemove);
            saveOrUpdate(from);
            //add new derivation event to target
            to.addDerivationEvent(DerivationEvent.NewSimpleInstance(to, derivate, eventToRemove==null?null:eventToRemove.getType()));
            saveOrUpdate(to);
            return true;
        }
        return false;
    }

}
