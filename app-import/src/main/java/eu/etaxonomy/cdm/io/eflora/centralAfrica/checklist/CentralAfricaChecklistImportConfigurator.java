/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.eflora.centralAfrica.checklist;

import java.lang.reflect.Method;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;


/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
public class CentralAfricaChecklistImportConfigurator extends ImportConfiguratorBase<CentralAfricaChecklistImportState, Source> implements IImportConfigurator{
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CentralAfricaChecklistImportConfigurator.class);

	private UUID uuidGenevaReference = UUID.fromString("cf3fd13d-6cad-430c-ab70-7ea841b7159f");
	
	private String genevaReferenceTitle = null;
	
	public static CentralAfricaChecklistImportConfigurator NewInstance(Source ermsSource, ICdmDataSource destination){
			return new CentralAfricaChecklistImportConfigurator(ermsSource, destination);
	}

	/* Max number of records to be saved with one service call */
	private int recordsPerTransaction = 1000;  //defaultValue

	//TODO needed ??
	private Method userTransformationMethod;
	
	private boolean doVernaculars = true;
	private boolean doLinks = true;
	private boolean doNotes = true;
	private boolean doImages = true;
	
	private static IInputTransformer defaultTransformer = new CentralAfricaChecklistTransformer();
	
	protected void makeIoClassList(){
		ioClassList = new Class[]{
				//ErmsGeneralImportValidator.class
				 CentralAfricaChecklistReferenceImport.class ,
				 CentralAfricaChecklistTaxonImport.class,
				 CentralAfricaChecklistSynonymImport.class
		};	
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getNewState()
	 */
	public ImportStateBase getNewState() {
		return new CentralAfricaChecklistImportState(this);
	}



	private CentralAfricaChecklistImportConfigurator(Source source, ICdmDataSource destination) {
	   super(defaultTransformer);
	   setNomenclaturalCode(NomenclaturalCode.ICZN); //default for ERMS
	   setSource(source);
	   setDestination(destination);
	}
	
	
	public Source getSource() {
		return (Source)super.getSource();
	}
	public void setSource(Source berlinModelSource) {
		super.setSource(berlinModelSource);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#getSourceReference()
	 */
	public ReferenceBase getSourceReference() {
		if (sourceReference == null){
			sourceReference =  ReferenceFactory.newDatabase();
			if (getSource() != null){
				sourceReference.setTitleCache(getSource().getDatabase(), true);
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

	/**
	 * @return the userTransformationMethod
	 */
	public Method getUserTransformationMethod() {
		return userTransformationMethod;
	}

	/**
	 * @param userTransformationMethod the userTransformationMethod to set
	 */
	public void setUserTransformationMethod(Method userTransformationMethod) {
		this.userTransformationMethod = userTransformationMethod;
	}

	
	/**
	 * @return the limitSave
	 */
	public int getRecordsPerTransaction() {
		return recordsPerTransaction;
	}

	/**
	 * @param limitSave the limitSave to set
	 */
	public void setRecordsPerTransaction(int recordsPerTransaction) {
		this.recordsPerTransaction = recordsPerTransaction;
	}

	/**
	 * @param doVernaculars the doVernaculars to set
	 */
	public void setDoVernaculars(boolean doVernaculars) {
		this.doVernaculars = doVernaculars;
	}

	/**
	 * @return the doVernaculars
	 */
	public boolean isDoVernaculars() {
		return doVernaculars;
	}



	/**
	 * @param doLinks the doLinks to set
	 */
	public void setDoLinks(boolean doLinks) {
		this.doLinks = doLinks;
	}



	/**
	 * @return the doLinks
	 */
	public boolean isDoLinks() {
		return doLinks;
	}



	/**
	 * @param doNotes the doNotes to set
	 */
	public void setDoNotes(boolean doNotes) {
		this.doNotes = doNotes;
	}



	/**
	 * @return the doNotes
	 */
	public boolean isDoNotes() {
		return doNotes;
	}



	/**
	 * @param doImages the doImages to set
	 */
	public void setDoImages(boolean doImages) {
		this.doImages = doImages;
	}



	/**
	 * @return the doImages
	 */
	public boolean isDoImages() {
		return doImages;
	}


	public void setUuidGenevaReference(UUID uuidGenevaReference) {
		this.uuidGenevaReference = uuidGenevaReference;
	}


	public UUID getUuidGenevaReference() {
		return uuidGenevaReference;
	}


	public void setGenevaReferenceTitle(String genevaReferenceTitle) {
		this.genevaReferenceTitle = genevaReferenceTitle;
	}


	public String getGenevaReferenceTitle() {
		return genevaReferenceTitle;
	}
	
	

}
