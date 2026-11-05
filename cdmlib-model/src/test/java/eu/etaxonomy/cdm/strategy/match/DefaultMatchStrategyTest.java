/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.match;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.ZonedDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.common.DateTimeUtil;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.VerbatimTimePeriod;
import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.IBook;
import eu.etaxonomy.cdm.model.reference.IBookSection;
import eu.etaxonomy.cdm.model.reference.IPrintSeries;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.test.TermTestBase;

/**
 * @author a.mueller
 * @since 03.08.2009
 */
public class DefaultMatchStrategyTest extends TermTestBase {

    @SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	private DefaultMatchStrategy matchStrategy;
	private IBook book1;
	private String editionString1 ="Ed.1";
	private String volumeString1 ="Vol.1";
	private Team team1;
	private IPrintSeries printSeries1;
	private Annotation annotation1;
	private String title1 = "Title1";
	private VerbatimTimePeriod datePublished1 = VerbatimTimePeriod.NewVerbatimInstance(2000);
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
	private ZonedDateTime created2 = DateTimeUtil.of(1999, 3, 1);
	private VerbatimTimePeriod datePublished2 = VerbatimTimePeriod.NewVerbatimInstance(2002);
	private int hasProblem2 = 1;
	private LSID lsid2;

	@BeforeEach
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
	}

