/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common.monitor;

import java.io.Serializable;

/**
 * Empty default implementation
 *
 * @author n.hoffmann
 */
public class NullProgressMonitor implements IProgressMonitor {

    static final long serialVersionUID = -1641601841621954955L;

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

    @Override
    public void waitForFeedback() {
        //  do nothing
    }

    @Override
    public void setFeedback(Serializable feedback) {
        //  do nothing
    }

    @Override
    public Serializable getFeedback() {
        return null;
    }

    @Override
    public boolean getIsWaitingForFeedback() {
        return false;
    }

    @Override
    public void waitForFeedback(long feedbackWaitTimeout) {
        //  do nothing
    }

    @Override
    public boolean hasFeedbackWaitTimedOut() {
        return false;
    }

    @Override
    public String getOwner() {
        return null;
    }

    @Override
    public void setOwner(String owner) {
        //  do nothing
    }

    @Override
    public void interrupt() {
        //  do nothing
    }
}