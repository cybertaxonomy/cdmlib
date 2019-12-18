/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.api.service.IProgressMonitorService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.UpdateResult;
import eu.etaxonomy.cdm.api.service.config.SortIndexUpdaterConfigurator;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.SubProgressMonitor;
import eu.etaxonomy.cdm.database.update.SortIndexUpdater;

/**
 * @author k.luther
 * @since 08.07.2016
 */
@Component
public class SortIndexUpdaterWrapper implements Serializable {

    private static final long serialVersionUID = 1152526455024556637L;
    private static final Logger logger = Logger.getLogger(SortIndexUpdaterWrapper.class);

    private static final String TAXON_NODE = "TaxonNode";
    private static final String TERM_NODE = "TermRelation";
    private static final String POLYTOMOUS_KEY_NODE = "PolytomousKeyNode";

    @Autowired
    IAgentService agentService;
    @Autowired
    ITaxonService taxonService;
    @Autowired
    ITaxonNodeService taxonNodeService;
    @Autowired
    protected IProgressMonitorService progressMonitorService;

    @Autowired
    private HibernateTransactionManager transactionManager;
//    @Autowired
//    private CdmRepository repository;

    public UpdateResult doInvoke(SortIndexUpdaterConfigurator config) {

        SortIndexUpdater updater;
        UpdateResult result = new UpdateResult();
       // CaseType caseType = CaseType.caseTypeOfDatasource(config.getDestination());

        IProgressMonitor  monitor = config.getMonitor();

        if (config.isDoTaxonNode()){
            updater = SortIndexUpdater.NewInstance(null, "Update taxon node sortindex", TAXON_NODE, "parent_id", "sortIndex", true);

            result.includeResult(update(updater, monitor));
        }
        if (config.isDoTermNode()){
            updater = SortIndexUpdater.NewInstance(null, "Update term node sortindex", TERM_NODE, "parent_id", "sortIndex", true);
            result.includeResult(update(updater, monitor));
        }
        if (config.isDoPolytomousKeyNode()){
            updater = SortIndexUpdater.NewInstance(null, "Update polytomous key node sortindex", POLYTOMOUS_KEY_NODE, "parent_id", "sortindex", true);
            result.includeResult(update(updater, monitor));
        }
        return result;
    }

    private UpdateResult update(SortIndexUpdater updater,  IProgressMonitor  monitor){
        UpdateResult updateResult = new UpdateResult();
        try {

            TransactionStatus tx;
            tx = startTransaction(true);
            String query = updater.createIndexMapQuery();
            SQLQuery sqlQuery = agentService.getSession().createSQLQuery(query);

            List<?> data = sqlQuery.list();
            int c = 2;
            if (updater.getTableName().equals(TAXON_NODE)){
                c= 3;
            }
            monitor.beginTask("Update index", data.size()*c);
            monitor.subTask("Create new index");
            IProgressMonitor subMonitor = new SubProgressMonitor(monitor, data.size());
            List<Integer[]> result = new ArrayList<>();
            int done = 0;
            for(Object object : data){
               Object[] row = (Object[])object;
               Object oId = row[0];

                if (oId != null){
                    int id = Integer.valueOf(oId.toString());
                    Integer[] rowArray = new Integer[2];
                    Object oParentId = row[1];
                    if (oParentId != null){
                        int parentId = Integer.valueOf(oParentId.toString());
                        rowArray[1]= parentId;

                    }else{
                        rowArray[1]= null;
                    }
                    rowArray[0]= id;
                    result.add(rowArray);
                }
                done++;
                subMonitor.internalWorked(done);
            }
            subMonitor.done();
            monitor.subTask("update indeces");

            Map<Integer, Set<Integer>> indexMap =  updater.makeIndexMap(result);
            subMonitor = new SubProgressMonitor(monitor, indexMap.size());
            done = 0;
            for (Map.Entry<Integer, Set<Integer>> entry: indexMap.entrySet()){
                String idSet = SortIndexUpdater.makeIdSetString(entry.getValue());
                query = updater.createUpdateIndicesQuery(null,entry.getKey(), idSet);
                sqlQuery = agentService.getSession().createSQLQuery(query);
                int resultInt = sqlQuery.executeUpdate();
                logger.debug("update all indice with index "+entry.getKey()+ " - " + resultInt);
                done++;
                subMonitor.internalWorked(done);
            }
            subMonitor.done();
            //Update childrenCount
            if (updater.getTableName().equals(TAXON_NODE)){

                query = updater.getChildrenCountQuery();
                sqlQuery = taxonService.getSession().createSQLQuery(query);
                data = sqlQuery.list();
                subMonitor = new SubProgressMonitor(monitor, data.size());
                int realCount;
                int countChildren;
                int work = 0;
                for(Object object : data){
                   Object[] row = (Object[])object;
                   realCount =  ((Number) row[0]).intValue();
                   countChildren = ((Number) row[1]).intValue();
                   int id = ((Number) row[2]).intValue();

                   if (realCount != countChildren){
                       query = updater.getUpdateChildrenCount(realCount, id);
                       sqlQuery = taxonNodeService.getSession().createSQLQuery(query);
                       int resultInt = sqlQuery.executeUpdate();
                       logger.debug("update all childrenCount "+ resultInt);
                   }
                   work ++;
                   subMonitor.internalWorked(work);
                   subMonitor.done();
                 }
            }
            //TODO: correct handling of updateResult!
            monitor.done();
            commitTransaction(tx);
            return updateResult;
        } catch (Exception e) {
            monitor.warning("Stopped sortIndex updater");
            updateResult.setAbort();
            updateResult.addException(e);
        }
        return null;
    }

    public TransactionStatus startTransaction(Boolean readOnly) {

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

        TransactionStatus txStatus = transactionManager.getTransaction(txDef);

        getSession().setFlushMode(FlushMode.COMMIT);

        return txStatus;
    }

    private Session getSession() {
        return agentService.getSession();
    }

    public void commitTransaction(TransactionStatus txStatus){
        logger.debug("commiting transaction ...");
        transactionManager.commit(txStatus);
        return;
    }

}
