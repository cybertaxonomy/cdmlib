package eu.etaxonomy.cdm.persistence.validation;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

public class ValidationExecutorTest {

	private ValidationExecutor pool;


	@Before
	public void setUp() throws Exception
	{
	}


	@Test
	public void testSetMaximumPoolSize()
	{
		try {
			// Test that an exception is thrown when trying to change
			// the thread pool size
			ValidationExecutor pool = new ValidationExecutor();
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
		int taskQueueSize = 10;
		ValidationExecutor pool = new ValidationExecutor(taskQueueSize);
		// we only want to test that ValidationExecutor.rejectedExecution()
		// never throws an exception (for the rest it just logs something).
		for(int i =0; i< taskQueueSize * 2; ++i) { // Force a task queue overrun
			EmployeeWithLongRunningValidation emp = new EmployeeWithLongRunningValidation();
			Level2ValidationTask task = new Level2ValidationTask(emp);
			pool.execute(task);
		}
		try {
			// Make sure the test case waits at least long enough for
			// the queue to actually overflow. Since the validation of
			// takes at least 1000 milliseconds
			pool.threads.iterator().next().get().join();
		}
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	@Test
	public void testBeforeExecute()
	{

	}

}
