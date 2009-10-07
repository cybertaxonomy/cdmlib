/**
 * 
 */
package eu.etaxonomy.cdm.model.agent;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author b.clark
 *
 */
public class AgentValidationTest {

	private static Validator validator;

	@BeforeClass
	public static void onSetUp() throws Exception {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();	
		validator = factory.getValidator();
	}

	@Test
	public void testNullTitleCache() {
		Person person = Person.NewInstance();
		Set<ConstraintViolation<Person>> constraintViolations = validator.validate(person);
		
		for(ConstraintViolation<Person> constraintViolation : constraintViolations) {
			System.out.println(constraintViolation.getMessage());
		}
	}
}
