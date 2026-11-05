/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.reference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.model.reference.IJournal;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmSpringIntegrationTest;

@CdmDataSet
public class ReferenceDaoHibernateImplTest extends CdmSpringIntegrationTest {

	@Autowired
	private IReferenceDao referenceDao;

	private UUID firstBookUuid;
	private UUID firstJournalUuid;
	private UUID proceedingsUuid;
	private UUID articleUuid;
	private UUID nomenclaturalReferenceBookUuid;

	@BeforeEach
	public void setUp() {
	    //TODO: is proceedings in reality (!! dirty data !!) AND same as proceedingsUuid below
		firstBookUuid = UUID.fromString("596b1325-be50-4b0a-9aa2-3ecd610215f2");
	    firstJournalUuid = UUID.fromString("ad4322b7-4b05-48af-be70-f113e46c545e");
	    proceedingsUuid = UUID.fromString("596b1327-be50-4b0a-9aa2-3ecd610215f2");
	    //note: is an article in book in reality (!!dirty data!!)
	    articleUuid = UUID.fromString("596b1327-be51-4b0a-9aa2-3ecd610215f1");
	    nomenclaturalReferenceBookUuid = UUID.fromString("596b1325-be50-4b0a-9aa2-3ecd610215f3");
	}

	@Test
	public void testGetPublishers() {
		IJournal firstJournal = referenceDao.findByUuid(firstJournalUuid);
		assert firstJournal!= null : "journal must exist";
	}

	@Test
	public void testGetSubordinateReferences() {

	    Reference book = referenceDao.findByUuid(firstBookUuid);
	    Reference proceedings = referenceDao.findByUuid(proceedingsUuid);

	    // 1.)
		List<Reference> book_subordinateReferences = referenceDao.getSubordinateReferences(book);
		assertEquals(book_subordinateReferences.size(), 1, "expecting one subordinate reference");
		Reference sub_1 = book_subordinateReferences.get(0);
		assertEquals("Better Testing made easy", sub_1.getTitleCache(), "expecting BookSection as first subordinateReferences");
		assertEquals(articleUuid, sub_1.getUuid(), "first subordinateReferences matches uuid");

		// 2.)
		List<Reference> proceedings_subordinateReferences = referenceDao.getSubordinateReferences(proceedings);
		assertEquals(2, proceedings_subordinateReferences.size(), "expecting one subordinate reference");
		sub_1 = proceedings_subordinateReferences.get(0);
		Reference sub_2 = proceedings_subordinateReferences.get(1);
		assertEquals("Proceedings of Testing Vol. 1", sub_1.getTitleCache(), "expecting BookSection as first subordinateReferences");
		assertEquals("Better Testing made easy", sub_2.getTitleCache(), "expecting BookSection as first subordinateReferences");
		assertEquals(firstBookUuid, sub_1.getUuid(), "first subordinateReferences matches uuid");
		assertEquals(articleUuid, sub_2.getUuid(), "second subordinateReferences matches uuid");
	}

