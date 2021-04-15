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
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.grouping.TopGroups;
import org.apache.lucene.util.BytesRef;
import org.hibernate.TransientObjectException;
import org.hibernate.search.spatial.impl.Rectangle;
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
import eu.etaxonomy.cdm.api.service.dto.DNASampleDTO;
import eu.etaxonomy.cdm.api.service.dto.DerivedUnitDTO;
import eu.etaxonomy.cdm.api.service.dto.FieldUnitDTO;
import eu.etaxonomy.cdm.api.service.dto.MediaDTO;
import eu.etaxonomy.cdm.api.service.dto.SpecimenOrObservationBaseDTO;
import eu.etaxonomy.cdm.api.service.dto.SpecimenOrObservationDTOFactory;
import eu.etaxonomy.cdm.api.service.exception.ReferencedObjectUndeletableException;
import eu.etaxonomy.cdm.api.service.molecular.ISequenceService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.AbstractPagerImpl;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.api.service.search.ILuceneIndexToolProvider;
import eu.etaxonomy.cdm.api.service.search.ISearchResultBuilder;
import eu.etaxonomy.cdm.api.service.search.LuceneParseException;
import eu.etaxonomy.cdm.api.service.search.LuceneSearch;
import eu.etaxonomy.cdm.api.service.search.QueryFactory;
import eu.etaxonomy.cdm.api.service.search.SearchResult;
import eu.etaxonomy.cdm.api.service.search.SearchResultBuilder;
import eu.etaxonomy.cdm.api.util.TaxonRelationshipEdge;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.CdmBaseType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.molecular.AmplificationResult;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.molecular.SingleRead;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonName;
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
import eu.etaxonomy.cdm.persistence.dao.initializer.AbstractBeanInitializer;
import eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao;
import eu.etaxonomy.cdm.persistence.dto.SpecimenNodeWrapper;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.AssignmentStatus;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

/**
 * @author a.babadshanjan
 * @since 01.09.2008
 */
