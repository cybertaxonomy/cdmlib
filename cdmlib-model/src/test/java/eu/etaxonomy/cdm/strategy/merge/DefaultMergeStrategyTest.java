/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.merge;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Address;
import eu.etaxonomy.cdm.model.agent.Contact;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.InstitutionalMembership;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
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
import eu.etaxonomy.cdm.strategy.cache.reference.INomenclaturalReferenceCacheStrategy;

/**
 * @author a.mueller
 * @since 03.08.2009
 */
public class DefaultMergeStrategyTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DefaultMergeStrategyTest.class);

	private DefaultMergeStrategy bookMergeStrategy;
	private Reference book1;
	private String editionString1 ="Ed.1";
	private String volumeString1 ="Vol.1";
	private Team team1;
	private Reference printSeries1;
	private Annotation annotation1;
	private String title1 = "Title1";
	private TimePeriod datePublished1 = TimePeriod.NewInstance(2000);
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
	private DateTime created2 = new DateTime(1999, 3, 1, 0, 0, 0, 0);
	private TimePeriod datePublished2 = TimePeriod.NewInstance(2002);
	private int hasProblem2 = 1;
	private LSID lsid2;


	private Reference book3;

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
		book1.setNomenclaturallyRelevant(false);

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
		book2.setNomenclaturallyRelevant(true);

	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

