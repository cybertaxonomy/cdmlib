/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal;

/**
 * @author a.mueller
 * @date 19.01.2023
 */
public class AnnotatableDto extends CdmBaseDto {

    private ContainerDto<AnnotationDto> annotations;
    private ContainerDto<MarkerDto> markers;


    public ContainerDto<AnnotationDto> getAnnotations() {
        return annotations;
    }
    public void addAnnotation(AnnotationDto annotation) {
        if (annotations == null) {
            annotations = new ContainerDto<>();
        }
        annotations.addItem(annotation);
    }


    public ContainerDto<MarkerDto> getMarkers() {
        return markers;
    }
    public void addMarker(MarkerDto marker) {
        if (markers == null) {
            markers = new ContainerDto<>();
        }
        markers.addItem(marker);
    }

}
