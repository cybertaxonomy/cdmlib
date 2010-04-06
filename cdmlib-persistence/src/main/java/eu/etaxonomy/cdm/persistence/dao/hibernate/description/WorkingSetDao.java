package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.WorkingSet;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.description.IWorkingSetDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.AnnotatableDaoImpl;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

@Repository 
@Qualifier("workingSetDaoImpl")
public class WorkingSetDao extends AnnotatableDaoImpl<WorkingSet> implements IWorkingSetDao {

	public WorkingSetDao() {
		super(WorkingSet.class);
	}

	public Map<DescriptionBase, Set<DescriptionElementBase>> getDescriptionElements(WorkingSet workingSet, Set<Feature> features, Integer pageSize,	Integer pageNumber,	List<String> propertyPaths) {
		checkNotInPriorView("WorkingSetDao.getDescriptionElements(WorkingSet workingSet, Set<Feature> features, Integer pageSize,Integer pageNumber, List<OrderHint> orderHints,	List<String> propertyPaths)");
		Query query = getSession().createQuery("select description from WorkingSet workingSet join workingSet.descriptions description order by description.titleCache asc");
		
		if(pageSize != null) {
			query.setMaxResults(pageSize);
	        if(pageNumber != null) {
	        	query.setFirstResult(pageNumber * pageSize);
	        } else {
	        	query.setFirstResult(0);
	        }
	    }
		List<DescriptionBase> descriptions = (List<DescriptionBase>)query.list();
		Map<DescriptionBase, Set<DescriptionElementBase>> result = new HashMap<DescriptionBase, Set<DescriptionElementBase>>();
		for(DescriptionBase description : descriptions) {
			Criteria criteria = getSession().createCriteria(DescriptionElementBase.class);
			criteria.add(Restrictions.eq("inDescription", description));
			if(features != null && !features.isEmpty()) {
				criteria.add(Restrictions.in("feature", features));
			}
			
			List<DescriptionElementBase> r = (List<DescriptionElementBase>)criteria.list();
			defaultBeanInitializer.initializeAll(r, propertyPaths);
			result.put(description, new HashSet<DescriptionElementBase>(r));
		}
		return result;
	}
}
