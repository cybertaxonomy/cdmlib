/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.name;

import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.ref.TaggedEntityReference;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

/**
 * @author a.mueller
 * @since 24.11.2020
 */
public class TypeDesignationDTO<T extends TypeDesignationBase> extends TaggedEntityReference<T> {

    private static final long serialVersionUID = -7638336499975494954L;

    private UUID typeUuid;

    /**
     * @param type the typeDesignations subclass
     * @param uuid the typeDesignations uuid
     * @param taggedText
     * @param typeUuid the uuid of the type (may it be specimen or name)
     */
    public TypeDesignationDTO(Class<T> type, UUID uuid, List<TaggedText> taggedText, UUID typeUuid) {
        super(type, uuid, taggedText);
        this.typeUuid = typeUuid;
    }

    public UUID getTypeUuid() {
        return typeUuid;
    }

}
