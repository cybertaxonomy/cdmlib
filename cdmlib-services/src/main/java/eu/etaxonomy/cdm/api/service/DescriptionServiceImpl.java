/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionDao;
import eu.etaxonomy.cdm.persistence.dao.description.IFeatureTreeDao;

/**
 * @author a.mueller
 * @created 24.06.2008
 * @version 1.0
 */
@Service
@Transactional(readOnly = true)
public class DescriptionServiceImpl extends IdentifiableServiceBase<DescriptionBase> implements IDescriptionService {
	private static final Logger logger = Logger.getLogger(DescriptionServiceImpl.class);

	IFeatureTreeDao featureTreeDao;
	
	@Autowired
	protected void setDao(IDescriptionDao dao) {
		this.dao = dao;
	}
	
	@Autowired
	protected void setFeatureTreeDao(IFeatureTreeDao featureTreeDao) {
		this.featureTreeDao = featureTreeDao;
	}
	
	/**
	 * 
	 */
	public DescriptionServiceImpl() {
		logger.info("Load DescriptionService Bean");
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDescriptionService#getDescriptionBaseByUuid(java.util.UUID)
	 */
	public DescriptionBase getDescriptionBaseByUuid(UUID uuid) {
		return super.getCdmObjectByUuid(uuid);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDescriptionService#saveDescription(eu.etaxonomy.cdm.model.description.DescriptionBase)
	 */
	@Transactional(readOnly = false)
	public UUID saveDescription(DescriptionBase description) {
		return super.saveCdmObject(description);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IIdentifiableEntityService#generateTitleCache()
	 */
	public void generateTitleCache() {
		logger.warn("generateTitleCache not yet implemented");
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDescriptionService#saveFeatureTree(eu.etaxonomy.cdm.model.description.FeatureTree)
	 */
	@Transactional(readOnly = false)
	public UUID saveFeatureTree(FeatureTree tree) {
		return featureTreeDao.saveOrUpdate(tree);
	}

	
}
