/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.model.validation.CRUDEventType;
import eu.etaxonomy.cdm.model.validation.EntityConstraintViolation;
import eu.etaxonomy.cdm.model.validation.EntityValidation;
import eu.etaxonomy.cdm.model.validation.Severity;
import eu.etaxonomy.cdm.persistence.dao.validation.IEntityValidationDao;
import eu.etaxonomy.cdm.persistence.validation.Company;
import eu.etaxonomy.cdm.persistence.validation.Employee;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmExpectedDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmTransactionalSpringIntegrationTest;
import eu.etaxonomy.cdm.validation.Level2;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

/**
 * @author a.holleman
 * @since 09.04.2014
 */
@CdmDataSet
public class EntityValidationDaoHibernateImplTest extends CdmTransactionalSpringIntegrationTest {

    private static final String MEDIA = "eu.etaxonomy.cdm.model.media.Media";
    private static final String SYNONYM = "eu.etaxonomy.cdm.model.taxon.Synonym";
    private static final String GATHERING_EVENT = "eu.etaxonomy.cdm.model.occurrence.GatheringEvent";

    @Autowired
    private IEntityValidationDao dao;

    @Test
    public void init() {
        assertNotNull(dao, "Expecting an instance of IEntityValidationDao");
    }

    @Test
    public void testSaveValidationResult() {

        HibernateValidatorConfiguration config = Validation.byProvider(HibernateValidator.class).configure();
        ValidatorFactory factory = config.buildValidatorFactory();

        // This is the bean that is going to be tested
        Employee emp = new Employee();
        emp.setId(1);
        UUID uuid = emp.getUuid();
        // ERROR 1 (should be JOHN)
        emp.setGivenName("john");
        // This is an error (should be SMITH), but it is a Level-3
        // validation error, so the error should be ignored
        emp.setFamilyName("smith");

        // This is an @Valid bean on the Employee class, so Level-2
        // validation errors on the Company object should also be
        // listed.
        Company comp = new Company();
        // ERROR 2 (should be GOOGLE)
        comp.setName("Google");
        emp.setCompany(comp);

        Set<ConstraintViolation<Employee>> errors = factory.getValidator().validate(emp, Level2.class);
        dao.saveEntityValidation(emp, errors, CRUDEventType.NONE, null);

        EntityValidation result = dao.getEntityValidation(Employee.class.getName(), 1);
        assertNotNull(result);
        assertEquals(result.getValidatedEntityUuid(), uuid, "Unexpected UUID");
        assertEquals(2, result.getEntityConstraintViolations().size(), "Unexpected number of constraint violations");
        Set<EntityConstraintViolation> violations = result.getEntityConstraintViolations();
        List<EntityConstraintViolation> list = new ArrayList<EntityConstraintViolation>(violations);
        Collections.sort(list, new Comparator<EntityConstraintViolation>() {
            @Override
            public int compare(EntityConstraintViolation o1, EntityConstraintViolation o2) {
                return o1.getPropertyPath().toString().compareTo(o2.getPropertyPath().toString());
            }
        });
        assertEquals(list.get(0).getPropertyPath().toString(), "company.name", "Unexpected propertypath");
        assertEquals(list.get(1).getPropertyPath().toString(), "givenName", "Unexpected propertypath");
    }

    @Test
    @CdmExpectedDataSet
    public void testDeleteValidationResult() {
        dao.deleteEntityValidation(SYNONYM, 200);

        commitAndStartNewTransaction(null);

        List<EntityValidation> results = dao.getEntityValidations(SYNONYM);
        assertEquals(0, results.size(), "Unexpected number of validation results");
    }

    @Test
    public void testGetEntityValidation() {
        EntityValidation result;

        result = dao.getEntityValidation(MEDIA, 100);
        assertNotNull(result);
        assertEquals(1, result.getId(), "Unexpected entity id");
        assertEquals(1, result.getEntityConstraintViolations().size(), "Unexpected number of constraint violations");

        result = dao.getEntityValidation(SYNONYM, 200);
        assertNotNull(result);
        assertEquals(2, result.getId(), "Unexpected entity id");
        assertEquals(2, result.getEntityConstraintViolations().size(), "Unexpected number of constraint violations");

        result = dao.getEntityValidation(GATHERING_EVENT, 300);
        assertNotNull(result);
        assertEquals(3, result.getId(), "Unexpected entity id");
        assertEquals(3, result.getEntityConstraintViolations().size(), "Unexpected number of constraint violations");

        result = dao.getEntityValidation(GATHERING_EVENT, 301);
        assertNotNull(result);
        assertEquals(4, result.getId(), "Unexpected entity id");
        assertEquals(1, result.getEntityConstraintViolations().size(), "Unexpected number of constraint violations");

        // Test we get a null back
        result = dao.getEntityValidation("Foo Bar", 100);
        assertNull(result);
    }

