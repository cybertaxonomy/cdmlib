// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.print;

import java.io.File;
import java.sql.SQLException;
import java.util.UUID;

import org.junit.Assert;

import org.apache.log4j.Logger;
import org.jdom.Element;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.monitor.DefaultProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.print.out.pdf.PdfOutputModule;
import eu.etaxonomy.cdm.print.out.xml.XMLOutputModule;

/**
 * @author l.morris
 * @date Mar 7, 2013
 *
 */
public class PubTest {
	
	private static final Logger logger = Logger.getLogger(PubTest.class);

	private static PublishConfigurator configurator; 
	
	private static Publisher publisher;
	
	
	/**
	 * @throws java.lang.Exception
	 */
	public static void setUpBeforeClass() throws Exception {
		
		ICdmDataSource dataSource = customDataSource();
		
		//Resource applicationContextResource, ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation, boolean omitTermLoading, IProgressMonitor progressMonitor)
		IProgressMonitor progressMonitor = DefaultProgressMonitor.NewInstance();
		
		//Connecting to a CDMDataSource
		ICdmApplicationConfiguration app = CdmApplicationController.NewInstance(null, dataSource, DbSchemaValidation.VALIDATE, false, progressMonitor);
		//ICdmApplicationConfiguration app = CdmApplicationController.NewInstance(resource, dataSource, DbSchemaValidation.VALIDATE, false, progressMonitor);
		//configurator = PublishConfigurator.NewLocalInstance(CdmStore.getCurrentApplicationConfiguration());//from taxeditor GeneratePdfHandler
		configurator = PublishConfigurator.NewLocalInstance(app);
				
		IXMLEntityFactory factory = configurator.getFactory();
		
		UUID taxonNodeUuid = UUID.fromString("a605e87e-113e-4ebd-ad97-f086b734b4da");//5168a18b-c0b1-44cc-80aa-7a5572fefe04
		Element taxonNodeElement = factory.getTaxonNode(taxonNodeUuid);
		configurator.addSelectedTaxonNodeElements(taxonNodeElement);
		
		configurator.setDoPublishEntireBranches(false);
		
		configurator.addOutputModule(new PdfOutputModule());
		configurator.addOutputModule(new XMLOutputModule());
		
		
		//configurator.setWebserviceUrl("http://localhost:8080/");
		
//		Element selectedTaxonNodeElement = new Element("TaxonNode");
//		
//		configurator.addSelectedTaxonNodeElements(selectedTaxonNodeElement);
		
		configurator.setExportFolder(new File("/Users/nho/tmp/"));
				
	}
	
    private static ICdmDataSource customDataSource() {

       String dataSourceName = CdmUtils.readInputLine("Database name: ");
       String username = CdmUtils.readInputLine("Username: ");
       String password = CdmUtils.readInputLine("Password: ");
       
       dataSourceName = (dataSourceName.equals("")) ? "cdm_test4" : dataSourceName;
       username = (username.equals("")) ? "ljm" : username;
       
       ICdmDataSource dataSource = CdmDataSource.NewMySqlInstance("160.45.63.201", "cdm_edit_flora_central_africa", 3306, "edit", password, NomenclaturalCode.ICNAFP);
       //ICdmDataSource dataSource = CdmDataSource.NewMySqlInstance("127.0.0.1", dataSourceName, 3306, username, password, NomenclaturalCode.ICBN);
       //ICdmDataSource dataSource = CdmDataSource.NewMySqlInstance("127.0.0.1", "cdm_edit_cichorieae", 3306, "ljm", password, NomenclaturalCode.ICBN);
       //ICdmDataSource dataSource = CdmDataSource.NewMySqlInstance("160.45.63.201", "cdm_edit_cichorieae", 3306, "edit", password, NomenclaturalCode.ICBN);
       boolean connectionAvailable;
       try {
           connectionAvailable = dataSource.testConnection();
           logger.debug("LORNA connection available " + connectionAvailable);
           Assert.assertTrue("Testdatabase is not available", connectionAvailable);

       } catch (ClassNotFoundException e1) {
           // TODO Auto-generated catch block
           e1.printStackTrace();
       } catch (SQLException e1) {
           // TODO Auto-generated catch block
           e1.printStackTrace();
       }
       return dataSource;
    }


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		PubTest pubTest = new PubTest();
		try {
			pubTest.setUpBeforeClass();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Auto-generated method stub

	}

}
