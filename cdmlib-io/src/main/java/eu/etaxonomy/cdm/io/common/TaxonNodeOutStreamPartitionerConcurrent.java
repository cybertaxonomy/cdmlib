/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.common.concurrent.ConcurrentQueue;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author a.mueller
 * @since 01.07.2017
 */
public class TaxonNodeOutStreamPartitionerConcurrent implements ITaxonNodeOutStreamPartitioner  {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(TaxonNodeOutStreamPartitionerConcurrent.class);

  //*********************** VARIABLES *************************************************/

    private ICdmRepository repository;

    private ICdmDataSource source;

    private ConcurrentQueue<TaxonNode> queue;

    private boolean readOnly = true;

    private TaxonNodeOutStreamPartitioner<?> innerPartitioner;

    private Future<ICdmRepository> repoFuture;

    private Integer partitionSize;
    private IProgressMonitor parentMonitor;
    private Integer parentTicks;
    private List<String> propertyPaths;
    private TaxonNodeFilter filter;



//************************* STATIC ***************************************************/

	public static TaxonNodeOutStreamPartitionerConcurrent NewInstance(
	        ICdmDataSource source, TaxonNodeFilter filter, Integer partitionSize,
            IProgressMonitor parentMonitor, Integer parentTicks){

	    TaxonNodeOutStreamPartitionerConcurrent taxonNodePartitionerThread
		        = new TaxonNodeOutStreamPartitionerConcurrent(source, filter, partitionSize,
		                parentMonitor, parentTicks, null);
		return taxonNodePartitionerThread;
	}

    public static ITaxonNodeOutStreamPartitioner NewInstance(ICdmDataSource source, TaxonNodeFilter filter,
            int partitionSize, IProgressMonitor parentMonitor, Integer parentTicks, List<String> fullpropertypaths) {

        TaxonNodeOutStreamPartitionerConcurrent taxonNodePartitionerThread
            = new TaxonNodeOutStreamPartitionerConcurrent(source, filter, partitionSize,
                parentMonitor, parentTicks, fullpropertypaths);
        return taxonNodePartitionerThread;
    }

//*********************** CONSTRUCTOR *************************************************/

	private TaxonNodeOutStreamPartitionerConcurrent(ICdmDataSource source,
	        TaxonNodeFilter filter, Integer partitionSize,
	        IProgressMonitor parentMonitor, Integer parentTicks, List<String> propertyPaths){

	    this.source = source;
	    this.queue = new ConcurrentQueue<>(10);
	    this.repoFuture = getExecutorService().submit(repoCall);
	    this.partitionSize = partitionSize;
	    this.parentMonitor = parentMonitor;
	    this.parentTicks = parentTicks;
	    this.propertyPaths = propertyPaths;
	    this.filter = filter;

	}

//************************ METHODS ****************************************************/
	boolean isStarted = false;
	private ExecutorService es;


    public void initialize(){
	    if (isStarted){
	        return;
	    }
	    getExecutorService().submit(()->{
            try {
                ICdmRepository repo = repoFuture.get();
                innerPartitioner = TaxonNodeOutStreamPartitioner.NewInstance(
                        repo, null, filter, partitionSize, parentMonitor, parentTicks, propertyPaths);

                //state = null
                TaxonNodeOutStreamPartitioner<?> partitioner = innerPartitioner;
                partitioner.setReadOnly(readOnly);
                 try {
                     TaxonNode node = partitioner.next();
                     while (node!= null) {
                        this.queue.enqueue(node);
                        node = partitioner.next();
                    }
                } catch (InterruptedException ex) {
                    System.out.println(Thread.currentThread().getName() +
                            " interrupted");
                }
            } catch (ExecutionException | InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
	    isStarted = true;
	}


    @Override
    public TaxonNode next(){
	    initialize();
	    try {
            return queue.dequeue();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
	}

    private Callable<ICdmRepository> repoCall = ()->{
        if (repository == null){
            System.out.println("start source repo");
            boolean omitTermLoading = true;
            repository = CdmApplicationController.NewInstance(source,
                   DbSchemaValidation.VALIDATE, omitTermLoading);
            System.out.println("end source repo");
        }
        return repository;
    };


    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }


    private ExecutorService getExecutorService() {
        if (es == null){
            es = Executors.newSingleThreadExecutor();
        }
        return es;
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub
    }


}
