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
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:15:10
 */
public abstract class PrintedUnitBase extends PublicationBase {
	static Logger logger = Logger.getLogger(PrintedUnitBase.class);

	@Description("")
	private String editor;
	@Description("")
	private String volume;
	@Description("")
	private String pages;
	private PrintSeries inSeries;

	public PrintSeries getInSeries(){
		return inSeries;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setInSeries(PrintSeries newVal){
		inSeries = newVal;
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

}