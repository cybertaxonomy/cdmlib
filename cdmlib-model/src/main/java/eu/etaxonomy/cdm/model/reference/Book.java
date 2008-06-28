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
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:13
 */
@Entity
public class Book extends PrintedUnitBase implements INomenclaturalReference {
	private static final Logger logger = Logger.getLogger(Book.class);
	private String edition;
	private String isbn;


	public static Book NewInstance(){
		return new Book();
	}
	
	
	public String getEdition(){
		return this.edition;
	}
	public void setEdition(String edition){
		this.edition = edition;
	}

	public String getIsbn(){
		return this.isbn;
	}
	public void setIsbn(String isbn){
		this.isbn = isbn;
	}

	/**
	 * returns a formatted string containing the entire reference citation including
	 * authors
	 */
	@Transient
	public String getCitation(){
		//TODO
		logger.warn("Not yet fully implemented");
		return "";
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.reference.INomenclaturalReference#getNomenclaturalCitation(java.lang.String)
	 */
	@Transient
	public String getNomenclaturalCitation(String microReference) {
		String result = getTokenizedFullNomenclaturalTitel();
		result = result.replaceAll(MICRO_REFERENCE_TOKEN, microReference);
		return result;
	}


	@Override
	public String generateTitle(){
		//TODO
		logger.warn("Not yet fully implemented");
		return "";
	}
	
	private String getTokenizedFullNomenclaturalTitel() {
		//TODO
		logger.warn("Not yet fully implemented");
		return this.getTitleCache() +  MICRO_REFERENCE_TOKEN;
	}
	
	private String setTokenizedFullNomenclaturalTitel(String tokenizedFullNomenclaturalTitel) {
		//TODO
		logger.warn("Not yet fully implemented");
		return this.getTitleCache() +  MICRO_REFERENCE_TOKEN;
	}

}