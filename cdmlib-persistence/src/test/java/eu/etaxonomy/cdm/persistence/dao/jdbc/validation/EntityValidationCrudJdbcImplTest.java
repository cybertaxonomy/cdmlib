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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.ExpectedDataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.validation.CRUDEventType;
import eu.etaxonomy.cdm.model.validation.EntityConstraintViolation;
import eu.etaxonomy.cdm.model.validation.EntityValidation;
import eu.etaxonomy.cdm.model.validation.Severity;
import eu.etaxonomy.cdm.persistence.validation.Company;
import eu.etaxonomy.cdm.persistence.validation.Employee;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;
import eu.etaxonomy.cdm.validation.Level2;

/**
 * @author ayco_holleman
 \* @since 20 jan. 2015
 *
 */
@Ignore
public class EntityValidationCrudJdbcImplTest extends CdmIntegrationTest {

    private static final String MEDIA = "eu.etaxonomy.cdm.model.media.Media";
    private static final String SYNONYM = "eu.etaxonomy.cdm.model.taxon.Synonym";
    private static final String GATHERING_EVENT = "eu.etaxonomy.cdm.model.occurrence.GatheringEvent";

    @SpringBeanByType
    private EntityValidationCrudJdbcImpl validationCrudJdbcDao;

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
    @DataSet
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

        EntityValidation result = dao.getEntityValidation(emp.getClass().getName(), emp.getId());
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

    @Test
    @DataSet("EntityValidationCrudJdbcImplTest.testSave.xml")
    @ExpectedDataSet("EntityValidationCrudJdbcImplTest.testSaveAlreadyExistingError-result.xml")
    // Test proving that if an exactly identical
    // EntityConstraintViolation (as per equals() method)
    // is already in database, the only thing that happens
    // is an increment of the validation counter.
    public void testSaveAlreadyExistingError() {

        // All same as in @DataSet:

        DateTime created = new DateTime(2014, 1, 1, 0, 0);

        Employee emp = new Employee();
        emp.setId(100);
        emp.setUuid(UUID.fromString("f8de74c6-aa56-4de3-931e-87b61da0218c"));
        // Other properties not relevant for this test

        EntityValidation entityValidation = EntityValidation.newInstance();
        entityValidation.setValidatedEntity(emp);
        entityValidation.setId(1);
        entityValidation.setUuid(UUID.fromString("dae5b090-30e8-45bc-9460-2eb2028d3c18"));
        entityValidation.setCreated(created);
        entityValidation.setCrudEventType(CRUDEventType.INSERT);
        entityValidation.setValidationCount(5);

        EntityConstraintViolation error = EntityConstraintViolation.newInstance();

        // Actually not same as in @DataSet to force
        // EntityConstraintViolation.equals() method to take
        // other properties into account (e.g. propertyPath,
        // invalidValue, etc.)
        error.setId(Integer.MIN_VALUE);

        error.setCreated(created);
        error.setUuid(UUID.fromString("358da71f-b646-4b79-b00e-dcb68b6425ba"));
        error.setSeverity(Severity.ERROR);
        error.setPropertyPath("firstName");
        error.setInvalidValue("Foo");
        error.setMessage("Garbage In Garbage Out");
        error.setValidationGroup("eu.etaxonomy.cdm.validation.Level2");
        error.setValidator("eu.etaxonomy.cdm.persistence.validation.GarbageValidator");
        Set<EntityConstraintViolation> errors = new HashSet<EntityConstraintViolation>(1);
        errors.add(error);

        entityValidation.addEntityConstraintViolation(error);

        EntityValidationCrudJdbcImpl dao = new EntityValidationCrudJdbcImpl(dataSource);
        dao.saveEntityValidation(entityValidation, new Class[] { Level2.class });
    }

