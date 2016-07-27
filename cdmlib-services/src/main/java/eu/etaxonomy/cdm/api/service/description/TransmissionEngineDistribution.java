// $Id$
/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.description;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.search.Search;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.common.DynamicBatch;
import eu.etaxonomy.cdm.common.JvmLimitsException;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.NullProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.SubProgressMonitor;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dto.ClassificationLookupDTO;

/**
 *
 * <h2>GENERAL NOTES </h2>
 * <em>TODO: These notes are directly taken from original Transmission Engine Occurrence
 * version 14 written in Visual Basic and still need to be
 * adapted to the java version of the transmission engine!</em>
 *
 * <h3>summaryStatus</h3>
 *
 *   Each distribution information has a summaryStatus, this is an summary of the status codes
 *   as stored in the fields of emOccurrence native, introduced, cultivated, ...
 *   The summaryStatus seems to be equivalent to  the CDM DistributionStatus
 *
 * <h3>map generation</h3>
 *
 *   When generating maps from the accumulated distribution information some special cases have to be handled:
 * <ol>
 *   <li>if a entered or imported status information exist for the same area for which calculated (accumulated)
 *       data is available, the calculated data has to be given preference over other data.
 *   </li>
 *   <li>If there is an area with a sub area and both areas have the same calculated status only the subarea
 *       status should be shown in the map, whereas the super area should be ignored.
 *   </li>
 * </ol>
 *
 * @author Anton Güntsch (author of original Transmission Engine Occurrence version 14 written in Visual Basic)
 * @author Andreas Kohlbecker (2013, porting Transmission Engine Occurrence to Java)
 * @date Feb 22, 2013
 */
@Service
public class TransmissionEngineDistribution { //TODO extends IoBase?


    public static final String EXTENSION_VALUE_PREFIX = "transmissionEngineDistribution.priority:";

    public static final Logger logger = Logger.getLogger(TransmissionEngineDistribution.class);

    /**
     * only used for performance testing
     */
    final boolean ONLY_FISRT_BATCH = false;


    protected static final List<String> TAXONDESCRIPTION_INIT_STRATEGY = Arrays.asList(new String [] {
            "description.markers.markerType",
            "description.elements.markers.markerType",
            "description.elements.area",
            "description.elements.status",
            "description.elements.sources.citation.authorship",
//            "description.elements.sources.nameUsedInSource",
//            "description.elements.multilanguageText",
//            "name.status.type",
    });


    /**
     * A map which contains the status terms as key and the priority as value
     * The map will contain both, the PresenceTerms and the AbsenceTerms
     */
    private Map<PresenceAbsenceTerm, Integer> statusPriorityMap = null;

    @Autowired
    private IDescriptionService descriptionService;

    @Autowired
    private ITermService termService;

    @Autowired
    private ITaxonService taxonService;

    @Autowired
    private IClassificationService classificationService;

    @Autowired
    private INameService mameService;

    @Autowired
    private HibernateTransactionManager transactionManager;

    private List<PresenceAbsenceTerm> byAreaIgnoreStatusList = null;

    private List<PresenceAbsenceTerm> byRankIgnoreStatusList = null;

    private final Map<NamedArea, Set<NamedArea>> subAreaMap = new HashMap<NamedArea, Set<NamedArea>>();

    int byRankTicks = 300;
    int byAreasTicks = 100;


    private static final long BATCH_MIN_FREE_HEAP = 800  * 1024 * 1024;
    /**
     * ratio of the initially free heap which should not be used
     * during the batch processing. This amount of the heap is reserved
     * for the flushing of the session and to the index
     */
    private static final double BATCH_FREE_HEAP_RATIO = 0.9;
    private static final int BATCH_SIZE_BY_AREA = 1000;
    private static final int BATCH_SIZE_BY_RANK = 500;

    private long batchMinFreeHeap = BATCH_MIN_FREE_HEAP;



    /**
     * byAreaIgnoreStatusList contains by default:
     *  <ul>
     *    <li>AbsenceTerm.CULTIVATED_REPORTED_IN_ERROR()</li>
     *    <li>AbsenceTerm.INTRODUCED_REPORTED_IN_ERROR()</li>
     *    <li>AbsenceTerm.INTRODUCED_FORMERLY_INTRODUCED()</li>
     *    <li>AbsenceTerm.NATIVE_REPORTED_IN_ERROR()</li>
     *    <li>AbsenceTerm.NATIVE_FORMERLY_NATIVE()</li>
     *  </ul>
     *
     * @return the byAreaIgnoreStatusList
     */
    public List<PresenceAbsenceTerm> getByAreaIgnoreStatusList() {
        if(byAreaIgnoreStatusList == null ){
            byAreaIgnoreStatusList = Arrays.asList(
                    new PresenceAbsenceTerm[] {
                    		PresenceAbsenceTerm.CULTIVATED_REPORTED_IN_ERROR(),
                    		PresenceAbsenceTerm.INTRODUCED_REPORTED_IN_ERROR(),
                    		PresenceAbsenceTerm.NATIVE_REPORTED_IN_ERROR(),
                    		PresenceAbsenceTerm.INTRODUCED_FORMERLY_INTRODUCED(),
                    		PresenceAbsenceTerm.NATIVE_FORMERLY_NATIVE()
                            // TODO what about PresenceAbsenceTerm.ABSENT() also ignore?
                    });
        }
        return byAreaIgnoreStatusList;
    }

