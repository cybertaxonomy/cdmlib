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
 * @author KatjaLuther
 * @date 23.06.2023
 *
 */
public class CdmBaseDto implements Serializable,ICdmBaseDto {

    private static final long serialVersionUID = -5979861496250590244L;

    private UUID uuid;
    private int id;
    private DateTime created;
    private String createdBy;
    private UUID createdByUuid;
    private DateTime updated;
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

    public CdmBaseDto(UUID uuid, int id, DateTime created, String createdBy, DateTime updated, String updatedBy) {
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
    public DateTime getCreated() {
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
    public DateTime getUpdated() {
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

    public void setCreated(DateTime created) {
        this.created = created;
    }


    public void setCreatedBy(String createdBy) {
        this.createdBy= createdBy ;
    }

    public void setUpdated(DateTime updated) {
        this.updated= updated ;
    }


    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

}
