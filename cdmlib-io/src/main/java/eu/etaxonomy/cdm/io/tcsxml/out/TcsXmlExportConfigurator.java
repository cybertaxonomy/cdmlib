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
import eu.etaxonomy.cdm.io.common.IExportConfigurator;
import eu.etaxonomy.cdm.io.common.XmlExportConfiguratorBase;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
public class TcsXmlExportConfigurator extends XmlExportConfiguratorBase implements IExportConfigurator{
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(TcsXmlExportConfigurator.class);

	private boolean doAuthors;
	private boolean doTaxonNames;
	private TcsXmlExportState<TcsXmlExportConfigurator> state;
	
	public enum IdType{
		CDM_ID,
		ORIGINAL_SOURCE_ID
	}
	
	private IdType idType = IdType.CDM_ID;
	
	public static TcsXmlExportConfigurator NewInstance(File destination, ICdmDataSource source){
			return new TcsXmlExportConfigurator(destination, source);
	}
	
	protected void makeIoClassList(){
		ioClassList = new Class[]{
//				BerlinModelAuthorExport.class
//				, BerlinModelAuthorTeamExport.class
//				, BerlinModelReferenceExport.class
//				, BerlinModelTaxonNameExport.class
//				, BerlinModelNameRelationExport.class
////				, BerlinModelNameFactsImport.class
////				, BerlinModelTypesImport.class
//				, BerlinModelTaxonExport.class
//				, BerlinModelTaxonRelationExport.class
//				, BerlinModelFactExport.class
////				, BerlinModelOccurrenceImport.class
		};
		
	}
	
	/**
	 * @param berlinModelSource
	 * @param sourceReference
	 * @param destination
	 */
	private TcsXmlExportConfigurator(File destination, ICdmDataSource cdmSource) {
	   super(destination, cdmSource);
	   setState(new TcsXmlExportState<TcsXmlExportConfigurator>());
	}
	
	
	public ICdmDataSource getSource() {
		return (ICdmDataSource)super.getSource();
	}
	public void setSource(ICdmDataSource cdmSource) {
		super.setSource(cdmSource);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#getSourceReference()
	 */
	public ReferenceBase getSourceReference() {
		if (sourceReference == null){
			sourceReference =  Database.NewInstance();
			if (getSource() != null){
				sourceReference.setTitleCache(getSource().getDatabase());
			}
		}
		return sourceReference;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getSourceNameString()
	 */
	public String getSourceNameString() {
		if (this.getSource() == null){
			return null;
		}else{
			return this.getSource().getDatabase();
		}
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IIoConfigurator#getDestinationNameString()
	 */
	public String getDestinationNameString() {
		if (getSource() != null){
			return getSource().getDatabase();
		}else{
			return null;
		}
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
	 * @return the idType
	 */
	public IdType getIdType() {
		return idType;
	}

	/**
	 * @param idType the idType to set
	 */
	public void setIdType(IdType idType) {
		this.idType = idType;
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
	
	

	
}
