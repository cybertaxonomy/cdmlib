package extern;

import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.NameServiceImpl;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;


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
		List<TaxonNameBase> tnl = ns.getAllNames(1,1);
		logger.setLevel(Level.INFO);
		if (tnl.isEmpty()){
			logger.warn("No name exists");
		}else{
			logger.info("Uuid for 1st TaxonName: " + tnl.get(0).getUuid());
		}
		System.out.println("End Success");
	}

}
