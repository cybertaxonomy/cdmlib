/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionElementDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmTransactionalSpringIntegrationTest;

@CdmDataSet
@Disabled //TODO indexing does not work at all, even before the unitils upgrade
public class FreeTextSearchIntegrationTest extends CdmTransactionalSpringIntegrationTest {

	@SuppressWarnings("unused")
	private static final Logger loger = LogManager.getLogger();

	@Autowired
	private ITaxonDao taxonDao;

	@Autowired
	private IDescriptionElementDao descriptionElementDao;

	@Test
	public void testSearchTextData() {
		List<OrderHint> orderHints = new ArrayList<>();
		orderHints.add(new OrderHint("inDescription.titleCache", SortOrder.ASCENDING));

		List<String> propertyPaths = new ArrayList<>();
		propertyPaths.add("inDescription");
		propertyPaths.add("inDescription.taxon");
		List<DescriptionElementBase> results = descriptionElementDao.search(TextData.class,"Lorem",null,null,orderHints,propertyPaths);

		assertNotNull(results, "searchTextData should return a List");
		assertEquals(4, results.size(), "there should be 4 TextData entities in the list");

		assertTrue(Hibernate.isInitialized(results.get(0).getFeature()), "DescriptionElementBase.feature should be initialized");
		assertTrue(Hibernate.isInitialized(results.get(0).getInDescription()), "DescriptionElementBase.inDescription should be initialized");
		assertTrue(results.get(0).getInDescription() instanceof TaxonDescription, "inDescription should be an instance of TaxonDescription");
		TaxonDescription taxonDescription = (TaxonDescription)results.get(0).getInDescription();
		assertTrue(Hibernate.isInitialized(taxonDescription.getTaxon()), "TaxonDescription.taxon should be initialized");
		assertEquals("Aglaodorum Schott sec. cate-araceae.org", taxonDescription.getTaxon().getTitleCache(), "The results should be sorted alphabetically");
	}

    @Test
    public void testCountTextData() {
    	long matches = descriptionElementDao.count(TextData.class,"Lorem");
    	assertEquals(4, matches, "countTextData should return 4");
    }

    @Test
    public void testSearchWord() {
    	List<OrderHint> orderHints = new ArrayList<>();
    	orderHints.add(new OrderHint("name.titleCache",SortOrder.ASCENDING));
    	List<String> propertyPaths = new ArrayList<>();
    	propertyPaths.add("name");

    	List<TaxonBase> results = taxonDao.search(null,"Arum", null, null, orderHints, propertyPaths);
		assertEquals(46, results.size(), "searchTaxa should return 463 results");
		assertTrue(Hibernate.isInitialized(results.get(0).getName()), "TaxonBase.name should be initialized");
    }

    @Test
    public void testSearchCount() {
		long numberOfResults = taxonDao.count(null,"Arum");
		assertEquals(46, numberOfResults, "countTaxa should return 46");
    }

    @Test
    public void testSearchPaged() {
    	List<OrderHint> orderHints = new ArrayList<>();
    	orderHints.add(new OrderHint("name.titleCache",SortOrder.ASCENDING));
    	List<String> propertyPaths = new ArrayList<>();
    	propertyPaths.add("name");
		List<TaxonBase> page1 = taxonDao.search(null,"Arum", 30, 0,orderHints,propertyPaths);
		List<TaxonBase> page2 = taxonDao.search(null,"Arum", 30, 1,orderHints,propertyPaths);

		assertEquals(30, page1.size(), "page 1 should contain 30 taxa");
		assertEquals("Arum L.", page1.get(0).getName().getTitleCache(), "page 1 should be sorted alphabetically");
		assertEquals("Arum lucanum Cavara & Grande", page1.get(29).getName().getTitleCache(), "page 1 should be sorted alphabetically");
		assertEquals(16, page2.size(), "page 2 should contain 16 taxa");
		assertEquals("Arum maculatum L.", page2.get(0).getName().getTitleCache(), "page 2 should be sorted alphabetically");
		assertEquals("Arum x sooi Terpó", page2.get(15).getName().getTitleCache(), "page 2 should be sorted alphabetically");
    }

    @Test
    public void testSearchPhrase() {
    	List<OrderHint> orderHints = new ArrayList<>();
    	orderHints.add(new OrderHint("name.titleCache",SortOrder.ASCENDING));
    	List<String> propertyPaths = new ArrayList<>();
    	propertyPaths.add("name");

		List<TaxonBase> results = taxonDao.search(null,"\"Arum italicum\"", null, null,orderHints,propertyPaths);
		assertEquals(5, results.size(), "searchTaxa should return 5 taxa");
    }

    @Test
    public void testSearchWildcard()  {
    	List<OrderHint> orderHints = new ArrayList<>();
    	orderHints.add(new OrderHint("name.titleCache", SortOrder.ASCENDING));
    	List<String> propertyPaths = new ArrayList<>();
    	propertyPaths.add("name");

		List<TaxonBase> results = taxonDao.search(null, "Aroph*",  null, null, orderHints, propertyPaths);
		assertEquals(7, results.size(), "searchTaxa should return 6 taxa");
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}