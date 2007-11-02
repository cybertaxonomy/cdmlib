/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.occurrence;


import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.common.Media;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import org.apache.log4j.Logger;

/**
 * type figures are observations with at least a figure object in media
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:18:28
 */
public class Occurrence extends IdentifiableEntity {
	static Logger logger = Logger.getLogger(Occurrence.class);

	//Locality name (as free text) where this occurrence happened 
	@Description("Locality name (as free text) where this occurrence happened ")
	private String locality;
	//Date on which this occurrence happened 
	@Description("Date on which this occurrence happened ")
	private Calendar eventDate;
	private ArrayList media;
	private Point exactLocation;
	private NamedArea namedArea;
	private Team collector;
	private Collection collection;

	public Collection getCollection(){
		return collection;
	}

	/**
	 * 
	 * @param collection
	 */
	public void setCollection(Collection collection){
		;
	}

	public Point getExactLocation(){
		return exactLocation;
	}

	/**
	 * 
	 * @param exactLocation
	 */
	public void setExactLocation(Point exactLocation){
		;
	}

	public ArrayList getMedia(){
		return media;
	}

	/**
	 * 
	 * @param media
	 */
	public void setMedia(ArrayList media){
		;
	}

	public Team getCollector(){
		return collector;
	}

	/**
	 * 
	 * @param collector
	 */
	public void setCollector(Team collector){
		;
	}

	public NamedArea getNamedArea(){
		return namedArea;
	}

	/**
	 * 
	 * @param namedArea
	 */
	public void setNamedArea(NamedArea namedArea){
		;
	}

	public String getLocality(){
		return locality;
	}

	/**
	 * 
	 * @param locality
	 */
	public void setLocality(String locality){
		;
	}

	public Calendar getEventDate(){
		return eventDate;
	}

	/**
	 * 
	 * @param eventDate
	 */
	public void setEventDate(Calendar eventDate){
		;
	}

	public String generateTitle(){
		return "";
	}

}