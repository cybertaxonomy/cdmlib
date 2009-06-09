/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import java.io.File;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
public abstract class XmlExportConfiguratorBase extends ExportConfiguratorBase<File> implements IExportConfigurator{
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(XmlExportConfiguratorBase.class);

//	private XmlExportState<XmlExportConfigurator> state;
	
	
	/**
	 * @param berlinModelSource
	 * @param sourceReference
	 * @param destination
	 */
	protected XmlExportConfiguratorBase(File destination, ICdmDataSource cdmSource) {
	   super();
	   setSource(cdmSource);
	   setDestination(destination);
//	   setState(new XmlExportState<XmlExportConfigurator>());
	}
	
	

//	/**
//	 * @return the state
//	 */
//	public XmlExportState<XmlExportConfigurator> getState() {
//		return state;
//	}
//
//	/**
//	 * @param state the state to set
//	 */
//	public void setState(BerlinModelExportState<XmlExportConfigurator> state) {
//		this.state = state;
//	}
//	
	

	
}
