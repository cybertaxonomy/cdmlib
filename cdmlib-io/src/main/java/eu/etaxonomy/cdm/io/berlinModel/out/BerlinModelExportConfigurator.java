/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.berlinModel.out;

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

	//ID for the isHomotypic relationship in RelName, if not exist null
	private Integer isHomotypicId = null;
	
	private boolean doOccurrence = true;
	private boolean doFacts = true;
	private boolean doNameFacts = true;
	
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
				, BerlinModelClassificationExport.class
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
	
	
	public boolean isDoOccurrence() {
		return doOccurrence;
	}
	public void setDoOccurrence(boolean doOccurrence) {
		this.doOccurrence = doOccurrence;
	}
	
	public boolean isDoFacts() {
		return doFacts;
	}
	public void setDoFacts(boolean doFacts) {
		this.doFacts = doFacts;
	}

	public boolean isDoNameFacts() {
		return doNameFacts;
	}
	public void setDoNameFacts(boolean doNameFacts) {
		this.doNameFacts = doNameFacts;
	}

	

	
}
