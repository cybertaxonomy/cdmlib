package eu.etaxonomy.cdm.persistence.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintValidator;
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
	 * Test that all and only Level-2 validation
	 */
	@Test
	public void testValidateForLevel2()
	{

		// This is the bean that is bean that is going to be tested
		Employee emp = new Employee();
		// Error 1 (should be JOHN)
		emp.setFirstName("john");
		// Level-3 constraint violation (should be ignored)
		emp.setLastName("smith");
		emp.setAge(40);
		emp.setSalary(30000);

		// This is an @Valid bean on emp
		Company comp = new Company();
		// Error 2 (should be GOOGLE)
		comp.setName("Google");
		emp.setCompany(comp);

		// This is an @Valid bean on emp
		List<Address> addresses = new ArrayList<Address>();
		Address address1 = new Address();
		// Error 2 (should be MARKET STREET)
		address1.setStreet("Market Street");
		address1.setStreetNo("22");
		address1.setZip("1234AB");
		address1.setCity("Palo Alto");
		addresses.add(address1);
		emp.setAddresses(addresses);

		// Validate
		Level2ValidationTask task = new Level2ValidationTask(emp);
		task.setValidator(factory.getValidator());
		Set<ConstraintViolation<CdmBase>> violations = task.validate();

		Assert.assertEquals(violations.size(), 3);

		String[] paths = new String[3];
		Class<?>[] leaveBeans = new Class<?>[3];
		int i = 0;
		for (ConstraintViolation<CdmBase> cv : violations) {
			paths[i] = cv.getPropertyPath().toString();
			leaveBeans[i] = cv.getLeafBean().getClass();
			i++;
			System.out.println("leave bean: " + cv.getLeafBean().getClass());
			System.out.println("propterty: " + cv.getPropertyPath().toString());
			// In all cases the properties violated just one constraint
			List<?> validators = cv.getConstraintDescriptor().getConstraintValidatorClasses();
			// Test that only one constraint was violated
			Assert.assertEquals(validators.size(), 1);
			// That that it was the CheckCaseValidator that threw up the validation error
			Assert.assertEquals(validators.iterator().next(), CheckCaseValidator.class);
		}

		// Test that validation failed on the expected properties
		Arrays.sort(paths);
		Assert.assertArrayEquals(paths, new String[] { "addresses[0].street", "company.name", "firstName" });

		// Test that each class in the object graph occurs once and only once
		Arrays.sort(leaveBeans, new Comparator<Class<?>>() {

			@Override
			public int compare(Class<?> o1, Class<?> o2)
			{
				return o1.getName().compareTo(o2.getName());
			}

		});
		Assert.assertArrayEquals(leaveBeans, new Class[] { Address.class, Company.class, Employee.class });
	}


	@Test
	public void testValidateForLevel3()
	{
		Employee one = new Employee();
		one.setFirstName("john");
		one.setLastName("smith");
		one.setAge(40);
		one.setSalary(30000);
		Level3ValidationTask task = new Level3ValidationTask(one);
		task.setValidator(factory.getValidator());
		Set<ConstraintViolation<CdmBase>> violations = task.validate();
		Assert.assertEquals(violations.size(), 1);
		Assert.assertEquals(violations.iterator().next().getInvalidValue(), "smith");
	}

}
