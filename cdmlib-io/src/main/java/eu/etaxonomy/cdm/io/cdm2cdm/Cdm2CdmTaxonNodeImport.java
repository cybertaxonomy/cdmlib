/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.cdm2cdm;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.io.common.ITaxonNodeOutStreamPartitioner;
import eu.etaxonomy.cdm.io.common.TaxonNodeOutStreamPartitioner;
import eu.etaxonomy.cdm.io.common.TaxonNodeOutStreamPartitionerConcurrent;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author a.mueller
 * @since 17.08.2019
 */
@Component
public class Cdm2CdmTaxonNodeImport
        extends Cdm2CdmImportBase {

    private static final long serialVersionUID = -2111102574346601573L;
    private static final Logger logger = Logger.getLogger(Cdm2CdmTaxonNodeImport.class);


    @Override
    protected void doInvoke(Cdm2CdmImportState state) {
        setState(state);
        IProgressMonitor monitor = state.getConfig().getProgressMonitor();

        Cdm2CdmImportConfigurator config = state.getConfig();

        ITaxonNodeOutStreamPartitioner partitioner = getPartitioner(state, monitor, config);
        monitor.subTask("Start partitioning");
        doData(state, partitioner);
    }

    private void doData(Cdm2CdmImportState state, ITaxonNodeOutStreamPartitioner partitioner){
        TaxonNode node = partitioner.next();
        int partitionSize = 100;
        int count = 0;
        TransactionStatus tx = startTransaction();
        while (node != null) {
            node = doSingleNode(state, node);
            count++;
            if (count>=partitionSize){
                clearCache();
                try {
                    commitTransaction(tx);
                } catch (Exception e) {
                    logger.warn("Exception during commit node " + node.treeIndex());
                    e.printStackTrace();
                }
                tx = startTransaction();
                count=0;
            }
            node = partitioner.next();
        }
        commitTransaction(tx);
        partitioner.close();
    }

    private TaxonNode doSingleNode(Cdm2CdmImportState state, TaxonNode node) {
        TaxonNode result = null;
        logger.info(node.treeIndex());
        try {
            result = detache(node);
        } catch (Exception e) {
            logger.warn("Exception during detache node " + node.treeIndex());
            e.printStackTrace();
        }
        try {
            if (result != null){
                getTaxonNodeService().saveOrUpdate(node);
                getCommonService().saveOrUpdate(toSave);
                toSave.clear();
            }
        } catch (Exception e) {
            logger.warn("Exception during save node " + node.treeIndex());
             e.printStackTrace();
        }

        return result;
    }

    private ITaxonNodeOutStreamPartitioner getPartitioner(Cdm2CdmImportState state, IProgressMonitor monitor,
            Cdm2CdmImportConfigurator config) {
        ITaxonNodeOutStreamPartitioner partitioner = config.getPartitioner();
        if (partitioner == null){
            if(!config.isConcurrent()){
                partitioner = TaxonNodeOutStreamPartitioner.NewInstance(sourceRepo(state), state,
                        state.getConfig().getTaxonNodeFilter(), 100,
                        monitor, 1, TaxonNodeOutStreamPartitioner.fullPropertyPaths);
                ((TaxonNodeOutStreamPartitioner)partitioner).setLastCommitManually(true);
            }else{
                partitioner = TaxonNodeOutStreamPartitionerConcurrent
                        .NewInstance(state.getConfig().getSource(), state.getConfig().getTaxonNodeFilter(),
                                1000, monitor, 1, TaxonNodeOutStreamPartitioner.fullPropertyPaths);
            }
        }
        return partitioner;
    }


    @Override
    protected boolean doCheck(Cdm2CdmImportState state) {
        return false;
    }

    @Override
    protected boolean isIgnore(Cdm2CdmImportState state) {
        return !state.getConfig().isDoTaxa();
    }

}
