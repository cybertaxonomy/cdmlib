/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.database.annotations.Transactional;
import org.unitils.database.util.TransactionMode;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.ExpectedDataSet;
import org.unitils.spring.annotation.SpringBeanByType;

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
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.TaxonTitleType;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 */
@Transactional(TransactionMode.DISABLED)
public class IdentifiableServiceBaseTest extends CdmTransactionalIntegrationTest {

    @SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	@SpringBeanByType
	private INameService nameService;

	@SpringBeanByType
	private ITermService termService;

	@SpringBeanByType
	private IVocabularyService vocService;

	@SpringBeanByType
	private ITaxonService taxonService;

	@SpringBeanByType
    private IReferenceService referenceService;

	@SpringBeanByType
	private IClassificationService classificationService;

/****************** TESTS *****************************/

	@Test
	public final void voidTestSeriveExists(){
		Assert.assertNotNull("Service shoulb be initialized", nameService);
	}

	@Test
	@DataSet
	@ExpectedDataSet
	public final void testUpdateTitleCache() {
		Assert.assertEquals("There should be 5 TaxonNames in the data set", 5, nameService.count(TaxonName.class));
		Class<TaxonName> clazz = TaxonName.class;
		int stepSize = 2;
		nameService.updateCaches(clazz, stepSize, null, null);
		commit();
//		commitAndStartNewTransaction(new String[]{"TaxonName","TaxonName_AUD"});
	}

	@Test
	@DataSet(value="IdentifiableServiceBaseTest.testFindByIdentifierOrMarker.xml")
	public final void testFindByIdentifier(){
		UUID uuidIdentifierType1 = UUID.fromString("02bb62db-a229-4eeb-83e6-a9a093943d5e");
		UUID uuidIdentifierType2 = UUID.fromString("ef6e960f-5289-456c-b25c-cff7f4de2f63");

		IdentifierType it1 = (IdentifierType)termService.find(uuidIdentifierType1);
		Assert.assertNotNull("identifier type must not be null", it1);

		boolean includeEntity = true;
		Pager<IdentifiedEntityDTO<Taxon>> taxonPager = taxonService.findByIdentifier(Taxon.class, "ext-1234", it1, null, includeEntity, null, null, null);
		Assert.assertTrue("Result should not be empty", taxonPager.getCount() == 1);
		IdentifiedEntityDTO<Taxon>.CdmEntity entity = taxonPager.getRecords().get(0).getCdmEntity();
		Taxon taxon = entity.getEntity();
		Assert.assertEquals(UUID.fromString("888cded1-cadc-48de-8629-e32927919879"), taxon.getUuid());
		Assert.assertEquals(UUID.fromString("888cded1-cadc-48de-8629-e32927919879"), entity.getUuid());
		Assert.assertEquals("Taxon should have 1 identifier", 1, taxon.getIdentifiers().size());
		Identifier identifier = taxon.getIdentifiers().get(0);
		IdentifierType type = CdmBase.deproxy(identifier.getType(), IdentifierType.class);
		Assert.assertEquals(uuidIdentifierType1, type.getUuid());

		Pager<IdentifiedEntityDTO<TaxonName>> names = nameService.findByIdentifier(
				TaxonName.class, "ext-1234", null, null, includeEntity, null, null, null);
		Assert.assertTrue("Identifier does not exist for TaxonName", names.getCount() == 0);

		taxonPager = taxonService.findByIdentifier(null, "ext-1234", null, null, includeEntity, null, null, null);
		Assert.assertEquals("Result size for 'ext-1234' should be 1", 1, taxonPager.getRecords().size());

		taxonPager = taxonService.findByIdentifier(Taxon.class, null, null, null, includeEntity, null, null, null);
		Assert.assertEquals("Result should not be empty", 2 , taxonPager.getRecords().size());

		//includeEntity
		includeEntity = false;
		taxonPager = taxonService.findByIdentifier(Taxon.class, "ext-1234", it1, null, includeEntity, null, null, null);
		entity = taxonPager.getRecords().get(0).getCdmEntity();
		Assert.assertNull("Taxon must not be returned with includeEntity = false", entity.getEntity());


		//Matchmode
		includeEntity = false;
		MatchMode matchmode = null;
		taxonPager = taxonService.findByIdentifier(Taxon.class, "123", null, matchmode, includeEntity, null, null, null);
		Assert.assertTrue("Result size for '123' should be 0", taxonPager.getCount() == 0);

		taxonPager = taxonService.findByIdentifier(Taxon.class, "123", null, MatchMode.EXACT, includeEntity, null, null, null);
		Assert.assertTrue("Result size for '123' should be 0", taxonPager.getCount() == 0);

		taxonPager = taxonService.findByIdentifier(Taxon.class, "123", null, MatchMode.ANYWHERE, includeEntity, null, null, null);
		Assert.assertTrue("Result size for '123' should be 1", taxonPager.getCount() == 1);

		taxonPager = taxonService.findByIdentifier(Taxon.class, "123", null, MatchMode.BEGINNING, includeEntity, null, null, null);
		Assert.assertTrue("Result size for '123' should be 0", taxonPager.getCount() == 0);

		taxonPager = taxonService.findByIdentifier(Taxon.class, "ext", null, MatchMode.BEGINNING, includeEntity, null, null, null);
		Assert.assertTrue("Result size for 'ext' should be 1", taxonPager.getCount() == 2);

		//Paging
		taxonPager = taxonService.findByIdentifier(null, "ext", null, MatchMode.BEGINNING, includeEntity, null, null, null);
		Assert.assertEquals("Total result size for starts with 'ext' should be 4", 4, taxonPager.getRecords().size());
		taxonPager = taxonService.findByIdentifier(null, "ext", null, MatchMode.BEGINNING, includeEntity, 2, 1, null);
		Assert.assertEquals("Total result size for starts with 'ext' should be 4", Long.valueOf(4), taxonPager.getCount());
		Assert.assertEquals("Result size for starts with 'ext' second page should be 2", Integer.valueOf(2), taxonPager.getPageSize());
		Assert.assertEquals("The third taxon (first on second page) should be ext-syn1", "ext-syn1", taxonPager.getRecords().get(0).getIdentifier().getIdentifier());

		taxonPager = taxonService.findByIdentifier(Taxon.class, "ext", null, MatchMode.BEGINNING, includeEntity, null, null, null);
		Assert.assertTrue("Result size for 'ext' should be 2", taxonPager.getCount() == 2);

	}