    @Test
    public void testGetEntityValidations() {
    	List<EntityValidation> results = dao.getEntityValidations();
    	assertNotNull(results);
    	assertEquals(4, results.size(), "Unexpected number of entity validations");
    	int errors = 0;
    	for(EntityValidation ev : results) {
    		errors += ev.getEntityConstraintViolations().size();
    	}
    	assertEquals(7, errors, "Unexpected number of errors");
    }

    @Test
    public void testGetEntityValidations_String() {
        List<EntityValidation> results;

        results = dao.getEntityValidations(MEDIA);
        assertEquals(1, results.size(), "Unexpected number of validation results");
        assertEquals(1, results.get(0).getEntityConstraintViolations().size(), "Unexpected number of error");

        results = dao.getEntityValidations(SYNONYM);
        assertEquals(1, results.size(), "Unexpected number of validation results");
        assertEquals(2, results.get(0).getEntityConstraintViolations().size(), "Unexpected number of error");

        results = dao.getEntityValidations(GATHERING_EVENT);
        assertEquals(2, results.size(), "Unexpected number of validation results");
        assertEquals(3, results.get(0).getEntityConstraintViolations().size(), "Unexpected number of error");
        assertEquals(1, results.get(1).getEntityConstraintViolations().size(), "Unexpected number of error");

        results = dao.getEntityValidations("foo.bar");
        assertEquals(0, results.size(), "Unexpected number of validation results");
    }

    @Test
    public void testGetEntitiesViolatingConstraint_String() {
        List<EntityValidation> results;

        results = dao.getEntitiesViolatingConstraint("com.example.NameValidator");
        assertEquals(1, results.size(), "Unexpected number of validation results");

        results = dao.getEntitiesViolatingConstraint("com.example.DistanceToGroundValidator");
        assertEquals(1, results.size(), "Unexpected number of validation results");

        results = dao.getEntitiesViolatingConstraint("com.example.CountryValidator");
        assertEquals(2, results.size(), "Unexpected number of validation results");

        results = dao.getEntitiesViolatingConstraint("foo.bar");
        assertEquals(0, results.size(), "Unexpected number of validation results");
    }

    @Test
    public void testGetEntityValidations_String_Severity() {
        List<EntityValidation> results;

        results = dao.getEntityValidations(MEDIA, Severity.NOTICE);
        assertEquals(0, results.size(), "Unexpected number of validation results");
        results = dao.getEntityValidations(MEDIA, Severity.WARNING);
        assertEquals(0, results.size(), "Unexpected number of validation results");
        results = dao.getEntityValidations(MEDIA, Severity.ERROR);
        assertEquals(1, results.size(), "Unexpected number of validation results");
        assertEquals(1, results.iterator().next()
                .getEntityConstraintViolations().size(), "Unexpected number of validation results");
        assertEquals(Severity.ERROR, results.iterator().next().getEntityConstraintViolations()
                .iterator().next().getSeverity(), "Unexpected severity");

        results = dao.getEntityValidations(SYNONYM, Severity.NOTICE);
        assertEquals(0, results.size(), "Unexpected number of validation results");
        results = dao.getEntityValidations(SYNONYM, Severity.WARNING);
        assertEquals(1, results.size(), "Unexpected number of validation results");
        results = dao.getEntityValidations(SYNONYM, Severity.ERROR);
        assertEquals(1, results.size(), "Unexpected number of validation results");

        results = dao.getEntityValidations(GATHERING_EVENT, Severity.NOTICE);
        assertEquals(1, results.size(), "Unexpected number of validation results");
        results = dao.getEntityValidations(GATHERING_EVENT, Severity.WARNING);
        assertEquals(1, results.size(), "Unexpected number of validation results");
        results = dao.getEntityValidations(GATHERING_EVENT, Severity.ERROR);
        assertEquals(2, results.size(), "Unexpected number of validation results");

        results = dao.getEntityValidations("foo.bar", Severity.ERROR);
        assertEquals(0, results.size(), "Unexpected number of validation results");
    }

    @Test
    public void testGetEntityValidations_Severity() {
        List<EntityValidation> results;
        results = dao.getEntityValidations(Severity.NOTICE);
        assertEquals(1, results.size(), "Unexpected number of validation results");
        results = dao.getEntityValidations(Severity.WARNING);
        assertEquals(2, results.size(), "Unexpected number of validation results");
        results = dao.getEntityValidations(Severity.ERROR);
        assertEquals(4, results.size(), "Unexpected number of validation results");
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub
    }

}
