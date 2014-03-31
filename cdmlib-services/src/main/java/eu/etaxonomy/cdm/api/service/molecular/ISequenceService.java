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

import eu.etaxonomy.cdm.api.service.IAnnotatableService;
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
    public boolean moveSingleRead(Sequence from, Sequence to, SingleRead singleRead);

}
