// $Id$
/**
* Copyright (C) 2013 EDIT
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

import eu.etaxonomy.cdm.model.common.EventBase;
import eu.etaxonomy.cdm.persistence.dao.common.IEventBaseDao;

/**
 * @author a.kohlbecker
 * @date Jan 9, 2013
 *
 */
@Service
@Transactional(readOnly = true)
public class EventBaseServiceImpl extends AnnotatableServiceBase<EventBase, IEventBaseDao> implements IEventBaseService {

    @Override
    @Autowired
    protected void setDao(IEventBaseDao dao) {
        this.dao = dao;
    }

}
