/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.specimen;


import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import org.apache.log4j.Logger;
import java.util.*;
import javax.persistence.*;

/**
 * @author Andreas Mueller
 * @version 1.0
 * @created 15-Aug-2007 18:36:13
 */
@Entity
public class Specimen extends IdentifiableEntity {
	static Logger logger = Logger.getLogger(Specimen.class);

	private String citation;
	private Calendar collectingDate;
	private String coordinates;
	//Elevation in meter above sea level
	private int elevation;
	private float latitude;
	private float longitude;
	private Locality locality;
	private MaterialCategory material;
	private Team collector;
	private Collection collection;

	public String getCitation(){
		return citation;
	}

	public Calendar getCollectingDate(){
		return collectingDate;
	}

	public Collection getCollection(){
		return collection;
	}

	public Team getCollector(){
		return collector;
	}

	public String getCoordinates(){
		return coordinates;
	}

	public int getElevation(){
		return elevation;
	}

	public float getLatitude(){
		return latitude;
	}

	public Locality getLocality(){
		return locality;
	}

	public float getLongitude(){
		return longitude;
	}

	public MaterialCategory getMaterial(){
		return material;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setCitation(String newVal){
		citation = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setCollectingDate(Calendar newVal){
		collectingDate = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setCollection(Collection newVal){
		collection = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setCollector(Team newVal){
		collector = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setCoordinates(String newVal){
		coordinates = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setElevation(int newVal){
		elevation = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setLatitude(float newVal){
		latitude = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setLocality(Locality newVal){
		locality = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setLongitude(float newVal){
		longitude = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setMaterial(MaterialCategory newVal){
		material = newVal;
	}

}