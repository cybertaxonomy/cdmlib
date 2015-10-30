package eu.etaxonomy.cdm.common.monitor;

import java.io.Serializable;



/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/**
 * An abstract wrapper around a progress monitor which,
 * unless overridden, forwards <code>IProgressMonitor</code>
 * and <code>IProgressMonitorWithBlocking</code> methods to the wrapped progress monitor.
 * <p>
 * This class can be used without OSGi running.
 * </p><p>
 * Clients may subclass.
 * </p>
 */
public abstract class ProgressMonitorWrapper implements
        IProgressMonitor /*, IProgressMonitorWithBlocking*/ {

    /** The wrapped progress monitor. */
    private IProgressMonitor progressMonitor;

    /**
     * Creates a new wrapper around the given monitor.
     *
     * @param monitor the progress monitor to forward to
     */
    protected ProgressMonitorWrapper(IProgressMonitor monitor) {
//        Assert.isNotNull(monitor);
        progressMonitor = monitor;
    }

    /**
     * This implementation of a <code>IProgressMonitor</code>
     * method forwards to the wrapped progress monitor.
     * Clients may override this method to do additional
     * processing.
     *
     * @see IProgressMonitor#beginTask(String, int)
     */
    @Override
    public void beginTask(String name, int totalWork) {
        progressMonitor.beginTask(name, totalWork);
    }
//
//    /**
//     * This implementation of a <code>IProgressMonitorWithBlocking</code>
//     * method forwards to the wrapped progress monitor.
//     * Clients may override this method to do additional
//     * processing.
//     *
//     * @see IProgressMonitorWithBlocking#clearBlocked()
//     * @since 3.0
//     */
//    public void clearBlocked() {
//        if (progressMonitor instanceof  IProgressMonitorWithBlocking)
//            ((IProgressMonitorWithBlocking) progressMonitor)
//                    .clearBlocked();
//    }

    /**
     * This implementation of a <code>IProgressMonitor</code>
     * method forwards to the wrapped progress monitor.
     * Clients may override this method to do additional
     * processing.
     *
     * @see IProgressMonitor#done()
     */
    @Override
    public void done() {
        progressMonitor.done();
    }

    /**
     * Returns the wrapped progress monitor.
     *
     * @return the wrapped progress monitor
     */
    public IProgressMonitor getWrappedProgressMonitor() {
        return progressMonitor;
    }

    /**
     * This implementation of a <code>IProgressMonitor</code>
     * method forwards to the wrapped progress monitor.
     * Clients may override this method to do additional
     * processing.<BR>
     * As the  {@link IProgressMonitor interface} documentation for
     * {@link IProgressMonitor#internalWorked(double) this method}
     * says the method must not be called by a client.
     * Clients should always use the method </code>worked(int)</code>.
     *
     * @see IProgressMonitor#internalWorked(double)
     */
    @Override
    public void internalWorked(double work) {
        progressMonitor.internalWorked(work);
    }

    /**
     * This implementation of a <code>IProgressMonitor</code>
     * method forwards to the wrapped progress monitor.
     * Clients may override this method to do additional
     * processing.
     *
     * @see IProgressMonitor#isCanceled()
     */
    @Override
    public boolean isCanceled() {
        return progressMonitor.isCanceled();
    }

//    /**
//     * This implementation of a <code>IProgressMonitorWithBlocking</code>
//     * method forwards to the wrapped progress monitor.
//     * Clients may override this method to do additional
//     * processing.
//     *
//     * @see IProgressMonitorWithBlocking#setBlocked(IStatus)
//     * @since 3.0
//     */
//    public void setBlocked(IStatus reason) {
//        if (progressMonitor instanceof  IProgressMonitorWithBlocking)
//            ((IProgressMonitorWithBlocking) progressMonitor)
//                    .setBlocked(reason);
//    }

    /**
     * This implementation of a <code>IProgressMonitor</code>
     * method forwards to the wrapped progress monitor.
     * Clients may override this method to do additional
     * processing.
     *
     * @see IProgressMonitor#setCanceled(boolean)
     */
    @Override
    public void setCanceled(boolean b) {
        progressMonitor.setCanceled(b);
    }

    /**
     * This implementation of a <code>IProgressMonitor</code>
     * method forwards to the wrapped progress monitor.
     * Clients may override this method to do additional
     * processing.
     *
     * @see IProgressMonitor#setTaskName(String)
     */
    @Override
    public void setTaskName(String name) {
        progressMonitor.setTaskName(name);
    }

    /**
     * This implementation of a <code>IProgressMonitor</code>
     * method forwards to the wrapped progress monitor.
     * Clients may override this method to do additional
     * processing.
     *
     * @see IProgressMonitor#subTask(String)
     */
    @Override
    public void subTask(String name) {
        progressMonitor.subTask(name);
    }

    /**
     * This implementation of a <code>IProgressMonitor</code>
     * method forwards to the wrapped progress monitor.
     * Clients may override this method to do additional
     * processing.
     *
     * @see IProgressMonitor#worked(int)
     */
    @Override
    public void worked(int work) {
        progressMonitor.worked(work);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForFeedback() {
        progressMonitor.waitForFeedback();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFeedback(Serializable feedback) {
        progressMonitor.setFeedback(feedback);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Serializable getFeedback() {
        return progressMonitor.getFeedback();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getIsWaitingForFeedback() {
        return progressMonitor.getIsWaitingForFeedback();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForFeedback(long feedbackWaitTimeout) {
        progressMonitor.waitForFeedback(feedbackWaitTimeout);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasFeedbackWaitTimedOut() {
        return progressMonitor.hasFeedbackWaitTimedOut();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getOwner() {
        return progressMonitor.getOwner();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOwner(String owner) {
       progressMonitor.setOwner(owner);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void interrupt() {
        progressMonitor.interrupt();
    }
}
