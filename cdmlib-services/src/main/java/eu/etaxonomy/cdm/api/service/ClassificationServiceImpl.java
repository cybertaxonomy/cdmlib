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
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.PagerUtils;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaUtils;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.ITaxonNodeComparator;
import eu.etaxonomy.cdm.model.taxon.ITaxonTreeNode;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

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
        return taxonNodeDao.save(taxonNode);
    }

    @Override
    public Map<UUID, TaxonNode> saveTaxonNodeAll(
            Collection<TaxonNode> taxonNodeCollection) {
        return taxonNodeDao.saveAll(taxonNodeCollection);
    }

    @Override
    public UUID saveTreeNode(ITaxonTreeNode treeNode) {
        if(treeNode instanceof Classification){
            return dao.save((Classification) treeNode);
        }else if(treeNode instanceof TaxonNode){
            return taxonNodeDao.save((TaxonNode)treeNode);
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


}
