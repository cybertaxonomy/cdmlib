/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.validation;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.validation.ConstraintValidator;

import org.apache.log4j.Logger;

/**
 * A {@code ThreadPoolExecutor} specialized in dealing with
 * {@link EntityValidationThread}s and validation tasks (see
 * {@link EntityValidationTaskBase}). This implementation creates a thread pool
 * containing just one thread, meaning all validation tasks are run one after
 * another on that one thread. Especially for Level-3 validation tasks this is
 * probably exactly what you want. These tasks are run upon CRUD events, and you
 * don't want the database to be crawled to validate entire object graphs every
 * time a CRUD event takes place, especially since one CRUD operation may be
 * meant to cancel or correct a previous CRUD operation (e.g. a user of the
 * taxonomic editor may realize he/she did something wrong and then quickly
 * correct it).
 *
 * <p>
 * Although a {@code ValidationExecutor} sets up a thread pool containing just a
 * single thread, it does not logically or functionally <i>depend</i> on the
 * thread pool containing at most one thread. Thus, should performance become an
 * issue, and concurrency the solution, increasing the pool size is still an
 * option. For example, Level-2 validation tasks might be quite amenable to
 * being executed concurrently.
 *
 * <p>
 * The reason we extend {@code ThreadPoolExecutor} rather than simply use
 * {@link Executors#newSingleThreadExecutor()} is that we need access to the
 * threads in the thread pool for the reason indicated above: if an entity
 * annotated with Level-2 or Level-3 validation constraints is updated, it will
 * be validated on the validation thread. However, if it is quickly thereafter
 * updated again, you really would like to terminate the first validation if
 * it's still running. After all, it doesn't make sense to validate an entity in
 * a state that it no longer has. For Level-2 validations this may not be so
 * important, because they are likely to run fast. But for Level-3 validations
 * you want to prevent needless queueing and execution of long-running tasks.
 * Thus, you really would like to know which entity is being validated on the
 * validation thread. The {@code ThreadPoolExecutor} provides a
 * {@link #beforeExecute(Thread, Runnable)} method, passing us the thread and
 * the task that it is about to run. This allows us to track the threads in the
 * thread pool.
 * <p>
 * If the {@code ValidationExecutor} detects that a validation task enters the
 * task queue that will validate the same entity as the entity currently being
 * validated on the validation thread, it will call
 * {@link EntityValidationThread#setTerminationRequested(boolean)}. This gives
 * the {@link ConstraintValidator} running in the validation thread a chance to
 * terminate itself:<br>
 * <code>
 * if(Thread.currentThread() instanceof EntityValidationThread) {
 * 	EntityValidationThread evt = (EntityValidationThread) Thread.currentThread();
 * 	if(evt.isTerminationRequested()) {
 * 		// Stop with what I am doing
 * 	}
 * }
 * </code><br>
 * Constraint validators are free to include this logic or not. If they know
 * themselves to be short-lived it may not be worth it. But if they potentially
 * take a lot of time to complete, they can and and probably should include this
 * logic to prevent needless queueing and queue overruns. This would make them
 * dependent, though, on at least the {@link EntityValidationThread} class, so
 * there are some architectural issues here.
 *
 * @author ayco_holleman
 *
 */
//maybe we want to make this a spring component ?  #4663
public class ValidationExecutor extends ThreadPoolExecutor implements RejectedExecutionHandler {

	private static final Logger logger = Logger.getLogger(ValidationExecutor.class);

	// Number of threads to keep in the thread pool
	static final int CORE_POOL_SIZE = 0;
	// Maximum number of theads in the thread pool
	static final int MAX_POOL_SIZE = 1;
	// Number of seconds to wait for a new task before killing the validation
	// thread
	static final int KEEP_ALIFE_TIME = 5;
	// Maximum number of tasks allowed to wait to be executed by the validation
	// thread
	static final int TASK_QUEUE_SIZE = 1000;

	// Our basis for tracking the threads in the thread pool. We maintain
	// a list of weak references to the thread in the real thread pool,
	// maintained but totally hidden by the super class (ThreadPoolExecutor).
	final ArrayList<WeakReference<EntityValidationThread>> threads = new ArrayList<WeakReference<EntityValidationThread>>(
			MAX_POOL_SIZE);

