/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.printPub;

import java.io.FileNotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.DataSets;

import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubExportConfigurator;
import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubExportState;
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
        ExportResult result = defaultExport.invoke(config);
        checkAndGetData(result);

//        if (destinationDir != null) {

//          File outputFile = new File("/home/kluther/Dokumente/cdmLight/", "Test.odt");
//
//          try (FileOutputStream fos = new FileOutputStream(outputFile)) {
//              fos.write(data);
//          } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//      } else {
//          state.getResult().addError("No destination directory configured. File could not be written.");
//      }
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