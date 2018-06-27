/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionElementDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

@DataSet
@Ignore //TODO indexing does not work at all, even before the unitils upgrade
public class FreeTextSearchIntegration extends CdmTransactionalIntegrationTest {

	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(FreeTextSearchIntegration.class);

	@SpringBeanByType
	ITaxonDao taxonDao;

	@SpringBeanByType
	IDescriptionElementDao descriptionElementDao;

//	@SpringBeanByType
//	TaxonAlternativeSpellingSuggestionParser alternativeSpellingSuggestionParser;
//
//	@Test
//	public void test() {
//		taxonDao.rebuildIndex();
//		taxonDao.optimizeIndex();
//
//		descriptionElementDao.rebuildIndex();
//		descriptionElementDao.optimizeIndex();
//		setComplete();
//		endTransaction();
//		taxonDao.countTaxa("Arum",null); // For some reason this flushes the indexes and allows the next method to create the spellings index
//	}
//
//	@Test
//	public void test1() {
//		alternativeSpellingSuggestionParser.refresh();
//	}

	@Test
	public void testSearchTextData() {
		List<OrderHint> orderHints = new ArrayList<OrderHint>();
		orderHints.add(new OrderHint("inDescription.titleCache",SortOrder.ASCENDING));

		List<String> propertyPaths = new ArrayList<String>();
		propertyPaths.add("inDescription");
		propertyPaths.add("inDescription.taxon");
		List<DescriptionElementBase> results = descriptionElementDao.search(TextData.class,"Lorem",null,null,orderHints,propertyPaths);

		assertNotNull("searchTextData should return a List",results);
		assertEquals("there should be 4 TextData entities in the list",4,results.size());

		assertTrue("DescriptionElementBase.feature should be initialized",Hibernate.isInitialized(results.get(0).getFeature()));
		assertTrue("DescriptionElementBase.inDescription should be initialized",Hibernate.isInitialized(results.get(0).getInDescription()));
		assertTrue("inDescription should be an instance of TaxonDescription",results.get(0).getInDescription() instanceof TaxonDescription);
		TaxonDescription taxonDescription = (TaxonDescription)results.get(0).getInDescription();
		assertTrue("TaxonDescription.taxon should be initialized",Hibernate.isInitialized(taxonDescription.getTaxon()));
		assertEquals("The results should be sorted alphabetically","Aglaodorum Schott sec. cate-araceae.org",taxonDescription.getTaxon().getTitleCache());
	}

    @Test
    public void testCountTextData() {
    	long matches = descriptionElementDao.count(TextData.class,"Lorem");
    	assertEquals("countTextData should return 4",4,matches);
    }

    @Test
    public void testSearchWord() {
    	List<OrderHint> orderHints = new ArrayList<OrderHint>();
    	orderHints.add(new OrderHint("name.titleCache",SortOrder.ASCENDING));
    	List<String> propertyPaths = new ArrayList<String>();
    	propertyPaths.add("name");

    	List<TaxonBase> results = taxonDao.search(null,"Arum", null, null, orderHints, propertyPaths);
		assertEquals("searchTaxa should return 463 results",46,results.size());
		assertTrue("TaxonBase.name should be initialized",Hibernate.isInitialized(results.get(0).getName()));
    }

    @Test
    public void testSearchCount() {
		long numberOfResults = taxonDao.count(null,"Arum");
		assertEquals("countTaxa should return 46",46,numberOfResults);

    }

    @Test
    public void testSearchPaged() {
    	List<OrderHint> orderHints = new ArrayList<OrderHint>();
    	orderHints.add(new OrderHint("name.titleCache",SortOrder.ASCENDING));
    	List<String> propertyPaths = new ArrayList<String>();
    	propertyPaths.add("name");
		List<TaxonBase> page1 = taxonDao.search(null,"Arum", 30, 0,orderHints,propertyPaths);
		List<TaxonBase> page2 = taxonDao.search(null,"Arum", 30, 1,orderHints,propertyPaths);

		assertEquals("page 1 should contain 30 taxa",30,page1.size());
		assertEquals("page 1 should be sorted alphabetically","Arum L.",page1.get(0).getName().getTitleCache());
		assertEquals("page 1 should be sorted alphabetically","Arum lucanum Cavara & Grande",page1.get(29).getName().getTitleCache());
		assertEquals("page 2 should contain 16 taxa",16,page2.size());
		assertEquals("page 2 should be sorted alphabetically","Arum maculatum L.",page2.get(0).getName().getTitleCache());
		assertEquals("page 2 should be sorted alphabetically","Arum x sooi Terpó",page2.get(15).getName().getTitleCache());
    }

    @Test
    public void testSearchPhrase() {
    	List<OrderHint> orderHints = new ArrayList<OrderHint>();
    	orderHints.add(new OrderHint("name.titleCache",SortOrder.ASCENDING));
    	List<String> propertyPaths = new ArrayList<String>();
    	propertyPaths.add("name");

		List<TaxonBase> results = taxonDao.search(null,"\"Arum italicum\"", null, null,orderHints,propertyPaths);
		assertEquals("searchTaxa should return 5 taxa",5,results.size());
    }

    @Test
    public void testSearchWildcard()  {
    	List<OrderHint> orderHints = new ArrayList<OrderHint>();
    	orderHints.add(new OrderHint("name.titleCache",SortOrder.ASCENDING));
    	List<String> propertyPaths = new ArrayList<String>();
    	propertyPaths.add("name");

		List<TaxonBase> results = taxonDao.search(null,"Aroph*",  null, null,orderHints,propertyPaths);
		assertEquals("searchTaxa should return 6 taxa",7,results.size());
    }

    @Test
    @Ignore //we currently don't use suggest anymore
    public void testSuggestSingleTerm() {
    	String suggestion = taxonDao.suggestQuery("Aram");
    	assertNotNull("suggestQuery should return a String",suggestion);
    	assertEquals("The spelling suggestion for \"Aram\" should be \"arum\"","arum",suggestion);
    }

    @Test
    @Ignore //we currently don't use suggest anymore
    public void testSuggestSingleTermInCompositeQuery() {
    	String suggestion = taxonDao.suggestQuery("Aram italicum");
    	assertNotNull("suggestQuery should return a String",suggestion);
    	assertEquals("The spelling suggestion for \"Aram italicum\" should be \"arum italicum\"","arum italicum",suggestion);
    }

    @Test
    @Ignore //we currently don't use suggest anymore
    public void testSuggestMultipleTermsInCompositeQuery() {
    	String suggestion = taxonDao.suggestQuery("Aram italocum");
    	assertNotNull("suggestQuery should return a String",suggestion);
    	assertEquals("The spelling suggestion for \"Aram italocum\" should be \"arum italicum\"","arum italicum",suggestion);
    }

    @Test
    @Ignore //we currently don't use suggest anymore
    public void testSuggestMultipleTermsInCompositeQueryWithAnd() {
    	String suggestion = taxonDao.suggestQuery("Aram AND italocum");
    	assertNotNull("suggestQuery should return a String",suggestion);
    	assertEquals("The spelling suggestion for \"Aram AND italocum\" should be \"+arum +italicum\"","+arum +italicum",suggestion);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub

    }
}
