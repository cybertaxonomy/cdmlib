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

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.AbstractPagerImpl;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.common.IProgressMonitor;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.description.IIdentificationKeyDao;
import eu.etaxonomy.cdm.persistence.dao.description.IPolytomousKeyDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = false)
public class PolytomousKeyServiceImpl extends IdentifiableServiceBase<PolytomousKey, IPolytomousKeyDao> implements IPolytomousKeyService {

	private IIdentificationKeyDao identificationKeyDao;
	private ITaxonDao taxonDao;


	@Autowired
	protected void setDao(IPolytomousKeyDao dao) {
		this.dao = dao;
	}
	
	@Autowired
	protected void setDao(IIdentificationKeyDao identificationKeyDao) {
		this.identificationKeyDao = identificationKeyDao;
	}
	
	@Autowired
	protected void setDao(ITaxonDao taxonDao) {
		this.taxonDao = taxonDao;
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
		
		if(nodePaths == null){
			nodePaths = new ArrayList<String>();
		}
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


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IPolytomousKeyService#findByTaxonomicScope(eu.etaxonomy.cdm.model.taxon.TaxonBase, java.lang.Class, java.lang.Integer, java.lang.Integer, java.util.List)
	 */
	@Override
	public Pager<PolytomousKey> findByTaxonomicScope(
			TaxonBase taxon, Integer pageSize,
			Integer pageNumber, List<String> propertyPaths) {
		
		List<PolytomousKey> list = new ArrayList<PolytomousKey>();
		taxon = taxonDao.findById(taxon.getId());
		Integer numberOfResults = identificationKeyDao.countByTaxonomicScope(taxon, PolytomousKey.class).intValue();
		if(AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)){
			list = identificationKeyDao.findByTaxonomicScope(taxon, PolytomousKey.class, pageSize, pageNumber, propertyPaths);
		}
		Pager<PolytomousKey> pager = new DefaultPagerImpl<PolytomousKey>(pageNumber, numberOfResults, pageSize, list);
		return pager;
	}
	
}
