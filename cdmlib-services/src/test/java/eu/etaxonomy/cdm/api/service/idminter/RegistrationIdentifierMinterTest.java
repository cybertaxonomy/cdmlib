/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.idminter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.api.service.idminter.IdentifierMinter.Identifier;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmServicesTransactionalSpringIntegrationTest;

/**
 * @author a.kohlbecker
 * @since Dec 13, 2017
 */
public class RegistrationIdentifierMinterTest extends CdmServicesTransactionalSpringIntegrationTest {

    @Autowired
    private SessionFactory factory;

    @Test
    @CdmDataSet("RegistrationIdentifierMinterTest.xml")
    public void testNextValueNoDomain(){

        RegistrationIdentifierMinter minter = new RegistrationIdentifierMinter();
        minter.setSessionFactory(factory);
        minter.setMinLocalId("100");
        minter.setMaxLocalId("110");
        Identifier<String> indentifier = minter.mint();
        assertEquals("104", indentifier.getLocalId());
    }

    @Test
    @CdmDataSet("RegistrationIdentifierMinterTest.xml")
    public void testNextValueAndString(){
        RegistrationIdentifierMinter minter = new RegistrationIdentifierMinter();
        minter.setSessionFactory(factory);
        minter.setMinLocalId("100");
        minter.setMaxLocalId("110");
        minter.setIdentifierFormatString("http://phycobank/%s");
        Identifier<String> indentifier = minter.mint();
        assertEquals("103", indentifier.getLocalId());
        assertEquals("http://phycobank/103", indentifier.identifier);
    }

    @Test
    @CdmDataSet("RegistrationIdentifierMinterTest.xml")
    public void testNextValueNewRange(){
        RegistrationIdentifierMinter minter = new RegistrationIdentifierMinter();
        minter.setSessionFactory(factory);
        minter.setMinLocalId("300");
        minter.setMaxLocalId("400");
        minter.setIdentifierFormatString("http://phycobank/%s");
        Identifier<String> indentifier = minter.mint();
        assertEquals("300", indentifier.getLocalId());
    }

    @Test
    @CdmDataSet("RegistrationIdentifierMinterTest.xml")
    public void testNextValueOutOfRange(){
        RegistrationIdentifierMinter minter = new RegistrationIdentifierMinter();
        minter.setSessionFactory(factory);
        minter.setMinLocalId("100");
        minter.setMaxLocalId("102");
        minter.setIdentifierFormatString("http://phycobank/%s");
        boolean thrown = false;
        try {
            minter.mint();
        } catch (OutOfIdentifiersException e){
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    public void testIsFromOwnRegistration(){
        RegistrationIdentifierMinter minter = new RegistrationIdentifierMinter();
        assertNull(minter.getIdentifierFormatString());
        assertTrue(minter.isFromOwnRegistration("1234"));

        minter = new RegistrationIdentifierMinter();
        minter.setIdentifierFormatString("http://phycobank/%s");
        assertTrue(minter.isFromOwnRegistration("http://phycobank/1234"));
        assertFalse(minter.isFromOwnRegistration("http://phycobank/"));
        assertFalse(minter.isFromOwnRegistration("http://otherreg/1234"));
        assertFalse(minter.isFromOwnRegistration("1234"));

        minter = new RegistrationIdentifierMinter();
        minter.setIdentifierFormatString("%s");
        assertTrue(minter.isFromOwnRegistration("1234"));
        assertFalse(minter.isFromOwnRegistration("http://phycobank/1234"));
        assertFalse(minter.isFromOwnRegistration("http://phycobank/"));
        assertFalse(minter.isFromOwnRegistration("http://otherreg/1234"));
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}

}
