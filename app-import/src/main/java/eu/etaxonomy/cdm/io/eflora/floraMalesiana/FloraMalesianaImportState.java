// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.eflora.floraMalesiana;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.model.description.FeatureNode;

/**
 * @author a.mueller
 * @created 11.05.2009
 * @version 1.0
 */
public class FloraMalesianaImportState extends ImportStateBase<FloraMalesianaImportConfigurator, FloraMalesianaImportBase>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FloraMalesianaImportState.class);
	

	private UnmatchedLeads unmatchedLeads;

	private Set<FeatureNode> featureNodesToSave = new HashSet<FeatureNode>();
	
//**************************** CONSTRUCTOR ******************************************/
	
	public FloraMalesianaImportState(FloraMalesianaImportConfigurator config) {
		super(config);
		setTransformer(new FloraMalesianaTransformer());
	}

// ********************************** GETTER / SETTER *************************************/	
	
	public UnmatchedLeads getUnmatchedLeads() {
		return unmatchedLeads;
	}

	public void setUnmatchedLeads(UnmatchedLeads unmatchedKeys) {
		this.unmatchedLeads = unmatchedKeys;
	}

	public void setFeatureNodesToSave(Set<FeatureNode> featureNodesToSave) {
		this.featureNodesToSave = featureNodesToSave;
	}

	public Set<FeatureNode> getFeatureNodesToSave() {
		return featureNodesToSave;
	}


}