    /**
     * @param byAreaIgnoreStatusList the byAreaIgnoreStatusList to set
     */
    public void setByAreaIgnoreStatusList(List<PresenceAbsenceTerm> byAreaIgnoreStatusList) {
        this.byAreaIgnoreStatusList = byAreaIgnoreStatusList;
    }

    /**
     * byRankIgnoreStatusList contains by default
     *  <ul>
     *    <li>PresenceTerm.ENDEMIC_FOR_THE_RELEVANT_AREA()</li>
     *  </ul>
     *
     * @return the byRankIgnoreStatusList
     */
    public List<PresenceAbsenceTerm> getByRankIgnoreStatusList() {

        if (byRankIgnoreStatusList == null) {
            byRankIgnoreStatusList = Arrays.asList(
                    new PresenceAbsenceTerm[] {
                    		PresenceAbsenceTerm.ENDEMIC_FOR_THE_RELEVANT_AREA()
                    });
        }
        return byRankIgnoreStatusList;
    }

    /**
     * @param byRankIgnoreStatusList the byRankIgnoreStatusList to set
     */
    public void setByRankIgnoreStatusList(List<PresenceAbsenceTerm> byRankIgnoreStatusList) {
        this.byRankIgnoreStatusList = byRankIgnoreStatusList;
    }

    /**
     *
     * @param superAreas
     */
    public TransmissionEngineDistribution() {
    }

    /**
     * initializes the map which contains the status terms as key and the priority as value
     * The map will contain both, the PresenceTerms and the AbsenceTerms
     */
    private void initializeStatusPriorityMap() {

        statusPriorityMap = new HashMap<PresenceAbsenceTerm, Integer>();
        Integer priority;

        // PresenceTerms
        for(PresenceAbsenceTerm term : termService.list(PresenceAbsenceTerm.class, null, null, null, null)){
            priority = getPriorityFor(term);
            if(priority != null){
                statusPriorityMap.put(term, priority);
            }
        }
    }

    /**
     * Compares the PresenceAbsenceTermBase terms contained in <code>a.status</code> and <code>b.status</code> after
     * the priority as stored in the statusPriorityMap. The StatusAndSources object with
     * the higher priority is returned. In the case of <code>a == b</code> the sources of b will be added to the sources
     * of a.
     *
     * If either a or b or the status are null b or a is returned.
     *
     * @see initializeStatusPriorityMap()
     *
     * @param a
     * @param b
     * @param sourcesForWinnerB
     *  In the case when <code>b</code> is preferred over <code>a</code> these Set of sources will be added to the sources of <code>b</code>
     * @return
     */
    private StatusAndSources choosePreferred(StatusAndSources a, StatusAndSources b, Set<DescriptionElementSource> sourcesForWinnerB){

        if (statusPriorityMap == null) {
            initializeStatusPriorityMap();
        }

        if (b == null || b.status == null) {
            return a;
        }
        if (a == null || a.status == null) {
            return b;
        }

        if (statusPriorityMap.get(a.status) == null) {
            logger.warn("No priority found in map for " + a.status.getLabel());
            return b;
        }
        if (statusPriorityMap.get(b.status) == null) {
            logger.warn("No priority found in map for " + b.status.getLabel());
            return a;
        }
        if(statusPriorityMap.get(a.status) < statusPriorityMap.get(b.status)){
            if(sourcesForWinnerB != null) {
                b.addSources(sourcesForWinnerB);
            }
            return b;
        } else if (statusPriorityMap.get(a.status) == statusPriorityMap.get(b.status)){
            a.addSources(b.sources);
            return a;
        } else {
            return a;
        }
    }

    /**
     * reads the priority for the given status term from the extensions.
     *
     * @param term
     * @return the priority value
     */
    private Integer getPriorityFor(DefinedTermBase<?> term) {
        Set<Extension> extensions = term.getExtensions();
        for(Extension extension : extensions){
            if(!extension.getType().equals(ExtensionType.ORDER())) {
                continue;
            }
            int pos = extension.getValue().indexOf(EXTENSION_VALUE_PREFIX);
            if(pos == 0){ // if starts with EXTENSION_VALUE_PREFIX
                try {
                    Integer priority = Integer.valueOf(extension.getValue().substring(EXTENSION_VALUE_PREFIX.length()));
                    return priority;
                } catch (NumberFormatException e) {
                    logger.warn("Invalid number format in Extension:" + extension.getValue());
                }
            }
        }
        logger.warn("no priority defined for '" + term.getLabel() + "'");
        return null;
    }

