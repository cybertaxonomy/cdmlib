/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.description;

import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;

/**
 * @author a.mueller
 * @since 10.07.2008
 * @version 1.0
 */
public interface IFeatureTreeDao extends IIdentifiableDao<FeatureTree> {
    public List<FeatureTree> list();

    public UUID DefaultFeatureTreeUuid = UUID.fromString("ac8d4e58-926d-4f81-ac77-cebdd295df7c");

    /**
     * Loads nodes and the nodes child nodes recursivly
     * @param nodes
     * @param nodePaths
     */
    public void deepLoadNodes(List<FeatureNode> nodes, List<String> nodePaths);
}
