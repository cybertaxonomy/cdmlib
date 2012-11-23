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
import java.sql.SQLException;

import junit.framework.Assert;

import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IExportConfigurator;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.sdd.out.SDDCdmExporter;
import eu.etaxonomy.cdm.io.sdd.out.SDDExportConfigurator;
import eu.etaxonomy.cdm.io.sdd.out.SDDExportState;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
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
	
	@SpringBeanByType
	ITaxonService taxonService;
	
	@SpringBeanByType
	IClassificationService classificationService;
	
	private IExportConfigurator exportConfigurator;

    @Before
    public void setUp() throws URISyntaxException, MalformedURLException {
        URL url = this.getClass().getResource("/eu/etaxonomy/cdm/io/sdd/SDDImportTest-input.xml");
		URI uri = url.toURI();
//		URI	uri = URI.create("file:///C:/localCopy/Data/xper/Cichorieae-DA2.sdd.xml");
        Assert.assertNotNull(url);
        
        logger.setLevel(Level.DEBUG);
		//ICdmDataSource dataSource = CdmDataSource.NewMySqlInstance("192.168.2.10", "cdm_test_niels2", 3306, "edit", password, code);
        String dataSourceName = "test";
		String password = CdmUtils.readInputLine("Password: ");
		ICdmDataSource dataSource = CdmDataSource.NewMySqlInstance("127.0.0.1", "cdm_test", 3306, "ljm", password, NomenclaturalCode.ICBN);
		//ICdmDataSource dataSource = CdmDataSource.NewMySqlInstance("160.45.63.201", "cdm_test", 3306, "edit", password, NomenclaturalCode.ICBN);
		boolean connectionAvailable;
		try {
			connectionAvailable = dataSource.testConnection();
			logger.debug("LORNA connection avaiable " + connectionAvailable);
			Assert.assertTrue("Testdatabase is not available", connectionAvailable);
			
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		CdmPersistentDataSource.save(dataSourceName, dataSource);

		try {
			CdmPersistentDataSource loadedDataSource = CdmPersistentDataSource.NewInstance(dataSourceName);
//			CdmApplicationController.NewInstance(loadedDataSource, DbSchemaValidation.CREATE);

			NomenclaturalCode loadedCode = loadedDataSource.getNomenclaturalCode();
			
			Assert.assertEquals(NomenclaturalCode.ICBN, loadedCode);
			
			//importConfigurator = SDDImportConfigurator.NewInstance(uri, null);
			//importConfigurator = SDDImportConfigurator.NewInstance(uri, dataSource);
			importConfigurator = SDDImportConfigurator.NewInstance(uri, loadedDataSource);
			
			String exporturlStr ="SDDImportExportTest.sdd.xml";
			
			File f =  new File(exporturlStr);

			exporturlStr  = f.toURI().toURL().toString();
			logger.debug("LORNA the exporturlStr is " + exporturlStr);
			//exportConfigurator = SDDExportConfigurator.NewInstance(null, exporturlStr);
			exportConfigurator = SDDExportConfigurator.NewInstance(loadedDataSource, exporturlStr);
			//exportConfigurator = SDDExportConfigurator.NewInstance(null, "file:///C:/Users/l.morris/workspace/cdmlib/cdmlib-io/SDDImportExportTest.sdd.xml");
			//exportConfigurator = SDDExportConfigurator.NewInstance(null, "SDDImportExportTest.sdd.xml", "file:/C:/Users/l.morris/workspace/cdmlib/cdmlib-io");
	   
			CdmPersistentDataSource.delete(loadedDataSource);//lorna - where should i put this

		} catch (DataSourceNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
       
        logger.setLevel(Level.WARN);
        commitAndStartNewTransaction(new String[]{"DEFINEDTERMBASE"});
        logger.setLevel(Level.DEBUG);
        
        logger.debug("Name count: " + (nameService.count(null)));
        logger.debug("Classification count: " + (classificationService.count(Classification.class)));
        logger.debug("Taxon count: " + (taxonService.count(Taxon.class)));
        
        //sddCdmExporter.doInvoke(null);
		sddCdmExporter.doInvoke(new SDDExportState((SDDExportConfigurator) exportConfigurator));
        assertEquals("Number of TaxonNames should be 1", 1, nameService.count(null));
    }
}
