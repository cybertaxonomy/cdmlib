/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto;

import java.util.UUID;

import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.ref.TypedEntityReference;

/**
 * @author a.kohlbecker
 * @since Dec 10, 2021
 */
public class AnnotationDTO extends TypedEntityReference<Annotation> {

    private static final long serialVersionUID = 4865632821417238523L;

    private UUID annotationTypeUuid = null;
    private String text = null;

    public AnnotationDTO(Class<Annotation> clazz, UUID uuid) {
        super(clazz, uuid, null);
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