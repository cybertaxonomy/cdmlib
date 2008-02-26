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
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:45
 */
@Entity
public abstract class PrintedUnitBase extends PublicationBase {
	static Logger logger = Logger.getLogger(PrintedUnitBase.class);
	private String editor;
	private String volume;
	private String pages;
	private PrintSeries inSeries;

	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public PrintSeries getInSeries(){
		return this.inSeries;
	}

	/**
	 * 
	 * @param inSeries    inSeries
	 */
	public void setInSeries(PrintSeries inSeries){
		this.inSeries = inSeries;
	}

	public String getEditor(){
		return this.editor;
	}

	/**
	 * 
	 * @param editor    editor
	 */
	public void setEditor(String editor){
		this.editor = editor;
	}

	public String getVolume(){
		return this.volume;
	}

	/**
	 * 
	 * @param volume    volume
	 */
	public void setVolume(String volume){
		this.volume = volume;
	}

	public String getPages(){
		return this.pages;
	}

	/**
	 * 
	 * @param pages    pages
	 */
	public void setPages(String pages){
		this.pages = pages;
	}

}