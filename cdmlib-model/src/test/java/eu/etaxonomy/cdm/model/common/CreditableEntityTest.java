/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;

/**
 * @author a.mueller
 * @since 02.12.2025
 */
public class CreditableEntityTest extends EntityTestBase {

    private Classification classification;

	@BeforeEach
	public void setUp() throws Exception {
	    classification = Classification.NewInstance("Classification with credits");
	}

	@Test
	public void testAddCredit() {
		assertNotNull(classification.getCredits(), "A list should always be returned");
		assertTrue(classification.getCredits().isEmpty(), "No credits should exist");
		String text1 = "Credit1";
		String text2 = "Credit2";
		String text3 = "Credit0"; //for sorting order
		Person person = Person.NewTitledInstance("Me");
		TimePeriod timePeriod = TimePeriod.NewInstance(1925);
		classification.addCredit(Credit.NewInstance(person, timePeriod, text1));
		assertEquals(1, classification.getCredits().size(), "Number of credits should be 1");
		classification.addCredit(Credit.NewInstance(person, timePeriod, text2));
		assertEquals(2, classification.getCredits().size(), "Number of credits should be 2");
		classification.addCredit(Credit.NewInstance(person, timePeriod, text3));
		assertEquals(3, classification.getCredits().size(), "Number of credits should be 3");
		assertEquals(text3, classification.getCredits(2).getText(), "Credit0 should be last in list");
	}

	@Test
	public void testRemoveCredit() {
		assertNotNull(classification.getCredits(), "A list should always be returned");
		String text1 = "Credit1";
		String text2 = "Credit2";
		Person person = Person.NewTitledInstance("Me");
		Credit credit1 = Credit.NewInstance(person, null, text1);
		Credit credit2 = Credit.NewInstance(person, null, text2);
		classification.addCredit(credit1);
		classification.addCredit(credit2);
		assertEquals(2, classification.getCredits().size(), "Number of credits should be 2");
		classification.removeCredit(credit1);
		assertNotNull(classification.getCredits(), "A list should always be returned");
		assertFalse(classification.getCredits().isEmpty(), "The list should not be empty");
		assertEquals(1, classification.getCredits().size(), "Number of credits should be 1");
		assertEquals(credit2, classification.getCredits().get(0), "Remaining credit should be credit2");
		classification.removeCredit(credit2);
		assertNotNull(classification.getCredits(), "A list should always be returned");
		assertTrue(classification.getCredits().isEmpty(), "No credits should exist");
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
