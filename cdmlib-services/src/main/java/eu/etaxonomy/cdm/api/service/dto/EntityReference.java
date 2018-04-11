/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.UUID;

import org.apache.commons.lang.builder.HashCodeBuilder;

public class EntityReference {
    UUID uuid;
    String label;

    public EntityReference(UUID uuid, String label) {
        this.uuid = uuid;
        this.label = label;
    }


    public UUID getUuid() {
        return uuid;
    }

    public String getLabel() {
        return label;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31)
                .append(label)
                .append(uuid)
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        try {
            EntityReference other = (EntityReference) obj;
            return uuid.equals(other.uuid) && label.equals(other.label);

        } catch (Exception e) {
            return false;
        }
    }

}