    @Test
    @DataSet("EntityValidationCrudJdbcImplTest.testSave.xml")
    @ExpectedDataSet("EntityValidationCrudJdbcImplTest.testReplaceError-result.xml")
    // Test proving that if an entity has been validated,
    // yielding 1 error (as in @DataSet), and a subsequent
    // validation also yields 1 error, but a different one,
    // then validation count is increased, the old error is
    // removed and the new error is inserted.
    public void testReplaceError() {

        // All identical to @DataSet:

        DateTime created = new DateTime(2014, 1, 1, 0, 0);

        Employee emp = new Employee();
        emp.setId(100);
        emp.setUuid(UUID.fromString("f8de74c6-aa56-4de3-931e-87b61da0218c"));

        EntityValidation entityValidation = EntityValidation.newInstance();
        entityValidation.setValidatedEntity(emp);
        entityValidation.setId(1);
        entityValidation.setUuid(UUID.fromString("dae5b090-30e8-45bc-9460-2eb2028d3c18"));
        entityValidation.setCreated(created);
        entityValidation.setCrudEventType(CRUDEventType.INSERT);
        entityValidation.setValidationCount(5);

        EntityConstraintViolation error = EntityConstraintViolation.newInstance();
        error.setId(38);
        error.setCreated(created);
        error.setUuid(UUID.fromString("358da71f-b646-4b79-b00e-dcb68b6425ba"));
        error.setSeverity(Severity.ERROR);
        error.setPropertyPath("firstName");

        // Except for:
        error.setInvalidValue("Bar");

        error.setMessage("Garbage In Garbage Out");
        error.setValidationGroup("eu.etaxonomy.cdm.validation.Level2");
        error.setValidator("eu.etaxonomy.cdm.persistence.validation.GarbageValidator");
        Set<EntityConstraintViolation> errors = new HashSet<EntityConstraintViolation>(1);
        errors.add(error);

        entityValidation.addEntityConstraintViolation(error);

        EntityValidationCrudJdbcImpl dao = new EntityValidationCrudJdbcImpl(dataSource);
        dao.saveEntityValidation(entityValidation, new Class[] { Level2.class });
    }

    @Test
    @DataSet("EntityValidationCrudJdbcImplTest.testSave.xml")
    @ExpectedDataSet("EntityValidationCrudJdbcImplTest.testSameErrorOtherEntity-result.xml")
    // Test proving that if an entity has been validated,
    // yielding 1 error (as in @DataSet), and _another_
    // entity is now validated yielding an equals() error,
    // things behave as expected (2 entityvalidations, each
    // having 1 entityconstraintviolation)
    public void testSameErrorOtherEntity() {

        DateTime created = new DateTime(2014, 1, 1, 0, 0);

        // Not in @DataSet
        Employee emp = new Employee();
        emp.setId(200);
        emp.setUuid(UUID.fromString("f8de74c6-aa56-4de3-931e-87b61da0218d"));

        EntityValidation entityValidation = EntityValidation.newInstance();
        entityValidation.setValidatedEntity(emp);
        entityValidation.setId(2);
        entityValidation.setUuid(UUID.fromString("dae5b090-30e8-45bc-9460-2eb2028d3c19"));
        entityValidation.setCreated(created);
        entityValidation.setCrudEventType(CRUDEventType.INSERT);
        entityValidation.setValidationCount(1);

        // equals() error in @DataSet
        EntityConstraintViolation error = EntityConstraintViolation.newInstance();
        error.setId(2);
        error.setCreated(created);
        error.setUuid(UUID.fromString("358da71f-b646-4b79-b00e-dcb68b6425bb"));
        error.setSeverity(Severity.ERROR);
        error.setPropertyPath("firstName");
        error.setInvalidValue("Foo");

        error.setMessage("Garbage In Garbage Out");
        error.setValidationGroup("eu.etaxonomy.cdm.validation.Level2");
        error.setValidator("eu.etaxonomy.cdm.persistence.validation.GarbageValidator");
        Set<EntityConstraintViolation> errors = new HashSet<EntityConstraintViolation>(1);
        errors.add(error);

        entityValidation.addEntityConstraintViolation(error);

        EntityValidationCrudJdbcImpl dao = new EntityValidationCrudJdbcImpl(dataSource);
        dao.saveEntityValidation(entityValidation, new Class[] { Level2.class });
    }
    @Test
    @DataSet("EntityValidationCrudJdbcImplTest.testSave.xml")
    @ExpectedDataSet("EntityValidationCrudJdbcImplTest.testOneOldOneNewError-result.xml")
    // Test proving that if an entity has been validated,
    // yielding 1 error (as in @DataSet), and _another_
    // entity is now validated yielding an equals() error,
    // things behave as expected (2 entityvalidations, each
    // having 1 entityconstraintviolation)
    public void testOneOldOneNewError() {

        DateTime created = new DateTime(2014, 1, 1, 0, 0);

        // Same entity as in @DataSet
        Employee emp = new Employee();
        emp.setId(100);
        emp.setUuid(UUID.fromString("f8de74c6-aa56-4de3-931e-87b61da0218c"));
        // Other properties not relevant for this test

        EntityValidation entityValidation = EntityValidation.newInstance();
        entityValidation.setValidatedEntity(emp);
        entityValidation.setId(1);
        entityValidation.setUuid(UUID.fromString("dae5b090-30e8-45bc-9460-2eb2028d3c18"));
        entityValidation.setCreated(created);
        entityValidation.setCrudEventType(CRUDEventType.INSERT);


        // Old error (in @DataSet)
        EntityConstraintViolation error = EntityConstraintViolation.newInstance();
        error.setId(Integer.MIN_VALUE);
        error.setCreated(created);
        error.setUuid(UUID.fromString("358da71f-b646-4b79-b00e-dcb68b6425ba"));
        error.setSeverity(Severity.ERROR);
        error.setPropertyPath("firstName");
        error.setInvalidValue("Foo");
        error.setMessage("Garbage In Garbage Out");
        error.setValidationGroup("eu.etaxonomy.cdm.validation.Level2");
        error.setValidator("eu.etaxonomy.cdm.persistence.validation.GarbageValidator");
        entityValidation.addEntityConstraintViolation(error);

        // New error (not in @DataSet)
        error = EntityConstraintViolation.newInstance();
        // Don't leave ID generation to chance; we want it to be same as in
        // @ExpectedDataSet
        error.setId(2);
        error.setCreated(created);
        error.setUuid(UUID.fromString("358da71f-b646-4b79-b00e-dcb68b6425bb"));
        error.setSeverity(Severity.ERROR);
        error.setPropertyPath("lastName");
        error.setInvalidValue("Bar");
        error.setMessage("Garbage In Garbage Out");
        error.setValidationGroup("eu.etaxonomy.cdm.validation.Level2");
        error.setValidator("eu.etaxonomy.cdm.persistence.validation.LastNameValidator");
        entityValidation.addEntityConstraintViolation(error);

//        EntityValidationCrudJdbcImpl dao = new EntityValidationCrudJdbcImpl(dataSource);
        validationCrudJdbcDao.saveEntityValidation(entityValidation, new Class[] { Level2.class });
    }



