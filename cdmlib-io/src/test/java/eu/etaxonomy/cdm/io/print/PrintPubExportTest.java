/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.print;

import java.io.FileNotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.DataSets;

import eu.etaxonomy.cdm.common.monitor.DefaultProgressMonitor;
import eu.etaxonomy.cdm.io.common.ExportResult;
import eu.etaxonomy.cdm.io.common.ExportType;
import eu.etaxonomy.cdm.io.out.TaxonTreeExportTestBase;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author k.luther
 * @since 10.06.2026
 */
public class PrintPubExportTest
        extends TaxonTreeExportTestBase<PrintPubExportConfigurator,PrintPubExportState> {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    //requires log level info for DefaultProgressMonitor and other monitors
    private static final boolean useCommandLineMonitor = false;

    @Before
    public void setUp()  {
        createFullTestDataSet();
    }

    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml")
    })
    public void testGeneralExport(){

        //config + invoke
        PrintPubExportConfigurator config = newConfigurator();
        if (useCommandLineMonitor) {
            config.setProgressMonitor(DefaultProgressMonitor.NewInstance());
        }
        ExportResult result = defaultExport.invoke(config);
        checkAndGetData(result);

        Assert.assertTrue(result.getExportType().equals(ExportType.PRINT_PUBLICATION));
        //test export type
    }

    @Override
    protected PrintPubExportConfigurator newConfigurator() {
        return PrintPubExportConfigurator.NewInstance();
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}