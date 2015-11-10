// $Id$
/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.molecular;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.api.service.DeleteResult;
import eu.etaxonomy.cdm.api.service.IAnnotatableService;
import eu.etaxonomy.cdm.api.service.UpdateResult;
import eu.etaxonomy.cdm.model.molecular.AmplificationResult;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.molecular.SingleRead;

/**
 * @author pplitzner
 * @date 11.03.2014
 *
 */
public interface ISequenceService extends IAnnotatableService<Sequence>{

    /**
     * Moves the given {@link SingleRead} from one {@link Sequence} to another.
     * @param from the Sequence from which the SingleRead will be removed
     * @param to the Sequence to which the SingleRead will be added
     * @param singleRead the SingleRead to move
     * @return <code>true</code> if successfully moved, <code>false</code> otherwise
     */
    public UpdateResult moveSingleRead(Sequence from, Sequence to, SingleRead singleRead);


    /**
     * @param fromUuid
     * @param toUuid
     * @param singleReadUuid
     * @return
     */
    public UpdateResult moveSingleRead(UUID fromUuid, UUID toUuid, UUID singleReadUuid);


    /**
     * Creates and returns a map with {@link SingleRead}s as keys.
     * The value for each key is a list of those {@link Sequence}s that link to this SingleRead.
     * @return a map of SingleReads as keys and the Sequences linking to them
     */
    public Map<SingleRead, Collection<Sequence>> getSingleReadSequencesMap();

    /**
     * Deletes a {@link SingleRead} from the given {@link Sequence} and its {@link AmplificationResult}.
     * @param singleRead the single read to delete
     * @param sequence the sequence to which the single read belongs
     * @return the {@link DeleteResult} which holds information about the outcome of this operation
     */
    public DeleteResult deleteSingleRead(SingleRead singleRead, Sequence sequence);

    /**
     * Deletes a {@link SingleRead} from the given {@link Sequence} and its {@link AmplificationResult},
     * by first loading the corresponding uuids and then calling {@link #deleteSingleRead(SingleRead, Sequence) deleteSingleRead}
     * @param singleReadUuid uuid of the single read to delete
     * @param sequenceUuid uuid of the sequence to which the single read belongs
     * @return the {@link DeleteResult} which holds information about the outcome of this operation
     */
    public DeleteResult deleteSingleRead(UUID singleReadUuid, UUID sequenceUuid);

}
