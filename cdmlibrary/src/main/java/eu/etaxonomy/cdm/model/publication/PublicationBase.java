/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.publication;


import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.NameAlias;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import org.apache.log4j.Logger;
import java.util.*;
import javax.persistence.*;

/**
 * A year() method is required to get the year of publication out of the
 * datePublished field
 * @author Andreas Mueller
 * @version 1.0
 * @created 15-Aug-2007 18:36:11
 */
@Entity
public abstract class PublicationBase extends IdentifiableEntity {
	static Logger logger = Logger.getLogger(PublicationBase.class);

	private String datePublished;
	private String doi;
	private String pages;
	private String placePublished;
	private String publisher;
	private String title;
	private String url;
	private ArrayList otherTitles;
	private Team authorship;

	public Team getAuthorship(){
		return authorship;
	}

	public String getDatePublished(){
		return datePublished;
	}

	public String getDoi(){
		return doi;
	}

	public ArrayList getOtherTitles(){
		return otherTitles;
	}

	public String getPages(){
		return pages;
	}

	public String getPlacePublished(){
		return placePublished;
	}

	public String getPublisher(){
		return publisher;
	}

	public String getTitle(){
		return title;
	}

	public String getUrl(){
		return url;
	}

	/**
	 * 
	 * @param datePublished
	 */
	@Transient
	public int getYear(String datePublished){
		return 0;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setAuthorship(Team newVal){
		authorship = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setDatePublished(String newVal){
		datePublished = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setDoi(String newVal){
		doi = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setOtherTitles(ArrayList newVal){
		otherTitles = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setPages(String newVal){
		pages = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setPlacePublished(String newVal){
		placePublished = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setPublisher(String newVal){
		publisher = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setTitle(String newVal){
		title = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setUrl(String newVal){
		url = newVal;
	}

}