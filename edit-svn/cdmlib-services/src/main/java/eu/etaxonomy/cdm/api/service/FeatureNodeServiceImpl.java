// $Id$
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.persistence.dao.description.IFeatureNodeDao;

/**
 * @author n.hoffmann
 * @created Aug 5, 2010
 * @version 1.0
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = false)
public class FeatureNodeServiceImpl extends VersionableServiceBase<FeatureNode, IFeatureNodeDao> implements IFeatureNodeService {
	private static final Logger logger = Logger.getLogger(FeatureNodeServiceImpl.class);
	
	@Autowired
	protected void setDao(IFeatureNodeDao dao) {
		this.dao = dao;
	}
	
}
