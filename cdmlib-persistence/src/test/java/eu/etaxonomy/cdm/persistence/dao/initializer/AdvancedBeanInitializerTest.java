/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.initializer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.Hibernate;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.agent.Address;
import eu.etaxonomy.cdm.model.agent.Contact;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.persistence.dao.agent.IAgentDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 \* @since 16.11.2015
 */
public class AdvancedBeanInitializerTest extends CdmTransactionalIntegrationTest {

    private static final UUID personUuid = UUID.fromString("d0568bb1-4dc8-40dc-a405-d0b9e714a7a9");

    private static final UUID referenceUuid = UUID.fromString("f48196c6-854a-416e-8f2a-67bd39e988dc");

    private static final UUID nameUuid = UUID.fromString("98cbb643-d521-4ca7-86f7-8180bea85d9f");

    @SpringBeanByType
    private IAgentDao agentDao;

    @SpringBeanByType
    private IReferenceDao referenceDao;

    @SpringBeanByType
    private ITaxonNameDao nameDao;

    @SpringBeanByType
    private AdvancedBeanInitializer initializer;

    @DataSet
    @Test
    public void testContact() {
        Person person = (Person)agentDao.findByUuid(personUuid);

        final List<String> propPath = Arrays.asList(new String[]{
            "contact.urls",
            "contact.phoneNumbers",
            "contact.addresses",
            "contact.faxNumbers",
            "contact.emailAddresses",
        });
        initializer.initialize(person, propPath);
    }

    /**
     * Attempt to reproduce #7331 without success
     */
    @DataSet
    @Test
    public void testFullNameGraphWithPreloadedReference() {
        // find the reference by iD (not load!)
        Reference ref = referenceDao.findById(5000);
        TaxonName name = nameDao.findById(5000);
        assertFalse("for this test to be significant the authorship must be uninitialized", Hibernate.isInitialized(name.getNomenclaturalReference().getAuthorship()));
        initializer.initialize(name, Arrays.asList(new String[]{"nomenclaturalReference.authorship.$"}));
        assertTrue(Hibernate.isInitialized(name.getNomenclaturalReference().getAuthorship()));
    }



    @Override
    // @Test
    public void createTestDataSet() throws FileNotFoundException {
        // 1. create person and a reference
        Person person = Person.NewTitledInstance("Hallo you");
        Set<Address> addresses = new HashSet<Address>();
        addresses.add(Address.NewInstance(Country.GERMANY(), "locality", "pobox", "postcode", "region", "street", Point.NewInstance(50.02,33.3, ReferenceSystem.GOOGLE_EARTH(), 3)));
        List<String> emailAddresses = new ArrayList<String>();
        emailAddresses.add("My.email@web.de");
        List<String> faxNumbers = new ArrayList<String>();
        faxNumbers.add("0049-30-1234545");
        List<String> phoneNumbers = new ArrayList<String>();
        phoneNumbers.add("0049-30-1234546");
        List<URI> urls = new ArrayList<URI>();
        urls.add(URI.create("http://www.test.de"));
        Contact contact = Contact.NewInstance(addresses, emailAddresses, faxNumbers, phoneNumbers, urls);

        person.setContact(contact);
        person.setUuid(personUuid);
        person = (Person)agentDao.save(person);

        Reference ref = ReferenceFactory.newBook();
        ref.setUuid(referenceUuid);
        ref.setAuthorship(person);
        ref.setTitleCache("The Book", true);
        referenceDao.save(ref);

        TaxonName name = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        name.setUuid(nameUuid);
        name.setNomenclaturalReference(ref);
        name.setTitleCache("Species testii", true);
        nameDao.save(name);

        // 2. end the transaction so that all data is actually written to the db
        setComplete();
        endTransaction();

        // use the fileNameAppendix if you are creating a data set file which need to be named differently
        // from the standard name. For example if a single test method needs different data then the other
        // methods the test class you may want to set the fileNameAppendix when creating the data for this method.
        String fileNameAppendix = null;

        // 3.
        writeDbUnitDataSetFile(new String[] {
            "ADDRESS", "AGENTBASE","AgentBase_contact_emailaddresses",
            "AgentBase_contact_faxnumbers","AgentBase_contact_phonenumbers",
            "AgentBase_contact_urls","AgentBase_Address",
            "REFERENCE", "TaxonName", "HomotypicalGroup",
            "HIBERNATE_SEQUENCES" // IMPORTANT!!!
            },
            fileNameAppendix );

    }

}
