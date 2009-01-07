/**
 * 
 */
package eu.etaxonomy.cdm.app.excelImport.taxa;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.util.TestDatabase;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.excel.taxa.NormalExplicitImportConfigurator;

/**
 * @author a.babadshanjan
 * @created 06.01.2009
 *
 */
public class NormalExplicitActivator {

	private static final String dbName = "cdm_test_anahit";
	private static String fileName = 
		new String( System.getProperty("user.home") + "\\workspaces\\cdmlib\\app-import\\src\\main\\resources\\distribution\\Africa plus x.xls");
//	private static String fileName = new String( System.getProperty("user.home") + File.separator + "Africa plus x.xls");
	
	private static final ICdmDataSource destinationDb = TestDatabase.CDM_DB(dbName);
    private static final Logger logger = Logger.getLogger(NormalExplicitActivator.class);
    
    public static void main(String[] args) {

    	NormalExplicitImportConfigurator normalExplicitImportConfigurator = 
    		NormalExplicitImportConfigurator.NewInstance(fileName, destinationDb);

		CdmDefaultImport<NormalExplicitImportConfigurator> normalExplicitImport = 
			new CdmDefaultImport<NormalExplicitImportConfigurator>();

		// invoke import
		logger.debug("Invoking Normal Explicit Excel import");
		normalExplicitImport.invoke(normalExplicitImportConfigurator);
    	
    }
}
