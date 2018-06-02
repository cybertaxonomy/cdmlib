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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.config.CreateHierarchyForClassificationConfigurator;
import eu.etaxonomy.cdm.api.service.config.NodeDeletionConfigurator.ChildHandling;
import eu.etaxonomy.cdm.api.service.config.TaxonDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.dto.EntityDTO;
import eu.etaxonomy.cdm.api.service.dto.GroupedTaxonDTO;
import eu.etaxonomy.cdm.api.service.dto.MarkedEntityDTO;
import eu.etaxonomy.cdm.api.service.dto.TaxonInContextDTO;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.PagerUtils;
import eu.etaxonomy.cdm.api.service.pager.impl.AbstractPagerImpl;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.hibernate.HHH_9751_Util;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.ITreeNode;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.TreeIndex;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaUtils;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.ITaxonNodeComparator;
import eu.etaxonomy.cdm.model.taxon.ITaxonTreeNode;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeDao;
import eu.etaxonomy.cdm.persistence.dto.ClassificationLookupDTO;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;
import eu.etaxonomy.cdm.persistence.dto.TaxonStatus;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

/**
 * @author n.hoffmann
 * @since Sep 21, 2009
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
    private ITaxonNodeService taxonNodeService;

    @Autowired
    private IDefinedTermDao termDao;

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
    @Transactional(readOnly = false)
    public UpdateResult cloneClassification(UUID classificationUuid,
    		String name, Reference sec, TaxonRelationshipType relationshipType) {
        UpdateResult result = new UpdateResult();
    	Classification classification = load(classificationUuid);
    	Classification clone = Classification.NewInstance(name);
    	clone.setReference(sec);

    	//clone taxa and taxon nodes
    	List<TaxonNode> childNodes = classification.getRootNode().getChildNodes();
    	for (TaxonNode taxonNode : childNodes) {
    		addChildTaxa(taxonNode, null, clone, relationshipType);
    	}
    	dao.saveOrUpdate(clone);
    	result.setCdmEntity(clone);
    	return result;
    }

    private void addChildTaxa(TaxonNode originalParentNode, TaxonNode cloneParentNode, Classification classification, TaxonRelationshipType relationshipType){
        Reference reference = classification.getReference();
    	Taxon cloneTaxon = (Taxon) HibernateProxyHelper.deproxy(originalParentNode.getTaxon(), Taxon.class).clone();
    	cloneTaxon.setSec(reference);
		String microReference = null;
		List<TaxonNode> originalChildNodes = originalParentNode.getChildNodes();
		HHH_9751_Util.removeAllNull(originalChildNodes);

		//add relation between taxa
		if (relationshipType != null){
		    cloneTaxon.addTaxonRelation(originalParentNode.getTaxon(), relationshipType, reference, microReference);
		}

		TaxonNode cloneChildNode = null;
    	//add taxon node to either parent node or classification (no parent node)
    	if(cloneParentNode==null){
    		cloneChildNode = classification.addChildTaxon(cloneTaxon, reference, microReference);
    	}
    	else{
    		cloneChildNode = cloneParentNode.addChildTaxon(cloneTaxon, reference, microReference);
    	}
    	taxonNodeDao.saveOrUpdate(cloneChildNode);
    	//add children
		for (TaxonNode originalChildNode : originalChildNodes) {
    		addChildTaxa(originalChildNode, cloneChildNode, classification, relationshipType);
    	}
    }

    @Override
    public List<TaxonNode> listRankSpecificRootNodes(Classification classification, Rank rank, Integer pageSize,
            Integer pageIndex, List<String> propertyPaths) {
        return pageRankSpecificRootNodes(classification, rank, pageSize, pageIndex, propertyPaths).getRecords();
    }

    @Override
    public Pager<TaxonNode> pageRankSpecificRootNodes(Classification classification, Rank rank, Integer pageSize,
            Integer pageIndex, List<String> propertyPaths) {
        long[] numberOfResults = dao.countRankSpecificRootNodes(classification, rank);
        long totalNumberOfResults = numberOfResults[0] + (numberOfResults.length > 1 ? numberOfResults[1] : 0);

        List<TaxonNode> results = new ArrayList<TaxonNode>();

        if (AbstractPagerImpl.hasResultsInRange(totalNumberOfResults, pageIndex, pageSize)) { // no point checking again
            Integer limit = PagerUtils.limitFor(pageSize);
            Integer start = PagerUtils.startFor(pageSize, pageIndex);

            Integer remainingLimit = limit;
            int[] queryIndexes = rank == null ? new int[]{0} : new int[]{0,1};

            for(int queryIndex: queryIndexes) {
                if(start != null && start > numberOfResults[queryIndex]) {
                    // start in next query with new start value
                    start = start - (int)numberOfResults[queryIndex];
                    continue;
                }

                List<TaxonNode> perQueryResults = dao.listRankSpecificRootNodes(classification, rank, remainingLimit, start, propertyPaths, queryIndex);
                results.addAll(perQueryResults);
                if(remainingLimit != null ){
                    remainingLimit = remainingLimit - results.size();
                    if(remainingLimit <= 0) {
                        // no need to run further queries if first query returned enough items!
                        break;
                    }
                    // start at with fist item of next query to fetch the remaining items
                    start = 0;
                }
            }
        }
//        long start_t = System.currentTimeMillis();
        Collections.sort(results, taxonNodeComparator); // TODO is ordering during the hibernate query in the dao possible?
//        System.err.println("service.pageRankSpecificRootNodes() - Collections.sort(results,  taxonNodeComparator) " + (System.currentTimeMillis() - start_t));
        return new DefaultPagerImpl<TaxonNode>(pageIndex, (int) totalNumberOfResults, pageSize, results);

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
        if(baseRank != null){
            baseRank = (Rank) termDao.load(baseRank.getUuid());
        }
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
    public Pager<TaxonNode> pageSiblingsOfTaxon(UUID taxonUuid, UUID classificationUuid, Integer pageSize,
            Integer pageIndex, List<String> propertyPaths){

        Classification classification = dao.load(classificationUuid);
        Taxon taxon = (Taxon) taxonDao.load(taxonUuid);

        long numberOfResults = dao.countSiblingsOf(taxon, classification);

        List<TaxonNode> results;
        if(PagerUtils.hasResultsInRange(numberOfResults, pageIndex, pageSize)) {
            results = dao.listSiblingsOf(taxon, classification, pageSize, pageIndex, propertyPaths);
            Collections.sort(results, taxonNodeComparator); // FIXME this is only a HACK, order during the hibernate query in the dao
        } else {
            results = new ArrayList<>();
        }

        return new DefaultPagerImpl<TaxonNode>(pageIndex, numberOfResults, pageSize, results);
    }

    @Override
    public List<TaxonNode> listSiblingsOfTaxon(UUID taxonUuid, UUID classificationUuid, Integer pageSize,
            Integer pageIndex, List<String> propertyPaths){

        Pager<TaxonNode> pager = pageSiblingsOfTaxon(taxonUuid, classificationUuid, pageSize, pageIndex, propertyPaths);
        return pager.getRecords();
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
    public TaxonNode getRootNode(UUID classificationUuid){
        return dao.getRootNode(classificationUuid);
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
    public UUID saveClassification(Classification classification) {

       taxonNodeDao.saveOrUpdateAll(classification.getAllNodes());
       UUID result =dao.saveOrUpdate(classification);
       return result;
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
    public List<UuidAndTitleCache<TaxonNode>> getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(UUID classificationUuid, Integer limit, String pattern, boolean searchForClassifications) {
        return taxonNodeDao.getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(dao.load(classificationUuid),  limit, pattern, searchForClassifications);
    }

    @Override
    public List<UuidAndTitleCache<TaxonNode>> getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(Classification classification,  Integer limit, String pattern, boolean searchForClassifications) {
        return taxonNodeDao.getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(classification,  limit, pattern, searchForClassifications);
    }

    @Override
    public List<UuidAndTitleCache<TaxonNode>> getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(UUID classificationUuid, boolean searchForClassifications ) {
        return taxonNodeDao.getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(dao.load(classificationUuid), null, null, searchForClassifications);
    }

    @Override
    public List<UuidAndTitleCache<TaxonNode>> getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(Classification classification, boolean searchForClassifications ) {
        return taxonNodeDao.getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(classification, null, null, searchForClassifications);
    }

    @Override
    public List<UuidAndTitleCache<Classification>> getUuidAndTitleCache(Integer limit, String pattern) {
        return dao.getUuidAndTitleCache(limit, pattern);
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
    	Map<String, List<TaxonNode>> sortedGenusMap = new HashMap<>();
    	for(TaxonNode node:allNodesOfClassification){
    		final TaxonNode tn = node;
    		Taxon taxon = node.getTaxon();
    		INonViralName name = taxon.getName();
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
    @SuppressWarnings({ "unchecked" })
	@Transactional(readOnly = false)
	@Override
    public UpdateResult createHierarchyInClassification(Classification classification, CreateHierarchyForClassificationConfigurator configurator){
        UpdateResult result = new UpdateResult();
    	classification = dao.findByUuid(classification.getUuid());
    	Map<String, List<TaxonNode>> map = getSortedGenusList(classification.getAllNodes());

    	final String APPENDIX = "repaired";
    	String titleCache = StringUtils.isBlank(classification.getTitleCache()) ? " " : classification.getTitleCache() ;
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
    			TaxonName name = tNode.getTaxon().getName();
    			if(name.getNameCache().equalsIgnoreCase(genus)){
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
    			TaxonName TaxonName = (TaxonName)parser.parseFullName(genus);
    			//TODO Sec via configurator
    			Taxon taxon = Taxon.NewInstance(TaxonName, null);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public ClassificationLookupDTO classificationLookup(Classification classification) {
        return dao.classificationLookup(classification);
    }


    @Override
    @Transactional
    public DeleteResult delete(UUID classificationUuid, TaxonDeletionConfigurator config){
        DeleteResult result = new DeleteResult();
        Classification classification = dao.findByUuid(classificationUuid);
        if (classification == null){
            result.addException(new IllegalArgumentException("The classification does not exist in database."));
            result.setAbort();
            return result;
        }
        if (!classification.hasChildNodes()){
            dao.delete(classification);
            result.addDeletedObject(classification);
            return result;
        }
        if (config.getTaxonNodeConfig().getChildHandling().equals(ChildHandling.DELETE)){
//            TaxonNode root = classification.getRootNode();
//            result.includeResult(taxonNodeService.deleteTaxonNode(HibernateProxyHelper.deproxy(root), config));
//            result.addDeletedObject(classification);
            dao.delete(classification);
            result.addDeletedObject(classification);
            return result;
        }


        return result;
    }

    @Override
    public List<GroupedTaxonDTO> groupTaxaByHigherTaxon(List<UUID> originalTaxonUuids, UUID classificationUuid, Rank minRank, Rank maxRank){
        List<GroupedTaxonDTO> result = new ArrayList<>();

        //get treeindex for each taxonUUID
        Map<UUID, TreeIndex> taxonIdTreeIndexMap = dao.treeIndexForTaxonUuids(classificationUuid, originalTaxonUuids);

        //build treeindex list (or tree)
        //TODO make it work with TreeIndex or move there
        List<String> treeIndexClosureStr = new ArrayList<>();
        for (TreeIndex treeIndex : taxonIdTreeIndexMap.values()){
            String[] splits = treeIndex.toString().substring(1).split(ITreeNode.separator);
            String currentIndex = ITreeNode.separator;
            for (String split : splits){
                if (split.equals("")){
                    continue;
                }
                currentIndex += split + ITreeNode.separator;
                if (!treeIndexClosureStr.contains(currentIndex) && !split.startsWith(ITreeNode.treePrefix)){
                    treeIndexClosureStr.add(currentIndex);
                }
            }
        }

        //get rank sortindex for all parent taxa with sortindex <= minRank and sortIndex >= maxRank (if available)
        Integer minRankOrderIndex = minRank == null ? null : minRank.getOrderIndex();
        Integer maxRankOrderIndex = maxRank == null ? null : maxRank.getOrderIndex();
        List<TreeIndex> treeIndexClosure = TreeIndex.NewListInstance(treeIndexClosureStr);

        Map<TreeIndex, Integer> treeIndexSortIndexMapTmp = taxonNodeDao.rankOrderIndexForTreeIndex(treeIndexClosure, minRankOrderIndex, maxRankOrderIndex);

        //remove all treeindex with "exists child in above map(and child.sortindex > xxx)
        List<TreeIndex> treeIndexList = TreeIndex.sort(treeIndexSortIndexMapTmp.keySet());

        Map<TreeIndex, Integer> treeIndexSortIndexMap = new HashMap<>();
        TreeIndex lastTreeIndex = null;
        for (TreeIndex treeIndex : treeIndexList){
            if (lastTreeIndex != null && lastTreeIndex.hasChild(treeIndex)){
                treeIndexSortIndexMap.remove(lastTreeIndex);
            }
            treeIndexSortIndexMap.put(treeIndex, treeIndexSortIndexMapTmp.get(treeIndex));
            lastTreeIndex = treeIndex;
        }

        //get taxonID for treeIndexes
        Map<TreeIndex, UuidAndTitleCache<?>> treeIndexTaxonIdMap = taxonNodeDao.taxonUuidsForTreeIndexes(treeIndexSortIndexMap.keySet());

        //fill result list
        for (UUID originalTaxonUuid : originalTaxonUuids){
            GroupedTaxonDTO item = new GroupedTaxonDTO();
            result.add(item);
            item.setTaxonUuid(originalTaxonUuid);
            TreeIndex groupTreeIndex = taxonIdTreeIndexMap.get(originalTaxonUuid);
            String groupIndexX = TreeIndex.toString(groupTreeIndex);
            while (groupTreeIndex != null){
                if (treeIndexTaxonIdMap.get(groupTreeIndex) != null){
                    UuidAndTitleCache<?> uuidAndLabel = treeIndexTaxonIdMap.get(groupTreeIndex);
                    item.setGroupTaxonUuid(uuidAndLabel.getUuid());
                    item.setGroupTaxonName(uuidAndLabel.getTitleCache());
                    break;
                }else{
                    groupTreeIndex = groupTreeIndex.parent();
//                    int index = groupIndex.substring(0, groupIndex.length()-1).lastIndexOf(ITreeNode.separator);
//                    groupIndex = index < 0 ? null : groupIndex.substring(0, index+1);
                }
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<GroupedTaxonDTO> groupTaxaByMarkedParents(List<UUID> originalTaxonUuids, UUID classificationUuid,
            MarkerType markerType, Boolean flag) {

        List<GroupedTaxonDTO> result = new ArrayList<>();

        //get treeindex for each taxonUUID
        Map<UUID, TreeIndex> taxonIdTreeIndexMap = dao.treeIndexForTaxonUuids(classificationUuid, originalTaxonUuids);

        //get all marked tree indexes
        Set<TreeIndex> markedTreeIndexes = dao.getMarkedTreeIndexes(markerType, flag);


        Map<TreeIndex, TreeIndex> groupedMap = TreeIndex.group(markedTreeIndexes, taxonIdTreeIndexMap.values());
        Set<TreeIndex> notNullGroups = new HashSet<>(groupedMap.values());
        notNullGroups.remove(null);

        //get taxonInfo for treeIndexes
        Map<TreeIndex, UuidAndTitleCache<?>> treeIndexTaxonIdMap = taxonNodeDao.taxonUuidsForTreeIndexes(notNullGroups);

        //fill result list
        for (UUID originalTaxonUuid : originalTaxonUuids){
            GroupedTaxonDTO item = new GroupedTaxonDTO();
            result.add(item);
            item.setTaxonUuid(originalTaxonUuid);

            TreeIndex toBeGroupedTreeIndex = taxonIdTreeIndexMap.get(originalTaxonUuid);
            TreeIndex groupTreeIndex = groupedMap.get(toBeGroupedTreeIndex);
            UuidAndTitleCache<?> uuidAndLabel = treeIndexTaxonIdMap.get(groupTreeIndex);
            if (uuidAndLabel != null){
                item.setGroupTaxonUuid(uuidAndLabel.getUuid());
                item.setGroupTaxonName(uuidAndLabel.getTitleCache());
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID getTaxonNodeUuidByTaxonUuid(UUID classificationUuid, UUID taxonUuid) {
        Map<UUID, UUID> map = dao.getTaxonNodeUuidByTaxonUuid(classificationUuid, Arrays.asList(taxonUuid));
        UUID taxonNodeUuid = map.get(taxonUuid);
        return taxonNodeUuid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TaxonInContextDTO getTaxonInContext(UUID classificationUuid, UUID taxonBaseUuid,
            Boolean doChildren, Boolean doSynonyms, boolean includeUnpublished, List<UUID> ancestorMarkers,
            NodeSortMode sortMode) {
        TaxonInContextDTO result = new TaxonInContextDTO();

        TaxonBase<?> taxonBase = taxonDao.load(taxonBaseUuid);
        if (taxonBase == null){
            throw new EntityNotFoundException("Taxon with uuid " + taxonBaseUuid + " not found in datasource");
        }
        boolean isSynonym = false;
        Taxon acceptedTaxon;
        if (taxonBase.isInstanceOf(Synonym.class)){
            isSynonym = true;
            Synonym synonym = CdmBase.deproxy(taxonBase, Synonym.class);
            acceptedTaxon = synonym.getAcceptedTaxon();
            if (acceptedTaxon == null) {
                throw new EntityNotFoundException("Accepted taxon not found for synonym"  );
            }
            TaxonStatus taxonStatus = TaxonStatus.Synonym;
            if (synonym.getName()!= null && acceptedTaxon.getName() != null
                    && synonym.getName().getHomotypicalGroup().equals(acceptedTaxon.getName().getHomotypicalGroup())){
                taxonStatus = TaxonStatus.SynonymObjective;
            }
            result.setTaxonStatus(taxonStatus);

        }else{
            acceptedTaxon = CdmBase.deproxy(taxonBase, Taxon.class);
            result.setTaxonStatus(TaxonStatus.Accepted);
        }
        UUID acceptedTaxonUuid = acceptedTaxon.getUuid();

        UUID taxonNodeUuid = getTaxonNodeUuidByTaxonUuid(classificationUuid, acceptedTaxonUuid);
        if (taxonNodeUuid == null) {
            throw new EntityNotFoundException("Taxon not found in classficiation with uuid " + classificationUuid + ". Either classification does not exist or does not contain taxon/synonym with uuid " + taxonBaseUuid );
        }
        result.setTaxonNodeUuid(taxonNodeUuid);

        //TODO make it a dao call
        Taxon parentTaxon = getParentTaxon(classificationUuid, acceptedTaxon);
        if (parentTaxon != null){
            result.setParentTaxonUuid(parentTaxon.getUuid());
            result.setParentTaxonLabel(parentTaxon.getTitleCache());
            if (parentTaxon.getName() != null){
                result.setParentNameLabel(parentTaxon.getName().getTitleCache());
            }
        }


        result.setTaxonUuid(taxonBaseUuid);
        result.setClassificationUuid(classificationUuid);
        if (taxonBase.getSec() != null){
            result.setSecundumUuid(taxonBase.getSec().getUuid());
            result.setSecundumLabel(taxonBase.getSec().getTitleCache());
        }
        result.setTaxonLabel(taxonBase.getTitleCache());

        TaxonName name = taxonBase.getName();
        result.setNameUuid(name.getUuid());
        result.setNameLabel(name.getTitleCache());
        result.setNameWithoutAuthor(name.getNameCache());
        result.setGenusOrUninomial(name.getGenusOrUninomial());
        result.setInfraGenericEpithet(name.getInfraGenericEpithet());
        result.setSpeciesEpithet(name.getSpecificEpithet());
        result.setInfraSpecificEpithet(name.getInfraSpecificEpithet());

        result.setAuthorship(name.getAuthorshipCache());

        Rank rank = name.getRank();
        if (rank != null){
            result.setRankUuid(rank.getUuid());
            String rankLabel = rank.getAbbreviation();
            if (StringUtils.isBlank(rankLabel)){
                rankLabel = rank.getLabel();
            }
            result.setRankLabel(rankLabel);
        }

        boolean recursive = false;
        Integer pageSize = null;
        Integer pageIndex = null;
        Pager<TaxonNodeDto> children = taxonNodeService.pageChildNodesDTOs(taxonNodeUuid, recursive, includeUnpublished, doSynonyms,
                sortMode, pageSize, pageIndex);

        //children
        if(! isSynonym) {
            for (TaxonNodeDto childDto : children.getRecords()){
                if (doChildren && childDto.getStatus().equals(TaxonStatus.Accepted)){
                    EntityDTO<Taxon> child = new EntityDTO<Taxon>(childDto.getTaxonUuid(), childDto.getTitleCache());
                    result.addChild(child);
                }else if (doSynonyms && childDto.getStatus().isSynonym()){
                    EntityDTO<Synonym> child = new EntityDTO<Synonym>(childDto.getTaxonUuid(), childDto.getTitleCache());
                    result.addSynonym(child);
                }
            }
        }else{
            result.setAcceptedTaxonUuid(acceptedTaxonUuid);
            String nameTitel = acceptedTaxon.getName() == null ? null : acceptedTaxon.getName().getTitleCache();
            result.setAcceptedTaxonLabel(acceptedTaxon.getTitleCache());
            result.setAcceptedNameLabel(nameTitel);
        }

        //marked ancestors
        if (ancestorMarkers != null && !ancestorMarkers.isEmpty()){
            List<DefinedTermBase> markerTypesTerms = termDao.list(ancestorMarkers, pageSize, null, null, null);
            List<MarkerType> markerTypes = new ArrayList<>();
            for (DefinedTermBase<?> term : markerTypesTerms){
                if (term.isInstanceOf(MarkerType.class)){
                    markerTypes.add(CdmBase.deproxy(term, MarkerType.class));
                }
            }
            if (! markerTypes.isEmpty()){
                TaxonNode node = taxonNodeDao.findByUuid(taxonNodeUuid);
                handleAncestorsForMarkersRecursive(result, markerTypes, node);
            }
        }

        return result;
    }

    /**
     * @param classificationUuid
     * @param acceptedTaxon
     * @return
     */
    private Taxon getParentTaxon(UUID classificationUuid, Taxon acceptedTaxon) {
        if (classificationUuid == null){
            return null;
        }
        TaxonNode parent = null;
        for (TaxonNode node : acceptedTaxon.getTaxonNodes()){
            if (classificationUuid.equals(node.getClassification().getUuid())){
                parent = node.getParent();
            }
        }
        if (parent != null){
            return parent.getTaxon();
        }
        return null;
    }

    /**
     * @param result
     * @param markerTypes
     * @param node
     */
    private void handleAncestorsForMarkersRecursive(TaxonInContextDTO result, List<MarkerType> markerTypes, TaxonNode node) {
       for (MarkerType type : markerTypes){
            Taxon taxon = node.getTaxon();
            if (taxon != null && taxon.hasMarker(type, true)){
                String label =  taxon.getName() == null? taxon.getTitleCache() : taxon.getName().getTitleCache();
                MarkedEntityDTO<Taxon> dto = new MarkedEntityDTO<>(type, true, taxon.getUuid(), label);
                result.addMarkedAncestor(dto);
            }
        }
        TaxonNode parentNode = node.getParent();
        if (parentNode != null){
            handleAncestorsForMarkersRecursive(result, markerTypes, parentNode);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UuidAndTitleCache<TaxonNode>> getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(
            Classification classification) {
        return getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(classification, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UuidAndTitleCache<TaxonNode>> getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(
            UUID classificationUuid) {
        return getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(classificationUuid, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UuidAndTitleCache<TaxonNode>> getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(
            UUID classificationUuid, Integer limit, String pattern) {
        return  getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(classificationUuid,  limit, pattern, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UuidAndTitleCache<TaxonNode>> getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(
            Classification classification, Integer limit, String pattern) {
        return getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(classification, limit, pattern, false);
    }


}
