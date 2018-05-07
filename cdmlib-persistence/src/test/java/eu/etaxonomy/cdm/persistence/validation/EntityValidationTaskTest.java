/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.validation;

import java.util.Arrays;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.ICdmBase;

/**
 * Basically just tests that the JSR-303 validation is working in the first place, and
 * that it is working as expected.
 *
 * @author ayco holleman
 *
 */
public class EntityValidationTaskTest {

	private ValidatorFactory factory;


	@Before
	public void setUp() throws Exception{
		HibernateValidatorConfiguration config = Validation.byProvider(HibernateValidator.class).configure();
		factory = config.buildValidatorFactory();
	}


	/**
	 * Test that all and only Level-2 validation errors are found by the
	 * {@code EntityValidationTask}.
	 */
	@Test
	public void testValidateForLevel2(){

		// This is the bean that is bean that is going to be tested
		Employee emp = new Employee();
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

		// Validate
		Level2ValidationTask task = new Level2ValidationTask(emp, null);
		task.setValidator(factory.getValidator());
		Set<ConstraintViolation<ICdmBase>> violations = task.validateWithErrorHandling();

		Assert.assertEquals("Expecting three validation errors", 2, violations.size());

		// Test that validation failed where we expected it to fail
		String[] paths = new String[violations.size()];
		int i = 0;
		for (ConstraintViolation<ICdmBase> cv : violations) {
			paths[i++] = cv.getPropertyPath().toString();
		}
		Arrays.sort(paths);
		Assert.assertArrayEquals(paths, new String[] { "company.name", "givenName" });

	}


	@Test
	public void testValidateForLevel3()
	{
		Employee one = new Employee();
		// This is an error (should be JOHN), but it is a Level-2
		// validation error, so the error should be ignored.
		one.setGivenName("john");
		// ERROR 1 (should be SMITH)
		one.setFamilyName("smith");
		Level3ValidationTask task = new Level3ValidationTask(one, null);
		task.setValidator(factory.getValidator());
		Set<ConstraintViolation<ICdmBase>> violations = task.validateWithErrorHandling();
		Assert.assertEquals(violations.size(), 1);
		// Assert that validation failed where we expected it to fail.
		Assert.assertEquals(violations.iterator().next().getInvalidValue(), "smith");
	}

}
