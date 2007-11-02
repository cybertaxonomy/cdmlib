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
 * @created 02-Nov-2007 19:18:32
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
	 * @param inSeries
	 */
	public void setInSeries(PrintSeries inSeries){
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

}