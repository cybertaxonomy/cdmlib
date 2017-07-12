/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.UUID;

import eu.etaxonomy.cdm.api.service.config.FeatureNodeDeletionConfigurator;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;

/**
 * @author n.hoffmann
 * @created Aug 5, 2010
 * @version 1.0
 */
public interface IFeatureNodeService extends IVersionableService<FeatureNode>{


    /**
     * @param node
     * @param config
     * @return
     */
    DeleteResult isDeletable(FeatureNode node, FeatureNodeDeletionConfigurator config);

    /**
     * @param nodeUuid
     * @param config
     * @return
     */
    DeleteResult deleteFeatureNode(UUID nodeUuid, FeatureNodeDeletionConfigurator config);


    /**
     * Adds the specified feature as a child node to the given feature node
     * @param parentNode the feature node where the new feature should be added
     * @param featureChild the feature which should be added to the given feature node
     * @return the result of the operation
     */
    public UpdateResult addChildFeatureNode(FeatureNode parentNode, Feature featureChild);

    /**
     * Adds the specified feature as a child node to the given feature node
     * @param parentNodeUUID the UUID of the feature node where the new feature should be added
     * @param featureChildUuid the UUID of the feature which should be added to the given feature node
     * @return the result of the operation
     */
    public UpdateResult addChildFeatureNode(UUID parentNodeUUID, UUID featureChildUuid);

    /**
     * Moves a given {@link FeatureNode} to the target node at the given position;
     * @param movedNodeUuid the node to move
     * @param targetNodeUuid the target node
     * @param position the position in the list of children of the target node
     * @return the result of the operation
     */
    public UpdateResult moveFeatureNode(UUID movedNodeUuid, UUID targetNodeUuid, int position);

    /**
     * Moves a given {@link FeatureNode} to the target node;
     * @param movedNodeUuid the node to move
     * @param targetNodeUuid the target node
     * @return the result of the operation
     */
    public UpdateResult moveFeatureNode(UUID movedNodeUuid, UUID targetNodeUuid);

}
