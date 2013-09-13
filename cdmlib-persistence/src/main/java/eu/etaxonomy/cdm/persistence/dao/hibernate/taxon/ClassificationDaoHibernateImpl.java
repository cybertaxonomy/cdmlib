// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.taxon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeDao;

/**
 * @author a.mueller
 * @created 16.06.2009
 * @version 1.0
 */
@Repository
@Qualifier("classificationDaoHibernateImpl")
public class ClassificationDaoHibernateImpl extends IdentifiableDaoBase<Classification>
        implements IClassificationDao {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ClassificationDaoHibernateImpl.class);

    @Autowired
    private ITaxonNodeDao taxonNodeDao;

    public ClassificationDaoHibernateImpl() {
        super(Classification.class);
        indexedClasses = new Class[1];
        indexedClasses[0] = Classification.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<TaxonNode> loadRankSpecificRootNodes(Classification classification, Rank rank,
            Integer limit, Integer start, List<String> propertyPaths){

        Query query = prepareRankSpecificRootNodes(classification, rank, false);

        if(limit != null) {
            query.setMaxResults(limit);
            if(start != null) {
                query.setFirstResult(start);
            }
        }

        List<TaxonNode> results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;

    }

    @Override
    public long countRankSpecificRootNodes(Classification classification, Rank rank) {

        Query query = prepareRankSpecificRootNodes(classification, rank, true);
        return (Long)query.uniqueResult();
    }

    /**
     * @param classification
     * @param rank
     * @return
     */
    private Query prepareRankSpecificRootNodes(Classification classification, Rank rank, boolean doCount) {
        Query query;

        String whereClassification = "";
        if (classification != null){
            whereClassification = " AND tn.classification = :classification ";
        }

        String selectWhat = doCount ? "count(distinct tn)" : "distinct tn";

        if(rank == null){
            String hql = "SELECT " + selectWhat + " FROM TaxonNode tn LEFT JOIN tn.childNodes as tnc" +
                " WHERE tn.parent = null " +
                whereClassification;
            query = getSession().createQuery(hql);
        } else {
            String hql = "SELECT " + selectWhat + " FROM TaxonNode tn LEFT JOIN tn.childNodes as tnc" +
                " WHERE " +
                " (tn.taxon.name.rank = :rank" +
                "   OR (tn.taxon.name.rank.orderIndex > :rankOrderIndex AND tn.parent = null)" +
                "   OR (tn.taxon.name.rank.orderIndex < :rankOrderIndex AND tnc.taxon.name.rank.orderIndex > :rankOrderIndex)" +
                " )" +
                whereClassification;
            query = getSession().createQuery(hql);
            query.setParameter("rank", rank);
            query.setParameter("rankOrderIndex", rank.getOrderIndex());
        }

        if (classification != null){
            query.setParameter("classification", classification);
        }
        return query;
    }

    @Override
    public UUID delete(Classification persistentObject){
        //delete all childnodes, then delete the tree

        List<TaxonNode> nodes = persistentObject.getChildNodes();
        List<TaxonNode> nodesTmp = new ArrayList<TaxonNode>(nodes);
//        Iterator<TaxonNode> nodesIterator = nodes.iterator();
        
        
        for(TaxonNode node : nodesTmp){
            taxonNodeDao.delete(node);
        }

        super.delete(persistentObject);

        return persistentObject.getUuid();
    }



}
