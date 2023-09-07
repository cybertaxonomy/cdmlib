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
public class AnnotationDto extends CdmBaseDto implements Serializable, Comparable<AnnotationDto>{

    private static final long serialVersionUID = -5138126315299678336L;

    private String text;
    private UUID typeUuid;
    private String typeLabel;

    public AnnotationDto(UUID uuid, Integer id, UUID typeUuid, String typeLabel, String text, DateTime created, String createdBy, DateTime updated, String updatedBy) {
        super(uuid, id, created, createdBy, updated, updatedBy);
        this.typeUuid = typeUuid;
        this.typeLabel = typeLabel;
        this.text = text;

    }

    public AnnotationDto(UUID uuid, int id) {
        super(uuid, id);

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