	@Test
	public void testListCoveredTaxa() {

		Reference book = referenceDao.findByUuid(firstBookUuid);
		List<OrderHint> orderHints = Arrays.asList(new OrderHint[]{new OrderHint("titleCache", OrderHint.SortOrder.DESCENDING)});

		List<TaxonBase> coveredTaxa = referenceDao.listCoveredTaxa(book, false, orderHints, null);
		assertEquals(1, coveredTaxa.size(), "expecting one Taxa covered by this book");
		assertEquals("Lactuca perennis", coveredTaxa.get(0).getName().getTitleCache(), "covered taxon is 'Lactuca perennis'");

		coveredTaxa = referenceDao.listCoveredTaxa(book, true, orderHints, null);
		assertEquals(2, coveredTaxa.size(), "expecting 2 Taxa covered by this book");
		Set<String> titles = makeTitleCacheSet(coveredTaxa);
		assertTrue(titles.contains("Lactuca perennis"), "covered taxa must contain 'Lactuca perennis'");
		assertTrue(titles.contains("Lactuca virosa"), "covered taxon must contain 'Lactuca virosa'");
		assertEquals("Lactuca virosa", coveredTaxa.get(1).getName().getTitleCache(), "2nd covered taxon is 'Lactuca virosa'");

		Reference bookSection = referenceDao.findByUuid(articleUuid);
		coveredTaxa = referenceDao.listCoveredTaxa(bookSection, false, orderHints, null);
		assertEquals(2, coveredTaxa.size(), "expecting two Taxa covered by this bookSection");
		titles = makeTitleCacheSet(coveredTaxa);
		assertTrue(titles.contains("Lactuca perennis"), "covered taxa must contain 'Lactuca perennis'");
		assertTrue(titles.contains("Lactuca virosa"), "covered taxon must contain 'Lactuca virosa'");
		assertEquals("Lactuca perennis", coveredTaxa.get(0).getName().getTitleCache(), "1st covered taxon is 'Lactuca perennis'");
		assertEquals("Lactuca virosa", coveredTaxa.get(1).getName().getTitleCache(), "2nd covered taxon is 'Lactuca virosa'");

		// by nomenclaturalReference
		Reference nomRef = referenceDao.findByUuid(nomenclaturalReferenceBookUuid);
		coveredTaxa = referenceDao.listCoveredTaxa(nomRef, false, orderHints, null);
		assertEquals(2, coveredTaxa.size(), "expecting two Taxa covered nomenclaturalReference");
		titles = makeTitleCacheSet(coveredTaxa);
		assertTrue(titles.contains("Lactuca perennis"), "covered taxa must contain 'Lactuca perennis'");
		assertTrue(titles.contains("Lactuca virosa"), "covered taxon must contain 'Lactuca virosa'");
		assertEquals("Lactuca perennis", coveredTaxa.get(0).getName().getTitleCache(), "covered taxon is 'Lactuca perennis'");
		assertEquals("Lactuca virosa", coveredTaxa.get(1).getName().getTitleCache(), "2nd covered taxon is 'Lactuca virosa'");
	}

	private Set<String> makeTitleCacheSet(List<TaxonBase> coveredTaxa) {
		Set<String> titles = new HashSet<>();
		titles.add(coveredTaxa.get(0).getName().getTitleCache());
		titles.add(coveredTaxa.get(1).getName().getTitleCache());
		return titles;
	}

	@Test
	public void testUUIDAndTitleCacheWithReferenceType(){
	    List<UuidAndTitleCache<Reference>> referenceUuidAndTitleCacheList = referenceDao.getUuidAndTitleCache(100, "Better Testing*", ReferenceType.Article);
	    assertEquals(1, referenceUuidAndTitleCacheList.size());
	    referenceUuidAndTitleCacheList = referenceDao.getUuidAndTitleCache(100, "Better Testing*", ReferenceType.Book);
	    assertEquals(0, referenceUuidAndTitleCacheList.size());
	}

    @Test
    public void testGetUuidAndTitleCacheForUuidsAndType(){
        Set<UUID> referenceUuids = new HashSet<>(Arrays.asList(new UUID[]{proceedingsUuid, articleUuid}));

        List<UuidAndTitleCache<Reference>> referenceUuidAndTitleCacheList = referenceDao.getUuidAndTitleCache(referenceUuids, null);
        assertEquals(2, referenceUuidAndTitleCacheList.size(), "2 references should be found for 2 UUIDs");

        referenceUuidAndTitleCacheList = referenceDao.getUuidAndTitleCache(referenceUuids, ReferenceType.Article);
        assertEquals(1, referenceUuidAndTitleCacheList.size());

        referenceUuidAndTitleCacheList = referenceDao.getUuidAndTitleCache(referenceUuids, ReferenceType.Book);
        assertEquals(0, referenceUuidAndTitleCacheList.size());
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}