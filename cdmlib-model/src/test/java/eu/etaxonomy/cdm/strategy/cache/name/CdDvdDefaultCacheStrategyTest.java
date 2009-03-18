
package eu.etaxonomy.cdm.strategy.cache.name;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.hibernate.search.store.DirectoryProviderHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.reference.CdDvd;
import eu.etaxonomy.cdm.strategy.cache.reference.CdDvdDefaultCacheStrategy;

public class CdDvdDefaultCacheStrategyTest {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CdDvdDefaultCacheStrategyTest.class);

	CdDvd cdDvd;
	String title;
	String publisher;
	TimePeriod publicationDate;
	CdDvdDefaultCacheStrategy<CdDvd> instance;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		this.cdDvd = CdDvd.NewInstance();
		title = "A nice CD title";
		cdDvd.setTitle(title);
		publisher = "An ugly publisher";
		cdDvd.setPublisher(publisher);
		publicationDate = TimePeriod.NewInstance(1999, 2001);
		cdDvd.setDatePublished(publicationDate);
		this.instance = CdDvdDefaultCacheStrategy.NewInstance();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testNewInstance() {
		assertNotNull(instance);
	}
	
	@Test
	public void testGetNomRefTitleWithoutYearAndAuthor() {
		String result = instance.getNomRefTitleWithoutYearAndAuthor(cdDvd);
		assertEquals(title, result);
	}

	//TODO missing publicationPlace and publisher has to be discussed
	@Test
	public void testGetTitleCache() {
		String result = instance.getTitleCache(cdDvd);
		assertEquals(title + ". 1999-2001", result);
	}
	

}
