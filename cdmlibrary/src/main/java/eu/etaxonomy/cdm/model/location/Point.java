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

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:43:36
 */
public class Point extends VersionableEntity {
	static Logger logger = Logger.getLogger(Point.class);

	@Description("")
	private float longitude;
	@Description("")
	private float latitude;
	//in Meters
	@Description("in Meters")
	private int errorRadius;
	private ReferenceSystem referenceSystem;

	public ReferenceSystem getReferenceSystem(){
		return referenceSystem;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setReferenceSystem(ReferenceSystem newVal){
		referenceSystem = newVal;
	}

	public float getLongitude(){
		return longitude;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setLongitude(float newVal){
		longitude = newVal;
	}

	public float getLatitude(){
		return latitude;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setLatitude(float newVal){
		latitude = newVal;
	}

	public int getErrorRadius(){
		return errorRadius;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setErrorRadius(int newVal){
		errorRadius = newVal;
	}

}