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

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Person;

/**
 * @author a.mueller
 * @since 29.09.2009
 */
public class PersonDefaultCacheStrategyTest {

    @SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(PersonDefaultCacheStrategyTest.class);

	private static Person person1;
	private static Person person2;
	private static Person person3;

	@Before
	public void setUp() throws Exception {
		person1 = Person.NewInstance();

		person1.setGivenName("P1GN");
		person1.setFamilyName("P1FN");
		person1.setPrefix("Dr1.");
		person1.setSuffix("Suff1");

		person2 = Person.NewInstance();
		person2.setNomenclaturalTitle("P2NomT");
		person2.setFamilyName("P2FN");
		person2.setGivenName("P2GN");
		person2.setSuffix("P2Suff");
		person2.setCollectorTitle("P2CT");

		person3 = Person.NewInstance(); //empty person
	}

//**************************************** TESTS **************************************

	@Test
	public final void testNewInstance() {
		PersonDefaultCacheStrategy cacheStrategy = PersonDefaultCacheStrategy.NewInstance();
		assertNotNull(cacheStrategy);
	}

	@Test
	public final void testGetNomenclaturalTitleCache(){
		Assert.assertNotNull("person1 nomenclatural title must not to be null", person1.getNomenclaturalTitleCache());
		Assert.assertEquals("Person1 nomenclatural title should be taken from titleCache", "P1FN, P.", person1.getNomenclaturalTitleCache());
		person1.setSuffix(null);
		Assert.assertEquals("Person1 title should be taken from titleCache", "P1FN, P.", person1.getNomenclaturalTitleCache());
		//peson2
		Assert.assertEquals("Person2 title should be P2NomT", "P2NomT", person2.getNomenclaturalTitleCache());
		//person3
		Assert.assertNotNull("person3 nomenclatural title must not to be null", person3.getNomenclaturalTitleCache());
		Assert.assertTrue("Person3 nomenclatural title must not be empty", StringUtils.isNotBlank(person3.getNomenclaturalTitleCache()));
		//don't take to serious, may be also something different, but not empty
		Assert.assertEquals("Person3 title should start with Person#0", "Person#0", person3.getNomenclaturalTitleCache().substring(0, 8));
	}

   @Test
    public final void testGetCollectorTitleCache(){
        Assert.assertNotNull("person1 collector title cache must not to be null", person1.getCollectorTitleCache());
        Assert.assertEquals("Person1 collector title cache should be taken from titleCache", "P. P1FN", person1.getCollectorTitleCache());
        person1.setSuffix(null);
        Assert.assertEquals("Person1 collector title cache should be taken from titleCache", "P. P1FN", person1.getCollectorTitleCache());
        //peson2
        Assert.assertEquals("Person2 collector title cache should be P2CT", "P2CT", person2.getCollectorTitleCache());
        //person3
        Assert.assertNotNull("person3 collector title cache must not to be null", person3.getCollectorTitleCache());
        Assert.assertTrue("Person3 collector title cache must not be empty", StringUtils.isNotBlank(person3.getCollectorTitleCache()));
        //don't take to serious, may be also something different, but not empty
        Assert.assertEquals("Person3 title should start with Person#0", "Person#0", person3.getCollectorTitleCache().substring(0, 8));
    }

	@Test
	public final void testGetTitleCacheAdaptedFromOldVersion(){
	    Assert.assertNotNull("person1 title cache must not to be null", person1.getTitleCache());
		Assert.assertEquals("Person1 title cache should be created by familyname and computed initials", "P1FN, P.", person1.getTitleCache());
		person1.setSuffix(null);
		Assert.assertEquals("Person1 title cache should be Dr1. P1GN P1FN", "P1FN, P.", person1.getTitleCache());
		//peson2
		Assert.assertEquals("Person2 title cache should be P2NomT", "P2FN, P.", person2.getTitleCache());
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
        pers.setFamilyName("Last ");  //family should be trimmed during titleCache generation
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
                "Dr1. P1GN P1FN Suff1", person1.getFullTitle());
        person1.setSuffix(null);
        Assert.assertEquals("Person1 full title should be Dr1. P1GN P1FN",
                "Dr1. P1GN P1FN", person1.getFullTitle());
        //peson2
        Assert.assertEquals("Person2 full title should be P2NomT",
                "P2GN P2FN P2Suff", person2.getFullTitle());
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
        Assert.assertNull(formatter.getInitialsFromGivenName(givenname, force));

        givenname = "  ";
        Assert.assertNull("We expect blanks to be trimmed", formatter.getInitialsFromGivenName(givenname, force));

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

        givenname = "(Brother)"; //example from Salvador DB
        Assert.assertNull("Non-parsable string should not be empty but null", formatter.getInitialsFromGivenName(givenname, force));
	}

	@Test
    public final void testUpdateCaches(){
	    Assert.assertEquals("P1FN, P.", person1.getTitleCache());
	    Assert.assertEquals("P1FN, P.", person1.getNomenclaturalTitleCache());
	    Assert.assertEquals("P. P1FN", person1.getCollectorTitleCache());
        person1.setTitleCache("protected cache", true);
        Assert.assertEquals("protected cache", person1.getTitleCache());
        Assert.assertEquals("protected cache", person1.getNomenclaturalTitleCache());
        Assert.assertEquals("protected cache", person1.getCollectorTitleCache());

        person3.setNomenclaturalTitle("nom title");
        Assert.assertEquals("nom title", person3.getTitleCache());
        Assert.assertEquals("nom title", person3.getNomenclaturalTitleCache());
        Assert.assertEquals("nom title", person3.getCollectorTitleCache());

        Person person4 = Person.NewInstance();
        person4.setCollectorTitle("collector title");
        Assert.assertEquals("collector title", person4.getTitleCache());
        Assert.assertEquals("collector title", person4.getNomenclaturalTitleCache());
        Assert.assertEquals("collector title", person4.getCollectorTitleCache());
	}
}