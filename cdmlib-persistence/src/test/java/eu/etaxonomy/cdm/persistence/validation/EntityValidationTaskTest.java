package eu.etaxonomy.cdm.persistence.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * Basically just tests that the JSR-303 validation is working in the first place, and that it
 * is working as expected.
 * 
 * @author ayco holleman
 * 
 */
public class EntityValidationTaskTest {

	private ValidatorFactory factory;


	@Before
	public void setUp() throws Exception
	{
		HibernateValidatorConfiguration config = Validation.byProvider(HibernateValidator.class).configure();
		factory = config.buildValidatorFactory();

	}


	/**
	 * Test that all and only Level-2 validation errors are found by the
	 * {@code EntityValidationTask}.
	 */
	@Test
	public void testValidateForLevel2()
	{

		// This is the bean that is bean that is going to be tested
		Employee emp = new Employee();
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

		// This is an @Valid bean on the Employee class
		List<Address> addresses = new ArrayList<Address>();
		Address address1 = new Address();
		// ERROR 3 (should be MARKET STREET)
		address1.setStreet("Market Street");
		emp.setAddresses(addresses);

		// Validate
		Level2ValidationTask task = new Level2ValidationTask(emp);
		task.setValidator(factory.getValidator());
		Set<ConstraintViolation<CdmBase>> violations = task.validate();

		Assert.assertEquals("Expecting three validation errors", violations.size(), 3);

		// Test that validation failed where we expected it to fail
		String[] paths = new String[3];
		int i = 0;
		for (ConstraintViolation<CdmBase> cv : violations) {
			paths[i++] = cv.getPropertyPath().toString();
		}
		Arrays.sort(paths);
		Assert.assertArrayEquals(paths, new String[] { "addresses[0].street", "company.name", "firstName" });

	}


	@Test
	public void testValidateForLevel3()
	{
		Employee one = new Employee();
		// This is an error (should be JOHN), but it is a Level-2
		// validation error, so the error should be ignored.
		one.setFirstName("john");
		// ERROR 1 (should be SMITH)
		one.setLastName("smith");
		Level3ValidationTask task = new Level3ValidationTask(one);
		task.setValidator(factory.getValidator());
		Set<ConstraintViolation<CdmBase>> violations = task.validate();
		Assert.assertEquals(violations.size(), 1);
		// Assert that validation failed where we expected it to fail.
		Assert.assertEquals(violations.iterator().next().getInvalidValue(), "smith");
	}

}
