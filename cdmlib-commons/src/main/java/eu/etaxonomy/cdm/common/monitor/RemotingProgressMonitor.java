// $Id$
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
import java.util.ArrayList;
import java.util.List;


/**
 * @author cmathew
 * @date 14 Oct 2015
 *
 */
public class RemotingProgressMonitor extends RestServiceProgressMonitor implements IRemotingProgressMonitor {

    private Serializable result;
    private List<String> reports = new ArrayList<String>();
    private String owner;
    private Serializable feedback;
    private transient Object feedbackLock;
    private boolean isWaitingForFeedback = false;


    /**
     * {@inheritDoc}
     */
    @Override
    public Object getResult() {
        return result;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setResult(Serializable result) {
        this.result = result;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getReports() {
        return reports;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void addReport(String report) {
        reports.add(report);
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
    public void waitForFeedback() {
        if(feedbackLock == null) {
            feedbackLock =  new Object();
        }
        synchronized (feedbackLock) {
            feedback = null;
            while(feedback == null) {
                isWaitingForFeedback = true;
                try {
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
    public boolean isWaitingForFeedback() {
        return isWaitingForFeedback;
    }

}
