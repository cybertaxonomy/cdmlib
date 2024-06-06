/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author a.mueller
 * @date 07.01.2023
 */
public class CdmBaseDto implements IPortalDtoBase {

    //the taxon uuid
    //TODO move to base class
    private UUID uuid;
    private Integer id;

    public CdmBaseDto() {
    }

    public CdmBaseDto(UUID uuid, Integer id, LocalDateTime lastUpdated) {
        this.uuid = uuid;
        this.id = id;
        this.lastUpdated = lastUpdated;
    }

    //computed from updated of all relevant data
    //uses java.time.XXX  to have less dependencies
    //TODO or should we use jodatime
    private LocalDateTime lastUpdated;

    public UUID getUuid() {
        return uuid;
    }
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    @Override
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}