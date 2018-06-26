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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.grouping.TopGroups;
import org.apache.lucene.util.BytesRef;
import org.hibernate.TransientObjectException;
import org.hibernate.search.spatial.impl.Rectangle;
import org.joda.time.Partial;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeConfigurator;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeNotSupportedException;
import eu.etaxonomy.cdm.api.service.UpdateResult.Status;
import eu.etaxonomy.cdm.api.service.config.DeleteConfiguratorBase;
import eu.etaxonomy.cdm.api.service.config.FindOccurrencesConfigurator;
import eu.etaxonomy.cdm.api.service.config.IIdentifiableEntityServiceConfigurator;
import eu.etaxonomy.cdm.api.service.config.SpecimenDeleteConfigurator;
import eu.etaxonomy.cdm.api.service.dto.DerivateDTO;
import eu.etaxonomy.cdm.api.service.dto.DerivateDataDTO;
import eu.etaxonomy.cdm.api.service.dto.DerivateDataDTO.ContigFile;
import eu.etaxonomy.cdm.api.service.dto.DerivateDataDTO.Link;
import eu.etaxonomy.cdm.api.service.dto.DerivateDataDTO.MolecularData;
import eu.etaxonomy.cdm.api.service.dto.FieldUnitDTO;
import eu.etaxonomy.cdm.api.service.dto.PreservedSpecimenDTO;
import eu.etaxonomy.cdm.api.service.exception.ReferencedObjectUndeletableException;
import eu.etaxonomy.cdm.api.service.molecular.ISequenceService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.api.service.search.ILuceneIndexToolProvider;
import eu.etaxonomy.cdm.api.service.search.ISearchResultBuilder;
import eu.etaxonomy.cdm.api.service.search.LuceneParseException;
import eu.etaxonomy.cdm.api.service.search.LuceneSearch;
import eu.etaxonomy.cdm.api.service.search.QueryFactory;
import eu.etaxonomy.cdm.api.service.search.SearchResult;
import eu.etaxonomy.cdm.api.service.search.SearchResultBuilder;
import eu.etaxonomy.cdm.api.service.util.TaxonRelationshipEdge;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.format.CdmFormatterFactory;
import eu.etaxonomy.cdm.format.ICdmFormatter.FormatKey;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.CdmBaseType;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.media.MediaUtils;
import eu.etaxonomy.cdm.model.molecular.AmplificationResult;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.molecular.SingleRead;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.initializer.AbstractBeanInitializer;
import eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao;
import eu.etaxonomy.cdm.persistence.dto.SpecimenNodeWrapper;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.AssignmentStatus;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.common.IdentifiableEntityDefaultCacheStrategy;

/**
 * @author a.babadshanjan
 * @since 01.09.2008
 */
@Service
@Transactional(readOnly = true)
public class OccurrenceServiceImpl extends IdentifiableServiceBase<SpecimenOrObservationBase, IOccurrenceDao> implements IOccurrenceService {

    static private final Logger logger = Logger.getLogger(OccurrenceServiceImpl.class);

    @Autowired
    private IDefinedTermDao definedTermDao;

    @Autowired
    private IDescriptionService descriptionService;

    @Autowired
    private INameService nameService;

    @Autowired
    private IEventBaseService eventService;

    @Autowired
    private ITaxonService taxonService;

    @Autowired
    private ISequenceService sequenceService;

    @Autowired
    private AbstractBeanInitializer beanInitializer;

    @Autowired
    private ILuceneIndexToolProvider luceneIndexToolProvider;

    private static final String SEPARATOR_STRING = ", ";

    public OccurrenceServiceImpl() {
        logger.debug("Load OccurrenceService Bean");
    }


    @Override
    @Transactional(readOnly = false)
    public void updateTitleCache(Class<? extends SpecimenOrObservationBase> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<SpecimenOrObservationBase> cacheStrategy, IProgressMonitor monitor) {
        if (clazz == null) {
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
        List<? extends DefinedTermBase> terms = this.definedTermDao.findByTitle(Country.class, name, null, null, null, null, null, null);
        List<Country> countries = new ArrayList<>();
        for (int i = 0; i < terms.size(); i++) {
            countries.add((Country) terms.get(i));
        }
        return countries;
    }

    @Override
    @Autowired
    protected void setDao(IOccurrenceDao dao) {
        this.dao = dao;
    }

    @Override
    public Pager<DerivationEvent> getDerivationEvents(SpecimenOrObservationBase occurence, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        long numberOfResults = dao.countDerivationEvents(occurence);

        List<DerivationEvent> results = new ArrayList<>();
        if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
            results = dao.getDerivationEvents(occurence, pageSize, pageNumber, propertyPaths);
        }

        return new DefaultPagerImpl<DerivationEvent>(pageNumber, numberOfResults, pageSize, results);
    }

    @Override
    public long countDeterminations(SpecimenOrObservationBase occurence, TaxonBase taxonbase) {
        return dao.countDeterminations(occurence, taxonbase);
    }

    @Override
    public Pager<DeterminationEvent> getDeterminations(SpecimenOrObservationBase occurrence, TaxonBase taxonBase,
            Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        long numberOfResults = dao.countDeterminations(occurrence, taxonBase);

        List<DeterminationEvent> results = new ArrayList<>();
        if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
            results = dao.getDeterminations(occurrence, taxonBase, pageSize, pageNumber, propertyPaths);
        }

