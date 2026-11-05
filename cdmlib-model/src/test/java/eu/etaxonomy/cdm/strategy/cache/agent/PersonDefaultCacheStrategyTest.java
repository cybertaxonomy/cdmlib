/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.model.agent.Person;

/**
 * @author a.mueller
 * @since 29.09.2009
 */
public class PersonDefaultCacheStrategyTest {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	private static Person person1;
	private static Person person2;
	private static Person person3;

	@BeforeEach
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
		assertNotNull(person1.getNomenclaturalTitleCache(), "person1 nomenclatural title must not to be null");
		assertEquals("P1FN, P.", person1.getNomenclaturalTitleCache(), "Person1 nomenclatural title should be taken from titleCache");
		person1.setSuffix(null);
		assertEquals("P1FN, P.", person1.getNomenclaturalTitleCache(), "Person1 title should be taken from titleCache");
		//peson2
		assertEquals("P2NomT", person2.getNomenclaturalTitleCache(), "Person2 title should be P2NomT");
		//person3
		assertNotNull(person3.getNomenclaturalTitleCache(), "person3 nomenclatural title must not to be null");
		assertTrue(StringUtils.isNotBlank(person3.getNomenclaturalTitleCache()), "Person3 nomenclatural title must not be empty");
		//don't take to serious, may be also something different, but not empty
		assertEquals("Person#0", person3.getNomenclaturalTitleCache().substring(0, 8), "Person3 title should start with Person#0");
	}

   @Test
    public final void testGetCollectorTitleCache(){
        assertNotNull(person1.getCollectorTitleCache(), "person1 collector title cache must not to be null");
        assertEquals("P. P1FN", person1.getCollectorTitleCache(), "Person1 collector title cache should be taken from titleCache");
        person1.setSuffix(null);
        assertEquals("P. P1FN", person1.getCollectorTitleCache(), "Person1 collector title cache should be taken from titleCache");
        //peson2
        assertEquals("P2CT", person2.getCollectorTitleCache(), "Person2 collector title cache should be P2CT");
        //person3
        assertNotNull(person3.getCollectorTitleCache(), "person3 collector title cache must not to be null");
        assertTrue(StringUtils.isNotBlank(person3.getCollectorTitleCache()), "Person3 collector title cache must not be empty");
        //don't take to serious, may be also something different, but not empty
        assertEquals("Person#0", person3.getCollectorTitleCache().substring(0, 8), "Person3 title should start with Person#0");
    }

	@Test
	public final void testGetTitleCacheAdaptedFromOldVersion(){
	    assertNotNull(person1.getTitleCache(), "person1 title cache must not to be null");
		assertEquals("P1FN, P.", person1.getTitleCache(), "Person1 title cache should be created by familyname and computed initials");
		person1.setSuffix(null);
		assertEquals("P1FN, P.", person1.getTitleCache(), "Person1 title cache should be Dr1. P1GN P1FN");
		//peson2
		assertEquals("P2FN, P.", person2.getTitleCache(), "Person2 title cache should be P2NomT");
		//person3
		assertNotNull(person3.getTitleCache(), "person3 title cache must not to be null");
		assertTrue(StringUtils.isNotBlank(person3.getTitleCache()), "Person3 title cache must not be empty");
		//don't take to serious, may be also something different, but not empty
		assertEquals("Person#0", person3.getTitleCache().substring(0, 8), "Person3 title cache should start with Person#0");
		person3.setGivenName("Klaus");
		assertEquals("K.", person3.getTitleCache(), "Person3 title cache should be Klaus");
	}

	@Test
    public final void testGetTitleCache(){
        Person pers = Person.NewInstance();
        pers.setFamilyName("Last ");  //family should be trimmed during titleCache generation
        pers.setInitials("E.M.");

        String expected = "Last, E.M.";
	    assertNotNull(pers.getTitleCache(), "pers title cache must not to be null");
        assertEquals(expected, pers.getTitleCache(), "pers title cache should be created by familyname and initials");

        pers.setSuffix("xyz");
        assertEquals(expected, pers.getTitleCache(), "Suffix should not influence title cache");
        pers.setPrefix("abc");
        assertEquals(expected, pers.getTitleCache(), "Prefix should not influence title cache");

        pers.setGivenName("First");
        assertEquals(expected, pers.getTitleCache(), "Given name should not influence title cache if initials are set");

        pers.setInitials(null);
        expected = "Last, F.";
        assertEquals(expected, pers.getTitleCache(), "Initials should be computed from givenname if not set manually");
    }

    @Test
    public final void testGetFullTitle(){
        assertNotNull(person1.getFullTitle(), "person1 full titlemust not to be null");
        assertEquals("Dr1. P1GN P1FN Suff1", person1.getFullTitle(), "Person1 full title should be created by elements");
        person1.setSuffix(null);
        assertEquals("Dr1. P1GN P1FN", person1.getFullTitle(), "Person1 full title should be Dr1. P1GN P1FN");
        //peson2
        assertEquals("P2GN P2FN P2Suff", person2.getFullTitle(), "Person2 full title should be P2NomT");
        //person3
        assertNotNull(person3.getFullTitle(), "person3 full title must not to be null");
        assertTrue(StringUtils.isNotBlank(person3.getFullTitle()), "Person3 full title must not be empty");
        //don't take to serious, may be also something different, but not empty
        assertEquals("Person#0", person3.getFullTitle().substring(0, 8), "Person3 full title should start with Person#0");
        person3.setGivenName("Klaus");
        assertEquals("Klaus", person3.getFullTitle(), "Person3 full title should be Klaus");
    }

	@Test
    public final void testInitialsFromGivenName(){
	    PersonDefaultCacheStrategy formatter = PersonDefaultCacheStrategy.NewInstance();
	    boolean force = true;

	    String givenname = null;
        assertNull(formatter.getInitialsFromGivenName(givenname, force));

        givenname = "";
        assertNull(formatter.getInitialsFromGivenName(givenname, force));

        givenname = "  ";
        assertNull(formatter.getInitialsFromGivenName(givenname, force), "We expect blanks to be trimmed");

	    givenname = "John Michael ";
	    assertEquals("J.M.", formatter.getInitialsFromGivenName(givenname, force));

	    givenname = "Walter G.";
        assertEquals("W.G.", formatter.getInitialsFromGivenName(givenname, force));

        givenname = "A.L.";
        assertEquals("A.L.", formatter.getInitialsFromGivenName(givenname, force));

        givenname = "A.Ludw. W.";
        assertEquals("A.L.W.", formatter.getInitialsFromGivenName(givenname, force));

        givenname = "A. Ludw.  Norbert W.";
        assertEquals("A.L.N.W.", formatter.getInitialsFromGivenName(givenname, force));

        force = false;
        givenname = "A. Ludw.  Norbert W.";
        assertEquals("A.Ludw.N.W.", formatter.getInitialsFromGivenName(givenname, force));

        givenname = "W.-H.";
        assertEquals("W.-H.", formatter.getInitialsFromGivenName(givenname, force));
        givenname = "W.-Henning";
        assertEquals("W.-H.", formatter.getInitialsFromGivenName(givenname, force));
        givenname = "W.-Henn.";
        assertEquals("W.-Henn.", formatter.getInitialsFromGivenName(givenname, force));
        givenname = "Wolf-Henning";
        assertEquals("W.-H.", formatter.getInitialsFromGivenName(givenname, force));
        givenname = "Wolf\u2013 Henning";
        assertEquals("W.\u2013H.", formatter.getInitialsFromGivenName(givenname, force));

        givenname = "W";
        assertEquals("W.", formatter.getInitialsFromGivenName(givenname, force));
        givenname = "W K";
        assertEquals("W.K.", formatter.getInitialsFromGivenName(givenname, force));
        givenname = "WK";
        assertEquals("W.K.", formatter.getInitialsFromGivenName(givenname, force));
        givenname = "WKH";
        assertEquals("W.K.H.", formatter.getInitialsFromGivenName(givenname, force));

        //force
        force = true;
        givenname = "W.-H.";
        assertEquals("W.-H.", formatter.getInitialsFromGivenName(givenname, force));
        givenname = "W.-Henning";
        assertEquals("W.-H.", formatter.getInitialsFromGivenName(givenname, force));
        givenname = "W.-Henn.";
        assertEquals("W.-H.", formatter.getInitialsFromGivenName(givenname, force));
        givenname = "Wolf-Henning";
        assertEquals("W.-H.", formatter.getInitialsFromGivenName(givenname, force));

        givenname = "W";
        assertEquals("W.", formatter.getInitialsFromGivenName(givenname, force));
        givenname = "W K";
        assertEquals("W.K.", formatter.getInitialsFromGivenName(givenname, force));
        givenname = "WK";
        assertEquals("W.K.", formatter.getInitialsFromGivenName(givenname, force));
        givenname = "WKH";
        assertEquals("W.K.H.", formatter.getInitialsFromGivenName(givenname, force));
        force = false;
        givenname = "Pe. Y.";
        assertEquals("Pe.Y.", formatter.getInitialsFromGivenName(givenname, force));

        //brackets
        force = true;
        givenname = "Constantin (Konstantin) Georg Alexander";
        assertEquals("C.G.A.", formatter.getInitialsFromGivenName(givenname, force));
        givenname = "Franz (Joseph Andreas Nicolaus)";
        assertEquals("F.", formatter.getInitialsFromGivenName(givenname, force));
        givenname = "Viktor V. (W.W.)";
        assertEquals("V.V.", formatter.getInitialsFromGivenName(givenname, force));
        givenname = "(Georg Ferdinand) Otto";
        assertEquals("O.", formatter.getInitialsFromGivenName(givenname, force));
        givenname = "(Sébastien-) René";
        assertEquals("R.", formatter.getInitialsFromGivenName(givenname, force));
        givenname = "Joyce (M.) Chismore Lewin";
        assertEquals("J.C.L.", formatter.getInitialsFromGivenName(givenname, force));
        givenname = "Joyce (M.) Chismore Lewin";
        assertEquals("J.C.L.", formatter.getInitialsFromGivenName(givenname, force));

//      "Robert. K." wurde auf "Robert. K." gemapped

        //must not throw exception (exact result may change in future)
        givenname = "W.-H.-";
        assertEquals("W.-H.-", formatter.getInitialsFromGivenName(givenname, force));
        givenname = "W.-Hennin-";
        assertEquals("W.-H.-", formatter.getInitialsFromGivenName(givenname, force));

        force = false;
        givenname = "W.-H.-";
        assertEquals("W.-H.-", formatter.getInitialsFromGivenName(givenname, force));
        givenname = "W.-Hennin-";
        assertEquals("W.-H.-", formatter.getInitialsFromGivenName(givenname, force));

        givenname = "(Brother)"; //example from Salvador DB
        assertNull(formatter.getInitialsFromGivenName(givenname, force), "Non-parsable string should not be empty but null");
	}

	@Test
    public final void testUpdateCaches(){
	    assertEquals("P1FN, P.", person1.getTitleCache());
	    assertEquals("P1FN, P.", person1.getNomenclaturalTitleCache());
	    assertEquals("P. P1FN", person1.getCollectorTitleCache());
        person1.setTitleCache("protected cache", true);
        assertEquals("protected cache", person1.getTitleCache());
        assertEquals("protected cache", person1.getNomenclaturalTitleCache());
        assertEquals("protected cache", person1.getCollectorTitleCache());

        person3.setNomenclaturalTitle("nom title");
        assertEquals("nom title", person3.getTitleCache());
        assertEquals("nom title", person3.getNomenclaturalTitleCache());
        assertEquals("nom title", person3.getCollectorTitleCache());

        Person person4 = Person.NewInstance();
        person4.setCollectorTitle("collector title");
        assertEquals("collector title", person4.getTitleCache());
        assertEquals("collector title", person4.getNomenclaturalTitleCache());
        assertEquals("collector title", person4.getCollectorTitleCache());
	}
}
