/**
 * Copyright (C) 2008 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 */

package eu.etaxonomy.cdm.io.jaxb;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.excel.distribution.DistributionImportConfigurator;
import eu.etaxonomy.cdm.io.excel.distribution.DistributionImporter;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

/**
 * @author a.babadshanjan
 * @created 14.11.2008
 */
public class JaxbImportConfigurator extends ImportConfiguratorBase implements IImportConfigurator {

	private static final Logger logger = Logger.getLogger(JaxbImportConfigurator.class);

	private int maxRows = 0;

//	private boolean doAgents = true;
	private boolean doAgentData = true;
	private boolean doLanguageData = true;
	private boolean doFeatureData = true;
	private boolean doDescriptions = true;
	private boolean doMedia = true;
//	private boolean doOccurrences = true;
//	private boolean doReferences = true;
	private boolean doReferencedEntities = true;
//	private boolean doRelationships = true;
	private boolean doSynonyms = true;
//	private boolean doTaxonNames = true;
//	private boolean doTaxa = true;
	private boolean doTerms = true;
	private boolean doTermVocabularies = true;
	private boolean doHomotypicalGroups = true;

//	private Object source;
//	private ICdmDataSource cdmDb;
//	private ICdmDataSource cdmSource;
//	private ICdmDataSource cdmDestination;
//	private DbSchemaValidation cdmSourceSchemaValidation = DbSchemaValidation.VALIDATE;
//	private DbSchemaValidation cdmDestSchemaValidation = DbSchemaValidation.CREATE;
//	private CdmApplicationController cdmApp = null;


	/**
	 * Returns a <code>CdmApplicationController</code> created by the values of this configuration.
	 * If create new is true always a new controller is returned, else the last created controller is returned. If no controller has
	 * been created before a new controller is returned.
	 * @return
	 */
//	public CdmApplicationController getSourceAppController(ICdmDataSource cdmDb, boolean createNew){
//		if (cdmApp == null || createNew == true){
//			try {
//				cdmApp = CdmApplicationController.NewInstance(this.getCdmSource(), this.getCdmSourceSchemaValidation(), true);
//			} catch (DataSourceNotFoundException e) {
//				logger.error("Could not connect to source database");
//				return null;
//			}catch (TermNotFoundException e) {
//				logger.error("Terms not found in source database. " +
//				"This error should not happen since preloaded terms are not expected for this application.");
//				return null;
//			}
//		}
//		return cdmApp;
//	}

	/**
	 * Returns a <code>CdmApplicationController</code> created by the values of this configuration.
	 * If create new is true always a new controller is returned, else the last created controller is returned. If no controller has
	 * been created before a new controller is returned.
	 * @return
	 */
//	public CdmApplicationController getDestinationAppController(ICdmDataSource cdmDb, boolean createNew){
//		if (cdmApp == null || createNew == true){
//			try {
//				cdmApp = CdmApplicationController.NewInstance(this.getCdmDestination(), this.getCdmDestSchemaValidation(), true);
//			} catch (DataSourceNotFoundException e) {
//				logger.error("Could not connect to destination database");
//				return null;
//			}catch (TermNotFoundException e) {
//				logger.error("Terms not found in destination database. " +
//				"This error should not happen since preloaded terms are not expected for this application.");
//				return null;
//			}
//		}
//		return cdmApp;
//	}

	public int getMaxRows() {
		return maxRows;
	}

	public void setMaxRows(int maxRows) {
		this.maxRows = maxRows;
	}

//	public ICdmDataSource getCdmSource() {
//		return cdmSource;
//	}
//
//	public void setCdmSource(ICdmDataSource cdmSource) {
//		this.cdmSource = cdmSource;
//	}

//	public ICdmDataSource getCdmDestination() {
//		return super.getDestination();
//	}
//
//	public void setCdmDestination(ICdmDataSource cdmDestination) {
//		super.setDestination(cdmDestination);
//	}

//	public DbSchemaValidation getCdmSourceSchemaValidation() {
//		return cdmSourceSchemaValidation;
//	}
//
//	public void setCdmSourceSchemaValidation(DbSchemaValidation cdmSchemaValidation) {
//		this.cdmSourceSchemaValidation = cdmSchemaValidation;
//	}

//	public boolean isDoAgents() {
//		return doAgents;
//	}
//
//	public void setDoAgents(boolean doAgents) {
//		this.doAgents = doAgents;
//	}

