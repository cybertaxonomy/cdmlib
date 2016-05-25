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

import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;

public class ReferenceFactory {
	private static final Logger logger = Logger.getLogger(ReferenceFactory.class);

	public static Reference newArticle(){
		Reference article = new Reference(ReferenceType.Article);
		article.setCacheStrategy(ReferenceType.Article.getCacheStrategy());
		return article;
	}

	public static Reference newJournal(){
		Reference journal = new Reference(ReferenceType.Journal);
		journal.setCacheStrategy(ReferenceType.Journal.getCacheStrategy());
		return journal;
	}

	public static Reference newBook(){
		Reference book = new Reference(ReferenceType.Book);
		book.setCacheStrategy(ReferenceType.Book.getCacheStrategy());
		return book;
	}

	public static Reference newThesis(){
		Reference thesis = new Reference(ReferenceType.Thesis);
		thesis.setCacheStrategy(ReferenceType.Thesis.getCacheStrategy());
		return thesis;
	}

	public static Reference newInProceedings(){
		Reference inProceedings = new Reference(ReferenceType.InProceedings);
		inProceedings.setCacheStrategy(ReferenceType.InProceedings.getCacheStrategy());
		return inProceedings;
	}

	public static Reference newProceedings(){
		Reference proceedings = new Reference(ReferenceType.Proceedings);
		proceedings.setCacheStrategy(ReferenceType.Proceedings.getCacheStrategy());
		return proceedings;
	}

	public static Reference newBookSection(){
		Reference bookSection = new Reference(ReferenceType.BookSection);
		bookSection.setCacheStrategy(ReferenceType.BookSection.getCacheStrategy());
		return bookSection;
	}


	public static Reference newSection(){
		Reference section = new Reference(ReferenceType.Section);
		section.setCacheStrategy(ReferenceType.Section.getCacheStrategy());
		return section;
	}

	public static Reference newCdDvd(){
		Reference cdDvd= new Reference(ReferenceType.CdDvd);
		cdDvd.setCacheStrategy(ReferenceType.CdDvd.getCacheStrategy());
		return cdDvd;
	}

	public static Reference newGeneric(){
		Reference generic = new Reference(ReferenceType.Generic);
		generic.setCacheStrategy(ReferenceType.Generic.getCacheStrategy());
		return generic;
	}

	public static Reference newMap(){
		Reference map = new Reference(ReferenceType.Map);
		map.setCacheStrategy(ReferenceType.Map.getCacheStrategy());
		return map;

	}

	public static Reference newReport(){
		Reference report = new Reference(ReferenceType.Report);
		report.setCacheStrategy(ReferenceType.Report.getCacheStrategy());
		return report;

	}

	public static Reference newWebPage(){
		Reference webPage = new Reference(ReferenceType.WebPage);
		webPage.setCacheStrategy(ReferenceType.WebPage.getCacheStrategy());
		return webPage;
	}

	public static Reference newDatabase(){
		Reference db = new Reference(ReferenceType.Database);
		db.setCacheStrategy(ReferenceType.Database.getCacheStrategy());
		return db;
	}


	/**
	 * Creates a new empty print series instance.
	 */
	public static Reference newPrintSeries() {
		Reference refBase = new Reference(ReferenceType.PrintSeries);
		refBase.setCacheStrategy(ReferenceType.PrintSeries.getCacheStrategy());
		return refBase;
	}

	/**
	 * Creates a new print series instance with a given title string.
	 */
	public static Reference newPrintSeries(String series) {
		Reference refBase = newPrintSeries();
		refBase.setTitle(series);
		refBase.setCacheStrategy(ReferenceType.PrintSeries.getCacheStrategy());
		return refBase;
	}

	public static Reference newBookSection(Reference book, TeamOrPersonBase partAuthor,
			String sectionTitle, String pages) {
		Reference bookSection = newBookSection();
		bookSection.setInBook(book);
		bookSection.setAuthorship(partAuthor);
		bookSection.setTitle(sectionTitle);
		bookSection.setPages(pages);
		return bookSection;
	}

	public static Reference newArticle(Reference inJournal, TeamOrPersonBase partAuthor,
			String title, String pages, String seriesPart, String volume, TimePeriod datePublished) {
		IArticle article = newArticle();
		article.setInReference(inJournal);
		article.setAuthorship(partAuthor);
		article.setTitle(title);
		article.setPages(pages);
		article.setVolume(volume);
		article.setSeriesPart(seriesPart);
		article.setDatePublished(datePublished);
		return (Reference)article;
	}

	/**
	 * Returns a new reference for the according reference type. If reference type is <code>null</code>,
	 * <code>null</code> is returned.
	 * @param referenceType
	 * @return
	 */
	public static Reference newReference(ReferenceType referenceType) {
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
			case Section:
				return newSection();
			default:
				logger.warn("Unknown reference type " + referenceType.getMessage() + ". Created generic reference instead.");
				return newGeneric();
		}
	}

	public static Reference newPersonalCommunication() {
		Reference personalCommunication = new Reference(ReferenceType.PersonalCommunication);
		personalCommunication.setCacheStrategy(ReferenceType.PersonalCommunication.getCacheStrategy());
		return personalCommunication;
	}

	public static Reference newPatent() {
		Reference patent = new Reference(ReferenceType.Patent);
		patent.setCacheStrategy(ReferenceType.Patent.getCacheStrategy());
		return patent;
	}



}
