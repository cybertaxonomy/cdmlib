/**
 *
 */
package eu.etaxonomy.cdm.common.monitor;

import java.io.Serializable;


/**
 * Empty default implementation
 *
 * @author n.hoffmann
 *
 */
public class NullProgressMonitor implements IProgressMonitor {

	@Override
	public void beginTask(String name, int totalWork) {
		// do nothing
	}

	@Override
	public void done() {
		//  do nothing
	}

	@Override
	public boolean isCanceled() {
		//  do nothing
		return false;
	}

	@Override
	public void setCanceled(boolean value) {
		// do nothing
	}

	@Override
	public void setTaskName(String name) {
//		 do nothing
	}

	@Override
	public void subTask(String name) {
		//  do nothing
	}

	@Override
	public void worked(int work) {
		// do nothing
	}

	@Override
	public void warning(String message) {
		//  do nothing
	}

	@Override
	public void warning(String message, Throwable throwable) {
		// do nothing
	}

	@Override
	public void internalWorked(double work) {
		//  do nothing
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForFeedback() {
        //  do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFeedback(Serializable feedback) {
        //  do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Serializable getFeedback() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getIsWaitingForFeedback() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForFeedback(long feedbackWaitTimeout) {
    //  do nothing

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasFeedbackWaitTimedOut() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getOwner() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOwner(String owner) {
        //  do nothing

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void interrupt() {
        //  do nothing

    }

}
