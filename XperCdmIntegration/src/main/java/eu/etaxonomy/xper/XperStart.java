package eu.etaxonomy.xper;

import xper2.fr_jussieu_snv_lis.Xper;
import xper2.fr_jussieu_snv_lis.edition.XPDisplay;
import xper2.fr_jussieu_snv_lis.utils.Utils;
import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;

public class XperStart {
	
	
	
	public XperStart(){
		
		DbSchemaValidation dbSchemaValidation = DbSchemaValidation.VALIDATE;
		ICdmDataSource datasource = CdmDestinations.cdm_test_local_xper();

		final CdmApplicationController appCtr = CdmApplicationController.NewInstance(datasource, dbSchemaValidation);
		
		
		Thread t = new Thread() {
			public void run() {
				new Xper(appCtr);
			}
		};
		t.start();
	}
	
	public static void xperloadDataFromCdm(){
		// create a new empty base and load data from CDM
		if(Xper.getCdmApplicationController() != null){
			Xper.getMainframe().newBase("baseTest");
			XPDisplay.getControler().getBase().deleteVariable(XPDisplay.getControler().getBase().getVariableAt(0));
			XPDisplay.displayNbVariable();
			XPDisplay.getControler().displayJifVarTree();
			AdaptaterCdmXper adaptaterCdmXper = new AdaptaterCdmXper();
			
			if (Utils.currentBase != null) {
//				adaptaterCdmXper.createWorkingSet();
				adaptaterCdmXper.loadFeatures();
				adaptaterCdmXper.loadTaxaAndDescription();
			
				XPDisplay.displayNbVariable();
				XPDisplay.getControler().displayJifVarTree();
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("cdm start");
		XperStart start = new XperStart();
		System.out.println("cdm started :::");
		
		while(!Utils.xperReady){
		}
		System.out.println("start load data");
		xperloadDataFromCdm();
		System.out.println("stop load data");
	}

}
