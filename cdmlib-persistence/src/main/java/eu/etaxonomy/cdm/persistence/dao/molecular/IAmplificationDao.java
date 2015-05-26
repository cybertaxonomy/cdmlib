// $Id$
/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.molecular;

import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.model.molecular.Amplification;
import eu.etaxonomy.cdm.persistence.dao.common.IAnnotatableDao;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;

/**
 * @author pplitzner
 * @date 11.03.2014
 *
 */
public interface IAmplificationDao extends IAnnotatableDao<Amplification>{

    /**
     * Retrieves the {@link UUID} and the string representation (title cache) of all
     * {@link Amplification}s found in the data base.
     * @return a list of {@link UuidAndTitleCache}
     */
    public List<UuidAndTitleCache<Amplification>> getAmplificationUuidAndLabelCache();
}
