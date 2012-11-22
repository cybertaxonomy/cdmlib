// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.sdd.in;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import junit.framework.Assert;

import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IExportConfigurator;
import eu.etaxonomy.cdm.io.sdd.out.SDDCdmExporter;
import eu.etaxonomy.cdm.io.sdd.out.SDDExportConfigurator;
import eu.etaxonomy.cdm.io.sdd.out.SDDExportState;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author l.morris
 * @date 14 Nov 2012
 *
 */
public class SDDImportExportTest extends CdmTransactionalIntegrationTest {

	@SpringBeanByType
    SDDImport sddImport;

    private SDDImportConfigurator importConfigurator;
    
	@SpringBeanByType
	SDDCdmExporter sddCdmExporter;
	
	@SpringBeanByType
	INameService nameService;
	
	private IExportConfigurator exportConfigurator;

    @Before
    public void setUp() throws URISyntaxException, MalformedURLException {
        URL url = this.getClass().getResource("/eu/etaxonomy/cdm/io/sdd/SDDImportTest-input.xml");
		URI uri = url.toURI();
//		URI	uri = URI.create("file:///C:/localCopy/Data/xper/Cichorieae-DA2.sdd.xml");
        Assert.assertNotNull(url);
		importConfigurator = SDDImportConfigurator.NewInstance(uri, null);
		
		String exporturlStr ="SDDImportExportTest.sdd.xml";
		
		File f =  new File(exporturlStr);

		logger.warn("LORNA the exporturlStr " + f.toString());
		logger.warn("LORNA the exporturlStr uri " + f.toURI().toString());
		exporturlStr  = f.toURI().toURL().toString();
		logger.warn("LORNA the exporturlStr is " + exporturlStr);
		exportConfigurator = SDDExportConfigurator.NewInstance(null, exporturlStr);
		//exportConfigurator = SDDExportConfigurator.NewInstance(null, "file:///C:/Users/l.morris/workspace/cdmlib/cdmlib-io/SDDImportExportTest.sdd.xml");
		//exportConfigurator = SDDExportConfigurator.NewInstance(null, "SDDImportExportTest.sdd.xml", "file:/C:/Users/l.morris/workspace/cdmlib/cdmlib-io");
    }

    @Test
    public void testInit() {
        assertNotNull("sddImport should not be null", sddImport);
        assertNotNull("sddCdmExporter should not be null", sddCdmExporter);
    }

    @Test
	public void testDoInvoke() {
    	
    	//printDataSet(System.err, new String[]{"DEFINEDTERMBASE"});
    	
        sddImport.doInvoke(new SDDImportState(importConfigurator));
        
        
        commitAndStartNewTransaction(new String[]{"DEFINEDTERMBASE"});
        logger.setLevel(Level.DEBUG);
        
        logger.warn("Name service count: " + (nameService.count(null)));
        
        //sddCdmExporter.doInvoke(null);
		sddCdmExporter.doInvoke(new SDDExportState((SDDExportConfigurator) exportConfigurator));
        assertEquals("Number of TaxonNames should be 1", 1, nameService.count(null));
    }
}
