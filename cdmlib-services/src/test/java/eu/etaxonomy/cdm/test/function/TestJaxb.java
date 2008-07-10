/* just for testing */

package eu.etaxonomy.cdm.test.function;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.IDatabaseService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.jaxb.CdmDocumentBuilder;
import eu.etaxonomy.cdm.model.DataSet;
import eu.etaxonomy.cdm.model.DataSetTest;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Journal;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.StrictReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;


public class TestJaxb {
	
	private static final Logger logger = Logger.getLogger(TestJaxb.class);
	
	private static final String dbName = "cdm_test_jaxb";
	private String server = "192.168.2.10";
	private String username = "edit";
	private String marshOut = new String( System.getProperty("user.home") + File.separator + "cdm_test_jaxb_marshalled.xml");

	private DataSet     dataSet = null;
	private DataSetTest dataSetTest = null;
	private CdmDocumentBuilder cdmDocumentBuilder = null;
	
    public TestJaxb() {	
    }

    public void roundTripXml2Xml() {
    	
		try {
			String password = CdmUtils.readInputLine("Password: ");
			DbSchemaValidation dbSchemaValidation = DbSchemaValidation.CREATE;
			ICdmDataSource datasource = CdmDataSource.NewMySqlInstance(server, dbName, username, password);
			CdmApplicationController appCtr = CdmApplicationController.NewInstance(datasource, dbSchemaValidation);
			
            // TODO: Call unmarshalling of test XML file
			dataSetTest = new DataSetTest();
			dataSet = new DataSet();
			dataSet = dataSetTest.buildDataSet(dataSet, true);

			/* 
			 * Deserialize
			 */
			
			// save data in DB
			appCtr.getTaxonService().saveTaxonAll(dataSet.getTaxa());

			/* 
			 * Serialize
			 */

			// read data from DB
			appCtr.getNameService().getAllNames(0, 10);
			
			// TODO: Call marshalling
			
			appCtr.close();

		} catch (DataSourceNotFoundException e) {
			logger.error("datasource error");
		} catch (TermNotFoundException e) {
			logger.error("defined terms not found");
		}
    	
    }
    
	public void testDeserialize(){
		try {
			String password = CdmUtils.readInputLine("Password: ");
			DbSchemaValidation dbSchemaValidation = DbSchemaValidation.CREATE;
			ICdmDataSource datasource = CdmDataSource.NewMySqlInstance(server, dbName, username, password);
			CdmApplicationController appCtr = CdmApplicationController.NewInstance(datasource, dbSchemaValidation);
			
            // TODO: Call unmarshalling of test XML file
			dataSetTest = new DataSetTest();
			dataSet = new DataSet();
			dataSet = dataSetTest.buildDataSet(dataSet, true);
			
			// save data in DB
			appCtr.getTaxonService().saveTaxonAll(dataSet.getTaxa());

			appCtr.close();

		} catch (DataSourceNotFoundException e) {
			logger.error("datasource error");
		} catch (TermNotFoundException e) {
			logger.error("defined terms not found");
		}
	}
	
	public void testSerialize(){
		
		CdmApplicationController appCtr = null;

		try {
			String password = CdmUtils.readInputLine("Password: ");
			DbSchemaValidation dbSchemaValidation = DbSchemaValidation.VALIDATE;
			ICdmDataSource datasource = CdmDataSource.NewMySqlInstance(server, dbName, username, password);
			appCtr = CdmApplicationController.NewInstance(datasource, dbSchemaValidation);
			

		} catch (DataSourceNotFoundException e) {
			logger.error("datasource error");
		} catch (TermNotFoundException e) {
			logger.error("defined terms not found");
		}
		TransactionStatus txStatus = appCtr.startTransaction();
		dataSet = new DataSet();

		// get data from DB

		try {

			dataSet.setTaxonomicNames(appCtr.getNameService().getAllNames(10, 0));
			dataSet.setReferences(appCtr.getReferenceService().getAllReferences(10, 0));

		} catch (Exception e) {
			logger.error("data retrieving error");
		}

		try {
			cdmDocumentBuilder = new CdmDocumentBuilder();
			cdmDocumentBuilder.marshal(dataSet, new FileWriter(marshOut));

		} catch (Exception e) {
			logger.error("marshalling error");
		} 
		appCtr.commitTransaction(txStatus);
		appCtr.close();
	}
	
	private void test(){
		System.out.println("Start Test Database Serializing/Deserializing");
		//testDeserialize();
		testSerialize();
		System.out.println("\nEnd Test Database Serializing/Deserializing");
	}
	
	/**
	 * @param args
	 */
	public static void  main(String[] args) {
		TestJaxb sc = new TestJaxb();
    	sc.test();
	}

}
