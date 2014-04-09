package eu.etaxonomy.cdm.persistence.validation;

import static org.junit.Assert.fail;

import java.lang.ref.WeakReference;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

public class ValidationExecutorTest {

	public static final Logger logger = Logger.getLogger(ValidationExecutorTest.class);


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
		// Constructor test  not implemented
	}


	/**
	 * Test behaviour when the ValidationExecutor's task queue fills up. Make sure task queue
	 * overruns do not throw an exception. To test this, we rapidly fill the queue with tasks
	 * that we know will take some time to complete. See {@link LongRunningCheckCaseValidator}.
	 */
	@Test
	public void testRejectedExecution()
	{
		try {
			// Bit awkward, but since unit tests themselves also run in a separate thread,
			// we allow the previous test case some time to complete, otherwise the output
			// from this test may interleave with the output from the previous test, which
			// is confusing.
			//Thread.sleep(3000);
			int taskQueueSize = 5;
			ValidationExecutor pool = new ValidationExecutor(taskQueueSize);
			EmployeeWithLongRunningValidation emp;
			Level2ValidationTask task;
			System.out.println("************************************************************");
			System.out.println("Forcing task queue overflow. Error messages are expected !!!");
			System.out.println("************************************************************");
			for (int i = 0; i < taskQueueSize * 2; ++i) { // Force a task queue overrun
				emp = new EmployeeWithLongRunningValidation();
				task = new Level2ValidationTask(emp);
				pool.execute(task);
			}
			// Make sure the test case waits long enough for the queue to actually overflow.
			for (WeakReference<EntityValidationThread> thread : pool.threads) {
				if (thread.get() != null) {
					thread.get().join();
				}
			}
			//Thread.sleep(3000);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


	@Test
	public void testBeforeExecute()
	{

	}

}
