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

import org.junit.Assert;

import org.apache.log4j.Logger;
import org.junit.After;
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

	
	NonViralName<?> nonViralName1;
	NonViralName<?> nonViralName2;
	
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
		NonViralName<?> nonViralName = NonViralName.NewInstance(Rank.GENUS());
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
		NonViralName<?> nonViralName = new NonViralName(Rank.GENUS(), "Genus", "infraGen", "species", "infraSpec", agent, article, "mikro", homotypicalGroup);
		assertEquals("Genus", nonViralName.getGenusOrUninomial() );
		assertEquals("infraGen", nonViralName.getInfraGenericEpithet());
		assertEquals("species", nonViralName.getSpecificEpithet() );
		assertEquals("infraSpec", nonViralName.getInfraSpecificEpithet());
		assertEquals(agent, nonViralName.getCombinationAuthorTeam() );
		assertEquals(article, nonViralName.getNomenclaturalReference() );
		assertEquals("mikro", nonViralName.getNomenclaturalMicroReference() );
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#getCombinationAuthorTeam()}.
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#setCombinationAuthorTeam(eu.etaxonomy.cdm.model.agent.Agent)}.
	 */
	@Test
	public final void testGetSetCombinationAuthorTeam() {
		Team team1 = Team.NewInstance();
		nonViralName1.setCombinationAuthorTeam(team1);
		assertEquals(team1, nonViralName1.getCombinationAuthorTeam());
		nonViralName1.setCombinationAuthorTeam(null);
		assertEquals(null, nonViralName1.getCombinationAuthorTeam());
		nonViralName2.setCombinationAuthorTeam(null);
		assertEquals(null, nonViralName2.getCombinationAuthorTeam());
	}


	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#getExCombinationAuthorTeam()}.
	 */
	@Test
	public final void testGetSetExCombinationAuthorTeam() {
		Team team1 = Team.NewInstance();
		nonViralName1.setExCombinationAuthorTeam(team1);
		assertEquals(team1, nonViralName1.getExCombinationAuthorTeam());
		nonViralName1.setExCombinationAuthorTeam(null);
		assertEquals(null, nonViralName1.getExCombinationAuthorTeam());
		nonViralName2.setExCombinationAuthorTeam(null);
		assertEquals(null, nonViralName2.getExCombinationAuthorTeam());
	}
	

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#getCombinationAuthorTeam()}.
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#setCombinationAuthorTeam(eu.etaxonomy.cdm.model.agent.Agent)}.
	 */
	@Test
	public final void testGetSetBasionymAuthorTeam() {
		Team team1 = Team.NewInstance();
		nonViralName1.setBasionymAuthorTeam(team1);
		assertEquals(team1, nonViralName1.getBasionymAuthorTeam());
		nonViralName1.setBasionymAuthorTeam(null);
		assertEquals(null, nonViralName1.getBasionymAuthorTeam());
		nonViralName2.setBasionymAuthorTeam(null);
		assertEquals(null, nonViralName2.getBasionymAuthorTeam());
	}


	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#getExCombinationAuthorTeam()}.
	 */
	@Test
	public final void testGetSetExBasionymAuthorTeam() {
		Team team1 = Team.NewInstance();
		nonViralName1.setExBasionymAuthorTeam(team1);
		assertEquals(team1, nonViralName1.getExBasionymAuthorTeam());
		nonViralName1.setExBasionymAuthorTeam(null);
		assertEquals(null, nonViralName1.getExBasionymAuthorTeam());
		nonViralName2.setExBasionymAuthorTeam(null);
		assertEquals(null, nonViralName2.getExBasionymAuthorTeam());
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
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#getInfraGenericEpithet()}.
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
		
		nonViralName1.setCombinationAuthorTeam(team1);
		assertEquals(team1, nonViralName1.getCombinationAuthorTeam());
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
		
		nonViralName1.setCombinationAuthorTeam(null);
		assertEquals(null, nonViralName1.getCombinationAuthorTeam());
	}
	

	@Test
	public final void testGetChildAndParentRelationships() {
		NonViralName<?> nonViralName1 = new NonViralName();
		assertEquals(0, nonViralName1.getParentRelationships().size());
		assertEquals(0, nonViralName1.getHybridChildRelations().size());
		BotanicalName femaleParent = BotanicalName.NewInstance(null);
		HybridRelationship hybridRelationship = new HybridRelationship(nonViralName1, femaleParent, HybridRelationshipType.FEMALE_PARENT(), null );
		assertEquals(1, nonViralName1.getHybridChildRelations().size());
		assertEquals(hybridRelationship, nonViralName1.getHybridChildRelations().iterator().next());
		assertEquals(1, femaleParent.getParentRelationships().size());
	}

	@Test
	public final void testAddHybridRelationships() {
		NonViralName<?> nonViralName1 = new NonViralName();
		assertEquals(0, nonViralName1.getParentRelationships().size());
		assertEquals(0, nonViralName1.getChildRelationships().size());
		BotanicalName femaleParent = BotanicalName.NewInstance(null);
		BotanicalName maleParent = BotanicalName.NewInstance(null);
		
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
		NonViralName<?> nonViralName1 = new NonViralName();
		assertEquals(0, nonViralName1.getHybridParentRelations().size());
		assertEquals(0, nonViralName1.getHybridChildRelations().size());
		NonViralName<?> botanicalName2 = new NonViralName();
		botanicalName2.addHybridRelationship(null);
	}

	@Test
	public final void testRemoveHybridRelationship() {
		NonViralName<?> botanicalName1 = new NonViralName();
		assertEquals(0, botanicalName1.getHybridParentRelations().size());
		assertEquals(0, botanicalName1.getHybridChildRelations().size());
		BotanicalName femaleParent = BotanicalName.NewInstance(null);
		NonViralName<?> maleParent = NonViralName.NewInstance(null);
		ZoologicalName child = ZoologicalName.NewInstance(null);
		
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
		nonViralName1.setCombinationAuthorTeam(combinationAuthor);
		nonViralName1.setGenusOrUninomial("Aus");
		nonViralName1.setInfraGenericEpithet("Infaus");
		nonViralName1.setSpecificEpithet("bus");
		nonViralName1.setInfraSpecificEpithet("infrabus");
		nonViralName1.setBinomHybrid(true);
		
		NonViralName<?> parent = NonViralName.NewInstance(Rank.SPECIES());
		NonViralName<?> parent2 = NonViralName.NewInstance(Rank.SPECIES());
		NonViralName<?> child = NonViralName.NewInstance(Rank.SPECIES());
		NonViralName<?> child2 = NonViralName.NewInstance(Rank.SPECIES());
		nonViralName1.addHybridParent(parent, HybridRelationshipType.FIRST_PARENT(), "parent rule");
		nonViralName1.addHybridParent(parent2, HybridRelationshipType.SECOND_PARENT(), "parent rule2");
		nonViralName1.addHybridChild(child, HybridRelationshipType.FEMALE_PARENT(), "child rule");
		
		
		NonViralName<?> clone = (NonViralName)nonViralName1.clone();
		Assert.assertEquals("Genus should be equal", "Aus", clone.getGenusOrUninomial());
		Assert.assertEquals("Infragenus should be equal", "Infaus", clone.getInfraGenericEpithet());
		Assert.assertEquals("Specific epithet should be equal", "bus", clone.getSpecificEpithet());
		Assert.assertEquals("Infraspecific epithet should be equal", "infrabus", clone.getInfraSpecificEpithet());
		Assert.assertEquals("BinomHybrid should be equal", true, clone.isBinomHybrid());
		Assert.assertSame("Combination author should be the same", combinationAuthor, clone.getCombinationAuthorTeam());
		Assert.assertEquals("NameCache should be equal", nonViralName1.getNameCache(), clone.getNameCache());
		Assert.assertEquals("AuthorshipCache should be equal", nonViralName1.getAuthorshipCache(), clone.getAuthorshipCache());
		
		clone.setSpecificEpithet("sub");
		Assert.assertEquals("NameCache should be changed", "Aus (\u00D7Infaus) sub subsp. infrabus", clone.getNameCache());
		
		//hybrid parents of clone 
		Assert.assertEquals("There should be exactly 2 hybrid relationships in which the clone takes the child role", 2, clone.getChildRelationships().size());
		Set<NonViralName> parentSet = new HashSet<NonViralName>();
		Set<NonViralName> childSet = new HashSet<NonViralName>();
		for (Object object : clone.getHybridChildRelations()){
			HybridRelationship childRelation = (HybridRelationship)object;
			NonViralName<?> relatedFrom = childRelation.getRelatedFrom();
			parentSet.add(relatedFrom);
			NonViralName<?> relatedTo = childRelation.getRelatedTo();
			childSet.add(relatedTo);
		}
		Assert.assertTrue("Parent set should contain parent1", parentSet.contains(parent));
		Assert.assertTrue("Parent set should contain parent2", parentSet.contains(parent2));
		Assert.assertTrue("Child set should contain clone", childSet.contains(clone));
		
		//hybrid child of clone
		Assert.assertEquals("There should be exactly 1 hybrid relationship in which the clone takes the parent role", 1, clone.getParentRelationships().size());
		HybridRelationship parentRelation = (HybridRelationship)clone.getHybridParentRelations().iterator().next();
		Assert.assertSame("Clone should be parent in parentRelationship", clone, parentRelation.getRelatedFrom());
		Assert.assertSame("Child should be child in parentRelationship", child, parentRelation.getRelatedTo());
		Assert.assertSame("Relationship type should be cloned correctly", HybridRelationshipType.FEMALE_PARENT(), parentRelation.getType());
		Assert.assertEquals("Rule should be cloned correctly", "child rule", parentRelation.getRuleConsidered());
	}
}
