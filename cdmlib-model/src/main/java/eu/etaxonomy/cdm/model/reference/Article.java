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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;
import org.springframework.beans.factory.annotation.Configurable;

import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.strategy.cache.reference.ArticleDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.INomenclaturalReferenceCacheStrategy;


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
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Article", propOrder = {
//		"volume",
//		"pages",
//		"series",
//		"inJournal"
})
@XmlRootElement(name = "Article")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.reference.ReferenceBase")
@Audited
@Configurable
@Deprecated
public class Article extends ReferenceBase<INomenclaturalReferenceCacheStrategy<Article>> implements INomenclaturalReference, IVolumeReference, Cloneable {
	private static final long serialVersionUID = -1528079480114388117L;
	private static final Logger logger = Logger.getLogger(Article.class);
	
//    @XmlElement(name = "Series")
//    @Field(index=Index.TOKENIZED)
//	private String series;
//	
//    @XmlElement(name = "Volume")
//    @Field(index=Index.TOKENIZED)
//	private String volume;
//	
//    @XmlElement(name = "Pages")
//    @Field(index=Index.TOKENIZED)
//	private String pages;
//	
//    @XmlElement(name = "InJournal")
//    @XmlIDREF
//    @XmlSchemaType(name = "IDREF")
//    @ManyToOne(fetch = FetchType.LAZY)
//    @IndexedEmbedded
//    @Cascade(CascadeType.SAVE_UPDATE)
//	private Journal inJournal;


	/** 
	 * Class constructor: creates a new empty article instance
	 * only containing the {@link eu.etaxonomy.cdm.strategy.cache.reference.ArticleDefaultCacheStrategy default cache strategy}.
	 * 
	 * @see eu.etaxonomy.cdm.strategy.cache.reference.ArticleDefaultCacheStrategy
	 */
	protected Article(){
		super();
		this.type = ReferenceType.Article;
		this.cacheStrategy = ArticleDefaultCacheStrategy.NewInstance();
	}	
	
	/** 
	 * Creates a new empty article instance
	 * only containing the {@link eu.etaxonomy.cdm.strategy.cache.reference.ArticleDefaultCacheStrategy default cache strategy}.
	 * 
	 * @see #Article()
	 * @see #NewInstance(Journal, TeamOrPersonBase, String, String, String, String, TimePeriod)
	 * @see eu.etaxonomy.cdm.strategy.cache.reference.ArticleDefaultCacheStrategy
	 */
	public static Article NewInstance(){
		Article result = new Article();
		return result;
	}
	
	/** 
	 * Creates a new article instance with the given values and with the
	 * {@link eu.etaxonomy.cdm.strategy.cache.reference.ArticleDefaultCacheStrategy default cache strategy}.
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
	 * @see 					TeamOrPersonBase
	 * @see 					TimePeriod
	 * @see 					eu.etaxonomy.cdm.strategy.cache.reference.ArticleDefaultCacheStrategy
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
	@Transient
	public Journal getInJournal(){
		if (inReference == null){
			return null;
		}
		if (! this.inReference.isInstanceOf(Journal.class)){
			throw new IllegalStateException("The in-reference of an article may only be of type Journal");
		}
		return CdmBase.deproxy(this.inReference,Journal.class);
	}
	
	/**
	 * @see #getInJournal()
	 */
	public void setInJournal(Journal inJournal){
		this.inReference = inJournal;
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
	 *//*
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
	 * This method overrides the generic and inherited getCitation method
	 * from {@link StrictReferenceBase StrictReferenceBase}.
	 * 
	 * @see  #getNomenclaturalCitation(String)
	 * @see  StrictReferenceBase#getCitation()
	 */
//	@Override
//	@Transient
//	public String getCitation(){
//		return nomRefBase.getCitation();
//	}

	/**
	 * Returns a formatted string containing the entire citation used for
	 * nomenclatural purposes based on <i>this</i> article - including
	 * (abbreviated) title of the journal but not authors of the article -
	 * and on the given details.
	 * 
	 * @param  microReference	the string with the details (generally pages)
	 * 							within the journal
	 * @return					the formatted string representing the
	 * 							nomenclatural citation
	 * 
	 * @see  					#getCitation()
	 */
	@Transient
	public String getNomenclaturalCitation(String microReference) {
		if (cacheStrategy == null){
			logger.warn("No CacheStrategy defined for "+ this.getClass() + ": " + this.getUuid());
			return null;
		}else{
			return cacheStrategy.getNomenclaturalCitation(this,microReference);
		}
	}


	/**
	 * Generates, according to the {@link eu.etaxonomy.cdm.strategy.cache.reference.ArticleDefaultCacheStrategy default cache strategy}
	 * assigned to <i>this</i> article, a string that identifies <i>this</i>
	 * article and returns it. This string may be stored in the inherited
	 * {@link eu.etaxonomy.cdm.model.common.IdentifiableEntity#getTitleCache() titleCache} attribute.<BR>
	 * This method overrides the generic and inherited generateTitle method
	 * from {@link ReferenceBase ReferenceBase}.
	 *
	 * @return  the string identifying <i>this</i> article
	 * @see  	#getCitation()
	 * @see  	eu.etaxonomy.cdm.model.common.IdentifiableEntity#getTitleCache()
	 * @see  	eu.etaxonomy.cdm.model.common.IdentifiableEntity#generateTitle()
	 */
//	@Override
//	public String generateTitle(){
//		return nomRefBase.generateTitle();
//	}
	

	
	
	/** 
	 * Clones <i>this</i> article. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i> article
	 * by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link StrictReferenceBase StrictReferenceBase}.
	 * 
	 * @see StrictReferenceBase#clone()
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Article clone(){
		Article result = (Article)super.clone();
		result.cacheStrategy = ArticleDefaultCacheStrategy.NewInstance();
		//no changes to: inJournal, pages, series, volume
		return result;
	}
	

}