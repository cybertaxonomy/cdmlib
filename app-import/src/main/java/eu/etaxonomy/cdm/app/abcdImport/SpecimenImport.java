package eu.etaxonomy.cdm.app.abcdImport;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.unitsPortal.SpecimenImportConfigurator;

public class SpecimenImport {
private static Logger logger = Logger.getLogger(SpecimenImport.class);
	
	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
	final static String xmlSource = "/home/patricia/Desktop/multiABCD.xml";
	final static String excelSource ="/home/patricia/Desktop/CDMtabular9c04a474e2_23_09_08.xls";
	
	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_patricia();
	//check - import
	static final CHECK check = CHECK.IMPORT_WITHOUT_CHECK;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String source;
		
		try {
			if (args[0] == "xml")
				source =  xmlSource;
			else
				source = excelSource;
		} catch (Exception e) {
			source =  xmlSource;
		}
		System.out.println(source);
		System.out.println("Start import from Specimen data("+ source.toString() + ") ...");
		
		ICdmDataSource destination = cdmDestination;
		SpecimenImportConfigurator specimenImportConfigurator = SpecimenImportConfigurator.NewInstance(source,  destination);
		
		specimenImportConfigurator.setSourceSecId("specimen");
		specimenImportConfigurator.setCheck(check);
		specimenImportConfigurator.setDbSchemaValidation(hbm2dll);
		
		// invoke import
		CdmDefaultImport<SpecimenImportConfigurator> specimenImport = new CdmDefaultImport<SpecimenImportConfigurator>();
		//new Test().invoke(tcsImportConfigurator);
		specimenImport.invoke(specimenImportConfigurator);
		System.out.println("End import from SpecimenData ("+ source.toString() + ")...");
	}

}
