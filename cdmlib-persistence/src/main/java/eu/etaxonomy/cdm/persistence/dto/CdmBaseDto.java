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
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * @author KatjaLuther
 * @date 23.06.2023
 *
 */
public class CdmBaseDto implements Serializable, ICdmBaseDto {

    private static final long serialVersionUID = -5979861496250590244L;

    private UUID uuid;
    private int id;
    private ZonedDateTime created;
    private String createdBy;
    private UUID createdByUuid;
    private ZonedDateTime updated;
    private String updatedBy;
    private UUID updatedByUuid;

    public CdmBaseDto() {

    }

    public CdmBaseDto(UUID uuid, int id) {
        this.uuid = uuid;
        this.id = id;
    }

    public CdmBaseDto(UUID uuid, int id, UUID createdByUuid, UUID updatedByUuid) {
        this.uuid = uuid;
        this.id = id;
        this.updatedByUuid = updatedByUuid;
        this.createdByUuid = createdByUuid;
    }

    public CdmBaseDto(UUID uuid, int id, ZonedDateTime created, String createdBy, ZonedDateTime updated, String updatedBy) {
        this(uuid, id);
        this.created = created;
        this.createdBy = createdBy;
        this.updated = updated;
        this.updatedBy = updatedBy;
    }


    @Override
    public UUID getUuid() {
       return uuid;
    }


    public void setUuid(UUID uuid) {
       this.uuid = uuid ;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public ZonedDateTime getCreated() {
        return created;
    }

    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * @return the createdByUuid
     */
    public UUID getCreatedByUuid() {
        return createdByUuid;
    }

    /**
     * @param createdByUuid the createdByUuid to set
     */
    public void setCreatedByUuid(UUID createdByUuid) {
        this.createdByUuid = createdByUuid;
    }

    @Override
    public ZonedDateTime getUpdated() {
        return updated;
    }

    @Override
    public String getUpdatedBy() {
        return updatedBy;
    }


    /**
     * @return the updatedByUuid
     */
    public UUID getUpdatedByUuid() {
        return updatedByUuid;
    }

    /**
     * @param updatedByUuid the updatedByUuid to set
     */
    public void setUpdatedByUuid(UUID updatedByUuid) {
        this.updatedByUuid = updatedByUuid;
    }

    public void setCreated(ZonedDateTime created) {
        this.created = created;
    }


    public void setCreatedBy(String createdBy) {
        this.createdBy= createdBy ;
    }

    public void setUpdated(ZonedDateTime updated) {
        this.updated= updated ;
    }


    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

}
