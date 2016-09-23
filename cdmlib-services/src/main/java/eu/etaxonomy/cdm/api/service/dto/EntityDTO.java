// $Id$
/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.UUID;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;

/**
 * DTO for any CdmBase.
 *
 * @author a.mueller
 * @date 2016-09-21
 *
 */
//might extend CdmBase in future, when a default formatter is available for all CdmBase classes
public class EntityDTO<T extends IdentifiableEntity> extends EntityDTOBase<T> {

	public EntityDTO(T entity){
	    super(entity);
	}

	public EntityDTO(UUID entityUuid, String titleCache){
		super(entityUuid, titleCache);
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "(" + cdmEntity.getTitleCache() + "; " + cdmEntity.cdmUuid +  ")";
    }


}
