/* just for testing */

package eu.etaxonomy.cdm.test.function;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.IDatabaseService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.model.DataSet;
import eu.etaxonomy.cdm.model.DataSetTest;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Journal;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;


public class TestJaxb {
	
	private static final Logger logger = Logger.getLogger(TestJaxb.class);
	
	private static final String dbName = "cdm_test_jaxb";
	private DataSet     dataSet = null;
	private DataSetTest dataSetTest = null;
	
    public TestJaxb() {	
    	
	
	
    }

	public void testJaxb(){
		try {
			String server = "192.168.2.10";
			String username = "edit";
			String password = CdmUtils.readInputLine("Password: ");
			DbSchemaValidation dbSchemaValidation = DbSchemaValidation.CREATE;
			ICdmDataSource datasource = CdmDataSource.NewMySqlInstance(server, dbName, username, password);
			CdmApplicationController appCtr = CdmApplicationController.NewInstance(datasource, dbSchemaValidation);
			
			dataSetTest = new DataSetTest();
			dataSet = new DataSet();
			dataSet = dataSetTest.buildDataSet(dataSet, true);
			
			
			//save
			appCtr.getTaxonService().saveTaxonAll(dataSet.getTaxa());
//			appCtr.getNameService().getAllNames(limit, start);

			
			appCtr.close();

		} catch (DataSourceNotFoundException e) {
			logger.error("datasource error");
		} catch (TermNotFoundException e) {
			logger.error("defined terms not found");
		}
	}
	
	
	private void test(){
		System.out.println("Start TestDatabase");
		testJaxb();
		System.out.println("\nEnd TestDatabase");
	}
	
	/**
	 * @param args
	 */
	public static void  main(String[] args) {
		TestJaxb sc = new TestJaxb();
    	sc.test();
	}

}
