/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.api.service.lsid.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import eu.etaxonomy.cdm.api.service.lsid.LSIDRegistry;
import eu.etaxonomy.cdm.model.common.IIdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.LSIDAuthority;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import eu.etaxonomy.cdm.persistence.dao.common.ILsidAuthorityDao;

@Component
public class LsidRegistryImpl implements LSIDRegistry {
	private static Log log = LogFactory.getLog(LsidRegistryImpl.class);
    //	 the main registry, stores all pattern mappings.
	private Map<String,IIdentifiableDao<? extends IdentifiableEntity>> registry = new HashMap<>();

	private Set<IIdentifiableDao> identifiableDaos;

	private ILsidAuthorityDao lsidAuthorityDao;

	protected PlatformTransactionManager transactionManager;

	protected DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition();

	@Autowired
	public void setIdentifiableDaos(Set<IIdentifiableDao> identifiableDaos) {
		this.identifiableDaos = identifiableDaos;
	}

	@Autowired
	public void setLsidAuthorityDao(ILsidAuthorityDao lsidAuthorityDao) {
		this.lsidAuthorityDao = lsidAuthorityDao;
	}

	@Autowired
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	@PostConstruct
	public void init() {
		registry = new HashMap<String,IIdentifiableDao<? extends IdentifiableEntity>>();
		TransactionStatus txStatus = transactionManager.getTransaction(txDefinition);
		for(LSIDAuthority lsidAuthority : lsidAuthorityDao.list(null, null)) {
			for(String namespace : lsidAuthority.getNamespaces().keySet()) {
				Class<? extends IIdentifiableEntity> clazz = lsidAuthority.getNamespaces().get(namespace);
				boolean foundDao = false;
				for(IIdentifiableDao identifiableDao : identifiableDaos) {
					if(clazz.equals(identifiableDao.getType())) {
						foundDao = true;
						registry.put(lsidAuthority.getAuthority() + ":" + namespace, identifiableDao);
						break;
					}
				}

				if(!foundDao) {
					log.warn("Did not find DAO serving classes of type " + clazz + " for authority " + lsidAuthority.getAuthority() + " with namespace " + namespace);
				}
			}
		}

		transactionManager.commit(txStatus);
	}

	@Override
    public IIdentifiableDao<? extends IdentifiableEntity> lookupDAO(LSID lsid) {
        //		 if the LSID is null, then we return whatever is registered with the no lsid pattern

		if (lsid == null) {
			IIdentifiableDao<? extends IdentifiableEntity> identifiableDAO = registry.get(Pattern.NO_LSID_PATTERN.toString());
			return identifiableDAO;
		}

		Pattern lookup = new Pattern();
		lookup.setAuthority(lsid.getAuthority().toString());
		lookup.setNamespace(lsid.getNamespace());

		IIdentifiableDao<? extends IdentifiableEntity> identifiableDAO = registry.get(lookup.toString());
		if (identifiableDAO != null) {
			log.info("Found DAO " + identifiableDAO.getClass().getSimpleName());
			return identifiableDAO;
		}
		log.info("Didn't find dao, returning null");
		return null;
	}
}
