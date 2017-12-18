/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.idminter;

import java.io.FileNotFoundException;

import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.idminter.IdentifierMinter.Identifier;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.kohlbecker
 * @since Dec 13, 2017
 *
 */
@DataSet("RegistrationIdentifierMinterTest.xml")
public class RegistrationIdentifierMinterTest extends CdmTransactionalIntegrationTest {

    @SpringBeanByType
    private SessionFactory factory;

    /**
     * {@inheritDoc}
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {

    }

    @Test
    @DataSet("RegistrationIdentifierMinterTest.xml")
    public void testNextValueNoDomain(){

        RegistrationIdentifierMinter minter = new RegistrationIdentifierMinter();
        minter.setSessionFactory(factory);
        minter.setMinLocalId("100");
        minter.setMaxLocalId("110");
        Identifier<String> indentifier = minter.mint();
        Assert.assertEquals("104", indentifier.localId);
    }

    @Test
    @DataSet("RegistrationIdentifierMinterTest.xml")
    public void testNextValueAndString(){
        RegistrationIdentifierMinter minter = new RegistrationIdentifierMinter();
        minter.setSessionFactory(factory);
        minter.setMinLocalId("100");
        minter.setMaxLocalId("110");
        minter.setIdentifierFormatString("http://phycobank/%s");
        Identifier<String> indentifier = minter.mint();
        Assert.assertEquals("103", indentifier.localId);
        Assert.assertEquals("http://phycobank/103", indentifier.identifier);
    }

    @Test
    @DataSet("RegistrationIdentifierMinterTest.xml")
    public void testNextValueNewRange(){
        RegistrationIdentifierMinter minter = new RegistrationIdentifierMinter();
        minter.setSessionFactory(factory);
        minter.setMinLocalId("300");
        minter.setMaxLocalId("400");
        minter.setIdentifierFormatString("http://phycobank/%s");
        Identifier<String> indentifier = minter.mint();
        Assert.assertEquals("300", indentifier.localId);
    }

    @Test
    @DataSet("RegistrationIdentifierMinterTest.xml")
    public void testNextValueOutOfRange(){
        RegistrationIdentifierMinter minter = new RegistrationIdentifierMinter();
        minter.setSessionFactory(factory);
        minter.setMinLocalId("100");
        minter.setMaxLocalId("102");
        minter.setIdentifierFormatString("http://phycobank/%s");
        boolean thrown = false;
        try {
            Identifier<String> indentifier = minter.mint();
        } catch (OutOfIdentifiersException e){
            thrown = true;
        }
        Assert.assertTrue(thrown);
    }

}
