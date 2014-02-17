package eu.etaxonomy.cdm.persistence.validation;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.validation.ConstraintValidator;

import org.apache.log4j.Logger;

/**
 * A {@code ThreadPoolExecutor} specialised in dealing with {@link ValidationThread}s and
 * validation tasks (see {@link EntityValidationTask}). This implementation creates a thread
 * pool containing just one thread, meaning all validation tasks are run one after another on
 * that one thread. Especially for Level-3 validation tasks this is probably exactly what you
 * want. These tasks are run upon CRUD events, and you don't want the database to be crawled to
 * validate entire object graphs every time a CRUD event takes place, especially since one CRUD
 * operation may be meant to cancel or correct a previous CRUD operation (e.g. a user of the
 * taxonomic editor may realize he/she did something wrong and then quickly correct it).
 * 
 * <p>
 * Although a {@code ValidationExecutor} sets up a thread pool containing just a single thread,
 * it does not logically or functionally <i>depend</i> on the thread pool containing at most
 * one thread. Thus, should performance become an issue, and concurrency the solution,
 * increasing the pool size is still an option. For example, Level-2 validation tasks might be
 * quite suitable for being executed concurrently.
 * 
 * <p>
 * The reason we extend {@code ThreadPoolExecutor} rather than simply use
 * {@link Executors#newSingleThreadExecutor()} is that we need access to the threads in the
 * thread pool for the reason indicated above: if an entity annotated with Level-2 or Level-3
 * validation constraints is updated, it will be validated on the validation thread. However,
 * if it is quickly thereafter updated again, you really would like to terminate the first
 * validation if it's still running. After all, it doesn't make sense to validate the entity in
 * a state that it no longer has. For Level-2 validations this may not be so important, because
 * they are not likely to run fast. But for Level-3 validations you would like to prevent
 * needless queueing and execution of long-running tasks. Thus, you really would like to know
 * which entity is being validated on the validation thread. The {@code ThreadPoolExecutor}
 * provides a {@link #beforeExecute(Thread, Runnable)} method, passing us the thread and the
 * task that it is about to run. This allows us to track the threads in the thread pool.
 * 
 * <p>
 * If the {@code ValidationExecutor} detects that a validation task enters the task queue that
 * will validate the same entity (in a different state) as the entity currently being validated
 * on the validation thread, it will call
 * {@link ValidationThread#setTerminationRequested(boolean)}. This gives the
 * {@link ConstraintValidator} running in the validation thread a chance to terminate itself:<br>
 * <code>
 * if(Thread.currentThread() instanceof ValidationThread) {
 * 	ValidationThread vt = (ValidationThread) Thread.currentThread();
 * 	if(vt.isTerminationRequested()) {
 * 		// Stop with what I am doing
 * 	}
 * }
 * </code><br>
 * Constraint validators are free to include this logic or not. If they know themselves to be
 * short-lived it may not be worth it. But if they potentially take a lot of time to complete,
 * they can and and probably should include this logic to prevent needless queueing and queue
 * overruns. This would make them dependent on at least the {@code ValidationThread} class, so
 * there are some architectural issues here.
 * 
 * @author a. holleman
 * 
 */
public class ValidationExecutor extends ThreadPoolExecutor implements RejectedExecutionHandler {

	private static final Logger logger = Logger.getLogger(ValidationExecutor.class);

	// Number of threads to keep in the thread pool
	private static final int CORE_POOL_SIZE = 0;
	// Maximum number of theads in the thread pool
	private static final int MAX_POOL_SIZE = 1;
	// Number of seconds to wait for a new task before killing the validation thread
	private static final int KEEP_ALIFE_TIME = 5;
	// Maximum number of tasks allowed to wait to be executed by the validation thread
	private static final int TASK_QUEUE_SIZE = 1000;

	// Our basis for tracking the threads in the thread pool
	private final ArrayList<WeakReference<ValidationThread>> threads = new ArrayList<WeakReference<ValidationThread>>(MAX_POOL_SIZE);


	public ValidationExecutor()
	{
		super(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIFE_TIME, TimeUnit.SECONDS, new EntityValidationTaskQueue(TASK_QUEUE_SIZE));
		setThreadFactory(new ValidationThreadFactory());
		setRejectedExecutionHandler(this);
	}


	/**
	 * Implements the one method from {@link RejectedExecutionHandler}, which is called in case
	 * of task queue overruns. Because Level-2 and Level-3 validations may not obstruct the
	 * CRUD events that triggered them, or impair the stability of the system as a whole, this
	 * method only writes an error message to the log4j log file. Thus, task queue overruns may
	 * cause Level-2 and/or Level-3 constraint violations to creep into the database. And thus,
	 * some other, batch-like process needs to crawl the entire database in search of Level-2
	 * and Level-3 validations every once in a while.
	 */
	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor)
	{
		EntityValidationTask task = (EntityValidationTask) r;
		logger.error(String.format("Validation of %s cancelled. Too many validation tasks waiting to be executed.", task.getEntity().toString()));
	}


	/**
	 * Overrides method from {@link ThreadPoolExecutor} to prevent thread pool size from being
	 * altered. Will throw a RuntimeException. Future versions could abandon this restriction
	 * once it has become clear that concurrent execution of Level-2 and/or Level-3 validations
	 * constitutes no problem and may solve performance problems.
	 */
	@Override
	public void setMaximumPoolSize(int maximumPoolSize)
	{
		throw new RuntimeException("Altering maximum pool size for ValidationExecutor instances not allowed");
	}


	@Override
	protected void beforeExecute(Thread thread, Runnable runnable)
	{
		checkPool(thread);
	}


	/*
	 * Keep track of the threads in the thread pool.
	 */
	private void checkPool(Thread thread)
	{
		boolean found = false;
		for (WeakReference<ValidationThread> ref : threads) {
			if (ref.get() == thread) {
				found = true;
			}
		}
		if (!found) {
			ValidationThread t = (ValidationThread) thread;
			threads.add(new WeakReference<ValidationThread>(t));
		}
		threads.trimToSize();
	}

}
