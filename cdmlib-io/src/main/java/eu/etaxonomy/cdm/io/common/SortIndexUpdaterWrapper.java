// $Id$
/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.monitor.DefaultProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CaseType;
import eu.etaxonomy.cdm.database.update.SortIndexUpdater;

/**
 * @author k.luther
 * @date 08.07.2016
 *
 */
@Component
public class SortIndexUpdaterWrapper extends CdmImportBase<SortIndexUpdaterConfigurator, DefaultImportState<SortIndexUpdaterConfigurator>> {

    private static final long serialVersionUID = 1152526455024556637L;
    private static final Logger logger = Logger.getLogger(SortIndexUpdaterWrapper.class);


    @Override
    protected void doInvoke(DefaultImportState<SortIndexUpdaterConfigurator> state) {
        SortIndexUpdaterConfigurator config = state.getConfig();
        SortIndexUpdater updater;
        ICdmDataSource source = config.getDestination();
        CaseType caseType = CaseType.caseTypeOfDatasource(config.getDestination());
        IProgressMonitor  monitor = DefaultProgressMonitor.NewInstance();
        if (config.isDoTaxonNode()){
            updater = SortIndexUpdater.NewInstance("Update taxonnode sortindex", "TaxonNode", "parent_id", "sortIndex", true);
            try {
                source.startTransaction();
                updater.invoke(config.getDestination(), monitor, caseType);
                source.commitTransaction();
            } catch (SQLException e) {

                monitor.warning("Stopped sortIndex updater");
            }
        }
        if (config.isDoFeatureNode()){
            updater = SortIndexUpdater.NewInstance("Update Feature node sortindex", "FeatureNode", "parent_id", "sortIndex", true);
            try {
                source.startTransaction();
                updater.invoke(config.getDestination(), monitor, caseType);
                source.commitTransaction();
            } catch (SQLException e) {

                monitor.warning("Stopped sortIndex updater");
            }
        }
        if (config.isDoPolytomousKeyNode()){
            updater = SortIndexUpdater.NewInstance("Update Polytomouskey node sortindex", "PolytomousKeyNode", "parent_id", "sortindex", true);
            try {
                source.startTransaction();
                updater.invoke(config.getDestination(), monitor, caseType);
                source.commitTransaction();
            } catch (SQLException e) {

                monitor.warning("Stopped sortIndex updater");
            }
        }
        return;

    }




    @Override
    protected boolean doCheck(DefaultImportState<SortIndexUpdaterConfigurator> state) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected boolean isIgnore(DefaultImportState<SortIndexUpdaterConfigurator> state) {
        // TODO Auto-generated method stub
        return false;
    }

}
