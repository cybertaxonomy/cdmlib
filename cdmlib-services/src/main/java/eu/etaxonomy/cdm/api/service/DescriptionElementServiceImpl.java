/**
* Copyright (C) 2007 EDIT
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

import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionElementDao;

/**
 * Note: methods in this service were copied from originally being in DescriptionService
 *
 * @author a.mueller
 * @since 12.2021
 */
@Service
@Transactional(readOnly = true)
public class DescriptionElementServiceImpl
        extends AnnotatableServiceBase<DescriptionElementBase,IDescriptionElementDao>
        implements IDescriptionElementService {

    private static final Logger logger = Logger.getLogger(DescriptionElementServiceImpl.class);

    @Override
    @Autowired
    protected void setDao(IDescriptionElementDao dao) {
        this.dao = dao;
    }

    public DescriptionElementServiceImpl() {
        if (logger.isDebugEnabled()){logger.debug("Load DescriptionElementService Bean");}
    }

}
