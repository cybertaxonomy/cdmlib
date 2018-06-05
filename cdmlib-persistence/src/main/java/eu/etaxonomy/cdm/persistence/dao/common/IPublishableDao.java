/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.common;

import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @since 04.06.2018
 *
 */
public interface IPublishableDao<T extends CdmBase> {

    /**
     * Like {@link #load(int, List)} but allows excluding unpublished records.
     *
     * @param uuid
     * @param includeUnpublished
     * @param propertyPaths
     * @see #load(UUID, List)
     */
    public T load(UUID uuid, boolean includeUnpublished, List<String> propertyPaths);
}
