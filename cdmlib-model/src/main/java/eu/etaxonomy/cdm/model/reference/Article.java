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

import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.strategy.cache.reference.ArticleDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.BookDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.BookSectionDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.INomenclaturalReferenceCacheStrategy;

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
	private NomenclaturalReferenceHelper nomRefBase = NomenclaturalReferenceHelper.NewInstance(this);


	public static Article NewInstance(){
		Article result = new Article();
		return result;
	}
	
	public static Article NewInstance(Journal inJournal, TeamOrPersonBase authorTeam, String articleTitle, String pages, String series, String volume, TimePeriod datePublished ){
		Article result = new Article();
		result.setInJournal(inJournal);
		result.setTitle(articleTitle);
		result.setPages(pages);
		result.setAuthorTeam(authorTeam);
		result.setSeries(series);
		result.setDatePublished(datePublished);
		result.setVolume(volume);
		return result;
	}
	
	protected Article(){
		super();
		this.cacheStrategy = ArticleDefaultCacheStrategy.NewInstance();
	}	
	


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

}