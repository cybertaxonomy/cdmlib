package eu.etaxonomy.cdm.persistence.dao.hibernate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionElementDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

@DataSet
public class FreeTextSearchIntegration extends CdmIntegrationTest {

	private static Log log = LogFactory.getLog(FreeTextSearchIntegration.class);

	@SpringBeanByType
	ITaxonDao taxonDao;
	
	@SpringBeanByType
	IDescriptionElementDao descriptionElementDao;
	
//	@SpringBeanByType
//	TaxonAlternativeSpellingSuggestionParser alternativeSpellingSuggestionParser;

//	@Test
//	public void test() {
//		taxonDao.rebuildIndex();
//		taxonDao.optimizeIndex();
//		
//		descriptionElementDao.rebuildIndex();
//		descriptionElementDao.optimizeIndex();
//	}
	
//	@Test
//	public void test1() {
//		alternativeSpellingSuggestionParser.refresh();
//	}
	
	@Test
	public void testSearchTextData() {
		List<TextData> results = descriptionElementDao.searchTextData("Lorem",null,null);
		
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
    	int matches = descriptionElementDao.countTextData("Lorem");
    	assertEquals("countTextData should return 4",4,matches);
    }
	
    @Test
    public void testSearchWord() {
    	List<TaxonBase> results = taxonDao.searchTaxa("Arum",null, null, null);
		assertEquals("searchTaxa should return 43 results",43,results.size());
		assertTrue("TaxonBase.name should be initialized",Hibernate.isInitialized(results.get(0).getName()));
    }
    
    @Test
    public void testSearchCount() {
		int numberOfResults = taxonDao.countTaxa("Arum",null);
		assertEquals("countTaxa should return 43",43,numberOfResults);
		
    }
    
    @Test
    public void testSearchPaged() {
		List<TaxonBase> page1 = taxonDao.searchTaxa("Arum",null, 30, 0);
		List<TaxonBase> page2 = taxonDao.searchTaxa("Arum",null, 30, 1);
		
		assertEquals("page 1 should contain 30 taxa",30,page1.size());
		assertEquals("page 1 should be sorted alphabetically","Arum L.",page1.get(0).getName().getTitleCache());
		assertEquals("page 1 should be sorted alphabetically","Arum lucanum Cavara & Grande",page1.get(29).getName().getTitleCache());
		assertEquals("page 2 should contain 13 taxa",13,page2.size());
		assertEquals("page 2 should be sorted alphabetically","Arum maculatum L.",page2.get(0).getName().getTitleCache());
		assertEquals("page 2 should be sorted alphabetically","Arum x sooi Terpó",page2.get(12).getName().getTitleCache());
    }
    
    @Test
    public void testSearchPhrase() {
		List<TaxonBase> results = taxonDao.searchTaxa("\"Arum italicum\"",null, null, null);
		assertEquals("searchTaxa should return 5 taxa",5,results.size());
    }
    
    @Test
    public void testSearchWildcard()  {
		List<TaxonBase> results = taxonDao.searchTaxa("Aroph*", null, null, null);
		assertEquals("searchTaxa should return 6 taxa",7,results.size());
    }
    
    @Test 
    public void testSuggestSingleTerm() {
    	String suggestion = taxonDao.suggestQuery("Aram");
    	assertNotNull("suggestQuery should return a String",suggestion);
    	assertEquals("The spelling suggestion for \"Aram\" should be \"arum\"","arum",suggestion);
    }
    
    @Test 
    public void testSuggestSingleTermInCompositeQuery() {
    	String suggestion = taxonDao.suggestQuery("Aram italicum");
    	assertNotNull("suggestQuery should return a String",suggestion);
    	assertEquals("The spelling suggestion for \"Aram italicum\" should be \"arum italicum\"","arum italicum",suggestion);
    }
    
    @Test 
    public void testSuggestMultipleTermsInCompositeQuery() {
    	String suggestion = taxonDao.suggestQuery("Aram italocum");
    	assertNotNull("suggestQuery should return a String",suggestion);
    	assertEquals("The spelling suggestion for \"Aram italocum\" should be \"arum italicum\"","arum italicum",suggestion);
    }
    
    @Test 
    public void testSuggestMultipleTermsInCompositeQueryWithAnd() {
    	String suggestion = taxonDao.suggestQuery("Aram AND italocum");
    	assertNotNull("suggestQuery should return a String",suggestion);
    	assertEquals("The spelling suggestion for \"Aram AND italocum\" should be \"+arum +italicum\"","+arum +italicum",suggestion);
    }
}
