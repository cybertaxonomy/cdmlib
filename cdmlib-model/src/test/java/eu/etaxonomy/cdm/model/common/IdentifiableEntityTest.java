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
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;

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

		abies = NonViralName.NewInstance(Rank.GENUS(), null);
		abies.setNameCache("Abies");
		abies.setTitleCache("Abies");
		
		abiesMill = NonViralName.NewInstance(Rank.GENUS(), null);
		abiesMill.setNameCache("Abies");
		abiesMill.setTitleCache("Abies Mill.");
		
		abiesAlba = NonViralName.NewInstance(Rank.SPECIES(), null);
		abiesAlba.setNameCache("Abies alba");
		abiesAlba.setTitleCache("Abies alba");
		
		abiesAlbaMichx = NonViralName.NewInstance(Rank.SPECIES(), null);
		abiesAlbaMichx.setNameCache("Abies alba");
		abiesAlbaMichx.setTitleCache("Abies alba Michx.");
		
		abiesAlbaMill = NonViralName.NewInstance(Rank.SPECIES(), null);
		abiesAlbaMill.setNameCache("Abies alba");
		abiesAlbaMill.setTitleCache("Abies alba Mill.");
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
}
