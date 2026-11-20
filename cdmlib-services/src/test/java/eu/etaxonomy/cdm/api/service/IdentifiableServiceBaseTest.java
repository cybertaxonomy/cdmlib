/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.api.filter.MatchMode;
import eu.etaxonomy.cdm.api.service.dto.IdentifiedEntityDTO;
import eu.etaxonomy.cdm.api.service.dto.MarkedEntityDTO;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Identifier;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.term.IdentifierType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.model.term.VocabularyEnum;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.TaxonTitleType;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmExpectedDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmServicesTransactionalSpringIntegrationTest;

/**
 * @author a.mueller
 */
public class IdentifiableServiceBaseTest extends CdmServicesTransactionalSpringIntegrationTest {

    @SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	@Autowired
	private INameService nameService;

	@Autowired
	private ITermService termService;

	@Autowired
	private IVocabularyService vocService;

	@Autowired
	private ITaxonService taxonService;

	@Autowired
    private IReferenceService referenceService;

	@Autowired
	private IClassificationService classificationService;

/****************** TESTS *****************************/

	@Test
	public final void voidTestSeriveExists(){
		assertNotNull(nameService, "Service shoulb be initialized");
	}

	@Test
	@CdmDataSet
	@CdmExpectedDataSet
	public final void testUpdateTitleCache() {
		assertEquals(5, nameService.count(TaxonName.class), "There should be 5 TaxonNames in the data set");
		Class<TaxonName> clazz = TaxonName.class;
		int stepSize = 2;
		nameService.updateCaches(clazz, stepSize, null, null);
		commit();
//		commitAndStartNewTransaction(new String[]{"TaxonName","TaxonName_AUD"});
	}

	@Test
	@CdmDataSet("IdentifiableServiceBaseTest.testFindByIdentifierOrMarker.xml")
	public final void testFindByIdentifier(){
		UUID uuidIdentifierType1 = UUID.fromString("02bb62db-a229-4eeb-83e6-a9a093943d5e");
		UUID uuidIdentifierType2 = UUID.fromString("ef6e960f-5289-456c-b25c-cff7f4de2f63");

		IdentifierType it1 = (IdentifierType)termService.find(uuidIdentifierType1);
		assertNotNull(it1, "identifier type must not be null");

		boolean includeEntity = true;
		Pager<IdentifiedEntityDTO<Taxon>> taxonPager = taxonService.findByIdentifier(Taxon.class, "ext-1234", it1, null, includeEntity, null, null, null);
		assertTrue(taxonPager.getCount() == 1, "Result should not be empty");
		IdentifiedEntityDTO<Taxon>.CdmEntity entity = taxonPager.getRecords().get(0).getCdmEntity();
		Taxon taxon = entity.getEntity();
		assertEquals(UUID.fromString("888cded1-cadc-48de-8629-e32927919879"), taxon.getUuid());
		assertEquals(UUID.fromString("888cded1-cadc-48de-8629-e32927919879"), entity.getUuid());
		assertEquals(1, taxon.getIdentifiers().size(), "Taxon should have 1 identifier");
		Identifier identifier = taxon.getIdentifiers().get(0);
		IdentifierType type = CdmBase.deproxy(identifier.getType(), IdentifierType.class);
		assertEquals(uuidIdentifierType1, type.getUuid());

		Pager<IdentifiedEntityDTO<TaxonName>> names = nameService.findByIdentifier(
				TaxonName.class, "ext-1234", null, null, includeEntity, null, null, null);
		assertTrue(names.getCount() == 0, "Identifier does not exist for TaxonName");

		taxonPager = taxonService.findByIdentifier(null, "ext-1234", null, null, includeEntity, null, null, null);
		assertEquals(1, taxonPager.getRecords().size(), "Result size for 'ext-1234' should be 1");

		taxonPager = taxonService.findByIdentifier(Taxon.class, null, null, null, includeEntity, null, null, null);
		assertEquals(2, taxonPager.getRecords().size(), "Result should not be empty");

		//includeEntity
		includeEntity = false;
		taxonPager = taxonService.findByIdentifier(Taxon.class, "ext-1234", it1, null, includeEntity, null, null, null);
		entity = taxonPager.getRecords().get(0).getCdmEntity();
		assertNull(entity.getEntity(), "Taxon must not be returned with includeEntity = false");

		//Matchmode
		includeEntity = false;
		MatchMode matchmode = null;
		taxonPager = taxonService.findByIdentifier(Taxon.class, "123", null, matchmode, includeEntity, null, null, null);
		assertTrue(taxonPager.getCount() == 0, "Result size for '123' should be 0");

		taxonPager = taxonService.findByIdentifier(Taxon.class, "123", null, MatchMode.EXACT, includeEntity, null, null, null);
		assertTrue(taxonPager.getCount() == 0, "Result size for '123' should be 0");

		taxonPager = taxonService.findByIdentifier(Taxon.class, "123", null, MatchMode.ANYWHERE, includeEntity, null, null, null);
		assertTrue(taxonPager.getCount() == 1, "Result size for '123' should be 1");

		taxonPager = taxonService.findByIdentifier(Taxon.class, "123", null, MatchMode.BEGINNING, includeEntity, null, null, null);
		assertTrue(taxonPager.getCount() == 0, "Result size for '123' should be 0");

		taxonPager = taxonService.findByIdentifier(Taxon.class, "ext", null, MatchMode.BEGINNING, includeEntity, null, null, null);
		assertTrue(taxonPager.getCount() == 2, "Result size for 'ext' should be 1");

		//Paging
		taxonPager = taxonService.findByIdentifier(null, "ext", null, MatchMode.BEGINNING, includeEntity, null, null, null);
		assertEquals(4, taxonPager.getRecords().size(), "Total result size for starts with 'ext' should be 4");
		taxonPager = taxonService.findByIdentifier(null, "ext", null, MatchMode.BEGINNING, includeEntity, 2, 1, null);
		assertEquals(Long.valueOf(4), taxonPager.getCount(), "Total result size for starts with 'ext' should be 4");
		assertEquals(Integer.valueOf(2), taxonPager.getPageSize(), "Result size for starts with 'ext' second page should be 2");
		assertEquals("ext-syn1", taxonPager.getRecords().get(0).getIdentifier().getIdentifier(), "The third taxon (first on second page) should be ext-syn1");

		taxonPager = taxonService.findByIdentifier(Taxon.class, "ext", null, MatchMode.BEGINNING, includeEntity, null, null, null);
		assertTrue(taxonPager.getCount() == 2, "Result size for 'ext' should be 2");

	}

