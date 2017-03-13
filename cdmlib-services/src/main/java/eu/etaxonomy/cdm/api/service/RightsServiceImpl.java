/**
* Copyright (C) 2017 EDIT
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

import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.persistence.dao.common.IRightsDao;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;

/**
 * @author k.luther
 * @date 15.02.2017
 *
 */
@Service
@Transactional(readOnly = true)
public class RightsServiceImpl extends AnnotatableServiceBase<Rights, IRightsDao> implements IRightsService {
    private static final Logger logger = Logger.getLogger(RightsServiceImpl.class);
    /**
     * {@inheritDoc}
     */
    @Override
    @Autowired
    protected void setDao(IRightsDao dao) {
        this.dao = dao;
    }

    /**
     * Constructor
     */
    public RightsServiceImpl(){
        if (logger.isDebugEnabled()) { logger.debug("Load RightsService Bean"); }
    }

    @Override
    public List<UuidAndTitleCache<Rights>> getUuidAndLabelText(Integer limit, String pattern){
        return dao.getUuidAndTitleCache(limit, pattern);
    }




}
