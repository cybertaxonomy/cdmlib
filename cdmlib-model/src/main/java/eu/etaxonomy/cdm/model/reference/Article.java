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


/**
 * This class represents articles in a {@link Journal journal}. An article is an independent
 * piece of prose written by an {@link TeamOrPersonBase author (team)} which is published among
 * other articles within a particular issue of a journal.
 * <P>
 * This class corresponds, according to the TDWG ontology, to the publication type
 * terms (from PublicationTypeTerm): <ul>
 * <li> "JournalArticle"
 * <li> "NewspaperArticle"
 * <li> "MagazineArticle"
 * </ul>
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:10
 */
@Entity
public class Article extends StrictReferenceBase implements INomenclaturalReference, Cloneable {
	static Logger logger = Logger.getLogger(Article.class);
	private String series;
	private String volume;
	private String pages;
	private Journal inJournal;
	private NomenclaturalReferenceHelper nomRefBase = NomenclaturalReferenceHelper.NewInstance(this);


	/** 
	 * Class constructor: creates a new empty article instance
	 * only containing the {@link strategy.cache.reference.ArticleDefaultCacheStrategy default cache strategy}.
	 * 
	 * @see strategy.cache.reference.ArticleDefaultCacheStrategy
	 */
	protected Article(){
		super();
		this.cacheStrategy = ArticleDefaultCacheStrategy.NewInstance();
	}	
	
	/** 
	 * Creates a new empty article instance
	 * only containing the {@link strategy.cache.reference.ArticleDefaultCacheStrategy default cache strategy}.
	 * 
	 * @see #Article()
	 * @see #NewInstance(Journal, TeamOrPersonBase, String, String, String, String, TimePeriod)
	 * @see strategy.cache.reference.ArticleDefaultCacheStrategy
	 */
	public static Article NewInstance(){
		Article result = new Article();
		return result;
	}
	
	/** 
	 * Creates a new article instance with the given values and with the
	 * {@link strategy.cache.reference.ArticleDefaultCacheStrategy default cache strategy}.
	 * 
	 * @param	inJournal		the journal in which <i>this</i> article has
	 * 							been published 
	 * @param	authorTeam		the team or person who wrote <i>this</i> article
	 * @param	articleTitle	the string representing the title of <i>this</i>
	 * 							article
	 * @param	pages			the string representing the pages in the journal
	 * 							issue where <i>this</i> article can be found  
	 * @param	series			the string representing the series (within the
	 * 							journal) in which <i>this</i> article has been 
	 * 							published
	 * @param	volume			the string representing the volume of the journal
	 * 							in which <i>this</i> article has been published
	 * @param	datePublished	the date (time period) in which <i>this</i>
	 * 							article has been published
	 * @see 					#NewInstance()
	 * @see 					Journal
	 * @see 					agent.TeamOrPersonBase
	 * @see 					common.TimePeriod
	 * @see 					strategy.cache.reference.ArticleDefaultCacheStrategy
	 */
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
	


	/**
	 * Returns the {@link Journal journal} in which <i>this</i> article has been published.
	 * 
	 * @return  the journal
	 * @see 	Journal
	 */
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public Journal getInJournal(){
		return this.inJournal;
	}
	/**
	 * @see #getInJournal()
	 */
	public void setInJournal(Journal inJournal){
		this.inJournal = inJournal;
	}

	/**
	 * Returns the string representing the series (within the journal) in which
	 * <i>this</i> article was published.
	 * 
	 * @return  the string identifying the series
	 */
	public String getSeries(){
		return this.series;
	}

	/**
	 * @see #getSeries()
	 */
	public void setSeries(String series){
		this.series = series;
	}

	/**
	 * Returns the string representing the volume of the journal in which
	 * <i>this</i> article was published.
	 * 
	 * @return  the string identifying the series
	 */
	public String getVolume(){
		return this.volume;
	}

	/**
	 * @see #getVolume()
	 */
	public void setVolume(String volume){
		this.volume = volume;
	}

	/**
	 * Returns the string representing the page(s) where the content of
	 * <i>this</i> article is located within the journal issue.
	 * 
	 * @return  the string with the pages corresponding to <i>this</i> article
	 */
	public String getPages(){
		return this.pages;
	}

	/**
	 * @see #getPages()
	 */
	public void setPages(String pages){
		this.pages = pages;
	}


	/**
	 * Returns a formatted string containing the entire reference citation,
	 * including authors, title, journal, pages, corresponding to <i>this</i>
	 * article.<BR>
	 * This method overrides the generic and inherited
	 * StrictReferenceBase#getCitation() method.
	 * 
	 * @see  NomenclaturalReferenceHelper#getCitation()
	 * @see  StrictReferenceBase#getCitation()
	 */
	@Transient
	public String getCitation(){
		return nomRefBase.getCitation();
	}

	/**
	 * Returns a formatted string containing the entire citation used for
	 * nomenclatural purposes based on <i>this</i> article - including
	 * (abbreviated) title of the journal but not authors -
	 * and on the given details.
	 * 
	 * @param  microReference	the string with the details (generally pages)
	 * 							within t<i>this</i> generic reference
	 * @return					the formatted string representing the
	 * 							nomenclatural citation
	 * @see  					NomenclaturalReferenceHelper#getNomenclaturalCitation(String)
	 * @see  					INomenclaturalReference#getNomenclaturalCitation(String)
	 */
	@Transient
	public String getNomenclaturalCitation(String microReference) {
		return nomRefBase.getNomenclaturalCitation(microReference);
	}


	/**
	 * Generates, according to the {@link strategy.cache.reference.ArticleDefaultCacheStrategy default cache strategy}
	 * assigned to <i>this</i> article, a string that identifies <i>this</i>
	 * article and returns it. This string may be stored in the inherited
	 * {@link common.IdentifiableEntity#getTitleCache() titleCache} attribute.<BR>
	 * This method overrides the generic and inherited
	 * ReferenceBase#generateTitle() method.
	 *
	 * @return  the string identifying <i>this</i> article
	 * @see  	#getCitation()
	 * @see  	NomenclaturalReferenceHelper#generateTitle()
	 * @see  	common.IdentifiableEntity#getTitleCache()
	 * @see  	common.IdentifiableEntity#generateTitle()
	 */
	@Override
	public String generateTitle(){
		return nomRefBase.generateTitle();
	}
	
//*********** CLONE **********************************/	

	
	/** 
	 * Clones <i>this</i> article. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i> article
	 * by modifying only some of the attributes.<BR>
	 * This method overrides the {@link StrictReferenceBase#clone() method} from StrictReferenceBase.
	 * 
	 * @see StrictReferenceBase#clone()
	 * @see media.IdentifyableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	public Article clone(){
		Article result = (Article)super.clone();
		//no changes to: inJournal, pages, series, volume
		return result;
	}
	

}