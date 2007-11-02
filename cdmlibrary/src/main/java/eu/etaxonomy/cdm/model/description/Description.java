/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:18:08
 */
public class Description extends IdentifiableEntity {
	static Logger logger = Logger.getLogger(Description.class);

	//in 95% of all cases this will be the taxon name. getLabel() should return the taxon name in case label is null.
	@Description("in 95% of all cases this will be the taxon name. getLabel() should return the taxon name in case label is null.")
	private String label;
	private ArrayList features;
	private ArrayList scopes;
	private ArrayList sources;
	private ArrayList geoScopes;

	public ArrayList getSources(){
		return sources;
	}

	/**
	 * 
	 * @param sources
	 */
	public void setSources(ArrayList sources){
		;
	}

	public ArrayList getGeoScopes(){
		return geoScopes;
	}

	/**
	 * 
	 * @param geoScopes
	 */
	public void setGeoScopes(ArrayList geoScopes){
		;
	}

	public ArrayList getScopes(){
		return scopes;
	}

	/**
	 * 
	 * @param scopes
	 */
	public void setScopes(ArrayList scopes){
		;
	}

	public ArrayList getFeatures(){
		return features;
	}

	/**
	 * 
	 * @param features
	 */
	public void setFeatures(ArrayList features){
		;
	}

	public String getLabel(){
		return label;
	}

	/**
	 * 
	 * @param label
	 */
	public void setLabel(String label){
		;
	}

}