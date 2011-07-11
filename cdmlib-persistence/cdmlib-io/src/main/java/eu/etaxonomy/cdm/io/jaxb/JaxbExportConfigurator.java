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

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.ExportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.IExportConfigurator;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;

/**
 * @author a.babadshanjan
 * @created 03.09.2008
 */
public class JaxbExportConfigurator extends ExportConfiguratorBase implements IExportConfigurator {
	private static final Logger logger = Logger.getLogger(JaxbExportConfigurator.class);

	private int maxRows = 0;
	private boolean formattedOutput = Boolean.TRUE;
	private String encoding = "UTF-8"; 

	private boolean doUsers = true;
	private boolean doAgentData = true;
	private boolean doLanguageData = true;
	private boolean doFeatureData = true;
	private boolean doDescriptions = true;
	private boolean doMedia = true;
	private boolean doOccurrence = true;
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
	private boolean doClassificationData = true;
	
	
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IExportConfigurator#getNewState()
	 */
	public JaxbExportState getNewState() {
		return new JaxbExportState(this);
	}

	public int getMaxRows() {
		return maxRows;
	}
	
	public void setMaxRows(int maxRows) {
		this.maxRows = maxRows;
	}
	
	public boolean isFormattedOutput() {
		return formattedOutput;
	}
	
	public void setFormattedOutput(boolean formattedOutput) {
		this.formattedOutput = formattedOutput;
	}
	
	public String getEncoding() {
		return encoding;
	}
	
	public void setEncoding(String encoding) {
		this.encoding = encoding;
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

	public boolean isDoClassificationData() {
		return doClassificationData;
	}
	public void setDoClassificationData(boolean doClassificationData) {
		this.doClassificationData = doClassificationData;
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
	public boolean isDoUsers() {
		return doUsers;
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
				JaxbExport.class,
		};
	};

	
	public static JaxbExportConfigurator NewInstance(ICdmDataSource source, String url) {
		return new JaxbExportConfigurator(source, url);
	}
	
	
	/**
	 * @param url
	 * @param destination
	 */
	private JaxbExportConfigurator(ICdmDataSource source, String url) {
		super();
		setDestination(url);
		setSource(source);
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSource()
	 */
	public String getDestination() {
		return (String)super.getDestination();
	}

	
	/**
	 * @param file
	 */
	public void setDestination(String fileName) {
		super.setDestination(fileName);
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IExportConfigurator#getDestinationNameString()
	 */
	public String getDestinationNameString() {
		if (this.getDestination() == null) {
			return null;
		} else {
			return (String)this.getDestination();
		}
	}

	public boolean isDoTypeDesignations() {
		return doTypeDesignations;
	}

	public void setDoTypeDesignations(boolean doTypeDesignations) {
		this.doTypeDesignations = doTypeDesignations;
	}
	
	public boolean isDoOccurrence() {
		return doOccurrence;
	}
	public void setDoOccurrence(boolean doOccurrence) {
		this.doOccurrence = doOccurrence;
	}
	
	private boolean doAuthors = true;
	//references
	private DO_REFERENCES doReferences = DO_REFERENCES.ALL;
	//names
	private boolean doTaxonNames = true;
	//taxa
	private boolean doTaxa = true;

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


		
}
