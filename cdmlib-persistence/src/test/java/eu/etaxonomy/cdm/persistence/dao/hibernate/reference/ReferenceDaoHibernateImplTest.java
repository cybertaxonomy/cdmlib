/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.reference;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.reference.IJournal;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

@DataSet
public class ReferenceDaoHibernateImplTest extends CdmIntegrationTest {

	@SpringBeanByType
	IReferenceDao referenceDao;

	private UUID firstBookUuid;
	private UUID firstJournalUuid;
	private UUID proceedingsUuid;
	private UUID bookSectionUuid;
	private UUID nomenclaturalReferenceBookUuid;


	@Before
	public void setUp() {
		firstBookUuid = UUID.fromString("596b1325-be50-4b0a-9aa2-3ecd610215f2");
	    firstJournalUuid = UUID.fromString("ad4322b7-4b05-48af-be70-f113e46c545e");
	    proceedingsUuid = UUID.fromString("596b1327-be50-4b0a-9aa2-3ecd610215f2");
	    bookSectionUuid = UUID.fromString("596b1327-be51-4b0a-9aa2-3ecd610215f1");
	    nomenclaturalReferenceBookUuid = UUID.fromString("596b1325-be50-4b0a-9aa2-3ecd610215f3");
	}

	@Test
	public void testDummy() {
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
		assertEquals("expecting one subordinate reference", book_subordinateReferences.size(), 1);
		Reference sub_1 = book_subordinateReferences.get(0);
		assertEquals("expecting BookSection as first subordinateReferences", "Better Testing made easy", sub_1.getTitleCache());
		assertEquals("first subordinateReferences matches uuid", bookSectionUuid, sub_1.getUuid());

		// 2.)
		List<Reference> proceedings_subordinateReferences = referenceDao.getSubordinateReferences(proceedings);
		assertEquals("expecting one subordinate reference",2 ,proceedings_subordinateReferences.size());
		sub_1 = proceedings_subordinateReferences.get(0);
		Reference sub_2 = proceedings_subordinateReferences.get(1);
		assertEquals("expecting BookSection as first subordinateReferences", "Proceedings of Testing Vol. 1", sub_1.getTitleCache());
		assertEquals("expecting BookSection as first subordinateReferences", "Better Testing made easy", sub_2.getTitleCache());
		assertEquals("first subordinateReferences matches uuid", firstBookUuid, sub_1.getUuid());
		assertEquals("second subordinateReferences matches uuid", bookSectionUuid, sub_2.getUuid());
	}

	@Test
	public void testListCoveredTaxa() {

		Reference book = referenceDao.findByUuid(firstBookUuid);
		List<OrderHint> orderHints = Arrays.asList(new OrderHint[]{new OrderHint("titleCache", OrderHint.SortOrder.DESCENDING)});

		List<TaxonBase> coveredTaxa = referenceDao.listCoveredTaxa(book, false, orderHints, null);
		assertEquals("expecting one Taxa covered by this book", 1, coveredTaxa.size());
		assertEquals("covered taxon is 'Lactuca perennis'", "Lactuca perennis", coveredTaxa.get(0).getName().getTitleCache() );

		coveredTaxa = referenceDao.listCoveredTaxa(book, true, orderHints, null);
		assertEquals("expecting 2 Taxa covered by this book", 2, coveredTaxa.size());
		Set<String> titles = makeTitleCacheSet(coveredTaxa);
		Assert.assertTrue("covered taxa must contain 'Lactuca perennis'", titles.contains("Lactuca perennis"));
		Assert.assertTrue("covered taxon must contain 'Lactuca virosa'", titles.contains("Lactuca virosa"));
		assertEquals("2nd covered taxon is 'Lactuca virosa'", "Lactuca virosa", coveredTaxa.get(1).getName().getTitleCache() );

		Reference bookSection = referenceDao.findByUuid(bookSectionUuid);
		coveredTaxa = referenceDao.listCoveredTaxa(bookSection, false, orderHints, null);
		assertEquals("expecting two Taxa covered by this bookSection", 2, coveredTaxa.size());
		titles = makeTitleCacheSet(coveredTaxa);
		Assert.assertTrue("covered taxa must contain 'Lactuca perennis'", titles.contains("Lactuca perennis"));
		Assert.assertTrue("covered taxon must contain 'Lactuca virosa'", titles.contains("Lactuca virosa"));
		assertEquals("1st covered taxon is 'Lactuca perennis'", "Lactuca perennis", coveredTaxa.get(0).getName().getTitleCache() );
		assertEquals("2nd covered taxon is 'Lactuca virosa'", "Lactuca virosa", coveredTaxa.get(1).getName().getTitleCache() );

		// by nomenclaturalReference
		Reference nomRef = referenceDao.findByUuid(nomenclaturalReferenceBookUuid);
		coveredTaxa = referenceDao.listCoveredTaxa(nomRef, false, orderHints, null);
		assertEquals("expecting two Taxa covered nomenclaturalReference", 2, coveredTaxa.size());
		titles = makeTitleCacheSet(coveredTaxa);
		Assert.assertTrue("covered taxa must contain 'Lactuca perennis'", titles.contains("Lactuca perennis"));
		Assert.assertTrue("covered taxon must contain 'Lactuca virosa'", titles.contains("Lactuca virosa"));
		assertEquals("covered taxon is 'Lactuca perennis'", "Lactuca perennis", coveredTaxa.get(0).getName().getTitleCache() );
		assertEquals("2nd covered taxon is 'Lactuca virosa'", "Lactuca virosa", coveredTaxa.get(1).getName().getTitleCache() );
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


    @Override
    public void createTestDataSet() throws FileNotFoundException {}


}
