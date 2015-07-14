/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.validation;

import static org.junit.Assert.fail;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ValidationExecutorTest {

	public static final Logger logger = Logger.getLogger(ValidationExecutorTest.class);


	@Before
	public void setUp() throws Exception{
	}


	@Test
	public void testSetMaximumPoolSize(){
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
	public void testValidationExecutor(){
		// Constructor test  not implemented
	}


	/**
	 * Test behavior when the ValidationExecutor's task queue fills up. Make sure task queue
	 * overruns do not throw an exception. To test this, we rapidly fill the queue with tasks
	 * that we know will take some time to complete. See {@link LongRunningCheckCaseValidator}.
	 
	//TODO does this work already?
	@Test
	@Ignore
	public void testRejectedExecution(){
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
				task = new Level2ValidationTask(emp, null);
				pool.execute(task);
			}
			// Make sure the test case waits long enough for the queue to actually overflow.
			List<WeakReference<EntityValidationThread>> threads = new ArrayList<WeakReference<EntityValidationThread>>(pool.threads);
			for (WeakReference<EntityValidationThread> thread : threads) {
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
*/

	@Test
	public void testBeforeExecute(){

	}

}
