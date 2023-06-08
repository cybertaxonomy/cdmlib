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

/**
 * @author K.Luther
 * @date 05.06.2023
 *
 */
public class AnnotationDto implements Serializable, Comparable<AnnotationDto>, ICdmBaseDto {
    private static final long serialVersionUID = -5138126315299678336L;

    private String text;
    private UUID typeUuid;
    private String typeLabel;
    private UUID uuid;
    private int id;
    //TODO do we need type label, too?

    public AnnotationDto() {

    }

    public AnnotationDto(UUID uuid, int id) {
        this.uuid = uuid;
        this.id = id;
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

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public UUID getTypeUuid() {
        return typeUuid;
    }
    public void setTypeUuid(UUID typeUuid) {
        this.typeUuid = typeUuid;
    }

    public String getTypeLabel() {
        return typeLabel;
    }

    public void setTypeLabel(String typeLabel) {
        this.typeLabel = typeLabel;
    }

    @Override
    public int compareTo(AnnotationDto o) {
        if (this.getUuid().equals(o.getUuid())) {
            return 0;
        }
        if(this.getText() == o.getText()) {
            return 0;
        }
        if (this.getText() == null) {
            return -1;
        }
        if (o.getText() == null) {
            return 1;
        }
        return this.getText().compareTo(o.getText());
    }
}
