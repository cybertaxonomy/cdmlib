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
import eu.etaxonomy.cdm.ref.RepresentableEntityReference;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

/**
 * @author a.mueller
 * @since 24.11.2020
 */
public class TypeDesignationDTO<T extends TypeDesignationBase> extends RepresentableEntityReference<T> {


    public TypeDesignationDTO(Class<T> type, UUID uuid, List<TaggedText> taggedText) {
        super(type, uuid, taggedText);
    }


}
