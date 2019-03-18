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
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.FeatureNode;

/**
 * @author n.hoffmann
 * @since Aug 5, 2010
 * @version 1.0
 */
public interface IFeatureNodeService extends IVersionableService<FeatureNode>{


    DeleteResult isDeletable(FeatureNode node, FeatureNodeDeletionConfigurator config);

    DeleteResult deleteFeatureNode(UUID nodeUuid, FeatureNodeDeletionConfigurator config);


    /**
     * Adds the specified term as a child node to the given feature node
     * @param parentNode the feature node where the new term should be added
     * @param term the term which should be added to the given feature node
     * @return the result of the operation
     */
    public UpdateResult addChildFeatureNode(FeatureNode parentNode, DefinedTermBase term);

    /**
     * Adds the specified feature as a child node to the given feature node
     * @param parentNode the feature node where the new feature should be added
     * @param term the term which should be added to the given feature node
     * @param position the position where the child node should be added
     * @return the result of the operation
     */
    public UpdateResult addChildFeatureNode(FeatureNode parentNode, DefinedTermBase term, int position);


    /**
     * <b>Saves</b> and adds the specified feature as a child node to the given feature node.
     * @see IFeatureNodeService#addChildFeatureNode(FeatureNode, DefinedTermBase)
     * @param parentNode the feature node where the new feature should be added
     * @param term the term which should be <b>saved</b> and added to the given feature node
     * @param vocabularyUuid the UUID of the vocabulary where the term should be saved
     * @return the result of the operation
     */
    public UpdateResult createChildFeatureNode(FeatureNode node, DefinedTermBase term, UUID vocabularyUuid);

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