	@Test
	@DataSet(value="IdentifiableServiceBaseTest.testFindByIdentifierOrMarker.xml")
	public final void testFindByIdentifierClassification(){
		//classification Filter
		Classification classification = classificationService.find(5000);
		TaxonNode rootNode = classification.getRootNode();
		Pager<IdentifiedEntityDTO<Taxon>> taxonPager = taxonService.findByIdentifier(Taxon.class, "ext-1234", null, rootNode, MatchMode.EXACT, false, null, null, null);
		Assert.assertEquals("Result size for 'ext' should be 1", Long.valueOf(1), taxonPager.getCount());
		Assert.assertEquals("Result size for 'ext' should be 1", 1, taxonPager.getRecords().size());

		Pager<IdentifiedEntityDTO<Taxon>> taxPager = taxonService.findByIdentifier(Taxon.class, "ext-cache1", null, rootNode, MatchMode.EXACT, false, null, null, null);
		Assert.assertEquals("Result size for 'ext' should be 0", Long.valueOf(0), taxPager.getCount());
		Assert.assertEquals("Result size for 'ext' should be 0", 0, taxPager.getRecords().size());

		rootNode = null;  //check against missing filter
		taxPager = taxonService.findByIdentifier(Taxon.class, "ext-cache1", null, rootNode, MatchMode.EXACT, false, null, null, null);
		Assert.assertEquals("Result size for 'ext-cache1' without filter should be 1", Long.valueOf(1), taxPager.getCount());
		Assert.assertEquals("Result size for 'ext-cache1' without filter should be 1", 1, taxPager.getRecords().size());

		//TaxonBase
		rootNode = classification.getRootNode();
		Pager<IdentifiedEntityDTO<TaxonBase>> tbPager = taxonService.findByIdentifier(TaxonBase.class, "ext-1234", null, rootNode, MatchMode.EXACT, false, null, null, null);
		Assert.assertEquals("Result size for 'ext' should be 1", Long.valueOf(1), tbPager.getCount());
		Assert.assertEquals("Result size for 'ext' should be 1", 1, tbPager.getRecords().size());

		tbPager = taxonService.findByIdentifier(TaxonBase.class, "ext-cache1", null, rootNode, MatchMode.EXACT, false, null, null, null);
		Assert.assertEquals("Result size for 'ext' should be 0", Long.valueOf(0), tbPager.getCount());
		Assert.assertEquals("Result size for 'ext' should be 0", 0, tbPager.getRecords().size());

		//Synonym
		Pager<IdentifiedEntityDTO<Synonym>> synPager = taxonService.findByIdentifier(Synonym.class, "ext-syn", null, rootNode, MatchMode.BEGINNING, false, null, null, null);
		Assert.assertEquals("1 Synonym should be linked to the according classification", Long.valueOf(1), synPager.getCount());
		Assert.assertEquals("1 Synonym should be linked to the according classification", 1, synPager.getRecords().size());

	}

