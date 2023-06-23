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
public class MarkerDto implements Serializable, Comparable<MarkerDto>, ICdmBaseDto {

    private Boolean value;
    private UUID typeUuid;
    private String type;
    private UUID uuid;
    private int id;
    private DateTime created;
    private String createdBy;

    public MarkerDto(UUID uuid, int id) {
        this.uuid = uuid;
        this.id = id;
    }

    public MarkerDto(UUID uuid, Integer id, UUID typeUuid, String type, Boolean value, DateTime created, String createdBy) {
        this(uuid, id);
        this.typeUuid = typeUuid;
        this.type = type;
        this.value = value;
        this.created = created;
        this.createdBy = createdBy;
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
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    @Override
    public DateTime getCreated() {
        return created;
    }

    @Override
    public String getCreatedBy() {
        return createdBy;
    }
}