    /**
     * runs both steps
     * <ul>
     * <li>Step 1: Accumulate occurrence records by area</li>
     * <li>Step 2: Accumulate by ranks starting from lower rank to upper rank,
     * the status of all children are accumulated on each rank starting from
     * lower rank to upper rank.</li>
     * </ul>
     *
     * @param superAreas
     *            the areas to which the subordinate areas should be projected.
     * @param lowerRank
     * @param upperRank
     * @param classification
     * @param classification
     *            limit the accumulation process to a specific classification
     *            (not yet implemented)
     * @param monitor
     *            the progress monitor to use for reporting progress to the
     *            user. It is the caller's responsibility to call done() on the
     *            given monitor. Accepts null, indicating that no progress
     *            should be reported and that the operation cannot be cancelled.
     */
    public void accumulate(AggregationMode mode, List<NamedArea> superAreas, Rank lowerRank, Rank upperRank,
            Classification classification, IProgressMonitor monitor) throws JvmLimitsException {

        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }

        // only for debugging:
        //logger.setLevel(Level.TRACE); // TRACE will slow down a lot since it forces loading all term representations
        //Logger.getLogger("org.hibernate.SQL").setLevel(Level.DEBUG);

        logger.info("Hibernate JDBC Batch size: "
                + ((SessionFactoryImplementor) getSession().getSessionFactory()).getSettings().getJdbcBatchSize());

        Set<Classification> classifications = new HashSet<Classification>();
        if(classification == null) {
            classifications.addAll(classificationService.listClassifications(null, null, null, null));
        } else {
            classifications.add(classification);
        }

        int aggregationWorkTicks;
        switch(mode){
        case byAreasAndRanks:
            aggregationWorkTicks = byAreasTicks + byRankTicks;
            break;
        case byAreas:
            aggregationWorkTicks = byAreasTicks;
            break;
        case byRanks:
            aggregationWorkTicks = byRankTicks;
            break;
        default:
            aggregationWorkTicks = 0;
            break;
        }

        // take start time for performance testing
        // NOTE: use ONLY_FISRT_BATCH = true to measure only one batch
        double start = System.currentTimeMillis();

        monitor.beginTask("Accumulating distributions", (classifications.size() * aggregationWorkTicks) + 1 );

        updatePriorities();

        List<Rank> ranks = rankInterval(lowerRank, upperRank);

        monitor.worked(1);


