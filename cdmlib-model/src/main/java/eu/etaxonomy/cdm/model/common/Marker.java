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

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * This class aims to make available some "flags" for identifiable entities in a
 * flexible way. Application developers (and even users) can define their own
 * "flags" as a MarkerType.
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:33
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Marker")
@Entity
public class Marker extends VersionableEntity {
	private static final Logger logger = Logger.getLogger(Marker.class);
	
    @XmlElement(name = "Flag")
	private boolean flag;
    
    @XmlElement(name = "MarkerType")
	private MarkerType markerType;
    
    @XmlElement(name = "MarkedObject")
	private AnnotatableEntity markedObj;
	
	/**
	 * Factory method
	 * @return
	 */
	public static Marker NewInstance(){
		return new Marker();
	}

	/**
	 * Factory method
	 * @param markerType The type of the marker
	 * @param flag The value of the marker
	 * @return
	 */
	public static Marker NewInstance(MarkerType markerType, boolean flag){
		return new Marker(markerType, flag);
	}
	
	/**
	 * Default Constructor
	 */
	private Marker() {
	}

	/**
	 * Constructor
	 * @param flag
	 */
	protected Marker(MarkerType markerType, boolean flag){
		this.markerType = markerType;
		this.flag = flag;
	}
	
	/**
	 * @return
	 */
	@Transient
	public AnnotatableEntity getMarkedObj() {
		return markedObj;
	}
	protected void setMarkedObj(AnnotatableEntity newMarkedObject) {
		this.markedObj = newMarkedObject;
	}

	/**
	 * @return
	 */
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public MarkerType getMarkerType(){
		return this.markerType;
	}
	public void setMarkerType(MarkerType type){
		this.markerType = type;
	}

	/**
	 * The flag value.
	 * @return
	 */
	public boolean getFlag(){
		return this.flag;
	}
	public void setFlag(boolean flag){
		this.flag = flag;
	}

}