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

import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * @author a.mueller
 * @created 29.01.2009
 * @version 1.0
 */
public class CdmDefaultImport<T extends IImportConfigurator> extends CdmDefaultIOBase<IImportConfigurator> implements ICdmImporter<T> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CdmDefaultImport.class);
	
	public boolean invoke(T config){
		ICdmDataSource destination = config.getDestination();
		boolean omitTermLoading = config.isOmitTermLoading();
		return invoke(config, destination, omitTermLoading);
	}

	public boolean invoke(IImportConfigurator config, ICdmDataSource destination, boolean omitTermLoading){
		boolean createNew = config.isCreateNew();
		
		if (startApplicationController(config, destination, omitTermLoading, createNew) == false){
			return false;
		}else{
			CdmApplicationAwareDefaultImport<?> defaultImport = (CdmApplicationAwareDefaultImport<?>)getCdmAppController().getBean("defaultImport");
			defaultImport.authenticate(config);
			return defaultImport.invoke(config);
		}
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
	
}
