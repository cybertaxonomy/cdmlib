/**
 * Copyright (C) 2008 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.jaxb;

import java.net.URI;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.babadshanjan
 * @since 14.11.2008
 */
public class JaxbImportConfigurator extends ImportConfiguratorBase<JaxbImportState, URI> implements IImportConfigurator {

	private static final Logger logger = Logger.getLogger(JaxbImportConfigurator.class);
		
	private int maxRows = 0;
	
	//TODO
	private static IInputTransformer defaultTransformer = null;

	
	private boolean doUser = true;
	private boolean doAgentData = true;
	private boolean doLanguageData = true;
	private boolean doFeatureData = true;
	private boolean doDescriptions = true;
	private boolean doMedia = true;
	private boolean doOccurrence = true;
	private boolean doReferencedEntities = true;
	private boolean doSynonyms = true;
	private boolean doTerms = true;
	private boolean doTermVocabularies = true;
	private boolean doHomotypicalGroups = true;

	private boolean doTypeDesignations = true;
	private boolean doClassificationData = true;
	private boolean doAuthors = true;
	//references
	private DO_REFERENCES doReferences = DO_REFERENCES.ALL;
	//names
	private boolean doTaxonNames = true;
	//taxa
	private boolean doTaxa = true;

	@Override
	public JaxbImportState getNewState() {
		return new JaxbImportState(this);
	}

	public int getMaxRows() {
		return maxRows;
	}

	public void setMaxRows(int maxRows) {
		this.maxRows = maxRows;
	}
	
	
	
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


	public boolean isDoReferencedEntities() {
		return doReferencedEntities;
	}

	public void setDoReferencedEntities(boolean doReferencedEntities) {
		this.doReferencedEntities = doReferencedEntities;
	}


	public boolean isDoSynonyms() {
		return doSynonyms;
	}

	public void setDoSynonyms(boolean doSynonyms) {
		this.doSynonyms = doSynonyms;
	}


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
				JaxbImport.class,
		};
	};

	public static JaxbImportConfigurator NewInstance(URI uri, ICdmDataSource destination){
		return new JaxbImportConfigurator(uri, destination);
	}


	/**
	 * @param url
	 * @param destination
	 */
	private JaxbImportConfigurator(URI uri, ICdmDataSource destination) {
		super(defaultTransformer);
		setSource(uri);
		setDestination(destination);
		setDbSchemaValidation(DbSchemaValidation.CREATE);
	}

	@Override
	public Reference getSourceReference() {
		//TODO
		if (this.sourceReference == null){
			logger.warn("getSource Reference not yet fully implemented");
			sourceReference = ReferenceFactory.newDatabase();
			sourceReference.setTitleCache("Jaxb import", true);
		}
		return sourceReference;
	}


	@Override
	public String getSourceNameString() {
		if (this.getSource() == null){
			return null;
		}else{
			return this.getSource().toString();
		}
	}
	
//******************** GETTER / SETTER ***************************************/	
	
	public boolean isDoTypeDesignations() {
		return doTypeDesignations;
	}

	public void setDoTypeDesignations(boolean doTypeDesignations) {
		this.doTypeDesignations = doTypeDesignations;
	}

	public boolean isDoClassificationData() {
		return this.doClassificationData;
	}
	
	
	public boolean isDoOccurrence() {
		return doOccurrence;
	}
	public void setDoOccurrence(boolean doOccurrence) {
		this.doOccurrence = doOccurrence;
	}
	

	public boolean isDoUser() {
		return doUser;
	}

	public void setDoUser(boolean doUser) {
		this.doUser = doUser;
	}
	

	public boolean isDoAuthors() {
		return doAuthors;
	}
	public void setDoAuthors(boolean doAuthors) {
		this.doAuthors = doAuthors;
	}

	public DO_REFERENCES getDoReferences() {
		return doReferences;
	}
	public void setDoReferences(DO_REFERENCES doReferences) {
		this.doReferences = doReferences;
	}
	
	public boolean isDoTaxonNames() {
		return doTaxonNames;
	}
	public void setDoTaxonNames(boolean doTaxonNames) {
		this.doTaxonNames = doTaxonNames;
	}

	public boolean isDoTaxa() {
		return doTaxa;
	}
	public void setDoTaxa(boolean doTaxa) {
		this.doTaxa = doTaxa;
	}


	@Override
	public boolean isOmitTermLoading() {
		return true;
	}

	@Override
	public boolean isCreateNew(){
		return true;
	}

	@Override
	public DbSchemaValidation getDbSchemaValidation() {
		if (isCreateNew()){
			return DbSchemaValidation.CREATE;
		}else{
			return super.getDbSchemaValidation();
		}
	}


}

   
