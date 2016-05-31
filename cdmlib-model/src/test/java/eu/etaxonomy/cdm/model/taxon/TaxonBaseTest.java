/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;

/**
 * @author a.mueller
 *
 */
public class TaxonBaseTest extends EntityTestBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TaxonBaseTest.class);

	private Reference sec;
	private ZoologicalName name1;
	private BotanicalName name2;
	private Taxon rootT;
	private Taxon taxon1;
	private Synonym synonym1;
	private Taxon freeT;

	@BeforeClass
	public static void setUpBeforeClass() {
		DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
		vocabularyStore.initialize();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		sec= ReferenceFactory.newBook();
		sec.setTitleCache("Schoenes saftiges Allg�u", true);
		name1 = ZoologicalName.NewInstance(Rank.SPECIES(),"Panthera",null,"onca",null,null,null,"p.1467", null);
		HomotypicalGroup homotypicalGroup = HomotypicalGroup.NewInstance();
		name2 = BotanicalName.NewInstance(Rank.SPECIES(),"Abies",null,"alba",null,null,null,"p.317", homotypicalGroup);
		// taxa
		taxon1 = Taxon.NewInstance(name1,sec);
		synonym1 = Synonym.NewInstance(name2,sec);
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
//
//	/**
//	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.TaxonBase#setName(eu.etaxonomy.cdm.model.name.TaxonNameBase)}.
//	 */
//	@Test
//	public final void testSetName() {
//		assertNull(freeT.getName());
//		freeT.setName(name2);
//		assertNotNull(freeT.getName());
//		assertSame(freeT.getName(), name2);
//		assertTrue(name1.getTaxa().contains(taxon1));
//		assertTrue(name2.getSynonyms().contains(synonym1));
//	}

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
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.TaxonBase#setSec(eu.etaxonomy.cdm.model.reference.Reference)}.
	 */
	@Test
	public final void testSetSec() {
		assertNull(freeT.getSec());
		freeT.setSec(sec);
		assertNotNull(freeT.getSec());
		assertSame(freeT.getSec(), sec);
	}

	@Test

	public final void testClone(){

		BotanicalName test = BotanicalName.NewInstance(Rank.SPECIES());
		String genus = "test";
		String infraGenericEpithet = "test";
		test.setGenusOrUninomial(genus);
		test.setInfraGenericEpithet(infraGenericEpithet);
		Reference secRef = ReferenceFactory.newArticle();
		secRef.setTitle("Test ...");
		freeT.setSec(secRef);
		freeT.setName(test);
		Taxon clone = (Taxon)freeT.clone();
		assertNull(clone.getSec());
		assertSame(freeT.getName(), clone.getName());
	}
}
