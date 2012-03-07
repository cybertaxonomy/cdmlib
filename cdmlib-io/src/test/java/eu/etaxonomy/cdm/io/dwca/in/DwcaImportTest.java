// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.in;

import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.io.common.events.LoggingIoObserver;
import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;

/**
 * @author a.mueller
 * @date 23.11.2011
 */
public class DwcaImportTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaImportTest.class);
	
	private URI uri;
	private DwcaImportConfigurator configurator;
	private DwcaImport dwcaImport;
	private DwcaImportState state;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception{
		DefaultTermInitializer initializer = new DefaultTermInitializer();
		initializer.initialize();
	}
	
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		String inputFile = "/eu/etaxonomy/cdm/io/dwca/in/DwcaZipToStreamConverterTest-input.zip";
		URL url = this.getClass().getResource(inputFile);
		uri = url.toURI();
		assertNotNull("URI for the test file '" + inputFile + "' does not exist", uri);
		try {
			configurator = DwcaImportConfigurator.NewInstance(url.toURI(), null);
			configurator.addObserver(new LoggingIoObserver());
		} catch (URISyntaxException e) {
			e.printStackTrace();
			Assert.fail();
		}
		assertNotNull("Configurator could not be created", configurator);
		dwcaImport = new DwcaImport();
		state = new DwcaImportState(configurator);
	}
	
	
	@Test
	public void testResultSet() {
		boolean result = dwcaImport.invoke(state);
		Assert.assertTrue("Import result should be true", result);
	}
	
}
