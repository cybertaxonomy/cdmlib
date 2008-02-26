package testJar;

//import eu.etaxonomy.cdm.api.application.CdmApplicationController;
//import eu.etaxonomy.cdm.api.service.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.model.name.*;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jdom.Element;



public class TestJarMain {
	static Logger logger = Logger.getLogger(TestJarMain.class);
		
	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		TestAspect ta = new TestAspect();
//		ta.testAppController();
		test();
		testDirectory();
	}
	
	private static void testDirectory(){
		System.out.println("Start testDirectory ...");
		InputStream is2;
		File f = new File("C:/Dokumente und Einstellungen/a.mueller/.cdmLibrary/writableResources/applicationContext.xml");
		System.out.println(f.exists());
		
		try {
			String strFile = "terms/Language.csv";
			logger.info(CdmUtils.getReadableResourceStream(strFile));
			//test directory
			is2 = CdmUtils.getReadableResourceStream(CdmDataSource.DATASOURCE_FILE_NAME);
			Element el = XmlHelp.getRoot(is2);
			System.out.println(el);
			
			//CdmDataSource dataSource = CdmDataSource.NewInstance("testPostgre");
			CdmApplicationController app = new CdmApplicationController();
			logger.info(app.getNameService());
			app.close();
		} catch (Exception e) {
			logger.error("Exception: " + e.getMessage());
		}
		System.out.println("End testDirectory ...");
	}

	private static void test(){
		NonViralName tn;
		System.out.println("Start Test ...");
		Logger.getRootLogger().setLevel(Level.WARN);
		//INameService ns = new CdmApplicationController().getNameService();
		//tn = (TaxonName)ns.getAllNames().get(0);
		tn = new NonViralName(null);
		logger.setLevel(Level.DEBUG);
		System.out.println("****************");
		tn.setUninomial("TestUninomialName");
		logger.info(tn.getUninomial());
		if (tn != null){
			logger.info("Uuid for TaxonName(1): " + tn.getUuid());
		}else{
			logger.warn("No name with id = 1");
		}
		System.out.println("End Test (successful");
	}

}
