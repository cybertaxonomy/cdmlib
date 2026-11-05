/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.name;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.reference.IGeneric;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;

/**
 * @author a.mueller
 */
public class NonViralNameTest extends EntityTestBase {

    private static final Logger logger = LogManager.getLogger();

	private INonViralName nonViralName1;
	private INonViralName nonViralName2;

	@BeforeEach
	public void setUp() throws Exception {
		nonViralName1 = TaxonNameFactory.NewBotanicalInstance(null);
		nonViralName2 = TaxonNameFactory.NewBotanicalInstance(null);
	}

// ******************* TESTS ***********************************/

	@Test
	public final void testGenerateTitle() {
		String fullName = "Abies alba subsp. beta (L.) Mill.";
		nonViralName1.setGenusOrUninomial("Genus");
		nonViralName1.setSpecificEpithet("spec");
		nonViralName1.setRank(Rank.SPECIES());
		assertEquals("Genus spec", nonViralName1.generateTitle());
		assertEquals("", nonViralName2.generateTitle());
	}

	@Test
	public final void testIsCodeCompliant() {
		logger.warn("Not yet implemented"); // TODO
	}

	@Test
	public final void testNonViralName() {
		assertNotNull(nonViralName1);
	}

	@Test
	public final void testNonViralNameRank() {
		INonViralName nonViralName = TaxonNameFactory.NewNonViralInstance(Rank.GENUS());
		assertNotNull(nonViralName);
	}

