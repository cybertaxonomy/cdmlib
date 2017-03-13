/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.reference.IGeneric;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;

/**
 * @author a.mueller
 *
 */
public class NonViralNameTest extends EntityTestBase {
	private static Logger logger = Logger.getLogger(NonViralNameTest.class);


	INonViralName nonViralName1;
	INonViralName nonViralName2;

	@BeforeClass
	public static void setUpBeforeClass() {
		DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
		vocabularyStore.initialize();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		nonViralName1 = new BotanicalName();
		nonViralName2 = new BotanicalName();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

// ******************* TESTS ***********************************/

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#generateTitle()}.
	 */
	@Test
	public final void testGenerateTitle() {
		String fullName = "Abies alba subsp. beta (L.) Mill.";
		nonViralName1.setGenusOrUninomial("Genus");
		nonViralName1.setSpecificEpithet("spec");
		nonViralName1.setRank(Rank.SPECIES());
		assertEquals("Genus spec", nonViralName1.generateTitle());
		assertEquals("", nonViralName2.generateTitle());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#isCodeCompliant()}.
	 */
	@Test
	public final void testIsCodeCompliant() {
		logger.warn("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#NonViralName()}.
	 */
	@Test
	public final void testNonViralName() {
		assertNotNull(nonViralName1);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#NonViralName(eu.etaxonomy.cdm.model.name.Rank)}.
	 */
	@Test
	public final void testNonViralNameRank() {
		INonViralName nonViralName = TaxonNameFactory.NewNonViralInstance(Rank.GENUS());
		assertNotNull(nonViralName);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#NonViralName(eu.etaxonomy.cdm.model.name.Rank, java.lang.String, java.lang.String, java.lang.String, eu.etaxonomy.cdm.model.agent.Agent, eu.etaxonomy.cdm.model.reference.INomenclaturalReference, java.lang.String)}.
	 */
	@Test
	public final void testNonViralNameRankStringStringStringAgentINomenclaturalReferenceString() {
		Team agent = Team.NewInstance();
		INomenclaturalReference article = ReferenceFactory.newArticle();
		HomotypicalGroup homotypicalGroup = HomotypicalGroup.NewInstance();
		INonViralName nonViralName = new NonViralName(Rank.GENUS(), "Genus", "infraGen", "species", "infraSpec", agent, article, "mikro", homotypicalGroup);
		assertEquals("Genus", nonViralName.getGenusOrUninomial() );
		assertEquals("infraGen", nonViralName.getInfraGenericEpithet());
		assertEquals("species", nonViralName.getSpecificEpithet() );
		assertEquals("infraSpec", nonViralName.getInfraSpecificEpithet());
		assertEquals(agent, nonViralName.getCombinationAuthorship() );
		assertEquals(article, nonViralName.getNomenclaturalReference() );
		assertEquals("mikro", nonViralName.getNomenclaturalMicroReference() );
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#getCombinationAuthorship()}.
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#setCombinationAuthorship(eu.etaxonomy.cdm.model.agent.Agent)}.
	 */
	@Test
	public final void testGetSetCombinationAuthorship() {
		Team team1 = Team.NewInstance();
		nonViralName1.setCombinationAuthorship(team1);
		assertEquals(team1, nonViralName1.getCombinationAuthorship());
		nonViralName1.setCombinationAuthorship(null);
		assertEquals(null, nonViralName1.getCombinationAuthorship());
		nonViralName2.setCombinationAuthorship(null);
		assertEquals(null, nonViralName2.getCombinationAuthorship());
	}


	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#getExCombinationAuthorship()}.
	 */
	@Test
	public final void testGetSetExCombinationAuthorship() {
		Team team1 = Team.NewInstance();
		nonViralName1.setExCombinationAuthorship(team1);
		assertEquals(team1, nonViralName1.getExCombinationAuthorship());
		nonViralName1.setExCombinationAuthorship(null);
		assertEquals(null, nonViralName1.getExCombinationAuthorship());
		nonViralName2.setExCombinationAuthorship(null);
		assertEquals(null, nonViralName2.getExCombinationAuthorship());
	}


	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#getCombinationAuthorship()}.
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#setCombinationAuthorship(eu.etaxonomy.cdm.model.agent.Agent)}.
	 */
	@Test
	public final void testGetSetBasionymAuthorship() {
		Team team1 = Team.NewInstance();
		nonViralName1.setBasionymAuthorship(team1);
		assertEquals(team1, nonViralName1.getBasionymAuthorship());
		nonViralName1.setBasionymAuthorship(null);
		assertEquals(null, nonViralName1.getBasionymAuthorship());
		nonViralName2.setBasionymAuthorship(null);
		assertEquals(null, nonViralName2.getBasionymAuthorship());
	}


	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#getExCombinationAuthorship()}.
	 */
	@Test
	public final void testGetSetExBasionymAuthorship() {
		Team team1 = Team.NewInstance();
		nonViralName1.setExBasionymAuthorship(team1);
		assertEquals(team1, nonViralName1.getExBasionymAuthorship());
		nonViralName1.setExBasionymAuthorship(null);
		assertEquals(null, nonViralName1.getExBasionymAuthorship());
		nonViralName2.setExBasionymAuthorship(null);
		assertEquals(null, nonViralName2.getExBasionymAuthorship());
	}


	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#getGenusOrUninomial()}.
	 */
	@Test
	public final void testGetSetGenusOrUninomial() {
		nonViralName1.setGenusOrUninomial("Genus");
		assertEquals("Genus", nonViralName1.getGenusOrUninomial());
		nonViralName1.setGenusOrUninomial(null);
		assertEquals(null, nonViralName1.getGenusOrUninomial());
		nonViralName2.setGenusOrUninomial(null);
		assertEquals(null, nonViralName2.getGenusOrUninomial());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#getInfragenericEpithet()}.
	 */
	@Test
	public final void testGetSetInfraGenericEpithet() {
		nonViralName1.setInfraGenericEpithet("InfraGenus");
		assertEquals("InfraGenus", nonViralName1.getInfraGenericEpithet());
		nonViralName1.setInfraGenericEpithet(null);
		assertEquals(null, nonViralName1.getInfraGenericEpithet());
		nonViralName2.setInfraGenericEpithet(null);
		assertEquals(null, nonViralName2.getInfraGenericEpithet());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#getSpecificEpithet()}.
	 */
	@Test
	public final void testGetSetSpecificEpithet() {
		nonViralName1.setSpecificEpithet("specEpi");
		assertEquals("specEpi", nonViralName1.getSpecificEpithet());
		nonViralName1.setSpecificEpithet(null);
		assertEquals(null, nonViralName1.getSpecificEpithet());
		nonViralName2.setSpecificEpithet(null);
		assertEquals(null, nonViralName2.getSpecificEpithet());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#getInfraSpecificEpithet()}.
	 */
	@Test
	public final void testGetSetInfraSpecificEpithet() {
		nonViralName1.setInfraSpecificEpithet("InfraSpecEpi");
		assertEquals("InfraSpecEpi", nonViralName1.getInfraSpecificEpithet());
		nonViralName1.setInfraSpecificEpithet(null);
		assertEquals(null, nonViralName1.getInfraSpecificEpithet());
		nonViralName2.setInfraSpecificEpithet(null);
		assertEquals(null, nonViralName2.getInfraSpecificEpithet());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#setAuthorshipCache(java.lang.String)}.
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#getAuthorshipCache()}.
	 * NOT FINISHED YET
	 */
	@Test

	public final void testGetSetAuthorshipCache() {
		String strTeam1 = "Team1";
		String strTeam2 = "Team2";
		String strTeam3 = "Team3";
		IGeneric ref1 = ReferenceFactory.newGeneric();
		ref1.setTitleCache("RefTitle",true);

		Team team1 = Team.NewInstance();
		Team team2 = Team.NewInstance();
		team1.setNomenclaturalTitle(strTeam1);
		team2.setNomenclaturalTitle(strTeam2);
		nonViralName1.setGenusOrUninomial("Abies");
		nonViralName1.setSpecificEpithet("alba");
		nonViralName1.setNomenclaturalReference(ref1);
		Assert.assertEquals("Abies alba", nonViralName1.getNameCache());

		nonViralName1.setCombinationAuthorship(team1);
		assertEquals(team1, nonViralName1.getCombinationAuthorship());
		assertEquals(strTeam1, nonViralName1.getAuthorshipCache());
		Assert.assertEquals("Abies alba "+strTeam1, nonViralName1.getTitleCache());
		Assert.assertEquals("Abies alba "+strTeam1+ ", RefTitle", nonViralName1.getFullTitleCache());

		nonViralName1.setAuthorshipCache(strTeam2);
		assertEquals(strTeam2, nonViralName1.getAuthorshipCache());
		nonViralName1.setGenusOrUninomial("Calendula");
		Assert.assertEquals("Calendula alba "+strTeam2, nonViralName1.getTitleCache());

		nonViralName1.setAuthorshipCache(strTeam3);
		Assert.assertEquals("Calendula alba "+strTeam3, nonViralName1.getTitleCache());

		Assert.assertEquals("Calendula alba "+strTeam3+ ", RefTitle", nonViralName1.getFullTitleCache());

		nonViralName1.setCombinationAuthorship(null);
		assertEquals(null, nonViralName1.getCombinationAuthorship());
	}


	@Test
	public final void testGetChildAndParentRelationships() {
		INonViralName nonViralName1 = TaxonNameFactory.NewNonViralInstance(null);
		assertEquals(0, nonViralName1.getHybridParentRelations().size());
		assertEquals(0, nonViralName1.getHybridChildRelations().size());
		IBotanicalName femaleParent = TaxonNameFactory.NewBotanicalInstance(null);
		HybridRelationship hybridRelationship = new HybridRelationship(nonViralName1, femaleParent, HybridRelationshipType.FEMALE_PARENT(), null );
		assertEquals(1, nonViralName1.getHybridChildRelations().size());
		assertEquals(hybridRelationship, nonViralName1.getHybridChildRelations().iterator().next());
		assertEquals(1, femaleParent.getHybridParentRelations().size());
	}

	@Test
	public final void testAddHybridRelationships() {
		INonViralName nonViralName1 = TaxonNameFactory.NewNonViralInstance(null);
		assertEquals(0, nonViralName1.getHybridParentRelations().size());
		assertEquals(0, nonViralName1.getHybridChildRelations().size());
		IBotanicalName femaleParent = TaxonNameFactory.NewBotanicalInstance(null);
		IBotanicalName maleParent = TaxonNameFactory.NewBotanicalInstance(null);

		nonViralName1.addHybridParent(femaleParent, HybridRelationshipType.MALE_PARENT(), null);
		nonViralName1.addHybridParent(maleParent, HybridRelationshipType.MALE_PARENT(), null);

		assertEquals(2, nonViralName1.getHybridChildRelations().size());
		assertEquals(0, nonViralName1.getHybridParentRelations().size());
		assertEquals(1, maleParent.getHybridParentRelations().size());
		assertEquals(1, femaleParent.getHybridParentRelations().size());
		assertEquals(0, maleParent.getHybridChildRelations().size());
		assertEquals(0, femaleParent.getHybridChildRelations().size());

	}

	@Test(expected=IllegalArgumentException.class)
	public final void testAddHybridRelationship() {
		INonViralName nonViralName1 = TaxonNameFactory.NewNonViralInstance(null);
		assertEquals(0, nonViralName1.getHybridParentRelations().size());
		assertEquals(0, nonViralName1.getHybridChildRelations().size());
		TaxonNameBase<?,?> botanicalName2 = TaxonNameFactory.NewNonViralInstance(null);
		botanicalName2.addHybridRelationship(null);
	}

	@Test
	public final void testRemoveHybridRelationship() {
		INonViralName botanicalName1 = TaxonNameFactory.NewNonViralInstance(null);
		assertEquals(0, botanicalName1.getHybridParentRelations().size());
		assertEquals(0, botanicalName1.getHybridChildRelations().size());
		IBotanicalName femaleParent = TaxonNameFactory.NewBotanicalInstance(null);
		TaxonNameBase<?,?> maleParent = TaxonNameFactory.NewNonViralInstance(null);
		IZoologicalName child = TaxonNameFactory.NewZoologicalInstance(null);

		botanicalName1.addHybridParent(femaleParent, HybridRelationshipType.FEMALE_PARENT(), null);
		botanicalName1.addHybridParent(maleParent, HybridRelationshipType.MALE_PARENT(), null);
		botanicalName1.addHybridChild(child, HybridRelationshipType.FIRST_PARENT(), null);
		assertEquals(2, botanicalName1.getHybridChildRelations().size());
		assertEquals(1, botanicalName1.getHybridParentRelations().size());
		assertEquals(1, child.getHybridChildRelations().size());

		botanicalName1.removeHybridParent(femaleParent);
		assertEquals(1, botanicalName1.getHybridChildRelations().size());
		assertEquals(1, botanicalName1.getHybridParentRelations().size());

		botanicalName1.removeHybridParent(maleParent);
		assertEquals(0, botanicalName1.getHybridChildRelations().size());
		assertEquals(1, botanicalName1.getHybridParentRelations().size());

		botanicalName1.removeHybridChild(child);
		assertEquals(0, botanicalName1.getHybridParentRelations().size());

		//null
		botanicalName1.removeHybridRelationship(null);
		assertEquals(0, botanicalName1.getHybridChildRelations().size());
	}



	@Test
	public void testClone(){

		Team combinationAuthor = Team.NewTitledInstance("CombinationAuthor", "comb. auth.");
		nonViralName1.setRank(Rank.SUBSPECIES());
		nonViralName1.setCombinationAuthorship(combinationAuthor);
		nonViralName1.setGenusOrUninomial("Aus");
		nonViralName1.setInfraGenericEpithet("Infaus");
		nonViralName1.setSpecificEpithet("bus");
		nonViralName1.setInfraSpecificEpithet("infrabus");
		nonViralName1.setBinomHybrid(true);

		INonViralName parent = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES());
		INonViralName parent2 = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES());
		INonViralName child = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES());
		INonViralName child2 = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES());
		nonViralName1.addHybridParent(parent, HybridRelationshipType.FIRST_PARENT(), "parent rule");
		nonViralName1.addHybridParent(parent2, HybridRelationshipType.SECOND_PARENT(), "parent rule2");
		nonViralName1.addHybridChild(child, HybridRelationshipType.FEMALE_PARENT(), "child rule");


		INonViralName clone = (INonViralName)((NonViralName)nonViralName1).clone();
		Assert.assertEquals("Genus should be equal", "Aus", clone.getGenusOrUninomial());
		Assert.assertEquals("Infragenus should be equal", "Infaus", clone.getInfraGenericEpithet());
		Assert.assertEquals("Specific epithet should be equal", "bus", clone.getSpecificEpithet());
		Assert.assertEquals("Infraspecific epithet should be equal", "infrabus", clone.getInfraSpecificEpithet());
		Assert.assertEquals("BinomHybrid should be equal", true, clone.isBinomHybrid());
		Assert.assertSame("Combination author should be the same", combinationAuthor, clone.getCombinationAuthorship());
		Assert.assertEquals("NameCache should be equal", nonViralName1.getNameCache(), clone.getNameCache());
		Assert.assertEquals("AuthorshipCache should be equal", nonViralName1.getAuthorshipCache(), clone.getAuthorshipCache());

		clone.setSpecificEpithet("sub");
		Assert.assertEquals("NameCache should be changed", "Aus (\u00D7Infaus) sub subsp. infrabus", clone.getNameCache());

		//hybrid parents of clone
		Assert.assertEquals("There should be exactly 2 hybrid relationships in which the clone takes the child role", 2, clone.getHybridChildRelations().size());
		Set<TaxonNameBase> parentSet = new HashSet<>();
		Set<TaxonNameBase> childSet = new HashSet<>();
		for (Object object : clone.getHybridChildRelations()){
			HybridRelationship childRelation = (HybridRelationship)object;
			TaxonNameBase<?,?> relatedFrom = childRelation.getRelatedFrom();
			parentSet.add(relatedFrom);
			TaxonNameBase<?,?> relatedTo = childRelation.getRelatedTo();
			childSet.add(relatedTo);
		}
		Assert.assertTrue("Parent set should contain parent1", parentSet.contains(parent));
		Assert.assertTrue("Parent set should contain parent2", parentSet.contains(parent2));
		Assert.assertTrue("Child set should contain clone", childSet.contains(clone));

		//hybrid child of clone
		Assert.assertEquals("There should be exactly 1 hybrid relationship in which the clone takes the parent role", 1, clone.getHybridParentRelations().size());
		HybridRelationship parentRelation = clone.getHybridParentRelations().iterator().next();
		Assert.assertSame("Clone should be parent in parentRelationship", clone, parentRelation.getRelatedFrom());
		Assert.assertSame("Child should be child in parentRelationship", child, parentRelation.getRelatedTo());
		Assert.assertSame("Relationship type should be cloned correctly", HybridRelationshipType.FEMALE_PARENT(), parentRelation.getType());
		Assert.assertEquals("Rule should be cloned correctly", "child rule", parentRelation.getRuleConsidered());
	}
}
