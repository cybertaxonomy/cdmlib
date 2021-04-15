/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.io.Serializable;
import java.util.UUID;

import eu.etaxonomy.cdm.model.reference.NamedSourceBase;

/**
 * @author a.kohlbecker
 * @since Aug 31, 2018
 */
public class SourceDTO implements Serializable{

    private UUID uuid;
    String citationDetail;
    ReferenceDTO citation;

    public static SourceDTO fromDescriptionElementSource(NamedSourceBase entity) {
        if(entity == null) {
            return null;
        }
        SourceDTO dto = new SourceDTO();
        dto.uuid = entity.getUuid();
        dto.citation = ReferenceDTO.fromReference(entity.getCitation());
        dto.citationDetail = entity.getCitationMicroReference();
        return dto;
    }

    public UUID getUuid() {
        return uuid;
    }


    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public ReferenceDTO getCitation() {
        return citation;
    }


    public void setCitation(ReferenceDTO citation) {
        this.citation = citation;
    }


    public String getCitationDetail() {
        return citationDetail;
    }


    public void setCitationDetail(String citationDetail) {
        this.citationDetail = citationDetail;
    }

}
