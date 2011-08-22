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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.common.IProgressMonitor;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaUtils;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.ITaxonNodeComparator;
import eu.etaxonomy.cdm.model.taxon.ITreeNode;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dao.BeanInitializer;
import eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

/**
 * @author n.hoffmann
 * @created Sep 21, 2009
 * @version 1.0
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class ClassificationServiceImpl extends IdentifiableServiceBase<Classification, IClassificationDao> implements IClassificationService {
	private static final Logger logger = Logger.getLogger(ClassificationServiceImpl.class);

	@Autowired
	private ITaxonNodeDao taxonNodeDao;
	@Autowired
	private ITaxonDao taxonDao;
	@Autowired
	private BeanInitializer defaultBeanInitializer;
	private Comparator<? super TaxonNode> taxonNodeComparator;
	@Autowired
	public void setTaxonNodeComparator(ITaxonNodeComparator<? super TaxonNode> taxonNodeComparator){
		this.taxonNodeComparator = (Comparator<? super TaxonNode>) taxonNodeComparator;
	}

	
	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IClassificationService#loadTaxonNodeByTaxon(eu.etaxonomy.cdm.model.taxon.Taxon, java.util.UUID, java.util.List)
	 */
	public TaxonNode loadTaxonNodeByTaxon(Taxon taxon, UUID classificationUuid, List<String> propertyPaths){
		Classification tree = dao.load(classificationUuid);
		TaxonNode node = tree.getNode(taxon);
	
		return loadTaxonNode(node.getUuid(), propertyPaths);
	}
	
	@Deprecated // use loadTaxonNode(UUID, List<String>) instead 
	public TaxonNode loadTaxonNode(TaxonNode taxonNode, List<String> propertyPaths){
		return taxonNodeDao.load(taxonNode.getUuid(), propertyPaths);
	}
	
	public TaxonNode loadTaxonNode(UUID taxonNodeUuid, List<String> propertyPaths){
		return taxonNodeDao.load(taxonNodeUuid, propertyPaths);
	}
	
	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IClassificationService#loadRankSpecificRootNodes(eu.etaxonomy.cdm.model.taxon.Classification, eu.etaxonomy.cdm.model.name.Rank, java.util.List)
	 */
	public List<TaxonNode> loadRankSpecificRootNodes(Classification classification, Rank rank, List<String> propertyPaths){
		
		List<TaxonNode> rootNodes = dao.loadRankSpecificRootNodes(classification, rank, propertyPaths);
		
		//sort nodes by TaxonName
		Collections.sort(rootNodes, taxonNodeComparator);
		
		// initialize all nodes
		defaultBeanInitializer.initializeAll(rootNodes, propertyPaths);
		
		return rootNodes;
	}
	
	/**
     * (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITaxonService#loadTreeBranchTo(eu.etaxonomy.cdm.model.taxon.TaxonNode, eu.etaxonomy.cdm.model.name.Rank, java.util.List)
	 * FIXME Candidate for harmonization
	 * move to classification service
	 */
	public List<TaxonNode> loadTreeBranch(TaxonNode taxonNode, Rank baseRank, List<String> propertyPaths){
		
		TaxonNode thisNode = taxonNodeDao.load(taxonNode.getUuid(), propertyPaths);
		List<TaxonNode> pathToRoot = new ArrayList<TaxonNode>();
		pathToRoot.add(thisNode);
		
		TaxonNode parentNode = thisNode.getParent();
		while(parentNode != null){
			TaxonNode parent = parentNode;
			Rank parentNodeRank = parent.getTaxon().getName().getRank();
			// stop if the next parent is higher than the baseRank
			if(baseRank != null && baseRank.isLower(parentNodeRank)){
				break;
			}
			pathToRoot.add(parentNode);
			parentNode = parentNode.getParent();
		}
		
		// initialize and invert order of nodes in list
		defaultBeanInitializer.initializeAll(pathToRoot, propertyPaths);
		Collections.reverse(pathToRoot);
		
		return pathToRoot;
	}
	
	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IClassificationService#loadTreeBranchToTaxon(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.Classification, eu.etaxonomy.cdm.model.name.Rank, java.util.List)
	 */
	public List<TaxonNode> loadTreeBranchToTaxon(Taxon taxon, Classification classification, Rank baseRank, List<String> propertyPaths){
		Classification tree = dao.load(classification.getUuid());
		taxon = (Taxon) taxonDao.load(taxon.getUuid());
		TaxonNode node = tree.getNode(taxon);
		if(node == null){
			logger.warn("The specified taxon is not found in the given tree.");
			return null;
		}
		return loadTreeBranch(node, baseRank, propertyPaths);
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IClassificationService#loadChildNodesOfTaxon(eu.etaxonomy.cdm.model.taxon.TaxonNode, java.util.List)
	 */
	public List<TaxonNode> loadChildNodesOfTaxonNode(TaxonNode taxonNode,
			List<String> propertyPaths) {
		taxonNode = taxonNodeDao.load(taxonNode.getUuid());
		List<TaxonNode> childNodes = new ArrayList<TaxonNode>(taxonNode.getChildNodes());
		defaultBeanInitializer.initializeAll(childNodes, propertyPaths);
		Collections.sort(childNodes, taxonNodeComparator);
		return childNodes;
	}
	
	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IClassificationService#loadChildNodesOfTaxon(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.Classification, java.util.List)
	 */
	public List<TaxonNode> loadChildNodesOfTaxon(Taxon taxon, Classification classification, List<String> propertyPaths){
		Classification tree = dao.load(classification.getUuid());
		taxon = (Taxon) taxonDao.load(taxon.getUuid());
		
		TaxonNode node = tree.getNode(taxon);
		if(node != null){
			return loadChildNodesOfTaxonNode(node, propertyPaths);
		} else {
			return null;
		}
	}
	
	
	
	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IClassificationService#getTaxonNodeByUuid(java.util.UUID)
	 */
	public TaxonNode getTaxonNodeByUuid(UUID uuid) {
		return taxonNodeDao.findByUuid(uuid);
	}
	
	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IClassificationService#getTreeNodeByUuid(java.util.UUID)
	 */
	public ITreeNode getTreeNodeByUuid(UUID uuid){
		ITreeNode treeNode = taxonNodeDao.findByUuid(uuid);
		if(treeNode == null){
			treeNode = dao.findByUuid(uuid);
		}
		
		return treeNode;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IClassificationService#listClassifications(java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
	 */
	public List<Classification> listClassifications(Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
		return dao.list(limit, start, orderHints, propertyPaths);
	}	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IClassificationService#removeTaxonNode(eu.etaxonomy.cdm.model.taxon.Classification)
	 */
	public UUID removeTaxonNode(TaxonNode taxonNode) {
		return taxonNodeDao.delete(taxonNode);
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IClassificationService#removeTreeNode(eu.etaxonomy.cdm.model.taxon.ITreeNode)
	 */
	public UUID removeTreeNode(ITreeNode treeNode) {
		if(treeNode instanceof Classification){
			return dao.delete((Classification) treeNode);
		}else if(treeNode instanceof TaxonNode){
			return taxonNodeDao.delete((TaxonNode)treeNode);
		}
		return null;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IClassificationService#saveTaxonNode(eu.etaxonomy.cdm.model.taxon.Classification)
	 */
	public UUID saveTaxonNode(TaxonNode taxonNode) {
		return taxonNodeDao.save(taxonNode);
	}
	
	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IClassificationService#saveTaxonNodeAll(java.util.Collection)
	 */
	public Map<UUID, TaxonNode> saveTaxonNodeAll(
			Collection<TaxonNode> taxonNodeCollection) {
		return taxonNodeDao.saveAll(taxonNodeCollection);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IClassificationService#saveTreeNode(eu.etaxonomy.cdm.model.taxon.ITreeNode)
	 */
	public UUID saveTreeNode(ITreeNode treeNode) {
		if(treeNode instanceof Classification){
			return dao.save((Classification) treeNode);
		}else if(treeNode instanceof TaxonNode){
			return taxonNodeDao.save((TaxonNode)treeNode);
		}
		return null;
	}
	
	public List<TaxonNode> getAllNodes(){
		return taxonNodeDao.list(null,null);
	}
	
	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITaxonService#getUuidAndTitleCacheOfAcceptedTaxa(eu.etaxonomy.cdm.model.taxon.Classification)
	 */
	public List<UuidAndTitleCache<TaxonNode>> getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(Classification classification) {
		return taxonDao.getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(classification);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IdentifiableServiceBase#getUuidAndTitleCache()
	 */
	@Override
	public List<UuidAndTitleCache<Classification>> getUuidAndTitleCache() {
		return dao.getUuidAndTitleCache();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IClassificationService#getAllMediaForChildNodes(eu.etaxonomy.cdm.model.taxon.TaxonNode, java.util.List, int, int, int, java.lang.String[])
	 */
	public Map<UUID, List<MediaRepresentation>> getAllMediaForChildNodes(
			TaxonNode taxonNode, List<String> propertyPaths, int size,
			int height, int widthOrDuration, String[] mimeTypes) {
		
		TreeMap<UUID, List<MediaRepresentation>> result = new TreeMap<UUID, List<MediaRepresentation>>();
		List<Media> taxonMedia = new ArrayList<Media>();
		List<MediaRepresentation> mediaRepresentations = new ArrayList<MediaRepresentation>();
		
		//add all media of the children to the result map
		if (taxonNode != null){
					
			List<TaxonNode> nodes = new ArrayList<TaxonNode>();
			
			nodes.add(loadTaxonNode(taxonNode, propertyPaths));
			nodes.addAll(loadChildNodesOfTaxonNode(taxonNode, propertyPaths));
			
			if (nodes != null){
				for(TaxonNode node : nodes){
					Taxon taxon = node.getTaxon();
					for (TaxonDescription taxonDescription: taxon.getDescriptions()){
						for (DescriptionElementBase descriptionElement: taxonDescription.getElements()){
							for(Media media : descriptionElement.getMedia()){
								taxonMedia.add(media);
								
								//find the best matching representation
								mediaRepresentations.add(MediaUtils.findBestMatchingRepresentation(media,null, size, height, widthOrDuration, mimeTypes));
								
							}
						}
					}
					result.put(taxon.getUuid(), mediaRepresentations);
										
				}	
			}
			
		}
		
		
		return result;
		
	}
	
	public Map<UUID, List<MediaRepresentation>> getAllMediaForChildNodes(Taxon taxon, Classification taxTree, List<String> propertyPaths, int size, int height, int widthOrDuration, String[] mimeTypes){
		TaxonNode node = taxTree.getNode(taxon);
		
		return getAllMediaForChildNodes(node, propertyPaths, size, height, widthOrDuration, mimeTypes);
	}
	
	
	
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ServiceBase#setDao(eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao)
	 */
	@Autowired
	protected void setDao(IClassificationDao dao) {
		this.dao = dao;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IIdentifiableEntityService#updateTitleCache(java.lang.Integer, eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy)
	 */
	@Override
	public void updateTitleCache(Class<? extends Classification> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<Classification> cacheStrategy, IProgressMonitor monitor) {
		if (clazz == null){
			clazz = Classification.class;
		}
		super.updateTitleCacheImpl(clazz, stepSize, cacheStrategy, monitor);
	}


}
