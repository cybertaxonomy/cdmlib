/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.name;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author a.mueller
 * @date 14.09.2017
 *
 */
public class RegistrationTest {

    private TaxonName name1;
    private TaxonName name2;


    /**
     * @throws java.lang.Exception
     */
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

}