	@Test
	@CdmDataSet("IdentifiableServiceBaseTest.testFindByIdentifierOrMarker.xml")
	public final void testFindByIdentifierClassification(){
		//classification Filter
		Classification classification = classificationService.find(5000);
		TaxonNode rootNode = classification.getRootNode();
		Pager<IdentifiedEntityDTO<Taxon>> taxonPager = taxonService.findByIdentifier(Taxon.class, "ext-1234", null, rootNode, MatchMode.EXACT, false, null, null, null);
		assertEquals(Long.valueOf(1), taxonPager.getCount(), "Result size for 'ext' should be 1");
		assertEquals(1, taxonPager.getRecords().size(), "Result size for 'ext' should be 1");

		Pager<IdentifiedEntityDTO<Taxon>> taxPager = taxonService.findByIdentifier(Taxon.class, "ext-cache1", null, rootNode, MatchMode.EXACT, false, null, null, null);
		assertEquals(Long.valueOf(0), taxPager.getCount(), "Result size for 'ext' should be 0");
		assertEquals(0, taxPager.getRecords().size(), "Result size for 'ext' should be 0");

		rootNode = null;  //check against missing filter
		taxPager = taxonService.findByIdentifier(Taxon.class, "ext-cache1", null, rootNode, MatchMode.EXACT, false, null, null, null);
		assertEquals(Long.valueOf(1), taxPager.getCount(), "Result size for 'ext-cache1' without filter should be 1");
		assertEquals(1, taxPager.getRecords().size(), "Result size for 'ext-cache1' without filter should be 1");

		//TaxonBase
		rootNode = classification.getRootNode();
		Pager<IdentifiedEntityDTO<TaxonBase>> tbPager = taxonService.findByIdentifier(TaxonBase.class, "ext-1234", null, rootNode, MatchMode.EXACT, false, null, null, null);
		assertEquals(Long.valueOf(1), tbPager.getCount(), "Result size for 'ext' should be 1");
		assertEquals(1, tbPager.getRecords().size(), "Result size for 'ext' should be 1");

		tbPager = taxonService.findByIdentifier(TaxonBase.class, "ext-cache1", null, rootNode, MatchMode.EXACT, false, null, null, null);
		assertEquals(Long.valueOf(0), tbPager.getCount(), "Result size for 'ext' should be 0");
		assertEquals(0, tbPager.getRecords().size(), "Result size for 'ext' should be 0");

		//Synonym
		Pager<IdentifiedEntityDTO<Synonym>> synPager = taxonService.findByIdentifier(Synonym.class, "ext-syn", null, rootNode, MatchMode.BEGINNING, false, null, null, null);
		assertEquals(Long.valueOf(1), synPager.getCount(), "1 Synonym should be linked to the according classification");
		assertEquals(1, synPager.getRecords().size(), "1 Synonym should be linked to the according classification");

	}