        for(Classification _classification : classifications) {

            ClassificationLookupDTO classificationLookupDao = classificationService.classificationLookup(_classification);
            classificationLookupDao.filterInclude(ranks);

            double end1 = System.currentTimeMillis();
            logger.info("Time elapsed for classificationLookup() : " + (end1 - start) / (1000) + "s");
            double start2 = System.currentTimeMillis();

            monitor.subTask("Accumulating distributions to super areas for " + _classification.getTitleCache());
            if (mode.equals(AggregationMode.byAreas) || mode.equals(AggregationMode.byAreasAndRanks)) {
                accumulateByArea(superAreas, classificationLookupDao, new SubProgressMonitor(monitor, byAreasTicks), true);
            }
            monitor.subTask("Accumulating distributions to higher ranks for " + _classification.getTitleCache());

            double end2 = System.currentTimeMillis();
            logger.info("Time elapsed for accumulateByArea() : " + (end2 - start2) / (1000) + "s");

            double start3 = System.currentTimeMillis();
            if (mode.equals(AggregationMode.byRanks) || mode.equals(AggregationMode.byAreasAndRanks)) {
                accumulateByRank(ranks, classificationLookupDao, new SubProgressMonitor(monitor, byRankTicks), mode.equals(AggregationMode.byRanks));
            }

            double end3 = System.currentTimeMillis();
            logger.info("Time elapsed for accumulateByRank() : " + (end3 - start3) / (1000) + "s");
            logger.info("Time elapsed for accumulate(): " + (end3 - start) / (1000) + "s");

            if(ONLY_FISRT_BATCH) {
                monitor.done();
                break;
            }
        }
        monitor.done();
    }


    /**
     * Step 1: Accumulate occurrence records by area
     * <ul>
     * <li>areas are projected to super areas e.g.:  HS <-- HS(A), HS(G), HS(S)</li>
     * <li>super areas do initially not have a status set ==> Prerequisite to check in CDM</li>
     * <li>areas having a summary status of summary value different from {@link #getByAreaIgnoreStatusList()} are ignored</li>
     * <li>areas have a priority value, the status of the area with highest priority determines the status of the super area</li>
     * <li>the source references of the accumulated distributions are also accumulated into the new distribution,,</li>
     * <li>this has been especially implemented for the EuroMed Checklist Vol2 and might not be a general requirement</li>
     * </ul>
     *
     * @param superAreas
     *      the areas to which the subordinate areas should be projected
     * @param classificationLookupDao
     * @throws JvmLimitsException
     *
     */
    protected void accumulateByArea(List<NamedArea> superAreas, ClassificationLookupDTO classificationLookupDao,  IProgressMonitor subMonitor, boolean doClearDescriptions) throws JvmLimitsException {

        DynamicBatch batch = new DynamicBatch(BATCH_SIZE_BY_AREA, batchMinFreeHeap);
        batch.setRequiredFreeHeap(BATCH_FREE_HEAP_RATIO);

        TransactionStatus txStatus = startTransaction(false);

        // reload superAreas TODO is it faster to getSession().merge(object) ??
        Set<UUID> superAreaUuids = new HashSet<UUID>(superAreas.size());
        for (NamedArea superArea : superAreas){
            superAreaUuids.add(superArea.getUuid());
        }

        // visit all accepted taxa
        subMonitor.beginTask("Accumulating by area ",  classificationLookupDao.getTaxonIds().size());
        Iterator<Integer> taxonIdIterator = classificationLookupDao.getTaxonIds().iterator();

        while (taxonIdIterator.hasNext() || batch.hasUnprocessedItems()) {

            if(txStatus == null) {
                // transaction has been comitted at the end of this batch, start a new one
                txStatus = startTransaction(false);
            }

            // the session is cleared after each batch, so load the superAreaList for each batch
            List<NamedArea> superAreaList = (List)termService.find(superAreaUuids);

            // load taxa for this batch
            List<Integer> taxonIds = batch.nextItems(taxonIdIterator);
//            logger.debug("accumulateByArea() - taxon " + taxonPager.getFirstRecord() + " to " + taxonPager.getLastRecord() + " of " + taxonPager.getCount() + "]");
            List<TaxonBase> taxa = taxonService.loadByIds(taxonIds, TAXONDESCRIPTION_INIT_STRATEGY);

            // iterate over the taxa and accumulate areas
            // start processing the new batch

            for(TaxonBase taxonBase : taxa) {
                if(logger.isDebugEnabled()){
                    logger.debug("accumulateByArea() - taxon :" + taxonToString(taxonBase));
                }

                batch.incementCounter();

                Taxon taxon = (Taxon)taxonBase;
                TaxonDescription description = findComputedDescription(taxon, doClearDescriptions);
                List<Distribution> distributions = distributionsFor(taxon);

                // Step through superAreas for accumulation of subAreas
                for (NamedArea superArea : superAreaList){

                    // accumulate all sub area status
                    StatusAndSources accumulatedStatusAndSources = null;
                    // TODO consider using the TermHierarchyLookup (only in local branch a.kohlbecker)
                    Set<NamedArea> subAreas = getSubAreasFor(superArea);
                    for(NamedArea subArea : subAreas){
                        if(logger.isTraceEnabled()){
                            logger.trace("accumulateByArea() - \t\t" + termToString(subArea));
                        }
                        // step through all distributions for the given subArea
                        for(Distribution distribution : distributions){
                            if(distribution.getArea() != null && distribution.getArea().equals(subArea) && distribution.getStatus() != null) {
                                PresenceAbsenceTerm status = distribution.getStatus();
                                if(logger.isTraceEnabled()){
                                    logger.trace("accumulateByArea() - \t\t" + termToString(subArea) + ": " + termToString(status));
                                }
                                // skip all having a status value different of those in byAreaIgnoreStatusList
                                if (getByAreaIgnoreStatusList().contains(status)){
                                    continue;
                                }
                                StatusAndSources subStatusAndSources = new StatusAndSources(status, distribution.getSources());
                                accumulatedStatusAndSources = choosePreferred(accumulatedStatusAndSources, subStatusAndSources, null);
                            }
                        }
                    } // next sub area
                    if (accumulatedStatusAndSources != null) {
                        if(logger.isDebugEnabled()){
                            logger.debug("accumulateByArea() - \t >> " + termToString(superArea) + ": " + termToString(accumulatedStatusAndSources.status));
                        }
                        // store new distribution element for superArea in taxon description
                        Distribution newDistribitionElement = Distribution.NewInstance(superArea, accumulatedStatusAndSources.status);
                        newDistribitionElement.getSources().addAll(accumulatedStatusAndSources.sources);
                        newDistribitionElement.addMarker(Marker.NewInstance(MarkerType.COMPUTED(), true));
                        description.addElement(newDistribitionElement);
                    }

                } // next super area ....

                descriptionService.saveOrUpdate(description);
                taxonService.saveOrUpdate(taxon);
                subMonitor.worked(1);
                if(!batch.isWithinJvmLimits()) {
                    break; // flushAndClear and start with new batch
                }

            } // next taxon

            flushAndClear();

            // commit for every batch, otherwise the persistent context
            // may grow too much and eats up all the heap
            commitTransaction(txStatus);
            txStatus = null;

            if(ONLY_FISRT_BATCH) {
                break;
            }

        } // next batch of taxa

        subMonitor.done();
    }

   /**
    * Step 2: Accumulate by ranks starting from lower rank to upper rank, the status of all children
    * are accumulated on each rank starting from lower rank to upper rank.
    * <ul>
    * <li>aggregate distribution of included taxa of the next lower rank for any rank level starting from the lower rank (e.g. sub species)
    *    up to upper rank (e.g. Genus)</li>
    *  <li>the accumulation id done for each distribution area found in the included taxa</li>
    *  <li>areas of subtaxa with status endemic are ignored</li>
    *  <li>the status with the highest priority determines the value for the accumulated distribution</li>
    *  <li>the source reference of the accumulated distributions are also accumulated into the new distribution,
    *    this has been especially implemented for the EuroMed Checklist Vol2 and might not be a general requirement</li>
    *</ul>
 * @throws JvmLimitsException
    */
    protected void accumulateByRank(List<Rank> rankInterval, ClassificationLookupDTO classificationLookupDao,  IProgressMonitor subMonitor, boolean doClearDescriptions) throws JvmLimitsException {

        DynamicBatch batch = new DynamicBatch(BATCH_SIZE_BY_RANK, batchMinFreeHeap);
        batch.setRequiredFreeHeap(BATCH_FREE_HEAP_RATIO);
        batch.setMaxAllowedGcIncreases(10);

        int ticksPerRank = 100;

        TransactionStatus txStatus = startTransaction(false);

        // the loadRankSpecificRootNodes() method not only finds
        // taxa of the specified rank but also taxa of lower ranks
        // if no taxon of the specified rank exists, so we need to
        // remember which taxa have been processed already
        Set<Integer> taxaProcessedIds = new HashSet<Integer>();
        List<TaxonBase> taxa = null;
        List<TaxonBase> childTaxa = null;

        List<Rank> ranks = rankInterval;

        subMonitor.beginTask("Accumulating by rank", ranks.size() * ticksPerRank);

        for (Rank rank : ranks) {

            if(logger.isDebugEnabled()){
                logger.debug("accumulateByRank() - at Rank '" + termToString(rank) + "'");
            }

            Set<Integer> taxonIdsPerRank = classificationLookupDao.getTaxonIdByRank().get(rank);

            int taxonCountperRank = taxonIdsPerRank != null ? taxonIdsPerRank.size() : 0;

            SubProgressMonitor taxonSubMonitor = new SubProgressMonitor(subMonitor, ticksPerRank);
            taxonSubMonitor.beginTask("Accumulating by rank " + termToString(rank), taxonCountperRank);

            if(taxonCountperRank == 0) {
                taxonSubMonitor.done();
                continue;
            }


            Iterator<Integer> taxonIdIterator = taxonIdsPerRank.iterator();
            while (taxonIdIterator.hasNext() || batch.hasUnprocessedItems()) {

                if(txStatus == null) {
                    // transaction has been committed at the end of this batch, start a new one
                    txStatus = startTransaction(false);
                }

                // load taxa for this batch
                List<Integer> taxonIds = batch.nextItems(taxonIdIterator);

                taxa = taxonService.loadByIds(taxonIds, null);

//                if(logger.isDebugEnabled()){
//                           logger.debug("accumulateByRank() - taxon " + taxonPager.getFirstRecord() + " to " + taxonPager.getLastRecord() + " of " + taxonPager.getCount() + "]");
//                }

                for(TaxonBase taxonBase : taxa) {

                    batch.incementCounter();

                    Taxon taxon = (Taxon)taxonBase;
                    if (taxaProcessedIds.contains(taxon.getId())) {
                        if(logger.isDebugEnabled()){
                            logger.debug("accumulateByRank() - skipping already processed taxon :" + taxonToString(taxon));
                        }
                        continue;
                    }
                    taxaProcessedIds.add(taxon.getId());
                    if(logger.isDebugEnabled()){
                        logger.debug("accumulateByRank() [" + rank.getLabel() + "] - taxon :" + taxonToString(taxon));
                    }

                    // Step through direct taxonomic children for accumulation
                    Map<NamedArea, StatusAndSources> accumulatedStatusMap = new HashMap<NamedArea, StatusAndSources>();

                    List<Integer> childTaxonIds = new ArrayList<>();
                    Set<Integer> childSet = classificationLookupDao.getChildTaxonMap().get(taxon.getId());
                    if(childSet != null) {
                        childTaxonIds.addAll(childSet);
                    }
                    if(!childTaxonIds.isEmpty()) {
                        childTaxa = taxonService.loadByIds(childTaxonIds, TAXONDESCRIPTION_INIT_STRATEGY);
                        LinkedList<TaxonBase> childStack = new LinkedList<TaxonBase>(childTaxa);
                        childTaxa = null; // allow to be garbage collected

                        while(childStack.size() > 0){

                            TaxonBase childTaxonBase = childStack.pop();
                            getSession().setReadOnly(childTaxonBase, true);

                            Taxon childTaxon = (Taxon) childTaxonBase;
                            getSession().setReadOnly(childTaxon, true);
                            if(logger.isTraceEnabled()){
                                logger.trace("                   subtaxon :" + taxonToString(childTaxon));
                            }

                            for(Distribution distribution : distributionsFor(childTaxon) ) {
                                PresenceAbsenceTerm status = distribution.getStatus();
                                NamedArea area = distribution.getArea();
                                if (status == null || getByRankIgnoreStatusList().contains(status)){
                                  continue;
                                }

                                StatusAndSources subStatusAndSources = new StatusAndSources(status, distribution.getSources());
                                accumulatedStatusMap.put(area, choosePreferred(accumulatedStatusMap.get(area), subStatusAndSources, null));
                             }

                            // evict all initialized entities of the childTaxon
                            // TODO consider using cascade="evict" in the model classes
//                            for( TaxonDescription description : ((Taxon)childTaxonBase).getDescriptions()) {
//                                for (DescriptionElementBase deb : description.getElements()) {
//                                    getSession().evict(deb);
//                                }
//                                getSession().evict(description); // this causes in some cases the taxon object to be detached from the session
//                            }
                            getSession().evict(childTaxonBase); // no longer needed, save heap
                        }

                        if(accumulatedStatusMap.size() > 0) {
                            TaxonDescription description = findComputedDescription(taxon, doClearDescriptions);
                            for (NamedArea area : accumulatedStatusMap.keySet()) {
                                Distribution distribition = findDistribution(description, area, accumulatedStatusMap.get(area).status);
                                if(distribition == null) {
                                    // create a new distribution element
                                    distribition = Distribution.NewInstance(area, accumulatedStatusMap.get(area).status);
                                    distribition.addMarker(Marker.NewInstance(MarkerType.COMPUTED(), true));
                                }
                                addSourcesDeduplicated(distribition.getSources(), accumulatedStatusMap.get(area).sources);

                                description.addElement(distribition);
                            }
                            taxonService.saveOrUpdate(taxon);
                            descriptionService.saveOrUpdate(description);
                        }

                    }
                    taxonSubMonitor.worked(1); // one taxon worked
                    if(!batch.isWithinJvmLimits()) {
                        break; // flushAndClear and start with new batch
                    }

                } // next taxon ....

                flushAndClear();

                // commit for every batch, otherwise the persistent context
                // may grow too much and eats up all the heap
                commitTransaction(txStatus);
                txStatus = null;

                // flushing the session and to the index (flushAndClear() ) can impose a
                // massive heap consumption. therefore we explicitly do a check after the
                // flush to detect these situations and to reduce the batch size.
                if(batch.getJvmMonitor().getGCRateSiceLastCheck() > 0.05) {
                    batch.reduceSize(0.5);
                }

                if(ONLY_FISRT_BATCH) {
                    break;
                }
            } // next batch

            taxonSubMonitor.done();
            subMonitor.worked(1);

            if(ONLY_FISRT_BATCH) {
                break;
            }
        } // next Rank

        logger.info("accumulateByRank() - done");
        subMonitor.done();
    }

