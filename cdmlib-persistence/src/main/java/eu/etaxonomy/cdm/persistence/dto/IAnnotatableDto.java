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

import java.util.Set;



/**
 * @author K.Luther
 * @date 02.06.2023
 *
 */
public interface IAnnotatableDto {
    public Set<MarkerDto> getMarkers();

    public void setMarkers(Set<MarkerDto> markers);
    public void addMarker(MarkerDto marker) ;
    public void removeMarker(MarkerDto marker);

    public Set<AnnotationDto> getAnnotations() ;
    public void setAnnotations(Set<AnnotationDto> annotations);
    public void addAnnotation(AnnotationDto annotation);
    public void removeAnnotation(AnnotationDto annotation);

    public String getLabel();


}
