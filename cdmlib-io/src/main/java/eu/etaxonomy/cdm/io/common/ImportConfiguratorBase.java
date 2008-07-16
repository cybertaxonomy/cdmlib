/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

/**
 * @author a.mueller
 * @created 20.06.2008
 * @version 1.0
 */
public abstract class ImportConfiguratorBase /*implements IImportConfigurator*/ {
	private static final Logger logger = Logger.getLogger(ImportConfiguratorBase.class);

	//TODO
	private boolean deleteAll = false;
	
	private boolean doAuthors = true;
	//references
	private DO_REFERENCES doReferences = DO_REFERENCES.ALL;
	
	//check
	private CHECK check = CHECK.CHECK_AND_IMPORT;
	
	
	//names
	private boolean doTaxonNames = true;
	private boolean doRelNames = true;
	private boolean doNameStatus = true;
	private boolean doTypes = true;
	private boolean doNameFacts = true;
	
	//taxa
	private boolean doTaxa = true;
	private boolean doRelTaxa = true;
	private boolean doFacts = true;

	//occurrence
	private boolean doOccurrence = true;
	
	//Nomenclatural Code
	private NomenclaturalCode nomenclaturalCode = null;
	
	protected ICdmIO<IImportConfigurator>[] iCdmIoArray; 
	
	private MapWrapper<Feature> featureMap = new MapWrapper<Feature>(null);
	
	//uuid of concept reference
	private UUID  secUuid = UUID.randomUUID();
	private int sourceSecId = -1;
	
	private Object source;
	protected ReferenceBase sourceReference;
	private ICdmDataSource destination;
	private Person commentator =  Person.NewTitledInstance("automatic BerlinModel2CDM importer");
	
	private Language factLanguage = Language.ENGLISH();
	private DbSchemaValidation dbSchemaValidation = DbSchemaValidation.VALIDATE;
	private CdmApplicationController cdmApp = null;
	
/* *****************CONSTRUCTOR *****************************/
	
	public ImportConfiguratorBase(){
		super();
		makeIOs();
	}
	
	abstract protected void makeIOs();
	
