/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.filter;

import java.util.UUID;

import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author muellera
 * @since 20.05.2026
 */
public final class CdmBaseFilters {

    public static EntityFilter<Reference> uuidFilter(UUID uuid) {
        return (root, cb) -> uuid == null ? null :  cb.equal(root.get("uuid"), uuid);
    }

}
