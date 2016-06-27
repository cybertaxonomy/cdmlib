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
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
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
    public List<TaxonNode> listRankSpecificRootNodes(Classification classification, Rank rank,
            Integer limit, Integer start, List<String> propertyPaths, int queryIndex){

        List<TaxonNode> results = new ArrayList<TaxonNode>();
        Query[] queries = prepareRankSpecificRootNodes(classification, rank, false);

        // since this method is using two queries sequentially the handling of limit and start
        // is a bit more complex
        // the prepareRankSpecificRootNodes returns 1 or 2 queries

        Query q = queries[queryIndex];
        if(limit != null) {
            q.setMaxResults(limit);
            if(start != null) {
                q.setFirstResult(start);
            }
        }
//        long start_t = System.currentTimeMillis();
        results = q.list();
//        System.err.println("dao.listRankSpecificRootNodes() - query[" + queryIndex + "].list() " + (System.currentTimeMillis() - start_t));
//        start_t = System.currentTimeMillis();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
//        System.err.println("dao.listRankSpecificRootNodes() - defaultBeanInitializer.initializeAll() " + (System.currentTimeMillis() - start_t));

        return results;

    }

    @Override
    public long[] countRankSpecificRootNodes(Classification classification, Rank rank) {

        long[] result = new long[(rank == null ? 1 : 2)];
        Query[] queries = prepareRankSpecificRootNodes(classification, rank, true);
        int i = 0;
        for(Query q : queries) {
            result[i++] = (Long)q.uniqueResult();
        }
        return result;
    }

    /**
     * See <a href="http://dev.e-taxonomy.eu/trac/wiki/CdmClassificationRankSpecificRootnodes">
     * http://dev.e-taxonomy.eu/trac/wiki/CdmClassificationRankSpecificRootnodes</a>
     *
     * @param classification
     * @param rank
     * @return
     *      one or two Queries as array, depending on the <code>rank</code> parameter:
     *      <code>rank == null</code>: array with one item, <code>rank != null</code>: array with two items.
     */
    private Query[] prepareRankSpecificRootNodes(Classification classification, Rank rank, boolean doCount) {
        Query query1;
        Query query2 = null;

        String whereClassification = "";
        if (classification != null){
            whereClassification = " AND tn.classification = :classification ";
        }

        String selectWhat = doCount ? "count(distinct tn)" : "distinct tn";

        String joinFetch = doCount ? "" : " JOIN FETCH tn.taxon t JOIN FETCH t.name n LEFT JOIN FETCH n.rank LEFT JOIN FETCH t.sec ";

        if(rank == null){
            String hql = "SELECT " + selectWhat + " FROM TaxonNode tn" +
                    joinFetch +
                    " WHERE tn.parent.parent = null " +
                    whereClassification;
            query1 = getSession().createQuery(hql);
        } else {
            // this is for the cases
            //   - exact match of the ranks
            //   - rank of root node is lower but is has no parents
            String hql1 = "SELECT " + selectWhat + " FROM TaxonNode tn " +
                    joinFetch +
                    " WHERE " +
                    " (tn.taxon.name.rank = :rank" +
                    "   OR (tn.taxon.name.rank.orderIndex > :rankOrderIndex AND tn.parent.parent = null)" +
                    " )"
                    + whereClassification ;

            // this is for the case
            //   - rank of root node is lower and it has a parent with higher rank
            String hql2 = "SELECT " + selectWhat + " FROM TaxonNode tn JOIN tn.parent as parent" +
                    joinFetch +
                    " WHERE " +
                    " (tn.taxon.name.rank.orderIndex > :rankOrderIndex AND parent.taxon.name.rank.orderIndex < :rankOrderIndex )"
                    + whereClassification ;
            query1 = getSession().createQuery(hql1);
            query2 = getSession().createQuery(hql2);
            query1.setParameter("rank", rank);
            query1.setParameter("rankOrderIndex", rank.getOrderIndex());
            query2.setParameter("rankOrderIndex", rank.getOrderIndex());
        }

        if (classification != null){
            query1.setParameter("classification", classification);
            if(query2 != null) {
                query2.setParameter("classification", classification);
            }
        }
        if(query2 != null) {
            return new Query[]{query1, query2};
        } else {
            return new Query[]{query1};
        }
    }

    @Override
    public List<TaxonNode> listChildrenOf(Taxon taxon, Classification classification, Integer pageSize, Integer pageIndex, List<String> propertyPaths){
    	 Query query = prepareListChildrenOf(taxon, classification, false);

         setPagingParameter(query, pageSize, pageIndex);

         @SuppressWarnings("unchecked")
         List<TaxonNode> result = query.list();
         //check if array is "empty" (not containing null objects)
         if(!result.isEmpty() && result.iterator().next()==null){
         	return java.util.Collections.emptyList();
         }
         defaultBeanInitializer.initializeAll(result, propertyPaths);
         return result;
    }

    @Override
    public TaxonNode getRootNode(UUID classificationUuid){
        String queryString = "select tn from TaxonNode tn, Classification c where tn = c.rootNode and c.uuid = :classificationUuid";

        Query query = getSession().createQuery(queryString);
        query.setParameter("classificationUuid", classificationUuid);


        List results = query.list();
        if(results.size()!=1){
            return null;
        }
        return taxonNodeDao.load(((TaxonNode) results.iterator().next()).getUuid());
    }

    @Override
    public List<TaxonNode> listSiblingsOf(Taxon taxon, Classification classification, Integer pageSize, Integer pageIndex, List<String> propertyPaths){
         Query query = prepareListSiblingsOf(taxon, classification, false);

         setPagingParameter(query, pageSize, pageIndex);

         @SuppressWarnings("unchecked")
         List<TaxonNode> result = query.list();
         //check if array is "empty" (not containing null objects)
         if(!result.isEmpty() && result.iterator().next()==null){
            return java.util.Collections.emptyList();
         }
         defaultBeanInitializer.initializeAll(result, propertyPaths);
         return result;
    }



    @Override
    public Long countChildrenOf(Taxon taxon, Classification classification){
        Query query = prepareListChildrenOf(taxon, classification, true);
        Long count = (Long) query.uniqueResult();
        return count;
    }

    @Override
    public Long countSiblingsOf(Taxon taxon, Classification classification){
        Query query = prepareListSiblingsOf(taxon, classification, true);
        Long count = (Long) query.uniqueResult();
        return count;
    }

    private Query prepareListChildrenOf(Taxon taxon, Classification classification, boolean doCount){

    	String selectWhat = doCount ? "count(cn)" : "cn";

         String hql = "select " + selectWhat + " from TaxonNode as tn JOIN tn.classification as c JOIN tn.taxon as t JOIN tn.childNodes as cn "
                 + "where t = :taxon and c = :classification";
         Query query = getSession().createQuery(hql);
         query.setParameter("taxon", taxon);
         query.setParameter("classification", classification);
         return query;
    }

    private Query prepareListSiblingsOf(Taxon taxon, Classification classification, boolean doCount){

        String selectWhat = doCount ? "count(tn)" : "tn";

         String subSelect = "SELECT tn.parent FROM TaxonNode as tn JOIN tn.classification as c JOIN tn.taxon as t "
                 + "WHERE t = :taxon AND c = :classification";
         String hql = "SELECT " + selectWhat + " FROM TaxonNode as tn WHERE tn.parent IN ( " + subSelect + ")";
         Query query = getSession().createQuery(hql);
         query.setParameter("taxon", taxon);
         query.setParameter("classification", classification);
         return query;
    }


    @Override
    public UUID delete(Classification persistentObject){
        //delete all childnodes, then delete the tree

        List<TaxonNode> nodes = persistentObject.getChildNodes();
        List<TaxonNode> nodesTmp = new ArrayList<TaxonNode>(nodes);

//        Iterator<TaxonNode> nodesIterator = nodes.iterator();
        for(TaxonNode node : nodesTmp){
            persistentObject.deleteChildNode(node, true);
            taxonNodeDao.delete(node, true);
        }

        TaxonNode rootNode = persistentObject.getRootNode();
        persistentObject.removeRootNode();
        taxonNodeDao.delete(rootNode);
        super.delete(persistentObject);

        return persistentObject.getUuid();
    }




}
