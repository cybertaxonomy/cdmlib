/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.api.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;

public interface IFeatureTreeService extends IIdentifiableEntityService<FeatureTree> {
	
	public List<FeatureNode> getFeatureNodesAll();
	
	/**
	 * Loads a feature tree including all of its nodes (all the way down to the tips of the tree). 
	 * Because this method automatically adds feature nodes recursively, adding "root" to property paths
	 * is supurfluous - the propertyPaths argument should be used to initialize property paths <i>in addition</i>
	 * to the feature nodes. The nodePaths argument is applied to each node in turn, so again, adding "children" 
	 * is also supurfluous. The nodePaths argument should be used to specify additional propertys of the featureNode
	 * to initialize (e.g. feature).
	 * 
	 */
	public FeatureTree loadWithNodes(UUID uuid, List<String> propertyPaths, List<String> nodePaths);

	public Map<UUID, FeatureNode> saveFeatureNodesAll(Collection<FeatureNode> featureNodeCollection);
	
	public Map<UUID, FeatureNode> saveOrUpdateFeatureNodesAll(Collection<FeatureNode> featureNodeCollection);
	
}