//********************* TEST *********************************************/

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.merge.DefaultMergeStrategy#NewInstance(java.lang.Class)}.
	 */
	@Test
	public void testNewInstance() {
		Assert.assertNotNull(bookMergeStrategy);
		Assert.assertEquals(Reference.class, bookMergeStrategy.getMergeClass());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.merge.DefaultMergeStrategy#getMergeMode(java.lang.String)}.
	 */
	@Test
	public void testGetMergeMode() {
		Assert.assertEquals("Merge mode for title should be MergeMode.FIRST", MergeMode.FIRST, bookMergeStrategy.getMergeMode("title"));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.merge.DefaultMergeStrategy#getMergeMode(java.lang.String)}.
	 */
	@Test
	public void testGetSetMergeMode() {
		//legal value
		try {
			bookMergeStrategy.setMergeMode("edition", MergeMode.SECOND);
			Assert.assertEquals("Merge mode for edition should be", MergeMode.SECOND, bookMergeStrategy.getMergeMode("edition"));
		} catch (MergeException e1) {
			Assert.fail();
		}
		//illegalValue
		try {
			bookMergeStrategy.setMergeMode("xxx", MergeMode.SECOND);
			Assert.fail("A property name must exist, otherwise an exception must be thrown");
		} catch (Exception e) {
			//ok
		}
		//illegalValue
		try {
			bookMergeStrategy.setMergeMode("cacheStrategy", MergeMode.SECOND);
			Assert.fail("CacheStrategy is transient and therefore not a legal merge parameter");
		} catch (Exception e) {
			//ok
		}
		//illegalValue
		try {
			bookMergeStrategy.setMergeMode("id", MergeMode.SECOND);
			Assert.fail("Identifier merge mode must always be MergeMode.FIRST");
		} catch (Exception e) {
			//ok
		}
		//illegalValue
		try {
			bookMergeStrategy.setMergeMode("uuid", MergeMode.SECOND);
			Assert.fail("Identifier merge mode must always be MergeMode.FIRST");
		} catch (Exception e) {
			//ok
		}


	}


	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.merge.DefaultMergeStrategy#invoke(eu.etaxonomy.cdm.strategy.merge.IMergable, eu.etaxonomy.cdm.strategy.merge.IMergable)}.
	 * @throws MergeException
	 */
	@Test
	public void testInvokeReferences() throws MergeException {
		INomenclaturalReferenceCacheStrategy cacheStrategy1 = book1.getCacheStrategy();
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
			//Assert.fail("An unexpected merge exception occurred: " + e.getMessage() + ";" + e.getCause().getMessage());
		}
		Assert.assertEquals("Title should stay the same", title1, book1.getTitle());
		Assert.assertEquals("Edition should become edition 2", editionString2, book1.getEdition());
		Assert.assertNull("Volume should be null", book1.getVolume());

		//Boolean
		Assert.assertEquals("Has problem must be hasProblem2", hasProblem2, book1.getParsingProblem());
		Assert.assertEquals("nomenclaturally relevant must have value true (AND semantics)", true, book1.isNomenclaturallyRelevant() );


		//CdmBase
		Assert.assertSame("Authorship must be the one of book2", team2, book1.getAuthorship());
		Assert.assertSame("In Series must be the one of book2", printSeries2, book1.getInReference());

		//Transient
		Assert.assertSame("Cache strategy is transient and shouldn't change therefore", cacheStrategy1, book1.getCacheStrategy());


		//UserType
		Assert.assertSame("Created must be created2", created2, book1.getCreated());
		//TODO updated should have the actual date if any value has changed
		Assert.assertSame("Created must be created2", null, book1.getUpdated());
		Assert.assertSame("Created must be datePublsihed2", datePublished2, book1.getDatePublished());
		//TODO this may not be correct
		Assert.assertSame("LSID must be LSID2", lsid2, book1.getLsid());


		//TODO
		//	book1.setProblemEnds(end);
		//	book1.setProtectedTitleCache(protectedTitleCache);

		//annotations -> ADD_CLONE
		Assert.assertEquals("Annotations should contain annotations of both books", 2, book1.getAnnotations().size());
		boolean cloneExists = false;
		for (Annotation annotation : book1.getAnnotations()){
			if (annotation == this.annotation2){
				//Hibernate will not persist the exact same object. Probably this is a bug (the according row in the
				//M:M table is not deleted and a unique constraints does not allow adding 2 rows with the same annotation_id
				//This test can be changed once this bug does not exist anymore
				Assert.fail("Book1 should contain a clone of annotation2 but contains annotation2 itself");
			}else if (annotationString2.equals(annotation.getText())){
				cloneExists = true;
			}
		}
		Assert.assertTrue("Book1 should contain a clone of annotation2", cloneExists);
	//	Assert.assertEquals("Annotations from book2 should be deleted", 0, book2.getAnnotations().size());

		//identifier
		Assert.assertSame("Identifier must never be changed", id, book1.getId());
		Assert.assertSame("Identifier must never be changed", uuid, book1.getUuid());

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
		Assert.assertSame("school must be school2", school2, thesis1.getSchool());
	}


	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.merge.DefaultMergeStrategy#invoke(eu.etaxonomy.cdm.strategy.merge.IMergable, eu.etaxonomy.cdm.strategy.merge.IMergable)}.
	 * @throws MergeException
	 */
	@Test
	//@Ignore
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
		botName2.addBasionym(botName3, book1, "p.22", null);
		DerivedUnit specimen1 = DerivedUnit.NewPreservedSpecimenInstance();
		botName2.addSpecimenTypeDesignation(specimen1, SpecimenTypeDesignationStatus.HOLOTYPE(), book2, "p.56", "originalNameString", false, true);

		//descriptions
		TaxonNameDescription description1 = TaxonNameDescription.NewInstance();
		botName1.addDescription(description1);
		TaxonNameDescription description2 = TaxonNameDescription.NewInstance();
		botName1.addDescription(description2);

		//authors
		Team team1 = Team.NewInstance();
		Team team2 = Team.NewInstance();
		Person person1 = Person.NewInstance();
		botName1.setCombinationAuthorship(team1);
		botName2.setCombinationAuthorship(team2);

		//taxa
		TaxonBase<?> taxon1= Taxon.NewInstance(botName1, book1);
		TaxonBase<?> taxon2= Taxon.NewInstance(botName2, book2);

		try {
			botNameMergeStrategy.setMergeMode("combinationAuthorship", MergeMode.SECOND);
			botNameMergeStrategy.setMergeMode("anamorphic", MergeMode.AND);

//			botNameMergeStrategy.invoke(botName1, botName2);
		} catch (MergeException e) {
			throw e;
			//Assert.fail("An unexpected merge exception occurred: " + e.getMessage() + ";" + e.getCause().getMessage());
		}

		//Boolean
		Assert.assertEquals("Is anamorphic must be false", true && false, botName2.isAnamorphic());

		//NameRelations
		Set<NameRelationship> toRelations = botName2.getRelationsToThisName();
		Set<NameRelationship> basionymRelations = new HashSet<NameRelationship>();
		for (NameRelationship toRelation : toRelations){
			if (toRelation.getType().equals(NameRelationshipType.BASIONYM())){
				basionymRelations.add(toRelation);
			}
		}
		Assert.assertEquals("Number of basionyms must be 1", 1, basionymRelations.size());
		Assert.assertEquals("Basionym must have same reference", book1, basionymRelations.iterator().next().getCitation());
		//TODO merge relation if matches() = true

		//Types
		Assert.assertEquals("Number of specimen type designations must be 1", 1, botName2.getSpecimenTypeDesignations().size());
		//TODO add to all names etc.

		//Description
		Assert.assertEquals("Number of descriptions must be 2", 2, botName1.getDescriptions().size());

		//Authorships
		Assert.assertEquals("Combination author must be combination author 1", team1, botName1.getCombinationAuthorship());

		//Taxa
		Assert.assertEquals("TaxonName of taxon1 must be name1", botName1, taxon1.getName());
		Assert.assertEquals("TaxonName of taxon2 must be name2", botName2, taxon2.getName());

	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.merge.DefaultMergeStrategy#invoke(eu.etaxonomy.cdm.strategy.merge.IMergable, eu.etaxonomy.cdm.strategy.merge.IMergable)}.
	 * @throws MergeException
	 */
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
		team1.setNomenclaturalTitle("T.1");
		String street1 = "Strasse1";
		team1.setContact(Contact.NewInstance(street1, "12345", "Berlin", Country.ARGENTINAARGENTINEREPUBLIC(),"pobox" , "Region", "a@b.de", "f12345", "+49-30-123456", URI.create("www.abc.de"), Point.NewInstance(2.4, 3.2, ReferenceSystem.WGS84(), 3)));
		team2.setContact(Contact.NewInstance("Street2", null, "London", null, null, null, null, "874599873", null, null, null));
		String street3 = "Street3";
		team2.addAddress(street3, null, null, null, null, null, Point.NewInstance(1.1, 2.2, null, 4));
		String emailAddress1 = "Email1";
		team1.addEmailAddress(emailAddress1);

		team2.addTeamMember(person1);
		team2.addTeamMember(person2);
		String emailAddress2 = "Email2";
		team2.addEmailAddress(emailAddress2);

		team3.addTeamMember(person3);
		team3.addEmailAddress("emailAddress3");

		teamMergeStrategy.invoke(team2, team3);

		Assert.assertEquals("Team2 must have 3 persons as members",3, team2.getTeamMembers().size());
		Assert.assertTrue("Team2 must have person3 as new member", team2.getTeamMembers().contains(person3));
		Assert.assertSame("Team2 must have person3 as third member",person3, team2.getTeamMembers().get(2));

		//Contact
		teamMergeStrategy.invoke(team2, team1);
		Contact team2Contact = team2.getContact();
		Assert.assertNotNull("team2Contact must not be null", team2Contact);
		Assert.assertNotNull("Addresses must not be null", team2Contact.getAddresses());
		Assert.assertEquals("Number of addresses must be 3", 3, team2Contact.getAddresses().size());
		Assert.assertEquals("Number of email addresses must be 4", 4, team2Contact.getEmailAddresses().size());

		boolean street1Exists = false;
		boolean street3Exists = false;
		boolean country1Exists = false;
		for  (Address address : team2Contact.getAddresses()){
			if (street1.equals(address.getStreet())){
				street1Exists = true;
			}
			if (street3.equals(address.getStreet())){
				street3Exists = true;
			}
			if (Country.ARGENTINAARGENTINEREPUBLIC() == address.getCountry()){
				country1Exists = true;
			}
		}
		Assert.assertTrue("Street1 must be one of the streets in team2's addresses", street1Exists);
		Assert.assertTrue("Street3 must be one of the streets in team2's addressesss", street3Exists);
		Assert.assertTrue("Argentina must be one of the countries in team2's addresses", country1Exists);

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

		Assert.assertEquals("Number of institutional memberships must be 2", 2, person1.getInstitutionalMemberships().size());
		for (InstitutionalMembership institutionalMembership : person1.getInstitutionalMemberships()){
			Assert.assertSame("Person of institutional memebership must be person1", person1, institutionalMembership.getPerson());
		}

	}

}
