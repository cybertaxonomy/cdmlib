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

import org.junit.Assert;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.RegistrationStatus;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.persistence.dao.name.IRegistrationDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITypeDesignationDao;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 * @since 05.05.2017
 */
public class RegistrationDaoHibernateImplTest extends CdmTransactionalIntegrationTest {

    @SpringBeanByType
    private IRegistrationDao registrationDao;

    @SpringBeanByType
    private IReferenceDao referenceDao;

    @SpringBeanByType
    private ITaxonNameDao nameDao;

    @SpringBeanByType
    private ITypeDesignationDao typeDesignationDao;

    @Test
    public void testListByReference() {

        List<Registration> registrationList;
        List<RegistrationStatus> statusList = new ArrayList<>();
        statusList.add(RegistrationStatus.PUBLISHED);
        Optional<Reference> fullNullReferenceOptional = null;

        //test name with ref
        TaxonName name = TaxonNameFactory.NewBotanicalInstance(null);
        nameDao.save(name);
        Optional<Reference> nomRef = Optional.of(ReferenceFactory.newBook());
        referenceDao.save(nomRef.get());
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
        Reference desigRef = ReferenceFactory.newArticle();
        referenceDao.save(desigRef);
        SpecimenTypeDesignation desig = SpecimenTypeDesignation.NewInstance();
        typeDesignationDao.save(desig);
        desig.setCitation(desigRef);
        regWithType.addTypeDesignation(desig);
        registrationDao.save(regWithType);
        registrationList = registrationDao.list(Optional.of(desigRef), null, null, null, null);
        assertEquals("List should have 1 entry", 1, registrationList.size());
        count = registrationDao.count(Optional.of(desigRef), null);
        assertEquals(1, count);
        registrationList = registrationDao.list( fullNullReferenceOptional, null, null, null,null);
        assertEquals("In total there should be 2 registrations", 2, registrationList.size());

        //test unused ref
        Reference newRef = ReferenceFactory.newArticle();
        registrationList = registrationDao.list(Optional.of(newRef), null, null, null, null);
        assertTrue("List should be empty", registrationList.isEmpty());
        //... and saved
        referenceDao.save(newRef);
        registrationList = registrationDao.list(Optional.of(newRef), null, null, null, null);
        assertTrue("List should be empty", registrationList.isEmpty());
        count = registrationDao.count(Optional.of(newRef), null);
        assertEquals(0, count);

        //test null
        Optional<Reference> nullRef = Optional.empty();
        registrationList = registrationDao.list(nullRef, null, null, null, null);
        assertEquals("List should be empty", 0, registrationList.size());
        count = registrationDao.count( nullRef, null);
        assertEquals(0, count);

        //... name without ref
        Registration regWithUnreferenceName = Registration.NewInstance();
        TaxonName nameWithoutRef = TaxonNameFactory.NewBotanicalInstance(null);
        nameDao.save(nameWithoutRef);
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
        TaxonName additionalName = TaxonNameFactory.NewBotanicalInstance(null);
        nameDao.save(additionalName);
        additionalName.setNomenclaturalReference(desigRef);
        regWithType.setName(additionalName);
        registrationList = registrationDao.list(Optional.of(desigRef), null, null, null,null);
        assertEquals("List should still have 1 entry", 1, registrationList.size());
        assertEquals("",regWithType, registrationList.get(0));
        count = registrationDao.count(Optional.of(desigRef), null);
        assertEquals(1, count);

        //test dirty data (it is sufficient that 1 reference matches, not necessarily all)
        SpecimenTypeDesignation desig2 = SpecimenTypeDesignation.NewInstance();
        typeDesignationDao.save(desig2);
        Reference otherRef = ReferenceFactory.newGeneric();
        referenceDao.save(otherRef);
        desig2.setCitation(otherRef);
        regWithType.addTypeDesignation(desig2);
        registrationDao.saveOrUpdate(regWithType);
        registrationList = registrationDao.list(Optional.of(desigRef), null, null, null,null);
        assertEquals("List should still have 1 entry", 1, registrationList.size());
        assertEquals("",regWithType, registrationList.get(0));
        registrationList = registrationDao.list( Optional.of(otherRef), null, null, null,null);
        assertEquals("Also for otherRef the registration should be found", 1, registrationList.size());
        assertEquals("", regWithType, registrationList.get(0));
        count = registrationDao.count(Optional.of(otherRef), null);
        assertEquals(1, count);
        registrationList = registrationDao.list( fullNullReferenceOptional, null, null, null,null);
        assertEquals("In total there should be 4 registrations", 4, registrationList.size());

        //> 1
        Registration registration2 = Registration.NewInstance();
        TaxonName name2 = TaxonNameFactory.NewBotanicalInstance(null);
        nameDao.save(name2);
        name2.setNomenclaturalReference(desigRef);
        registration2.setName(name2);
        registrationDao.save(registration2);

        Registration registration3 = Registration.NewInstance();
        TaxonName name3 = TaxonNameFactory.NewBotanicalInstance(null);
        nameDao.save(name3);
        name3.setNomenclaturalReference(desigRef);
        registration3.setName(name3);
        registrationDao.save(registration3);

        Registration registration4 = Registration.NewInstance();
        SpecimenTypeDesignation desig4 = SpecimenTypeDesignation.NewInstance();
        typeDesignationDao.save(desig4);
        desig4.setCitation(desigRef);
        registration4.addTypeDesignation(desig4);
        registrationDao.save(registration4);

        registrationList = registrationDao.list( fullNullReferenceOptional, null, null, null,null);
        assertEquals("In total there should be 7 registrations", 7, registrationList.size());

        count = registrationDao.count(Optional.of(desigRef), null);
        assertEquals(4, count);
        registrationList = registrationDao.list(Optional.of(desigRef), null, null, null,null);
        assertEquals("List should have 4 entries", 4, registrationList.size());
        registrationList = registrationDao.list(Optional.of(desigRef), null, 3, null,null);
        assertEquals("List should have 3 entries", 3, registrationList.size());
        registrationList = registrationDao.list(Optional.of(desigRef), null, 3, 2, null);
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

    @Test
    public void testListWithSections() {

        // test nomRef as section
        Reference journal = ReferenceFactory.newJournal();
        Reference section = ReferenceFactory.newSection();
        referenceDao.save(journal, section);
        section.setInReference(journal);

        TaxonName nameInJournal = TaxonNameFactory.NewBotanicalInstance(null);
        nameInJournal.setNomenclaturalReference(journal);
        TaxonName nameInSection = TaxonNameFactory.NewBotanicalInstance(null);
        nameInSection.setNomenclaturalReference(section);
        nameDao.save(nameInJournal, nameInSection);

        SpecimenTypeDesignation desigInJournal = SpecimenTypeDesignation.NewInstance();
        desigInJournal.setCitation(journal);
        SpecimenTypeDesignation desigInSection = SpecimenTypeDesignation.NewInstance();
        desigInSection.setCitation(section);
        typeDesignationDao.save(desigInJournal, desigInSection);

        Registration registration1 = Registration.NewInstance();
        registration1.setName(nameInSection);
        registrationDao.save(registration1);

        Registration registration2 = Registration.NewInstance();
        registration2.setName(nameInJournal);
        registrationDao.save(registration2);

        Registration registration3 = Registration.NewInstance();
        registration3.addTypeDesignation(desigInSection);
        registrationDao.save(registration3);

        Registration registration4 = Registration.NewInstance();
        registration4.addTypeDesignation(desigInJournal);
        registrationDao.save(registration4);

        List<Registration> journalRegistrationList = registrationDao.list( Optional.of(journal), null, null, null,null);
        Assert.assertEquals(4, journalRegistrationList.size());

        List<Registration> sectionRegistrationList = registrationDao.list( Optional.of(section), null, null, null,null);
        Assert.assertEquals(2, sectionRegistrationList.size());
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}