/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.images;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.io.common.CdmIoBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * 
 * @author n.hoffmann
 * @created 11.11.2008
 * @version 1.0
 */
public abstract class AbstractImageImporter extends CdmIoBase<IImportConfigurator> implements ICdmIO<IImportConfigurator> {
	private static Logger logger = Logger.getLogger(AbstractImageImporter.class);
	
	protected CdmApplicationController appCtr;
	
	protected CdmApplicationController cdmApp;
	protected ITaxonService taxonService;
	protected IDescriptionService descriptionService;
	
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IImportConfigurator, java.util.Map)
	 */
	@Override
	public boolean doInvoke(IImportConfigurator config, Map<String, MapWrapper<? extends CdmBase>> stores) {
		cdmApp = config.getCdmAppController();
		
		TransactionStatus status = cdmApp.startTransaction();
		
		taxonService = cdmApp.getTaxonService();

		boolean result = invokeImageImport(config);
		
		cdmApp.commitTransaction(status);
		
		return result;
	}

	/**
	 * This method defines the image import. 
	 * It should take care of where to get the images from and what object they get attached to.
	 * 
	 * @param config
	 */
	protected abstract boolean invokeImageImport(IImportConfigurator config);


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(IImportConfigurator config) {
		boolean result = true;
		logger.warn("No check implemented for distribution data import");
		return result;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean isIgnore(IImportConfigurator config) {
		return false;
	}


	
}
