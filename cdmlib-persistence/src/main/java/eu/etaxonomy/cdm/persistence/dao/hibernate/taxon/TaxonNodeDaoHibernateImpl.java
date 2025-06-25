/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.taxon;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.TreeIndex;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.NamedSource;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.SecundumSource;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeAgentRelation;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.persistence.dao.common.Restriction;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.AnnotatableDaoBaseImpl;
import eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonRelationshipDao;
import eu.etaxonomy.cdm.persistence.dto.SortableTaxonNodeQueryResult;
import eu.etaxonomy.cdm.persistence.dto.SortableTaxonNodeQueryResultComparator;
import eu.etaxonomy.cdm.persistence.dto.SortableTaxonNodeWithoutSecQueryResult;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.mueller
 * @since 16.06.2009
 */
@Repository
@Qualifier("taxonNodeDaoHibernateImpl")
public class TaxonNodeDaoHibernateImpl extends AnnotatableDaoBaseImpl<TaxonNode>
		implements ITaxonNodeDao {

	private static final Logger logger = LogManager.getLogger();

    private static final int DEFAULT_SET_SUBTREE_PARTITION_SIZE = 100;

	@Autowired
	private ITaxonDao taxonDao;
	@Autowired
	private IClassificationDao classificationDao;
    @Autowired
    private ITaxonRelationshipDao taxonRelDao;

	public TaxonNodeDaoHibernateImpl() {
		super(TaxonNode.class);
	}

	@Override
	public UUID delete(TaxonNode persistentObject, boolean deleteChildren){
		Taxon taxon = persistentObject.getTaxon();
		taxon = HibernateProxyHelper.deproxy(taxon);

		/*Session session = this.getSession();
		Query<TaxonNode> query = session.createQuery("from TaxonNode t where t.taxon = :taxon", TaxonNode.class);
		query.setParameter("taxon", taxon);
		List result = query.list();*/
		if (taxon != null){
		    Hibernate.initialize(taxon);
		    Hibernate.initialize(taxon.getTaxonNodes());
			Set<TaxonNode> nodes = taxon.getTaxonNodes();
			//Hibernate.initialize(taxon.getTaxonNodes());
			for (TaxonNode node:nodes) {
			    node = HibernateProxyHelper.deproxy(node);

			    if (node.equals(persistentObject)){
			        if (node.hasChildNodes()){
			            Iterator<TaxonNode> childNodes = node.getChildNodes().iterator();
			            TaxonNode childNode;
			            List<TaxonNode> listForDeletion = new ArrayList<>();
	                    while (childNodes.hasNext()){
	                        childNode = childNodes.next();
	                        listForDeletion.add(childNode);
	                        childNodes.remove();

	                    }
	                    for (TaxonNode deleteNode:listForDeletion){
	                        delete(deleteNode, deleteChildren);
	                    }
	                }

			        taxon.removeTaxonNode(node, deleteChildren);
			        taxonDao.saveOrUpdate(taxon);
    				taxon = HibernateProxyHelper.deproxy(taxonDao.findByUuid(taxon.getUuid()), Taxon.class);
    				taxonDao.delete(taxon);

			    }
			}
		}

		UUID result = super.delete(persistentObject);
		return result;
	}

	@Override
	public List<TaxonNode> getTaxonOfAcceptedTaxaByClassification(Classification classification, Integer start, Integer end) {
		int classificationId = classification.getId();
		String limit = "";
		if(start !=null && end != null){
		    limit = "LIMIT "+start+"," +end;
		}
		//FIXME write test
        String queryString = "SELECT DISTINCT nodes.*,taxa.titleCache "
                + " FROM TaxonNode AS nodes "
                + "    LEFT JOIN TaxonBase AS taxa ON nodes.taxon_id = taxa.id "
                + " WHERE taxa.DTYPE = 'Taxon' "
                + "    AND nodes.classification_id = " + classificationId +
                  " ORDER BY taxa.titleCache " + limit;
        @SuppressWarnings("unchecked")
        List<TaxonNode> result  = getSession().createSQLQuery(queryString).addEntity(TaxonNode.class).list();

        return result;
	}

    @Override
    public int countTaxonOfAcceptedTaxaByClassification(Classification classification){
        int classificationId = classification.getId();
        //FIXME write test
        String queryString = ""
                + " SELECT DISTINCT COUNT('nodes.*') "
                + " FROM TaxonNode AS nodes "
                + "   LEFT JOIN TaxonBase AS taxa ON nodes.taxon_id = taxa.id "
                + " WHERE taxa.DTYPE = 'Taxon' AND nodes.classification_id = " + classificationId;
         @SuppressWarnings("unchecked")
         List<BigInteger> result = getSession().createSQLQuery(queryString).list();
         return result.get(0).intValue ();
    }

    @Override
    public List<TaxonNodeDto> listChildNodesAsUuidAndTitleCache(TaxonNodeDto parent) {
        String queryString =
                  " SELECT tn.uuid, tn.id, t.titleCache "
                + " FROM TaxonNode tn "
                + "    INNER JOIN tn.taxon AS t "
                + " WHERE tn.parent.uuid = :parentId";

        Query<Object[]> query =  getSession().createQuery(queryString, Object[].class);
        query.setParameter("parentId", parent.getUuid());

        List<Object[]> result = query.list();

        List<TaxonNodeDto> list = new ArrayList<>();
        for(Object[] object : result){
            list.add(new TaxonNodeDto((UUID) object[0],(Integer) object[1], (String) object[2]));
        }
        return list;
    }

    @Override
    public List<TaxonNodeDto> listChildNodesAsTaxonNodeDto(TaxonNodeDto parent) {
        /*String queryString =
                 " SELECT tn "
               + " FROM TaxonNode tn "
               + "    INNER JOIN tn.taxon AS t "
               + " WHERE tn.parent.uuid = :parentId";*/

        Query<SortableTaxonNodeQueryResult> query =  createQueryForUuidAndTitleCacheForChildren(parent);
        //query.setParameter("parentId", parent.getUuid());
        List<SortableTaxonNodeQueryResult> result = query.list();
        Collections.sort(result, new SortableTaxonNodeQueryResultComparator());
        if(logger.isTraceEnabled()){
            logger.trace("number of matches:" + result.size());
            result.stream().forEach(o -> logger.trace("uuid: " + o.getTaxonNodeUuid() + " titleCache:" + o.getTaxonTitleCache() + " rank: " + o.getNameRank()));
        }
        List<TaxonNodeDto> list = new ArrayList<>();
        for(SortableTaxonNodeQueryResult stnqr : result){
            TaxonNodeDto newNode = new TaxonNodeDto(stnqr.getTaxonNodeUuid(),stnqr.getTaxonNodeId(), stnqr.getTaxonUuid(), stnqr.getTreeIndex(), stnqr.getNameTitleCache(),stnqr.getTaxonTitleCache(),
                    stnqr.getNameRank().getOrderIndex(), parent.getUuid(),stnqr.getSortIndex(),parent.getClassificationUUID(), stnqr.isTaxonIsPublish(), stnqr.getStatus(), stnqr.getPlacementNote(), stnqr.getChildrenCount(), stnqr.getSecUuid(), null);

            list.add(newNode);
        }


        return list;
    }

    @Override
    public List<TaxonNodeDto> getUuidAndTitleCache(Integer limit, String pattern, UUID classificationUuid, boolean includeDoubtful) {

        Query<SortableTaxonNodeQueryResult> query = createQueryForUuidAndTitleCache(limit, classificationUuid, pattern, includeDoubtful);
        List<SortableTaxonNodeQueryResult> result = query.list();
        Collections.sort(result, new SortableTaxonNodeQueryResultComparator());
        if(logger.isTraceEnabled()){
            logger.trace("number of matches:" + result.size());
            result.stream().forEach(o -> logger.trace("uuid: " + o.getTaxonNodeUuid() + " titleCache:" + o.getTaxonTitleCache() + " rank: " + o.getNameRank()));
        }
        List<TaxonNodeDto> list = new ArrayList<>();
        for(SortableTaxonNodeQueryResult stnqr : result){
            list.add(new TaxonNodeDto(stnqr.getTaxonNodeUuid(),stnqr.getTaxonNodeId(), stnqr.getTaxonTitleCache()));
        }

        return list;
    }

    private Query<SortableTaxonNodeQueryResult> createQueryForUuidAndTitleCache(Integer limit, UUID classificationUuid, String pattern, boolean includeDoubtful){
        String doubtfulPattern = "";
        String queryString = "SELECT new " + SortableTaxonNodeQueryResult.class.getName() + "("
                + " node.uuid, node.id, node.treeIndex, t.uuid, t.titleCache, rank, parent.uuid"
                + ") "
                + " FROM TaxonNode AS node "
                + "   JOIN node.taxon as t " // FIXME why not inner join here?
                + "   INNER JOIN t.name AS name "
                + "   LEFT OUTER JOIN node.parent as parent"
                + "   LEFT OUTER JOIN name.rank AS rank "
                + " WHERE ";

      if (classificationUuid != null){
          queryString = queryString + " node.classification.uuid like :classificationUuid " ;
      }
      if (pattern == null){
          pattern = "*";
      }

      if (pattern.equals("?")){
          limit = null;
      } else{
          if (!pattern.endsWith("*")){
              pattern += "%";
          }
          pattern = pattern.replace("*", "%");
          pattern = pattern.replace("?", "%");
          if (classificationUuid != null){
              queryString = queryString + " AND ";
          }
          queryString = queryString + " (t.titleCache LIKE (:pattern) " ;
          doubtfulPattern = "?" + pattern;
          if (includeDoubtful){
              queryString = queryString + " OR t.titleCache LIKE (:doubtfulPattern))";
          }else{
              queryString = queryString + ")";
          }
      }



      Query<SortableTaxonNodeQueryResult> query =  getSession().createQuery(queryString, SortableTaxonNodeQueryResult.class);
      if (pattern != null){
          query.setParameter("pattern", pattern);
      }
      if (includeDoubtful){
          query.setParameter("doubtfulPattern", doubtfulPattern);
      }

      if(classificationUuid != null){
          query.setParameter("classificationUuid", classificationUuid);
      }
      if (limit != null){
          query.setMaxResults(limit);
      }
      return query;
    }


    private Query<SortableTaxonNodeQueryResult> createQueryForUuidAndTitleCacheForChildren(TaxonNodeDto parent){

//        UUID taxonNodeUuid, Integer taxonNodeId, String treeIndex, UUID taxonUuid,
//        String taxonTitleCache, String nameTitleCache, Rank nameRank, UUID parentNodeUuid,
//        Integer sortIndex, UUID classificationUuid, Boolean taxonPublish, TaxonNodeStatus status,
//        Integer childrenCount, UUID secUuid
        String queryString = getTaxonNodeDtoQuery();
        queryString += " WHERE p.id = :parent_id ";


      Query<SortableTaxonNodeQueryResult> query =  getSession().createQuery(queryString, SortableTaxonNodeQueryResult.class);
      if (parent != null){
          query.setParameter("parent_id", parent.getId());
      }

      return query;
    }


    @Override
    public TaxonNodeDto getParentUuidAndTitleCache(TaxonNodeDto child) {
        String queryString = ""
                + " SELECT tn.parent.uuid, tn.parent.id, tn.parent.taxon.titleCache, "
                + "                  tn.parent.classification.titleCache "
                + " FROM TaxonNode tn"
                + "    LEFT OUTER JOIN tn.parent.taxon"
                + " WHERE tn.id = :childId";
        Query<Object[]> query =  getSession().createQuery(queryString, Object[].class);
        query.setParameter("childId", child.getId());
        List<TaxonNodeDto> list = new ArrayList<>();

        List<Object[]> result = query.list();

        for(Object[] object : result){
            UUID uuid = (UUID) object[0];
            Integer id = (Integer) object[1];
            String taxonTitleCache = (String) object[2];
            String classificationTitleCache = (String) object[3];
            if(taxonTitleCache!=null){
                list.add(new TaxonNodeDto(uuid,id, taxonTitleCache));
            }
            else{
                list.add(new TaxonNodeDto(uuid,id, classificationTitleCache));
            }
        }
        if(list.size()==1){
            return list.iterator().next();
        }
        return null;
    }
    @Override
    public List<TaxonNode> listChildrenOf(TaxonNode node, Integer pageSize, Integer pageIndex,
            boolean recursive, boolean includeUnpublished, List<String> propertyPaths, Comparator<TaxonNode> comparator){
        return listChildrenOfRecursive(node,new ArrayList<>(), pageSize, pageIndex, recursive, includeUnpublished, propertyPaths, comparator);
    }

    private List<TaxonNode> listChildrenOfRecursive(TaxonNode node, List<TaxonNode> previousResult, Integer pageSize, Integer pageIndex,
            boolean recursive, boolean includeUnpublished, List<String> propertyPaths, Comparator<TaxonNode> comparator){

        if (recursive == true && comparator == null ){

            CriteriaBuilder cb = getCriteriaBuilder();
            CriteriaQuery<TaxonNode> cq = cb.createQuery(TaxonNode.class);
            Root<TaxonNode> root = cq.from(TaxonNode.class);
    		Predicate predicate = childrenOfCriteria(cb, root, node, includeUnpublished);

    		cq.select(root)
    		  .where(predicate);

    		List<TaxonNode> results = addPageSizeAndNumber(
    		        getSession().createQuery(cq), pageSize, pageIndex)
    		    .getResultList();

    		results.remove(node);
    		defaultBeanInitializer.initializeAll(results, propertyPaths);
    		return results;

    	} else if (recursive == true){
    	    List<TaxonNode> children = node.getChildNodes();
    	    Collections.sort(children, comparator);
    	    for (TaxonNode child: children){
    	        if (!previousResult.contains(child)){
    	            previousResult.add(child);
    	        }
    	        if (child.hasChildNodes()){
    	            previousResult = listChildrenOfRecursive(child, previousResult, pageSize, pageIndex,
    	                    recursive, includeUnpublished, propertyPaths, comparator);
    	        }
    	    }
    	    return previousResult;

        } else{
    		return classificationDao.listChildrenOf(node.getTaxon(), node.getClassification(), null,
    		       includeUnpublished, pageSize, pageIndex, propertyPaths);
    	}
    }

    @Override
	public Long countChildrenOf(TaxonNode node, Classification classification,
			boolean recursive, boolean includeUnpublished) {

		if (recursive == true){
		    CriteriaBuilder cb = getCriteriaBuilder();
		    CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		    Root<TaxonNode> root = cq.from(TaxonNode.class);

		    cq.select(cb.count(root))
		      .where(childrenOfCriteria(cb, root, node, includeUnpublished));
		    return getSession().createQuery(cq).getSingleResult();
		}else{
			return classificationDao.countChildrenOf(
			        node.getTaxon(), classification, null, includeUnpublished);
		}
	}

    private Predicate childrenOfCriteria(CriteriaBuilder cb, Root<TaxonNode> root, TaxonNode node, boolean includeUnpublished) {

        Predicate p = predicateLike(cb, root, "treeIndex", node.treeIndex()+ "%");

        if (!includeUnpublished){
            Join<TaxonNode, Taxon> taxonJoin = root.join("taxon");
            p = cb.and(p, predicateBoolean(cb, taxonJoin, "publish", Boolean.TRUE));
        }
        return p;
    }

    @Override
    public List<TaxonNodeAgentRelation> listTaxonNodeAgentRelations(UUID taxonUuid, UUID classificationUuid,
            UUID agentUuid, UUID rankUuid, UUID relTypeUuid, Integer start, Integer limit,
            List<String> propertyPaths) {

        StringBuilder hql = prepareListTaxonNodeAgentRelations(taxonUuid, classificationUuid,
                agentUuid, rankUuid, relTypeUuid, false);

        Query<TaxonNodeAgentRelation> query =  getSession().createQuery(hql.toString(), TaxonNodeAgentRelation.class);
        if(limit != null) {
            query.setMaxResults(limit);
            if(start != null) {
                query.setFirstResult(start);
            }
        }

        setParamsForListTaxonNodeAgentRelations(taxonUuid, classificationUuid, agentUuid, rankUuid, relTypeUuid, query);

        List<TaxonNodeAgentRelation> records = query.list();

        if(propertyPaths != null) {
            defaultBeanInitializer.initializeAll(records, propertyPaths);
        }
        return records;
    }

    @Override
    public <S extends TaxonNode> List<S> list(Class<S> type, List<Restriction<?>> restrictions, Integer limit,
            Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {

        return list(type, restrictions, limit, start, orderHints, propertyPaths, INCLUDE_UNPUBLISHED);
    }

    @Override
    public <S extends TaxonNode> List<S> list(Class<S> type, List<Restriction<?>> restrictions, Integer limit,
            Integer start, List<OrderHint> orderHints, List<String> propertyPaths, boolean includePublished) {

        Criteria criteria = createCriteria(type, restrictions, false);

        if(!includePublished){
            criteria.add(Restrictions.eq("taxon.publish", true));
        }

        addLimitAndStart(criteria, limit, start);
        addOrder(criteria, orderHints);

        @SuppressWarnings("unchecked")
        List<S> result = criteria.list();
        defaultBeanInitializer.initializeAll(result, propertyPaths);
        return result;
    }

    @Override
    public long count(Class<? extends TaxonNode> type, List<Restriction<?>> restrictions) {
        return count(type, restrictions, INCLUDE_UNPUBLISHED);
    }


    @Override
    public long count(Class<? extends TaxonNode> type, List<Restriction<?>> restrictions, boolean includePublished) {

        Criteria criteria = createCriteria(type, restrictions, false);
        if(!includePublished){
            criteria.add(Restrictions.eq("taxon.publish", true));
        }
        criteria.setProjection(Projections.projectionList().add(Projections.rowCount()));
        return (Long) criteria.uniqueResult();
    }

    @Override
    public long countTaxonNodeAgentRelations(UUID taxonUuid, UUID classificationUuid, UUID agentUuid, UUID rankUuid, UUID relTypeUuid) {

        StringBuilder hql = prepareListTaxonNodeAgentRelations(taxonUuid, classificationUuid, agentUuid, rankUuid, relTypeUuid, true);
        Query<Long> query =  getSession().createQuery(hql.toString(), Long.class);

        setParamsForListTaxonNodeAgentRelations(taxonUuid, classificationUuid, agentUuid, rankUuid, relTypeUuid, query);

        Long count = query.uniqueResult();

        return count;
    }

    /**
     * @param taxonUuid
     * @param classificationUuid
     * @param agentUuid
     * @param relTypeUuid TODO
     * @param doCount TODO
     * @param rankId
     *     limit to taxa having this rank, only applies if <code>taxonUuid = null</code>
     * @return
     */
    private StringBuilder prepareListTaxonNodeAgentRelations(UUID taxonUuid,
            UUID classificationUuid, UUID agentUuid, UUID rankUuid, UUID relTypeUuid,
            boolean doCount) {

        StringBuilder hql = new StringBuilder();

        String join_fetch_mode = doCount ? "JOIN" : "JOIN FETCH";

        if(doCount) {
            hql.append("SELECT COUNT(tnar)");
        } else {
            hql.append("SELECT tnar");
        }

        hql.append(" FROM TaxonNodeAgentRelation AS tnar ");
        if(taxonUuid != null) {
            // taxonUuid is search filter, do not fetch it
            hql.append(" JOIN tnar.taxonNode AS tn "
                    + "  JOIN tn.taxon AS t ");
        } else {
            hql.append(join_fetch_mode)
                .append(" tnar.taxonNode AS tn ")
                .append(join_fetch_mode).append(" tn.taxon AS t ");
            if(rankUuid != null) {
                hql.append(" join t.name as n ");
            }
        }
        hql.append(" JOIN tn.classification AS c ");
        if(agentUuid != null) {
            // agentUuid is search filter, do not fetch it
//            hql.append(" join tnar.agent as a ");
            hql.append(join_fetch_mode).append(" tnar.agent AS a ");
        } else {
            hql.append(join_fetch_mode).append(" tnar.agent AS a ");
        }

        hql.append(" WHERE (1 = 1) ");

        if(relTypeUuid != null) {
            hql.append(" AND tnar.type.uuid = :relTypeUuid ");
        }

        if(taxonUuid != null) {
            hql.append(" AND t.uuid = :taxonUuid ");
        } else {
            if(rankUuid != null) {
                hql.append(" AND n.rank.uuid = :rankUuid ");
            }
        }
        if(classificationUuid != null) {
            hql.append(" AND c.uuid = :classificationUuid ");
        }
        if(agentUuid != null) {
            hql.append(" AND a.uuid = :agentUuid ");
        }

        hql.append(" ORDER BY a.titleCache");
        return hql;
    }

    private void setParamsForListTaxonNodeAgentRelations(UUID taxonUuid, UUID classificationUuid, UUID agentUuid,
            UUID rankUuid, UUID relTypeUuid, Query<?> query) {

        if(taxonUuid != null) {
            query.setParameter("taxonUuid", taxonUuid);
        } else {
            if(rankUuid != null) {
                query.setParameter("rankUuid", rankUuid);
            }
        }
        if(classificationUuid != null) {
            query.setParameter("classificationUuid", classificationUuid);
        }
        if(agentUuid != null) {
            query.setParameter("agentUuid", agentUuid);
        }
        if(relTypeUuid != null) {
            query.setParameter("relTypeUuid", relTypeUuid);
        }
    }

    @Override
    public Map<TreeIndex, Integer> rankOrderIndexForTreeIndex(List<TreeIndex> treeIndexes,
            Integer minRankOrderIndex,
            Integer maxRankOrderIndex) {

        Map<TreeIndex, Integer> result = new HashMap<>();
        if (treeIndexes == null || treeIndexes.isEmpty()){
            return result;
        }

        String hql = " SELECT tn.treeIndex, r.orderIndex "
                + " FROM TaxonNode tn "
                + "     JOIN tn.taxon t "
                + "     JOIN t.name n "
                + "      JOIN n.rank r "
                + " WHERE tn.treeIndex IN (:treeIndexes) ";
        if (minRankOrderIndex != null){
            hql += " AND r.orderIndex <= :minOrderIndex";
        }
        if (maxRankOrderIndex != null){
            hql += " AND r.orderIndex >= :maxOrderIndex";
        }

        Query<Object[]> query =  getSession().createQuery(hql, Object[].class);
        query.setParameterList("treeIndexes", TreeIndex.toString(treeIndexes));
        if (minRankOrderIndex != null){
            query.setParameter("minOrderIndex", minRankOrderIndex);
        }
        if (maxRankOrderIndex != null){
            query.setParameter("maxOrderIndex", maxRankOrderIndex);
        }

        @SuppressWarnings("unchecked")
        List<Object[]> list = query.list();
        for (Object[] o : list){
            result.put(TreeIndex.NewInstance((String)o[0]), (Integer)o[1]);
        }
        return result;
    }

    @Override
    public Map<TreeIndex, UuidAndTitleCache<?>> taxonUuidsForTreeIndexes(Collection<TreeIndex> treeIndexes) {
        Map<TreeIndex, UuidAndTitleCache<?>> result = new HashMap<>();
        if (treeIndexes == null || treeIndexes.isEmpty()){
            return result;
        }

        String hql =
                  " SELECT tn.treeIndex, t.uuid, tnb.titleCache "
                + " FROM TaxonNode tn JOIN tn.taxon t Join t.name tnb "
                + " WHERE tn.treeIndex IN (:treeIndexes) ";
        Query<Object[]> query =  getSession().createQuery(hql, Object[].class);
        query.setParameterList("treeIndexes", TreeIndex.toString(treeIndexes));

        List<Object[]> list = query.list();
        for (Object[] o : list){
            result.put(TreeIndex.NewInstance((String)o[0]), new UuidAndTitleCache<>((UUID)o[1], null, (String)o[2]));
        }
        return result;
    }

    @Override
    public List<TaxonNodeDto> getParentTaxonNodeDtoForRank(
            Classification classification, Rank rank, TaxonBase<?> taxonBase) {

        Taxon taxon = null;
        if (taxonBase instanceof Taxon) {
            taxon = CdmBase.deproxy(taxonBase, Taxon.class);
        }else {
            taxon = CdmBase.deproxy(((Synonym)taxonBase).getAcceptedTaxon());
        }
        TaxonNode node = null;
        if (taxon != null) {
            node = taxon.getTaxonNode(classification);
        }
        List<TaxonNodeDto> result = new ArrayList<>();
        if (node != null) {
            String treeIndex = node.treeIndex();
            List<Integer> ancestorNodeIds = TreeIndex.NewInstance(treeIndex).parentNodeIds(false);


            CriteriaBuilder cb = getCriteriaBuilder();
            CriteriaQuery<TaxonNode> cq = cb.createQuery(TaxonNode.class);
            Root<TaxonNode> root = cq.from(TaxonNode.class);

            List<Predicate> predicates = new ArrayList<>();
            Join<TaxonNode, Taxon> taxonJoin = root.join("taxon");
            Join<Taxon, TaxonName> nameJoin = taxonJoin.join("name");

            predicates.add(predicateIn(root, "id", ancestorNodeIds));
            predicates.add(predicateEqual(cb, root, "classification", classification));
            predicates.add(predicateEqual(cb, nameJoin, "rank", rank));

            cq.select(root)
              .where(predicateAnd(cb, predicates));
            List<TaxonNode> list = getSession().createQuery(cq).getResultList();

            for (TaxonNode rankNode : list){
                TaxonNodeDto dto = new TaxonNodeDto(rankNode);
                result.add(dto);
            }
        }
        return result;
    }

    @Override
    public List<TaxonNodeDto> getParentTaxonNodeDtoForRank(
            Classification classification, Rank rank, TaxonName name) {

    	Set<TaxonBase> taxa = name.getTaxonBases();
    	List<TaxonNodeDto> result = new ArrayList<>();
    	for (TaxonBase<?> taxonBase:taxa) {
    	    List<TaxonNodeDto> tmpList = getParentTaxonNodeDtoForRank(classification, rank, taxonBase);
    	    for (TaxonNodeDto tmpDto : tmpList){
    	        boolean exists = false; //an equal method does not yet exist for TaxonNodeDto therefore this workaround
    	        for (TaxonNodeDto dto: result){
    	            if (dto.getTreeIndex().equals(tmpDto.getTreeIndex())){
    	                exists = true;
    	            }
    	        }
    	        if (!exists){
    	            result.add(tmpDto);
    	        }
    	    }
    	}
    	return result;
    }

    @Override
    public int countSecundumForSubtreeAcceptedTaxa(TreeIndex subTreeIndex, Reference newSec,
            boolean overwriteExisting, boolean includeSharedTaxa, boolean emptySecundumDetail) {
        String queryStr = forSubtreeAcceptedQueryStr(includeSharedTaxa, subTreeIndex, false, SelectMode.COUNT);
        if (!overwriteExisting){
            queryStr += " AND t.secSource.citation IS NULL ";
        }
        return countResult(queryStr);
    }

    private int countResult(String queryStr) {
        Query<Long> query = getSession().createQuery(queryStr, Long.class);
        return query.uniqueResult().intValue();
    }

    @Override
    public int countSecundumForSubtreeSynonyms(TreeIndex subTreeIndex, Reference newSec,
            boolean overwriteExisting, boolean includeSharedTaxa, boolean emptySecundumDetail) {
        String queryStr = forSubtreeSynonymQueryStr(includeSharedTaxa, subTreeIndex, false, SelectMode.COUNT);
        if (!overwriteExisting){
            queryStr += " AND syn.secSource.citation IS NULL ";
        }
        return countResult(queryStr);
    }

    @Override
    public int countSecundumForSubtreeRelations(TreeIndex subTreeIndex, Reference newSec,
            boolean overwriteExisting, boolean includeSharedTaxa, boolean emptySecundumDetail) {
        String queryStr = forSubtreeRelationQueryStr(includeSharedTaxa, overwriteExisting, subTreeIndex, SelectMode.COUNT);
        return countResult(queryStr);
    }

    //#3465
    @Override
    public Set<CdmBase> setSecundumForSubtreeAcceptedTaxa(TreeIndex subTreeIndex, Reference newSec,
            boolean overwriteExisting, boolean includeSharedTaxa, boolean emptyDetail, IProgressMonitor monitor) {
        //for some reason this does not work, maybe because the listeners are not activated,
        //but also the first taxon for some reason does not get updated in terms of secundum, but only by the update listener
//        String where = "SELECT t.id FROM TaxonNode tn JOIN tn.taxon t " +
//                " WHERE tn.treeIndex like '%s%%' ORDER BY t.id";
//        where = String.format(where, subTreeIndex.toString());
//        Query query1 = getSession().createQuery(where);
//        List l = query1.list();
//
//        String hql = "UPDATE Taxon SET sec = :newSec, publish=false WHERE id IN (" + where + ")";
//        Query query = getSession().createQuery(hql);
//        query.setParameter("newSec", newSec);
//        int n = query.executeUpdate();

        String queryStr = forSubtreeAcceptedQueryStr(includeSharedTaxa, subTreeIndex, false, SelectMode.ID);
        if (!overwriteExisting){
            queryStr += " AND t.secSource.citation IS NULL ";
        }
        return setSecundum(newSec, emptyDetail, queryStr, monitor);
    }

    @Override
    public Set<CdmBase> setSecundumForSubtreeSynonyms(TreeIndex subTreeIndex, Reference newSec,
            boolean overwriteExisting, boolean includeSharedTaxa, boolean emptyDetail, IProgressMonitor monitor) {

        String queryStr = forSubtreeSynonymQueryStr(includeSharedTaxa, subTreeIndex, false, SelectMode.ID);
        if (!overwriteExisting){
            queryStr += " AND syn.secSource.citation IS NULL ";
        }
        return setSecundum(newSec, emptyDetail, queryStr, monitor);
    }

    private <T extends TaxonBase<?>> Set<CdmBase> setSecundum(Reference newSec, boolean emptyDetail, String queryStr, IProgressMonitor monitor) {
        Set<CdmBase> result = new HashSet<>();
        Query<Integer> query = getSession().createQuery(queryStr, Integer.class);
        List<List<Integer>> partitionList = splitIdList(query.list(), DEFAULT_SET_SUBTREE_PARTITION_SIZE);
        for (List<Integer> taxonIdList : partitionList){
            @SuppressWarnings({ "unchecked", "rawtypes" })
            List<T> taxonList = (List)taxonDao.loadList(taxonIdList, null, null);
            for (T taxonBase : taxonList){
                if (taxonBase != null){
                    taxonBase = CdmBase.deproxy(taxonBase);
                    SecundumSource secSourceBefore = taxonBase.getSecSource();
                    Reference refBefore = taxonBase.getSec();
                    String refDetailBefore = taxonBase.getSecMicroReference();
                    if (newSec == null && taxonBase.getSec() !=null
                            || newSec != null && (taxonBase.getSec() == null || !newSec.equals(taxonBase.getSec()) )){
                        taxonBase.setSec(newSec);
                    }
                    if (emptyDetail){
                        if (taxonBase.getSecMicroReference() != null){
                            taxonBase.setSecMicroReference(null);
                        }
                    }
                    //compute updated objects
                    SecundumSource secSourceAfter = taxonBase.getSecSource();

                    if (!CdmUtils.nullSafeEqual(secSourceBefore, secSourceAfter)){
                        result.add(taxonBase);
                        //FIXME #9627 remove if fixed
                        result.add(taxonBase);
                        //EMXIF
                    }else if (secSourceBefore != null && secSourceBefore.equals(secSourceAfter)
                            && (!CdmUtils.nullSafeEqual(refBefore, secSourceAfter.getCitation())
                                 || !CdmUtils.nullSafeEqual(refDetailBefore, secSourceAfter.getCitationMicroReference()))
                            ){
                        result.add(secSourceBefore);
                        //FIXME #9627 remove if fixed
                        result.add(taxonBase);
                        //EMXIF
                    }

                    monitor.worked(1);
                    if (monitor.isCanceled()){
                        return result;
                    }
                }
            }
            commitAndRestartTransaction(newSec);
            monitor.worked(taxonIdList.size());
        }
        return result;
    }

    private void commitAndRestartTransaction(CdmBase... cdmBaseToUpdate) {
        getSession().getTransaction().commit();
        getSession().clear();
        getSession().beginTransaction();
        for (CdmBase cdmBase : cdmBaseToUpdate){
            if (cdmBase != null){
                getSession().update(cdmBase);
            }
        }
    }

    @Override
    public Set<CdmBase> setSecundumForSubtreeRelations(TreeIndex subTreeIndex, Reference newRef,
            Set<UUID> relationTypes,  boolean overwriteExisting, boolean includeSharedTaxa, boolean emptyDetail, IProgressMonitor monitor) {

        String queryStr = forSubtreeRelationQueryStr(includeSharedTaxa, overwriteExisting, subTreeIndex, SelectMode.ID);

        Set<CdmBase> result = new HashSet<>();
        Query<Integer> query = getSession().createQuery(queryStr, Integer.class);

        List<List<Integer>> partitionList = splitIdList(query.list(), DEFAULT_SET_SUBTREE_PARTITION_SIZE);
        for (List<Integer> relIdList : partitionList){
            List<TaxonRelationship> relList = taxonRelDao.loadList(relIdList, null, null);
            for (TaxonRelationship rel : relList){
                if (rel != null){
                    rel = CdmBase.deproxy(rel);

                    NamedSource sourceBefore = rel.getSource();
                    Reference refBefore = rel.getCitation();
                    String refDetailBefore = rel.getCitationMicroReference();
                    if (newRef == null && rel.getCitation() !=null
                            || newRef != null && (rel.getCitation() == null || !newRef.equals(rel.getCitation()) )){
                        rel.setCitation(newRef);
                    }
                    if (emptyDetail){
                        if (rel.getCitationMicroReference() != null){
                            rel.setCitationMicroReference(null);
                        }
                    }
                    //compute updated objects
                    NamedSource sourceAfter = rel.getSource();
                    if (!CdmUtils.nullSafeEqual(sourceBefore, sourceAfter)){
                        result.add(rel);
                        //FIXME #9627 remove if fixed
                        result.add(rel.getToTaxon());
                        //EMXIF

                    }else if (sourceBefore != null && sourceBefore.equals(sourceAfter)
                            && (!CdmUtils.nullSafeEqual(refBefore, sourceAfter.getCitation())
                                 || !CdmUtils.nullSafeEqual(refDetailBefore,sourceAfter.getCitationMicroReference()))
                            ){
                        result.add(sourceBefore);
                        //FIXME #9627 remove if fixed
                        result.add(rel.getToTaxon());
                        //EMXIF
                    }

                    monitor.worked(1);
                    if (monitor.isCanceled()){
                        return result;
                    }
                }
            }
            commitAndRestartTransaction();
            monitor.worked(relList.size());
        }

        return result;
    }

    private List<List<Integer>> splitIdList(List<Integer> idList, Integer size){
        List<List<Integer>> result = new ArrayList<>();
        for (int i = 0; (i*size)<idList.size(); i++) {
            int upper = Math.min((i+1)*size, idList.size());
            result.add(idList.subList(i*size, upper));
        }
        return result;
    }

    @Override
    public int countPublishForSubtreeAcceptedTaxa(TreeIndex subTreeIndex, boolean publish, boolean includeSharedTaxa, boolean includeHybrids) {
        String queryStr = forSubtreeAcceptedQueryStr(includeSharedTaxa, subTreeIndex, !includeHybrids, SelectMode.COUNT);
        queryStr += " AND t.publish != :publish ";
        Query<Long> query = getSession().createQuery(queryStr, Long.class);
        query.setParameter("publish", publish);
        return query.uniqueResult().intValue();
    }

    @Override
    public int countPublishForSubtreeSynonyms(TreeIndex subTreeIndex, boolean publish, boolean includeSharedTaxa, boolean includeHybrids) {
        String queryStr = forSubtreeSynonymQueryStr(includeSharedTaxa, subTreeIndex, !includeHybrids, SelectMode.COUNT);
        queryStr += " AND syn.publish != :publish ";
        Query<Long> query = getSession().createQuery(queryStr, Long.class);
        query.setParameter("publish", publish);
        return query.uniqueResult().intValue();
    }

    @Override
    public Set<TaxonBase> setPublishForSubtreeAcceptedTaxa(TreeIndex subTreeIndex, boolean publish,
            boolean includeSharedTaxa, boolean includeHybrids, IProgressMonitor monitor) {
        String queryStr = forSubtreeAcceptedQueryStr(includeSharedTaxa, subTreeIndex, !includeHybrids, SelectMode.ID);
        queryStr += " AND t.publish != :publish ";
        return setPublish(publish, queryStr, null, monitor);
    }

    @Override
    public Set<TaxonBase> setPublishForSubtreeSynonyms(TreeIndex subTreeIndex, boolean publish,
            boolean includeSharedTaxa, boolean includeHybrids, IProgressMonitor monitor) {
        String queryStr = forSubtreeSynonymQueryStr(includeSharedTaxa, subTreeIndex, !includeHybrids, SelectMode.ID);
        queryStr += " AND syn.publish != :publish ";
        return setPublish(publish, queryStr, null, monitor);
    }

    @Override
    public int countPublishForSubtreeRelatedTaxa(TreeIndex subTreeIndex, boolean publish, boolean includeSharedTaxa, boolean includeHybrids) {
        String queryStr = forSubtreeRelatedTaxaQueryStr(includeSharedTaxa, subTreeIndex, !includeHybrids, SelectMode.COUNT);
        queryStr += " AND relTax.publish != :publish ";
        Query<Long> query = getSession().createQuery(queryStr, Long.class);
        query.setParameter("publish", publish);
        return query.uniqueResult().intValue();
    }

    @Override
    public Set<TaxonBase> setPublishForSubtreeRelatedTaxa(TreeIndex subTreeIndex, boolean publish,
            Set<UUID> relationTypes, boolean includeSharedTaxa, boolean includeHybrids,
            IProgressMonitor monitor) {
        String queryStr = forSubtreeRelatedTaxaQueryStr(includeSharedTaxa, subTreeIndex, !includeHybrids, SelectMode.ID);
        queryStr += " AND relTax.publish != :publish ";
        queryStr += " AND rel.type.uuid IN (:relTypeUuid)";
        return setPublish(publish, queryStr, relationTypes, monitor);
    }

    private <T extends TaxonBase<?>> Set<T> setPublish(boolean publish, String queryStr, Set<UUID> relTypeUuids, IProgressMonitor monitor) {
        Set<T> result = new HashSet<>();
        Query<Integer> query = getSession().createQuery(queryStr, Integer.class);
        query.setParameter("publish", publish);
        if (relTypeUuids != null && !relTypeUuids.isEmpty()){
            query.setParameterList("relTypeUuid", relTypeUuids);
        }

        List<List<Integer>> partitionList = splitIdList(query.list(), DEFAULT_SET_SUBTREE_PARTITION_SIZE);
        for (List<Integer> taxonIdList : partitionList){
            @SuppressWarnings({ "unchecked", "rawtypes" })
            List<T> taxonList = (List)taxonDao.loadList(taxonIdList, null, null);
            for (T taxonBase : taxonList){
                if (taxonBase != null){
                    if (taxonBase.isPublish() != publish){  //to be on the save side
                        taxonBase.setPublish(publish);
                        result.add(CdmBase.deproxy(taxonBase));
                    }
                    monitor.worked(1);
                    if (monitor.isCanceled()){
                        return result;
                    }
                }
            }
            commitAndRestartTransaction();
        }
        return result;
    }

    private String forSubtreeSynonymQueryStr(boolean includeSharedTaxa, TreeIndex subTreeIndex, boolean excludeHybrids, SelectMode mode) {
        String queryStr = "SELECT " + mode.hql("syn")
                + " FROM TaxonNode tn "
                + "   JOIN tn.taxon t "
                + "   JOIN t.synonyms syn  "
                + "   LEFT JOIN syn.name n "
                + "   LEFT JOIN syn.secSource ss ";
        String whereStr = " tn.treeIndex LIKE '%1$s%%' ";
        if (!includeSharedTaxa){
            whereStr += " AND NOT EXISTS ("
                    + "FROM TaxonNode tn2 WHERE tn2.taxon = t AND tn2.treeIndex not like '%1$s%%')  ";
        }
        whereStr = handleExcludeHybrids(whereStr, excludeHybrids, "syn");
        queryStr += " WHERE " + String.format(whereStr, subTreeIndex.toString());

        return queryStr;
    }

    private String handleExcludeHybrids(String whereStr, boolean excludeHybrids, String t) {
        if(excludeHybrids){

            String hybridWhere =  " AND (n is NULL OR "
                    + " (n.monomHybrid=0 AND n.binomHybrid=0 "
                    + "   AND n.trinomHybrid=0 AND n.hybridFormula=0 )) ";

            whereStr += hybridWhere; //String.format(hybridWhere, t);
        }
        return whereStr;
    }

    private String forSubtreeRelatedTaxaQueryStr(boolean includeSharedTaxa, TreeIndex subTreeIndex,
            boolean excludeHybrids, SelectMode mode) {
        String queryStr = "SELECT " + mode.hql("relTax")
                + " FROM TaxonNode tn "
                + "   JOIN tn.taxon t "
                + "   JOIN t.relationsToThisTaxon rel"
                + "   JOIN rel.relatedFrom relTax "
                + "   LEFT JOIN relTax.name n ";
        String whereStr =" tn.treeIndex LIKE '%1$s%%' ";
        if (!includeSharedTaxa){
            //toTaxon should only be used in the given subtree
            whereStr += " AND NOT EXISTS ("
                    + "FROM TaxonNode tn2 WHERE tn2.taxon = t AND tn2.treeIndex not like '%1$s%%')  ";
            //from taxon should not be used in another classification
            whereStr += " AND NOT EXISTS ("
                    + "FROM TaxonNode tn3 WHERE tn3.taxon = relTax AND tn3.treeIndex not like '%1$s%%')  ";
            //fromTaxon should not be related as e.g. pro parte synonym or misapplication to
            //another taxon which is not part of the subtree
            //TODO and has not the publish state
            whereStr += " AND NOT EXISTS ("
                    + "FROM TaxonNode tn4 JOIN tn4.taxon t2 JOIN t2.relationsToThisTaxon rel2  "
                    + "   WHERE rel2.relatedFrom = relTax AND tn4.treeIndex not like '%1$s%%' "
                    + "         AND tn4.taxon.publish != :publish ) ";
        }
        whereStr = handleExcludeHybrids(whereStr, excludeHybrids, "relTax");
        queryStr += " WHERE " + String.format(whereStr, subTreeIndex.toString());

        return queryStr;
    }

    private String forSubtreeRelationQueryStr(boolean includeSharedTaxa, boolean overwriteExisting,
            TreeIndex subTreeIndex, SelectMode mode) {

        String queryStr = "SELECT " + mode.hql("rel")
                + " FROM TaxonNode tn "
                + "   JOIN tn.taxon t "
                + "   JOIN t.relationsToThisTaxon rel "
                + "   LEFT JOIN rel.source src ";
        String whereStr =" tn.treeIndex LIKE '%1$s%%' ";
        if (!includeSharedTaxa){
            //toTaxon should only be used in the given subtree
            whereStr += " AND NOT EXISTS ("
                    + "FROM TaxonNode tn2 WHERE tn2.taxon = t AND tn2.treeIndex not like '%1$s%%')  ";
        }
        queryStr += " WHERE " + String.format(whereStr, subTreeIndex.toString());
        if (!overwriteExisting){
            queryStr += " AND (rel.source IS NULL OR src.citation IS NULL) ";
        }

        return queryStr;
    }

    private enum SelectMode{
        COUNT(" count(*) "),
        ID ("id "),
        UUID("uuid "),
        FULL("");
        private String hql;
        SelectMode(String hql){
            this.hql = hql;
        }
        public String hql(String prefix){
            switch (this){
            case ID:
            case UUID:
                return CdmUtils.Nz(prefix)+"." + hql;
            case FULL:
                return CdmUtils.Nz(prefix) + hql;
            case COUNT:
            default: return hql;
            }

        }
    }

    private String forSubtreeAcceptedQueryStr(boolean includeSharedTaxa, TreeIndex subTreeIndex, boolean excludeHybrids, SelectMode mode) {
        String queryStr = "SELECT " + mode.hql("t")
                + " FROM TaxonNode tn "
                + "   JOIN tn.taxon t "
                + "   LEFT JOIN t.name n "
                + "   LEFT JOIN t.secSource ss ";
        String whereStr = " tn.treeIndex like '%1$s%%' ";
        if (!includeSharedTaxa){
            whereStr += " AND NOT EXISTS ("
                    + "FROM TaxonNode tn2 WHERE tn2.taxon = t AND tn2.treeIndex not like '%1$s%%')  ";
        }
        whereStr = handleExcludeHybrids(whereStr, excludeHybrids, "t");
        queryStr += " WHERE " + String.format(whereStr, subTreeIndex.toString());

        return queryStr;
    }

    @Override
    public List<UuidAndTitleCache<TaxonNode>> getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(Classification classification, Integer limit, String pattern, boolean searchForClassifications, boolean includeDoubtful) {

         Query<SortableTaxonNodeQueryResult> query = createQueryForUuidAndTitleCache(limit, classification.getUuid(), pattern, includeDoubtful);
         List<SortableTaxonNodeQueryResult> result = query.list();


         if (searchForClassifications){
             String queryString = "SELECT new " + SortableTaxonNodeQueryResult.class.getName() + "("
                     + " node.uuid, node.id, node.classification.titleCache, parent.uuid"
                     + ") "
                     + " FROM TaxonNode AS node "
                     + " LEFT OUTER JOIN node.parent as parent"
                     + " WHERE node.classification.id = " + classification.getId() +
                          " AND node.taxon IS NULL";
             if (pattern != null){
                 if (pattern.equals("?")){
                     limit = null;
                 } else{
                     if (!pattern.endsWith("*")){
                         pattern += "%";
                     }
                     pattern = pattern.replace("*", "%");
                     pattern = pattern.replace("?", "%");
                     queryString = queryString + " AND node.classification.titleCache LIKE (:pattern) " ;
                 }
             }
             query = getSession().createQuery(queryString, SortableTaxonNodeQueryResult.class);

             if (limit != null){
                 query.setMaxResults(limit);
             }

             if (pattern != null && !pattern.equals("?")){
                 query.setParameter("pattern", pattern);
             }

             List<SortableTaxonNodeQueryResult> resultClassifications = query.list();

             result.addAll(resultClassifications);
         }

         if(result.size() == 0){
             return null;
         }else{
             List<UuidAndTitleCache<TaxonNode>> list = new ArrayList<>(result.size());
             Collections.sort(result, new SortableTaxonNodeQueryResultComparator());
             for (SortableTaxonNodeQueryResult resultDTO : result){
                 list.add(new UuidAndTitleCache<>(TaxonNode.class, resultDTO.getTaxonNodeUuid(), resultDTO.getTaxonNodeId(), resultDTO.getTaxonTitleCache()));
             }
             return list;
         }
    }

    @Override
    public List<TaxonNodeDto> getTaxonNodeDto(Integer limit, String pattern, UUID classificationUuid) {

        String queryString = getTaxonNodeDtoQuery();
        queryString += "  INNER JOIN tn.classification AS cls " + " WHERE t.titleCache LIKE :pattern ";

        if(classificationUuid != null){
            queryString += "AND cls.uuid = :classificationUuid";
        }

        Query<SortableTaxonNodeQueryResult> query =  getSession().createQuery(queryString, SortableTaxonNodeQueryResult.class);

        query.setParameter("pattern", pattern.toLowerCase()+"%");
        if(classificationUuid != null){
            query.setParameter("classificationUuid", classificationUuid);
        }

        List<SortableTaxonNodeQueryResult> result = query.list();
        List<TaxonNodeDto> list = SortableTaxonNodeQueryResult.toTaxonNodeDtoList(result);

        return list;
    }

    @Override
    public TaxonNodeDto getTaxonNodeDto(UUID nodeUuid) {

        String queryString = getTaxonNodeDtoQuery();
        queryString += " WHERE tn.uuid = :uuid ";
        Query<SortableTaxonNodeQueryResult> query =  getSession().createQuery(queryString, SortableTaxonNodeQueryResult.class);
        query.setParameter("uuid", nodeUuid);

        List<SortableTaxonNodeQueryResult> result = query.list();
        List<TaxonNodeDto> list = SortableTaxonNodeQueryResult.toTaxonNodeDtoList(result);
        if (list.isEmpty()) {
        	return null;
        }
        return list.get(0);
    }

    private String getTaxonNodeDtoQuery() {


	        String queryString = "SELECT new " + SortableTaxonNodeQueryResult.class.getName() + "("
                + "tn.uuid, tn.id, tn.treeIndex, t.uuid, t.titleCache, name.titleCache, rank, p.uuid, index(tn), cl.uuid,  t.publish, tn.status, note, tn.countChildren, sec.uuid "
                + ") "
                + " FROM TaxonNode p "
                + "   INNER JOIN p.childNodes AS tn"
                + "   INNER JOIN tn.taxon AS t "
                + "   INNER JOIN t.name AS name "
                + "   INNER JOIN tn.classification AS cl "
                + "   LEFT OUTER JOIN t.secSource as secSource "
                + "   LEFT OUTER JOIN secSource.citation as sec "
                + "	  LEFT OUTER JOIN tn.placementNote as note "
                + "   LEFT OUTER JOIN name.rank AS rank ";
        return queryString;
    }

    private String getTaxonNodeDtoWithoutSecQuery() {

        String queryString = "SELECT new " + SortableTaxonNodeWithoutSecQueryResult.class.getName() + "("
            + "tn.uuid, tn.id, tn.treeIndex, t.uuid, t.titleCache, name.titleCache, rank, p.uuid, index(tn), cl.uuid,  t.publish, tn.status, note, tn.countChildren "
            + ") "
            + " FROM TaxonNode p "
            + "   INNER JOIN p.childNodes AS tn"
            + "   INNER JOIN tn.taxon AS t "
            + "   INNER JOIN t.name AS name "
            + "   INNER JOIN tn.classification AS cl "
            + "   LEFT OUTER JOIN tn.placementNote as note "
            + "   LEFT OUTER JOIN name.rank AS rank ";
    return queryString;
}

    public String getTaxonNodeDtoQueryWithoutParent() {
        String queryString = "SELECT new " + SortableTaxonNodeQueryResult.class.getName() + "("
		+	"tn.uuid, tn.id, t.uuid, t.titleCache, tn.treeIndex"// rank "
            + ") "
            + " FROM TaxonNode tn "
            + "   LEFT JOIN tn.taxon AS t "     ;

        return queryString;
    }



    @Override
    public List<TaxonNodeDto> getTaxonNodeDtos(List<UUID> nodeUuids) {
        String queryString = getTaxonNodeDtoQuery();
        queryString = queryString + " WHERE tn.uuid IN (:uuid) ";

        Query<SortableTaxonNodeQueryResult> query =  getSession().createQuery(queryString, SortableTaxonNodeQueryResult.class);
        query.setParameterList("uuid", nodeUuids);

        List<SortableTaxonNodeQueryResult> result = query.list();

        List<TaxonNodeDto> list = SortableTaxonNodeQueryResult.toTaxonNodeDtoList(result);

        return list;
    }

    @Override
    public List<TaxonNodeDto> getTaxonNodeDtosWithoutParent(List<UUID> nodeUuids) {
        String queryString = getTaxonNodeDtoQueryWithoutParent();
        queryString = queryString + " WHERE tn.uuid IN (:uuid) ";

        Query<SortableTaxonNodeQueryResult> query =  getSession().createQuery(queryString, SortableTaxonNodeQueryResult.class);
        query.setParameterList("uuid", nodeUuids);

        List<SortableTaxonNodeQueryResult> result = query.list();

        List<TaxonNodeDto> list = SortableTaxonNodeQueryResult.toTaxonNodeDtoList(result);

        return list;
    }
    @Override
    public List<TaxonNodeDto> getTaxonNodeDtosFromTaxon(UUID taxonUuid, String subTreeIndex) {
    	String queryString = getTaxonNodeDtoQuery();
        queryString += " WHERE t.uuid = :uuid ";
        if (subTreeIndex != null) {
        	subTreeIndex += "%";
        	queryString += " AND tn.treeIndex like :subTreeIndex ";
        }
        Query<SortableTaxonNodeQueryResult> query =  getSession().createQuery(queryString, SortableTaxonNodeQueryResult.class);
        query.setParameter("uuid", taxonUuid);
        if (subTreeIndex != null) {
        	query.setParameter("subTreeIndex", subTreeIndex);
        }

        List<SortableTaxonNodeQueryResult> result = query.list();
        List<TaxonNodeDto> list = SortableTaxonNodeQueryResult.toTaxonNodeDtoList(result);
        if (list.isEmpty()) {
        	return null;
        }
        return list;
    }

    @Override
    public List<TaxonNodeDto> getTaxonNodeForTaxonInClassificationDto(UUID taxonUUID, UUID classificationUuid) {
        String queryString = getTaxonNodeDtoQuery();
        queryString = queryString + "   INNER JOIN tn.classification AS cls "  + " WHERE t.uuid = :uuid ";

        if(classificationUuid != null){
            queryString += "AND cls.uuid = :classificationUuid";
        }

        Query<SortableTaxonNodeQueryResult> query =  getSession().createQuery(queryString, SortableTaxonNodeQueryResult.class);
        query.setParameter("uuid", taxonUUID);

        if(classificationUuid != null){
            query.setParameter("classificationUuid", classificationUuid);
        }

        List<SortableTaxonNodeQueryResult> result = query.list();
        List<TaxonNodeDto> list = SortableTaxonNodeQueryResult.toTaxonNodeDtoList(result);
        return list;
    }

    @Override
    public List<TaxonNodeDto> getUuidAndTitleCache(Integer limit, String pattern, UUID classificationUuid) {
        return getUuidAndTitleCache(limit, pattern, classificationUuid, false);
    }

    @Override
    public List<UuidAndTitleCache<TaxonNode>> getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(
            Classification classification, Integer limit, String pattern, boolean searchForClassifications) {
        return getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(classification, limit, pattern, searchForClassifications, false);
    }


}