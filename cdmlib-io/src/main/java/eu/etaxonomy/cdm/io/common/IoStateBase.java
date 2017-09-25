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

import eu.etaxonomy.cdm.common.IoResultBase;


/**
 * @author a.mueller
 * @created 11.05.2009
 */
public abstract class IoStateBase<CONFIG extends IIoConfigurator, IO extends ICdmIO, RESULT extends IoResultBase> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(IoStateBase.class);

	private IO currentIO;

	protected CONFIG config;

	private RESULT result;

//	private IProgressMonitor currentMonitor;

	//TODO config not necessary ones it it implemented in constructor for IOs too.
	public void initialize(CONFIG config){
	    this.config = config;
	}

	public CONFIG getConfig() {
		return config;
	}
	public void setConfig(CONFIG config) {
		this.config = config;
	}

	public void setCurrentIO(IO currentIO) {
		this.currentIO = currentIO;
	}
	public IO getCurrentIO() {
		return currentIO;
	}


    public RESULT getResult() {
        return result;
    }
    public void setResult(RESULT result) {
        this.result = result;
    }


//    public IProgressMonitor getCurrentMonitor() {
//        return currentMonitor;
//    }
//    public void setCurrentMonitor(IProgressMonitor currentMonitor) {
//        this.currentMonitor = currentMonitor;
//    }



}
