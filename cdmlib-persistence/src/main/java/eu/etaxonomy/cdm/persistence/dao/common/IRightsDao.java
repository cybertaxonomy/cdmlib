/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.common;

import java.util.List;

import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;

/**
 * @author k.luther
 * @since 15.02.2017
 */
public interface IRightsDao extends IAnnotatableDao<Rights> {

    public List<UuidAndTitleCache<Rights>> getUuidAndTitleCache(Integer limit, String pattern);

}