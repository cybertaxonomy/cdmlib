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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.config.NodeDeletionConfigurator.ChildHandling;
import eu.etaxonomy.cdm.api.service.config.TermNodeDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.persistence.dao.common.Restriction;
import eu.etaxonomy.cdm.persistence.dao.term.ITermNodeDao;
import eu.etaxonomy.cdm.persistence.dao.term.ITermTreeDao;
import eu.etaxonomy.cdm.persistence.dto.MergeResult;
import eu.etaxonomy.cdm.persistence.dto.TermTreeDto;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

@Service
@Transactional(readOnly = false)
public class TermTreeServiceImpl
            extends IdentifiableServiceBase<TermTree, ITermTreeDao>
            implements ITermTreeService {

    private ITermNodeDao termNodeDao;

    @Autowired
    private ITermNodeService termNodeService;

    @Override
    @Autowired
    protected void setDao(ITermTreeDao dao) {
        this.dao = dao;
    }

    @Autowired
    protected void setTermNodeDao(ITermNodeDao termNodeDao) {
        this.termNodeDao = termNodeDao;
    }

    @Override
    @Transactional(readOnly = false)
    public UpdateResult updateCaches(Class<? extends TermTree> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<TermTree> cacheStrategy, IProgressMonitor monitor) {
        if (clazz == null){
            clazz = TermTree.class;
        }
        return super.updateCachesImpl(clazz, stepSize, cacheStrategy, monitor);
    }

    @Override
    public Map<UUID, TermNode> saveNodesAll(Collection<TermNode> nodeCollection) {
        return termNodeDao.saveAll(nodeCollection);
    }

    @Override
    public Map<UUID, TermNode> saveOrUpdateNodesAll(Collection<TermNode> nodeCollection) {
        return termNodeDao.saveOrUpdateAll(nodeCollection);
    }


    @Override
    public UpdateResult saveOrUpdateTermTreeDtoList(List<TermTreeDto> dtos){
        UpdateResult result = new UpdateResult();
        MergeResult<TermTree> mergeResult;
        List<UUID> uuids = new ArrayList<>();
        dtos.stream().forEach(dto -> uuids.add(dto.getUuid()));
        List<TermTree> trees = dao.list(uuids, null, 0, null, null);
        //check all attributes for changes and adapt
        for (TermTree tree: trees){
            for (TermTreeDto dto: dtos){

                if (dto.getUuid().equals(tree.getUuid())){
                    tree.setTitleCache(dto.getTitleCache());
                    tree.setAllowDuplicates(dto.isAllowDuplicate());
                    tree.setFlat(dto.isFlat());
                    tree.setOrderRelevant(dto.isOrderRelevant());
                }

                mergeResult = dao.merge(tree, true);
                result.addUpdatedObject(mergeResult.getMergedEntity());
            }
        }
        return result;
    }

    @Override
    public TermTree loadWithNodes(UUID uuid, List<String> propertyPaths, List<String> nodePaths) {

        if(nodePaths==null){
            nodePaths = new ArrayList<>();
        }

        if(!nodePaths.contains("children")) {
            nodePaths.add("children");
        }

        List<String> rootPaths = new ArrayList<>();
        rootPaths.add("root");
        for(String path : nodePaths) {
            rootPaths.add("root." + path);
        }

        if(propertyPaths != null) {
            rootPaths.addAll(propertyPaths);
        }

        TermTree featureTree = load(uuid, rootPaths);
        if(featureTree == null){
            throw new EntityNotFoundException("No FeatureTree entity found for " + uuid);
        }
        dao.deepLoadNodes(featureTree.getRoot().getChildNodes(), nodePaths);
        return featureTree;
    }

    /**
     * Returns the featureTree specified by the given <code>uuid</code>.
     * The specified featureTree either can be one of those stored in the CDM database or can be the
     * DefaultFeatureTree (contains all Features in use).
     * The uuid of the DefaultFeatureTree is defined in {@link ITermTreeDao#DefaultFeatureTreeUuid}.
      *
     * @see eu.etaxonomy.cdm.api.service.ServiceBase#load(java.util.UUID, java.util.List)
     */
    @Override
    public TermTree load(UUID uuid, List<String> propertyPaths) {
        return super.load(uuid, propertyPaths);
    }

    @Override
    public DeleteResult delete(UUID featureTreeUuid){
        DeleteResult result = new DeleteResult();
        TermTree tree = dao.load(featureTreeUuid);

        TermNode rootNode = CdmBase.deproxy(tree.getRoot());
        TermNodeDeletionConfigurator config = new TermNodeDeletionConfigurator();
        config.setChildHandling(ChildHandling.DELETE);
        result = termNodeService.deleteNode(rootNode.getUuid(), config);
        //FIXME test if this is necessary
        tree.removeRootNode();
        if (result.isOk()){
          dao.delete(tree);
          result.addDeletedObject(tree);
        }
        return result;
    }

    @Override
    public <S extends TermTree> List<UuidAndTitleCache<S>> getUuidAndTitleCacheByTermType(Class<S> clazz, TermType termType, Integer limit,
            String pattern) {
        return dao.getUuidAndTitleCacheByTermType(clazz, termType, limit, pattern);
    }

    @Override
    public List<TermTree> list(TermType termType, Integer limit, Integer start, List<OrderHint> orderHints,
            List<String> propertyPaths) {
        return dao.list(null, buildTermTypeFilterRestrictions(termType), limit, start, orderHints, propertyPaths);
    }

    @Override
    public Pager<TermTree> page(TermType termType, Integer pageSize, Integer pageIndex, List<OrderHint> orderHints, List<String> propertyPaths) {

        return page(null, buildTermTypeFilterRestrictions(termType), pageSize, pageIndex, orderHints, propertyPaths);
    }

    /**
     * @param termType
     * @return
     */
    @Override
    public List<Restriction<?>> buildTermTypeFilterRestrictions(TermType termType) {
        List<Restriction<?>> filterRestrictions = null;
        if(termType != null){
           Set<TermType> termTypes = termType.getGeneralizationOf(true);
           termTypes.add(termType);
           filterRestrictions = Arrays.asList(new Restriction<>("termType", null, termTypes.toArray()));
        }
        return filterRestrictions;
    }

    @Override
    public List<TermTreeDto> listTermTreeDtosByTermType(TermType termType) {
        return dao.listTermTreeDtosByTermType(termType);
    }

    @Override
    public TermTreeDto getTermTreeDtoByUuid(UUID uuid) {
        return dao.getTermTreeDtosByUuid(uuid);
    }

}
