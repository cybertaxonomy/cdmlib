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
import org.apache.log4j.Logger;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:44
 */
@Embeddable
public class Point {
	static Logger logger = Logger.getLogger(Point.class);
	private float longitude;
	private float latitude;
	//in Meters
	private int errorRadius = 0;
	private ReferenceSystem referenceSystem;
	
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

	public float getLongitude(){
		return this.longitude;
	}

	/**
	 * 
	 * @param longitude    longitude
	 */
	public void setLongitude(float longitude){
		this.longitude = longitude;
	}

	public float getLatitude(){
		return this.latitude;
	}

	/**
	 * 
	 * @param latitude    latitude
	 */
	public void setLatitude(float latitude){
		this.latitude = latitude;
	}

	public int getErrorRadius(){
		return this.errorRadius;
	}

	/**
	 * 
	 * @param errorRadius    errorRadius
	 */
	public void setErrorRadius(int errorRadius){
		this.errorRadius = errorRadius;
	}

}