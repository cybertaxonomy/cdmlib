/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.SubProgressMonitor;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;

/**
 * @author a.mueller
 * @since 01.07.2017
 */
public class TaxonNodeOutStreamPartitioner<STATE extends IoStateBase>
        implements ITaxonNodeOutStreamPartitioner {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(TaxonNodeOutStreamPartitioner.class);


    private static final List<String> defaultPropertyPaths = Arrays.asList(new String[]{"taxon","taxon.name"});

    public static final List<String> fullPropertyPaths = Arrays.asList(new String[]{
            "*",
            "excludedNote.*",
            "classification.*",
            "classification.name.*",
            "classification.description.*",
            "classification.rootNode.*",
            "classification.rootNode.excludedNote.*",
            "parent.*",
            "agentRelations.*",
            "agentRelations.agent.*",
            "agentRelations.agent.sources.*",
            "agentRelations.agent.sources.citation.*",
            "agentRelations.agent.contact.*",
            "agentRelations.agent.institutionalMemberships.*",
            "agentRelations.agent.institutionalMemberships.institute.*",
            "agentRelations.agent.institutionalMemberships.institute.contact.*",
            "agentRelations.type.*",
            "agentRelations.type.representations.*",
            "taxon.*",
            "taxon.extensions.type.*",
            "taxon.extensions.type.representations.*",
            "taxon.extensions.type.vocabulary.*",
            "taxon.extensions.type.vocabulary.representations.*",
            "taxon.extensions.type.vocabulary.termRelations.*",
            "taxon.sources.*",

            "taxon.name.*",
            "taxon.name.relationsFromThisName.*",
            "taxon.name.relationsToThisName.*",
            "taxon.name.sources.*",
            "taxon.name.extensions.type.*",
            "taxon.name.extensions.type.representations.*",
            "taxon.name.extensions.type.vocabulary.*",
            "taxon.name.extensions.type.vocabulary.terms.*",
            "taxon.name.extensions.type.vocabulary.terms.type.*",
            "taxon.name.extensions.type.vocabulary.terms.representations.*",
            "taxon.name.homotypicalGroup.*",

            "taxon.synonyms.*",
            "taxon.synonyms.name.*",
            "taxon.synonyms.name.relationsFromThisName.*",
            "taxon.synonyms.name.relationsToThisName.*",
            "taxon.synonyms.name.sources.*",
            "taxon.synonyms.markers.type.*",
            "taxon.synonyms.markers.type.representations.*",
            "taxon.synonyms.markers.type.vocabulary.*",
            "taxon.synonyms.markers.type.vocabulary.terms.*",
            "taxon.synonyms.markers.type.vocabulary.terms.type.*",
            "taxon.synonyms.markers.type.vocabulary.terms.representations.*",


            "taxon.name.combinationAuthorship.*",
            "taxon.name.combinationAuthorship.sources.*",
            "taxon.name.combinationAuthorship.contact.*",
            "taxon.name.combinationAuthorship.teamMembers.*",
            "taxon.name.combinationAuthorship.teamMembers.contact.*",
            "taxon.name.combinationAuthorship.teamMembers.sources.*",

            "taxon.name.exCombinationAuthorship.*",
            "taxon.name.basionymAuthorship.*",
            "taxon.name.basionymAuthorship.sources.*",
            "taxon.name.basionymAuthorship.contact.*",
            "taxon.name.basionymAuthorship.teamMembers.*",
            "taxon.name.basionymAuthorship.teamMembers.contact.*",
            "taxon.name.basionymAuthorship.teamMembers.sources.*",
            "taxon.name.exBasionymAuthorship.*",

            "taxon.descriptions.*",
            "taxon.descriptions.elements",
            "taxon.descriptions.elements.*",
            "taxon.descriptions.elements.modifyingText.*",
            "taxon.descriptions.elements.sources.*",
            "taxon.descriptions.elements.sources.citation.*",
            "taxon.descriptions.elements.area.*",
            "taxon.descriptions.elements.area.representations.*",
            "taxon.descriptions.elements.area.annotations.*",
            "taxon.descriptions.elements.area.vocabulary.*",
            "taxon.descriptions.elements.area.vocabulary.terms.*",
            "taxon.descriptions.elements.area.vocabulary.terms.type.*",
            "taxon.descriptions.elements.area.vocabulary.terms.annotations.*",
            "taxon.descriptions.elements.area.vocabulary.terms.representations.*",
//            "taxon.descriptions.elements.area.vocabulary.terms.representations.annotations.*",
            "taxon.descriptions.elements.area.vocabulary.representations.*",

    });

//************************* STATIC ***************************************************/

	public static <ST  extends IoStateBase>  TaxonNodeOutStreamPartitioner NewInstance(
	        ICdmRepository repository, ST state,
            TaxonNodeFilter filter, Integer partitionSize,
            IProgressMonitor parentMonitor, Integer parentTicks){

	    TaxonNodeOutStreamPartitioner<ST> taxonNodePartitioner
		        = new TaxonNodeOutStreamPartitioner(repository, state, filter, partitionSize,
		                parentMonitor, parentTicks, null);
		return taxonNodePartitioner;
	}

    public static <ST  extends IoStateBase> TaxonNodeOutStreamPartitioner NewInstance(
            ICdmRepository repository, ST state,
            TaxonNodeFilter filter, Integer partitionSize,
            IProgressMonitor parentMonitor, Integer parentTicks, List<String> propertyPath){

        TaxonNodeOutStreamPartitioner<ST> taxonNodePartitioner
                = new TaxonNodeOutStreamPartitioner(repository, state, filter, partitionSize,
                        parentMonitor, parentTicks, propertyPath);
        return taxonNodePartitioner;
    }

//*********************** VARIABLES *************************************************/


	/**
	 * counter for the partitions
	 */
	private int currentPartition;

	private TransactionStatus txStatus;
	private Map<TaxonNode, TransactionStatus> txMap = new HashMap<>();
	private TransactionStatus txStatus_old;

    private boolean readOnly = true;

    /**
     * If <code>true</code> the final commit/rollback is executed only by calling
     * {@link #close()}
     */
    private boolean lastCommitManually = false;



    private List<String> propertyPaths = defaultPropertyPaths;

//******************


    private final ICdmRepository repository;

	private final TaxonNodeFilter filter;

	private STATE state;

    /**
     * Number of records handled in the partition
     */
	private final int partitionSize;

	private int totalCount = -1;
	private List<Integer> idList;

	private IProgressMonitor parentMonitor;
	private Integer parentTicks;

	private SubProgressMonitor monitor;

	private LinkedList<TaxonNode> fifo = new LinkedList<>();

	private Iterator<Integer> idIterator;

	private int currentIndex;

	private static final int retrieveFactor = 1;
	private static final int iterateFactor = 2;


	//*********************** CONSTRUCTOR *************************************************/

	private TaxonNodeOutStreamPartitioner(ICdmRepository repository, STATE state,
	        TaxonNodeFilter filter, Integer partitionSize,
	        IProgressMonitor parentMonitor, Integer parentTicks, List<String> propertyPaths){
		this.repository = repository;
		this.filter = filter;
		this.partitionSize = partitionSize;
		this.state = state;
		this.parentMonitor = parentMonitor;
		this.parentTicks = parentTicks;
		if (propertyPaths != null){
		    this.propertyPaths = propertyPaths;
		}
	}

//************************ METHODS ****************************************************/

    public void initialize(){
	    if (totalCount < 0){

	        parentMonitor.subTask("Compute total number of records");
	        totalCount = ((Long)repository.getTaxonNodeService().count(filter)).intValue();
	        idList = repository.getTaxonNodeService().idList(filter);
	        int parTicks = this.parentTicks == null? totalCount : this.parentTicks;

	        monitor = SubProgressMonitor.NewStarted(parentMonitor, parTicks,
	                "Taxon node streamer", totalCount * (retrieveFactor +  iterateFactor));
	        idIterator = idList.iterator();
	        monitor.subTask("id iterator created");
	    }
	}


	@Override
    public TaxonNode next(){
	    int currentIndexAtStart = currentIndex;
	    initialize();
	    if(fifo.isEmpty()){
	        List<TaxonNode> list = getNextPartition();
	        fifo.addAll(list);
	    }
	    if (!fifo.isEmpty()){
	        TaxonNode result = fifo.removeFirst();
	        // worked should be called after each step is ready,
	        //this is usually after each next() call but not for the first
	        if (currentIndexAtStart > 0){
	            monitor.worked(iterateFactor);
	        }
	        return result;
	    }else{
	        if(!lastCommitManually){
	            commitTransaction();
	        }
	        return null;
	    }
	}

	@Override
    public void close(){
	    monitor.done();
	    commitTransaction();
	}


	int i = 0;
    private List<TaxonNode> getNextPartition() {
        List<Integer> partList = new ArrayList<>();

        if (txStatus != null){
            commitTransaction();
        }

        txStatus = startTransaction();
//        if (readOnly){
//            txStatus.setRollbackOnly();  //unclear if this is correct way to handle rollback, see comment on method
//        }
        while (partList.size() < partitionSize && idIterator.hasNext()){
            partList.add(idIterator.next());
            currentIndex++;
        }
        List<TaxonNode> partition = new ArrayList<>();
        if (!partList.isEmpty()){
            monitor.subTask(String.format("Reading partition %d/%d", currentPartition + 1, (totalCount / partitionSize) +1 ));
            OrderHint orderHint = new OrderHint("treeIndex", SortOrder.ASCENDING);
            List<OrderHint> orderHints = Arrays.asList(new OrderHint[]{orderHint});
            partition = repository.getTaxonNodeService().loadByIds(partList, orderHints, propertyPaths);
            monitor.worked(partition.size());
            currentPartition++;
            monitor.subTask(String.format("Writing partition %d/%d", currentPartition, (totalCount / partitionSize) +1 ));
        }
        return partition;
    }

    public void commit(TaxonNode node){
        txMap.remove(node);
    }

    private void commitTransaction() {
//        TransactionStatus txStatus = null;
//        if(this.txStatus.size()>0){
//            txStatus =  this.txStatus.poll();
//        }
//        TransactionStatus txStatus = txStatus_old;
        if (txStatus != null && !txStatus.isCompleted()){
            if (this.readOnly){
                repository.rollbackTransaction(txStatus);
            }else{
                repository.commitTransaction(txStatus);
            }
        }
//        txStatus_old = this.txStatus;
    }

    private TransactionStatus startTransaction() {
        return repository.startTransaction(readOnly);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isLastCommitManually() {
        return lastCommitManually;
    }

    public void setLastCommitManually(boolean lastCommitManually) {
        this.lastCommitManually = lastCommitManually;
    }
}
