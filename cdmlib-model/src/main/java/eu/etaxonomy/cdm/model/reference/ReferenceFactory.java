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
		article.setCacheStrategy(ArticleDefaultCacheStrategy.NewInstance());
		return article;
	}

	public ReferenceBase newJournal(){
		ReferenceBase<JournalDefaultCacheStrategy<ReferenceBase>> journal = new ReferenceBase(ReferenceType.Journal);
		journal.setCacheStrategy(JournalDefaultCacheStrategy.NewInstance());
		return journal;
	}
	
	public ReferenceBase newBook(){
		ReferenceBase<BookDefaultCacheStrategy<ReferenceBase>> book = new ReferenceBase<BookDefaultCacheStrategy<ReferenceBase>>(ReferenceType.Book);
		book.setCacheStrategy(BookDefaultCacheStrategy.NewInstance());
		return book;
	}
	
	public ReferenceBase newThesis(){
		ReferenceBase<ThesisDefaultCacheStrategy<ReferenceBase>> thesis = new ReferenceBase<ThesisDefaultCacheStrategy<ReferenceBase>>(ReferenceType.Thesis);
		thesis.setCacheStrategy(ThesisDefaultCacheStrategy.NewInstance());
		return thesis;
	}
	
	public ReferenceBase newInProceedings(){
		ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>> inProceedings = new ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>>(ReferenceType.InProceedings);
		inProceedings.setCacheStrategy(ReferenceBaseDefaultCacheStrategy.NewInstance());
		return inProceedings;
	}
	
	public ReferenceBase newProceedings(){
		ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>> proceedings = new ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>>(ReferenceType.InProceedings);
		proceedings.setCacheStrategy(ReferenceBaseDefaultCacheStrategy.NewInstance());
		return proceedings;
	}
	
	public ReferenceBase newBookSection(){
		ReferenceBase<BookSectionDefaultCacheStrategy<ReferenceBase>> bookSection = new ReferenceBase<BookSectionDefaultCacheStrategy<ReferenceBase>>(ReferenceType.BookSection);
		bookSection.setCacheStrategy(BookSectionDefaultCacheStrategy.NewInstance());
		return bookSection;
	}
	
	public ReferenceBase newCdDvd(){
		ReferenceBase<CdDvdDefaultCacheStrategy<ReferenceBase>> cdDvd= new ReferenceBase<CdDvdDefaultCacheStrategy<ReferenceBase>>(ReferenceType.CdDvd);
		cdDvd.setCacheStrategy(CdDvdDefaultCacheStrategy.NewInstance());
		return cdDvd;
	}
	
	public ReferenceBase newGeneric(){
		ReferenceBase generic = new ReferenceBase<GenericDefaultCacheStrategy>(ReferenceType.Generic);
		generic.setCacheStrategy(GenericDefaultCacheStrategy.NewInstance());
		return generic;
	}
	
	public ReferenceBase newMap(){
		ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>> map = new ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>>(ReferenceType.Map);
		map.setCacheStrategy(ReferenceBaseDefaultCacheStrategy.NewInstance());
		return map;
		
	}
	
	public ReferenceBase newReport(){
		ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>> report = new ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>>(ReferenceType.Report);
		report.setCacheStrategy(ReferenceBaseDefaultCacheStrategy.NewInstance());
		return report;
		
	}
	
	public ReferenceBase newWebPage(){
		ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>> webPage = new ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>>(ReferenceType.WebPage);
		webPage.setCacheStrategy(ReferenceBaseDefaultCacheStrategy.NewInstance());
		return webPage;
	}
	
	public ReferenceBase newDatabase(){
		ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>> db = new ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>>(ReferenceType.Database);
		db.setCacheStrategy(ReferenceBaseDefaultCacheStrategy.NewInstance());
		return db;
	}
	
	public static ReferenceFactory newInstance(){
		return new ReferenceFactory();
	}

	public ReferenceBase newPrintSeries(String series) {
		ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>> refBase = new ReferenceBase<ReferenceBaseDefaultCacheStrategy<ReferenceBase>>(ReferenceType.PrintSeries);
		refBase.setSeries(series);
		refBase.setCacheStrategy(ReferenceBaseDefaultCacheStrategy.NewInstance());
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
	
	
	
}
