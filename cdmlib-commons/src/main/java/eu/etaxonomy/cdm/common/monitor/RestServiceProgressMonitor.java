// $Id$
/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common.monitor;

/**
 * @author Andreas Kohlbecker
 * @date Jul 16, 2012
 *
 */
public class RestServiceProgressMonitor extends DefaultProgressMonitor implements IRestServiceProgressMonitor {

    public static final int STOPPED_WORK_INDICATOR = -1;

    private boolean isFailed;

    private boolean isDone;

    public RestServiceProgressMonitor(){
        super();
    }

    public String getTaskName(){
        return taskName;
    }

    public String getSubTask(){
        return subTask;
    }

    public int getTotalWork() {
        return totalWork;
    }

    public double getWorkDone() {
        return workDone;
    }

    public boolean isFailed() {
        return isFailed;
    }

    public void setIsFailed(boolean isStopped) {
        this.isFailed = isStopped;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.common.IProgressMonitor#worked(int)
     */
    @Override
    public void worked(int work) {
        if(work == -1){
            setIsFailed(true);
        }
        super.worked(work);
    }


    @Override
    public void internalWorked(double work) {
        if(work == -1){
            setIsFailed(true);
        }
        super.internalWorked(work);
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean isDone) {
        this.isDone = isDone;
    }

    @Override
    public void done() {
        this.isDone = true;
        super.done();
    }

}
