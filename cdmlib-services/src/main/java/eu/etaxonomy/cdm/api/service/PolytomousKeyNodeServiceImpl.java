/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.hibernate.HHH_9751_Util;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.persistence.dao.description.IPolytomousKeyDao;
import eu.etaxonomy.cdm.persistence.dao.description.IPolytomousKeyNodeDao;

/**
 * @author a.kohlbecker
 \* @since 24.03.2011
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

	@Autowired
	protected IPolytomousKeyDao keyDao;

	@Override
	public DeleteResult delete(UUID nodeUUID, boolean deleteChildren){
        DeleteResult result = new DeleteResult();
        PolytomousKeyNode node = dao.findByUuid(nodeUUID);
        node = HibernateProxyHelper.deproxy(node);
        if(node == null) {
           // result.addException(new Exception("The polytomouskey node was already deleted."));;
            return result;
        }
        List<PolytomousKeyNode> children = new ArrayList<PolytomousKeyNode>();

        node.removeNullValueFromChildren();
        for (PolytomousKeyNode child: node.getChildren()){
            children.add(child);
        }
        PolytomousKeyNode parent = node.getParent();
        parent = HibernateProxyHelper.deproxy(parent, PolytomousKeyNode.class);
        PolytomousKey key = null;
        if (parent == null){
            key = node.getKey();
            key = HibernateProxyHelper.deproxy(key, PolytomousKey.class);
        }

        if(!deleteChildren){

            for (PolytomousKeyNode child: children){
                if (!child.equals(node)){
                    parent.addChild(child);
                    dao.saveOrUpdate(child);
                    result.addUpdatedObject(child);
                }

            }


            dao.saveOrUpdate(node);
            result.addUpdatedObject(node);
        }
        if (parent!= null){
            if (parent.getChildren().contains(null)){
                List<PolytomousKeyNode> parentChildren = parent.getChildren();
                HHH_9751_Util.removeAllNull(parentChildren);
            }
            parent.removeChild(node);
            dao.saveOrUpdate(parent);
        }
        if (node.getKey() != null && key != null){
            key.setRoot(null);
        }
        if (node.getTaxon() != null){
            node.removeTaxon();
        }
        if (dao.delete(node) == null){
            result.setAbort();
        }else{
            result.addDeletedObject(node);
        }
        if (parent != null){
            dao.saveOrUpdate(parent);
            result.addUpdatedObject(parent);
        } else{
            keyDao.saveOrUpdate(key);
            result.addUpdatedObject(key);
        }

        return result;

    }

}
