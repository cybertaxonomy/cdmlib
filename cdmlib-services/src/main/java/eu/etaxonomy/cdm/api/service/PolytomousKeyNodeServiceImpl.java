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

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.persistence.dao.description.IPolytomousKeyNodeDao;

/**
 * @author a.kohlbecker
 * @date 24.03.2011
 *
 */
@Service
@Transactional(readOnly = false)
public class PolytomousKeyNodeServiceImpl  extends VersionableServiceBase<PolytomousKeyNode, IPolytomousKeyNodeDao> implements IPolytomousKeyNodeService {


	@Override
    @Autowired
	protected void setDao(IPolytomousKeyNodeDao dao) {
		this.dao = dao;
	}

	@Override
	public DeleteResult delete(UUID nodeUUID, boolean deleteChildren){
		DeleteResult result = new DeleteResult();
		PolytomousKeyNode node = dao.findByUuid(nodeUUID);
		node = (PolytomousKeyNode)HibernateProxyHelper.deproxy(node);
		if(node == null) {
		    return null;
		}
		List<PolytomousKeyNode> children = node.getChildren();
		PolytomousKeyNode parent = node.getParent();
		parent = HibernateProxyHelper.deproxy(parent, PolytomousKeyNode.class);

		if(!deleteChildren){

			for (PolytomousKeyNode child: children){
				parent.addChild(child);
				parent.removeChild(node);
				dao.update(child);
				result.addUpdatedObject(child);
			}

			dao.update(node);
			result.addUpdatedObject(node);
		}
		if (node.getParent()!= null){
			node.getParent().removeChild(node);
		}
		if (node.getKey().getRoot().equals(node)){
			node.getKey().setRoot(null);
		}
		if (dao.delete(node) == null){
			result.setAbort();
		}
		dao.saveOrUpdate(parent);
		result.addUpdatedObject(parent);
		return result;

	}

}
