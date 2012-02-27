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
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.TimePeriod;
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
		team1 = Team.NewTitledInstance("Author", "TT.");
	}
	
//**************************** TESTS ***********************************

	
	@Test
	public void testGetTitleCache(){
		generic1.setTitle("auct.");
		Assert.assertEquals("Unexpected title cache.", "auct.", generic1.getTitleCache());
	}
	

	
}
