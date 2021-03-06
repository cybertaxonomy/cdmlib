
package eu.etaxonomy.cdm.common.monitor;

import java.io.Serializable;



/**
 * This progress monitor interface is ad adaptation of the eclipse
 * org.eclipse.core.runtime.IProgressMonitor ;
 *
 * The <code>IProgressMonitor</code> interface is implemented
 * by objects that monitor the progress of an activity; the methods
 * in this interface are invoked by code that performs the activity.
 * <p>
 * All activity is broken down into a linear sequence of tasks against
 * which progress is reported. When a task begins, a <code>beginTask(String, int)
 * </code> notification is reported, followed by any number and mixture of
 * progress reports (<code>worked()</code>) and subtask notifications
 * (<code>subTask(String)</code>).  When the task is eventually completed, a
 * <code>done()</code> notification is reported.  After the <code>done()</code>
 * notification, the progress monitor cannot be reused;  i.e., <code>
 * beginTask(String, int)</code> cannot be called again after the call to
 * <code>done()</code>.
 * </p>
 * <p>
 * A request to cancel an operation can be signaled using the
 * <code>setCanceled</code> method.  Operations taking a progress
 * monitor are expected to poll the monitor (using <code>isCanceled</code>)
 * periodically and abort at their earliest convenience.  Operation can however
 * choose to ignore cancelation requests.
 * </p>
 * <p>
 * Since notification is synchronous with the activity itself, the listener should
 * provide a fast and robust implementation. If the handling of notifications would
 * involve blocking operations, or operations which might throw uncaught exceptions,
 * the notifications should be queued, and the actual processing deferred (or perhaps
 * delegated to a separate thread).
 * </p><p>
 * This interface can be used without OSGi running.
 * </p><p>
 * Clients may implement this interface.
 * </p>
 */
public interface IProgressMonitor extends Serializable {

	/** Constant indicating an unknown amount of work.
	 */
	public final static int UNKNOWN = -1;

	/**
	 * Notifies that the main task is beginning.  This must only be called once
	 * on a given progress monitor instance.
	 *
	 * @param name the name (or description) of the main task
	 * @param totalWork the total number of work units into which
	 *  the main task is been subdivided. If the value is <code>UNKNOWN</code>
	 *  the implementation is free to indicate progress in a way which
	 *  doesn't require the total number of work units in advance.
	 */
	public void beginTask(String name, int totalWork);

	/**
	 * Notifies that the work is done; that is, either the main task is completed
	 * or the user canceled it. This method may be called more than once
	 * (implementations should be prepared to handle this case).
	 */
	public void done();


	/**
	 * Returns whether cancelation of current operation has been requested.
	 * Long-running operations should poll to see if cancelation
	 * has been requested.
	 *
	 * @return <code>true</code> if cancellation has been requested,
	 *    and <code>false</code> otherwise
	 * @see #setCanceled(boolean)
	 */
	public boolean isCanceled();

	/**
	 * Sets the cancel state to the given value.
	 *
	 * @param value <code>true</code> indicates that cancelation has
	 *     been requested (but not necessarily acknowledged);
	 *     <code>false</code> clears this flag
	 * @see #isCanceled()
	 */
	public void setCanceled(boolean value);

	/**
	 * Sets the task name to the given value. This method is used to
	 * restore the task label after a nested operation was executed.
	 * Normally there is no need for clients to call this method.
	 *
	 * @param name the name (or description) of the main task
	 * @see #beginTask(java.lang.String, int)
	 */
	public void setTaskName(String name);

	/**
	 * Notifies that a subtask of the main task is beginning.
	 * Subtasks are optional; the main task might not have subtasks.
	 *
	 * @param name the name (or description) of the subtask
	 */
	public void subTask(String name);

	/**
	 * Notifies that a given number of work unit of the main task
	 * has been completed. Note that this amount represents an
	 * installment, as opposed to a cumulative amount of work done
	 * to date.
	 *
	 * @param work a non-negative number of work units just completed
	 */
	public void worked(int work);

    /**
     * Internal method to handle scaling correctly. This method
     * must not be called by a client. Clients should
     * always use the method </code>worked(int)</code>.
     *
     * @param work the amount of work done
     */
    public void internalWorked(double work);


	/**
	 * Notifies about a warning
	 * @param message
	 */
	public void warning(String message);

	/**
	 * Notifies about a warning that was caused by an exception.
	 * @param message
	 * @param throwable
	 */
	public void warning(String message, Throwable throwable);


	/**
	 * Waits for external feedback during the lifetime of the
	 * operation
	 */
	public void waitForFeedback();

	/**
     * Waits for external feedback during the lifetime of the
     * operation with given timeout
     *
     * @param feedbackWaitTimeout time (in ms to wait for feedback)
     */
    public void waitForFeedback(long feedbackWaitTimeout);


    public boolean hasFeedbackWaitTimedOut();

    /**
     * Sets the feedback object the monitor is currently waiting for.
     *
     * @param feedback
     */
    public void setFeedback(Serializable feedback);

    /**
     * Returns the most recently set feedback object
     *
     * @return most recently set feedback object
     */
    public Serializable getFeedback();


    /**
     * Boolean flag to indicate whether the monitor is
     * waiting for feedback
     *
     * @return true if monitor is waiting for feedback, o/w false
     */
    public boolean getIsWaitingForFeedback();


    /**
     * Returns the user who started the monitored job
     * @return the user who started the monitored job
     */
    public String getOwner();

    /**
     * Sets the user who started the monitored job
     * @param owner the user who started the monitored job
     */
    public void setOwner(String owner);


    public void interrupt();

}


