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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.config.CreateHierarchyForClassificationConfigurator;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.PagerUtils;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaUtils;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.ITaxonNodeComparator;
import eu.etaxonomy.cdm.model.taxon.ITaxonTreeNode;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeDao;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

/**
 * @author n.hoffmann
 * @created Sep 21, 2009
 */
@Service
@Transactional(readOnly = true)
public class ClassificationServiceImpl extends IdentifiableServiceBase<Classification, IClassificationDao>
    implements IClassificationService {
    private static final Logger logger = Logger.getLogger(ClassificationServiceImpl.class);

    @Autowired
    private ITaxonNodeDao taxonNodeDao;

    @Autowired
    private ITaxonDao taxonDao;

    @Autowired
    private IBeanInitializer defaultBeanInitializer;

    @Override
    @Autowired
    protected void setDao(IClassificationDao dao) {
        this.dao = dao;
    }

    private Comparator<? super TaxonNode> taxonNodeComparator;

    @Autowired
    public void setTaxonNodeComparator(ITaxonNodeComparator<? super TaxonNode> taxonNodeComparator){
        this.taxonNodeComparator = (Comparator<? super TaxonNode>) taxonNodeComparator;
    }

    @Override
    public TaxonNode loadTaxonNodeByTaxon(Taxon taxon, UUID classificationUuid, List<String> propertyPaths){
        Classification tree = dao.load(classificationUuid);
        TaxonNode node = tree.getNode(taxon);

        return loadTaxonNode(node.getUuid(), propertyPaths);
    }

    @Override
    @Deprecated // use loadTaxonNode(UUID, List<String>) instead
    public TaxonNode loadTaxonNode(TaxonNode taxonNode, List<String> propertyPaths){
        return taxonNodeDao.load(taxonNode.getUuid(), propertyPaths);
    }

    public TaxonNode loadTaxonNode(UUID taxonNodeUuid, List<String> propertyPaths){
        return taxonNodeDao.load(taxonNodeUuid, propertyPaths);
    }

    @Override
    @Deprecated
    public List<TaxonNode> loadRankSpecificRootNodes(Classification classification, Rank rank, Integer limit, Integer start, List<String> propertyPaths){

        List<TaxonNode> rootNodes = dao.listRankSpecificRootNodes(classification, rank, limit , start, propertyPaths);

        //sort nodes by TaxonName
        Collections.sort(rootNodes, taxonNodeComparator);

        // initialize all nodes
        defaultBeanInitializer.initializeAll(rootNodes, propertyPaths);

        return rootNodes;
    }

    @Override
    public List<TaxonNode> listRankSpecificRootNodes(Classification classification, Rank rank, Integer pageSize,
            Integer pageIndex, List<String> propertyPaths) {
        return pageRankSpecificRootNodes(classification, rank, pageSize, pageIndex, propertyPaths).getRecords();
    }

    @Override
    public Pager<TaxonNode> pageRankSpecificRootNodes(Classification classification, Rank rank, Integer pageSize,
            Integer pageIndex, List<String> propertyPaths) {
        Long numberOfResults = dao.countRankSpecificRootNodes(classification, rank);

        List<TaxonNode> results = new ArrayList<TaxonNode>();
        if (numberOfResults > 0) { // no point checking again

            results = dao.listRankSpecificRootNodes(classification, rank, PagerUtils.limitFor(pageSize),
                    PagerUtils.startFor(pageSize, pageIndex), propertyPaths);
        }

        Collections.sort(results, taxonNodeComparator); // FIXME this is only a HACK, order during the hibernate query in the dao
        return new DefaultPagerImpl<TaxonNode>(pageIndex, numberOfResults.intValue(), pageSize, results);

    }

    /**
     * @implements {@link IClassificationService#loadTreeBranch(TaxonNode, Rank, List)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#loadTreeBranchTo(eu.etaxonomy.cdm.model.taxon.TaxonNode, eu.etaxonomy.cdm.model.name.Rank, java.util.List)
     * FIXME Candidate for harmonization
     * move to classification service
     */
    @Override
    public List<TaxonNode> loadTreeBranch(TaxonNode taxonNode, Rank baseRank, List<String> propertyPaths){

        TaxonNode thisNode = taxonNodeDao.load(taxonNode.getUuid(), propertyPaths);
        List<TaxonNode> pathToRoot = new ArrayList<TaxonNode>();
        pathToRoot.add(thisNode);

        while(!thisNode.isTopmostNode()){
            //TODO why do we need to deproxy here?
            //     without this thisNode.getParent() will return NULL in
            //     some cases (environment dependend?) even if the parent exits
            TaxonNode parentNode = CdmBase.deproxy(thisNode, TaxonNode.class).getParent();

            if(parentNode == null){
                throw new NullPointerException("taxonNode " + thisNode + " must have a parent since it is not top most");
            }
            if(parentNode.getTaxon() == null){
                throw new NullPointerException("The taxon associated with taxonNode " + parentNode + " is NULL");
            }
            if(parentNode.getTaxon().getName() == null){
                throw new NullPointerException("The name of the taxon associated with taxonNode " + parentNode + " is NULL");
            }

            Rank parentNodeRank = parentNode.getTaxon().getName() == null ? null : parentNode.getTaxon().getName().getRank();
            // stop if the next parent is higher than the baseRank
            if(baseRank != null && parentNodeRank != null && baseRank.isLower(parentNodeRank)){
                break;
            }

            pathToRoot.add(parentNode);
            thisNode = parentNode;
        }

        // initialize and invert order of nodes in list
        defaultBeanInitializer.initializeAll(pathToRoot, propertyPaths);
        Collections.reverse(pathToRoot);

        return pathToRoot;
    }

    @Override
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


    @Override
    public List<TaxonNode> loadChildNodesOfTaxonNode(TaxonNode taxonNode,
            List<String> propertyPaths) {
        taxonNode = taxonNodeDao.load(taxonNode.getUuid());
        List<TaxonNode> childNodes = new ArrayList<TaxonNode>(taxonNode.getChildNodes());
        defaultBeanInitializer.initializeAll(childNodes, propertyPaths);
        Collections.sort(childNodes, taxonNodeComparator);
        return childNodes;
    }

    @Override
    public List<TaxonNode> listChildNodesOfTaxon(UUID taxonUuid, UUID classificationUuid, Integer pageSize,
            Integer pageIndex, List<String> propertyPaths){

        Classification classification = dao.load(classificationUuid);
        Taxon taxon = (Taxon) taxonDao.load(taxonUuid);

        List<TaxonNode> results = dao.listChildrenOf(taxon, classification, pageSize, pageIndex, propertyPaths);
        Collections.sort(results, taxonNodeComparator); // FIXME this is only a HACK, order during the hibernate query in the dao
        return results;
    }

    @Override
    public TaxonNode getTaxonNodeByUuid(UUID uuid) {
        return taxonNodeDao.findByUuid(uuid);
    }

    @Override
    public ITaxonTreeNode getTreeNodeByUuid(UUID uuid){
        ITaxonTreeNode treeNode = taxonNodeDao.findByUuid(uuid);
        if(treeNode == null){
            treeNode = dao.findByUuid(uuid);
        }

        return treeNode;
    }

    @Override
    public List<Classification> listClassifications(Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        return dao.list(limit, start, orderHints, propertyPaths);
    }

    @Override
    public UUID removeTaxonNode(TaxonNode taxonNode) {
        return taxonNodeDao.delete(taxonNode);
    }
    @Override
    public UUID removeTreeNode(ITaxonTreeNode treeNode) {
        if(treeNode instanceof Classification){
            return dao.delete((Classification) treeNode);
        }else if(treeNode instanceof TaxonNode){
            return taxonNodeDao.delete((TaxonNode)treeNode);
        }
        return null;
    }
    @Override
    public UUID saveTaxonNode(TaxonNode taxonNode) {
        return taxonNodeDao.save(taxonNode).getUuid();
    }

    @Override
    public Map<UUID, TaxonNode> saveTaxonNodeAll(
            Collection<TaxonNode> taxonNodeCollection) {
        return taxonNodeDao.saveAll(taxonNodeCollection);
    }

    @Override
    public UUID saveTreeNode(ITaxonTreeNode treeNode) {
        if(treeNode instanceof Classification){
            return dao.save((Classification) treeNode).getUuid();
        }else if(treeNode instanceof TaxonNode){
            return taxonNodeDao.save((TaxonNode)treeNode).getUuid();
        }
        return null;
    }

    @Override
    public List<TaxonNode> getAllNodes(){
        return taxonNodeDao.list(null,null);
    }

    @Override
    public List<UuidAndTitleCache<TaxonNode>> getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(Classification classification) {
        return taxonDao.getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(classification);
    }

    @Override
    public List<UuidAndTitleCache<Classification>> getUuidAndTitleCache() {
        return dao.getUuidAndTitleCache();
    }

    @Override
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

    @Override
    public Map<UUID, List<MediaRepresentation>> getAllMediaForChildNodes(Taxon taxon, Classification taxTree, List<String> propertyPaths, int size, int height, int widthOrDuration, String[] mimeTypes){
        TaxonNode node = taxTree.getNode(taxon);

        return getAllMediaForChildNodes(node, propertyPaths, size, height, widthOrDuration, mimeTypes);
    }

    @Override
    @Transactional(readOnly = false)
    public void updateTitleCache(Class<? extends Classification> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<Classification> cacheStrategy, IProgressMonitor monitor) {
        if (clazz == null){
            clazz = Classification.class;
        }
        super.updateTitleCacheImpl(clazz, stepSize, cacheStrategy, monitor);
    }

    /**
     *
     * @param allNodesOfClassification
     * @return null - if  allNodesOfClassification is empty <br>
     */

    private Map<String, List<TaxonNode>> getSortedGenusList(Collection<TaxonNode> allNodesOfClassification){

    	if(allNodesOfClassification == null || allNodesOfClassification.isEmpty()){
    		return null;
    	}
    	Map<String, List<TaxonNode>> sortedGenusMap = new HashMap<String, List<TaxonNode>>();
    	for(TaxonNode node:allNodesOfClassification){
    		final TaxonNode tn = node;
    		Taxon taxon = node.getTaxon();
    		NonViralName name = CdmBase.deproxy(taxon.getName(), NonViralName.class);
    		String genusOrUninomial = name.getGenusOrUninomial();
    		//if rank unknown split string and take first word
    		if(genusOrUninomial == null){
    			String titleCache = taxon.getTitleCache();
    			String[] split = titleCache.split("\\s+");
    			for(String s:split){
    				genusOrUninomial = s;
    				break;
    			}
    		}
    		//if node has children

    		//retrieve list from map if not create List
    		if(sortedGenusMap.containsKey(genusOrUninomial)){
    			List<TaxonNode> list = sortedGenusMap.get(genusOrUninomial);
    			list.add(node);
    			sortedGenusMap.put(genusOrUninomial, list);
    		}else{
    			//create List for genus
    			List<TaxonNode> list = new ArrayList<TaxonNode>();
    			list.add(node);
    			sortedGenusMap.put(genusOrUninomial, list);
    		}
    	}
    	return sortedGenusMap;
    }

    /**
     *
     * creates new Classification and parent TaxonNodes at genus level
     *
     *
     * @param map GenusMap which holds a name (Genus) and all the same Taxa as a list
     * @param classification you want to improve the hierarchy (will not be modified)
     * @param configurator to change certain settings, if null then standard settings will be taken
     * @return new classification with parentNodes for each entry in the map
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Transactional(readOnly = false)
	@Override
    public UpdateResult createHierarchyInClassification(Classification classification, CreateHierarchyForClassificationConfigurator configurator){
        UpdateResult result = new UpdateResult();
    	classification = dao.findByUuid(classification.getUuid());
    	Map<String, List<TaxonNode>> map = getSortedGenusList(classification.getAllNodes());

    	final String APPENDIX = "repaired";
    	String titleCache = org.apache.commons.lang.StringUtils.isBlank(classification.getTitleCache()) ? " " : classification.getTitleCache() ;
    	//TODO classification clone???
    	Classification newClassification = Classification.NewInstance(titleCache +" "+ APPENDIX);
    	newClassification.setReference(classification.getReference());

    	for(Map.Entry<String, List<TaxonNode>> entry:map.entrySet()){
    		String genus = entry.getKey();
    		List<TaxonNode> listOfTaxonNodes = entry.getValue();
    		TaxonNode parentNode = null;
    		//Search for genus in list
    		for(TaxonNode tNode:listOfTaxonNodes){
    			//take that taxonNode as parent and remove from list with all it possible children
    			//FIXME NPE for name
    			TaxonNameBase name = tNode.getTaxon().getName();
				NonViralName nonViralName = CdmBase.deproxy(name, NonViralName.class);
    			if(nonViralName.getNameCache().equalsIgnoreCase(genus)){
    				TaxonNode clone = (TaxonNode) tNode.clone();
    				if(!tNode.hasChildNodes()){
    					//FIXME remove classification
//    					parentNode = newClassification.addChildNode(clone, 0, classification.getCitation(), classification.getMicroReference());
    					parentNode = newClassification.addChildNode(clone, 0, clone.getReference(), clone.getMicroReference());
    					//remove taxonNode from list because just added to classification
    					result.addUpdatedObject(tNode);
    					listOfTaxonNodes.remove(tNode);
    				}else{
    					//get all childNodes
    					//save prior Hierarchy and remove them from the list
    					List<TaxonNode> copyAllChildrenToTaxonNode = copyAllChildrenToTaxonNode(tNode, clone, result);
//    					parentNode = newClassification.addChildNode(clone, 0, classification.getCitation(), classification.getMicroReference());
      					//FIXME remove classification
    					parentNode = newClassification.addChildNode(clone, 0, clone.getReference(), clone.getMicroReference());
    					//remove taxonNode from list because just added to classification
    					result.addUpdatedObject(tNode);
    					listOfTaxonNodes.remove(tNode);
    					if(copyAllChildrenToTaxonNode != null){
    						listOfTaxonNodes = (List<TaxonNode>) CollectionUtils.removeAll(listOfTaxonNodes, copyAllChildrenToTaxonNode);
    					}
    				}
    				break;
    			}
    		}
    		if(parentNode == null){
    			//if no match found in list, create parentNode
    			NonViralNameParserImpl parser = NonViralNameParserImpl.NewInstance();
    			NonViralName nonViralName = parser.parseFullName(genus);
    			TaxonNameBase taxonNameBase = nonViralName;
    			//TODO Sec via configurator
    			Taxon taxon = Taxon.NewInstance(taxonNameBase, null);
    			parentNode = newClassification.addChildTaxon(taxon, 0, null, null);
    			result.addUpdatedObject(parentNode);
    		}
    		//iterate over the rest of the list
    		for(TaxonNode tn : listOfTaxonNodes){
    			//if TaxonNode has a parent and this is not the classification then skip it
    			//and add to new classification via the parentNode as children of it
    			//this should assures to keep the already existing hierarchy
    			//FIXME: Assert is not rootnode --> entrypoint is not classification in future but rather rootNode

    			if(!tn.isTopmostNode()){
    				continue; //skip to next taxonNode
    			}

    			TaxonNode clone = (TaxonNode) tn.clone();
    			//FIXME: citation from node
    			//TODO: addchildNode without citation and references
//    			TaxonNode taxonNode = parentNode.addChildNode(clone, classification.getCitation(), classification.getMicroReference());
    			TaxonNode taxonNode = parentNode.addChildNode(clone, clone.getReference(), clone.getMicroReference());
    			result.addUnChangedObject(clone);
    			if(tn.hasChildNodes()){
    				//save hierarchy in new classification
    				List<TaxonNode> copyAllChildrenToTaxonNode = copyAllChildrenToTaxonNode(tn, taxonNode, result);
    				if(copyAllChildrenToTaxonNode != null){
    					listOfTaxonNodes = (List<TaxonNode>) CollectionUtils.removeAll(listOfTaxonNodes, copyAllChildrenToTaxonNode);
    				}
    			}
    		}
    	}
    	dao.saveOrUpdate(newClassification);
    	result.setCdmEntity(newClassification);
    	return result;
    }

    /**
     *
     * recursive method to get all childnodes of taxonNode in classification.
     *
     * @param classification just for References and Citation, can be null
     * @param copyFromNode TaxonNode with Children
     * @param copyToNode TaxonNode which will receive the children
     * @return List of ChildNode which has been added. If node has no children returns null
     */
   private List<TaxonNode> copyAllChildrenToTaxonNode(TaxonNode copyFromNode, TaxonNode copyToNode, UpdateResult result) {
		List<TaxonNode> childNodes;
		if(!copyFromNode.hasChildNodes()){
			return null;
		}else{
			childNodes = copyFromNode.getChildNodes();
		}
		for(TaxonNode childNode:childNodes){
			TaxonNode clone = (TaxonNode) childNode.clone();
			result.addUnChangedObject(clone);
			if(childNode.hasChildNodes()){
				copyAllChildrenToTaxonNode(childNode, clone, result);
			}
			//FIXME: citation from node instead of classification
//			copyToNode.addChildNode(clone,classification.getCitation(), classification.getMicroReference());
			copyToNode.addChildNode(clone, clone.getReference(), clone.getMicroReference());
		}
		return childNodes;
	}

}
