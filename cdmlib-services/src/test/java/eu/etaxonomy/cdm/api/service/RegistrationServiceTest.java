/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.RegistrationStatus;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.persistence.hibernate.permission.Role;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTestWithSecurity;

/**
 * @author a.kohlbecker
 * @since Jun 28, 2019
 *
 */
public class RegistrationServiceTest extends CdmTransactionalIntegrationTestWithSecurity {

    public static final UUID USER2_UUID = UUID.fromString("669f582c-e97f-425b-97f6-bc3b0c08f2a5");

    public static final UUID USER1_UUID = UUID.fromString("68033f81-9947-4b61-b33b-3d05bd438579");

    public static final UUID NDT1_UUID = UUID.fromString("be66964a-ea2b-480e-9dcf-0ee1dd7313eb");

    public static final UUID STD2_UUID = UUID.fromString("8cd056fb-259a-45aa-ab4f-b34033eef2e9");

    public static final UUID STD1_UUID = UUID.fromString("1c29e80a-2611-4be4-9b2f-15bbd15066bf");

    @SpringBeanByType
    @Qualifier("CdmRepository")
    private ICdmRepository repo;

    @Test
    @DataSet
    public void testPage(){

        OrderHint orderBySpecificIdentifier = new OrderHint("specificIdentifier", SortOrder.ASCENDING);
        OrderHint orderById = new OrderHint("id", SortOrder.ASCENDING);

        repo.authenticate("user1", "00000");

        Pager<Registration> pager;


        pager = repo.getRegistrationService().page((UUID)null, null, null, null, null, null, null, null, null);
        assertEquals(pager.getRecords().size(), pager.getCount().intValue());
        assertEquals("with authenticated user expecting all 4 Registrations", 4l, pager.getCount().longValue());

        pager = repo.getRegistrationService().page(USER1_UUID, null, null, null, null, null, null, null, null);
        assertEquals(2l, pager.getCount().longValue());

        pager = repo.getRegistrationService().page(USER2_UUID, null, null, null, null, null, null, null, null);
        assertEquals(2l, pager.getCount().longValue());

        // status filter
        pager = repo.getRegistrationService().page((UUID)null, Arrays.asList(RegistrationStatus.PREPARATION), null, null, null, null, null, null, null);
        assertEquals(1l, pager.getCount().longValue());
        assertEquals(RegistrationStatus.PREPARATION, pager.getRecords().get(0).getStatus());

        pager = repo.getRegistrationService().page((UUID)null, Arrays.asList(RegistrationStatus.PREPARATION, RegistrationStatus.PUBLISHED), null, null, null, null, null, null, null);
        assertEquals(2l, pager.getCount().longValue());

        // status filter with submitter
        pager = repo.getRegistrationService().page(USER1_UUID, Arrays.asList(RegistrationStatus.PREPARATION, RegistrationStatus.PUBLISHED), null, null, null, null, null, null, null);
        assertEquals(2l, pager.getCount().longValue());

        pager = repo.getRegistrationService().page(USER2_UUID, Arrays.asList(RegistrationStatus.PREPARATION, RegistrationStatus.PUBLISHED), null, null, null, null, null, null, null);
        assertEquals(0l, pager.getCount().longValue());

        pager = repo.getRegistrationService().page(USER2_UUID, Arrays.asList(RegistrationStatus.CURATION), null, null, null, null, null, null, null);
        assertEquals(1l, pager.getCount().longValue());

        // identifier filter
        pager = repo.getRegistrationService().page((UUID)null, null, "100", null, null, null, null, null, null);
        assertEquals(4l, pager.getCount().longValue());

        pager = repo.getRegistrationService().page((UUID)null, null, "test/1001", null, null, null, null, null, null);
        assertEquals(1l, pager.getCount().longValue());

        // identifier filter with submitter
        pager = repo.getRegistrationService().page(USER1_UUID, null, "100", null, null, null, null, null, null);
        assertEquals(2l, pager.getCount().longValue());

        pager = repo.getRegistrationService().page(USER2_UUID, null, "1002", null, null, null, null, null, null);
        assertEquals(1l, pager.getCount().longValue());

        // taxon name filter
        pager = repo.getRegistrationService().page((UUID)null, null, null, "Digilalus", null, null, null, null, null);
        assertEquals(4l, pager.getCount().longValue());

        pager = repo.getRegistrationService().page((UUID)null, null, null, "Dig*lus", null, null, null, null, null);
        assertEquals(4l, pager.getCount().longValue());

        pager = repo.getRegistrationService().page((UUID)null, null, null, "Digilalus prim", null, null, null, null, null);
        assertEquals(1l, pager.getCount().longValue());

        pager = repo.getRegistrationService().page((UUID)null, null, null, "Digilalus secundus", null, null, null, Arrays.asList(orderBySpecificIdentifier), null);
        assertEquals(2l, pager.getCount().longValue());

        assertEquals("test/1001", pager.getRecords().get(0).getIdentifier());
        assertNotNull("expecting registration test/1001 to be with name", pager.getRecords().get(0).getName());
        assertTrue("expecting registration test/1001 to be witout type designation",  pager.getRecords().get(0).getTypeDesignations().isEmpty());

        assertEquals("test/1003", pager.getRecords().get(1).getIdentifier());
        assertNull("Expecting the registration test/1003 to be without name", pager.getRecords().get(1).getName());
        assertEquals(STD2_UUID, pager.getRecords().get(1).getTypeDesignations().iterator().next().getUuid());

        pager = repo.getRegistrationService().page((UUID)null, null, null, "Digila*", null, null, null, null, null);
        assertEquals(4l, pager.getCount().longValue());

        pager = repo.getRegistrationService().page((UUID)null, null, null, "*imus", null, null, null, null, null);
        assertEquals(1l, pager.getCount().longValue());

        // taxon name filter with user
        pager = repo.getRegistrationService().page(USER2_UUID, null, null, "Digilalus", null, null, null, null, null);
        assertEquals(2l, pager.getCount().longValue());

        // taxon name filter with user and status
        pager = repo.getRegistrationService().page(USER1_UUID, Arrays.asList(RegistrationStatus.PREPARATION), null, "Digilalus", null, null, null, null, null);
        assertEquals(1l, pager.getCount().longValue());

        pager = repo.getRegistrationService().page(USER1_UUID, Arrays.asList(RegistrationStatus.PREPARATION, RegistrationStatus.PUBLISHED), "1001", "Digilalus", null, null, null, null, null);
        assertEquals(1l, pager.getCount().longValue());

        // type designation status

        // assure the terms are loaded
        assertNotNull(SpecimenTypeDesignationStatus.HOLOTYPE());
        assertNotNull(NameTypeDesignationStatus.TAUTONYMY());

        pager = repo.getRegistrationService().page((UUID)null, null, null, null, Arrays.asList(SpecimenTypeDesignationStatus.HOLOTYPE().getUuid()),
                null, null, null, null);
        assertEquals(1l, pager.getCount().longValue());
        assertEquals(STD1_UUID, pager.getRecords().get(0).getTypeDesignations().iterator().next().getUuid());

        pager = repo.getRegistrationService().page((UUID)null, null, null, null, Arrays.asList(NameTypeDesignationStatus.TAUTONYMY().getUuid()),
                null, null, Arrays.asList(orderBySpecificIdentifier), null);
        assertEquals(1l, pager.getCount().longValue());
        assertEquals(NDT1_UUID, pager.getRecords().get(0).getTypeDesignations().iterator().next().getUuid());

        pager = repo.getRegistrationService().page((UUID)null, null, null, null, Arrays.asList(SpecimenTypeDesignationStatus.HOLOTYPE().getUuid(), NameTypeDesignationStatus.TAUTONYMY().getUuid()),
                null, null, Arrays.asList(orderBySpecificIdentifier), null);
        assertEquals(2l, pager.getCount().longValue());
        assertEquals("test/1000", pager.getRecords().get(0).getIdentifier());
        assertEquals(STD1_UUID, pager.getRecords().get(0).getTypeDesignations().iterator().next().getUuid());
        assertEquals("test/1002", pager.getRecords().get(1).getIdentifier());
        assertEquals(NDT1_UUID, pager.getRecords().get(1).getTypeDesignations().iterator().next().getUuid());

        // type designation status with user
        pager = repo.getRegistrationService().page(USER2_UUID, null, null, null, Arrays.asList(SpecimenTypeDesignationStatus.HOLOTYPE().getUuid(), NameTypeDesignationStatus.TAUTONYMY().getUuid()),
                null, null, null, null);
        assertEquals(1l, pager.getCount().longValue());
        assertEquals(NDT1_UUID, pager.getRecords().get(0).getTypeDesignations().iterator().next().getUuid());

        // type designation status with name
        /*
        pager = repo.getRegistrationService().page((UUID)null, null, null, "Digital", Arrays.asList(SpecimenTypeDesignationStatus.HOLOTYPE().getUuid(), NameTypeDesignationStatus.TAUTONYMY().getUuid()),
                null, null, Arrays.asList(orderBySpecificIdentifier), null);
        assertEquals(2l, pager.getCount().longValue());
        assertEquals(STD1_UUID, pager.getRecords().get(0).getTypeDesignations().iterator().next().getUuid());
        assertEquals(NDT1_UUID, pager.getRecords().get(1).getTypeDesignations().iterator().next().getUuid());
        */
    }

