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

import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;

public interface IPolytomousKeyService extends IIdentifiableEntityService<PolytomousKey> {
	
	public List<PolytomousKeyNode> getPolytomousKeyNodesAll();
	
	/**
	 * Loads a polytomous key including all of its nodes (all the way down to the tips of the tree). 
	 * Because this method automatically adds key nodes recursively, adding "root" to property paths
	 * is superfluous - the propertyPaths argument should be used to initialize property paths <i>in addition</i>
	 * to the key nodes. The nodePaths argument is applied to each node in turn, so again, adding "children" 
	 * is also superfluous. The nodePaths argument should be used to specify additional properties of the 
	 * key node to initialize.
	 * 
	 */
	public PolytomousKey loadWithNodes(UUID uuid, List<String> propertyPaths, List<String> nodePaths);

	public Map<UUID, PolytomousKeyNode> savePolytomousKeyNodesAll(Collection<PolytomousKeyNode> polytomousKeyNodeCollection);
	
	public Map<UUID, PolytomousKeyNode> saveOrUpdatePolytomousKeyNodesAll(Collection<PolytomousKeyNode> polytomousKeyNodeCollection);
	
}
