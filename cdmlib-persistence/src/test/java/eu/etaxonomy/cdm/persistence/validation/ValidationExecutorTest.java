package eu.etaxonomy.cdm.persistence.validation;

import static org.junit.Assert.fail;

import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.junit.Before;
import org.junit.Test;

public class ValidationExecutorTest {

	private ValidationExecutor pool;
	private ValidatorFactory factory;


	@Before
	public void setUp() throws Exception
	{
		HibernateValidatorConfiguration config = Validation.byProvider(HibernateValidator.class).configure();
		factory = config.buildValidatorFactory();
		pool = new ValidationExecutor();
	}


	@Test
	public void testSetMaximumPoolSize()
	{
		try {
			pool.setMaximumPoolSize(10);
		}
		catch (Throwable t) {
			// As expected
			return;
		}
		fail("setMaximumPoolSize expected to throw a runtime exception");
	}


	@Test
	public void testValidationExecutor()
	{
		//fail("Not yet implemented");
	}


	@Test
	public void testRejectedExecution()
	{
		//fail("Not yet implemented");
	}


	@Test
	public void testBeforeExecuteThreadRunnable()
	{

		TestEntity03 one = new TestEntity03();
		one.setFirstName("john");
		one.setLastName("smith");
		one.setAge(40);
		one.setSalary(30000);

		EntityValidationTask task = new Level2ValidationTask(one);
		task.setValidator(factory.getValidator());
		pool.execute(task);
		
		try {
			Thread.sleep(10000);
		}
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
