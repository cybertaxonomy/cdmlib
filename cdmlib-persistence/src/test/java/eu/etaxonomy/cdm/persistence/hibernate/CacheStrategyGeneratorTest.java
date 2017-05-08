/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.hibernate;


import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.ExpectedDataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.molecular.Amplification;
import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.IArticle;
import eu.etaxonomy.cdm.model.reference.IBook;
import eu.etaxonomy.cdm.model.reference.IJournal;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.agent.IAgentDao;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.molecular.IAmplificationDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 * @created 18.03.2009
 */
public class CacheStrategyGeneratorTest extends CdmTransactionalIntegrationTest {
	private static Logger logger = Logger.getLogger(CacheStrategyGeneratorTest.class);

	private TaxonBase<?> cdmBase;

	@SpringBeanByType
	private ITaxonNameDao cdmEntityDaoBase;

	@SpringBeanByType
	private IAgentDao agentDao;

	@SpringBeanByType
	private IReferenceDao referenceDao;

	@SpringBeanByType
	private IAmplificationDao amplificationDao;

	@SpringBeanByType
	private IDefinedTermDao termDao;


	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		cdmBase = Taxon.NewInstance(null, null);
		cdmBase.setUuid(UUID.fromString("e463b270-c76b-11dd-ad8b-0800200c9a66"));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase#CdmEntityDaoBase(java.lang.Class)}.
	 * @throws Exception
	 */
	@Test
	public void testDaos() throws Exception {
		assertNotNull("cdmEntityDaoBase should exist",cdmEntityDaoBase);
		assertNotNull("agentDao should exist",agentDao);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase#saveOrUpdate(eu.etaxonomy.cdm.model.common.CdmBase)}.
	 */
	@Test
	@DataSet("CacheStrategyGeneratorTest.xml")
	@ExpectedDataSet
	public void testOnSaveOrUpdateNames() {
		//names
	    TaxonName<?,?> name =  cdmEntityDaoBase.findByUuid(UUID.fromString("a49a3963-c4ea-4047-8588-2f8f15352730"));
		name.setTitleCache(null, false);
		name.setNameCache(null, false);
		name.setGenusOrUninomial("Abies");
		name.setAuthorshipCache("Mill.", true);
		cdmEntityDaoBase.saveOrUpdate(name);
		TaxonName<?,?> name2 =  cdmEntityDaoBase.findByUuid(UUID.fromString("05a438d6-065f-49ef-84db-c7dc2c259975"));
		name2.setProtectedFullTitleCache(false);
		name2.setProtectedTitleCache(false);
		name2.setProtectedNameCache(false);
		name2.setGenusOrUninomial("Abies");
		name2.setSpecificEpithet("alba");
		name2.setAuthorshipCache("Mill.", true);
		IBook ref = ReferenceFactory.newBook();
		ref.setTitle("My Book");
		name2.setNomenclaturalReference(ref);
		name2.setNomenclaturalMicroReference("44");

		cdmEntityDaoBase.saveOrUpdate(name2);

		Assert.assertEquals(name, cdmEntityDaoBase.findByUuid(name.getUuid()));
		IBotanicalName nameTest = cdmEntityDaoBase.findByUuid(name.getUuid());

		Assert.assertEquals(name2, cdmEntityDaoBase.findByUuid(name2.getUuid()));
		logger.debug("FulltitleCache: "+ cdmEntityDaoBase.findByUuid(name2.getUuid()).getFullTitleCache());
		logger.debug("updated: " + cdmEntityDaoBase.findByUuid(name2.getUuid()).getUpdated());
		TaxonName<?,?> name3 = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
		name3.setFullTitleCache("Test: MyBook");
		name3.setTitleCache("Test", true);
		cdmEntityDaoBase.saveOrUpdate(name3);
		List<TaxonName> taxa = cdmEntityDaoBase.findByTitle("Test");

		TaxonName<?,?> nameBase = taxa.get (0);
		IBotanicalName botName = nameBase;

		logger.debug("created "+botName.getCreated());
		logger.debug("updated: " +botName.getUpdated());
//		IBotanicalName name3 =  (IBotanicalName)cdmEntityDaoBase.findByUuid(UUID.fromString("049a3963-c4ea-4047-8588-2f8f15352730"));
//		printDataSet(System.err, new String[]{"TaxonName", "Reference"});
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase#saveOrUpdate(eu.etaxonomy.cdm.model.common.CdmBase)}.
	 */
	@Test
	@DataSet("CacheStrategyGeneratorTest.xml")
	@ExpectedDataSet
	public void testOnSaveOrUpdateAgents() {

		//person
		Person person1;
		Person person2;
		Person person3;

		person1 = makePerson1();

		person2 = makePerson2();

		person3 = Person.NewInstance(); //empty person
		person3.setUuid(UUID.fromString("4c4e15e3-3a4f-4505-900a-fae2555ac9e4"));

//		System.out.println(person1.getTitleCache());
//		System.out.println(person1.getNomenclaturalTitle());
//		System.out.println(person2.getTitleCache());
//		System.out.println(person2.getNomenclaturalTitle());
//		System.out.println(person3.getTitleCache());
//		System.out.println(person3.getNomenclaturalTitle());

		agentDao.saveOrUpdate(person1);
		agentDao.saveOrUpdate(person2);
		agentDao.saveOrUpdate(person3);

		//Teams
		Team team1 = Team.NewInstance();
		team1.addTeamMember(person1);
		team1.setUuid(UUID.fromString("db957a0a-1494-49bb-8d17-d3eaa2076573"));
		agentDao.saveOrUpdate(team1);

		Person person4 = (Person)agentDao.findByUuid(UUID.fromString("4c4e15e3-3a4f-4505-900a-fae2555ac9e4"));
		Assert.assertEquals(person3, person4);
		Team team2 = (Team) agentDao.findByUuid(UUID.fromString("db957a0a-1494-49bb-8d17-d3eaa2076573"));
		Assert.assertEquals(team1, team2);
	}



	private Person makePerson1() {
		Person person1;
		person1 = Person.NewInstance();

		person1.setUuid(UUID.fromString("646dad4b-0f0e-4f5a-b059-8099ad9a6125"));
		person1.setFirstname("P1FN");
		person1.setLastname("P1LN");
		person1.setPrefix("Dr1.");
		person1.setSuffix("Suff1");
		return person1;
	}

	private Person makePerson2() {
		Person person2;
		person2 = Person.NewInstance();
		person2.setUuid(UUID.fromString("ca904533-2a70-49f3-9a0e-5e4bcc12c154"));
		person2.setNomenclaturalTitle("P2NomT");
		person2.setLastname("P2LN");
		person2.setFirstname("P2FN");
		person2.setSuffix("P2Suff");
		return person2;
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase#saveOrUpdate(eu.etaxonomy.cdm.model.common.CdmBase)}.
	 */
	@Test
	@DataSet("CacheStrategyGeneratorTest.xml")
	@ExpectedDataSet
	//TODO correct abbrevTitleCache for article still unclear (open question: with or without article title ?)
	public void testOnSaveOrUpdateReferences() {
		//References
		IJournal journal1 = ReferenceFactory.newJournal();
		Person journalAuthor = makePerson1();

		journal1.setTitle("My journal");
		journal1.setUuid(UUID.fromString("a7fdf3b8-acd8-410a-afcd-1768d29d67e9"));
		journal1.setAbbrevTitle("M. Journ.");
		journal1.setAuthorship(journalAuthor);

		referenceDao.save((Reference)journal1);

		Person articleAuthor = makePerson2();
		IArticle article1 = ReferenceFactory.newArticle();
		article1.setUuid(UUID.fromString("eb090fbc-5895-405c-aba5-cac287efb128"));
		article1.setAbbrevTitle("M. Art.");
		article1.setVolume("1");
		article1.setDatePublished(TimePeriod.NewInstance(1972));
		article1.setInJournal(journal1);
		article1.setAuthorship(articleAuthor);
		article1.getAbbrevTitleCache();

		referenceDao.saveOrUpdate((Reference)article1);

		commit();
	}

	@Test
//	@DataSet("CacheStrategyGeneratorTest.xml")
//	@ExpectedDataSet
	public void testOnSaveOrUpdateAmplification() {
		Amplification amplification = Amplification.NewInstance();
		UUID amplUuid = UUID.fromString("11e6b2d5-3eb5-4434-9c56-5bb4c1102147");
		amplification.setUuid(amplUuid);

		amplificationDao.save(amplification);
		Assert.assertEquals("<Amplification:11e6b2d5-3eb5-4434-9c56-5bb4c1102147>", amplification.getLabelCache());

		Person author = Person.NewTitledInstance("Person");
		Institution institution = Institution.NewInstance();
		institution.setName("My institute");
		DefinedTerm marker = DefinedTerm.NewDnaMarkerInstance("marker", "marker", "dm");

		amplification.setActor(author);
		amplification.setTimeperiod(TimePeriodParser.parseString("2008"));
		amplification.setDnaMarker(marker);
		amplification.setInstitution(institution);


		termDao.save(marker);
		amplificationDao.saveOrUpdate(amplification);
		Assert.assertEquals("My institute_Person_marker_2008", amplification.getLabelCache());
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
	 */
	@Override
	public void createTestDataSet() throws FileNotFoundException {
		// TODO Auto-generated method stub
	}
}

