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
 * @version 1.0
 */
public interface IAnnotatableEntity extends IVersionableEntity {

	public Set<Annotation> getAnnotations();
	
	
	public void addAnnotation(Annotation annotation);
	
	
	public void removeAnnotation(Annotation annotation);
	
	
	public Set<Marker> getMarkers();
	
	
	public void addMarker(Marker marker);
	
	
	public void removeMarker(Marker marker);
	
	public boolean hasMarker(MarkerType type, boolean value);
	
	public boolean hasMarker(UUID uuidMarkerType, boolean value);
	
}
