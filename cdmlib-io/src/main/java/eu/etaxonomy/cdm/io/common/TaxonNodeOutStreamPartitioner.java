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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.SubProgressMonitor;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author a.mueller
 * @since 01.07.2017
 */
public class TaxonNodeOutStreamPartitioner<STATE extends IoStateBase> {

    private static final Logger logger = Logger.getLogger(TaxonNodeOutStreamPartitioner.class);

//************************* STATIC ***************************************************/

	public static <ST  extends XmlExportState>  TaxonNodeOutStreamPartitioner NewInstance(
	        ICdmRepository repository, IoStateBase state,
            TaxonNodeFilter filter, Integer partitionSize,
            IProgressMonitor parentMonitor, Integer parentTicks){
		TaxonNodeOutStreamPartitioner<ST> taxonNodePartitioner
		        = new TaxonNodeOutStreamPartitioner(repository, state, filter, partitionSize,
		                parentMonitor, parentTicks);
		return taxonNodePartitioner;
	}

//*********************** VARIABLES *************************************************/


	/**
	 * counter for the partitions
	 */
	private int currentPartition;


	private TransactionStatus txStatus;


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
	        IProgressMonitor parentMonitor, Integer parentTicks){
		this.repository = repository;
		this.filter = filter;
		this.partitionSize = partitionSize;
		this.state = state;
		this.parentMonitor = parentMonitor;
		this.parentTicks = parentTicks;


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


//    public boolean hasNext() {
//        initialize();
//        return idIterator.hasNext()|| idList.size() > 0;
//    }

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
	        commitTransaction();
	        return null;
	    }
	}

	public void close(){
	    monitor.done();
	    commitTransaction();
	}

    private List<TaxonNode> getNextPartition() {
        List<Integer> partList = new ArrayList<>();

        if (txStatus != null){
            commitTransaction();
        }
        txStatus = startTransaction();
        while (partList.size() < partitionSize && idIterator.hasNext()){
            partList.add(idIterator.next());
            currentIndex++;
        }
        List<TaxonNode> partition = new ArrayList<>();
        if (!partList.isEmpty()){
            monitor.subTask(String.format("Reading partition %d/%d", currentPartition + 1, (totalCount / partitionSize) +1 ));
            List<String> propertyPaths = new ArrayList<String>();
            propertyPaths.add("taxon");
            propertyPaths.add("taxon.name");
            partition = repository.getTaxonNodeService().loadByIds(partList, propertyPaths);
            monitor.worked(partition.size());
            currentPartition++;
            monitor.subTask(String.format("Writing partition %d/%d", currentPartition, (totalCount / partitionSize) +1 ));
        }
        return partition;
    }

    private void commitTransaction() {
        if (!txStatus.isCompleted()){
            repository.commitTransaction(txStatus);
        }
    }

    private TransactionStatus startTransaction() {
        return repository.startTransaction();
    }



	/**
	 * @param recordsPerTransaction
	 * @param partitionedIO
	 * @param i
	 */
	private TransactionStatus getTransaction(int recordsPerTransaction, IPartitionedIO partitionedIO) {
		//if (loopNeedsHandling (i, recordsPerTransaction) || txStatus == null) {
			txStatus = partitionedIO.startTransaction();
			if(logger.isInfoEnabled()) {
				logger.debug("currentPartitionNumber = " + currentPartition + " - Transaction started");
			}
		//}
		return txStatus;
	}





}
