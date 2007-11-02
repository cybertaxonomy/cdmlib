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
 * @created 02-Nov-2007 19:18:31
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
	 * @param referenceSystem
	 */
	public void setReferenceSystem(ReferenceSystem referenceSystem){
		;
	}

	public float getLongitude(){
		return longitude;
	}

	/**
	 * 
	 * @param longitude
	 */
	public void setLongitude(float longitude){
		;
	}

	public float getLatitude(){
		return latitude;
	}

	/**
	 * 
	 * @param latitude
	 */
	public void setLatitude(float latitude){
		;
	}

	public int getErrorRadius(){
		return errorRadius;
	}

	/**
	 * 
	 * @param errorRadius
	 */
	public void setErrorRadius(int errorRadius){
		;
	}

}