	@Test
    @CdmDataSet("IdentifiableServiceBaseTest.testFindByIdentifierOrMarker.xml")
    public final void testListByIdentifier(){
        //classification Filter

        List<IdentifiedEntityDTO<Taxon>> taxonPager = taxonService.listByIdentifier(Taxon.class, "ext-1234", null, MatchMode.EXACT, false, null, null);//.listByIdentifier(Taxon.class, "ext-1234", null, rootNode, MatchMode.EXACT, false, null, null);
        assertEquals(1, taxonPager.size(), "Result size for 'ext' should be 1");
     //   assertEquals(1, taxonPager.getRecords().size(), "Result size for 'ext' should be 1");
        taxonPager = taxonService.listByIdentifier(Taxon.class, "ext-1234", null, MatchMode.EXACT, true, null, null);//.listByIdentifier(Taxon.class, "ext-1234", null, rootNode, MatchMode.EXACT, false, null, null);
        assertEquals(1, taxonPager.size(), "Result size for 'ext' should be 1");
        IdentifiedEntityDTO<Taxon> identifierDTO = taxonPager.get(0);
        assertTrue(identifierDTO.getCdmEntity() instanceof UuidAndTitleCache);

        List<IdentifiedEntityDTO<Taxon>> taxPager = taxonService.listByIdentifier(Taxon.class, "ext-cache1", null, MatchMode.EXACT, false, null, null);
        assertEquals(1, taxPager.size(), "Result size for 'ext' should be 1");

        List<IdentifiedEntityDTO<TaxonBase>> tbPager = taxonService.listByIdentifier(TaxonBase.class, "ext-1234", null, MatchMode.EXACT, false, null, null);
        assertEquals(1, tbPager.size(), "Result size for 'ext' should be 1");

        //Synonym
        List<IdentifiedEntityDTO<Synonym>> synPager = taxonService.listByIdentifier(Synonym.class, "ext-syn", null,  MatchMode.BEGINNING, false, null, null);
        assertEquals(2, synPager.size(), "There should be 2 synonyms with identifier ext-syn");

        List<IdentifiedEntityDTO<Reference>> referenceList = referenceService.listByIdentifier(Reference.class, "123", null, MatchMode.EXACT, false, null, null);//.listByIdentifier(Taxon.class, "ext-1234", null, rootNode, MatchMode.EXACT, false, null, null);
        assertEquals(1, taxonPager.size(), "Result size for 'ext' should be 1");

        referenceList = referenceService.listByIdentifier(Reference.class, "123", null, MatchMode.EXACT, true, null, null);//.listByIdentifier(Taxon.class, "ext-1234", null, rootNode, MatchMode.EXACT, false, null, null);
        assertEquals(1, referenceList.size(), "Result size for 'ext' should be 1");
        IdentifiedEntityDTO<Reference> identifierDTOReference = referenceList.get(0);
        assertTrue(identifierDTO.getCdmEntity() instanceof UuidAndTitleCache);
	}

