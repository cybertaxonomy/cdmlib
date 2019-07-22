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

import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;

public interface IFeatureTreeService extends IIdentifiableEntityService<TermTree> {

	public List<TermNode> getFeatureNodesAll();

	/**
	 * Loads a feature tree including all of its nodes (all the way down to the tips of the tree).
	 * Because this method automatically adds feature nodes recursively, adding "root" to property paths
	 * is supurfluous - the propertyPaths argument should be used to initialize property paths <i>in addition</i>
	 * to the feature nodes. The nodePaths argument is applied to each node in turn, so again, adding "children"
	 * is also supurfluous. The nodePaths argument should be used to specify additional propertys of the featureNode
	 * to initialize (e.g. feature).
	 *
	 */
	public TermTree loadWithNodes(UUID uuid, List<String> propertyPaths, List<String> nodePaths);

	public Map<UUID, TermNode> saveFeatureNodesAll(Collection<TermNode> featureNodeCollection);

	public Map<UUID, TermNode> saveOrUpdateFeatureNodesAll(Collection<TermNode> featureNodeCollection);

	public TermTree createTransientDefaultFeatureTree();

	public <S extends TermTree> List<UuidAndTitleCache<S>> getUuidAndTitleCacheByTermType(Class<S> clazz, TermType termType, Integer limit,
            String pattern);
}
