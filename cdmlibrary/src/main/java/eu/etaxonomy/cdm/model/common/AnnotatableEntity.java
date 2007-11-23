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
@Entity
public abstract class AnnotatableEntity extends VersionableEntity {
	public AnnotatableEntity() {
		super();
		// TODO Auto-generated constructor stub
	}

	static Logger logger = Logger.getLogger(AnnotatableEntity.class);
	private Set<Marker> markers = new HashSet();
	private Set<Annotation> annotations = new HashSet();
	
	
	@OneToMany(mappedBy="markedObj")
	public Set<Marker> getMarkers(){
		return this.markers;
	}
	public void addMarker(Marker marker){

	}
	public void removeMarker(Marker marker){

	}
	protected void setMarkers(Set<Marker> markers) {
		this.markers = markers;
	}

	
	@OneToMany(mappedBy="annotatedObj")
	public Set<Annotation> getAnnotations(){
		return this.annotations;
	}
	public void addAnnotations(Annotation annotation){

	}
	public void removeAnnotations(Annotation annotation){

	}
	protected void setAnnotations(Set<Annotation> annotations) {
		this.annotations = annotations;
	}

}