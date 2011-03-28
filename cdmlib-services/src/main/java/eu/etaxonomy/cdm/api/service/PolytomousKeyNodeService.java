// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.persistence.dao.description.IPolytomousKeyNodeDao;

/**
 * @author a.kohlbecker
 * @date 24.03.2011
 *
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = false)
public class PolytomousKeyNodeService  extends VersionableServiceBase<PolytomousKeyNode, IPolytomousKeyNodeDao> implements IPolytomousKeyNodeService {


	@Autowired
	protected void setDao(IPolytomousKeyNodeDao dao) {
		this.dao = dao;
	}

}
