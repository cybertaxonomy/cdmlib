/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.name;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.RegistrationStatus;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.persistence.dao.name.IRegistrationDao;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 * @date 05.05.2017
 *
 */
public class RegistrationDaoHibernateImplTest  extends CdmTransactionalIntegrationTest {

    @SpringBeanByType
    IRegistrationDao registrationDao;

    @SpringBeanByType
    IReferenceDao referenceDao;


    @Before
    public void setUp() {

    }

    @Test
    public void testListByReference() {
        List<Registration> registrationList;
        List<RegistrationStatus> statusList = new ArrayList<>();
        statusList.add(RegistrationStatus.PUBLISHED);
        Optional<Reference> fullNullReferenceOptional = null;

        //test name with ref
        TaxonNameBase<?,?> name = TaxonNameFactory.NewBotanicalInstance(null);
        Optional<Reference> nomRef = Optional.of(ReferenceFactory.newBook());
        name.setNomenclaturalReference(nomRef.get());
        Registration registration = Registration.NewInstance();
        registration.setName(name);
        registrationDao.save(registration);
        registrationList = registrationDao.list( nomRef, null, null, null,null);
        assertEquals("List should have 1 entry", 1, registrationList.size());
        long count = registrationDao.count( nomRef, null);
        assertEquals(1, count);
        // ... with status
        registrationList = registrationDao.list( nomRef, statusList, null, null,null);
        assertTrue("List should be empty", registrationList.isEmpty());
        registration.setStatus(RegistrationStatus.PUBLISHED);
        registrationList = registrationDao.list( nomRef, statusList, null, null,null);
        assertEquals("List should have 1 entry", 1, registrationList.size());
        registrationList = registrationDao.list( fullNullReferenceOptional, null, null, null,null);
        assertEquals("In total there should be 1 registrations", 1, registrationList.size());


        //test type designation with ref
        Registration regWithType = Registration.NewInstance();
        Optional<Reference> desigRef = Optional.of(ReferenceFactory.newArticle());
        SpecimenTypeDesignation desig = SpecimenTypeDesignation.NewInstance();
        desig.setCitation(desigRef.get());
        regWithType.addTypeDesignation(desig);
        registrationDao.save(regWithType);
        registrationList = registrationDao.list(desigRef, null, null, null, null);
        assertEquals("List should have 1 entry", 1, registrationList.size());
        count = registrationDao.count( desigRef, null);
        assertEquals(1, count);
        registrationList = registrationDao.list( fullNullReferenceOptional, null, null, null,null);
        assertEquals("In total there should be 2 registrations", 2, registrationList.size());

        //test unused ref
        Optional<Reference> newRef = Optional.of(ReferenceFactory.newArticle());
        registrationList = registrationDao.list(newRef, null, null, null, null);
        assertTrue("List should be empty", registrationList.isEmpty());
        //... and saved
        referenceDao.save(newRef.get());
        registrationList = registrationDao.list(newRef, null, null, null, null);
        assertTrue("List should be empty", registrationList.isEmpty());
        count = registrationDao.count( newRef, null);
        assertEquals(0, count);


        //test null
        Optional<Reference> nullRef = Optional.empty();
        registrationList = registrationDao.list(nullRef, null, null, null, null);
        assertEquals("List should be empty", 0, registrationList.size());
        count = registrationDao.count( nullRef, null);
        assertEquals(0, count);

        //... name without ref
        Registration regWithUnreferenceName = Registration.NewInstance();
        TaxonNameBase<?,?> nameWithoutRef = TaxonNameFactory.NewBotanicalInstance(null);
        regWithUnreferenceName.setName(nameWithoutRef);
        registrationDao.save(regWithUnreferenceName);
        registrationList = registrationDao.list(nullRef, null, null, null, null);
        assertEquals("List should have 1 entry", 1, registrationList.size());
        count = registrationDao.count( nullRef, null);
        assertEquals(1, count);
        registrationList = registrationDao.list( fullNullReferenceOptional, null, null, null,null);
        assertEquals("In total there should be 3 registrations", 3, registrationList.size());


        //... registration without name and type designation
        Registration emptyRegistration = Registration.NewInstance();
        registrationDao.save(emptyRegistration);
        registrationList = registrationDao.list(nullRef, null, null, null, null);
        assertEquals("List should have 2 entries now", 2, registrationList.size());
        count = registrationDao.count( nullRef, null);
        assertEquals(2, count);
        registrationList = registrationDao.list( fullNullReferenceOptional, null, null, null,null);
        assertEquals("In total there should be 4 registrations", 4, registrationList.size());


        //test name and type designation with same ref
        TaxonNameBase<?,?> additionalName = TaxonNameFactory.NewBotanicalInstance(null);
        additionalName.setNomenclaturalReference(desigRef.get());
        regWithType.setName(additionalName);
        registrationList = registrationDao.list(desigRef, null, null, null,null);
        assertEquals("List should still have 1 entry", 1, registrationList.size());
        assertEquals("",regWithType, registrationList.get(0));
        count = registrationDao.count( desigRef, null);
        assertEquals(1, count);

        //test dirty data (it is sufficient that 1 reference matches, not necessarily all)
        SpecimenTypeDesignation desig2 = SpecimenTypeDesignation.NewInstance();
        Optional<Reference> otherRef = Optional.of(ReferenceFactory.newGeneric());
        desig2.setCitation(otherRef.get());
        regWithType.addTypeDesignation(desig2);
        registrationDao.saveOrUpdate(regWithType);
        registrationList = registrationDao.list( desigRef, null, null, null,null);
        assertEquals("List should still have 1 entry", 1, registrationList.size());
        assertEquals("",regWithType, registrationList.get(0));
        registrationList = registrationDao.list( otherRef, null, null, null,null);
        assertEquals("Also for otherRef the registration should be found", 1, registrationList.size());
        assertEquals("", regWithType, registrationList.get(0));
        count = registrationDao.count( otherRef, null);
        assertEquals(1, count);
        registrationList = registrationDao.list( fullNullReferenceOptional, null, null, null,null);
        assertEquals("In total there should be 4 registrations", 4, registrationList.size());

        //> 1
        Registration registration2 = Registration.NewInstance();
        TaxonNameBase<?,?> name2 = TaxonNameFactory.NewBotanicalInstance(null);
        name2.setNomenclaturalReference(desigRef.get());
        registration2.setName(name2);
        registrationDao.save(registration2);

        Registration registration3 = Registration.NewInstance();
        TaxonNameBase<?,?> name3 = TaxonNameFactory.NewBotanicalInstance(null);
        name3.setNomenclaturalReference(desigRef.get());
        registration3.setName(name3);
        registrationDao.save(registration3);

        Registration registration4 = Registration.NewInstance();
        SpecimenTypeDesignation desig4 = SpecimenTypeDesignation.NewInstance();
        desig4.setCitation(desigRef.get());
        registration4.addTypeDesignation(desig4);
        registrationDao.save(registration4);

        registrationList = registrationDao.list( fullNullReferenceOptional, null, null, null,null);
        assertEquals("In total there should be 7 registrations", 7, registrationList.size());

        count = registrationDao.count( desigRef, null);
        assertEquals(4, count);
        registrationList = registrationDao.list( desigRef, null, null, null,null);
        assertEquals("List should have 4 entries", 4, registrationList.size());
        registrationList = registrationDao.list( desigRef, null, 3, null,null);
        assertEquals("List should have 3 entries", 3, registrationList.size());
        registrationList = registrationDao.list( desigRef, null, 3, 2,null);
        assertEquals("List should have 2 entries", 2, registrationList.size());

        registrationList = registrationDao.list( fullNullReferenceOptional, statusList, null, null,null);
        assertEquals("In total there should be 1 registration with status published", 1, registrationList.size());

        statusList.add(RegistrationStatus.PREPARATION);
        registrationList = registrationDao.list( fullNullReferenceOptional, statusList, null, null,null);
        assertEquals("In total there should be 7 registration with status preparation or published", 7, registrationList.size());
        statusList.remove(RegistrationStatus.PUBLISHED);
        registrationList = registrationDao.list( fullNullReferenceOptional, statusList, null, null,null);
        assertEquals("In total there should be 6 registration with status preparation", 6, registrationList.size());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
