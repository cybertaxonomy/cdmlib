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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.model.validation.CRUDEventType;
import eu.etaxonomy.cdm.model.validation.EntityConstraintViolation;
import eu.etaxonomy.cdm.model.validation.EntityValidationResult;
import eu.etaxonomy.cdm.persistence.validation.Company;
import eu.etaxonomy.cdm.persistence.validation.Employee;
import eu.etaxonomy.cdm.validation.Level2;

/**
 * @author ayco_holleman
 * @date 20 jan. 2015
 *
 */
public class EntityValidationResultCrudJdbcImplTest {

    CdmDataSource datasource;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        datasource = CdmDataSource.NewMySqlInstance("localhost", "cdm", "root", null);
        if (!datasource.testConnection()) {
            throw new Exception("Could not connect to datasource");
        }

    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        if (datasource != null) {
            datasource.closeOpenConnections();
        }
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.persistence.dao.jdbc.validation.EntityValidationResultCrudJdbcImpl#EntityValidationResultCrudJdbcImpl()}
     * .
     */
    @Test
    public void test_EntityValidationResultCrudJdbcImpl() {
        EntityValidationResultCrudJdbcImpl dao = new EntityValidationResultCrudJdbcImpl();
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.persistence.dao.jdbc.validation.EntityValidationResultCrudJdbcImpl#EntityValidationResultCrudJdbcImpl (eu.etaxonomy.cdm.database.ICdmDataSource)}
     * .
     */
    @Test
    public void test_EntityValidationResultCrudJdbcImplI_CdmDataSource() {
        EntityValidationResultCrudJdbcImpl dao = new EntityValidationResultCrudJdbcImpl(datasource);
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.persistence.dao.jdbc.validation.EntityValidationResultCrudJdbcImpl#saveValidationResult (java.util.Set, eu.etaxonomy.cdm.model.common.CdmBase, eu.etaxonomy.cdm.model.validation.CRUDEventType)}
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
        EntityValidationResultCrudJdbcImpl dao = new EntityValidationResultCrudJdbcImpl(datasource);
        dao.saveValidationResult(errors, emp, CRUDEventType.NONE);

        EntityValidationResult result = dao.getValidationResult(emp);
        assertNotNull(result);
        assertEquals("Unexpected UUID", result.getValidatedEntityUuid(), uuid);
        assertEquals("Unexpected number of constraint violations", 2, result.getEntityConstraintViolations().size());
        Set<EntityConstraintViolation> violations = result.getEntityConstraintViolations();
        List<EntityConstraintViolation> list = new ArrayList<EntityConstraintViolation>(violations);
        Collections.sort(list, new Comparator<EntityConstraintViolation>() {
            @Override
            public int compare(EntityConstraintViolation o1, EntityConstraintViolation o2)
            {
                return o1.getPropertyPath().toString().compareTo(o2.getPropertyPath().toString());
            }
        });
        assertEquals("Unexpected propertypath", list.get(0).getPropertyPath().toString(), "company.name");
        assertEquals("Unexpected propertypath", list.get(1).getPropertyPath().toString(), "firstName");


    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.persistence.dao.jdbc.validation.EntityValidationResultCrudJdbcImpl#deleteValidationResult (java.lang.String, int)}
     * .
     */
    @Test
    public void test_DeleteValidationResult() {
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.persistence.dao.jdbc.validation.EntityValidationResultCrudJdbcImpl#setDatasource (eu.etaxonomy.cdm.database.ICdmDataSource)}
     * .
     */
    @Test
    public void testSetDatasource() {
    }

}
