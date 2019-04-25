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

import eu.etaxonomy.cdm.api.service.config.FeatureNodeDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.config.NodeDeletionConfigurator.ChildHandling;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.term.FeatureNode;
import eu.etaxonomy.cdm.model.term.FeatureTree;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.persistence.dao.description.IFeatureNodeDao;
import eu.etaxonomy.cdm.persistence.dao.description.IFeatureTreeDao;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

@Service
@Transactional(readOnly = false)
public class FeatureTreeServiceImpl extends IdentifiableServiceBase<FeatureTree, IFeatureTreeDao> implements IFeatureTreeService {

    private IFeatureNodeDao featureNodeDao;

    @Autowired
    private IFeatureNodeService featureNodeService;

    @Override
    @Autowired
    protected void setDao(IFeatureTreeDao dao) {
        this.dao = dao;
    }

    @Autowired
    protected void setFeatureNodeDao(IFeatureNodeDao featureNodeDao) {
        this.featureNodeDao = featureNodeDao;
    }

    @Override
    @Transactional(readOnly = false)
    public UpdateResult updateCaches(Class<? extends FeatureTree> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<FeatureTree> cacheStrategy, IProgressMonitor monitor) {
        if (clazz == null){
            clazz = FeatureTree.class;
        }
        return super.updateCachesImpl(clazz, stepSize, cacheStrategy, monitor);
    }

    @Override
    public List<FeatureNode> getFeatureNodesAll() {
        return featureNodeDao.list();
    }

    @Override
    public Map<UUID, FeatureNode> saveFeatureNodesAll(Collection<FeatureNode> featureNodeCollection) {
        return featureNodeDao.saveAll(featureNodeCollection);
    }

    @Override
    public Map<UUID, FeatureNode> saveOrUpdateFeatureNodesAll(Collection<FeatureNode> featureNodeCollection) {
        return featureNodeDao.saveOrUpdateAll(featureNodeCollection);
    }

    @Override
    public FeatureTree loadWithNodes(UUID uuid, List<String> propertyPaths, List<String> nodePaths) {

        if(nodePaths==null){
            nodePaths = new ArrayList<>();
        }

        if(!nodePaths.contains("children")) {
            nodePaths.add("children");
        }

        List<String> rootPaths = new ArrayList<String>();
        rootPaths.add("root");
        for(String path : nodePaths) {
            rootPaths.add("root." + path);
        }

        if(propertyPaths != null) {
            rootPaths.addAll(propertyPaths);
        }

        FeatureTree featureTree = load(uuid, rootPaths);
        if(featureTree == null){
            throw new EntityNotFoundException("No FeatureTree entity found for " + uuid);
        }
        dao.deepLoadNodes(featureTree.getRoot().getChildNodes() ,nodePaths);
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
        return super.load(uuid, propertyPaths);
    }

    @Override
    public FeatureTree createTransientDefaultFeatureTree() {
        return load(IFeatureTreeDao.DefaultFeatureTreeUuid);
    }

    @Override
    public DeleteResult delete(UUID featureTreeUuid){
        DeleteResult result = new DeleteResult();
        FeatureTree tree = dao.load(featureTreeUuid);

        FeatureNode rootNode = HibernateProxyHelper.deproxy(tree.getRoot());
        FeatureNodeDeletionConfigurator config = new FeatureNodeDeletionConfigurator();
        config.setChildHandling(ChildHandling.DELETE);
        result =featureNodeService.deleteFeatureNode(rootNode.getUuid(), config);
        //FIXME test if this is necessary
        tree.removeRootNode();
        if (result.isOk()){
          dao.delete(tree);
          result.addDeletedObject(tree);
        }
        return result;
    }

    @Override
    public <S extends FeatureTree> List<UuidAndTitleCache<S>> getUuidAndTitleCacheByTermType(Class<S> clazz, TermType termType, Integer limit,
            String pattern) {
        return dao.getUuidAndTitleCacheByTermType(clazz, termType, limit, pattern);
    }
}
