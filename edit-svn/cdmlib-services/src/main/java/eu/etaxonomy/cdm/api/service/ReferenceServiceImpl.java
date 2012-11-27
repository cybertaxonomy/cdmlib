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

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;


@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class ReferenceServiceImpl extends IdentifiableServiceBase<Reference,IReferenceDao> implements IReferenceService {
	
	static Logger logger = Logger.getLogger(ReferenceServiceImpl.class);

	/**
	 * Constructor
	 */
	public ReferenceServiceImpl(){
		if (logger.isDebugEnabled()) { logger.debug("Load ReferenceService Bean"); }
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IIdentifiableEntityService#updateTitleCache(java.lang.Integer, eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy)
	 */
	@Override
	@Transactional(readOnly = false)
    public void updateTitleCache(Class<? extends Reference> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<Reference> cacheStrategy, IProgressMonitor monitor) {
		if (clazz == null){
			clazz = Reference.class;
		}
		super.updateTitleCacheImpl(clazz, stepSize, cacheStrategy, monitor);
	}


	@Autowired
	protected void setDao(IReferenceDao dao) {
		this.dao = dao;
	}

	public List<UuidAndTitleCache<Reference>> getUuidAndTitle() {
		
		return dao.getUuidAndTitle();
	}
	
	public List<Reference> getAllReferencesForPublishing(){
		return dao.getAllNotNomenclaturalReferencesForPublishing();
	}

	public List<Reference> getAllNomenclaturalReferences() {
		
		return dao.getAllNomenclaturalReferences();
	}

	@Override
	public List<TaxonBase> listCoveredTaxa(Reference reference, boolean includeSubordinateReferences, List<String> propertyPaths) {
		
		List<TaxonBase> taxonList = dao.listCoveredTaxa(reference, includeSubordinateReferences, propertyPaths);
		
		return taxonList;
	}

}
