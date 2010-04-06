/**
 * Copyright (C) 2008 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.jaxb;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.reference.IDatabase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.babadshanjan
 * @created 14.11.2008
 */
public class JaxbImportConfigurator extends ImportConfiguratorBase implements IImportConfigurator {

	private static final Logger logger = Logger.getLogger(JaxbImportConfigurator.class);
		
	private int maxRows = 0;
	
	//TODO
	private static IInputTransformer defaultTransformer = null;

	
	private boolean doUsers = true;
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

	private boolean doTypeDesignations = true;
	private boolean doTaxonomicTreeData = true;

	
	
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getNewState()
	 */
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

	public static JaxbImportConfigurator NewInstance(String url,
			ICdmDataSource destination){
		return new JaxbImportConfigurator(url, destination);
	}


	/**
	 * @param url
	 * @param destination
	 */
	private JaxbImportConfigurator(String url, ICdmDataSource destination) {
		super(defaultTransformer);
		setSource(url);
		setDestination(destination);
		setDbSchemaValidation(DbSchemaValidation.CREATE);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSource()
	 */
	public Object getSource() {
//	public String getSource() {
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
			ReferenceFactory refFactory = ReferenceFactory.newInstance();
			logger.warn("getSource Reference not yet fully implemented");
			sourceReference = refFactory.newDatabase();
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
			return (String)this.getSource();
		}
	}
	
	public boolean isDoTypeDesignations() {
		return doTypeDesignations;
	}

	public void setDoTypeDesignations(boolean doTypeDesignations) {
		this.doTypeDesignations = doTypeDesignations;
	}

	public boolean isDoTaxonomicTreeData() {
		return this.doTaxonomicTreeData;
	}
}

   
