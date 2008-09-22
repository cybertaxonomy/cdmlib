/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.io.jaxb;

import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;

/**
 * @author a.babadshanjan
 * @created 03.09.2008
 */
public class JaxbExportConfigurator {

	private static final Logger logger = Logger.getLogger(JaxbExportConfigurator.class);

	private HashMap<String, Integer> configuration = new HashMap<String, Integer>(13);
	
	private boolean doAgents = true;
	private boolean doAgentData = true;
	private boolean doFeatureData = true;
	private boolean doTerms = true;
	private boolean doTermVocabularies = true;
	private boolean doOccurrences = true;
	private boolean doReferences = true;
	private boolean doReferencedEntities = true;
	private boolean doTaxonNames = true;
	private boolean doTaxa = true;
	private boolean doSynonyms = true;
	private boolean doRelationships = true;
	private boolean doMedia = true;

	public JaxbExportConfigurator() {
		
		
	}
	
	public boolean isDoAgents() {
		return doAgents;
	}
	
	public void setDoAgents(boolean doAgents) {
		this.doAgents = doAgents;
	}
}
