// $Id$
/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.molecular.Amplification;
import eu.etaxonomy.cdm.model.molecular.SingleRead;
import eu.etaxonomy.cdm.persistence.dao.molecular.IAmplificationDao;

/**
 * @author pplitzner
 * @date 11.03.2014
 *
 */
@Service
@Transactional(readOnly = true)
public class AmplificationServiceImpl extends AnnotatableServiceBase<Amplification, IAmplificationDao> implements IAmplificationService{
    private static final Logger logger = Logger.getLogger(PreferenceServiceImpl.class);

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ServiceBase#setDao(eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao)
     */
    @Override
    @Autowired
    protected void setDao(IAmplificationDao dao) {
        this.dao = dao;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IAmplificationService#moveSingleRead(eu.etaxonomy.cdm.model.molecular.Amplification, eu.etaxonomy.cdm.model.molecular.Amplification, eu.etaxonomy.cdm.model.molecular.SingleRead)
     */
    @Override
    public boolean moveSingleRead(Amplification from, Amplification to, SingleRead singleRead) {
        from.removeSingleRead(singleRead);
        saveOrUpdate(from);
        to.addSingleRead(singleRead);
        saveOrUpdate(to);
        return true;
    }
}
