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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.search.Search;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.DescriptionType;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.term.OrderedTermBase;
import eu.etaxonomy.cdm.model.term.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.term.TermCollection;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.VocabularyEnum;

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
 * @author a.mueller (refactoring and merge with Structured Description Aggregation)
 * @since Feb 22, 2013
 */
public class DistributionAggregation
            extends DescriptionAggregationBase<DistributionAggregation,DistributionAggregationConfiguration>{

    public static final Logger logger = Logger.getLogger(DistributionAggregation.class);

    protected static final List<String> TAXONDESCRIPTION_INIT_STRATEGY = Arrays.asList(new String [] {
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

    private final Map<NamedArea, Set<NamedArea>> subAreaMap = new HashMap<>();

// ******************* CONSTRUCTOR *********************************/

    public DistributionAggregation() {}
    @Override
    protected String pluralDataType(){
        return "distributions";
    }

// ********************* METHODS *********************************/

    @Override
    protected void preAggregate(IProgressMonitor monitor) {
        monitor.subTask("make status order");

        // take start time for performance testing
        double start = System.currentTimeMillis();

        makeStatusOrder();

        double end1 = System.currentTimeMillis();
        logger.info("Time elapsed for making status order : " + (end1 - start) / (1000) + "s");

        makeSuperAreas();
        double end2 = System.currentTimeMillis();
        logger.info("Time elapsed for making super areas : " + (end2 - end1) / (1000) + "s");
    }

    @Override
    protected void initTransaction() {
    }

    List<NamedArea> superAreaList;

    private void makeSuperAreas() {
        TransactionStatus tx = startTransaction(true);
        if (getConfig().getSuperAreas()!= null){
            Set<UUID> superAreaUuids = new HashSet<>(getConfig().getSuperAreas());
            superAreaList = getTermService().find(NamedArea.class, superAreaUuids);
            for (NamedArea superArea : superAreaList){
                Set<NamedArea> subAreas = getSubAreasFor(superArea);
                for(NamedArea subArea : subAreas){
                    if (logger.isTraceEnabled()) {
                        logger.trace("Initialize " + subArea.getTitleCache());
                    }
                }
            }
        }
        commitTransaction(tx);
    }


    @Override
    protected List<String> descriptionInitStrategy() {
        return TAXONDESCRIPTION_INIT_STRATEGY;
    }

// ********************* METHODS *****************************************/

    private List<PresenceAbsenceTerm> getByAreaIgnoreStatusList() {
        return getConfig().getByAreaIgnoreStatusList();
    }

    private List<PresenceAbsenceTerm> getByRankIgnoreStatusList() {
        return getConfig().getByRankIgnoreStatusList();
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
     * @param accumulatedStatus
     * @param newStatus
     * @param additionalSourcesForWinningNewStatus Not in Use!
     *  In the case when <code>newStatus</code> is preferred over <code>accumulatedStatus</code> these Set of sources will be added to the sources of <code>b</code>
     * @param aggregationSourceMode
     * @return
     */
    private StatusAndSources choosePreferredOrMerge(StatusAndSources accumulatedStatus, StatusAndSources newStatus,
            Set<DescriptionElementSource> additionalSourcesForWinningNewStatus, AggregationSourceMode aggregationSourceMode){

        if (newStatus == null || newStatus.status == null) {
            return accumulatedStatus;
        }
        if (accumulatedStatus == null || accumulatedStatus.status == null) {
            return newStatus;
        }

        Integer indexAcc = statusOrder.indexOf(accumulatedStatus.status);
        Integer indexNew = statusOrder.indexOf(newStatus.status);

        if (indexNew == -1) {
            logger.warn("No priority found in map for " + newStatus.status.getLabel());
            return accumulatedStatus;
        }
        if (indexAcc == -1) {
            logger.warn("No priority found in map for " + accumulatedStatus.status.getLabel());
            return newStatus;
        }
        if(indexAcc < indexNew){
            if(additionalSourcesForWinningNewStatus != null) {
                newStatus.addSources(additionalSourcesForWinningNewStatus);
            }
            if (aggregationSourceMode == AggregationSourceMode.ALL){
                newStatus.addSources(accumulatedStatus.sources);
            }
            return newStatus;
        } else {
            if (indexAcc == indexNew || aggregationSourceMode == AggregationSourceMode.ALL){
                accumulatedStatus.addSources(newStatus.sources);
            }
            return accumulatedStatus;
        }
    }

    @Override
    protected void addAggregationResultToDescription(TaxonDescription targetDescription,
            ResultHolder resultHolder) {

        Map<NamedArea, StatusAndSources> accumulatedStatusMap = ((DistributionResultHolder)resultHolder).accumulatedStatusMap;

        Set<Distribution> toDelete = new HashSet<>();
        if (getConfig().isDoClearExistingDescription()){
            clearDescription(targetDescription);
        }else{
            toDelete = new HashSet<>();
        }
        for (NamedArea area : accumulatedStatusMap.keySet()) {
            PresenceAbsenceTerm status = accumulatedStatusMap.get(area).status;
            Distribution distribution = findDistributionForArea(targetDescription, area);
            //old: if we want to reuse distribution only with exact same status
//          Distribution distribution = findDistributionForAreaAndStatus(aggregationDescription, area, status);

            if(distribution == null) {
                // create a new distribution element
                distribution = Distribution.NewInstance(area, status);
                targetDescription.addElement(distribution);
            }else{
                distribution.setStatus(status);
                toDelete.remove(distribution);  //we keep the distribution for reuse
            }
            replaceSources(distribution.getSources(), accumulatedStatusMap.get(area).sources);
//            addSourcesDeduplicated(distribution.getSources(), accumulatedStatusMap.get(area).sources);
        }
        for(Distribution toDeleteDist: toDelete){
            targetDescription.removeElement(toDeleteDist);
        }
    }

    /**
     * Removes all description elements of type {@link Distribution} from the
     * (aggregation) description.
     */
    private void clearDescription(TaxonDescription aggregationDescription) {
        int deleteCount = 0;
        Set<DescriptionElementBase> deleteCandidates = new HashSet<>();
        for (DescriptionElementBase descriptionElement : aggregationDescription.getElements()) {
            if(descriptionElement.isInstanceOf(Distribution.class)) {
                deleteCandidates.add(descriptionElement);
            }
        }
        aggregationDescription.addType(DescriptionType.AGGREGATED_DISTRIBUTION);
        if(deleteCandidates.size() > 0){
            for(DescriptionElementBase descriptionElement : deleteCandidates) {
                aggregationDescription.removeElement(descriptionElement);
                getDescriptionService().deleteDescriptionElement(descriptionElement);
                descriptionElement = null;
                deleteCount++;
            }
            getDescriptionService().saveOrUpdate(aggregationDescription);
            logger.debug("\t" + deleteCount +" distributions cleared");
        }
    }

    @Override
    protected void aggregateWithinSingleTaxon(Taxon taxon,
            ResultHolder  resultHolder,
            Set<TaxonDescription> excludedDescriptions) {

        Map<NamedArea, StatusAndSources> accumulatedStatusMap =
                ((DistributionResultHolder)resultHolder).accumulatedStatusMap;

        if(logger.isDebugEnabled()){
            logger.debug("accumulateByArea() - taxon :" + taxonToString(taxon));
        }

        Set<TaxonDescription> descriptions = descriptionsFor(taxon, excludedDescriptions);
        Set<Distribution> distributions = distributionsFor(descriptions);

        // Step through superAreas for accumulation of subAreas
        for (NamedArea superArea : superAreaList){

            // accumulate all sub area status
            StatusAndSources accumulatedStatusAndSources = null;
            AggregationSourceMode aggregationSourceMode = getConfig().getWithinTaxonSourceMode();
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
                        // skip all having a status value in the ignore list
                        if (status == null || getByAreaIgnoreStatusList().contains(status)
                                || (getConfig().isIgnoreAbsentStatusByArea() && status.isAbsenceTerm())){
                            continue;
                        }
                        StatusAndSources subAreaStatusAndSources = new StatusAndSources(status, distribution, aggregationSourceMode);
                        accumulatedStatusAndSources = choosePreferredOrMerge(accumulatedStatusAndSources, subAreaStatusAndSources, null, aggregationSourceMode);
                    }
                }
            } // next sub area


            if (accumulatedStatusAndSources != null) {
                StatusAndSources preferedStatus = choosePreferredOrMerge(accumulatedStatusMap.get(superArea), accumulatedStatusAndSources, null, aggregationSourceMode);
                accumulatedStatusMap.put(superArea, preferedStatus);
            }

        } // next super area ....
    }

    private class DistributionResultHolder extends ResultHolder{
        Map<NamedArea, StatusAndSources> accumulatedStatusMap = new HashMap<>();
    }

    @Override
    protected ResultHolder createResultHolder() {
        return new DistributionResultHolder();
    }

    protected class StatusAndSources {

        private final PresenceAbsenceTerm status;
        private final Set<DescriptionElementSource> sources = new HashSet<>();

        public StatusAndSources(PresenceAbsenceTerm status, DescriptionElementBase deb, AggregationSourceMode aggregationSourceMode) {
            this.status = status;
            if (aggregationSourceMode == AggregationSourceMode.NONE){
                return;
            }else if (aggregationSourceMode == AggregationSourceMode.DESCRIPTION){
                sources.add(DescriptionElementSource.NewAggregationInstance(deb.getInDescription()));
            }else if (aggregationSourceMode == AggregationSourceMode.TAXON){
                if (deb.getInDescription().isInstanceOf(TaxonDescription.class)){
                    TaxonDescription td = CdmBase.deproxy(deb.getInDescription(), TaxonDescription.class);
                    sources.add(DescriptionElementSource.NewAggregationInstance(td.getTaxon()));
                }else{
                    logger.warn("Description is not of type TaxonDescription. Adding source not possible");
                }
            }else if (aggregationSourceMode == AggregationSourceMode.ALL || aggregationSourceMode == AggregationSourceMode.ALL_SAMEVALUE){
                addSourcesDeduplicated(this.sources, deb.getSources());
            }else{
                throw new RuntimeException("Unhandled source aggregation mode: " + aggregationSourceMode);
            }
        }

        public void addSources(Set<DescriptionElementSource> sources) {
            addSourcesDeduplicated(this.sources, sources);
        }
    }

    @Override
    protected void aggregateToParentTaxon(TaxonNode taxonNode,
            ResultHolder  resultHolder,
            Set<TaxonDescription> excludedDescriptions) {

        Map<NamedArea, StatusAndSources> accumulatedStatusMap =
                ((DistributionResultHolder)resultHolder).accumulatedStatusMap;

        Taxon taxon = CdmBase.deproxy(taxonNode.getTaxon());
        if(logger.isDebugEnabled()){
            logger.debug("accumulateByRank() [" + /*rank.getLabel() +*/ "] - taxon :" + taxonToString(taxon));
        }

        if(!taxonNode.getChildNodes().isEmpty()) {

            LinkedList<Taxon> childStack = new LinkedList<>();
            for (TaxonNode node : taxonNode.getChildNodes()){
                if (node == null){
                    continue;  //just in case if sortindex is broken
                }
                Taxon child = CdmBase.deproxy(node.getTaxon());
                //TODO maybe we should also use child catching from taxon node filter
                //     we could e.g. clone the filter and set the parent as subtree filter
                //     and this way get all children via service layer, this may improve also
                //     memory usage
                if (getConfig().getTaxonNodeFilter().isIncludeUnpublished()||
                        taxon.isPublish()){
                    childStack.add(child);
                }
            }

            while(childStack.size() > 0){

                Taxon childTaxon = childStack.pop();
                getSession().setReadOnly(childTaxon, true);
                if(logger.isTraceEnabled()){
                    logger.trace("                   subtaxon :" + taxonToString(childTaxon));
                }

                Set<Distribution> distributions = distributionsFor(descriptionsFor(childTaxon, excludedDescriptions));
                for(Distribution distribution : distributions) {

                    PresenceAbsenceTerm status = distribution.getStatus();
                    if (status == null || getByRankIgnoreStatusList().contains(status)
                            || (getConfig().isIgnoreAbsentStatusByRank() && status.isAbsenceTerm())){
                        continue;
                    }

                    NamedArea area = distribution.getArea();
                    AggregationSourceMode aggregationSourceMode = getConfig().getToParentSourceMode();

                    StatusAndSources childStatusAndSources = new StatusAndSources(status, distribution, aggregationSourceMode);
                    StatusAndSources preferedStatus = choosePreferredOrMerge(accumulatedStatusMap.get(area),
                            childStatusAndSources, null, aggregationSourceMode );
                    accumulatedStatusMap.put(area, preferedStatus);
                }

                // evict all initialized entities of the childTaxon
                // TODO consider using cascade="evict" in the model classes
    //                            for( TaxonDescription description : ((Taxon)childTaxonBase).getDescriptions()) {
    //                                for (DescriptionElementBase deb : description.getElements()) {
    //                                    getSession().evict(deb);
    //                                }
    //                                getSession().evict(description); // this causes in some cases the taxon object to be detached from the session
    //                            }
    //            getSession().evict(childTaxon); // no longer needed, save heap
            }
        }
    }

    private Distribution findDistributionForArea(TaxonDescription description, NamedArea area) {
        for(DescriptionElementBase item : description.getElements()) {
            if(!(item.isInstanceOf(Distribution.class))) {
                continue;
            }
            Distribution distribution = CdmBase.deproxy(item, Distribution.class);
            if(distribution.getArea().equals(area)) {
                return distribution;
            }
        }
        return null;
    }

    /**
     * Old: For if we want to reuse distributions only for the exact same status or
     * if we aggregate for each status separately. Otherwise use {@link #findDistributionForArea(TaxonDescription, NamedArea)}
     */
    private Distribution findDistributionForAreaAndStatus(TaxonDescription description, NamedArea area, PresenceAbsenceTerm status) {
        for(DescriptionElementBase item : description.getElements()) {
            if(!(item.isInstanceOf(Distribution.class))) {
                continue;
            }
            Distribution distribution = CdmBase.deproxy(item, Distribution.class);
            if(distribution.getArea().equals(area) && distribution.getStatus().equals(status)) {
                return distribution;
            }
        }
        return null;
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

    @Override
    protected TaxonDescription createNewDescription(Taxon taxon) {
        String title = taxon.getTitleCache();
        logger.debug("creating new description for " + title);
        TaxonDescription description = TaxonDescription.NewInstance(taxon);
        description.addType(DescriptionType.AGGREGATED_DISTRIBUTION);
        setDescriptionTitle(description, taxon);
        return description;
    }

    @Override
    protected boolean hasDescriptionType(TaxonDescription description) {
        return description.isAggregatedDistribution();
    }

    @Override
    protected void setDescriptionTitle(TaxonDescription description, Taxon taxon) {
        String title = taxon.getName() != null? taxon.getName().getTitleCache() : taxon.getTitleCache();
        description.setTitleCache("Aggregated distribution for " + title, true);
        return;
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

    private Set<TaxonDescription> descriptionsFor(Taxon taxon, Set<TaxonDescription> excludedDescriptions) {
        Set<TaxonDescription> result = new HashSet<>();
        for(TaxonDescription description: taxon.getDescriptions()) {
//            readOnlyIfInSession(description); //not needed for tests anymore
            if (!excludedDescriptions.contains(description)){
                result.add(description);
            }
        }
        return result;
    }

    private Set<Distribution> distributionsFor(Set<TaxonDescription> descriptions) {
        Set<Distribution> result = new HashSet<>();
        for(TaxonDescription description: descriptions) {
            for(DescriptionElementBase deb : description.getElements()) {
                if(deb.isInstanceOf(Distribution.class)) {
//                    readOnlyIfInSession(deb); //not needed for tests anymore
                    result.add(CdmBase.deproxy(deb, Distribution.class));
                }
            }
        }
        return result;
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

    private void replaceSources(Set<DescriptionElementSource> oldSources, Set<DescriptionElementSource> newSources) {
        Set<DescriptionElementSource> toDeleteSources = new HashSet<>(oldSources);
        for(DescriptionElementSource newSource : newSources) {
            boolean contained = false;
            for(DescriptionElementSource existingSource: oldSources) {
                if(existingSource.equalsByShallowCompare(newSource)) {
                    contained = true;
                    toDeleteSources.remove(existingSource);
                    break;
                }
            }
            if(!contained) {
                try {
                    oldSources.add(newSource.clone());
                } catch (CloneNotSupportedException e) {
                    // should never happen
                    throw new RuntimeException(e);
                }
            }
        }
        for (DescriptionElementSource toDeleteSource : toDeleteSources){
            oldSources.remove(toDeleteSource);
        }
    }


}
