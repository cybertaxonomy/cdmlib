/**
* Copyright (C) 2025 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.util.UUID;

import eu.etaxonomy.cdm.model.occurrence.Collection;

/**
 * @author kluther
 * @since 06.10.2025
 */
public class UuidAndTitleCacheWithCode extends UuidAndTitleCache<Collection> {

    String code;

    /**
     * @param type
     * @param uuid
     * @param id
     * @param titleCache
     */
    public UuidAndTitleCacheWithCode(UUID uuid, Integer id, String titleCache, String code) {
        super(Collection.class, uuid, id, titleCache);
        this.code = code;
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }





}