    @Test
    @DataSet("EntityValidationCrudJdbcImplTest.testSave.xml")
    @ExpectedDataSet("EntityValidationCrudJdbcImplTest.testAllErrorsSolved-result.xml")
    // Test proving that if an entity has been validated,
    // yielding 1 error (as in @DataSet), and a subsequent
    // validation yields 0 errors, all that remains is an
    // EntityValidation record with its validation counter
    // increased.
    public void testAllErrorsSolved() {

        DateTime created = new DateTime(2014, 1, 1, 0, 0);

        Employee emp = new Employee();
        emp.setId(100);
        emp.setUuid(UUID.fromString("f8de74c6-aa56-4de3-931e-87b61da0218c"));

        EntityValidation entityValidation = EntityValidation.newInstance();
        entityValidation.setValidatedEntity(emp);
        entityValidation.setId(1);
        entityValidation.setUuid(UUID.fromString("dae5b090-30e8-45bc-9460-2eb2028d3c18"));
        entityValidation.setCreated(created);
        entityValidation.setCrudEventType(CRUDEventType.INSERT);
        entityValidation.setValidationCount(5);

        EntityValidationCrudJdbcImpl dao = new EntityValidationCrudJdbcImpl(dataSource);
        dao.saveEntityValidation(entityValidation, new Class[] { Level2.class });
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.persistence.dao.jdbc.validation.EntityValidationCrudJdbcImpl#deleteEntityValidation (java.lang.String, int)}
     * .
     */
    @Test
    @DataSet
    @ExpectedDataSet
    public void test_DeleteValidationResult() {
        EntityValidationCrudJdbcImpl dao = new EntityValidationCrudJdbcImpl(dataSource);
        dao.deleteEntityValidation(SYNONYM, 200);
        EntityValidation result = dao.getEntityValidation(SYNONYM, 200);
        assertTrue(result == null);
    }

    @Test
    @DataSet
    public void testGetEntityValidation() {
        EntityValidationCrudJdbcImpl dao = new EntityValidationCrudJdbcImpl(dataSource);
        EntityValidation result;

        result = dao.getEntityValidation(MEDIA, 100);
        assertNotNull("A validation result for media id=100 should exist", result);
        assertEquals("Unexpected entity id", 1, result.getId());
        assertEquals("Unexpected number of constraint violations", 1, result.getEntityConstraintViolations().size());

        result = dao.getEntityValidation(SYNONYM, 200);
        assertNotNull(result);
        assertEquals("Unexpected entity id", 2, result.getId());
        assertEquals("Unexpected number of constraint violations", 2, result.getEntityConstraintViolations().size());

        result = dao.getEntityValidation(GATHERING_EVENT, 300);
        assertNotNull(result);
        assertEquals("Unexpected entity id", 3, result.getId());
        assertEquals("Unexpected number of constraint violations", 3, result.getEntityConstraintViolations().size());

        result = dao.getEntityValidation(GATHERING_EVENT, 301);
        assertNotNull(result);
        assertEquals("Unexpected entity id", 4, result.getId());
        assertEquals("Unexpected number of constraint violations", 1, result.getEntityConstraintViolations().size());

        // Test we get a null back
        result = dao.getEntityValidation("Foo Bar", 100);
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
