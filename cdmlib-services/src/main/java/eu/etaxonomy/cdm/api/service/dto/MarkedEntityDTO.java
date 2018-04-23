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
 \* @since 2016-09-16
 *
 */
//might extend AnnotatableEntity in future
public class MarkedEntityDTO<T extends IdentifiableEntity> extends EntityDTOBase<T> {

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

	private Marker marker;

	public MarkedEntityDTO(MarkerType markerType, Boolean flag, T entity){
	    super(entity);
		this.marker = new Marker(markerType, flag);
	}

	public MarkedEntityDTO(MarkerType markerType, Boolean flag, UUID entityUuid, String titleCache){
	    super(entityUuid, titleCache);
	    this.marker = new Marker(markerType, flag);
	}

	public Marker getMarker() {
		return marker;
	}


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "(" + marker.typeLabel + "; "  + cdmEntity.getTitleCache() + "; " + cdmEntity.getUuid() +  ")";
    }


}
