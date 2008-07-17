/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import java.util.Map;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @created 01.07.2008
 * @version 1.0
 */
public abstract class CdmIoBase implements ICdmIO {
	private static Logger logger = Logger.getLogger(CdmIoBase.class);

	protected String ioName = null;

	
	/**
	 * 
	 */
	public CdmIoBase() {
		super();
		this.ioName = this.getClass().getSimpleName();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ICdmIO#check(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	public boolean check(IImportConfigurator config) {
		if (isIgnore(config)){
			logger.warn("No check for " + ioName + " (ignored)");
			return true;
		}else{
			return doCheck(config);
		}
	}
	
	protected abstract boolean doCheck(IImportConfigurator config);


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ICdmIO#invoke(eu.etaxonomy.cdm.io.common.IImportConfigurator, eu.etaxonomy.cdm.api.application.CdmApplicationController, java.util.Map)
	 */
	public boolean invoke(IImportConfigurator config,
			CdmApplicationController app, Map stores) {
		if (isIgnore(config)){
			logger.warn("No invoke for " + ioName + " (ignored)");
			return true;
		}else{
			return doInvoke(config, app, stores);
		}
	}
	
	protected abstract boolean doInvoke(IImportConfigurator config,
			CdmApplicationController app, Map<String, MapWrapper<? extends CdmBase>> stores);

	
	protected abstract boolean isIgnore(IImportConfigurator config);



}
