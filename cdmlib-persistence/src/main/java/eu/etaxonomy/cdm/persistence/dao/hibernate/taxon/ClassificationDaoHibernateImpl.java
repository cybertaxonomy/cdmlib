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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.query.Query;
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
import eu.etaxonomy.cdm.persistence.dto.SortableTaxonNodeQueryResult;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;

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
    private static final Logger logger = LogManager.getLogger();

    @Autowired
    private ITaxonNodeDao taxonNodeDao;

    public ClassificationDaoHibernateImpl() {
        super(Classification.class);
        indexedClasses = new Class[1];
        indexedClasses[0] = Classification.class;
    }

    @Override
    public List<TaxonNode> listRankSpecificRootNodes(Classification classification, TaxonNode taxonNode, Rank rank,
            boolean includeUnpublished, Integer limit, Integer start, List<String> propertyPaths, int queryIndex){

        List<TaxonNode> results = new ArrayList<>();
        Query<TaxonNode>[] queries = prepareRankSpecificRootNodes(classification, taxonNode, rank, includeUnpublished, false, TaxonNode.class);

        // since this method is using two queries sequentially the handling of limit and start
        // is a bit more complex
        // the prepareRankSpecificRootNodes returns 1 or 2 queries

        Query<TaxonNode> q = queries[queryIndex];
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
    public long[] countRankSpecificRootNodes(Classification classification, TaxonNode subtree, boolean includeUnpublished, Rank rank) {

        long[] result = new long[(rank == null ? 1 : 2)];
        Query<Long>[] queries = prepareRankSpecificRootNodes(classification, subtree, rank, includeUnpublished, true, Long.class);
        int i = 0;
        for(Query<Long> q : queries) {
            result[i++] = q.uniqueResult();
        }
        return result;
    }

    /**
     * See <a href="https://dev.e-taxonomy.eu/redmine/projects/edit/wiki/CdmClassificationRankSpecificRootnodes">
     * https://dev.e-taxonomy.eu/redmine/projects/edit/wiki/CdmClassificationRankSpecificRootnodes</a>
     *
     * @param classification
     * @param rank
     * @return
     *      one or two Queries as array, depending on the <code>rank</code> parameter:
     *      <code>rank == null</code>: array with one item, <code>rank != null</code>: array with two items.
     */
    private <R extends Object> Query<R>[] prepareRankSpecificRootNodes(Classification classification,
            TaxonNode subtree, Rank rank,
            boolean includeUnpublished, boolean doCount, Class<R> resultClass) {

        Query<R> query1;
        Query<R> query2 = null;

        String whereClassification = classification != null? " AND tn.classification = :classification " : "";
        String whereUnpublished = includeUnpublished? "" : " AND tn.taxon.publish = :publish ";
        String whereSubtree = subtree != null ? " AND tn.treeIndex like :treeIndexLike " : "";
        TreeIndex treeIndex = TreeIndex.NewInstance(subtree);
        String whereHighest =
                treeIndex == null ? " tn.parent.parent = null ":
                treeIndex.isTreeRoot() ? " tn.parent.treeIndex = :treeIndex ":
                            " tn.treeIndex = :treeIndex "   ;

        String selectWhat = doCount ? "COUNT(distinct tn)" : "DISTINCT tn";

        String joinFetch = doCount ? "" : " JOIN FETCH tn.taxon t JOIN FETCH t.name n LEFT JOIN FETCH n.rank LEFT JOIN FETCH t.secSource ss LEFT JOIN FETCH ss.citation ";

        if(rank == null){
            String hql = "SELECT " + selectWhat +
                    " FROM TaxonNode tn" +
                        joinFetch +
                    " WHERE " + whereHighest +
                    whereClassification + whereUnpublished;
            query1 = getSession().createQuery(hql, resultClass);
        } else {
            // this is for the cases
            //   - exact match of the ranks
            //   - rank of root node is lower but it has no parents
            String hql1 = "SELECT " + selectWhat +
                    " FROM TaxonNode tn " +
                       joinFetch +
                    " WHERE " +
                    " (tn.taxon.name.rank = :rank" +
                    "   OR ((tn.taxon.name.rank.orderIndex > :rankOrderIndex) AND (" + whereHighest + "))" +
                    " )"
                    + whereClassification + whereSubtree + whereUnpublished ;

            // this is for the case
            //   - rank of root node is lower and it has a parent with higher rank
            String whereParentSubtree = subtree != null ? " AND parent.treeIndex like :treeIndexLike " : "";
            String hql2 = "SELECT " + selectWhat +
                    " FROM TaxonNode tn JOIN tn.parent as parent" +
                       joinFetch +
                    " WHERE " +
                    " (tn.taxon.name.rank.orderIndex > :rankOrderIndex "
                    + "     AND parent.taxon.name.rank.orderIndex < :rankOrderIndex )"
                    + whereClassification + whereSubtree
                    + whereParentSubtree + whereUnpublished;

            query1 = getSession().createQuery(hql1, resultClass);
            query2 = getSession().createQuery(hql2, resultClass);
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
        if (subtree != null){
            query1.setParameter("treeIndex", subtree.treeIndex());
            if (rank != null){
                query1.setParameter("treeIndexLike", subtree.treeIndex()+"%");
            }
            if(query2 != null) {
                query2.setParameter("treeIndexLike", subtree.treeIndex()+"%");
            }
        }
        if (!includeUnpublished){
            query1.setParameter("publish", true);
            if(query2 != null) {
                query2.setParameter("publish", true);
            }
        }

        if(query2 != null) {
            return new Query[]{query1, query2};
        } else {
            return new Query[]{query1};
        }
    }

    @Override
    public List<TaxonNodeDto> listChildrenOf(Taxon taxon, Classification classification, TaxonNode subtree,
            boolean includeUnpublished, Integer pageSize, Integer pageIndex){

         Query<SortableTaxonNodeQueryResult> query = prepareListChildrenOf(taxon, classification,
                 subtree, QueryType.DTO, includeUnpublished, SortableTaxonNodeQueryResult.class);

         addPageSizeAndNumber(query, pageSize, pageIndex);

         List<SortableTaxonNodeQueryResult> queryResult = query.list();
         //check if array is "empty" (not containing null objects)  //copied from non-DTO version. Necessary here?
         if(!queryResult.isEmpty() && queryResult.iterator().next()==null){
            return java.util.Collections.emptyList();
         }
         List<TaxonNodeDto> result = SortableTaxonNodeQueryResult.toTaxonNodeDtoList(queryResult);
         return result;
    }

    @Override
    public List<TaxonNode> listChildrenOf(Taxon taxon, Classification classification, TaxonNode subtree, boolean includeUnpublished,
            Integer pageSize, Integer pageIndex, List<String> propertyPaths){

        Query<TaxonNode> query = prepareListChildrenOf(taxon, classification,
                 subtree, QueryType.INSTANCE, includeUnpublished, TaxonNode.class);

         addPageSizeAndNumber(query, pageSize, pageIndex);
//         query.setHint( "org.hibernate.readOnly", true );  //TODO does not seem to have an effect. (https://docs.jboss.org/hibernate/orm/5.4/userguide/html_single/Hibernate_User_Guide.html#hql-read-only-entities)
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

        Query<TaxonNode> query = getSession().createQuery(queryString, TaxonNode.class);
        query.setParameter("classificationUuid", classificationUuid);

        List<TaxonNode> results = query.list();
        if(results.size()!=1){
            return null;
        }
        return taxonNodeDao.load((results.iterator().next()).getUuid());
    }

    @Override
    public List<TaxonNode> listSiblingsOf(Taxon taxon, Classification classification, boolean includeUnpublished,
            Integer pageSize, Integer pageIndex, List<String> propertyPaths){
         Query<TaxonNode> query = prepareListSiblingsOf(taxon, classification, includeUnpublished, false, TaxonNode.class);

         addPageSizeAndNumber(query, pageSize, pageIndex);

         List<TaxonNode> result = query.list();
         //check if array is "empty" (not containing null objects)
         if(!result.isEmpty() && result.iterator().next()==null){
            return java.util.Collections.emptyList();
         }
         defaultBeanInitializer.initializeAll(result, propertyPaths);
         return result;
    }

    @Override
    public Long countChildrenOf(Taxon taxon, Classification classification, TaxonNode subtree,
            boolean includeUnpublished){
        Query<Long> query = prepareListChildrenOf(taxon, classification, subtree,
                QueryType.COUNT, includeUnpublished, Long.class);
        Long count = query.uniqueResult();
        return count;
    }

    @Override
    public Long countSiblingsOf(Taxon taxon, Classification classification, boolean includeUnpublished){
        Query<Long> query = prepareListSiblingsOf(taxon, classification, includeUnpublished, true, Long.class);
        Long count = query.uniqueResult();
        return count;
    }

    private enum QueryType {COUNT, DTO, INSTANCE}

    public class TaxonNodeQueryResult {
        public UUID nodeUuid; Integer nodeId; UUID taxonUuid; String taxonTitleCache;

        public TaxonNodeQueryResult(String titleCache) {
        }
        public TaxonNodeQueryResult(UUID nodeUuid) {
        }
        public TaxonNodeQueryResult(UUID nodeUuid, int nodeId, UUID taxonUuid, String taxonTitleCache) {
            this.nodeUuid = nodeUuid;
            this.nodeId = nodeId;
            this.taxonUuid = taxonUuid;
            this.taxonTitleCache = taxonTitleCache;
        }

    }

    private <R extends Object> Query<R> prepareListChildrenOf(Taxon taxon, Classification classification, TaxonNode subtree,
            QueryType queryType, boolean includeUnpublished, Class<R> resultClass){

    	 String selectWhat = queryType == QueryType.COUNT ? "COUNT(cn)"
    	         : queryType == QueryType.INSTANCE ? "cn, cn.taxon, cn.taxon.name"
    	         : (" new " +SortableTaxonNodeQueryResult.class.getName()+"("
    	                 + "cn.uuid "
    	                 + ", cn.id "
    	                 + ", cn.treeIndex "
    	                 + ", cn.taxon.uuid "
    	                 + ", cn.taxon.titleCache "
    	                 + ", cn.taxon.name.titleCache "
    	                 + ", cn.taxon.name.rank "
    	                 + ", tn.uuid " //parent.uuid
    	                 + ", index(cn) " //sortIndex,"
    	                 + ", c.uuid "
     	                 + ", cn.taxon.publish "
     	                 + ", cn.status "
    	                 + ", cn.countChildren"
    	                 + ", cn.taxon.secSource.citation.uuid"
    	                 + ", cn.taxon.name.nameType"
                         + ", cn.taxon.name.genusOrUninomial"
                         + ", cn.taxon.name.infraGenericEpithet"
                         + ", cn.taxon.name.specificEpithet"
                         + ", cn.taxon.name.infraSpecificEpithet"
                         + ", cn.taxon.name.appendedPhrase"
                         + ", cn.taxon.name.protectedTitleCache"
                         + ", cn.taxon.name.protectedNameCache"
                         + ", cn.taxon.name.nameCache"
                         + ", cn.taxon.name.authorshipCache"
                         + ", cn.taxon.name.publicationYear"
                         + ", cn.taxon.name.monomHybrid"
                         + ", cn.taxon.name.binomHybrid"
                         + ", cn.taxon.name.trinomHybrid"

//     	                 + ", entry(cn.statusNote) "  //cn.statusNote
//    	                 + ", cn.taxon.name.rank.orderIndex "
//    	                 + "cn.taxon.name.rank.titleCache "  //TODO maybe ...rank.representations (?)
                         + ")"
)

    	                 ;   //TODO language dependent;

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
         if (subtree != null){
             hql += "  AND tn.treeIndex like :treeIndexLike ";
         }
         Query<R> query = getSession().createQuery(hql, resultClass);
         query.setParameter("taxon", taxon);
         query.setParameter("classification", classification);
         if (!includeUnpublished){
             query.setParameter("publish", Boolean.TRUE);
         }
         if (subtree != null){
             query.setParameter("treeIndexLike", subtree.treeIndexLike());
         }
         return query;
    }

    private <R extends Object> Query<R> prepareListSiblingsOf(Taxon taxon, Classification classification,
            boolean includeUnpublished, boolean doCount, Class<R> resultClass){

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
         Query<R> query = getSession().createQuery(hql, resultClass);
         query.setParameter("taxon", taxon);
         query.setParameter("classification", classification);
         if (!includeUnpublished){
             query.setParameter("publish", true);
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

        Query<Object[]> query = getSession().createQuery(hql, Object[].class);
        query.setParameter("classification", classification);

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
        Query<Object[]> query =  getSession().createQuery(hql, Object[].class);
        query.setParameter("classificationUuid", classificationUuid);
        query.setParameterList("taxonUuids", taxonUuids);

        Map<UUID, TreeIndex> result = new HashMap<>();
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

        Query<String> query =  getSession().createQuery(hql, String.class);
        if (flag != null){
            query.setParameter("flag", flag);
        }
        query.setParameter("markerType", markerType);

        Set<TreeIndex> result = new HashSet<>();

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
        Query<Object[]> query =  getSession().createQuery(hql, Object[].class);
        query.setParameter("classificationUuid", classificationUuid);
        query.setParameterList("taxonUuids", taxonUuids);

        Map<UUID, UUID> result = new HashMap<>();
        List<Object[]> list = query.list();
        for (Object[] o : list){
            result.put((UUID)o[0], (UUID)o[1]);
        }
        return result;
    }
}