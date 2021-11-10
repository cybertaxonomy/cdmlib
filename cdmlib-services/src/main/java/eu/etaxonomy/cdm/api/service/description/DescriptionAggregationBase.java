/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.description;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.api.service.DeleteResult;
import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.IDescriptiveDataSetService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.common.DynamicBatch;
import eu.etaxonomy.cdm.common.JvmLimitsException;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.NullProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.SubProgressMonitor;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter.ORDER;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * A common base class to run aggregation tasks on descriptive data.
 *
 * Usable for all types of descriptive data like structured descriptive data,
 * ( {@link CategoricalData and QuantitativeData}, {@link Distribution},
 * {@link Media}, etc.
 *
 * @author a.mueller
 * @since 03.11.2019
 */
public abstract class DescriptionAggregationBase<T extends DescriptionAggregationBase<T, CONFIG>, CONFIG extends DescriptionAggregationConfigurationBase<T>> {

    public static final Logger logger = Logger.getLogger(DescriptionAggregationBase.class);

    private static final long BATCH_MIN_FREE_HEAP = 150  * 1024 * 1024;  //800 MB
    /**
     * ratio of the initially free heap which should not be used
     * during the batch processing. This amount of the heap is reserved
     * for the flushing of the session and to the index
     */
    private static final double BATCH_FREE_HEAP_RATIO = 0.9;
//    private static final int BATCH_SIZE_BY_AREA = 1000;
//    private static final int BATCH_SIZE_BY_RANK = 500;
    private static final int BATCH_SIZE_BY_TAXON = 200;

    private ICdmRepository repository;
    private CONFIG config;
    private DeleteResult result;

    private long batchMinFreeHeap = BATCH_MIN_FREE_HEAP;


    public final DeleteResult invoke(CONFIG config, ICdmRepository repository){
        init(config, repository);
        return doInvoke();
    }

    protected DeleteResult doInvoke() {

        try {
            //TODO FIXME use UpdateResult

            double start = System.currentTimeMillis();
            IProgressMonitor monitor = getConfig().getMonitor();

            // only for debugging:
            //logger.setLevel(Level.TRACE); // TRACE will slow down a lot since it forces loading all term representations
            //Logger.getLogger("org.hibernate.SQL").setLevel(Level.DEBUG);
            logger.info("Hibernate JDBC Batch size: " +  getSession().getSessionFactory().getSessionFactoryOptions().getJdbcBatchSize());

            TaxonNodeFilter filter = getConfig().getTaxonNodeFilter();
            filter.setOrder(ORDER.TREEINDEX_DESC); //DESC guarantees that child taxa are aggregated before parent
            filter.setIncludeRootNodes(false);  //root nodes do not make sense for aggregation

            monitor.beginTask("Accumulating " + pluralDataType(), 100);
            Long countTaxonNodes = getTaxonNodeService().count(filter);
            int aggregationWorkTicks = countTaxonNodes.intValue();
            logger.info(aggregationWorkTicks + " taxa to aggregate");
            int getIdListTicks = 1;
            int preAccumulateTicks = 1;
            monitor.worked(5);
            SubProgressMonitor subMonitor = SubProgressMonitor.NewStarted(monitor,
                    95, "Accumulating " + pluralDataType(), aggregationWorkTicks + getIdListTicks + preAccumulateTicks);

            subMonitor.subTask("Get taxon node ID list");
            List<Integer> taxonNodeIdList = getTaxonNodeService().idList(filter);

            subMonitor.worked(getIdListTicks);

            try {
                preAggregate(subMonitor);
            } catch (Exception e) {
                return handleException(e, "Unhandled error during pre-aggregation");
            }

            try {
                verifyConfiguration(subMonitor);
            } catch (Exception e) {
                return handleException(e, "Unhandled error during configuration check");
            }

            subMonitor.worked(preAccumulateTicks);
            subMonitor.subTask("Accumulating "+pluralDataType()+" per taxon for taxon filter " + filter.toString());

            double startAccumulate = System.currentTimeMillis();

            //TODO AM move to invokeOnSingleTaxon()
            IProgressMonitor aggregateMonitor = new SubProgressMonitor(subMonitor, aggregationWorkTicks);
            try {
                aggregate(taxonNodeIdList, aggregateMonitor);
            } catch (Exception e) {
                return handleException(e, "Unhandled error during aggregation");
            }

            double end = System.currentTimeMillis();
            logger.info("Time elapsed for accumulate only(): " + (end - startAccumulate) / (1000) + "s");
            logger.info("Time elapsed for invoking task(): " + (end - start) / (1000) + "s");

            done();
            return getResult();
        } catch (Exception e) {
            getResult().addException(new RuntimeException("Unhandled error during doInvoke", e));
            return getResult();
        }
    }

    private DeleteResult handleException(Exception e, String unhandledMessage) {
        Exception ex;
        if (e instanceof AggregationException){
            ex = e;
        }else{
            ex = new RuntimeException(unhandledMessage + ": " + e.getMessage() , e);
            e.printStackTrace();
        }
        getResult().addException(ex);
        getResult().setError();
        done();
        return getResult();
    }

    protected void aggregate(List<Integer> taxonNodeIdList, IProgressMonitor subMonitor)  throws JvmLimitsException {

        DynamicBatch batch = new DynamicBatch(BATCH_SIZE_BY_TAXON, batchMinFreeHeap);
        batch.setRequiredFreeHeap(BATCH_FREE_HEAP_RATIO);
        //TODO AM from aggByRank          batch.setMaxAllowedGcIncreases(10);

        TransactionStatus txStatus = startTransaction(false);
        initTransaction();

        // visit all accepted taxa
//        subMonitor.beginTask("Work on taxa.", taxonNodeIdList.size());
        subMonitor.subTask("Accumulating bottom up " + taxonNodeIdList.size() + " taxa.");

        //TODO FIXME this was a Taxon not a TaxonNode id list
        Iterator<Integer> taxonIdIterator = taxonNodeIdList.iterator();

        while (taxonIdIterator.hasNext() || batch.hasUnprocessedItems()) {
            if(getConfig().getMonitor().isCanceled()){
                break;
            }

            if(txStatus == null) {
                // transaction has been committed at the end of this batch, start a new one
                txStatus = startTransaction(false);
                initTransaction();
            }

            //load taxa for this batch
            List<Integer> taxonIds = batch.nextItems(taxonIdIterator);
//            logger.debug("accumulateByArea() - taxon " + taxonPager.getFirstRecord() + " to " + taxonPager.getLastRecord() + " of " + taxonPager.getCount() + "]");

            //TODO AM adapt init-strat to taxonnode if it stays a taxon node list
            List<OrderHint> orderHints = new ArrayList<>();
            orderHints.add(OrderHint.BY_TREE_INDEX_DESC);
            List<TaxonNode> taxonNodes = getTaxonNodeService().loadByIds(taxonIds, orderHints, descriptionInitStrategy());

            // iterate over the taxa and accumulate areas
            // start processing the new batch

            for(TaxonNode taxonNode : taxonNodes) {
                if(getConfig().getMonitor().isCanceled()){
                    break;
                }
                subMonitor.subTask("Accumulating " + taxonNode.getTaxon().getTitleCache());

                accumulateSingleTaxon(taxonNode);
                batch.incrementCounter();

                subMonitor.worked(1);

                //TODO handle canceled better if needed
                if(subMonitor.isCanceled()){
                    return;
                }

                if(!batch.isWithinJvmLimits()) {
                    break; // flushAndClear and start with new batch
                }
            } // next taxon

//            flushAndClear();

            // commit for every batch, otherwise the persistent context
            // may grow too much and eats up all the heap
            commitTransaction(txStatus);
            txStatus = null;


            // flushing the session and to the index (flushAndClear() ) can impose a
            // massive heap consumption. therefore we explicitly do a check after the
            // flush to detect these situations and to reduce the batch size.
            if(getConfig().isAdaptBatchSize() && batch.getJvmMonitor().getGCRateSiceLastCheck() > 0.05) {
                batch.reduceSize(0.5);
            }

        } // next batch of taxa

    }

    /**
     * Base class for temporary aggregation results for a single taxon. For within taxon
     * and from child to parent aggregation. Should be extended by implementing aggregation classes.
     */
    protected class ResultHolder{
        //descriptions are identifiable and therefore are not deleted automatically by removing them from taxon or specimen
        //here we store all descriptions that need to be deleted after aggregation as they are not needed anymore
        Set<DescriptionBase<?>> descriptionsToDelete = new HashSet<>();
    }

    protected void accumulateSingleTaxon(TaxonNode taxonNode){

        Taxon taxon = CdmBase.deproxy(taxonNode.getTaxon());
        if(logger.isDebugEnabled()){logger.debug("accumulate - taxon :" + taxonToString(taxon));}

        //description
        TaxonDescription targetDescription = getAggregatedDescription(taxon);

        //temporary result
        ResultHolder resultHolder = createResultHolder();
        for (AggregationMode mode : getConfig().getAggregationModes()){
            if (mode == AggregationMode.ToParent){
                aggregateToParentTaxon(taxonNode, resultHolder, new HashSet<>()); ////excludedDescriptions because aggregating from children
            } else if (mode == AggregationMode.WithinTaxon){
                Set<TaxonDescription> excludedDescriptions = new HashSet<>();
                excludedDescriptions.add(targetDescription);
                aggregateWithinSingleTaxon(taxon, resultHolder, excludedDescriptions);
            }else{
                throw new IllegalArgumentException("Mode " + mode + " not yet supported");
            }
        }

        //persist
        mergeAggregationResultIntoTargetDescription(targetDescription, resultHolder);
        removeDescriptionIfEmpty(targetDescription, resultHolder);  //AM: necessary? Seems to be done in addAggregationResultToDescription() already...
        deleteDescriptionsToDelete(resultHolder);
    }

    /**
     * Remove descriptions to be deleted from persistent data if possible.
     */
    private void deleteDescriptionsToDelete(DescriptionAggregationBase<T, CONFIG>.ResultHolder resultHolder) {
        for (DescriptionBase<?> descriptionToDelete : resultHolder.descriptionsToDelete){
            if (descriptionToDelete.isPersited()){
                getSession().flush(); // move to service method #9801
                DeleteResult descriptionDeleteResult = repository.getDescriptionService().deleteDescription(descriptionToDelete);
                //TODO handle result somehow if not OK, but careful, descriptions may be linked >1x and therefore maybe deleted only after last link was removed
                this.getResult().includeResult(descriptionDeleteResult, true);
            }
        }
    }

    /**
     * If target description is empty the description is added to the descriptions to be deleted
     * in the result holder.
     */
    protected void removeDescriptionIfEmpty(TaxonDescription description, ResultHolder resultHolder) {
        if (description.getElements().isEmpty()){
            description.getTaxon().removeDescription(description);
            resultHolder.descriptionsToDelete.add(description);
        }
    }

    /**
     * Removes description elements not needed anymore from their description and
     * updates the {@link DeleteResult}.
     */
    protected void handleDescriptionElementsToRemove(TaxonDescription targetDescription,
            Set<? extends DescriptionElementBase> elementsToRemove) {
        //remove all elements not needed anymore
        for(DescriptionElementBase elementToRemove : elementsToRemove){
            targetDescription.removeElement(elementToRemove);
            //AM: do we really want to add each element to the deleteResult?
            this.getResult().addDeletedObject(elementToRemove);
        }
    }

    /**
     * Adds the temporary aggregated data (resultHolder) to the description.
     * Tries to reuse existing data if possible.
     */
    protected abstract void mergeAggregationResultIntoTargetDescription(TaxonDescription targetDescription,
            ResultHolder resultHolder);

    protected abstract void aggregateToParentTaxon(TaxonNode taxonNode, ResultHolder resultHolder,
            Set<TaxonDescription> excludedDescriptions);

    protected abstract void aggregateWithinSingleTaxon(Taxon taxon, ResultHolder resultHolder,
            Set<TaxonDescription> excludedDescriptions);

    /**
     * Creates a {@link ResultHolder} object to temporarily store the aggregation
     * result (within taxon and from child to parent) for a single taxon.
     */
    protected abstract ResultHolder createResultHolder();

    /**
     * Either finds an existing taxon description for the given taxon or creates a new one.
     */
    private TaxonDescription getAggregatedDescription(Taxon taxon) {

        // find existing one
        for (TaxonDescription description : taxon.getDescriptions()) {
            if (hasDescriptionType(description)){
                if (logger.isDebugEnabled()){logger.debug("reusing existing aggregated description for " + taxonToString(taxon));}
                setDescriptionTitle(description, taxon);  //maybe we want to redefine the title
                if (getConfig().isDoClearExistingDescription()){
                    clearDescription(description);
                }

                return description;
            }
        }

        // create a new one
        TaxonDescription newDescription = createNewDescription(taxon);

        //TODO maybe not necessary here as the new description only will be kept if not empty
        //(otherwise they could end up in updated and deleted objects which is unwanted)
        getResult().addUpdatedObject(newDescription);
        return newDescription;
    }

    /**
     * Removes all description elements of the according type from the
     * (aggregation) description.
     */
    private void clearDescription(TaxonDescription aggregationDescription) {
        Set<DescriptionElementBase> deleteCandidates = new HashSet<>();
        for (DescriptionElementBase descriptionElement : aggregationDescription.getElements()) {

            if(isRelevantDescriptionElement(descriptionElement)) {
                deleteCandidates.add(descriptionElement);
            }
        }
        if(deleteCandidates.size() > 0){
            for(DescriptionElementBase descriptionElement : deleteCandidates) {
                aggregationDescription.removeElement(descriptionElement);
                getDescriptionService().deleteDescriptionElement(descriptionElement);
                getResult().addDeletedObject(descriptionElement);
            }
            getDescriptionService().saveOrUpdate(aggregationDescription);
        }
    }

    protected <S extends DescriptionElementBase, TE extends DefinedTermBase<?>> void mergeDescriptionElements(
            TaxonDescription targetDescription, Map<TE, S> newElementsMap, Class<S> debClass) {

        //init elements to remove
        Set<DescriptionElementBase> elementsToRemove = new HashSet<>(
                targetDescription.getElements().stream()
                    .filter(el->el.isInstanceOf(debClass))
                    .collect(Collectors.toSet()));

        //for each character in "characters of new elements"
        for (TE keyTerm : newElementsMap.keySet()) {
            S newElement = newElementsMap.get(keyTerm);

            //if elements for this character exist in old data, remember any of them to keep
            //(in clean data there should be only max. 1
            DescriptionElementBase elementToStay = null;
            for (DescriptionElementBase existingDeb : elementsToRemove) {
                if(existingDeb.getFeature().equals(keyTerm)){
                    elementToStay = existingDeb;
                    break;
                }
            }

            //if there is no element for this character in old data, add the new element for this character to the target description (otherwise reuse old element)
            if (elementToStay == null){
                targetDescription.addElement(newElement);
            }else{
                elementsToRemove.remove(elementToStay);
                mergeDescriptionElement(elementToStay, newElement);
            }
        }

        handleDescriptionElementsToRemove(targetDescription, elementsToRemove);
    }

    /**
     * Merges a new (temporary description element into an existing one)
     */
    protected abstract <S extends DescriptionElementBase>
            void mergeDescriptionElement(S targetElement, S newElement);

    protected void mergeSourcesForDescriptionElements(DescriptionElementBase deb, Set<DescriptionElementSource> newSources) {
        Set<DescriptionElementSource> toDeleteSources = new HashSet<>(deb.getSources());
        for(DescriptionElementSource newSource : newSources) {
            boolean contained = false;
            for(DescriptionElementSource existingSource: deb.getSources()) {
                if(existingSource.equalsByShallowCompare(newSource)) {
                    contained = true;
                    toDeleteSources.remove(existingSource);
                    break;
                }
            }
            if(!contained) {
                try {
                    deb.addSource(newSource.clone());
                } catch (CloneNotSupportedException e) {
                    // should never happen
                    throw new RuntimeException(e);
                }
            }
        }
        for (DescriptionElementSource toDeleteSource : toDeleteSources){
            deb.removeSource(toDeleteSource);
        }
    }

    protected abstract TaxonDescription createNewDescription(Taxon taxon);

    protected abstract boolean hasDescriptionType(TaxonDescription description);

    protected abstract void setDescriptionTitle(TaxonDescription description, Taxon taxon);

    protected abstract boolean isRelevantDescriptionElement(DescriptionElementBase deb);

    protected String taxonToString(TaxonBase<?> taxon) {
        if(logger.isTraceEnabled()) {
            return taxon.getTitleCache();
        } else {
            return taxon.toString();
        }
    }

    protected abstract List<String> descriptionInitStrategy();

    protected abstract void preAggregate(IProgressMonitor monitor);

    protected abstract void verifyConfiguration(IProgressMonitor monitor);

    /**
     * hook for initializing object when a new transaction starts
     */
    protected abstract void initTransaction();

    protected abstract String pluralDataType();

    private void init(CONFIG config, ICdmRepository repository) {
        this.repository = repository;
        this.config = config;
        if(config.getMonitor() == null){
            config.setMonitor(new NullProgressMonitor());
        }
        result = new DeleteResult();
    }

    protected void addSourcesDeduplicated(DescriptionElementBase targetDeb, Set<DescriptionElementSource> sourcesToAdd) {
        for(DescriptionElementSource source : sourcesToAdd) {
            boolean contained = false;
            if (!hasValidSourceType(source)&& !isAggregationSource(source)){  //only aggregate sources of defined source types
                continue;
            }
            for(DescriptionElementSource existingSource: targetDeb.getSources()) {
                if(existingSource.equalsByShallowCompare(source)) {
                    contained = true;
                    break;
                }
            }
            if(!contained) {
                try {
                    targetDeb.addSource(source.clone());
                } catch (CloneNotSupportedException e) {
                    // should never happen
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private boolean hasValidSourceType(DescriptionElementSource source) {
        return getConfig().getAggregatingSourceTypes().contains(source.getType());
    }

    private boolean isAggregationSource(DescriptionElementSource source) {
        return source.getType().equals(OriginalSourceType.Aggregation) && source.getCdmSource() != null;
    }

// ******************** GETTER / SETTER *************************/

    protected IDescriptionService getDescriptionService(){
        return repository.getDescriptionService();
    }

    protected IDescriptiveDataSetService getDescriptiveDatasetService() {
        return repository.getDescriptiveDataSetService();
    }

    protected ITaxonService getTaxonService() {
        return repository.getTaxonService();
    }

    protected ITaxonNodeService getTaxonNodeService() {
        return repository.getTaxonNodeService();
    }

    protected ITermService getTermService() {
        return repository.getTermService();
    }

    protected IClassificationService getClassificationService() {
        return repository.getClassificationService();
    }

    protected PlatformTransactionManager getTransactionManager(){
        return repository.getTransactionManager();
    }

    // TODO merge with CdmRepository#startTransaction() into common base class
    protected void commitTransaction(TransactionStatus txStatus){
        logger.debug("commiting transaction ...");
        repository.commitTransaction(txStatus);
        return;
    }

    protected TransactionStatus startTransaction(Boolean readOnly) {

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

        TransactionStatus txStatus = getTransactionManager().getTransaction(txDef);
        getSession().setFlushMode(FlushMode.COMMIT);

        return txStatus;
    }

    protected Session getSession() {
        return getDescriptionService().getSession();
    }

    protected ICdmRepository getRepository() {
        return repository;
    }

    protected CONFIG getConfig() {
        return config;
    }

    protected DeleteResult getResult() {
        return result;
    }

    protected void done(){
        getConfig().getMonitor().done();
    }

    public void setBatchMinFreeHeap(long batchMinFreeHeap) {
        this.batchMinFreeHeap = batchMinFreeHeap;
    }

}
