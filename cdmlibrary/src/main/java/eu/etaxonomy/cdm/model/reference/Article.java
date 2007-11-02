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

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:43:06
 */
public class Article extends StrictReferenceBase implements INomenclaturalReference {
	static Logger logger = Logger.getLogger(Article.class);

	@Description("")
	private String series;
	@Description("")
	private String volume;
	@Description("")
	private String pages;
	private Journal inJournal;

	public Journal getInJournal(){
		return inJournal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setInJournal(Journal newVal){
		inJournal = newVal;
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