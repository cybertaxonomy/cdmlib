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
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.media.Rights;

import java.util.*;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

/**
 * Abstract superclass implementing human annotations and machine markers to be assigned to CDM objects.
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:10
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AnnotatableEntity", propOrder = {
    "markers",
    "annotations"
})
@MappedSuperclass
public abstract class AnnotatableEntity<T extends AnnotatableEntity> extends VersionableEntity<T> {
	static Logger logger = Logger.getLogger(AnnotatableEntity.class);

	@XmlElementWrapper(name = "Markers")
	@XmlElement(name = "Marker")
	protected Set<Marker> markers = getNewMarkerSet();
	
	@XmlElementWrapper(name = "Annotations")
	@XmlElement(name = "Annotation")
	protected Set<Annotation> annotations = getNewAnnotationSet();
	
	protected AnnotatableEntity() {
		super();
	}

//*************** MARKER **********************************************
	
	
	@OneToMany(fetch=FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<Marker> getMarkers(){
		return this.markers;
	}
	public void addMarker(Marker marker){
		if (marker != null){
			marker.setMarkedObj(this);
			markers.add(marker);
		}
	}
	public void removeMarker(Marker marker){
		marker.setMarkedObj(null);
	}
	protected void setMarkers(Set<Marker> markers) {
		this.markers = markers;
	}

//*************** ANNOTATIONS **********************************************
	
	@OneToMany(fetch=FetchType.LAZY) //(mappedBy="AnnotatedObj")
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<Annotation> getAnnotations(){
		return this.annotations;
	}
	public void addAnnotation(Annotation annotation){
		if (annotation != null){
			annotation.setAnnotatedObj(this);
			annotations.add(annotation);
		}
	}
	public void removeAnnotation(Annotation annotation){
		annotation.setAnnotatedObj(null);
	}
	protected void setAnnotations(Set<Annotation> annotations) {
		this.annotations = annotations;
	}
	
//********************** CLONE *****************************************/

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.VersionableEntity#clone()
	 */
	public Object clone() throws CloneNotSupportedException{
		AnnotatableEntity result = (AnnotatableEntity)super.clone();
		
		//Annotations
		Set<Annotation> newAnnotations = getNewAnnotationSet();
		for (Annotation annotation : this.annotations ){
			Annotation newExtension = (Annotation)annotation.clone(this);
			newAnnotations.add(newExtension);
		}
		result.setAnnotations(newAnnotations);
		
		
		//Markers
		Set<Marker> newMarkers = getNewMarkerSet();
		for (Marker marker : this.markers ){
			Marker newMarker = (Marker)marker.clone(this);
			newMarkers.add(newMarker);
		}
		result.setMarkers(newMarkers);
		
		//no changes to: -
		return result;
	}
	
	@Transient
	private Set<Annotation> getNewAnnotationSet(){
		return new HashSet<Annotation>();
	}
	
	@Transient
	private Set<Marker> getNewMarkerSet(){
		return new HashSet<Marker>();
	}
	
}