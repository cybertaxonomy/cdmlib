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
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.Media;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * type figures are observations with at least a figure object in media
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:41
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public class Occurrence extends IdentifiableEntity {
	static Logger logger = Logger.getLogger(Occurrence.class);
	//Locality name (as free text) where this occurrence happened
	private String locality;
	//Date on which this occurrence happened
	private Calendar eventDate;
	private ArrayList media;
	private Point exactLocation;
	private NamedArea namedArea;
	private Team collector;
	private Collection collection;

	public Collection getCollection(){
		return this.collection;
	}

	/**
	 * 
	 * @param collection    collection
	 */
	public void setCollection(Collection collection){
		this.collection = collection;
	}

	public Point getExactLocation(){
		return this.exactLocation;
	}

	/**
	 * 
	 * @param exactLocation    exactLocation
	 */
	public void setExactLocation(Point exactLocation){
		this.exactLocation = exactLocation;
	}

	public ArrayList getMedia(){
		return this.media;
	}

	/**
	 * 
	 * @param media    media
	 */
	public void setMedia(ArrayList media){
		this.media = media;
	}

	public Team getCollector(){
		return this.collector;
	}

	/**
	 * 
	 * @param collector    collector
	 */
	public void setCollector(Team collector){
		this.collector = collector;
	}

	public NamedArea getNamedArea(){
		return this.namedArea;
	}

	/**
	 * 
	 * @param namedArea    namedArea
	 */
	public void setNamedArea(NamedArea namedArea){
		this.namedArea = namedArea;
	}

	public String getLocality(){
		return this.locality;
	}

	/**
	 * 
	 * @param locality    locality
	 */
	public void setLocality(String locality){
		this.locality = locality;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Calendar getEventDate(){
		return this.eventDate;
	}

	/**
	 * 
	 * @param eventDate    eventDate
	 */
	public void setEventDate(Calendar eventDate){
		this.eventDate = eventDate;
	}

	public String generateTitle(){
		return "";
	}

}