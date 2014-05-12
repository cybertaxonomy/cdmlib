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


import org.junit.Assert;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.reference.IBook;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

/**
 * @author a.mueller
 * @date 16.06.2010
 *
 */
public class BookDefaultCacheStrategyTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(BookDefaultCacheStrategyTest.class);
	
	private static IBook book1;
	private static Team bookTeam1;
	private static BookDefaultCacheStrategy defaultStrategy;
	private static final String detail1 = "55";
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		defaultStrategy = BookDefaultCacheStrategy.NewInstance();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		book1 = ReferenceFactory.newBook();
		bookTeam1 = Team.NewTitledInstance("Book Author", "TT.");
	}
	
//**************************** TESTS ***********************************

	
	@Test
	public void testGetTitleCache(){
		book1.setTitle("My book");
		book1.setAuthorTeam(bookTeam1);
		book1.setDatePublished(TimePeriod.NewInstance(1975));
		Assert.assertEquals("Unexpected title cache.", "Book Author, My book. 1975", book1.getTitleCache());
		
		book1.setTitleCache(null, false);
		book1.setEdition("ed. 3");
		Assert.assertEquals("Unexpected title cache.", "Book Author, My book, ed. 3. 1975", book1.getTitleCache());
		
		TimePeriod newDatePublished = TimePeriodParser.parseString("1975 (after Aug.)");
		book1.setDatePublished(newDatePublished);
		book1.setTitleCache(null, false);
		//TODO this behaviour needs to be discussed. Maybe better the complete date published string should be returned.
		Assert.assertEquals("Unexpected title cache.", "Book Author, My book, ed. 3", book1.getTitleCache());
		
		
	}
	

	
	@Test
	public void testGetNomenclaturalCitation(){
		book1.setTitle("My book");
		book1.setAuthorTeam(bookTeam1);
		book1.setDatePublished(TimePeriod.NewInstance(1975));
		Assert.assertEquals("My book: 55. 1975", book1.getNomenclaturalCitation(detail1));
	}
	
}
