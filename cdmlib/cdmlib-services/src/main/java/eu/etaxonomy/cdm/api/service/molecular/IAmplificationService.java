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

import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.api.service.IAnnotatableService;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.molecular.Amplification;
import eu.etaxonomy.cdm.model.molecular.SingleRead;

/**
 * @author pplitzner
 * @date 11.03.2014
 *
 */
public interface IAmplificationService extends IAnnotatableService<Amplification>{

    /**
     * Moves the given {@link SingleRead} from one {@link Amplification} to another.
     * @param from the Amplification from which the SingleRead will be removed
     * @param to the Amplification to which the SingleRead will be added
     * @param singleRead the SingleRead to move
     * @return <code>true</code> if successfully moved, <code>false</code> otherwise
     */
    public boolean moveSingleRead(Amplification from, Amplification to, SingleRead singleRead);

    /**
     * Retrieves the {@link UUID} and the string representation (title cache) of all
     * {@link Amplification}s found in the data base.
     * @return a list of {@link UuidAndTitleCache}
     */
    public List<UuidAndTitleCache<Amplification>> getAmplificationUuidAndDescription();

}
