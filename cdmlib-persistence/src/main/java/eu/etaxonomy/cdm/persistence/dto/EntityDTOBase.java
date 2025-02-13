/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.io.Serializable;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;

/**
 * @author a.mueller
 * @since 21.09.2016
 */
public abstract class EntityDTOBase<T extends CdmBase> implements Serializable{

    private static final long serialVersionUID = -4208986597695983584L;

    public class CdmEntity extends UuidAndTitleCache<T>{

        private static final long serialVersionUID = -4683693381724342023L;
        private T entity;

        public CdmEntity(UUID cdmUuid, String titleCache, T entity) {
            super(cdmUuid, titleCache);
            this.entity = entity;
        }

        public CdmEntity(UUID entityUuid, Object object, String label, String abbrevTitleCache) {
            super(entityUuid, null, label, abbrevTitleCache);
        }

        public T getEntity() {return entity;}
    }

    protected CdmEntity cdmEntity;

    public EntityDTOBase(IdentifiableEntity entity){
        this.cdmEntity = new CdmEntity(entity.getUuid(), entity.getTitleCache(), (T)entity);
    }

    public EntityDTOBase(UUID entityUuid, String label){
        this.cdmEntity = new CdmEntity(entityUuid, label, null);
    }
    public EntityDTOBase(UUID entityUuid, String label, String abbrevTitleCache){
        this.cdmEntity = new CdmEntity(entityUuid, null, label, abbrevTitleCache);
    }

    public CdmEntity getCdmEntity() {
        return cdmEntity;
    }
}