// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.match;

import java.net.URI;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Contact;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.IBook;
import eu.etaxonomy.cdm.model.reference.IBookSection;
import eu.etaxonomy.cdm.model.reference.IPrintSeries;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @created 03.08.2009
 * @version 1.0
 */
public class DefaultMatchStrategyTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DefaultMatchStrategyTest.class);

	private DefaultMatchStrategy matchStrategy;
	private IBook book1;
	private String editionString1 ="Ed.1";
	private String volumeString1 ="Vol.1";
	private Team team1;
	private IPrintSeries printSeries1;
	private Annotation annotation1;
	private String title1 = "Title1";
	private TimePeriod datePublished1 = TimePeriod.NewInstance(2000);
	private int hasProblem1 = 1;
	private LSID lsid1;

	private IBook book2;
	private String editionString2 ="Ed.2";
	private String volumeString2 ="Vol.2";
	private Team team2;
	private IPrintSeries printSeries2;
	private Annotation annotation2;
	private String annotationString2;
	private String title2 = "Title2";
	private DateTime created2 = new DateTime(1999, 3, 1, 0, 0, 0, 0);
	private TimePeriod datePublished2 = TimePeriod.NewInstance(2002);
	private int hasProblem2 = 1;
	private LSID lsid2;

	private IBook book3;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		DefaultTermInitializer termInitializer = new DefaultTermInitializer();
		termInitializer.initialize();
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
		team1 = Team.NewInstance();
		team1.setTitleCache("Team1",true);
		team2 = Team.NewInstance();
		team2.setTitleCache("Team2",true);
		printSeries1 = ReferenceFactory.newPrintSeries("Series1");
		printSeries1.setTitle("print series");
		printSeries2 = ReferenceFactory.newPrintSeries("Series2");
		annotation1 = Annotation.NewInstance("Annotation1", null);
		annotationString2 = "Annotation2";
		annotation2 = Annotation.NewInstance(annotationString2, null);

		book1 = ReferenceFactory.newBook();
		book1.setAuthorship(team1);
		book1.setTitle(title1);
		book1.setEdition(editionString1);
		book1.setVolume(volumeString1);
		book1.setInSeries(printSeries1);
		((AnnotatableEntity) book1).addAnnotation(annotation1);
		book1.setDatePublished(datePublished1);
		book1.setParsingProblem(hasProblem1);
		lsid1 = new LSID("authority1", "namespace1", "object1", "revision1");
		book1.setLsid(lsid1);
		((Reference) book1).setNomenclaturallyRelevant(false);

		book2 = ReferenceFactory.newBook();
		book2.setAuthorship(team2);
		book2.setTitle(title2);
		book2.setEdition(editionString2);
		book2.setVolume(volumeString2);
		book2.setInSeries(printSeries2);
		( (AnnotatableEntity) book2).addAnnotation(annotation2);
		book2.setCreated(created2);
		book2.setDatePublished(datePublished2);
		book2.setParsingProblem(hasProblem2);
		lsid2 = new LSID("authority2", "namespace2", "object2", "revision2");
		book2.setLsid(lsid2);
		((Reference) book2).setNomenclaturallyRelevant(true);

	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

