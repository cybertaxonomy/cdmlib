/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.exception.HomotypicalGroupChangeException;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * This test checks of all the business logic methods do what they are expected to do.
 *
 * @author n.hoffmann
 * @created Dec 16, 2010
 * @version 1.0
 */
public class TaxonServiceImplBusinessTest extends CdmIntegrationTest {

	private Synonym s1;
	private Synonym s2;
	private Taxon t2;
	private Taxon t1;
	@SpringBeanByType
	private ITaxonService service;

	@SpringBeanByType
	private INameService nameService;
	private String referenceDetail;
	private Reference reference;
	private SynonymType homoTypicSynonymType;
	private SynonymType heteroTypicSynonymType;
	private NonViralName<?> s1n;
	private NonViralName<?> t2n;
	private NonViralName<?> t1n;
	private NonViralName<?> s2n;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		//service = new TaxonServiceImpl();
		//nameService = new NameServiceImpl();

		t1n = TaxonNameFactory.NewNonViralInstance(null);
		t1 = Taxon.NewInstance(t1n, reference);

		t2n = TaxonNameFactory.NewNonViralInstance(null);
		t2 = Taxon.NewInstance(t2n, reference);

		s1n = TaxonNameFactory.NewNonViralInstance(null);
		s1 = Synonym.NewInstance(s1n, reference);

		s2n = TaxonNameFactory.NewNonViralInstance(null);
		s2 = Synonym.NewInstance(s2n, reference);