//********************* TEST *********************************************/

	@Test
	public void testNewInstance() {
		matchStrategy = DefaultMatchStrategy.NewInstance(Reference.class);
		assertNotNull(matchStrategy);
//		assertEquals(Reference.class, matchStrategy.getMatchClass());
	}

	@Test
	public void testGetMatchMode() {
		matchStrategy = DefaultMatchStrategy.NewInstance(Reference.class);
//		assertEquals(MatchMode.EQUAL, matchStrategy.getMatchMode("isbn"), "Match mode for isbn should be MatchMode.EQUAL_");
//		assertEquals(MatchMode.EQUAL_REQUIRED, matchStrategy.getMatchMode("title"), "Match mode for title should be MatchMode.EQUAL_REQUIRED");
	}

	@Test
	public void testGetSetMatchMode() {
		//legal value
		try {
			matchStrategy = DefaultMatchStrategy.NewInstance(Reference.class);
			matchStrategy.setMatchMode("edition", MatchMode.EQUAL_REQUIRED);

			matchStrategy.getMatching().getFieldMatcher("edition");
//			assertEquals(MatchMode.EQUAL_REQUIRED, matchStrategy.getMatchMode("edition"), "Match mode for edition should be");
		} catch (MatchException e1) {
			fail();
		}
		//illegalValue
		try {
			matchStrategy.setMatchMode("xxx", MatchMode.EQUAL_REQUIRED);
			fail("A property name must exist, otherwise an exception must be thrown");
		} catch (Exception e) {
			//ok
		}
		//illegalValue
		try {
			matchStrategy.setMatchMode("cacheStrategy", MatchMode.EQUAL_REQUIRED);
			fail("CacheStrategy is transient and therefore not a legal match parameter");
		} catch (Exception e) {
			//ok
		}
	}

	@Test
	public void testInvokeCache() throws MatchException {
		matchStrategy = DefaultMatchStrategy.NewInstance(Reference.class);
		assertTrue(matchStrategy.invoke(book1, book1).isSuccessful(), "Same object should always match");

		IBook bookClone = (IBook) book1.clone();
		assertTrue(matchStrategy.invoke(book1, bookClone).isSuccessful(), "Cloned book should match");
		book1.setTitleCache("cache1",true);
		assertFalse(matchStrategy.invoke(book1, bookClone).isSuccessful(), "Cached book should not match");

		bookClone.setTitleCache("cache1",true);
		assertTrue(matchStrategy.invoke(book1, bookClone).isSuccessful(), "Cached book with same cache should match");

		bookClone.setTitleCache("cache2",true);
		assertFalse(matchStrategy.invoke(book1, bookClone).isSuccessful(), "Cached book with differings caches should not match");
		bookClone.setTitleCache("cache1",true); //restore

		bookClone.setEdition(null);
		assertTrue(matchStrategy.invoke(book1, bookClone).isSuccessful(), "Cached book with a defined and a null edition should match");

		matchStrategy = DefaultMatchStrategy.NewInstance(TaxonName.class);
		IBotanicalName botName1 = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
		IBotanicalName botName2 = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
		assertNotNull(botName1.getRank(), "Rank should not be null");

		botName1.setGenusOrUninomial("Genus1");
		botName2.setGenusOrUninomial("Genus1");
		assertTrue(matchStrategy.invoke(botName1, botName2).isSuccessful(), "Names with equal genus should match");

		botName1.setCombinationAuthorship(team1);
		botName2.setCombinationAuthorship(null);
		assertFalse(matchStrategy.invoke(botName1, botName2).isSuccessful(), "Names one having an author the other one not should not match");

		botName1.setAuthorshipCache("authorCache1");
		botName2.setAuthorshipCache("authorCache1");
		assertTrue(matchStrategy.invoke(botName1, botName2).isSuccessful(), "Names with cached authors should match");
	}

	@Test
	public void testInvokeReferences() throws MatchException {
		matchStrategy = DefaultMatchStrategy.NewInstance(Reference.class);
		assertTrue(matchStrategy.invoke(book1, book1).isSuccessful(), "Same object should always match");

		IBook bookClone = ((Reference) book1).clone();
		assertTrue(matchStrategy.invoke(book1, bookClone).isSuccessful(), "Cloned book should match");
		bookClone.setTitle("Any title");
		assertFalse(matchStrategy.invoke(book1, bookClone).isSuccessful(), "Books with differing titles should not match");
		String originalTitle = book1.getTitle();
		bookClone.setTitle(originalTitle);
		assertTrue(matchStrategy.invoke(book1, bookClone).isSuccessful(), "Cloned book should match");
		book1.setTitle(null);
		bookClone.setTitle(null);
		assertFalse(matchStrategy.invoke(book1, bookClone).isSuccessful(), "Books with no title should not match");
		book1.setTitle(originalTitle);
		bookClone.setTitle(originalTitle);

		bookClone.setInSeries(printSeries2);
		assertFalse(matchStrategy.invoke(book1, bookClone).isSuccessful(), "Cloned book with differing print series should not match");
		IPrintSeries seriesClone = ((Reference)printSeries1).clone();
		bookClone.setInSeries(seriesClone);
		assertTrue(matchStrategy.invoke(book1, bookClone).isSuccessful(), "Cloned book with cloned bookSeries should match");
		seriesClone.setTitle("Another title");
		assertFalse(matchStrategy.invoke(book1, bookClone).isSuccessful(), "Cloned book should not match with differing series title");
		bookClone.setInSeries(printSeries1);
		assertTrue(matchStrategy.invoke(book1, bookClone).isSuccessful(), "Original printSeries should match");

		IBook bookTitle1 = ReferenceFactory.newBook();
		IBook bookTitle2 = ReferenceFactory.newBook();
		assertFalse(matchStrategy.invoke(bookTitle1, bookTitle2).isSuccessful(), "Books without title should not match");
		String title = "Any title";
		bookTitle1.setTitle(title);
		bookTitle2.setTitle(title);
		assertTrue(matchStrategy.invoke(bookTitle1, bookTitle2).isSuccessful(), "Books with same title (not empty) should match");
		bookTitle1.setTitle("");
		bookTitle2.setTitle("");
		assertFalse(matchStrategy.invoke(bookTitle1, bookTitle2).isSuccessful(), "Books with empty title should not match");

		//Time period
		bookTitle1.setTitle(title);
		bookTitle2.setTitle(title);
		bookTitle1.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1999, 2002));
		assertFalse(matchStrategy.invoke(bookTitle1, bookTitle2).isSuccessful(), "Books with differing publication dates should not match");
		bookTitle2.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1998));
		assertFalse(matchStrategy.invoke(bookTitle1, bookTitle2).isSuccessful(), "Books with differing publication dates should not match");
		bookTitle2.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1999));
		assertFalse(matchStrategy.invoke(bookTitle1, bookTitle2).isSuccessful(), "Books with differing publication dates should not match");
		bookTitle2.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1999, 2002));
		assertTrue(matchStrategy.invoke(bookTitle1, bookTitle2).isSuccessful(), "Books with same publication dates should match");

		//BookSection
		IBookSection section1 = ReferenceFactory.newBookSection();
		section1.setInBook(bookTitle1);
		section1.setTitle("SecTitle");
		section1.setPages("22-33");
		IBookSection section2 = ReferenceFactory.newBookSection();
		section2.setInBook(bookTitle1);
		section2.setTitle("SecTitle");
		section2.setPages("22-33");

		IMatchStrategyEqual bookSectionMatchStrategy = DefaultMatchStrategy.NewInstance(Reference.class);
		assertTrue(bookSectionMatchStrategy.invoke(section1, section2).isSuccessful(), "Equal BookSections should match");
		section2.setInBook(bookTitle2);
		assertTrue(bookSectionMatchStrategy.invoke(section1, section2).isSuccessful(), "Matching books should result in matching book sections");
		bookTitle2.setPages("xx");
		assertFalse(bookSectionMatchStrategy.invoke(section1, section2).isSuccessful(), "Sections with differing books should not match");
		//restore
		bookTitle2.setPages(null);
		assertTrue(bookSectionMatchStrategy.invoke(section1, section2).isSuccessful(), "Matching books should result in matching book sections");
		printSeries2.setTitle("A new series title");
		IMatchStrategyEqual printSeriesMatchStrategy = DefaultMatchStrategy.NewInstance(Reference.class);
		assertFalse(printSeriesMatchStrategy.invoke(printSeries1, printSeries2).isSuccessful(), "Print series with differing titles should not match");
		bookTitle1.setInSeries(printSeries1);
		bookTitle2.setInSeries(printSeries2);
		assertFalse(matchStrategy.invoke(bookTitle1, bookTitle2).isSuccessful(), "Books with not matching in series should not match");
		assertFalse(bookSectionMatchStrategy.invoke(section1, section2).isSuccessful(), "Sections with differing print series should not match");

		//Authorship
		Person person1 = Person.NewTitledInstance("person");
		Person person2 = Person.NewTitledInstance("person");

		person1.setPrefix("pre1");
		person2.setPrefix("pre2");
		book2= ((Reference) book1).clone();

		assertTrue(matchStrategy.invoke(book1, book2).isSuccessful(), "Equal books should match");

		book1.setAuthorship(person1);
		book2.setAuthorship(person1);
		assertTrue(matchStrategy.invoke(book1, book2).isSuccessful(), "Books with same author should match");

		book2.setAuthorship(person2);
		assertFalse(matchStrategy.invoke(book1, book2).isSuccessful(), "Books with different authors should not match");

		person2.setPrefix("pre1");
		assertTrue(matchStrategy.invoke(book1, book2).isSuccessful(), "Books with matching authors should not match");
	}

	@Test
	public void testInvokeTaxonNames() throws MatchException {
		matchStrategy = DefaultMatchStrategy.NewInstance(TaxonName.class);

		TaxonName botName1 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		TaxonName botName2 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		IBotanicalName botName3 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());

		assertFalse(matchStrategy.invoke(botName1, botName2).isSuccessful(), "Names without title should not match");

		botName1.setGenusOrUninomial("Genus1");
		botName1.setSpecificEpithet("species1");

		botName2.setGenusOrUninomial("Genus1");
		botName2.setSpecificEpithet("species1");
		assertTrue(matchStrategy.invoke(botName1, botName2).isSuccessful(), "Similar names with titles set should match");

		botName1.setAnamorphic(true);
		botName2.setAnamorphic(false);
		assertFalse(matchStrategy.invoke(botName1, botName2).isSuccessful(), "Similar names with differing boolean marker values should not match");
		botName2.setAnamorphic(true);
		assertTrue(matchStrategy.invoke(botName1, botName2).isSuccessful(), "Similar names with same boolean marker values should match");

