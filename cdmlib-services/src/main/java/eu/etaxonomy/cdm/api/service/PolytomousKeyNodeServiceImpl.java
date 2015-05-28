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
	public DeleteResult delete(PolytomousKeyNode node, boolean deleteChildren){
		UUID uuid = node.getUuid();
		node = (PolytomousKeyNode)HibernateProxyHelper.deproxy(node);
		List<PolytomousKeyNode> children = node.getChildren();
		PolytomousKeyNode parent = node.getParent();
		if(!deleteChildren){

			for (PolytomousKeyNode child: children){
				parent.addChild(child);
				parent.removeChild(node);
				dao.update(child);
			}

			dao.update(node);
		}
		if (node.getParent()!= null){
			node.getParent().removeChild(node);
		}
		if (node.getKey().getRoot().equals(node)){
			node.getKey().setRoot(null);
		}
		dao.delete(node);
		dao.saveOrUpdate(parent);
		DeleteResult result = new DeleteResult();
		result.addUpdatedObject(parent);
		return result;

	}

	   @Override
	    public DeleteResult delete(UUID nodeUuid, boolean deleteChildren){
	       PolytomousKeyNode node = dao.load(nodeUuid);
	       return delete(node, deleteChildren);
	   }

}
