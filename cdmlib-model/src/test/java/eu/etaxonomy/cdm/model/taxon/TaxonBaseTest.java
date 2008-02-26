/**
 * 
 */
package eu.etaxonomy.cdm.model.taxon;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;

/**
 * @author a.mueller
 *
 */
public class TaxonBaseTest extends EntityTestBase {
	private static final Logger logger = Logger.getLogger(TaxonBaseTest.class);
	
	private ReferenceBase sec;
	private ZoologicalName name1;
	private BotanicalName name2;
	private Taxon rootT;
	private Taxon taxon1;
	private Taxon taxon2;
	private Taxon freeT;
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		sec=new Book();
		sec.setTitleCache("Schoenes saftiges Allg�u");
		name1=new ZoologicalName(Rank.SPECIES(),"Panthera","onca",null,null,null,"p.1467");
		name2=new BotanicalName(Rank.SPECIES(),"Abies","alba",null,null,null,"p.317");
		// taxa
		taxon1=Taxon.NewInstance(name1,sec);
		taxon2=Taxon.NewInstance(name2,sec);
		freeT = Taxon.NewInstance(null, null);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
/**************** TESTS **************************************/

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.TaxonBase#getName()}.
	 */
	@Test
	public final void testGetName() {
		assertEquals(name1.getTitleCache(), taxon1.getName().getTitleCache());
		assertNull(freeT.getName());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.TaxonBase#setName(eu.etaxonomy.cdm.model.name.TaxonNameBase)}.
	 */
	@Test
	public final void testSetName() {
		assertNull(freeT.getName());
		freeT.setName(name2);
		assertNotNull(freeT.getName());
		assertSame(freeT.getName(), name2);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.TaxonBase#isDoubtful()}.
	 */
	@Test
	public final void testIsDoubtful() {
		boolean oldValue;
		oldValue = taxon1.isDoubtful(); 
		taxon1.setDoubtful(!oldValue);
		assertEquals(! oldValue, taxon1.isDoubtful());
		taxon1.setDoubtful(oldValue);
		assertEquals( oldValue, taxon1.isDoubtful());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.TaxonBase#setDoubtful(boolean)}.
	 */
	@Test
	public final void testSetDoubtful() {
		//covered by testIsDoubtful
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.TaxonBase#getSec()}.
	 */
	@Test
	public final void testGetSec() {
		assertEquals(sec.getTitleCache(), taxon1.getSec().getTitleCache());
		assertNull(freeT.getSec());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.TaxonBase#setSec(eu.etaxonomy.cdm.model.reference.ReferenceBase)}.
	 */
	@Test
	public final void testSetSec() {
		assertNull(freeT.getSec());
		freeT.setSec(sec);
		assertNotNull(freeT.getSec());
		assertSame(freeT.getSec(), sec);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.TaxonBase#isSaveable()}.
	 */
	@Test
	public final void testIsSaveable() {
		assertFalse(freeT.isSaveable());
		assertTrue(taxon1.isSaveable());
		freeT.setName(name1);
		assertFalse(freeT.isSaveable());
		freeT.setSec(sec);
		assertTrue(freeT.isSaveable());
		freeT.setName(null);
		assertFalse(freeT.isSaveable());
	}

}
