// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;

/**
 * This test checks of all the business logic methods do what they are expected to do.
 * 
 * @author n.hoffmann
 * @created Dec 16, 2010
 * @version 1.0
 */
public class TaxonServiceImplBusinessTest {

	private Synonym s1;
	private Taxon t2;
	private Taxon t1;
	private TaxonServiceImpl service;
	private String referenceDetail;
	private Reference reference;
	private SynonymRelationshipType synonymRelationshipType;
	private SynonymRelationshipType heteroTypicSynonymRelationshipType;
	private NonViralName s1n;
	private NonViralName t2n;
	private NonViralName t1n;
	private NonViralName s2n;

	@BeforeClass
	public static void setUpClass() throws Exception{
		new DefaultTermInitializer().initialize();
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		service = new TaxonServiceImpl();
		
		t1n = NonViralName.NewInstance(null);
		t1 = Taxon.NewInstance(t1n, reference);
		
		t2n = NonViralName.NewInstance(null);
		t2 = Taxon.NewInstance(t2n, reference);
		
		s1n = NonViralName.NewInstance(null);
		s1 = Synonym.NewInstance(s1n, reference);
		
		s2n = NonViralName.NewInstance(null);
		
		// referencing
		synonymRelationshipType = SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF();
		heteroTypicSynonymRelationshipType = SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF();
		reference = ReferenceFactory.newGeneric();
		referenceDetail = "test"; 
	}


	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#swapSynonymAndAcceptedTaxon(eu.etaxonomy.cdm.model.taxon.Synonym, eu.etaxonomy.cdm.model.taxon.Taxon)}.
	 */
	@Test
	public final void testSwapSynonymAndAcceptedTaxon() {
		t1.addSynonym(s1, synonymRelationshipType);
		
		service.swapSynonymAndAcceptedTaxon(s1, t1);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#changeSynonymToAcceptedTaxon(eu.etaxonomy.cdm.model.taxon.Synonym, eu.etaxonomy.cdm.model.taxon.Taxon)}.
	 */
	@Test
	public final void testChangeSynonymToAcceptedTaxon() {

		t1.addSynonym(s1, synonymRelationshipType);
		HomotypicalGroup oldGroup = s1.getName().getHomotypicalGroup();
		Assert.assertEquals("Homotypical group of new accepted taxon should contain exactly one name", 1, oldGroup.getTypifiedNames().size());
		Taxon taxon = service.changeSynonymToAcceptedTaxon(s1, t1,false, true, null, null);
		
		
		Assert.assertTrue("Former accepted taxon should not have synonyms anymore", t1.getSynonyms().isEmpty());
		Assert.assertNotNull(taxon);
		Assert.assertEquals(s1n, taxon.getName());
		HomotypicalGroup newGroup = taxon.getName().getHomotypicalGroup();
		Assert.assertEquals("Homotypical group of new accepted taxon should contain exactly one name", 1, newGroup.getTypifiedNames().size());
	}
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#changeSynonymToAcceptedTaxon(eu.etaxonomy.cdm.model.taxon.Synonym, eu.etaxonomy.cdm.model.taxon.Taxon)}.
	 */
	@Test
	public final void testChangeSynonymWithMultipleSynonymsInHomotypicalGroupToAcceptedTaxon() {
		t1.addSynonym(s1, heteroTypicSynonymRelationshipType);
		TaxonNameBase otherHeteroSynonymName = NonViralName.NewInstance(null);
		t1.addHeterotypicSynonymName(otherHeteroSynonymName);
		TaxonNameBase homotypicSynonymName = NonViralName.NewInstance(null);
		Synonym homotypicSynonym = Synonym.NewInstance(homotypicSynonymName, t1.getSec());
		t1.addHomotypicSynonym(homotypicSynonym, null, null);
		
		HomotypicalGroup group = s1.getHomotypicGroup();
		Reference citation1 = ReferenceFactory.newBook();
		String microReference1 = "p. 55";
		SynonymRelationship s2rel = t1.addHeterotypicSynonymName(s2n, group, citation1, microReference1);
		Synonym s2 = s2rel.getSynonym();
		HomotypicalGroup homoGroup2 = s1.getHomotypicGroup();
		Assert.assertEquals("Homotypical group must be the same group as for the old synonym", group, homoGroup2);
			
		Taxon newTaxon = service.changeSynonymToAcceptedTaxon(s1, t1, false, true, null, null);
	
		Assert.assertEquals("Former accepted taxon should now have 2 synonyms left", 2, t1.getSynonyms().size());
		Assert.assertEquals("Former accepted taxon should now have 1 heterotypic synonym group left", 1, t1.getHeterotypicSynonymyGroups().size());
		
		Assert.assertNotNull(newTaxon);
		Assert.assertEquals(s1n, newTaxon.getName());
		Assert.assertEquals("New accepted taxon should have 1 synonym", 1, newTaxon.getSynonyms().size());
		Assert.assertEquals("The new synonym must be the homotypic synonym of the old synonym", s2, newTaxon.getSynonyms().iterator().next());
		
		HomotypicalGroup homoGroup = newTaxon.getHomotypicGroup();
		Assert.assertEquals("Homotypical group must be the same group as for the old synonym", group, homoGroup);
		List<Synonym> synonymsInNewTaxonsGroup = homoGroup.getSynonymsInGroup(newTaxon.getSec());
		Assert.assertEquals("New accepted taxons homotypic group should have 2 synonym: s2 and the old synonym (which has not been deleted)", 2, synonymsInNewTaxonsGroup.size());
		Assert.assertTrue("The old synonym's homotypic 'partner' must be a synonym of the new accepted taxon, too.", synonymsInNewTaxonsGroup.contains(s2));
		Assert.assertTrue("The old synonym must be in the new accepted taxons homotypic group as it has not been deleted ", synonymsInNewTaxonsGroup.contains(s2));
		boolean iWasHere = false;
		for (Synonym syn : synonymsInNewTaxonsGroup){
			if (syn.equals(s2) ){
				SynonymRelationship rel = s2.getSynonymRelations().iterator().next();
				Assert.assertEquals("s2 relationship needs to have the same citation as the former relation to the given accepted taxon.", citation1, rel.getCitation());
				iWasHere = true;
			}
		}
		Assert.assertTrue("Relationship to s2 must have been concidered in 'for'-loop", iWasHere);
		
		try {
			Taxon newTaxon2 = service.changeSynonymToAcceptedTaxon(homotypicSynonym, t1, false, true, null, null);
			Assert.fail("The method should throw an exception when invoked on taxa in the same homotypical group");
		} catch (IllegalArgumentException e) {
			//OK
		}
		
//		Assert.assertNull("Synonym should not be used in a name anymore", s1.getName());
		
		
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#changeSynonymToRelatedTaxon(eu.etaxonomy.cdm.model.taxon.Synonym, eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType, eu.etaxonomy.cdm.model.reference.Reference, java.lang.String)}.
	 */
	@Test
	public final void testChangeSynonymToRelatedTaxon() {
		t1.addSynonym(s1, synonymRelationshipType);
		service.changeSynonymToRelatedTaxon(s1, t2, TaxonRelationshipType.CONGRUENT_OR_EXCLUDES(), reference, referenceDetail);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#moveSynonymToAnotherTaxon(eu.etaxonomy.cdm.model.taxon.SynonymRelationship, eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType, eu.etaxonomy.cdm.model.reference.Reference, java.lang.String)}.
	 */
	@Test
	public final void testMoveSynonymToAnotherTaxon() {
		t1.addSynonym(s1, synonymRelationshipType);
			
		SynonymRelationship synonymRelation = t1.getSynonymRelations().iterator().next();
		
		service.moveSynonymToAnotherTaxon(synonymRelation, t2, synonymRelationshipType, reference, referenceDetail);
		
		Assert.assertTrue("t1 should have no synonym relationships", t1.getSynonymRelations().isEmpty());
		
		Set<SynonymRelationship> synonymRelations = t2.getSynonymRelations();
		Assert.assertTrue("t2 should have exactly one synonym relationship", synonymRelations.size() == 1);
		
		synonymRelation = synonymRelations.iterator().next();
		
		Assert.assertEquals(t2, synonymRelation.getAcceptedTaxon());
		Assert.assertEquals(synonymRelationshipType, synonymRelation.getType());
		Assert.assertEquals(reference, synonymRelation.getCitation());
		Assert.assertEquals(referenceDetail, synonymRelation.getCitationMicroReference());
	}

}
