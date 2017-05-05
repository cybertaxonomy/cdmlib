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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.name.Registration;
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

        //test name with ref
        TaxonNameBase<?,?> name = TaxonNameFactory.NewBotanicalInstance(null);
        Reference nomRef = ReferenceFactory.newBook();
        name.setNomenclaturalReference(nomRef);
        Registration registration = Registration.NewInstance();
        registration.setName(name);
        registrationDao.save(registration);
        registrationList = registrationDao.list( nomRef, null, null,null);
        assertFalse("List should not be empty", registrationList.isEmpty());

        //test type designation with ref
        Registration regWithType = Registration.NewInstance();
        Reference desigRef = ReferenceFactory.newArticle();
        SpecimenTypeDesignation desig = SpecimenTypeDesignation.NewInstance();
        desig.setCitation(desigRef);
        regWithType.addTypeDesignation(desig);
        registrationDao.save(regWithType);
        registrationList = registrationDao.list(desigRef, null, null, null);
        assertEquals("List should have 1 entry", 1, registrationList.size());

        //test unused ref
        Reference newRef = ReferenceFactory.newArticle();
        registrationList = registrationDao.list(newRef, null, null, null);
        assertTrue("List should be empty", registrationList.isEmpty());
        //... and saved
        referenceDao.save(newRef);
        registrationList = registrationDao.list(newRef, null, null, null);
        assertTrue("List should be empty", registrationList.isEmpty());


        //test null
        Reference nullRef = null;
        registrationList = registrationDao.list(nullRef, null, null, null);
        assertEquals("List should be empty", 0, registrationList.size());

        //... name without ref
        Registration regWithUnreferenceName = Registration.NewInstance();
        TaxonNameBase<?,?> nameWithoutRef = TaxonNameFactory.NewBotanicalInstance(null);
        regWithUnreferenceName.setName(nameWithoutRef);
        registrationDao.save(regWithUnreferenceName);
        registrationList = registrationDao.list(nullRef, null, null, null);
        assertEquals("List should have 1 entry", 1, registrationList.size());

        //... registration without name and type designation
        Registration emptyRegistration = Registration.NewInstance();
        registrationDao.save(emptyRegistration);
        registrationList = registrationDao.list(nullRef, null, null, null);
        assertEquals("List should have 2 entries now", 2, registrationList.size());


        //test name and type designation with same ref
        TaxonNameBase<?,?> additionalName = TaxonNameFactory.NewBotanicalInstance(null);
        additionalName.setNomenclaturalReference(desigRef);
        regWithType.setName(additionalName);
        registrationList = registrationDao.list( desigRef, null, null,null);
        assertEquals("List should still have 1 entry", 1, registrationList.size());
        assertEquals("",regWithType, registrationList.get(0));

        //test dirty data (it is sufficient that 1 reference matches, not necessarily all)
        SpecimenTypeDesignation desig2 = SpecimenTypeDesignation.NewInstance();
        Reference otherRef = ReferenceFactory.newGeneric();
        desig2.setCitation(otherRef);
        regWithType.addTypeDesignation(desig2);
        registrationDao.saveOrUpdate(regWithType);
        registrationList = registrationDao.list( desigRef, null, null,null);
        assertEquals("List should still have 1 entry", 1, registrationList.size());
        assertEquals("",regWithType, registrationList.get(0));
        registrationList = registrationDao.list( otherRef, null, null,null);
        assertEquals("Also for otherRef the registration should be found", 1, registrationList.size());
        assertEquals("",regWithType, registrationList.get(0));

        //> 1
        Registration registration2 = Registration.NewInstance();
        TaxonNameBase<?,?> name2 = TaxonNameFactory.NewBotanicalInstance(null);
        name2.setNomenclaturalReference(desigRef);
        registration2.setName(name2);
        registrationDao.save(registration2);

        Registration registration3 = Registration.NewInstance();
        TaxonNameBase<?,?> name3 = TaxonNameFactory.NewBotanicalInstance(null);
        name3.setNomenclaturalReference(desigRef);
        registration3.setName(name3);
        registrationDao.save(registration3);

        Registration registration4 = Registration.NewInstance();
        SpecimenTypeDesignation desig4 = SpecimenTypeDesignation.NewInstance();
        desig4.setCitation(desigRef);
        registration4.addTypeDesignation(desig4);
        registrationDao.save(registration4);

        registrationList = registrationDao.list( desigRef, null, null,null);
        assertEquals("List should have 4 entries", 4, registrationList.size());
        registrationList = registrationDao.list( desigRef, 3, null,null);
        assertEquals("List should have 3 entries", 3, registrationList.size());
        registrationList = registrationDao.list( desigRef, 3, 2,null);
        assertEquals("List should have 2 entries", 2, registrationList.size());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
