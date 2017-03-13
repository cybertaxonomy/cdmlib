/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.util.List;

import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;

/**
 * @author k.luther
 * @date 15.02.2017
 *
 */
public interface IRightsService extends IAnnotatableService<Rights>{

    /**
     * @param limit
     * @param pattern
     * @return
     */
    List<UuidAndTitleCache<Rights>> getUuidAndLabelText(Integer limit, String pattern);

}
