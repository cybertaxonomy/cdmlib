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


/**
 * @author a.mueller
 * @created 11.05.2009
 */
public abstract class IoStateBase<CONFIG extends IIoConfigurator, IO extends ICdmIO> {
//	public abstract class IoStateBase<CONFIG extends IIoConfigurator, IO extends ICdmIO<IoStateBase<CONFIG, IO>>> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(IoStateBase.class);

	private IO currentIO;

	private boolean success = true;

	CONFIG config;

	/**
	 * @return the config
	 */
	public CONFIG getConfig() {
		return config;
	}

	/**
	 * @param config the config to set
	 */
	public void setConfig(CONFIG config) {
		this.config = config;
	}

	/**
	 * @param config
	 */
	//TODO config not necessary ones it it implemented in constructor for Imports too.
	public void initialize(CONFIG config){
		this.config = config;
	}

	/**
	 * @param currentImport the currentImport to set
	 */
	public void setCurrentIO(IO currentIO) {
		this.currentIO = currentIO;
	}

	/**
	 * @return the currentImport
	 */
	public IO getCurrentIO() {
		return currentIO;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public void setUnsuccessfull(){
		this.success = false;
	}

	public boolean isSuccess() {
		return success;
	}


}
