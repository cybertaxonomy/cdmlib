/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.model.reference;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.strategy.cache.reference.ArticleDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.BookDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.BookSectionDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.CdDvdDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.GenericDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.JournalDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.ReferenceBaseDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.ThesisDefaultCacheStrategy;

public class ReferenceFactory {
	private static final Logger logger = Logger.getLogger(ReferenceFactory.class);
	
	
	/**
	 * @return
	 * @deprecated //use static methods instead
	 */
	public static ReferenceFactory newInstance(){
		return new ReferenceFactory();
	}
	
	public static ReferenceBase newArticle(){
		ReferenceBase<ArticleDefaultCacheStrategy> article = new ReferenceBase(ReferenceType.Article);
		article.setCacheStrategy(ReferenceType.Article.getCacheStrategy());
		return article;
	}

	public static ReferenceBase newJournal(){
		ReferenceBase<JournalDefaultCacheStrategy<ReferenceBase>> journal = new ReferenceBase(ReferenceType.Journal);
		journal.setCacheStrategy(ReferenceType.Journal.getCacheStrategy());
		return journal;
	}
	
	public static ReferenceBase newBook(){
		ReferenceBase<BookDefaultCacheStrategy<ReferenceBase>> book = new ReferenceBase<BookDefaultCacheStrategy<ReferenceBase>>(ReferenceType.Book);
		book.setCacheStrategy(ReferenceType.Book.getCacheStrategy());
		return book;
	}
	
	public static ReferenceBase newThesis(){
		ReferenceBase<ThesisDefaultCacheStrategy<ReferenceBase>> thesis = new ReferenceBase<ThesisDefaultCacheStrategy<ReferenceBase>>(ReferenceType.Thesis);
		thesis.setCacheStrategy(ReferenceType.Thesis.getCacheStrategy());
		return thesis;
	}
	
	public static ReferenceBase newInProceedings(){
		ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>> inProceedings = new ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>>(ReferenceType.InProceedings);
		inProceedings.setCacheStrategy(ReferenceType.InProceedings.getCacheStrategy());
		return inProceedings;
	}
	
	public static ReferenceBase newProceedings(){
		ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>> proceedings = new ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>>(ReferenceType.Proceedings);
		proceedings.setCacheStrategy(ReferenceType.Proceedings.getCacheStrategy());
		return proceedings;
	}
	
	public static ReferenceBase newBookSection(){
		ReferenceBase<BookSectionDefaultCacheStrategy<ReferenceBase>> bookSection = new ReferenceBase<BookSectionDefaultCacheStrategy<ReferenceBase>>(ReferenceType.BookSection);
		bookSection.setCacheStrategy(ReferenceType.BookSection.getCacheStrategy());
		return bookSection;
	}
	
	public static ReferenceBase newCdDvd(){
		ReferenceBase<CdDvdDefaultCacheStrategy<ReferenceBase>> cdDvd= new ReferenceBase<CdDvdDefaultCacheStrategy<ReferenceBase>>(ReferenceType.CdDvd);
		cdDvd.setCacheStrategy(ReferenceType.CdDvd.getCacheStrategy());
		return cdDvd;
	}
	
	public static ReferenceBase newGeneric(){
		ReferenceBase generic = new ReferenceBase<GenericDefaultCacheStrategy>(ReferenceType.Generic);
		generic.setCacheStrategy(ReferenceType.Generic.getCacheStrategy());
		return generic;
	}
	
	public static ReferenceBase newMap(){
		ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>> map = new ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>>(ReferenceType.Map);
		map.setCacheStrategy(ReferenceType.Map.getCacheStrategy());
		return map;
		
	}
	
	public static ReferenceBase newReport(){
		ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>> report = new ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>>(ReferenceType.Report);
		report.setCacheStrategy(ReferenceType.Report.getCacheStrategy());
		return report;
		
	}
	
	public static ReferenceBase newWebPage(){
		ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>> webPage = new ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>>(ReferenceType.WebPage);
		webPage.setCacheStrategy(ReferenceType.WebPage.getCacheStrategy());
		return webPage;
	}
	
	public static ReferenceBase newDatabase(){
		ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>> db = new ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>>(ReferenceType.Database);
		db.setCacheStrategy(ReferenceType.Database.getCacheStrategy());
		return db;
	}


	/** 
	 * Creates a new empty print series instance.
	 */
	public static ReferenceBase newPrintSeries() {
		ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>> refBase = new ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>>(ReferenceType.PrintSeries);
		refBase.setCacheStrategy(ReferenceType.PrintSeries.getCacheStrategy());
		return refBase;
	}
	
	/** 
	 * Creates a new print series instance with a given title string.
	 */
	public static ReferenceBase newPrintSeries(String series) {
		ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>> refBase = newPrintSeries();
		refBase.setCacheStrategy(ReferenceType.PrintSeries.getCacheStrategy());
		return refBase;
	}

	public ReferenceBase newBookSection(ReferenceBase book, Person partAuthor,
			String sectionTitle, String pages) {
		ReferenceBase bookSection = newBookSection();
		bookSection.setAuthorTeam(partAuthor);
		bookSection.setTitle(sectionTitle);
		bookSection.setPages(pages);
		return bookSection;
	}

	public static ReferenceBase newArticle(ReferenceBase inJournal, Person partAuthor,
			String title, String pages, String series, String volume, TimePeriod datePublished) {
		ReferenceBase article = newArticle();
		article.setInReference(inJournal);
		article.setAuthorTeam(partAuthor);
		article.setTitle(title);
		article.setPages(pages);
		article.setVolume(volume);
		article.setDatePublished(datePublished);
		return article;
	}

	/**
	 * Returns a new reference for the according reference type. If reference type is <code>null</code>,
	 * <code>null</code> is returned.
	 * @param referenceType
	 * @return
	 */
	public static ReferenceBase newReference(ReferenceType referenceType) {
		if (referenceType == null){
			return null;
		}
		switch(referenceType){
			case Article:
				return newArticle();
			case Journal:
				return newJournal();
			case BookSection:
				return newBookSection();
			case CdDvd:
				return newCdDvd();
			case Database:
				return newDatabase();
			case InProceedings:
				return newInProceedings();
			case Map:
				return newMap();
			case Patent:
				return newPatent();
			case PersonalCommunication:
				return newPersonalCommunication();
			case PrintSeries:
				return newPrintSeries();
			case Proceedings:
				return newProceedings();
			case Report:
				return newReport();
			case Thesis:
				return newThesis();
			case WebPage:
				return newWebPage();
			case Book:
				return newBook();
			case Generic:
				return newGeneric();
			default:
				logger.warn("Unknown reference type " + referenceType.getMessage() + ". Created generic reference instead.");
				return newGeneric();	
		}
	}

	public static ReferenceBase newPersonalCommunication() {
		ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>> personalCommunication = new ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>>(ReferenceType.PersonalCommunication);
		personalCommunication.setCacheStrategy(ReferenceType.PersonalCommunication.getCacheStrategy());
		return personalCommunication;
	}

	public static ReferenceBase newPatent() {
		ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>> patent = new ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>>(ReferenceType.Patent);
		patent.setCacheStrategy(ReferenceType.Patent.getCacheStrategy());
		return patent;
	}
	
	
	
}
