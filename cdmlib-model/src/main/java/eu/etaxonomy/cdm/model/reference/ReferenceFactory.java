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
import eu.etaxonomy.cdm.model.common.VerbatimTimePeriod;

public class ReferenceFactory {
	private static final Logger logger = Logger.getLogger(ReferenceFactory.class);

	public static Reference newArticle(){
		return new Reference(ReferenceType.Article);
	}

	public static Reference newJournal(){
	    return new Reference(ReferenceType.Journal);
	}

	public static Reference newBook(){
	    return new Reference(ReferenceType.Book);
	}

	public static Reference newThesis(){
	    return new Reference(ReferenceType.Thesis);
	}

	public static Reference newInProceedings(){
	    return new Reference(ReferenceType.InProceedings);
	}

	public static Reference newProceedings(){
	    return new Reference(ReferenceType.Proceedings);
	}

	public static Reference newBookSection(){
	    return new Reference(ReferenceType.BookSection);
	}

	public static Reference newSection(){
	    return new Reference(ReferenceType.Section);
	}

	public static Reference newCdDvd(){
	    return new Reference(ReferenceType.CdDvd);
	}

	public static Reference newGeneric(){
	    return new Reference(ReferenceType.Generic);
	}

	public static Reference newMap(){
	    return new Reference(ReferenceType.Map);
	}

	public static Reference newReport(){
	    return new Reference(ReferenceType.Report);
	}

	public static Reference newWebPage(){
	    return new Reference(ReferenceType.WebPage);
	}

	public static Reference newDatabase(){
	    return new Reference(ReferenceType.Database);
	}

	public static Reference newPrintSeries() {
	    return new Reference(ReferenceType.PrintSeries);
	}

    public static Reference newPersonalCommunication() {
        return new Reference(ReferenceType.PersonalCommunication);
    }

    public static Reference newPatent() {
        return new Reference(ReferenceType.Patent);
    }

// ******************** Short cuts **********************************************/

	/**
	 * Creates a new print series instance with a given title string.
	 */
	public static Reference newPrintSeries(String series) {
		Reference refBase = newPrintSeries();
		refBase.setTitle(series);
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
			String title, String pages, String seriesPart, String volume, VerbatimTimePeriod datePublished) {
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

//****************************** by Type **************************************/

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

}
