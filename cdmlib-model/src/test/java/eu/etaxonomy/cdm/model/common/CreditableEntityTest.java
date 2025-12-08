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

import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;

/**
 * @author a.mueller
 * @since 02.12.2025
 */
public class CreditableEntityTest extends EntityTestBase {

    private Classification classification;

	@Before
	public void setUp() throws Exception {
	    classification = Classification.NewInstance("Classification with credits");
	}

	@Test
	public void testAddCredit() {
		assertNotNull("A list should always be returned", classification.getCredits());
		assertTrue("No credits should exist", classification.getCredits().isEmpty());
		String text1 = "Credit1";
		String text2 = "Credit2";
		String text3 = "Credit0"; //for sorting order
		Person person = Person.NewTitledInstance("Me");
		TimePeriod timePeriod = TimePeriod.NewInstance(1925);
		classification.addCredit(Credit.NewInstance(person, timePeriod, text1));
		assertEquals("Number of credits should be 1", 1, classification.getCredits().size());
		classification.addCredit(Credit.NewInstance(person, timePeriod, text2));
		assertEquals("Number of credits should be 2", 2, classification.getCredits().size());
		classification.addCredit(Credit.NewInstance(person, timePeriod, text3));
		assertEquals("Number of credits should be 3", 3, classification.getCredits().size());
		assertEquals("Credit0 should be last in list", text3, classification.getCredits(2).getText());
	}

	@Test
	public void testRemoveCredit() {
		assertNotNull("A list should always be returned", classification.getCredits());
		String text1 = "Credit1";
		String text2 = "Credit2";
		Person person = Person.NewTitledInstance("Me");
		Credit credit1 = Credit.NewInstance(person, null, text1);
		Credit credit2 = Credit.NewInstance(person, null, text2);
		classification.addCredit(credit1);
		classification.addCredit(credit2);
		assertEquals("Number of credits should be 2", 2, classification.getCredits().size());
		classification.removeCredit(credit1);
		assertNotNull("A list should always be returned", classification.getCredits());
		assertFalse("The list should not be empty", classification.getCredits().isEmpty());
		assertEquals("Number of credits should be 1", 1, classification.getCredits().size());
		assertEquals("Remaining credit should be credit2", credit2,classification.getCredits().get(0));
		classification.removeCredit(credit2);
		assertNotNull("A list should always be returned", classification.getCredits());
		assertTrue("No credits should exist", classification.getCredits().isEmpty());
	}

	@Test
	public void testClone(){
		IdentifiableEntity<?> clone = classification.clone();
		assertNotNull(clone);
		assertEquals(clone.annotations, classification.annotations);
		assertEquals(clone.markers, classification.markers);
		assertFalse(clone.uuid.equals(classification.uuid));
	}
}