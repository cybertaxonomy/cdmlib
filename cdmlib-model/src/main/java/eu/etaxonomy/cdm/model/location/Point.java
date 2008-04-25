/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.location;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.description.Scope;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:44
 */
@Embeddable
public class Point {
	private static final Logger logger = Logger.getLogger(Point.class);
	
	private Float longitude;
	private Float latitude;
	//in Meters
	private Integer errorRadius = 0;
	private ReferenceSystem referenceSystem;
	
	/**
	 * Factory method
	 * @return
	 */
	public static Point NewInstance(){
		return new Point();
	}
	
	/**
	 * Constructor
	 */
	public Point() {
	}
	
	@ManyToOne
	public ReferenceSystem getReferenceSystem(){
		return this.referenceSystem;
	}

	/**
	 * 
	 * @param referenceSystem    referenceSystem
	 */
	public void setReferenceSystem(ReferenceSystem referenceSystem){
		this.referenceSystem = referenceSystem;
	}

	public Float getLongitude(){
		return this.longitude;
	}

	/**
	 * 
	 * @param longitude    longitude
	 */
	public void setLongitude(Float longitude){
		this.longitude = longitude;
	}

	public Float getLatitude(){
		return this.latitude;
	}

	/**
	 * 
	 * @param latitude    latitude
	 */
	public void setLatitude(Float latitude){
		this.latitude = latitude;
	}

	public Integer getErrorRadius(){
		return this.errorRadius;
	}

	/**
	 * 
	 * @param errorRadius    errorRadius
	 */
	public void setErrorRadius(Integer errorRadius){
		this.errorRadius = errorRadius;
	}

}