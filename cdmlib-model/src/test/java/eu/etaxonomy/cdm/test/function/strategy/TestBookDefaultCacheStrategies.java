/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.test.function.strategy;

import java.util.Calendar;

import org.joda.time.DateTime;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.media.Media;
//import eu.etaxonomy.cdm.model.reference.Article;
//import eu.etaxonomy.cdm.model.reference.Book;
//import eu.etaxonomy.cdm.model.reference.BookSection;
//import eu.etaxonomy.cdm.model.reference.Generic;
//import eu.etaxonomy.cdm.model.reference.Journal;
import eu.etaxonomy.cdm.model.reference.IGeneric;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author AM
 *
 */
public class TestBookDefaultCacheStrategies {

	private boolean testNonViralNameDefaultCacheStrategy(){
		
		//Book
		ReferenceFactory refFactory = ReferenceFactory.newInstance();
		System.out.println("*********** BOOK**************");
		ReferenceBase book = refFactory.newBook();
		book.setTitle("TestTitel eines Buches");
		Calendar cal = Calendar.getInstance();

		book.setDatePublished(TimePeriod.NewInstance(cal));
		Person bookAuthor = Person.NewTitledInstance("BuchAuthorTitle");
		book.setAuthorTeam(bookAuthor);
		book.setVolume("v 22");
		book.setEdition("55");
		System.out.println("FULL" + book.getNomenclaturalCitation("344"));
		System.out.println("Citation: " + book.getCitation());
		System.out.println("Titel: " + book.getTitleCache());
		
		//BookSection
		System.out.println("*********** BOOK SECTION**************");
		Person partAuthor = Person.NewTitledInstance("PartAuthorTitle");
		ReferenceBase bookSection = refFactory.newBookSection(book, partAuthor, "SectionTitle der Biene", "222-234");
		System.out.println("FULL: " + bookSection.getNomenclaturalCitation("344"));
		System.out.println("Citation: " + bookSection.getCitation());
		System.out.println("Titel: " + bookSection.getTitleCache());

		//Article
		System.out.println("*********** ARTICLE **************");
		ReferenceBase inJournal = refFactory.newJournal();
		Person journalAuthor = Person.NewTitledInstance("JournalAuthorTitle");
		inJournal.setAuthorTeam(journalAuthor);
		inJournal.setTitle("JournalTitle");
		inJournal.setIssn("issn");
		ReferenceBase article = refFactory.newArticle(inJournal, partAuthor, "artTitel", "123-456", "ser4", "55", TimePeriod.NewInstance(cal));
		System.out.println("FULL: " + article.getNomenclaturalCitation("922 fig"));
		System.out.println("Citation: " + article.getCitation());
		System.out.println("Titel: " + article.getTitleCache());

		//Generic
		System.out.println("*********** GENERIC **************");
		ReferenceBase generic = refFactory.newGeneric();
		Person genericAuthor = Person.NewTitledInstance("GenericAuthorTitle");
		generic.setAuthorTeam(genericAuthor);
		generic.setTitle("GenericTitle");
		generic.setDatePublished(TimePeriod.NewInstance(cal));
		generic.setEditor("EditorString");
		
		generic.setPages("p.124-754");
		generic.setSeries("ser2");
		generic.setVolume("33");
		System.out.println("FULL: " + generic.getNomenclaturalCitation("4444"));
		System.out.println("Citation: " + generic.getCitation());
		System.out.println("Titel: " + generic.getTitleCache());
		
		Media media = Media.NewInstance();
		generic.addMedia(media);
		IGeneric newGeneric = (IGeneric)generic.clone();
		System.out.println(newGeneric);
		
		return true;
		
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestBookDefaultCacheStrategies test = new TestBookDefaultCacheStrategies();
    	test.testNonViralNameDefaultCacheStrategy();

	}

}
