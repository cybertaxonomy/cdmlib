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

import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.ref.TypedEntityReference;

/**
 * DTO class for defined terms.
 *
 * @author muellera
 * @since 16.02.2024
 */
public class DefinedTermDTO extends TypedEntityReference<DefinedTermBase> {

    private static final long serialVersionUID = 4865632821417238523L;

    private UUID annotationTypeUuid = null;
    private String text = null;

    public DefinedTermDTO(Class<? extends DefinedTermBase> clazz, UUID uuid, String label) {
        super((Class)clazz, uuid, label);
    }

    public UUID getAnnotationTypeUuid() {
        return annotationTypeUuid;
    }
    public void setAnnotationTypeUuid(UUID annotationTypeUuid) {
        this.annotationTypeUuid = annotationTypeUuid;
    }

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
}
