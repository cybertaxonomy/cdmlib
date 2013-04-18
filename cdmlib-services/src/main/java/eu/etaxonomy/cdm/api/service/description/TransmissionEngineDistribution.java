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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.description.AbsenceTerm;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * The TransmissionEngineDistribution is meant to be used from within a service class.
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
@Transactional(readOnly = false)
public class TransmissionEngineDistribution {

    public static final String EXTENSION_VALUE_PREFIX = "transmissionEngineDistribution.priority:";

    public static final Logger logger = Logger.getLogger(TransmissionEngineDistribution.class);

    private static final Integer batchSize = 1000;


    /**classification
     * A map which contains the status terms as key and the priority as value
     * The map will contain both, the PresenceTerms and the AbsenceTerms
     */
    private Map<PresenceAbsenceTermBase, Integer> statusPriorityMap = null;

//    private ICdmApplicationConfiguration repo;
//    @Autowired
//    public void setRepository(ICdmApplicationConfiguration repo){
//        this.repo = repo;
//    }
//    public ICdmApplicationConfiguration getRepository(){
//        return repo;
//    }

    @Autowired
    private IDescriptionService descriptionService;

    @Autowired
    private ITermService termService;

    @Autowired
    private ITaxonService taxonService;

    @Autowired
    private IClassificationService classificationService;

    @Autowired
    private INameService nameService;


    private List<PresenceAbsenceTermBase> byAreaIgnoreStatusList = null;

    private List<PresenceAbsenceTermBase> byRankIgnoreStatusList = null;

    private final Map<NamedArea, Set<NamedArea>> subAreaMap = new HashMap<NamedArea, Set<NamedArea>>();


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
    public List<PresenceAbsenceTermBase> getByAreaIgnoreStatusList() {
        if(byAreaIgnoreStatusList == null ){
            byAreaIgnoreStatusList = Arrays.asList(
                    new PresenceAbsenceTermBase[] {
                            AbsenceTerm.CULTIVATED_REPORTED_IN_ERROR(),
                            AbsenceTerm.INTRODUCED_REPORTED_IN_ERROR(),
                            AbsenceTerm.NATIVE_REPORTED_IN_ERROR(),
                            AbsenceTerm.INTRODUCED_FORMERLY_INTRODUCED(),
                            AbsenceTerm.NATIVE_FORMERLY_NATIVE()
                            // TODO what about AbsenceTerm.ABSENT() also ignore?
                    });
        }
        return byAreaIgnoreStatusList;
    }

