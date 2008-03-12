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

import java.util.*;

import javax.persistence.*;

/**
 * Abstract superclass implementing human annotations and machine markers to be assigned to CDM objects.
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:10
 */
@MappedSuperclass
public abstract class AnnotatableEntity<T extends AnnotatableEntity> extends VersionableEntity<T> {
	static Logger logger = Logger.getLogger(AnnotatableEntity.class);

	protected Set<Marker> markers = new HashSet();
	protected Set<Annotation> annotations = new HashSet();
	
	
	public AnnotatableEntity() {
		super();
		// TODO Auto-generated constructor stub
	}

	@OneToMany
	public Set<Marker> getMarkers(){
		return this.markers;
	}
	public void addMarker(Marker marker){
		marker.setMarkedObj(this);
	}
	public void removeMarker(Marker marker){
		marker.setMarkedObj(null);
	}
	protected void setMarkers(Set<Marker> markers) {
		this.markers = markers;
	}

	
	@OneToMany
	public Set<Annotation> getAnnotations(){
		return this.annotations;
	}
	public void addAnnotations(Annotation annotation){
		annotation.setAnnotatedObj(this);
	}
	public void removeAnnotations(Annotation annotation){
		annotation.setAnnotatedObj(null);
	}
	protected void setAnnotations(Set<Annotation> annotations) {
		this.annotations = annotations;
	}

}