	/**
	 * Creates a {@code ValidationExecutor} with a task queue size of 1000. Thus
	 * there can be at most 1000 pending validations. Thereafter newly submitted
	 * validation tasks will simply be discarded. See
	 * {@link #rejectedExecution(Runnable, ThreadPoolExecutor)}.
	 */
	public ValidationExecutor(){
	    this(TASK_QUEUE_SIZE);
	}

	/**
	 * Creates a {@code ValidationExecutor} with a custom task queue size.
	 *
	 * @param taskQueueSize
	 */
	public ValidationExecutor(int taskQueueSize){
		super(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIFE_TIME, TimeUnit.SECONDS,
				new EntityValidationTaskQueue(taskQueueSize));
		setThreadFactory(new ValidationThreadFactory());
		setRejectedExecutionHandler(this);
	}

	/**
	 * Implements the one method from {@link RejectedExecutionHandler}, which is
	 * called in case of task queue overruns. Because Level-2 and Level-3
	 * validations may not obstruct the CRUD events that triggered them, or
	 * impair the stability of the system as a whole, this method only writes an
	 * error message to the log4j log file. Thus, task queue overruns may cause
	 * Level-2 and/or Level-3 constraint violations to creep into the database.
	 * And thus, some other, batch-like process needs to crawl the entire
	 * database in search of Level-2 and Level-3 constraint violations every
	 * once in a while.
	 */
	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor){
		EntityValidationTaskBase task = (EntityValidationTaskBase) r;
		logger.error(String.format(
				"Validation of %s cancelled. Too many validation tasks waiting to be executed.",
				task.getEntity().toString()));
	}

	/**
	 * Overrides method from {@link ThreadPoolExecutor} to prevent thread pool
	 * size from being altered. Will throw a RuntimeException. Future versions
	 * could abandon this restriction once it has become clear that concurrent
	 * execution of Level-2 and/or Level-3 validations constitutes no problem
	 * and may solve performance problems.
	 */
	@Override
	public void setMaximumPoolSize(int maximumPoolSize){
		throw new RuntimeException(
				"Altering maximum pool size for ValidationExecutor instances currently not allowed");
	}

	@Override
	protected void beforeExecute(Thread thread, Runnable runnable){
		EntityValidationThread validationThread = (EntityValidationThread) thread;
		EntityValidationTaskBase task = (EntityValidationTaskBase) runnable;
		validationThread.setTerminationRequested(false);
		task.setValidator(validationThread.getValidator());
		checkPool(validationThread, task);
		validationThread.setCurrentTask(task);
	}

	/*
	 * This method does 2 things. [A] It keeps track of the threads in the
	 * thread pool. If pendingThread is not yet in our "shadow pool" we add it
	 * to the shadow pool. [B] It searches for other threads in the trhead pool
	 * that are still busy validating an older version of the entity to be
	 * validated during pendingTask. If there is such a thread, we ask it to
	 * terminate itself. Whether or not this request is honored, we wait for the
	 * thread to complete. Otherwise the two threads might conflict with
	 * eachother when reading/writing from the error tables (i.e. the tables in
	 * which the outcome of a validation is stored). Note that, currently, this
	 * is all a bit theoretical because we only allow one thread in the thread
	 * pool. However, we want to be prepared for a future with truely concurrent
	 * validation.
	 */
	private void checkPool(EntityValidationThread pendingThread,
			EntityValidationTaskBase pendingTask){
		boolean found = false;
		Iterator<WeakReference<EntityValidationThread>> iterator = threads.iterator();
		while (iterator.hasNext()) {
			EntityValidationThread pooledThread = iterator.next().get();
			if (pooledThread == null) {
				// Thread has been removed from the real thread pool
				// and got garbage collected. Remove our weak reference
				// to the thread
				iterator.remove();
			} else if (pooledThread == pendingThread) {
				found = true;
			} else if (pooledThread.isAlive()) {
				if (pooledThread.getCurrentTask().equals(pendingTask)) {
					pooledThread.setTerminationRequested(true);
					pendingTask.waitFor(pooledThread);
				}
			}
		}
		if (!found) {
			threads.add(new WeakReference<EntityValidationThread>(pendingThread));
		}
		threads.trimToSize();
	}

}
