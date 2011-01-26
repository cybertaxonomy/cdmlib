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
import org.junit.Ignore;
import org.junit.Test;

import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.NonViralName;
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
	private NonViralName s1n;
	private NonViralName t2n;
	private NonViralName t1n;
	private NonViralName s2n;

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
	@Ignore
	public final void testChangeSynonymToAcceptedTaxon() {
		t1.addSynonym(s1, synonymRelationshipType);
		Taxon taxon = service.changeSynonymToAcceptedTaxon(s1, t1);
		
		Assert.assertTrue("Former accepted taxon should not have synonyms anymore", t1.getSynonyms().isEmpty());
		Assert.assertNotNull(taxon);
		Assert.assertEquals(s1n, taxon.getName());
		HomotypicalGroup newGroup = taxon.getName().getHomotypicalGroup();
		Assert.assertTrue("Homotypical group of new accepted taxon should contain exactly one name", newGroup.getTypifiedNames().size() == 1);
	}
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#changeSynonymToAcceptedTaxon(eu.etaxonomy.cdm.model.taxon.Synonym, eu.etaxonomy.cdm.model.taxon.Taxon)}.
	 */
	@Test
	@Ignore
	public final void testChangeSynonymWithMultipleSynonymsInHomotypicalGroupToAcceptedTaxon() {
		t1.addSynonym(s1, synonymRelationshipType);
				
		HomotypicalGroup group = s1.getHomotypicGroup();
		group.addTypifiedName(s2n);
		t1.addHeterotypicSynonymName(s2n);
		
		Taxon taxon = service.changeSynonymToAcceptedTaxon(s1, t1);
		
		Assert.assertTrue("Former accepted taxon should not have synonyms anymore", t1.getSynonyms().isEmpty());
		Assert.assertNotNull(taxon);
		Assert.assertEquals(s1n, taxon.getName());
		Assert.assertTrue("New accepted taxon should have a synonym", ! taxon.getSynonyms().isEmpty());
		
		List<Synonym> synonymsInNewTaxonsGroup = taxon.getHomotypicGroup().getSynonymsInGroup(taxon.getSec());
		Assert.assertTrue("New accepted taxons homotypic group should have a synonym", synonymsInNewTaxonsGroup.size() == 1);
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
