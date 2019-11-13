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
import org.hibernate.HibernateException;
import org.hibernate.search.Search;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.service.UpdateResult;
import eu.etaxonomy.cdm.common.DynamicBatch;
import eu.etaxonomy.cdm.common.JvmLimitsException;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.SubProgressMonitor;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter.ORDER;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.DescriptionType;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.term.OrderedTermBase;
import eu.etaxonomy.cdm.model.term.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.term.TermCollection;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.VocabularyEnum;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

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
 *   <li>if an entered or imported status information exists for the same area for which calculated (accumulated)
 *       data is available, the calculated data has to be given preference over other data.
 *   </li>
 *   <li>If there is an area with a sub area and both areas have the same calculated status only the subarea
 *       status should be shown in the map, whereas the super area should be ignored.
 *   </li>
 * </ol>
 *
 * @author Anton Güntsch (author of original Transmission Engine Occurrence version 14 written in Visual Basic)
 * @author Andreas Kohlbecker (2013, porting Transmission Engine Occurrence to Java)
 * @since Feb 22, 2013
 */
public class DistributionAggregation
            extends DescriptionAggregationBase<DistributionAggregation,DistributionAggregationConfiguration>{

    public static final Logger logger = Logger.getLogger(DistributionAggregation.class);

    public static final String EXTENSION_VALUE_PREFIX = "transmissionEngineDistribution.priority:";

    private static final long BATCH_MIN_FREE_HEAP = 800  * 1024 * 1024;
    /**
     * ratio of the initially free heap which should not be used
     * during the batch processing. This amount of the heap is reserved
     * for the flushing of the session and to the index
     */
    private static final double BATCH_FREE_HEAP_RATIO = 0.9;
    private static final int BATCH_SIZE_BY_AREA = 1000;
    private static final int BATCH_SIZE_BY_RANK = 500;

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
    private List<PresenceAbsenceTerm> statusOrder = null;

    private List<PresenceAbsenceTerm> byAreaIgnoreStatusList = null;

    private List<PresenceAbsenceTerm> byRankIgnoreStatusList = null;

    private final Map<NamedArea, Set<NamedArea>> subAreaMap = new HashMap<>();

    private long batchMinFreeHeap = BATCH_MIN_FREE_HEAP;

// ******************* CONSTRUCTOR *********************************/

    public DistributionAggregation() {
    }

// ********************* METHODS *****************************************/


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
     * @throws JvmLimitsException
     */
    @Override
    protected UpdateResult doInvoke() throws JvmLimitsException {

        //TODO FIXME use UpdateResult
        UpdateResult result = new UpdateResult();

        // only for debugging:
        //logger.setLevel(Level.TRACE); // TRACE will slow down a lot since it forces loading all term representations
        //Logger.getLogger("org.hibernate.SQL").setLevel(Level.DEBUG);

        logger.info("Hibernate JDBC Batch size: "
                +  getSession().getSessionFactory().getSessionFactoryOptions().getJdbcBatchSize());

        TaxonNodeFilter filter = getConfig().getTaxonNodeFilter();
        filter.setOrder(ORDER.TREEINDEX_DESC); //DESC guarantees that child taxa are aggregated before parent

        Long countTaxonNodes = getTaxonNodeService().count(filter);

        List<Integer> taxonNodeIdList = getTaxonNodeService().idList(filter);

        // take start time for performance testing
        double start = System.currentTimeMillis();

        int aggregationWorkTicks = countTaxonNodes.intValue();
        beginTask("Accumulating distributions", (aggregationWorkTicks) + 1 );

        makeStatusOrder();
        worked(1);

        double end1 = System.currentTimeMillis();
        logger.info("Time elapsed for classificationLookup() : " + (end1 - start) / (1000) + "s");
//        double start2 = System.currentTimeMillis();

        //TODO AM toString for filter
        subTask("Accumulating distributions to super areas for " + filter.toString());

        boolean doClearExistingDistribution = true;

        //TODO AM move to invokeOnSingleTaxon()
        accumulate(getConfig().getSuperAreas(),
                    taxonNodeIdList,
                    new SubProgressMonitor(getMonitor(), aggregationWorkTicks),
                    doClearExistingDistribution);

        double end3 = System.currentTimeMillis();
        logger.info("Time elapsed for accumulate(): " + (end3 - start) / (1000) + "s");

        done();

        return result;
    }


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
    private List<PresenceAbsenceTerm> getByAreaIgnoreStatusList() {
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

    public void setByAreaIgnoreStatusList(List<PresenceAbsenceTerm> byAreaIgnoreStatusList) {
        this.byAreaIgnoreStatusList = byAreaIgnoreStatusList;
    }

    /**
     * byRankIgnoreStatusList contains by default
     *  <ul>
     *    <li>PresenceTerm.ENDEMIC_FOR_THE_RELEVANT_AREA()</li>
     *    <li>PresenceTerm.ENDEMIC_DOUBTFULLY_PRESENT()</li>
     *    <li>PresenceTerm.ENDEMIC_REPORTED_IN_ERROR()</li>
     *    <li>PresenceTerm.NOT_ENDEMIC_FOR_THE_RELEVANT_AREA()</li>
     *  </ul>
     *
     * @return the byRankIgnoreStatusList
     */
    private List<PresenceAbsenceTerm> getByRankIgnoreStatusList() {

        if (byRankIgnoreStatusList == null) {
            byRankIgnoreStatusList = Arrays.asList(
                    new PresenceAbsenceTerm[] {
                    		PresenceAbsenceTerm.ENDEMIC_FOR_THE_RELEVANT_AREA(),
                    		PresenceAbsenceTerm.ENDEMIC_DOUBTFULLY_PRESENT(),
                            PresenceAbsenceTerm.ENDEMIC_REPORTED_IN_ERROR(),
                            PresenceAbsenceTerm.NOT_ENDEMIC_FOR_THE_RELEVANT_AREA()
                    });
        }
        return byRankIgnoreStatusList;
    }

    public void setByRankIgnoreStatusList(List<PresenceAbsenceTerm> byRankIgnoreStatusList) {
        this.byRankIgnoreStatusList = byRankIgnoreStatusList;
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
    private StatusAndSources choosePreferred(
            StatusAndSources a, StatusAndSources b, Set<DescriptionElementSource> sourcesForWinnerB){

        if (b == null || b.status == null) {
            return a;
        }
        if (a == null || a.status == null) {
            return b;
        }

        Integer indexA = statusOrder.indexOf(a.status);
        Integer indexB = statusOrder.indexOf(b.status);

        if (indexB == -1) {
            logger.warn("No priority found in map for " + b.status.getLabel());
            return a;
        }
        if (indexA == -1) {
            logger.warn("No priority found in map for " + a.status.getLabel());
            return b;
        }
        if(indexA < indexB){
            if(sourcesForWinnerB != null) {
                b.addSources(sourcesForWinnerB);
            }
            return b;
        } else {
            if (indexA == indexB){
                a.addSources(b.sources);
            }
            return a;
        }
    }
//
//    /**
//     * reads the priority for the given status term from the extensions.
//     *
//     * @param term
//     * @return the priority value
//     */
//    private Integer getPriorityFor(PresenceAbsenceTerm term) {
//        Set<Extension> extensions = term.getExtensions();
//        for(Extension extension : extensions){
//            if(!extension.getType().equals(ExtensionType.ORDER())) {
//                continue;
//            }
//            int pos = extension.getValue().indexOf(EXTENSION_VALUE_PREFIX);
//            if(pos == 0){ // if starts with EXTENSION_VALUE_PREFIX
//                try {
//                    Integer priority = Integer.valueOf(extension.getValue().substring(EXTENSION_VALUE_PREFIX.length()));
//                    return priority;
//                } catch (NumberFormatException e) {
//                    logger.warn("Invalid number format in Extension:" + extension.getValue());
//                }
//            }
//        }
//        logger.warn("no priority defined for '" + term.getLabel() + "'");
//        return null;
//    }


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
     * @param classificationLookupDto
     * @throws JvmLimitsException
     *
     */
    protected void accumulate(List<NamedArea> superAreas, List<Integer> taxonNodeIdList,
            IProgressMonitor subMonitor, boolean doClearDescriptions) throws JvmLimitsException {

        DynamicBatch batch = new DynamicBatch(BATCH_SIZE_BY_AREA, batchMinFreeHeap);
        batch.setRequiredFreeHeap(BATCH_FREE_HEAP_RATIO);
        //TODO AM from aggByRank          batch.setMaxAllowedGcIncreases(10);

        TransactionStatus txStatus = startTransaction(false);

        // reload superAreas TODO is it faster to getSession().merge(object) ??
        Set<UUID> superAreaUuids = new HashSet<>(superAreas.size());
        for (NamedArea superArea : superAreas){
            superAreaUuids.add(superArea.getUuid());
        }

        // visit all accepted taxa
        subMonitor.beginTask("Accumulating bottom up ", taxonNodeIdList.size());

        //TODO FIXME this was a Taxon not a TaxonNode id list
        Iterator<Integer> taxonIdIterator = taxonNodeIdList.iterator();

        while (taxonIdIterator.hasNext() || batch.hasUnprocessedItems()) {

            if(txStatus == null) {
                // transaction has been committed at the end of this batch, start a new one
                txStatus = startTransaction(false);
            }

            // the session is cleared after each batch, so load the superAreaList for each batch
            //TODO AM reload areas needed? Terms do not cascade!
            List<NamedArea> superAreaList = getTermService().find(NamedArea.class, superAreaUuids);

            // load taxa for this batch
            List<Integer> taxonIds = batch.nextItems(taxonIdIterator);
//            logger.debug("accumulateByArea() - taxon " + taxonPager.getFirstRecord() + " to " + taxonPager.getLastRecord() + " of " + taxonPager.getCount() + "]");

            //TODO AM adapt init-strat to taxonnode if it stays a taxon node list
            List<OrderHint> orderHints = new ArrayList<>();
            orderHints.add(OrderHint.BY_TREE_INDEX_DESC);
            List<TaxonNode> taxonNodes = getTaxonNodeService().loadByIds(taxonIds, orderHints, TAXONDESCRIPTION_INIT_STRATEGY);

            // iterate over the taxa and accumulate areas
            // start processing the new batch

            for(TaxonNode taxonNode : taxonNodes) {
                Taxon taxon = CdmBase.deproxy(taxonNode.getTaxon());
                if(logger.isDebugEnabled()){
                    logger.debug("accumulate - taxon :" + taxonToString(taxon));
                }
                batch.incrementCounter();

                TaxonDescription description = findComputedDescription(taxon, doClearDescriptions);
                if (getConfig().getAggregationMode().isByArea()){
                    accumulateByAreaSingleTaxon(subMonitor, description, superAreaList, taxonNode);
                }
                if (getConfig().getAggregationMode().isByRank()){
                    accumulateByRankSingleTaxon(description, batch, taxonNode);
                }
                //TODO handle canceled better
                if(subMonitor.isCanceled()){
                    return;
                }

                if(!batch.isWithinJvmLimits()) {
                    break; // flushAndClear and start with new batch
                }
            } // next taxon

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

        } // next batch of taxa

        subMonitor.done();
    }

    private void accumulateByAreaSingleTaxon(IProgressMonitor subMonitor, TaxonDescription description,
            List<NamedArea> superAreaList, TaxonNode taxonNode) {

        Taxon taxon = CdmBase.deproxy(taxonNode.getTaxon());
        if(logger.isDebugEnabled()){
            logger.debug("accumulateByArea() - taxon :" + taxonToString(taxon));
        }

//        TaxonDescription description = findComputedDescription(taxon, doClearDescriptions);
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
                    //TODO AM is the status handling here correct? The mapping to CDM handled
                    if(subArea.equals(distribution.getArea()) && distribution.getStatus() != null) {
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
                //TODO AM element marker needed?
//                newDistribitionElement.addMarker(Marker.NewInstance(MarkerType.COMPUTED(), true));
                description.addElement(newDistribitionElement);
            }

        } // next super area ....

        getDescriptionService().saveOrUpdate(description);
        getTaxonService().saveOrUpdate(taxon);
        subMonitor.worked(1);
    }

//   /**
//    * Step 2: Accumulate by ranks starting from lower rank to upper rank, the status of all children
//    * are accumulated on each rank starting from lower rank to upper rank.
//    * <ul>
//    * <li>aggregate distribution of included taxa of the next lower rank for any rank level starting from the lower rank (e.g. sub species)
//    *    up to upper rank (e.g. Genus)</li>
//    *  <li>the accumulation id done for each distribution area found in the included taxa</li>
//    *  <li>areas of subtaxa with status endemic are ignored</li>
//    *  <li>the status with the highest priority determines the value for the accumulated distribution</li>
//    *  <li>the source reference of the accumulated distributions are also accumulated into the new distribution,
//    *    this has been especially implemented for the EuroMed Checklist Vol2 and might not be a general requirement</li>
//    *</ul>
//    * @throws JvmLimitsException
//    */
//    protected void accumulateByRank(List<Integer> taxonNodeIdList, IProgressMonitor subMonitor,
//            boolean doClearDescriptions) throws JvmLimitsException {
//
//        DynamicBatch batch = new DynamicBatch(BATCH_SIZE_BY_RANK, batchMinFreeHeap);
//        batch.setRequiredFreeHeap(BATCH_FREE_HEAP_RATIO);
//        batch.setMaxAllowedGcIncreases(10);
//
//        TransactionStatus txStatus = startTransaction(false);
//
//        // the loadRankSpecificRootNodes() method not only finds
//        // taxa of the specified rank but also taxa of lower ranks
//        // if no taxon of the specified rank exists, so we need to
//        // remember which taxa have been processed already
//        Set<Integer> taxaProcessedIds = new HashSet<>();
//
////        subMonitor.beginTask("Accumulating by rank", ticksPerRank);
//
////        for (Rank rank : ranks) {
//
////            if(logger.isDebugEnabled()){
////                logger.debug("accumulateByRank() - at Rank '" + termToString(rank) + "'");
////            }
//
////            Set<Integer> taxonIdsPerRank = classificationLookupDao.getTaxonIdByRank().get(rank);
//
////            int taxonCountPerRank = taxonNodeIdList.size();
//
////            SubProgressMonitor taxonSubMonitor = new SubProgressMonitor(subMonitor, ticksPerRank);
////            taxonSubMonitor.beginTask("Accumulating by rank " + termToString(rank), taxonCountperRank);
//
////            if(taxonCountPerRank == 0) {
////                taxonSubMonitor.done();
////                continue;
////            }
//
//            Iterator<Integer> taxonNodeIdIterator = taxonNodeIdList.iterator();
//            while (taxonNodeIdIterator.hasNext() || batch.hasUnprocessedItems()) {
//
//                if(txStatus == null) {
//                    // transaction has been committed at the end of this batch, start a new one
//                    txStatus = startTransaction(false);
//                }
//
//                // load taxa for this batch
//                List<Integer> taxonNodeIds = batch.nextItems(taxonNodeIdIterator);
//
//                List<OrderHint> orderHints = new ArrayList<>();
//                orderHints.add(OrderHint.BY_TREE_INDEX_DESC);
//                List<TaxonNode> nodes = getTaxonNodeService().loadByIds(taxonNodeIds, orderHints, null);
//
////                if(logger.isDebugEnabled()){
////                           logger.debug("accumulateByRank() - taxon " + taxonPager.getFirstRecord() + " to " + taxonPager.getLastRecord() + " of " + taxonPager.getCount() + "]");
////                }
//
//                for(TaxonNode taxonNode : nodes) {
//
//                    accumulateByRankSingleTaxon(
//                            doClearDescriptions, batch, taxonNode);
//                    if(!batch.isWithinJvmLimits()) {
//                        break; // flushAndClear and start with new batch
//                    }
//
//                } // next taxon ....
//
//                flushAndClear();
//
//                // commit for every batch, otherwise the persistent context
//                // may grow too much and eats up all the heap
//                commitTransaction(txStatus);
//                txStatus = null;
//
//                // flushing the session and to the index (flushAndClear() ) can impose a
//                // massive heap consumption. therefore we explicitly do a check after the
//                // flush to detect these situations and to reduce the batch size.
//                if(batch.getJvmMonitor().getGCRateSiceLastCheck() > 0.05) {
//                    batch.reduceSize(0.5);
//                }
//
//            } // next batch
//
//            subMonitor.done();
////            subMonitor.worked(1);
//
////        } // next Rank
//
//        logger.info("accumulateByRank() - done");
//        subMonitor.done();
//    }

    private void accumulateByRankSingleTaxon(TaxonDescription description, DynamicBatch batch,
            TaxonNode taxonNode) {
        batch.incrementCounter();

        Taxon taxon = CdmBase.deproxy(taxonNode.getTaxon());
//        if (taxaProcessedIds.contains(taxon.getId())) {
//            if(logger.isDebugEnabled()){
//                logger.debug("accumulateByRank() - skipping already processed taxon :" + taxonToString(taxon));
//            }
//            return;
//        }
//        taxaProcessedIds.add(taxon.getId());
        if(logger.isDebugEnabled()){
            logger.debug("accumulateByRank() [" + /*rank.getLabel() +*/ "] - taxon :" + taxonToString(taxon));
        }

        // Step through direct taxonomic children for accumulation
        Map<NamedArea, StatusAndSources> accumulatedStatusMap = new HashMap<>();

    //                    List<Integer> childTaxonIds = new ArrayList<>();
    //                    Set<Integer> childSet = classificationLookupDao.getChildTaxonMap().get(taxon.getId());
    //                    if(childSet != null) {
    //                        childTaxonIds.addAll(childSet);
    //                    }
        if(!taxonNode.getChildNodes().isEmpty()) {
    //                        childTaxa = getTaxonService().loadByIds(childTaxonIds, TAXONDESCRIPTION_INIT_STRATEGY);

            LinkedList<Taxon> childStack = new LinkedList<>();
            for (TaxonNode node : taxonNode.getChildNodes()){
                childStack.add(CdmBase.deproxy(node.getTaxon()));
            }

            while(childStack.size() > 0){

                Taxon childTaxon = childStack.pop();
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
                getSession().evict(childTaxon); // no longer needed, save heap
            }

            if(accumulatedStatusMap.size() > 0) {
//                TaxonDescription description = findComputedDescription(taxon, doClearDescriptions);
                for (NamedArea area : accumulatedStatusMap.keySet()) {
                    Distribution distribition = findDistribution(description, area, accumulatedStatusMap.get(area).status);
                    if(distribition == null) {
                        // create a new distribution element
                        distribition = Distribution.NewInstance(area, accumulatedStatusMap.get(area).status);
                        //TODO element marker needed
//                        distribition.addMarker(Marker.NewInstance(MarkerType.COMPUTED(), true));
                    }
                    addSourcesDeduplicated(distribition.getSources(), accumulatedStatusMap.get(area).sources);

                    description.addElement(distribition);
                }
                getTaxonService().saveOrUpdate(taxon);
                getDescriptionService().saveOrUpdate(description);
            }

        }
    //                    taxonSubMonitor.worked(1); // one taxon worked
    }

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

    private List<Rank> rankInterval(UUID lowerRankUuid, UUID upperRankUuid) {

        TransactionStatus txStatus = startTransaction(false);
        Rank lowerRank = (Rank)getTermService().load(lowerRankUuid);
        Rank upperRank = (Rank)getTermService().load(upperRankUuid);

        Rank currentRank = lowerRank;
        List<Rank> ranks = new ArrayList<>();
        do {
            ranks.add(currentRank);
            currentRank = findNextHigherRank(currentRank);
        }while (!currentRank.isHigher(upperRank));


        commitTransaction(txStatus);
        txStatus = null;
        return ranks;
    }

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

    private void flushAndClear() {
       flush();
       logger.debug("clearing session ...");
       getSession().clear();
   }


    /**
     * Returns the next higher rank
     *
     * TODO better implement OrderedTermBase.getNextHigherTerm() and OrderedTermBase.getNextLowerTerm()?
     *
     * @param rank the lower rank
     */
    private Rank findNextHigherRank(Rank rank) {
        rank = (Rank) getTermService().load(rank.getUuid());
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
     *     has a description of type {@link DescriptionType#AGGREGATED_DISTRIBUTION}
     *     (or a MarkerType COMPUTED for historical reasons, will be removed in future)
     * @return
     */
    private TaxonDescription findComputedDescription(Taxon taxon, boolean doClear) {

        String descriptionTitle = this.getClass().getSimpleName();

        // find existing one
        for (TaxonDescription description : taxon.getDescriptions()) {
            // TODO remove COMPUTED;
            if (description.isAggregatedDistribution() || description.hasMarker(MarkerType.COMPUTED(), true)) {
                logger.debug("reusing computed description for " + taxon.getTitleCache());
                if (doClear) {
                    int deleteCount = 0;
                    Set<DescriptionElementBase> deleteCandidates = new HashSet<>();
                    for (DescriptionElementBase descriptionElement : description.getElements()) {
                        if(descriptionElement.isInstanceOf(Distribution.class)) {
                            deleteCandidates.add(descriptionElement);
                        }
                    }
                    description.addType(DescriptionType.AGGREGATED_DISTRIBUTION);
                    if(deleteCandidates.size() > 0){
                        for(DescriptionElementBase descriptionElement : deleteCandidates) {
                            description.removeElement(descriptionElement);
                            getDescriptionService().deleteDescriptionElement(descriptionElement);
                            descriptionElement = null;
                            deleteCount++;
                        }
                        getDescriptionService().saveOrUpdate(description);
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
        description.addType(DescriptionType.AGGREGATED_DISTRIBUTION);
        return description;
    }

    private Set<NamedArea> getSubAreasFor(NamedArea superArea) {

        if(!subAreaMap.containsKey(superArea)) {
            if(logger.isDebugEnabled()){
                logger.debug("loading included areas for " + superArea.getLabel());
            }
            subAreaMap.put(superArea, superArea.getIncludes());
        }
        return subAreaMap.get(superArea);
    }

    private List<Distribution> distributionsFor(Taxon taxon) {
        List<Distribution> distributions = new ArrayList<>();
        for(TaxonDescription description: taxon.getDescriptions()) {
            readOnlyIfInSession(description);
            for(DescriptionElementBase deb : description.getElements()) {
                if(deb.isInstanceOf(Distribution.class)) {
                    readOnlyIfInSession(deb);
                    distributions.add(CdmBase.deproxy(deb, Distribution.class));
                }
            }
        }
        return distributions;
    }

    /**
     * This method avoids problems when running the {@link DistributionAggregationTest}.
     * For some unknown reason entities are not in the PersitenceContext even if they are
     * loaded by a service method. Setting these entities to read-only would raise a
     * TransientObjectException("Instance was not associated with this persistence context")
     *
     * @param entity
     */
    private void readOnlyIfInSession(CdmBase entity) {
        if(getSession().contains(entity)) {
            getSession().setReadOnly(entity, true);
        }
    }

    private String taxonToString(TaxonBase<?> taxon) {
        if(logger.isTraceEnabled()) {
            return taxon.getTitleCache();
        } else {
            return taxon.toString();
        }
    }

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
    private void makeStatusOrder() {

        TransactionStatus txStatus = startTransaction(false);

        @SuppressWarnings("rawtypes")
        TermCollection<PresenceAbsenceTerm, TermNode> stOrder = getConfig().getStatusOrder();
        if (stOrder == null){
            stOrder = defaultStatusOrder();
        }
        if (stOrder.isInstanceOf(TermTree.class)){
            statusOrder = CdmBase.deproxy(stOrder, TermTree.class).asTermList();
        }else if (stOrder.isInstanceOf(OrderedTermVocabulary.class)){
            statusOrder = new ArrayList<>(CdmBase.deproxy(stOrder, OrderedTermVocabulary.class).getOrderedTerms());
        }else{
            throw new RuntimeException("TermCollection type for status order not supported: " + statusOrder.getClass().getSimpleName());
        }

        commitTransaction(txStatus);
    }

    private OrderedTermVocabulary<PresenceAbsenceTerm> defaultStatusOrder() {
        @SuppressWarnings("unchecked")
        OrderedTermVocabulary<PresenceAbsenceTerm> voc = (OrderedTermVocabulary<PresenceAbsenceTerm>)getRepository().getVocabularyService().find(VocabularyEnum.PresenceAbsenceTerm.getUuid());
        return voc;
    }

    private void addSourcesDeduplicated(Set<DescriptionElementSource> target, Set<DescriptionElementSource> sources) {
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

    public void setBatchMinFreeHeap(long batchMinFreeHeap) {
        this.batchMinFreeHeap = batchMinFreeHeap;
    }

    public enum AggregationMode {
        byAreas,
        byRanks,
        byAreasAndRanks;
        public boolean isByRank() {
           return this==byRanks || this == byAreasAndRanks;
        }
        public boolean isByArea() {
            return this==byAreas || this == byAreasAndRanks;
         }
    }

    private class StatusAndSources {

        private final PresenceAbsenceTerm status;
        private final Set<DescriptionElementSource> sources = new HashSet<>();

        public StatusAndSources(PresenceAbsenceTerm status, Set<DescriptionElementSource> sources) {
            this.status = status;
            addSourcesDeduplicated(this.sources, sources);
        }

        public void addSources(Set<DescriptionElementSource> sources) {
            addSourcesDeduplicated(this.sources, sources);
        }
    }

    @Override
    protected UpdateResult invokeOnSingleTaxon() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected UpdateResult removeExistingAggregationOnTaxon() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected UpdateResult invokeHigherRankAggregation() {
        // TODO Auto-generated method stub
        return null;
    }
}
