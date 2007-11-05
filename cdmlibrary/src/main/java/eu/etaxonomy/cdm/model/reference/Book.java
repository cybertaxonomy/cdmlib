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
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:35:58
 */
@Entity
public class Book extends PrintedUnitBase implements INomenclaturalReference {
	static Logger logger = Logger.getLogger(Book.class);

	@Description("")
	private String edition;
	@Description("")
	private String isbn;

	public String getEdition(){
		return edition;
	}

	/**
	 * 
	 * @param edition
	 */
	public void setEdition(String edition){
		;
	}

	public String getIsbn(){
		return isbn;
	}

	/**
	 * 
	 * @param isbn
	 */
	public void setIsbn(String isbn){
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

	@Override
	public String generateTitle() {
		// TODO Auto-generated method stub
		return null;
	}

}