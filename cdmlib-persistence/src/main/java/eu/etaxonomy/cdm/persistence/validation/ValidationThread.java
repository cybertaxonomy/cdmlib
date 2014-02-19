package eu.etaxonomy.cdm.persistence.validation;

import javax.validation.ConstraintValidator;
import javax.validation.Validator;

/**
 * A subclass of {@code Thread} specialised in running validation tasks. This class is not
 * specifically targeted at <i>entity validation</a> tasks. Each {@code ValidationThread} has
 * its own {@link Validator} instance. In addition it allows a flag to be set (by the main
 * thread) that the currently running {@link ConstraintValidator} may query to see if there is
 * a termination request. See {@link ValidationExecutor} for the rationale behind this.
 * 
 * @author ayco holleman
 * 
 */
public final class ValidationThread extends Thread {

	private final Validator validator;
	private boolean terminationRequested;


	ValidationThread(ThreadGroup group, Runnable runnable, String name, Validator validator)
	{
		super(group, runnable, name);
		this.validator = validator;
		setPriority(MIN_PRIORITY);
	}


	/**
	 * Flag indicating that the {@link ConstraintValidator} currently running in this
	 * {@code ValidationThread} is requested to terminate itself. Constraint validators can
	 * check whether to abort the validation like so:<br>
	 * <code>
	 * if(Thread.currentThread() instanceof ValidationThread) {
	 * 	ValidationThread vt = (ValidationThread) Thread.currentThread();
	 * 	if(vt.isTerminationRequested()) {
	 * 		// Stop with what I am doing
	 * 	}
	 * }
	 * </code>
	 * 
	 * @return Whether or not the currently running {@link ConstraintValidator} is requested to
	 *         terminate itself
	 */
	public boolean isTerminationRequested()
	{
		return terminationRequested;
	}


	void setTerminationRequested(boolean b)
	{
		this.terminationRequested = b;
	}


	Validator getValidator()
	{
		return validator;
	}

}