		// referencing
		homoTypicSynonymType = SynonymType.HOMOTYPIC_SYNONYM_OF();
		heteroTypicSynonymType = SynonymType.HETEROTYPIC_SYNONYM_OF();
		reference = ReferenceFactory.newGeneric();
		referenceDetail = "test";
	}


	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#swapSynonymAndAcceptedTaxon(eu.etaxonomy.cdm.model.taxon.Synonym, eu.etaxonomy.cdm.model.taxon.Taxon)}.
	 */
	@Test
	public final void testSwapSynonymAndAcceptedTaxon() {
		t1.addSynonym(s1, homoTypicSynonymType);

		service.swapSynonymAndAcceptedTaxon(s1, t1);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#changeSynonymToAcceptedTaxon(eu.etaxonomy.cdm.model.taxon.Synonym, eu.etaxonomy.cdm.model.taxon.Taxon)}.
	 */
	@Test
	public final void testChangeSynonymToAcceptedTaxon() {

		t1.addSynonym(s1, homoTypicSynonymType);
		HomotypicalGroup oldGroup = s1.getName().getHomotypicalGroup();
		Assert.assertEquals("Homotypical group of new accepted taxon should contain exactly 2 names", 2, oldGroup.getTypifiedNames().size());
		boolean deleteSynonym = false;
		boolean copyCitationInfo = true;
		Taxon taxon = null;
		try {
			taxon = service.changeSynonymToAcceptedTaxon(s1, t1, deleteSynonym);
			Assert.fail("Change must fail for synonym and taxon in same homotypical group");
		} catch (HomotypicalGroupChangeException e) {
			//OK
		}
		t1.addSynonym(s2, heteroTypicSynonymType);
		Assert.assertEquals("Homotypical group of old accepted taxon should still contain exactly 2 names", 2, oldGroup.getTypifiedNames().size());
		Assert.assertTrue("Old accepted taxon should now have 2 synonyms", t1.getSynonyms().size() == 2);
		try {
			taxon = service.changeSynonymToAcceptedTaxon(s2, t1, deleteSynonym);
		} catch (HomotypicalGroupChangeException e) {
			Assert.fail("Change must not throw exception for heterotypic synonym change");
		}

		Assert.assertTrue("Former accepted taxon should still have 1 synonym", t1.getSynonyms().size() == 1);
		Assert.assertNotNull(taxon);
		Assert.assertEquals(s2n, taxon.getName());
		HomotypicalGroup newGroup = taxon.getName().getHomotypicalGroup();
		Assert.assertEquals("Homotypical group of new accepted taxon should contain exactly one name", 1, newGroup.getTypifiedNames().size());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#changeSynonymToAcceptedTaxon(eu.etaxonomy.cdm.model.taxon.Synonym, eu.etaxonomy.cdm.model.taxon.Taxon)}.
	 */
	@Test
	public final void testChangeSynonymWithMultipleSynonymsInHomotypicalGroupToAcceptedTaxon() {
		t1.addSynonym(s1, heteroTypicSynonymType);
		TaxonNameBase<?,?> otherHeteroSynonymName = TaxonNameFactory.NewNonViralInstance(null);
		t1.addHeterotypicSynonymName(otherHeteroSynonymName);
		TaxonNameBase<?,?> homotypicSynonymName = TaxonNameFactory.NewNonViralInstance(null);
		Synonym homotypicSynonym = Synonym.NewInstance(homotypicSynonymName, t1.getSec());
		t1.addHomotypicSynonym(homotypicSynonym);

		HomotypicalGroup group = s1.getHomotypicGroup();
		Reference citation1 = ReferenceFactory.newBook();
		String microReference1 = "p. 55";
		Synonym s2 = t1.addHeterotypicSynonymName(s2n, citation1, microReference1, group);
		HomotypicalGroup homoGroup2 = s1.getHomotypicGroup();
		Assert.assertEquals("Homotypical group must be the same group as for the old synonym", group, homoGroup2);

		//run
		Taxon newTaxon = null;
		try {
			newTaxon = service.changeSynonymToAcceptedTaxon(s1, t1, false);
		} catch (HomotypicalGroupChangeException e1) {
			Assert.fail("Invocation of change method should not throw an exception");
		}

		Assert.assertEquals("Former accepted taxon should now have 2 synonyms left", 2, t1.getSynonyms().size());
		Assert.assertEquals("Former accepted taxon should now have 1 heterotypic synonym group left", 1, t1.getHeterotypicSynonymyGroups().size());
		Assert.assertNotNull(newTaxon);
		Assert.assertEquals(s1n, newTaxon.getName());
		Assert.assertEquals("New accepted taxon should have 1 synonym", 1, newTaxon.getSynonyms().size());
		Assert.assertEquals("The new synonym must be the homotypic synonym of the old synonym", s2, newTaxon.getSynonyms().iterator().next());
		HomotypicalGroup homoGroup = newTaxon.getHomotypicGroup();
		Assert.assertEquals("Homotypical group must be the same group as for the old synonym", group, homoGroup);

		List<Synonym> synonymsInNewTaxonsGroup = newTaxon.getSynonymsInGroup(homoGroup);
		String message = "New accepted taxon should have 1 synonym in its homotypic group: s2. The old synonym may still exist (or not) but not as a synonym of the new taxon";
		Assert.assertEquals(message, 1, synonymsInNewTaxonsGroup.size());
		Assert.assertTrue("The old synonym's homotypic 'partner' must be a synonym of the new accepted taxon, too.", synonymsInNewTaxonsGroup.contains(s2));
		Assert.assertTrue("The old synonym must be in the new accepted taxons homotypic group as it has not been deleted ", newTaxon.getName().getHomotypicalGroup().equals(s2.getName().getHomotypicalGroup()));

		boolean iWasHere = false;
		for (Synonym syn : synonymsInNewTaxonsGroup){
			if (syn.equals(s2) ){
				Assert.assertEquals("s2 relationship needs to have the same citation as the former relation to the given accepted taxon.", citation1, s2.getSec());
				iWasHere = true;
			}
		}
		Assert.assertTrue("Relationship to s2 must have been concidered in 'for'-loop", iWasHere);

		try {
			service.changeSynonymToAcceptedTaxon(homotypicSynonym, t1, false);
			Assert.fail("The method should throw an exception when invoked on taxa in the same homotypical group");
		} catch (HomotypicalGroupChangeException e) {
			//OK
		}

//		Assert.assertNull("Synonym should not be used in a name anymore", s1.getName());


	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#changeSynonymToRelatedTaxon(eu.etaxonomy.cdm.model.taxon.Synonym, eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType, eu.etaxonomy.cdm.model.reference.Reference, java.lang.String)}.
	 */
	@Test
	public final void testChangeSynonymToRelatedTaxon() {
		t1.addSynonym(s1, homoTypicSynonymType);
		t1.addSynonym(s2, homoTypicSynonymType);
		Set<TaxonBase> newInstances = new HashSet<>();
		newInstances.add(s1);
		newInstances.add(t1);
		newInstances.add(t2);
		service.save(newInstances);
		TaxonNameBase<?,?> synonymName = s1.getName();
		UUID synNameUUID = synonymName.getUuid();

		Taxon newTaxon = service.changeSynonymToRelatedTaxon(s1, t2, TaxonRelationshipType.CONGRUENT_OR_EXCLUDES(), reference, referenceDetail);
		//check removeTaxonBase()
		//UUID s1UUID = service.update(s1);
		UUID newTaxonUUID = service.save(newTaxon).getUuid();


		s1 =(Synonym)service.find(s1.getUuid());
		newTaxon = (Taxon)service.find(newTaxonUUID);
		assertNull(s1);
		synonymName = nameService.find(synNameUUID);
		assertFalse(synonymName.getTaxonBases().contains(s1));
		assertTrue(synonymName.getTaxonBases().contains(newTaxon));

		synonymName = s2.getName();
        newTaxon = service.changeSynonymToRelatedTaxon(s2, t1, TaxonRelationshipType.MISAPPLIED_NAME_FOR(), reference, referenceDetail);
        //check removeTaxonBase()
        //UUID s1UUID = service.update(s1);
        newTaxonUUID = service.save(newTaxon).getUuid();


        s2 =(Synonym)service.find(s2.getUuid());
        newTaxon = (Taxon)service.find(newTaxonUUID);
        assertNull(s2);

        assertFalse(synonymName.getTaxonBases().contains(s2));
        assertTrue(synonymName.getTaxonBases().contains(newTaxon));
	}


	@Test
	public void changeHomotypicalGroupOfSynonym(){

		//s1 - Heterotypic
		t1.addSynonym(s1, heteroTypicSynonymType);

		//s2 - heterotypic
		TaxonNameBase otherHeteroSynonymName = TaxonNameFactory.NewNonViralInstance(null);
		Synonym s2 = Synonym.NewInstance(otherHeteroSynonymName, t1.getSec());
		t1.addSynonym(s2, heteroTypicSynonymType);
		TaxonNameBase<?,?> otherHeteroSynonymNameB = TaxonNameFactory.NewNonViralInstance(null);
		otherHeteroSynonymName.addBasionym(otherHeteroSynonymNameB);
		Synonym s2b = Synonym.NewInstance(otherHeteroSynonymNameB, t1.getSec());
		t1.addSynonym(s2b, heteroTypicSynonymType);

		//homotypic
		TaxonNameBase<?,?> homotypicSynonymName = TaxonNameFactory.NewNonViralInstance(null);
		Synonym homotypicSynonym = Synonym.NewInstance(homotypicSynonymName, t1.getSec());
		t1.addHomotypicSynonym(homotypicSynonym);
		t1.getName().addBasionym(homotypicSynonymName);

		//Preconditions test
		Assert.assertFalse("s2 must not be in s1 homotypic group", s2.getHomotypicGroup().equals(s1.getHomotypicGroup()));
		Assert.assertFalse("s2 must not be in t1 homotypic group", s2.getHomotypicGroup().equals(t1.getHomotypicGroup()));
		Assert.assertNotNull("s2 must have exactly 1 synonym relationship/ must have accepted taxon", s2.getAcceptedTaxon());
		Assert.assertEquals("s2 must have heterotypic relationship type", heteroTypicSynonymType, s2.getType());
		Assert.assertEquals("s2 must have exactly 1 basionym relationships", 1, s2.getName().getBasionyms().size());

		//do it
		service.changeHomotypicalGroupOfSynonym(s2, s1.getHomotypicGroup(), t1, true);

		//postconditions
		Assert.assertEquals("s2 must be in s1 homotypic group", s2.getHomotypicGroup(), s1.getHomotypicGroup());
		Assert.assertNotNull("s2 must have exactly 1 synonym relationship/accepted taxon", s2.getAcceptedTaxon());
		Assert.assertEquals("s2 must have heterotypic relationship", heteroTypicSynonymType, s2.getType());
		Assert.assertEquals("s2 must have exactly 0 basionym relationships", 0, s2.getName().getBasionyms().size());


		//Preconditions test
		Assert.assertEquals("'homotypicSynonym' must have exactly 1 basionym relationships", 1, homotypicSynonym.getName().getNameRelations().size());
		Assert.assertEquals("'t1' must have exactly 1 basionym relationships", 1, t1.getName().getBasionyms().size());
		Assert.assertFalse("s2 must not be in t1 homotypic group", s2.getHomotypicGroup().equals(t1.getHomotypicGroup()));


		//do it
		service.changeHomotypicalGroupOfSynonym(s2, homotypicSynonym.getHomotypicGroup(), null, true);

		//postconditions
		Assert.assertEquals("s2 must be in 'homotypicSynonym' homotypic group", s2.getHomotypicGroup(), homotypicSynonym.getHomotypicGroup());
		Assert.assertEquals("s2 must be in 't1' homotypic group", s2.getHomotypicGroup(), t1.getHomotypicGroup());
		Assert.assertNotNull("s2 must have exactly 1 synonym relationship/accepted taxon", s2.getAcceptedTaxon());
		Assert.assertEquals("s2 must have homotypic relationship", this.homoTypicSynonymType, s2.getType());
		Assert.assertEquals("s2 must have exactly 1 basionym relationships", 1, s2.getName().getBasionyms().size());
		Assert.assertEquals("'homotypicSynonym' must have exactly 2 basionym relationships", 2, homotypicSynonym.getName().getNameRelations().size());
		Assert.assertEquals("'t1' must have exactly 1 basionym relationships", 1, t1.getName().getBasionyms().size());
		Assert.assertEquals("'t1' must have exactly 2 homotypic synonyms", 2, t1.getHomotypicSynonymsByHomotypicSynonymType().size());
		Assert.assertEquals("'t1' must have exactly 2 names in homotypic group", 2, t1.getHomotypicSynonymsByHomotypicGroup(null).size());
		Assert.assertEquals("'t1' homotypic group must include 3 names (t1, s2, homotypicSynonym)", 3, t1.getHomotypicGroup().getTypifiedNames().size());


		//do it
		service.changeHomotypicalGroupOfSynonym(s2, t2.getHomotypicGroup(), t2, false);

		//postconditions
		Assert.assertEquals("s2 must be in 't2' homotypic group", t2.getHomotypicGroup(), s2.getHomotypicGroup());
		Assert.assertFalse("s2 must not be in 't1' homotypic group", s2.getHomotypicGroup().equals(t1.getHomotypicGroup()));
		Assert.assertNotNull("s2 must have exactly 1 synonym relationship/accepted taxon", s2.getAcceptedTaxon());
		Assert.assertEquals("s2 must have homotypic relationship", this.homoTypicSynonymType, s2.getType());
		Assert.assertEquals("s2 must have exactly 0 basionym relationships", 0, s2.getName().getBasionyms().size());
		Assert.assertEquals("'homotypicSynonym' must have exactly 1 basionym relationships", 1, homotypicSynonym.getName().getNameRelations().size());
		Assert.assertEquals("'t1' must have exactly 1 basionym relationships", 1, t1.getName().getBasionyms().size());
		Assert.assertEquals("'t1' must have exactly 1 homotypic synonyms", 1, t1.getHomotypicSynonymsByHomotypicSynonymType().size());
		Assert.assertEquals("'t1' must have exactly 1 names in homotypic group", 1, t1.getHomotypicSynonymsByHomotypicGroup().size());
		Assert.assertEquals("'t1' homotypic group must include 2 names (t1, homotypicSynonym)", 2, t1.getHomotypicGroup().getTypifiedNames().size());

		//do it
		service.changeHomotypicalGroupOfSynonym(s2, s1.getHomotypicGroup(), t1, false);

		//postconditions
		Assert.assertEquals("s2 must be in s1 homotypic group", s2.getHomotypicGroup(), s1.getHomotypicGroup());
		Assert.assertFalse("s2 must not be in 't2' homotypic group", t2.getHomotypicGroup().equals(s2.getHomotypicGroup()));
		//had 2 accepted taxa when synonym relationships still existed
		Assert.assertNotNull("s2 must have accepted taxon", s2.getAcceptedTaxon());
		Assert.assertEquals("Both relationships of s2 must be heterotypic", heteroTypicSynonymType, s2.getType());
		Assert.assertEquals("s2 must have exactly 0 basionym relationships", 0, s2.getName().getBasionyms().size());

	}


    @Override
    public void createTestDataSet() throws FileNotFoundException {}

}
