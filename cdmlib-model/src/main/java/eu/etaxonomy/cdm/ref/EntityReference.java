/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ref;

import java.io.Serializable;
import java.util.UUID;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author a.kohlbecker
 */
public class EntityReference implements Serializable, Comparable<EntityReference> {

    private static final long serialVersionUID = -8173845668898512626L;

    protected UUID uuid;
    protected String label;

    public EntityReference() {

    }

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

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31)
                .append(label)
                .append(uuid)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        try {
            EntityReference other = (EntityReference) obj;
            return uuid.equals(other.uuid) && label.equals(other.label);

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int compareTo(EntityReference o2) {
        if(o2 == null){
            return -1;
        }
        if (this.label == null && o2.label != null){
            return -1;
        }else if (this.label != null && o2.label == null){
            return 1;
        }else if (this.label == null && o2.label == null){
            return this.uuid.compareTo(o2.uuid);  //TODO also test null?
        }else{
            return this.label.compareTo(o2.label);
        }
    }

}