    @Test
    public void testPage_unautheticated(){
        Pager<Registration> pager = repo.getRegistrationService().page((UUID)null, null, null, null, null, null, null, null, null);
        assertEquals(pager.getRecords().size(), pager.getCount().intValue());
        assertEquals("expecting only the PUBLISHED Registration, since the user is not authenticated", 1l, pager.getCount().longValue());
    }

    @Override
    @Test
    public void createTestDataSet() throws FileNotFoundException {

        User user1 = User.NewInstance("user1", "00000");
        User user2 = User.NewInstance("user2", "00000");
        // granted authorities are irrelevant for the test but are required to allow creating the test data
        user1.getAuthorities().add(Role.ROLE_ADMIN);
        user2.getAuthorities().add(Role.ROLE_ADMIN);
        user1.setUuid(USER1_UUID);
        user2.setUuid(USER2_UUID);
        repo.getUserService().save(user1);
        repo.getUserService().save(user2);

        Reference book1 = ReferenceFactory.newBook();
        book1.setTitle("book1");
        Reference book2 = ReferenceFactory.newBook();
        book1.setTitle("book2");
        repo.getReferenceService().save(book1);
        repo.getReferenceService().save(book2);

        TaxonName species1 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES(), "Digilalus", null, "primus", null, null, book1, "11", null);
        TaxonName species2 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES(), "Digilalus", null, "secundus", null, null, book1, "22", null);
        TaxonName genus = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS(), "Digilalus", null, null, null, null, book1, "33", null);
        repo.getNameService().save(Arrays.asList(species1, species2, genus));

        SpecimenTypeDesignation std1 = SpecimenTypeDesignation.NewInstance();
        std1.setCitation(book1);
        std1.setUuid(STD1_UUID);
        std1.setTypeStatus(SpecimenTypeDesignationStatus.HOLOTYPE());

        SpecimenTypeDesignation std2 = SpecimenTypeDesignation.NewInstance();
        std2.setCitation(book2);
        std2.setUuid(STD2_UUID);
        std2.setTypeStatus(SpecimenTypeDesignationStatus.EPITYPE());

        NameTypeDesignation ntd1 = NameTypeDesignation.NewInstance();
        ntd1.setCitation(book1);
        ntd1.setTypeName(species1);
        ntd1.setUuid(NDT1_UUID);
        ntd1.setTypeStatus(NameTypeDesignationStatus.TAUTONYMY());

        genus.addTypeDesignation(ntd1, false);
        species1.addTypeDesignation(std1, false);
        species2.addTypeDesignation(std2, false);

        repo.getNameService().saveOrUpdate(Arrays.asList(species1, species2, genus));

        repo.authenticate("user1", "00000");
        Registration reg1 = Registration.NewInstance("test/1000", "1000", species1, new HashSet(Arrays.asList(std1))); // --> book1
        reg1.setStatus(RegistrationStatus.PUBLISHED);
        // the authenticated user will be set as submitter in new Registrations
        // see RegistrationServiceImpl.prepareForSave(Registration reg)
        repo.getRegistrationService().save(reg1);

        // the authenticated user will be set as submitter in new Registrations
        // see RegistrationServiceImpl.prepareForSave(Registration reg)
        Registration reg2 = Registration.NewInstance("test/1001", "1001", species2, null); // --> book2
        reg2.setStatus(RegistrationStatus.PREPARATION);
        repo.getRegistrationService().save(reg2);

        repo.authenticate("user2", "00000");
        // the authenticated user will be set as submitter in new Registrations
        // see RegistrationServiceImpl.prepareForSave(Registration reg)
        Registration reg3 = Registration.NewInstance("test/1002", "1002", genus, new HashSet(Arrays.asList(ntd1))); // --> book1
        repo.getRegistrationService().save(reg3);
        reg3.setStatus(RegistrationStatus.CURATION);

        Registration reg4 = Registration.NewInstance("test/1003", "1003", null, new HashSet(Arrays.asList(std2))); // --> book2
        repo.getRegistrationService().save(reg4);
        reg4.setStatus(RegistrationStatus.READY);

        // 2. end the transaction so that all data is actually written to the db
        setComplete();
        endTransaction();

        // use the fileNameAppendix if you are creating a data set file which need to be named differently
        // from the standard name. For example if a single test method needs different data then the other
        // methods the test class you may want to set the fileNameAppendix when creating the data for this method.
        String fileNameAppendix = null;

        // 3.
        writeDbUnitDataSetFile(new String[] {
            "UserAccount",
            "REFERENCE",
            "Registration", "Registration_TypeDesignationBase",
            "TaxonName", "TaxonName_TypeDesignationBase",
            "TypeDesignationBase",
            "AGENTBASE", "HOMOTYPICALGROUP",
            "HIBERNATE_SEQUENCES" // IMPORTANT!!!
            },
            fileNameAppendix, true );
    }
}
