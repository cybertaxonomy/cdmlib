/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:10
 */
@MappedSuperclass
public abstract class AnnotatableEntity extends VersionableEntity {
	public AnnotatableEntity() {
		super();
		// TODO Auto-generated constructor stub
	}

	static Logger logger = Logger.getLogger(AnnotatableEntity.class);
	private Set<Marker> markers;
	private Set<Annotation> annotations;
	public Set<Marker> getMarkers(){
		return this.markers;
	}

	/**
	 * 
	 * @param marker    marker
	 */
	public void addMarker(Marker marker){

	}

	/**
	 * 
	 * @param marker    marker
	 */
	public void removeMarker(Marker marker){

	}

	public Set<Annotation> getAnnotations(){
		return this.annotations;
	}

	/**
	 * @param annotations
	 * 
	 * @param annotation
	 */
	public void addAnnotations(Annotation annotation){

	}

	/**
	 * 
	 * @param annotation
	 */
	public void removeAnnotations(Annotation annotation){

	}

	public void setMarkers(Set<Marker> markers) {
		this.markers = markers;
	}

	public void setAnnotations(Set<Annotation> annotations) {
		this.annotations = annotations;
	}

}