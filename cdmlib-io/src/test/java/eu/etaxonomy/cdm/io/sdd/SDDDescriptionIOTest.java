/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.sdd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URL;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author b.clark
 * @created 20.01.2009
 * @version 1.0
 */


@Ignore
public class SDDDescriptionIOTest extends CdmTransactionalIntegrationTest {
	
	@SpringBeanByType
	SDDDescriptionIO sddDescriptionIo;
	
	@SpringBeanByType
	INameService nameService;
	
	private SDDImportConfigurator configurator;
	
	@Before
	public void setUp() {
		URL url = this.getClass().getResource("/eu/etaxonomy/cdm/io/sdd/SDDDescriptionIOTest-input.xml");
		configurator = SDDImportConfigurator.NewInstance(url.toString(), null);
	}
	
	@Test
	public void testInit() {
		assertNotNull("sddDescriptionIo should not be null",sddDescriptionIo);
		assertNotNull("nameService should not be null", nameService);
	}
	
	@Test
	public void testDoInvoke() {
		sddDescriptionIo.doInvoke(new SDDImportState(configurator));
		this.setComplete();
		this.endTransaction();
		assertEquals("Number of TaxonNames should be 1", 1, nameService.count(null));
	}

}
