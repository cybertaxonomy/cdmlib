/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.occurrence;


import etaxonomy.cdm.model.location.Point;
import etaxonomy.cdm.model.agent.Team;
import etaxonomy.cdm.model.location.NamedArea;
import etaxonomy.cdm.model.common.IdentifiableEntity;
import etaxonomy.cdm.model.common.Media;
import org.apache.log4j.Logger;

/**
 * type figures are observations with at least a figure object in media
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:15:06
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
	private Collection collection;
	private Point exactLocation;
	private NamedArea namedArea;
	private Team collector;

	public Collection getCollection(){
		return collection;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setCollection(Collection newVal){
		collection = newVal;
	}

	public Point getExactLocation(){
		return exactLocation;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setExactLocation(Point newVal){
		exactLocation = newVal;
	}

	public ArrayList getMedia(){
		return media;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setMedia(ArrayList newVal){
		media = newVal;
	}

	public Team getCollector(){
		return collector;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setCollector(Team newVal){
		collector = newVal;
	}

	public NamedArea getNamedArea(){
		return namedArea;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setNamedArea(NamedArea newVal){
		namedArea = newVal;
	}

	public String getLocality(){
		return locality;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setLocality(String newVal){
		locality = newVal;
	}

	public Calendar getEventDate(){
		return eventDate;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setEventDate(Calendar newVal){
		eventDate = newVal;
	}

	public String generateTitle(){
		return "";
	}

}