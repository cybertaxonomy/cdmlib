/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.model.reference;

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
	
	public ReferenceBase newArticle(){
		ReferenceBase<ArticleDefaultCacheStrategy> article = new ReferenceBase(ReferenceType.Article);
		article.setCacheStrategy(ReferenceType.Article.getCacheStrategy());
		return article;
	}

	public ReferenceBase newJournal(){
		ReferenceBase<JournalDefaultCacheStrategy<ReferenceBase>> journal = new ReferenceBase(ReferenceType.Journal);
		journal.setCacheStrategy(ReferenceType.Journal.getCacheStrategy());
		return journal;
	}
	
	public ReferenceBase newBook(){
		ReferenceBase<BookDefaultCacheStrategy<ReferenceBase>> book = new ReferenceBase<BookDefaultCacheStrategy<ReferenceBase>>(ReferenceType.Book);
		book.setCacheStrategy(ReferenceType.Book.getCacheStrategy());
		return book;
	}
	
	public ReferenceBase newThesis(){
		ReferenceBase<ThesisDefaultCacheStrategy<ReferenceBase>> thesis = new ReferenceBase<ThesisDefaultCacheStrategy<ReferenceBase>>(ReferenceType.Thesis);
		thesis.setCacheStrategy(ReferenceType.Thesis.getCacheStrategy());
		return thesis;
	}
	
	public ReferenceBase newInProceedings(){
		ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>> inProceedings = new ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>>(ReferenceType.InProceedings);
		inProceedings.setCacheStrategy(ReferenceType.InProceedings.getCacheStrategy());
		return inProceedings;
	}
	
	public ReferenceBase newProceedings(){
		ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>> proceedings = new ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>>(ReferenceType.InProceedings);
		proceedings.setCacheStrategy(ReferenceType.Proceedings.getCacheStrategy());
		return proceedings;
	}
	
	public ReferenceBase newBookSection(){
		ReferenceBase<BookSectionDefaultCacheStrategy<ReferenceBase>> bookSection = new ReferenceBase<BookSectionDefaultCacheStrategy<ReferenceBase>>(ReferenceType.BookSection);
		bookSection.setCacheStrategy(ReferenceType.BookSection.getCacheStrategy());
		return bookSection;
	}
	
	public ReferenceBase newCdDvd(){
		ReferenceBase<CdDvdDefaultCacheStrategy<ReferenceBase>> cdDvd= new ReferenceBase<CdDvdDefaultCacheStrategy<ReferenceBase>>(ReferenceType.CdDvd);
		cdDvd.setCacheStrategy(ReferenceType.CdDvd.getCacheStrategy());
		return cdDvd;
	}
	
	public ReferenceBase newGeneric(){
		ReferenceBase generic = new ReferenceBase<GenericDefaultCacheStrategy>(ReferenceType.Generic);
		generic.setCacheStrategy(ReferenceType.Generic.getCacheStrategy());
		return generic;
	}
	
	public ReferenceBase newMap(){
		ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>> map = new ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>>(ReferenceType.Map);
		map.setCacheStrategy(ReferenceType.Map.getCacheStrategy());
		return map;
		
	}
	
	public ReferenceBase newReport(){
		ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>> report = new ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>>(ReferenceType.Report);
		report.setCacheStrategy(ReferenceType.Report.getCacheStrategy());
		return report;
		
	}
	
	public ReferenceBase newWebPage(){
		ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>> webPage = new ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>>(ReferenceType.WebPage);
		webPage.setCacheStrategy(ReferenceType.WebPage.getCacheStrategy());
		return webPage;
	}
	
	public ReferenceBase newDatabase(){
		ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>> db = new ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>>(ReferenceType.Database);
		db.setCacheStrategy(ReferenceType.Database.getCacheStrategy());
		return db;
	}
	
	public static ReferenceFactory newInstance(){
		return new ReferenceFactory();
	}

	/** 
	 * Creates a new empty print series instance.
	 */
	public ReferenceBase newPrintSeries() {
		ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>> refBase = new ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>>(ReferenceType.PrintSeries);
		refBase.setCacheStrategy(ReferenceType.PrintSeries.getCacheStrategy());
		return refBase;
	}
	
	/** 
	 * Creates a new print series instance with a given title string.
	 */
	public ReferenceBase newPrintSeries(String series) {
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
		return null;
	}

	public ReferenceBase newArticle(ReferenceBase inJournal, Person partAuthor,
			String title, String pages, String series, String volume, TimePeriod datePublished) {
		ReferenceBase article = newArticle();
		article.setInReference(inJournal);
		article.setAuthorTeam(partAuthor);
		article.setTitle(title);
		article.setPages(pages);
		article.setVolume(volume);
		article.setDatePublished(datePublished);
		return null;
	}

	public ReferenceBase newReference(ReferenceType refType) {
		ReferenceBase refBase;
		if (refType ==ReferenceType.Article)
			refBase = newArticle();
		else if (refType == ReferenceType.Journal)
			refBase = newJournal();
		else if (refType == ReferenceType.Generic)
			refBase = newGeneric();
		else if (refType == ReferenceType.BookSection)
			refBase = newBookSection();
		else if (refType == ReferenceType.CdDvd)
			refBase = newCdDvd();
		else if (refType == ReferenceType.Database)
			refBase = newDatabase();
		else if (refType == ReferenceType.InProceedings)
			refBase = newInProceedings();
		else if (refType == ReferenceType.Map)
			refBase = newMap();
		else if (refType == ReferenceType.Patent)
			refBase = newPatent();
		else if (refType == ReferenceType.PersonalCommunication)
			refBase = newPersonalCommunication();
		else if (refType == ReferenceType.PrintSeries)
			refBase = newPrintSeries();
		else if (refType == ReferenceType.Proceedings)
			refBase = newProceedings();
		else if (refType == ReferenceType.Report)
			refBase = newReport();
		else if (refType == ReferenceType.Thesis)
			refBase = newThesis();
		else 
			refBase = newGeneric();
		
		
		return refBase;
	}

	public ReferenceBase newPersonalCommunication() {
		ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>> personalCommunication = new ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>>(ReferenceType.PersonalCommunication);
		personalCommunication.setCacheStrategy(ReferenceType.PersonalCommunication.getCacheStrategy());
		return personalCommunication;
	}

	public ReferenceBase newPatent() {
		ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>> patent = new ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>>(ReferenceType.Patent);
		patent.setCacheStrategy(ReferenceType.Patent.getCacheStrategy());
		return patent;
	}
	
	
	
}
