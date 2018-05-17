/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.tropicos;

import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultImport;
import eu.etaxonomy.cdm.io.common.ImportResult;
import eu.etaxonomy.cdm.io.tropicos.in.TropicosNameImportConfigurator;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;


/**
 * @author a.mueller
 * @since 15.11.2017
 *
 */
public class TropicosNameImportTest extends CdmTransactionalIntegrationTest{

    @SpringBeanByName
    private CdmApplicationAwareDefaultImport<?> defaultImport;

    @SpringBeanByType
    private INameService nameService;

    @SpringBeanByType
    private ITaxonService taxonService;

    private TropicosNameImportConfigurator configShort;
    private TropicosNameImportConfigurator configLong;

    @Before
    public void setUp() {
        String inputFile = "/eu/etaxonomy/cdm/io/tropicos/TropicosNameImportTest-input.txt";

        try {
            URL url = this.getClass().getResource(inputFile);
            assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);

            String inputFileLong = "/eu/etaxonomy/cdm/io/tropicos/TropicosNameImportTest-input.txt";
            URL urlLong = this.getClass().getResource(inputFileLong);
            assertNotNull("URL for the test file '" + inputFileLong + "' does not exist", urlLong);

            configShort = TropicosNameImportConfigurator.NewInstance(url.toURI(), null);
            configLong = TropicosNameImportConfigurator.NewInstance(urlLong.toURI(), null);


        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
        assertNotNull("Configurator could not be created", configShort);
        assertNotNull("Configurator could not be created", configLong);
        assertNotNull("nameService should not be null", nameService);
    }

//***************************** TESTS *************************************//

    @Test
    @DataSet( value="/eu/etaxonomy/cdm/database/BlankDataSet.xml", loadStrategy=CleanSweepInsertLoadStrategy.class)
    //@Ignore
    public void testShort() {
        configShort.setCreateTaxa(true);
        ImportResult result = defaultImport.invoke(configShort);
        String report = result.createReport().toString();



        Integer expected = 2;
        Assert.assertEquals(expected, result.getNewRecords(TaxonName.class));

        Assert.assertTrue(report.length() > 0);
        System.out.println(report);

        List<TaxonName> list = nameService.list(TaxonName.class, null, null, null, null);
        Assert.assertEquals("There should be 2 new taxon names", 2, list.size());
        for (TaxonName name : list){
            //TODO
        }

        expected = 1;
        Assert.assertEquals(expected, result.getNewRecords(Reference.class));
    }

    @Test
    @DataSet( value="/eu/etaxonomy/cdm/database/BlankDataSet.xml", loadStrategy=CleanSweepInsertLoadStrategy.class)
    //@Ignore
    public void testShortCreateTaxa() {
        configShort.setCreateTaxa(true);
        ImportResult result = defaultImport.invoke(configShort);

        Integer expected = 2;
        Assert.assertEquals(expected, result.getNewRecords(Taxon.class));

        List<Taxon> list = taxonService.list(Taxon.class, null, null, null, null);
        Assert.assertEquals("There should be 2 new taxa", 2, list.size());
    }

    @Test
    @Ignore
    public void testLongFile() {
        ImportResult result = defaultImport.invoke(configLong);
        String report = result.createReport().toString();
        System.out.println(report);

        Integer expected = 118;  //did not count yet
        Assert.assertEquals(expected, result.getNewRecords(Reference.class));



    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
