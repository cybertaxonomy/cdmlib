/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.cache.agent;


import static org.junit.Assert.assertNotNull;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Person;

/**
 * @author a.mueller
 * @created 29.09.2009
 * @version 1.0
 */
public class PersonDefaultCacheStrategyTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(PersonDefaultCacheStrategyTest.class);

	private static Person person1;
	private static Person person2;
	private static Person person3;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		
		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		person1 = Person.NewInstance();
		
		person1.setFirstname("P1FN");
		person1.setLastname("P1LN");
		person1.setPrefix("Dr1.");
		person1.setSuffix("Suff1");
		
		person2 = Person.NewInstance();
		person2.setNomenclaturalTitle("P2NomT");
		person2.setLastname("P2LN");
		person2.setFirstname("P2FN");
		person2.setSuffix("P2Suff");
		
		person3 = Person.NewInstance(); //empty person
		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
//**************************************** TESTS **************************************
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.agent.PersonDefaultCacheStrategy#NewInstance()}.
	 */
	@Test
	public final void testNewInstance() {
		PersonDefaultCacheStrategy cacheStrategy = PersonDefaultCacheStrategy.NewInstance();
		assertNotNull(cacheStrategy);
	}

	@Test
	public final void testGetNomenclaturalTitleCache(){
		Assert.assertNotNull("person1 nomenclatural title must not to be null", person1.getNomenclaturalTitle());
		Assert.assertEquals("Person1 nomenclatural title should be created by elements", "Dr1. P1FN P1LN Suff1", person1.getNomenclaturalTitle());
		person1.setSuffix(null);
		Assert.assertEquals("Person1 title should be Dr1. P1FN P1LN", "Dr1. P1FN P1LN", person1.getNomenclaturalTitle());
		//peson2
		Assert.assertEquals("Person2 title should be P2NomT", "P2NomT", person2.getNomenclaturalTitle());
		//person3
		Assert.assertNotNull("person3 nomenclatural title must not to be null", person3.getNomenclaturalTitle());
		Assert.assertTrue("Person3 nomenclatural title must not be empty", StringUtils.isNotBlank(person3.getNomenclaturalTitle()));
		//don't take to serious, may be also something different, but not empty
		Assert.assertEquals("Person3 title should start with Person#0", "Person#0", person3.getNomenclaturalTitle().substring(0, 8));
	}
	

	@Test
	public final void testGetTitleCache(){
		Assert.assertNotNull("person1 title cache must not to be null", person1.getTitleCache());
		Assert.assertEquals("Person1 title cache should be created by elements", "Dr1. P1FN P1LN Suff1", person1.getTitleCache());
		person1.setSuffix(null);
		Assert.assertEquals("Person1 title cache should be Dr1. P1FN P1LN", "Dr1. P1FN P1LN", person1.getTitleCache());
		//peson2
		Assert.assertEquals("Person2 title cache should be P2NomT", "P2FN P2LN P2Suff", person2.getTitleCache());
		//person3
		Assert.assertNotNull("person3 title cache must not to be null", person3.getTitleCache());
		Assert.assertTrue("Person3 title cache must not be empty", StringUtils.isNotBlank(person3.getTitleCache()));
		//don't take to serious, may be also something different, but not empty
		Assert.assertEquals("Person3 title cache should start with Person#0", "Person#0", person3.getTitleCache().substring(0, 8));
		person3.setFirstname("Klaus");
		Assert.assertEquals("Person3 title cache should be Klaus", "Klaus", person3.getTitleCache());
	}

	@Test
    public final void testInitialsFromFirstname(){
	    PersonDefaultCacheStrategy formatter = PersonDefaultCacheStrategy.NewInstance();
	    boolean force = true;

	    String firstname = null;
        Assert.assertNull(formatter.getInitialsFromFirstname(firstname, force));

        firstname = "";
        Assert.assertEquals("", formatter.getInitialsFromFirstname(firstname, force));

        firstname = "  ";
        Assert.assertEquals("We expect blanks to be trimmed", "", formatter.getInitialsFromFirstname(firstname, force));

	    firstname = "John Michael ";
	    Assert.assertEquals("J.M.", formatter.getInitialsFromFirstname(firstname, force));

	    firstname = "Walter G.";
        Assert.assertEquals("W.G.", formatter.getInitialsFromFirstname(firstname, force));

        firstname = "A.L.";
        Assert.assertEquals("A.L.", formatter.getInitialsFromFirstname(firstname, force));

        firstname = "A.Ludw. W.";
        Assert.assertEquals("A.L.W.", formatter.getInitialsFromFirstname(firstname, force));

        firstname = "A. Ludw.  Norbert W.";
        Assert.assertEquals("A.L.N.W.", formatter.getInitialsFromFirstname(firstname, force));

        force = false;
        firstname = "A. Ludw.  Norbert W.";
        Assert.assertEquals("A.Ludw.N.W.", formatter.getInitialsFromFirstname(firstname, force));

        firstname = "W.-H.";
        Assert.assertEquals("W.-H.", formatter.getInitialsFromFirstname(firstname, force));
        firstname = "W.-Henning";
        Assert.assertEquals("W.-H.", formatter.getInitialsFromFirstname(firstname, force));
        firstname = "W.-Henn.";
        Assert.assertEquals("W.-Henn.", formatter.getInitialsFromFirstname(firstname, force));
        firstname = "Wolf-Henning";
        Assert.assertEquals("W.-H.", formatter.getInitialsFromFirstname(firstname, force));
        firstname = "Wolf\u2013 Henning";
        Assert.assertEquals("W.\u2013H.", formatter.getInitialsFromFirstname(firstname, force));


        firstname = "W";
        Assert.assertEquals("W.", formatter.getInitialsFromFirstname(firstname, force));

        firstname = "W K";
        Assert.assertEquals("W.K.", formatter.getInitialsFromFirstname(firstname, force));

        firstname = "WK";
        Assert.assertEquals("W.K.", formatter.getInitialsFromFirstname(firstname, force));

        firstname = "WKH";
        Assert.assertEquals("W.K.H.", formatter.getInitialsFromFirstname(firstname, force));


        //force
        force = true;
        firstname = "W.-H.";
        Assert.assertEquals("W.-H.", formatter.getInitialsFromFirstname(firstname, force));
        firstname = "W.-Henning";
        Assert.assertEquals("W.-H.", formatter.getInitialsFromFirstname(firstname, force));
        firstname = "W.-Henn.";
        Assert.assertEquals("W.-H.", formatter.getInitialsFromFirstname(firstname, force));
        firstname = "Wolf-Henning";
        Assert.assertEquals("W.-H.", formatter.getInitialsFromFirstname(firstname, force));

        firstname = "W";
        Assert.assertEquals("W.", formatter.getInitialsFromFirstname(firstname, force));

        firstname = "W K";
        Assert.assertEquals("W.K.", formatter.getInitialsFromFirstname(firstname, force));

        firstname = "WK";
        Assert.assertEquals("W.K.", formatter.getInitialsFromFirstname(firstname, force));

        firstname = "WKH";
        Assert.assertEquals("W.K.H.", formatter.getInitialsFromFirstname(firstname, force));

        force = false;
        firstname = "Pe. Y.";
        Assert.assertEquals("Pe.Y.", formatter.getInitialsFromFirstname(firstname, force));


        //must not throw exception (exact result may change in future)
        firstname = "W.-H.-";
        Assert.assertEquals("W.-H.-", formatter.getInitialsFromFirstname(firstname, force));
        firstname = "W.-Hennin-";
        Assert.assertEquals("W.-H.-", formatter.getInitialsFromFirstname(firstname, force));

        force = false;
        firstname = "W.-H.-";
        Assert.assertEquals("W.-H.-", formatter.getInitialsFromFirstname(firstname, force));
        firstname = "W.-Hennin-";
        Assert.assertEquals("W.-H.-", formatter.getInitialsFromFirstname(firstname, force));

	}

}
