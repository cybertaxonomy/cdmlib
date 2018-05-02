/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.sdd.in;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author b.clark
 * @since 20.01.2009
 * @version 1.0
 */

//@Ignore // we ignore this test at the moment because it does not run with maven
//org.hibernate.PropertyAccessException: Null value was assigned to a property of primitive type setter of eu.etaxonomy.cdm.model.common.Language.protectedTitleCache
//...at eu.etaxonomy.cdm.persistence.dao.hibernate.common.DefinedTermDaoImpl.getLanguageByIso(DefinedTermDaoImpl.java:286)
public class SDDImportTest extends CdmTransactionalIntegrationTest {

    @SpringBeanByType
    SDDImport sddImport;

    @SpringBeanByType
    INameService nameService;

    private SDDImportConfigurator configurator;

    @Before
    public void setUp() throws URISyntaxException {
        //URL url = this.getClass().getResource("/eu/etaxonomy/cdm/io/sdd/SDDImportTest-input.xml");
        //URL url = this.getClass().getResource("/eu/etaxonomy/cdm/io/sdd/SDDImportTest-input2.xml");
        URL url = this.getClass().getResource("/eu/etaxonomy/cdm/io/sdd/SDD-Test-Simple.xml");
		URI uri = url.toURI();
//		URI	uri = URI.create("file:///C:/localCopy/Data/xper/Cichorieae-DA2.sdd.xml");
		assertNotNull(url);
		configurator = SDDImportConfigurator.NewInstance(uri, null);
    }

    @Test
    public void testInit() {
        assertNotNull("sddImport should not be null", sddImport);
        assertNotNull("nameService should not be null", nameService);
    }

    @Test
    @DataSet(/*loadStrategy=CleanSweepInsertLoadStrategy.class, */value="/eu/etaxonomy/cdm/database/BlankDataSet.xml")
	public void testDoInvoke() {
        sddImport.doInvoke(new SDDImportState(configurator));
        this.setComplete();
        logger.warn("Name service count: " + (nameService.count(null)));

        this.endTransaction();
        assertEquals("Number of TaxonNames should be 1", 1, nameService.count(null));
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}


}