        return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
    }

    @Override
    public Pager<Media> getMedia(SpecimenOrObservationBase occurence, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        long numberOfResults = dao.countMedia(occurence);

        List<Media> results = new ArrayList<>();
        if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
            results = dao.getMedia(occurence, pageSize, pageNumber, propertyPaths);
        }

        return new DefaultPagerImpl<Media>(pageNumber, numberOfResults, pageSize, results);
    }

    @Override
    public Pager<Media> getMediainHierarchy(SpecimenOrObservationBase rootOccurence, Integer pageSize,
            Integer pageNumber, List<String> propertyPaths) {
        List<Media> media = new ArrayList<>();
        //media specimens
        if(rootOccurence.isInstanceOf(MediaSpecimen.class)){
            MediaSpecimen mediaSpecimen = HibernateProxyHelper.deproxy(rootOccurence, MediaSpecimen.class);
            media.add(mediaSpecimen.getMediaSpecimen());
        }
        // pherograms & gelPhotos
        if (rootOccurence.isInstanceOf(DnaSample.class)) {
            DnaSample dnaSample = CdmBase.deproxy(rootOccurence, DnaSample.class);
            Set<Sequence> sequences = dnaSample.getSequences();
            //we do show only those gelPhotos which lead to a consensus sequence
            for (Sequence sequence : sequences) {
                Set<Media> dnaRelatedMedia = new HashSet<>();
                for (SingleRead singleRead : sequence.getSingleReads()){
                    AmplificationResult amplification = singleRead.getAmplificationResult();
                    dnaRelatedMedia.add(amplification.getGelPhoto());
                    dnaRelatedMedia.add(singleRead.getPherogram());
                    dnaRelatedMedia.remove(null);
                }
                media.addAll(dnaRelatedMedia);
            }
        }
        if(rootOccurence.isInstanceOf(DerivedUnit.class)){
            DerivedUnit derivedUnit = HibernateProxyHelper.deproxy(rootOccurence, DerivedUnit.class);
            for (DerivationEvent derivationEvent : derivedUnit.getDerivationEvents()) {
                for (DerivedUnit childDerivative : derivationEvent.getDerivatives()) {
                    media.addAll(getMediainHierarchy(childDerivative, pageSize, pageNumber, propertyPaths).getRecords());
                }
            }
        }
        return new DefaultPagerImpl<Media>(pageNumber, Long.valueOf(media.size()), pageSize, media);
    }

    @Override
    public Pager<SpecimenOrObservationBase> list(Class<? extends SpecimenOrObservationBase> type, TaxonName determinedAs,
            Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        long numberOfResults = dao.count(type, determinedAs);
        @SuppressWarnings("rawtypes")
        List<SpecimenOrObservationBase> results = new ArrayList<>();
        if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
            results = dao.list(type, determinedAs, pageSize, pageNumber, orderHints, propertyPaths);
        }
        return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
    }

    @Override
    public Pager<SpecimenOrObservationBase> list(Class<? extends SpecimenOrObservationBase> type, TaxonBase determinedAs,
            Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        long numberOfResults = dao.count(type, determinedAs);
        @SuppressWarnings("rawtypes")
        List<SpecimenOrObservationBase> results = new ArrayList<>();
        if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
            results = dao.list(type, determinedAs, pageSize, pageNumber, orderHints, propertyPaths);
        }
        return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
    }

    @Override
    public List<UuidAndTitleCache<DerivedUnit>> getDerivedUnitUuidAndTitleCache(Integer limit, String pattern) {
        return dao.getDerivedUnitUuidAndTitleCache(limit, pattern);
    }

    @Override
    public List<UuidAndTitleCache<FieldUnit>> getFieldUnitUuidAndTitleCache() {
        return dao.getFieldUnitUuidAndTitleCache();
    }

    @Override
    public DerivedUnitFacade getDerivedUnitFacade(DerivedUnit derivedUnit, List<String> propertyPaths) throws DerivedUnitFacadeNotSupportedException {
        derivedUnit = (DerivedUnit) dao.load(derivedUnit.getUuid(), null);
        DerivedUnitFacadeConfigurator config = DerivedUnitFacadeConfigurator.NewInstance();
        config.setThrowExceptionForNonSpecimenPreservationMethodRequest(false);
        DerivedUnitFacade derivedUnitFacade = DerivedUnitFacade.NewInstance(derivedUnit, config);
        beanInitializer.initialize(derivedUnitFacade, propertyPaths);
        return derivedUnitFacade;
    }

    @Override
    public List<DerivedUnitFacade> listDerivedUnitFacades(
            DescriptionBase description, List<String> propertyPaths) {

        List<DerivedUnitFacade> derivedUnitFacadeList = new ArrayList<>();
        IndividualsAssociation tempIndividualsAssociation;
        SpecimenOrObservationBase tempSpecimenOrObservationBase;
        List<IndividualsAssociation> elements = descriptionService.listDescriptionElements(description, null, IndividualsAssociation.class, null, 0, Arrays.asList(new String []{"associatedSpecimenOrObservation"}));
        for (IndividualsAssociation element : elements) {
            tempIndividualsAssociation = HibernateProxyHelper.deproxy(element, IndividualsAssociation.class);
            if (tempIndividualsAssociation.getAssociatedSpecimenOrObservation() != null) {
                tempSpecimenOrObservationBase = HibernateProxyHelper.deproxy(tempIndividualsAssociation.getAssociatedSpecimenOrObservation(), SpecimenOrObservationBase.class);
                if (tempSpecimenOrObservationBase.isInstanceOf(DerivedUnit.class)) {
                    try {
                        derivedUnitFacadeList.add(DerivedUnitFacade.NewInstance(HibernateProxyHelper.deproxy(tempSpecimenOrObservationBase, DerivedUnit.class)));
                    } catch (DerivedUnitFacadeNotSupportedException e) {
                        logger.warn(tempIndividualsAssociation.getAssociatedSpecimenOrObservation().getTitleCache() + " : " + e.getMessage());
                    }
                }
            }
        }

        beanInitializer.initializeAll(derivedUnitFacadeList, propertyPaths);

        return derivedUnitFacadeList;
    }


    @Override
    public <T extends SpecimenOrObservationBase> List<T> listByAssociatedTaxon(Class<T> type, Set<TaxonRelationshipEdge> includeRelationships,
            Taxon associatedTaxon, Integer maxDepth, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {

        return pageByAssociatedTaxon(type, includeRelationships, associatedTaxon, maxDepth, pageSize, pageNumber, orderHints, propertyPaths).getRecords();
    }

    @Override
    public Collection<SpecimenNodeWrapper> listUuidAndTitleCacheByAssociatedTaxon(List<UUID> taxonNodeUuids,
            Integer limit, Integer start) {
        return dao.listUuidAndTitleCacheByAssociatedTaxon(taxonNodeUuids, limit, start);
        }

    @Override
    public Collection<SpecimenOrObservationBase> listFieldUnitsByAssociatedTaxon(Taxon associatedTaxon, List<OrderHint> orderHints, List<String> propertyPaths) {
        return pageFieldUnitsByAssociatedTaxon(null, associatedTaxon, null, null, null, null, propertyPaths).getRecords();
    }

    @Override
    public Pager<SpecimenOrObservationBase> pageFieldUnitsByAssociatedTaxon(Set<TaxonRelationshipEdge> includeRelationships,
            Taxon associatedTaxon, Integer maxDepth, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,
            List<String> propertyPaths) {

        if (!getSession().contains(associatedTaxon)) {
            associatedTaxon = (Taxon) taxonService.load(associatedTaxon.getUuid());
        }

        // gather the IDs of all relevant field units
        Set<UUID> fieldUnitUuids = new HashSet<>();
        List<SpecimenOrObservationBase> records = listByAssociatedTaxon(null, includeRelationships, associatedTaxon, maxDepth, null, null, orderHints, propertyPaths);
        for (SpecimenOrObservationBase<?> specimen : records) {
            for (FieldUnit fieldUnit : getFieldUnits(specimen.getUuid(), null)) {
                fieldUnitUuids.add(fieldUnit.getUuid());
            }
        }
        //dao.list() does the paging of the field units. Passing the field units directly to the Pager would not work
        List<SpecimenOrObservationBase> fieldUnits = dao.list(fieldUnitUuids, pageSize, pageNumber, orderHints, propertyPaths);
        return new DefaultPagerImpl<>(pageNumber, fieldUnitUuids.size(), pageSize, fieldUnits);
    }

    @Override
    public FieldUnitDTO assembleFieldUnitDTO(FieldUnit fieldUnit, UUID associatedTaxonUuid) {

        if (!getSession().contains(fieldUnit)) {
            fieldUnit = (FieldUnit) load(fieldUnit.getUuid());
        }
        TaxonBase associatedTaxon = taxonService.load(associatedTaxonUuid);

        FieldUnitDTO fieldUnitDTO = new FieldUnitDTO();

        if (fieldUnit.getGatheringEvent() != null) {
            GatheringEvent gatheringEvent = fieldUnit.getGatheringEvent();
            // Country
            NamedArea country = gatheringEvent.getCountry();
            fieldUnitDTO.setCountry(country != null ? country.getLabel() : null);
            // Collection
            AgentBase collector = gatheringEvent.getCollector();
            String fieldNumber = fieldUnit.getFieldNumber();
            String collectionString = "";
            if (collector != null || fieldNumber != null) {
                collectionString += collector != null ? collector : "";
                if (!collectionString.isEmpty()) {
                    collectionString += " ";
                }
                collectionString += (fieldNumber != null ? fieldNumber : "");
                collectionString.trim();
            }
            fieldUnitDTO.setCollection(collectionString);
            // Date
            Partial gatheringDate = gatheringEvent.getGatheringDate();
            String dateString = null;
            if (gatheringDate != null) {
                dateString = gatheringDate.toString();
            }
            else if(gatheringEvent.getTimeperiod()!=null && gatheringEvent.getTimeperiod().getFreeText()!=null){
                dateString = gatheringEvent.getTimeperiod().getFreeText();
            }
            fieldUnitDTO.setDate(dateString);
        }

        // Taxon Name
        fieldUnitDTO.setTaxonName(associatedTaxon.getName().getTitleCache());

        // Herbaria map
        Map<eu.etaxonomy.cdm.model.occurrence.Collection, Integer> collectionToCountMap = new HashMap<>();
        // List of accession numbers for citation
        List<String> preservedSpecimenAccessionNumbers = new ArrayList<>();

        // assemble preserved specimen DTOs
        Set<DerivationEvent> derivationEvents = fieldUnit.getDerivationEvents();
        for (DerivationEvent derivationEvent : derivationEvents) {
            Set<DerivedUnit> derivatives = derivationEvent.getDerivatives();
            for (DerivedUnit derivedUnit : derivatives) {
                if(!derivedUnit.isPublish()){
                    continue;
                }
                // collect accession numbers for citation
                String mostSignificantIdentifier = getMostSignificantIdentifier(derivedUnit);
                if (mostSignificantIdentifier != null) {
                    preservedSpecimenAccessionNumbers.add(mostSignificantIdentifier);
                }
                // collect collections for herbaria column
                if (derivedUnit.getCollection() != null) {
                    Integer herbariumCount = collectionToCountMap.get(derivedUnit.getCollection());
                    if (herbariumCount == null) {
                        herbariumCount = 0;
                    }
                    collectionToCountMap.put(derivedUnit.getCollection(), herbariumCount + 1);
                }
                if (derivedUnit.getRecordBasis().equals(SpecimenOrObservationType.PreservedSpecimen)) {
                    PreservedSpecimenDTO preservedSpecimenDTO = assemblePreservedSpecimenDTO(derivedUnit, fieldUnitDTO);
                    fieldUnitDTO.addPreservedSpecimenDTO(preservedSpecimenDTO);
                    fieldUnitDTO.setHasCharacterData(fieldUnitDTO.isHasCharacterData() || preservedSpecimenDTO.isHasCharacterData());
                    fieldUnitDTO.setHasDetailImage(fieldUnitDTO.isHasDetailImage() || preservedSpecimenDTO.isHasDetailImage());
                    fieldUnitDTO.setHasDna(fieldUnitDTO.isHasDna() || preservedSpecimenDTO.isHasDna());
                    fieldUnitDTO.setHasSpecimenScan(fieldUnitDTO.isHasSpecimenScan() || preservedSpecimenDTO.isHasSpecimenScan());
                }
            }
        }
        // assemble derivate data DTO
        assembleDerivateDataDTO(fieldUnitDTO, fieldUnit);

        // assemble citation
        String citation = fieldUnit.getTitleCache();
        if((CdmUtils.isBlank(citation) || citation.equals(IdentifiableEntityDefaultCacheStrategy.TITLE_CACHE_GENERATION_NOT_IMPLEMENTED))
                && !fieldUnit.isProtectedTitleCache()){
            fieldUnit.setTitleCache(null);
            citation = fieldUnit.getTitleCache();
        }
        if (!preservedSpecimenAccessionNumbers.isEmpty()) {
            citation += " (";
            for (String accessionNumber : preservedSpecimenAccessionNumbers) {
                if (!accessionNumber.isEmpty()) {
                    citation += accessionNumber + SEPARATOR_STRING;
                }
            }
            citation = removeTail(citation, SEPARATOR_STRING);
            citation += ")";
        }
        fieldUnitDTO.setCitation(citation);

        // assemble herbaria string
        String herbariaString = "";
        for (Entry<eu.etaxonomy.cdm.model.occurrence.Collection, Integer> e : collectionToCountMap.entrySet()) {
            eu.etaxonomy.cdm.model.occurrence.Collection collection = e.getKey();
            if (collection.getCode() != null) {
                herbariaString += collection.getCode();
            }
            if (e.getValue() > 1) {
                herbariaString += "(" + e.getValue() + ")";
            }
            herbariaString += SEPARATOR_STRING;
        }
        herbariaString = removeTail(herbariaString, SEPARATOR_STRING);
        fieldUnitDTO.setHerbarium(herbariaString);

        return fieldUnitDTO;
    }

    @Override
    public PreservedSpecimenDTO assemblePreservedSpecimenDTO(DerivedUnit derivedUnit) {
        return assemblePreservedSpecimenDTO(derivedUnit, null);
    }

    @Override
    public String getMostSignificantIdentifier(DerivedUnit derivedUnit) {
        if (derivedUnit.getAccessionNumber() != null && !derivedUnit.getAccessionNumber().isEmpty()) {
            return derivedUnit.getAccessionNumber();
        }
        else if(derivedUnit.getBarcode()!=null && !derivedUnit.getBarcode().isEmpty()){
            return derivedUnit.getBarcode();
        }
        else if(derivedUnit.getCatalogNumber()!=null && !derivedUnit.getCatalogNumber().isEmpty()){
            return derivedUnit.getCatalogNumber();
        }
        return null;
    }

    public PreservedSpecimenDTO assemblePreservedSpecimenDTO(DerivedUnit derivedUnit, FieldUnitDTO fieldUnitDTO) {
        if (!getSession().contains(derivedUnit)) {
            derivedUnit = (DerivedUnit) load(derivedUnit.getUuid());
        }
        PreservedSpecimenDTO preservedSpecimenDTO = new PreservedSpecimenDTO();

        //specimen identifier
        FormatKey collectionKey = FormatKey.COLLECTION_CODE;
        String specimenIdentifier = CdmFormatterFactory.format(derivedUnit, collectionKey);
        if (CdmUtils.isBlank(specimenIdentifier)) {
            collectionKey = FormatKey.COLLECTION_NAME;
        }
        specimenIdentifier = CdmFormatterFactory.format(derivedUnit, new FormatKey[] {
                collectionKey, FormatKey.SPACE,
                FormatKey.MOST_SIGNIFICANT_IDENTIFIER, FormatKey.SPACE });
        if(CdmUtils.isBlank(specimenIdentifier)){
            specimenIdentifier = derivedUnit.getUuid().toString();
        }
        preservedSpecimenDTO.setAccessionNumber(specimenIdentifier);
        preservedSpecimenDTO.setUuid(derivedUnit.getUuid().toString());

        //preferred stable URI
        preservedSpecimenDTO.setPreferredStableUri(derivedUnit.getPreferredStableUri());

        // citation
        Collection<FieldUnit> fieldUnits = getFieldUnits(derivedUnit, null);
        if (fieldUnits.size() == 1) {
            preservedSpecimenDTO.setCitation(fieldUnits.iterator().next().getTitleCache());
        }
        else{
            preservedSpecimenDTO.setCitation("No Citation available. This specimen either has no or multiple field units.");
        }

        // character state data
        Collection<DescriptionElementBase> characterDataForSpecimen = getCharacterDataForSpecimen(derivedUnit);
        if (!characterDataForSpecimen.isEmpty()) {
            if (fieldUnitDTO != null) {
                fieldUnitDTO.setHasCharacterData(true);
            }
        }
        for (DescriptionElementBase descriptionElementBase : characterDataForSpecimen) {
            String character = descriptionElementBase.getFeature().getLabel();
            ArrayList<Language> languages = new ArrayList<>(Collections.singleton(Language.DEFAULT()));
            if (descriptionElementBase instanceof QuantitativeData) {
                QuantitativeData quantitativeData = (QuantitativeData) descriptionElementBase;
                DefaultQuantitativeDescriptionBuilder builder = new DefaultQuantitativeDescriptionBuilder();
                String state = builder.build(quantitativeData, languages).getText(Language.DEFAULT());
                preservedSpecimenDTO.addCharacterData(character, state);
            }
            else if(descriptionElementBase instanceof CategoricalData){
                CategoricalData categoricalData = (CategoricalData) descriptionElementBase;
                DefaultCategoricalDescriptionBuilder builder = new DefaultCategoricalDescriptionBuilder();
                String state = builder.build(categoricalData, languages).getText(Language.DEFAULT());
                preservedSpecimenDTO.addCharacterData(character, state);
            }
        }
        // check type designations
        Collection<SpecimenTypeDesignation> specimenTypeDesignations = listTypeDesignations(derivedUnit, null, null, null, null);
        for (SpecimenTypeDesignation specimenTypeDesignation : specimenTypeDesignations) {
            if (fieldUnitDTO != null) {
                fieldUnitDTO.setHasType(true);
            }
            TypeDesignationStatusBase<?> typeStatus = specimenTypeDesignation.getTypeStatus();
            if (typeStatus != null) {
                List<String> typedTaxaNames = new ArrayList<>();
                String label = typeStatus.getLabel();
                Set<TaxonName> typifiedNames = specimenTypeDesignation.getTypifiedNames();
                for (TaxonName taxonName : typifiedNames) {
                    typedTaxaNames.add(taxonName.getNameCache());
                }
                preservedSpecimenDTO.addTypes(label, typedTaxaNames);
            }
        }

        // individuals associations
        Collection<IndividualsAssociation> individualsAssociations = listIndividualsAssociations(derivedUnit, null, null, null, null);
        for (IndividualsAssociation individualsAssociation : individualsAssociations) {
            if (individualsAssociation.getInDescription() != null) {
                if (individualsAssociation.getInDescription().isInstanceOf(TaxonDescription.class)) {
                    TaxonDescription taxonDescription = HibernateProxyHelper.deproxy(individualsAssociation.getInDescription(), TaxonDescription.class);
                    Taxon taxon = taxonDescription.getTaxon();
                    if (taxon != null) {
                        preservedSpecimenDTO.addAssociatedTaxon(taxon);
                    }
                }
            }
        }
        // assemble sub derivates
        preservedSpecimenDTO.setDerivateDataDTO(assembleDerivateDataDTO(preservedSpecimenDTO, derivedUnit));
        return preservedSpecimenDTO;
    }

    private DerivateDataDTO assembleDerivateDataDTO(DerivateDTO derivateDTO, SpecimenOrObservationBase<?> specimenOrObservation) {
        DerivateDataDTO derivateDataDTO = new DerivateDataDTO();
        Collection<DerivedUnit> childDerivates = getDerivedUnitsFor(specimenOrObservation);
        for (DerivedUnit childDerivate : childDerivates) {
            // assemble molecular data
            //pattern: DNAMarker [contig1, primer1_1, primer1_2, ...][contig2, primer2_1, ...]...
            if (childDerivate.isInstanceOf(DnaSample.class)) {
                if (childDerivate.getRecordBasis() == SpecimenOrObservationType.TissueSample) {
                    // TODO implement TissueSample assembly for web service
                }
                if (childDerivate.getRecordBasis() == SpecimenOrObservationType.DnaSample) {

                    DnaSample dna = HibernateProxyHelper.deproxy(childDerivate, DnaSample.class);
                    if (!dna.getSequences().isEmpty()) {
                        derivateDTO.setHasDna(true);
                    }
                    for (Sequence sequence : dna.getSequences()) {
                        URI boldUri = null;
                        try {
                            boldUri = sequence.getBoldUri();
                        } catch (URISyntaxException e1) {
                            logger.error("Could not create BOLD URI", e1);
                        }
                        final DefinedTerm dnaMarker = sequence.getDnaMarker();
                        Link providerLink = null;
                        if(boldUri!=null && dnaMarker!=null){
                        	providerLink = new DerivateDataDTO.Link(boldUri, dnaMarker.getLabel());
                        }
                        MolecularData molecularData = derivateDataDTO.addProviderLink(providerLink);

                        //contig file
                        ContigFile contigFile = null;
                        if (sequence.getContigFile() != null) {
                            MediaRepresentationPart contigMediaRepresentationPart = MediaUtils.getFirstMediaRepresentationPart(sequence.getContigFile());
                            if (contigMediaRepresentationPart != null) {
                                contigFile = molecularData.addContigFile(new Link(contigMediaRepresentationPart.getUri(), "contig"));
                            }
                        }
                        else{
                        	contigFile = molecularData.addContigFile(null);
                        }
                        // primer files
                        if (sequence.getSingleReads() != null) {
                            int readCount = 1;
                            for (SingleRead singleRead : sequence.getSingleReads()) {
                                MediaRepresentationPart pherogramMediaRepresentationPart = MediaUtils.getFirstMediaRepresentationPart(singleRead.getPherogram());
                                if (pherogramMediaRepresentationPart != null) {
                                    contigFile.addPrimerLink(pherogramMediaRepresentationPart.getUri(), "read"+readCount++);
                                }
                            }
                        }
                    }
                }
            }
            // assemble media data
            else if (childDerivate.isInstanceOf(MediaSpecimen.class)) {
                MediaSpecimen media = HibernateProxyHelper.deproxy(childDerivate, MediaSpecimen.class);

                URI mediaUri = getMediaUri(media);
                if (media.getKindOfUnit() != null) {
                    // specimen scan
                    if (media.getKindOfUnit().getUuid().equals(DefinedTerm.uuidSpecimenScan)) {
                        derivateDataDTO.addSpecimenScanUuid(media.getMediaSpecimen().getUuid());
                        derivateDTO.setHasSpecimenScan(true);
                        String imageLinkText = "scan";
                        if (derivateDTO instanceof PreservedSpecimenDTO && ((PreservedSpecimenDTO) derivateDTO).getAccessionNumber() != null) {
                            imageLinkText = ((PreservedSpecimenDTO) derivateDTO).getAccessionNumber();
                        }
                        derivateDataDTO.addSpecimenScan(mediaUri, imageLinkText);
                    }
                    // detail image
                    else if (media.getKindOfUnit().getUuid().equals(DefinedTerm.uuidDetailImage)) {
                        derivateDataDTO.addDetailImageUuid(media.getMediaSpecimen().getUuid());
                        derivateDTO.setHasDetailImage(true);
                        String motif = "detail image";
                        if (media.getMediaSpecimen()!=null){
                        	if(CdmUtils.isNotBlank(media.getMediaSpecimen().getTitleCache())) {
                        		motif = media.getMediaSpecimen().getTitleCache();
                        	}
                        }
                        derivateDataDTO.addDetailImage(mediaUri, motif);
                    }
                }
            }
        }
        return derivateDataDTO;
    }

    private String removeTail(String string, final String tail) {
        if (string.endsWith(tail)) {
            string = string.substring(0, string.length() - tail.length());
        }
        return string;
    }

    private URI getMediaUri(MediaSpecimen mediaSpecimen) {
        URI mediaUri = null;
        Collection<MediaRepresentation> mediaRepresentations = mediaSpecimen.getMediaSpecimen().getRepresentations();
        if (mediaRepresentations != null && !mediaRepresentations.isEmpty()) {
            Collection<MediaRepresentationPart> mediaRepresentationParts = mediaRepresentations.iterator().next().getParts();
            if (mediaRepresentationParts != null && !mediaRepresentationParts.isEmpty()) {
                MediaRepresentationPart part = mediaRepresentationParts.iterator().next();
                if (part.getUri() != null) {
                    mediaUri = part.getUri();
                }
            }
        }
        return mediaUri;
    }

    private Collection<DerivedUnit> getDerivedUnitsFor(SpecimenOrObservationBase<?> specimen) {
        Collection<DerivedUnit> derivedUnits = new ArrayList<>();
        for (DerivationEvent derivationEvent : specimen.getDerivationEvents()) {
            for (DerivedUnit derivative : derivationEvent.getDerivatives()) {
                derivedUnits.add(derivative);
                derivedUnits.addAll(getDerivedUnitsFor(derivative));
            }
        }
        return derivedUnits;
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T extends SpecimenOrObservationBase> Pager<T> pageByAssociatedTaxon(Class<T> type, Set<TaxonRelationshipEdge> includedRelationships,
            Taxon associatedTaxon, Integer maxDepth, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {

        Set<Taxon> taxa = new HashSet<>();
        Set<Integer> occurrenceIds = new HashSet<>();
        List<T> occurrences = new ArrayList<>();
        boolean includeUnpublished = INCLUDE_UNPUBLISHED;

        // Integer limit = PagerUtils.limitFor(pageSize);
        // Integer start = PagerUtils.startFor(pageSize, pageNumber);

        if (!getSession().contains(associatedTaxon)) {
            associatedTaxon = (Taxon) taxonService.load(associatedTaxon.getUuid());
        }

        if (includedRelationships != null) {
            taxa = taxonService.listRelatedTaxa(associatedTaxon, includedRelationships, maxDepth, includeUnpublished, null, null, propertyPaths);
        }

        taxa.add(associatedTaxon);

        for (Taxon taxon : taxa) {
            List<T> perTaxonOccurrences = dao.listByAssociatedTaxon(type, taxon, null, null, orderHints, propertyPaths);
            for (SpecimenOrObservationBase<?> o : perTaxonOccurrences) {
                occurrenceIds.add(o.getId());
            }
        }
        occurrences = (List<T>) dao.loadList(occurrenceIds, propertyPaths);

        return new DefaultPagerImpl<T>(pageNumber, Long.valueOf(occurrenceIds.size()), pageSize, occurrences);

    }

    @Override
    public <T extends SpecimenOrObservationBase> Pager<T> pageByAssociatedTaxon(Class<T> type, Set<TaxonRelationshipEdge> includeRelationships,
            String taxonUUID, Integer maxDepth, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {

        UUID uuid = UUID.fromString(taxonUUID);
        Taxon tax = (Taxon) taxonService.load(uuid);
        // TODO REMOVE NULL STATEMENT
        type = null;
        return pageByAssociatedTaxon(type, includeRelationships, tax, maxDepth, pageSize, pageNumber, orderHints, propertyPaths);

    }

    @Override
    public  List<DerivedUnit> findByAccessionNumber(
            String accessionNumberString, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,
            List<String> propertyPaths)  {

        List<DerivedUnit> records = new ArrayList<>();
        records = dao.getByGeneticAccessionNumber(accessionNumberString, propertyPaths);

        return records;

    }

    @Override
    public Pager<SearchResult<SpecimenOrObservationBase>> findByFullText(
            Class<? extends SpecimenOrObservationBase> clazz, String queryString, Rectangle boundingBox, List<Language> languages,
            boolean highlightFragments, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,
            List<String> propertyPaths) throws IOException, LuceneParseException {

        LuceneSearch luceneSearch = prepareByFullTextSearch(clazz, queryString, boundingBox, languages, highlightFragments);

        // --- execute search
        TopGroups<BytesRef> topDocsResultSet;
        try {
            topDocsResultSet = luceneSearch.executeSearch(pageSize, pageNumber);
        } catch (ParseException e) {
            LuceneParseException parseException = new LuceneParseException(e.getMessage());
            parseException.setStackTrace(e.getStackTrace());
            throw parseException;
        }

        Map<CdmBaseType, String> idFieldMap = new HashMap<>();
        idFieldMap.put(CdmBaseType.SPECIMEN_OR_OBSERVATIONBASE, "id");

        // --- initialize taxa, highlight matches ....
        ISearchResultBuilder searchResultBuilder = new SearchResultBuilder(luceneSearch, luceneSearch.getQuery());
        @SuppressWarnings("rawtypes")
        List<SearchResult<SpecimenOrObservationBase>> searchResults = searchResultBuilder.createResultSet(
                topDocsResultSet, luceneSearch.getHighlightFields(), dao, idFieldMap, propertyPaths);

        int totalHits = topDocsResultSet != null ? topDocsResultSet.totalGroupCount : 0;

        return new DefaultPagerImpl<>(pageNumber, Long.valueOf(totalHits), pageSize, searchResults);

    }

    private LuceneSearch prepareByFullTextSearch(Class<? extends SpecimenOrObservationBase> clazz, String queryString, Rectangle bbox,
            List<Language> languages, boolean highlightFragments) {

        Builder finalQueryBuilder = new Builder();
        Builder textQueryBuilder = new Builder();

        LuceneSearch luceneSearch = new LuceneSearch(luceneIndexToolProvider, FieldUnit.class);
        QueryFactory queryFactory = luceneIndexToolProvider.newQueryFactoryFor(FieldUnit.class);

        // --- criteria
        luceneSearch.setCdmTypRestriction(clazz);
        if (queryString != null) {
            textQueryBuilder.add(queryFactory.newTermQuery("titleCache", queryString), Occur.SHOULD);
            finalQueryBuilder.add(textQueryBuilder.build(), Occur.MUST);
        }

        // --- spacial query
        if (bbox != null) {
            finalQueryBuilder.add(QueryFactory.buildSpatialQueryByRange(bbox, "gatheringEvent.exactLocation.point"), Occur.MUST);
        }

        luceneSearch.setQuery(finalQueryBuilder.build());

        // --- sorting
        SortField[] sortFields = new SortField[] { SortField.FIELD_SCORE, new SortField("titleCache__sort", SortField.Type.STRING, false) };
        luceneSearch.setSortFields(sortFields);

        if (highlightFragments) {
            luceneSearch.setHighlightFields(queryFactory.getTextFieldNamesAsArray());
        }
        return luceneSearch;
    }


    @Override
    public Collection<FieldUnit> getFieldUnits(UUID derivedUnitUuid, List<String> propertyPaths) {
        //It will search recursively over all {@link DerivationEvent}s and get the "originals" ({@link SpecimenOrObservationBase})
        //from which this DerivedUnit was derived until all FieldUnits are found.

        // FIXME: use HQL queries to increase performance
        SpecimenOrObservationBase<?> specimen = load(derivedUnitUuid, propertyPaths);
//        specimen = HibernateProxyHelper.deproxy(specimen, SpecimenOrObservationBase.class);
        Collection<FieldUnit> fieldUnits = new ArrayList<>();

        if (specimen.isInstanceOf(FieldUnit.class)) {
            fieldUnits.add(HibernateProxyHelper.deproxy(specimen, FieldUnit.class));
        }
        else if(specimen.isInstanceOf(DerivedUnit.class)){
            fieldUnits.addAll(getFieldUnits(HibernateProxyHelper.deproxy(specimen, DerivedUnit.class), propertyPaths));
        }
        return fieldUnits;
    }

    private Collection<FieldUnit> getFieldUnits(DerivedUnit derivedUnit, List<String> propertyPaths) {
        Collection<FieldUnit> fieldUnits = new HashSet<>();
        Set<SpecimenOrObservationBase> originals = derivedUnit.getOriginals();
        if (originals != null && !originals.isEmpty()) {
            for (SpecimenOrObservationBase<?> original : originals) {
                if (original.isInstanceOf(FieldUnit.class)) {
                    fieldUnits.add((FieldUnit) load(original.getUuid(), propertyPaths));
                }
                else if(original.isInstanceOf(DerivedUnit.class)){
                    fieldUnits.addAll(getFieldUnits(HibernateProxyHelper.deproxy(original, DerivedUnit.class), propertyPaths));
                }
            }
        }
        return fieldUnits;
    }

    @Override
    @Transactional(readOnly = false)
    public UpdateResult moveSequence(DnaSample from, DnaSample to, Sequence sequence) {
        return moveSequence(from.getUuid(), to.getUuid(), sequence.getUuid());
    }

    @Override
    @Transactional(readOnly = false)
    public UpdateResult moveSequence(UUID fromUuid, UUID toUuid, UUID sequenceUuid) {
        // reload specimens to avoid session conflicts
        DnaSample from = (DnaSample) load(fromUuid);
        DnaSample to = (DnaSample) load(toUuid);
        Sequence sequence = sequenceService.load(sequenceUuid);

        if (from == null || to == null || sequence == null) {
            throw new TransientObjectException("One of the CDM entities has not been saved to the data base yet. Moving only works for persisted/saved CDM entities.\n" +
                    "Operation was move "+sequence+ " from "+from+" to "+to);
        }
        UpdateResult result = new UpdateResult();
        from.removeSequence(sequence);
        saveOrUpdate(from);
        to.addSequence(sequence);
        saveOrUpdate(to);
        result.setStatus(Status.OK);
        result.addUpdatedObject(from);
        result.addUpdatedObject(to);
        return result;
    }

    @Override
    @Transactional(readOnly = false)
    public boolean moveDerivate(SpecimenOrObservationBase<?> from, SpecimenOrObservationBase<?> to, DerivedUnit derivate) {
        return moveDerivate(from!=null?from.getUuid():null, to.getUuid(), derivate.getUuid()).isOk();
    }

    @Override
    @Transactional(readOnly = false)
    public UpdateResult moveDerivate(UUID specimenFromUuid, UUID specimenToUuid, UUID derivateUuid) {
        // reload specimens to avoid session conflicts
        SpecimenOrObservationBase<?> from = null;
        if(specimenFromUuid!=null){
            from = load(specimenFromUuid);
        }
        SpecimenOrObservationBase<?> to = load(specimenToUuid);
        DerivedUnit derivate = (DerivedUnit) load(derivateUuid);

        if ((specimenFromUuid!=null && from == null) || to == null || derivate == null) {
            throw new TransientObjectException("One of the CDM entities has not been saved to the data base yet. Moving only works for persisted/saved CDM entities.\n" +
                    "Operation was move "+derivate+ " from "+from+" to "+to);
        }
        UpdateResult result = new UpdateResult();
        SpecimenOrObservationType derivateType = derivate.getRecordBasis();
        SpecimenOrObservationType toType = to.getRecordBasis();
        // check if type is a sub derivate type
        if(toType==SpecimenOrObservationType.FieldUnit //moving to FieldUnit always works
                || derivateType==SpecimenOrObservationType.Media //moving media always works
                || (derivateType.isKindOf(toType) && toType!=derivateType)){ //moving only to parent derivate type
            if(from!=null){
                // remove derivation event from parent specimen of dragged object
                DerivationEvent eventToRemove = null;
                for (DerivationEvent event : from.getDerivationEvents()) {
                    if (event.getDerivatives().contains(derivate)) {
                        eventToRemove = event;
                        break;
                    }
                }
                from.removeDerivationEvent(eventToRemove);
                if(eventToRemove!=null){
                    // add new derivation event to target and copy the event parameters of the old one
                    DerivationEvent derivedFromNewOriginalEvent = DerivationEvent.NewSimpleInstance(to, derivate, null);
                    derivedFromNewOriginalEvent.setActor(eventToRemove.getActor());
                    derivedFromNewOriginalEvent.setDescription(eventToRemove.getDescription());
                    derivedFromNewOriginalEvent.setInstitution(eventToRemove.getInstitution());
                    derivedFromNewOriginalEvent.setTimeperiod(eventToRemove.getTimeperiod());
                    derivedFromNewOriginalEvent.setType(eventToRemove.getType());
                    to.addDerivationEvent(derivedFromNewOriginalEvent);
                    derivate.setDerivedFrom(derivedFromNewOriginalEvent);
                }
            }
            else{
                //derivative had no parent before so we use empty derivation event
                DerivationEvent derivedFromNewOriginalEvent = DerivationEvent.NewSimpleInstance(to, derivate, null);
                to.addDerivationEvent(derivedFromNewOriginalEvent);
                derivate.setDerivedFrom(derivedFromNewOriginalEvent);
            }

            if(from!=null){
                saveOrUpdate(from);
            }
            saveOrUpdate(to);
            result.setStatus(Status.OK);
            result.addUpdatedObject(from);
            result.addUpdatedObject(to);
        } else {
            result.setStatus(Status.ERROR);
        }
        return result;
    }

    @Override
    public DeleteResult isDeletable(UUID specimenUuid, DeleteConfiguratorBase config) {
        DeleteResult deleteResult = new DeleteResult();
        SpecimenOrObservationBase specimen = this.load(specimenUuid);
        SpecimenDeleteConfigurator specimenDeleteConfigurator = (SpecimenDeleteConfigurator) config;

        // check elements found by super method
        Set<CdmBase> relatedObjects = super.isDeletable(specimenUuid, config).getRelatedObjects();
        for (CdmBase cdmBase : relatedObjects) {
            // check for type designation
            if (cdmBase.isInstanceOf(SpecimenTypeDesignation.class) && !specimenDeleteConfigurator.isDeleteFromTypeDesignation()) {
                deleteResult.setAbort();
                deleteResult.addException(new ReferencedObjectUndeletableException("Specimen or obeservation is a type specimen."));
                deleteResult.addRelatedObject(cdmBase);
                break;
            }
            // check for IndividualsAssociations
            else if (cdmBase.isInstanceOf(IndividualsAssociation.class) && !specimenDeleteConfigurator.isDeleteFromIndividualsAssociation()) {
                deleteResult.setAbort();
                deleteResult.addException(new ReferencedObjectUndeletableException("Specimen or obeservation is still associated via IndividualsAssociations"));
                deleteResult.addRelatedObject(cdmBase);
                break;
            }
            // check for taxon description
            else if(cdmBase.isInstanceOf(TaxonDescription.class)
                    && HibernateProxyHelper.deproxy(cdmBase, TaxonDescription.class).getDescribedSpecimenOrObservation().equals(specimen)
                    && !specimenDeleteConfigurator.isDeleteFromDescription()){
                deleteResult.setAbort();
                deleteResult.addException(new ReferencedObjectUndeletableException("Specimen or obeservation is still used as \"Described Specimen\" in a taxon description."));
                deleteResult.addRelatedObject(cdmBase);
                break;
            }
            // check for children and parents (derivation events)
            else if (cdmBase.isInstanceOf(DerivationEvent.class)) {
                DerivationEvent derivationEvent = HibernateProxyHelper.deproxy(cdmBase, DerivationEvent.class);
                // check if derivation event is empty
                if (!derivationEvent.getDerivatives().isEmpty() && derivationEvent.getOriginals().contains(specimen)) {
                    // if derivationEvent is the childEvent and contains derivations
//                    if (derivationEvent.getDerivatives().contains(specimen)) {
//                        //if it is the parent event the specimen is still deletable
//                        continue;
//                    }
                    if(!specimenDeleteConfigurator.isDeleteChildren()){
                        //if children should not be deleted then it is undeletable
                        deleteResult.setAbort();
                        deleteResult.addException(new ReferencedObjectUndeletableException("Specimen or obeservation still has child derivatives."));
                        deleteResult.addRelatedObject(cdmBase);
                        break;
                    }
                    else{
                        // check all children if they can be deleted
                        Set<DerivedUnit> derivatives = derivationEvent.getDerivatives();
                        DeleteResult childResult = new DeleteResult();
                        for (DerivedUnit derivedUnit : derivatives) {
                            childResult.includeResult(isDeletable(derivedUnit.getUuid(), specimenDeleteConfigurator));
                        }
                        if (!childResult.isOk()) {
                            deleteResult.setAbort();
                            deleteResult.includeResult(childResult);
                            deleteResult.addRelatedObject(cdmBase);
                            break;
                        }
                    }
                }
            }
            // check for amplification
            else if (cdmBase.isInstanceOf(AmplificationResult.class)
                    && !specimenDeleteConfigurator.isDeleteMolecularData()
                    && !specimenDeleteConfigurator.isDeleteChildren()) {
                deleteResult.setAbort();
                deleteResult.addException(new ReferencedObjectUndeletableException("DnaSample is used in amplification results."));
                deleteResult.addRelatedObject(cdmBase);
                break;
            }
            // check for sequence
            else if (cdmBase.isInstanceOf(Sequence.class)
                    && !specimenDeleteConfigurator.isDeleteMolecularData()
                    && !specimenDeleteConfigurator.isDeleteChildren()) {
                deleteResult.setAbort();
                deleteResult.addException(new ReferencedObjectUndeletableException("DnaSample is used in sequences."));
                deleteResult.addRelatedObject(cdmBase);
                break;
            }
        }
        if (deleteResult.isOk()) {
            //add all related object if deletion is OK so they can be handled by the delete() method
            deleteResult.addRelatedObjects(relatedObjects);
        }
        return deleteResult;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = false)
    @Override
    public DeleteResult delete(UUID specimenUuid, SpecimenDeleteConfigurator config) {
        return delete(load(specimenUuid), config);
    }


    @Transactional(readOnly = false)
    @Override
    public DeleteResult delete(SpecimenOrObservationBase<?> specimen, SpecimenDeleteConfigurator config) {
        specimen = HibernateProxyHelper.deproxy(specimen, SpecimenOrObservationBase.class);

        DeleteResult deleteResult = isDeletable(specimen.getUuid(), config);
        if (!deleteResult.isOk()) {
            return deleteResult;
        }

        if (config.isDeleteChildren()) {
            Set<DerivationEvent> derivationEvents = specimen.getDerivationEvents();
            //clone to avoid concurrent modification
            //can happen if the child is deleted and deleted its own derivedFrom event
            Set<DerivationEvent> derivationEventsClone = new HashSet<>(derivationEvents);
            for (DerivationEvent derivationEvent : derivationEventsClone) {
                Set<DerivedUnit> derivatives = derivationEvent.getDerivatives();
                Iterator<DerivedUnit> it = derivatives.iterator();
                Set<DerivedUnit> derivativesToDelete = new HashSet<>();
                while (it.hasNext()) {
                    DerivedUnit unit = it.next();
                    derivativesToDelete.add(unit);
                }
                for (DerivedUnit unit:derivativesToDelete){
                    deleteResult.includeResult(delete(unit, config));
                }
            }
        }




        // check related objects
        Set<CdmBase> relatedObjects = deleteResult.getRelatedObjects();

        for (CdmBase relatedObject : relatedObjects) {
            // check for TypeDesignations
            if (relatedObject.isInstanceOf(SpecimenTypeDesignation.class)) {
                SpecimenTypeDesignation designation = HibernateProxyHelper.deproxy(relatedObject, SpecimenTypeDesignation.class);
                designation.setTypeSpecimen(null);
                List<TaxonName> typifiedNames = new ArrayList<>();
                typifiedNames.addAll(designation.getTypifiedNames());
                for (TaxonName taxonName : typifiedNames) {
                    taxonName.removeTypeDesignation(designation);
                }
            }
            // delete IndividualsAssociation
            if (relatedObject.isInstanceOf(IndividualsAssociation.class)) {
                IndividualsAssociation association = HibernateProxyHelper.deproxy(relatedObject, IndividualsAssociation.class);
                association.setAssociatedSpecimenOrObservation(null);
                association.getInDescription().removeElement(association);
            }
            // check for "described specimen" (deprecated)
            if (relatedObject.isInstanceOf(TaxonDescription.class)) {
                TaxonDescription description = HibernateProxyHelper.deproxy(relatedObject, TaxonDescription.class);
                description.setDescribedSpecimenOrObservation(null);
            }
            // check for specimen description
            if (relatedObject.isInstanceOf(SpecimenDescription.class)) {
                SpecimenDescription specimenDescription = HibernateProxyHelper.deproxy(relatedObject, SpecimenDescription.class);
                specimenDescription.setDescribedSpecimenOrObservation(null);
                // check if description is a description of the given specimen
                if (specimen.getDescriptions().contains(specimenDescription)) {
                    specimen.removeDescription(specimenDescription);
                }
                DeleteResult descriptionDelete = descriptionService.isDeletable(specimenDescription.getUuid(), null);
                if (descriptionDelete.isOk()){
                    deleteResult.includeResult(descriptionService.delete(specimenDescription));
                }
            }
            // check for amplification
            if (relatedObject.isInstanceOf(AmplificationResult.class)) {
                AmplificationResult amplificationResult = HibernateProxyHelper.deproxy(relatedObject, AmplificationResult.class);
                amplificationResult.getDnaSample().removeAmplificationResult(amplificationResult);
            }
            // check for sequence
            if (relatedObject.isInstanceOf(Sequence.class)) {
                Sequence sequence = HibernateProxyHelper.deproxy(relatedObject, Sequence.class);
                sequence.getDnaSample().removeSequence(sequence);
            }
            // check for children and parents (derivation events)
            if (relatedObject.isInstanceOf(DerivationEvent.class)) {
                DerivationEvent derivationEvent = HibernateProxyHelper.deproxy(relatedObject, DerivationEvent.class);
                // parent derivation event (derivedFrom)
                if (derivationEvent.getDerivatives().contains(specimen) && specimen.isInstanceOf(DerivedUnit.class)) {
                    derivationEvent.removeDerivative(HibernateProxyHelper.deproxy(specimen, DerivedUnit.class));
                    if (derivationEvent.getDerivatives().isEmpty()) {
                        Set<SpecimenOrObservationBase> originals = derivationEvent.getOriginals();
                        for (SpecimenOrObservationBase specimenOrObservationBase : originals) {
                            specimenOrObservationBase.removeDerivationEvent(derivationEvent);
                            deleteResult.addUpdatedObject(specimenOrObservationBase);
                        }
                        // if derivationEvent has no derivates anymore, delete it
                        deleteResult.includeResult(eventService.delete(derivationEvent));
                    }
                }
                else{
                    //child derivation events should not occur since we delete the hierarchy from bottom to top
                }
            }
        }
        if (specimen instanceof FieldUnit){
            FieldUnit fieldUnit = HibernateProxyHelper.deproxy(specimen, FieldUnit.class);
            GatheringEvent event = fieldUnit.getGatheringEvent();
            fieldUnit.setGatheringEvent(null);
            if (event != null){
                DeleteResult result = eventService.isDeletable(event.getUuid(), null);
                if (result.isOk()){
                    deleteResult.includeResult( eventService.delete(event));
                }
            }

        }
        deleteResult.includeResult(delete(specimen));

        return deleteResult;
    }

    @Override
    public Collection<IndividualsAssociation> listIndividualsAssociations(SpecimenOrObservationBase<?> specimen, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        return dao.listIndividualsAssociations(specimen, limit, start, orderHints, propertyPaths);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<TaxonBase<?>> listAssociatedTaxa(SpecimenOrObservationBase<?> specimen, Integer limit,
            Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        return listAssociatedTaxa(specimen, INCLUDE_UNPUBLISHED, limit, start, orderHints, propertyPaths);
    }
    @Override
    public Collection<TaxonBase<?>> listAssociatedTaxa(SpecimenOrObservationBase<?> specimen, boolean includeUnpublished,
            Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        Collection<TaxonBase<?>> associatedTaxa = new HashSet<>();

        //individuals associations
        associatedTaxa.addAll(listIndividualsAssociationTaxa(specimen, includeUnpublished, limit, start, orderHints, propertyPaths));
        //type designation
        if(specimen.isInstanceOf(DerivedUnit.class)){
            associatedTaxa.addAll(listTypeDesignationTaxa(HibernateProxyHelper.deproxy(specimen, DerivedUnit.class),
                  includeUnpublished, limit, start, orderHints, propertyPaths));
        }
        //determinations
        associatedTaxa.addAll(listDeterminedTaxa(specimen, includeUnpublished, limit, start, orderHints, propertyPaths));

        return associatedTaxa;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<TaxonBase<?>> listDeterminedTaxa(SpecimenOrObservationBase<?> specimen, Integer limit,
            Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        return listDeterminedTaxa(specimen, INCLUDE_UNPUBLISHED, limit, start, orderHints, propertyPaths);
    }
    @Override
    public Collection<TaxonBase<?>> listDeterminedTaxa(SpecimenOrObservationBase<?> specimen, boolean includeUnpublished, Integer limit, Integer start,
            List<OrderHint> orderHints, List<String> propertyPaths) {
        Collection<TaxonBase<?>> associatedTaxa = new HashSet<>();
        for (DeterminationEvent determinationEvent : listDeterminationEvents(specimen, limit, start, orderHints, propertyPaths)) {
            if(determinationEvent.getIdentifiedUnit().equals(specimen)){
                if(determinationEvent.getTaxon()!=null){
                    associatedTaxa.add(taxonService.load(determinationEvent.getTaxon().getUuid(), includeUnpublished, propertyPaths));
                }
                if(determinationEvent.getTaxonName()!=null){
                    Collection<TaxonBase> taxonBases = determinationEvent.getTaxonName().getTaxonBases();
                    for (TaxonBase taxonBase : taxonBases) {
                        associatedTaxa.add(taxonService.load(taxonBase.getUuid(), includeUnpublished, propertyPaths));
                    }
                }
            }
        }
        return associatedTaxa;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<TaxonBase<?>> listTypeDesignationTaxa(DerivedUnit specimen, Integer limit, Integer start,
            List<OrderHint> orderHints, List<String> propertyPaths) {
        return listTypeDesignationTaxa(specimen, INCLUDE_UNPUBLISHED, limit, start, orderHints, propertyPaths);
    }
    @Override
    public Collection<TaxonBase<?>> listTypeDesignationTaxa(DerivedUnit specimen, boolean includeUnpublished,
            Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        Collection<TaxonBase<?>> associatedTaxa = new HashSet<>();
        for (SpecimenTypeDesignation typeDesignation : listTypeDesignations(specimen, limit, start, orderHints, propertyPaths)) {
            if(typeDesignation.getTypeSpecimen().equals(specimen)){
                Set<TaxonName> typifiedNames = typeDesignation.getTypifiedNames();
                for (TaxonName taxonName : typifiedNames) {
                    Set<Taxon> taxa = taxonName.getTaxa();
                    for (Taxon taxon : taxa) {
                        associatedTaxa.add(taxonService.load(taxon.getUuid(), includeUnpublished, propertyPaths));
                    }
                }
            }
        }
        return associatedTaxa;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<TaxonBase<?>> listIndividualsAssociationTaxa(SpecimenOrObservationBase<?> specimen, Integer limit,
            Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        return listIndividualsAssociationTaxa(specimen, INCLUDE_UNPUBLISHED, limit, start, orderHints, propertyPaths);
    }

    @Override
    public Collection<TaxonBase<?>> listIndividualsAssociationTaxa(SpecimenOrObservationBase<?> specimen, boolean includeUnpublished,
            Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        Collection<TaxonBase<?>> associatedTaxa = new HashSet<>();
        for (IndividualsAssociation individualsAssociation : listIndividualsAssociations(specimen, null, null, null, null)) {
            if(individualsAssociation.getInDescription().isInstanceOf(TaxonDescription.class)){
                TaxonDescription taxonDescription = HibernateProxyHelper.deproxy(individualsAssociation.getInDescription(), TaxonDescription.class);
                if(taxonDescription.getTaxon()!=null){
                    associatedTaxa.add(taxonService.load(taxonDescription.getTaxon().getUuid(), includeUnpublished, propertyPaths));
                }
            }
        }
        return associatedTaxa;
    }

    @Override
    public Collection<DeterminationEvent> listDeterminationEvents(SpecimenOrObservationBase<?> specimen,
            Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        return dao.listDeterminationEvents(specimen, limit, start, orderHints, propertyPaths);
    }

    @Override
    public Map<DerivedUnit, Collection<SpecimenTypeDesignation>> listTypeDesignations(
            Collection<DerivedUnit> specimens, Integer limit, Integer start,
            List<OrderHint> orderHints, List<String> propertyPaths) {
        Map<DerivedUnit, Collection<SpecimenTypeDesignation>> typeDesignationMap = new HashMap<>();
        for (DerivedUnit specimen : specimens) {
            Collection<SpecimenTypeDesignation> typeDesignations = listTypeDesignations(specimen, limit, start, orderHints, propertyPaths);
            typeDesignationMap.put(specimen, typeDesignations);
        }
        return typeDesignationMap;
    }

    @Override
    public Collection<SpecimenTypeDesignation> listTypeDesignations(DerivedUnit specimen,
            Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        return dao.listTypeDesignations(specimen, limit, start, orderHints, propertyPaths);
    }

    @Override
    public Collection<DescriptionBase<?>> listDescriptionsWithDescriptionSpecimen(
            SpecimenOrObservationBase<?> specimen, Integer limit, Integer start, List<OrderHint> orderHints,
            List<String> propertyPaths) {
        return dao.listDescriptionsWithDescriptionSpecimen(specimen, limit, start, orderHints, propertyPaths);
    }

    @Override
    @Deprecated //this is not a service layer task so it may be removed in future versions
    public Collection<DescriptionElementBase> getCharacterDataForSpecimen(SpecimenOrObservationBase<?> specimen) {
        if (specimen != null) {
            return specimen.characterData();
        }else{
            return new ArrayList<>();
        }
    }

    @Override
    public Collection<DescriptionElementBase> getCharacterDataForSpecimen(UUID specimenUuid) {
        SpecimenOrObservationBase<?> specimen = load(specimenUuid);
        if (specimen != null) {
            return getCharacterDataForSpecimen(specimen);
        }
        else{
            throw new DataRetrievalFailureException("Specimen with the given uuid not found in the data base");
        }
    }


    @Override
    public long countByTitle(IIdentifiableEntityServiceConfigurator<SpecimenOrObservationBase> config){
        if (config instanceof FindOccurrencesConfigurator) {
            FindOccurrencesConfigurator occurrenceConfig = (FindOccurrencesConfigurator) config;
            Taxon taxon = null;
            if(occurrenceConfig.getAssociatedTaxonUuid()!=null){
                TaxonBase<?> taxonBase = taxonService.load(occurrenceConfig.getAssociatedTaxonUuid());
                if(taxonBase.isInstanceOf(Taxon.class)){
                    taxon = HibernateProxyHelper.deproxy(taxonBase, Taxon.class);
                }
            }
            TaxonName taxonName = null;
            if(occurrenceConfig.getAssociatedTaxonNameUuid()!=null){
                taxonName = nameService.load(occurrenceConfig.getAssociatedTaxonNameUuid());
            }
            /*TODO: #6484 Neither isRetrieveIndirectlyAssociatedSpecimens() nor the AssignmentStatus
             * is currently reflected in the HQL query. So using these in the count method will
             * significantly slow down this method as we have to retreive the entities instead of
             * the just the amount.
             */
            if(occurrenceConfig.isRetrieveIndirectlyAssociatedSpecimens() || !occurrenceConfig.getAssignmentStatus().equals(AssignmentStatus.ALL_SPECIMENS)){
                List<SpecimenOrObservationBase> occurrences = new ArrayList<>();
                occurrences.addAll(dao.findOccurrences(occurrenceConfig.getClazz(),
                        occurrenceConfig.getTitleSearchStringSqlized(), occurrenceConfig.getSignificantIdentifier(),
                        occurrenceConfig.getSpecimenType(), taxon, taxonName, occurrenceConfig.getMatchMode(), null, null,
                        occurrenceConfig.getOrderHints(), occurrenceConfig.getPropertyPaths()));
                occurrences = filterOccurencesByAssignmentAndHierarchy(occurrenceConfig, occurrences, taxon, taxonName);
                return occurrences.size();
            }

            return dao.countOccurrences(occurrenceConfig.getClazz(),
                    occurrenceConfig.getTitleSearchStringSqlized(), occurrenceConfig.getSignificantIdentifier(),
                    occurrenceConfig.getSpecimenType(), taxon, taxonName, occurrenceConfig.getMatchMode(), null, null,
                    occurrenceConfig.getOrderHints(), occurrenceConfig.getPropertyPaths());
        }
        else{
            return super.countByTitle(config);
        }
    }

    @Override
    public Pager<UuidAndTitleCache<SpecimenOrObservationBase>> findByTitleUuidAndTitleCache(
            FindOccurrencesConfigurator config){
        List<UuidAndTitleCache<SpecimenOrObservationBase>> occurrences = new ArrayList<>();
        Taxon taxon = null;
        if(config.getAssociatedTaxonUuid()!=null){
            TaxonBase<?> taxonBase = taxonService.load(config.getAssociatedTaxonUuid());
            if(taxonBase.isInstanceOf(Taxon.class)){
                taxon = CdmBase.deproxy(taxonBase, Taxon.class);
            }
        }
        TaxonName taxonName = null;
        if(config.getAssociatedTaxonNameUuid()!=null){
            taxonName = nameService.load(config.getAssociatedTaxonNameUuid());
        }
        occurrences.addAll(dao.findOccurrencesUuidAndTitleCache(config.getClazz(),
                config.getTitleSearchString(), config.getSignificantIdentifier(),
                config.getSpecimenType(), taxon, taxonName, config.getMatchMode(), null, null,
                config.getOrderHints()));

        return new DefaultPagerImpl<>(config.getPageNumber(), occurrences.size(), config.getPageSize(), occurrences);
    }

    @Override
    public Pager<SpecimenOrObservationBase> findByTitle(
            IIdentifiableEntityServiceConfigurator<SpecimenOrObservationBase> config) {
        if (config instanceof FindOccurrencesConfigurator) {
            FindOccurrencesConfigurator occurrenceConfig = (FindOccurrencesConfigurator) config;
            List<SpecimenOrObservationBase> occurrences = new ArrayList<>();
            Taxon taxon = null;
            if(occurrenceConfig.getAssociatedTaxonUuid()!=null){
                TaxonBase<?> taxonBase = taxonService.load(occurrenceConfig.getAssociatedTaxonUuid());
                if(taxonBase.isInstanceOf(Taxon.class)){
                    taxon = HibernateProxyHelper.deproxy(taxonBase, Taxon.class);
                }
            }
            TaxonName taxonName = null;
            if(occurrenceConfig.getAssociatedTaxonNameUuid()!=null){
                taxonName = nameService.load(occurrenceConfig.getAssociatedTaxonNameUuid());
            }
            occurrences.addAll(dao.findOccurrences(occurrenceConfig.getClazz(),
                    occurrenceConfig.getTitleSearchString(), occurrenceConfig.getSignificantIdentifier(),
                    occurrenceConfig.getSpecimenType(), taxon, taxonName, occurrenceConfig.getMatchMode(), null, null,
                    occurrenceConfig.getOrderHints(), occurrenceConfig.getPropertyPaths()));
            occurrences = filterOccurencesByAssignmentAndHierarchy(occurrenceConfig, occurrences, taxon, taxonName);

            return new DefaultPagerImpl<>(config.getPageNumber(), occurrences.size(), config.getPageSize(), occurrences);
        }
        return super.findByTitle(config);
    }

    private List<SpecimenOrObservationBase> filterOccurencesByAssignmentAndHierarchy(
            FindOccurrencesConfigurator occurrenceConfig, List<SpecimenOrObservationBase> occurrences, Taxon taxon,
            TaxonName taxonName) {
        //filter out (un-)assigned specimens
        if(taxon==null && taxonName==null){
            AssignmentStatus assignmentStatus = occurrenceConfig.getAssignmentStatus();
            List<SpecimenOrObservationBase<?>> specimenWithAssociations = new ArrayList<>();
            if(!assignmentStatus.equals(AssignmentStatus.ALL_SPECIMENS)){
                for (SpecimenOrObservationBase<?> specimenOrObservationBase : occurrences) {
                    boolean includeUnpublished = true;  //TODO not sure if this is correct, maybe we have to propagate publish flag to higher methods.
                    Collection<TaxonBase<?>> associatedTaxa = listAssociatedTaxa(specimenOrObservationBase,
                            includeUnpublished, null, null, null, null);
                    if(!associatedTaxa.isEmpty()){
                        specimenWithAssociations.add(specimenOrObservationBase);
                    }
                }
            }
            if(assignmentStatus.equals(AssignmentStatus.UNASSIGNED_SPECIMENS)){
                occurrences.removeAll(specimenWithAssociations);
            }
            if(assignmentStatus.equals(AssignmentStatus.ASSIGNED_SPECIMENS)){
                occurrences = new ArrayList<>(specimenWithAssociations);
            }
        }
        // indirectly associated specimens
        if(occurrenceConfig.isRetrieveIndirectlyAssociatedSpecimens()){
            List<SpecimenOrObservationBase> indirectlyAssociatedOccurrences = new ArrayList<>(occurrences);
            for (SpecimenOrObservationBase<?> specimen : occurrences) {
                List<SpecimenOrObservationBase<?>> allHierarchyDerivates = getAllHierarchyDerivatives(specimen);
                for (SpecimenOrObservationBase<?> specimenOrObservationBase : allHierarchyDerivates) {
                    if(!occurrences.contains(specimenOrObservationBase)){
                        indirectlyAssociatedOccurrences.add(specimenOrObservationBase);
                    }
                }
            }
            occurrences = indirectlyAssociatedOccurrences;
        }
        return occurrences;
    }

    @Override
    public List<SpecimenOrObservationBase<?>> getAllHierarchyDerivatives(SpecimenOrObservationBase<?> specimen){
        List<SpecimenOrObservationBase<?>> allHierarchyDerivatives = new ArrayList<>();
        Collection<FieldUnit> fieldUnits = getFieldUnits(specimen.getUuid(), null);
        if(fieldUnits.isEmpty()){
            allHierarchyDerivatives.add(specimen);
            allHierarchyDerivatives.addAll(getAllChildDerivatives(specimen));
        }
        else{
            for (FieldUnit fieldUnit : fieldUnits) {
                allHierarchyDerivatives.add(fieldUnit);
                allHierarchyDerivatives.addAll(getAllChildDerivatives(fieldUnit));
            }
        }
        return allHierarchyDerivatives;
    }

    @Override
    public List<DerivedUnit> getAllChildDerivatives(UUID specimenUuid){
        return getAllChildDerivatives(load(specimenUuid));
    }

    @Override
    public List<DerivedUnit> getAllChildDerivatives(SpecimenOrObservationBase<?> specimen){
        if (specimen == null){
            return null;
        }
        List<DerivedUnit> childDerivate = new ArrayList<>();
        Set<DerivationEvent> derivationEvents = specimen.getDerivationEvents();
        for (DerivationEvent derivationEvent : derivationEvents) {
            Set<DerivedUnit> derivatives = derivationEvent.getDerivatives();
            for (DerivedUnit derivedUnit : derivatives) {
                childDerivate.add(derivedUnit);
                childDerivate.addAll(getAllChildDerivatives(derivedUnit.getUuid()));
            }
        }
        return childDerivate;
    }

    @Override
    public long countOccurrences(IIdentifiableEntityServiceConfigurator<SpecimenOrObservationBase> config){
        return countByTitle(config);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FieldUnit> getFieldUnitsForGatheringEvent(UUID gatheringEventUuid) {
        return dao.getFieldUnitsForGatheringEvent(gatheringEventUuid, null, null, null, null);
    }
}
