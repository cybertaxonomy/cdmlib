/**
 * 
 */
package eu.etaxonomy.cdm.test.function.strategy;

import java.util.Calendar;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.BookSection;

/**
 * @author AM
 *
 */
public class TestBookDefaultCacheStrategies {

	private boolean testNonViralNameDefaultCacheStrategy(){
		
		//Book
		Book book = Book.NewInstance();
		book.setTitle("TestTitel eines Buches");
		Calendar cal = Calendar.getInstance();
		book.setDatePublished(TimePeriod.NewInstance(cal));
		Person author = Person.NewTitledInstance("AuthorTitle");
		book.setAuthorTeam(author);
		book.setVolume("v 22");
		book.setEdition("55");
		System.out.println(book.getNomenclaturalCitation("344"));
		System.out.println(book.getCitation());
		System.out.println(book.getTitleCache());
		
		//BookSection
		BookSection bookSection = BookSection.NewInstance(book, "222-234", "SectionTitle der Biene");
		System.out.println(bookSection.getNomenclaturalCitation("344"));
		System.out.println(bookSection.getCitation());
		System.out.println(bookSection.getTitleCache());
		
		
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