//********************* TEST *********************************************/

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.match.DefaultMatchStrategy#NewInstance(java.lang.Class)}.
	 */
	@Test
	public void testNewInstance() {
		matchStrategy = DefaultMatchStrategy.NewInstance(Reference.class);
		Assert.assertNotNull(matchStrategy);
		Assert.assertEquals(Reference.class, matchStrategy.getMatchClass());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.match.DefaultMatchStrategy#getMatchMode(java.lang.String)}.
	 */
	@Test
	public void testGetMatchMode() {
		matchStrategy = DefaultMatchStrategy.NewInstance(Reference.class);
		Assert.assertEquals("Match mode for isbn should be MatchMode.EQUAL_", MatchMode.EQUAL, matchStrategy.getMatchMode("isbn"));
		Assert.assertEquals("Match mode for title should be MatchMode.EQUAL", MatchMode.EQUAL_REQUIRED, matchStrategy.getMatchMode("title"));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.match.DefaultMatchStrategy#getMatchMode(java.lang.String)}.
	 */
	@Test
	public void testGetSetMatchMode() {
		//legal value
		try {
			matchStrategy = DefaultMatchStrategy.NewInstance(Reference.class);
			matchStrategy.setMatchMode("edition", MatchMode.EQUAL_REQUIRED);
			Assert.assertEquals("Match mode for edition should be", MatchMode.EQUAL_REQUIRED, matchStrategy.getMatchMode("edition"));
		} catch (MatchException e1) {
			Assert.fail();
		}
		//illegalValue
		try {
			matchStrategy.setMatchMode("xxx", MatchMode.EQUAL_REQUIRED);
			Assert.fail("A property name must exist, otherwise an exception must be thrown");
		} catch (Exception e) {
			//ok
		}
		//illegalValue
		try {
			matchStrategy.setMatchMode("cacheStrategy", MatchMode.EQUAL_REQUIRED);
			Assert.fail("CacheStrategy is transient and therefore not a legal match parameter");
		} catch (Exception e) {
			//ok
		}
	}

	@Test
	public void testInvokeCache() throws MatchException {
		matchStrategy = DefaultMatchStrategy.NewInstance(Reference.class);
		Assert.assertTrue("Same object should always match", matchStrategy.invoke(book1, book1));

		IBook bookClone = (IBook) book1.clone();
		Assert.assertTrue("Cloned book should match", matchStrategy.invoke(book1, bookClone));
		book1.setTitleCache("cache1",true);
		Assert.assertFalse("Cached book should not match", matchStrategy.invoke(book1, bookClone));

		bookClone.setTitleCache("cache1",true);
		Assert.assertTrue("Cached book with same cache should match", matchStrategy.invoke(book1, bookClone));

		bookClone.setTitleCache("cache2",true);
		Assert.assertFalse("Cached book with differings caches should not match", matchStrategy.invoke(book1, bookClone));
		bookClone.setTitleCache("cache1",true); //restore

		bookClone.setEdition(null);
		Assert.assertTrue("Cached book with a defined and a null edition should match", matchStrategy.invoke(book1, bookClone));

		matchStrategy = DefaultMatchStrategy.NewInstance(BotanicalName.class);
		BotanicalName botName1 = BotanicalName.NewInstance(Rank.GENUS());
		BotanicalName botName2 = BotanicalName.NewInstance(Rank.GENUS());
		Assert.assertNotNull("Rank should not be null", botName1.getRank());

		botName1.setGenusOrUninomial("Genus1");
		botName2.setGenusOrUninomial("Genus1");
		Assert.assertTrue("Names with equal genus should match", matchStrategy.invoke(botName1, botName2));

		botName1.setCombinationAuthorship(team1);
		botName2.setCombinationAuthorship(null);
		Assert.assertFalse("Names one having an author the other one not should not match", matchStrategy.invoke(botName1, botName2));

		botName1.setAuthorshipCache("authorCache1");
		botName2.setAuthorshipCache("authorCache1");
		Assert.assertTrue("Names with cached authors should match", matchStrategy.invoke(botName1, botName2));

	}


	@Test
	public void testInvokeReferences() throws MatchException {
		matchStrategy = DefaultMatchStrategy.NewInstance(Reference.class);
		Assert.assertTrue("Same object should always match", matchStrategy.invoke(book1, book1));

		IBook bookClone = (IBook) ((Reference) book1).clone();
		Assert.assertTrue("Cloned book should match", matchStrategy.invoke(book1, bookClone));
		bookClone.setTitle("Any title");
		Assert.assertFalse("Books with differing titles should not match", matchStrategy.invoke(book1, bookClone));
		String originalTitle = book1.getTitle();
		bookClone.setTitle(originalTitle);
		Assert.assertTrue("Cloned book should match", matchStrategy.invoke(book1, bookClone));
		book1.setTitle(null);
		bookClone.setTitle(null);
		Assert.assertFalse("Books with no title should not match", matchStrategy.invoke(book1, bookClone));
		book1.setTitle(originalTitle);
		bookClone.setTitle(originalTitle);


		bookClone.setInSeries(printSeries2);
		Assert.assertFalse("Cloned book with differing print series should not match", matchStrategy.invoke(book1, bookClone));
		IPrintSeries seriesClone = (IPrintSeries)((Reference)printSeries1).clone();
		bookClone.setInSeries(seriesClone);
		Assert.assertTrue("Cloned book with cloned bookSeries should match", matchStrategy.invoke(book1, bookClone));
		seriesClone.setTitle("Another title");
		Assert.assertFalse("Cloned book should not match with differing series title", matchStrategy.invoke(book1, bookClone));
		bookClone.setInSeries(printSeries1);
		Assert.assertTrue("Original printSeries should match", matchStrategy.invoke(book1, bookClone));

		IBook bookTitle1 = ReferenceFactory.newBook();
		IBook bookTitle2 = ReferenceFactory.newBook();
		Assert.assertFalse("Books without title should not match", matchStrategy.invoke(bookTitle1, bookTitle2));
		String title = "Any title";
		bookTitle1.setTitle(title);
		bookTitle2.setTitle(title);
		Assert.assertTrue("Books with same title (not empty) should match", matchStrategy.invoke(bookTitle1, bookTitle2));
		bookTitle1.setTitle("");
		bookTitle2.setTitle("");
		Assert.assertFalse("Books with empty title should not match", matchStrategy.invoke(bookTitle1, bookTitle2));

		//Time period
		bookTitle1.setTitle(title);
		bookTitle2.setTitle(title);
		bookTitle1.setDatePublished(TimePeriod.NewInstance(1999, 2002));
		Assert.assertFalse("Books with differing publication dates should not match", matchStrategy.invoke(bookTitle1, bookTitle2));
		bookTitle2.setDatePublished(TimePeriod.NewInstance(1998));
		Assert.assertFalse("Books with differing publication dates should not match", matchStrategy.invoke(bookTitle1, bookTitle2));
		bookTitle2.setDatePublished(TimePeriod.NewInstance(1999));
		Assert.assertFalse("Books with differing publication dates should not match", matchStrategy.invoke(bookTitle1, bookTitle2));
		bookTitle2.setDatePublished(TimePeriod.NewInstance(1999, 2002));
		Assert.assertTrue("Books with same publication dates should match", matchStrategy.invoke(bookTitle1, bookTitle2));

		//BookSection
		IBookSection section1 = ReferenceFactory.newBookSection();
		section1.setInBook(bookTitle1);
		section1.setTitle("SecTitle");
		section1.setPages("22-33");
		IBookSection section2 = ReferenceFactory.newBookSection();
		section2.setInBook(bookTitle1);
		section2.setTitle("SecTitle");
		section2.setPages("22-33");


		IMatchStrategy bookSectionMatchStrategy = DefaultMatchStrategy.NewInstance(Reference.class);
		Assert.assertTrue("Equal BookSections should match", bookSectionMatchStrategy.invoke(section1, section2));
		section2.setInBook(bookTitle2);
		Assert.assertTrue("Matching books should result in matching book sections", bookSectionMatchStrategy.invoke(section1, section2));
		bookTitle2.setPages("xx");
		Assert.assertFalse("Sections with differing books should not match", bookSectionMatchStrategy.invoke(section1, section2));
		//restore
		bookTitle2.setPages(null);
		Assert.assertTrue("Matching books should result in matching book sections", bookSectionMatchStrategy.invoke(section1, section2));
		printSeries2.setTitle("A new series title");
		IMatchStrategy printSeriesMatchStrategy = DefaultMatchStrategy.NewInstance(Reference.class);
		Assert.assertFalse("Print series with differing titles should not match", printSeriesMatchStrategy.invoke(printSeries1, printSeries2));
		bookTitle1.setInSeries(printSeries1);
		bookTitle2.setInSeries(printSeries2);
		Assert.assertFalse("Books with not matching in series should not match", matchStrategy.invoke(bookTitle1, bookTitle2));
		Assert.assertFalse("Sections with differing print series should not match", bookSectionMatchStrategy.invoke(section1, section2));

		//Authorship
		Person person1 = Person.NewTitledInstance("person");
		Person person2 = Person.NewTitledInstance("person");

		person1.setPrefix("pre1");
		person2.setPrefix("pre2");
		book2= (IBook) ((Reference) book1).clone();

		Assert.assertTrue("Equal books should match", matchStrategy.invoke(book1, book2));

		book1.setAuthorship(person1);
		book2.setAuthorship(person1);
		Assert.assertTrue("Books with same author should match", matchStrategy.invoke(book1, book2));

		book2.setAuthorship(person2);
		Assert.assertFalse("Books with different authors should not match", matchStrategy.invoke(book1, book2));

		person2.setPrefix("pre1");
		Assert.assertTrue("Books with matching authors should not match", matchStrategy.invoke(book1, book2));
	}


	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.match.DefaultMatchStrategy#invoke(eu.etaxonomy.cdm.strategy.match.IMergable, eu.etaxonomy.cdm.strategy.match.IMergable)}.
	 * @throws MatchException
	 */
	@Test
	public void testInvokeTaxonNames() throws MatchException {
		matchStrategy = DefaultMatchStrategy.NewInstance(BotanicalName.class);

		BotanicalName botName1 = BotanicalName.NewInstance(Rank.SPECIES());
		BotanicalName botName2 = BotanicalName.NewInstance(Rank.SPECIES());
		BotanicalName botName3 = BotanicalName.NewInstance(Rank.SPECIES());

		Assert.assertFalse("Names without title should not match", matchStrategy.invoke(botName1, botName2));

		botName1.setGenusOrUninomial("Genus1");
		botName1.setSpecificEpithet("species1");

		botName2.setGenusOrUninomial("Genus1");
		botName2.setSpecificEpithet("species1");
		Assert.assertTrue("Similar names with titles set should match", matchStrategy.invoke(botName1, botName2));


		botName1.setAnamorphic(true);
		botName2.setAnamorphic(false);
		Assert.assertFalse("Similar names with differing boolean marker values should not match", matchStrategy.invoke(botName1, botName2));
		botName2.setAnamorphic(true);
		Assert.assertTrue("Similar names with same boolean marker values should match", matchStrategy.invoke(botName1, botName2));



//		//name relations
//		botName2.addBasionym(botName3, book1, "p.22", null);
//		Specimen specimen1 = Specimen.NewInstance();
//		botName2.addSpecimenTypeDesignation(specimen1, SpecimenTypeDesignationStatus.HOLOTYPE(), book2, "p.56", "originalNameString", false, true);
//
//		//descriptions
//		TaxonNameDescription description1 = TaxonNameDescription.NewInstance();
//		botName1.addDescription(description1);
//		TaxonNameDescription description2 = TaxonNameDescription.NewInstance();
//		botName2.addDescription(description2);

		//authors
		Team team1 = Team.NewInstance();
		Team team2 = Team.NewInstance();
		botName1.setCombinationAuthorship(team1);
		botName2.setCombinationAuthorship(team2);
		Assert.assertTrue("Similar teams should match", DefaultMatchStrategy.NewInstance(Team.class).invoke(team1, team2));
		Assert.assertTrue("Similar names with matching authors should match", matchStrategy.invoke(botName1, botName2));

	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.match.DefaultMatchStrategy#invoke(IMatchable, IMatchable), eu.etaxonomy.cdm.strategy.match.IMatchable)}.
	 * @throws MatchException
	 */
	@Test
	public void testInvokeAgents() throws MatchException {
		IMatchStrategy matchStrategy = DefaultMatchStrategy.NewInstance(Team.class);

		Team team1 = Team.NewInstance();
		Team team2 = Team.NewInstance();
		Team team3 = Team.NewInstance();

		Assert.assertTrue("Teams should match", matchStrategy.invoke(team1, team2));
		Assert.assertTrue("Teams should match", matchStrategy.invoke(team1, team3));

		String street1 = "Strasse1";
		team1.setContact(Contact.NewInstance(street1, "12345", "Berlin", Country.ARGENTINAARGENTINEREPUBLIC(),"pobox" , "Region", "a@b.de", "f12345", "+49-30-123456", URI.create("www.abc.de"), Point.NewInstance(2.4, 3.2, ReferenceSystem.WGS84(), 3)));
		team2.setContact(Contact.NewInstance("Street2", null, "London", null, null, null, null, "874599873", null, null, null));
		Assert.assertTrue("Contacts should be ignoredin default match strategy", matchStrategy.invoke(team1, team2));

		team1.setNomenclaturalTitle("nomTitle1");
		team2.setNomenclaturalTitle("nomTitle2");
		Assert.assertFalse("Agents with differing nomenclatural titles should not match", matchStrategy.invoke(team1, team2));
		//restore
		team2.setNomenclaturalTitle("nomTitle1");
		Assert.assertTrue("Agents with equal nomenclatural titles should match", matchStrategy.invoke(team1, team2));


		Person person1 = Person.NewTitledInstance("person1");
		person1.setProtectedTitleCache(true);
		Person person2 = Person.NewTitledInstance("person2");
		person2.setProtectedTitleCache(true);
		Person person3 = Person.NewTitledInstance("person3");
		person3.setProtectedTitleCache(true);

		team1.addTeamMember(person1);
		team2.addTeamMember(person1);
		Assert.assertTrue("Teams with same team members should match", matchStrategy.invoke(team1, team2));

		team1.addTeamMember(person2);
		Assert.assertFalse("Teams with differing team members should not match", matchStrategy.invoke(team1, team2));
		team2.addTeamMember(person2);
		Assert.assertTrue("Teams with same team members should match", matchStrategy.invoke(team1, team2));
		team2.removeTeamMember(person2);
		person3.setPrefix("pre3");
		team2.addTeamMember(person3);
		Assert.assertFalse("Teams with differing team members should not match", matchStrategy.invoke(team1, team2));
		person3.setTitleCache(person2.getTitleCache(),true);
		person2.setPrefix("pre3");
		Assert.assertTrue("Teams with matching members in right order should match", matchStrategy.invoke(team1, team2));
		team2.removeTeamMember(person1);
		team2.addTeamMember(person1);
		Assert.assertFalse("Teams with matching members in wrong order should not match", matchStrategy.invoke(team1, team2));

	}

	@Test
	public void testInvokeTeamOrPersonBase(){

	}

}
