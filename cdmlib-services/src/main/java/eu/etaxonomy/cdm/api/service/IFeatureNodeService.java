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

import eu.etaxonomy.cdm.api.service.config.TermNodeDeletionConfigurator;
import eu.etaxonomy.cdm.model.term.TermNode;

/**
 * @author n.hoffmann
 * @since Aug 5, 2010
 * @deprecated use ITermNodeService instead
 */
@Deprecated
public interface IFeatureNodeService extends IVersionableService<TermNode>{

    DeleteResult isDeletable(UUID nodeUuid, TermNodeDeletionConfigurator config);

    DeleteResult deleteFeatureNode(UUID nodeUuid, TermNodeDeletionConfigurator config);



    /**
     * Adds the specified feature as a child node to the given feature node
     * @param parentNodeUUID the UUID of the feature node where the new feature should be added
     * @param termChildUuid the UUID of the term which should be added to the given feature node
     * @return the result of the operation
     */
    public UpdateResult addChildFeatureNode(UUID parentNodeUUID, UUID termChildUuid);

    /**
     * Adds the specified feature as a child node to the given feature node at the given position
     * @param parentNodeUUID the UUID of the feature node where the new feature should be added
     * @param termChildUuid the UUID of the term which should be added to the given feature node
     * @param position the position where the child node should be added
     * @return the result of the operation
     */
    public UpdateResult addChildFeatureNode(UUID parentNodeUUID, UUID termChildUuid, int position);

    /**
     * Moves a given {@link TermNode} to the target node at the given position;
     * @param movedNodeUuid the node to move
     * @param targetNodeUuid the target node
     * @param position the position in the list of children of the target node
     * @return the result of the operation
     */
    public UpdateResult moveFeatureNode(UUID movedNodeUuid, UUID targetNodeUuid, int position);

    /**
     * Moves a given {@link TermNode} to the target node;
     * @param movedNodeUuid the node to move
     * @param targetNodeUuid the target node
     * @return the result of the operation
     */
    public UpdateResult moveFeatureNode(UUID movedNodeUuid, UUID targetNodeUuid);




}
