/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.berlinModel.out;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.DbExportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.ExportStateBase;
import eu.etaxonomy.cdm.io.common.IExportConfigurator;
import eu.etaxonomy.cdm.io.common.Source;

/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
public class BerlinModelExportConfigurator extends DbExportConfiguratorBase implements IExportConfigurator{
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(BerlinModelExportConfigurator.class);

	private boolean doAuthors;
	private boolean doTaxonNames;
	private BerlinModelExportState state;

	private Method relNameQualifierMethod;
	//ID for the isHomotypic relationship in RelName, if not exist null
	private Integer isHomotypicId = null;
	
	public static BerlinModelExportConfigurator NewInstance(Source berlinModelDestination, ICdmDataSource source){
			return new BerlinModelExportConfigurator(berlinModelDestination, source);
	}
	
	protected void makeIoClassList(){
		ioClassList = new Class[]{
				BerlinModelAuthorExport.class
				, BerlinModelAuthorTeamExport.class
				, BerlinModelReferenceExport.class
				, BerlinModelTaxonNameExport.class
				, BerlinModelNameRelationExport.class
//				, BerlinModelNameFactsImport.class
//				, BerlinModelTypesImport.class
				, BerlinModelTaxonExport.class
				, BerlinModelTaxonRelationExport.class
				, BerlinModelFactExport.class
//				, BerlinModelOccurrenceImport.class
		};
		
	}
	
	/**
	 * @param berlinModelSource
	 * @param sourceReference
	 * @param destination
	 */
	private BerlinModelExportConfigurator(Source destination, ICdmDataSource cdmSource) {
	   super();
	   setSource(cdmSource);
	   setDestination(destination);
//	   setState((BerlinModelExportState)getNewState());
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

//
//	/**
//	 * @return the state
//	 */
//	public BerlinModelExportState getState() {
//		return state;
//	}
//
//	/**
//	 * @param state the state to set
//	 */
//	public void setState(BerlinModelExportState state) {
//		this.state = state;
//	}

	/**
	 * @return the isHomotypicId
	 */
	public Integer getIsHomotypicId() {
		return isHomotypicId;
	}

	/**
	 * @param isHomotypicId the isHomotypicId to set
	 */
	public void setIsHomotypicId(Integer isHomotypicId) {
		this.isHomotypicId = isHomotypicId;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IExportConfigurator#getNewState()
	 */
	public ExportStateBase getNewState() {
		return new BerlinModelExportState(this);
	}
	
	

	
}
