/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import java.util.Set;
import java.util.UUID;

/**
 * @author n.hoffmann
 * @since Sep 15, 2010
 */
public interface IAnnotatableEntity extends IVersionableEntity {

	public Set<Annotation> getAnnotations();
	public void addAnnotation(Annotation annotation);
	public void removeAnnotation(Annotation annotation);


	public Set<Marker> getMarkers();
	public Set<Marker> getMarkers(UUID uuidMarkerType);
	public void addMarker(Marker marker);
	public void removeMarker(Marker marker);
	public void removeMarker(UUID markerTypeUuid);  //removes the marker no matter what value it has

	public boolean hasMarker(MarkerType type, boolean value);
	public boolean hasMarker(UUID uuidMarkerType, boolean value);
	public boolean hasAnyMarkerOf(Set<UUID> uuidMarkerTypes, boolean value);
	public Boolean markerValue(UUID uuidMarkerType);

}
