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
 * @created 08-Nov-2007 13:06:10
 */
@Entity
public class Article extends StrictReferenceBase implements INomenclaturalReference {
	static Logger logger = Logger.getLogger(Article.class);
	private String series;
	private String volume;
	private String pages;
	private Journal inJournal;

	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public Journal getInJournal(){
		return this.inJournal;
	}
	public void setInJournal(Journal inJournal){
		this.inJournal = inJournal;
	}

	public String getSeries(){
		return this.series;
	}

	/**
	 * 
	 * @param series    series
	 */
	public void setSeries(String series){
		this.series = series;
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