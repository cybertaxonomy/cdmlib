// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.reference;


import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.reference.IArticle;
import eu.etaxonomy.cdm.model.reference.IBook;
import eu.etaxonomy.cdm.model.reference.IBookSection;
import eu.etaxonomy.cdm.model.reference.IJournal;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @date 16.06.2010
 *
 */
public class BookSectionDefaultCacheStrategyTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(BookSectionDefaultCacheStrategyTest.class);
	
	private static IBookSection bookSection1;
	private static IBook book1;
	private static Team sectionTeam1;
	private static Team bookTeam1;
	private static BookSectionDefaultCacheStrategy defaultStrategy;
	private static final String detail1 = "55";
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		defaultStrategy = BookSectionDefaultCacheStrategy.NewInstance();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		bookSection1 = ReferenceFactory.newBookSection();
		bookSection1.setCacheStrategy(defaultStrategy);
		book1 = ReferenceFactory.newBook();
		sectionTeam1 = Team.NewTitledInstance("Section Author", "T.");
		bookTeam1 = Team.NewTitledInstance("Book Author", "TT.");
	}
	
//**************************** TESTS ***********************************

	
	@Test
	public void testGetTitleCache(){
		book1.setTitle("My book");
		book1.setAuthorTeam(bookTeam1);
		bookSection1.setTitle("My chapter");
		bookSection1.setInBook(book1);
		bookSection1.setAuthorTeam(sectionTeam1);
		book1.setDatePublished(TimePeriod.NewInstance(1975));
		Assert.assertEquals("Unexpected title cache.", "Section Author - My chapter in Book Author, My book. 1975", bookSection1.getTitleCache());
		book1.setDatePublished(null);
		bookSection1.setDatePublished(TimePeriod.NewInstance(1976));
		Assert.assertEquals("Unexpected title cache.", "Section Author - My chapter in Book Author, My book. 1976", bookSection1.getTitleCache());
		book1.setDatePublished(TimePeriod.NewInstance(1977));
		bookSection1.setTitleCache(null);
		Assert.assertEquals("Unexpected title cache.", "Section Author - My chapter in Book Author, My book. 1976", bookSection1.getTitleCache());
		
		bookSection1.setInBook(null);
		bookSection1.setTitleCache(null);
		Assert.assertEquals("Unexpected title cache.", "Section Author - My chapter in " + BookSectionDefaultCacheStrategy.UNDEFINED_BOOK + ". 1976", bookSection1.getTitleCache());
		
	}
	
	@Ignore
	@Test
	//This test is just to show that there is still the title cache bug which is not
	//set to null by setInBook(null) and otheres
	public void testGetTitleCache2(){
		book1.setTitle("My book");
		book1.setAuthorTeam(bookTeam1);
		bookSection1.setTitle("My chapter");
		bookSection1.setInBook(book1);
		bookSection1.setAuthorTeam(sectionTeam1);
		book1.setDatePublished(TimePeriod.NewInstance(1975));
		Assert.assertEquals("Unexpected title cache.", "Section Author - My chapter in Book Author, My book. 1975", bookSection1.getTitleCache());
		book1.setDatePublished(null);
		bookSection1.setDatePublished(TimePeriod.NewInstance(1976));
		Assert.assertEquals("Unexpected title cache.", "Section Author - My chapter in Book Author, My book. 1976", bookSection1.getTitleCache());
		book1.setDatePublished(TimePeriod.NewInstance(1977));
		Assert.assertEquals("Unexpected title cache.", "Section Author - My chapter in Book Author, My book. 1976", bookSection1.getTitleCache());
		
		bookSection1.setInBook(null);
		Assert.assertEquals("Unexpected title cache.", "Section Author - My chapter in " + BookSectionDefaultCacheStrategy.UNDEFINED_BOOK + ". 1976", bookSection1.getTitleCache());
		
	}

	
	@Test
	public void testGetNomenclaturalCitation(){
		book1.setTitle("My book");
		book1.setAuthorTeam(bookTeam1);
		bookSection1.setTitle("My chapter");
		bookSection1.setInBook(book1);
		bookSection1.setAuthorTeam(sectionTeam1);
		book1.setDatePublished(TimePeriod.NewInstance(1975));
		Assert.assertEquals("in Book Author, My book: 55. 1975", bookSection1.getNomenclaturalCitation(detail1));
	}
	
}
