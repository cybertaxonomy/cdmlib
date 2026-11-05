/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.merge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.common.DateTimeUtil;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.InstitutionalMembership;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.VerbatimTimePeriod;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.cache.reference.IReferenceCacheStrategy;
import eu.etaxonomy.cdm.test.TermTestBase;

/**
 * @author a.mueller
 * @since 03.08.2009
 */
public class DefaultMergeStrategyTest extends TermTestBase {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	private DefaultMergeStrategy bookMergeStrategy;
	private Reference book1;
	private String editionString1 ="Ed.1";
	private String volumeString1 ="Vol.1";
	private Team team1;
	private Reference printSeries1;
	private Annotation annotation1;
	private String title1 = "Title1";
	private VerbatimTimePeriod datePublished1 = VerbatimTimePeriod.NewVerbatimInstance(2000);
	private int hasProblem1 = 1;
	private LSID lsid1;

	private Reference book2;
	private String editionString2 ="Ed.2";
	private String volumeString2 ="Vol.2";
	private Team team2;
	private Reference printSeries2;
	private Annotation annotation2;
	private String annotationString2;
	private String title2 = "Title2";
	private ZonedDateTime created2 = DateTimeUtil.of(1999, 3, 1);
	private VerbatimTimePeriod datePublished2 = VerbatimTimePeriod.NewVerbatimInstance(2002);
	private int hasProblem2 = 1;
	private LSID lsid2;

	@BeforeEach
	public void setUp() throws Exception {
		bookMergeStrategy = DefaultMergeStrategy.NewInstance(Reference.class);
		team1 = Team.NewInstance();
		team1.setTitleCache("Team1", true);
		team2 = Team.NewInstance();
		team2.setTitleCache("Team2", true);
		printSeries1 = ReferenceFactory.newPrintSeries("Series1");
		printSeries2 = ReferenceFactory.newPrintSeries("Series2");
		annotation1 = Annotation.NewInstance("Annotation1", null);
		annotationString2 = "Annotation2";
		annotation2 = Annotation.NewInstance(annotationString2, null);

		book1 = ReferenceFactory.newBook();
		book1.setAuthorship(team1);
		book1.setTitle(title1);
		book1.setEdition(editionString1);
		book1.setVolume(volumeString1);
		book1.setInReference(printSeries1);
		book1.addAnnotation(annotation1);
		book1.setDatePublished(datePublished1);
		book1.setParsingProblem(hasProblem1);
		lsid1 = new LSID("authority1", "namespace1", "object1", "revision1");
		book1.setLsid(lsid1);

		book2 = ReferenceFactory.newBook();
		book2.setAuthorship(team2);
		book2.setTitle(title2);
		book2.setEdition(editionString2);
		book2.setVolume(volumeString2);
		book2.setInReference(printSeries2);
		book2.addAnnotation(annotation2);
		book2.setCreated(created2);
		book2.setDatePublished(datePublished2);
		book2.setParsingProblem(hasProblem2);
		lsid2 = new LSID("authority2", "namespace2", "object2", "revision2");
		book2.setLsid(lsid2);
	}

//********************* TEST *********************************************/

	@Test
	public void testNewInstance() {
		assertNotNull(bookMergeStrategy);
		assertEquals(Reference.class, bookMergeStrategy.getMergeClass());
	}

	@Test
	public void testGetMergeMode() {
		assertEquals(MergeMode.FIRST, bookMergeStrategy.getMergeMode("title"), "Merge mode for title should be MergeMode.FIRST");
	}

	@Test
	public void testGetSetMergeMode() {
		//legal value
		try {
			bookMergeStrategy.setMergeMode("edition", MergeMode.SECOND);
			assertEquals(MergeMode.SECOND, bookMergeStrategy.getMergeMode("edition"), "Merge mode for edition should be");
		} catch (MergeException e1) {
			fail();
		}
		//illegalValue
		try {
			bookMergeStrategy.setMergeMode("xxx", MergeMode.SECOND);
			fail("A property name must exist, otherwise an exception must be thrown");
		} catch (Exception e) {
			//ok
		}
		//illegalValue
		try {
			bookMergeStrategy.setMergeMode("cacheStrategy", MergeMode.SECOND);
			fail("CacheStrategy is transient and therefore not a legal merge parameter");
		} catch (Exception e) {
			//ok
		}
		//illegalValue
		try {
			bookMergeStrategy.setMergeMode("id", MergeMode.SECOND);
			fail("Identifier merge mode must always be MergeMode.FIRST");
		} catch (Exception e) {
			//ok
		}
		//illegalValue
		try {
			bookMergeStrategy.setMergeMode("uuid", MergeMode.SECOND);
			fail("Identifier merge mode must always be MergeMode.FIRST");
		} catch (Exception e) {
			//ok
		}
	}

