/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.io.jaxb.JaxbImportConfigurator;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.mueller
 * @created 20.06.2008
 * @version 1.0
 */

public class CdmDefaultImport<T extends IImportConfigurator> implements ICdmImport<T> {
	private static final Logger logger = Logger.getLogger(CdmDefaultImport.class);
	
	//Constants
	final boolean OBLIGATORY = true; 
	final boolean FACULTATIVE = false; 
	final int modCount = 1000;

	IService service = null;
	
	Map<String, MapWrapper<? extends CdmBase>> stores = new HashMap<String, MapWrapper<? extends CdmBase>>();

	public CdmDefaultImport(){
		stores.put(ICdmIO.AUTHOR_STORE, new MapWrapper<TeamOrPersonBase>(service));
		stores.put(ICdmIO.REFERENCE_STORE, new MapWrapper<ReferenceBase>(service));
		stores.put(ICdmIO.NOMREF_STORE, new MapWrapper<ReferenceBase>(service));
		stores.put(ICdmIO.NOMREF_DETAIL_STORE, new MapWrapper<ReferenceBase>(service));
		stores.put(ICdmIO.REF_DETAIL_STORE, new MapWrapper<ReferenceBase>(service));
		stores.put(ICdmIO.TAXONNAME_STORE, new MapWrapper<TaxonNameBase>(service));
		stores.put(ICdmIO.TAXON_STORE, new MapWrapper<TaxonBase>(service));
		stores.put(ICdmIO.SPECIMEN_STORE, new MapWrapper<Specimen>(service));
	}
	
	public boolean invoke(IImportConfigurator config){
		if (config.getCheck().equals(IImportConfigurator.CHECK.CHECK_ONLY)){
			return doCheck(config);
		}else if (config.getCheck().equals(IImportConfigurator.CHECK.CHECK_AND_IMPORT)){
			doCheck(config);
			return doImport(config);
		}else if (config.getCheck().equals(IImportConfigurator.CHECK.IMPORT_WITHOUT_CHECK)){
			return doImport(config);
		}else{
			logger.error("Unknown CHECK type");
			return false;
		}
	}
	
	
	protected boolean doCheck(IImportConfigurator config){
		boolean result = true;
		System.out.println("Start checking Source ("+ config.getSourceNameString() + ") ...");
		
		//check
		if (config == null){
			logger.warn("CdmImportConfiguration is null");
			return false;
		}else if (! config.isValid()){
			logger.warn("CdmImportConfiguration is not valid");
			return false;
		}
		
		//do check for each class
		for (Class<ICdmIO> ioClass: config.getIoClassList()){
			ICdmIO cdmIo = null;
			try {
				cdmIo = ioClass.newInstance();
				result &= cdmIo.check(config);
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}
		}
		
		//return
		System.out.println("End checking Source ("+ config.getSourceNameString() + ") for import to Cdm");
		return result;

	}
	
	
	/**
	 * Executes the whole 
	 */
	protected boolean doImport(IImportConfigurator config){
		CdmApplicationController cdmApp;
		boolean result = true;
		if (config == null){
			logger.warn("Configuration is null");
			return false;
		}else if (! config.isValid()){
			logger.warn("Configuration is not valid");
			return false;
		}
		// For Jaxb import, omit term loading
		if (config instanceof JaxbImportConfigurator) {
			cdmApp = config.getCdmAppController(false, true);
		} else {
			cdmApp = config.getCdmAppController();
		}
		
		ReferenceBase sourceReference = config.getSourceReference();
		
		System.out.println("Start import from Source ("+ config.getSourceNameString() + ") to Cdm (" 
				+ cdmApp.getDatabaseService().getUrl() + ") ...");
		
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
		
		System.out.println("End import from Source ("+ config.getSourceNameString() + ") to Cdm (" 
				+ cdmApp.getDatabaseService().getUrl() + ") ...");
		return true;
	}
	

	
}
