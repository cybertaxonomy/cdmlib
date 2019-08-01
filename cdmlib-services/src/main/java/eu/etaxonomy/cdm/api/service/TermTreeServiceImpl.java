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

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.config.NodeDeletionConfigurator.ChildHandling;
import eu.etaxonomy.cdm.api.service.config.TermNodeDeletionConfigurator;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.persistence.dao.description.ITermNodeDao;
import eu.etaxonomy.cdm.persistence.dao.description.ITermTreeDao;
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
     * The uuid of the DefaultFeatureTree is defined in {@link IFeatureTreeService#DefaultFeatureTreeUuid}.
     * The DefaultFeatureTree is also returned if no feature tree at all is stored in the cdm database.
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
        return dao.list(termType, limit, start, orderHints, propertyPaths);
    }


}
