/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal;

import java.util.UUID;

/**
 * @author a.mueller
 * @date 19.01.2023
 */
public class AnnotationDto extends CdmBaseDto {

    private String text;
    private UUID typeUuid;
    //TODO do we need type label, too?

    public AnnotationDto() {
        super(null, null, null);
    }

    public AnnotationDto(UUID uuid, int id) {
        super(uuid, id, null);
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
}
