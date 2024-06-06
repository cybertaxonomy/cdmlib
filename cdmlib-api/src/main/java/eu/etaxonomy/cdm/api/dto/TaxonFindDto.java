/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto;

import java.util.UUID;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;

/**
 * @author muellera
 * @since 25.02.2024
 */
public class TaxonFindDto {

    private IdentifiableEntity<?> entity;

    private UUID acceptedTaxonUuid;

    private String sourceString;


    public IdentifiableEntity<?> getEntity() {
        return entity;
    }
    public void setEntity(IdentifiableEntity<?> entity) {
        this.entity = entity;
    }


    public UUID getAcceptedTaxonUuid() {
        return acceptedTaxonUuid;
    }
    public void setAcceptedTaxonUuid(UUID acceptedTaxonUuid) {
        this.acceptedTaxonUuid = acceptedTaxonUuid;
    }

    public String getSourceString() {
        return sourceString;
    }
    public void setSourceString(String sourceString) {
        this.sourceString = sourceString;
    }
}