package eu.etaxonomy.cdm.persistence.validation;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;


import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * Basically tests that the JSR-303 validation is working in the first place,
 * and that it is working as expected.
 * @author ayco holleman
 *
 */
public class EntityValidationTaskTest {
	
	private ValidatorFactory factory;
	
	@Before
	public void setUp() throws Exception {
		HibernateValidatorConfiguration config = Validation.byProvider(HibernateValidator.class).configure();
		factory = config.buildValidatorFactory();
		
	}

	
	@Test
	public void testValidate() {
		TestEntity01 one = new TestEntity01();
		one.setFirstName("john");
		one.setLastName("smith");
		one.setAge(40);
		one.setSalary(30000);
		Level2ValidationTask task = new Level2ValidationTask(one);
		task.setValidator(factory.getValidator());
		Set<ConstraintViolation<CdmBase>> violations = task.validate();
		Assert.assertEquals(violations.size(), 1);
		Assert.assertEquals(violations.iterator().next().getInvalidValue(), "john");
	}

}
