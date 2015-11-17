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

import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeAgentRelation;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.AnnotatableDaoImpl;
import eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeDao;

/**
 * @author a.mueller
 * @created 16.06.2009
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
		taxon = HibernateProxyHelper.deproxy(taxon, Taxon.class);

		/*Session session = this.getSession();
		Query query = session.createQuery("from TaxonNode t where t.taxon = :taxon");
		query.setParameter("taxon", taxon);
		List result = query.list();*/
		if (taxon != null){
			Set<TaxonNode> nodes = taxon.getTaxonNodes();

			if (nodes.size()==1){

				TaxonNode node = nodes.iterator().next();
				node = HibernateProxyHelper.deproxy(node, TaxonNode.class);

				taxon.removeTaxonNode(node, deleteChildren);
				taxonDao.delete(taxon);
			}
		}
		//persistentObject.delete();

		super.delete(persistentObject);



		//taxon = (Taxon)taxonDao.findByUuid(taxon.getUuid());
		return persistentObject.getUuid();
	}

	@Override
	public List<TaxonNode> getTaxonOfAcceptedTaxaByClassification(Classification classification, Integer start, Integer end) {
		int classificationId = classification.getId();
		String limit = "";
		if(start !=null && end != null){
		    limit = "LIMIT "+start+"," +end;
		}
		//FIXME write test
        String queryString = "SELECT DISTINCT nodes.*,taxa.titleCache FROM TaxonNode AS nodes LEFT JOIN TaxonBase AS taxa ON nodes.taxon_id = taxa.id WHERE taxa.DTYPE = 'Taxon' AND nodes.classification_id = " + classificationId + " ORDER BY taxa.titleCache " + limit;
        List<TaxonNode> result  = getSession().createSQLQuery(queryString).addEntity(TaxonNode.class).list();

       return result;


	}
    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeDao#countTaxonOfAcceptedTaxaByClassification(eu.etaxonomy.cdm.model.taxon.Classification)
     */
    @Override
    public int countTaxonOfAcceptedTaxaByClassification(Classification classification){
        int classificationId = classification.getId();
        //FIXME write test
        String queryString = "SELECT DISTINCT COUNT('nodes.*') FROM TaxonNode AS nodes LEFT JOIN TaxonBase AS taxa ON nodes.taxon_id = taxa.id WHERE taxa.DTYPE = 'Taxon' AND nodes.classification_id = " + classificationId;
         List<BigInteger> result = getSession().createSQLQuery(queryString).list();
         return result.get(0).intValue ();
    }

    @Override
    public List<TaxonNode> listChildrenOf(TaxonNode node, Integer pageSize, Integer pageIndex, List<String> propertyPaths, boolean recursive){
    	if (recursive == true){
    		Criteria crit = getSession().createCriteria(TaxonNode.class);
    		crit.add( Restrictions.like("treeIndex", node.treeIndex()+ "%") );
    		if(pageSize != null) {
                crit.setMaxResults(pageSize);
                if(pageIndex != null) {
                    crit.setFirstResult(pageIndex * pageSize);
                } else {
                    crit.setFirstResult(0);
                }
            }
    		List<TaxonNode> results = crit.list();
    		results.remove(node);
    		defaultBeanInitializer.initializeAll(results, propertyPaths);
    		return results;
    	}else{
    		return classificationDao.listChildrenOf(node.getTaxon(), node.getClassification(), pageSize, pageIndex, propertyPaths);
    	}

    }

    @Override
	public Long countChildrenOf(TaxonNode node, Classification classification,
			boolean recursive) {

		if (recursive == true){
			Criteria crit = getSession().createCriteria(TaxonNode.class);
    		crit.add( Restrictions.like("treeIndex", node.treeIndex()+ "%") );
    		crit.setProjection(Projections.rowCount());
    		return ((Integer)crit.uniqueResult().hashCode()).longValue();
		}else{
			return classificationDao.countChildrenOf(node.getTaxon(), classification);
		}
	}
    /**
     * {@inheritDoc}
     */
    @Override
    public List<TaxonNodeAgentRelation> listTaxonNodeAgentRelations(UUID taxonUuid, UUID classificationUuid,
            UUID agentUuid, UUID rankUuid, Integer start, Integer limit, List<String> propertyPaths) {


        StringBuilder hql = prepareListTaxonNodeAgentRelations(taxonUuid, classificationUuid, agentUuid, rankUuid, false);

        Query query =  getSession().createQuery(hql.toString());

        if(limit != null) {
            query.setMaxResults(limit);
            if(start != null) {
                query.setFirstResult(start);
            }
        }

        setParamsForListTaxonNodeAgentRelations(taxonUuid, classificationUuid, agentUuid, rankUuid, query);

        List<TaxonNodeAgentRelation> records = query.list();

        if(propertyPaths != null) {
            defaultBeanInitializer.initializeAll(records, propertyPaths);
        }
        return records;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public long countTaxonNodeAgentRelations(UUID taxonUuid, UUID classificationUuid, UUID agentUuid, UUID rankUuid) {

        StringBuilder hql = prepareListTaxonNodeAgentRelations(taxonUuid, classificationUuid, agentUuid, rankUuid, true);
        Query query =  getSession().createQuery(hql.toString());

        setParamsForListTaxonNodeAgentRelations(taxonUuid, classificationUuid, agentUuid, rankUuid, query);

        Long count = Long.parseLong(query.uniqueResult().toString());

        return count;
    }
    /**
     * @param taxonUuid
     * @param classificationUuid
     * @param agentUuid
     * @param rankId
     *     limit to taxa having this rank, only applies if <code>taxonUuid = null</code>
     * @param doCount TODO
     * @return
     */
    private StringBuilder prepareListTaxonNodeAgentRelations(UUID taxonUuid, UUID classificationUuid, UUID agentUuid, UUID rankUuid, boolean doCount) {

        StringBuilder hql = new StringBuilder();

        String join_fetch_mode = doCount ? "join" : "join fetch";

        if(doCount) {
            hql.append("select count(tnar)");
        } else {
            hql.append("select tnar");
        }

        hql.append(" from TaxonNodeAgentRelation as tnar ");
        if(taxonUuid != null) {
            // taxonUuid is search filter, do not fetch it
            hql.append(" join tnar.taxonNode as tn join tn.taxon as t ");
        } else {
            hql.append(join_fetch_mode).append(" tnar.taxonNode as tn ").append(join_fetch_mode).append(" tn.taxon as t ");
            if(rankUuid != null) {
                hql.append(" join t.name as n ");
            }
        }
        hql.append(" join tn.classification as c ");
        if(agentUuid != null) {
            // agentUuid is search filter, do not fetch it
            hql.append(" join tnar.agent as a ");
        } else {
            hql.append(join_fetch_mode).append(" tnar.agent as a ");
        }

        hql.append(" where 1 = 1 ");

        if(taxonUuid != null) {
            hql.append(" and t.uuid = :taxonUuid ");
        } else {
            if(rankUuid != null) {
                hql.append(" and n.rank.uuid = :rankUuid ");
            }
        }
        if(classificationUuid != null) {
            hql.append(" and c.uuid = :classificationUuid ");
        }
        if(agentUuid != null) {
            hql.append(" and a.uuid = :agentUuid ");
        }

        hql.append(" order by a.titleCache");
        return hql;
    }
    /**
     * @param taxonUuid
     * @param classificationUuid
     * @param agentUuid
     * @param rankId TODO
     * @param query
     */
    private void setParamsForListTaxonNodeAgentRelations(UUID taxonUuid, UUID classificationUuid, UUID agentUuid,
            UUID rankUuid, Query query) {

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
    }

}
