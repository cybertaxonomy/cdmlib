/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.agent;

import java.util.GregorianCalendar;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.TimePeriod;

/**
 * @author a.babadshanjan
 *
 */
public class InstitutionalMembershipTest {

	private InstitutionalMembership mship;

	@Before
	public void onSetUp() throws Exception {

		mship = InstitutionalMembership.NewInstance();

		mship.setPerson(new Person("Steve", "Miller", "Mil."));
		GregorianCalendar joined = new GregorianCalendar(1967, 4, 23);
		//with java.time.LocalDate it is not allowed to have a day without a month
		GregorianCalendar resigned = new GregorianCalendar(1999, 1, 10);
		mship.setPeriod(TimePeriod.NewInstance(joined, resigned));
		mship.setInstitute(Institution.NewInstance());
		mship.setDepartment("Biodiversity");
		mship.setRole("Head");
	}

	@Test
	public void testMembershipInit() {
		Assert.assertNotNull(mship);
	}
}