	@Test
    @CdmDataSet("IdentifiableServiceBaseTest.testFindByIdentifierOrMarker.xml")
    public final void testFindByMarker(){
        //classification Filter
        Classification classification = classificationService.find(5000);
        TaxonNode rootNode = classification.getRootNode();
        Boolean markerValue = true;

        UUID uuidMarkerTypeCompleted = MarkerType.uuidComplete;
        UUID uuidMarkerTypeDoubtful = UUID.fromString("b51325c8-05fe-421a-832b-d86fc249ef6e");

        MarkerType markerType1 = (MarkerType)termService.find(uuidMarkerTypeCompleted);
        MarkerType noMarkerType = null;
        MarkerType markerType2 = (MarkerType)termService.find(uuidMarkerTypeDoubtful);
        assertNotNull(markerType2);

        TaxonTitleType titleType = TaxonTitleType.TAXON;

        MarkerType markerType = markerType1;
        Pager<MarkedEntityDTO<Taxon>> taxonPager = taxonService.findByMarker(Taxon.class, markerType, markerValue,
                rootNode, true, titleType, null, null, null);
        assertEquals(Long.valueOf(1), taxonPager.getCount(), "Result size for 'marker1=true' should be 1");
        assertEquals(1, taxonPager.getRecords().size(), "Result size for 'marker1=true' should be 1");
        MarkedEntityDTO<Taxon> dto = taxonPager.getRecords().get(0);
        MarkedEntityDTO<Taxon>.Marker marker = dto.getMarker();
        assertTrue(marker.getFlag(), "Flag must be true");
        assertEquals(uuidMarkerTypeCompleted, marker.getTypeUuid(), "Flag must be true");
        assertNotNull(dto.getCdmEntity().getEntity(), "the CDM entity in the dto must not be empty if includeEntity=true");
        assertEquals(5000, dto.getCdmEntity().getEntity().getId());

        markerValue = false;
        taxonPager = taxonService.findByMarker(Taxon.class, markerType, markerValue, rootNode, false, titleType, null, null, null);
        assertEquals(Long.valueOf(0), taxonPager.getCount(), "Result size for 'marker1=false' should be 0");

        markerValue = true;
        markerType = noMarkerType;
        taxonPager = taxonService.findByMarker(Taxon.class, markerType, markerValue, rootNode, false, titleType, null, null, null);
        assertEquals(Long.valueOf(0), taxonPager.getCount(), "Result size for not existing marker type should be 0");

        markerType = markerType2;
        taxonPager = taxonService.findByMarker(Taxon.class, markerType, markerValue, rootNode, false, titleType, null, null, null);
        assertEquals(Long.valueOf(0), taxonPager.getCount(), "Result size for markerType2 should be 0");

        rootNode = null;
        markerType = markerType1;
        taxonPager = taxonService.findByMarker(Taxon.class, markerType, markerValue, rootNode, false, titleType, null, null, null);
        assertEquals(Long.valueOf(2), taxonPager.getCount(), "Result size for no subtree should be 2");

        Pager<MarkedEntityDTO<TaxonBase>> taxonBasePager = taxonService.findByMarker(TaxonBase.class, markerType, markerValue, rootNode, false, titleType, null, null, null);
        assertEquals(Long.valueOf(3), taxonBasePager.getCount(), "Result size for taxa and synonyms without subtree filter with flag = true should be 3");

        markerValue = null;
        taxonBasePager = taxonService.findByMarker(TaxonBase.class, markerType, markerValue, rootNode, false, titleType, null, null, null);
        assertEquals(Long.valueOf(4), taxonBasePager.getCount(), "Result size for taxa and synonyms without subtree filter with any flag value should be 4");

        markerValue = true;
        Pager<MarkedEntityDTO<TaxonName>> namePager = nameService.findByMarker(TaxonName.class, markerType, markerValue, false, null, null, null);
        assertEquals(Long.valueOf(1), namePager.getCount(), "Result size for names with flag = true should be 1");

    }

//	@Test
    @Override
    public void createTestDataSet() throws FileNotFoundException {
		TermVocabulary<IdentifierType> voc = vocService.find(VocabularyEnum.IdentifierType.getUuid());

		IdentifierType identifierType1 = IdentifierType.NewInstance(null, "identifierType1", null);
    	voc.addTerm(identifierType1);
		termService.save(identifierType1);
		IdentifierType identifierType2 = IdentifierType.NewInstance(null, "identifierType2", null);
    	voc.addTerm(identifierType2);
		termService.save(identifierType2);

    	IBotanicalName name = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        Taxon taxonBase = Taxon.NewInstance(name, null);
        taxonBase.addIdentifier("ext-1234", identifierType1);
        name.addIdentifier("ext-name12", identifierType2);
        taxonService.save(taxonBase);

        Taxon taxonBase2 = Taxon.NewInstance(null, null);
        taxonBase2.setTitleCache("Cached taxon", true);
        taxonBase2.addIdentifier("ext-cache1", identifierType2);
        taxonService.save(taxonBase2);

        Classification classification = Classification.NewInstance("My classification");
        classification.addChildTaxon(taxonBase, null, null);
        classificationService.save(classification);

        taxonBase2.addSynonymName(null, SynonymType.HOMOTYPIC_SYNONYM_OF);

        commitAndStartNewTransaction(null);

        // this will write flat xml file to the same package in the test resources
        // the test file is named after the test class like: TestClassName.xml
		writeDbUnitDataSetFile(new String[] {
		        "TAXONBASE", "TAXONNAME","IDENTIFIER","TAXONBASE_IDENTIFIER",
		        "TAXONNAME_IDENTIFIER",
		        "REFERENCE",
		        "CLASSIFICATION", "TAXONNODE",
		        "HOMOTYPICALGROUP",
		        "TERMVOCABULARY"
		 }, "xxxx", true);
    }
}