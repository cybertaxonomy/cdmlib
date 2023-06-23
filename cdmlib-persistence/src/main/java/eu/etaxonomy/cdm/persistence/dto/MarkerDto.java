// $Id$
/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.io.Serializable;
import java.util.UUID;

import org.joda.time.DateTime;

/**
 * @author K.Luther
 * @date 05.06.2023
 *
 */
public class MarkerDto extends CdmBaseDto implements Serializable, Comparable<MarkerDto> {

    private Boolean value;
    private UUID typeUuid;
    private String type;


    public MarkerDto(UUID uuid, int id) {
        super(uuid, id);

    }

    public MarkerDto(UUID uuid, Integer id, UUID typeUuid, String type, Boolean value, DateTime created, String createdBy, DateTime updated, String updatedBy) {
        super(uuid, id, created, createdBy, updated, updatedBy);
        this.typeUuid = typeUuid;
        this.type = type;
        this.value = value;

    }

    public Boolean getValue() {
        return value;
    }

    public void setValue(Boolean value) {
        this.value = value;
    }

    public UUID getTypeUuid() {
        return typeUuid;
    }

    public void setTypeUuid(UUID typeUuid) {
        this.typeUuid = typeUuid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }



    @Override
    public int compareTo(MarkerDto o) {
        if (this.getTypeUuid() != null && o.getTypeUuid() != null && this.getTypeUuid().equals(o.getTypeUuid())) {
            return 0;
        }
        if(this.getType() == o.getType()) {
            if (this.value && !o.getValue()) {
                return 1;
            }
            if (this.value == o.getValue()) {
                return 0;
            }
            if (o.getValue() && !this.getValue()) {
                return -1;
            }
            return 0;
        }
        if (this.getType() == null) {
            return -1;
        }
        if (o.getType() == null) {
            return 1;
        }

        return this.getType().compareTo(o.getType());
    }


}