	/**
	 * @param source the source to set
	 */
	protected void setSource(Object source) {
		this.source = source;
	}
	
	
	/**
	 * @param source the source to get
	 */
	protected Object getSource() {
		return source;
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#isValid()
	 */
	public boolean isValid(){
		boolean result = true;
		if (source == null){
			logger.warn("Connection to BerlinModel could not be established");
			result = false;
		}
		if (destination == null){
			logger.warn("Connection to Cdm could not be established");
			result = false;
		}
		
		return result;
	}
	
	
	
/* ****************** GETTER/SETTER **************************/	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#isDeleteAll()
	 */
	public boolean isDeleteAll() {
		return deleteAll;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#setDeleteAll(boolean)
	 */
	public void setDeleteAll(boolean deleteAll) {
		this.deleteAll = deleteAll;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#isDoAuthors()
	 */
	public boolean isDoAuthors() {
		return doAuthors;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#setDoAuthors(boolean)
	 */
	public void setDoAuthors(boolean doAuthors) {
		this.doAuthors = doAuthors;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#getDoReferences()
	 */
	public DO_REFERENCES getDoReferences() {
		return doReferences;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#setDoReferences(eu.etaxonomy.cdm.io.tcs.TcsImportConfigurator.DO_REFERENCES)
	 */
	public void setDoReferences(DO_REFERENCES doReferences) {
		this.doReferences = doReferences;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#getCheck()
	 */
	public CHECK getCheck() {
		return this.check;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#setCheck(eu.etaxonomy.cdm.io.tcs.TcsImportConfigurator.CHECK)
	 */
	public void setCheck(CHECK check) {
		this.check = check;
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#isDoTaxonNames()
	 */
	public boolean isDoTaxonNames() {
		return doTaxonNames;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#setDoTaxonNames(boolean)
	 */
	public void setDoTaxonNames(boolean doTaxonNames) {
		this.doTaxonNames = doTaxonNames;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#isDoRelNames()
	 */
	public boolean isDoRelNames() {
		return doRelNames;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#setDoRelNames(boolean)
	 */
	public void setDoRelNames(boolean doRelNames) {
		this.doRelNames = doRelNames;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#isDoNameStatus()
	 */
	public boolean isDoNameStatus() {
		return doNameStatus;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#setDoNameStatus(boolean)
	 */
	public void setDoNameStatus(boolean doNameStatus) {
		this.doNameStatus = doNameStatus;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#isDoNameFacts()
	 */
	public boolean isDoNameFacts() {
		return doNameFacts;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#setDoNameFacts(boolean)
	 */
	public void setDoNameFacts(boolean doNameFacts) {
		this.doNameFacts = doNameFacts;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#isDoTypes()
	 */
	public boolean isDoTypes() {
		return doTypes;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#setDoTypes(boolean)
	 */
	public void setDoTypes(boolean doTypes) {
		this.doTypes = doTypes;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#isDoTaxa()
	 */
	public boolean isDoTaxa() {
		return doTaxa;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#setDoTaxa(boolean)
	 */
	public void setDoTaxa(boolean doTaxa) {
		this.doTaxa = doTaxa;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#isDoRelTaxa()
	 */
	public boolean isDoRelTaxa() {
		return doRelTaxa;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#setDoRelTaxa(boolean)
	 */
	public void setDoRelTaxa(boolean doRelTaxa) {
		this.doRelTaxa = doRelTaxa;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#isDoFacts()
	 */
	public boolean isDoFacts() {
		return doFacts;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#setDoFacts(boolean)
	 */
	public void setDoFacts(boolean doFacts) {
		this.doFacts = doFacts;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#isDoOccurrence()
	 */
	public boolean isDoOccurrence() {
		return doOccurrence;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#setDoOccurrence(boolean)
	 */
	public void setDoOccurrence(boolean doOccurrence) {
		this.doOccurrence = doOccurrence;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#getDestination()
	 */
	public ICdmDataSource getDestination() {
		return destination;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#setDestination(eu.etaxonomy.cdm.database.ICdmDataSource)
	 */
	public void setDestination(ICdmDataSource destination) {
		this.destination = destination;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#getDbSchemaValidation()
	 */
	public DbSchemaValidation getDbSchemaValidation() {
		return dbSchemaValidation;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#setDbSchemaValidation(eu.etaxonomy.cdm.database.DbSchemaValidation)
	 */
	public void setDbSchemaValidation(DbSchemaValidation dbSchemaValidation) {
		this.dbSchemaValidation = dbSchemaValidation;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#getSourceReference()
	 */
	public abstract ReferenceBase getSourceReference();
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#setSourceReference(eu.etaxonomy.cdm.model.reference.ReferenceBase)
	 */
	public void setSourceReference(ReferenceBase sourceReference) {
		this.sourceReference = sourceReference;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#getSourceReferenceTitle()
	 */
	public String getSourceReferenceTitle() {
		return getSourceReference().getTitleCache();
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#setSourceReferenceTitle(java.lang.String)
	 */
	public void setSourceReferenceTitle(String sourceReferenceTitle) {
		getSourceReference().setTitleCache(sourceReferenceTitle);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#getCommentator()
	 */
	public Person getCommentator() {
		return commentator;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#setCommentator(eu.etaxonomy.cdm.model.agent.Person)
	 */
	public void setCommentator(Person commentator) {
		this.commentator = commentator;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#getFactLanguage()
	 */
	public Language getFactLanguage() {
		return factLanguage;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#setFactLanguage(eu.etaxonomy.cdm.model.common.Language)
	 */
	public void setFactLanguage(Language factLanguage) {
		this.factLanguage = factLanguage;
	}


	/**
	 * @return the nomenclaturalCode
	 */
	public NomenclaturalCode getNomenclaturalCode() {
		return nomenclaturalCode;
	}


	/**
	 * @param nomenclaturalCode the nomenclaturalCode to set
	 */
	public void setNomenclaturalCode(NomenclaturalCode nomenclaturalCode) {
		this.nomenclaturalCode = nomenclaturalCode;
	}


	/**
	 * @return the secUuid
	 */
	public UUID getSecUuid() {
		return secUuid;
	}


	/**
	 * @param secUuid the secUuid to set
	 */
	public void setSecUuid(UUID secUuid) {
		this.secUuid = secUuid;
	}

	/**
	 * @return the sourceSecId
	 */
	public int getSourceSecId() {
		return sourceSecId;
	}

	/**
	 * @param sourceSecId the sourceSecId to set
	 */
	public void setSourceSecId(int sourceSecId) {
		this.sourceSecId = sourceSecId;
	}

	/**
	 * @return the iioArray
	 */
	public ICdmIO<IImportConfigurator>[] getICdmIo() {
		return iCdmIoArray;
	}

	/**
	 * @return the featureMap
	 */
	public MapWrapper<Feature> getFeatureMap() {
		return featureMap;
	}

	/**
	 * @param featureMap the featureMap to set
	 */
	public void setFeatureMap(MapWrapper<Feature> featureMap) {
		this.featureMap = featureMap;
	}

	
	/**
	 * Returns a <code>CdmApplicationController</code> created by the values of this configuration.
	 * If a controller was already created before the last created controller is returned.
	 * @return
	 */
	public CdmApplicationController getCdmAppController(){
		return getCdmAppController(false);
	}
	
	/**
	 * Returns a new instance of <code>CdmApplicationController</code> created by the values of this configuration.
	 * @return
	 */
	public CdmApplicationController getNewCdmAppController(){
		return getCdmAppController(true);
	}
	
	/**
	 * Returns a <code>CdmApplicationController</code> created by the values of this configuration.
	 * If create new is true always a new controller is returned, else the last created controller is returned. If no controller has
	 * been created before a new controller is returned.
	 * @return
	 */
	public CdmApplicationController getCdmAppController(boolean createNew){
		if (cdmApp == null || createNew == true){
			try {
				cdmApp = CdmApplicationController.NewInstance(this.getDestination(), this.getDbSchemaValidation());
			} catch (DataSourceNotFoundException e) {
				logger.error("could not connect to destination database");
				return null;
			}catch (TermNotFoundException e) {
				logger.error("could not find needed term in destination datasource");
				return null;
			}
		}
		return cdmApp;
	}

}
