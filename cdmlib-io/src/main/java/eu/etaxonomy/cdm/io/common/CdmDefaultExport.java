/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.io.common;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.io.jaxb.JaxbExportConfigurator;
import eu.etaxonomy.cdm.io.jaxb.JaxbImportConfigurator;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

/**
 * @author a.babadshanjan
 * @created 17.11.2008
 */
public class CdmDefaultExport<T extends IExportConfigurator> implements ICdmExport<T> {
	
	private static final Logger logger = Logger.getLogger(CdmDefaultExport.class);

	public boolean invoke(IExportConfigurator config){
		if (config.getCheck().equals(IExportConfigurator.CHECK.CHECK_ONLY)){
			return doCheck(config);
		}else if (config.getCheck().equals(IExportConfigurator.CHECK.CHECK_AND_EXPORT)){
			doCheck(config);
			return doExport(config);
		}else if (config.getCheck().equals(IExportConfigurator.CHECK.EXPORT_WITHOUT_CHECK)){
			return doExport(config);
		}else{
			logger.error("Unknown CHECK type");
			return false;
		}
	}
	
	
	protected boolean doCheck(IExportConfigurator config){
		boolean result = true;
		System.out.println("Start checking Source ("+ config.getDestinationNameString() + ") ...");
		
		//check
		if (config == null){
			logger.warn("CdmExportConfiguration is null");
			return false;
		}else if (!config.isValid()){
			logger.warn("CdmExportConfiguration is not valid");
			return false;
		}
		
		//do check for each class
		for (Class<ICdmIoExport> ioClass: config.getIoClassList()){
			ICdmIoExport cdmIo = null;
			try {
				cdmIo = ioClass.newInstance();
				result &= cdmIo.check(config);
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}
		}
		
		//return
		System.out.println("End checking Source ("+ config.getDestinationNameString() + ") for Export to CDM");
		return result;

	}
	
	
	/**
	 * Executes the whole 
	 */
	protected boolean doExport(IExportConfigurator config){
		CdmApplicationController cdmApp;
		boolean result = true;
		if (config == null){
			logger.warn("Configuration is null");
			return false;
		}else if (! config.isValid()){
			logger.warn("Configuration is not valid");
			return false;
		}
//		cdmApp = config.getCdmAppController();
		// For Jaxb import, omit term loading
		if (config instanceof JaxbExportConfigurator) {
			cdmApp = config.getCdmAppController(true, true);
		} else {
			cdmApp = config.getCdmAppController(true, false);
		}
		
		System.out.println("Start Export from Source (" + config.getDestinationNameString() 
				+ ") to CDM  (" + cdmApp.getDatabaseService().getUrl() + ") ...");
		
		//do invoke for each class
		// TODO: Replace ICdmIoExport by ICdmIO
		for (Class<ICdmIoExport> ioClass: config.getIoClassList()){
			ICdmIoExport cdmIo = null;
			try {
				cdmIo = ioClass.newInstance();
				result &= cdmIo.invoke(config);
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}
		}
		
		//return
		System.out.println("End Export from Source ("+ config.getDestinationNameString() 
				+ ") to Cdm  (" + cdmApp.getDatabaseService().getUrl() + ") ...");
		return true;
	}
	

}
