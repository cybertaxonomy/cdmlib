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
public class BookSection extends PublicationBase implements NomenclaturalReference {
	static Logger logger = Logger.getLogger(BookSection.class);

	private Book inBook;

	public Book getInBook(){
		return inBook;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setInBook(Book newVal){
		inBook = newVal;
	}

}