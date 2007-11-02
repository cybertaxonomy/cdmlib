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
 * A year() method is required to get the year of publication out of the
 * datePublished field
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:18:41
 */
public abstract class StrictReferenceBase extends ReferenceBase {
	static Logger logger = Logger.getLogger(StrictReferenceBase.class);

	//Title of the reference
	@Description("Title of the reference")
	private String title;
	//The date range assigned to the reference.
	//ISO Date range like. Flexible, year can be left out, etc
	@Description("The date range assigned to the reference.
	ISO Date range like. Flexible, year can be left out, etc")
	private DateRange datePublished;

	public String getTitle(){
		return title;
	}

	/**
	 * 
	 * @param title
	 */
	public void setTitle(String title){
		;
	}

	public DateRange getDatePublished(){
		return datePublished;
	}

	/**
	 * 
	 * @param datePublished
	 */
	public void setDatePublished(DateRange datePublished){
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

	@Transient
	public int getYear(){
		return 0;
	}

}