/**
 * @param description
 * @param area
 * @param status
 * @return
 */
private Distribution findDistribution(TaxonDescription description, NamedArea area, PresenceAbsenceTerm status) {
    for(DescriptionElementBase item : description.getElements()) {
        if(!(item instanceof Distribution)) {
            continue;
        }
        Distribution distribution = ((Distribution)item);
        if(distribution.getArea().equals(area) && distribution.getStatus().equals(status)) {
            return distribution;
        }
    }
    return null;
}

/**
 * @param lowerRank
 * @param upperRank
 * @return
 */
private List<Rank> rankInterval(Rank lowerRank, Rank upperRank) {

    TransactionStatus txStatus = startTransaction(false);
    Rank currentRank = lowerRank;
    List<Rank> ranks = new ArrayList<Rank>();
    ranks.add(currentRank);
    while (!currentRank.isHigher(upperRank)) {
        currentRank = findNextHigherRank(currentRank);
        ranks.add(currentRank);
    }
    commitTransaction(txStatus);
    txStatus = null;
    return ranks;
}

    /**
     * @return
     */
    private Session getSession() {
        return descriptionService.getSession();
    }

    /**
     *
     */
    private void flush() {
        logger.debug("flushing session ...");
        getSession().flush();
        try {
            logger.debug("flushing to indexes ...");
            Search.getFullTextSession(getSession()).flushToIndexes();
        } catch (HibernateException e) {
            /* IGNORE - Hibernate Search Event listeners not configured ... */
            if(!e.getMessage().startsWith("Hibernate Search Event listeners not configured")){
                throw e;
            }
        }
    }

    /**
    *
    */
   private void flushAndClear() {
       flush();
       logger.debug("clearing session ...");
       getSession().clear();
   }


    // TODO merge with CdmApplicationDefaultConfiguration#startTransaction() into common base class
    public TransactionStatus startTransaction(Boolean readOnly) {

        DefaultTransactionDefinition defaultTxDef = new DefaultTransactionDefinition();
        defaultTxDef.setReadOnly(readOnly);
        TransactionDefinition txDef = defaultTxDef;

        // Log some transaction-related debug information.
        if (logger.isTraceEnabled()) {
            logger.trace("Transaction name = " + txDef.getName());
            logger.trace("Transaction facets:");
            logger.trace("Propagation behavior = " + txDef.getPropagationBehavior());
            logger.trace("Isolation level = " + txDef.getIsolationLevel());
            logger.trace("Timeout = " + txDef.getTimeout());
            logger.trace("Read Only = " + txDef.isReadOnly());
            // org.springframework.orm.hibernate5.HibernateTransactionManager
            // provides more transaction/session-related debug information.
        }

        TransactionStatus txStatus = transactionManager.getTransaction(txDef);

        getSession().setFlushMode(FlushMode.COMMIT);

        return txStatus;
    }

    // TODO merge with CdmApplicationDefaultConfiguration#startTransaction() into common base class
    public void commitTransaction(TransactionStatus txStatus){
        logger.debug("commiting transaction ...");
        transactionManager.commit(txStatus);
        return;
    }

    /**
     * returns the next higher rank
     *
     * TODO better implement OrderedTermBase.getNextHigherTerm() and OrderedTermBase.getNextLowerTerm()?
     *
     * @param rank
     * @return
     */
    private Rank findNextHigherRank(Rank rank) {
        rank = (Rank) termService.load(rank.getUuid());
        return rank.getNextHigherTerm();
//        OrderedTermVocabulary<Rank> rankVocabulary = mameService.getRankVocabulary();;
//        return rankVocabulary.getNextHigherTerm(rank);
    }

    /**
     * Either finds an existing taxon description of the given taxon or creates a new one.
     * If the doClear is set all existing description elements will be cleared.
     *
     * @param taxon
     * @param doClear will remove all existing Distributions if the taxon already
     * has a MarkerType.COMPUTED() TaxonDescription
     * @return
     */
    private TaxonDescription findComputedDescription(Taxon taxon, boolean doClear) {

        String descriptionTitle = this.getClass().getSimpleName();

        // find existing one
        for (TaxonDescription description : taxon.getDescriptions()) {
            if (description.hasMarker(MarkerType.COMPUTED(), true)) {
                logger.debug("reusing computed description for " + taxon.getTitleCache());
                if (doClear) {
                    int deleteCount = 0;
                    Set<DescriptionElementBase> deleteCandidates = new HashSet<DescriptionElementBase>();
                    for (DescriptionElementBase descriptionElement : description.getElements()) {
                        if(descriptionElement instanceof Distribution) {
                            deleteCandidates.add(descriptionElement);
                        }
                    }
                    if(deleteCandidates.size() > 0){
                        for(DescriptionElementBase descriptionElement : deleteCandidates) {
                            description.removeElement(descriptionElement);
                            descriptionService.deleteDescriptionElement(descriptionElement);
                            descriptionElement = null;
                            deleteCount++;
                        }
                        descriptionService.saveOrUpdate(description);
                        logger.debug("\t" + deleteCount +" distributions cleared");
                    }

                }
                return description;
            }
        }

        // create a new one
        logger.debug("creating new description for " + taxon.getTitleCache());
        TaxonDescription description = TaxonDescription.NewInstance(taxon);
        description.setTitleCache(descriptionTitle, true);
        description.addMarker(Marker.NewInstance(MarkerType.COMPUTED(), true));
        return description;
    }

    /**
     * @param superArea
     * @return
     */
    private Set<NamedArea> getSubAreasFor(NamedArea superArea) {

        if(!subAreaMap.containsKey(superArea)) {
            if(logger.isDebugEnabled()){
                logger.debug("loading included areas for " + superArea.getLabel());
            }
            subAreaMap.put(superArea, superArea.getIncludes());
        }
        return subAreaMap.get(superArea);
    }

    /**
     * @param taxon
     * @return
     */
    private List<Distribution> distributionsFor(Taxon taxon) {
        List<Distribution> distributions = new ArrayList<Distribution>();
        for(TaxonDescription description: taxon.getDescriptions()) {
            readOnlyIfInSession(description);
            for(DescriptionElementBase deb : description.getElements()) {
                if(deb instanceof Distribution) {
                    readOnlyIfInSession(deb);
                    distributions.add((Distribution)deb);
                }
            }
        }
        return distributions;
    }

    /**
     * This method avoids problems when running the TransmissionEngineDistribution test.
     * For some unknown reason entities are not in the PersitenceContext even if they are
     * loaded by a service method. Setting these entities to readonly would raise a
     * TransientObjectException("Instance was not associated with this persistence context")
     *
     * @param entity
     */
    private void readOnlyIfInSession(CdmBase entity) {
        if(getSession().contains(entity)) {
            getSession().setReadOnly(entity, true);
        }
    }

    /**
     * @param taxon
     * @param logger2
     * @return
     */
    private String taxonToString(TaxonBase taxon) {
        if(logger.isTraceEnabled()) {
            return taxon.getTitleCache();
        } else {
            return taxon.toString();
        }
    }

    /**
     * @param taxon
     * @param logger2
     * @return
     */
    private String termToString(OrderedTermBase<?> term) {
        if(logger.isTraceEnabled()) {
            return term.getLabel() + " [" + term.getIdInVocabulary() + "]";
        } else {
            return term.getIdInVocabulary();
        }
    }

    /**
     * Sets the priorities for presence and absence terms, the priorities are stored in extensions.
     * This method will start a new transaction and commits it after the work is done.
     */
    public void updatePriorities() {

        TransactionStatus txStatus = startTransaction(false);

        Map<PresenceAbsenceTerm, Integer> priorityMap = new HashMap<PresenceAbsenceTerm, Integer>();

        priorityMap.put(PresenceAbsenceTerm.CULTIVATED_REPORTED_IN_ERROR(), 1);
        priorityMap.put(PresenceAbsenceTerm.INTRODUCED_UNCERTAIN_DEGREE_OF_NATURALISATION(), 2);
        priorityMap.put(PresenceAbsenceTerm.INTRODUCED_FORMERLY_INTRODUCED(), 3);
        priorityMap.put(PresenceAbsenceTerm.INTRODUCED_REPORTED_IN_ERROR(), 20);
        priorityMap.put(PresenceAbsenceTerm.NATIVE_REPORTED_IN_ERROR(), 30);
        priorityMap.put(PresenceAbsenceTerm.CULTIVATED(), 45);
        priorityMap.put(PresenceAbsenceTerm.NATIVE_FORMERLY_NATIVE(), 40);
        priorityMap.put(PresenceAbsenceTerm.NATIVE_PRESENCE_QUESTIONABLE(), 60);
        priorityMap.put(PresenceAbsenceTerm.INTRODUCED_PRESENCE_QUESTIONABLE(), 50);
        priorityMap.put(PresenceAbsenceTerm.INTRODUCED_DOUBTFULLY_INTRODUCED(), 80);
        priorityMap.put(PresenceAbsenceTerm.INTRODUCED(), 90);
        priorityMap.put(PresenceAbsenceTerm.CASUAL(), 100);
        priorityMap.put(PresenceAbsenceTerm.NATURALISED(), 110);
        priorityMap.put(PresenceAbsenceTerm.NATIVE_DOUBTFULLY_NATIVE(), 120); // null
        priorityMap.put(PresenceAbsenceTerm.NATIVE(), 130); // null
        priorityMap.put(PresenceAbsenceTerm.ENDEMIC_FOR_THE_RELEVANT_AREA(), 999);

        for(PresenceAbsenceTerm term : priorityMap.keySet()) {
            // load the term
            term = (PresenceAbsenceTerm) termService.load(term.getUuid());
            // find the extension
            Extension priorityExtension = null;
            Set<Extension> extensions = term.getExtensions();
            for(Extension extension : extensions){
                if (!extension.getType().equals(ExtensionType.ORDER())) {
                    continue;
                }
                int pos = extension.getValue().indexOf(EXTENSION_VALUE_PREFIX);
                if(pos == 0){ // if starts with EXTENSION_VALUE_PREFIX
                    priorityExtension = extension;
                    break;
                }
            }
            if(priorityExtension == null) {
                priorityExtension = Extension.NewInstance(term, null, ExtensionType.ORDER());
            }
            priorityExtension.setValue(EXTENSION_VALUE_PREFIX + priorityMap.get(term));

            // save the term
            termService.saveOrUpdate(term);
            if (logger.isDebugEnabled()) {
                logger.debug("Priority updated for " + term.getLabel());
            }
        }

        commitTransaction(txStatus);
    }

    public static void addSourcesDeduplicated(Set<DescriptionElementSource> target, Set<DescriptionElementSource> sources) {
        for(DescriptionElementSource source : sources) {
            boolean contained = false;
            for(DescriptionElementSource existingSource: target) {
                if(existingSource.equalsByShallowCompare(source)) {
                    contained = true;
                    break;
                }
            }
            if(!contained) {
                try {
                    target.add((DescriptionElementSource)source.clone());
                } catch (CloneNotSupportedException e) {
                    // should never happen
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * @return the batchMinFreeHeap
     */
    public long getBatchMinFreeHeap() {
        return batchMinFreeHeap;
    }

    /**
     * @param batchMinFreeHeap the batchMinFreeHeap to set
     */
    public void setBatchMinFreeHeap(long batchMinFreeHeap) {
        this.batchMinFreeHeap = batchMinFreeHeap;
    }

    public enum AggregationMode {
        byAreas,
        byRanks,
        byAreasAndRanks

    }

    private class StatusAndSources {

        private final PresenceAbsenceTerm status;

        private final Set<DescriptionElementSource> sources = new HashSet<>();

        public StatusAndSources(PresenceAbsenceTerm status, Set<DescriptionElementSource> sources) {
            this.status = status;
            addSourcesDeduplicated(this.sources, sources);
        }

        /**
         * @param sources
         */
        public void addSources(Set<DescriptionElementSource> sources) {
            addSourcesDeduplicated(this.sources, sources);
        }

    }
}
