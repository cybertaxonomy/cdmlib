/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.name;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author a.mueller
 * @since 14.09.2017
 */
public class RegistrationTest {

    private TaxonName name1;
    private TaxonName name2;

    @Before
    public void setUp() throws Exception {
        name1 = TaxonNameFactory.NewBotanicalInstance(null);
        name2 = TaxonNameFactory.NewBotanicalInstance(null);
    }

    @Test
    public void testSetName() {
        Registration registration = Registration.NewInstance();

        //Assert start
        Assert.assertNull(registration.getName());
        Assert.assertTrue(name1.getRegistrations().size() == 0);
        Assert.assertTrue(name2.getRegistrations().size() == 0);

        //set name1
        registration.setName(name1);
        Assert.assertEquals(name1, registration.getName());
        Assert.assertTrue(name1.getRegistrations().size() == 1);
        Assert.assertTrue(name1.getRegistrations().contains(registration));
        Assert.assertTrue(name2.getRegistrations().size() == 0);

        //set name2
        registration.setName(name2);
        Assert.assertEquals(name2, registration.getName());
        Assert.assertTrue(name1.getRegistrations().size() == 0);
        Assert.assertTrue(name2.getRegistrations().size() == 1);
        Assert.assertTrue(name2.getRegistrations().contains(registration));

        //set null
        registration.setName(null);
        Assert.assertNull(registration.getName());
        Assert.assertTrue(name1.getRegistrations().size() == 0);
        Assert.assertTrue(name2.getRegistrations().size() == 0);

        //name with 2 registrations
        registration.setName(name1);
        Registration registration2 = Registration.NewInstance();
        registration2.setName(name1);
        Assert.assertTrue(name1.getRegistrations().size() == 2);
        Assert.assertTrue(name1.getRegistrations().contains(registration));
        Assert.assertTrue(name1.getRegistrations().contains(registration2));
    }

    /**
     * see https://dev.e-taxonomy.eu/redmine/issues/7995
     * @throws InterruptedException
     */
    @Test
    public void testUpdateStatusAndDate() throws InterruptedException {
        Registration registration = Registration.NewInstance();

        registration.setStatus(RegistrationStatus.CURATION);
        assertNull(registration.getRegistrationDate());

        DateTime before = DateTime.now();
        Thread.sleep(10);
        // The Registration.registrationDate should be set to now when the status is set to PUBLISHED.
        registration.updateStatusAndDate(RegistrationStatus.PUBLISHED);
        assertNotNull(registration.getRegistrationDate());
        assertTrue(registration.getRegistrationDate().isAfter(before));
        Thread.sleep(10);
        assertTrue(registration.getRegistrationDate().isBeforeNow());

        // When status changes from PUBLISHED to something else the registrationDate should be resetted to null.
        registration.updateStatusAndDate(RegistrationStatus.CURATION);
        assertNull(registration.getRegistrationDate());
    }
}