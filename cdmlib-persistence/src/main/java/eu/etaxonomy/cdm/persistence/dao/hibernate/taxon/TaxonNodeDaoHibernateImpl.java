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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
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
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeAgentRelation;
import eu.etaxonomy.cdm.model.taxon.UuidAndTitleCacheTaxonComparator;
import eu.etaxonomy.cdm.persistence.dao.common.Restriction;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.AnnotatableDaoImpl;
import eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeDao;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.mueller
 * @since 16.06.2009
 */
@Repository
@Qualifier("taxonNodeDaoHibernateImpl")
public class TaxonNodeDaoHibernateImpl extends AnnotatableDaoImpl<TaxonNode>
		implements ITaxonNodeDao {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TaxonNodeDaoHibernateImpl.class);

	@Autowired
	private ITaxonDao taxonDao;
	@Autowired
	private IClassificationDao classificationDao;

	public TaxonNodeDaoHibernateImpl() {
		super(TaxonNode.class);
	}
	@Override
	public UUID delete(TaxonNode persistentObject, boolean deleteChildren){
		Taxon taxon = persistentObject.getTaxon();
		taxon = HibernateProxyHelper.deproxy(taxon);

		/*Session session = this.getSession();
		Query query = session.createQuery("from TaxonNode t where t.taxon = :taxon");
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

        Query query =  getSession().createQuery(queryString);
        query.setParameter("parentId", parent.getUuid());

        @SuppressWarnings("unchecked")
        List<Object[]> result = query.list();

        List<TaxonNodeDto> list = new ArrayList<>();
        for(Object[] object : result){
            list.add(new TaxonNodeDto((UUID) object[0],(Integer) object[1], (String) object[2]));
        }
        return list;
    }

    @Override
    public List<TaxonNodeDto> listChildNodesAsTaxonNodeDto(TaxonNodeDto parent) {
        String queryString =
                 " SELECT tn "
               + " FROM TaxonNode tn "
               + "    INNER JOIN tn.taxon AS t "
               + " WHERE tn.parent.uuid = :parentId";
        Query query =  getSession().createQuery(queryString);
        query.setParameter("parentId", parent.getUuid());

        @SuppressWarnings("unchecked")
        List<TaxonNode> result = query.list();

        List<TaxonNodeDto> list = new ArrayList<>();
        for(TaxonNode object : result){
            list.add(new TaxonNodeDto(object));
        }
        return list;
    }

    @Override
    public List<TaxonNodeDto> getUuidAndTitleCache(Integer limit, String pattern, UUID classificationUuid) {
        String queryString = "SELECT tn.uuid, tn.id, t.titleCache, t.name.rank "
                + " FROM TaxonNode tn "
        		+ "   INNER JOIN tn.taxon AS t "
        		+ "   INNER JOIN tn.classification AS cls "
        		+ "WHERE t.titleCache LIKE :pattern ";
        if(classificationUuid != null){
        	queryString += "AND cls.uuid = :classificationUuid";
        }
        Query query =  getSession().createQuery(queryString);

        query.setParameter("pattern", pattern.toLowerCase()+"%");
        query.setParameter("classificationUuid", classificationUuid);


        @SuppressWarnings("unchecked")
        List<Object[]> result = query.list();
        Collections.sort(result, new UuidAndTitleCacheTaxonComparator());

        List<TaxonNodeDto> list = new ArrayList<>();
        for(Object[] object : result){
            list.add(new TaxonNodeDto((UUID) object[0],(Integer) object[1], (String) object[2]));
        }
        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TaxonNodeDto getParentUuidAndTitleCache(TaxonNodeDto child) {
        String queryString = ""
                + " SELECT tn.parent.uuid, tn.parent.id, tn.parent.taxon.titleCache, "
                + "                  tn.parent.classification.titleCache "
                + " FROM TaxonNode tn"
                + "    LEFT OUTER JOIN tn.parent.taxon"
                + " WHERE tn.id = :childId";
        Query query =  getSession().createQuery(queryString);
        query.setParameter("childId", child.getId());
        List<TaxonNodeDto> list = new ArrayList<>();

        @SuppressWarnings("unchecked")
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
            boolean recursive, boolean includeUnpublished, List<String> propertyPaths){

        if (recursive == true){
    		Criteria crit = childrenOfCriteria(node, includeUnpublished);

    		if(pageSize != null) {
                crit.setMaxResults(pageSize);
                if(pageIndex != null) {
                    crit.setFirstResult(pageIndex * pageSize);
                } else {
                    crit.setFirstResult(0);
                }
            }
    		@SuppressWarnings("unchecked")
            List<TaxonNode> results = crit.list();
    		results.remove(node);
    		defaultBeanInitializer.initializeAll(results, propertyPaths);
    		return results;
    	}else{
    		return classificationDao.listChildrenOf(node.getTaxon(), node.getClassification(), null,
    		       includeUnpublished, pageSize, pageIndex, propertyPaths);
    	}

    }

    @Override
	public Long countChildrenOf(TaxonNode node, Classification classification,
			boolean recursive, boolean includeUnpublished) {

		if (recursive == true){
			Criteria crit = childrenOfCriteria(node, includeUnpublished);
    		crit.setProjection(Projections.rowCount());
    		return ((Integer)crit.uniqueResult().hashCode()).longValue();
		}else{
			return classificationDao.countChildrenOf(
			        node.getTaxon(), classification, null, includeUnpublished);
		}
	}
    /**
     * @param node
     * @param includeUnpublished
     * @return
     */
    private Criteria childrenOfCriteria(TaxonNode node, boolean includeUnpublished) {
        Criteria crit = getSession().createCriteria(TaxonNode.class);
        crit.add( Restrictions.like("treeIndex", node.treeIndex()+ "%") );
        if (!includeUnpublished){
            crit.createCriteria("taxon").add( Restrictions.eq("publish", Boolean.TRUE));
        }
        return crit;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public List<TaxonNodeAgentRelation> listTaxonNodeAgentRelations(UUID taxonUuid, UUID classificationUuid,
            UUID agentUuid, UUID rankUuid, UUID relTypeUuid, Integer start, Integer limit,
            List<String> propertyPaths) {

        StringBuilder hql = prepareListTaxonNodeAgentRelations(taxonUuid, classificationUuid,
                agentUuid, rankUuid, relTypeUuid, false);

        Query query =  getSession().createQuery(hql.toString());

        if(limit != null) {
            query.setMaxResults(limit);
            if(start != null) {
                query.setFirstResult(start);
            }
        }

        setParamsForListTaxonNodeAgentRelations(taxonUuid, classificationUuid, agentUuid, rankUuid, relTypeUuid, query);

        @SuppressWarnings("unchecked")
        List<TaxonNodeAgentRelation> records = query.list();

        if(propertyPaths != null) {
            defaultBeanInitializer.initializeAll(records, propertyPaths);
        }
        return records;
    }

    @Override
    public <S extends TaxonNode> List<S> list(Class<S> type, List<Restriction<?>> restrictions, Integer limit,
            Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        // TODO Auto-generated method stub
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


    /**
     * {@inheritDoc}
     */
    @Override
    public long countTaxonNodeAgentRelations(UUID taxonUuid, UUID classificationUuid, UUID agentUuid, UUID rankUuid, UUID relTypeUuid) {

        StringBuilder hql = prepareListTaxonNodeAgentRelations(taxonUuid, classificationUuid, agentUuid, rankUuid, relTypeUuid, true);
        Query query =  getSession().createQuery(hql.toString());

        setParamsForListTaxonNodeAgentRelations(taxonUuid, classificationUuid, agentUuid, rankUuid, relTypeUuid, query);

        Long count = Long.parseLong(query.uniqueResult().toString());

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
    private StringBuilder prepareListTaxonNodeAgentRelations(UUID taxonUuid, UUID classificationUuid, UUID agentUuid, UUID rankUuid, UUID relTypeUuid, boolean doCount) {

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
    /**
     * @param taxonUuid
     * @param classificationUuid
     * @param agentUuid
     * @param relTypeUuid TODO
     * @param query
     * @param rankId TODO
     */
    private void setParamsForListTaxonNodeAgentRelations(UUID taxonUuid, UUID classificationUuid, UUID agentUuid,
            UUID rankUuid, UUID relTypeUuid, Query query) {

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

        Query query =  getSession().createQuery(hql);
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
        Query query =  getSession().createQuery(hql);
        query.setParameterList("treeIndexes", TreeIndex.toString(treeIndexes));

        @SuppressWarnings("unchecked")
        List<Object[]> list = query.list();
        for (Object[] o : list){
            result.put(TreeIndex.NewInstance((String)o[0]), new UuidAndTitleCache<>((UUID)o[1], null, (String)o[2]));
        }
        return result;
    }

    @Override
    public TaxonNodeDto getParentTaxonNodeDtoForRank(Classification classification, Rank rank, TaxonName name) {

    	Set<TaxonBase> taxa = name.getTaxonBases();
    	TaxonNode node = null;
    	String treeIndex = null;
    	Taxon taxon = null;
    	for (TaxonBase taxonBase:taxa) {
    		if (taxonBase instanceof Taxon) {
    			taxon = HibernateProxyHelper.deproxy(taxonBase, Taxon.class);
    		}else {
    			taxon = HibernateProxyHelper.deproxy(((Synonym)taxonBase).getAcceptedTaxon(), Taxon.class);
    		}
    		if (taxon != null) {
    			node = taxon.getTaxonNode(classification);
	    		if (node != null) {
	    			break;
	    		}
    		}
    	}
    	if (node != null) {
    		treeIndex = node.treeIndex();
    	}
    	Criteria nodeCrit = getSession().createCriteria(TaxonNode.class);
    	Criteria taxonCrit = nodeCrit.createCriteria("taxon");
    	Criteria nameCrit = taxonCrit.createCriteria("name");
    	nodeCrit.add(Restrictions.eq("classification", classification));
    	nameCrit.add(Restrictions.eq("rank", rank));

    	 ProjectionList projList = Projections.projectionList();
         projList.add(Projections.property("treeIndex"));
         projList.add(Projections.property("uuid"));
         nodeCrit.setProjection(projList);
        @SuppressWarnings("unchecked")
        List<Object[]> list = nodeCrit.list();
        UUID uuid = null;
        if (list.size() > 0) {
        	for (Object o: list) {
        		 Object[] objectArray = (Object[]) o;
                 uuid = (UUID)objectArray[1];
                 String treeIndexParent = (String) objectArray[0];
                 try {
                 if (treeIndex.startsWith(treeIndexParent)) {
                	 node = load(uuid);
                	 break;
                 }
                 }catch(NullPointerException e) {
                	 System.err.println("Parent: " + treeIndexParent + " treeIndex: " + treeIndex + " taxon: " + taxon != null?taxon.getTitleCache(): "");
                 }
        	}
        	if (node != null) {
        		TaxonNodeDto dto = new TaxonNodeDto(node);
        		return dto;
        	}

        }

        return null;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public int countSecundumForSubtreeAcceptedTaxa(TreeIndex subTreeIndex, Reference newSec,
            boolean overwriteExisting, boolean includeSharedTaxa, boolean emptySecundumDetail) {
        String queryStr = forSubtreeAcceptedQueryStr(includeSharedTaxa, subTreeIndex, false, SelectMode.COUNT);
        if (!overwriteExisting){
            queryStr += " AND t.sec IS NULL ";
        }
        return countResult(queryStr);
    }
    /**
     * @param queryStr
     * @return
     */
    private int countResult(String queryStr) {
        Query query = getSession().createQuery(queryStr);
        return ((Long)query.uniqueResult()).intValue();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public int countSecundumForSubtreeSynonyms(TreeIndex subTreeIndex, Reference newSec,
            boolean overwriteExisting, boolean includeSharedTaxa, boolean emptySecundumDetail) {
        String queryStr = forSubtreeSynonymQueryStr(includeSharedTaxa, subTreeIndex, false, SelectMode.COUNT);
        if (!overwriteExisting){
            queryStr += " AND syn.sec IS NULL ";
        }
        return countResult(queryStr);
    }


    /**
     * {@inheritDoc}
     */
    //#3465
    @Override
    public Set<TaxonBase> setSecundumForSubtreeAcceptedTaxa(TreeIndex subTreeIndex, Reference newSec,
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
            queryStr += " AND t.sec IS NULL ";
        }
        return setSecundum(newSec, emptyDetail, queryStr, monitor);

    }

    @Override
    public Set<TaxonBase> setSecundumForSubtreeSynonyms(TreeIndex subTreeIndex, Reference newSec,
            boolean overwriteExisting, boolean includeSharedTaxa, boolean emptyDetail, IProgressMonitor monitor) {

        String queryStr = forSubtreeSynonymQueryStr(includeSharedTaxa, subTreeIndex, false, SelectMode.ID);
        if (!overwriteExisting){
            queryStr += " AND syn.sec IS NULL ";
        }
        return setSecundum(newSec, emptyDetail, queryStr, monitor);
    }
    /**
     * @param newSec
     * @param emptyDetail
     * @param queryStr
     * @param monitor
     * @return
     */
    @SuppressWarnings("unchecked")
    private <T extends TaxonBase<?>> Set<T> setSecundum(Reference newSec, boolean emptyDetail, String queryStr, IProgressMonitor monitor) {
        Set<T> result = new HashSet<>();
        Query query = getSession().createQuery(queryStr);
        List<List<Integer>> partitionList = splitIdList(query.list(), DEFAULT_PARTITION_SIZE);
        for (List<Integer> taxonIdList : partitionList){
            List<TaxonBase> taxonList = taxonDao.loadList(taxonIdList, null, null);
            for (TaxonBase taxonBase : taxonList){
                if (taxonBase != null){
                    taxonBase = taxonDao.load(taxonBase.getUuid());
                    taxonBase.setSec(newSec);
                    if (emptyDetail){
                        taxonBase.setSecMicroReference(null);
                    }
                    result.add((T)CdmBase.deproxy(taxonBase));
                    monitor.worked(1);
                    if (monitor.isCanceled()){
                        return result;
                    }
                }
            }
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
        System.out.println(queryStr);
        Query query = getSession().createQuery(queryStr);
        query.setBoolean("publish", publish);
        return ((Long)query.uniqueResult()).intValue();
    }

    @Override
    public int countPublishForSubtreeSynonyms(TreeIndex subTreeIndex, boolean publish, boolean includeSharedTaxa, boolean includeHybrids) {
        String queryStr = forSubtreeSynonymQueryStr(includeSharedTaxa, subTreeIndex, !includeHybrids, SelectMode.COUNT);
        queryStr += " AND syn.publish != :publish ";
        Query query = getSession().createQuery(queryStr);
        query.setBoolean("publish", publish);
        return ((Long)query.uniqueResult()).intValue();
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
        Query query = getSession().createQuery(queryStr);
        query.setBoolean("publish", publish);
        return ((Long)query.uniqueResult()).intValue();
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

    private static final int DEFAULT_PARTITION_SIZE = 100;
    /**
     * @param publish
     * @param queryStr
     * @return
     */
    private <T extends TaxonBase<?>> Set<T> setPublish(boolean publish, String queryStr, Set<UUID> relTypeUuids, IProgressMonitor monitor) {
        Set<T> result = new HashSet<>();
        Query query = getSession().createQuery(queryStr);
        query.setBoolean("publish", publish);
        if (relTypeUuids != null && !relTypeUuids.isEmpty()){
            query.setParameterList("relTypeUuid", relTypeUuids);
        }
        @SuppressWarnings("unchecked")
        List<List<Integer>> partitionList = splitIdList(query.list(), DEFAULT_PARTITION_SIZE);
        for (List<Integer> taxonIdList : partitionList){
            List<TaxonBase> taxonList = taxonDao.loadList(taxonIdList, null, null);
            for (TaxonBase taxonBase : taxonList){
                if (taxonBase != null){
                    taxonBase.setPublish(publish);
                    result.add((T)CdmBase.deproxy(taxonBase));
                    monitor.worked(1);
                    if (monitor.isCanceled()){
                        return result;
                    }
                }
            }
        }
        return result;
    }


    /**
     * @param includeSharedTaxa
     * @param subTreeIndex
     * @param includeHybrids
     * @param includeSharedTaxa2
     * @return
     */
    private String forSubtreeSynonymQueryStr(boolean includeSharedTaxa, TreeIndex subTreeIndex, boolean excludeHybrids, SelectMode mode) {
        String queryStr = "SELECT " + mode.hql("syn")
                + " FROM TaxonNode tn "
                + "   JOIN tn.taxon t "
                + "   JOIN t.synonyms syn  LEFT JOIN syn.name n ";
        String whereStr = " tn.treeIndex LIKE '%1$s%%' ";
        if (!includeSharedTaxa){
            whereStr += " AND NOT EXISTS ("
                    + "FROM TaxonNode tn2 WHERE tn2.taxon = t AND tn2.treeIndex not like '%1$s%%')  ";
        }
        whereStr = handleExcludeHybrids(whereStr, excludeHybrids, "syn");
        queryStr += " WHERE " + String.format(whereStr, subTreeIndex.toString());

        return queryStr;
    }

    /**
     * @param queryStr
     * @param excludeHybrids
     * @return
     */
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
                + "   JOIN rel.relatedFrom relTax  LEFT JOIN relTax.name n ";
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
                + " FROM TaxonNode tn JOIN tn.taxon t LEFT JOIN t.name n ";
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
    public List<UuidAndTitleCache<TaxonNode>> getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(Classification classification, Integer limit, String pattern, boolean searchForClassifications) {
        int classificationId = classification.getId();

         String queryString =
                   " SELECT nodes.uuid, nodes.id,  taxon.titleCache, taxon.name.rank " +
                   " FROM TaxonNode AS nodes "
                   + "    JOIN nodes.taxon as taxon " +
                   " WHERE nodes.classification.id = " + classificationId ;
         if (pattern != null){
             if (pattern.equals("?")){
                 limit = null;
             } else{
                 if (!pattern.endsWith("*")){
                     pattern += "%";
                 }
                 pattern = pattern.replace("*", "%");
                 pattern = pattern.replace("?", "%");
                 queryString = queryString + " AND taxon.titleCache LIKE (:pattern) " ;
             }
         }

         Query query = getSession().createQuery(queryString);

         if (limit != null){
             query.setMaxResults(limit);
         }

         if (pattern != null && !pattern.equals("?")){
             query.setParameter("pattern", pattern);
         }
         @SuppressWarnings("unchecked")
         List<Object[]> result = query.list();

         if (searchForClassifications){
             queryString = ""
                     + " SELECT nodes.uuid, nodes.id,  nodes.classification.titleCache, NULLIF(1,1) "
                     + " FROM TaxonNode AS nodes "
                     + " WHERE nodes.classification.id = " + classificationId +
                          " AND nodes.taxon IS NULL";
             if (pattern != null){
                 if (pattern.equals("?")){
                     limit = null;
                 } else{
                     if (!pattern.endsWith("*")){
                         pattern += "%";
                     }
                     pattern = pattern.replace("*", "%");
                     pattern = pattern.replace("?", "%");
                     queryString = queryString + " AND nodes.classification.titleCache LIKE (:pattern) " ;
                 }
             }
             query = getSession().createQuery(queryString);

             if (limit != null){
                 query.setMaxResults(limit);
             }

             if (pattern != null && !pattern.equals("?")){
                 query.setParameter("pattern", pattern);
             }
             @SuppressWarnings("unchecked")
             List<Object[]> resultClassifications = query.list();

             result.addAll(resultClassifications);
         }

         if(result.size() == 0){
             return null;
         }else{
             List<UuidAndTitleCache<TaxonNode>> list = new ArrayList<>(result.size());
             if (result.iterator().next().length == 4){
                 Collections.sort(result, new UuidAndTitleCacheTaxonComparator());
             }
             for (Object object : result){
                 Object[] objectArray = (Object[]) object;
                 UUID uuid = (UUID)objectArray[0];
                 Integer id = (Integer) objectArray[1];
                 String titleCache = (String) objectArray[2];

                 list.add(new UuidAndTitleCache<>(TaxonNode.class, uuid, id, titleCache));
             }

             return list;
         }
    }


}
