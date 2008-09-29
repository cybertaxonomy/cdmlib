/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.io.jaxb;

import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.Source;

/**
 * @author a.babadshanjan
 * @created 03.09.2008
 */
public class JaxbExportImportConfigurator {

	private static final Logger logger = Logger.getLogger(JaxbExportImportConfigurator.class);

	private boolean doAgents = true;
	private boolean doAgentData = true;
	private boolean doLanguageData = true;
	private boolean doFeatureData = true;
	private boolean doDescriptions = true;
	private boolean doMedia = true;
	private boolean doOccurrences = true;
	private boolean doReferences = true;
	private boolean doReferencedEntities = true;
	private boolean doRelationships = true;
	private boolean doSynonyms = true;
	private boolean doTaxonNames = true;
	private boolean doTaxa = true;
	private boolean doTerms = true;
	private boolean doTermVocabularies = true;
	private boolean doHomotypicalGroups = true;
	
	private ICdmDataSource cdmSource;

//	private Object source;

	public JaxbExportImportConfigurator(ICdmDataSource sourceDb) {

		this.cdmSource = sourceDb;
	}
	
	public ICdmDataSource getSource() {
		return cdmSource;
	}
	
	public void setSource(ICdmDataSource cdmSource) {
		setSource(cdmSource);
	}

	public boolean isDoAgents() {
		return doAgents;
	}
	
	public void setDoAgents(boolean doAgents) {
		this.doAgents = doAgents;
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

	public boolean isDoOccurrences() {
		return doOccurrences;
	}
	
	public void setDoOccurrences(boolean doOccurrences) {
		this.doOccurrences = doOccurrences;
	}
	
	public boolean isDoReferences() {
		return doReferences;
	}
	
	public void setDoReferences(boolean doReferences) {
		this.doReferences = doReferences;
	}

	public boolean isDoReferencedEntities() {
		return doReferencedEntities;
	}
	
	public void setDoReferencedEntities(boolean doReferencedEntities) {
		this.doReferencedEntities = doReferencedEntities;
	}

	public boolean isDoRelationships() {
		return doRelationships;
	}
	
	public void setDoRelationships(boolean doRelationships) {
		this.doRelationships = doRelationships;
	}

	public boolean isDoSynonyms() {
		return doSynonyms;
	}
	
	public void setDoSynonyms(boolean doSynonyms) {
		this.doSynonyms = doSynonyms;
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
}
