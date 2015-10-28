/**
 *
 */
package eu.etaxonomy.cdm.common.monitor;

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

}
