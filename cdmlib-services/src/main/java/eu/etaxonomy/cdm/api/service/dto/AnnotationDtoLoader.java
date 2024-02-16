/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import eu.etaxonomy.cdm.api.dto.AnnotationDTO;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * Loader for {@link AnnotationDTO}s. Extracted from DTO class.
 *
 * @author muellera
 * @since 13.02.2024
 */
public class AnnotationDtoLoader {

    public static AnnotationDtoLoader INSTANCE(){
        return new AnnotationDtoLoader();
    }

    public <T extends CdmBase> AnnotationDTO fromEntity(Annotation annotation) {
        @SuppressWarnings("unchecked")
        AnnotationDTO dto = new AnnotationDTO((Class<Annotation>)annotation.getClass(), annotation.getUuid());
        if(annotation.getAnnotationType() != null) {
            dto.setAnnotationTypeUuid(annotation.getAnnotationType().getUuid());
        }
        dto.setText(annotation.getText());
        return dto;
    }

}
