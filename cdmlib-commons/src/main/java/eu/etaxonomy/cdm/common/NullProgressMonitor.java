/**
 * 
 */
package eu.etaxonomy.cdm.common;

/**
 * Empty default implementation
 * 
 * @author n.hoffmann
 *
 */
public class NullProgressMonitor implements IProgressMonitor {

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.common.IProgressMonitor#beginTask(java.lang.String, int)
	 */
	@Override
	public void beginTask(String name, int totalWork) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.common.IProgressMonitor#done()
	 */
	@Override
	public void done() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.common.IProgressMonitor#isCanceled()
	 */
	@Override
	public boolean isCanceled() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.common.IProgressMonitor#setCanceled(boolean)
	 */
	@Override
	public void setCanceled(boolean value) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.common.IProgressMonitor#setTaskName(java.lang.String)
	 */
	@Override
	public void setTaskName(String name) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.common.IProgressMonitor#subTask(java.lang.String)
	 */
	@Override
	public void subTask(String name) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.common.IProgressMonitor#worked(int)
	 */
	@Override
	public void worked(int work) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.common.IProgressMonitor#warning(java.lang.String)
	 */
	@Override
	public void warning(String message) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.common.IProgressMonitor#warning(java.lang.String, java.lang.Exception)
	 */
	@Override
	public void warning(String message, Throwable throwable) {
		// TODO Auto-generated method stub

	}

}
