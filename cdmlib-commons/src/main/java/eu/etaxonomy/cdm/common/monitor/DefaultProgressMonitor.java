/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common.monitor;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * This is a console style progress monitor with prints the progress information to configured {@link Logger} with level {@link Level#INFO}
 *
 * @author a.mueller
 * @date 14.09.2010
 *
 */
public class DefaultProgressMonitor implements IProgressMonitor {
    private static final long serialVersionUID = 8782649283568146667L;

    private static final Logger logger = Logger.getLogger(DefaultProgressMonitor.class);

    public static final DefaultProgressMonitor NewInstance(){
        return new DefaultProgressMonitor();
    }

    private boolean isCanceled = false;
    protected String taskName = "No task name";
    protected int totalWork = 0;
    protected double workDone = 0;
    protected String subTask = "No subtask name";


    private Serializable feedback;
    private transient Object feedbackLock;
    private boolean isWaitingForFeedback = false;
    private long feedbackWaitStartTime;
    private static final long DEFAULT_FEEDBACK_WAIT_TIMEOUT = 1000 * 60 * 60 * 24; // 24 hours
    private long feedbackWaitTimeout = DEFAULT_FEEDBACK_WAIT_TIMEOUT;

    private String owner;

    protected DefaultProgressMonitor(){

    }

    @Override
    public void beginTask(String taskName, int totalWork) {
        logger.info("Start " + taskName);
        this.taskName = taskName;
        this.totalWork = totalWork;
    }

    @Override
    public void done() {
        logger.info(taskName + "...Done");
    }

    @Override
    public boolean isCanceled() {
        return isCanceled;
    }

    @Override
    public void setCanceled(boolean isCanceled) {
        this.isCanceled = isCanceled;
    }

    @Override
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    @Override
    public void subTask(String subTask) {
        this.subTask = subTask;
        logger.info(/*getPercentage() + "% done." + */  " Next Task: " + subTask);
    }

    @Override
    public void worked(int work) {
        computeWorked(work);
//      this.workDone = this.workDone +  work;
    }


    @Override
    public void internalWorked(double work) {
        computeWorked(work);
//      this.workDone = this.workDone +  work;
    }

    private void computeWorked(double work){
        this.workDone = this.workDone +  work;
        if (logger.isInfoEnabled()){ logger.info(getPercentage() + "% done (Completed Task: " + subTask + ")");}
    }

    @Override
    public void warning(String warning) {
        logger.warn(warning);
    }

    @Override
    public void warning(String warning, Throwable exception) {
        logger.warn(warning);
        exception.printStackTrace();
    }

    /**
     * Percentage of work done. With all work done = 100.0d.
     * As rounding errors may occur especially when using
     * {@link SubProgressMonitor} the result is rounded to 5 digits.
     * So do not use the result for additive percentages.
     */
    public Double getPercentage(){
        if(totalWork == 0 ){
            return null;
        }

        double result = this.workDone * 100 / this.totalWork ;
        //as double may have rounding errors especially when using subprogressmonitors
        //we do round the result slightly
        result = Math.round((result * 100000.0)) / 100000.0;
        return result;
    }

    public BigDecimal getPercentageRounded(int scale){
        if(totalWork == 0 ){
            return null;
        }
        double percentage = this.workDone * 100 / this.totalWork ;
        BigDecimal result = new BigDecimal(percentage).setScale( scale, BigDecimal.ROUND_HALF_UP );
        return result;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForFeedback() {
        if(feedbackLock == null) {
            feedbackLock =  new Object();
        }
        synchronized (feedbackLock) {
            feedback = null;
            while(feedback == null) {
                isWaitingForFeedback = true;
                try {
                    feedbackWaitStartTime = System.currentTimeMillis();
                    feedbackLock.wait();
                } catch (InterruptedException ie) {
                    throw new IllegalStateException(ie);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFeedback(Serializable feedback) {
        synchronized (feedbackLock) {
            this.feedback = feedback;
            this.feedbackLock.notifyAll();
            isWaitingForFeedback = false;
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Serializable getFeedback() {
        return feedback;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getIsWaitingForFeedback() {
        return isWaitingForFeedback;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void waitForFeedback(long feedbackWaitTimeout) {
        if(feedbackWaitTimeout <= 0 ) {
            throw new IllegalStateException("Feedback wait timeout should be a positive number");
        }
        this.feedbackWaitTimeout = feedbackWaitTimeout;
        waitForFeedback();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasFeedbackWaitTimedOut() {
       long now = System.currentTimeMillis();
       return isWaitingForFeedback && (now - feedbackWaitStartTime > feedbackWaitTimeout);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public String getOwner() {
        return owner;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setOwner(String owner) {
        this.owner = owner;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void interrupt() {
        // do nothing
    }



}
