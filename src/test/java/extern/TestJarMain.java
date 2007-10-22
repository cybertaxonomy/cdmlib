package extern;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.NameServiceImpl;
import eu.etaxonomy.cdm.model.name.TaxonName;


public class TestJarMain {
	static Logger logger = Logger.getLogger(TestJarMain.class);
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Start Test");
		//TODO
		CdmApplicationController app = new CdmApplicationController();
		INameService ns = app.getNameService();
		TaxonName tn = ns.getTaxonNameById(1);
		logger.setLevel(Level.INFO);
		if (tn != null){
			logger.info("Uuid for TaxonName(1): " + tn.getUuid());
		}else{
			logger.warn("No name with id = 1");
		}
		System.out.println("End Success");
		
	}

}
