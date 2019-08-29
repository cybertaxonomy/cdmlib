/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.term;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.model.term.VocabularyEnum;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.term.ITermTreeDao;
import eu.etaxonomy.cdm.persistence.dao.term.ITermVocabularyDao;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;

/**
 * @author a.mueller
 * @since 10.07.2008
 */
@Repository
public class TermTreeDaoImpl extends IdentifiableDaoBase<TermTree> implements ITermTreeDao{

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(TermTreeDaoImpl.class);

    @Autowired
    private ITermVocabularyDao termVocabularyDao;

    public TermTreeDaoImpl() {
        super(TermTree.class);
        indexedClasses = new Class[1];
        indexedClasses[0] = TermTree.class;
    }

    @Override
    public List<TermTree> list() {
        Criteria crit = getSession().createCriteria(type);
        @SuppressWarnings("unchecked")
        List<TermTree> result = crit.list();
        return result;
    }

    @Override
    public void deepLoadNodes(List<TermNode> nodes, List<String> nodePaths) {

        defaultBeanInitializer.initializeAll(nodes, nodePaths);

        List<TermNode> childrenOfChildren = new ArrayList<>();
        for(TermNode<?> node : nodes) {
            if(node.getChildCount() > 0){
                childrenOfChildren.addAll(node.getChildNodes());
            }
        }
        if(childrenOfChildren.size() > 0){
            deepLoadNodes(childrenOfChildren, nodePaths);
        }
    }

    @Override
    public TermTree load(UUID uuid, List<String> propertyPaths) {
        TermTree result = super.load(uuid, propertyPaths);
        if (result == null && uuid.equals(DefaultFeatureTreeUuid)){
            return createDefaultFeatureTree();
        }
        return super.load(uuid, propertyPaths);
    }

    @Override
    public TermTree load(UUID uuid) {
        return load(uuid, null);
    }

    private TermTree<Feature> createDefaultFeatureTree() {

        TermVocabulary<Feature> featureVocabulary = termVocabularyDao.findByUuid(VocabularyEnum.Feature.getUuid());

        List<Feature> featureList = new ArrayList<>(featureVocabulary.getTerms());
        List<Feature> selectedFeatures = new ArrayList<>();
        for(Feature feature : featureList){
            if(!feature.equals(Feature.INDIVIDUALS_ASSOCIATION())){
                selectedFeatures.add(feature);
            }
        }
        TermTree<Feature> featureTree = TermTree.NewFeatureInstance(selectedFeatures);
        featureTree.setUuid(DefaultFeatureTreeUuid);
        return featureTree;
    }

    @Override
    public <S extends TermTree> List<UuidAndTitleCache<S>> getUuidAndTitleCacheByTermType(Class<S> clazz, TermType termType, Integer limit,
            String pattern) {
        Session session = getSession();
        Query query = session.createQuery(
                " SELECT uuid, id, titleCache "
                        + " FROM " + clazz.getSimpleName()
                        + (pattern!=null?" WHERE titleCache LIKE :pattern":" WHERE 1 = 1 ")
                        + (termType!=null?" AND termType = :termType ":"")
                );
        if(pattern!=null){
            pattern = pattern.replace("*", "%");
            pattern = pattern.replace("?", "_");
            pattern = pattern + "%";
            query.setParameter("pattern", pattern);
        }
        if(termType!=null){
            query.setParameter("termType", termType);
        }
        if (limit != null){
           query.setMaxResults(limit);
        }
        return getUuidAndTitleCache(query);
    }

}