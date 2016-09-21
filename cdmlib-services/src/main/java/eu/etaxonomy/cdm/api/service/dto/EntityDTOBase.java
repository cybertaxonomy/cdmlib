// $Id$
/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.UUID;

import eu.etaxonomy.cdm.model.common.CdmBase;


/**
 * @author a.mueller
 * @date 21.09.2016
 *
 */
public abstract class EntityDTOBase<T extends CdmBase> {

    public class CdmEntity{
        UUID cdmUuid;
        String titleCache;
        T entity;
        public CdmEntity(UUID cdmUuid, String titleCache, T entity) {
            this.cdmUuid = cdmUuid;
            this.titleCache = titleCache;
            this.entity = entity;

        }
        public UUID getCdmUuid() {return cdmUuid;}
        public String getTitleCache() {return titleCache;}
        public T getEntity() {return entity;}
    }

    protected CdmEntity cdmEntity;

    public CdmEntity getCdmEntity() {
        return cdmEntity;
    }
}
