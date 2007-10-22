/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.publication;


import org.apache.log4j.Logger;
import java.util.*;
import javax.persistence.*;

/**
 * @author Andreas Mueller
 * @version 1.0
 * @created 15-Aug-2007 18:36:00
 */
@Entity
public class Book extends PublicationBase implements NomenclaturalReference {
	static Logger logger = Logger.getLogger(Book.class);

	private String edition;
	private String editor;
	private String isbn;
	private BookSeries inSeries;

	public String getEdition(){
		return edition;
	}

	public String getEditor(){
		return editor;
	}

	public BookSeries getInSeries(){
		return inSeries;
	}

	public String getIsbn(){
		return isbn;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setEdition(String newVal){
		edition = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setEditor(String newVal){
		editor = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setInSeries(BookSeries newVal){
		inSeries = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setIsbn(String newVal){
		isbn = newVal;
	}

}