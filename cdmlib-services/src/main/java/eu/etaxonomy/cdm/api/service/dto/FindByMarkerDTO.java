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
import eu.etaxonomy.cdm.model.common.MarkerType;

/**
 * DTO for IdentifiableEntities matching a certain marker.
 *
 * @author a.mueller
 * @date 2016-09-16
 *
 */
public class FindByMarkerDTO<T extends IdentifiableEntity> {

	public class Marker{
		UUID typeUuid;
		String typeLabel;
		Boolean flag;
		public Marker(MarkerType markerType, Boolean flag) {
			this.typeUuid = markerType.getUuid();
			this.typeLabel = markerType.getTitleCache();
			this.flag = flag;
		}
		public UUID getTypeUuid() {return typeUuid;}
		public String getTypeLabel() {return typeLabel;}
		public Boolean getFlag() {return flag;}
	}

	public class CdmEntity{
		UUID cdmUuid;
		String titleCache;
		T entitys;
		public CdmEntity(UUID cdmUuid, String titleCache, T entity) {
			this.cdmUuid = cdmUuid;
			this.titleCache = titleCache;
			this.entitys = entity;
		}
		public UUID getCdmUuid() {return cdmUuid;}
		public String getTitleCache() {return titleCache;}
		public T getEntityX() {return entitys;}

	}

	private Marker marker;

	private CdmEntity cdmEntity;

	public FindByMarkerDTO(MarkerType markerType, Boolean flag, T entity){
		this.marker = new Marker(markerType, flag);
		this.cdmEntity = new CdmEntity(entity.getUuid(), entity.getTitleCache(), entity);
	}

	public FindByMarkerDTO(MarkerType markerType, Boolean flag, UUID entityUuid, String titleCache){
		this.marker = new Marker(markerType, flag);
		this.cdmEntity = new CdmEntity(entityUuid, titleCache, null);
	}

	public Marker getMarker() {
		return marker;
	}

	public CdmEntity getCdmEntity() {
		return cdmEntity;
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "(" + marker.typeLabel + "; "  + cdmEntity.getTitleCache() + "; " + cdmEntity.cdmUuid +  ")";
    }


}
