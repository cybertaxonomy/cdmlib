/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;


import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:14
 */
@Entity
public class BookSection extends SectionBase implements INomenclaturalReference {
	static Logger logger = Logger.getLogger(BookSection.class);
	private Book inBook;

	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public Book getInBook(){
		return this.inBook;
	}

	/**
	 * 
	 * @param inBook    inBook
	 */
	public void setInBook(Book inBook){
		this.inBook = inBook;
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