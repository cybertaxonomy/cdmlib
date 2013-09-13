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
import eu.etaxonomy.cdm.model.reference.IBook;
import eu.etaxonomy.cdm.model.reference.IGeneric;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @date 16.06.2010
 *
 */
public class GenericDefaultCacheStrategyTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(GenericDefaultCacheStrategyTest.class);
	
	private static IGeneric generic1;
	private static Team team1;
	private static GenericDefaultCacheStrategy<Reference> defaultStrategy;
	private static final String detail1 = "55";
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		defaultStrategy = GenericDefaultCacheStrategy.NewInstance();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		generic1 = ReferenceFactory.newGeneric();
		generic1.setCacheStrategy(defaultStrategy);
		team1 = Team.NewTitledInstance("Authorteam", "TT.");
	}
	
//**************************** TESTS ***********************************

	
	@Test
	public void testGetTitleCache(){
		generic1.setTitle("auct.");
		Assert.assertEquals("Unexpected title cache.", "auct.", generic1.getTitleCache());
	}
	
	
	@Test
	public void testGetInRef(){
		generic1.setTitle("auct.");
		IBook book1 = ReferenceFactory.newBook();
		book1.setTitle("My book title");
		book1.setAuthorTeam(team1);
		Reference<?> inRef = (Reference<?>)book1;
		generic1.setInReference(inRef);
		generic1.setTitleCache(null);  //reset cache in case aspectJ is not enabled
		Assert.assertEquals("Unexpected title cache.", "in Authorteam, My book title: 2", generic1.getNomenclaturalCitation("2"));
	}

	@Test
	public void testGetInRefWithoutInRef(){
		generic1.setTitle("My generic title");
		generic1.setAuthorTeam(team1);
		generic1.setTitleCache(null);  //reset cache in case aspectJ is not enabled
		Assert.assertEquals("Unexpected title cache.", "My generic title: 2", generic1.getNomenclaturalCitation("2"));
	}
	
	@Test
	public void testGetTitleCache2(){
		generic1.setTitle("Part Title");
		IBook book1 = ReferenceFactory.newBook();
		book1.setTitle("My book title");
		book1.setAuthorTeam(team1);
		Reference<?> inRef = (Reference<?>)book1;
		generic1.setInReference(inRef);
		generic1.setTitleCache(null);  //reset cache in case aspectJ is not enabled
		Assert.assertEquals("Unexpected title cache.", "Part Title in Authorteam, My book title", generic1.getTitleCache());
	}

	@Test
	public void testGetTitleCacheWithoutInRef(){
		generic1.setTitle("My generic title");
		generic1.setAuthorTeam(team1);
		generic1.setTitleCache(null);  //reset cache in case aspectJ is not enabled
		Assert.assertEquals("Unexpected title cache.", "Authorteam, My generic title", generic1.getTitleCache());
	}

	
}