	@Test
	public final void testNonViralNameRankStringStringStringAgentReferenceString() {
		Team agent = Team.NewInstance();
		Reference article = ReferenceFactory.newArticle();
		HomotypicalGroup homotypicalGroup = HomotypicalGroup.NewInstance();
		INonViralName nonViralName = TaxonNameFactory.NewNonViralInstance(Rank.GENUS(), "Genus", "infraGen", "species", "infraSpec", agent, article, "mikro", homotypicalGroup);
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

	@Test
	public final void testGetSetGenusOrUninomial() {
		nonViralName1.setGenusOrUninomial("Genus");
		assertEquals("Genus", nonViralName1.getGenusOrUninomial());
		nonViralName1.setGenusOrUninomial(null);
		assertEquals(null, nonViralName1.getGenusOrUninomial());
		nonViralName2.setGenusOrUninomial(null);
		assertEquals(null, nonViralName2.getGenusOrUninomial());
	}

	@Test
	public final void testGetSetInfraGenericEpithet() {
		nonViralName1.setInfraGenericEpithet("InfraGenus");
		assertEquals("InfraGenus", nonViralName1.getInfraGenericEpithet());
		nonViralName1.setInfraGenericEpithet(null);
		assertEquals(null, nonViralName1.getInfraGenericEpithet());
		nonViralName2.setInfraGenericEpithet(null);
		assertEquals(null, nonViralName2.getInfraGenericEpithet());
	}

	@Test
	public final void testGetSetSpecificEpithet() {
		nonViralName1.setSpecificEpithet("specEpi");
		assertEquals("specEpi", nonViralName1.getSpecificEpithet());
		nonViralName1.setSpecificEpithet(null);
		assertEquals(null, nonViralName1.getSpecificEpithet());
		nonViralName2.setSpecificEpithet(null);
		assertEquals(null, nonViralName2.getSpecificEpithet());
	}

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
		team1.setNomenclaturalTitleCache(strTeam1, true);
		team2.setNomenclaturalTitleCache(strTeam2, true);
		nonViralName1.setGenusOrUninomial("Abies");
		nonViralName1.setSpecificEpithet("alba");
		nonViralName1.setNomenclaturalReference(ref1);
		assertEquals("Abies alba", nonViralName1.getNameCache());

		nonViralName1.setCombinationAuthorship(team1);
		assertEquals(team1, nonViralName1.getCombinationAuthorship());
		assertEquals(strTeam1, nonViralName1.getAuthorshipCache());
		assertEquals("Abies alba "+strTeam1, nonViralName1.getTitleCache());
		assertEquals("Abies alba "+strTeam1+ ", RefTitle", nonViralName1.getFullTitleCache());

		nonViralName1.setAuthorshipCache(strTeam2);
		assertEquals(strTeam2, nonViralName1.getAuthorshipCache());
		nonViralName1.setGenusOrUninomial("Calendula");
		assertEquals("Calendula alba "+strTeam2, nonViralName1.getTitleCache());

		nonViralName1.setAuthorshipCache(strTeam3);
		assertEquals("Calendula alba "+strTeam3, nonViralName1.getTitleCache());

		assertEquals("Calendula alba "+strTeam3+ ", RefTitle", nonViralName1.getFullTitleCache());

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

	@Test
	public final void testAddHybridRelationship() {
		INonViralName nonViralName1 = TaxonNameFactory.NewNonViralInstance(null);
		assertEquals(0, nonViralName1.getHybridParentRelations().size());
		assertEquals(0, nonViralName1.getHybridChildRelations().size());
		TaxonName botanicalName2 = TaxonNameFactory.NewNonViralInstance(null);
		assertThrows(IllegalArgumentException.class, () -> botanicalName2.addHybridRelationship(null));
	}

	@Test
	public final void testRemoveHybridRelationship() {
		INonViralName botanicalName1 = TaxonNameFactory.NewNonViralInstance(null);
		assertEquals(0, botanicalName1.getHybridParentRelations().size());
		assertEquals(0, botanicalName1.getHybridChildRelations().size());
		IBotanicalName femaleParent = TaxonNameFactory.NewBotanicalInstance(null);
		TaxonName maleParent = TaxonNameFactory.NewNonViralInstance(null);
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
		HybridRelationship childRel = nonViralName1.addHybridChild(child, HybridRelationshipType.FEMALE_PARENT(), "child rule");


		INonViralName clone = (nonViralName1).clone();
		assertEquals("Aus", clone.getGenusOrUninomial(), "Genus should be equal");
		assertEquals("Infaus", clone.getInfraGenericEpithet(), "Infragenus should be equal");
		assertEquals("bus", clone.getSpecificEpithet(), "Specific epithet should be equal");
		assertEquals("infrabus", clone.getInfraSpecificEpithet(), "Infraspecific epithet should be equal");
		assertEquals(true, clone.isBinomHybrid(), "BinomHybrid should be equal");
		assertSame(combinationAuthor, clone.getCombinationAuthorship(), "Combination author should be the same");
		assertEquals(nonViralName1.getNameCache(), clone.getNameCache(), "NameCache should be equal");
		assertEquals(nonViralName1.getAuthorshipCache(), clone.getAuthorshipCache(), "AuthorshipCache should be equal");

		clone.setSpecificEpithet("sub");
		assertEquals("Aus (\u00D7Infaus) sub subsp. infrabus", clone.getNameCache(), "NameCache should be changed");

		//hybrid parents of clone
		assertEquals(2, clone.getHybridChildRelations().size(), "There should be exactly 2 hybrid relationships in which the clone takes the child role");
		Set<TaxonName> parentSet = new HashSet<>();
		Set<TaxonName> childSet = new HashSet<>();
		for (Object object : clone.getHybridChildRelations()){
			HybridRelationship childRelation = (HybridRelationship)object;
			TaxonName relatedFrom = childRelation.getRelatedFrom();
			parentSet.add(relatedFrom);
			TaxonName relatedTo = childRelation.getRelatedTo();
			childSet.add(relatedTo);
		}
		assertTrue(parentSet.contains(parent), "Parent set should contain parent1");
		assertTrue(parentSet.contains(parent2), "Parent set should contain parent2");
		assertTrue(childSet.contains(clone), "Child set should contain clone");

		//hybrid child of clone
		assertEquals(1, clone.getHybridParentRelations().size(), "There should be exactly 1 hybrid relationship in which the clone takes the parent role");
		HybridRelationship parentRelation = clone.getHybridParentRelations().iterator().next();
		assertSame(clone, parentRelation.getRelatedFrom(), "Clone should be parent in parentRelationship");
		assertSame(child, parentRelation.getRelatedTo(), "Child should be child in parentRelationship");
		assertSame(HybridRelationshipType.FEMALE_PARENT(), parentRelation.getType(), "Relationship type should be cloned correctly");
		assertEquals("child rule", parentRelation.getRuleConsidered(), "Rule should be cloned correctly");
        assertEquals(childRel.getRuleConsidered(), parentRelation.getRuleConsidered(), "Rule should be cloned correctly");
        //TODO string can be same but RuleConsidered object must differ, but this has no getter currently
//	    assertNotSame(childRel.getRuleConsidered(), parentRelation.getRuleConsidered(), "Rule should be cloned correctly");
	}
}
