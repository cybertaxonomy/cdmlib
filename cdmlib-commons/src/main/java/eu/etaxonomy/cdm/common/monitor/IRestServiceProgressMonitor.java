package eu.etaxonomy.cdm.common.monitor;



public interface IRestServiceProgressMonitor extends IProgressMonitor{

    public String getTaskName();

    public String getSubTask();

    /**
     * @return the percentage with an accuracy of two decimal digits
     * of the work done, or <code>null</code>
     * in case the <code>totalWork</code> was <code>0</code>.
     */
    public Double getPercentage();

    public int getTotalWork();

    public double getWorkDone();

    /**
     * Whether an <code>Exception</code> or another event was causing the monitored process to stop before it has been fully worked.
     *
     * @return
     */
    public boolean isFailed();

    /**
     * Should be set true if an <code>Exception</code> or another event was causing the monitored process to stop before it has been fully worked.
     *
     * @param isFailed
     */
    public void setIsFailed(boolean isFailed);

    public boolean isDone();

    public void setDone(boolean isDone);

    /**
     * Should be set if original request has to be specified
     * @param downloadPath
     * @return
     */
    public void setOrigin(String origin);

    public String getOrigin();


}