@Service
@Transactional(readOnly = true)
public class OccurrenceServiceImpl
        extends IdentifiableServiceBase<SpecimenOrObservationBase, IOccurrenceDao>
        implements IOccurrenceService {

    static private final Logger logger = Logger.getLogger(OccurrenceServiceImpl.class);

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
    private AbstractBeanInitializer<?> beanInitializer;

    @Autowired
    private ILuceneIndexToolProvider luceneIndexToolProvider;

    public OccurrenceServiceImpl() {
        logger.debug("Load OccurrenceService Bean");
    }

    @Override
    @Transactional(readOnly = false)
    public UpdateResult updateCaches(Class<? extends SpecimenOrObservationBase> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<SpecimenOrObservationBase> cacheStrategy, IProgressMonitor monitor) {
        if (clazz == null) {
            clazz = SpecimenOrObservationBase.class;
        }
        return super.updateCachesImpl(clazz, stepSize, cacheStrategy, monitor);
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
    public Pager<MediaDTO> getMediaDTOs(SpecimenOrObservationBase<?> occurence, Integer pageSize, Integer pageNumber) {
        long numberOfResults = dao.countMedia(occurence);

        List<Media> results = new ArrayList<>();
        if(AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)) {
            results = dao.getMedia(occurence, pageSize, pageNumber, null);
        }
        List<MediaDTO> mediaDTOs = results.stream()
                .map(m -> MediaDTO.fromEntity(m))
                .flatMap(dtos -> dtos.stream())
                .collect(Collectors.toList()
                );
        return new DefaultPagerImpl<MediaDTO>(pageNumber, numberOfResults, pageSize, mediaDTOs);
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
    public DerivedUnitFacade getDerivedUnitFacade(DerivedUnit derivedUnit, List<String> derivedUnitFacadeInitStrategy) throws DerivedUnitFacadeNotSupportedException {
        derivedUnit = (DerivedUnit) dao.load(derivedUnit.getUuid(), null);
        DerivedUnitFacadeConfigurator config = DerivedUnitFacadeConfigurator.NewInstance();
        config.setThrowExceptionForNonSpecimenPreservationMethodRequest(false);
        DerivedUnitFacade derivedUnitFacade = DerivedUnitFacade.NewInstance(derivedUnit, config);
        beanInitializer.initialize(derivedUnitFacade, derivedUnitFacadeInitStrategy);
        return derivedUnitFacade;
    }

    @Override
    public List<DerivedUnitFacade> listDerivedUnitFacades(
            DescriptionBase description, List<String> derivedUnitFacadeInitStrategy) {

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

        beanInitializer.initializeAll(derivedUnitFacadeList, derivedUnitFacadeInitStrategy);

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
    @Deprecated
    public Collection<FieldUnit> listFieldUnitsByAssociatedTaxon(Taxon associatedTaxon, List<OrderHint> orderHints, List<String> propertyPaths) {
        return pageRootUnitsByAssociatedTaxon(FieldUnit.class, null, associatedTaxon, null, null, null, null, propertyPaths).getRecords();
    }

    @Override
    public <T extends SpecimenOrObservationBase> Collection<T> listRootUnitsByAssociatedTaxon(Class<T> type, Taxon associatedTaxon, List<OrderHint> orderHints, List<String> propertyPaths) {
        return pageRootUnitsByAssociatedTaxon(type, null, associatedTaxon, null, null, null, null, propertyPaths).getRecords();
    }

    @Override
    public <T extends SpecimenOrObservationBase> Pager<T> pageRootUnitsByAssociatedTaxon(Class<T> type, Set<TaxonRelationshipEdge> includeRelationships,
            Taxon associatedTaxon, Integer maxDepth, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,
            List<String> propertyPaths) {

        if (!getSession().contains(associatedTaxon)) {
            associatedTaxon = (Taxon) taxonService.load(associatedTaxon.getUuid());
        }

        // gather the IDs of all relevant root units
        Set<UUID> rootUnitUuids = new HashSet<>();
        List<SpecimenOrObservationBase> records = listByAssociatedTaxon(null, includeRelationships, associatedTaxon, maxDepth, null, null, orderHints, propertyPaths);
        for (SpecimenOrObservationBase<?> specimen : records) {
            for (SpecimenOrObservationBase<?> rootUnit : findRootUnits(specimen.getUuid(), null)) {
                if(type == null || type.isAssignableFrom(rootUnit.getClass())) {
                    rootUnitUuids.add(rootUnit.getUuid());
                }
            }
        }
        //dao.list() does the paging of the field units. Passing the field units directly to the Pager would not work
        List<SpecimenOrObservationBase> rootUnits = dao.list(rootUnitUuids, pageSize, pageNumber, orderHints, propertyPaths);
        List<T> castedUnits = new ArrayList<>(rootUnits.size());
        for(SpecimenOrObservationBase sob : rootUnits) {
            // this cast should be save since the uuids have been filtered by type above
            castedUnits.add((T)sob);
        }
        return new DefaultPagerImpl<T>(pageNumber, (long)castedUnits.size(), pageSize, castedUnits);
    }

    @Override
    public FieldUnitDTO assembleFieldUnitDTO(FieldUnit fieldUnit) {

        if (!getSession().contains(fieldUnit)) {
            fieldUnit = (FieldUnit) load(fieldUnit.getUuid());
        }
        // FIXME the filter for SpecimenOrObservationType.PreservedSpecimen has been preserved from the former implementation (see commit 07e3f63c7d  and older)
        // it is questionable if this filter makes sense for all use cases or if it is only a sensible default for the
        // compressed specimen table in the cdm-dataportal (see #6816, #6870)
        EnumSet<SpecimenOrObservationType> typeIncludeFilter = EnumSet.of(SpecimenOrObservationType.PreservedSpecimen);
        FieldUnitDTO fieldUnitDTO = FieldUnitDTO.fromEntity(fieldUnit, null, typeIncludeFilter);
        return fieldUnitDTO;
    }


    @Override
    @Transactional
    public DerivedUnitDTO assembleDerivedUnitDTO(DerivedUnit derivedUnit) {

        if (!getSession().contains(derivedUnit)) {
            derivedUnit = (DerivedUnit) load(derivedUnit.getUuid());
        }
        DerivedUnitDTO derivedUnitDTO = DerivedUnitDTO.fromEntity(derivedUnit, null, null, null);

        // individuals associations
        Collection<IndividualsAssociation> individualsAssociations = listIndividualsAssociations(derivedUnit, null, null, null, null);
        if(individualsAssociations != null) {
            for (IndividualsAssociation individualsAssociation : individualsAssociations) {
                if (individualsAssociation.getInDescription() != null) {
                    if (individualsAssociation.getInDescription().isInstanceOf(TaxonDescription.class)) {
                        TaxonDescription taxonDescription = HibernateProxyHelper.deproxy(individualsAssociation.getInDescription(), TaxonDescription.class);
                        Taxon taxon = taxonDescription.getTaxon();
                        if (taxon != null) {
                            derivedUnitDTO.addAssociatedTaxon(taxon);
                        }
                    }
                }
            }
        }

        return derivedUnitDTO;
    }

    @Override
    @Deprecated
    public String getMostSignificantIdentifier(DerivedUnit derivedUnit) {
        return derivedUnit.getMostSignificantIdentifier();
    }

    /**
     * TODO there is a very similar function in {@link SpecimenOrObservationBaseDTO#assembleDerivative}.
     * If possible we should avoid using this function here by the method in <code>SpecimenOrObservationBaseDTO</code>.
     */
    private Set<DerivedUnitDTO> getDerivedUnitDTOsFor(SpecimenOrObservationBaseDTO specimenDto, DerivedUnit specimen,
            HashMap<UUID, SpecimenOrObservationBaseDTO> alreadyCollectedSpecimen) {

        Set<DerivedUnitDTO> derivedUnits = new HashSet<>();
        for (DerivationEvent derivationEvent : specimen.getDerivationEvents()) {
            for (DerivedUnit derivative : derivationEvent.getDerivatives()) {
                if (!alreadyCollectedSpecimen.containsKey(specimenDto.getUuid())){
                    DerivedUnitDTO dto = (DerivedUnitDTO) SpecimenOrObservationDTOFactory.fromEntity(derivative, 0);
                    alreadyCollectedSpecimen.put(dto.getUuid(), dto);
                    dto.addAllDerivatives(getDerivedUnitDTOsFor(dto, derivative, alreadyCollectedSpecimen));
                    derivedUnits.add(dto);
                } else {
                    if(alreadyCollectedSpecimen.get(specimenDto.getUuid()).getDerivatives().isEmpty() && !derivative.getDerivationEvents().isEmpty()) {
                        // we need to add the missing derivatives!
                        SpecimenOrObservationBaseDTO dto = alreadyCollectedSpecimen.get(specimenDto.getUuid());
                        alreadyCollectedSpecimen.get(specimenDto.getUuid()).addAllDerivatives(getDerivedUnitDTOsFor(dto, derivative, alreadyCollectedSpecimen));
                    }
                }
            }
        }
        return derivedUnits;
    }

//    private Set<DerivateDTO> getDerivedUnitDTOsFor(DerivateDTO specimenDto, DerivedUnit specimen, HashMap<UUID, DerivateDTO> alreadyCollectedSpecimen) {
//        Set<DerivateDTO> derivedUnits = new HashSet<>();
////        load
//        for (DerivationEvent derivationEvent : specimen.getDerivationEvents()) {
//            for (DerivedUnit derivative : derivationEvent.getDerivatives()) {
//                if (!alreadyCollectedSpecimen.containsKey(specimenDto.getUuid())){
//                    PreservedSpecimenDTO dto;
//                    if (derivative instanceof DnaSample){
//                        dto = DNASampleDTO.newInstance(derivative);
//                    }else{
//                        dto = PreservedSpecimenDTO.newInstance(derivative);
//                    }
//                    alreadyCollectedSpecimen.put(dto.getUuid(), dto);
//                    dto.addAllDerivates(getDerivedUnitDTOsFor(dto, derivative, alreadyCollectedSpecimen));
//                    derivedUnits.add(dto);
//                }
//            }
//        }
//        return derivedUnits;
//    }


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
        occurrences = (List<T>) dao.loadList(occurrenceIds, null, propertyPaths);

        return new DefaultPagerImpl<T>(pageNumber, Long.valueOf(occurrences.size()), pageSize, occurrences);

    }

    @Override
    public <T extends SpecimenOrObservationBase> Pager<T> pageByAssociatedTaxon(Class<T> type, Set<TaxonRelationshipEdge> includeRelationships,
            String taxonUUID, Integer maxDepth, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {

        UUID uuid = UUID.fromString(taxonUUID);
        Taxon taxon = (Taxon) taxonService.load(uuid);
        return pageByAssociatedTaxon(type, includeRelationships, taxon, maxDepth, pageSize, pageNumber, orderHints, propertyPaths);

    }

    @Override
    @Transactional
    public List<SpecimenOrObservationBaseDTO> listRootUnitDTOsByAssociatedTaxon(Set<TaxonRelationshipEdge> includedRelationships,
            UUID associatedTaxonUuid, List<String> propertyPaths) {

        Set<Taxon> taxa = new HashSet<>();
        Set<SpecimenOrObservationBaseDTO> rootUnitDTOs = new HashSet<>();
        boolean includeUnpublished = INCLUDE_UNPUBLISHED;

        Taxon associatedTaxon = (Taxon) taxonService.load(associatedTaxonUuid);
        if (includedRelationships != null) {
            taxa = taxonService.listRelatedTaxa(associatedTaxon, includedRelationships, null, includeUnpublished, null, null, null);
        }
        taxa.add(associatedTaxon);

        HashMap<UUID, SpecimenOrObservationBaseDTO> alreadyCollectedUnits = new HashMap<>();
        for (Taxon taxon : taxa) {
            // TODO there might be a good potential to speed up the whole processing by collecting all entities first
            // and to create the DTOs in a second step
            Set<SpecimenOrObservationBase> perTaxonOccurrences = dao.listByAssociatedTaxon(null, taxon, null, null, null, propertyPaths)
                    .stream()
                    .map(u -> HibernateProxyHelper.deproxy(u, SpecimenOrObservationBase.class))
                    .collect(Collectors.toSet());
            for (SpecimenOrObservationBase<?> unit : perTaxonOccurrences) {
                unit = HibernateProxyHelper.deproxy(unit);
                if (unit instanceof DerivedUnit){
                    DerivedUnitDTO derivativeDTO;
                    if (!alreadyCollectedUnits.containsKey(unit.getUuid())){
                        DerivedUnit derivedUnit = (DerivedUnit)unit;
                        derivativeDTO = (DerivedUnitDTO) SpecimenOrObservationDTOFactory.fromEntity(derivedUnit, null);
                        if (unit instanceof DnaSample) {
                            derivativeDTO = DNASampleDTO.fromEntity((DnaSample)unit);
                        } else {
                            derivativeDTO = DerivedUnitDTO.fromEntity(derivedUnit, null, null, null);
                        }
                        alreadyCollectedUnits.put(derivativeDTO.getUuid(), derivativeDTO);
                        derivativeDTO.addAllDerivatives(getDerivedUnitDTOsFor(derivativeDTO, derivedUnit, alreadyCollectedUnits));
                    }
                    derivativeDTO = (DerivedUnitDTO) alreadyCollectedUnits.get(unit.getUuid());
                    rootUnitDTOs.addAll(findRootUnitDTOs(derivativeDTO, alreadyCollectedUnits));
                } else {
                    // only other option is FieldUnit
                    rootUnitDTOs.add(FieldUnitDTO.fromEntity((FieldUnit)unit, 0, null));
                }
            }
        }

        List<SpecimenOrObservationBaseDTO> orderdDTOs = new ArrayList<>(rootUnitDTOs);
        // TODO order dtos by date can only be done by string comparison
        // the FieldUnitDTO.date needs to be a Partial object for sensible ordering
        Collections.sort(orderdDTOs, new Comparator<SpecimenOrObservationBaseDTO>() {

            @Override
            public int compare(SpecimenOrObservationBaseDTO o1, SpecimenOrObservationBaseDTO o2) {
                if(o1 instanceof FieldUnitDTO && o2 instanceof FieldUnitDTO) {
                    FieldUnitDTO fu1 = (FieldUnitDTO)o1;
                    FieldUnitDTO fu2 = (FieldUnitDTO)o2;
                    if(fu1.getDate() == null && fu2.getDate() == null) {
                        return 0;
                    }
                    if(fu1.getDate() != null && fu2.getDate() == null) {
                        return 1;
                    }
                    if(fu1.getDate() == null && fu2.getDate() != null) {
                        return -1;
                    }
                    return fu1.getDate().compareTo(fu2.getDate());
                }
                if(o1 instanceof DerivedUnitDTO && o2 instanceof DerivedUnitDTO) {
                    SpecimenOrObservationBaseDTO du1 = o1;
                    SpecimenOrObservationBaseDTO du2 = o2;
                    return StringUtils.compare(du1.getLabel(), du2.getLabel());
                 }
                if(o1 instanceof FieldUnitDTO && o2 instanceof DerivedUnitDTO) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });

        return orderdDTOs;
    }

    @Override
    @Transactional
    @Deprecated
    public  SpecimenOrObservationBaseDTO findByAccessionNumber(String accessionNumberString, List<OrderHint> orderHints)  {
        return findByAccessionNumber(accessionNumberString, orderHints);
    }

    @Override
    @Transactional
    public  SpecimenOrObservationBaseDTO findByGeneticAccessionNumber(String accessionNumberString, List<OrderHint> orderHints)  {

        DnaSample dnaSample = dao.findByGeneticAccessionNumber(accessionNumberString, null);
        DerivedUnitDTO dnaSampleDTO;
        if (dnaSample != null){
            dnaSampleDTO = new DNASampleDTO(dnaSample);
            Collection<SpecimenOrObservationBaseDTO> fieldUnitDTOs = this.findRootUnitDTOs(dnaSampleDTO, new HashMap<>());
            // FIXME change return type to Collection<FieldUnitDTO>
            if(fieldUnitDTOs.isEmpty()) {
                return null;
            } else {
               return fieldUnitDTOs.iterator().next();
            }
        }
        return null;
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
    @Transactional(readOnly=true)
    public Collection<FieldUnit> findFieldUnits(UUID derivedUnitUuid, List<String> propertyPaths) {
        //It will search recursively over all {@link DerivationEvent}s and get the "originals" ({@link SpecimenOrObservationBase})
        //from which this DerivedUnit was derived until all FieldUnits are found.

        // FIXME: use HQL queries to avoid entity instantiation and to increase performance

        SpecimenOrObservationBase<?> specimen = load(derivedUnitUuid);
        Collection<FieldUnit> fieldUnits = new ArrayList<>();
        if (specimen == null){
            return null;
        }
        if (specimen.isInstanceOf(FieldUnit.class)) {
            fieldUnits.add(HibernateProxyHelper.deproxy(specimen, FieldUnit.class));
        }
        else if(specimen.isInstanceOf(DerivedUnit.class)){
            fieldUnits.addAll(HibernateProxyHelper.deproxy(specimen, DerivedUnit.class).collectRootUnits(FieldUnit.class));
        }

        fieldUnits = beanInitializer.initializeAll(fieldUnits, propertyPaths);
        return fieldUnits;
    }

    @Override
    @Transactional(readOnly=true)
    public Collection<SpecimenOrObservationBase> findRootUnits(UUID derivedUnitUuid, List<String> propertyPaths) {

        // FIXME: use HQL queries to avoid entity instantiation and to increase performance

        SpecimenOrObservationBase<?> specimen = load(derivedUnitUuid);
        Collection<SpecimenOrObservationBase> rootUnits = new ArrayList<>();
        if (specimen == null){
            return null;
        }
        if (specimen.isInstanceOf(FieldUnit.class)) {
            rootUnits.add(HibernateProxyHelper.deproxy(specimen, FieldUnit.class));
        }
        else if(specimen.isInstanceOf(DerivedUnit.class)){
            rootUnits.addAll(HibernateProxyHelper.deproxy(specimen, DerivedUnit.class).collectRootUnits(SpecimenOrObservationBase.class));
        }

        rootUnits = beanInitializer.initializeAll(rootUnits, propertyPaths);
        return rootUnits;
    }

    @Override
    public Collection<SpecimenOrObservationBaseDTO> findRootUnitDTOs(UUID unitUUID) {


        SpecimenOrObservationBase<?> entity = load(unitUUID);
        SpecimenOrObservationBaseDTO derivedUnitDTO = SpecimenOrObservationDTOFactory.fromEntity(entity);
        Collection<SpecimenOrObservationBaseDTO> rootUnitDTOs = new ArrayList<>();
        if(derivedUnitDTO != null) {
            if(derivedUnitDTO instanceof FieldUnitDTO) {
                rootUnitDTOs.add(derivedUnitDTO);
            } else {
                Map<UUID, SpecimenOrObservationBaseDTO> alreadyCollectedSpecimen = new HashMap<>();
                rootUnitDTOs  = findRootUnitDTOs((DerivedUnitDTO)derivedUnitDTO, alreadyCollectedSpecimen);
            }
        }

        return rootUnitDTOs;

    }

    /**
     * Recursively searches all {@link DerivationEvent}s to find all "originals" ({@link SpecimenOrObservationBase})
     * from which this DerivedUnit was derived until all FieldUnits are found.
     * <p>
     * <b>NOTE:</b> The recursive search still is a bit incomplete and may miss originals in the rare case where a
     * derivative has more than one original. (see https://dev.e-taxonomy.eu/redmine/issues/9253)
     *
     * @param derivedUnitDTO
     *  The DerivedUnitDTO to start the search from.
     * @param alreadyCollectedSpecimen
     *  A map to hold all originals that have been sees during the recursive walk.
     * @return
     *  The collection of all Field Units that are accessible from the derivative from where the search was started.
     */
    public Collection<SpecimenOrObservationBaseDTO> findRootUnitDTOs(DerivedUnitDTO derivedUnitDTO,
            Map<UUID, SpecimenOrObservationBaseDTO> alreadyCollectedSpecimen) {

        HashMap<UUID, SpecimenOrObservationBaseDTO> rootUnitDTOs = new HashMap<>();
        _findRootUnitDTOs(derivedUnitDTO, rootUnitDTOs, alreadyCollectedSpecimen);
        return rootUnitDTOs.values();

    }

    /**
     * Method for recursive calls, must only be used by {@link #findRootUnitDTOs(DerivedUnitDTO, HashMap)}
     * <p>
     * It will search recursively over all {@link DerivationEvent}s and get the "originals" ({@link SpecimenOrObservationBase})
     * from which this DerivedUnit was derived until all FieldUnits are found.
     */
    private void _findRootUnitDTOs(DerivedUnitDTO derivedUnitDTO, Map<UUID, SpecimenOrObservationBaseDTO> rootUnitDTOs,
                Map<UUID, SpecimenOrObservationBaseDTO> alreadyCollectedSpecimen) {

        List<String> propertyPaths = new ArrayList<>();

        // add the supplied DTO the the alreadyCollectedSpecimen if not yet there
        if(!alreadyCollectedSpecimen.containsKey(derivedUnitDTO.getUuid())) {
            alreadyCollectedSpecimen.put(derivedUnitDTO.getUuid(), derivedUnitDTO);
        }

        List<SpecimenOrObservationBase> originals = dao.findOriginalsForDerivedUnit(derivedUnitDTO.getUuid(), propertyPaths);
        if (originals.size() > 0){
            if (originals.size() > 1){
                logger.warn("The derived unit with uuid " + derivedUnitDTO.getUuid() + "has more than one orginal");
            }
            // FIXME allow handling multiple originals
            SpecimenOrObservationBase<?> original = originals.get(0);
            original = HibernateProxyHelper.deproxy(original);

            if (alreadyCollectedSpecimen.containsKey(original.getUuid())){
                alreadyCollectedSpecimen.get(original.getUuid()).addDerivative(derivedUnitDTO);
    //            if ( alreadyCollectedSpecimen.get(specimen.getUuid()) instanceof FieldUnitDTO){
    //                ((FieldUnitDTO)alreadyCollectedSpecimen.get(specimen.getUuid())).getTaxonRelatedDerivedUnits().add(derivedUnitDTO.getUuid());
    //            }
            }else{
                if(!rootUnitDTOs.containsKey(original.getUuid())){
                    // the direct derivatives of the field unit are added in the factory method, so it is guaranteed that
                    // the derivedUnitDTO is already contained.
                    // ----
                    // Don't assemble derivatives for the field unit, since we have them collected already
                    // when ascending to the originals, we only want to collect those derivatives which are on the path up to the root
                    final Integer maxDepth = 0;
                    SpecimenOrObservationBaseDTO originalDTO = SpecimenOrObservationDTOFactory.fromEntity(original, maxDepth);
                    originalDTO.addDerivative(derivedUnitDTO);
                    alreadyCollectedSpecimen.put(originalDTO.getUuid(), originalDTO);
                    if (original instanceof FieldUnit){
                        rootUnitDTOs.put(originalDTO.getUuid(), originalDTO);
                    }else{
                        _findRootUnitDTOs((DerivedUnitDTO) originalDTO, rootUnitDTOs, alreadyCollectedSpecimen);
                    }
                } else {
                    SpecimenOrObservationBaseDTO previouslyFoundRootUnit = rootUnitDTOs.get(original.getUuid());
                    if(!previouslyFoundRootUnit.getDerivatives().stream().anyMatch(uDTO -> uDTO.getUuid().equals(derivedUnitDTO.getUuid()))) {
                        previouslyFoundRootUnit.addDerivative(derivedUnitDTO);
                    }
                }
            }
        } else {
            rootUnitDTOs.put(derivedUnitDTO.getUuid(), derivedUnitDTO);
        }

    }

    @Override
    @Transactional(readOnly=true)
    public FieldUnitDTO loadFieldUnitDTO(UUID derivedUnitUuid) {

        FieldUnitDTO fieldUnitDTO = null;
        DerivedUnitDTO derivedUnitDTO = null;

        Map<UUID, SpecimenOrObservationBaseDTO> cycleDetectionMap = new HashMap<>();
        SpecimenOrObservationBase<?> derivative = dao.load(derivedUnitUuid);
        if(derivative != null){
            if (derivative instanceof FieldUnit){
                fieldUnitDTO = FieldUnitDTO.fromEntity((FieldUnit)derivative);
                return fieldUnitDTO;
            } else {
                // must be a DerivedUnit otherwise
                derivedUnitDTO = DerivedUnitDTO.fromEntity((DerivedUnit)derivative);
                while(true){

                    Set<SpecimenOrObservationBaseDTO> originals = originalDTOs(derivedUnitDTO.getUuid());

                    if(originals.isEmpty()){
                        break;
                    }
                    if (originals.size() > 1){
                        logger.debug("The derived unit with uuid " + derivedUnitUuid + "has more than one orginal, ignoring all but the first one.");
                    }

                    SpecimenOrObservationBaseDTO originalDTO = originals.iterator().next();

                    // cycle detection and handling
                    if(cycleDetectionMap.containsKey(originalDTO.getUuid())){
                        // cycle detected!!!
                        try {
                            throw new Exception();
                        } catch(Exception e){
                            logger.error("Cycle in derivate graph detected at DerivedUnit with uuid=" + originalDTO.getUuid() , e);
                        }
                        // to solve the situation for the output we remove the derivate from the more distant graph node
                        // by removing it from the derivatives of its original
                        // but let the derivate to be added to the original which is closer to the FieldUnit (below at originalDTO.addDerivate(derivedUnitDTO);)
                        for(SpecimenOrObservationBaseDTO seenOriginal: cycleDetectionMap.values()){
                            for(SpecimenOrObservationBaseDTO derivateDTO : seenOriginal.getDerivatives()){
                                if(derivateDTO.equals(originalDTO)){
                                    seenOriginal.getDerivatives().remove(originalDTO);
                                }
                            }
                        }
                    } else {
                        cycleDetectionMap.put(originalDTO.getUuid(), originalDTO);
                    }

                    if (originalDTO instanceof FieldUnitDTO){
                        fieldUnitDTO = (FieldUnitDTO)originalDTO;
                        break;
                    }else{
                        // So this must be a DerivedUnitDTO
                        if (derivedUnitDTO == null){
                            derivedUnitDTO = (DerivedUnitDTO)originalDTO;
                        } else {
                            derivedUnitDTO = (DerivedUnitDTO)originalDTO;
                        }
                    }
                }
            }
        }
        return fieldUnitDTO;

    }

    /**
     * @param originalDTO
     * @return
     */
    private Set<SpecimenOrObservationBaseDTO> originalDTOs(UUID derivativeUuid) {

        Set<SpecimenOrObservationBaseDTO> dtos = new HashSet<>();
        List<SpecimenOrObservationBase> specimens = dao.findOriginalsForDerivedUnit(derivativeUuid, null);
        for(SpecimenOrObservationBase sob : specimens){
            if(sob instanceof FieldUnit) {
                dtos.add(FieldUnitDTO.fromEntity((FieldUnit)sob));
            } else {
                dtos.add(DerivedUnitDTO.fromEntity((DerivedUnit)sob));
            }
        }
        return dtos;
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
    public Collection<DescriptionElementBase> getCharacterDataForSpecimen(UUID specimenUuid) {
        SpecimenOrObservationBase<?> specimen = load(specimenUuid);
        if (specimen != null) {
            return specimen.characterData();
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
             * significantly slow down this method as we have to retrieve the entities instead of
             * just the amount.
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
    public List<DerivedUnitDTO> findByTitleDerivedUnitDTO(FindOccurrencesConfigurator config) {
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
        List<DerivedUnit> occurrences = new ArrayList<>();
        occurrences.addAll(dao.findOccurrences(DerivedUnit.class,
                config.getTitleSearchString(), config.getSignificantIdentifier(),
                config.getSpecimenType(), taxon, taxonName, config.getMatchMode(), null, null,
                config.getOrderHints(), null));

        List<DerivedUnitDTO> dtos = new ArrayList<>();
        occurrences.forEach(derivedUnit->dtos.add(assembleDerivedUnitDTO(derivedUnit)));
        return dtos;
    }

    @Override
    public <S extends SpecimenOrObservationBase> Pager<S> findByTitle(
            IIdentifiableEntityServiceConfigurator<S> config) {
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
            List<? extends SpecimenOrObservationBase> foundOccurrences = dao.findOccurrences(occurrenceConfig.getClazz(),
                    occurrenceConfig.getTitleSearchString(), occurrenceConfig.getSignificantIdentifier(),
                    occurrenceConfig.getSpecimenType(), taxon, taxonName, occurrenceConfig.getMatchMode(), null, null,
                    occurrenceConfig.getOrderHints(), occurrenceConfig.getPropertyPaths());
            occurrences.addAll(foundOccurrences);
            occurrences = filterOccurencesByAssignmentAndHierarchy(occurrenceConfig, occurrences, taxon, taxonName);

            return new DefaultPagerImpl<>(config.getPageNumber(), occurrences.size(), config.getPageSize(), (List<S>)occurrences);
        }
        return super.findByTitle(config);
    }

    private List<SpecimenOrObservationBase> filterOccurencesByAssignmentAndHierarchy(
            FindOccurrencesConfigurator occurrenceConfig, List<SpecimenOrObservationBase> occurrences, Taxon taxon,
            TaxonName taxonName) {
        //filter out (un-)assigned specimens
        if(taxon==null && taxonName==null){
            AssignmentStatus assignmentStatus = occurrenceConfig.getAssignmentStatus();
            List<SpecimenOrObservationBase> specimenWithAssociations = new ArrayList<>();
            if(!assignmentStatus.equals(AssignmentStatus.ALL_SPECIMENS)){
                for (SpecimenOrObservationBase specimenOrObservationBase : occurrences) {
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
        Collection<FieldUnit> fieldUnits = findFieldUnits(specimen.getUuid(), null);
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

    @Override
    public List<FieldUnit> findFieldUnitsForGatheringEvent(UUID gatheringEventUuid) {
        return dao.findFieldUnitsForGatheringEvent(gatheringEventUuid, null, null, null, null);
    }

    @Override
    public List<Point> findPointsForFieldUnitList(List<UUID> fieldUnitUuids) {
        return dao.findPointsForFieldUnitList(fieldUnitUuids);
    }
}