	public boolean isDoAgentData() {
		return doAgentData;
	}

	public void setDoAgentData(boolean doAgentData) {
		this.doAgentData = doAgentData;
	}

	public boolean isDoLanguageData() {
		return doLanguageData;
	}

	public void setDoLanguageData(boolean doLanguageData) {
		this.doLanguageData = doLanguageData;
	}

	public boolean isDoFeatureData() {
		return doFeatureData;
	}

	public void setDoFeatureData(boolean doFeatureData) {
		this.doFeatureData = doFeatureData;
	}

	public boolean isDoDescriptions() {
		return doDescriptions;
	}

	public void setDoDescriptions(boolean doDescriptions) {
		this.doDescriptions = doDescriptions;
	}

	public boolean isDoMedia() {
		return doMedia;
	}

	public void setDoMedia(boolean doMedia) {
		this.doMedia = doMedia;
	}

//	public boolean isDoOccurrences() {
//		return doOccurrences;
//	}
//
//	public void setDoOccurrences(boolean doOccurrences) {
//		this.doOccurrences = doOccurrences;
//	}

//	public boolean isDoReferences() {
//		return doReferences;
//	}
//
//	public void setDoReferences(boolean doReferences) {
//		this.doReferences = doReferences;
//	}

	public boolean isDoReferencedEntities() {
		return doReferencedEntities;
	}

	public void setDoReferencedEntities(boolean doReferencedEntities) {
		this.doReferencedEntities = doReferencedEntities;
	}

//	public boolean isDoRelationships() {
//		return doRelationships;
//	}
//
//	public void setDoRelationships(boolean doRelationships) {
//		this.doRelationships = doRelationships;
//	}

	public boolean isDoSynonyms() {
		return doSynonyms;
	}

	public void setDoSynonyms(boolean doSynonyms) {
		this.doSynonyms = doSynonyms;
	}

//	public boolean isDoTaxonNames() {
//		return doTaxonNames;
//	}
//
//	public void setDoTaxonNames(boolean doTaxonNames) {
//		this.doTaxonNames = doTaxonNames;
//	}

//	public boolean isDoTaxa() {
//		return doTaxa;
//	}
//
//	public void setDoTaxa(boolean doTaxa) {
//		this.doTaxa = doTaxa;
//	}

	public boolean isDoTerms() {
		return doTerms;
	}

	public void setDoTerms(boolean doTerms) {
		this.doTerms = doTerms;
	}

	public boolean isDoTermVocabularies() {
		return doTermVocabularies;
	}

	public void setDoTermVocabularies(boolean doTermVocabularies) {
		this.doTermVocabularies = doTermVocabularies;
	}

	public boolean isDoHomotypicalGroups() {
		return doHomotypicalGroups;
	}

	public void setDoHomotypicalGroups(boolean doHomotypicalGroups) {
		this.doHomotypicalGroups = doHomotypicalGroups;
	}

//	@SuppressWarnings("unchecked")
	protected void makeIoClassList() {
		ioClassList = new Class[] {
				CdmImporter.class,
		};
	};

	public static JaxbImportConfigurator NewInstance(String url,
			ICdmDataSource destination){
		return new JaxbImportConfigurator(url, destination);
	}


	/**
	 * @param url
	 * @param destination
	 */
	private JaxbImportConfigurator(String url, ICdmDataSource destination) {
		super();
		setSource(url);
		setDestination(destination);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSource()
	 */
	public String getSource() {
		return (String)super.getSource();
	}


	/**
	 * @param file
	 */
	public void setSource(String fileName) {
		super.setSource(fileName);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSourceReference()
	 */
	@Override
	public ReferenceBase getSourceReference() {
		//TODO
		if (this.sourceReference == null){
			logger.warn("getSource Reference not yet fully implemented");
			sourceReference = Database.NewInstance();
			sourceReference.setTitleCache("Jaxb import");
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
			return this.getSource();
		}
	}

}
