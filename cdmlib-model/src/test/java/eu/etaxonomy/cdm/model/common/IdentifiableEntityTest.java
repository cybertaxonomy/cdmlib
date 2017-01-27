/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.babadshanjan
 * @created 02.02.2009
 * @version 1.0
 */
public class IdentifiableEntityTest {

	private NonViralName<?> abies;
	private NonViralName<?> abiesMill;
	private NonViralName<?> abiesAlba;
	private NonViralName<?> abiesAlbaMichx;
	private NonViralName<?> abiesAlbaMill;
	private NonViralName<?> abiesAlbaxPinusBeta;
	private NonViralName<?> pinusBeta;

	private Taxon abiesTaxon;
	private Taxon abiesMillTaxon;

	private NonViralName<?> abiesAutonym;
	private Taxon abiesAutonymTaxon;

	private NonViralName<?> abiesBalsamea;
	private Taxon abiesBalsameaTaxon;
	private Taxon abiesAlbaxPinusBetaTaxon;
	/**
	 * @throws java.lang.Exception
	 */

	@BeforeClass
	public static void setUpBeforeClass() {
		DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
		vocabularyStore.initialize();

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

		abies = TaxonNameBase.NewNonViralInstance(Rank.GENUS(), null);
		abies.setNameCache("Abies");
		abies.setTitleCache("Abies", true);
		Reference sec = ReferenceFactory.newArticle();
		sec.setTitle("Abies alba Ref");

		abiesTaxon = Taxon.NewInstance(abies, sec);

		abiesMill = TaxonNameBase.NewNonViralInstance(Rank.GENUS(), null);
		abiesMill.setNameCache("Abies");
		abiesMill.setTitleCache("Abies Mill.", true);
		abiesMillTaxon = Taxon.NewInstance(abiesMill, sec);

		abiesAlba = TaxonNameBase.NewNonViralInstance(Rank.SPECIES(), null);
		abiesAlba.setNameCache("Abies alba");
		abiesAlba.setTitleCache("Abies alba", true);

		abiesAlbaMichx = TaxonNameBase.NewNonViralInstance(Rank.SPECIES(), null);
		abiesAlbaMichx.setNameCache("Abies alba");
		abiesAlbaMichx.setTitleCache("Abies alba Michx.", true);

		abiesAlbaMill = TaxonNameBase.NewNonViralInstance(Rank.SPECIES(), null);
		abiesAlbaMill.setNameCache("Abies alba");
		abiesAlbaMill.setTitleCache("Abies alba Mill.", true);

		abiesAutonym  = TaxonNameBase.NewNonViralInstance(Rank.SECTION_BOTANY());
		abiesAutonym.setGenusOrUninomial("Abies");
		abiesAutonym.setInfraGenericEpithet("Abies");

		abiesAutonym.setTitleCache("Abies Mill. sect. Abies", true);
		abiesAutonym.getNameCache();
		abiesAutonymTaxon = Taxon.NewInstance(abiesAutonym, sec);

		abiesBalsamea  = TaxonNameBase.NewNonViralInstance(Rank.SECTION_BOTANY());
		abiesBalsamea.setGenusOrUninomial("Abies");
		abiesBalsamea.setInfraGenericEpithet("Balsamea");
		abiesBalsamea.getNameCache();
		abiesBalsamea.setTitleCache("Abies sect. Balsamea L.", true);
		abiesBalsameaTaxon = Taxon.NewInstance(abiesBalsamea, sec);

		abiesAlbaxPinusBeta = TaxonNameBase.NewNonViralInstance(Rank.SPECIES());
		pinusBeta = TaxonNameBase.NewNonViralInstance(Rank.SPECIES());
		pinusBeta.setGenusOrUninomial("Pinus");
		pinusBeta.setSpecificEpithet("beta");
		abiesAlbaxPinusBeta.setHybridFormula(true);
		abiesAlbaxPinusBeta.addHybridParent(abiesAlba, HybridRelationshipType.FIRST_PARENT(), null);
		abiesAlbaxPinusBeta.addHybridParent(pinusBeta, HybridRelationshipType.SECOND_PARENT(), null);

	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.IdentifiableEntity#compareTo(eu.etaxonomy.cdm.model.common.IdentifiableEntity)}.
	 */
	@Test
	public void testCompareTo() {
		int result = 0;

		// "Abies" < "Abies Mill."
		result = abies.compareTo(abiesMill);
		assertTrue(result < 0);

		abiesTaxon = abies.getTaxa().iterator().next();

		assertTrue(abiesTaxon.compareTo(abiesTaxon) == 0);

		assertTrue(abiesMillTaxon.compareTo(abiesTaxon) > 0);

		assertTrue(abiesTaxon.compareTo(abiesMillTaxon) < 0);

		// "Abies Mill." > "Abies"
		result = abiesMill.compareTo(abies);
		assertTrue(result > 0);

		// "Abies" < "Abies alba"
		result = abies.compareTo(abiesAlba);
		assertTrue(result < 0);

		// "Abies alba" > "Abies"
		result = abiesAlba.compareTo(abies);
		assertTrue(result > 0);

		// "Abies Mill." < "Abies alba Michx."
		result = abiesMill.compareTo(abiesAlbaMichx);
		assertTrue(result < 0);

		// "Abies alba Michx." > "Abies Mill."
		result = abiesAlbaMichx.compareTo(abiesMill);
		assertTrue(result > 0);

		//Autonym should sorted without the authorstring

		result = abiesAutonym.compareTo(abiesBalsamea);
		assertTrue(result < 0);
	    // Test consistency of compareTo() with equals():
		// Is consistent if and only if for every e1 and e2 of class C
		// e1.compareTo(e2) == 0 has the same boolean value as e1.equals(e2)

		boolean compareResult = false;
		boolean equalsResult = false;

		compareResult = (abies.compareTo(abies) == 0);
		equalsResult = abies.equals(abies);
		assertEquals(compareResult, equalsResult);

		compareResult = (abies.compareTo(abiesAlba) == 0);
		equalsResult = abies.equals(abiesAlba);
		assertEquals(compareResult, equalsResult);

		compareResult = (abiesMill.compareTo(abies) == 0);
		equalsResult = abiesMill.equals(abies);
		assertEquals(compareResult, equalsResult);

		//Abies alba x Pinus beta < Abies alba xinus
		BotanicalName abiesAlbaXinus = BotanicalName.NewInstance(Rank.SUBSPECIES());
		abiesAlbaXinus.setGenusOrUninomial("Abies");
		abiesAlbaXinus.setSpecificEpithet("alba");
		abiesAlbaXinus.setInfraSpecificEpithet("xinus");
		result = abiesAlbaxPinusBeta.compareTo(abiesAlbaXinus);
		assertTrue(result < 0);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.IdentifiableEntity#addCredit(eu.etaxonomy.cdm.model.common.IdentifiableEntity)}.
	 */
	@Test
	public void testAddCredit() {
		assertNotNull("A list should always be returned",abies.getCredits());
		assertTrue("No credits should exist",abies.getCredits().isEmpty());
		String text1 = "Credit1";
		String text2 = "Credit2";
		String text3 = "Credit0"; //for sorting order
		Person person = Person.NewTitledInstance("Me");
		abies.addCredit(Credit.NewInstance(person, text1));
		assertEquals("Number of credits should be 1",1,abies.getCredits().size());
		abies.addCredit(Credit.NewInstance(person, text2));
		assertEquals("Number of credits should be 2",2,abies.getCredits().size());
		abies.addCredit(Credit.NewInstance(person, text3));
		assertEquals("Number of credits should be 3",3,abies.getCredits().size());
		assertEquals("Credit0 should be last in list", text3, abies.getCredits(2).getText());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.IdentifiableEntity#addCredit(eu.etaxonomy.cdm.model.common.IdentifiableEntity)}.
	 */
	@Test
	public void testRemoveCredit() {
		assertNotNull("A list should always be returned",abies.getCredits());
		String text1 = "Credit1";
		String text2 = "Credit2";
		Person person = Person.NewTitledInstance("Me");
		Credit credit1 = Credit.NewInstance(person, text1);
		Credit credit2 = Credit.NewInstance(person, text2);
		abies.addCredit(credit1);
		abies.addCredit(credit2);
		assertEquals("Number of credits should be 2",2,abies.getCredits().size());
		abies.removeCredit(credit1);
		assertNotNull("A list should always be returned",abies.getCredits());
		assertFalse("The list should not be empty",abies.getCredits().isEmpty());
		assertEquals("Number of credits should be 1",1,abies.getCredits().size());
		assertEquals("Remaining credit should be credit2",credit2,abies.getCredits().get(0));
		abies.removeCredit(credit2);
		assertNotNull("A list should always be returned",abies.getCredits());
		assertTrue("No credits should exist",abies.getCredits().isEmpty());
	}

	@Test
	public void testClone(){
		IdentifiableEntity clone = (IdentifiableEntity)abies.clone();
		assertNotNull(clone);
		assertEquals(clone.annotations, abies.annotations);
		assertEquals(clone.markers, abies.markers);
		assertFalse(clone.uuid.equals(abies.uuid));
	}
}
