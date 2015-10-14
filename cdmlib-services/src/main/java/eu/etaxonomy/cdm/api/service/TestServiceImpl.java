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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.UpdateResult.Status;
import eu.etaxonomy.cdm.api.service.dto.CdmEntityIdentifier;
import eu.etaxonomy.cdm.common.monitor.IRemotingProgressMonitor;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author cmathew
 * @date 26 Jun 2015
 *
 */
@Service
public class TestServiceImpl implements ITestService {

    @Autowired
    ITaxonNodeService taxonNodeService;

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITestService#wait(int)
     */
    @Override
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
        taxonNode.addChildTaxon(Taxon.NewInstance(null, null), null, null);
        UpdateResult result = new UpdateResult();
        result.addUpdatedCdmId(taxonNodeCei);
        result.setStatus(Status.OK);
        return result;
    }

    @Override
    public String execLongRunningMethod(IRemotingProgressMonitor monitor) {

    }

}
