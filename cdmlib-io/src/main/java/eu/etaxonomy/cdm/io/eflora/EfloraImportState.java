// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.eflora;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.eflora.EfloraImportBase;
import eu.etaxonomy.cdm.io.eflora.EfloraTransformer;
import eu.etaxonomy.cdm.model.description.FeatureNode;

/**
 * @author a.mueller
 * @created 11.05.2009
 * @version 1.0
 */
public class EfloraImportState extends ImportStateBase<EfloraImportConfigurator, EfloraImportBase>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(EfloraImportState.class);
	

	private UnmatchedLeads unmatchedLeads;

	private Set<FeatureNode> featureNodesToSave = new HashSet<FeatureNode>();
	
//**************************** CONSTRUCTOR ******************************************/
	
	public EfloraImportState(EfloraImportConfigurator config) {
		super(config);
		if (getTransformer() == null){
			IInputTransformer newTransformer = config.getTransformer();
			if (newTransformer == null){
				newTransformer = new EfloraTransformer();
			}
			setTransformer(newTransformer);
		}
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