	@Test
    @DataSet(value="IdentifiableServiceBaseTest.testFindByIdentifierOrMarker.xml")
    public final void testListByIdentifier(){
        //classification Filter

        List<IdentifiedEntityDTO<Taxon>> taxonPager = taxonService.listByIdentifier(Taxon.class, "ext-1234", null, MatchMode.EXACT, false, null, null);//.listByIdentifier(Taxon.class, "ext-1234", null, rootNode, MatchMode.EXACT, false, null, null);
        Assert.assertEquals("Result size for 'ext' should be 1", 1, taxonPager.size());
     //   Assert.assertEquals("Result size for 'ext' should be 1", 1, taxonPager.getRecords().size());
        taxonPager = taxonService.listByIdentifier(Taxon.class, "ext-1234", null, MatchMode.EXACT, true, null, null);//.listByIdentifier(Taxon.class, "ext-1234", null, rootNode, MatchMode.EXACT, false, null, null);
        Assert.assertEquals("Result size for 'ext' should be 1", 1, taxonPager.size());
        IdentifiedEntityDTO<Taxon> identifierDTO = taxonPager.get(0);
        Assert.assertTrue(identifierDTO.getCdmEntity() instanceof UuidAndTitleCache);

        List<IdentifiedEntityDTO<Taxon>> taxPager = taxonService.listByIdentifier(Taxon.class, "ext-cache1", null, MatchMode.EXACT, false, null, null);
        Assert.assertEquals("Result size for 'ext' should be 1", 1, taxPager.size());

        List<IdentifiedEntityDTO<TaxonBase>> tbPager = taxonService.listByIdentifier(TaxonBase.class, "ext-1234", null, MatchMode.EXACT, false, null, null);
        Assert.assertEquals("Result size for 'ext' should be 1",1, tbPager.size());


        //Synonym
        List<IdentifiedEntityDTO<Synonym>> synPager = taxonService.listByIdentifier(Synonym.class, "ext-syn", null,  MatchMode.BEGINNING, false, null, null);
        Assert.assertEquals("There should be 2 synonyms with identifier ext-syn", 2, synPager.size());

        List<IdentifiedEntityDTO<Reference>> referenceList = referenceService.listByIdentifier(Reference.class, "123", null, MatchMode.EXACT, false, null, null);//.listByIdentifier(Taxon.class, "ext-1234", null, rootNode, MatchMode.EXACT, false, null, null);
        Assert.assertEquals("Result size for 'ext' should be 1", 1, taxonPager.size());

        referenceList = referenceService.listByIdentifier(Reference.class, "123", null, MatchMode.EXACT, true, null, null);//.listByIdentifier(Taxon.class, "ext-1234", null, rootNode, MatchMode.EXACT, false, null, null);
        Assert.assertEquals("Result size for 'ext' should be 1", 1, referenceList.size());
        IdentifiedEntityDTO<Reference> identifierDTOReference = referenceList.get(0);
        Assert.assertTrue(identifierDTO.getCdmEntity() instanceof UuidAndTitleCache);
	}

