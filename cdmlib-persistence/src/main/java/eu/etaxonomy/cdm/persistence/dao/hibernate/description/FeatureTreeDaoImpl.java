/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VocabularyEnum;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.persistence.dao.common.ITermVocabularyDao;
import eu.etaxonomy.cdm.persistence.dao.description.IFeatureTreeDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;

/**
 * @author a.mueller
 * @created 10.07.2008
 * @version 1.0
 */
@Repository
public class FeatureTreeDaoImpl extends IdentifiableDaoBase<FeatureTree> implements IFeatureTreeDao{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FeatureTreeDaoImpl.class);
	
	@Autowired
	private ITermVocabularyDao termVocabularyDao;

	public FeatureTreeDaoImpl() {
		super(FeatureTree.class); 
		indexedClasses = new Class[2];
		indexedClasses[0] = FeatureTree.class;
		indexedClasses[1] = PolytomousKey.class;
	}
	
	public List<FeatureTree> list() {
		Criteria crit = getSession().createCriteria(type); 
		return crit.list(); 
	}

	public void loadNodes(FeatureNode root, List<String> nodePaths) {
		for(FeatureNode child : root.getChildren()) {
			defaultBeanInitializer.initialize(child, nodePaths);
			loadNodes(child,nodePaths);
		}
	}

	@Override
	public FeatureTree load(UUID uuid, List<String> propertyPaths) {
		if (uuid.equals(DefaultFeatureTreeUuid) || count() == 0){
			return createDefaultFeatureTree();
		}
		return super.load(uuid, propertyPaths);
	}

	@Override
	public FeatureTree load(UUID uuid) {
		if (uuid.equals(DefaultFeatureTreeUuid) || count() == 0){
			return createDefaultFeatureTree();
		}
		return super.load(uuid);
	}
	
	/**
	 * 
	 */
	private FeatureTree createDefaultFeatureTree() {
		
		TermVocabulary featureVocabulary = termVocabularyDao.findByUuid(VocabularyEnum.Feature.getUuid());
		
		List<Feature> featureList = new ArrayList<Feature>(featureVocabulary.getTerms());
		List<Feature> selectedFeatures = new ArrayList<Feature>();
		for(Feature feature : featureList){
			if(!feature.equals(Feature.INDIVIDUALS_ASSOCIATION())){
				selectedFeatures.add(feature);
			}
		}
		FeatureTree featureTree = FeatureTree.NewInstance(selectedFeatures);
		featureTree.setUuid(DefaultFeatureTreeUuid);
		return featureTree;
	}

	
}
