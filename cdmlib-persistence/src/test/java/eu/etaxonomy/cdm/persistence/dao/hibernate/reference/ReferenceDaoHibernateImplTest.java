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
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

@DataSet
public class ReferenceDaoHibernateImplTest extends CdmIntegrationTest {

	@SpringBeanByType
	IReferenceDao referenceDao;

	private UUID firstBookUuid;
	private UUID firstJournalUuid;
	private UUID genericUuid;
	private UUID proceedingsUuid;
	private UUID bookSectionUuid;
	private UUID nomenclaturalReferenceBookUuid;

	private final String firstPublisherName ="First Publisher";
	private final String secondPublisherName ="Second Publisher";
	private final String thirdPublisherName ="Third Publisher";
	private final String fourthPublisherName ="Fourth Publisher";


	@Before
	public void setUp() {
		firstBookUuid = UUID.fromString("596b1325-be50-4b0a-9aa2-3ecd610215f2");
	    firstJournalUuid = UUID.fromString("ad4322b7-4b05-48af-be70-f113e46c545e");
	    genericUuid = UUID.fromString("bd4822b7-4b05-4eaf-be70-f113446c585e");
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

//		List<Publisher> publishers = firstJournal.getPublishers();
//
//		assertNotNull("getPublishers should return a list", publishers);
//		assertFalse("the list should not be empty", publishers.isEmpty());
//		assertEquals("getPublishers should return 4 Publisher instances",4,publishers.size());
//		assertEquals("first publisher should come first",firstPublisherName,publishers.get(0).getPublisherName());
//		assertEquals("second publisher should come second",secondPublisherName,publishers.get(1).getPublisherName());
//		assertEquals("third publisher should come third",thirdPublisherName,publishers.get(2).getPublisherName());
//		assertEquals("fourth publisher should come fourth",fourthPublisherName,publishers.get(3).getPublisherName());


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
		Set<String> titles = new HashSet<String>();
		titles.add(coveredTaxa.get(0).getName().getTitleCache());
		titles.add(coveredTaxa.get(1).getName().getTitleCache());
		return titles;
	}

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub

    }


}
