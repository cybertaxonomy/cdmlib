/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.io.Serializable;
import java.util.Set;

import eu.etaxonomy.cdm.model.common.ICdmBase;

/**
 * @author cmathew
 * @since 7 Oct 2015
 *
 */
public class MergeResult<T extends ICdmBase> implements Serializable {

    private static final long serialVersionUID = 4886558003386941487L;

    private T mergedEntity;
    private Set<T> newEntities;


    public MergeResult(T mergedEntity, Set<T> newEntities) {
        this.mergedEntity = mergedEntity;
        this.newEntities = newEntities;
    }

    /**
     * @return the mergedEntity
     */
    public T getMergedEntity() {
        return mergedEntity;
    }
    /**
     * @param mergedEntity the mergedEntity to set
     */
    public void setMergedEntity(T mergedEntity) {
        this.mergedEntity = mergedEntity;
    }
    /**
     * @return the newEntities
     */
    public Set<T> getNewEntities() {
        return newEntities;
    }
    /**
     * @param newEntities the newEntities to set
     */
    public void setNewEntities(Set<T> newEntities) {
        this.newEntities = newEntities;
    }

}
