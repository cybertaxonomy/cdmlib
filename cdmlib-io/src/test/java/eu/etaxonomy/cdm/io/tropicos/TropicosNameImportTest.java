/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.tropicos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultImport;
import eu.etaxonomy.cdm.io.common.ImportResult;
import eu.etaxonomy.cdm.io.tropicos.in.TropicosNameImportConfigurator;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmIoTransactionalSpringIntegrationTest;

/**
 * @author a.mueller
 * @since 15.11.2017
 */
public class TropicosNameImportTest extends CdmIoTransactionalSpringIntegrationTest{

    @Autowired
    @Qualifier("defaultImport")
    private CdmApplicationAwareDefaultImport<?> defaultImport;

    @Autowired
    private INameService nameService;

    @Autowired
    private ITaxonService taxonService;

    private TropicosNameImportConfigurator configShort;
    private TropicosNameImportConfigurator configLong;

    @BeforeEach
    public void setUp() {
        String inputFile = "/eu/etaxonomy/cdm/io/tropicos/TropicosNameImportTest-input.txt";

        try {
            URL url = this.getClass().getResource(inputFile);
            assertNotNull(url, "URL for the test file '" + inputFile + "' does not exist");

            String inputFileLong = "/eu/etaxonomy/cdm/io/tropicos/TropicosNameImportTest-input.txt";
            URL urlLong = this.getClass().getResource(inputFileLong);
            assertNotNull(urlLong, "URL for the test file '" + inputFileLong + "' does not exist");

            configShort = TropicosNameImportConfigurator.NewInstance(URI.fromUrl(url), null);
            configLong = TropicosNameImportConfigurator.NewInstance(URI.fromUrl(urlLong), null);

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        assertNotNull(configShort, "Configurator could not be created");
        assertNotNull(configLong, "Configurator could not be created");
        assertNotNull(nameService, "nameService should not be null");
    }

//***************************** TESTS *************************************//

    @Test
    @CdmDataSet(cleanSweep = true, value = "/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
    public void testShort() {
        configShort.setCreateTaxa(true);
        ImportResult result = defaultImport.invoke(configShort);
        assertTrue(result.isSuccess());
        assertEquals(0,result.getExceptions().size());
        assertEquals(0,result.getErrors().size());
        String report = result.createReport().toString();

        Integer expected = 2;
        assertEquals(expected, result.getNewRecords(TaxonName.class));

        assertTrue(report.length() > 0);
        System.out.println(report);

        List<TaxonName> list = nameService.list(TaxonName.class, null, null, null, null);
        assertEquals(2, list.size(), "There should be 2 new taxon names");
        for (TaxonName name : list){
            //TODO
        }

        expected = 1;
        assertEquals(expected, result.getNewRecords(Reference.class));
    }

    @Test
    @CdmDataSet(cleanSweep = true, value = "/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
    //@Disabled
    public void testShortCreateTaxa() {
        configShort.setCreateTaxa(true);
        ImportResult result = defaultImport.invoke(configShort);
        assertTrue(result.isSuccess());
        assertEquals(0,result.getExceptions().size());
        assertEquals(0,result.getErrors().size());

        Integer expected = 2;
        assertEquals(expected, result.getNewRecords(Taxon.class));

        List<Taxon> list = taxonService.list(Taxon.class, null, null, null, null);
        assertEquals(2, list.size(), "There should be 2 new taxa");
    }

    @Test
    @Disabled
    public void testLongFile() {
        ImportResult result = defaultImport.invoke(configLong);
        String report = result.createReport().toString();
        System.out.println(report);

        Integer expected = 118;  //did not count yet
        assertEquals(expected, result.getNewRecords(Reference.class));
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}