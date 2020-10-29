/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.api.service.config.TermNodeDeletionConfigurator;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.persistence.dto.CharacterNodeDto;
import eu.etaxonomy.cdm.persistence.dto.TermNodeDto;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author n.hoffmann
 * @since Aug 5, 2010
 */
public interface ITermNodeService extends IVersionableService<TermNode>{

    public List<TermNode> list(TermType termType, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);

    public DeleteResult isDeletable(UUID nodeUuid, TermNodeDeletionConfigurator config);

    public DeleteResult deleteNode(UUID nodeUuid, TermNodeDeletionConfigurator config);


    /**
     * <b>Saves</b> and adds the specified term as a child node to the given term node.
     * @see ITermNodeService#addChildNode(TermNode, DefinedTermBase)
     * @param parentNodeUuid the term node to which the new term should be added
     * @param term the term which should be <b>saved</b> and added to the given term node
     * @param vocabularyUuid the UUID of the vocabulary where the term should be saved
     * @return the result of the operation
     */
    public UpdateResult createChildNode(UUID parentNodeUuid, DefinedTermBase term, UUID vocabularyUuid);

    /**
     * Adds the specified term as a child node to the given term node
     * @param parentNodeUUID the UUID of the term node to which the new term should be added
     * @param termChildUuid the UUID of the term which should be added to the given term node
     * @return the result of the operation
     */
    public UpdateResult addChildNode(UUID parentNodeUUID, UUID termChildUuid);

    /**
     * Adds the specified term as a child node to the given term node at the given position
     * @param parentNodeUUID the UUID of the term node to which the new term should be added
     * @param termChildUuid the UUID of the term which should be added to the given term node
     * @param position the position where the child node should be added
     * @return the result of the operation
     */
    public UpdateResult addChildNode(UUID parentNodeUUID, UUID termChildUuid, int position);

    /**
     * Moves a given {@link TermNode} to the target node at the given position;
     * @param movedNodeUuid the node to move
     * @param targetNodeUuid the target node
     * @param position the position in the list of children of the target node
     * @return the result of the operation
     */
    public UpdateResult moveNode(UUID movedNodeUuid, UUID targetNodeUuid, int position);

    /**
     * Moves a given {@link TermNode} to the target node;
     * @param movedNodeUuid the node to move
     * @param targetNodeUuid the target node
     * @return the result of the operation
     */
    public UpdateResult moveNode(UUID movedNodeUuid, UUID targetNodeUuid);

    /**
     * @param dtos
     * @return
     */
    UpdateResult saveTermNodeDtoList(List<TermNodeDto> dtos);

    /**
     * @param dtos
     * @return
     */
    public UpdateResult saveCharacterNodeDtoList(List<CharacterNodeDto> dtos);

}
