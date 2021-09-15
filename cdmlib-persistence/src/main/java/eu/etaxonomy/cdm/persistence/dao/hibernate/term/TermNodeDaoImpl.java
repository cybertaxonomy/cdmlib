/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.term;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
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

	@Override
    public List<TermNode> list() {
		Criteria crit = getSession().createCriteria(type);
		@SuppressWarnings("unchecked")
        List<TermNode> result = crit.list();
		return result;
	}

	//TODO still needs to be tested
	@Override
    public List<TermNode> list(TermType termType, Integer limit, Integer start, List<OrderHint> orderHints,
            List<String> propertyPaths){

	    Criteria criteria = getSession().createCriteria(type);
        if (termType != null){
            Set<TermType> types = termType.getGeneralizationOf(true);
            types.add(termType);
            criteria.add(Restrictions.in("termType", types));
        }

        addLimitAndStart(criteria, limit, start);

        addOrder(criteria, orderHints);

        @SuppressWarnings("unchecked")
        List<TermNode> results = criteria.list();

        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
	}

	@Override
	public List<UuidAndTitleCache<TermNode>> getUuidAndTitleCache(Integer limit, String pattern){
	    Session session = getSession();
        Query query = session.createQuery(

                "SELECT new " + SortableTaxonNodeQueryResult.class.getName() + "("
                + " uuid, id, titleCache "
                + ") "
                + " FROM TermNode node "
                + " JOIN DefinedTerm term ON node.term = term "
                + (pattern!=null?" WHERE term.titleCache LIKE :pattern":""));
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
	 protected List<UuidAndTitleCache<TermNode>> getUuidAndTitleCache(Query query){
	        List<UuidAndTitleCache<TermNode>> list = new ArrayList<>();
	        List<Object> result = query.list();

	        for(Object obj : result){
	            if (obj instanceof SortableTaxonNodeQueryResult) {
	                SortableTaxonNodeQueryResult stnqr = (SortableTaxonNodeQueryResult) obj;
	                list.add(new UuidAndTitleCache<>(stnqr.getTaxonNodeUuid(),stnqr.getTaxonNodeId(), stnqr.getTaxonTitleCache()));
	            }else{
	                Object[] object = (Object[])obj;
	                list.add(new UuidAndTitleCache<>((UUID) object[0],(Integer) object[1], (String) object[2]));
	            }
	        }
	        return list;
	    }

}
