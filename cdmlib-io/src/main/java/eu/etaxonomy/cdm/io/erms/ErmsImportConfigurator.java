/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.erms;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.erms.validation.ErmsGeneralImportValidator;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;


/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
public class ErmsImportConfigurator extends ImportConfiguratorBase<ErmsImportState> implements IImportConfigurator{
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ErmsImportConfigurator.class);

	public static ErmsImportConfigurator NewInstance(Source ermsSource, ICdmDataSource destination){
			return new ErmsImportConfigurator(ermsSource, destination);
	}

	/* Max number of records to be saved with one service call */
	private int recordsPerTransaction = 1000;  //defaultValue

	private Method namerelationshipTypeMethod;
	private Method uuidForDefTermMethod;
	private Method userTransformationMethod;
	private Method nameTypeDesignationStatusMethod;
	
	private boolean doVernaculars = true;
	private boolean doLinks = true;
	private boolean doNotes = true;
	private boolean doImages = true;
	
	
	// NameFact stuff
	private int maximumNumberOfNameFacts;
	private boolean isIgnore0AuthorTeam = false;
	
	protected void makeIoClassList(){
		ioClassList = new Class[]{
				ErmsGeneralImportValidator.class
				, ErmsImportRankMap.class
				, ErmsReferenceImport.class
				, ErmsTaxonImport.class
				, ErmsVernacularImport.class
				, ErmsLinkImport.class
				, ErmsImageImport.class
				, ErmsNotesImport.class
//				, ErmsAreaImport.class
//				, ErmsDrImport.class
				, ErmsSourceUsesImport.class
				, ErmsTaxonRelationImport.class
		};	
	}
	
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getNewState()
	 */
	public ImportStateBase getNewState() {
		return new ErmsImportState(this);
	}



	/**
	 * @param berlinModelSource
	 * @param sourceReference
	 * @param destination
	 */
	private ErmsImportConfigurator(Source ermsSource, ICdmDataSource destination) {
	   super();
	   setNomenclaturalCode(NomenclaturalCode.ICZN); //default for ERMS
	   setSource(ermsSource);
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
		ReferenceFactory refFactory = ReferenceFactory.newInstance();
		if (sourceReference == null){
			sourceReference =  refFactory.newDatabase();
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


	/**
	 * @return the maximumNumberOfNameFacts
	 */
	public int getMaximumNumberOfNameFacts() {
		return maximumNumberOfNameFacts;
	}

	/**
	 * set to 0 for unlimited
	 * 
	 * @param maximumNumberOfNameFacts the maximumNumberOfNameFacts to set
	 */
	public void setMaximumNumberOfNameFacts(int maximumNumberOfNameFacts) {
		this.maximumNumberOfNameFacts = maximumNumberOfNameFacts;
	}

	/**
	 * If true, an authorTeam with authorTeamId = 0 is not imported (casus Salvador)
	 * @return the isIgnore0AuthorTeam
	 */
	public boolean isIgnore0AuthorTeam() {
		return isIgnore0AuthorTeam;
	}

	/**
	 * @param isIgnore0AuthorTeam the isIgnore0AuthorTeam to set
	 */
	public void setIgnore0AuthorTeam(boolean isIgnore0AuthorTeam) {
		this.isIgnore0AuthorTeam = isIgnore0AuthorTeam;
	}

	/**
	 * @return the namerelationshipTypeMethod
	 */
	public Method getNamerelationshipTypeMethod() {
		return namerelationshipTypeMethod;
	}

	/**
	 * @param namerelationshipTypeMethod the namerelationshipTypeMethod to set
	 */
	public void setNamerelationshipTypeMethod(Method namerelationshipTypeMethod) {
		this.namerelationshipTypeMethod = namerelationshipTypeMethod;
	}
	

	/**
	 * @return the uuidForDefTermMethod
	 */
	public Method getUuidForDefTermMethod() {
		return uuidForDefTermMethod;
	}

	/**
	 * @param uuidForDefTermMethod the uuidForDefTermMethod to set
	 */
	public void setUuidForDefTermMethod(Method uuidForDefTermMethod) {
		this.uuidForDefTermMethod = uuidForDefTermMethod;
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
	 * @return the nameTypeDesignationStatusMethod
	 */
	public Method getNameTypeDesignationStatusMethod() {
		return nameTypeDesignationStatusMethod;
	}


	/**
	 * @param nameTypeDesignationStatusMethod the nameTypeDesignationStatusMethod to set
	 */
	public void setNameTypeDesignationStatusMethod(
			Method nameTypeDesignationStatusMethod) {
		this.nameTypeDesignationStatusMethod = nameTypeDesignationStatusMethod;
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

}
