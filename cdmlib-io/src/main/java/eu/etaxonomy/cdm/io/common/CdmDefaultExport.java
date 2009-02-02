/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.io.common;

import java.util.Map;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;

/**
 * @author a.babadshanjan
 * @created 17.11.2008
 */
public class CdmDefaultExport<T extends IExportConfigurator> implements ICdmExport<T> {
	
	private static final Logger logger = Logger.getLogger(CdmDefaultExport.class);

	private CdmApplicationController cdmApp = null;
	
	public boolean invoke(T config){
		ICdmDataSource source = config.getSource();
		return invoke(config, source);
	}

	
	public boolean invoke(IExportConfigurator config, ICdmDataSource source){
		source = source;
		boolean omitTermLoading = false;
		boolean createNew = false;
		
		if (startApplicationController(config, source, omitTermLoading, createNew) == false){
			return false;
		}else{
			CdmApplicationAwareDefaultExport<?> defaultExport = 
				(CdmApplicationAwareDefaultExport<?>)cdmApp.applicationContext.getBean("defaultExport");
			return defaultExport.invoke(config);
		}
	}
	
	/**
	 * Executes the whole 
	 */
	protected boolean doExport(IExportConfigurator config){
		Map stores = null;  //TODO 
		boolean result = true;
		if (config == null){
			logger.warn("Configuration is null");
			return false;
		}else if (! config.isValid()){
			logger.warn("Configuration is not valid");
			return false;
		}

		
		System.out.println("Start export from Cdm to Destination (" + config.getDestinationNameString() + ") ...");
				
		//do invoke for each class
		for (Class<ICdmIO> ioClass: config.getIoClassList()){
			ICdmIO cdmIo = null;
			try {
				cdmIo = ioClass.newInstance();
				result &= cdmIo.invoke(config, stores);
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}
		}
		
		//return
		System.out.println("End export from Cdm to Destination (" + config.getDestinationNameString() + ") ...");
		return true;
	}
	

	/**
	 * Creates a new {@link CdmApplicationController} if it does not exist yet or if createNew is <ocde>true</code>
	 * @param config
	 * @param destination
	 * @param omitTermLoading
	 * @param createNew
	 * @return
	 */
	private boolean startApplicationController(IExportConfigurator config, ICdmDataSource source, boolean omitTermLoading, boolean createNew){
		try {
			if ( createNew == true || cdmApp == null){
				cdmApp = CdmApplicationController.NewInstance(source, DbSchemaValidation.VALIDATE);
				if (cdmApp != null){
					return true;
				}else{
					return false;
				}
			}
			return true;
		} catch (DataSourceNotFoundException  e) {
			logger.error("could not connect to destination database");
			return false;
		}catch (TermNotFoundException e) {
			logger.error("could not find needed term in destination datasource");
			return false;
		}
	}
	
	
	/**
	 * Returns the {@link CdmApplicationController}. This is null if invoke() has not been called yet and if the controller
	 * has not been set manually by setCdmApp() yet. 
	 * @return the cdmApp
	 */
	public CdmApplicationController getCdmApp() {
		return cdmApp;
	}


	/**
	 * @param cdmApp the cdmApp to set
	 */
	public void setCdmApp(CdmApplicationController cdmApp) {
		this.cdmApp = cdmApp;
	}
	

}
