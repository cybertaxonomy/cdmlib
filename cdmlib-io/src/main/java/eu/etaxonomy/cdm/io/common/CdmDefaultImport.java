/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;

/**
 * @author a.mueller
 * @created 29.01.2009
 * @version 1.0
 */
public class CdmDefaultImport<T extends IImportConfigurator> implements ICdmImport<T> {
	private static final Logger logger = Logger.getLogger(CdmDefaultImport.class);
	
	private CdmApplicationController cdmApp = null;
	
	public boolean invoke(T config){
		ICdmDataSource destination = config.getDestination();
		boolean omitTermLoading = false;
		return invoke(config, destination, omitTermLoading);
	}

	/**
	 * Starts the CdmApplicationController if not yet started
	 * @param config Configuration
	 * @param destination destination
	 * @return false if start not successful
	 */
	public boolean startController(IImportConfigurator config, ICdmDataSource destination){
		boolean omitTermLoading = false;
		boolean createNew = false;
		return startApplicationController(config, destination, omitTermLoading, createNew);
	}
	
	public boolean invoke(IImportConfigurator config, ICdmDataSource destination, boolean omitTermLoading){
		destination = destination;
		omitTermLoading = omitTermLoading;
		boolean createNew = false;
		
		if (startApplicationController(config, destination, omitTermLoading, createNew) == false){
			return false;
		}else{
			CdmApplicationAwareDefaultImport<?> defaultImport = (CdmApplicationAwareDefaultImport<?>)cdmApp.applicationContext.getBean("defaultImport");
			return defaultImport.invoke(config);
		}
	}

	/**
	 * Creates a new {@link CdmApplicationController} if it does not exist yet or if createNew is <ocde>true</code>
	 * @param config
	 * @param destination
	 * @param omitTermLoading
	 * @param createNew
	 * @return
	 */
	private boolean startApplicationController(IImportConfigurator config, ICdmDataSource destination, boolean omitTermLoading, boolean createNew){
		try {
			if ( createNew == true || cdmApp == null){
				cdmApp = CdmApplicationController.NewInstance(destination, config.getDbSchemaValidation(), omitTermLoading);
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
