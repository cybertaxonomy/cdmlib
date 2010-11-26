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

import java.net.URISyntaxException;
import java.net.URL;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author b.clark
 * @created 20.01.2009
 * @version 1.0
 */

@Ignore // we ignore this test at the moment because it does not run with maven
public class SDDImportTest extends CdmTransactionalIntegrationTest {
	
	@SpringBeanByType
	SDDImport sddImport;
	
	@SpringBeanByType
	INameService nameService;
	
	private SDDImportConfigurator configurator;
	
	@Before
	public void setUp() throws URISyntaxException {
		URL url = this.getClass().getResource("/eu/etaxonomy/cdm/io/sdd/SDDImportTest-input.xml");
		Assert.assertNotNull(url);
		configurator = SDDImportConfigurator.NewInstance(url.toURI(), null);
	}
	
	@Test
	public void testInit() {
		assertNotNull("sddImport should not be null", sddImport);
		assertNotNull("nameService should not be null", nameService);
	}
	
	@Test
	public void testDoInvoke() {
		sddImport.doInvoke(new SDDImportState(configurator));
		this.setComplete();
		this.endTransaction();
		assertEquals("Number of TaxonNames should be 1", 1, nameService.count(null));
	}

}
