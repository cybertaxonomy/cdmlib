/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.operation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.common.monitor.DefaultProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.update.SortIndexUpdater;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.DefaultImportState;
import eu.etaxonomy.cdm.io.operation.config.SortIndexUpdaterConfigurator;

/**
 * @author k.luther
 * @since 08.07.2016
 *
 */
@Component
public class SortIndexUpdaterWrapper extends CdmImportBase<SortIndexUpdaterConfigurator, DefaultImportState<SortIndexUpdaterConfigurator>> {

    private static final long serialVersionUID = 1152526455024556637L;
    private static final Logger logger = Logger.getLogger(SortIndexUpdaterWrapper.class);


    @Override
    protected void doInvoke(DefaultImportState<SortIndexUpdaterConfigurator> state) {
        SortIndexUpdaterConfigurator config =  state.getConfig();
        SortIndexUpdater updater;

       // CaseType caseType = CaseType.caseTypeOfDatasource(config.getDestination());
        IProgressMonitor  monitor = DefaultProgressMonitor.NewInstance();

        if (config.isDoTaxonNode()){
            updater = SortIndexUpdater.NewInstance("Update taxonnode sortindex", "TaxonNode", "parent_id", "sortIndex", true);

            update(updater, monitor);


        }
        if (config.isDoFeatureNode()){
            updater = SortIndexUpdater.NewInstance("Update Feature node sortindex", "FeatureNode", "parent_id", "sortIndex", true);
            update(updater, monitor);
        }
        if (config.isDoPolytomousKeyNode()){
            updater = SortIndexUpdater.NewInstance("Update Polytomouskey node sortindex", "PolytomousKeyNode", "parent_id", "sortindex", true);
           update(updater, monitor);
        }
        return;

    }

    private void update(SortIndexUpdater updater,  IProgressMonitor  monitor){
        try {
            TransactionStatus tx;
            tx = startTransaction();
            String query = updater.createIndexMapQuery();
            SQLQuery sqlQuery = getAgentService().getSession().createSQLQuery(query);

            List data = sqlQuery.list();

           List<Integer[]> result = new ArrayList<Integer[]>();
           int id;
           int parentId;
           Object oId;
           Object oParentId;
           Integer[] rowArray = new Integer[2];
            for(Object object : data)
            {
               Object[] row = (Object[])object;
               oId = row[0];

                if (oId != null){
                    id = Integer.valueOf(oId.toString());
                    rowArray = new Integer[2];
                    oParentId = row[1];
                    if (oParentId != null){
                        parentId = Integer.valueOf(oParentId.toString());
                        rowArray[1]= parentId;

                    }else{
                        rowArray[1]= null;
                    }
                    rowArray[0]= id;
                    result.add(rowArray);
                }
            }
            Map<Integer, Set<Integer>> indexMap =  updater.makeIndexMap(result);
            for (Map.Entry<Integer, Set<Integer>> entry: indexMap.entrySet()){
                String idSet = updater.makeIdSetString(entry.getValue());
                query = updater.createUpdateIndicesQuery(null,entry.getKey(), idSet);
                sqlQuery = getAgentService().getSession().createSQLQuery(query);
                int resultInt = sqlQuery.executeUpdate();
                logger.debug("update all indice with index "+entry.getKey()+ " - " + resultInt);
            }
            //Update childrenCount
            if (updater.getTableName().equals("TaxonNode")){
                query = updater.getChildrenCountQuery();
                sqlQuery = getTaxonNodeService().getSession().createSQLQuery(query);
                data = sqlQuery.list();
                int realCount;
                int countChildren;
                for(Object object : data)
                {
                   Object[] row = (Object[])object;
                   realCount =  ((Number) row[0]).intValue();
                   countChildren = ((Number) row[1]).intValue();
                   id = ((Number) row[2]).intValue();

                   if (realCount != countChildren){
                       query = updater.getUpdateChildrenCount(realCount, id);
                       sqlQuery = getTaxonNodeService().getSession().createSQLQuery(query);
                       int resultInt = sqlQuery.executeUpdate();
                       logger.debug("update all childrenCount "+ resultInt);
                   }
                 }
            }
              commitTransaction(tx);
        } catch (SQLException e) {

            monitor.warning("Stopped sortIndex updater");
        }
    }

    @Override
    protected boolean isIgnore(DefaultImportState<SortIndexUpdaterConfigurator> state) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean doCheck(DefaultImportState<SortIndexUpdaterConfigurator> state) {
        return true;
    }

}
