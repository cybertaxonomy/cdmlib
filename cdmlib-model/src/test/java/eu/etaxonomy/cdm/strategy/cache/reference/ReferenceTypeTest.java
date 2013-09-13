/**
 * 
 */
package eu.etaxonomy.cdm.strategy.cache.reference;


import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.reference.ReferenceType;

/**
 * @author a.mueller
 *
 */
public class ReferenceTypeTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

// ****************************** TESTS ***************************************
	
	@Test
	public void testIsPrintedUnit(){
		Assert.assertTrue("Proceedings must be printed unit", ReferenceType.Proceedings.isPrintedUnit());
		Assert.assertTrue("Book must be printed unit", ReferenceType.Book.isPrintedUnit());
		Assert.assertFalse("Article must not be printed unit", ReferenceType.Article.isPrintedUnit());
		Assert.assertFalse("BookSection must not be printed unit", ReferenceType.BookSection.isPrintedUnit());
		Assert.assertFalse("CdDvd must not be printed unit", ReferenceType.CdDvd.isPrintedUnit());
		Assert.assertFalse("Generic must not be printed unit", ReferenceType.Generic.isPrintedUnit());
		Assert.assertFalse("Database must not be printed unit", ReferenceType.Database.isPrintedUnit());
		Assert.assertFalse("InProceedings must not be printed unit", ReferenceType.InProceedings.isPrintedUnit());
		Assert.assertFalse("Journal must not be printed unit", ReferenceType.Journal.isPrintedUnit());
		Assert.assertFalse("Map must not be printed unit", ReferenceType.Map.isPrintedUnit());
		Assert.assertFalse("Patent must not be printed unit", ReferenceType.Patent.isPrintedUnit());
		Assert.assertFalse("PersonalCommunication must not be printed unit", ReferenceType.PersonalCommunication.isPrintedUnit());
		Assert.assertFalse("PrintSeries must not be printed unit", ReferenceType.PrintSeries.isPrintedUnit());
		Assert.assertFalse("Report must not be printed unit", ReferenceType.Report.isPrintedUnit());
		Assert.assertFalse("Thesis must not be printed unit", ReferenceType.Thesis.isPrintedUnit());
		Assert.assertFalse("WebPage must not be printed unit", ReferenceType.WebPage.isPrintedUnit());
	}
	
	@Test
	public void testIsPublication(){
		Assert.assertTrue("Proceedings must be publication", ReferenceType.Proceedings.isPublication());
		Assert.assertTrue("Book must be publication", ReferenceType.Book.isPublication());
		Assert.assertFalse("Article must not be publication", ReferenceType.Article.isPublication());
		Assert.assertFalse("BookSection must not be publication", ReferenceType.BookSection.isPublication());
		Assert.assertTrue("CdDvd must be publication", ReferenceType.CdDvd.isPublication());
		Assert.assertTrue("Generic must be publication", ReferenceType.Generic.isPublication());
		Assert.assertTrue("Database must be publication", ReferenceType.Database.isPublication());
		Assert.assertFalse("InProceedings must not be publication", ReferenceType.InProceedings.isPublication());
		Assert.assertTrue("Journal must be publication", ReferenceType.Journal.isPublication());
		Assert.assertTrue("Map must be publication", ReferenceType.Map.isPublication());
		Assert.assertFalse("Patent must not be publication", ReferenceType.Patent.isPublication());
		Assert.assertFalse("PersonalCommunication must not be publication", ReferenceType.PersonalCommunication.isPublication());
		Assert.assertTrue("PrintSeries must be publication", ReferenceType.PrintSeries.isPublication());
		Assert.assertTrue("Report must be publication", ReferenceType.Report.isPublication());
		Assert.assertTrue("Thesis must be publication", ReferenceType.Thesis.isPublication());
		Assert.assertTrue("WebPage must be publication", ReferenceType.WebPage.isPublication());
	}
	

	@Test
	public void testIsVolumeReference(){
		Assert.assertTrue("Proceedings must be volume reference", ReferenceType.Proceedings.isVolumeReference());
		Assert.assertTrue("Book must be volume reference", ReferenceType.Book.isVolumeReference());
		Assert.assertTrue("Article must be volume reference", ReferenceType.Article.isVolumeReference());
		Assert.assertFalse("BookSection must not be volume reference", ReferenceType.BookSection.isVolumeReference());
		Assert.assertFalse("CdDvd must not be volume reference", ReferenceType.CdDvd.isVolumeReference());
		Assert.assertTrue("Generic must be volume reference", ReferenceType.Generic.isVolumeReference());
		Assert.assertFalse("Database must not be volume reference", ReferenceType.Database.isVolumeReference());
		Assert.assertFalse("InProceedings must not be volume reference", ReferenceType.InProceedings.isVolumeReference());
		Assert.assertFalse("Journal must not be volume reference", ReferenceType.Journal.isVolumeReference());
		Assert.assertFalse("Map must not be volume reference", ReferenceType.Map.isVolumeReference());
		Assert.assertFalse("Patent must not be volume reference", ReferenceType.Patent.isVolumeReference());
		Assert.assertFalse("PersonalCommunication must not be volume reference", ReferenceType.PersonalCommunication.isVolumeReference());
		Assert.assertFalse("PrintSeries must not be volume reference", ReferenceType.PrintSeries.isVolumeReference());
		Assert.assertFalse("Report must not be volume reference", ReferenceType.Report.isVolumeReference());
		Assert.assertFalse("Thesis must not be volume reference", ReferenceType.Thesis.isVolumeReference());
		Assert.assertFalse("WebPage must not be volume reference", ReferenceType.WebPage.isVolumeReference());
	}
	

	@Test
	public void testIsSection(){
		Assert.assertTrue("Proceedings must be section", ReferenceType.Proceedings.isSection());
		Assert.assertTrue("Book must be section", ReferenceType.Book.isSection());
		Assert.assertTrue("Article must be section", ReferenceType.Article.isSection());
		Assert.assertTrue("BookSection must be section", ReferenceType.BookSection.isSection());
		Assert.assertFalse("CdDvd must not be section", ReferenceType.CdDvd.isSection());
		Assert.assertFalse("Generic must not be section", ReferenceType.Generic.isSection());
		Assert.assertFalse("Database must not be section", ReferenceType.Database.isSection());
		Assert.assertTrue("InProceedings must be section", ReferenceType.InProceedings.isSection());
		Assert.assertFalse("Journal must not be section", ReferenceType.Journal.isSection());
		Assert.assertFalse("Map must not be section", ReferenceType.Map.isSection());
		Assert.assertFalse("Patent must not be section", ReferenceType.Patent.isSection());
		Assert.assertFalse("PersonalCommunication must not be section", ReferenceType.PersonalCommunication.isSection());
		Assert.assertFalse("PrintSeries must not be section", ReferenceType.PrintSeries.isSection());
		Assert.assertFalse("Report must not be section", ReferenceType.Report.isSection());
		Assert.assertFalse("Thesis must not be section", ReferenceType.Thesis.isSection());
		Assert.assertFalse("WebPage must not be section", ReferenceType.WebPage.isSection());
	}
}
