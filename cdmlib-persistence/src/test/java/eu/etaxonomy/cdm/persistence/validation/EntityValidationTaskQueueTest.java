package eu.etaxonomy.cdm.persistence.validation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EntityValidationTaskQueueTest {

	EntityValidationTask evt1;
	EntityValidationTask evt2;
	EntityValidationTask evt3;


	/*
	 * Creates two equal EntityValidationTasks. EntityValidationTasks are equal if the entities
	 * they validate are equal and if the ValidationGroups applied are equal. The simplisticc
	 * equals() method on Employee makes the employees created in setUp() equal, while
	 * Level2ValidationTask extends EntityValidationTask in that in only validates constraints
	 * belonging to the "Level2" validation group.
	 */
	@Before
	public void setUp()
	{

		Employee emp1 = new Employee();
		emp1.setFirstName("John");
		emp1.setLastName("Smith");

		Employee emp2 = new Employee();
		emp2.setFirstName("John");
		emp2.setLastName("Smith");

		evt1 = new Level2ValidationTask(emp1);
		evt2 = new Level2ValidationTask(emp2);
		evt3 = new Level2ValidationTask(emp1);

	}


	/*
	 * Tests that the queue cannot contain two equal EntityValidationTasks, and that the
	 * EntityValidationTask that has driven out the two previously added tasks
	 */
	@Test
	public void testOffer()
	{
		EntityValidationTaskQueue queue = new EntityValidationTaskQueue(10);
		queue.offer(evt1);
		queue.offer(evt2);
		queue.offer(evt3);
		Assert.assertEquals(queue.size(), 1);
		Assert.assertTrue(queue.iterator().next() == evt3);
	}


	/*
	 * Tests that the queue cannot contain two equal EntityValidationTasks, and that the
	 * EntityValidationTask that was last added will be in the queue.
	 */
	@Test
	public void testAdd()
	{
		EntityValidationTaskQueue queue = new EntityValidationTaskQueue(10);
		queue.add(evt1);
		queue.add(evt2);
		Assert.assertEquals(queue.size(), 1);
		Assert.assertTrue(queue.iterator().next() == evt2);
	}


	/*
	 * Tests that the queue cannot contain two equal EntityValidationTasks, and that the
	 * EntityValidationTask that was last added will be in the queue.
	 */
	@Test
	public void testPut() throws InterruptedException
	{
		EntityValidationTaskQueue queue = new EntityValidationTaskQueue(10);
		queue.put(evt1);
		queue.put(evt2);
		Assert.assertEquals(queue.size(), 1);
		Assert.assertTrue(queue.iterator().next() == evt2);
	}

}
