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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.common.IProgressMonitor;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.persistence.dao.description.IPolytomousKeyDao;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = false)
public class PolytomousKeyServiceImpl extends IdentifiableServiceBase<PolytomousKey, IPolytomousKeyDao> implements IPolytomousKeyService {

	@Autowired
	protected void setDao(IPolytomousKeyDao dao) {
		this.dao = dao;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IIdentifiableEntityService#updateTitleCache(java.lang.Integer, eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy)
	 */
	@Override
	public void updateTitleCache(Class<? extends PolytomousKey> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<PolytomousKey> cacheStrategy, IProgressMonitor monitor) {
		if (clazz == null){
			clazz = PolytomousKey.class;
		}
		super.updateTitleCacheImpl(clazz, stepSize, cacheStrategy, monitor);
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IFeatureTreeService#loadWithNodes(java.util.UUID, java.util.List, java.util.List)
	 */
	public PolytomousKey loadWithNodes(UUID uuid, List<String> propertyPaths, List<String> nodePaths) {
		nodePaths.add("children");
		
		List<String> rootPaths = new ArrayList<String>();
		rootPaths.add("root");
		for(String path : nodePaths) {
			rootPaths.add("root." + path);
		}
		
		if(propertyPaths != null) { 
		    rootPaths.addAll(propertyPaths);
		}
		
		PolytomousKey polytomousKey = load(uuid, rootPaths);
		dao.loadNodes(polytomousKey.getRoot(),nodePaths);
		return polytomousKey;
	}
	
	/**
	 * Returns the polytomous key specified by the given <code>uuid</code>.
	 * The specified polytomous key either can be one of those stored in the CDM database or can be the 
	 * DefaultFeatureTree (contains all Features in use). 
	 * The uuid of the DefaultFeatureTree is defined in {@link IFeatureTreeService#DefaultFeatureTreeUuid}.
	 * The DefaultFeatureTree is also returned if no feature tree at all is stored in the cdm database.
	 *  
	 * @see eu.etaxonomy.cdm.api.service.ServiceBase#load(java.util.UUID, java.util.List)
	 */
	@Override
	public PolytomousKey load(UUID uuid, List<String> propertyPaths) {
		return super.load(uuid, propertyPaths);
	}
	
}
