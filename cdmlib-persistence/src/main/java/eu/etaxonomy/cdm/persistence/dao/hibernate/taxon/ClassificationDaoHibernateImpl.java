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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.TreeIndex;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeDao;
import eu.etaxonomy.cdm.persistence.dto.ClassificationLookupDTO;

/**
 * @author a.mueller
 * @since 16.06.2009
 */
@Repository
@Qualifier("classificationDaoHibernateImpl")
public class ClassificationDaoHibernateImpl
        extends IdentifiableDaoBase<Classification>
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
            boolean includeUnpublished, Integer limit, Integer start, List<String> propertyPaths, int queryIndex){

        List<TaxonNode> results = new ArrayList<>();
        Query[] queries = prepareRankSpecificRootNodes(classification, rank, includeUnpublished, false);

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
    public long[] countRankSpecificRootNodes(Classification classification, boolean includeUnpublished, Rank rank) {

        long[] result = new long[(rank == null ? 1 : 2)];
        Query[] queries = prepareRankSpecificRootNodes(classification, rank, includeUnpublished, true);
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
    private Query[] prepareRankSpecificRootNodes(Classification classification, Rank rank,
            boolean includeUnpublished, boolean doCount) {
        Query query1;
        Query query2 = null;

        String whereClassification = classification != null? " AND tn.classification = :classification " : "";
        String whereUnpublished = includeUnpublished? "" : " AND tn.taxon.publish = :publish ";

        String selectWhat = doCount ? "COUNT(distinct tn)" : "DISTINCT tn";

        String joinFetch = doCount ? "" : " JOIN FETCH tn.taxon t JOIN FETCH t.name n LEFT JOIN FETCH n.rank LEFT JOIN FETCH t.sec ";

        if(rank == null){
            String hql = "SELECT " + selectWhat + " FROM TaxonNode tn" +
                    joinFetch +
                    " WHERE tn.parent.parent = null " +
                    whereClassification +  whereUnpublished;
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
                    + whereClassification + whereUnpublished ;

            // this is for the case
            //   - rank of root node is lower and it has a parent with higher rank
            String hql2 = "SELECT " + selectWhat + " FROM TaxonNode tn JOIN tn.parent as parent" +
                    joinFetch +
                    " WHERE " +
                    " (tn.taxon.name.rank.orderIndex > :rankOrderIndex AND parent.taxon.name.rank.orderIndex < :rankOrderIndex )"
                    + whereClassification + whereUnpublished;
            query1 = getSession().createQuery(hql1);
            query2 = getSession().createQuery(hql2);
            query1.setParameter("rank", rank);
            query1.setParameter("rankOrderIndex", rank.getOrderIndex());
            query2.setParameter("rankOrderIndex", rank.getOrderIndex());
        }

        //parameters
        if (classification != null){
            query1.setParameter("classification", classification);
            if(query2 != null) {
                query2.setParameter("classification", classification);
            }
        }
        if (!includeUnpublished){
            query1.setBoolean("publish", true);
            if(query2 != null) {
                query2.setBoolean("publish", true);
            }
        }

        if(query2 != null) {
            return new Query[]{query1, query2};
        } else {
            return new Query[]{query1};
        }
    }

    @Override
    public List<TaxonNode> listChildrenOf(Taxon taxon, Classification classification, boolean includeUnpublished,
            Integer pageSize, Integer pageIndex, List<String> propertyPaths){
    	 Query query = prepareListChildrenOf(taxon, classification, false, includeUnpublished);

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
        String queryString =
                  " SELECT tn "
                + " FROM TaxonNode tn, Classification c "
                + " WHERE tn = c.rootNode AND c.uuid = :classificationUuid";

        Query query = getSession().createQuery(queryString);
        query.setParameter("classificationUuid", classificationUuid);

        @SuppressWarnings("unchecked")
        List<TaxonNode> results = query.list();
        if(results.size()!=1){
            return null;
        }
        return taxonNodeDao.load((results.iterator().next()).getUuid());
    }

    @Override
    public List<TaxonNode> listSiblingsOf(Taxon taxon, Classification classification, boolean includeUnpublished,
            Integer pageSize, Integer pageIndex, List<String> propertyPaths){
         Query query = prepareListSiblingsOf(taxon, classification, includeUnpublished, false);

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
    public Long countChildrenOf(Taxon taxon, Classification classification,
            boolean includeUnpublished){
        Query query = prepareListChildrenOf(taxon, classification, true, includeUnpublished);
        Long count = (Long) query.uniqueResult();
        return count;
    }

    @Override
    public Long countSiblingsOf(Taxon taxon, Classification classification, boolean includeUnpublished){
        Query query = prepareListSiblingsOf(taxon, classification, includeUnpublished, true);
        Long count = (Long) query.uniqueResult();
        return count;
    }

    private Query prepareListChildrenOf(Taxon taxon, Classification classification,
            boolean doCount, boolean includeUnpublished){

    	 String selectWhat = doCount ? "COUNT(cn)" : "cn";

         String hql = "SELECT " + selectWhat
                 + " FROM TaxonNode AS tn "
                 + "   JOIN tn.classification AS c "
                 + "   JOIN tn.taxon AS t "
                 + "   JOIN tn.childNodes AS cn "
                 + " WHERE t = :taxon "
                 + "   AND c = :classification";
         if (!includeUnpublished){
             hql += "  AND cn.taxon.publish = :publish ";
         }
         Query query = getSession().createQuery(hql);
         query.setParameter("taxon", taxon);
         query.setParameter("classification", classification);
         if (!includeUnpublished){
             query.setBoolean("publish", Boolean.TRUE);
         }
         return query;
    }

    private Query prepareListSiblingsOf(Taxon taxon, Classification classification,
            boolean includeUnpublished, boolean doCount){

         String selectWhat = doCount ? "COUNT(tn)" : "tn";
         String whereUnpublished = includeUnpublished? "" : " AND t.publish = :publish ";

         String subSelect =
                   " SELECT tn.parent "
                 + " FROM TaxonNode AS tn "
                 + "     JOIN tn.classification AS c "
                 + "     JOIN tn.taxon AS t "
                 + " WHERE t = :taxon "
                 + "   AND c = :classification "
                 + whereUnpublished;
         String hql = " SELECT " + selectWhat
                 + " FROM TaxonNode as tn "
                 + " WHERE tn.parent IN ( " + subSelect + ")";
         Query query = getSession().createQuery(hql);
         query.setParameter("taxon", taxon);
         query.setParameter("classification", classification);
         if (!includeUnpublished){
             query.setBoolean("publish", true);
         }
         return query;
    }


    @Override
    public UUID delete(Classification persistentObject){
        //delete all child nodes, then delete the tree
        if (persistentObject.getRootNode() != null){
            List<TaxonNode> nodes = persistentObject.getChildNodes();
            List<TaxonNode> nodesTmp = new ArrayList<>(nodes);
            for(TaxonNode node : nodesTmp){
                persistentObject.deleteChildNode(node, true);
                taxonNodeDao.delete(node, true);
            }
        }

        TaxonNode rootNode = persistentObject.getRootNode();
        persistentObject.removeRootNode();
        taxonNodeDao.delete(rootNode);
        super.delete(persistentObject);

        return persistentObject.getUuid();
    }

    @Override
    public ClassificationLookupDTO classificationLookup(Classification classification) {

        ClassificationLookupDTO classificationLookupDTO = new ClassificationLookupDTO(classification);

        String hql =
                " SELECT t.id, n.rank, tp.id "
              + " FROM TaxonNode AS tn "
              +   " JOIN tn.classification AS c "
              +   " JOIN tn.taxon AS t "
              +   " JOIN t.name AS n "
              +   " LEFT JOIN tn.parent AS tnp "
              +   " LEFT JOIN tnp.taxon as tp "
              + " WHERE c = :classification";

        Query query = getSession().createQuery(hql);
        query.setParameter("classification", classification);
        @SuppressWarnings("unchecked")
        List<Object[]> result = query.list();
        for(Object[] row : result) {
            Integer parentId = null;
            parentId = (Integer) row[2];
            classificationLookupDTO.add((Integer)row[0], (Rank)row[1], parentId);
        }

        return classificationLookupDTO ;
    }

    @Override
    public Map<UUID, TreeIndex> treeIndexForTaxonUuids(UUID classificationUuid,
            List<UUID> taxonUuids) {
        String hql = " SELECT t.uuid, tn.treeIndex "
                + " FROM Taxon t JOIN t.taxonNodes tn "
                + " WHERE (1=1)"
                + "     AND tn.classification.uuid = :classificationUuid "
                + "     AND t.uuid IN (:taxonUuids) "
                ;
        Query query =  getSession().createQuery(hql);
        query.setParameter("classificationUuid", classificationUuid);
        query.setParameterList("taxonUuids", taxonUuids);

        Map<UUID, TreeIndex> result = new HashMap<>();
        @SuppressWarnings("unchecked")
        List<Object[]> list = query.list();
        for (Object[] o : list){
            result.put((UUID)o[0], TreeIndex.NewInstance((String)o[1]));
        }
        return result;
    }

    @Override
    public Set<TreeIndex> getMarkedTreeIndexes(MarkerType markerType, Boolean flag){
        String hql = " SELECT tn.treeIndex "
                + " FROM Taxon t "
                + "    JOIN t.taxonNodes tn "
                + "    JOIN t.markers m "
                + " WHERE (1=1)"
                + "    AND m.markerType = :markerType "
                ;
        if (flag != null){
            hql += "  AND m.flag = :flag ";

        }

        Query query =  getSession().createQuery(hql);
        if (flag != null){
            query.setParameter("flag", flag);
        }
        query.setParameter("markerType", markerType);

        Set<TreeIndex> result = new HashSet<>();
        @SuppressWarnings("unchecked")
        List<String> list = query.list();
        for (String o : list){
            result.add(TreeIndex.NewInstance(o));
        }
        return result;
    }

    @Override
    public Map<UUID, UUID> getTaxonNodeUuidByTaxonUuid(UUID classificationUuid, List<UUID> taxonUuids) {
        String hql = " SELECT t.uuid, tn.uuid "
                + " FROM Taxon t JOIN t.taxonNodes tn "
                + " WHERE (1=1)"
                + "     AND tn.classification.uuid = :classificationUuid "
                + "     AND t.uuid IN (:taxonUuids) "
                ;
        Query query =  getSession().createQuery(hql);
        query.setParameter("classificationUuid", classificationUuid);
        query.setParameterList("taxonUuids", taxonUuids);

        Map<UUID, UUID> result = new HashMap<>();
        @SuppressWarnings("unchecked")
        List<Object[]> list = query.list();
        for (Object[] o : list){
            result.put((UUID)o[0], (UUID)o[1]);
        }
        return result;
    }

}
