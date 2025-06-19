/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.persistence.dao.description.IPolytomousKeyDao;
import eu.etaxonomy.cdm.persistence.dao.description.IPolytomousKeyNodeDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;

/**
 * @author a.mueller
 * @since 08.11.2010
 */
@Repository
public class PolytomousKeyDaoImpl
        extends IdentifiableDaoBase<PolytomousKey>
        implements IPolytomousKeyDao {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	@Autowired
	private IPolytomousKeyNodeDao nodeDao;

	public PolytomousKeyDaoImpl() {
		super(PolytomousKey.class);
//		indexedClasses = new Class[1];
//		indexedClasses[0] = PolytomousKey.class;
	}

	@Override
	public List<PolytomousKey> list() {
	    return super.list();
	}

	//FIXME rewrite as node has a key attribute now
	@Override
    public void loadNodes(PolytomousKeyNode root, List<String> nodePaths) {
		for(PolytomousKeyNode child : root.getChildren()) {
			defaultBeanInitializer.initialize(child, nodePaths);
			loadNodes(child,nodePaths);
		}
	}

	@Override
    public UUID delete(PolytomousKey key){
	    key = this.load(key.getUuid());
	    key = HibernateProxyHelper.deproxy(key);
	    if (key.getRoot() != null){
	       PolytomousKeyNode root = HibernateProxyHelper.deproxy(key.getRoot());
	       key.setRoot(null);
	       nodeDao.delete(root);
	    }
	    getSession().delete(key);
	    return key.getUuid();
	}
}