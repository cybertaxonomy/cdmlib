// $Id$
/**
 * Copyright (C) 2015 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.dao.jdbc.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.ExpectedDataSet;

import eu.etaxonomy.cdm.model.validation.CRUDEventType;
import eu.etaxonomy.cdm.model.validation.EntityConstraintViolation;
import eu.etaxonomy.cdm.model.validation.EntityValidation;
import eu.etaxonomy.cdm.persistence.validation.Company;
import eu.etaxonomy.cdm.persistence.validation.Employee;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;
import eu.etaxonomy.cdm.validation.Level2;

/**
 * @author ayco_holleman
 * @date 20 jan. 2015
 *
 */
@DataSet
public class EntityValidationCrudJdbcImplTest extends CdmIntegrationTest {

    private static final String MEDIA = "eu.etaxonomy.cdm.model.media.Media";
    private static final String SYNONYM_RELATIONSHIP = "eu.etaxonomy.cdm.model.taxon.SynonymRelationship";
    private static final String GATHERING_EVENT = "eu.etaxonomy.cdm.model.occurrence.GatheringEvent";

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }


    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.persistence.dao.jdbc.validation.EntityValidationCrudJdbcImpl#EntityValidationCrudJdbcImpl()}
     * .
     */
    @SuppressWarnings("unused")
    @Test
    public void testEntityValidationCrudJdbcImpl() {
        new EntityValidationCrudJdbcImpl();
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.persistence.dao.jdbc.validation.EntityValidationCrudJdbcImpl#EntityValidationCrudJdbcImpl (eu.etaxonomy.cdm.database.ICdmDataSource)}
     * .
     */
    @SuppressWarnings("unused")
    @Test
    public void test_EntityValidationCrudJdbcImplI_CdmDataSource() {
        new EntityValidationCrudJdbcImpl(dataSource);
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.persistence.dao.jdbc.validation.EntityValidationCrudJdbcImpl#saveEntityValidation (eu.etaxonomy.cdm.model.common.CdmBase, java.util.Set, eu.etaxonomy.cdm.model.validation.CRUDEventType, Class)}
     * .
     */
    @Test
    public void test_SaveValidationResult_Set_T_CRUDEventType() {
        HibernateValidatorConfiguration config = Validation.byProvider(HibernateValidator.class).configure();
        ValidatorFactory factory = config.buildValidatorFactory();

        // This is the bean that is going to be tested
        Employee emp = new Employee();
        emp.setId(1);
        UUID uuid = emp.getUuid();
        // ERROR 1 (should be JOHN)
        emp.setFirstName("john");
        // This is an error (should be SMITH), but it is a Level-3
        // validation error, so the error should be ignored
        emp.setLastName("smith");

        // This is an @Valid bean on the Employee class, so Level-2
        // validation errors on the Company object should also be
        // listed.
        Company comp = new Company();
        // ERROR 2 (should be GOOGLE)
        comp.setName("Google");
        emp.setCompany(comp);

        Set<ConstraintViolation<Employee>> errors = factory.getValidator().validate(emp, Level2.class);
        EntityValidationCrudJdbcImpl dao = new EntityValidationCrudJdbcImpl(dataSource);
        dao.saveEntityValidation(emp, errors, CRUDEventType.NONE, null);

        EntityValidation result = dao.getValidationResult(emp.getClass().getName(), emp.getId());
        assertNotNull(result);
        assertEquals("Unexpected UUID", result.getValidatedEntityUuid(), uuid);
        assertEquals("Unexpected number of constraint violations", 2, result.getEntityConstraintViolations().size());
        Set<EntityConstraintViolation> violations = result.getEntityConstraintViolations();
        List<EntityConstraintViolation> list = new ArrayList<EntityConstraintViolation>(violations);
        Collections.sort(list, new Comparator<EntityConstraintViolation>() {
            @Override
            public int compare(EntityConstraintViolation o1, EntityConstraintViolation o2) {
                return o1.getPropertyPath().toString().compareTo(o2.getPropertyPath().toString());
            }
        });
        assertEquals("Unexpected propertypath", list.get(0).getPropertyPath().toString(), "company.name");
        assertEquals("Unexpected propertypath", list.get(1).getPropertyPath().toString(), "firstName");

    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.persistence.dao.jdbc.validation.EntityValidationCrudJdbcImpl#deleteEntityValidation (java.lang.String, int)}
     * .
     */
    @Test
    @ExpectedDataSet
    public void test_DeleteValidationResult() {
        EntityValidationCrudJdbcImpl dao = new EntityValidationCrudJdbcImpl(dataSource);
        dao.deleteEntityValidation(SYNONYM_RELATIONSHIP, 200);
        EntityValidation result = dao.getValidationResult(SYNONYM_RELATIONSHIP, 200);
        assertTrue(result == null);
    }

    @Test
    public void testGetEntityValidation() {
        EntityValidationCrudJdbcImpl dao = new EntityValidationCrudJdbcImpl(dataSource);
        EntityValidation result;

        result = dao.getValidationResult(MEDIA, 100);
        assertNotNull(result);
        assertEquals("Unexpected entity id", 1, result.getId());
        assertEquals("Unexpected number of constraint violations", 1, result.getEntityConstraintViolations().size());

        result = dao.getValidationResult(SYNONYM_RELATIONSHIP, 200);
        assertNotNull(result);
        assertEquals("Unexpected entity id", 2, result.getId());
        assertEquals("Unexpected number of constraint violations", 2, result.getEntityConstraintViolations().size());

        result = dao.getValidationResult(GATHERING_EVENT, 300);
        assertNotNull(result);
        assertEquals("Unexpected entity id", 3, result.getId());
        assertEquals("Unexpected number of constraint violations", 3, result.getEntityConstraintViolations().size());

        result = dao.getValidationResult(GATHERING_EVENT, 301);
        assertNotNull(result);
        assertEquals("Unexpected entity id", 4, result.getId());
        assertEquals("Unexpected number of constraint violations", 1, result.getEntityConstraintViolations().size());

        // Test we get a null back
        result = dao.getValidationResult("Foo Bar", 100);
        assertNull(result);
    }



    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.persistence.dao.jdbc.validation.EntityValidationCrudJdbcImpl#setDatasource (eu.etaxonomy.cdm.database.ICdmDataSource)}
     * .
     */
    @Test
    public void testSetDatasource() {
        EntityValidationCrudJdbcImpl dao = new EntityValidationCrudJdbcImpl();
        dao.setDatasource(dataSource);
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {
    }

}
