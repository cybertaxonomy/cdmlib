/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.cdm2cdm;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.io.common.ITaxonNodeOutStreamPartitioner;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author a.mueller
 * @since 17.08.2019
 */
@Component
public class Cdm2CdmDescriptionImport
        extends Cdm2CdmImportBase {

    private static final long serialVersionUID = -2111102574346601573L;
    private static final Logger logger = Logger.getLogger(Cdm2CdmDescriptionImport.class);

    @Override
    protected void doInvoke(Cdm2CdmImportState state) {
        IProgressMonitor monitor = state.getConfig().getProgressMonitor();

        Cdm2CdmImportConfigurator config = state.getConfig();

        ITaxonNodeOutStreamPartitioner partitioner = getTaxonNodePartitioner(state, monitor, config);
        monitor.subTask("Start partitioning");
        doData(state, partitioner);
    }

    private void doData(Cdm2CdmImportState state, ITaxonNodeOutStreamPartitioner partitioner){
        TaxonNode node = partitioner.next();
        int partitionSize = 100;
        int count = 0;
        TransactionStatus tx = startTransaction();
        while (node != null) {
            doSingleNode(state, node);
            count++;
            if (count>=partitionSize){
                state.clearSessionCache();
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

    private void doSingleNode(Cdm2CdmImportState state, TaxonNode node) {
        Set<TaxonDescription> result = new HashSet<>();
        logger.info(node.treeIndex());
        try {
            for (TaxonDescription desc : node.getTaxon().getDescriptions()){
                result.add(detache(desc, state));
            }
        } catch (Exception e) {
            logger.warn("Exception during detache node " + node.treeIndex());
            e.printStackTrace();
        }
        try {
            if (!result.isEmpty()){
                getDescriptionService().saveOrUpdate((Set)result);
                getCommonService().saveOrUpdate(state.getToSave());
                state.clearToSave();
            }
        } catch (Exception e) {
            logger.warn("Exception during save node " + node.treeIndex());
             e.printStackTrace();
        }
    }



    @Override
    protected boolean doDescriptions(Cdm2CdmImportState state) {
        return true;
    }

    @Override
    protected boolean doCheck(Cdm2CdmImportState state) {
        return true;
    }

    @Override
    protected boolean isIgnore(Cdm2CdmImportState state) {
        return !state.getConfig().isDoDescriptions();
    }

}
