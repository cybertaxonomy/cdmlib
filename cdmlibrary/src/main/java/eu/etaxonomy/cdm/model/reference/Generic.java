/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;


import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * This class is to represent all references which cannot be clearly assigned to a
 * specific reference type. Therefore attributes which are only used by a unique
 * reference type are not necessary here.
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:08
 */
@Entity
public class Generic extends StrictReferenceBase implements INomenclaturalReference {
	static Logger logger = Logger.getLogger(Generic.class);

	@Description("")
	private String publisher;
	@Description("")
	private String placePublished;
	@Description("")
	private String editor;
	@Description("")
	private String series;
	@Description("")
	private String volume;
	@Description("")
	private String pages;

	public String getPublisher(){
		return publisher;
	}

	/**
	 * 
	 * @param publisher
	 */
	public void setPublisher(String publisher){
		;
	}

	public String getPlacePublished(){
		return placePublished;
	}

	/**
	 * 
	 * @param placePublished
	 */
	public void setPlacePublished(String placePublished){
		;
	}

	public String getEditor(){
		return editor;
	}

	/**
	 * 
	 * @param editor
	 */
	public void setEditor(String editor){
		;
	}

	public String getSeries(){
		return series;
	}

	/**
	 * 
	 * @param series
	 */
	public void setSeries(String series){
		;
	}

	public String getVolume(){
		return volume;
	}

	/**
	 * 
	 * @param volume
	 */
	public void setVolume(String volume){
		;
	}

	public String getPages(){
		return pages;
	}

	/**
	 * 
	 * @param pages
	 */
	public void setPages(String pages){
		;
	}

	/**
	 * returns a formatted string containing the entire reference citation including
	 * authors
	 */
	@Transient
	public String getCitation(){
		return "";
	}

	/**
	 * returns a formatted string containing the reference citation excluding authors
	 * as used in a taxon name
	 */
	@Transient
	public String getNomenclaturalCitation(){
		return "";
	}

	@Transient
	public String getYear(){
		return "";
	}

}