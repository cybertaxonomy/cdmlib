/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.sdd.in;

import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.DataSets;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.IPolytomousKeyService;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.io.sdd.ikeyplus.IkeyPlusImport;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

public class IkeyPlusImportTest extends CdmTransactionalIntegrationTest {

    @SpringBeanByType
    private IkeyPlusImport ikeyPlusImport;

    @SpringBeanByType
    private IPolytomousKeyService polytomousKeyService;

    private URI sddUri;

    @Before
    public void setUp() throws URISyntaxException {

        String xxx = "/eu/etaxonomy/cdm/io/sdd/SDDImportTest-input.xml";
        String yyy = "/eu/etaxonomy/cdm/io/sdd/Cichorieae-fullSDD.xml";

        URL url = this.getClass().getResource(yyy);
        URI uri = URI.fromUrl(url);
        Assert.assertNotNull(url);
//        configurator = SDDImportConfigurator.NewInstance(uri, null);
        sddUri = uri;
    }

    @Test
    public void testInit() {
        assertNotNull("sddUri must not be null", sddUri);
    }

    @Test
    @DataSets({
        @DataSet(/*loadStrategy=CleanSweepInsertLoadStrategy.class,*/ value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml")
    })
    public void testDoInvoke() {

    	UUID newKeyUuid = null;
        try {
            ikeyPlusImport.getKey(sddUri, null);
            newKeyUuid = ikeyPlusImport.getCdmKey().getUuid();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        commitAndStartNewTransaction(null);

        PolytomousKey pk = polytomousKeyService.find(newKeyUuid);
        Assert.assertNotNull(pk);

//        assertEquals("Number of TaxonNames should be 1", 1, nameService.count(null));
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}