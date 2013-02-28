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
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao;

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

    @SuppressWarnings("unchecked")
    public List<TaxonNode> loadRankSpecificRootNodes(Classification classification, Rank rank, List<String> propertyPaths){
        List<TaxonNode> results;

        Query query;

        String whereClassification = "";
        if (classification != null){
            whereClassification = " AND tn.classification = :classification ";
        }

        if(rank == null){
            String hql = "SELECT DISTINCT tn FROM TaxonNode tn LEFT JOIN tn.childNodes as tnc" +
                " WHERE tn.parent = null " +
                whereClassification;
            query = getSession().createQuery(hql);
        } else {
            String hql = "SELECT DISTINCT tn FROM TaxonNode tn LEFT JOIN tn.childNodes as tnc" +
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

        results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;

    }

    @Override
    public UUID delete(Classification persistentObject){
        //delete all childnodes, then delete the tree

        Set<TaxonNode> nodes = persistentObject.getChildNodes();
        Iterator<TaxonNode> nodesIterator = nodes.iterator();

        while(nodesIterator.hasNext()){
            TaxonNode node = nodesIterator.next();
            taxonNodeDao.delete(node);
        }

        super.delete(persistentObject);

        return persistentObject.getUuid();
    }



}
