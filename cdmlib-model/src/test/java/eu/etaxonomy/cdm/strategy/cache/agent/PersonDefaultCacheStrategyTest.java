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
 * @since 29.09.2009
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

		person1.setGivenName("P1FN");
		person1.setFamilyName("P1LN");
		person1.setPrefix("Dr1.");
		person1.setSuffix("Suff1");

		person2 = Person.NewInstance();
		person2.setNomenclaturalTitle("P2NomT");
		person2.setFamilyName("P2LN");
		person2.setGivenName("P2FN");
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
		Assert.assertEquals("Person1 nomenclatural title should be taken from titleCache", "P1LN, P.", person1.getNomenclaturalTitle());
		person1.setSuffix(null);
		Assert.assertEquals("Person1 title should be taken from titleCache", "P1LN, P.", person1.getNomenclaturalTitle());
		//peson2
		Assert.assertEquals("Person2 title should be P2NomT", "P2NomT", person2.getNomenclaturalTitle());
		//person3
		Assert.assertNotNull("person3 nomenclatural title must not to be null", person3.getNomenclaturalTitle());
		Assert.assertTrue("Person3 nomenclatural title must not be empty", StringUtils.isNotBlank(person3.getNomenclaturalTitle()));
		//don't take to serious, may be also something different, but not empty
		Assert.assertEquals("Person3 title should start with Person#0", "Person#0", person3.getNomenclaturalTitle().substring(0, 8));
	}


	@Test
	public final void testGetTitleCacheAdaptedFromOldVersion(){
	    Assert.assertNotNull("person1 title cache must not to be null", person1.getTitleCache());
		Assert.assertEquals("Person1 title cache should be created by familyname and computed initials", "P1LN, P.", person1.getTitleCache());
		person1.setSuffix(null);
		Assert.assertEquals("Person1 title cache should be Dr1. P1FN P1LN", "P1LN, P.", person1.getTitleCache());
		//peson2
		Assert.assertEquals("Person2 title cache should be P2NomT", "P2LN, P.", person2.getTitleCache());
		//person3
		Assert.assertNotNull("person3 title cache must not to be null", person3.getTitleCache());
		Assert.assertTrue("Person3 title cache must not be empty", StringUtils.isNotBlank(person3.getTitleCache()));
		//don't take to serious, may be also something different, but not empty
		Assert.assertEquals("Person3 title cache should start with Person#0", "Person#0", person3.getTitleCache().substring(0, 8));
		person3.setGivenName("Klaus");
		Assert.assertEquals("Person3 title cache should be Klaus", "K.", person3.getTitleCache());
	}

	@Test
    public final void testGetTitleCache(){
        Person pers = Person.NewInstance();
        pers.setFamilyName("Last");
        pers.setInitials("E.M.");

        String expected = "Last, E.M.";
	    Assert.assertNotNull("pers title cache must not to be null", pers.getTitleCache());
        Assert.assertEquals("pers title cache should be created by familyname and initials",
                expected, pers.getTitleCache());

        pers.setSuffix("xyz");
        Assert.assertEquals("Suffix should not influence title cache",
                expected, pers.getTitleCache());
        pers.setPrefix("abc");
        Assert.assertEquals("Prefix should not influence title cache",
                expected, pers.getTitleCache());

        pers.setGivenName("First");
        Assert.assertEquals("Given name should not influence title cache if initials are set",
                expected, pers.getTitleCache());

        pers.setInitials(null);
        expected = "Last, F.";
        Assert.assertEquals("Initials should be computed from givenname if not set manually",
                expected, pers.getTitleCache());
    }


    @Test
    public final void testGetFullTitle(){
        Assert.assertNotNull("person1 full titlemust not to be null", person1.getFullTitle());
        Assert.assertEquals("Person1 full title should be created by elements",
                "Dr1. P1FN P1LN Suff1", person1.getFullTitle());
        person1.setSuffix(null);
        Assert.assertEquals("Person1 full title should be Dr1. P1FN P1LN",
                "Dr1. P1FN P1LN", person1.getFullTitle());
        //peson2
        Assert.assertEquals("Person2 full title should be P2NomT",
                "P2FN P2LN P2Suff", person2.getFullTitle());
        //person3
        Assert.assertNotNull("person3 full title must not to be null",
                person3.getFullTitle());
        Assert.assertTrue("Person3 full title must not be empty",
                StringUtils.isNotBlank(person3.getFullTitle()));
        //don't take to serious, may be also something different, but not empty
        Assert.assertEquals("Person3 full title should start with Person#0",
                "Person#0", person3.getFullTitle().substring(0, 8));
        person3.setGivenName("Klaus");
        Assert.assertEquals("Person3 full title should be Klaus",
                "Klaus", person3.getFullTitle());
    }

	@Test
    public final void testInitialsFromGivenName(){
	    PersonDefaultCacheStrategy formatter = PersonDefaultCacheStrategy.NewInstance();
	    boolean force = true;

	    String givenname = null;
        Assert.assertNull(formatter.getInitialsFromGivenName(givenname, force));

        givenname = "";
        Assert.assertEquals("", formatter.getInitialsFromGivenName(givenname, force));

        givenname = "  ";
        Assert.assertEquals("We expect blanks to be trimmed", "", formatter.getInitialsFromGivenName(givenname, force));

	    givenname = "John Michael ";
	    Assert.assertEquals("J.M.", formatter.getInitialsFromGivenName(givenname, force));

	    givenname = "Walter G.";
        Assert.assertEquals("W.G.", formatter.getInitialsFromGivenName(givenname, force));

        givenname = "A.L.";
        Assert.assertEquals("A.L.", formatter.getInitialsFromGivenName(givenname, force));

        givenname = "A.Ludw. W.";
        Assert.assertEquals("A.L.W.", formatter.getInitialsFromGivenName(givenname, force));

        givenname = "A. Ludw.  Norbert W.";
        Assert.assertEquals("A.L.N.W.", formatter.getInitialsFromGivenName(givenname, force));

        force = false;
        givenname = "A. Ludw.  Norbert W.";
        Assert.assertEquals("A.Ludw.N.W.", formatter.getInitialsFromGivenName(givenname, force));

        givenname = "W.-H.";
        Assert.assertEquals("W.-H.", formatter.getInitialsFromGivenName(givenname, force));
        givenname = "W.-Henning";
        Assert.assertEquals("W.-H.", formatter.getInitialsFromGivenName(givenname, force));
        givenname = "W.-Henn.";
        Assert.assertEquals("W.-Henn.", formatter.getInitialsFromGivenName(givenname, force));
        givenname = "Wolf-Henning";
        Assert.assertEquals("W.-H.", formatter.getInitialsFromGivenName(givenname, force));
        givenname = "Wolf\u2013 Henning";
        Assert.assertEquals("W.\u2013H.", formatter.getInitialsFromGivenName(givenname, force));


        givenname = "W";
        Assert.assertEquals("W.", formatter.getInitialsFromGivenName(givenname, force));

        givenname = "W K";
        Assert.assertEquals("W.K.", formatter.getInitialsFromGivenName(givenname, force));

        givenname = "WK";
        Assert.assertEquals("W.K.", formatter.getInitialsFromGivenName(givenname, force));

        givenname = "WKH";
        Assert.assertEquals("W.K.H.", formatter.getInitialsFromGivenName(givenname, force));


        //force
        force = true;
        givenname = "W.-H.";
        Assert.assertEquals("W.-H.", formatter.getInitialsFromGivenName(givenname, force));
        givenname = "W.-Henning";
        Assert.assertEquals("W.-H.", formatter.getInitialsFromGivenName(givenname, force));
        givenname = "W.-Henn.";
        Assert.assertEquals("W.-H.", formatter.getInitialsFromGivenName(givenname, force));
        givenname = "Wolf-Henning";
        Assert.assertEquals("W.-H.", formatter.getInitialsFromGivenName(givenname, force));

        givenname = "W";
        Assert.assertEquals("W.", formatter.getInitialsFromGivenName(givenname, force));

        givenname = "W K";
        Assert.assertEquals("W.K.", formatter.getInitialsFromGivenName(givenname, force));

        givenname = "WK";
        Assert.assertEquals("W.K.", formatter.getInitialsFromGivenName(givenname, force));

        givenname = "WKH";
        Assert.assertEquals("W.K.H.", formatter.getInitialsFromGivenName(givenname, force));

        force = false;
        givenname = "Pe. Y.";
        Assert.assertEquals("Pe.Y.", formatter.getInitialsFromGivenName(givenname, force));

        //brackets
        force = true;
        givenname = "Constantin (Konstantin) Georg Alexander";
        Assert.assertEquals("C.G.A.", formatter.getInitialsFromGivenName(givenname, force));
        givenname = "Franz (Joseph Andreas Nicolaus)";
        Assert.assertEquals("F.", formatter.getInitialsFromGivenName(givenname, force));
        givenname = "Viktor V. (W.W.)";
        Assert.assertEquals("V.V.", formatter.getInitialsFromGivenName(givenname, force));
        givenname = "(Georg Ferdinand) Otto";
        Assert.assertEquals("O.", formatter.getInitialsFromGivenName(givenname, force));
        givenname = "(Sébastien-) René";
        Assert.assertEquals("R.", formatter.getInitialsFromGivenName(givenname, force));
        givenname = "Joyce (M.) Chismore Lewin";
        Assert.assertEquals("J.C.L.", formatter.getInitialsFromGivenName(givenname, force));
        givenname = "Joyce (M.) Chismore Lewin";
        Assert.assertEquals("J.C.L.", formatter.getInitialsFromGivenName(givenname, force));

//      "Robert. K." wurde auf "Robert. K." gemapped

        //must not throw exception (exact result may change in future)
        givenname = "W.-H.-";
        Assert.assertEquals("W.-H.-", formatter.getInitialsFromGivenName(givenname, force));
        givenname = "W.-Hennin-";
        Assert.assertEquals("W.-H.-", formatter.getInitialsFromGivenName(givenname, force));

        force = false;
        givenname = "W.-H.-";
        Assert.assertEquals("W.-H.-", formatter.getInitialsFromGivenName(givenname, force));
        givenname = "W.-Hennin-";
        Assert.assertEquals("W.-H.-", formatter.getInitialsFromGivenName(givenname, force));

	}

}
