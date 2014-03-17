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

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.molecular.Primer;
import eu.etaxonomy.cdm.persistence.dao.molecular.IPrimerDao;

/**
 * @author pplitzner
 * @date 11.03.2014
 *
 */
@Service
@Transactional(readOnly = true)
public class PrimerServiceImpl extends AnnotatableServiceBase<Primer, IPrimerDao> implements IPrimerService{
    private static final Logger logger = Logger.getLogger(PreferenceServiceImpl.class);

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IPrimerService#getPrimerUuidAndTitleCache()
     */
    @Override
    public List<UuidAndTitleCache<Primer>> getPrimerUuidAndLabel() {
        return dao.getPrimerUuidAndTitleCache();
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ServiceBase#setDao(eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao)
     */
    @Override
    @Autowired
    protected void setDao(IPrimerDao dao) {
        this.dao = dao;
    }
}
