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
 * This class is to represent all references which cannot be clearly assigned to a
 * specific reference type. Therefore attributes which are only used by a unique
 * reference type are not necessary here.
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:26
 */
@Entity
public class Generic extends StrictReferenceBase implements INomenclaturalReference {
	static Logger logger = Logger.getLogger(Generic.class);
	private String publisher;
	private String placePublished;
	private String editor;
	private String series;
	private String volume;
	private String pages;

	public static Generic NewInstance(){
		return new Generic();
	}
	
	public Generic(){
		super();
	}
	
	
	public String getPublisher(){
		return this.publisher;
	}

	/**
	 * 
	 * @param publisher    publisher
	 */
	public void setPublisher(String publisher){
		this.publisher = publisher;
	}

	public String getPlacePublished(){
		return this.placePublished;
	}

	/**
	 * 
	 * @param placePublished    placePublished
	 */
	public void setPlacePublished(String placePublished){
		this.placePublished = placePublished;
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