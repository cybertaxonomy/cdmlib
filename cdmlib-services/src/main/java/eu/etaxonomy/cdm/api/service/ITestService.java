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

import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.api.service.dto.CdmEntityIdentifier;
import eu.etaxonomy.cdm.common.monitor.IRemotingProgressMonitor;

/**
 * Service interface for the testing of client applications using the service
 * layer remotely (like the Taxonomic Editor)
 *
 * @author cmathew
 * @date 26 Jun 2015
 *
 */
public interface ITestService {


    public void waitFor(long timeToWaitInMs) throws InterruptedException ;

    public UpdateResult returnResult(UpdateResult result);

    public UpdateResult throwException(Exception ex);

    public UpdateResult addChild(CdmEntityIdentifier taxonNodeCei);


    /**
     * @param monitor
     * @return
     */
    public String longRunningMethod(IRemotingProgressMonitor monitor, RuntimeException ex, List<String> feedbacks);


    /**
     * @return
     */
    public UUID monitLongRunningMethod(RuntimeException ex, List<String> feedbacks);





}
