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

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.UUID;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.api.service.dto.RegistrationDTO;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.registration.IRegistrationWorkingSetService;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.RegistrationStatus;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTestWithSecurity;

/**
 * @author a.kohlbecker
 * @since Jun 28, 2019
 *
 */
public class RegistrationWorkingSetServiceTest extends CdmTransactionalIntegrationTestWithSecurity {


    public static final UUID USER2_UUID = UUID.fromString("669f582c-e97f-425b-97f6-bc3b0c08f2a5");

    public static final UUID USER1_UUID = UUID.fromString("68033f81-9947-4b61-b33b-3d05bd438579");

    public static final UUID NDT1_UUID = UUID.fromString("be66964a-ea2b-480e-9dcf-0ee1dd7313eb");

    public static final UUID STD2_UUID = UUID.fromString("8cd056fb-259a-45aa-ab4f-b34033eef2e9");

    public static final UUID STD1_UUID = UUID.fromString("1c29e80a-2611-4be4-9b2f-15bbd15066bf");

    @SpringBeanByType
    @Qualifier("CdmRepository")
    protected ICdmRepository repo;

    @SpringBeanByType
    private IRegistrationWorkingSetService service;


    @Test
    @DataSet("RegistrationServiceTest.xml")
    public void testPageDTOs(){

        OrderHint orderBySpecificIdentifier = new OrderHint("specificIdentifier", SortOrder.ASCENDING);
        OrderHint orderById = new OrderHint("id", SortOrder.ASCENDING);

        repo.authenticate("user1", "00000");

        Pager<RegistrationDTO> pager = service.pageDTOs((UUID)null, null, null, null, null, null, null, null);
        assertEquals(pager.getRecords().size(), pager.getCount().intValue());
        assertEquals("with authenticated user expecting all 3 Registrations", 3l, pager.getCount().longValue());

        pager = service.pageDTOs(USER1_UUID, null, null, null, null, null, null, null);
        assertEquals(pager.getRecords().size(), pager.getCount().intValue());
        assertEquals(2l, pager.getCount().longValue());

        pager = service.pageDTOs(USER2_UUID, null, null, null, null, null, null, null);
        assertEquals(pager.getRecords().size(), pager.getCount().intValue());
        assertEquals(1l, pager.getCount().longValue());

        // status filter
        pager = service.pageDTOs((UUID)null, Arrays.asList(RegistrationStatus.PREPARATION), null, null, null, null, null, null);
        assertEquals(pager.getRecords().size(), pager.getCount().intValue());
        assertEquals(1l, pager.getCount().longValue());
        assertEquals(RegistrationStatus.PREPARATION, pager.getRecords().get(0).getStatus());

        pager = service.pageDTOs((UUID)null, Arrays.asList(RegistrationStatus.PREPARATION, RegistrationStatus.PUBLISHED), null, null, null, null, null, null);
        assertEquals(pager.getRecords().size(), pager.getCount().intValue());
        assertEquals(2l, pager.getCount().longValue());

        // status filter with submitter
        pager = service.pageDTOs(USER1_UUID, Arrays.asList(RegistrationStatus.PREPARATION, RegistrationStatus.PUBLISHED), null, null, null, null, null, null);
        assertEquals(pager.getRecords().size(), pager.getCount().intValue());
        assertEquals(2l, pager.getCount().longValue());

        pager = service.pageDTOs(USER2_UUID, Arrays.asList(RegistrationStatus.PREPARATION, RegistrationStatus.PUBLISHED), null, null, null, null, null, null);
        assertEquals(pager.getRecords().size(), pager.getCount().intValue());
        assertEquals(0l, pager.getCount().longValue());

        pager = service.pageDTOs(USER2_UUID, Arrays.asList(RegistrationStatus.CURATION), null, null, null, null, null, null);
        assertEquals(pager.getRecords().size(), pager.getCount().intValue());
        assertEquals(1l, pager.getCount().longValue());

        // identifier filter
        pager = service.pageDTOs((UUID)null, null, "100", null, null, null, null, null);
        assertEquals(pager.getRecords().size(), pager.getCount().intValue());
        assertEquals(3l, pager.getCount().longValue());

        pager = service.pageDTOs((UUID)null, null, "test/1001", null, null, null, null, null);
        assertEquals(pager.getRecords().size(), pager.getCount().intValue());
        assertEquals(1l, pager.getCount().longValue());

        // identifier filter with submitter
        pager = service.pageDTOs(USER1_UUID, null, "100", null, null, null, null, null);
        assertEquals(pager.getRecords().size(), pager.getCount().intValue());
        assertEquals(2l, pager.getCount().longValue());

        pager = service.pageDTOs(USER2_UUID, null, "1002", null, null, null, null, null);
        assertEquals(pager.getRecords().size(), pager.getCount().intValue());
        assertEquals(1l, pager.getCount().longValue());

        // taxon name filter
        pager = service.pageDTOs((UUID)null, null, null, "Digilalus", null, null, null, null);
        assertEquals(pager.getRecords().size(), pager.getCount().intValue());
        assertEquals(3l, pager.getCount().longValue());


        pager = service.pageDTOs((UUID)null, null, null, "Digilalus prim", null, null, null, null);
        assertEquals(pager.getRecords().size(), pager.getCount().intValue());
        assertEquals(1l, pager.getCount().longValue());

        // taxon name filter with user
        pager = service.pageDTOs(USER2_UUID, null, null, "Digilalus", null, null, null, null);
        assertEquals(pager.getRecords().size(), pager.getCount().intValue());
        assertEquals(1l, pager.getCount().longValue());

        // taxon name filter with user and status
        pager = service.pageDTOs(USER1_UUID, Arrays.asList(RegistrationStatus.PREPARATION), null, "Digilalus", null, null, null, null);
        assertEquals(pager.getRecords().size(), pager.getCount().intValue());
        assertEquals(1l, pager.getCount().longValue());

        pager = service.pageDTOs(USER1_UUID, Arrays.asList(RegistrationStatus.PREPARATION, RegistrationStatus.PUBLISHED), "1001", "Digilalus", null, null, null, null);
        assertEquals(pager.getRecords().size(), pager.getCount().intValue());
        assertEquals(1l, pager.getCount().longValue());

        // type designation status

        pager = service.pageDTOs((UUID)null, null, null, null, Arrays.asList(SpecimenTypeDesignationStatus.HOLOTYPE().getUuid()),
                null, null, Arrays.asList(orderById));
        assertEquals(pager.getRecords().size(), pager.getCount().intValue());
        assertEquals(1l, pager.getCount().longValue());
        assertEquals(STD1_UUID, pager.getRecords().get(0).registration().getTypeDesignations().iterator().next().getUuid());

        pager = service.pageDTOs((UUID)null, null, null, null, Arrays.asList(NameTypeDesignationStatus.TAUTONYMY().getUuid()),
                null, null, Arrays.asList(orderById));
        assertEquals(pager.getRecords().size(), pager.getCount().intValue());
        assertEquals(1l, pager.getCount().longValue());
        assertEquals(NDT1_UUID, pager.getRecords().get(1).registration().getTypeDesignations().iterator().next().getUuid());

        pager = service.pageDTOs((UUID)null, null, null, null, Arrays.asList(SpecimenTypeDesignationStatus.HOLOTYPE().getUuid(), NameTypeDesignationStatus.TAUTONYMY().getUuid()),
                null, null, Arrays.asList(orderById));
        assertEquals(pager.getRecords().size(), pager.getCount().intValue());
        assertEquals(2l, pager.getCount().longValue());
        assertEquals(STD1_UUID, pager.getRecords().get(0).registration().getTypeDesignations().iterator().next().getUuid());
        assertEquals(NDT1_UUID, pager.getRecords().get(1).registration().getTypeDesignations().iterator().next().getUuid());

        // type designation status with user
        pager = service.pageDTOs(USER1_UUID, null, null, null, Arrays.asList(SpecimenTypeDesignationStatus.HOLOTYPE().getUuid(), NameTypeDesignationStatus.TAUTONYMY().getUuid()),
                null, null, Arrays.asList(orderById));
        assertEquals(pager.getRecords().size(), pager.getCount().intValue());
        assertEquals(1l, pager.getCount().longValue());
        assertEquals(STD1_UUID, pager.getRecords().get(0).registration().getTypeDesignations().iterator().next().getUuid());

        // type designation status with name
        pager = service.pageDTOs((UUID)null, null, null, "Digital", Arrays.asList(SpecimenTypeDesignationStatus.HOLOTYPE().getUuid(), NameTypeDesignationStatus.TAUTONYMY().getUuid()),
                null, null, Arrays.asList(orderById));
        assertEquals(pager.getRecords().size(), pager.getCount().intValue());
        assertEquals(2l, pager.getCount().longValue());
        assertEquals(STD1_UUID, pager.getRecords().get(0).registration().getTypeDesignations().iterator().next().getUuid());
        assertEquals(NDT1_UUID, pager.getRecords().get(1).registration().getTypeDesignations().iterator().next().getUuid());

    }

    @Test
    @DataSet("RegistrationServiceTest.xml")
    public void testPageDTOs_unautheticated(){
        Pager<RegistrationDTO> pager = service.pageDTOs((UUID)null, null, null, null, null, null, null, null);
        assertEquals(pager.getRecords().size(), pager.getCount().intValue());
        assertEquals("expecting only the PUBLISHED Registration, since the user is not authenticated", 1l, pager.getCount().longValue());
    }



    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // Reusing RegistrationServiceTest.xml
    }

}
