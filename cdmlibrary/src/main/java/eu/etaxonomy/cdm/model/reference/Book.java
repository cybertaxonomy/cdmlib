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

import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:13
 */
@Entity
public class Book extends PrintedUnitBase implements INomenclaturalReference {
	static Logger logger = Logger.getLogger(Book.class);
	private String edition;
	private String isbn;

	public String getEdition(){
		return this.edition;
	}

	/**
	 * 
	 * @param edition    edition
	 */
	public void setEdition(String edition){
		this.edition = edition;
	}

	public String getIsbn(){
		return this.isbn;
	}

	/**
	 * 
	 * @param isbn    isbn
	 */
	public void setIsbn(String isbn){
		this.isbn = isbn;
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

	@Override
	public String generateTitle(){
		return "";
	}

}