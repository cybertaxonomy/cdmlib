/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.api.service.exception.HomotypicalGroupChangeException;
import eu.etaxonomy.cdm.model.metadata.SecReferenceHandlingEnum;
import eu.etaxonomy.cdm.model.metadata.SecReferenceHandlingSwapEnum;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmServicesTransactionalSpringIntegrationTest;

/**
 * This test checks of all the business logic methods do what they are expected to do.
 *
 * @author n.hoffmann
 * @since Dec 16, 2010
 */
public class TaxonServiceImplBusinessTest extends CdmServicesTransactionalSpringIntegrationTest {

    @Autowired
    private ITaxonService service;

    @Autowired
    private INameService nameService;

    @Autowired
    private IReferenceDao referenceDao;

	private Synonym s1;
	private Synonym s2;
	private Taxon t2;
	private Taxon t1;
	private String referenceDetail;
	private Reference reference;
	private SynonymType homoTypicSynonymType;
	private SynonymType heteroTypicSynonymType;
	private INonViralName s1n;
	private INonViralName t2n;
	private INonViralName t1n;
	private TaxonName s2n;

	@BeforeEach
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
		service.save(s2);

		// referencing
		homoTypicSynonymType = SynonymType.HOMOTYPIC_SYNONYM_OF;
		heteroTypicSynonymType = SynonymType.HETEROTYPIC_SYNONYM_OF;
		reference = ReferenceFactory.newGeneric();
		referenceDao.save(reference);
		referenceDetail = "test";
	}

	@Test
	public final void testSwapSynonymAndAcceptedTaxon() {
		t1.addSynonym(s1, homoTypicSynonymType);

		UpdateResult result = service.swapSynonymAndAcceptedTaxon(s1, t1, true, false, SecReferenceHandlingSwapEnum.KeepOrWarn, null, null);
		Synonym syn = (Synonym)service.load(s1.getUuid());
		//only the name has changed
		assertNotNull(syn);
		TaxonName name = nameService.load(s1n.getUuid());
		assertNotNull(name);

		//The name of the new accepted taxon should be the same as from the former synonym
		assertEquals(((Taxon)result.getCdmEntity()).getName(), name);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#changeSynonymToAcceptedTaxon(eu.etaxonomy.cdm.model.taxon.Synonym, eu.etaxonomy.cdm.model.taxon.Taxon)}.
	 */
	@Test
	public final void testChangeSynonymToAcceptedTaxon() {

		t1.addSynonym(s1, homoTypicSynonymType);
		HomotypicalGroup oldGroup = s1.getName().getHomotypicalGroup();
		assertEquals(2, oldGroup.getTypifiedNames().size(), "Homotypical group of new accepted taxon should contain exactly 2 names");
		boolean deleteSynonym = false;
		Taxon taxon = null;

		UpdateResult result;
        try {
            result = service.changeSynonymToAcceptedTaxon(s1, t1, null, null, null,deleteSynonym);
            assertTrue(result.isAbort(), "Change must fail for synonym and taxon in same homotypical group");
        } catch (HomotypicalGroupChangeException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

		t1.addSynonym(s2, heteroTypicSynonymType);
		assertEquals(2, oldGroup.getTypifiedNames().size(), "Homotypical group of old accepted taxon should still contain exactly 2 names");
		assertTrue(t1.getSynonyms().size() == 2, "Old accepted taxon should now have 2 synonyms");
		try {
			taxon = (Taxon)service.changeSynonymToAcceptedTaxon(s2, t1, null, null, null,deleteSynonym).getCdmEntity();
		} catch (HomotypicalGroupChangeException e) {
			fail("Change must not throw exception for heterotypic synonym change");
		}

		assertTrue(t1.getSynonyms().size() == 1, "Former accepted taxon should still have 1 synonym");
		assertNotNull(taxon);
		assertEquals(s2n, taxon.getName());
		HomotypicalGroup newGroup = taxon.getName().getHomotypicalGroup();
		assertEquals(1, newGroup.getTypifiedNames().size(), "Homotypical group of new accepted taxon should contain exactly one name");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#changeSynonymToAcceptedTaxon(eu.etaxonomy.cdm.model.taxon.Synonym, eu.etaxonomy.cdm.model.taxon.Taxon)}.
	 */
	@Test
	public final void testChangeSynonymWithMultipleSynonymsInHomotypicalGroupToAcceptedTaxon() {
		t1.addSynonym(s1, heteroTypicSynonymType);
		TaxonName otherHeteroSynonymName = TaxonNameFactory.NewNonViralInstance(null);
		t1.addHeterotypicSynonymName(otherHeteroSynonymName);
		TaxonName homotypicSynonymName = TaxonNameFactory.NewNonViralInstance(null);
		Synonym homotypicSynonym = Synonym.NewInstance(homotypicSynonymName, t1.getSec());
		t1.addHomotypicSynonym(homotypicSynonym);

		HomotypicalGroup group = s1.getHomotypicGroup();
		Reference citation1 = ReferenceFactory.newBook();
		String microReference1 = "p. 55";
		Synonym s2 = t1.addHeterotypicSynonymName(s2n, citation1, microReference1, group);
		HomotypicalGroup homoGroup2 = s1.getHomotypicGroup();
		assertEquals(group, homoGroup2, "Homotypical group must be the same group as for the old synonym");

		//run
		Taxon newTaxon = null;
		try {
			newTaxon = (Taxon)service.changeSynonymToAcceptedTaxon(s1, t1, null, null, SecReferenceHandlingEnum.KeepOrWarn, false).getCdmEntity();
		} catch (HomotypicalGroupChangeException e1) {
			fail("Invocation of change method should not throw an exception");
		}

		assertEquals(2, t1.getSynonyms().size(), "Former accepted taxon should now have 2 synonyms left");
		assertEquals(1, t1.getHeterotypicSynonymyGroups().size(), "Former accepted taxon should now have 1 heterotypic synonym group left");
		assertNotNull(newTaxon);
		assertEquals(s1n, newTaxon.getName());
		assertEquals(1, newTaxon.getSynonyms().size(), "New accepted taxon should have 1 synonym");
		assertEquals(s2, newTaxon.getSynonyms().iterator().next(), "The new synonym must be the homotypic synonym of the old synonym");
		HomotypicalGroup homoGroup = newTaxon.getHomotypicGroup();
		assertEquals(group, homoGroup, "Homotypical group must be the same group as for the old synonym");

		List<Synonym> synonymsInNewTaxonsGroup = newTaxon.getSynonymsInGroup(homoGroup);
		String message = "New accepted taxon should have 1 synonym in its homotypic group: s2. The old synonym may still exist (or not) but not as a synonym of the new taxon";
		assertEquals(1, synonymsInNewTaxonsGroup.size(), message);
		assertTrue(synonymsInNewTaxonsGroup.contains(s2), "The old synonym's homotypic 'partner' must be a synonym of the new accepted taxon, too.");
		assertTrue(newTaxon.getName().getHomotypicalGroup().equals(s2.getName().getHomotypicalGroup()), "The old synonym must be in the new accepted taxons homotypic group as it has not been deleted ");

		boolean iWasHere = false;
		for (Synonym syn : synonymsInNewTaxonsGroup){
			if (syn.equals(s2) ){
				assertEquals(citation1, s2.getSec(), "s2 relationship needs to have the same citation as the former relation to the given accepted taxon.");
				iWasHere = true;
			}
		}
		assertTrue(iWasHere, "Relationship to s2 must have been concidered in 'for'-loop");

		try {
			UpdateResult result = service.changeSynonymToAcceptedTaxon(homotypicSynonym, t1, null, null, null,false);

			assertTrue(!result.getExceptions().isEmpty(), "The method should throw an exception when invoked on taxa in the same homotypical group");
		} catch (HomotypicalGroupChangeException e) {
			//OK
		}

//		assertNull(s1.getName(), "Synonym should not be used in a name anymore");
	}

	@Test
    @CdmDataSet(cleanSweep = true, value = "/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
	public final void testChangeSynonymToRelatedTaxon() {

	    t1.addSynonym(s1, homoTypicSynonymType);
		t1.addSynonym(s2, homoTypicSynonymType);
		Set<TaxonBase> newInstances = new HashSet<>();
		newInstances.add(s1);
		newInstances.add(t1);
		newInstances.add(t2);
		service.save(newInstances);
		TaxonName synonymName = s1.getName();
		UUID synNameUUID = synonymName.getUuid();

		Taxon newTaxon = (Taxon) service.changeSynonymToRelatedTaxon(s1, t2, TaxonRelationshipType.CONGRUENT_OR_EXCLUDES(), reference, referenceDetail).getCdmEntity();
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
        newTaxon = (Taxon)service.changeSynonymToRelatedTaxon(
                s2, t1, TaxonRelationshipType.MISAPPLIED_NAME_FOR(), reference, referenceDetail)
                .getCdmEntity();
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
		TaxonName otherHeteroSynonymName = TaxonNameFactory.NewNonViralInstance(null);
		Synonym s2 = Synonym.NewInstance(otherHeteroSynonymName, t1.getSec());
		t1.addSynonym(s2, heteroTypicSynonymType);
		TaxonName otherHeteroSynonymNameB = TaxonNameFactory.NewNonViralInstance(null);
		otherHeteroSynonymName.addBasionym(otherHeteroSynonymNameB);
		Synonym s2b = Synonym.NewInstance(otherHeteroSynonymNameB, t1.getSec());
		t1.addSynonym(s2b, heteroTypicSynonymType);

		//homotypic
		TaxonName homotypicSynonymName = TaxonNameFactory.NewNonViralInstance(null);
		Synonym homotypicSynonym = Synonym.NewInstance(homotypicSynonymName, t1.getSec());
		t1.addHomotypicSynonym(homotypicSynonym);
		t1.getName().addBasionym(homotypicSynonymName);

		//Preconditions test
		assertFalse(s2.getHomotypicGroup().equals(s1.getHomotypicGroup()), "s2 must not be in s1 homotypic group");
		assertFalse(s2.getHomotypicGroup().equals(t1.getHomotypicGroup()), "s2 must not be in t1 homotypic group");
		assertNotNull(s2.getAcceptedTaxon(), "s2 must have exactly 1 synonym relationship/ must have accepted taxon");
		assertEquals(heteroTypicSynonymType, s2.getType(), "s2 must have heterotypic relationship type");
		assertEquals(1, s2.getName().getBasionyms().size(), "s2 must have exactly 1 basionym relationships");

		//do it
		service.changeHomotypicalGroupOfSynonym(s2, s1.getHomotypicGroup(), t1, true);

		//postconditions
		assertEquals(s2.getHomotypicGroup(), s1.getHomotypicGroup(), "s2 must be in s1 homotypic group");
		assertNotNull(s2.getAcceptedTaxon(), "s2 must have exactly 1 synonym relationship/accepted taxon");
		assertEquals(heteroTypicSynonymType, s2.getType(), "s2 must have heterotypic relationship");
		assertEquals(0, s2.getName().getBasionyms().size(), "s2 must have exactly 0 basionym relationships");

		//Preconditions test
		assertEquals(1, homotypicSynonym.getName().getNameRelations().size(), "'homotypicSynonym' must have exactly 1 basionym relationships");
		assertEquals(1, t1.getName().getBasionyms().size(), "'t1' must have exactly 1 basionym relationships");
		assertFalse(s2.getHomotypicGroup().equals(t1.getHomotypicGroup()), "s2 must not be in t1 homotypic group");

		//do it
		service.changeHomotypicalGroupOfSynonym(s2, homotypicSynonym.getHomotypicGroup(), null, true);

		//postconditions
		assertEquals(s2.getHomotypicGroup(), homotypicSynonym.getHomotypicGroup(), "s2 must be in 'homotypicSynonym' homotypic group");
		assertEquals(s2.getHomotypicGroup(), t1.getHomotypicGroup(), "s2 must be in 't1' homotypic group");
		assertNotNull(s2.getAcceptedTaxon(), "s2 must have exactly 1 synonym relationship/accepted taxon");
		assertEquals(this.homoTypicSynonymType, s2.getType(), "s2 must have homotypic relationship");
		assertEquals(1, s2.getName().getBasionyms().size(), "s2 must have exactly 1 basionym relationships");
		assertEquals(2, homotypicSynonym.getName().getNameRelations().size(), "'homotypicSynonym' must have exactly 2 basionym relationships");
		assertEquals(1, t1.getName().getBasionyms().size(), "'t1' must have exactly 1 basionym relationships");
		assertEquals(2, t1.getHomotypicSynonymsByHomotypicSynonymType().size(), "'t1' must have exactly 2 homotypic synonyms");
		assertEquals(2, t1.getHomotypicSynonymsByHomotypicGroup(null).size(), "'t1' must have exactly 2 names in homotypic group");
		assertEquals(3, t1.getHomotypicGroup().getTypifiedNames().size(), "'t1' homotypic group must include 3 names (t1, s2, homotypicSynonym)");

		//do it
		service.changeHomotypicalGroupOfSynonym(s2, t2.getHomotypicGroup(), t2, false);

		//postconditions
		assertEquals(t2.getHomotypicGroup(), s2.getHomotypicGroup(), "s2 must be in 't2' homotypic group");
		assertFalse(s2.getHomotypicGroup().equals(t1.getHomotypicGroup()), "s2 must not be in 't1' homotypic group");
		assertNotNull(s2.getAcceptedTaxon(), "s2 must have exactly 1 synonym relationship/accepted taxon");
		assertEquals(this.homoTypicSynonymType, s2.getType(), "s2 must have homotypic relationship");
		assertEquals(0, s2.getName().getBasionyms().size(), "s2 must have exactly 0 basionym relationships");
		assertEquals(1, homotypicSynonym.getName().getNameRelations().size(), "'homotypicSynonym' must have exactly 1 basionym relationships");
		assertEquals(1, t1.getName().getBasionyms().size(), "'t1' must have exactly 1 basionym relationships");
		assertEquals(1, t1.getHomotypicSynonymsByHomotypicSynonymType().size(), "'t1' must have exactly 1 homotypic synonyms");
		assertEquals(1, t1.getHomotypicSynonymsByHomotypicGroup().size(), "'t1' must have exactly 1 names in homotypic group");
		assertEquals(2, t1.getHomotypicGroup().getTypifiedNames().size(), "'t1' homotypic group must include 2 names (t1, homotypicSynonym)");

		//do it
		service.changeHomotypicalGroupOfSynonym(s2, s1.getHomotypicGroup(), t1, false);

		//postconditions
		assertEquals(s2.getHomotypicGroup(), s1.getHomotypicGroup(), "s2 must be in s1 homotypic group");
		assertFalse(t2.getHomotypicGroup().equals(s2.getHomotypicGroup()), "s2 must not be in 't2' homotypic group");
		//had 2 accepted taxa when synonym relationships still existed
		assertNotNull(s2.getAcceptedTaxon(), "s2 must have accepted taxon");
		assertEquals(heteroTypicSynonymType, s2.getType(), "Both relationships of s2 must be heterotypic");
		assertEquals(0, s2.getName().getBasionyms().size(), "s2 must have exactly 0 basionym relationships");

	}

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}