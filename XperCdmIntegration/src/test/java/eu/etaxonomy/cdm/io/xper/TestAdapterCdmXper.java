package eu.etaxonomy.cdm.io.xper;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.api.application.CdmIoApplicationController;
import eu.etaxonomy.cdm.model.description.WorkingSet;
import fr_jussieu_snv_lis.XPApp;
import fr_jussieu_snv_lis.utils.Utils;

public class TestAdapterCdmXper {
	private static final Logger logger = Logger.getLogger(TestAdapterCdmXper.class);
	
	private CdmXperAdapter adapterCdmXper;
	
	/**
	 * Starts CDM and Xper. Xper is started via the CdmXperAdapter.startXper().
	 */
	private boolean startApplications() {
		boolean result = false;
		
		//start CDM
		DbSchemaValidation dbSchemaValidation = DbSchemaValidation.VALIDATE;
		ICdmDataSource datasource = CdmDestinations.cdm_test_local_xper();
		System.out.println("cdm start");
		CdmApplicationController appCtr = CdmIoApplicationController.NewInstance(datasource, dbSchemaValidation);
		System.out.println("cdm started :::");
		
		List<WorkingSet> workingSets = appCtr.getWorkingSetService().list(null, 1, 0, null, null);
		if (workingSets.isEmpty()){
			logger.warn("There is no working set");
			return false;
		}else{
			UUID uuidWorkingSet =  workingSets.iterator().next().getUuid();
			
			adapterCdmXper = (CdmXperAdapter)appCtr.getBean("cdmXperAdapter");
//			adapterCdmXper = new CdmXperAdapter(appCtr, uuidWorkingSet);
			result =  adapterCdmXper.startXper(uuidWorkingSet);
		}

		return result;
		
	}
	
	public void xperLoadDataFromCdm(){
		System.out.println("start load data");
		// display a loading gif
		Utils.displayLoadingGif(true);
		
		
		// create a new empty base and load data from CDM
		if(XPApp.cdmAdapter != null){
			// create a new base
			XPApp.getMainframe().newBase("baseTest");
			// specify that the current base is not new (needed to be able to add images)
			XPApp.isNewBase = false;
			// delete the variable create by default and update the frame
			XPApp.getCurrentBase().deleteVariable(XPApp.getCurrentBase().getVariableAt(0));
			XPApp.getMainframe().displayNbVariable();
			XPApp.getMainframe().getControler().displayJifVarTree();
			
			if (XPApp.getCurrentBase() != null) {
//				adaptaterCdmXper.createWorkingSet();
				adapterCdmXper.load();

				XPApp.getMainframe().displayNbVariable();
				XPApp.getMainframe().getControler().displayJifVarTree();
			}
		}
		// undisplay a loading gif
		Utils.displayLoadingGif(false);
		System.out.println("data loaded :::");
	}

	/**
	 * 
	 */
	private void createThumbnailDirectory() {
		// create a _thumbnail directory to store thumbnails
		new File(System.getProperty("user.dir") + Utils.sep + "images" + Utils.sep + "_thumbnails").mkdirs();
	}
	
	/**
	 * 
	 */
	private void generateThumbnails() {
		System.out.println("start generate thumbnails");
		// generate all thumbnails (a loading gif is automatically displayed
		XPApp.generateThumbnailsFromURLImage(XPApp.getCurrentBase().getAllResources());
		System.out.println("stop generate thumbnails");
	}
	

	/**
	 * PartialCDM loads CDM data into Xper2 only when required.
	 * Data like images, comments, etc. are loaded on the fly.
	 * Changes to data are always persisted immediately.
	 */
	private void startPartialCdm() {
		System.out.println("start load data");
		// display a loading gif
//		Utils.displayLoadingGif(true);
		
		
		// create a new empty base and load data from CDM
		if(XPApp.cdmAdapter != null){
			// create a new base
//			XPApp.getMainframe().newBase("baseTest");
			// specify that the current base is not new (needed to be able to add images), not really needed
			XPApp.isNewBase = false;

			XPApp.getMainframe().displayNbVariable();
			XPApp.getMainframe().getControler().displayJifVarTree();
			
//			if (XPApp.getCurrentBase() != null) {
////				adaptaterCdmXper.createWorkingSet();
//				adapterCdmXper.load();
//
//				XPApp.getMainframe().displayNbVariable();
//				XPApp.getMainframe().getControler().displayJifVarTree();
//			}
		}
		// undisplay a loading gif
		Utils.displayLoadingGif(false);
		System.out.println("data loaded :::");

		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("start test adapter");
		//start CDM and Xper
		TestAdapterCdmXper testAdapter = new TestAdapterCdmXper();
		boolean success = testAdapter.startApplications();
		while(!XPApp.xperReady){
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("xper2 started :::");
		
		
//		success = false;  //as I don't understand what the following code is for I temporarily disable it it from running
		if (success){
			testAdapter.createThumbnailDirectory();
			if (args.length >= 1 && "-p".equals(args[0]) ){
				testAdapter.startPartialCdm();
			}else{
				// load the data from CDM
				testAdapter.xperLoadDataFromCdm();
				// use the current directory as working directory for Xper2
				XPApp.getCurrentBase().setPathName(System.getProperty("user.dir") + Utils.sep);
				
				testAdapter.generateThumbnails();
			}
		}else{
			System.out.println("end test adapter with errors");
			System.exit(-1);

		}
		System.out.println("end test adapter");

		
	}




}
