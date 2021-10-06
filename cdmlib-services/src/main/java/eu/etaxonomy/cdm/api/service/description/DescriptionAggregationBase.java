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
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.IDescriptiveDataSetService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.UpdateResult;
import eu.etaxonomy.cdm.common.DynamicBatch;
import eu.etaxonomy.cdm.common.JvmLimitsException;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.NullProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.SubProgressMonitor;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter.ORDER;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
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

    private static final long BATCH_MIN_FREE_HEAP = 200  * 1024 * 1024;  //800 MB
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
    private UpdateResult result;

    private long batchMinFreeHeap = BATCH_MIN_FREE_HEAP;


    public final UpdateResult invoke(CONFIG config, ICdmRepository repository) throws JvmLimitsException{
        init(config, repository);
        return doInvoke();
    }

    protected UpdateResult doInvoke() {

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
                result.addException(new RuntimeException("Unhandled error during pre-aggregation", e));
                result.setError();
                done();
                return result;
            }

            subMonitor.worked(preAccumulateTicks);
            subMonitor.subTask("Accumulating "+pluralDataType()+" per taxon for taxon filter " + filter.toString());

            double startAccumulate = System.currentTimeMillis();

            //TODO AM move to invokeOnSingleTaxon()
            IProgressMonitor aggregateMonitor = new SubProgressMonitor(subMonitor, aggregationWorkTicks);
            try {
                aggregate(taxonNodeIdList, aggregateMonitor);
            } catch (Exception e) {
                result.addException(new RuntimeException("Unhandled error during aggregation: " + e.getMessage() , e));
                result.setError();
                done();
                return result;
            }

            double end = System.currentTimeMillis();
            logger.info("Time elapsed for accumulate only(): " + (end - startAccumulate) / (1000) + "s");
            logger.info("Time elapsed for invoking task(): " + (end - start) / (1000) + "s");

            done();
            return result;
        } catch (Exception e) {
            result.addException(new RuntimeException("Unhandled error during doInvoke", e));
            return result;
        }

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

            // load taxa for this batch
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

    protected interface ResultHolder{

    }

    protected void accumulateSingleTaxon(TaxonNode taxonNode){

        Taxon taxon = CdmBase.deproxy(taxonNode.getTaxon());
        if(logger.isDebugEnabled()){
            logger.debug("accumulate - taxon :" + taxonToString(taxon));
        }

        TaxonDescription targetDescription = getAggregatedDescription(taxon);
        ResultHolder resultHolder = createResultHolder();
        for (AggregationMode mode : getConfig().getAggregationModes()){
            if (mode == AggregationMode.ToParent){
                Set<TaxonDescription> excludedDescriptions = new HashSet<>();
//              excludedDescriptions.add(targetDescription); //not possible because aggregating from children
                aggregateToParentTaxon(taxonNode, resultHolder, excludedDescriptions);
            } else if (mode == AggregationMode.WithinTaxon){
                Set<TaxonDescription> excludedDescriptions = new HashSet<>();
                excludedDescriptions.add(targetDescription);
                aggregateWithinSingleTaxon(taxon, resultHolder, excludedDescriptions);
            }else{
                throw new IllegalArgumentException("Mode " + mode + " not yet supported");
            }
        }
        addAggregationResultToDescription(targetDescription, resultHolder);
        removeDescriptionIfEmpty(targetDescription);
    }

    protected void removeDescriptionIfEmpty(TaxonDescription description) {
        if (description.getElements().isEmpty()){
            description.getTaxon().removeDescription(description);
        }
    }

    protected abstract void addAggregationResultToDescription(TaxonDescription targetDescription,
            ResultHolder resultHolder);

    protected abstract void aggregateToParentTaxon(TaxonNode taxonNode, ResultHolder resultHolder,
            Set<TaxonDescription> excludedDescriptions);

    protected abstract void aggregateWithinSingleTaxon(Taxon taxon, ResultHolder resultHolder,
            Set<TaxonDescription> excludedDescriptions);

    protected abstract ResultHolder createResultHolder();

    /**
     * Either finds an existing taxon description of the given taxon or creates a new one.
     */
    private TaxonDescription getAggregatedDescription(Taxon taxon) {

        // find existing one
        for (TaxonDescription description : taxon.getDescriptions()) {
            if (hasDescriptionType(description)){
                if (logger.isDebugEnabled()){logger.debug("reusing existing aggregated description for " + taxonToString(taxon));}
                setDescriptionTitle(description, taxon);  //maybe we want to redefine the title
                return description;
            }
        }

        // create a new one
        return createNewDescription(taxon);
    }

    protected abstract TaxonDescription createNewDescription(Taxon taxon);

    protected abstract boolean hasDescriptionType(TaxonDescription description);

    protected abstract void setDescriptionTitle(TaxonDescription description, Taxon taxon) ;

    protected String taxonToString(TaxonBase<?> taxon) {
        if(logger.isTraceEnabled()) {
            return taxon.getTitleCache();
        } else {
            return taxon.toString();
        }
    }

    protected abstract List<String> descriptionInitStrategy();

    protected abstract void preAggregate(IProgressMonitor monitor);

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
        result = new UpdateResult();
    }

    protected void addSourcesDeduplicated(Set<DescriptionElementSource> target, Set<DescriptionElementSource> sourcesToAdd) {
        for(DescriptionElementSource source : sourcesToAdd) {
            boolean contained = false;
            if (!hasValidSourceType(source)&& !isAggregationSource(source)){  //only aggregate sources of defined source types
                continue;
            }
            for(DescriptionElementSource existingSource: target) {
                if(existingSource.equalsByShallowCompare(source)) {
                    contained = true;
                    break;
                }
            }
            if(!contained) {
                try {
                    target.add(source.clone());
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

    protected UpdateResult getResult() {
        return result;
    }

    protected void done(){
        getConfig().getMonitor().done();
    }

    public void setBatchMinFreeHeap(long batchMinFreeHeap) {
        this.batchMinFreeHeap = batchMinFreeHeap;
    }

}