	@Test
	public void testInvokeReferences() throws MergeException {
		IReferenceCacheStrategy cacheStrategy1 = book1.cacheStrategy();
		int id = book1.getId();
		UUID uuid = book1.getUuid();
		try {
			bookMergeStrategy.setMergeMode("edition", MergeMode.SECOND);
			bookMergeStrategy.setMergeMode("volume", MergeMode.NULL);
			bookMergeStrategy.setMergeMode("authorship", MergeMode.SECOND);
			bookMergeStrategy.setMergeMode("created", MergeMode.SECOND);
			bookMergeStrategy.setMergeMode("updated",MergeMode.NULL);
			bookMergeStrategy.setMergeMode("datePublished", MergeMode.SECOND);
			bookMergeStrategy.setMergeMode("parsingProblem", MergeMode.SECOND);
			bookMergeStrategy.setMergeMode("inReference", MergeMode.SECOND);
			bookMergeStrategy.setMergeMode("lsid", MergeMode.SECOND);

			bookMergeStrategy.invoke(book1, book2);
		} catch (MergeException e) {
			throw e;
			//fail("An unexpected merge exception occurred: " + e.getMessage() + ";" + e.getCause().getMessage());
		}
		assertEquals(title1, book1.getTitle(), "Title should stay the same");
		assertEquals(editionString2, book1.getEdition(), "Edition should become edition 2");
		assertNull(book1.getVolume(), "Volume should be null");

		//Boolean
		assertEquals(hasProblem2, book1.getParsingProblem(), "Has problem must be hasProblem2");

		//CdmBase
		assertSame(team2, book1.getAuthorship(), "Authorship must be the one of book2");
		assertSame(printSeries2, book1.getInReference(), "In Series must be the one of book2");

		//Transient
		assertSame(cacheStrategy1, book1.cacheStrategy(), "Cache strategy is transient and shouldn't change therefore");

		//UserType
		assertEquals(created2, book1.getCreated(), "Created must be created2");
		//TODO updated should have the current date if any value has changed
		assertSame(null, book1.getUpdated(), "Created must be created2");
		assertEquals(datePublished2, book1.getDatePublished(), "Created must be datePublsihed2");
		//TODO this may not be correct
		assertSame(lsid2, book1.getLsid(), "LSID must be LSID2");

		//TODO
		//	book1.setProblemEnds(end);
		//	book1.setProtectedTitleCache(protectedTitleCache);

		//annotations -> ADD_CLONE
		assertEquals(2, book1.getAnnotations().size(), "Annotations should contain annotations of both books");
		boolean cloneExists = false;
		for (Annotation annotation : book1.getAnnotations()){
			if (annotation == this.annotation2){
				//Hibernate will not persist the exact same object. Probably this is a bug (the according row in the
				//M:M table is not deleted and a unique constraints does not allow adding 2 rows with the same annotation_id
				//This test can be changed once this bug does not exist anymore
				fail("Book1 should contain a clone of annotation2 but contains annotation2 itself");
			}else if (annotationString2.equals(annotation.getText())){
				cloneExists = true;
			}
		}
		assertTrue(cloneExists, "Book1 should contain a clone of annotation2");
	//	assertEquals(0, book2.getAnnotations().size(), "Annotations from book2 should be deleted");

		//identifier
		assertSame(id, book1.getId(), "Identifier must never be changed");
		assertSame(uuid, book1.getUuid(), "Identifier must never be changed");

		//Test Thesis
		Institution school1 = Institution.NewInstance();
		Institution school2 = Institution.NewInstance();

		Reference thesis1 = ReferenceFactory.newThesis();
		thesis1.setSchool(school1);
		//Thesis thesis1 = Thesis.NewInstance(school1);
		Reference thesis2 = ReferenceFactory.newThesis();
		thesis2.setSchool(school2);
		DefaultMergeStrategy thesisStrategy = DefaultMergeStrategy.NewInstance(Reference.class);

		thesisStrategy.setMergeMode("school", MergeMode.SECOND);
		thesisStrategy.invoke(thesis1, thesis2);
		assertSame(school2, thesis1.getSchool(), "school must be school2");
	}

	@Test
	public void testInvokeTxonNames() throws MergeException {
		IMergeStrategy botNameMergeStrategy = DefaultMergeStrategy.NewInstance(TaxonName.class);
		TaxonName botName1 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		TaxonName botName2 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		TaxonName botName3 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());

		botName1.setGenusOrUninomial("Genus1");
		botName1.setSpecificEpithet("species1");
		botName1.setAnamorphic(true);

		botName2.setGenusOrUninomial("Genus2");
		botName2.setSpecificEpithet("species2");
		botName2.setAnamorphic(false);