//		//name relations
//		botName2.addBasionym(botName3, book1, "p.22", null);
//		Specimen specimen1 = Specimen.NewInstance();
//		botName2.addSpecimenTypeDesignation(specimen1, SpecimenTypeDesignationStatus.HOLOTYPE(), book2, "p.56", "originalInfo", false, true);
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
		assertTrue(DefaultMatchStrategy.NewInstance(Team.class).invoke(team1, team2).isSuccessful(), "Similar teams should match");
		assertTrue(matchStrategy.invoke(botName1, botName2).isSuccessful(), "Similar names with matching authors should match");
	}

	@Test
	public void testInvokeAgents() throws MatchException {
		IMatchStrategyEqual matchStrategy = DefaultMatchStrategy.NewInstance(Team.class);

		Team team1 = Team.NewInstance();
		Team team2 = Team.NewInstance();
		Team team3 = Team.NewInstance();

		assertTrue(matchStrategy.invoke(team1, team2).isSuccessful(), "Teams should match");
		assertTrue(matchStrategy.invoke(team1, team3).isSuccessful(), "Teams should match");

		team1.setNomenclaturalTitleCache("nomTitle1", true);
		team2.setNomenclaturalTitleCache("nomTitle2", true);
		assertFalse(matchStrategy.invoke(team1, team2).isSuccessful(), "Agents with differing nomenclatural titles should not match");
		//restore
		team2.setNomenclaturalTitleCache("nomTitle1", true);
		assertTrue(matchStrategy.invoke(team1, team2).isSuccessful(), "Agents with equal nomenclatural titles should match");

		Person person1 = Person.NewTitledInstance("person1");
		person1.setProtectedTitleCache(true);
		Person person2 = Person.NewTitledInstance("person2");
		person2.setProtectedTitleCache(true);
		Person person3 = Person.NewTitledInstance("person3");
		person3.setProtectedTitleCache(true);

		team1.addTeamMember(person1);
		team2.addTeamMember(person1);
		assertTrue(matchStrategy.invoke(team1, team2).isSuccessful(), "Teams with same team members should match");

		team1.addTeamMember(person2);
		assertFalse(matchStrategy.invoke(team1, team2).isSuccessful(), "Teams with differing team members should not match");
		team2.addTeamMember(person2);
		assertTrue(matchStrategy.invoke(team1, team2).isSuccessful(), "Teams with same team members should match");
		team2.removeTeamMember(person2);
		person3.setPrefix("pre3");
		team2.addTeamMember(person3);
		assertFalse(matchStrategy.invoke(team1, team2).isSuccessful(), "Teams with differing team members should not match");
		person3.setTitleCache(person2.getTitleCache(),true);
		person2.setPrefix("pre3");
		assertTrue(matchStrategy.invoke(team1, team2).isSuccessful(), "Teams with matching members in right order should match");
		team2.removeTeamMember(person1);
		team2.addTeamMember(person1);
		assertFalse(matchStrategy.invoke(team1, team2).isSuccessful(), "Teams with matching members in wrong order should not match");
	}
}