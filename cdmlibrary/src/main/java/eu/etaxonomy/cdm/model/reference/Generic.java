/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.reference;


import org.apache.log4j.Logger;

/**
 * This class is to represent all references which cannot be clearly assigned to a
 * specific reference type. Therefore attributes which are only used by a unique
 * reference type are not necessary here.
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:14:50
 */
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
	 * @param newVal
	 */
	public void setPublisher(String newVal){
		publisher = newVal;
	}

	public String getPlacePublished(){
		return placePublished;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setPlacePublished(String newVal){
		placePublished = newVal;
	}

	public String getEditor(){
		return editor;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setEditor(String newVal){
		editor = newVal;
	}

	public String getSeries(){
		return series;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setSeries(String newVal){
		series = newVal;
	}

	public String getVolume(){
		return volume;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setVolume(String newVal){
		volume = newVal;
	}

	public String getPages(){
		return pages;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setPages(String newVal){
		pages = newVal;
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