    /**
     * @param byAreaIgnoreStatusList the byAreaIgnoreStatusList to set
     */
    public void setByAreaIgnoreStatusList(List<PresenceAbsenceTermBase> byAreaIgnoreStatusList) {
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
    public List<PresenceAbsenceTermBase> getByRankIgnoreStatusList() {

        if (byRankIgnoreStatusList == null) {
            byRankIgnoreStatusList = Arrays.asList(
                    new PresenceAbsenceTermBase[] {
                            PresenceTerm.ENDEMIC_FOR_THE_RELEVANT_AREA()
                    });
        }
        return byRankIgnoreStatusList;
    }

    /**
     * @param byRankIgnoreStatusList the byRankIgnoreStatusList to set
     */
    public void setByRankIgnoreStatusList(List<PresenceAbsenceTermBase> byRankIgnoreStatusList) {
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
    @SuppressWarnings("rawtypes")
    private void initializeStatusPriorityMap() {

        statusPriorityMap = new HashMap<PresenceAbsenceTermBase, Integer>();
        Integer priority;

        // PresenceTerms
        for(DefinedTermBase term : termService.list(PresenceTerm.class, null, null, null, null)){
            priority = getPriorityFor(term);
            if(priority != null){
                statusPriorityMap.put((PresenceAbsenceTermBase) term, priority);
            }
        }
        // AbsenceTerms
        for(DefinedTermBase term : termService.list(AbsenceTerm.class, null, null, null, null)){
            priority = getPriorityFor(term);
            if(priority != null){
                statusPriorityMap.put((PresenceAbsenceTermBase) term, priority);
            }
        }
    }

    /**
     * Compares the PresenceAbsenceTermBase terms <code>a</code> and <code>b</code>  and
     * returns the PresenceAbsenceTermBase with the higher priority as stored in the statusPriorityMap.
     * If either a or b are null b or a is returned.
     *
     * @see initializeStatusPriorityMap()
     *
     * @param a
     * @param b
     * @return
     */
    private PresenceAbsenceTermBase choosePreferred(PresenceAbsenceTermBase a, PresenceAbsenceTermBase b){

        if (statusPriorityMap == null) {
            initializeStatusPriorityMap();
        }

        if (b == null) {
            return a;
        }
        if (a == null) {
            return b;
        }

        if (statusPriorityMap.get(a) == null) {
            logger.warn("No priority found in map for " + a.getLabel());
            return b;
        }
        if (statusPriorityMap.get(b) == null) {
            logger.warn("No priority found in map for " + b.getLabel());
            return a;
        }
        if(statusPriorityMap.get(a) > statusPriorityMap.get(b)){
            return a;
        } else {
            return b;
        }
    }

    /**
     * reads the priority for the given status term from the extensions.
     *
     * @param term
     * @return the priority value
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Integer getPriorityFor(DefinedTermBase term) {
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
     *  <li>Step 1: Accumulate occurrence records by area</li>
     *  <li>Step 2: Accumulate by ranks starting from lower rank to upper rank, the status of all children
     * are accumulated on each rank starting from lower rank to upper rank.</li>
     * </ul>
     *
     * @param superAreas
     *      the areas to which the subordinate areas should be projected.
     * @param lowerRank
     * @param upperRank
     * @param classification
     * @param classification
     *      limit the accumulation process to a specific classification (not yet implemented)
     */
    public void accumulate(List<NamedArea> superAreas, Rank lowerRank, Rank upperRank, Classification classification) {

        // superAreas will be reloaded internally in accumulateByArea
        accumulateByArea(superAreas, classification);
        accumulateByRank(lowerRank, upperRank, classification);
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
     * @param classification
     *      limit the accumulation process to a specific classification (not yet implemented)
     */
    protected void accumulateByArea(List<NamedArea> superAreas, Classification classification) {

        // visit all accepted taxa
        List<TaxonBase> taxa = null;
        int start = 0;
        while (true) {
            taxa = taxonService.list(Taxon.class, batchSize, start, null, null); //TODO limit by classification is not null
            logger.debug("accumulateByArea() - next " + batchSize + " taxa at position " + start);
            if (taxa.size() == 0){
                break;
            }
            for(TaxonBase taxonBase : taxa) {
                if(logger.isDebugEnabled()){
                    logger.debug("accumulateByArea() - taxon :" + taxonBase.getTitleCache());
                }
                Taxon taxon = (Taxon)taxonBase;

                List<Distribution> distributions = distributionsFor(taxon);

                // Step through superAreas for accumulation of subAreas
                for (NamedArea superArea : superAreas){
                    // accumulate all sub area status
                    superArea = (NamedArea) termService.load(superArea.getUuid());
                    PresenceAbsenceTermBase accumulatedStatus = null;
                    Set<NamedArea> subAreas = getSubAreasFor(superArea);
                    for(NamedArea subArea : subAreas){
                        if(logger.isDebugEnabled()){
                            logger.debug("accumulateByArea() - \t\t" + subArea.getLabel());
                        }
                        // step through all distributions for the given subArea
                        for(Distribution distribution : distributions){
                            if(distribution.getArea().equals(subArea) && distribution.getStatus() != null) {
                                PresenceAbsenceTermBase status = distribution.getStatus();
                                if(logger.isDebugEnabled()){
                                    logger.debug("accumulateByArea() - \t\t" + subArea.getLabel() + ": " + status.getLabel());
                                }
                                // skip all having a status value different of those in byAreaIgnoreStatusList
                                if (getByAreaIgnoreStatusList().contains(status)){
                                    continue;
                                }
                                accumulatedStatus = choosePreferred(accumulatedStatus, status);
                            }
                        }
                    } // next sub area

                    if (accumulatedStatus != null) {
                        if(logger.isDebugEnabled()){
                            logger.debug("accumulateByArea() - \t >> " + superArea.getLabel() + ": " + accumulatedStatus.getLabel());
                        }
                        // store new distribution element for superArea in taxon description
                        TaxonDescription description = findComputedDescription(taxon, true);
                        Distribution newDistribitionElement = Distribution.NewInstance(superArea, accumulatedStatus);
                        description.addElement(newDistribitionElement);
                        descriptionService.saveOrUpdate(description);
                    }
                } // next super area ....
            } // next taxon
            start = start + batchSize;
        }
    }

   /**
    * Step 2: Accumulate by ranks staring from lower rank to upper rank, the status of all children
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
    */
    protected void accumulateByRank(Rank lowerRank, Rank upperRank, Classification classification) {

        // the loadRankSpecificRootNodes() method not only finds
        // taxa of the specified rank but also taxa of lower ranks
        // if no taxon of the specified rank exists, so we need to
        // remember which taxa have been processed already
        Set<Integer> taxaProcessedIds = new HashSet<Integer>();


        //TODO loop over ranks from lower to higher
        Rank rank = lowerRank;
        while (!rank.isHigher(upperRank)) {
            if(logger.isDebugEnabled()){
                logger.debug("accumulateByRank() - at Rank '" + rank.getLabel() + "'");
            }
            int start = 0;
            while (true) {
                List<TaxonNode> taxonNodes = classificationService
                        .loadRankSpecificRootNodes(classification, rank, batchSize, start, null);
                logger.debug("accumulateByRank() - next " + batchSize + " taxa at position " + start);
                if (taxonNodes.size() == 0){
                    break;
                }
                for(TaxonNode taxonNode : taxonNodes) {

                    Taxon taxon = taxonNode.getTaxon();
                    if (taxaProcessedIds.contains(taxon.getId())) {
                        if(logger.isDebugEnabled()){
                            logger.debug("accumulateByRank() - skipping already processed taxon :" + taxon.getTitleCache());
                        }
                        continue;
                    }
                    taxaProcessedIds.add(taxon.getId());
                    if(logger.isDebugEnabled()){
                        logger.debug("accumulateByRank() - taxon :" + taxon.getTitleCache());
                    }

                    // Step through direct taxonomic children for accumulation

                    Map<NamedArea, PresenceAbsenceTermBase> accumulatedStatusMap = new HashMap<NamedArea, PresenceAbsenceTermBase>();

                    for (TaxonNode subTaxonNode : taxonNode.getChildNodes()){
                        for(Distribution distribution : distributionsFor(subTaxonNode.getTaxon()) ) {
                            PresenceAbsenceTermBase status = distribution.getStatus();
                            NamedArea area = distribution.getArea();
                            if (status == null || getByRankIgnoreStatusList().contains(status)){
                              continue;
                            }
                            accumulatedStatusMap.put(area, choosePreferred(accumulatedStatusMap.get(area), status));
                         }
                    }

                    if(accumulatedStatusMap.size() > 0) {
                        TaxonDescription description = findComputedDescription(taxon, false);
                        for (NamedArea area : accumulatedStatusMap.keySet()) {
                            // store new distribution element in new Description
                            Distribution newDistribitionElement = Distribution.NewInstance(area, accumulatedStatusMap.get(area));
                            description.addElement(newDistribitionElement);
                        }
                        descriptionService.saveOrUpdate(description);
                    }
                } // next taxon node ....
                start = start + batchSize;
            } // next batch
            rank = findNextHigherRank(rank);
        } // next Rank
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
        OrderedTermVocabulary<Rank> rankVocabulary = nameService.getRankVocabulary();
        return rankVocabulary.getNextHigherTerm(rank);
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
                logger.debug("reusing description for " + taxon.getTitleCache());
                if (doClear) {
                    int deleteCount = 0;
                    for (DescriptionElementBase descriptionElement : description.getElements()) {
                        if(descriptionElement instanceof Distribution) {
                            descriptionService.deleteDescriptionElement(descriptionElement);
                            deleteCount++;
                        }
                    }
                    logger.debug("\t" + deleteCount +" distributions cleared");
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
            subAreaMap.put(superArea, superArea.getIncludes());
        }
        return subAreaMap.get(superArea);
    }

    /**
     * @param taxon
     * @return
     */
    private List<Distribution> distributionsFor(Taxon taxon) {
        return descriptionService
                .getDescriptionElementsForTaxon(taxon, null, Distribution.class, null, null, null);
    }

    /**
     * Sets the priorities for presence and absence terms, the priorities are stored in extensions.
     * This method must be called in a transactional context and the transaction should be committed after
     * running this method.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void updatePriorities() {
        Map<PresenceAbsenceTermBase, Integer> priorityMap = new HashMap<PresenceAbsenceTermBase, Integer>();

        priorityMap.put(AbsenceTerm.CULTIVATED_REPORTED_IN_ERROR(), 1);
        priorityMap.put(PresenceTerm.INTRODUCED_UNCERTAIN_DEGREE_OF_NATURALISATION(), 2);
        priorityMap.put(AbsenceTerm.INTRODUCED_FORMERLY_INTRODUCED(), 3);
        priorityMap.put(AbsenceTerm.INTRODUCED_REPORTED_IN_ERROR(), 20);
        priorityMap.put(AbsenceTerm.NATIVE_REPORTED_IN_ERROR(), 30);
        priorityMap.put(PresenceTerm.CULTIVATED(), 45);
        priorityMap.put(AbsenceTerm.NATIVE_FORMERLY_NATIVE(), 40);
        priorityMap.put(PresenceTerm.NATIVE_PRESENCE_QUESTIONABLE(), 60);
        priorityMap.put(PresenceTerm.INTRODUCED_PRESENCE_QUESTIONABLE(), 50);
        priorityMap.put(PresenceTerm.INTRODUCED_DOUBTFULLY_INTRODUCED(), 80);
        priorityMap.put(PresenceTerm.INTRODUCED(), 90);
        priorityMap.put(PresenceTerm.INTRODUCED_ADVENTITIOUS(), 100);
        priorityMap.put(PresenceTerm.INTRODUCED_NATURALIZED(), 110);
        priorityMap.put(PresenceTerm.NATIVE_DOUBTFULLY_NATIVE(), 120); // null
        priorityMap.put(PresenceTerm.NATIVE(), 130); // null
        priorityMap.put(PresenceTerm.ENDEMIC_FOR_THE_RELEVANT_AREA(), 999);

        for(PresenceAbsenceTermBase term : priorityMap.keySet()) {
            // load the term
            term = (PresenceAbsenceTermBase) termService.load(term.getUuid());
            // find the extension
            Extension priotityExtension = null;
            Set<Extension> extensions = term.getExtensions();
            for(Extension extension : extensions){
                if (!extension.getType().equals(ExtensionType.ORDER())) {
                    continue;
                }
                int pos = extension.getValue().indexOf(EXTENSION_VALUE_PREFIX);
                if(pos == 0){ // if starts with EXTENSION_VALUE_PREFIX
                    priotityExtension = extension;
                    break;
                }
            }
            if(priotityExtension == null) {
                priotityExtension = Extension.NewInstance(term, null, ExtensionType.ORDER());
            }
            priotityExtension.setValue(EXTENSION_VALUE_PREFIX + priorityMap.get(term));

            // save the term
            termService.saveOrUpdate(term);
            if (logger.isDebugEnabled()) {
                logger.debug("Priority updated for " + term.getLabel());
            }
        }

    }
}