		//name relations
		botName2.addBasionym(botName3, book1, "p.22", null, null);
		DerivedUnit specimen1 = DerivedUnit.NewPreservedSpecimenInstance();
		botName2.addSpecimenTypeDesignation(specimen1, SpecimenTypeDesignationStatus.HOLOTYPE(), book2, "p.56", "originalInfo", false, true);

		//descriptions
		TaxonNameDescription description1 = TaxonNameDescription.NewInstance();
		botName1.addDescription(description1);
		TaxonNameDescription description2 = TaxonNameDescription.NewInstance();
		botName1.addDescription(description2);

		//authors
		Team team1 = Team.NewInstance();
		Team team2 = Team.NewInstance();
		botName1.setCombinationAuthorship(team1);
		botName2.setCombinationAuthorship(team2);

		//taxa
		TaxonBase taxon1= Taxon.NewInstance(botName1, book1);
		TaxonBase taxon2= Taxon.NewInstance(botName2, book2);

		try {
			botNameMergeStrategy.setMergeMode("combinationAuthorship", MergeMode.SECOND);
			botNameMergeStrategy.setMergeMode("anamorphic", MergeMode.AND);

//			botNameMergeStrategy.invoke(botName1, botName2);
		} catch (MergeException e) {
			throw e;
			//fail("An unexpected merge exception occurred: " + e.getMessage() + ";" + e.getCause().getMessage());
		}

		//Boolean
		assertEquals(true && false, botName2.isAnamorphic(), "Is anamorphic must be false");

		//NameRelations
		Set<NameRelationship> toRelations = botName2.getRelationsToThisName();
		Set<NameRelationship> basionymRelations = new HashSet<NameRelationship>();
		for (NameRelationship toRelation : toRelations){
			if (toRelation.getType().equals(NameRelationshipType.BASIONYM())){
				basionymRelations.add(toRelation);
			}
		}
		assertEquals(1, basionymRelations.size(), "Number of basionyms must be 1");
		assertEquals(book1, basionymRelations.iterator().next().getCitation(), "Basionym must have same reference");
		//TODO merge relation if matches() = true

		//Types
		assertEquals(1, botName2.getSpecimenTypeDesignations().size(), "Number of specimen type designations must be 1");
		//TODO add to all names etc.

		//Description
		assertEquals(2, botName1.getDescriptions().size(), "Number of descriptions must be 2");

		//Authorships
		assertEquals(team1, botName1.getCombinationAuthorship(), "Combination author must be combination author 1");

		//Taxa
		assertEquals(botName1, taxon1.getName(), "TaxonName of taxon1 must be name1");
		assertEquals(botName2, taxon2.getName(), "TaxonName of taxon2 must be name2");
	}

	@Test
	public void testInvokeAgents() throws MergeException {
		IMergeStrategy teamMergeStrategy = DefaultMergeStrategy.NewInstance(Team.class);

		Team team1 = Team.NewInstance();
		Team team2 = Team.NewInstance();
		Team team3 = Team.NewInstance();

		Person person1 = Person.NewTitledInstance("person1");
		Person person2 = Person.NewTitledInstance("person2");
		Person person3 = Person.NewTitledInstance("person3");

		team1.setTitleCache("Team1", true);
		team1.setNomenclaturalTitleCache("T.1", true);

		team2.addTeamMember(person1);
		team2.addTeamMember(person2);

		team3.addTeamMember(person3);

		teamMergeStrategy.invoke(team2, team3);

		assertEquals(3, team2.getTeamMembers().size(), "Team2 must have 3 persons as members");
		assertTrue(team2.getTeamMembers().contains(person3), "Team2 must have person3 as new member");
		assertSame(person3, team2.getTeamMembers().get(2), "Team2 must have person3 as third member");

		//Person
		Institution institution1 = Institution.NewInstance();
		institution1.setTitleCache("inst1", true);
		Institution institution2 = Institution.NewInstance();
		institution2.setTitleCache("inst2", true);

		TimePeriod period1 = TimePeriod.NewInstance(2002, 2004);
		TimePeriod period2 = TimePeriod.NewInstance(2004, 2006);

		person1.addInstitutionalMembership(institution1, period1, "departement1", "role1");
		person2.addInstitutionalMembership(institution2, period2, "departement2", "role2");

		IMergeStrategy personMergeStrategy = DefaultMergeStrategy.NewInstance(Person.class);
		personMergeStrategy.invoke(person1, person2);

		assertEquals(2, person1.getInstitutionalMemberships().size(), "Number of institutional memberships must be 2");
		for (InstitutionalMembership institutionalMembership : person1.getInstitutionalMemberships()){
			assertSame(person1, institutionalMembership.getPerson(), "Person of institutional memebership must be person1");
		}
	}
}