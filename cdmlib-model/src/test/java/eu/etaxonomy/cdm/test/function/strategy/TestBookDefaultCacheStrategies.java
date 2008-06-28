/**
 * 
 */
package eu.etaxonomy.cdm.test.function.strategy;

import java.util.Calendar;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.reference.Article;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.BookSection;
import eu.etaxonomy.cdm.model.reference.Generic;
import eu.etaxonomy.cdm.model.reference.Journal;

/**
 * @author AM
 *
 */
public class TestBookDefaultCacheStrategies {

	private boolean testNonViralNameDefaultCacheStrategy(){
		
		//Book
		System.out.println("*********** BOOK**************");
		Book book = Book.NewInstance();
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
		BookSection bookSection = BookSection.NewInstance(book, partAuthor, "SectionTitle der Biene", "222-234");
		System.out.println("FULL: " + bookSection.getNomenclaturalCitation("344"));
		System.out.println("Citation: " + bookSection.getCitation());
		System.out.println("Titel: " + bookSection.getTitleCache());

		//Article
		System.out.println("*********** ARTICLE **************");
		Journal inJournal = Journal.NewInstance();
		Person journalAuthor = Person.NewTitledInstance("JournalAuthorTitle");
		inJournal.setAuthorTeam(journalAuthor);
		inJournal.setTitle("JournalTitle");
		inJournal.setIssn("issn");
		Article article = Article.NewInstance(inJournal, partAuthor, "artTitel", "123-456", "ser4", "55", TimePeriod.NewInstance(cal));
		System.out.println("FULL: " + article.getNomenclaturalCitation("922 fig"));
		System.out.println("Citation: " + article.getCitation());
		System.out.println("Titel: " + article.getTitleCache());

		//Article
		System.out.println("*********** GENERIC **************");
		Generic generic = Generic.NewInstance();
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
