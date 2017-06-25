/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.sdd.out;

import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;

import org.junit.Before;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IExportConfigurator;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
/**
 * @author a.mueller
 * @created 02.02.2009
 */
public class SDDCdmExporterTest extends CdmTransactionalIntegrationTest {

	@SpringBeanByType
	SDDCdmExporter sddCdmExporter;

	@SpringBeanByType
	INameService nameService;

	private IExportConfigurator exportConfigurator;

	@Before
	public void setUp() {
		String url = "";
		//FIXME
		ICdmDataSource source = null;
		//exportConfigurator = SDDExportConfigurator.NewInstance(source, url, null);
	}

	@Test
	public void testInit() {
		assertNotNull("sddCdmExporter should not be null", sddCdmExporter);
		assertNotNull("nameService should not be null", nameService);
	}

	@Test
	public void testDoInvoke() {
		//sddCdmExporter.doInvoke(exportConfigurator, null);
		//assertEquals("Number of TaxonNames should be 1", 1, nameService.count());
	}

    @Override
    public void createTestDataSet() throws FileNotFoundException {}

}
