/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.tcsxml.out;

import java.io.File;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.ExportStateBase;
import eu.etaxonomy.cdm.io.common.XmlExportConfiguratorBase;

/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
public class TcsXmlExportConfigurator extends XmlExportConfiguratorBase {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(TcsXmlExportConfigurator.class);


	private TcsXmlExportState<TcsXmlExportConfigurator> state;

	
	private boolean doAuthors;
	private boolean doTaxonNames;
	
	public static TcsXmlExportConfigurator NewInstance(File destination, ICdmDataSource source){
			return new TcsXmlExportConfigurator(destination, source);
	}
	
	protected void makeIoClassList(){
		ioClassList = new Class[]{
//				BerlinModelAuthorExport.class
//				, BerlinModelAuthorTeamExport.class
//				, BerlinModelReferenceExport.class
//				, BerlinModelTaxonNameExport.class
		};
		
	}
	
	/**
	 * @param berlinModelSource
	 * @param sourceReference
	 * @param destination
	 */
	private TcsXmlExportConfigurator(File destination, ICdmDataSource cdmSource) {
	   super(destination, cdmSource);
	}

	
	public boolean isDoAuthors(){
		return doAuthors;
	}

	public void setDoAuthors(boolean doAuthors){
		this.doAuthors = doAuthors;
	}

	/**
	 * @return the doTaxonNames
	 */
	public boolean isDoTaxonNames() {
		return doTaxonNames;
	}

	/**
	 * @param doTaxonNames the doTaxonNames to set
	 */
	public void setDoTaxonNames(boolean doTaxonNames) {
		this.doTaxonNames = doTaxonNames;
	}

	/**
	 * @return the state
	 */
	public TcsXmlExportState<TcsXmlExportConfigurator> getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(TcsXmlExportState<TcsXmlExportConfigurator> state) {
		this.state = state;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IExportConfigurator#getNewState()
	 */
	public ExportStateBase getNewState() {
		return new TcsXmlExportState(this);
	}
	
	

	
}
