/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.location;
import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:44
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Point", propOrder = {
    "longitude",
    "latitude",
    "errorRadius",
    "referenceSystem"
})
@XmlRootElement(name = "Point")
@Embeddable
public class Point implements Cloneable, Serializable {
	private static final long serialVersionUID = 531030660792800636L;
	private static final Logger logger = Logger.getLogger(Point.class);
	
	//TODO was Float but H2 threw errors
	@XmlElement(name = "Longitude")
	private Double longitude;
	
	@XmlElement(name = "Latitude")
	private Double latitude;
	
	//in Meters
	@XmlElement(name = "ErrorRadius")
	private Integer errorRadius = 0;
	
	@XmlElement(name = "ReferenceSystem")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	private ReferenceSystem referenceSystem;
	
	/**
	 * Factory method
	 * @return
	 */
	public static Point NewInstance(){
		return new Point();
	}
	
	/**
	 * Factory method
	 * @return
	 */
	public static Point NewInstance(Double longitude, Double latitude, ReferenceSystem referenceSystem, Integer errorRadius){
		Point result = new Point();
		result.setLongitude(longitude);
		result.setLatitude(latitude);
		result.setReferenceSystem(referenceSystem);
		result.setErrorRadius(errorRadius);
		return result;
	}
	
	/**
	 * Constructor
	 */
	public Point() {
	}
	
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

	public Double getLongitude(){
		return this.longitude;
	}

	/**
	 * 
	 * @param longitude    longitude
	 */
	public void setLongitude(Double longitude){
		this.longitude = longitude;
	}

	public Double getLatitude(){
		return this.latitude;
	}

	/**
	 * 
	 * @param latitude    latitude
	 */
	public void setLatitude(Double latitude){
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
	
	
//*********** CLONE **********************************/	
	
	/** 
	 * Clones <i>this</i> point. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i> point
	 * by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link DerivedUnitBase DerivedUnitBase}.
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Point clone(){
		try{
			Point result = (Point)super.clone();
			result.setReferenceSystem(this.referenceSystem);
			//no changes to: errorRadius, latitude, longitude
			return result;
		} catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}


}