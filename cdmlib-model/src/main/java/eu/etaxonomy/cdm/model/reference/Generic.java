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
import javax.persistence.Transient;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.strategy.cache.reference.GenericDefaultCacheStrategy;

/**
 * This class is to represent all references which cannot be clearly assigned to a
 * specific reference type. Therefore attributes which are only used by a unique
 * reference type are not necessary here.
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:26
 */
@Entity
public class Generic extends StrictReferenceBase implements INomenclaturalReference, Cloneable {
	static Logger logger = Logger.getLogger(Generic.class);
	private String publisher;
	private String placePublished;
	private String editor;
	private String series;
	private String volume;
	private String pages;
	private NomenclaturalReferenceHelper nomRefBase = NomenclaturalReferenceHelper.NewInstance(this);

	
	public static Generic NewInstance(){
		return new Generic();
	}
	
	public Generic(){
		super();
		this.cacheStrategy = GenericDefaultCacheStrategy.NewInstance();
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

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.reference.StrictReferenceBase#getCitation()
	 */
	@Transient
	public String getCitation(){
		return nomRefBase.getCitation();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.reference.INomenclaturalReference#getNomenclaturalCitation(java.lang.String)
	 */
	@Transient
	public String getNomenclaturalCitation(String microReference) {
		return nomRefBase.getNomenclaturalCitation(microReference);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.reference.ReferenceBase#generateTitle()
	 */
	@Override
	public String generateTitle(){
		return nomRefBase.generateTitle();
	}
	
//*********** CLONE **********************************/	
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.reference.StrictReferenceBase#clone()
	 */
	public Generic clone(){
		Generic result = (Generic)super.clone();
		//no changes to: editor, pages, placePublished,publisher, series, volume
		return result;
	}

}