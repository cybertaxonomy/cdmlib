/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.term;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.VersionableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.term.ITermNodeDao;
import eu.etaxonomy.cdm.persistence.dto.SortableTaxonNodeQueryResult;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.babadshanjan
 * @since 09.09.2008
 */
@Repository
public class TermNodeDaoImpl
        extends VersionableDaoBase<TermNode>
        implements ITermNodeDao {

	public TermNodeDaoImpl() {
		super(TermNode.class);
	}

	//TODO still needs to be tested
	@Override
    public List<TermNode> list(TermType termType, Integer limit, Integer start, List<OrderHint> orderHints,
            List<String> propertyPaths){

        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<TermNode> cq = cb.createQuery(TermNode.class);
        Root<TermNode> root = cq.from(TermNode.class);
        List<Predicate> predicates = new ArrayList<>();

        if (termType != null){
            Set<TermType> types = termType.getGeneralizationOf(true);
            types.add(termType);
            predicates.add(predicateIn(root, "termType", types));
        }

        cq.select(root)
          .where(predicateAnd(cb, predicates))
          .orderBy(ordersFrom(cb, root, orderHints));

        List<TermNode> results = addLimitAndStart(getSession().createQuery(cq), limit, start)
                .getResultList() ;

        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
	}

	@Override
	public List<UuidAndTitleCache<TermNode>> getUuidAndTitleCache(Integer limit, String pattern){
	    Session session = getSession();
        Query<SortableTaxonNodeQueryResult> query = session.createQuery(
                "SELECT new " + SortableTaxonNodeQueryResult.class.getName()
                + "       (uuid, id, titleCache) "
                + " FROM TermNode node "
                + " JOIN DefinedTerm term ON node.term = term "
                + (pattern!=null?" WHERE term.titleCache LIKE :pattern":""),
                SortableTaxonNodeQueryResult.class);
        if(pattern!=null){
            pattern = pattern.replace("*", "%");
            pattern = pattern.replace("?", "_");
            pattern = pattern + "%";
            query.setParameter("pattern", pattern);
        }
        if (limit != null){
           query.setMaxResults(limit);
        }
        return getUuidAndTitleCache(query);
	}

	protected List<UuidAndTitleCache<TermNode>> getUuidAndTitleCache(Query<SortableTaxonNodeQueryResult> query){
        List<UuidAndTitleCache<TermNode>> list = new ArrayList<>();
        List<SortableTaxonNodeQueryResult> result = query.list();

        for(SortableTaxonNodeQueryResult stnqr : result){
            list.add(new UuidAndTitleCache<>(stnqr.getTaxonNodeUuid(),stnqr.getTaxonNodeId(), stnqr.getTaxonTitleCache()));
        }
        return list;
    }
}