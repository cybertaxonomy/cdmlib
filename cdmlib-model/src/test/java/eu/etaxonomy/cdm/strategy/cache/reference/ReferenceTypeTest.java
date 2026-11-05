/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.reference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.model.reference.ReferenceType;

/**
 * @author a.mueller
 */
public class ReferenceTypeTest {

// ****************************** TESTS ***************************************

	@Test
	public void testIsPrintedUnit(){
		assertTrue(ReferenceType.Proceedings.isPrintedUnit(), "Proceedings must be printed unit");
		assertTrue(ReferenceType.Book.isPrintedUnit(), "Book must be printed unit");
		assertFalse(ReferenceType.Article.isPrintedUnit(), "Article must not be printed unit");
		assertFalse(ReferenceType.BookSection.isPrintedUnit(), "BookSection must not be printed unit");
		assertFalse(ReferenceType.CdDvd.isPrintedUnit(), "CdDvd must not be printed unit");
		assertFalse(ReferenceType.Generic.isPrintedUnit(), "Generic must not be printed unit");
		assertFalse(ReferenceType.Database.isPrintedUnit(), "Database must not be printed unit");
		assertFalse(ReferenceType.InProceedings.isPrintedUnit(), "InProceedings must not be printed unit");
		assertFalse(ReferenceType.Journal.isPrintedUnit(), "Journal must not be printed unit");
		assertFalse(ReferenceType.Map.isPrintedUnit(), "Map must not be printed unit");
		assertFalse(ReferenceType.Patent.isPrintedUnit(), "Patent must not be printed unit");
		assertFalse(ReferenceType.PersonalCommunication.isPrintedUnit(), "PersonalCommunication must not be printed unit");
		assertFalse(ReferenceType.PrintSeries.isPrintedUnit(), "PrintSeries must not be printed unit");
		assertFalse(ReferenceType.Report.isPrintedUnit(), "Report must not be printed unit");
		assertFalse(ReferenceType.Thesis.isPrintedUnit(), "Thesis must not be printed unit");
		assertFalse(ReferenceType.WebPage.isPrintedUnit(), "WebPage must not be printed unit");
	}

	@Test
	public void testIsPublication(){
		assertTrue(ReferenceType.Proceedings.isPublication(), "Proceedings must be publication");
		assertTrue(ReferenceType.Book.isPublication(), "Book must be publication");
		assertFalse(ReferenceType.Article.isPublication(), "Article must not be publication");
		assertFalse(ReferenceType.BookSection.isPublication(), "BookSection must not be publication");
		assertTrue(ReferenceType.CdDvd.isPublication(), "CdDvd must be publication");
		assertTrue(ReferenceType.Generic.isPublication(), "Generic must be publication");
		assertTrue(ReferenceType.Database.isPublication(), "Database must be publication");
		assertFalse(ReferenceType.InProceedings.isPublication(), "InProceedings must not be publication");
		assertTrue(ReferenceType.Journal.isPublication(), "Journal must be publication");
		assertTrue(ReferenceType.Map.isPublication(), "Map must be publication");
		assertFalse(ReferenceType.Patent.isPublication(), "Patent must not be publication");
		assertFalse(ReferenceType.PersonalCommunication.isPublication(), "PersonalCommunication must not be publication");
		assertTrue(ReferenceType.PrintSeries.isPublication(), "PrintSeries must be publication");
		assertTrue(ReferenceType.Report.isPublication(), "Report must be publication");
		assertTrue(ReferenceType.Thesis.isPublication(), "Thesis must be publication");
		assertTrue(ReferenceType.WebPage.isPublication(), "WebPage must be publication");
	}


	@Test
	public void testIsVolumeReference(){
		assertTrue(ReferenceType.Proceedings.isVolumeReference(), "Proceedings must be volume reference");
		assertTrue(ReferenceType.Book.isVolumeReference(), "Book must be volume reference");
		assertTrue(ReferenceType.Article.isVolumeReference(), "Article must be volume reference");
		assertFalse(ReferenceType.BookSection.isVolumeReference(), "BookSection must not be volume reference");
		assertFalse(ReferenceType.CdDvd.isVolumeReference(), "CdDvd must not be volume reference");
		assertTrue(ReferenceType.Generic.isVolumeReference(), "Generic must be volume reference");
		assertFalse(ReferenceType.Database.isVolumeReference(), "Database must not be volume reference");
		assertFalse(ReferenceType.InProceedings.isVolumeReference(), "InProceedings must not be volume reference");
		assertFalse(ReferenceType.Journal.isVolumeReference(), "Journal must not be volume reference");
		assertFalse(ReferenceType.Map.isVolumeReference(), "Map must not be volume reference");
		assertFalse(ReferenceType.Patent.isVolumeReference(), "Patent must not be volume reference");
		assertFalse(ReferenceType.PersonalCommunication.isVolumeReference(), "PersonalCommunication must not be volume reference");
		assertFalse(ReferenceType.PrintSeries.isVolumeReference(), "PrintSeries must not be volume reference");
		assertFalse(ReferenceType.Report.isVolumeReference(), "Report must not be volume reference");
		assertFalse(ReferenceType.Thesis.isVolumeReference(), "Thesis must not be volume reference");
		assertFalse(ReferenceType.WebPage.isVolumeReference(), "WebPage must not be volume reference");
	}

	@Test
	public void testIsSection(){
		assertTrue(ReferenceType.Proceedings.isSection(), "Proceedings must be section");
		assertTrue(ReferenceType.Book.isSection(), "Book must be section");
		assertTrue(ReferenceType.Article.isSection(), "Article must be section");
		assertTrue(ReferenceType.BookSection.isSection(), "BookSection must be section");
		assertFalse(ReferenceType.CdDvd.isSection(), "CdDvd must not be section");
		assertFalse(ReferenceType.Generic.isSection(), "Generic must not be section");
		assertFalse(ReferenceType.Database.isSection(), "Database must not be section");
		assertTrue(ReferenceType.InProceedings.isSection(), "InProceedings must be section");
		assertFalse(ReferenceType.Journal.isSection(), "Journal must not be section");
		assertFalse(ReferenceType.Map.isSection(), "Map must not be section");
		assertFalse(ReferenceType.Patent.isSection(), "Patent must not be section");
		assertFalse(ReferenceType.PersonalCommunication.isSection(), "PersonalCommunication must not be section");
		assertFalse(ReferenceType.PrintSeries.isSection(), "PrintSeries must not be section");
		assertFalse(ReferenceType.Report.isSection(), "Report must not be section");
		assertFalse(ReferenceType.Thesis.isSection(), "Thesis must not be section");
		assertTrue(ReferenceType.WebPage.isSection(), "WebPage must be section");
	}
}
