/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.io.Serializable;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;


/**
 * @author a.mueller
 \* @since 21.09.2016
 *
 */
public abstract class EntityDTOBase<T extends CdmBase> implements Serializable{

    public class CdmEntity extends UuidAndTitleCache implements Serializable{
//        UUID cdmUuid;
//        String titleCache;
        T entity;
        public CdmEntity(UUID cdmUuid, String titleCache, T entity) {
//            this.cdmUuid = cdmUuid;
//            this.titleCache = titleCache;
            super(cdmUuid, titleCache);

            this.entity = entity;

        }
        /**
         * @param entityUuid
         * @param object
         * @param label
         * @param abbrevTitleCache
         */
        public CdmEntity(UUID entityUuid, Object object, String label, String abbrevTitleCache) {
            super(entityUuid, null, label, abbrevTitleCache);

        }
//        public UUID getCdmUuid() {return super.getUuid();}
//        @Override
//        public String getTitleCache() {return super.getTitleCache();}
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