	@Test
    @DataSet(value="IdentifiableServiceBaseTest.testFindByIdentifierOrMarker.xml")
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
        Assert.assertNotNull(markerType2);

        TaxonTitleType titleType = TaxonTitleType.TAXON;

        MarkerType markerType = markerType1;
        Pager<MarkedEntityDTO<Taxon>> taxonPager = taxonService.findByMarker(Taxon.class, markerType, markerValue,
                rootNode, true, titleType, null, null, null);
        Assert.assertEquals("Result size for 'marker1=true' should be 1", Long.valueOf(1), taxonPager.getCount());
        Assert.assertEquals("Result size for 'marker1=true' should be 1", 1, taxonPager.getRecords().size());
        MarkedEntityDTO<Taxon> dto = taxonPager.getRecords().get(0);
        MarkedEntityDTO<Taxon>.Marker marker = dto.getMarker();
        Assert.assertTrue("Flag must be true", marker.getFlag());
        Assert.assertEquals("Flag must be true", uuidMarkerTypeCompleted, marker.getTypeUuid());
        Assert.assertNotNull("the CDM entity in the dto must not be empty if includeEntity=true", dto.getCdmEntity().getEntity());
        Assert.assertEquals(5000, dto.getCdmEntity().getEntity().getId());


        markerValue = false;
        taxonPager = taxonService.findByMarker(Taxon.class, markerType, markerValue, rootNode, false, titleType, null, null, null);
        Assert.assertEquals("Result size for 'marker1=false' should be 0", Long.valueOf(0), taxonPager.getCount());

        markerValue = true;
        markerType = noMarkerType;
        taxonPager = taxonService.findByMarker(Taxon.class, markerType, markerValue, rootNode, false, titleType, null, null, null);
        Assert.assertEquals("Result size for not existing marker type should be 0", Long.valueOf(0), taxonPager.getCount());

        markerType = markerType2;
        taxonPager = taxonService.findByMarker(Taxon.class, markerType, markerValue, rootNode, false, titleType, null, null, null);
        Assert.assertEquals("Result size for markerType2 should be 0", Long.valueOf(0), taxonPager.getCount());

        rootNode = null;
        markerType = markerType1;
        taxonPager = taxonService.findByMarker(Taxon.class, markerType, markerValue, rootNode, false, titleType, null, null, null);
        Assert.assertEquals("Result size for no subtree should be 2", Long.valueOf(2), taxonPager.getCount());

        Pager<MarkedEntityDTO<TaxonBase>> taxonBasePager = taxonService.findByMarker(TaxonBase.class, markerType, markerValue, rootNode, false, titleType, null, null, null);
        Assert.assertEquals("Result size for taxa and synonyms without subtree filter with flag = true should be 3", Long.valueOf(3), taxonBasePager.getCount());

        markerValue = null;
        taxonBasePager = taxonService.findByMarker(TaxonBase.class, markerType, markerValue, rootNode, false, titleType, null, null, null);
        Assert.assertEquals("Result size for taxa and synonyms without subtree filter with any flag value should be 4", Long.valueOf(4), taxonBasePager.getCount());

        markerValue = true;
        Pager<MarkedEntityDTO<TaxonName>> namePager = nameService.findByMarker(TaxonName.class, markerType, markerValue, false, null, null, null);
        Assert.assertEquals("Result size for names with flag = true should be 1", Long.valueOf(1), namePager.getCount());

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
        Taxon tb = Taxon.NewInstance(name, null);
        tb.addIdentifier("ext-1234", identifierType1);
        name.addIdentifier("ext-name12", identifierType2);
        taxonService.saveOrUpdate(tb);

        Taxon tb2 = Taxon.NewInstance(null, null);
        tb2.setTitleCache("Cached taxon", true);
        tb2.addIdentifier("ext-cache1", identifierType2);
        taxonService.saveOrUpdate(tb2);

        Classification classification = Classification.NewInstance("My classification");
        classification.addChildTaxon(tb, null, null);
        classificationService.saveOrUpdate(classification);

        tb2.addSynonymName(null, SynonymType.HOMOTYPIC_SYNONYM_OF);

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
