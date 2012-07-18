// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common.monitor;

import org.apache.log4j.Logger;

/**
 * @author a.mueller
 * @date 14.09.2010
 *
 */
public class DefaultProgressMonitor implements IProgressMonitor {
    private static final Logger logger = Logger.getLogger(DefaultProgressMonitor.class);

    public static final DefaultProgressMonitor NewInstance(){
        return new DefaultProgressMonitor();
    }

    private boolean isCanceled = false;
    private String taskName = "No task name";
    private int totalWork = 0;
    private double workDone = 0;
    private String subTask = "No subtask name";


    private DefaultProgressMonitor(){

    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.common.IProgressMonitor#beginTask(java.lang.String, int)
     */
    @Override
    public void beginTask(String taskName, int totalWork) {
        logger.info("Start " + taskName);
        this.taskName = taskName;
        this.totalWork = totalWork;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.common.IProgressMonitor#done()
     */
    @Override
    public void done() {
        logger.info(taskName + "...Done");
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.common.IProgressMonitor#isCanceled()
     */
    @Override
    public boolean isCanceled() {
        return isCanceled;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.common.IProgressMonitor#setCanceled(boolean)
     */
    @Override
    public void setCanceled(boolean isCanceled) {
        this.isCanceled = isCanceled;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.common.IProgressMonitor#setTaskName(java.lang.String)
     */
    @Override
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.common.IProgressMonitor#subTask(java.lang.String)
     */
    @Override
    public void subTask(String subTask) {
        this.subTask = subTask;
        logger.info(/*getPercentage() + "% done." + */  " Next Task: " + subTask);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.common.IProgressMonitor#worked(int)
     */
    @Override
    public void worked(int work) {
        computeWorked(work);
//		this.workDone = this.workDone +  work;
    }


    @Override
    public void internalWorked(double work) {
        computeWorked(work);
//		this.workDone = this.workDone +  work;
    }

    private void computeWorked(double work){
        this.workDone = this.workDone +  work;
        logger.info(getPercentage() + "% done (Completed Task: " + subTask + ")");
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.common.IProgressMonitor#warning(java.lang.String)
     */
    @Override
    public void warning(String warning) {
        logger.warn(warning);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.common.IProgressMonitor#warning(java.lang.String, java.lang.Exception)
     */
    @Override
    public void warning(String warning, Throwable exception) {
        logger.warn(warning);
        exception.printStackTrace();
    }

    public double getPercentage(){
        double result = this.workDone * 100 /this.totalWork ;
        return result;
    }



}
