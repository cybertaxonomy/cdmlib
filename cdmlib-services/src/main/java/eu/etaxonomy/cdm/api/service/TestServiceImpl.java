// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.UpdateResult.Status;
import eu.etaxonomy.cdm.api.service.dto.CdmEntityIdentifier;
import eu.etaxonomy.cdm.common.monitor.IRemotingProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.RemotingProgressMonitorThread;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author cmathew
 * @date 26 Jun 2015
 *
 */
@Service
public class TestServiceImpl implements ITestService {

    private static final Logger logger = Logger.getLogger(TestServiceImpl.class);

    @Autowired
    ITaxonNodeService taxonNodeService;

    @Autowired
    IProgressMonitorService progressMonitorService;

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITestService#wait(int)
     */
    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void waitFor(long timeToWaitInMs) throws InterruptedException {
        Thread.sleep(timeToWaitInMs);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITestService#returnResult(eu.etaxonomy.cdm.api.service.UpdateResult)
     */
    @Override
    public UpdateResult returnResult(UpdateResult result) {
        return result;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITestService#throwException(java.lang.Exception)
     */
    @Override
    public UpdateResult throwException(Exception ex) {
        throw new RuntimeException(ex);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITestService#addChild(eu.etaxonomy.cdm.api.service.dto.CdmEntityIdentifier)
     */
    @Override
    @Transactional(readOnly = false)
    public UpdateResult addChild(CdmEntityIdentifier taxonNodeCei) {
        TaxonNode taxonNode = taxonNodeService.find(taxonNodeCei.getId());
        TaxonNode child = taxonNode.addChildTaxon(Taxon.NewInstance(null, null), null, null);
        taxonNodeService.saveOrUpdate(child);
        UpdateResult result = new UpdateResult();
        result.addUpdatedCdmId(taxonNodeCei);
        result.setStatus(Status.OK);
        return result;
    }


    @Override
    public UUID monitLongRunningMethod(final RuntimeException ex, final List<String> feedbacks, final long feedbackWaitTimeout) {

        RemotingProgressMonitorThread monitorThread = new RemotingProgressMonitorThread() {
            @Override
            public Serializable doRun(IRemotingProgressMonitor monitor)  {
                Serializable result = longRunningMethod(monitor, ex, feedbacks, feedbackWaitTimeout);
                if(!monitor.isCanceled()) {
                    monitor.addReport("Report");
                }
                return result;
            }
        };

        UUID uuid = progressMonitorService.registerNewRemotingMonitor(monitorThread);
        monitorThread.setPriority(3);
        monitorThread.start();
        return uuid;
    }

    @Override
    public String longRunningMethod(IRemotingProgressMonitor monitor,
            RuntimeException ex,
            List<String> feedbacks,
            long feedbackWaitTimeout) {
        int noOfSteps = 10;
        int stepToThrowException = noOfSteps / 2;
        int stepToWaitForFeedback = noOfSteps / 2;
        monitor.beginTask("Long Running Task", noOfSteps);
        for(int i=0; i<noOfSteps; i++) {
            try {
                Thread.sleep(1000);
                if(i == stepToThrowException && ex != null) {
                    throw ex;
                }
                if(feedbacks != null && feedbacks.size() > 0 && i == stepToWaitForFeedback) {
                    for(String feedback : feedbacks) {
                        logger.warn("Monitor waiting for feedback");
                        if(feedbackWaitTimeout <= 0) {
                            monitor.waitForFeedback();
                        } else {
                            monitor.waitForFeedback(feedbackWaitTimeout);
                        }
                        logger.warn(" .. feedback received : " + monitor.getFeedback());
                        monitor.addReport(feedback);
                    }
                }
                if(monitor.isCanceled()) {
                    return "Cancelled";
                }
            } catch (InterruptedException e) {
                throw ex;
            }
            monitor.worked(1);
        }
        monitor.done();
        return "Success";
    }

}
