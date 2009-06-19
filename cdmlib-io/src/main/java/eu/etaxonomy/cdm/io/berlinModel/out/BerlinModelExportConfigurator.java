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
	private BerlinModelExportState<BerlinModelExportConfigurator> state;

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
//	   setNomenclaturalCode(NomenclaturalCode.ICBN); //default for Berlin Model
	   setSource(cdmSource);
	   setDestination(destination);
	   setState(new BerlinModelExportState<BerlinModelExportConfigurator>());
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
	public BerlinModelExportState<BerlinModelExportConfigurator> getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(BerlinModelExportState<BerlinModelExportConfigurator> state) {
		this.state = state;
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
	
	
	
//
//	/**
//	 * @return the relNameQualifierMethod
//	 */
//	public Method getRelNameQualifierMethod() {
//		return relNameQualifierMethod;
//	}
//
//	/**
//	 * @param relNameQualifierMethod the relNameQualifierMethod to set
//	 */
//	public void setRelNameQualifierMethod(Method relNameQualifierMethod) {
//		this.relNameQualifierMethod = relNameQualifierMethod;
//	}
//	
	

	
}
