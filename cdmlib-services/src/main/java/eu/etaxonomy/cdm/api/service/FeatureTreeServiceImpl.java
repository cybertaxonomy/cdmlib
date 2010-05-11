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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VocabularyEnum;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dao.description.IFeatureNodeDao;
import eu.etaxonomy.cdm.persistence.dao.description.IFeatureTreeDao;

@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = false)
public class FeatureTreeServiceImpl extends IdentifiableServiceBase<FeatureTree, IFeatureTreeDao> implements IFeatureTreeService {

	private IFeatureNodeDao featureNodeDao;
	
	@Autowired
	private IVocabularyService vocabularyService;
	
	@Autowired
	protected void setDao(IFeatureTreeDao dao) {
		this.dao = dao;
	}
	
	@Autowired
	protected void setFeatureNodeDao(IFeatureNodeDao featureNodeDao) {
		this.featureNodeDao = featureNodeDao;
	}

	public void generateTitleCache() {
		// TODO Auto-generated method stub
	}

	public List<FeatureNode> getFeatureNodesAll() {
		return featureNodeDao.list();
	}
	
	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IFeatureTreeService#saveFeatureNodesAll(java.util.Collection)
	 */
	public Map<UUID, FeatureNode> saveFeatureNodesAll(
			Collection<FeatureNode> featureNodeCollection) {
		return featureNodeDao.saveAll(featureNodeCollection);
	} 

	public FeatureTree loadWithNodes(UUID uuid, List<String> propertyPaths, List<String> nodePaths) {
		nodePaths.add("children");
		
		List<String> rootPaths = new ArrayList<String>();
		rootPaths.add("root");
		for(String path : nodePaths) {
			rootPaths.add("root." + path);
		}
		
		if(propertyPaths != null) { 
		    rootPaths.addAll(propertyPaths);
		}
		
		FeatureTree featureTree = load(uuid, rootPaths);
		dao.loadNodes(featureTree.getRoot(),nodePaths);
		return featureTree;
	}
	
	/**
	 * Returns the featureTree specified by the given <code>uuid</code>.
	 * The specified featureTree either can be one of those stored in the CDM database or can be the 
	 * DefaultFeatureTree (contains all Features in use). 
	 * The uuid of the DefaultFeatureTree is defined in {@link IFeatureTreeService#DefaultFeatureTreeUuid}.
	 * The DefaultFeatureTree is also returned if no feature tree at all is stored in the cdm database.
	 *  
	 * @see eu.etaxonomy.cdm.api.service.ServiceBase#load(java.util.UUID, java.util.List)
	 */
	@Override
	public FeatureTree load(UUID uuid, List<String> propertyPaths) {
		if (uuid.equals(DefaultFeatureTreeUuid) || dao.count() == 0){
			return createDefaultFeatureTree();
		}
		return super.load(uuid, propertyPaths);
	}
	
	/**
	 * 
	 */
	private FeatureTree createDefaultFeatureTree() {
		
		TermVocabulary featureVocabulary = vocabularyService.getVocabulary(VocabularyEnum.Feature);
		
		List<Feature> featureList = new ArrayList<Feature>(featureVocabulary.getTerms());
				
		FeatureTree featureTree = FeatureTree.NewInstance(featureList);
		featureTree.setUuid(DefaultFeatureTreeUuid);
		save(featureTree);
		logger.info("Default feature tree created.");
		return featureTree;
	}
}
