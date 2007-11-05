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
 * @created 02-Nov-2007 19:35:54
 */
@MappedSuperclass
public abstract class AnnotatableEntity extends VersionableEntity {
	static Logger logger = Logger.getLogger(AnnotatableEntity.class);

	private ArrayList markers;
	private ArrayList annotations;

	
	@Transient
	public ArrayList<Marker> getMarkers(){
		return markers;
	}
	/**
	 * 
	 * @param marker
	 */
	public void addMarker(Marker marker){

	}
	/**
	 * 
	 * @param marker
	 */
	public void removeMarker(Marker marker){

	}

	

	public ArrayList<Annotation> getAnnotations(){
		return annotations;
	}
	/**
	 * 
	 * @param annotations
	 */
	public void addAnnotations(Annotation annotation){
	}
	public void removeAnnotations(Annotation annotation){
	}

}