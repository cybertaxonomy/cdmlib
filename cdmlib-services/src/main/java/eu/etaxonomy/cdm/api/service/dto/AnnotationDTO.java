/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.UUID;

import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.ref.TypedEntityReference;

/**
 * @author a.kohlbecker
 * @since Dec 10, 2021
 */
public class AnnotationDTO extends TypedEntityReference<Annotation> {

    private static final long serialVersionUID = 4865632821417238523L;

    private UUID annotationTypeUuid = null;
    private String text = null;

    public static <T extends CdmBase> AnnotationDTO fromEntity(Annotation annotation) {
        AnnotationDTO dto = new AnnotationDTO(Annotation.class, annotation.getUuid());
        if(annotation.getAnnotationType() != null) {
            dto.setAnnotationTypeUuid(annotation.getAnnotationType().getUuid());
        }
        dto.setText(annotation.getText());
        return dto;
    }

    @SuppressWarnings("deprecation")
    public AnnotationDTO(Class<Annotation> type, UUID uuid) {
        super(type, uuid);
        this.label = null;
    }

    public UUID getAnnotationTypeUuid() {
        return annotationTypeUuid;
    }

    private void setAnnotationTypeUuid(UUID annotationTypeUuid) {
        this.annotationTypeUuid = annotationTypeUuid;
    }

    public String getText() {
        return text;
    }

    private void setText(String text) {
        this.text = text;
    }

}
