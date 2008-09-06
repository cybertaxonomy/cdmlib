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
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.persistence.dao.common.IOrderedTermVocabularyDao;
import eu.etaxonomy.cdm.persistence.dao.common.ITermVocabularyDao;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionDao;
import eu.etaxonomy.cdm.persistence.dao.description.IFeatureTreeDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.TermVocabularyDaoImpl;

/**
 * @author a.mueller
 * @created 24.06.2008
 * @version 1.0
 */
@Service
@Transactional(readOnly = true)
public class DescriptionServiceImpl extends IdentifiableServiceBase<DescriptionBase> implements IDescriptionService {
	private static final Logger logger = Logger.getLogger(DescriptionServiceImpl.class);

	protected IFeatureTreeDao featureTreeDao;
	protected ITermVocabularyDao vocabularyDao;
	
	
	@Autowired
	protected void setDao(IDescriptionDao dao) {
		this.dao = dao;
	}
	
	@Autowired
	protected void setFeatureTreeDao(IFeatureTreeDao featureTreeDao) {
		this.featureTreeDao = featureTreeDao;
	}
	
	@Autowired
	protected void setVocabularyDao(ITermVocabularyDao vocabularyDao) {
		this.vocabularyDao = vocabularyDao;
	}
	
	/**
	 * 
	 */
	public DescriptionServiceImpl() {
		logger.debug("Load DescriptionService Bean");
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
	
	public TermVocabulary<Feature> getFeatureVocabulary(UUID uuid){
		TermVocabulary<Feature> featureVocabulary;
		try {
			featureVocabulary = (TermVocabulary)vocabularyDao.findByUuid(uuid);
		} catch (ClassCastException e) {
			return null;
		}
		return featureVocabulary;
	}

	public TermVocabulary<Feature> getDefaultFeatureVocabulary(){
		String uuidFeature = "b187d555-f06f-4d65-9e53-da7c93f8eaa8";
		UUID featureUuid = UUID.fromString(uuidFeature);
		return getFeatureVocabulary(featureUuid);
	}


	
}
