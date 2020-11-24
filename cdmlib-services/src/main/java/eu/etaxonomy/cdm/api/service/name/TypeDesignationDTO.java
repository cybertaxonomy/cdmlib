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
import eu.etaxonomy.cdm.strategy.cache.TaggedCacheHelper;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

/**
 * @author a.mueller
 * @since 24.11.2020
 */
public class TypeDesignationDTO implements Comparable<TypeDesignationDTO> {

    private UUID uuid;
    private Class<? extends TypeDesignationBase> type;

    private List<TaggedText> taggedText;
    private String label;

    public TypeDesignationDTO(Class<? extends TypeDesignationBase> type, UUID uuid,
            List<TaggedText> taggedText) {
        this.uuid = uuid;
        this.type = type;
        this.taggedText = taggedText;
        this.label = TaggedCacheHelper.createString(taggedText);
    }

    public UUID getUuid() {
        return uuid;
    }

    public Class<? extends TypeDesignationBase> getType() {
        return type;
    }

    public List<TaggedText> getTaggedText() {
        return taggedText;
    }

    @Override
    public int compareTo(TypeDesignationDTO o2) {
        if(o2 == null){
            return -1;
        }
        if (this.label == null && o2.label != null){
            return -1;
        }else if (this.label != null && o2.label == null){
            return 1;
        }else if (this.label == null && o2.label == null){
            return this.uuid.compareTo(o2.uuid);  //TODO also test null?
        }else{
            return this.label.compareTo(o2.label);
        }
    }
}
